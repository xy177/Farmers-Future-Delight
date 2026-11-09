package xy177.farmersfuturedelight.common.worldgen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.IntBinaryOperator;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import xy177.farmersfuturedelight.common.world.ChunkGeneratorExtended;

final class FFDStructureTerrain {
    private static final String BOP_GENERATOR = "biomesoplenty.common.world.ChunkGeneratorOverworldBOP";
    private static final ThreadLocal<Boolean> SAMPLING = ThreadLocal.withInitial(() -> false);
    private static final ClassValue<Optional<BopTerrainAccess>> BOP_TERRAIN_ACCESS = new ClassValue<Optional<BopTerrainAccess>>() {
        @Override
        protected Optional<BopTerrainAccess> computeValue(Class<?> type) {
            if (!BOP_GENERATOR.equals(type.getName())) return Optional.empty();
            try {
                Method generateTerrain = type.getMethod("setChunkAirStoneWater", int.class, int.class, ChunkPrimer.class);
                Method replaceBiomeBlocks = type.getMethod("replaceBlocksForBiome", int.class, int.class,
                        ChunkPrimer.class, Biome[].class);
                Field random = type.getDeclaredField("rand");
                if (!Random.class.isAssignableFrom(random.getType())) return Optional.empty();
                random.setAccessible(true);
                return Optional.of(new BopTerrainAccess(generateTerrain, replaceBiomeBlocks, random));
            } catch (ReflectiveOperationException | SecurityException exception) {
                return Optional.empty();
            }
        }
    };
    private static final ClassValue<Optional<Field>> GENERATOR_DELEGATES = new ClassValue<Optional<Field>>() {
        @Override
        protected Optional<Field> computeValue(Class<?> type) {
            String fieldName;
            switch (type.getName()) {
                case "co.aikar.timings.TimedChunkGenerator":
                    fieldName = "timedGenerator";
                    break;
                case "org.bukkit.craftbukkit.v1_12_R1.generator.NormalChunkGenerator":
                    fieldName = "generator";
                    break;
                default:
                    return Optional.empty();
            }
            try {
                Field field = type.getDeclaredField(fieldName);
                if (!IChunkGenerator.class.isAssignableFrom(field.getType())) return Optional.empty();
                field.setAccessible(true);
                return Optional.of(field);
            } catch (ReflectiveOperationException | SecurityException exception) {
                return Optional.empty();
            }
        }
    };

    private FFDStructureTerrain() {
    }

    static boolean isSampling() {
        return SAMPLING.get();
    }

    static IntBinaryOperator heights(WorldServer world) {
        return heights(world, false);
    }

    static IntBinaryOperator projectedHeights(WorldServer world) {
        return heights(world, true);
    }

    static boolean supportsBiomesOPlenty(IChunkGenerator generator) {
        return generator != null && BOP_TERRAIN_ACCESS.get(generator.getClass()).isPresent();
    }

    private static IntBinaryOperator heights(WorldServer world, boolean includeStructures) {
        IChunkGenerator generator = generator(world);
        if (generator == null) return null;
        if (generator instanceof ChunkGeneratorExtended) {
            ChunkGeneratorExtended extended = (ChunkGeneratorExtended) generator;
            if (!includeStructures) return extended::structureSurfaceHeight;
            Map<Long, FFDStructureBeard> beards = new HashMap<>();
            Map<Long, Integer> surfaces = new HashMap<>();
            return (blockX, blockZ) -> surfaces.computeIfAbsent((long) blockX << 32 ^ blockZ & 0xffffffffL,
                    ignored -> extended.structureSurfaceHeight(blockX, blockZ,
                            beards.computeIfAbsent(ChunkPos.asLong(blockX >> 4, blockZ >> 4),
                                    chunk -> FFDModernStructures.prepareTerrain(world, extended, blockX >> 4, blockZ >> 4))));
        }
        Optional<BopTerrainAccess> bopAccess = BOP_TERRAIN_ACCESS.get(generator.getClass());
        if (bopAccess.isPresent()) {
            BopTerrainAccess access = bopAccess.get();
            Map<Long, int[]> cache = new HashMap<>();
            return (blockX, blockZ) -> {
                int chunkX = blockX >> 4;
                int chunkZ = blockZ >> 4;
                int[] heights = cache.computeIfAbsent(ChunkPos.asLong(chunkX, chunkZ),
                        ignored -> sample(access, generator, world, chunkX, chunkZ));
                return heights[(blockZ & 15) * 16 + (blockX & 15)];
            };
        }
        if (generator.getClass() != ChunkGeneratorOverworld.class) return null;
        ChunkGeneratorOverworld overworld = (ChunkGeneratorOverworld) generator;
        Map<Long, int[]> cache = new HashMap<>();
        return (blockX, blockZ) -> {
            int[] heights = cache.computeIfAbsent(ChunkPos.asLong(blockX >> 4, blockZ >> 4),
                    ignored -> sample(overworld, blockX >> 4, blockZ >> 4, includeStructures));
            return heights[(blockZ & 15) * 16 + (blockX & 15)];
        };
    }

