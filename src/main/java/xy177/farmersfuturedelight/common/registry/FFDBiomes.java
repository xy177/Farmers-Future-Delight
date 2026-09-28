package xy177.farmersfuturedelight.common.registry;

import net.minecraft.world.biome.Biome;

import xy177.farmersfuturedelight.common.biome.BiomeCrimsonForest;
import xy177.farmersfuturedelight.common.biome.BiomeModernMountain;
import xy177.farmersfuturedelight.common.biome.BiomeModernOcean;
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
    public static final BiomeModernOcean WARM_OCEAN = new BiomeModernOcean(
            "warm_ocean", "Warm Ocean", BiomeModernOcean.Type.WARM, 0x43D5EE, 0x041F33);
    public static final BiomeModernOcean LUKEWARM_OCEAN = new BiomeModernOcean(
            "lukewarm_ocean", "Lukewarm Ocean", BiomeModernOcean.Type.LUKEWARM,
            0x45ADF2, 0x041633);
    public static final BiomeModernOcean COLD_OCEAN = new BiomeModernOcean(
            "cold_ocean", "Cold Ocean", BiomeModernOcean.Type.COLD, 0x3D57D6, 0x050533);
    public static final BiomeModernOcean FROZEN_OCEAN = new BiomeModernOcean(
            "frozen_ocean", "Frozen Ocean", BiomeModernOcean.Type.FROZEN,
            0x3938C9, 0x050533);
    public static final BiomeModernOcean DEEP_LUKEWARM_OCEAN = new BiomeModernOcean(
            "deep_lukewarm_ocean", "Deep Lukewarm Ocean",
            BiomeModernOcean.Type.DEEP_LUKEWARM, 0x45ADF2, 0x041633);
    public static final BiomeModernOcean DEEP_COLD_OCEAN = new BiomeModernOcean(
            "deep_cold_ocean", "Deep Cold Ocean", BiomeModernOcean.Type.DEEP_COLD,
            0x3D57D6, 0x050533);
    public static final BiomeModernOcean DEEP_FROZEN_OCEAN = new BiomeModernOcean(
            "deep_frozen_ocean", "Deep Frozen Ocean", BiomeModernOcean.Type.DEEP_FROZEN,
            0x3938C9, 0x050533);

    public static final Biome[] MOUNTAIN_BIOMES = {
            MEADOW, GROVE, SNOWY_SLOPES, JAGGED_PEAKS, FROZEN_PEAKS, STONY_PEAKS
    };
    public static final Biome[] OCEAN_BIOMES = {
            WARM_OCEAN, LUKEWARM_OCEAN, COLD_OCEAN, FROZEN_OCEAN,
            DEEP_LUKEWARM_OCEAN, DEEP_COLD_OCEAN, DEEP_FROZEN_OCEAN
    };
    public static final Biome[] MODERN_WATER_BIOMES = {
            MEADOW, GROVE, SNOWY_SLOPES, JAGGED_PEAKS, FROZEN_PEAKS, STONY_PEAKS,
            WARM_OCEAN, LUKEWARM_OCEAN, COLD_OCEAN, FROZEN_OCEAN,
            DEEP_LUKEWARM_OCEAN, DEEP_COLD_OCEAN, DEEP_FROZEN_OCEAN
    };

    public static Biome forModernBiome(FFDModernBiomeResolver.ModernBiome biome) {
        switch (biome) {
            case WARM_OCEAN:
                return WARM_OCEAN;
            case LUKEWARM_OCEAN:
                return LUKEWARM_OCEAN;
            case COLD_OCEAN:
                return COLD_OCEAN;
            case FROZEN_OCEAN:
                return FROZEN_OCEAN;
            case DEEP_LUKEWARM_OCEAN:
                return DEEP_LUKEWARM_OCEAN;
            case DEEP_COLD_OCEAN:
                return DEEP_COLD_OCEAN;
            case DEEP_FROZEN_OCEAN:
                return DEEP_FROZEN_OCEAN;
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

    public static Biome forModernMountainBiome(FFDModernBiomeResolver.ModernBiome biome) {
        Biome resolved = forModernBiome(biome);
        return resolved instanceof BiomeModernMountain ? resolved : null;
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
