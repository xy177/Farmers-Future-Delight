package xy177.farmersfuturedelight.common.registry;

import net.minecraft.world.biome.Biome;

import xy177.farmersfuturedelight.common.biome.BiomeCrimsonForest;
import xy177.farmersfuturedelight.common.biome.BiomeModernMountain;
import xy177.farmersfuturedelight.common.biome.BiomeWarpedForest;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;

public final class FFDBiomes {
    public static final Biome CRIMSON_FOREST = new BiomeCrimsonForest();
    public static final Biome WARPED_FOREST = new BiomeWarpedForest();
    public static final Biome MEADOW = new BiomeModernMountain(
            "meadow", "Meadow", 0.5F, 0.8F, 0x7BA4FF, 0x0E4ECF, false,
            BiomeModernMountain.SpawnProfile.MEADOW);
    public static final Biome GROVE = new BiomeModernMountain(
            "grove", "Grove", -0.2F, 0.8F, 0x81A0FF, 0x3F76E4, true,
            BiomeModernMountain.SpawnProfile.GROVE);
    public static final Biome SNOWY_SLOPES = new BiomeModernMountain(
            "snowy_slopes", "Snowy Slopes", -0.3F, 0.9F, 0x829FFF, 0x3F76E4, true,
            BiomeModernMountain.SpawnProfile.SNOWY_SLOPES);
    public static final Biome JAGGED_PEAKS = new BiomeModernMountain(
            "jagged_peaks", "Jagged Peaks", -0.7F, 0.9F, 0x859DFF, 0x3F76E4, true,
            BiomeModernMountain.SpawnProfile.EMPTY);
    public static final Biome FROZEN_PEAKS = new BiomeModernMountain(
            "frozen_peaks", "Frozen Peaks", -0.7F, 0.9F, 0x859DFF, 0x3F76E4, true,
            BiomeModernMountain.SpawnProfile.EMPTY);
    public static final Biome STONY_PEAKS = new BiomeModernMountain(
            "stony_peaks", "Stony Peaks", 1.0F, 0.3F, 0x76A8FF, 0x3F76E4, false,
            BiomeModernMountain.SpawnProfile.EMPTY);

    public static final Biome[] MOUNTAIN_BIOMES = {
            MEADOW, GROVE, SNOWY_SLOPES, JAGGED_PEAKS, FROZEN_PEAKS, STONY_PEAKS
    };

    public static Biome forModernBiome(FFDModernBiomeResolver.ModernBiome biome) {
        switch (biome) {
            case MEADOW:
                return MEADOW;
            case GROVE:
                return GROVE;
            case SNOWY_SLOPES:
                return SNOWY_SLOPES;
            case JAGGED_PEAKS:
                return JAGGED_PEAKS;
            case FROZEN_PEAKS:
                return FROZEN_PEAKS;
            case STONY_PEAKS:
                return STONY_PEAKS;
            default:
                return null;
        }
    }

    public static Biome forVerticalBiome(FFDVerticalBiome biome) {
        switch (biome) {
            case MEADOW:
                return MEADOW;
            case GROVE:
                return GROVE;
            case SNOWY_SLOPES:
                return SNOWY_SLOPES;
            case JAGGED_PEAKS:
                return JAGGED_PEAKS;
            case FROZEN_PEAKS:
                return FROZEN_PEAKS;
            case STONY_PEAKS:
                return STONY_PEAKS;
            default:
                return null;
        }
    }

    private FFDBiomes() {
    }
}
