package xy177.farmersfuturedelight.api.biome;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public interface VerticalBiomeGenerationContext {
    long getWorldSeed();

    int getChunkX();

    int getChunkZ();

    int getMinY();

    int getMaxYExclusive();

    VerticalBiomeDefinition getBiome();

    VerticalBiomeDefinition.Stage getStage();

    Random getRandom();

    IBlockState getBlockState(int x, int y, int z);

    boolean setBlockState(int x, int y, int z, IBlockState state);

    boolean isTargetChunk(int x, int z);

    @Nullable
    VerticalBiomeDefinition getBiome(int x, int y, int z);

    default boolean isBiome(int x, int y, int z, ResourceLocation biomeId) {
        VerticalBiomeDefinition biome = getBiome(x, y, z);
        return biome != null && biome.getRegistryName().equals(biomeId);
    }

    void markFluidUpdate(int x, int y, int z);

    int getOceanFloorY(int x, int z);
}
