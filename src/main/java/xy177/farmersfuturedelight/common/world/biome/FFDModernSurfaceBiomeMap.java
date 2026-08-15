package xy177.farmersfuturedelight.common.world.biome;

import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver.ModernBiome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Exact surface-biome parameter map ported from 26.3 OverworldBiomeBuilder. */
final class FFDModernSurfaceBiomeMap {
    private static final Range FULL = range(-1.0F, 1.0F);
    private static final Range[] TEMPERATURES = ranges(
            -1.0F, -0.45F, -0.15F, 0.2F, 0.55F, 1.0F);
    private static final Range[] HUMIDITIES = ranges(
            -1.0F, -0.35F, -0.1F, 0.1F, 0.3F, 1.0F);
    private static final Range[] EROSIONS = ranges(
            -1.0F, -0.78F, -0.375F, -0.2225F, 0.05F, 0.45F, 0.55F, 1.0F);
    private static final Range[] CONTINENTALNESS = {
            range(-0.19F, -0.11F), range(-0.11F, 0.03F),
            range(0.03F, 0.3F), range(0.3F, 1.0F)
    };
    private static final float[] WEIRDNESS_BOUNDARIES = {
            -1.0F, -0.93333334F, -0.7666667F, -0.56666666F, -0.4F,
            -0.26666668F, -0.05F, 0.05F, 0.26666668F, 0.4F,
            0.56666666F, 0.7666667F, 0.93333334F, 1.0F
    };
    private static final Range[] WEIRDNESS = ranges(WEIRDNESS_BOUNDARIES);

    private static final Range MUSHROOM_FIELDS = range(-1.2F, -1.05F);
    private static final Range DEEP_OCEAN = range(-1.05F, -0.455F);
    private static final Range OCEAN = range(-0.455F, -0.19F);
    private static final Range COAST = CONTINENTALNESS[0];
    private static final Range INLAND = range(-0.11F, 0.55F);
    private static final Range NEAR_INLAND = CONTINENTALNESS[1];
    private static final Range MID_INLAND = CONTINENTALNESS[2];
    private static final Range FAR_INLAND = CONTINENTALNESS[3];
    private static final Range FROZEN = TEMPERATURES[0];
    private static final Range UNFROZEN = span(TEMPERATURES[1], TEMPERATURES[4]);

