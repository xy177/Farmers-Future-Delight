package xy177.farmersfuturedelight.api.biome;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;

public final class VerticalBiomeRegistry {
    private VerticalBiomeRegistry() {
    }

    public static VerticalBiomeDefinition register(VerticalBiomeDefinition definition) {
        FFDVerticalBiome.register(Objects.requireNonNull(definition, "definition"));
        return definition;
    }

    @Nullable
    public static VerticalBiomeDefinition get(ResourceLocation registryName) {
        return FFDVerticalBiome.byRegistryName(registryName).definition();
    }

    public static Collection<VerticalBiomeDefinition> getEntries() {
        return FFDVerticalBiome.definitions();
    }

    @Nullable
    public static VerticalBiomeDefinition getBiome(World world, BlockPos pos) {
        return FFDVerticalBiomeManager.getBiome(world, pos).definition();
    }

    public static boolean isBiome(World world, BlockPos pos, ResourceLocation registryName) {
        VerticalBiomeDefinition biome = getBiome(world, pos);
        return biome != null && biome.getRegistryName().equals(registryName);
    }
}
