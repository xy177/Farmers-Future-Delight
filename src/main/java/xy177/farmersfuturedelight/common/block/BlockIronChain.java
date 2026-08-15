package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockIronChain extends Block {
    public static final PropertyEnum<EnumFacing.Axis> AXIS =
            PropertyEnum.create("axis", EnumFacing.Axis.class);

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
        setDefaultState(blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
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
        return getDefaultState().withProperty(AXIS, facing.getAxis());
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
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        switch (meta % 3) {
            case 0:
                return getDefaultState().withProperty(AXIS, EnumFacing.Axis.X);
            case 2:
                return getDefaultState().withProperty(AXIS, EnumFacing.Axis.Z);
            default:
                return getDefaultState().withProperty(AXIS, EnumFacing.Axis.Y);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        switch (state.getValue(AXIS)) {
            case X:
                return 0;
            case Z:
                return 2;
            default:
                return 1;
        }
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
        return new BlockStateContainer(this, AXIS);
    }
}