    private static final ModernBiome[][] OCEANS = {
            {ModernBiome.DEEP_FROZEN_OCEAN, ModernBiome.DEEP_COLD_OCEAN,
                    ModernBiome.DEEP_OCEAN, ModernBiome.DEEP_LUKEWARM_OCEAN,
                    ModernBiome.WARM_OCEAN},
            {ModernBiome.FROZEN_OCEAN, ModernBiome.COLD_OCEAN, ModernBiome.OCEAN,
                    ModernBiome.LUKEWARM_OCEAN, ModernBiome.WARM_OCEAN}
    };
    private static final ModernBiome[][] MIDDLE_BIOMES = {
            {ModernBiome.SNOWY_PLAINS, ModernBiome.SNOWY_PLAINS, ModernBiome.SNOWY_PLAINS,
                    ModernBiome.SNOWY_TAIGA, ModernBiome.TAIGA},
            {ModernBiome.PLAINS, ModernBiome.PLAINS, ModernBiome.FOREST,
                    ModernBiome.TAIGA, ModernBiome.OLD_GROWTH_SPRUCE_TAIGA},
            {ModernBiome.FLOWER_FOREST, ModernBiome.PLAINS, ModernBiome.FOREST,
                    ModernBiome.BIRCH_FOREST, ModernBiome.DARK_FOREST},
            {ModernBiome.SAVANNA, ModernBiome.SAVANNA, ModernBiome.FOREST,
                    ModernBiome.JUNGLE, ModernBiome.JUNGLE},
            {ModernBiome.DESERT, ModernBiome.DESERT, ModernBiome.DESERT,
                    ModernBiome.DESERT, ModernBiome.DESERT}
    };
    private static final ModernBiome[][] MIDDLE_VARIANTS = {
            {ModernBiome.ICE_SPIKES, null, ModernBiome.SNOWY_TAIGA, null, null},
            {ModernBiome.DAPPLED_FOREST, null, null, null, ModernBiome.OLD_GROWTH_PINE_TAIGA},
            {ModernBiome.SUNFLOWER_PLAINS, null, null, ModernBiome.OLD_GROWTH_BIRCH_FOREST, null},
            {null, null, ModernBiome.PLAINS, ModernBiome.SPARSE_JUNGLE, ModernBiome.BAMBOO_JUNGLE},
            {null, null, null, null, null}
    };
    private static final ModernBiome[][] PLATEAU_BIOMES = {
            {ModernBiome.SNOWY_PLAINS, ModernBiome.SNOWY_PLAINS, ModernBiome.SNOWY_PLAINS,
                    ModernBiome.SNOWY_TAIGA, ModernBiome.SNOWY_TAIGA},
            {ModernBiome.MEADOW, ModernBiome.MEADOW, ModernBiome.FOREST,
                    ModernBiome.TAIGA, ModernBiome.OLD_GROWTH_SPRUCE_TAIGA},
            {ModernBiome.MEADOW, ModernBiome.MEADOW, ModernBiome.MEADOW,
                    ModernBiome.MEADOW, ModernBiome.PALE_GARDEN},
            {ModernBiome.SAVANNA_PLATEAU, ModernBiome.SAVANNA_PLATEAU, ModernBiome.FOREST,
                    ModernBiome.FOREST, ModernBiome.JUNGLE},
            {ModernBiome.BADLANDS, ModernBiome.BADLANDS, ModernBiome.BADLANDS,
                    ModernBiome.WOODED_BADLANDS, ModernBiome.WOODED_BADLANDS}
    };
    private static final ModernBiome[][] PLATEAU_VARIANTS = {
            {ModernBiome.ICE_SPIKES, null, null, null, null},
            {ModernBiome.CHERRY_GROVE, null, ModernBiome.MEADOW,
                    ModernBiome.MEADOW, ModernBiome.OLD_GROWTH_PINE_TAIGA},
            {ModernBiome.CHERRY_GROVE, ModernBiome.CHERRY_GROVE, ModernBiome.FOREST,
                    ModernBiome.BIRCH_FOREST, null},
            {null, null, null, null, null},
            {ModernBiome.ERODED_BADLANDS, ModernBiome.ERODED_BADLANDS, null, null, null}
    };
    private static final ModernBiome[][] SHATTERED_BIOMES = {
            {ModernBiome.WINDSWEPT_GRAVELLY_HILLS, ModernBiome.WINDSWEPT_GRAVELLY_HILLS,
                    ModernBiome.WINDSWEPT_HILLS, ModernBiome.WINDSWEPT_FOREST,
                    ModernBiome.WINDSWEPT_FOREST},
            {ModernBiome.WINDSWEPT_GRAVELLY_HILLS, ModernBiome.WINDSWEPT_GRAVELLY_HILLS,
                    ModernBiome.WINDSWEPT_HILLS, ModernBiome.WINDSWEPT_FOREST,
                    ModernBiome.WINDSWEPT_FOREST},
            {ModernBiome.WINDSWEPT_HILLS, ModernBiome.WINDSWEPT_HILLS,
                    ModernBiome.WINDSWEPT_HILLS, ModernBiome.WINDSWEPT_FOREST,
                    ModernBiome.WINDSWEPT_FOREST},
            {null, null, null, null, null},
            {null, null, null, null, null}
    };

    private static final List<Entry> ENTRIES = buildEntries();
    private static final RTree INDEX = RTree.create(ENTRIES);
    private FFDModernSurfaceBiomeMap() {
    }

    static long quantize(double value) {
        return (long) ((float) value * 10000.0F);
    }

    static long quantizeWeirdness(double value) {
        float sampled = (float) value;
        long quantized = quantize(sampled);
        for (int i = 1; i < WEIRDNESS_BOUNDARIES.length - 1; i++) {
            float boundary = WEIRDNESS_BOUNDARIES[i];
            if (quantized != quantize(boundary)) {
                continue;
            }
            if (sampled > boundary) {
                return quantized + 1L;
            }
            if (sampled < boundary) {
                return quantized - 1L;
            }
        }
        return quantized;
    }

    static double unquantize(long value) {
        return value / 10000.0D;
    }

    static ModernBiome select(long temperature, long humidity, long continentalness,
                              long erosion, long weirdness) {
        int temperatureIndex = index(temperature, TEMPERATURES);
        if (continentalness <= MUSHROOM_FIELDS.max) {
            return ModernBiome.MUSHROOM_FIELDS;
        }
        if (continentalness <= DEEP_OCEAN.max) {
            return OCEANS[0][temperatureIndex];
        }
        if (continentalness <= OCEAN.max) {
            return OCEANS[1][temperatureIndex];
        }
        return INDEX.nearest(temperature, humidity, continentalness, erosion, weirdness);
    }

