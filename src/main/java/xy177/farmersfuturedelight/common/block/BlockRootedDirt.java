package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockRootedDirt extends Block implements IGrowable {
    public BlockRootedDirt() {
        super(Material.GROUND);
        setRegistryName(FarmerFutureDelight.MODID, "rooted_dirt");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".rooted_dirt");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.5F);
        setSoundType(FFDSounds.ROOTED_DIRT);
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return FFDItems.isRootedDirtEnabled() && FFDItems.isHangingRootsEnabled()
                && world.isAirBlock(pos.down());
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return FFDItems.isRootedDirtEnabled() && FFDItems.isHangingRootsEnabled();
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        BlockPos rootsPos = pos.down();
        if (world.isAirBlock(rootsPos) && FFDBlocks.HANGING_ROOTS.canPlaceBlockAt(world, rootsPos)) {
            world.setBlockState(rootsPos, FFDBlocks.HANGING_ROOTS.getDefaultState(), 3);
        }
    }
}
