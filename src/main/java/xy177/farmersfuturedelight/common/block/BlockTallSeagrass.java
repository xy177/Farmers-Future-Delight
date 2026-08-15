package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class BlockTallSeagrass extends BlockUnderwaterPlant {
    public static final PropertyEnum<BlockDoublePlant.EnumBlockHalf> HALF = BlockDoublePlant.HALF;
    private static final AxisAlignedBB TALL_SEAGRASS_AABB =
            new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);

    public BlockTallSeagrass() {
        setRegistryName(FarmerFutureDelight.MODID, "tall_seagrass");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".tall_seagrass");
        setDefaultState(blockState.getBaseState().withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {BlockLiquid.LEVEL, HALF},
                WaterloggedPlantFluid.extendedProperties());
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
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return isFeatureEnabled() && isSourceWater(world, pos) && isSourceWater(world, pos.up())
                && BlockSeagrass.hasSeagrassSupport(world.getBlockState(pos.down()), world, pos.down());
    }

    @Override
    protected boolean canBlockStayAt(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) {
            return BlockSeagrass.hasSeagrassSupport(world.getBlockState(pos.down()), world, pos.down());
        }
        if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
            IBlockState below = world.getBlockState(pos.down());
            return below.getBlock() == this
                    && below.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER;
        }
        IBlockState above = world.getBlockState(pos.up());
        return above.getBlock() == this
                && above.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER
                && BlockSeagrass.hasSeagrassSupport(world.getBlockState(pos.down()), world, pos.down());
    }

    public boolean placeAt(World world, BlockPos lowerPos, int flags) {
        IBlockState lowerCurrent = world.getBlockState(lowerPos);
        boolean validLower = isSourceWater(world, lowerPos) || lowerCurrent.getBlock() == FFDBlocks.SEAGRASS;
        if (!isFeatureEnabled() || !validLower || !isVanillaWaterBlock(world, lowerPos.up())
                || !BlockSeagrass.hasSeagrassSupport(world.getBlockState(lowerPos.down()), world, lowerPos.down())) {
            return false;
        }
        world.setBlockState(lowerPos, getDefaultState()
                .withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER), flags & ~1);
        world.setBlockState(lowerPos.up(), getDefaultState()
                .withProperty(HALF, BlockDoublePlant.EnumBlockHalf.UPPER), flags);
        return true;
    }

    @Override
    protected void checkAndDropBlock(World world, BlockPos pos, IBlockState state) {
        if (!canBlockStayAt(world, pos)) {
            restoreBothWater(world, pos, state);
        }
    }

    private void restoreBothWater(World world, BlockPos pos, IBlockState state) {
        BlockPos lower = state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER ? pos.down() : pos;
        BlockPos upper = lower.up();
        if (world.getBlockState(lower).getBlock() == this) {
            world.setBlockState(lower, Blocks.WATER.getDefaultState(), 2);
        }
        if (world.getBlockState(upper).getBlock() == this) {
            world.setBlockState(upper, Blocks.WATER.getDefaultState(), 3);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        restoreBothWater(world, pos, state);
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return TALL_SEAGRASS_AABB;
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
            spawnAsEntity(world, pos, FFDItems.effectiveStack(FFDItems.SEAGRASS, 2));
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockLiquid.LEVEL, 0).withProperty(HALF,
                (meta & 1) == 1 ? BlockDoublePlant.EnumBlockHalf.UPPER
                        : BlockDoublePlant.EnumBlockHalf.LOWER);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.SEAGRASS);
    }
}
