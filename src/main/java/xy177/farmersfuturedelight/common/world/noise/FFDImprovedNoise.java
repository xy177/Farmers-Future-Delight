package xy177.farmersfuturedelight.common.world.noise;

import java.util.Random;

public final class FFDImprovedNoise {
    private static final int[] GRADIENT_X = {
            1, -1, 1, -1, 1, -1, 1, -1, 0, 0, 0, 0, 1, 0, -1, 0
    };
    private static final int[] GRADIENT_Y = {
            1, 1, -1, -1, 0, 0, 0, 0, 1, -1, 1, -1, 1, -1, 1, -1
    };
    private static final int[] GRADIENT_Z = {
            0, 0, 0, 0, 1, 1, -1, -1, 1, 1, -1, -1, 0, 1, 0, -1
    };

    private final int[] permutations = new int[256];
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    FFDImprovedNoise(FFDXoroshiroRandom random) {
        offsetX = random.nextDouble() * 256.0D;
        offsetY = random.nextDouble() * 256.0D;
        offsetZ = random.nextDouble() * 256.0D;
        for (int i = 0; i < 256; i++) {
            permutations[i] = i;
        }
        for (int i = 0; i < 256; i++) {
            int offset = random.nextInt(256 - i);
            int value = permutations[i];
            permutations[i] = permutations[i + offset];
            permutations[i + offset] = value;
        }
    }

    FFDImprovedNoise(Random random) {
        offsetX = random.nextDouble() * 256.0D;
        offsetY = random.nextDouble() * 256.0D;
        offsetZ = random.nextDouble() * 256.0D;
        for (int i = 0; i < 256; i++) {
            permutations[i] = i;
        }
        for (int i = 0; i < 256; i++) {
            int offset = random.nextInt(256 - i);
            int value = permutations[i];
            permutations[i] = permutations[i + offset];
            permutations[i + offset] = value;
        }
    }

    public double sample(double x, double y, double z) {
        return sample(x, y, z, 0.0D, 0.0D);
    }

    public double sample(double inputX, double inputY, double inputZ, double yScale, double yFudge) {
        double x = inputX + offsetX;
        double y = inputY + offsetY;
        double z = inputZ + offsetZ;
        int floorX = floor(x);
        int floorY = floor(y);
        int floorZ = floor(z);
        double localX = x - floorX;
        double localY = y - floorY;
        double localZ = z - floorZ;
        double adjustedY = 0.0D;
        if (yScale != 0.0D) {
            double limit = yFudge >= 0.0D && yFudge < localY ? yFudge : localY;
            adjustedY = Math.floor(limit / yScale + 1.0E-7D) * yScale;
        }
        return sampleAndLerp(floorX, floorY, floorZ, localX, localY - adjustedY, localZ, localY);
    }

    private double sampleAndLerp(int x, int y, int z, double localX, double localY, double localZ,
                                 double originalY) {
        int x0 = permutations[x & 255];
        int x1 = permutations[(x + 1) & 255];
        int xy00 = permutations[(x0 + y) & 255];
        int xy01 = permutations[(x0 + y + 1) & 255];
        int xy10 = permutations[(x1 + y) & 255];
        int xy11 = permutations[(x1 + y + 1) & 255];
        double d000 = gradient(permutations[(xy00 + z) & 255], localX, localY, localZ);
        double d100 = gradient(permutations[(xy10 + z) & 255], localX - 1.0D, localY, localZ);
        double d010 = gradient(permutations[(xy01 + z) & 255], localX, localY - 1.0D, localZ);
        double d110 = gradient(permutations[(xy11 + z) & 255], localX - 1.0D, localY - 1.0D, localZ);
        double d001 = gradient(permutations[(xy00 + z + 1) & 255], localX, localY, localZ - 1.0D);
        double d101 = gradient(permutations[(xy10 + z + 1) & 255], localX - 1.0D, localY, localZ - 1.0D);
        double d011 = gradient(permutations[(xy01 + z + 1) & 255], localX, localY - 1.0D, localZ - 1.0D);
        double d111 = gradient(permutations[(xy11 + z + 1) & 255], localX - 1.0D, localY - 1.0D, localZ - 1.0D);
        return lerp3(smoothstep(localX), smoothstep(originalY), smoothstep(localZ),
                d000, d100, d010, d110, d001, d101, d011, d111);
    }

    private static double gradient(int hash, double x, double y, double z) {
        int index = hash & 15;
        return GRADIENT_X[index] * x + GRADIENT_Y[index] * y + GRADIENT_Z[index] * z;
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    private static double smoothstep(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private static double lerp(double amount, double first, double second) {
        return first + amount * (second - first);
    }

    private static double lerp2(double x, double y, double x0y0, double x1y0, double x0y1, double x1y1) {
        return lerp(y, lerp(x, x0y0, x1y0), lerp(x, x0y1, x1y1));
    }

    private static double lerp3(double x, double y, double z, double d000, double d100, double d010, double d110,
                                double d001, double d101, double d011, double d111) {
        return lerp(z, lerp2(x, y, d000, d100, d010, d110), lerp2(x, y, d001, d101, d011, d111));
    }
}
