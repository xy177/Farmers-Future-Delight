package xy177.farmersfuturedelight.common.world.biome;

import java.util.LinkedHashMap;
import java.util.Map;

import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;
import xy177.farmersfuturedelight.common.world.terrain.FFDNoiseRouter;

public final class FFDVerticalBiomeSampler {
    private static final int MAX_CACHED_CHUNKS = 256;
    private static final double[] TEMPERATURE_THRESHOLDS = {
            -0.45D, -0.15D, 0.2D, 0.55D
    };
    private static final double[] HUMIDITY_THRESHOLDS = {
            -0.35D, -0.1D, 0.1D, 0.3D
    };
    private static final double[] EROSION_THRESHOLDS = {
            -0.78D, -0.375D, -0.2225D, 0.05D, 0.45D, 0.55D
    };
    private static final double COAST_START = -0.19D;
    private static final double NEAR_INLAND_START = -0.11D;
    private static final double MID_INLAND_START = 0.03D;
    private static final double FAR_INLAND_START = 0.3D;

    private static final double VALLEY_END = 0.05D;
    private static final double LOW_END = 0.26666668D;
    private static final double MID_END = 0.4D;
    private static final double HIGH_END = 0.56666666D;
    private static final double PEAK_END = 0.7666667D;
    private static final double OUTER_HIGH_END = 0.93333334D;

    private static final double UNDERGROUND_DEPTH_MIN = 0.2D;
    private static final double UNDERGROUND_DEPTH_MAX = 0.9D;
    private static final double LUSH_HUMIDITY_MIN = 0.7D;
    private static final double DRIPSTONE_CONTINENTALNESS_MIN = 0.8D;
    private static final int CAVE_SURFACE_BUFFER = 12;

