package xy177.farmersfuturedelight.common.worldgen;

import com.google.gson.JsonObject;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.WoodlandMansion;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDVillagerTypes;

public final class FFDExplorationMaps {
    private static final Logger LOGGER = LogManager.getLogger("FFD Exploration Maps");
    private static final MethodHandle CAN_SPAWN = spawnMethod();
    private static final MethodHandle INITIALIZE = method("initializeStructureData", "func_143027_a", World.class);
    private static final MethodHandle PREPARE = method("recursiveGenerate", "func_180701_a", World.class,
            int.class, int.class, int.class, int.class, ChunkPrimer.class);
    private static final Field STARTS = startsField();

    private FFDExplorationMaps() {
    }

    @Nullable
    public static BlockPos find(WorldServer world, BlockPos origin, JsonObject rule) {
        IChunkGenerator generator = ObfuscationReflectionHelper.getPrivateValue(
                net.minecraft.world.gen.ChunkProviderServer.class, world.getChunkProvider(), "field_186029_c");
        if (generator instanceof ChunkGeneratorOverworld) {
            Boolean enabled = ObfuscationReflectionHelper.getPrivateValue(
                    ChunkGeneratorOverworld.class, (ChunkGeneratorOverworld) generator, "field_185996_o");
            if (!Boolean.TRUE.equals(enabled)) return null;
        } else if (generator.getClass() != ChunkGeneratorFlat.class) return null;
        int radius = rule.has("search_radius") ? rule.get("search_radius").getAsInt() : 50;
        boolean skip = !rule.has("skip_existing_chunks") || rule.get("skip_existing_chunks").getAsBoolean();
        String destination = rule.get("destination").getAsString();
        if (radius < 0 || radius > 100) return null;
        if ("#minecraft:on_woodland_mansion_maps".equals(destination)) {
            MapGenStructure selected = structure(generator, "field_191060_C", "Mansion");
            WoodlandMansion mansion = selected instanceof WoodlandMansion ? (WoodlandMansion) selected : null;
            if (mansion == null || mansion.getClass() != WoodlandMansion.class) return null;
            return lookup(world, origin, mansion, 80, 20, 10387319, true, radius, skip, null);
        }
        if ("#minecraft:on_ocean_monument_maps".equals(destination)) {
            MapGenStructure selected = structure(generator, "field_185980_B", "Monument");
            StructureOceanMonument monument = selected instanceof StructureOceanMonument ? (StructureOceanMonument) selected : null;
            if (monument == null || monument.getClass() != StructureOceanMonument.class) return null;
            Integer spacing = ObfuscationReflectionHelper.getPrivateValue(
                    StructureOceanMonument.class, monument, "field_175800_f");
            Integer separation = ObfuscationReflectionHelper.getPrivateValue(
                    StructureOceanMonument.class, monument, "field_175801_g");
            return lookup(world, origin, monument, spacing, separation, 10387313, true, radius, skip, null);
        }
        if ("#minecraft:on_swamp_hut_maps".equals(destination) || "#minecraft:on_jungle_pyramid_maps".equals(destination)) {
            MapGenStructure selected = structure(generator, "field_186007_z", "Temple");
            MapGenScatteredFeature temple = selected instanceof MapGenScatteredFeature ? (MapGenScatteredFeature) selected : null;
            if (temple == null || temple.getClass() != MapGenScatteredFeature.class
                    && temple.getClass() != FFDModernScatteredFeatureGenerator.class) return null;
            Integer spacing = ObfuscationReflectionHelper.getPrivateValue(
                    MapGenScatteredFeature.class, temple, "field_82669_g");
            String type = destination.contains("swamp") ? "swamp" : "jungle";
            return lookup(world, origin, temple, spacing, 8, 14357617, false, radius, skip, type);
        }
        String prefix = "#minecraft:on_";
        String suffix = "_village_maps";
        if (destination.startsWith(prefix) && destination.endsWith(suffix)) {
            String type = destination.substring(prefix.length(), destination.length() - suffix.length());
            if ("snowy".equals(type)) type = "snow";
            if (!"plains".equals(type) && !"desert".equals(type) && !"savanna".equals(type)
                    && !"taiga".equals(type) && !"snow".equals(type)) return null;
            MapGenStructure selected = structure(generator, "field_186005_x", "Village");
            MapGenVillage village = selected instanceof MapGenVillage ? (MapGenVillage) selected : null;
            if (village == null || village.getClass() != MapGenVillage.class
                    && village.getClass() != FFDModernVillageGenerator.class) return null;
            Integer spacing = village.getClass() == FFDModernVillageGenerator.class
                    ? Math.max(9, FFDConfig.modernVillageSpacing)
                    : ObfuscationReflectionHelper.getPrivateValue(MapGenVillage.class, village, "field_82665_g");
            return lookup(world, origin, village, spacing, 8, 10387312, false, radius, skip, type);
        }
        return null;
    }

    @Nullable
    private static MapGenStructure structure(IChunkGenerator generator, String overworldField, String flatName) {
        if (generator instanceof ChunkGeneratorOverworld) {
            return ObfuscationReflectionHelper.getPrivateValue(ChunkGeneratorOverworld.class,
                    (ChunkGeneratorOverworld) generator, overworldField);
        }
        java.util.Map<String, MapGenStructure> structures = ObfuscationReflectionHelper.getPrivateValue(
                ChunkGeneratorFlat.class, (ChunkGeneratorFlat) generator, "field_82696_f");
        return structures.get(flatName);
    }

