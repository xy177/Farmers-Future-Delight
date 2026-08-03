package xy177.farmersfuturedelight.common.block;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;

/** Bridges 1.12's block-only liquid system with modern waterlogged plant states. */
public final class WaterloggedPlantFluid {
    private static final int WATER_TICK_RATE = 5;

    private WaterloggedPlantFluid() {
    }

    public static boolean isSourceWater(IBlockAccess world, BlockPos pos) {
        return isSourceWater(world.getBlockState(pos));
    }

    static boolean isSourceWater(IBlockState state) {
        if (state.getMaterial() != Material.WATER
                || !state.getPropertyKeys().contains(BlockLiquid.LEVEL)
                || state.getValue(BlockLiquid.LEVEL) != 0) {
            return false;
        }
        Block block = state.getBlock();
        return block == Blocks.WATER
                || block == Blocks.FLOWING_WATER
                || block instanceof BlockUnderwaterPlant
                || block instanceof BlockSeaPickle && BlockSeaPickle.isWaterlogged(state)
                || BlockSmallDripleaf.isWaterlogged(state)
                || BlockBigDripleafStem.isWaterlogged(state)
                || BlockBigDripleaf.isWaterlogged(state)
                || BlockHangingRoots.isWaterlogged(state)
                || BlockGlowLichen.isWaterlogged(state);
    }

    public static boolean isWaterlogged(IBlockState state) {
        return isSourceWater(state);
    }

    static void onBlockAdded(World world, BlockPos pos, Block block) {
        if (world.isRemote) {
            return;
        }
        if (!world.isAreaLoaded(pos, 1, false)) {
            IBlockState state = world.getBlockState(pos);
            if (isSourceWater(state)) {
                world.scheduleUpdate(pos, state.getBlock(), WATER_TICK_RATE);
            }
            return;
        }
        IBlockState state = waterlogFromNeighbors(world, pos);
        if (isSourceWater(state)) {
            Block currentBlock = state.getBlock();
            world.scheduleUpdate(pos, currentBlock, WATER_TICK_RATE);
            world.notifyNeighborsOfStateChange(pos, currentBlock, false);
        }
    }

    static void onNeighborChanged(World world, BlockPos pos, Block block) {
        if (world.isRemote) {
            return;
        }
        IBlockState state = waterlogFromNeighbors(world, pos);
        if (isSourceWater(state)) {
            world.scheduleUpdate(pos, state.getBlock(), WATER_TICK_RATE);
        }
    }

    static void updateTick(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote || !isSourceWater(state) || !world.isAreaLoaded(pos, 4)) {
            return;
        }

        BlockPos below = pos.down();
        if (canFlowInto(world, below)) {
            placeFlow(world, below, 8);
            return;
        }

        for (EnumFacing direction : getPossibleFlowDirections(world, pos)) {
            placeFlow(world, pos.offset(direction), 1);
        }
    }

    public static IBlockState withWaterlogged(IBlockState state, boolean waterlogged) {
        Block block = state.getBlock();
        if (block instanceof BlockSeaPickle) {
            return state.withProperty(BlockSeaPickle.WATERLOGGED, waterlogged);
        }
        if (block instanceof BlockSmallDripleaf) {
            return state.withProperty(BlockSmallDripleaf.WATERLOGGED, waterlogged);
        }
        if (block instanceof BlockBigDripleafStem) {
            return state.withProperty(BlockBigDripleafStem.WATERLOGGED, waterlogged);
        }
        if (BlockBigDripleaf.isBigDripleaf(state)) {
            BlockBigDripleaf target = waterlogged ? FFDBlocks.BIG_DRIPLEAF_WATERLOGGED
                    : FFDBlocks.BIG_DRIPLEAF;
            return target.getDefaultState()
                    .withProperty(BlockBigDripleaf.FACING, state.getValue(BlockBigDripleaf.FACING))
                    .withProperty(BlockBigDripleaf.TILT, state.getValue(BlockBigDripleaf.TILT));
        }
        if (block instanceof BlockHangingRoots) {
            return state.withProperty(BlockHangingRoots.WATERLOGGED, waterlogged);
        }
        if (BlockGlowLichen.isGlowLichen(state)) {
            return BlockGlowLichen.stateFor(BlockGlowLichen.getFaceMask(state), waterlogged);
        }
        return null;
    }

    private static IBlockState waterlogFromNeighbors(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        IBlockState wetState = withWaterlogged(state, true);
        if (wetState == null || isSourceWater(state) || !hasIncomingWater(world, pos)) {
            return state;
        }
        world.setBlockState(pos, wetState, 3);
        return world.getBlockState(pos);
    }

    private static boolean hasIncomingWater(World world, BlockPos pos) {
        if (world.getBlockState(pos.up()).getMaterial() == Material.WATER) {
            return true;
        }
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            if (world.getBlockState(pos.offset(direction)).getMaterial() == Material.WATER) {
                return true;
            }
        }
        return false;
    }

    private static Set<EnumFacing> getPossibleFlowDirections(World world, BlockPos pos) {
        int shortestSlope = Integer.MAX_VALUE;
        Set<EnumFacing> directions = EnumSet.noneOf(EnumFacing.class);
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            BlockPos target = pos.offset(direction);
            if (!canFlowInto(world, target)) {
                continue;
            }
            int slope = isFlowBlocked(world, target.down())
                    ? getSlopeDistance(world, target, 1, direction.getOpposite()) : 0;
            if (slope < shortestSlope) {
                directions.clear();
                shortestSlope = slope;
            }
            if (slope == shortestSlope) {
                directions.add(direction);
            }
        }
        return directions;
    }

    private static int getSlopeDistance(World world, BlockPos pos, int distance,
                                        EnumFacing excludedDirection) {
        int shortest = 1000;
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            if (direction == excludedDirection) {
                continue;
            }
            BlockPos target = pos.offset(direction);
            if (!canFlowInto(world, target)) {
                continue;
            }
            if (!isFlowBlocked(world, target.down())) {
                return distance;
            }
            if (distance < 4) {
                shortest = Math.min(shortest,
                        getSlopeDistance(world, target, distance + 1, direction.getOpposite()));
            }
        }
        return shortest;
    }

    private static boolean canFlowInto(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        if (material == Material.WATER || material == Material.LAVA || isFlowBlocked(world, pos)) {
            return false;
        }
        return true;
    }

    private static boolean isFlowBlocked(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        Material material = state.getMaterial();
        if (block instanceof BlockDoor || block == Blocks.STANDING_SIGN
                || block == Blocks.LADDER || block == Blocks.REEDS) {
            return true;
        }
        return material == Material.PORTAL || material == Material.STRUCTURE_VOID
                || material.blocksMovement();
    }

    private static void placeFlow(World world, BlockPos pos, int level) {
        IBlockState oldState = world.getBlockState(pos);
        if (!canFlowInto(world, pos)) {
            return;
        }
        IBlockState wetState = withWaterlogged(oldState, true);
        if (wetState != null) {
            world.setBlockState(pos, wetState, 3);
            world.scheduleUpdate(pos, wetState.getBlock(), WATER_TICK_RATE);
            return;
        }
        if (oldState.getMaterial() != Material.AIR && oldState.getBlock() != Blocks.SNOW_LAYER) {
            oldState.getBlock().dropBlockAsItem(world, pos, oldState, 0);
        }
        world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState()
                .withProperty(BlockLiquid.LEVEL, level), 3);
    }
}
