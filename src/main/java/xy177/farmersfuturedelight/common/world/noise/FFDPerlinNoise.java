package xy177.farmersfuturedelight.common.world.noise;

import java.util.Arrays;

public final class FFDPerlinNoise {
    private final FFDImprovedNoise[] levels;
    private final double[] amplitudes;
    private final double lowestFrequencyInputFactor;
    private final double lowestFrequencyValueFactor;
    private final double maxValue;

    private FFDPerlinNoise(FFDXoroshiroRandom random, int firstOctave, double[] amplitudes,
                           boolean newInitialization) {
        this.amplitudes = Arrays.copyOf(amplitudes, amplitudes.length);
        levels = new FFDImprovedNoise[amplitudes.length];
        int zeroOctaveIndex = -firstOctave;
        if (newInitialization) {
            FFDXoroshiroRandom.PositionalFactory positional = random.forkPositional();
            for (int i = 0; i < amplitudes.length; i++) {
                if (amplitudes[i] != 0.0D) {
                    levels[i] = new FFDImprovedNoise(positional.fromHashOf("octave_" + (firstOctave + i)));
                }
            }
        } else {
            FFDImprovedNoise zeroOctave = new FFDImprovedNoise(random);
            if (zeroOctaveIndex >= 0 && zeroOctaveIndex < amplitudes.length && amplitudes[zeroOctaveIndex] != 0.0D) {
                levels[zeroOctaveIndex] = zeroOctave;
            }
            for (int i = zeroOctaveIndex - 1; i >= 0; i--) {
                if (i < amplitudes.length && amplitudes[i] != 0.0D) {
                    levels[i] = new FFDImprovedNoise(random);
                } else {
                    random.consume(262);
                }
            }
            if (zeroOctaveIndex < amplitudes.length - 1) {
                throw new IllegalArgumentException("Positive octaves are not supported by legacy initialization");
            }
        }
        lowestFrequencyInputFactor = Math.pow(2.0D, -zeroOctaveIndex);
        lowestFrequencyValueFactor = Math.pow(2.0D, amplitudes.length - 1)
                / (Math.pow(2.0D, amplitudes.length) - 1.0D);
        maxValue = edgeValue(2.0D);
    }

    public static FFDPerlinNoise create(FFDXoroshiroRandom random, int firstOctave, double[] amplitudes) {
        return new FFDPerlinNoise(random, firstOctave, amplitudes, true);
    }

    public static FFDPerlinNoise createLegacy(FFDXoroshiroRandom random, int firstOctave, double[] amplitudes) {
        return new FFDPerlinNoise(random, firstOctave, amplitudes, false);
    }

    public double sample(double x, double y, double z) {
        return sample(x, y, z, 0.0D, 0.0D);
    }

    public double sample(double x, double y, double z, double yScale, double yFudge) {
        double value = 0.0D;
        double inputFactor = lowestFrequencyInputFactor;
        double valueFactor = lowestFrequencyValueFactor;
        for (int i = 0; i < levels.length; i++) {
            FFDImprovedNoise noise = levels[i];
            if (noise != null) {
                value += amplitudes[i] * noise.sample(wrap(x * inputFactor), wrap(y * inputFactor),
                        wrap(z * inputFactor), yScale * inputFactor, yFudge * inputFactor) * valueFactor;
            }
            inputFactor *= 2.0D;
            valueFactor /= 2.0D;
        }
        return value;
    }

    public FFDImprovedNoise octave(int octave) {
        return levels[levels.length - 1 - octave];
    }

    public double maxValue() {
        return maxValue;
    }

    public double maxBrokenValue(double yScale) {
        return edgeValue(yScale + 2.0D);
    }

    private double edgeValue(double noiseValue) {
        double value = 0.0D;
        double factor = lowestFrequencyValueFactor;
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] != null) {
                value += amplitudes[i] * noiseValue * factor;
            }
            factor /= 2.0D;
        }
        return value;
    }

    public static double wrap(double value) {
        return value - Math.floor(value / 3.3554432E7D + 0.5D) * 3.3554432E7D;
    }
}
