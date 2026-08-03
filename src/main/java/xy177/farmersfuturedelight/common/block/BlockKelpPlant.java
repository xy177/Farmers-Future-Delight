package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class BlockKelpPlant extends BlockUnderwaterPlant implements IGrowable {
    private static final AxisAlignedBB KELP_AABB =
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    public BlockKelpPlant() {
        setRegistryName(FarmerFutureDelight.MODID, "kelp_plant");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".kelp_plant");
    }

    public static boolean canSupportKelp(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state.getBlock() == FFDBlocks.KELP || state.getBlock() == FFDBlocks.KELP_YOUNG
                || state.getBlock() == FFDBlocks.KELP_PLANT) {
            return true;
        }
        return state.getBlock() != net.minecraft.init.Blocks.MAGMA
                && state.isSideSolid(world, pos, EnumFacing.UP);
    }

    @Override
    protected boolean isFeatureEnabled() {
        return FFDItems.isKelpEnabled();
    }

    @Override
    protected boolean canPlaceInto(World world, BlockPos pos) {
        return isSourceWater(world, pos);
    }

    @Override
    protected boolean canBlockStayAt(World world, BlockPos pos) {
        return canSupportKelp(world.getBlockState(pos.down()), world, pos.down());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return KELP_AABB;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        if (world.getBlockState(pos).getBlock() != this) {
            return;
        }
        if (!canBlockStayAt(world, pos)) {
            checkAndRestoreWater(world, pos, state);
            return;
        }
        if (!BlockKelpHead.isKelpPart(world.getBlockState(pos.up()))) {
            world.setBlockState(pos, BlockKelpHead.stateForAgeValue(world.rand.nextInt(25)), 3);
        }
    }

    private static BlockPos findHead(World world, BlockPos pos) {
        BlockPos cursor = pos;
        while (world.getBlockState(cursor).getBlock() == FFDBlocks.KELP_PLANT) {
            cursor = cursor.up();
        }
        return BlockKelpHead.isKelpHead(world.getBlockState(cursor)) ? cursor : null;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        BlockPos headPos = findHead(world, pos);
        if (!isFeatureEnabled() || headPos == null) {
            return false;
        }
        IBlockState headState = world.getBlockState(headPos);
        return ((BlockKelpHead) headState.getBlock()).canGrow(
                world, headPos, headState, isClient);
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return isFeatureEnabled();
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        BlockPos headPos = findHead(world, pos);
        if (headPos != null) {
            IBlockState headState = world.getBlockState(headPos);
            ((BlockKelpHead) headState.getBlock()).grow(world, random, headPos, headState);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return isFeatureEnabled() ? FFDItems.KELP : Item.getItemFromBlock(net.minecraft.init.Blocks.AIR);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        if (isFeatureEnabled()) {
            drops.add(new ItemStack(FFDItems.KELP));
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockLiquid.LEVEL, 0);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(FFDItems.KELP);
    }
}
