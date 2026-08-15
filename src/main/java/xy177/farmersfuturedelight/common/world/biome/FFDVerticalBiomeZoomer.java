package xy177.farmersfuturedelight.common.world.biome;

import com.google.common.hash.Hashing;

public final class FFDVerticalBiomeZoomer {
    private static final long MULTIPLIER = 6364136223846793005L;
    private static final long INCREMENT = 1442695040888963407L;

    private FFDVerticalBiomeZoomer() {
    }

    public static long obfuscateSeed(long seed) {
        return Hashing.sha256().hashLong(seed).asLong();
    }

    public static FFDVerticalBiome getBiome(long zoomSeed, int blockX, int blockY, int blockZ,
                                             Source source) {
        long quart = nearestQuartXZ(zoomSeed, blockX, blockY, blockZ);
        int quartX = (int) (quart >> 32);
        int quartZ = (int) quart;
        int quartY = nearestQuartY(zoomSeed, blockX, blockY, blockZ);
        return source.getNoiseBiome(quartX, quartY, quartZ);
    }

    public static long nearestQuartXZ(long zoomSeed, int blockX, int blockY, int blockZ) {
        int corner = nearestCorner(zoomSeed, blockX, blockY, blockZ);
        int offsetX = blockX - 2;
        int offsetZ = blockZ - 2;
        int parentX = offsetX >> 2;
        int parentZ = offsetZ >> 2;
        int quartX = (corner & 4) == 0 ? parentX : parentX + 1;
        int quartZ = (corner & 1) == 0 ? parentZ : parentZ + 1;
        return (long) quartX << 32 | quartZ & 0xFFFFFFFFL;
    }

    private static int nearestQuartY(long zoomSeed, int blockX, int blockY, int blockZ) {
        int corner = nearestCorner(zoomSeed, blockX, blockY, blockZ);
        int parentY = blockY - 2 >> 2;
        return (corner & 2) == 0 ? parentY : parentY + 1;
    }

    private static int nearestCorner(long zoomSeed, int blockX, int blockY, int blockZ) {
        int offsetX = blockX - 2;
        int offsetY = blockY - 2;
        int offsetZ = blockZ - 2;
        int parentX = offsetX >> 2;
        int parentY = offsetY >> 2;
        int parentZ = offsetZ >> 2;
        double fractionX = (offsetX & 3) / 4.0D;
        double fractionY = (offsetY & 3) / 4.0D;
        double fractionZ = (offsetZ & 3) / 4.0D;
        int nearestCorner = 0;
        double nearestDistance = Double.POSITIVE_INFINITY;

        for (int corner = 0; corner < 8; corner++) {
            boolean lowX = (corner & 4) == 0;
            boolean lowY = (corner & 2) == 0;
            boolean lowZ = (corner & 1) == 0;
            int quartX = lowX ? parentX : parentX + 1;
            int quartY = lowY ? parentY : parentY + 1;
            int quartZ = lowZ ? parentZ : parentZ + 1;
            double distance = fiddledDistance(zoomSeed, quartX, quartY, quartZ,
                    lowX ? fractionX : fractionX - 1.0D,
                    lowY ? fractionY : fractionY - 1.0D,
                    lowZ ? fractionZ : fractionZ - 1.0D);
            if (nearestDistance > distance) {
                nearestCorner = corner;
                nearestDistance = distance;
            }
        }

        return nearestCorner;
    }

    private static double fiddledDistance(long seed, int x, int y, int z,
                                            double distanceX, double distanceY, double distanceZ) {
        long random = seed;
        random = next(random, x);
        random = next(random, y);
        random = next(random, z);
        random = next(random, x);
        random = next(random, y);
        random = next(random, z);
        double fiddleX = fiddle(random);
        random = next(random, seed);
        double fiddleY = fiddle(random);
        random = next(random, seed);
        double fiddleZ = fiddle(random);
        return square(distanceZ + fiddleZ)
                + square(distanceY + fiddleY)
                + square(distanceX + fiddleX);
    }

    private static long next(long value, long salt) {
        value *= value * MULTIPLIER + INCREMENT;
        return value + salt;
    }

    private static double fiddle(long value) {
        double uniform = Math.floorMod(value >> 24, 1024) / 1024.0D;
        return (uniform - 0.5D) * 0.9D;
    }

    private static double square(double value) {
        return value * value;
    }

    public interface Source {
        FFDVerticalBiome getNoiseBiome(int quartX, int quartY, int quartZ);
    }
}
