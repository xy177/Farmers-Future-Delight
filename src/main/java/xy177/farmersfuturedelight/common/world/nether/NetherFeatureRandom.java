package xy177.farmersfuturedelight.common.world.nether;

import java.util.Random;
import xy177.farmersfuturedelight.common.world.noise.FFDXoroshiroRandom;

final class NetherFeatureRandom extends Random {
    private FFDXoroshiroRandom source;

    NetherFeatureRandom(long seed) { super(seed); }

    @Override public void setSeed(long seed) { source = new FFDXoroshiroRandom(seed); }
    @Override protected int next(int bits) { return (int) (source.nextLong() >>> 64 - bits); }
    @Override public int nextInt() { return source.nextInt(); }
    @Override public int nextInt(int bound) { return source.nextInt(bound); }
    @Override public long nextLong() { return source.nextLong(); }
    @Override public float nextFloat() { return source.nextFloat(); }
    @Override public double nextDouble() { return source.nextDouble(); }
    @Override public boolean nextBoolean() { return source.nextBoolean(); }

    long decorationSeed(long worldSeed, int blockX, int blockZ) {
        setSeed(worldSeed);
        long first = nextLong() | 1L;
        long second = nextLong() | 1L;
        long result = (blockX * first + blockZ * second) ^ worldSeed;
        setSeed(result);
        return result;
    }
}
