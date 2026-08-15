package xy177.farmersfuturedelight.common.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCake;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;

public class BlockCandleCake extends BlockAbstractCandle {
    private static final AxisAlignedBB OUTLINE = new AxisAlignedBB(
            1.0D / 16.0D, 0.0D, 1.0D / 16.0D,
            15.0D / 16.0D, 14.0D / 16.0D, 15.0D / 16.0D);
    private static final AxisAlignedBB CAKE = new AxisAlignedBB(
            1.0D / 16.0D, 0.0D, 1.0D / 16.0D,
            15.0D / 16.0D, 8.0D / 16.0D, 15.0D / 16.0D);
    private static final AxisAlignedBB CANDLE = new AxisAlignedBB(
            7.0D / 16.0D, 8.0D / 16.0D, 7.0D / 16.0D,
            9.0D / 16.0D, 14.0D / 16.0D, 9.0D / 16.0D);
    private static final Vec3d[] PARTICLE_OFFSETS = {
            new Vec3d(0.5D, 1.0D, 0.5D)
    };

    private final BlockCandle candle;

    public BlockCandleCake(String name, BlockCandle candle) {
        super(Material.CAKE);
        this.candle = candle;
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setHardness(0.5F);
        setSoundType(SoundType.CLOTH);
        setDefaultState(blockState.getBaseState().withProperty(LIT, false));
    }

    public BlockCandle getCandle() {
        return candle;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if ((!held.isEmpty() || hitY > 0.5F)
                && handleLightingInteraction(world, pos, state, player, hand)) {
            return true;
        }
        if (world.isRemote) {
            return player.canEat(false) || held.isEmpty();
        }
        if (!player.canEat(false)) {
            return false;
        }
        player.addStat(StatList.CAKE_SLICES_EATEN);
        player.getFoodStats().addStats(2, 0.1F);
        world.setBlockState(pos, Blocks.CAKE.getDefaultState()
                .withProperty(BlockCake.BITES, 1), 3);
        Block.spawnAsEntity(world, pos, new ItemStack(Item.getItemFromBlock(candle)));
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return canStay(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (!world.isRemote && !canStay(world, pos)) {
            world.destroyBlock(pos, true);
        }
    }

    private static boolean canStay(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).getMaterial().isSolid();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return OUTLINE;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
                                      AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entity, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, CAKE);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, CANDLE);
    }

    @Override
    protected Vec3d[] getParticleOffsets(IBlockState state) {
        return PARTICLE_OFFSETS;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(candle);
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        Item item = Item.getItemFromBlock(candle);
        if (item != Items.AIR) {
            drops.add(new ItemStack(item));
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(Items.CAKE);
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return 14;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LIT, (meta & 1) != 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LIT);
    }
}
