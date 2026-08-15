package xy177.farmersfuturedelight.core;

import java.lang.reflect.Method;

import net.minecraft.launchwrapper.Launch;

public final class FFDCoreCompat {
    private static final String CAVE_BIOMES_HEIGHT_API =
            "net/celestiald/cavebiomes/api/WorldHeightAPI.class";
    private static final String DEPTHS_UPDATE_HEIGHT_API =
            "sayys/depthsupdate/core/HeightManager.class";
    private static final String CAVES_NOT_CLIFFS_CORE =
            "net/celestiald/cavesnotcliffs/core/CavesNotCliffsCorePlugin.class";
    private static final boolean CAVE_BIOMES_API_PRESENT = detect(CAVE_BIOMES_HEIGHT_API);
    private static final boolean DEPTHS_UPDATE_HEIGHT_CORE_PRESENT = detect(DEPTHS_UPDATE_HEIGHT_API);
    private static final boolean CAVES_NOT_CLIFFS_PRESENT = detect(CAVES_NOT_CLIFFS_CORE);

    private FFDCoreCompat() {
    }

    public static boolean isCaveBiomesApiPresent() {
        return CAVE_BIOMES_API_PRESENT;
    }

    public static boolean isDepthsUpdateHeightCorePresent() {
        return DEPTHS_UPDATE_HEIGHT_CORE_PRESENT;
    }

    public static boolean isCavesNotCliffsPresent() {
        return CAVES_NOT_CLIFFS_PRESENT;
    }

    public static void validateCaveBiomesHeightRange(int expectedMinY, int expectedMaxY) {
        if (!CAVE_BIOMES_API_PRESENT) {
            return;
        }
        try {
            Class<?> api = Class.forName(
                    "net.celestiald.cavebiomes.api.WorldHeightAPI", true, Launch.classLoader);
            Method getMinY = api.getMethod("getMinY");
            Method getMaxY = api.getMethod("getMaxY");
            int actualMinY = ((Number) getMinY.invoke(null)).intValue();
            int actualMaxY = ((Number) getMaxY.invoke(null)).intValue();
            if (actualMinY != expectedMinY || actualMaxY != expectedMaxY) {
                throw new IllegalStateException("Farmer's Future Delight requires Cave Biomes API "
                        + "world height " + expectedMinY + ".." + expectedMaxY
                        + ", but config/cavebiomesapi.cfg currently selects "
                        + actualMinY + ".." + actualMaxY + '.');
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Farmer's Future Delight could not read the Cave Biomes API world height.",
                    exception);
        }
    }

    public static void validateDepthsUpdateHeightRange(int expectedMinY, int expectedMaxY) {
        if (!DEPTHS_UPDATE_HEIGHT_CORE_PRESENT) {
            return;
        }
        try {
            Class<?> manager = Class.forName(
                    "sayys.depthsupdate.core.HeightManager", true, Launch.classLoader);
            Method isExtended = manager.getMethod("isExtended", int.class);
            if (!((Boolean) isExtended.invoke(null, 0))) {
                throw new IllegalStateException("Farmer's Future Delight requires Depths Update "
                        + "height extension to include dimension 0.");
            }
            Object context = manager.getMethod("get", int.class).invoke(null, 0);
            int actualMinY = ((Number) context.getClass().getMethod("minY").invoke(context)).intValue();
            int actualMaxY = ((Number) context.getClass().getMethod("maxY").invoke(context)).intValue();
            if (actualMinY != expectedMinY || actualMaxY != expectedMaxY) {
                throw new IllegalStateException("Farmer's Future Delight requires Depths Update "
                        + "world height " + expectedMinY + ".." + expectedMaxY
                        + ", but config/depthsupdate.cfg currently selects "
                        + actualMinY + ".." + actualMaxY + '.');
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Farmer's Future Delight could not read the Depths Update world height.",
                    exception);
        }
    }

    private static boolean detect(String resource) {
        try {
            return Launch.classLoader != null && Launch.classLoader.getResource(resource) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
