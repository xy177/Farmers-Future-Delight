package xy177.farmersfuturedelight.common.block;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
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
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockLightningRod extends BlockDirectional
        implements IWeatheringCopper, IWaterloggableBlock {
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    private static final AxisAlignedBB SHAPE_X = new AxisAlignedBB(0.0D, 0.375D, 0.375D,
            1.0D, 0.625D, 0.625D);
    private static final AxisAlignedBB SHAPE_Y = new AxisAlignedBB(0.375D, 0.0D, 0.375D,
            0.625D, 1.0D, 0.625D);
    private static final AxisAlignedBB SHAPE_Z = new AxisAlignedBB(0.375D, 0.375D, 0.0D,
            0.625D, 0.625D, 1.0D);
    private static final Map<World, Map<Long, Long>> POWER_EXPIRY = new WeakHashMap<>();

    private final CopperWeathering.WeatherState weatherState;
    private final boolean waxed;
    private final boolean waterlogged;

    public BlockLightningRod(String name, CopperWeathering.WeatherState weatherState,
                             boolean waxed) {
        this(name, weatherState, waxed, false);
    }

    public BlockLightningRod(String name, CopperWeathering.WeatherState weatherState,
                             boolean waxed, boolean waterlogged) {
        super(Material.IRON);
        this.weatherState = weatherState;
        this.waxed = waxed;
        this.waterlogged = waterlogged;
        setRegistryName(FarmerFutureDelight.MODID,
                waterlogged ? name + "_waterlogged" : name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        if (!waterlogged) {
            setCreativeTab(FFDCreativeTab.INSTANCE);
        }
        setDefaultState(blockState.getBaseState()
                .withProperty(FACING, EnumFacing.UP)
                .withProperty(POWERED, false));
        setHardness(3.0F);
        setResistance(6.0F / 3.0F);
        setSoundType(FFDSounds.COPPER);
        setHarvestLevel("pickaxe", 1);
        setTickRandomly(!waxed && weatherState != CopperWeathering.WeatherState.OXIDIZED);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                             float hitX, float hitY, float hitZ, int meta,
                                             EntityLivingBase placer, net.minecraft.util.EnumHand hand) {
        IBlockState state = getDefaultState().withProperty(FACING, facing).withProperty(POWERED, false);
        return setWaterloggedState(state, WaterloggedBlockApi.containsWater(world, pos));
    }

    public void onLightningStrike(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            return;
        }
        IBlockState powered = state.withProperty(POWERED, true);
        world.setBlockState(pos, powered, 3);
        updateNeighbors(world, pos, powered);
        rememberPowerExpiry(world, pos, world.getTotalWorldTime() + 8L);
        world.scheduleUpdate(pos, powered.getBlock(), 8);
        spawnSparks(world, pos, powered, 8);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote && state.getValue(POWERED)) {
            long remaining = getPowerExpiry(world, pos) - world.getTotalWorldTime();
            if (remaining > 0L) {
                world.scheduleUpdate(pos, this, (int) Math.min(Integer.MAX_VALUE, remaining));
            } else {
                IBlockState unpowered = state.withProperty(POWERED, false);
                world.setBlockState(pos, unpowered, 3);
                updateNeighbors(world, pos, unpowered);
                clearPowerExpiry(world, pos);
            }
        }
        IBlockState current = world.getBlockState(pos);
        if (current.getBlock() == this && isWaterloggedState(current)) {
            WaterloggedBlockApi.updateTick(world, pos, current);
        }
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        CopperWeathering.tryWeather(world, pos, state, random);
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!world.isThundering() || random.nextInt(200) > world.getTotalWorldTime() % 200L
                || pos.getY() != world.getHeight(pos.getX(), pos.getZ()) - 1) {
            return;
        }
        spawnSparks(world, pos, state, 1 + random.nextInt(2));
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(POWERED) && !world.isUpdateScheduled(pos, this)) {
            if (getPowerExpiry(world, pos) <= world.getTotalWorldTime()) {
                rememberPowerExpiry(world, pos, world.getTotalWorldTime() + 8L);
            }
            world.scheduleUpdate(pos, this, 8);
        }
        WaterloggedBlockApi.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        WaterloggedBlockApi.onNeighborChanged(world, pos, this);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(POWERED)) {
            updateNeighbors(world, pos, state);
        }
        super.breakBlock(world, pos, state);
    }

    private void updateNeighbors(World world, BlockPos pos, IBlockState state) {
        EnumFacing behind = state.getValue(FACING).getOpposite();
        world.notifyNeighborsOfStateChange(pos.offset(behind), this, false);
    }

    private static void rememberPowerExpiry(World world, BlockPos pos, long tick) {
        Map<Long, Long> positions = POWER_EXPIRY.get(world);
        if (positions == null) {
            positions = new HashMap<>();
            POWER_EXPIRY.put(world, positions);
        }
        positions.put(pos.toLong(), tick);
    }

    private static long getPowerExpiry(World world, BlockPos pos) {
        Map<Long, Long> positions = POWER_EXPIRY.get(world);
        Long tick = positions == null ? null : positions.get(pos.toLong());
        return tick == null ? Long.MIN_VALUE : tick;
    }

    private static void clearPowerExpiry(World world, BlockPos pos) {
        Map<Long, Long> positions = POWER_EXPIRY.get(world);
        if (positions != null) {
            positions.remove(pos.toLong());
            if (positions.isEmpty()) {
                POWER_EXPIRY.remove(world);
            }
        }
    }

    private static void spawnSparks(World world, BlockPos pos, IBlockState state, int count) {
        EnumFacing.Axis axis = state.getValue(FACING).getAxis();
        Random random = world.rand;
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.5D + (axis == EnumFacing.Axis.X
                    ? random.nextDouble() - 0.5D : (random.nextDouble() - 0.5D) * 0.25D);
            double y = pos.getY() + 0.5D + (axis == EnumFacing.Axis.Y
                    ? random.nextDouble() - 0.5D : (random.nextDouble() - 0.5D) * 0.25D);
            double z = pos.getZ() + 0.5D + (axis == EnumFacing.Axis.Z
                    ? random.nextDouble() - 0.5D : (random.nextDouble() - 0.5D) * 0.25D);
            world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.getValue(POWERED) && state.getValue(FACING) == side ? 15 : 0;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING).getAxis()) {
            case X:
                return SHAPE_X;
            case Z:
                return SHAPE_Z;
            default:
                return SHAPE_Y;
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos,
                                            EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT
                || isWaterloggedState(state) && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(dryVariant());
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        Item item = getItemDropped(state, world.rand, 0);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getFront(meta & 7);
        return getDefaultState().withProperty(FACING, facing).withProperty(POWERED, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex() | (state.getValue(POWERED) ? 8 : 0);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {FACING, POWERED},
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
        return state.getBlock() == this && waterlogged;
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        BlockLightningRod target = variant(waterlogged);
        return target.getDefaultState()
                .withProperty(FACING, state.getValue(FACING))
                .withProperty(POWERED, state.getValue(POWERED));
    }

    private BlockLightningRod dryVariant() {
        return variant(false);
    }

    private BlockLightningRod variant(boolean waterlogged) {
        int index = weatherState.ordinal();
        if (waxed) {
            return waterlogged ? FFDBlocks.WAXED_LIGHTNING_RODS_WATERLOGGED[index]
                    : FFDBlocks.WAXED_LIGHTNING_RODS[index];
        }
        return waterlogged ? FFDBlocks.LIGHTNING_RODS_WATERLOGGED[index]
                : FFDBlocks.LIGHTNING_RODS[index];
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        return WaterloggedBlockApi.restoreFluid(world, pos, state,
                world.isRemote ? 11 : 3);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        dropBlockAsItem(world, pos, state, 0);
        WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, net.minecraft.entity.Entity entity,
                                    Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }

    @Override
    public CopperWeathering.WeatherState getWeatherState() {
        return weatherState;
    }

    @Override
    public boolean isWaxed() {
        return waxed;
    }

}
