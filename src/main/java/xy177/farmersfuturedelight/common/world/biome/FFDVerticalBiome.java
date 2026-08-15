package xy177.farmersfuturedelight.common.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public enum FFDVerticalBiome {
    NONE(0, null),
    LUSH_CAVES(1, "lush_caves"),
    DRIPSTONE_CAVES(2, "dripstone_caves"),
    MEADOW(3, "meadow"),
    GROVE(4, "grove"),
    SNOWY_SLOPES(5, "snowy_slopes"),
    JAGGED_PEAKS(6, "jagged_peaks"),
    FROZEN_PEAKS(7, "frozen_peaks"),
    STONY_PEAKS(8, "stony_peaks");

    private static final FFDVerticalBiome[] BY_ID = values();

    private final int id;
    private final String path;

    FFDVerticalBiome(int id, String path) {
        this.id = id;
        this.path = path;
    }

    public byte id() {
        return (byte) id;
    }

    public static FFDVerticalBiome byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : NONE;
    }

    public boolean isMountain() {
        return this == MEADOW || this == GROVE || this == SNOWY_SLOPES
                || this == JAGGED_PEAKS || this == FROZEN_PEAKS
                || this == STONY_PEAKS;
    }

    public boolean isCave() {
        return this == LUSH_CAVES || this == DRIPSTONE_CAVES;
    }

    public String registryPath() {
        return path;
    }

    public static FFDVerticalBiome adaptSurface(FFDVerticalBiome candidate, Biome legacyBiome,
                                                BlockPos pos) {
        if (!candidate.isMountain()) {
            return candidate;
        }
        if (BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.OCEAN)
                || BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.RIVER)
                || BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.BEACH)
                || BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.SWAMP)) {
            return NONE;
        }

        float temperature = legacyBiome.getTemperature(pos);
        boolean cold = BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.SNOWY)
                || BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.COLD)
                || temperature < 0.2F;
        boolean hotAndDry = BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.SANDY)
                || BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.HOT)
                && BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.DRY)
                || temperature >= 1.0F;

        switch (candidate) {
            case GROVE:
            case SNOWY_SLOPES:
                return cold ? candidate : hotAndDry ? NONE : MEADOW;
            case JAGGED_PEAKS:
            case FROZEN_PEAKS:
                return cold ? candidate : STONY_PEAKS;
            case MEADOW:
                return hotAndDry ? NONE : MEADOW;
            default:
                return candidate;
        }
    }
}
