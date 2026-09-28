package xy177.farmersfuturedelight.common.world.terrain;

import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver;

public final class FFDBiomeTerrainBridge {
    private final FFDModernBiomeResolver resolver;

    public FFDBiomeTerrainBridge(FFDModernBiomeResolver resolver) {
        this.resolver = resolver;
    }

    public boolean matchesModernBiome(int blockX, int blockZ, String target) {
        return resolver.matches(resolver.sampleFuzzy(blockX, blockZ), target);
    }
}
