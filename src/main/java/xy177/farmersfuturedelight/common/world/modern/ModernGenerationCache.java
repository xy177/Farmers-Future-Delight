package xy177.farmersfuturedelight.common.world.modern;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.HashCommon;
import java.util.concurrent.ExecutionException;

final class ModernGenerationCache {
    private final Cache<Long, ModernNoiseChunk> noise = CacheBuilder.newBuilder().maximumSize(256).build();
    private final ModernNodeCache nodes = new ModernNodeCache();
    private final Cache<Long, Integer> heights = CacheBuilder.newBuilder().maximumSize(65536).build();
    private final Cache<Long, ModernNeighborStates> states = CacheBuilder.newBuilder().maximumSize(256).build();

    ModernNoiseChunk noise(ModernNoiseContext context, int chunkX, int chunkZ) {
        long key = HashCommon.mix((chunkX & 0xFFFFFFFFL) | (long) chunkZ << 32);
        try {
            return noise.get(key, () -> new ModernNoiseChunk(context, chunkX, chunkZ));
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Cannot sample modern noise at " + chunkX + "," + chunkZ,
                    exception.getCause());
        }
    }

    double[] node(long key) {
        return nodes.get(key);
    }

    void rememberNode(long key, double[] values) {
        nodes.put(key, values);
    }

    int preliminaryHeight(ModernNoiseContext context, int blockX, int blockZ) {
        long key = HashCommon.mix((blockX & 0xFFFFFFFFL) | (long) blockZ << 32);
        Integer height = heights.getIfPresent(key);
        if (height == null) {
            height = context.data.preliminarySurfaceLevel(blockX, blockZ);
            heights.asMap().putIfAbsent(key, height);
        }
        return height;
    }

    ModernNeighborStates states(int chunkX, int chunkZ) {
        long key = HashCommon.mix((chunkX & 0xFFFFFFFFL) | (long) chunkZ << 32);
        try {
            return states.get(key, ModernNeighborStates::new);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Cannot cache modern neighbor terrain at " + chunkX + "," + chunkZ,
                    exception.getCause());
        }
    }

    void clear() {
        noise.invalidateAll();
        nodes.clear();
        heights.invalidateAll();
        states.invalidateAll();
    }

    void statistics(java.util.Map<String, Long> result) {
        result.put("cachedNoise", noise.size());
        result.put("cachedNodes", (long) nodes.size());
        result.put("cachedHeights", heights.size());
        long stateCount = 0L;
        long sectionCount = 0L;
        for (ModernNeighborStates cached : states.asMap().values()) {
            stateCount += cached.populatedStates();
            sectionCount += cached.allocatedSections();
        }
        result.put("cachedStates", stateCount);
        result.put("cachedStateChunks", states.size());
        result.put("cachedStateSections", sectionCount);
    }
}
