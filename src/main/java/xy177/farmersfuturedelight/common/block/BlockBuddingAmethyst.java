package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockBuddingAmethyst extends BlockAmethyst {
    public BlockBuddingAmethyst() {
        super("budding_amethyst", FFDSounds.AMETHYST);
        setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || random.nextInt(FFDConfig.amethystGrowthRoll) != 0) {
            return;
        }

        EnumFacing direction = EnumFacing.values()[random.nextInt(EnumFacing.values().length)];
        BlockPos growPos = pos.offset(direction);
        IBlockState relative = world.getBlockState(growPos);
        Block next = null;
        if (canClusterGrowAtState(relative)) {
            next = FFDBlocks.SMALL_AMETHYST_BUD;
        } else if (relative.getBlock() == FFDBlocks.SMALL_AMETHYST_BUD
                && relative.getValue(BlockAmethystCluster.FACING) == direction) {
            next = FFDBlocks.MEDIUM_AMETHYST_BUD;
        } else if (relative.getBlock() == FFDBlocks.MEDIUM_AMETHYST_BUD
                && relative.getValue(BlockAmethystCluster.FACING) == direction) {
            next = FFDBlocks.LARGE_AMETHYST_BUD;
        } else if (relative.getBlock() == FFDBlocks.LARGE_AMETHYST_BUD
                && relative.getValue(BlockAmethystCluster.FACING) == direction) {
            next = FFDBlocks.AMETHYST_CLUSTER;
        }

        if (next instanceof BlockAmethystCluster) {
            world.setBlockState(growPos, next.getDefaultState()
                    .withProperty(BlockLiquid.LEVEL, 0)
                    .withProperty(BlockAmethystCluster.FACING, direction)
                    .withProperty(BlockAmethystCluster.WATERLOGGED, isSourceWater(relative)), 3);
        }
    }

    public static boolean canClusterGrowAtState(IBlockState state) {
        return state.getBlock() == Blocks.AIR || isSourceWater(state);
    }

    private static boolean isSourceWater(IBlockState state) {
        Block block = state.getBlock();
        return (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
                && state.getPropertyKeys().contains(BlockLiquid.LEVEL)
                && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public void getDrops(NonNullList<net.minecraft.item.ItemStack> drops, IBlockAccess world,
                         BlockPos pos, IBlockState state, int fortune) {
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
}
