package xy177.farmersfuturedelight.common.block;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class WaterloggedPlantFluid {
    private static final int WATER_TICK_RATE = 5;
    private static Field quantaPerBlockField;
    private static boolean quantaPerBlockFieldResolved;
    private static final List<WaterloggedBlockApi.Adapter> ADAPTERS = new ArrayList<>();
    private static final List<WaterloggedBlockApi.FluidMixRule> FLUID_MIX_RULES = new ArrayList<>();
    private static final ThreadLocal<Boolean> IN_ADAPTER_CALLBACK = new ThreadLocal<>();
    private static final ThreadLocal<Integer> WORLDGEN_DEPTH = new ThreadLocal<>();
    private static final Map<Block, ExtendedBlockState> RENDER_STATES = new LinkedHashMap<>();
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
    public static final IUnlistedProperty<Float> WATER_MODEL_OFFSET_X =
            new FloatProperty("water_model_offset_x", -1.0F, 1.0F);
    public static final IUnlistedProperty<Float> WATER_MODEL_OFFSET_Y =
            new FloatProperty("water_model_offset_y", -1.0F, 1.0F);
    public static final IUnlistedProperty<Float> WATER_MODEL_OFFSET_Z =
            new FloatProperty("water_model_offset_z", -1.0F, 1.0F);
    public static final IUnlistedProperty<String> CONTAINED_FLUID =
            new StringProperty("contained_fluid");

    private WaterloggedPlantFluid() {
    }

    public static synchronized void registerAdapter(WaterloggedBlockApi.Adapter adapter) {
        if (adapter != null && !ADAPTERS.contains(adapter)) {
            ADAPTERS.add(adapter);
        }
    }

    public static synchronized void unregisterAdapter(WaterloggedBlockApi.Adapter adapter) {
        ADAPTERS.remove(adapter);
    }

    public static synchronized void registerFluidMixRule(WaterloggedBlockApi.FluidMixRule rule) {
        if (rule != null && !FLUID_MIX_RULES.contains(rule)) {
            FLUID_MIX_RULES.add(rule);
        }
    }

    public static synchronized void unregisterFluidMixRule(WaterloggedBlockApi.FluidMixRule rule) {
        FLUID_MIX_RULES.remove(rule);
    }

    public static WorldgenScope enterWorldgen() {
        Integer depth = WORLDGEN_DEPTH.get();
        WORLDGEN_DEPTH.set(depth == null ? 1 : depth + 1);
        return new WorldgenScope();
    }

    private static boolean isWorldgen() {
        Integer depth = WORLDGEN_DEPTH.get();
        return depth != null && depth > 0;
    }

    public static boolean isSourceWater(IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getSourceFluid(world, pos) == FluidRegistry.WATER;
    }

    public static boolean containsWater(IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getContainedFluid(world, pos) == FluidRegistry.WATER;
    }

    public static boolean containsWater(IBlockState state) {
        if (state == null) {
            return false;
        }
        if (isWaterloggedBlock(state)) {
            return true;
        }
        Block block = state.getBlock();
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
    }

    public static boolean isSourceWater(IBlockState state) {
        if (state == null) {
            return false;
        }
        if (isWaterloggedBlock(state)) {
            return true;
        }
        if (state.getMaterial() != Material.WATER
                || !state.getPropertyKeys().contains(BlockLiquid.LEVEL)
                || state.getValue(BlockLiquid.LEVEL) != 0) {
            return false;
        }
        Block block = state.getBlock();
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
    }

    public static boolean isWaterloggedBlock(IBlockState state) {
        if (state == null) {
            return false;
        }
        if (state instanceof IExtendedBlockState
                && ((IExtendedBlockState) state).getUnlistedNames().contains(CONTAINED_FLUID)
                && ((IExtendedBlockState) state).getValue(CONTAINED_FLUID) != null) {
            return !((IExtendedBlockState) state).getValue(CONTAINED_FLUID).isEmpty();
        }
        Block block = state.getBlock();
        if (block instanceof IWaterloggableBlock) {
            return ((IWaterloggableBlock) block).isWaterloggedState(state);
        }
        if (!FFDConfig.isWaterloggingEnabled()) {
            return false;
        }
        if (WaterloggedBlockApi.hasUniversalWaterloggedProperty(state)) {
            return state.getValue(WaterloggedBlockApi.UNIVERSAL_WATERLOGGED);
        }
        return adapterIsWaterlogged(state);
    }

    public static boolean isWaterlogged(IBlockState state) {
        return isWaterloggedBlock(state);
    }

    public static boolean hasWaterAbove(IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.containsWater(world, pos.up());
    }

    public static IUnlistedProperty<?>[] extendedProperties() {
        return new IUnlistedProperty<?>[] {
                WATER_ABOVE, WATER_NORTH_VISIBLE, WATER_SOUTH_VISIBLE,
                WATER_WEST_VISIBLE, WATER_EAST_VISIBLE, WATER_DOWN_VISIBLE,
                WATER_NORTH_WEST, WATER_SOUTH_WEST,
                WATER_SOUTH_EAST, WATER_NORTH_EAST,
                WATER_MODEL_OFFSET_X, WATER_MODEL_OFFSET_Y, WATER_MODEL_OFFSET_Z,
                CONTAINED_FLUID};
    }

    public static IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!WaterloggedBlockApi.isWaterlogged(world, pos)) {
            return state;
        }
        if (!(state instanceof IExtendedBlockState)
                || !((IExtendedBlockState) state).getUnlistedNames().contains(WATER_ABOVE)) {
            IBlockState original = state;
            ExtendedBlockState container;
            synchronized (RENDER_STATES) {
                container = RENDER_STATES.get(state.getBlock());
                if (container == null) {
                    List<IUnlistedProperty<?>> unlisted = new ArrayList<>();
                    if (state instanceof IExtendedBlockState) {
                        unlisted.addAll(((IExtendedBlockState) state).getUnlistedNames());
                    }
                    for (IUnlistedProperty<?> property : extendedProperties()) {
                        if (!unlisted.contains(property)) {
                            unlisted.add(property);
                        }
                    }
                    container = new ExtendedBlockState(state.getBlock(),
                            state.getPropertyKeys().toArray(new IProperty<?>[0]),
                            unlisted.toArray(new IUnlistedProperty<?>[0]));
                    RENDER_STATES.put(state.getBlock(), container);
                }
            }
            state = container.getBaseState();
            for (IProperty<?> property : original.getPropertyKeys()) {
                state = copyListedProperty(state, original, property);
            }
            if (original instanceof IExtendedBlockState) {
                for (IUnlistedProperty<?> property
                        : ((IExtendedBlockState) original).getUnlistedNames()) {
                    state = copyUnlistedProperty((IExtendedBlockState) state,
                            (IExtendedBlockState) original, property);
                }
            }
        }
        IExtendedBlockState extended = (IExtendedBlockState) state;
        Fluid fluid = WaterloggedBlockApi.getContainedFluid(world, pos);
        if (fluid == null) {
            return extended.withProperty(CONTAINED_FLUID, "");
        }
        float northWest = getFluidHeight(world, pos, fluid);
        float southWest = getFluidHeight(world, pos.south(), fluid);
        float southEast = getFluidHeight(world, pos.east().south(), fluid);
        float northEast = getFluidHeight(world, pos.east(), fluid);
        Vec3d offset = state.getOffset(world, pos);
        return extended
                .withProperty(WATER_ABOVE, containsFluid(world, pos.up(), fluid))
                .withProperty(WATER_NORTH_VISIBLE,
                        isFluidFaceVisible(state, world, pos, EnumFacing.NORTH))
                .withProperty(WATER_SOUTH_VISIBLE,
                        isFluidFaceVisible(state, world, pos, EnumFacing.SOUTH))
                .withProperty(WATER_WEST_VISIBLE,
                        isFluidFaceVisible(state, world, pos, EnumFacing.WEST))
                .withProperty(WATER_EAST_VISIBLE,
                        isFluidFaceVisible(state, world, pos, EnumFacing.EAST))
                .withProperty(WATER_DOWN_VISIBLE,
                        isFluidFaceVisible(state, world, pos, EnumFacing.DOWN))
                .withProperty(WATER_NORTH_WEST, northWest)
                .withProperty(WATER_SOUTH_WEST, southWest)
                .withProperty(WATER_SOUTH_EAST, southEast)
                .withProperty(WATER_NORTH_EAST, northEast)
                .withProperty(WATER_MODEL_OFFSET_X, (float) offset.x)
                .withProperty(WATER_MODEL_OFFSET_Y, (float) offset.y)
                .withProperty(WATER_MODEL_OFFSET_Z, (float) offset.z)
                .withProperty(CONTAINED_FLUID, fluid.getName());
    }

    private static <T extends Comparable<T>> IBlockState copyListedProperty(
            IBlockState target, IBlockState source, IProperty<T> property) {
        return target.withProperty(property, source.getValue(property));
    }

    private static <T> IBlockState copyUnlistedProperty(IExtendedBlockState target,
            IExtendedBlockState source, IUnlistedProperty<T> property) {
        return target.withProperty(property, source.getValue(property));
    }

    private static boolean isFluidFaceVisible(IBlockState state, IBlockAccess world,
            BlockPos pos, EnumFacing face) {
        IBlockState neighbor = world.getBlockState(pos.offset(face));
        Fluid fluid = WaterloggedBlockApi.getContainedFluid(world, pos);
        return !containsFluid(world, pos.offset(face), fluid)
                && (state.getBlock() instanceof BlockLeaves
                        || !state.isSideSolid(world, pos, face))
                && !neighbor.isOpaqueCube();
    }

    public static IBlockState getFluidState(IBlockState state) {
        if (state == null) {
            return Blocks.AIR.getDefaultState();
        }
        if (isSourceWater(state)) {
            return Blocks.WATER.getDefaultState();
        }
        if (state.getMaterial() == Material.WATER
                && state.getPropertyKeys().contains(BlockLiquid.LEVEL)) {
            return Blocks.FLOWING_WATER.getDefaultState()
                    .withProperty(BlockLiquid.LEVEL, state.getValue(BlockLiquid.LEVEL));
        }
        return Blocks.AIR.getDefaultState();
    }

    private static float getFluidHeight(IBlockAccess world, BlockPos pos, Fluid fluid) {
        int count = 0;
        float total = 0.0F;
        for (int index = 0; index < 4; index++) {
            BlockPos sample = pos.add(-(index & 1), 0, -((index >> 1) & 1));
            if (containsFluid(world, sample.up(), fluid)) {
                return 1.0F;
            }
            IBlockState state = world.getBlockState(sample);
            Material material = state.getMaterial();
            if (!containsFluid(world, sample, fluid)) {
                Fluid sampleFluid = WaterloggedBlockApi.getContainedFluid(world, sample);
                if (sampleFluid != null) {
                    continue;
                }
                if (!material.isSolid()) {
                    count++;
                }
                continue;
            }
            IBlockState stored = xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates.get(world, sample);
            int level = fluidLevel(stored == null ? state : stored);
            float height = fluidHeight(world, sample, stored == null ? state : stored, level);
            if (height >= 0.999F || level == 0) {
                total += height * 10.0F;
                count += 10;
            }
            total += height;
            count++;
        }
        return count == 0 ? 0.0F : total / count;
    }

    private static boolean isWaterState(IBlockState state) {
        return state != null
                && (state.getMaterial() == Material.WATER || isSourceWater(state));
    }

    public static boolean isSourceFluid(IBlockState state) {
        if (state == null || WaterloggedBlockApi.getFluidForBlock(state.getBlock()) == null) {
            return false;
        }
        return fluidLevel(state) == 0;
    }

    private static boolean containsFluid(IBlockAccess world, BlockPos pos, Fluid fluid) {
        return fluid != null && WaterloggedBlockApi.getContainedFluid(world, pos) == fluid;
    }

    private static int fluidLevel(IBlockState state) {
        if (state == null || isWaterloggedBlock(state)) {
            return 0;
        }
        if (state.getPropertyKeys().contains(BlockLiquid.LEVEL)) {
            return state.getValue(BlockLiquid.LEVEL);
        }
        if (state.getPropertyKeys().contains(BlockFluidBase.LEVEL)) {
            return state.getValue(BlockFluidBase.LEVEL);
        }
        return state.getBlock().getMetaFromState(state) & 15;
    }

    private static float fluidHeight(IBlockAccess world, BlockPos pos, IBlockState state,
                                     int level) {
        if (isWaterloggedBlock(state)) {
            return 1.0F - BlockLiquid.getLiquidHeightPercent(0);
        }
        if (state.getBlock() instanceof BlockFluidBase) {
            return Math.max(0.0F, Math.min(1.0F,
                    ((BlockFluidBase) state.getBlock()).getFluidHeightForRender(
                            world, pos, world.getBlockState(pos.up()))));
        }
        return 1.0F - BlockLiquid.getLiquidHeightPercent(level);
    }

    public static void onBlockAdded(World world, BlockPos pos, Block block) {
        if (world.isRemote || isWorldgen()) {
            return;
        }
        if (!world.isAreaLoaded(pos, 1, false)) {
            IBlockState state = world.getBlockState(pos);
            if (isWaterloggedBlock(state)) {
                world.scheduleUpdate(pos, state.getBlock(), WATER_TICK_RATE);
            }
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (isWaterloggedBlock(state)) {
            Block currentBlock = state.getBlock();
            Fluid fluid = WaterloggedBlockApi.getContainedFluid(world, pos);
            world.scheduleUpdate(pos, currentBlock,
                    WaterloggedBlockApi.fluidTickRate(world, fluid));
            world.notifyNeighborsOfStateChange(pos, currentBlock, false);
        }
    }

    public static void onNeighborChanged(World world, BlockPos pos, Block block) {
        if (world.isRemote) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (WaterloggedBlockApi.isWaterlogged(world, pos) || canFormWaterSource(world, pos, state)) {
            Fluid fluid = WaterloggedBlockApi.isWaterlogged(world, pos)
                    ? WaterloggedBlockApi.getContainedFluid(world, pos) : FluidRegistry.WATER;
            world.scheduleUpdate(pos, state.getBlock(),
                    WaterloggedBlockApi.fluidTickRate(world, fluid));
        }
    }

    public static void updateTick(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote || !world.isAreaLoaded(pos, 4)) {
            return;
        }
        if (!WaterloggedBlockApi.isWaterlogged(world, pos)) {
            if (!tryFormWaterSource(world, pos, state)) {
                return;
            }
            state = world.getBlockState(pos);
        }

        Fluid contained = WaterloggedBlockApi.getContainedFluid(world, pos);
        if (!updateImportedFlow(world, pos, state, contained)) return;
        tryMixContainedFluidNeighbors(world, pos, state, contained);
        flowContainedFluid(world, pos, contained);
    }

    private static boolean updateImportedFlow(World world, BlockPos pos, IBlockState host, Fluid fluid) {
        IBlockState stored = xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates.get(world, pos);
        if (stored == null || fluid == null || fluidLevel(stored) == 0) return true;
        int current = fluidLevel(stored);
        boolean forge = stored.getBlock() instanceof BlockFluidBase;
        int quanta = forge ? fluidQuanta(fluid) : 8;
        int minimum = Integer.MAX_VALUE;
        int sources = 0;
        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.offset(side);
            if (!containsFluid(world, neighbor, fluid)
                    || !canPassThrough(world, neighbor, pos, side.getOpposite())) continue;
            int level = fluidLevel(WaterloggedBlockApi.getFluidState(world, neighbor));
            if (level == 0) sources++;
            if (!forge && level >= 8) level = 0;
            minimum = Math.min(minimum, level);
        }
        int next = minimum == Integer.MAX_VALUE ? -1 : minimum + horizontalFlowLevel(world, fluid);
        if (next >= quanta) next = -1;
        EnumFacing vertical = fluid.isLighterThanAir() ? EnumFacing.UP : EnumFacing.DOWN;
        BlockPos feeder = pos.offset(vertical.getOpposite());
        if (containsFluid(world, feeder, fluid)
                && canPassThrough(world, feeder, pos, vertical)) {
            int level = fluidLevel(WaterloggedBlockApi.getFluidState(world, feeder));
            next = forge ? 1 : level >= 8 ? level : level + 8;
        }
        BlockPos floor = pos.offset(vertical);
        if (sources >= 2 && (world.getBlockState(floor).getMaterial().isSolid()
                || WaterloggedBlockApi.getSourceFluid(world, floor) == fluid)
                && ForgeEventFactory.canCreateFluidSource(world, pos, stored, fluid == FluidRegistry.WATER))
            next = 0;
        if (next == current) return true;
        if (next < 0) {
            WaterloggedBlockApi.recordContainedFluid(world, pos, null);
            IBlockState dry = withWaterlogged(host, false);
            if (dry != null && dry != host) world.setBlockState(pos, dry, 3);
            world.notifyNeighborsOfStateChange(pos, host.getBlock(), false);
            return false;
        }
        xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates.set(world, pos, flowState(fluid, next));
        int delay = WaterloggedBlockApi.fluidTickRate(world, fluid);
        if (fluid == FluidRegistry.LAVA && current < 8 && next < 8
                && next > current && world.rand.nextInt(4) != 0) delay *= 4;
        world.scheduleUpdate(pos, host.getBlock(), delay);
        world.notifyNeighborsOfStateChange(pos, host.getBlock(), false);
        return true;
    }

    private static boolean tryMixContainedFluidNeighbors(World world, BlockPos sourcePos,
            IBlockState sourceState, Fluid source) {
        if (source == null || !WaterloggedBlockApi.isWaterlogged(world, sourcePos)) {
            return false;
        }
        return tryMixFluidNeighbors(world, sourcePos, sourceState, source);
    }

    public static boolean tryMixFluidNeighbors(World world, BlockPos sourcePos,
            IBlockState sourceState) {
        if (world == null || sourcePos == null || sourceState == null || world.isRemote) {
            return false;
        }
        Fluid source = WaterloggedBlockApi.getFluidForBlock(sourceState.getBlock());
        if (source == null) {
            return false;
        }
        return tryMixFluidNeighbors(world, sourcePos, sourceState, source);
    }

    private static boolean tryMixFluidNeighbors(World world, BlockPos sourcePos,
            IBlockState sourceState, Fluid source) {
        boolean mixed = false;
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos targetPos = sourcePos.offset(side);
            IBlockState targetState = world.getBlockState(targetPos);
            Fluid target = WaterloggedBlockApi.getContainedFluid(world, targetPos);
            if (target == null || target == source
                    || !canFluidsMeet(world, sourcePos, sourceState, targetPos, targetState,
                            side)) {
                continue;
            }
            mixed |= mixFluids(world, sourcePos, targetPos, side, sourceState,
                    targetState, source, target);
        }
        return mixed;
    }

    private static void flowContainedFluid(World world, BlockPos pos, Fluid fluid) {
        if (fluid == null || fluid.getBlock() == null) {
            return;
        }
        EnumFacing vertical = fluid.isLighterThanAir() ? EnumFacing.UP : EnumFacing.DOWN;
        BlockPos verticalPos = pos.offset(vertical);
        int current = fluidLevel(WaterloggedBlockApi.getFluidState(world, pos));
        boolean forge = fluid.getBlock() instanceof BlockFluidBase;
        int verticalLevel = forge ? 1 : current >= 8 ? current : current + 8;
        if (canFlowInto(world, verticalPos, fluid)
                && canPassThrough(world, pos, verticalPos, vertical)) {
            if (placeFlow(world, pos, verticalPos, vertical, verticalLevel, fluid)) {
                return;
            }
        }
        int horizontalLevel = (forge || current < 8 ? current : 0) + horizontalFlowLevel(world, fluid);
        if (horizontalLevel >= (forge ? fluidQuanta(fluid) : 8)) return;
        int slopeDistance = slopeDistance(world, fluid);
        for (EnumFacing direction : getPossibleFlowDirections(world, pos, vertical, fluid,
                slopeDistance)) {
            placeFlow(world, pos, pos.offset(direction), direction, horizontalLevel, fluid);
        }
    }

    public static IBlockState withWaterlogged(IBlockState state, boolean waterlogged) {
        if (state == null) {
            return null;
        }
        Block block = state.getBlock();
        if (block instanceof IWaterloggableBlock) {
            return ((IWaterloggableBlock) block).setWaterloggedState(state, waterlogged);
        }
        if (!FFDConfig.isWaterloggingEnabled()) {
            return null;
        }
        if (WaterloggedBlockApi.hasUniversalWaterloggedProperty(state)) {
            return state.withProperty(WaterloggedBlockApi.UNIVERSAL_WATERLOGGED, waterlogged);
        }
        if (Boolean.TRUE.equals(IN_ADAPTER_CALLBACK.get())) {
            return null;
        }
        List<WaterloggedBlockApi.Adapter> adapters;
        synchronized (WaterloggedPlantFluid.class) {
            adapters = new ArrayList<>(ADAPTERS);
        }
        IN_ADAPTER_CALLBACK.set(true);
        try {
            for (WaterloggedBlockApi.Adapter adapter : adapters) {
                if (adapter.matches(state)) {
                    return adapter.withWaterlogged(state, waterlogged);
                }
            }
        } finally {
            IN_ADAPTER_CALLBACK.remove();
        }
        return null;
    }

    private static boolean adapterIsWaterlogged(IBlockState state) {
        if (Boolean.TRUE.equals(IN_ADAPTER_CALLBACK.get())) {
            return false;
        }
        List<WaterloggedBlockApi.Adapter> adapters;
        synchronized (WaterloggedPlantFluid.class) {
            adapters = new ArrayList<>(ADAPTERS);
        }
        IN_ADAPTER_CALLBACK.set(true);
        try {
            for (WaterloggedBlockApi.Adapter adapter : adapters) {
                if (adapter.matches(state)) {
                    return adapter.isWaterlogged(state);
                }
            }
        } finally {
            IN_ADAPTER_CALLBACK.remove();
        }
        return false;
    }

    private static Set<EnumFacing> getPossibleFlowDirections(World world, BlockPos pos,
            EnumFacing vertical, Fluid fluid, int maximumDistance) {
        int shortestSlope = Integer.MAX_VALUE;
        Set<EnumFacing> directions = EnumSet.noneOf(EnumFacing.class);
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            BlockPos target = pos.offset(direction);
            if (!canFlowInto(world, target, fluid)
                    || !canPassThrough(world, pos, target, direction)) {
                continue;
            }
            BlockPos verticalTarget = target.offset(vertical);
            int slope = !canFlowInto(world, verticalTarget, fluid)
                    || !canPassThrough(world, target, verticalTarget, vertical)
                    ? getSlopeDistance(world, target, 1, direction.getOpposite(), vertical,
                            fluid, maximumDistance) : 0;
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
            EnumFacing excludedDirection, EnumFacing vertical, Fluid fluid,
            int maximumDistance) {
        int shortest = 1000;
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            if (direction == excludedDirection) {
                continue;
            }
            BlockPos target = pos.offset(direction);
            if (!canFlowInto(world, target, fluid)
                    || !canPassThrough(world, pos, target, direction)) {
                continue;
            }
            BlockPos verticalTarget = target.offset(vertical);
            if (canFlowInto(world, verticalTarget, fluid)
                    && canPassThrough(world, target, verticalTarget, vertical)) {
                return distance;
            }
            if (distance < maximumDistance) {
                shortest = Math.min(shortest,
                        getSlopeDistance(world, target, distance + 1,
                                direction.getOpposite(), vertical, fluid, maximumDistance));
            }
        }
        return shortest;
    }

    private static boolean canFlowInto(World world, BlockPos pos, Fluid fluid) {
        IBlockState state = world.getBlockState(pos);
        Fluid contained = WaterloggedBlockApi.getContainedFluid(world, pos);
        if (contained == fluid) {
            return false;
        }
        if (isVanillaMix(fluid, contained)) {
            return true;
        }
        IBlockState wetState = withWaterlogged(state, true);
        if (wetState != null || FFDConfig.isAdditionalWaterloggingBlock(world, state.getBlock())
                || WaterloggedBlockApi.isWaterlogged(world, pos)) {
            return false;
        }
        Block fluidBlock = fluid == null ? null : fluid.getBlock();
        if (fluidBlock instanceof BlockFluidBase) {
            return ((BlockFluidBase) fluidBlock).canDisplace(world, pos);
        }
        Material material = state.getMaterial();
        Material sourceMaterial = WaterloggedBlockApi.sourceState(fluid).getMaterial();
        return material != sourceMaterial && material != Material.LAVA
                && !isFlowBlocked(world, pos);
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

    private static boolean placeFlow(World world, BlockPos sourcePos, BlockPos pos,
            EnumFacing direction, int level, Fluid fluid) {
        IBlockState oldState = world.getBlockState(pos);
        if (fluid == FluidRegistry.WATER && tryFormWaterSource(world, pos, oldState)) {
            return true;
        }
        Fluid existingFluid = WaterloggedBlockApi.getContainedFluid(world, pos);
        if (existingFluid == fluid) {
            return false;
        }
        if (mixFluids(world, sourcePos, pos, direction, flowState(fluid, level),
                oldState, fluid, existingFluid)) {
            return true;
        }
        if (WaterloggedBlockApi.isWaterlogged(world, pos)) {
            return false;
        }
        if (!canFlowInto(world, pos, fluid)) {
            return false;
        }
        Block fluidBlock = fluid.getBlock();
        if (fluidBlock instanceof BlockFluidBase
                && !((BlockFluidBase) fluidBlock).displaceIfPossible(world, pos)) {
            return false;
        }
        if (!(fluidBlock instanceof BlockFluidBase)
                && oldState.getMaterial() != Material.AIR
                && oldState.getBlock() != Blocks.SNOW_LAYER) {
            oldState.getBlock().dropBlockAsItem(world, pos, oldState, 0);
        }
        IBlockState flowState = flowState(fluid, level);
        if (flowState.getBlock() != Blocks.AIR) {
            IBlockState placedState = ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos,
                    sourcePos, flowState);
            if (isWaterloggedBlock(oldState)) {
                WaterloggedBlockApi.recordContainedFluid(world, pos, null);
            }
            if (world.setBlockState(pos, placedState, 3)) {
                Fluid placedFluid = WaterloggedBlockApi.getFluidForBlock(placedState.getBlock());
                if (placedFluid != null) {
                    world.scheduleUpdate(pos, placedState.getBlock(),
                            WaterloggedBlockApi.fluidTickRate(world, placedFluid));
                }
                return true;
            }
        }
        return false;
    }

    private static boolean canFluidsMeet(World world, BlockPos sourcePos,
            IBlockState sourceState, BlockPos targetPos, IBlockState targetState,
            EnumFacing direction) {
        return canPassThrough(world, sourcePos, targetPos, direction)
                || sourceState.getBlock() instanceof BlockLeaves && isWaterloggedBlock(sourceState)
                || targetState.getBlock() instanceof BlockLeaves && isWaterloggedBlock(targetState);
    }

    private static boolean mixFluids(World world, BlockPos sourcePos, BlockPos pos,
            EnumFacing direction, IBlockState sourceState, IBlockState targetState,
            Fluid source, Fluid target) {
        if (source == null || target == null || source == target) {
            return false;
        }
        IBlockState result = getMixResult(world, pos, sourcePos, direction,
                sourceState, targetState, source, target);
        if (result == null) {
            return false;
        }
        BlockPos resultPos = WaterloggedBlockApi.isWaterlogged(world, pos) ? sourcePos : pos;
        if (WaterloggedBlockApi.isWaterlogged(world, resultPos)) {
            return false;
        }
        BlockPos otherPos = resultPos.equals(pos) ? sourcePos : pos;
        result = ForgeEventFactory.fireFluidPlaceBlockEvent(world, resultPos, otherPos, result);
        if (!world.setBlockState(resultPos, result, 3)) {
            return false;
        }
        WaterloggedBlockApi.recordContainedFluid(world, resultPos, null);
        if (isVanillaMix(source, target)) {
            playMixEffects(world, resultPos);
        }
        return true;
    }

    private static IBlockState getMixResult(World world, BlockPos targetPos, BlockPos sourcePos,
            EnumFacing side, IBlockState sourceState, IBlockState targetState,
            Fluid source, Fluid target) {
        IBlockState result = vanillaMixResult(world, targetPos, sourcePos, side,
                sourceState, targetState, source, target);
        if (result != null) {
            return result;
        }
        List<WaterloggedBlockApi.FluidMixRule> rules;
        synchronized (WaterloggedPlantFluid.class) {
            rules = new ArrayList<>(FLUID_MIX_RULES);
        }
        for (WaterloggedBlockApi.FluidMixRule rule : rules) {
            if (rule.matches(source, target)) {
                result = rule.getResult(world, targetPos, sourcePos, side,
                        sourceState, targetState);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static IBlockState vanillaMixResult(World world, BlockPos targetPos,
            BlockPos sourcePos, EnumFacing side, IBlockState sourceState,
            IBlockState targetState, Fluid source, Fluid target) {
        if (!isVanillaMix(source, target)) {
            return null;
        }
        if (isFlowingFluidState(sourceState) && isFlowingFluidState(targetState)) {
            return Blocks.STONE.getDefaultState();
        }
        Material sourceMaterial = WaterloggedBlockApi.sourceState(source).getMaterial();
        if (sourceMaterial == Material.LAVA) {
            int sourceLevel = fluidLevel(sourceState);
            if (isWaterloggedBlock(targetState)) {
                return sourceLevel == 0 ? Blocks.OBSIDIAN.getDefaultState()
                        : Blocks.STONE.getDefaultState();
            }
            return side == EnumFacing.DOWN ? Blocks.STONE.getDefaultState()
                    : sourceLevel == 0 ? Blocks.OBSIDIAN.getDefaultState()
                            : Blocks.COBBLESTONE.getDefaultState();
        }
        int targetLevel = fluidLevel(targetState);
        if (targetLevel > 4) {
            return null;
        }
        return targetLevel == 0 ? Blocks.OBSIDIAN.getDefaultState()
                : Blocks.COBBLESTONE.getDefaultState();
    }

    private static boolean isFlowingFluidState(IBlockState state) {
        return isWaterloggedBlock(state) || fluidLevel(state) > 0;
    }

    private static boolean isVanillaMix(Fluid first, Fluid second) {
        if (first == null || second == null || first == second) {
            return false;
        }
        Material firstMaterial = WaterloggedBlockApi.sourceState(first).getMaterial();
        Material secondMaterial = WaterloggedBlockApi.sourceState(second).getMaterial();
        return firstMaterial == Material.WATER && secondMaterial == Material.LAVA
                || firstMaterial == Material.LAVA && secondMaterial == Material.WATER;
    }

    private static void playMixEffects(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS,
                0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
        for (int index = 0; index < 8; index++) {
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                    pos.getX() + world.rand.nextDouble(), pos.getY() + 1.2D,
                    pos.getZ() + world.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
        }
    }

    private static int horizontalFlowLevel(World world, Fluid fluid) {
        if (fluid.getBlock() instanceof BlockFluidBase) {
            return 1;
        }
        Material material = WaterloggedBlockApi.sourceState(fluid).getMaterial();
        return material == Material.LAVA && !world.provider.doesWaterVaporize() ? 2 : 1;
    }

    private static int slopeDistance(World world, Fluid fluid) {
        Material material = WaterloggedBlockApi.sourceState(fluid).getMaterial();
        if (!(fluid.getBlock() instanceof BlockFluidBase)) {
            return material == Material.LAVA && !world.provider.doesWaterVaporize() ? 2 : 4;
        }
        return Math.max(1, fluidQuanta(fluid) / 2);
    }

    private static int fluidQuanta(Fluid fluid) {
        int quanta = 8;
        try {
            Field field = quantaPerBlockField();
            if (field != null) {
                quanta = field.getInt(fluid.getBlock());
            }
        } catch (IllegalAccessException ignored) {
        }
        return Math.max(1, quanta);
    }

    private static Field quantaPerBlockField() {
        if (!quantaPerBlockFieldResolved) {
            quantaPerBlockFieldResolved = true;
            try {
                quantaPerBlockField = BlockFluidBase.class.getDeclaredField("quantaPerBlock");
                quantaPerBlockField.setAccessible(true);
            } catch (ReflectiveOperationException ignored) {
                quantaPerBlockField = null;
            }
        }
        return quantaPerBlockField;
    }

    private static IBlockState flowState(Fluid fluid, int level) {
        if (fluid == null || fluid.getBlock() == null) {
            return Blocks.AIR.getDefaultState();
        }
        IBlockState state;
        if (fluid == FluidRegistry.WATER) {
            state = Blocks.FLOWING_WATER.getDefaultState();
        } else if (fluid == FluidRegistry.LAVA) {
            state = Blocks.FLOWING_LAVA.getDefaultState();
        } else {
            state = fluid.getBlock().getDefaultState();
        }
        if (state.getPropertyKeys().contains(BlockLiquid.LEVEL)) {
            return state.withProperty(BlockLiquid.LEVEL, Math.max(0, Math.min(15, level)));
        }
        if (state.getPropertyKeys().contains(BlockFluidBase.LEVEL)) {
            return state.withProperty(BlockFluidBase.LEVEL, Math.max(0, Math.min(15, level)));
        }
        return state;
    }

    public static boolean tryFormWaterSource(World world, BlockPos pos, IBlockState state) {
        if (!canFormWaterSource(world, pos, state)) {
            return false;
        }
        IBlockState wet = withWaterlogged(state, true);
        if (world.setBlockState(pos, wet, 3)) {
            world.scheduleUpdate(pos, wet.getBlock(), WATER_TICK_RATE);
            return true;
        }
        return false;
    }

    private static boolean canFormWaterSource(World world, BlockPos pos, IBlockState state) {
        if (WaterloggedBlockApi.isWaterlogged(world, pos) || withWaterlogged(state, true) == null
                || !world.isAreaLoaded(pos, 1, false)) {
            return false;
        }
        IBlockState below = world.getBlockState(pos.down());
        if (!below.getMaterial().isSolid() && !isSourceWater(below)) {
            return false;
        }
        int sources = 0;
        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.offset(side);
            if (isSourceWater(world, neighbor) && canPassThrough(world, pos, neighbor, side)
                    && ++sources >= 2) {
                return ForgeEventFactory.canCreateFluidSource(world, pos, state, true);
            }
        }
        return false;
    }

    public static boolean canPassThrough(World world, BlockPos source, BlockPos target,
            EnumFacing direction) {
        List<double[]> rectangles = new ArrayList<>();
        collectFaceRectangles(world, source, direction, rectangles);
        collectFaceRectangles(world, target, direction.getOpposite(), rectangles);
        if (rectangles.isEmpty()) {
            return true;
        }
        TreeSet<Double> x = new TreeSet<>();
        TreeSet<Double> y = new TreeSet<>();
        x.add(0.0D);
        x.add(1.0D);
        y.add(0.0D);
        y.add(1.0D);
        for (double[] rectangle : rectangles) {
            x.add(rectangle[0]);
            x.add(rectangle[2]);
            y.add(rectangle[1]);
            y.add(rectangle[3]);
        }
        Double[] xs = x.toArray(new Double[0]);
        Double[] ys = y.toArray(new Double[0]);
        for (int i = 1; i < xs.length; i++) {
            for (int j = 1; j < ys.length; j++) {
                double cx = (xs[i - 1] + xs[i]) * 0.5D;
                double cy = (ys[j - 1] + ys[j]) * 0.5D;
                boolean covered = false;
                for (double[] rectangle : rectangles) {
                    if (cx > rectangle[0] && cx < rectangle[2]
                            && cy > rectangle[1] && cy < rectangle[3]) {
                        covered = true;
                        break;
                    }
                }
                if (!covered) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void collectFaceRectangles(World world, BlockPos pos, EnumFacing side,
            List<double[]> rectangles) {
        IBlockState state = world.getBlockState(pos);
        List<AxisAlignedBB> boxes = new ArrayList<>();
        state.addCollisionBoxToList(world, pos, new AxisAlignedBB(pos).grow(0.001D),
                boxes, null, false);
        for (AxisAlignedBB box : boxes) {
            double[] min = {box.minX - pos.getX(), box.minY - pos.getY(), box.minZ - pos.getZ()};
            double[] max = {box.maxX - pos.getX(), box.maxY - pos.getY(), box.maxZ - pos.getZ()};
            int axis = side.getAxis() == EnumFacing.Axis.X ? 0
                    : side.getAxis() == EnumFacing.Axis.Y ? 1 : 2;
            boolean touches = side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE
                    ? max[axis] >= 1.0D && min[axis] < 1.0D
                    : min[axis] <= 0.0D && max[axis] > 0.0D;
            if (!touches) {
                continue;
            }
            int a = axis == 0 ? 1 : 0;
            int b = axis == 2 ? 1 : 2;
            double x0 = Math.max(0.0D, min[a]);
            double y0 = Math.max(0.0D, min[b]);
            double x1 = Math.min(1.0D, max[a]);
            double y1 = Math.min(1.0D, max[b]);
            if (x0 < x1 && y0 < y1) {
                rectangles.add(new double[] {x0, y0, x1, y1});
            }
        }
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
        private final float minimum;
        private final float maximum;

        private FloatProperty(String name) {
            this(name, 0.0F, 1.0F);
        }

        private FloatProperty(String name, float minimum, float maximum) {
            this.name = name;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isValid(Float value) {
            return value != null && !value.isNaN() && !value.isInfinite()
                    && value >= minimum && value <= maximum;
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

    private static final class StringProperty implements IUnlistedProperty<String> {
        private final String name;

        private StringProperty(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isValid(String value) {
            return value != null;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }

        @Override
        public String valueToString(String value) {
            return value;
        }
    }

    public static final class WorldgenScope implements AutoCloseable {
        private boolean closed;

        private WorldgenScope() {
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            Integer depth = WORLDGEN_DEPTH.get();
            if (depth == null || depth <= 1) {
                WORLDGEN_DEPTH.remove();
            } else {
                WORLDGEN_DEPTH.set(depth - 1);
            }
        }
    }
}
