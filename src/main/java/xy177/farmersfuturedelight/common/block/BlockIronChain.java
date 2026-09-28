package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
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
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockIronChain extends Block implements IWaterloggableBlock {
    public static final PropertyEnum<EnumFacing.Axis> AXIS =
            PropertyEnum.create("axis", EnumFacing.Axis.class);
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");

    private static final AxisAlignedBB SHAPE_X = new AxisAlignedBB(
            0.0D, 6.5D / 16.0D, 6.5D / 16.0D,
            1.0D, 9.5D / 16.0D, 9.5D / 16.0D);
    private static final AxisAlignedBB SHAPE_Y = new AxisAlignedBB(
            6.5D / 16.0D, 0.0D, 6.5D / 16.0D,
            9.5D / 16.0D, 1.0D, 9.5D / 16.0D);
    private static final AxisAlignedBB SHAPE_Z = new AxisAlignedBB(
            6.5D / 16.0D, 6.5D / 16.0D, 0.0D,
            9.5D / 16.0D, 9.5D / 16.0D, 1.0D);

    public BlockIronChain() {
        super(Material.IRON);
        setRegistryName(FarmerFutureDelight.MODID, "iron_chain");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".iron_chain");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setDefaultState(blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y)
                .withProperty(WATERLOGGED, false));
        setHardness(5.0F);
        setResistance(6.0F / 3.0F);
        setSoundType(FFDSounds.CHAIN);
        setHarvestLevel("pickaxe", 0);
        setLightOpacity(0);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                             float hitX, float hitY, float hitZ, int meta,
                                             EntityLivingBase placer,
                                             net.minecraft.util.EnumHand hand) {
        return getDefaultState().withProperty(AXIS, facing.getAxis())
                .withProperty(WATERLOGGED, WaterloggedBlockApi.containsWater(world, pos));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(AXIS)) {
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
    public IBlockState getStateFromMeta(int meta) {
        switch (meta & 3) {
            case 0:
                return getDefaultState().withProperty(AXIS, EnumFacing.Axis.X)
                        .withProperty(WATERLOGGED, (meta & 4) != 0);
            case 2:
                return getDefaultState().withProperty(AXIS, EnumFacing.Axis.Z)
                        .withProperty(WATERLOGGED, (meta & 4) != 0);
            default:
                return getDefaultState().withProperty(AXIS, EnumFacing.Axis.Y)
                        .withProperty(WATERLOGGED, (meta & 4) != 0);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta;
        switch (state.getValue(AXIS)) {
            case X:
                meta = 0;
                break;
            case Z:
                meta = 2;
                break;
            default:
                meta = 1;
        }
        return state.getValue(WATERLOGGED) ? meta | 4 : meta;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        EnumFacing.Axis axis = state.getValue(AXIS);
        if (axis == EnumFacing.Axis.Y
                || rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) {
            return state;
        }
        return state.withProperty(AXIS,
                axis == EnumFacing.Axis.X ? EnumFacing.Axis.Z : EnumFacing.Axis.X);
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {AXIS, WATERLOGGED},
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
        return state.withProperty(WATERLOGGED, waterlogged);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedBlockApi.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        WaterloggedBlockApi.onNeighborChanged(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, java.util.Random random) {
        super.updateTick(world, pos, state, random);
        if (isWaterloggedState(state)) {
            WaterloggedBlockApi.updateTick(world, pos, state);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   net.minecraft.entity.player.EntityPlayer player,
                                   boolean willHarvest) {
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
    public Vec3d modifyAcceleration(World world, BlockPos pos, net.minecraft.entity.Entity entity,
                                    Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
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
}
