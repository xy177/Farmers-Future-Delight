package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;

import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public abstract class BlockKelpHead extends BlockUnderwaterPlant implements IGrowable {
    protected static final AxisAlignedBB KELP_HEAD_AABB =
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5625D, 1.0D);

    protected BlockKelpHead() {
        super();
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(getAgeProperty(), getDefaultAgeValue()));
    }

    protected abstract PropertyInteger getAgeProperty();

    protected abstract int getDefaultAgeValue();

    public abstract IBlockState stateForAge(int age);

    protected abstract int ageFromState(IBlockState state);

    protected abstract int metadataAge(IBlockState state);

    protected abstract IBlockState stateFromMetadataAge(int meta);

    public static boolean isKelpHead(IBlockState state) {
        return state.getBlock() == FFDBlocks.KELP || state.getBlock() == FFDBlocks.KELP_YOUNG;
    }

    public static boolean isKelpPart(IBlockState state) {
        return isKelpHead(state) || state.getBlock() == FFDBlocks.KELP_PLANT;
    }

    public static int getAgeValue(IBlockState state) {
        if (state.getBlock() instanceof BlockKelpHead) {
            return ((BlockKelpHead) state.getBlock()).ageFromState(state);
        }
        return 0;
    }

    public static IBlockState stateForAgeValue(int age) {
        age = Math.max(0, Math.min(25, age));
        return age <= 15 ? FFDBlocks.KELP_YOUNG.stateForAge(age)
                : FFDBlocks.KELP.stateForAge(age);
    }

    public static boolean stopGrowth(World world, BlockPos pos, IBlockState state) {
        if (!isKelpHead(state) || getAgeValue(state) >= 25) {
            return false;
        }
        world.setBlockState(pos, stateForAgeValue(25), 3);
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {
                        BlockLiquid.LEVEL, getAgeProperty()},
                WaterloggedBlockApi.extendedProperties());
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
        IBlockState below = world.getBlockState(pos.down());
        return BlockKelpPlant.canSupportKelp(below, world, pos.down());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return KELP_HEAD_AABB;
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
        if (isKelpPart(world.getBlockState(pos.up()))) {
            world.setBlockState(pos, FFDBlocks.KELP_PLANT.getDefaultState(), 2);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        if (world.getBlockState(pos).getBlock() != this) {
            return;
        }
        if (!world.isRemote && isFeatureEnabled() && ageFromState(state) < 25
                && rand.nextFloat() < FFDConfig.kelpGrowthChance
                && isVanillaWaterBlock(world, pos.up())) {
            grow(world, rand, pos, state);
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return isFeatureEnabled() && isVanillaWaterBlock(world, pos.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return canGrow(world, pos, state, world.isRemote);
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        int age = Math.min(25, ageFromState(state) + 1);
        BlockPos above = pos.up();
        if (!isVanillaWaterBlock(world, above)) {
            return;
        }
        world.setBlockState(pos, FFDBlocks.KELP_PLANT.getDefaultState(), 2);
        world.setBlockState(above, stateForAgeValue(age), 3);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return isFeatureEnabled() ? FFDItems.KELP : Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        if (isFeatureEnabled()) {
            drops.add(FFDItems.effectiveStack(FFDItems.KELP));
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return metadataAge(state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return stateFromMetadataAge(meta);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.KELP);
    }
}
