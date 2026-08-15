package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class BlockSeagrass extends BlockUnderwaterPlant implements IGrowable {
    private static final AxisAlignedBB SEAGRASS_AABB =
            new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.75D, 0.875D);

    public BlockSeagrass() {
        setRegistryName(FarmerFutureDelight.MODID, "seagrass");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".seagrass");
        setDefaultState(blockState.getBaseState().withProperty(BlockLiquid.LEVEL, 0));
    }

    public static boolean hasSeagrassSupport(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getBlock() != Blocks.MAGMA && state.isSideSolid(world, pos, EnumFacing.UP);
    }

    @Override
    protected boolean isFeatureEnabled() {
        return FFDItems.isSeagrassEnabled();
    }

    @Override
    protected boolean canPlaceInto(World world, BlockPos pos) {
        return isSourceWater(world, pos);
    }

    @Override
    protected boolean canBlockStayAt(World world, BlockPos pos) {
        return hasSeagrassSupport(world.getBlockState(pos.down()), world, pos.down());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SEAGRASS_AABB;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return isFeatureEnabled() && isVanillaWaterBlock(world, pos.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return isFeatureEnabled();
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        if (isVanillaWaterBlock(world, pos.up())) {
            FFDBlocks.TALL_SEAGRASS.placeAt(world, pos, 3);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity tile, ItemStack tool) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
        if (!world.isRemote && isFeatureEnabled() && tool.getItem() == Items.SHEARS) {
            spawnAsEntity(world, pos, FFDItems.effectiveStack(FFDItems.SEAGRASS));
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.SEAGRASS);
    }
}
