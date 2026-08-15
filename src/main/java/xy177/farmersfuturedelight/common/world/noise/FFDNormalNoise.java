package xy177.farmersfuturedelight.common.world.noise;

public final class FFDNormalNoise {
    private static final double SECOND_INPUT_FACTOR = 1.0181268882175227D;

    private final FFDPerlinNoise first;
    private final FFDPerlinNoise second;
    private final double valueFactor;
    private final double maxValue;

    public FFDNormalNoise(FFDXoroshiroRandom random, int firstOctave, double[] amplitudes) {
        first = FFDPerlinNoise.create(random, firstOctave, amplitudes);
        second = FFDPerlinNoise.create(random, firstOctave, amplitudes);
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < amplitudes.length; i++) {
            if (amplitudes[i] != 0.0D) {
                min = Math.min(min, i);
                max = Math.max(max, i);
            }
        }
        if (min == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Normal noise needs at least one non-zero amplitude");
        }
        valueFactor = 1.0D / 6.0D / expectedDeviation(max - min);
        maxValue = (first.maxValue() + second.maxValue()) * valueFactor;
    }

    public double sample(double x, double y, double z) {
        return (first.sample(x, y, z) + second.sample(x * SECOND_INPUT_FACTOR, y * SECOND_INPUT_FACTOR,
                z * SECOND_INPUT_FACTOR)) * valueFactor;
    }

    public double maxValue() {
        return maxValue;
    }

    private static double expectedDeviation(int octaveSpan) {
        return 0.1D * (1.0D + 1.0D / (octaveSpan + 1.0D));
    }
}