    private final FFDNoiseRouter router;
    private final FFDModernWorldgenData worldgenData;
    private final Map<Long, FFDVerticalBiomeData> chunkCache =
            new LinkedHashMap<Long, FFDVerticalBiomeData>(MAX_CACHED_CHUNKS, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, FFDVerticalBiomeData> eldest) {
                    return size() > MAX_CACHED_CHUNKS;
                }
            };

    public FFDVerticalBiomeSampler(FFDModernWorldgenData worldgenData) {
        this.worldgenData = worldgenData;
        router = worldgenData.router();
    }

    public FFDVerticalBiomeData sampleChunk(int chunkX, int chunkZ) {
        long key = chunkKey(chunkX, chunkZ);
        synchronized (chunkCache) {
            FFDVerticalBiomeData cached = chunkCache.get(key);
            if (cached != null) {
                return cached;
            }
        }
        FFDVerticalBiomeData generated = generateChunk(chunkX, chunkZ);
        synchronized (chunkCache) {
            FFDVerticalBiomeData cached = chunkCache.get(key);
            if (cached != null) {
                return cached;
            }
            chunkCache.put(key, generated);
        }
        return generated;
    }

    public FFDVerticalBiome sampleNoiseBiome(int quartX, int quartY, int quartZ) {
        return sampleAtBlock(quartX * FFDVerticalBiomeData.CELL_SIZE,
                quartY * FFDVerticalBiomeData.CELL_SIZE,
                quartZ * FFDVerticalBiomeData.CELL_SIZE);
    }

    private FFDVerticalBiomeData generateChunk(int chunkX, int chunkZ) {
        byte[] sampled = new byte[FFDVerticalBiomeData.ENTRY_COUNT];
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        for (int cellX = 0; cellX < FFDVerticalBiomeData.CELLS_X; cellX++) {
            int blockX = startX + cellX * FFDVerticalBiomeData.CELL_SIZE;
            for (int cellZ = 0; cellZ < FFDVerticalBiomeData.CELLS_Z; cellZ++) {
                int blockZ = startZ + cellZ * FFDVerticalBiomeData.CELL_SIZE;
                ColumnClimate climate = sampleColumnClimate(blockX, blockZ);
                for (int cellY = 0; cellY < FFDVerticalBiomeData.CELLS_Y; cellY++) {
                    int blockY = FFDModernWorldgenData.MIN_Y
                            + cellY * FFDVerticalBiomeData.CELL_SIZE;
                    sampled[FFDVerticalBiomeData.index(cellX, cellY, cellZ)] =
                            sampleAtBlock(blockX, blockY, blockZ, climate).id();
                }
            }
        }
        return new FFDVerticalBiomeData(sampled);
    }

    private FFDVerticalBiome sampleAtBlock(int blockX, int blockY, int blockZ) {
        return sampleAtBlock(blockX, blockY, blockZ, sampleColumnClimate(blockX, blockZ));
    }

    private FFDVerticalBiome sampleAtBlock(int blockX, int blockY, int blockZ,
                                           ColumnClimate climate) {
        if (blockY <= climate.surfaceY - CAVE_SURFACE_BUFFER) {
            double depth = quantize(sampleDepth(blockX, blockY, blockZ));
            FFDVerticalBiome underground = sampleUndergroundBiome(
                    climate.humidity, climate.continentalness, depth);
            if (underground != FFDVerticalBiome.NONE) {
                return underground;
            }
        }
        return climate.surfaceBiome;
    }

    private ColumnClimate sampleColumnClimate(int blockX, int blockZ) {
        double temperature = quantize(router.temperature.sample(blockX, 0, blockZ));
        double humidity = quantize(router.vegetation.sample(blockX, 0, blockZ));
        double continentalness = quantize(router.continents.sample(blockX, 0, blockZ));
        double erosion = quantize(router.erosion.sample(blockX, 0, blockZ));
        double weirdness = quantize(router.ridges.sample(blockX, 0, blockZ));
        return new ColumnClimate(humidity, continentalness,
                worldgenData.preliminarySurfaceLevel(blockX, blockZ),
                sampleSurfaceBiome(temperature, humidity, continentalness, erosion, weirdness));
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return chunkX & 0xFFFFFFFFL | (long) chunkZ << 32;
    }

    private static FFDVerticalBiome sampleUndergroundBiome(double humidity,
                                                            double continentalness,
                                                            double depth) {
        double surfaceFitness = Math.min(square(depth), square(depth - 1.0D));
        double depthDistance = distance(depth, UNDERGROUND_DEPTH_MIN,
                UNDERGROUND_DEPTH_MAX);
        double undergroundFitness = square(depthDistance);
        if (undergroundFitness >= surfaceFitness) {
            return FFDVerticalBiome.NONE;
        }

        double dripstoneFitness = undergroundFitness
                + square(distance(continentalness, DRIPSTONE_CONTINENTALNESS_MIN, 1.0D));
        double lushFitness = undergroundFitness
                + square(distance(humidity, LUSH_HUMIDITY_MIN, 1.0D));
        double bestFitness = surfaceFitness;
        FFDVerticalBiome selected = FFDVerticalBiome.NONE;
        if (dripstoneFitness < bestFitness) {
            bestFitness = dripstoneFitness;
            selected = FFDVerticalBiome.DRIPSTONE_CAVES;
        }
        if (lushFitness < bestFitness) {
            selected = FFDVerticalBiome.LUSH_CAVES;
        }
        return selected;
    }

    private static FFDVerticalBiome sampleSurfaceBiome(double temperature, double humidity,
                                                        double continentalness, double erosion,
                                                        double weirdness) {
        if (continentalness < COAST_START) {
            return FFDVerticalBiome.NONE;
        }

        int temperatureIndex = index(temperature, TEMPERATURE_THRESHOLDS);
        int humidityIndex = index(humidity, HUMIDITY_THRESHOLDS);
        int erosionIndex = index(erosion, EROSION_THRESHOLDS);
        int continentalnessIndex = continentalnessIndex(continentalness);
        Slice slice = slice(weirdness);

        switch (slice) {
            case PEAKS:
                return samplePeaks(temperatureIndex, humidityIndex, continentalnessIndex,
                        erosionIndex, weirdness);
            case HIGH:
                return sampleHigh(temperatureIndex, humidityIndex, continentalnessIndex,
                        erosionIndex, weirdness);
            case MID:
                return sampleMid(temperatureIndex, humidityIndex, continentalnessIndex,
                        erosionIndex, weirdness);
            case LOW:
                return sampleLow(temperatureIndex, humidityIndex, continentalnessIndex,
                        erosionIndex, weirdness);
            default:
                return FFDVerticalBiome.NONE;
        }
    }

    private static FFDVerticalBiome samplePeaks(int temperature, int humidity,
                                                 int continentalness, int erosion,
                                                 double weirdness) {
        if (erosion == 0) {
            return pickPeak(temperature, weirdness);
        }
        if (erosion == 1) {
            return continentalness >= Continentalness.MID.ordinal()
                    ? pickPeak(temperature, weirdness)
                    : temperature == 0 ? pickSlope(temperature, humidity, weirdness)
                    : FFDVerticalBiome.NONE;
        }
        if (erosion == 2 && continentalness >= Continentalness.MID.ordinal()
                || erosion == 3 && continentalness == Continentalness.FAR.ordinal()) {
            return pickPlateau(temperature, humidity, weirdness);
        }
        return FFDVerticalBiome.NONE;
    }

    private static FFDVerticalBiome sampleHigh(int temperature, int humidity,
                                                int continentalness, int erosion,
                                                double weirdness) {
        if (erosion == 0) {
            if (continentalness == Continentalness.NEAR.ordinal()) {
                return pickSlope(temperature, humidity, weirdness);
            }
            if (continentalness >= Continentalness.MID.ordinal()) {
                return pickPeak(temperature, weirdness);
            }
        } else if (erosion == 1) {
            if (continentalness == Continentalness.NEAR.ordinal() && temperature == 0
                    || continentalness >= Continentalness.MID.ordinal()) {
                return pickSlope(temperature, humidity, weirdness);
            }
        } else if (erosion == 2 && continentalness >= Continentalness.MID.ordinal()
                || erosion == 3 && continentalness == Continentalness.FAR.ordinal()) {
            return pickPlateau(temperature, humidity, weirdness);
        }
        return FFDVerticalBiome.NONE;
    }

    private static FFDVerticalBiome sampleMid(int temperature, int humidity,
                                               int continentalness, int erosion,
                                               double weirdness) {
        if (erosion == 0 && continentalness >= Continentalness.NEAR.ordinal()) {
            return pickSlope(temperature, humidity, weirdness);
        }
        if (erosion == 1) {
            if (temperature == 0 && continentalness >= Continentalness.NEAR.ordinal()) {
                return pickSlope(temperature, humidity, weirdness);
            }
            if (continentalness == Continentalness.FAR.ordinal()) {
                return pickPlateau(temperature, humidity, weirdness);
            }
        }
        if (erosion == 2 && continentalness == Continentalness.FAR.ordinal()) {
            return pickPlateau(temperature, humidity, weirdness);
        }
        return FFDVerticalBiome.NONE;
    }

    private static FFDVerticalBiome sampleLow(int temperature, int humidity,
                                               int continentalness, int erosion,
                                               double weirdness) {
        return temperature == 0 && erosion <= 1
                && continentalness >= Continentalness.MID.ordinal()
                ? pickSlope(temperature, humidity, weirdness)
                : FFDVerticalBiome.NONE;
    }

    private static FFDVerticalBiome pickPeak(int temperature, double weirdness) {
        if (temperature <= 2) {
            return weirdness < 0.0D
                    ? FFDVerticalBiome.JAGGED_PEAKS : FFDVerticalBiome.FROZEN_PEAKS;
        }
        return temperature == 3 ? FFDVerticalBiome.STONY_PEAKS : FFDVerticalBiome.NONE;
    }

    private static FFDVerticalBiome pickSlope(int temperature, int humidity,
                                               double weirdness) {
        if (temperature >= 3) {
            return pickPlateau(temperature, humidity, weirdness);
        }
        return humidity <= 1
                ? FFDVerticalBiome.SNOWY_SLOPES : FFDVerticalBiome.GROVE;
    }

    private static FFDVerticalBiome pickPlateau(int temperature, int humidity,
                                                 double weirdness) {
        if (weirdness < 0.0D) {
            if (temperature == 1 && humidity <= 1
                    || temperature == 2 && humidity <= 3) {
                return FFDVerticalBiome.MEADOW;
            }
        } else if (temperature == 1 && humidity >= 1 && humidity <= 3) {
            return FFDVerticalBiome.MEADOW;
        }
        return FFDVerticalBiome.NONE;
    }

    private static int index(double value, double[] thresholds) {
        for (int index = 0; index < thresholds.length; index++) {
            if (value < thresholds[index]) {
                return index;
            }
        }
        return thresholds.length;
    }

    private static int continentalnessIndex(double continentalness) {
        if (continentalness < NEAR_INLAND_START) {
            return Continentalness.COAST.ordinal();
        }
        if (continentalness < MID_INLAND_START) {
            return Continentalness.NEAR.ordinal();
        }
        if (continentalness < FAR_INLAND_START) {
            return Continentalness.MID.ordinal();
        }
        return Continentalness.FAR.ordinal();
    }

    private static Slice slice(double weirdness) {
        double absolute = Math.abs(weirdness);
        if (absolute < VALLEY_END) {
            return Slice.VALLEY;
        }
        if (absolute < LOW_END) {
            return Slice.LOW;
        }
        if (absolute < MID_END || absolute >= OUTER_HIGH_END) {
            return Slice.MID;
        }
        if (absolute < HIGH_END || absolute >= PEAK_END) {
            return Slice.HIGH;
        }
        return Slice.PEAKS;
    }

    private static double distance(double value, double minimum, double maximum) {
        if (value < minimum) {
            return minimum - value;
        }
        return value > maximum ? value - maximum : 0.0D;
    }

    private static double square(double value) {
        return value * value;
    }

    private double sampleDepth(int blockX, double blockY, int blockZ) {
        int lowerY = (int) Math.floor(blockY);
        int upperY = Math.min(lowerY + 1,
                FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1);
        double lower = router.depth.sample(blockX, lowerY, blockZ);
        if (upperY == lowerY) {
            return lower;
        }
        double upper = router.depth.sample(blockX, upperY, blockZ);
        return lower + (blockY - lowerY) * (upper - lower);
    }

    private static double quantize(double value) {
        return (long) ((float) value * 10000.0F) / 10000.0D;
    }

    private static final class ColumnClimate {
        private final double humidity;
        private final double continentalness;
        private final int surfaceY;
        private final FFDVerticalBiome surfaceBiome;

        private ColumnClimate(double humidity, double continentalness, int surfaceY,
                              FFDVerticalBiome surfaceBiome) {
            this.humidity = humidity;
            this.continentalness = continentalness;
            this.surfaceY = surfaceY;
            this.surfaceBiome = surfaceBiome;
        }
    }

    private enum Continentalness {
        COAST,
        NEAR,
        MID,
        FAR
    }

    private enum Slice {
        VALLEY,
        LOW,
        MID,
        HIGH,
        PEAKS
    }
}
