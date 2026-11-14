package xy177.farmersfuturedelight.common.world.nether;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import xy177.farmersfuturedelight.api.biome.VerticalBiomeDefinition;
import xy177.farmersfuturedelight.api.biome.VerticalBiomeSample;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeZoomer;

public final class NetherBiomeSource {
    private final long seed;
    private final long zoomSeed;
    private final NetherNoise noise;
    private final ThreadLocal<java.util.Map<Long, double[]>> climates = ThreadLocal.withInitial(() ->
            new java.util.LinkedHashMap<Long, double[]>(4096, .75F, true) {
                @Override protected boolean removeEldestEntry(java.util.Map.Entry<Long, double[]> eldest) { return size() > 4096; }
            });

    public NetherBiomeSource(long seed) {
        this.seed = seed;
        zoomSeed = FFDVerticalBiomeZoomer.obfuscateSeed(seed);
        noise = new NetherNoise(seed);
    }

    public NetherNoise noise() {
        return noise;
    }

    public FFDVerticalBiome sample(int quartX, int quartY, int quartZ) {
        Climate sample = new Climate(quartX * 4, quartY * 4, quartZ * 4);
        double best = Double.POSITIVE_INFINITY;
        int priority = Integer.MIN_VALUE;
        FFDVerticalBiome selected = FFDVerticalBiome.NETHER_WASTES;
        for (FFDVerticalBiome biome : FFDVerticalBiome.samplingEntries()) {
            VerticalBiomeDefinition definition = biome.definition();
            if (definition.getCategory() != VerticalBiomeDefinition.Category.NETHER || !available(biome)) continue;
            double fitness = definition.getSelectionRule().applyAsDouble(sample);
            if (!Double.isFinite(fitness) || fitness < 0) continue;
            if (definition.getPriority() > priority || definition.getPriority() == priority && fitness < best) {
                best = fitness;
                priority = definition.getPriority();
                selected = biome;
            }
        }
        return selected;
    }

    public FFDVerticalBiome get(int blockX, int blockY, int blockZ) {
        return FFDVerticalBiomeZoomer.getBiome(zoomSeed, blockX, blockY, blockZ, this::sample);
    }

    public FFDVerticalBiomeData sampleChunk(int chunkX, int chunkZ) {
        int[] cells = new int[FFDVerticalBiomeData.ENTRY_COUNT];
        for (int cellX = 0; cellX < 4; cellX++) {
            for (int cellZ = 0; cellZ < 4; cellZ++) {
                for (int cellY = 16; cellY < 80; cellY++) {
                    cells[(cellY * 4 + cellZ) * 4 + cellX] = sample(chunkX * 4 + cellX,
                            cellY - 16, chunkZ * 4 + cellZ).id();
                }
            }
        }
        return new FFDVerticalBiomeData(cells);
    }

    private static boolean available(FFDVerticalBiome biome) {
        if (biome == FFDVerticalBiome.CRIMSON_FOREST) return registered(FFDBiomes.CRIMSON_FOREST);
        if (biome == FFDVerticalBiome.WARPED_FOREST) return registered(FFDBiomes.WARPED_FOREST);
        return true;
    }

    private static boolean registered(Biome biome) {
        return biome.getRegistryName() != null && net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES
                .getValue(biome.getRegistryName()) == biome;
    }

    public static Biome legacy(FFDVerticalBiome biome) {
        if (biome == FFDVerticalBiome.CRIMSON_FOREST && registered(FFDBiomes.CRIMSON_FOREST)) return FFDBiomes.CRIMSON_FOREST;
        if (biome == FFDVerticalBiome.WARPED_FOREST && registered(FFDBiomes.WARPED_FOREST)) return FFDBiomes.WARPED_FOREST;
        return Biomes.HELL;
    }

    private final class Climate implements VerticalBiomeSample {
        private final int blockX;
        private final int blockY;
        private final int blockZ;
        private final double temperature;
        private final double humidity;

        private Climate(int blockX, int blockY, int blockZ) {
            this.blockX = blockX;
            this.blockY = blockY;
            this.blockZ = blockZ;
            long key = (blockX & 0xFFFFFFFFL) | (long) blockZ << 32;
            double[] values = climates.get().get(key);
            if (values == null) {
                values = new double[] {(long) (noise.temperature(blockX, blockZ) * 10000.0F) / 10000.0D,
                        (long) (noise.humidity(blockX, blockZ) * 10000.0F) / 10000.0D};
                climates.get().put(key, values);
            }
            temperature = values[0];
            humidity = values[1];
        }

        @Override public int getDimension() { return -1; }
        @Override public long getWorldSeed() { return seed; }
        @Override public int getX() { return blockX; }
        @Override public int getY() { return blockY; }
        @Override public int getZ() { return blockZ; }
        @Override public int getSurfaceY() { return 128; }
        @Override public double getTemperature() { return temperature; }
        @Override public double getHumidity() { return humidity; }
        @Override public double getContinentalness() { return 0; }
        @Override public double getErosion() { return 0; }
        @Override public double getWeirdness() { return 0; }
        @Override public double getDepth() { return 0; }
    }
}
