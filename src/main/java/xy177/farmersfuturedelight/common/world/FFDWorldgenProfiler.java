package xy177.farmersfuturedelight.common.world;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class FFDWorldgenProfiler {
    static final boolean ENABLED = Boolean.getBoolean("ffd.worldgen.profile");

    private static final Logger LOGGER = LogManager.getLogger("FFD Worldgen Profile");
    private static final String[] STAGE_NAMES = {
            "density", "base", "biomes", "surface", "carvers", "post_carvers",
            "geodes", "cave_local", "ores", "cave_decor", "cave_vegetation",
            "finalize"
    };
    private static final long[] TOTAL_NANOS = new long[STAGE_NAMES.length];
    private static long structureNanos;
    private static long skylightNanos;
    private static long caveSurfaceBuildNanos;
    private static long caveSurfaceStateNanos;
    private static long caveSurfacePatchNanos;
    private static long caveSurfaceBiomeNanos;
    private static long caveSurfaceFallbackNanos;
    private static long caveSurfaceCandidates;
    private static long caveSurfaceBases;
    private static long caveSurfaceBiomeQueries;
    private static long caveSurfaceFallbackQueries;
    private static long caveSurfaceWrites;
    private static final int REPORT_INTERVAL = Math.max(1,
            Integer.getInteger("ffd.worldgen.profile.interval", 128));
    private static long totalNanos;
    private static int chunks;

    private FFDWorldgenProfiler() {
    }

    static synchronized void record(long chunkNanos, long[] stageNanos,
                                    long structureStageNanos, long skylightStageNanos) {
        chunks++;
        totalNanos += chunkNanos;
        for (int index = 0; index < TOTAL_NANOS.length; index++) {
            TOTAL_NANOS[index] += stageNanos[index];
        }
        structureNanos += structureStageNanos;
        skylightNanos += skylightStageNanos;
        if (chunks % REPORT_INTERVAL == 0) {
            StringBuilder result = new StringBuilder(256);
            result.append("chunks=").append(chunks)
                    .append(" total_ms=").append(millis(totalNanos))
                    .append(" avg_ms=").append(millis(totalNanos) / chunks);
            for (int index = 0; index < STAGE_NAMES.length; index++) {
                result.append(' ').append(STAGE_NAMES[index]).append("_ms=")
                        .append(millis(TOTAL_NANOS[index]));
            }
            result.append(" structures_ms=").append(millis(structureNanos))
                    .append(" skylight_ms=").append(millis(skylightNanos))
                    .append(" cave_surface_build_ms=").append(millis(caveSurfaceBuildNanos))
                    .append(" cave_surface_state_sample_ms=").append(millis(caveSurfaceStateNanos))
                    .append(" cave_surface_patch_sample_ms=").append(millis(caveSurfacePatchNanos))
                    .append(" cave_surface_biome_sample_ms=").append(millis(caveSurfaceBiomeNanos))
                    .append(" cave_surface_fallback_sample_ms=").append(millis(caveSurfaceFallbackNanos))
                    .append(" cave_surface_candidates=").append(caveSurfaceCandidates)
                    .append(" cave_surface_bases=").append(caveSurfaceBases)
                    .append(" cave_surface_biome_queries=").append(caveSurfaceBiomeQueries)
                    .append(" cave_surface_fallback_queries=").append(caveSurfaceFallbackQueries)
                    .append(" cave_surface_writes=").append(caveSurfaceWrites);
            LOGGER.info(result.toString());
        }
    }

    static synchronized void recordCaveSurface(long buildNanos, long stateNanos,
                                                long patchNanos, long biomeNanos,
                                                long fallbackNanos, int candidates,
                                                int bases, int biomeQueries,
                                                int fallbackQueries, int writes) {
        caveSurfaceBuildNanos += buildNanos;
        caveSurfaceStateNanos += stateNanos;
        caveSurfacePatchNanos += patchNanos;
        caveSurfaceBiomeNanos += biomeNanos;
        caveSurfaceFallbackNanos += fallbackNanos;
        caveSurfaceCandidates += candidates;
        caveSurfaceBases += bases;
        caveSurfaceBiomeQueries += biomeQueries;
        caveSurfaceFallbackQueries += fallbackQueries;
        caveSurfaceWrites += writes;
    }

    private static long millis(long nanos) {
        return nanos / 1_000_000L;
    }
}
