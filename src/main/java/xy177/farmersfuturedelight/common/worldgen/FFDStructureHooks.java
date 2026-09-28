package xy177.farmersfuturedelight.common.worldgen;

import java.lang.reflect.Field;

import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenVillage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xy177.farmersfuturedelight.common.world.ChunkGeneratorExtended;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class FFDStructureHooks {
    private static final Logger LOGGER = LogManager.getLogger("FFD Structure Hooks");

    private FFDStructureHooks() {
    }

    public static void installModernGenerators(ChunkGeneratorOverworld generator, World world,
                                               ChunkGeneratorExtended extendedGenerator) {
        if (generator == null || !FFDHeightHooks.isExtended(world)) {
            return;
        }
        boolean villageInstalled = replaceGenerator(generator, MapGenVillage.class,
                new FFDModernVillageGenerator());
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

    private static boolean replaceGenerator(ChunkGeneratorOverworld generator, Class<?> type,
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
