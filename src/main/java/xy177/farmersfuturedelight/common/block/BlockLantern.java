package xy177.farmersfuturedelight.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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

public class BlockLantern extends Block implements IWaterloggableBlock {
    public static final PropertyBool HANGING = PropertyBool.create("hanging");
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private static final AxisAlignedBB BODY = new AxisAlignedBB(
            5.0D / 16.0D, 0.0D, 5.0D / 16.0D,
            11.0D / 16.0D, 7.0D / 16.0D, 11.0D / 16.0D);
    private static final AxisAlignedBB HANDLE = new AxisAlignedBB(
            6.0D / 16.0D, 7.0D / 16.0D, 6.0D / 16.0D,
            10.0D / 16.0D, 9.0D / 16.0D, 10.0D / 16.0D);

    public BlockLantern() {
        this(false);
    }

    public BlockLantern(boolean soul) {
        super(Material.IRON);
        String name = soul ? "soul_lantern" : "lantern";
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setDefaultState(blockState.getBaseState().withProperty(HANGING, false)
                .withProperty(WATERLOGGED, false));
        setHardness(3.5F);
        setResistance(3.5F / 3.0F);
        setHarvestLevel("pickaxe", 0);
        setLightLevel(soul ? 10.0F / 15.0F : 1.0F);
        setLightOpacity(0);
        setSoundType(FFDSounds.LANTERN);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        boolean hanging = facing == EnumFacing.DOWN
                || facing.getAxis() != EnumFacing.Axis.Y && placer.rotationPitch < 0;
        if (!canStay(world, pos, hanging)) {
            hanging = !hanging;
        }
        return getDefaultState().withProperty(HANGING, hanging)
                .withProperty(WATERLOGGED, WaterloggedBlockApi.isWaterSource(world, pos));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return canStay(world, pos, false) || canStay(world, pos, true);
    }

    public boolean canStay(World world, BlockPos pos, boolean hanging) {
        EnumFacing face = hanging ? EnumFacing.DOWN : EnumFacing.UP;
        BlockPos support = pos.offset(face.getOpposite());
        IBlockState state = world.getBlockState(support);
        if (state.getBlock() instanceof BlockLeaves
                || hanging && (state.getBlock() instanceof BlockFenceGate
                || state.getBlock() instanceof BlockCauldron)) {
            return false;
        }
        if (state.getBlock() instanceof BlockSnow) {
            return hanging || state.getValue(BlockSnow.LAYERS) == 8;
        }
        BlockFaceShape shape = state.getBlockFaceShape(world, support, face);
        if (shape == BlockFaceShape.SOLID || shape == BlockFaceShape.CENTER_SMALL
                || shape == BlockFaceShape.CENTER || shape == BlockFaceShape.CENTER_BIG) {
            return true;
        }
        List<AxisAlignedBB> boxes = new ArrayList<>();
        state.addCollisionBoxToList(world, support, FULL_BLOCK_AABB.offset(support), boxes, null, false);
        List<AxisAlignedBB> surfaces = new ArrayList<>();
        TreeSet<Double> boundariesX = new TreeSet<>();
        TreeSet<Double> boundariesZ = new TreeSet<>();
        double minimumX = support.getX() + 7.0D / 16.0D;
        double maximumX = support.getX() + 9.0D / 16.0D;
        double minimumZ = support.getZ() + 7.0D / 16.0D;
        double maximumZ = support.getZ() + 9.0D / 16.0D;
        double plane = support.getY() + (hanging ? 0.0D : 1.0D);
        boundariesX.add(minimumX);
        boundariesX.add(maximumX);
        boundariesZ.add(minimumZ);
        boundariesZ.add(maximumZ);
        for (AxisAlignedBB box : boxes) {
            if (box.minY > plane || box.maxY < plane
                    || box.minX >= maximumX || box.maxX <= minimumX
                    || box.minZ >= maximumZ || box.maxZ <= minimumZ) {
                continue;
            }
            surfaces.add(box);
            boundariesX.add(Math.max(minimumX, box.minX));
            boundariesX.add(Math.min(maximumX, box.maxX));
            boundariesZ.add(Math.max(minimumZ, box.minZ));
            boundariesZ.add(Math.min(maximumZ, box.maxZ));
        }
        Double[] columns = boundariesX.toArray(new Double[0]);
        Double[] rows = boundariesZ.toArray(new Double[0]);
        for (int column = 1; column < columns.length; column++) {
            for (int row = 1; row < rows.length; row++) {
                double centerX = (columns[column - 1] + columns[column]) * 0.5D;
                double centerZ = (rows[row - 1] + rows[row]) * 0.5D;
                boolean supported = false;
                for (AxisAlignedBB surface : surfaces) {
                    if (centerX >= surface.minX && centerX <= surface.maxX
                            && centerZ >= surface.minZ && centerZ <= surface.maxZ) {
                        supported = true;
                        break;
                    }
                }
                if (!supported) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return BODY.union(HANDLE).offset(0, state.getValue(HANGING) ? 1.0D / 16.0D : 0, 0);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
            AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean actualState) {
        double offset = state.getValue(HANGING) ? 1.0D / 16.0D : 0;
        addCollisionBoxToList(pos, entityBox, collidingBoxes, BODY.offset(0, offset, 0));
        addCollisionBoxToList(pos, entityBox, collidingBoxes, HANDLE.offset(0, offset, 0));
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return false;
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
        return getDefaultState().withProperty(HANGING, (meta & 1) != 0)
                .withProperty(WATERLOGGED, (meta & 2) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(HANGING) ? 1 : 0) | (state.getValue(WATERLOGGED) ? 2 : 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {HANGING, WATERLOGGED},
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
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!canStay(world, pos, state.getValue(HANGING))) {
            dropBlockAsItem(world, pos, state, 0);
            WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
            return;
        }
        WaterloggedBlockApi.onNeighborChanged(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, java.util.Random random) {
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
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }
}
