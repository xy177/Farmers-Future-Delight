package xy177.farmersfuturedelight.client;

import java.lang.reflect.Field;

import net.minecraftforge.fml.common.Loader;

public final class AquaAcrobaticsWaterCompat {
    private static final boolean ENABLED = resolve();

    private AquaAcrobaticsWaterCompat() {
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static String stillTexture() {
        return ENABLED ? "aquaacrobatics:blocks/water_still"
                : "minecraft:blocks/water_still";
    }

    public static String flowingTexture() {
        return ENABLED ? "aquaacrobatics:blocks/water_flow"
                : "minecraft:blocks/water_flow";
    }

    private static boolean resolve() {
        if (!Loader.isModLoaded("aquaacrobatics")) {
            return false;
        }
        try {
            Class<?> config = Class.forName(
                    "com.fuzs.aquaacrobatics.config.ConfigHandler$BlocksConfig",
                    false, AquaAcrobaticsWaterCompat.class.getClassLoader());
            Field field = config.getField("newWaterColors");
            return field.getBoolean(null);
        } catch (ReflectiveOperationException | LinkageError exception) {
            return true;
        }
    }
}
