package xy177.farmersfuturedelight.common.world.terrain;

import java.util.Random;
import xy177.farmersfuturedelight.common.world.noise.FFDSimplexNoise;

final class FFDEndIslandDensity implements FFDDensityFunction {
    private final FFDSimplexNoise islandNoise;

    FFDEndIslandDensity(long seed) {
        Random random = new Random(seed);
        for (int i = 0; i < 17292; i++) {
            random.nextInt();
        }
        islandNoise = new FFDSimplexNoise(random);
    }

    @Override
    public double sample(int x, int y, int z) {
        return (heightValue(x / 8, z / 8) - 8.0D) / 128.0D;
    }

    private float heightValue(int x, int z) {
        int halfX = x / 2;
        int halfZ = z / 2;
        int remainderX = x % 2;
        int remainderZ = z % 2;
        float height = clamp(100.0F - (float) Math.sqrt((float) (x * x + z * z)) * 8.0F,
                -100.0F, 80.0F);

        for (int offsetX = -12; offsetX <= 12; offsetX++) {
            for (int offsetZ = -12; offsetZ <= 12; offsetZ++) {
                long sampleX = halfX + offsetX;
                long sampleZ = halfZ + offsetZ;
                if (sampleX * sampleX + sampleZ * sampleZ <= 4096L
                        || islandNoise.sample(sampleX, sampleZ) >= -0.8999999761581421D) {
                    continue;
                }
                float scale = (Math.abs(sampleX) * 3439.0F + Math.abs(sampleZ) * 147.0F) % 13.0F + 9.0F;
                float dx = remainderX - offsetX * 2.0F;
                float dz = remainderZ - offsetZ * 2.0F;
                float candidate = clamp(100.0F - (float) Math.sqrt(dx * dx + dz * dz) * scale,
                        -100.0F, 80.0F);
                height = Math.max(height, candidate);
            }
        }
        return height;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
