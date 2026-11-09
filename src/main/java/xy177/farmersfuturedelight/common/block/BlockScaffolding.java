package xy177.farmersfuturedelight.common.block;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockScaffolding extends BlockFalling implements IWaterloggableBlock {
    public static final PropertyInteger DISTANCE = PropertyInteger.create("distance", 0, 7);
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    public static final PropertyBool BOTTOM = PropertyBool.create("bottom");
    private static final AxisAlignedBB TOP = new AxisAlignedBB(0, 0.875D, 0, 1, 1, 1);
    private static final AxisAlignedBB BOTTOM_PLATE = new AxisAlignedBB(0, 0, 0, 1, 0.125D, 1);
    private static final AxisAlignedBB[] LEGS = {
            new AxisAlignedBB(0, 0, 0, 0.125D, 1, 0.125D),
            new AxisAlignedBB(0.875D, 0, 0, 1, 1, 0.125D),
            new AxisAlignedBB(0, 0, 0.875D, 0.125D, 1, 1),
            new AxisAlignedBB(0.875D, 0, 0.875D, 1, 1, 1)};

    public BlockScaffolding() {
        super(Material.CIRCUITS);
        setRegistryName(FarmerFutureDelight.MODID, "scaffolding");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".scaffolding");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0);
        setLightOpacity(0);
        setSoundType(FFDSounds.SCAFFOLDING);
        setDefaultState(blockState.getBaseState().withProperty(DISTANCE, 7)
                .withProperty(WATERLOGGED, false).withProperty(BOTTOM, false));
    }

    public static int distance(IBlockAccess world, BlockPos pos) {
        IBlockState below = world.getBlockState(pos.down());
        int distance = 7;
        if (below.getBlock() instanceof BlockScaffolding) {
            distance = below.getValue(DISTANCE);
        } else if (!(below.getBlock() instanceof BlockLeaves)
                && (below.getBlock() instanceof BlockSnow ? below.getValue(BlockSnow.LAYERS) == 8
                : below.getBlockFaceShape(world, pos.down(), EnumFacing.UP) == BlockFaceShape.SOLID)) {
            return 0;
        }
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            IBlockState neighbor = world.getBlockState(pos.offset(direction));
            if (neighbor.getBlock() instanceof BlockScaffolding) {
                distance = Math.min(distance, neighbor.getValue(DISTANCE) + 1);
                if (distance == 1) {
                    break;
                }
            }
        }
        return distance;
    }

    private boolean bottom(IBlockAccess world, BlockPos pos, int distance) {
        return distance > 0 && world.getBlockState(pos.down()).getBlock() != this;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(BOTTOM, bottom(world, pos, state.getValue(DISTANCE)));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return distance(world, pos) < 7;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        int distance = distance(world, pos);
        return getDefaultState().withProperty(DISTANCE, distance)
                .withProperty(BOTTOM, bottom(world, pos, distance))
                .withProperty(WATERLOGGED, WaterloggedBlockApi.isWaterSource(world, pos));
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.scheduleUpdate(pos, this, 1);
        WaterloggedBlockApi.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        world.scheduleUpdate(pos, this, 1);
        world.markBlockRangeForRenderUpdate(pos, pos);
        WaterloggedBlockApi.onNeighborChanged(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || world.getBlockState(pos).getBlock() != this) {
            return;
        }
        int distance = distance(world, pos);
        if (distance == 7) {
            if (state.getValue(DISTANCE) == 7) {
                IBlockState falling = state.withProperty(WATERLOGGED, false)
                        .withProperty(BOTTOM, bottom(world, pos, distance));
                EntityFallingBlock entity = new EntityFallingBlock(world,
                        pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, falling);
                entity.fallTime = 1;
                WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
                world.spawnEntity(entity);
            } else {
                world.playEvent(2001, pos, Block.getStateId(state));
                dropBlockAsItem(world, pos, state, 0);
                WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
            }
            return;
        }
        IBlockState updated = state.withProperty(DISTANCE, distance)
                .withProperty(BOTTOM, bottom(world, pos, distance));
        if (updated != state) {
            world.setBlockState(pos, updated, 3);
        }
        if (isWaterloggedState(updated)) {
            WaterloggedBlockApi.updateTick(world, pos, updated);
        }
    }

    @Override
    public void onEndFalling(World world, BlockPos pos, IBlockState falling, IBlockState replaced) {
        boolean water = replaced.getMaterial() == Material.WATER
                && replaced.getPropertyKeys().contains(BlockLiquid.LEVEL)
                && replaced.getValue(BlockLiquid.LEVEL) == 0;
        int distance = distance(world, pos);
        world.setBlockState(pos, falling.withProperty(DISTANCE, distance)
                .withProperty(BOTTOM, bottom(world, pos, distance)).withProperty(WATERLOGGED, water), 3);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
            AxisAlignedBB query, List<AxisAlignedBB> boxes, Entity entity, boolean actual) {
        double feet = entity == null ? Double.POSITIVE_INFINITY : entity.getEntityBoundingBox().minY;
        if (feet >= pos.getY() + 1.0D - 1.0E-5D && (entity == null || !entity.isSneaking())) {
            addCollisionBoxToList(pos, query, boxes, TOP);
            for (AxisAlignedBB leg : LEGS) {
                addCollisionBoxToList(pos, query, boxes, leg);
            }
        } else if (state.getValue(DISTANCE) != 0 && bottom(world, pos, state.getValue(DISTANCE))
                && feet >= pos.getY() - 1.0E-5D) {
            addCollisionBoxToList(pos, query, boxes, BOTTOM_PLATE);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }

    public List<AxisAlignedBB> outline(IBlockState state, IBlockAccess world, BlockPos pos, boolean holding) {
        if (holding) {
            return Collections.singletonList(FULL_BLOCK_AABB);
        }
        List<AxisAlignedBB> boxes = new ArrayList<>();
        boxes.add(TOP);
        Collections.addAll(boxes, LEGS);
        if (bottom(world, pos, state.getValue(DISTANCE))) {
            boxes.add(BOTTOM_PLATE);
        }
        return boxes;
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        RayTraceResult nearest = null;
        for (AxisAlignedBB box : outline(state, world, pos,
                world.isRemote && FarmerFutureDelight.proxy.isHoldingScaffolding())) {
            RayTraceResult hit = rayTrace(pos, start, end, box);
            if (hit != null && (nearest == null
                    || start.squareDistanceTo(hit.hitVec) < start.squareDistanceTo(nearest.hitVec))) {
                nearest = hit;
            }
        }
        RayTraceResult interaction = super.collisionRayTrace(state, world, pos, start, end);
        return nearest != null && interaction != null
                && start.squareDistanceTo(interaction.hitVec) < start.squareDistanceTo(nearest.hitVec)
                ? new RayTraceResult(nearest.hitVec, interaction.sideHit, pos) : nearest;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
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
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return MapColor.SAND;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return isWaterloggedState(world.getBlockState(pos)) ? 0 : 60;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return isWaterloggedState(world.getBlockState(pos)) ? 0 : 60;
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
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
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {DISTANCE, WATERLOGGED, BOTTOM},
                WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(DISTANCE, meta & 7).withProperty(WATERLOGGED, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DISTANCE) | (isWaterloggedState(state) ? 8 : 0);
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
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }
}
