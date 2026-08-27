package xy177.farmersfuturedelight.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;
import xy177.farmersfuturedelight.common.biome.BiomeModernMountain;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;

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

    public static void registerBiomeColors() {
        if (!ENABLED) {
            return;
        }
        try {
            Class<?> config = Class.forName(
                    "com.fuzs.aquaacrobatics.config.ConfigHandler$MiscellaneousConfig",
                    false, AquaAcrobaticsWaterCompat.class.getClassLoader());
            Field field = config.getField("customBiomeWaterColors");
            String[] configured = (String[]) field.get(null);
            List<String> colors = new ArrayList<>(configured == null
                    ? java.util.Collections.emptyList() : Arrays.asList(configured));
            for (Biome biome : FFDBiomes.MOUNTAIN_BIOMES) {
                ResourceLocation registryName = biome.getRegistryName();
                if (registryName == null || contains(colors, registryName.toString())) {
                    continue;
                }
                int color = ((BiomeModernMountain) biome).getModernWaterColor();
                colors.add(registryName + ",0x" + String.format("%06X", color) + ",");
            }
            field.set(null, colors.toArray(new String[0]));
        } catch (ReflectiveOperationException | LinkageError exception) {
            return;
        }
    }

    private static boolean contains(List<String> colors, String registryName) {
        String prefix = registryName + ",";
        for (String color : colors) {
            if (color != null && color.startsWith(prefix)) {
                return true;
            }
        }
        return false;
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