    private static List<Entry> buildEntries() {
        List<Entry> entries = new ArrayList<>();
        addMidSlice(entries, WEIRDNESS[0]);
        addHighSlice(entries, WEIRDNESS[1]);
        addPeaks(entries, WEIRDNESS[2]);
        addHighSlice(entries, WEIRDNESS[3]);
        addMidSlice(entries, WEIRDNESS[4]);
        addLowSlice(entries, WEIRDNESS[5]);
        addValleys(entries, WEIRDNESS[6]);
        addLowSlice(entries, WEIRDNESS[7]);
        addMidSlice(entries, WEIRDNESS[8]);
        addHighSlice(entries, WEIRDNESS[9]);
        addPeaks(entries, WEIRDNESS[10]);
        addHighSlice(entries, WEIRDNESS[11]);
        addMidSlice(entries, WEIRDNESS[12]);
        return entries;
    }

    private static void addPeaks(List<Entry> entries, Range weirdness) {
        for (int temperature = 0; temperature < TEMPERATURES.length; temperature++) {
            for (int humidity = 0; humidity < HUMIDITIES.length; humidity++) {
                ModernBiome middle = pickMiddle(temperature, humidity, weirdness);
                ModernBiome middleOrBadlands = pickMiddleOrBadlands(temperature, humidity, weirdness);
                ModernBiome middleBadlandsOrSlope = pickMiddleBadlandsOrSlope(
                        temperature, humidity, weirdness);
                ModernBiome plateau = pickPlateau(temperature, humidity, weirdness);
                ModernBiome shattered = pickShattered(temperature, humidity, weirdness);
                ModernBiome shatteredOrSavanna = maybeWindsweptSavanna(
                        temperature, humidity, weirdness, shattered);
                ModernBiome peak = pickPeak(temperature, humidity, weirdness);
                add(entries, temperature, humidity, span(COAST, FAR_INLAND), EROSIONS[0], weirdness, peak);
                add(entries, temperature, humidity, span(COAST, NEAR_INLAND), EROSIONS[1], weirdness,
                        middleBadlandsOrSlope);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND), EROSIONS[1], weirdness, peak);
                add(entries, temperature, humidity, span(COAST, NEAR_INLAND), span(EROSIONS[2], EROSIONS[3]),
                        weirdness, middle);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND), EROSIONS[2], weirdness,
                        plateau);
                add(entries, temperature, humidity, MID_INLAND, EROSIONS[3], weirdness, middleOrBadlands);
                add(entries, temperature, humidity, FAR_INLAND, EROSIONS[3], weirdness, plateau);
                add(entries, temperature, humidity, span(COAST, FAR_INLAND), EROSIONS[4], weirdness, middle);
                add(entries, temperature, humidity, span(COAST, NEAR_INLAND), EROSIONS[5], weirdness,
                        shatteredOrSavanna);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND), EROSIONS[5], weirdness,
                        shattered);
                add(entries, temperature, humidity, span(COAST, FAR_INLAND), EROSIONS[6], weirdness, middle);
            }
        }
    }

    private static void addHighSlice(List<Entry> entries, Range weirdness) {
        for (int temperature = 0; temperature < TEMPERATURES.length; temperature++) {
            for (int humidity = 0; humidity < HUMIDITIES.length; humidity++) {
                ModernBiome middle = pickMiddle(temperature, humidity, weirdness);
                ModernBiome middleOrBadlands = pickMiddleOrBadlands(temperature, humidity, weirdness);
                ModernBiome middleBadlandsOrSlope = pickMiddleBadlandsOrSlope(
                        temperature, humidity, weirdness);
                ModernBiome plateau = pickPlateau(temperature, humidity, weirdness);
                ModernBiome shattered = pickShattered(temperature, humidity, weirdness);
                ModernBiome middleOrSavanna = maybeWindsweptSavanna(
                        temperature, humidity, weirdness, middle);
                ModernBiome slope = pickSlope(temperature, humidity, weirdness);
                ModernBiome peak = pickPeak(temperature, humidity, weirdness);
                add(entries, temperature, humidity, COAST, span(EROSIONS[0], EROSIONS[1]), weirdness, middle);
                add(entries, temperature, humidity, NEAR_INLAND, EROSIONS[0], weirdness, slope);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND), EROSIONS[0], weirdness, peak);
                add(entries, temperature, humidity, NEAR_INLAND, EROSIONS[1], weirdness,
                        middleBadlandsOrSlope);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND), EROSIONS[1], weirdness, slope);
                add(entries, temperature, humidity, span(COAST, NEAR_INLAND), span(EROSIONS[2], EROSIONS[3]),
                        weirdness, middle);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND), EROSIONS[2], weirdness,
                        plateau);
                add(entries, temperature, humidity, MID_INLAND, EROSIONS[3], weirdness, middleOrBadlands);
                add(entries, temperature, humidity, FAR_INLAND, EROSIONS[3], weirdness, plateau);
                add(entries, temperature, humidity, span(COAST, FAR_INLAND), EROSIONS[4], weirdness, middle);
                add(entries, temperature, humidity, span(COAST, NEAR_INLAND), EROSIONS[5], weirdness,
                        middleOrSavanna);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND), EROSIONS[5], weirdness,
                        shattered);
                add(entries, temperature, humidity, span(COAST, FAR_INLAND), EROSIONS[6], weirdness, middle);
            }
        }
    }

    private static void addMidSlice(List<Entry> entries, Range weirdness) {
        add(entries, FULL, FULL, COAST, span(EROSIONS[0], EROSIONS[2]), weirdness,
                ModernBiome.STONY_SHORE);
        add(entries, span(TEMPERATURES[1], TEMPERATURES[2]), FULL,
                span(NEAR_INLAND, FAR_INLAND), EROSIONS[6], weirdness, ModernBiome.SWAMP);
        add(entries, span(TEMPERATURES[3], TEMPERATURES[4]), FULL,
                span(NEAR_INLAND, FAR_INLAND), EROSIONS[6], weirdness,
                ModernBiome.MANGROVE_SWAMP);
        for (int temperature = 0; temperature < TEMPERATURES.length; temperature++) {
            for (int humidity = 0; humidity < HUMIDITIES.length; humidity++) {
                ModernBiome middle = pickMiddle(temperature, humidity, weirdness);
                ModernBiome middleOrBadlands = pickMiddleOrBadlands(temperature, humidity, weirdness);
                ModernBiome middleBadlandsOrSlope = pickMiddleBadlandsOrSlope(
                        temperature, humidity, weirdness);
                ModernBiome shattered = pickShattered(temperature, humidity, weirdness);
                ModernBiome plateau = pickPlateau(temperature, humidity, weirdness);
                ModernBiome beach = pickBeach(temperature);
                ModernBiome middleOrSavanna = maybeWindsweptSavanna(
                        temperature, humidity, weirdness, middle);
                ModernBiome shatteredCoast = pickShatteredCoast(temperature, humidity, weirdness);
                ModernBiome slope = pickSlope(temperature, humidity, weirdness);
                add(entries, temperature, humidity, span(NEAR_INLAND, FAR_INLAND), EROSIONS[0], weirdness,
                        slope);
                add(entries, temperature, humidity, span(NEAR_INLAND, MID_INLAND), EROSIONS[1], weirdness,
                        middleBadlandsOrSlope);
                add(entries, temperature, humidity, FAR_INLAND, EROSIONS[1], weirdness,
                        temperature == 0 ? slope : plateau);
                add(entries, temperature, humidity, NEAR_INLAND, EROSIONS[2], weirdness, middle);
                add(entries, temperature, humidity, MID_INLAND, EROSIONS[2], weirdness, middleOrBadlands);
                add(entries, temperature, humidity, FAR_INLAND, EROSIONS[2], weirdness, plateau);
                add(entries, temperature, humidity, span(COAST, NEAR_INLAND), EROSIONS[3], weirdness, middle);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND), EROSIONS[3], weirdness,
                        middleOrBadlands);
                if (weirdness.negative()) {
                    add(entries, temperature, humidity, COAST, EROSIONS[4], weirdness, beach);
                    add(entries, temperature, humidity, span(NEAR_INLAND, FAR_INLAND), EROSIONS[4],
                            weirdness, middle);
                } else {
                    add(entries, temperature, humidity, span(COAST, FAR_INLAND), EROSIONS[4], weirdness,
                            middle);
                }
                add(entries, temperature, humidity, COAST, EROSIONS[5], weirdness, shatteredCoast);
                add(entries, temperature, humidity, NEAR_INLAND, EROSIONS[5], weirdness, middleOrSavanna);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND), EROSIONS[5], weirdness,
                        shattered);
                add(entries, temperature, humidity, COAST, EROSIONS[6], weirdness,
                        weirdness.negative() ? beach : middle);
                if (temperature == 0) {
                    add(entries, temperature, humidity, span(NEAR_INLAND, FAR_INLAND), EROSIONS[6],
                            weirdness, middle);
                }
            }
        }
    }

    private static void addLowSlice(List<Entry> entries, Range weirdness) {
        add(entries, FULL, FULL, COAST, span(EROSIONS[0], EROSIONS[2]), weirdness,
                ModernBiome.STONY_SHORE);
        add(entries, span(TEMPERATURES[1], TEMPERATURES[2]), FULL,
                span(NEAR_INLAND, FAR_INLAND), EROSIONS[6], weirdness, ModernBiome.SWAMP);
        add(entries, span(TEMPERATURES[3], TEMPERATURES[4]), FULL,
                span(NEAR_INLAND, FAR_INLAND), EROSIONS[6], weirdness,
                ModernBiome.MANGROVE_SWAMP);
        for (int temperature = 0; temperature < TEMPERATURES.length; temperature++) {
            for (int humidity = 0; humidity < HUMIDITIES.length; humidity++) {
                ModernBiome middle = pickMiddle(temperature, humidity, weirdness);
                ModernBiome middleOrBadlands = pickMiddleOrBadlands(temperature, humidity, weirdness);
                ModernBiome middleBadlandsOrSlope = pickMiddleBadlandsOrSlope(
                        temperature, humidity, weirdness);
                ModernBiome beach = pickBeach(temperature);
                ModernBiome middleOrSavanna = maybeWindsweptSavanna(
                        temperature, humidity, weirdness, middle);
                ModernBiome shatteredCoast = pickShatteredCoast(temperature, humidity, weirdness);
                add(entries, temperature, humidity, NEAR_INLAND, span(EROSIONS[0], EROSIONS[1]),
                        weirdness, middleOrBadlands);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND),
                        span(EROSIONS[0], EROSIONS[1]), weirdness, middleBadlandsOrSlope);
                add(entries, temperature, humidity, NEAR_INLAND, span(EROSIONS[2], EROSIONS[3]),
                        weirdness, middle);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND),
                        span(EROSIONS[2], EROSIONS[3]), weirdness, middleOrBadlands);
                add(entries, temperature, humidity, COAST, span(EROSIONS[3], EROSIONS[4]), weirdness, beach);
                add(entries, temperature, humidity, span(NEAR_INLAND, FAR_INLAND), EROSIONS[4], weirdness,
                        middle);
                add(entries, temperature, humidity, COAST, EROSIONS[5], weirdness, shatteredCoast);
                add(entries, temperature, humidity, NEAR_INLAND, EROSIONS[5], weirdness, middleOrSavanna);
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND), EROSIONS[5], weirdness,
                        middle);
                add(entries, temperature, humidity, COAST, EROSIONS[6], weirdness, beach);
                if (temperature == 0) {
                    add(entries, temperature, humidity, span(NEAR_INLAND, FAR_INLAND), EROSIONS[6],
                            weirdness, middle);
                }
            }
        }
    }

    private static void addValleys(List<Entry> entries, Range weirdness) {
        add(entries, FROZEN, FULL, COAST, span(EROSIONS[0], EROSIONS[1]), weirdness,
                weirdness.negative() ? ModernBiome.STONY_SHORE : ModernBiome.FROZEN_RIVER);
        add(entries, UNFROZEN, FULL, COAST, span(EROSIONS[0], EROSIONS[1]), weirdness,
                weirdness.negative() ? ModernBiome.STONY_SHORE : ModernBiome.RIVER);
        add(entries, FROZEN, FULL, NEAR_INLAND, span(EROSIONS[0], EROSIONS[1]), weirdness,
                ModernBiome.FROZEN_RIVER);
        add(entries, UNFROZEN, FULL, NEAR_INLAND, span(EROSIONS[0], EROSIONS[1]), weirdness,
                ModernBiome.RIVER);
        add(entries, FROZEN, FULL, span(COAST, FAR_INLAND), span(EROSIONS[2], EROSIONS[5]),
                weirdness, ModernBiome.FROZEN_RIVER);
        add(entries, UNFROZEN, FULL, span(COAST, FAR_INLAND), span(EROSIONS[2], EROSIONS[5]),
                weirdness, ModernBiome.RIVER);
        add(entries, FROZEN, FULL, COAST, EROSIONS[6], weirdness, ModernBiome.FROZEN_RIVER);
        add(entries, UNFROZEN, FULL, COAST, EROSIONS[6], weirdness, ModernBiome.RIVER);
        add(entries, span(TEMPERATURES[1], TEMPERATURES[2]), FULL, span(INLAND, FAR_INLAND),
                EROSIONS[6], weirdness, ModernBiome.SWAMP);
        add(entries, span(TEMPERATURES[3], TEMPERATURES[4]), FULL, span(INLAND, FAR_INLAND),
                EROSIONS[6], weirdness, ModernBiome.MANGROVE_SWAMP);
        add(entries, FROZEN, FULL, span(INLAND, FAR_INLAND), EROSIONS[6], weirdness,
                ModernBiome.FROZEN_RIVER);
        for (int temperature = 0; temperature < TEMPERATURES.length; temperature++) {
            for (int humidity = 0; humidity < HUMIDITIES.length; humidity++) {
                add(entries, temperature, humidity, span(MID_INLAND, FAR_INLAND),
                        span(EROSIONS[0], EROSIONS[1]), weirdness,
                        pickMiddleOrBadlands(temperature, humidity, weirdness));
            }
        }
    }

    private static ModernBiome pickMiddle(int temperature, int humidity, Range weirdness) {
        if (weirdness.negative()) {
            return MIDDLE_BIOMES[temperature][humidity];
        }
        ModernBiome variant = MIDDLE_VARIANTS[temperature][humidity];
        return variant == null ? MIDDLE_BIOMES[temperature][humidity] : variant;
    }

    private static ModernBiome pickMiddleOrBadlands(int temperature, int humidity, Range weirdness) {
        return temperature == 4 ? pickBadlands(humidity, weirdness)
                : pickMiddle(temperature, humidity, weirdness);
    }

    private static ModernBiome pickMiddleBadlandsOrSlope(int temperature, int humidity,
                                                          Range weirdness) {
        return temperature == 0 ? pickSlope(temperature, humidity, weirdness)
                : pickMiddleOrBadlands(temperature, humidity, weirdness);
    }

    private static ModernBiome maybeWindsweptSavanna(int temperature, int humidity,
                                                      Range weirdness, ModernBiome fallback) {
        return temperature > 1 && humidity < 4 && !weirdness.negative()
                ? ModernBiome.WINDSWEPT_SAVANNA : fallback;
    }

    private static ModernBiome pickShatteredCoast(int temperature, int humidity, Range weirdness) {
        ModernBiome fallback = weirdness.negative()
                ? pickBeach(temperature) : pickMiddle(temperature, humidity, weirdness);
        return maybeWindsweptSavanna(temperature, humidity, weirdness, fallback);
    }

    private static ModernBiome pickBeach(int temperature) {
        if (temperature == 0) {
            return ModernBiome.SNOWY_BEACH;
        }
        return temperature == 4 ? ModernBiome.DESERT : ModernBiome.BEACH;
    }

    private static ModernBiome pickBadlands(int humidity, Range weirdness) {
        if (humidity < 2) {
            return weirdness.negative() ? ModernBiome.BADLANDS : ModernBiome.ERODED_BADLANDS;
        }
        return humidity < 3 ? ModernBiome.BADLANDS : ModernBiome.WOODED_BADLANDS;
    }

    private static ModernBiome pickPlateau(int temperature, int humidity, Range weirdness) {
        ModernBiome variant = PLATEAU_VARIANTS[temperature][humidity];
        return !weirdness.negative() && variant != null
                ? variant : PLATEAU_BIOMES[temperature][humidity];
    }

    private static ModernBiome pickPeak(int temperature, int humidity, Range weirdness) {
        if (temperature <= 2) {
            return weirdness.negative() ? ModernBiome.JAGGED_PEAKS : ModernBiome.FROZEN_PEAKS;
        }
        return temperature == 3 ? ModernBiome.STONY_PEAKS : pickBadlands(humidity, weirdness);
    }

    private static ModernBiome pickSlope(int temperature, int humidity, Range weirdness) {
        if (temperature >= 3) {
            return pickPlateau(temperature, humidity, weirdness);
        }
        return humidity <= 1 ? ModernBiome.SNOWY_SLOPES : ModernBiome.GROVE;
    }

    private static ModernBiome pickShattered(int temperature, int humidity, Range weirdness) {
        ModernBiome biome = SHATTERED_BIOMES[temperature][humidity];
        return biome == null ? pickMiddle(temperature, humidity, weirdness) : biome;
    }

    private static void add(List<Entry> entries, int temperature, int humidity,
                            Range continentalness, Range erosion, Range weirdness,
                            ModernBiome biome) {
        add(entries, TEMPERATURES[temperature], HUMIDITIES[humidity],
                continentalness, erosion, weirdness, biome);
    }

    private static void add(List<Entry> entries, Range temperature, Range humidity,
                            Range continentalness, Range erosion, Range weirdness,
                            ModernBiome biome) {
        entries.add(new Entry(temperature, humidity, continentalness, erosion, weirdness, biome));
    }

    private static int index(long value, Range[] ranges) {
        for (int i = 0; i < ranges.length; i++) {
            if (value <= ranges[i].max) {
                return i;
            }
        }
        return ranges.length - 1;
    }

    private static Range[] ranges(float... boundaries) {
        Range[] ranges = new Range[boundaries.length - 1];
        for (int i = 0; i < ranges.length; i++) {
            ranges[i] = range(boundaries[i], boundaries[i + 1]);
        }
        return ranges;
    }

    private static Range range(float min, float max) {
        return new Range(quantize(min), quantize(max));
    }

    private static Range span(Range min, Range max) {
        return new Range(min.min, max.max);
    }

    private static final class Range {
        private final long min;
        private final long max;

        private Range(long min, long max) {
            this.min = min;
            this.max = max;
        }

        private long distance(long value) {
            if (value > max) {
                return value - max;
            }
            return Math.max(min - value, 0L);
        }

        private boolean negative() {
            return max < 0L;
        }

        private long middle() {
            return min + (max - min) / 2L;
        }

        private Range span(Range other) {
            if (other == null) {
                return this;
            }
            return new Range(Math.min(min, other.min), Math.max(max, other.max));
        }
    }

    private static final class Entry {
        private final Range temperature;
        private final Range humidity;
        private final Range continentalness;
        private final Range erosion;
        private final Range weirdness;
        private final ModernBiome biome;

        private Entry(Range temperature, Range humidity, Range continentalness,
                      Range erosion, Range weirdness, ModernBiome biome) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.continentalness = continentalness;
            this.erosion = erosion;
            this.weirdness = weirdness;
            this.biome = biome;
        }

        private Range range(int dimension) {
            switch (dimension) {
                case 0: return temperature;
                case 1: return humidity;
                case 2: return continentalness;
                case 3: return erosion;
                default: return weirdness;
            }
        }
    }

    /** Five-dimensional specialization of 26.3 Climate.RTree. */
    private static final class RTree {
        private static final int DIMENSIONS = 5;
        private static final int CHILDREN_PER_NODE = 6;

        private final Node root;
        private final ThreadLocal<Leaf> lastResult = new ThreadLocal<>();

        private RTree(Node root) {
            this.root = root;
        }

        private static RTree create(List<Entry> entries) {
            if (entries.isEmpty()) {
                throw new IllegalArgumentException("Need at least one biome parameter entry");
            }
            List<Node> leaves = new ArrayList<>(entries.size());
            for (Entry entry : entries) {
                leaves.add(new Leaf(entry));
            }
            return new RTree(build(leaves));
        }

        private ModernBiome nearest(long temperature, long humidity, long continentalness,
                                    long erosion, long weirdness) {
            long[] target = {temperature, humidity, continentalness, erosion, weirdness};
            Leaf leaf = root.search(target, lastResult.get());
            lastResult.set(leaf);
            return leaf.entry.biome;
        }

        private static Node build(List<Node> children) {
            if (children.isEmpty()) {
                throw new IllegalStateException("Need at least one child to build a node");
            }
            if (children.size() == 1) {
                return children.get(0);
            }
            if (children.size() <= CHILDREN_PER_NODE) {
                Collections.sort(children, Comparator.comparingLong(RTree::totalMagnitude));
                return new SubTree(children);
            }

            long minCost = Long.MAX_VALUE;
            int minDimension = -1;
            List<SubTree> minBuckets = null;
            for (int dimension = 0; dimension < DIMENSIONS; dimension++) {
                sort(children, dimension, false);
                List<SubTree> buckets = bucketize(children);
                long totalCost = 0L;
                for (SubTree bucket : buckets) {
                    totalCost += cost(bucket.parameterSpace);
                }
                if (totalCost < minCost) {
                    minCost = totalCost;
                    minDimension = dimension;
                    minBuckets = buckets;
                }
            }

            sort(minBuckets, minDimension, true);
            List<Node> branches = new ArrayList<>(minBuckets.size());
            for (SubTree bucket : minBuckets) {
                List<Node> bucketChildren = new ArrayList<>(bucket.children.length);
                Collections.addAll(bucketChildren, bucket.children);
                branches.add(build(bucketChildren));
            }
            return new SubTree(branches);
        }

        private static long totalMagnitude(Node node) {
            long total = 0L;
            for (Range range : node.parameterSpace) {
                total += Math.abs((range.min + range.max) / 2L);
            }
            return total;
        }

        private static <T extends Node> void sort(List<T> nodes, int firstDimension,
                                                  boolean absolute) {
            Collections.sort(nodes, (first, second) -> {
                for (int offset = 0; offset < DIMENSIONS; offset++) {
                    int dimension = (firstDimension + offset) % DIMENSIONS;
                    long firstCenter = center(first.parameterSpace[dimension], absolute);
                    long secondCenter = center(second.parameterSpace[dimension], absolute);
                    int comparison = Long.compare(firstCenter, secondCenter);
                    if (comparison != 0) {
                        return comparison;
                    }
                }
                return 0;
            });
        }

        private static long center(Range range, boolean absolute) {
            long center = (range.min + range.max) / 2L;
            return absolute ? Math.abs(center) : center;
        }

        private static List<SubTree> bucketize(List<Node> nodes) {
            List<SubTree> buckets = new ArrayList<>();
            List<Node> children = new ArrayList<>();
            int expectedChildren = (int) Math.pow(CHILDREN_PER_NODE,
                    Math.floor(Math.log(nodes.size() - 0.01D) / Math.log(CHILDREN_PER_NODE)));
            for (Node child : nodes) {
                children.add(child);
                if (children.size() >= expectedChildren) {
                    buckets.add(new SubTree(children));
                    children = new ArrayList<>();
                }
            }
            if (!children.isEmpty()) {
                buckets.add(new SubTree(children));
            }
            return buckets;
        }

        private static long cost(Range[] parameterSpace) {
            long result = 0L;
            for (Range range : parameterSpace) {
                result += Math.abs(range.max - range.min);
            }
            return result;
        }

        private static Range[] buildParameterSpace(List<Node> children) {
            Range[] bounds = new Range[DIMENSIONS];
            for (Node child : children) {
                for (int dimension = 0; dimension < DIMENSIONS; dimension++) {
                    bounds[dimension] = child.parameterSpace[dimension].span(bounds[dimension]);
                }
            }
            return bounds;
        }

        private abstract static class Node {
            final Range[] parameterSpace;

            private Node(Range[] parameterSpace) {
                this.parameterSpace = parameterSpace;
            }

            abstract Leaf search(long[] target, Leaf candidate);

            final long distance(long[] target) {
                long distance = 0L;
                for (int dimension = 0; dimension < DIMENSIONS; dimension++) {
                    long delta = parameterSpace[dimension].distance(target[dimension]);
                    distance += delta * delta;
                }
                return distance;
            }
        }

        private static final class SubTree extends Node {
            private final Node[] children;

            private SubTree(List<Node> children) {
                super(buildParameterSpace(children));
                this.children = children.toArray(new Node[children.size()]);
            }

            @Override
            Leaf search(long[] target, Leaf candidate) {
                long minDistance = candidate == null ? Long.MAX_VALUE : candidate.distance(target);
                Leaf closest = candidate;
                for (Node child : children) {
                    long childDistance = child.distance(target);
                    if (minDistance <= childDistance) {
                        continue;
                    }
                    Leaf leaf = child.search(target, closest);
                    long leafDistance = child == leaf ? childDistance : leaf.distance(target);
                    if (minDistance <= leafDistance) {
                        continue;
                    }
                    minDistance = leafDistance;
                    closest = leaf;
                }
                return closest;
            }
        }

        private static final class Leaf extends Node {
            private final Entry entry;

            private Leaf(Entry entry) {
                super(new Range[] {
                        entry.range(0), entry.range(1), entry.range(2),
                        entry.range(3), entry.range(4)
                });
                this.entry = entry;
            }

            @Override
            Leaf search(long[] target, Leaf candidate) {
                return this;
            }
        }
    }
}
