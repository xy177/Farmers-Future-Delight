package xy177.farmersfuturedelight.common.world.biome;

import java.util.Random;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorImproved;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.common.BiomeDictionary;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;

public final class GenLayerModernOcean extends GenLayer {
    private static final int WARM = 0;
    private static final int LUKEWARM = 1;
    private static final int NORMAL = 2;
    private static final int COLD = 3;
    private static final int FROZEN = 4;

    private final GenLayer temperatureLayer;
    private final int coordinateScale;

    public GenLayerModernOcean(long layerSeed, long worldSeed, GenLayer parent, int coordinateScale) {
        super(layerSeed);
        if (coordinateScale != 1 && coordinateScale != 4) {
            throw new IllegalArgumentException("Unsupported ocean coordinate scale: " + coordinateScale);
        }
        this.parent = parent;
        this.coordinateScale = coordinateScale;
        GenLayer temperatures = GenLayerZoom.magnify(2001L, new OceanTemperatureLayer(worldSeed), 6);
        this.temperatureLayer = coordinateScale == 1
                ? new GenLayerVoronoiZoom(10L, temperatures) : temperatures;
    }

    @Override
    public void initWorldGenSeed(long seed) {
        super.initWorldGenSeed(seed);
        temperatureLayer.initWorldGenSeed(seed);
    }

    @Override
    public int[] getInts(int areaX, int areaZ, int areaWidth, int areaHeight) {
        int coastRadius = Math.max(1, 32 / coordinateScale);
        int parentX = areaX - coastRadius;
        int parentZ = areaZ - coastRadius;
        int parentWidth = areaWidth + coastRadius * 2;
        int parentHeight = areaHeight + coastRadius * 2;
        int[] parentBiomes = parent.getInts(parentX, parentZ, parentWidth, parentHeight);
        int temperatureWidth = areaWidth + (coordinateScale == 1 ? 3 : 0);
        int temperatureHeight = areaHeight + (coordinateScale == 1 ? 3 : 0);
        int[] temperatures = temperatureLayer.getInts(areaX, areaZ, temperatureWidth, temperatureHeight);
        int[] result = IntCache.getIntCache(areaWidth * areaHeight);

        for (int z = 0; z < areaHeight; z++) {
            for (int x = 0; x < areaWidth; x++) {
                int parentIndex = x + coastRadius + (z + coastRadius) * parentWidth;
                int original = parentBiomes[parentIndex];
                if (!isVanillaOcean(original)) {
                    result[x + z * areaWidth] = original;
                    continue;
                }
                int temperature = temperatures[x + z * temperatureWidth];
                if ((temperature == WARM || temperature == FROZEN)
                        && hasNearbyLand(parentBiomes, parentWidth, parentHeight,
                        x + coastRadius, z + coastRadius, coastRadius)) {
                    temperature = temperature == WARM ? LUKEWARM : COLD;
                }
                boolean deep = original == Biome.getIdForBiome(Biomes.DEEP_OCEAN);
                result[x + z * areaWidth] = biomeId(temperature, deep);
            }
        }
        return result;
    }

    private static final class OceanTemperatureLayer extends GenLayer {
        private final NoiseGeneratorImproved noise;

        private OceanTemperatureLayer(long worldSeed) {
            super(2L);
            noise = new NoiseGeneratorImproved(new Random(worldSeed ^ 0x6A09E667F3BCC909L));
        }

        @Override
        public int[] getInts(int areaX, int areaZ, int areaWidth, int areaHeight) {
            double[] values = new double[areaWidth * areaHeight];
            noise.populateNoiseArray(values, areaX / 8.0D, 0.0D, areaZ / 8.0D,
                    areaWidth, 1, areaHeight, 0.125D, 1.0D, 0.125D, 1.0D);
            int[] result = IntCache.getIntCache(areaWidth * areaHeight);
            for (int z = 0; z < areaHeight; z++) {
                for (int x = 0; x < areaWidth; x++) {
                    result[x + z * areaWidth] = classify(values[x * areaHeight + z]);
                }
            }
            return result;
        }
    }

    private static int classify(double value) {
        if (value > 0.4D) {
            return WARM;
        }
        if (value > 0.2D) {
            return LUKEWARM;
        }
        if (value < -0.4D) {
            return FROZEN;
        }
        return value < -0.2D ? COLD : NORMAL;
    }

    private static int biomeId(int temperature, boolean deep) {
        if (temperature == WARM) {
            return Biome.getIdForBiome(FFDBiomes.WARM_OCEAN);
        }
        if (temperature == LUKEWARM) {
            return Biome.getIdForBiome(deep
                    ? FFDBiomes.DEEP_LUKEWARM_OCEAN : FFDBiomes.LUKEWARM_OCEAN);
        }
        if (temperature == COLD) {
            return Biome.getIdForBiome(deep
                    ? FFDBiomes.DEEP_COLD_OCEAN : FFDBiomes.COLD_OCEAN);
        }
        if (temperature == FROZEN) {
            return Biome.getIdForBiome(deep
                    ? FFDBiomes.DEEP_FROZEN_OCEAN : FFDBiomes.FROZEN_OCEAN);
        }
        return Biome.getIdForBiome(deep ? Biomes.DEEP_OCEAN : Biomes.OCEAN);
    }

    private static boolean hasNearbyLand(int[] biomes, int width, int height,
                                         int centerX, int centerZ, int radius) {
        for (int z = Math.max(0, centerZ - radius);
             z <= Math.min(height - 1, centerZ + radius); z += Math.max(1, radius / 2)) {
            for (int x = Math.max(0, centerX - radius);
                 x <= Math.min(width - 1, centerX + radius); x += Math.max(1, radius / 2)) {
                if (!isAnyOcean(biomes[x + z * width])) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isVanillaOcean(int id) {
        return id == Biome.getIdForBiome(Biomes.OCEAN)
                || id == Biome.getIdForBiome(Biomes.DEEP_OCEAN)
                || id == Biome.getIdForBiome(Biomes.FROZEN_OCEAN);
    }

    private static boolean isAnyOcean(int id) {
        Biome biome = Biome.getBiome(id);
        return biome != null && (biome == Biomes.OCEAN || biome == Biomes.DEEP_OCEAN
                || biome == Biomes.FROZEN_OCEAN
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN));
    }
}
