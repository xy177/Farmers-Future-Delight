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
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;

/** Bridges 1.12's block-only liquid system with modern waterlogged plant states. */
public final class WaterloggedPlantFluid {
    private static final int WATER_TICK_RATE = 5;
    public static final IUnlistedProperty<Boolean> WATER_ABOVE = new BooleanProperty("water_above");
    public static final IUnlistedProperty<Boolean> WATER_NORTH_VISIBLE =
            new BooleanProperty("water_north_visible");
    public static final IUnlistedProperty<Boolean> WATER_SOUTH_VISIBLE =
            new BooleanProperty("water_south_visible");
    public static final IUnlistedProperty<Boolean> WATER_WEST_VISIBLE =
            new BooleanProperty("water_west_visible");
    public static final IUnlistedProperty<Boolean> WATER_EAST_VISIBLE =
            new BooleanProperty("water_east_visible");
    public static final IUnlistedProperty<Boolean> WATER_DOWN_VISIBLE =
            new BooleanProperty("water_down_visible");
    public static final IUnlistedProperty<Float> WATER_NORTH_WEST =
            new FloatProperty("water_north_west");
    public static final IUnlistedProperty<Float> WATER_SOUTH_WEST =
            new FloatProperty("water_south_west");
    public static final IUnlistedProperty<Float> WATER_SOUTH_EAST =
            new FloatProperty("water_south_east");
    public static final IUnlistedProperty<Float> WATER_NORTH_EAST =
            new FloatProperty("water_north_east");

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
                || BlockGlowLichen.isWaterlogged(state)
                || BlockAmethystCluster.isWaterlogged(state);
    }

    public static boolean isWaterlogged(IBlockState state) {
        return isSourceWater(state);
    }

    public static boolean hasWaterAbove(IBlockAccess world, BlockPos pos) {
        return isWaterState(world.getBlockState(pos.up()));
    }

    public static IUnlistedProperty<?>[] extendedProperties() {
        return new IUnlistedProperty<?>[] {
                WATER_ABOVE, WATER_NORTH_VISIBLE, WATER_SOUTH_VISIBLE,
                WATER_WEST_VISIBLE, WATER_EAST_VISIBLE, WATER_DOWN_VISIBLE,
                WATER_NORTH_WEST, WATER_SOUTH_WEST,
                WATER_SOUTH_EAST, WATER_NORTH_EAST};
    }

    public static IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!(state instanceof IExtendedBlockState)) {
            return state;
        }
        IExtendedBlockState extended = (IExtendedBlockState) state;
        if (!isSourceWater(state)) {
            return extended;
        }
        float northWest = getFluidHeight(world, pos);
        float southWest = getFluidHeight(world, pos.south());
        float southEast = getFluidHeight(world, pos.east().south());
        float northEast = getFluidHeight(world, pos.east());
        return extended
                .withProperty(WATER_ABOVE, hasWaterAbove(world, pos))
                .withProperty(WATER_NORTH_VISIBLE,
                        world.getBlockState(pos.north()).getMaterial() != Material.WATER)
                .withProperty(WATER_SOUTH_VISIBLE,
                        world.getBlockState(pos.south()).getMaterial() != Material.WATER)
                .withProperty(WATER_WEST_VISIBLE,
                        world.getBlockState(pos.west()).getMaterial() != Material.WATER)
                .withProperty(WATER_EAST_VISIBLE,
                        world.getBlockState(pos.east()).getMaterial() != Material.WATER)
                .withProperty(WATER_DOWN_VISIBLE,
                        world.getBlockState(pos.down()).getMaterial() != Material.WATER)
                .withProperty(WATER_NORTH_WEST, northWest)
                .withProperty(WATER_SOUTH_WEST, southWest)
                .withProperty(WATER_SOUTH_EAST, southEast)
                .withProperty(WATER_NORTH_EAST, northEast);
    }

    private static float getFluidHeight(IBlockAccess world, BlockPos pos) {
        int count = 0;
        float total = 0.0F;
        for (int index = 0; index < 4; index++) {
            BlockPos sample = pos.add(-(index & 1), 0, -((index >> 1) & 1));
            if (isWaterState(world.getBlockState(sample.up()))) {
                return 1.0F;
            }
            IBlockState state = world.getBlockState(sample);
            Material material = state.getMaterial();
            if (!isWaterState(state)) {
                if (!material.isSolid()) {
                    total += 1.0F;
                    count++;
                }
                continue;
            }
            int level = state.getPropertyKeys().contains(BlockLiquid.LEVEL)
                    ? state.getValue(BlockLiquid.LEVEL) : 0;
            float height = BlockLiquid.getLiquidHeightPercent(level);
            if (level >= 8 || level == 0) {
                total += height * 10.0F;
                count += 10;
            }
            total += height;
            count++;
        }
        return count == 0 ? 0.0F : 1.0F - total / count;
    }

    private static boolean isWaterState(IBlockState state) {
        return state.getMaterial() == Material.WATER;
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
        IBlockState state = world.getBlockState(pos);
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
        IBlockState state = world.getBlockState(pos);
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
        if (block instanceof BlockAmethystCluster) {
            return state.withProperty(BlockAmethystCluster.WATERLOGGED, waterlogged);
        }
        return null;
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
        if (material == Material.WATER || material == Material.LAVA) {
            return false;
        }
        IBlockState wetState = withWaterlogged(state, true);
        return wetState == null && !isFlowBlocked(world, pos);
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
        if (oldState.getMaterial() != Material.AIR && oldState.getBlock() != Blocks.SNOW_LAYER) {
            oldState.getBlock().dropBlockAsItem(world, pos, oldState, 0);
        }
        world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState()
                .withProperty(BlockLiquid.LEVEL, level), 3);
    }

    private static final class BooleanProperty implements IUnlistedProperty<Boolean> {
        private final String name;

        private BooleanProperty(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isValid(Boolean value) {
            return value != null;
        }

        @Override
        public Class<Boolean> getType() {
            return Boolean.class;
        }

        @Override
        public String valueToString(Boolean value) {
            return String.valueOf(value);
        }
    }

    private static final class FloatProperty implements IUnlistedProperty<Float> {
        private final String name;

        private FloatProperty(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isValid(Float value) {
            return value != null && !value.isNaN() && !value.isInfinite()
                    && value >= 0.0F && value <= 1.0F;
        }

        @Override
        public Class<Float> getType() {
            return Float.class;
        }

        @Override
        public String valueToString(Float value) {
            return String.valueOf(value);
        }
    }
}
