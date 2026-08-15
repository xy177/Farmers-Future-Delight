package xy177.farmersfuturedelight.common.world.noise;

import java.util.Random;

/** The exact LegacyRandomSource initialization used by the modern geode feature. */
public final class FFDLegacyNormalNoise {
    private static final double SECOND_INPUT_FACTOR = 1.0181268882175227D;
    private static final double INPUT_FACTOR = 1.0D / 16.0D;
    private static final double VALUE_FACTOR = 5.0D / 6.0D;
    private static final int OCTAVE_HASH = "octave_-4".hashCode();

    private final FFDImprovedNoise first;
    private final FFDImprovedNoise second;

    public FFDLegacyNormalNoise(long seed) {
        Random source = new Random(seed);
        first = octave(source.nextLong());
        second = octave(source.nextLong());
    }

    public double sample(double x, double y, double z) {
        double firstValue = first.sample(x * INPUT_FACTOR, y * INPUT_FACTOR, z * INPUT_FACTOR);
        double secondValue = second.sample(x * SECOND_INPUT_FACTOR * INPUT_FACTOR,
                y * SECOND_INPUT_FACTOR * INPUT_FACTOR, z * SECOND_INPUT_FACTOR * INPUT_FACTOR);
        return (firstValue + secondValue) * VALUE_FACTOR;
    }

    private static FFDImprovedNoise octave(long positionalSeed) {
        return new FFDImprovedNoise(new Random(positionalSeed ^ OCTAVE_HASH));
    }
}
