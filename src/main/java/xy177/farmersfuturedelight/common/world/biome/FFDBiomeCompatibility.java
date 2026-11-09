package xy177.farmersfuturedelight.common.world.biome;

import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class FFDBiomeCompatibility {
    private FFDBiomeCompatibility() {
    }

    public static boolean isKnown(ResourceLocation biomeId) {
        return ForgeRegistries.BIOMES.containsKey(biomeId)
                || "minecraft".equals(biomeId.getResourceDomain())
                && verticalBiome(biomeId.getResourcePath()) != FFDVerticalBiome.NONE;
    }

    public static boolean matches(World world, BlockPos position, ResourceLocation biomeId) {
        Biome actual = world.getBiome(position);
        ResourceLocation actualId = Biome.REGISTRY.getNameForObject(actual);
        if (biomeId.equals(actualId) || matchesLegacyBiome(biomeId, actual)) {
            return true;
        }
        if (!"minecraft".equals(biomeId.getResourceDomain())
                || !FFDHeightHooks.isExtended(world)) {
            return false;
        }
        FFDVerticalBiome vertical = FFDVerticalBiomeManager.getBiome(world, position);
        if (vertical.isMountain()) {
            vertical = FFDVerticalBiome.adaptSurface(vertical, actual, position);
        }
        return vertical != FFDVerticalBiome.NONE
                && biomeId.getResourcePath().equals(vertical.registryPath());
    }

    private static boolean matchesLegacyBiome(ResourceLocation expected, Biome actual) {
        if (!"minecraft".equals(expected.getResourceDomain())) {
            return false;
        }
        switch (expected.getResourcePath()) {
            case "jungle":
                return actual == Biomes.JUNGLE || actual == Biomes.MUTATED_JUNGLE;
            case "sparse_jungle":
                return actual == Biomes.JUNGLE_EDGE || actual == Biomes.MUTATED_JUNGLE_EDGE;
            case "bamboo_jungle":
                return actual == FFDBiomes.BAMBOO_JUNGLE;
            default:
                return false;
        }
    }

    private static FFDVerticalBiome verticalBiome(String path) {
        for (FFDVerticalBiome biome : FFDVerticalBiome.values()) {
            if (path.equals(biome.registryPath())) {
                return biome;
            }
        }
        return FFDVerticalBiome.NONE;
    }
}
