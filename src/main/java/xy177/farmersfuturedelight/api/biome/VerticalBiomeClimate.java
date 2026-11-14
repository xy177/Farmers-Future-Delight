package xy177.farmersfuturedelight.api.biome;

import java.util.function.ToDoubleFunction;

public final class VerticalBiomeClimate implements ToDoubleFunction<VerticalBiomeSample> {
    private final double[] minimums;
    private final double[] maximums;
    private final int minY;
    private final int maxY;
    private final int surfaceBuffer;

    private VerticalBiomeClimate(Builder builder) {
        minimums = builder.minimums.clone();
        maximums = builder.maximums.clone();
        minY = builder.minY;
        maxY = builder.maxY;
        surfaceBuffer = builder.surfaceBuffer;
    }

    public static Builder cave() {
        return new Builder();
    }

    @Override
    public double applyAsDouble(VerticalBiomeSample sample) {
        if (sample.getY() < minY || sample.getY() > maxY
                || sample.getY() > (long) sample.getSurfaceY() - surfaceBuffer) {
            return Double.POSITIVE_INFINITY;
        }
        return squaredDistance(sample.getDepth(), 0)
                + squaredDistance(sample.getHumidity(), 1)
                + squaredDistance(sample.getContinentalness(), 2)
                + squaredDistance(sample.getTemperature(), 3)
                + squaredDistance(sample.getErosion(), 4)
                + squaredDistance(sample.getWeirdness(), 5);
    }

    private double squaredDistance(double value, int index) {
        double distance = value < minimums[index] ? minimums[index] - value
                : value > maximums[index] ? value - maximums[index] : 0.0D;
        return distance * distance;
    }

    public static final class Builder {
        private final double[] minimums = {0.2D, Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        private final double[] maximums = {0.9D, Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        private int minY = Integer.MIN_VALUE;
        private int maxY = Integer.MAX_VALUE;
        private int surfaceBuffer = 12;

        private Builder() {
        }

        public Builder depth(double minimum, double maximum) {
            return range(0, minimum, maximum);
        }

        public Builder humidity(double minimum, double maximum) {
            return range(1, minimum, maximum);
        }

        public Builder continentalness(double minimum, double maximum) {
            return range(2, minimum, maximum);
        }

        public Builder temperature(double minimum, double maximum) {
            return range(3, minimum, maximum);
        }

        public Builder erosion(double minimum, double maximum) {
            return range(4, minimum, maximum);
        }

        public Builder weirdness(double minimum, double maximum) {
            return range(5, minimum, maximum);
        }

        public Builder heightRange(int minimum, int maximum) {
            if (minimum > maximum) {
                throw new IllegalArgumentException("Minimum height exceeds maximum height");
            }
            minY = minimum;
            maxY = maximum;
            return this;
        }

        public Builder surfaceBuffer(int value) {
            if (value < 0) {
                throw new IllegalArgumentException("Surface buffer must be nonnegative");
            }
            surfaceBuffer = value;
            return this;
        }

        private Builder range(int index, double minimum, double maximum) {
            if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum > maximum) {
                throw new IllegalArgumentException("Climate range requires finite ordered bounds");
            }
            minimums[index] = minimum;
            maximums[index] = maximum;
            return this;
        }

        public VerticalBiomeClimate build() {
            return new VerticalBiomeClimate(this);
        }
    }
}
