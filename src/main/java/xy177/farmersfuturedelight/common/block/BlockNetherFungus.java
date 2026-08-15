package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.worldgen.WorldGenHugeFungus;
import xy177.farmersfuturedelight.common.worldgen.FFDNetherBlockProvider;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public class BlockNetherFungus extends BlockNetherPlant implements net.minecraft.block.IGrowable {
    private final boolean warped;

    public BlockNetherFungus(boolean warped) {
        super(warped ? "warped_fungus" : "crimson_fungus", 8.0D, 9.0D, true);
        this.warped = warped;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return isHugeFungusEnabled() && hasMatchingNylium(world, pos)
                && pos.up().getY() < FFDHeightHooks.maxYExclusive(world);
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return isHugeFungusEnabled() && random.nextFloat() < 0.4F;
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        if (!canGrow(world, pos, state, false)) {
            return;
        }
        world.setBlockToAir(pos);
        if (!new WorldGenHugeFungus(warped, true).generate(world, random, pos)) {
            world.setBlockState(pos, state, 3);
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return isPlantEnabled() && super.canPlaceBlockAt(world, pos);
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return isPlantEnabled() && super.canBlockStay(world, pos, state);
    }

    public boolean isWarped() {
        return warped;
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.NONE;
    }

    private boolean hasMatchingNylium(World world, BlockPos pos) {
        return FFDNetherBlockProvider.get().isNylium(world.getBlockState(pos.down()), warped);
    }

    private boolean isPlantEnabled() {
        return warped ? FFDItems.isWarpedEnabled() : FFDItems.isCrimsonEnabled();
    }

    private boolean isHugeFungusEnabled() {
        return isPlantEnabled() && (warped ? FFDItems.isWarpedWoodEnabled()
                : FFDItems.isCrimsonWoodEnabled());
    }
}
