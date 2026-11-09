package xy177.farmersfuturedelight.common.world.noise;

import java.util.Random;

public final strictfp class FFDFlowerNoise {
    private static final double SECOND_INPUT_FACTOR = 1.0181268882175227D;
    private static final float VALUE_FACTOR = (float) (0.955388882960065D
            / (3.0D * 0.2702247831245211D * Math.sqrt(2.0D)));
    private static final int OCTAVE_HASH = "octave_0".hashCode();

    private final FFDImprovedNoise first;
    private final FFDImprovedNoise second;

    public FFDFlowerNoise(long seed) {
        Random random = new Random(seed);
        first = new FFDImprovedNoise(new Random(random.nextLong() ^ OCTAVE_HASH));
        second = new FFDImprovedNoise(new Random(random.nextLong() ^ OCTAVE_HASH));
    }

    public float sample(double x, double y, double z) {
        float value = VALUE_FACTOR * first.sampleFloat(x, y, z);
        value += VALUE_FACTOR * second.sampleFloat(x * SECOND_INPUT_FACTOR,
                y * SECOND_INPUT_FACTOR, z * SECOND_INPUT_FACTOR);
        return value;
    }
}
