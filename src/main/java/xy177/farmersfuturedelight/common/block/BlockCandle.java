package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockCandle extends BlockAbstractCandle implements IWaterloggableBlock {
    public static final PropertyInteger CANDLES = PropertyInteger.create("candles", 1, 4);
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");

    private static final AxisAlignedBB[] SHAPES = {
            new AxisAlignedBB(7.0D / 16.0D, 0.0D, 7.0D / 16.0D,
                    9.0D / 16.0D, 6.0D / 16.0D, 9.0D / 16.0D),
            new AxisAlignedBB(5.0D / 16.0D, 0.0D, 6.0D / 16.0D,
                    11.0D / 16.0D, 6.0D / 16.0D, 9.0D / 16.0D),
            new AxisAlignedBB(5.0D / 16.0D, 0.0D, 6.0D / 16.0D,
                    10.0D / 16.0D, 6.0D / 16.0D, 11.0D / 16.0D),
            new AxisAlignedBB(5.0D / 16.0D, 0.0D, 5.0D / 16.0D,
                    11.0D / 16.0D, 6.0D / 16.0D, 10.0D / 16.0D)
    };

    private static final Vec3d[][] PARTICLE_OFFSETS = {
            {vec(8, 8, 8)},
            {vec(6, 7, 8), vec(10, 8, 7)},
            {vec(8, 5, 10), vec(6, 7, 8), vec(9, 8, 7)},
            {vec(7, 5, 9), vec(10, 7, 9), vec(6, 7, 6), vec(9, 8, 6)}
    };

    public BlockCandle(String name) {
        super(Material.CIRCUITS);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.1F);
        setSoundType(FFDSounds.CANDLE);
        setDefaultState(blockState.getBaseState().withProperty(CANDLES, 1)
                .withProperty(LIT, false).withProperty(WATERLOGGED, false));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(WATERLOGGED,
                WaterloggedBlockApi.containsWater(world, pos));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (handleLightingInteraction(world, pos, state, player, hand)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canLight(IBlockState state) {
        return super.canLight(state) && !state.getValue(WATERLOGGED);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return FFDItems.isCandleEnabled() && canStay(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (!world.isRemote && !canStay(world, pos)) {
            dropBlockAsItem(world, pos, state, 0);
            WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
        } else {
            WaterloggedBlockApi.onNeighborChanged(world, pos, this);
        }
    }

    private static boolean canStay(World world, BlockPos pos) {
        IBlockState below = world.getBlockState(pos.down());
        return below.isSideSolid(world, pos.down(), EnumFacing.UP)
                || below.getBlock().canPlaceTorchOnTop(below, world, pos.down());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPES[state.getValue(CANDLES) - 1];
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world,
                                                  BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }

    @Override
    protected Vec3d[] getParticleOffsets(IBlockState state) {
        return PARTICLE_OFFSETS[state.getValue(CANDLES) - 1];
    }

    @Override
    protected int getCandleCount(IBlockState state) {
        return state.getValue(CANDLES);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return FFDItems.isCandleEnabled() ? Item.getItemFromBlock(this) : Items.AIR;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return FFDItems.isCandleEnabled() ? state.getValue(CANDLES) : 0;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        Item item = Item.getItemFromBlock(this);
        if (FFDItems.isCandleEnabled() && item != Items.AIR) {
            drops.add(new ItemStack(item, state.getValue(CANDLES)));
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(CANDLES) - 1 | (state.getValue(LIT) ? 4 : 0)
                | (state.getValue(WATERLOGGED) ? 8 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(CANDLES, (meta & 3) + 1)
                .withProperty(LIT, (meta & 4) != 0)
                .withProperty(WATERLOGGED, (meta & 8) != 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {CANDLES, LIT, WATERLOGGED},
                WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return isWaterloggedState(state) ? Material.WATER : super.getMaterial(state);
    }

    @Override
    public boolean isWaterloggedState(IBlockState state) {
        return state.getBlock() == this && state.getValue(WATERLOGGED);
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        return state.withProperty(WATERLOGGED, waterlogged)
                .withProperty(LIT, waterlogged ? false : state.getValue(LIT));
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedBlockApi.onBlockAdded(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        super.updateTick(world, pos, state, random);
        if (isWaterloggedState(state)) {
            WaterloggedBlockApi.updateTick(world, pos, state);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        return WaterloggedBlockApi.restoreFluid(world, pos, state, world.isRemote ? 11 : 3);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        dropBlockAsItem(world, pos, state, 0);
        WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public net.minecraft.util.BlockRenderLayer getBlockLayer() {
        return net.minecraft.util.BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, net.minecraft.util.BlockRenderLayer layer) {
        return layer == net.minecraft.util.BlockRenderLayer.CUTOUT
                || isWaterloggedState(state)
                && layer == net.minecraft.util.BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
                                        EnumFacing side) {
        if (isWaterloggedState(state)
                && WaterloggedBlockApi.containsWater(world, pos.offset(side))) {
            return false;
        }
        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, net.minecraft.entity.Entity entity,
                                    Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }

    private static Vec3d vec(double x, double y, double z) {
        return new Vec3d(x / 16.0D, y / 16.0D, z / 16.0D);
    }
}
