package xy177.farmersfuturedelight.common.world.terrain;

import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver;

/**
 * Compatibility facade for the public 1.12 biome map.
 *
 * The old implementation shifted the modern density field toward legacy
 * biome height bands. That made two unrelated generators fight each other.
 * This facade deliberately leaves the modern terrain and aquifer heights
 * untouched; legacy biomes are exposed only for surface styles and gameplay.
 */
public final class FFDBiomeTerrainBridge {
    private final FFDModernBiomeResolver resolver;

    public FFDBiomeTerrainBridge(FFDModernBiomeResolver resolver) {
        this.resolver = resolver;
    }

    public boolean matchesModernBiome(int blockX, int blockZ, String target) {
        return resolver.matches(resolver.sampleFuzzy(blockX, blockZ), target);
    }
}
