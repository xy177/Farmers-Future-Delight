package xy177.farmersfuturedelight.common.block;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class BlockModernWall extends BlockFutureStone implements IWaterloggableBlock {
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    public static final PropertyEnum<Side> NORTH = PropertyEnum.create("north", Side.class);
    public static final PropertyEnum<Side> EAST = PropertyEnum.create("east", Side.class);
    public static final PropertyEnum<Side> SOUTH = PropertyEnum.create("south", Side.class);
    public static final PropertyEnum<Side> WEST = PropertyEnum.create("west", Side.class);

    public BlockModernWall(String name, IBlockState base, float hardness, float resistance) {
        super(name, hardness, resistance, SoundType.STONE);
        setMapColor(base.getMapColor(null, BlockPos.ORIGIN));
        setDefaultState(blockState.getBaseState().withProperty(UP, true)
                .withProperty(NORTH, Side.NONE).withProperty(EAST, Side.NONE)
                .withProperty(SOUTH, Side.NONE).withProperty(WEST, Side.NONE)
                .withProperty(WATERLOGGED, false));
        useNeighborBrightness = true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty<?>[] {UP, NORTH, EAST, SOUTH, WEST,
                WATERLOGGED}, WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(WATERLOGGED, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(WATERLOGGED) ? 1 : 0;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    private static PropertyEnum<Side> property(EnumFacing facing) {
        switch (facing) {
            case NORTH: return NORTH;
            case SOUTH: return SOUTH;
            case EAST: return EAST;
            default: return WEST;
        }
    }

    private static boolean connects(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        BlockPos other = pos.offset(facing);
        IBlockState state = world.getBlockState(other);
        Block block = state.getBlock();
        if (block instanceof BlockModernWall || block instanceof BlockWall || block instanceof BlockPane) {
            return true;
        }
        if (block instanceof BlockFenceGate) {
            return state.getValue(BlockFenceGate.FACING).getAxis() != facing.getAxis();
        }
        return block.canBeConnectedTo(world, other, facing.getOpposite())
                || !isExceptBlockForAttachWithPiston(block) && block != Blocks.BARRIER
                && block != Blocks.MELON_BLOCK && block != Blocks.PUMPKIN && block != Blocks.LIT_PUMPKIN
                && state.getBlockFaceShape(world, other, facing.getOpposite()) == BlockFaceShape.SOLID;
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return facing.getAxis().isHorizontal();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        int ceiling = world instanceof World ? FFDHeightHooks.maxYExclusive((World) world)
                : FFDHeightHooks.MAX_Y_EXCLUSIVE;
        if (pos.getY() >= ceiling) return state;
        List<IBlockState> column = new ArrayList<>();
        BlockPos top = pos.up();
        IBlockState above = world.getBlockState(top);
        while (top.getY() < ceiling && above.getBlock() instanceof BlockModernWall) {
            column.add(above);
            top = top.up();
            above = world.getBlockState(top);
        }
        if (top.getY() >= ceiling) above = Blocks.AIR.getDefaultState();
        if (above.getBlock() instanceof BlockWall) {
            above = above.getActualState(world, top);
        }
        for (int index = column.size() - 1; index >= 0; index--) {
            top = top.down();
            above = ((BlockModernWall) column.get(index).getBlock())
                    .connectedState(column.get(index), world, top, above);
        }
        return connectedState(state, world, pos, above);
    }

    private IBlockState connectedState(IBlockState state, IBlockAccess world, BlockPos pos,
                                       IBlockState above) {
        BlockPos abovePos = pos.up();
        List<AxisAlignedBB> boxes = bottomShape(above, world, abovePos);
        for (EnumFacing direction : EnumFacing.HORIZONTALS) {
            Side side = connects(world, pos, direction)
                    ? covered(boxes, arm(direction, 7.0 / 16, 9.0 / 16, 9.0 / 16, 1))
                            ? Side.TALL : Side.LOW : Side.NONE;
            state = state.withProperty(property(direction), side);
        }
        boolean north = state.getValue(NORTH) == Side.NONE;
        boolean south = state.getValue(SOUTH) == Side.NONE;
        boolean east = state.getValue(EAST) == Side.NONE;
        boolean west = state.getValue(WEST) == Side.NONE;
        boolean postAbove = above.getBlock() instanceof BlockModernWall
                && above.getValue(UP)
                || above.getBlock() instanceof BlockWall && above.getValue(BlockWall.UP);
        boolean corner = north && south && east && west || north != south || east != west;
        boolean tallPair = state.getValue(NORTH) == Side.TALL && state.getValue(SOUTH) == Side.TALL
                || state.getValue(EAST) == Side.TALL && state.getValue(WEST) == Side.TALL;
        Block top = above.getBlock();
        boolean override = top instanceof BlockTorch || top instanceof BlockTripWire
                || top instanceof BlockSign || top instanceof BlockBanner || top instanceof BlockBasePressurePlate;
        boolean post = postAbove || corner || !tallPair && (override
                || covered(boxes, new AxisAlignedBB(7.0 / 16, 0, 7.0 / 16, 9.0 / 16, 1, 9.0 / 16)));
        return state.withProperty(UP, post);
    }

    private static List<AxisAlignedBB> bottomShape(IBlockState state, IBlockAccess world, BlockPos pos) {
        List<AxisAlignedBB> boxes = new ArrayList<>();
        if (state.getBlock() instanceof BlockModernWall) {
            return shape(state, true);
        }
        if (world instanceof World) {
            state.addCollisionBoxToList((World) world, pos, new AxisAlignedBB(pos), boxes, null, false);
            List<AxisAlignedBB> relative = new ArrayList<>();
            for (AxisAlignedBB box : boxes) {
                relative.add(box.offset(-pos.getX(), -pos.getY(), -pos.getZ()));
            }
            return relative;
        }
        if (state.getBlock() instanceof BlockStairs) {
            state = state.getActualState(world, pos);
            if (state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.BOTTOM) {
                boxes.add(FULL_BLOCK_AABB);
            } else {
                EnumFacing facing = state.getValue(BlockStairs.FACING);
                BlockStairs.EnumShape type = state.getValue(BlockStairs.SHAPE);
                AxisAlignedBB half = arm(facing, 0, 1, 0.5, 0.5);
                if (type == BlockStairs.EnumShape.STRAIGHT || type == BlockStairs.EnumShape.INNER_LEFT
                        || type == BlockStairs.EnumShape.INNER_RIGHT) {
                    boxes.add(half);
                }
                if (type != BlockStairs.EnumShape.STRAIGHT) {
                    boolean left = type == BlockStairs.EnumShape.OUTER_LEFT
                            || type == BlockStairs.EnumShape.INNER_LEFT;
                    AxisAlignedBB lateral = arm(left ? facing.rotateYCCW() : facing.rotateY(), 0, 1, 0.5, 0.5);
                    AxisAlignedBB longitudinal = type == BlockStairs.EnumShape.INNER_LEFT
                            || type == BlockStairs.EnumShape.INNER_RIGHT
                            ? arm(facing.getOpposite(), 0, 1, 0.5, 0.5) : half;
                    boxes.add(lateral.intersect(longitudinal));
                }
            }
        } else {
            AxisAlignedBB box = state.getCollisionBoundingBox(world, pos);
            if (box != null) {
                boxes.add(box);
            }
        }
        return boxes;
    }

    private static boolean covered(List<AxisAlignedBB> boxes, AxisAlignedBB test) {
        List<AxisAlignedBB> remaining = new ArrayList<>();
        remaining.add(test);
        for (AxisAlignedBB box : boxes) {
            if (box.minY > 0 || box.maxY <= 0) {
                continue;
            }
            List<AxisAlignedBB> next = new ArrayList<>();
            for (AxisAlignedBB area : remaining) {
                double minX = Math.max(area.minX, box.minX);
                double maxX = Math.min(area.maxX, box.maxX);
                double minZ = Math.max(area.minZ, box.minZ);
                double maxZ = Math.min(area.maxZ, box.maxZ);
                if (minX >= maxX || minZ >= maxZ) {
                    next.add(area);
                    continue;
                }
                if (area.minX < minX) next.add(new AxisAlignedBB(area.minX, 0, area.minZ, minX, 1, area.maxZ));
                if (maxX < area.maxX) next.add(new AxisAlignedBB(maxX, 0, area.minZ, area.maxX, 1, area.maxZ));
                if (area.minZ < minZ) next.add(new AxisAlignedBB(minX, 0, area.minZ, maxX, 1, minZ));
                if (maxZ < area.maxZ) next.add(new AxisAlignedBB(minX, 0, maxZ, maxX, 1, area.maxZ));
            }
            remaining = next;
            if (remaining.isEmpty()) return true;
        }
        return false;
    }

    private static AxisAlignedBB arm(EnumFacing facing, double min, double max, double reach, double height) {
        switch (facing) {
            case NORTH: return new AxisAlignedBB(min, 0, 0, max, height, reach);
            case SOUTH: return new AxisAlignedBB(min, 0, 1 - reach, max, height, 1);
            case EAST: return new AxisAlignedBB(1 - reach, 0, min, 1, height, max);
            default: return new AxisAlignedBB(0, 0, min, reach, height, max);
        }
    }

    private static List<AxisAlignedBB> shape(IBlockState state, boolean collision) {
        List<AxisAlignedBB> boxes = new ArrayList<>();
        if (state.getValue(UP)) boxes.add(new AxisAlignedBB(0.25, 0, 0.25, 0.75, collision ? 1.5 : 1, 0.75));
        for (EnumFacing direction : EnumFacing.HORIZONTALS) {
            Side side = state.getValue(property(direction));
            if (side != Side.NONE) boxes.add(arm(direction, 5.0 / 16, 11.0 / 16, 11.0 / 16,
                    collision ? 1.5 : side == Side.TALL ? 1 : 14.0 / 16));
        }
        return boxes;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        IBlockState rotated = state;
        for (EnumFacing direction : EnumFacing.HORIZONTALS) {
            rotated = rotated.withProperty(property(rotation.rotate(direction)), state.getValue(property(direction)));
        }
        return rotated;
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        IBlockState mirrored = state;
        for (EnumFacing direction : EnumFacing.HORIZONTALS) {
            mirrored = mirrored.withProperty(property(mirror.mirror(direction)), state.getValue(property(direction)));
        }
        return mirrored;
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) { return false; }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return face.getAxis().isHorizontal() ? BlockFaceShape.MIDDLE_POLE_THICK : BlockFaceShape.CENTER_BIG;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        AxisAlignedBB bounds = null;
        for (AxisAlignedBB box : shape(getActualState(state, world, pos), false)) bounds = bounds == null ? box : bounds.union(box);
        return bounds == null ? NULL_AABB : bounds;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB query,
                                      List<AxisAlignedBB> result, @Nullable Entity entity, boolean actual) {
        for (AxisAlignedBB box : shape(actual ? state : getActualState(state, world, pos), true)) {
            addCollisionBoxToList(pos, query, result, box);
        }
    }

    @Override
    @Nullable
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        RayTraceResult nearest = null;
        for (AxisAlignedBB box : shape(getActualState(state, world, pos), false)) {
            RayTraceResult hit = rayTrace(pos, start, end, box);
            if (hit != null && (nearest == null || start.squareDistanceTo(hit.hitVec) < start.squareDistanceTo(nearest.hitVec))) nearest = hit;
        }
        return nearest;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(WATERLOGGED, WaterloggedBlockApi.isWaterSource(world, pos));
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
        WaterloggedBlockApi.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos from) {
        WaterloggedBlockApi.onNeighborChanged(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, java.util.Random random) {
        if (isWaterloggedState(state)) WaterloggedBlockApi.updateTick(world, pos, state);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean harvest) {
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
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || isWaterloggedState(state) && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }

    public enum Side implements IStringSerializable {
        NONE, LOW, TALL;

        @Override
        public String getName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
}
