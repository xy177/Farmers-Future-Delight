package xy177.farmersfuturedelight.common.world.noise;

final class FFDSeed128 {
    final long low;
    final long high;

    FFDSeed128(long low, long high) {
        this.low = low;
        this.high = high;
    }

    FFDSeed128 xor(long low, long high) {
        return new FFDSeed128(this.low ^ low, this.high ^ high);
    }

    FFDSeed128 mixed() {
        return new FFDSeed128(mixStafford13(low), mixStafford13(high));
    }

    static FFDSeed128 upgrade(long seed) {
        long low = seed ^ 0x6A09E667F3BCC909L;
        return new FFDSeed128(low, low - 7046029254386353131L).mixed();
    }

    private static long mixStafford13(long value) {
        value = (value ^ value >>> 30) * -4658895280553007687L;
        value = (value ^ value >>> 27) * -7723592293110705685L;
        return value ^ value >>> 31;
    }
}