    static IChunkGenerator generator(WorldServer world) {
        if (world == null || world.getChunkProvider() == null) return null;
        IChunkGenerator generator = ObfuscationReflectionHelper.getPrivateValue(net.minecraft.world.gen.ChunkProviderServer.class,
                world.getChunkProvider(), "field_186029_c");
        if (generator == null) return null;
        if (!GENERATOR_DELEGATES.get(generator.getClass()).isPresent()) return generator;
        Set<IChunkGenerator> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int depth = 0; depth < 16 && visited.add(generator); depth++) {
            Optional<Field> field = GENERATOR_DELEGATES.get(generator.getClass());
            if (!field.isPresent()) break;
            try {
                Object delegate = field.get().get(generator);
                if (!(delegate instanceof IChunkGenerator) || visited.contains(delegate)) break;
                generator = (IChunkGenerator) delegate;
            } catch (IllegalAccessException | SecurityException exception) {
                break;
            }
        }
        return generator;
    }

    private static int[] sample(ChunkGeneratorOverworld generator, int chunkX, int chunkZ, boolean includeStructures) {
        ChunkPrimer terrain = new ChunkPrimer();
        Biome[] previous = ObfuscationReflectionHelper.getPrivateValue(ChunkGeneratorOverworld.class, generator, "field_185981_C");
        ObfuscationReflectionHelper.setPrivateValue(ChunkGeneratorOverworld.class, generator, null, "field_185981_C");
        boolean sampling = SAMPLING.get();
        SAMPLING.set(!includeStructures);
        try {
            generator.setBlocksInChunk(chunkX, chunkZ, terrain);
        } finally {
            if (sampling) SAMPLING.set(true);
            else SAMPLING.remove();
            ObfuscationReflectionHelper.setPrivateValue(ChunkGeneratorOverworld.class, generator, previous, "field_185981_C");
        }
        return surfaceHeights(terrain);
    }

    private static int[] sample(BopTerrainAccess access, IChunkGenerator generator, WorldServer world,
                                int chunkX, int chunkZ) {
        ChunkPrimer terrain = new ChunkPrimer();
        Random previousRandom;
        try {
            previousRandom = (Random) access.random.get(generator);
            access.random.set(generator, new Random((long) chunkX * 341873128712L
                    + (long) chunkZ * 132897987541L));
        } catch (IllegalAccessException | SecurityException exception) {
            throw new IllegalStateException("Unable to sample Biomes O' Plenty terrain", exception);
        }
        try {
            access.generateTerrain.invoke(generator, chunkX, chunkZ, terrain);
            Biome[] biomes = world.getBiomeProvider().getBiomes(null, chunkX * 16, chunkZ * 16, 16, 16);
            access.replaceBiomeBlocks.invoke(generator, chunkX, chunkZ, terrain, biomes);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to sample Biomes O' Plenty terrain", exception);
        } finally {
            try {
                access.random.set(generator, previousRandom);
            } catch (IllegalAccessException | SecurityException exception) {
                throw new IllegalStateException("Unable to restore Biomes O' Plenty generator state", exception);
            }
        }
        return surfaceHeights(terrain);
    }

    private static int[] surfaceHeights(ChunkPrimer terrain) {
        int[] result = new int[256];
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                for (int height = 255; height >= 0; height--) {
                    if (terrain.getBlockState(localX, height, localZ).getMaterial() == Material.AIR) continue;
                    result[localZ * 16 + localX] = height + 1;
                    break;
                }
            }
        }
        return result;
    }

    private static final class BopTerrainAccess {
        private final Method generateTerrain;
        private final Method replaceBiomeBlocks;
        private final Field random;

        private BopTerrainAccess(Method generateTerrain, Method replaceBiomeBlocks, Field random) {
            this.generateTerrain = generateTerrain;
            this.replaceBiomeBlocks = replaceBiomeBlocks;
            this.random = random;
        }
    }
}
