package xy177.farmersfuturedelight.common.world.modern;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLongArray;

final class ModernTerrainProfiler {
    static final boolean ENABLED = Boolean.getBoolean("ffd.worldgen.profile");
    private static final String[] NAMES = {"surface", "carvers", "caveSurfaces", "local", "ores", "decoration", "vegetation"};
    private static final AtomicLongArray TOTALS = new AtomicLongArray(NAMES.length);
    private static final AtomicLongArray CAVE_SURFACE = new AtomicLongArray(10);

    private ModernTerrainProfiler() {
    }

    static void record(int stage, long nanos) {
        if (ENABLED) TOTALS.addAndGet(stage, nanos);
    }

    static void recordCaveSurface(long buildNanos, long stateNanos, long patchNanos,
                                  long biomeNanos, long fallbackNanos, int candidates,
                                  int bases, int biomeQueries, int fallbackQueries, int writes) {
        if (!ENABLED) return;
        long[] values = {buildNanos, stateNanos, patchNanos, biomeNanos, fallbackNanos,
                candidates, bases, biomeQueries, fallbackQueries, writes};
        for (int index = 0; index < values.length; index++) CAVE_SURFACE.addAndGet(index, values[index]);
    }

    static Map<String, Long> snapshot() {
        Map<String, Long> result = new LinkedHashMap<>();
        if (ENABLED) {
            for (int stage = 0; stage < NAMES.length; stage++) result.put(NAMES[stage] + "Nanos", TOTALS.get(stage));
            result.put("caveSurfaceCandidates", CAVE_SURFACE.get(5));
            result.put("caveSurfaceWrites", CAVE_SURFACE.get(9));
        }
        return result;
    }
}
