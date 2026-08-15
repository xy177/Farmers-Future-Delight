package xy177.farmersfuturedelight.common.world.noise;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FFDXoroshiroRandom {
    private long seedLow;
    private long seedHigh;

    public FFDXoroshiroRandom(long seed) {
        this(FFDSeed128.upgrade(seed));
    }

    FFDXoroshiroRandom(FFDSeed128 seed) {
        this(seed.low, seed.high);
    }

    FFDXoroshiroRandom(long seedLow, long seedHigh) {
        this.seedLow = seedLow;
        this.seedHigh = seedHigh;
        if ((seedLow | seedHigh) == 0L) {
            this.seedLow = -7046029254386353131L;
            this.seedHigh = 7640891576956012809L;
        }
    }

    public long nextLong() {
        long low = seedLow;
        long high = seedHigh;
        long result = Long.rotateLeft(low + high, 17) + low;
        high ^= low;
        seedLow = Long.rotateLeft(low, 49) ^ high ^ high << 21;
        seedHigh = Long.rotateLeft(high, 28);
        return result;
    }

    public int nextInt() {
        return (int) nextLong();
    }

    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        long randomBits = Integer.toUnsignedLong(nextInt());
        long multiplied = randomBits * bound;
        long fractional = multiplied & 0xFFFFFFFFL;
        if (fractional < bound) {
            int unbiasedStart = Integer.remainderUnsigned(~bound + 1, bound);
            while (fractional < Integer.toUnsignedLong(unbiasedStart)) {
                randomBits = Integer.toUnsignedLong(nextInt());
                multiplied = randomBits * bound;
                fractional = multiplied & 0xFFFFFFFFL;
            }
        }
        return (int) (multiplied >>> 32);
    }

    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    public float nextFloat() {
        return (nextLong() >>> 40) * 0x1.0p-24F;
    }

    public boolean nextBoolean() {
        return (nextLong() & 1L) != 0L;
    }

    public void consume(int count) {
        for (int i = 0; i < count; i++) {
            nextLong();
        }
    }

    public FFDXoroshiroRandom fork() {
        return new FFDXoroshiroRandom(nextLong(), nextLong());
    }

    public PositionalFactory forkPositional() {
        return new PositionalFactory(nextLong(), nextLong());
    }

    public static final class PositionalFactory {
        private final long seedLow;
        private final long seedHigh;

        private PositionalFactory(long seedLow, long seedHigh) {
            this.seedLow = seedLow;
            this.seedHigh = seedHigh;
        }

        public FFDXoroshiroRandom fromHashOf(String name) {
            FFDSeed128 hash = hash(name);
            return new FFDXoroshiroRandom(hash.xor(seedLow, seedHigh));
        }

        public FFDXoroshiroRandom fromSeed(long seed) {
            return new FFDXoroshiroRandom(seed ^ seedLow, seed ^ seedHigh);
        }

        public FFDXoroshiroRandom at(int x, int y, int z) {
            long positionalSeed = (long) (x * 3129871) ^ (long) z * 116129781L ^ y;
            positionalSeed = positionalSeed * positionalSeed * 42317861L + positionalSeed * 11L;
            return new FFDXoroshiroRandom((positionalSeed >> 16) ^ seedLow, seedHigh);
        }

        private static FFDSeed128 hash(String name) {
            try {
                byte[] bytes = MessageDigest.getInstance("MD5").digest(name.getBytes(StandardCharsets.UTF_8));
                ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
                return new FFDSeed128(buffer.getLong(), buffer.getLong());
            } catch (NoSuchAlgorithmException exception) {
                throw new IllegalStateException("MD5 is unavailable", exception);
            }
        }
    }
}
