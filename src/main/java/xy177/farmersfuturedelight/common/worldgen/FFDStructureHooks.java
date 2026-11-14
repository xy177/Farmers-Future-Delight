package xy177.farmersfuturedelight.common.worldgen;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xy177.farmersfuturedelight.common.world.ChunkGeneratorExtended;
import xy177.farmersfuturedelight.common.world.FFDWorldgenTerrain;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class FFDStructureHooks {
    private static final Logger LOGGER = LogManager.getLogger("FFD Structure Hooks");

    private FFDStructureHooks() {
    }

    private static final class Fields {
        private static final Field WORLD = ObfuscationReflectionHelper.findField(ChunkGeneratorOverworld.class, "field_185995_n");
        private static final Field HEIGHTS = ObfuscationReflectionHelper.findField(ChunkGeneratorOverworld.class, "field_185998_q");
        private static final Field SETTINGS = ObfuscationReflectionHelper.findField(ChunkGeneratorOverworld.class, "field_186000_s");
        private static final Field VILLAGES = ObfuscationReflectionHelper.findField(ChunkGeneratorOverworld.class, "field_186005_x");
    }

    private static Object field(Field field, ChunkGeneratorOverworld generator) {
        try {
            return field.get(generator);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot read overworld structure context", exception);
        }
    }

    static FFDModernVillageGenerator villages(ChunkGeneratorOverworld generator) {
        ChunkGeneratorSettings settings = (ChunkGeneratorSettings) field(Fields.SETTINGS, generator);
        if (settings == null || !settings.useVillages) return null;
        Object villages = field(Fields.VILLAGES, generator);
        return villages instanceof FFDModernVillageGenerator ? (FFDModernVillageGenerator) villages : null;
    }

    public static void applyLegacyTerrain(ChunkGeneratorOverworld generator, int chunkX, int chunkZ, ChunkPrimer primer) {
        if (generator.getClass() != ChunkGeneratorOverworld.class || FFDStructureTerrain.isSampling()) return;
        World world = (World) field(Fields.WORLD, generator);
        if (!FFDModernStructures.supported(world, generator)) return;
        double[] heights = (double[]) field(Fields.HEIGHTS, generator);
        double[] original = heights.clone();
        FFDStructureBeard beard;
        try {
            beard = FFDModernStructures.prepareTerrain(world, generator, chunkX, chunkZ);
        } finally {
            System.arraycopy(original, 0, heights, 0, heights.length);
        }
        if (beard.isEmpty()) return;
        ChunkGeneratorSettings settings = (ChunkGeneratorSettings) field(Fields.SETTINGS, generator);
        IBlockState fluid = (settings.useLavaOceans ? Blocks.LAVA : Blocks.WATER).getDefaultState();
        for (int gridX = 0; gridX < 4; gridX++) {
            for (int gridZ = 0; gridZ < 4; gridZ++) {
                int corner00 = (gridX * 5 + gridZ) * 33;
                int corner01 = corner00 + 33;
                int corner10 = corner00 + 165;
                int corner11 = corner10 + 33;
                for (int gridY = 0; gridY < 32; gridY++) {
                    double density00 = original[corner00 + gridY];
                    double density01 = original[corner01 + gridY];
                    double density10 = original[corner10 + gridY];
                    double density11 = original[corner11 + gridY];
                    double step00 = (original[corner00 + gridY + 1] - density00) * 0.125D;
                    double step01 = (original[corner01 + gridY + 1] - density01) * 0.125D;
                    double step10 = (original[corner10 + gridY + 1] - density10) * 0.125D;
                    double step11 = (original[corner11 + gridY + 1] - density11) * 0.125D;
                    for (int withinY = 0; withinY < 8; withinY++) {
                        double density0 = density00;
                        double density1 = density01;
                        double step0 = (density10 - density00) * 0.25D;
                        double step1 = (density11 - density01) * 0.25D;
                        int blockY = gridY * 8 + withinY;
                        for (int withinX = 0; withinX < 4; withinX++) {
                            int localX = gridX * 4 + withinX;
                            double stepZ = (density1 - density0) * 0.25D;
                            double density = density0 - stepZ;
                            for (int withinZ = 0; withinZ < 4; withinZ++) {
                                density += stepZ;
                                int localZ = gridZ * 4 + withinZ;
                                float adjustment = beard.sample(chunkX * 16 + localX, blockY, chunkZ * 16 + localZ);
                                if (adjustment == 0.0F) continue;
                                double normalized = MathHelper.clamp(density / 128.0D * 0.64D, -1.0D, 1.0D);
                                double squeezed = normalized / 2.0D - normalized * normalized * normalized / 24.0D;
                                boolean solid = squeezed + adjustment > 0.0D;
                                if (solid == (density > 0.0D)) continue;
                                primer.setBlockState(localX, blockY, localZ, solid ? Blocks.STONE.getDefaultState()
                                        : blockY < settings.seaLevel ? fluid : Blocks.AIR.getDefaultState());
                            }
                            density0 += step0;
                            density1 += step1;
                        }
                        density00 += step00;
                        density01 += step01;
                        density10 += step10;
                        density11 += step11;
                    }
                }
            }
        }
    }

    public static BlockPos locate(BlockPos original, World world, String name, BlockPos origin, boolean unexplored) {
        if (MapGenFFDRuinedPortal.matches(name)) return MapGenFFDRuinedPortal.locate(world, origin, unexplored);
        if (FFDNetherStructures.isBastion(name)) return FFDNetherStructures.locate(world, origin, unexplored);
        return FFDModernStructures.isOutpost(name) ? FFDModernStructures.locateOutpost(world, origin, unexplored) : original;
    }

    public static java.util.List<String> completeLocate(java.util.List<String> original, String[] arguments) {
        if (arguments.length != 1) return original;
        java.util.List<String> result = new java.util.ArrayList<>(original);
        for (String name : net.minecraft.command.CommandBase.getListOfStringsMatchingLastWord(
                arguments, "Pillager_Outpost", "Bastion_Remnant", "Ruined_Portal")) {
            if (!result.contains(name)) result.add(name);
        }
        return result;
    }

    public static boolean isInside(boolean original, World world, String name, BlockPos position) {
        if (MapGenFFDRuinedPortal.matches(name)) return MapGenFFDRuinedPortal.inside(world, position);
        if (FFDNetherStructures.isBastion(name)) return FFDNetherStructures.inside(world, position);
        return FFDModernStructures.isOutpost(name) ? FFDModernStructures.insideOutpost(world, position) : original;
    }

    public static void installModernGenerators(ChunkGeneratorOverworld generator, World world,
                                               ChunkGeneratorExtended extendedGenerator) {
        installModernGenerators(generator, world, (FFDWorldgenTerrain) extendedGenerator);
    }

    public static void installModernGenerators(ChunkGeneratorOverworld generator, World world,
                                               FFDWorldgenTerrain extendedGenerator) {
        if (generator == null || !FFDHeightHooks.isExtended(world)) {
            return;
        }
        boolean villageInstalled = installModernVillage(generator);
        boolean mineshaftInstalled = replaceGenerator(generator, MapGenMineshaft.class,
                new FFDModernMineshaftGenerator(extendedGenerator));
        boolean scatteredFeatureInstalled = replaceScatteredFeatureGenerator(generator);
        if (villageInstalled || mineshaftInstalled || scatteredFeatureInstalled) {
            LOGGER.info("Installed 26.3 structure placement for village spacing {}, mineshaft heights, and mountain biome structures",
                    xy177.farmersfuturedelight.common.FFDConfig.modernVillageSpacing);
        }
    }

    public static int estimateModernSurfaceHeight(World world, int x, int z) {
        return world == null ? 63 : world.getSeaLevel();
    }

    static boolean installModernVillage(IChunkGenerator generator) {
        if (Loader.isModLoaded("villagenames")) {
            LOGGER.info("Keeping Village Names village layout");
            return false;
        }
        if (generator instanceof ChunkGeneratorFlat) {
            Map<String, MapGenStructure> structures = ObfuscationReflectionHelper.getPrivateValue(
                    ChunkGeneratorFlat.class, (ChunkGeneratorFlat) generator, "field_82696_f");
            MapGenStructure village = structures.get("Village");
            if (village == null || village instanceof FFDModernVillageGenerator) return false;
            if (village.getClass() != MapGenVillage.class) {
                LOGGER.info("Keeping external village implementation {}", village.getClass().getName());
                return false;
            }
            FlatGeneratorInfo settings = ObfuscationReflectionHelper.getPrivateValue(
                    ChunkGeneratorFlat.class, (ChunkGeneratorFlat) generator, "field_82699_e");
            structures.put("Village", new FFDModernVillageGenerator(settings.getWorldFeatures().get("village")));
            return true;
        }
        return replaceGenerator(generator, MapGenVillage.class, new FFDModernVillageGenerator());
    }

    private static boolean replaceGenerator(IChunkGenerator generator, Class<?> type,
                                            Object replacement) {
        Class<?> current = generator.getClass();
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (!type.isAssignableFrom(field.getType())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object installed = field.get(generator);
                    if (installed != null && installed.getClass() == replacement.getClass()) return false;
                    if (installed != null && installed.getClass() != type) {
                        LOGGER.info("Keeping external {} implementation {} in ffd_cac",
                                type.getSimpleName(), installed.getClass().getName());
                        return false;
                    }
                    field.set(generator, replacement);
                    return true;
                } catch (IllegalAccessException | SecurityException exception) {
                    LOGGER.warn("Could not replace {} structure generator", type.getSimpleName(), exception);
                    return false;
                }
            }
            current = current.getSuperclass();
        }
        LOGGER.warn("Could not find {} structure generator field", type.getSimpleName());
        return false;
    }

    private static boolean replaceScatteredFeatureGenerator(ChunkGeneratorOverworld generator) {
        Class<?> current = generator.getClass();
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (!MapGenScatteredFeature.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object installed = field.get(generator);
                    if (installed != null && installed.getClass() != MapGenScatteredFeature.class) {
                        LOGGER.info("Keeping external {} implementation {} in ffd_cac",
                                MapGenScatteredFeature.class.getSimpleName(),
                                installed.getClass().getName());
                        return false;
                    }
                    field.set(generator, new FFDModernScatteredFeatureGenerator(
                            (MapGenScatteredFeature) installed));
                    return true;
                } catch (IllegalAccessException | SecurityException exception) {
                    LOGGER.warn("Could not replace {} structure generator",
                            MapGenScatteredFeature.class.getSimpleName(), exception);
                    return false;
                }
            }
            current = current.getSuperclass();
        }
        LOGGER.warn("Could not find {} structure generator field",
                MapGenScatteredFeature.class.getSimpleName());
        return false;
    }
}
