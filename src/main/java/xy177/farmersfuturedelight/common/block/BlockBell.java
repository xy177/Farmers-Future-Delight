package xy177.farmersfuturedelight.common.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.registry.FFDStats;
import xy177.farmersfuturedelight.common.tile.TileEntityBell;

public class BlockBell extends BlockContainer {
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyEnum<BellAttachment> ATTACHMENT =
            PropertyEnum.create("attachment", BellAttachment.class);
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockBell() {
        super(Material.IRON);
        setRegistryName(FarmerFutureDelight.MODID, "bell");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".bell");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(5.0F);
        setResistance(5.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
                .withProperty(ATTACHMENT, BellAttachment.FLOOR).withProperty(POWERED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ATTACHMENT, POWERED);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityBell();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side,
                                            float hitX, float hitY, float hitZ, int metadata,
                                            EntityLivingBase placer) {
        return placementState(world, pos, side, placer.getHorizontalFacing());
    }

    private IBlockState placementState(World world, BlockPos pos, EnumFacing side,
                                        EnumFacing facing) {
        BellAttachment attachment;
        if (side.getAxis() == EnumFacing.Axis.Y) {
            attachment = side == EnumFacing.DOWN
                    ? BellAttachment.CEILING : BellAttachment.FLOOR;
        } else {
            facing = side.getOpposite();
            boolean attachedForward = isSolidSupport(world, pos.offset(facing),
                    facing.getOpposite());
            boolean attachedBackward = isSolidSupport(world, pos.offset(facing.getOpposite()),
                    facing);
            attachment = attachedForward && attachedBackward
                    ? BellAttachment.DOUBLE_WALL : BellAttachment.SINGLE_WALL;
        }
        IBlockState state = getDefaultState().withProperty(FACING, facing)
                .withProperty(ATTACHMENT, attachment);
        if (canSurviveAt(world, pos, state)) {
            return state;
        }
        if (side.getAxis() != EnumFacing.Axis.Y) {
            BellAttachment fallback = isSolidSupport(world, pos.down(), EnumFacing.UP)
                    ? BellAttachment.FLOOR : BellAttachment.CEILING;
            state = state.withProperty(ATTACHMENT, fallback);
            if (canSurviveAt(world, pos, state)) {
                return state;
            }
        }
        return null;
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState()
                .withProperty(FACING, EnumFacing.getHorizontal(metadata & 3))
                .withProperty(ATTACHMENT, BellAttachment.values()[(metadata >> 2) & 3]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex()
                | state.getValue(ATTACHMENT).ordinal() << 2;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (isSolidSupport(world, pos.offset(facing), facing.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return placementState(world, pos, side, EnumFacing.NORTH) != null;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        updatePower(world, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                net.minecraft.block.Block neighborBlock, BlockPos neighborPos) {
        BellAttachment attachment = state.getValue(ATTACHMENT);
        if (attachment == BellAttachment.SINGLE_WALL
                || attachment == BellAttachment.DOUBLE_WALL) {
            EnumFacing facing = state.getValue(FACING);
            boolean forward = isSolidSupport(world, pos.offset(facing), facing.getOpposite());
            boolean backward = isSolidSupport(world, pos.offset(facing.getOpposite()), facing);
            if (attachment == BellAttachment.DOUBLE_WALL) {
                if (forward && backward) {
                    updatePower(world, pos, state);
                    return;
                }
                if (forward || backward) {
                    state = state.withProperty(ATTACHMENT, BellAttachment.SINGLE_WALL)
                            .withProperty(FACING, forward ? facing : facing.getOpposite());
                    world.setBlockState(pos, state, 3);
                } else {
                    removeUnsupported(world, pos, state);
                    return;
                }
            } else if (forward) {
                if (backward) {
                    state = state.withProperty(ATTACHMENT, BellAttachment.DOUBLE_WALL);
                    world.setBlockState(pos, state, 3);
                }
            } else {
                removeUnsupported(world, pos, state);
                return;
            }
        } else if (!canSurviveAt(world, pos, state)) {
            removeUnsupported(world, pos, state);
            return;
        }
        updatePower(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing face,
                                    float hitX, float hitY, float hitZ) {
        boolean hit = onHit(world, pos, state, face, hitY);
        if (hit && !world.isRemote) {
            player.addStat(FFDStats.BELL_RING);
        }
        return hit;
    }

    public boolean onHit(World world, BlockPos pos, IBlockState state,
                         EnumFacing face, double hitY) {
        if (face == null || !isProperHit(state, face, hitY)) {
            return false;
        }
        ring(world, pos, face);
        return true;
    }

    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos,
                                 int eventId, int eventParameter) {
        super.eventReceived(state, world, pos, eventId, eventParameter);
        TileEntity tile = world.getTileEntity(pos);
        return tile != null && tile.receiveClientEvent(eventId, eventParameter);
    }

    @Override
    public IBlockState withRotation(IBlockState state, net.minecraft.util.Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, net.minecraft.util.Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos pos,
                                            net.minecraft.world.Explosion explosion) {
        if (!world.isRemote) {
            ring(world, pos, null);
        }
    }

    public void ring(World world, BlockPos pos, EnumFacing direction) {
        if (world.isRemote) {
            return;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityBell)) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        EnumFacing hitDirection = direction == null ? state.getValue(FACING) : direction;
        ((TileEntityBell) tile).ring(hitDirection);
        world.playSound(null, pos, FFDSounds.BELL_USE,
                net.minecraft.util.SoundCategory.BLOCKS, 2.0F, 1.0F);
    }

    public void updatePower(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            return;
        }
        boolean powered = world.isBlockPowered(pos);
        TileEntity tile = world.getTileEntity(pos);
        boolean wasPowered = tile instanceof TileEntityBell
                ? ((TileEntityBell) tile).isPowered() : state.getValue(POWERED);
        if (powered != wasPowered) {
            if (tile instanceof TileEntityBell) ((TileEntityBell) tile).setPowered(powered);
            if (powered) {
                ring(world, pos, state.getValue(FACING));
            }
            world.setBlockState(pos, state.withProperty(POWERED, powered), 3);
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityBell
                ? state.withProperty(POWERED, ((TileEntityBell) tile).isPowered()) : state;
    }

    private static boolean isProperHit(IBlockState state, EnumFacing side, double hitY) {
        if (side.getAxis() == EnumFacing.Axis.Y || hitY > 0.8124F) {
            return false;
        }
        EnumFacing facing = state.getValue(FACING);
        BellAttachment attachment = state.getValue(ATTACHMENT);
        if (attachment == BellAttachment.FLOOR) {
            return facing.getAxis() == side.getAxis();
        }
        if (attachment == BellAttachment.SINGLE_WALL
                || attachment == BellAttachment.DOUBLE_WALL) {
            return facing.getAxis() != side.getAxis();
        }
        return attachment == BellAttachment.CEILING;
    }

    private static boolean canSurviveAt(World world, BlockPos pos, IBlockState state) {
        BellAttachment attachment = state.getValue(ATTACHMENT);
        if (attachment == BellAttachment.FLOOR) {
            return isSolidSupport(world, pos.down(), EnumFacing.UP);
        }
        if (attachment == BellAttachment.CEILING) {
            return isSolidSupport(world, pos.up(), EnumFacing.DOWN);
        }
        return isSolidSupport(world, pos.offset(state.getValue(FACING)),
                state.getValue(FACING).getOpposite());
    }

    private void removeUnsupported(World world, BlockPos pos, IBlockState state) {
        dropBlockAsItem(world, pos, state, 0);
        world.setBlockToAir(pos);
    }

    private static boolean isSolidSupport(World world, BlockPos pos, EnumFacing side) {
        return world.getBlockState(pos).isSideSolid(world, pos, side);
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        List<AxisAlignedBB> shapes = shapes(state);
        AxisAlignedBB bounds = shapes.get(0);
        for (int index = 1; index < shapes.size(); index++) {
            bounds = bounds.union(shapes.get(index));
        }
        return bounds;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
                                      AxisAlignedBB entityBox, List<AxisAlignedBB> boxes,
                                      Entity entity, boolean actualState) {
        for (AxisAlignedBB shape : shapes(state)) {
            addCollisionBoxToList(pos, entityBox, boxes, shape);
        }
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos,
                                            Vec3d start, Vec3d end) {
        RayTraceResult closest = null;
        double distance = Double.MAX_VALUE;
        for (AxisAlignedBB shape : shapes(state)) {
            RayTraceResult hit = rayTrace(pos, start, end, shape);
            if (hit != null && start.squareDistanceTo(hit.hitVec) < distance) {
                closest = hit;
                distance = start.squareDistanceTo(hit.hitVec);
            }
        }
        return closest;
    }

    private static List<AxisAlignedBB> shapes(IBlockState state) {
        List<AxisAlignedBB> shapes = new ArrayList<>();
        BellAttachment attachment = state.getValue(ATTACHMENT);
        EnumFacing facing = state.getValue(FACING);
        if (attachment == BellAttachment.FLOOR) {
            shapes.add(facing.getAxis() == EnumFacing.Axis.Z
                    ? box(0, 0, 4, 16, 16, 12) : box(4, 0, 0, 12, 16, 16));
            return shapes;
        }
        shapes.add(box(5, 6, 5, 11, 13, 11));
        shapes.add(box(4, 4, 4, 12, 6, 12));
        if (attachment == BellAttachment.CEILING) {
            shapes.add(box(7, 13, 7, 9, 16, 9));
        } else if (attachment == BellAttachment.DOUBLE_WALL) {
            shapes.add(facing.getAxis() == EnumFacing.Axis.Z
                    ? box(7, 13, 0, 9, 15, 16) : box(0, 13, 7, 16, 15, 9));
        } else if (facing == EnumFacing.NORTH) {
            shapes.add(box(7, 13, 0, 9, 15, 13));
        } else if (facing == EnumFacing.SOUTH) {
            shapes.add(box(7, 13, 3, 9, 15, 16));
        } else if (facing == EnumFacing.WEST) {
            shapes.add(box(0, 13, 7, 13, 15, 9));
        } else {
            shapes.add(box(3, 13, 7, 16, 15, 9));
        }
        return shapes;
    }

    private static AxisAlignedBB box(int minX, int minY, int minZ,
                                     int maxX, int maxY, int maxZ) {
        return new AxisAlignedBB(minX / 16.0D, minY / 16.0D, minZ / 16.0D,
                maxX / 16.0D, maxY / 16.0D, maxZ / 16.0D);
    }
}
