package xy177.farmersfuturedelight.common.world.noise;

import java.util.Random;

public final class FFDSimplexNoise {
    private static final int[][] GRADIENTS = {
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
            {1, 0}, {-1, 0}, {1, 0}, {-1, 0},
            {0, 1}, {0, -1}, {0, 1}, {0, -1}
    };
    private static final double SQRT_THREE = Math.sqrt(3.0D);
    private static final double F2 = 0.5D * (SQRT_THREE - 1.0D);
    private static final double G2 = (3.0D - SQRT_THREE) / 6.0D;

    private final int[] permutations = new int[512];

    public FFDSimplexNoise(Random random) {
        random.nextDouble();
        random.nextDouble();
        random.nextDouble();
        for (int i = 0; i < 256; i++) {
            permutations[i] = i;
        }
        for (int i = 0; i < 256; i++) {
            int swap = random.nextInt(256 - i) + i;
            int value = permutations[i];
            permutations[i] = permutations[swap];
            permutations[swap] = value;
            permutations[i + 256] = permutations[i];
        }
    }

    public double sample(double x, double z) {
        double skew = (x + z) * F2;
        int cellX = fastFloor(x + skew);
        int cellZ = fastFloor(z + skew);
        double unskew = (cellX + cellZ) * G2;
        double localX = x - (cellX - unskew);
        double localZ = z - (cellZ - unskew);
        int offsetX = localX > localZ ? 1 : 0;
        int offsetZ = localX > localZ ? 0 : 1;
        double middleX = localX - offsetX + G2;
        double middleZ = localZ - offsetZ + G2;
        double lastX = localX - 1.0D + 2.0D * G2;
        double lastZ = localZ - 1.0D + 2.0D * G2;
        int wrappedX = cellX & 255;
        int wrappedZ = cellZ & 255;
        int firstGradient = permutations[wrappedX + permutations[wrappedZ]] % 12;
        int middleGradient = permutations[wrappedX + offsetX + permutations[wrappedZ + offsetZ]] % 12;
        int lastGradient = permutations[wrappedX + 1 + permutations[wrappedZ + 1]] % 12;
        return 70.0D * (contribution(firstGradient, localX, localZ)
                + contribution(middleGradient, middleX, middleZ)
                + contribution(lastGradient, lastX, lastZ));
    }

    private static double contribution(int gradient, double x, double z) {
        double attenuation = 0.5D - x * x - z * z;
        if (attenuation < 0.0D) {
            return 0.0D;
        }
        attenuation *= attenuation;
        int[] vector = GRADIENTS[gradient];
        return attenuation * attenuation * (vector[0] * x + vector[1] * z);
    }

    private static int fastFloor(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }
}
