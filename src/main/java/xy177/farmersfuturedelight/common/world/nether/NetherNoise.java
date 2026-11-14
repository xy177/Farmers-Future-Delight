package xy177.farmersfuturedelight.common.world.nether;

import java.util.Random;
import xy177.farmersfuturedelight.common.world.noise.FFDImprovedNoise;

public strictfp final class NetherNoise {
    private final Stack minimum;
    private final Stack maximum;
    private final Stack main;
    private final ClimateNoise temperature;
    private final ClimateNoise humidity;

    public NetherNoise(long seed) {
        Random random = new Random(seed);
        minimum = new Stack(random, 16, 684.412D * .375D * 8.0D, .9999847412109375D);
        maximum = new Stack(random, 16, 684.412D * .375D * 8.0D, .9999847412109375D);
        main = new Stack(random, 8, 684.412D * .375D * 8.0D / 60.0D, 12.75D);
        temperature = new ClimateNoise(seed);
        humidity = new ClimateNoise(seed + 1);
    }

    public float temperature(int blockX, int blockZ) {
        return temperature.sample(blockX * .25D, 0, blockZ * .25D);
    }

    public float humidity(int blockX, int blockZ) {
        return humidity.sample(blockX * .25D, 0, blockZ * .25D);
    }

    public float terrain(int blockX, int blockY, int blockZ) {
        double limitX = blockX * (684.412D * .25D);
        double limitY = blockY * (684.412D * .375D);
        double limitZ = blockZ * (684.412D * .25D);
        float alpha = clamp(main.sample(limitX / 80.0D, limitY / 60.0D, limitZ / 80.0D) + .5F);
        return lerp(alpha, minimum.sample(limitX, limitY, limitZ), maximum.sample(limitX, limitY, limitZ));
    }

    public float node(int blockX, int blockY, int blockZ) {
        float upperGradient = 1.0F + (Math.max(104, Math.min(128, blockY)) - 104) * (-1.0F / 24.0F);
        float top = lerp(upperGradient, .9375F, terrain(blockX, blockY, blockZ));
        return lerp(clamp((blockY + 8) / 32.0F), 2.5F, top) * .64F;
    }

    public float[] lattice(int chunkX, int chunkZ) {
        float[] values = new float[5 * 5 * 17];
        for (int cellZ = 0; cellZ <= 4; cellZ++) {
            for (int cellX = 0; cellX <= 4; cellX++) {
                for (int cellY = 0; cellY <= 16; cellY++) {
                    values[index(cellX, cellY, cellZ)] = node(chunkX * 16 + cellX * 4,
                            cellY * 8, chunkZ * 16 + cellZ * 4);
                }
            }
        }
        return values;
    }

    public static float density(float[] values, int localX, int blockY, int localZ) {
        int cellX = localX >> 2;
        int cellY = blockY >> 3;
        int cellZ = localZ >> 2;
        float alphaX = (localX & 3) * .25F;
        float alphaZ = (localZ & 3) * .25F;
        float low = plane(values, cellX, cellY, cellZ, alphaX, alphaZ);
        float high = plane(values, cellX, cellY + 1, cellZ, alphaX, alphaZ);
        float increment = (high - low) * .125F;
        for (int offsetY = 0; offsetY < (blockY & 7); offsetY++) low += increment;
        return squeeze(low);
    }

    public static void column(float[] values, int localX, int localZ, float[] output) {
        int cellX = localX >> 2;
        int cellZ = localZ >> 2;
        float alphaX = (localX & 3) * .25F;
        float alphaZ = (localZ & 3) * .25F;
        float low = plane(values, cellX, 0, cellZ, alphaX, alphaZ);
        for (int cellY = 0; cellY < 16; cellY++) {
            float high = plane(values, cellX, cellY + 1, cellZ, alphaX, alphaZ);
            float value = low;
            float increment = (high - low) * .125F;
            for (int offsetY = 0; offsetY < 8; offsetY++) {
                output[cellY * 8 + offsetY] = squeeze(value);
                value += increment;
            }
            low = high;
        }
    }

    private static float plane(float[] values, int cellX, int cellY, int cellZ, float alphaX, float alphaZ) {
        return lerp(alphaX,
                lerp(alphaZ, values[index(cellX, cellY, cellZ)], values[index(cellX, cellY, cellZ + 1)]),
                lerp(alphaZ, values[index(cellX + 1, cellY, cellZ)], values[index(cellX + 1, cellY, cellZ + 1)]));
    }

    private static float squeeze(float value) {
        float clamped = Math.max(-1.0F, Math.min(1.0F, value));
        return clamped / 2.0F - clamped * clamped * clamped / 24.0F;
    }

    private static int index(int cellX, int cellY, int cellZ) {
        return (cellZ * 5 + cellX) * 17 + cellY;
    }

    private static float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }

    private static float lerp(float alpha, float first, float second) {
        return first + alpha * (second - first);
    }

    private static final class Stack {
        private final FFDImprovedNoise[] levels;
        private final double[] frequencies;
        private final float[] amplitudes;
        private final double smearY;

        private Stack(Random random, int count, double smearY, double amplitude) {
            levels = new FFDImprovedNoise[count];
            frequencies = new double[count];
            amplitudes = new float[count];
            this.smearY = smearY;
            double frequency = 1;
            amplitude /= Math.pow(2, count) - 1;
            for (int octave = 0; octave < count; octave++) {
                levels[octave] = new FFDImprovedNoise(random);
                frequencies[octave] = frequency;
                amplitudes[octave] = (float) amplitude;
                frequency /= 2;
                amplitude *= 2;
            }
        }

        private float sample(double blockX, double blockY, double blockZ) {
            float value = 0;
            for (int octave = 0; octave < levels.length; octave++) {
                double frequency = frequencies[octave];
                value += amplitudes[octave] * levels[octave].sampleFloat(blockX * frequency,
                        blockY * frequency, blockZ * frequency, smearY * frequency);
            }
            return value;
        }
    }

    private static final class ClimateNoise {
        private final FFDImprovedNoise[] levels = new FFDImprovedNoise[4];
        private final float[] amplitudes = new float[4];

        private ClimateNoise(long seed) {
            Random random = new Random(seed);
            double baseAmplitude = .9494731054427981D;
            double lower = baseAmplitude * (2.0D / 3.0D);
            double upper = lower * .5D;
            double variance = Math.pow(.2702247831245211D * lower, 2)
                    + Math.pow(.2702247831245211D * upper, 2);
            float factor = (float) (((lower + upper) / 3.0D)
                    / (Math.sqrt(variance) * Math.sqrt(2.0D)) * baseAmplitude);
            for (int stack = 0; stack < 2; stack++) {
                new FFDImprovedNoise(random);
                for (int skipped = 6; skipped >= 2; skipped--) {
                    for (int draw = 0; draw < 262; draw++) random.nextInt();
                }
                levels[stack * 2 + 1] = new FFDImprovedNoise(random);
                levels[stack * 2] = new FFDImprovedNoise(random);
                amplitudes[stack * 2] = (float) (2.0D / 3.0D) * factor;
                amplitudes[stack * 2 + 1] = (float) (1.0D / 3.0D) * factor;
            }
        }

        private float sample(double blockX, double blockY, double blockZ) {
            float value = 0;
            for (int index = 0; index < levels.length; index++) {
                double frequency = Math.pow(2, -7 + (index & 1));
                if (index >= 2) frequency *= 1.0181268882175227D;
                value += amplitudes[index] * levels[index].sampleFloat(blockX * frequency,
                        blockY * frequency, blockZ * frequency);
            }
            return value;
        }
    }
}
