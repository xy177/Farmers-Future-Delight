package xy177.farmersfuturedelight.common.world.modern;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

final class ModernNodeCache {
    private static final int SEGMENT_COUNT = 16;
    private static final int SEGMENT_CAPACITY = 4096;
    private final Segment[] segments = new Segment[SEGMENT_COUNT];

    ModernNodeCache() {
        for (int index = 0; index < segments.length; index++) {
            segments[index] = new Segment();
        }
    }

    double[] get(long key) {
        Segment segment = segment(key);
        synchronized (segment) {
            return segment.values.getAndMoveToLast(key);
        }
    }

    void put(long key, double[] values) {
        Segment segment = segment(key);
        synchronized (segment) {
            if (segment.values.getAndMoveToLast(key) != null) return;
            segment.values.put(key, values);
            if (segment.values.size() > SEGMENT_CAPACITY) {
                segment.values.removeFirst();
            }
        }
    }

    int size() {
        int size = 0;
        for (Segment segment : segments) {
            synchronized (segment) {
                size += segment.values.size();
            }
        }
        return size;
    }

    void clear() {
        for (Segment segment : segments) {
            synchronized (segment) {
                segment.values.clear();
            }
        }
    }

    private Segment segment(long key) {
        return segments[(int) (HashCommon.mix(key) >>> 60)];
    }

    private static final class Segment {
        private final Long2ObjectLinkedOpenHashMap<double[]> values =
                new Long2ObjectLinkedOpenHashMap<>(SEGMENT_CAPACITY);
    }
}
