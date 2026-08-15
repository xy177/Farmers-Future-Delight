package xy177.farmersfuturedelight.common.worldgen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class FFDWorldgenFeatureProfiler {
    static final boolean ENABLED = Boolean.getBoolean("ffd.worldgen.profile");

    private static final Logger LOGGER = LogManager.getLogger("FFD Feature Profile");
    private static final String[] VEGETATION_NAMES = {
            "lichen", "tall_grass", "ceiling_moss", "cave_vines", "clay",
            "floor_moss", "azalea", "spore", "classic_vines"
    };
    private static final long[] VEGETATION_NANOS = new long[VEGETATION_NAMES.length];
    private static final int ACCESS_COUNTERS = 8;
    private static final long[] VEGETATION_ACCESS =
            new long[VEGETATION_NAMES.length * ACCESS_COUNTERS];
    private static final int REPORT_INTERVAL = Math.max(1,
            Integer.getInteger("ffd.worldgen.profile.interval", 128));
    private static int vegetationChunks;

    private FFDWorldgenFeatureProfiler() {
    }

    static synchronized void recordVegetation(long[] stageNanos, long[] accessCounts) {
        vegetationChunks++;
        for (int index = 0; index < VEGETATION_NANOS.length; index++) {
            VEGETATION_NANOS[index] += stageNanos[index];
        }
        if (accessCounts != null) {
            for (int index = 0; index < VEGETATION_ACCESS.length; index++) {
                VEGETATION_ACCESS[index] += accessCounts[index];
            }
        }
        if (vegetationChunks % REPORT_INTERVAL != 0) {
            return;
        }
        StringBuilder result = new StringBuilder(256);
        result.append("vegetation_chunks=").append(vegetationChunks);
        for (int index = 0; index < VEGETATION_NAMES.length; index++) {
            result.append(' ').append(VEGETATION_NAMES[index]).append("_ms=")
                    .append(VEGETATION_NANOS[index] / 1_000_000L);
        }
        appendAccess(result, GLOW_LICHEN_STAGE, "lichen");
        appendAccess(result, CEILING_MOSS_STAGE, "ceiling_moss");
        LOGGER.info(result.toString());
    }

    private static final int GLOW_LICHEN_STAGE = 0;
    private static final int CEILING_MOSS_STAGE = 2;

    private static void appendAccess(StringBuilder result, int stage, String name) {
        int offset = stage * ACCESS_COUNTERS;
        result.append(' ').append(name).append("_target_reads=")
                .append(VEGETATION_ACCESS[offset])
                .append(' ').append(name).append("_neighbor_reads=")
                .append(VEGETATION_ACCESS[offset + 1])
                .append(' ').append(name).append("_face_queries=")
                .append(VEGETATION_ACCESS[offset + 2])
                .append(' ').append(name).append("_biome_queries=")
                .append(VEGETATION_ACCESS[offset + 3])
                .append(' ').append(name).append("_floor_queries=")
                .append(VEGETATION_ACCESS[offset + 4])
                .append(' ').append(name).append("_writes=")
                .append(VEGETATION_ACCESS[offset + 5])
                .append(' ').append(name).append("_unique_neighbor_reads=")
                .append(VEGETATION_ACCESS[offset + 6])
                .append(' ').append(name).append("_unique_floor_queries=")
                .append(VEGETATION_ACCESS[offset + 7]);
    }
}