    @Nullable
    private static BlockPos lookup(WorldServer world, BlockPos origin, MapGenStructure structure,
                                    int spacing, int separation, int salt, boolean triangular,
                                    int radius, boolean skip, @Nullable String type) {
        if (CAN_SPAWN == null || INITIALIZE == null || PREPARE == null || STARTS == null
                || spacing <= separation || separation < 0) return null;
        Lookup lookup = new Lookup(world, structure, type);
        return lookup.find(origin, spacing, separation, salt, triangular, radius, skip);
    }

    @Nullable
    private static MethodHandle spawnMethod() {
        for (String name : new String[]{"canSpawnStructureAtCoords", "func_75047_a"}) {
            try {
                Method method = MapGenStructure.class.getDeclaredMethod(name, int.class, int.class);
                method.setAccessible(true);
                return MethodHandles.lookup().unreflect(method).asType(MethodType.methodType(
                        boolean.class, MapGenStructure.class, int.class, int.class));
            } catch (ReflectiveOperationException | SecurityException ignored) {
            }
        }
        LOGGER.error("Cannot access native structure candidates; exploration-map offers will be skipped");
        return null;
    }

    @Nullable
    private static MethodHandle method(String mapped, String runtime, Class<?>... arguments) {
        for (String name : new String[]{mapped, runtime}) {
            try {
                Method method = MapGenStructure.class.getDeclaredMethod(name, arguments);
                method.setAccessible(true);
                return MethodHandles.lookup().unreflect(method);
            } catch (ReflectiveOperationException | SecurityException ignored) {
            }
        }
        LOGGER.error("Cannot access structure planning method {}; exploration-map offers will be skipped", mapped);
        return null;
    }

    @Nullable
    private static Field startsField() {
        try {
            for (Field field : MapGenStructure.class.getDeclaredFields()) {
                if (Long2ObjectMap.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return field;
                }
            }
        } catch (SecurityException exception) {
            LOGGER.error("Cannot access native structure starts", exception);
        }
        return null;
    }

    private static final class Lookup extends MapGenStructure {
        private final MapGenStructure source;
        private final String type;
        private final FFDStructureMapReferences references;
        private boolean skipKnown;

        private Lookup(World world, MapGenStructure source, @Nullable String type) {
            this.world = world;
            this.source = source;
            this.type = type;
            this.references = FFDStructureMapReferences.get(world);
            ObfuscationReflectionHelper.setPrivateValue(MapGenBase.class, source, world, "field_75039_c");
            this.rand = ObfuscationReflectionHelper.getPrivateValue(MapGenBase.class, source, "field_75038_b");
        }

        private BlockPos find(BlockPos origin, int spacing, int separation, int salt,
                              boolean triangular, int radius, boolean skip) {
            skipKnown = skip;
            try {
                INITIALIZE.invoke(source, world);
                if (skip) {
                    for (StructureStart start : starts().values()) {
                        if (start.isSizeableStructure() && start.getBoundingBox().isVecInside(origin)
                                && matches(start.getChunkPosX(), start.getChunkPosZ()))
                            references.claim(source.getStructureName(), start);
                    }
                }
            } catch (Throwable exception) {
                throw new IllegalStateException("Cannot load exploration-map structure starts", exception);
            }
            BlockPos result = findNearestStructurePosBySpacing(world, this, origin, spacing, separation,
                    salt, triangular, radius, false);
            if (result != null && skip) {
                references.claim(source.getStructureName(), starts().get(ChunkPos.asLong(result.getX() >> 4, result.getZ() >> 4)));
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        private Long2ObjectMap<StructureStart> starts() {
            try {
                return (Long2ObjectMap<StructureStart>) STARTS.get(source);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Cannot read exploration-map structure starts", exception);
            }
        }

        private boolean matches(int chunkX, int chunkZ) {
            return type == null || type.equals(FFDVillagerTypes.byBiome(world.getBiomeProvider().getBiome(
                    new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8))));
        }

        @Override
        protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
            if (!matches(chunkX, chunkZ) || skipKnown && references.contains(source.getStructureName(), chunkX, chunkZ))
                return false;
            try {
                MapGenBase.setupChunkSeed(world.getSeed(), rand, chunkX, chunkZ);
                rand.nextInt();
                if (!(boolean) CAN_SPAWN.invokeExact(source, chunkX, chunkZ)) return false;
                MapGenBase.setupChunkSeed(world.getSeed(), rand, chunkX, chunkZ);
                PREPARE.invoke(source, world, chunkX, chunkZ, chunkX, chunkZ, (ChunkPrimer) null);
            } catch (Throwable exception) {
                throw new IllegalStateException("Cannot query structure candidate", exception);
            }
            StructureStart start = starts().get(ChunkPos.asLong(chunkX, chunkZ));
            return start != null && start.isSizeableStructure();
        }

        @Override
        public String getStructureName() {
            return source.getStructureName();
        }

        @Override
        public BlockPos getNearestStructurePos(World world, BlockPos origin, boolean unexplored) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected StructureStart getStructureStart(int chunkX, int chunkZ) {
            throw new UnsupportedOperationException();
        }
    }
}
