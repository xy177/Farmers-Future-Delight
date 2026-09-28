package xy177.farmersfuturedelight.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWall;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.BlockWaterloggedStateCarrier;
import xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid;
import xy177.farmersfuturedelight.common.fluid.FFDFluidloggedData;
import xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates;

public final class WaterloggedBlockApi {
    public static final PropertyBool UNIVERSAL_WATERLOGGED =
            new UniversalWaterloggedProperty();
    private static final Set<Block> RENDERED_BLOCKS =
            Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
    private static final Set<Block> WATER_COLOR_BLOCKS =
            Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
    private static final Set<ResourceLocation> RENDERED_MODELS =
            new java.util.LinkedHashSet<>();
    private static final Map<Block, Block> STATE_CARRIERS = new IdentityHashMap<>();
    private static final Map<IBlockState, Integer> WET_LEGACY_IDS = new IdentityHashMap<>();
    private static final Map<Integer, IBlockState> WET_STATES_BY_LEGACY_ID =
            new LinkedHashMap<>();

    private WaterloggedBlockApi() {
    }

    public interface Adapter {
        boolean matches(IBlockState state);

        boolean isWaterlogged(IBlockState state);

        IBlockState withWaterlogged(IBlockState state, boolean waterlogged);
    }

    public interface FluidMixRule {
        boolean matches(Fluid source, Fluid target);

        IBlockState getResult(World world, BlockPos targetPos, BlockPos sourcePos,
                               EnumFacing side, IBlockState sourceState, IBlockState targetState);
    }

    public static void registerAdapter(Adapter adapter) {
        WaterloggedPlantFluid.registerAdapter(adapter);
    }

    public static void unregisterAdapter(Adapter adapter) {
        WaterloggedPlantFluid.unregisterAdapter(adapter);
    }

    public static void registerFluidMixRule(FluidMixRule rule) {
        WaterloggedPlantFluid.registerFluidMixRule(rule);
    }

    public static void unregisterFluidMixRule(FluidMixRule rule) {
        WaterloggedPlantFluid.unregisterFluidMixRule(rule);
    }

    public static synchronized void registerRenderedBlock(Block block) {
        if (FFDConfig.isWaterloggingEnabled() && block != null) {
            RENDERED_BLOCKS.add(block);
        }
    }

    public static synchronized void unregisterRenderedBlock(Block block) {
        RENDERED_BLOCKS.remove(block);
    }

    public static synchronized void registerWaterColorBlock(Block block) {
        if (FFDConfig.isWaterloggingEnabled() && block != null) {
            WATER_COLOR_BLOCKS.add(block);
        }
    }

    public static synchronized void unregisterWaterColorBlock(Block block) {
        WATER_COLOR_BLOCKS.remove(block);
    }

    public static synchronized void registerRenderedModel(ResourceLocation model) {
        if (model != null) {
            RENDERED_MODELS.add(model);
        }
    }

    public static synchronized void unregisterRenderedModel(ResourceLocation model) {
        RENDERED_MODELS.remove(model);
    }

    public static synchronized List<Block> renderedBlocks() {
        return new ArrayList<>(RENDERED_BLOCKS);
    }

    public static synchronized List<Block> waterColorBlocks() {
        return new ArrayList<>(WATER_COLOR_BLOCKS);
    }

    public static synchronized boolean isRenderedModel(ResourceLocation model) {
        if (model == null) {
            return false;
        }
        if (RENDERED_MODELS.contains(new ResourceLocation(
                model.getResourceDomain(), model.getResourcePath()))) {
            return true;
        }
        for (Block block : RENDERED_BLOCKS) {
            ResourceLocation registryName = block.getRegistryName();
            if (registryName != null
                    && registryName.getResourceDomain().equals(model.getResourceDomain())
                    && registryName.getResourcePath().equals(model.getResourcePath())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWaterlogged(IBlockState state) {
        return WaterloggedPlantFluid.isWaterloggedBlock(state);
    }

    public static boolean hasUniversalWaterloggedProperty(IBlockState state) {
        if (state == null) {
            return false;
        }
        for (IProperty<?> property : state.getPropertyKeys()) {
            if (property == UNIVERSAL_WATERLOGGED) {
                return true;
            }
        }
        return false;
    }

    public static IProperty<?>[] appendUniversalWaterloggedProperty(
            Block block, IProperty<?>[] properties) {
        if (!FFDConfig.isWaterloggingEnabled()
                || !supportsUniversalWaterlogging(block) || properties == null) {
            return properties;
        }
        for (IProperty<?> property : properties) {
            if ("waterlogged".equals(property.getName())) {
                return properties;
            }
        }
        IProperty<?>[] expanded = java.util.Arrays.copyOf(properties, properties.length + 1);
        expanded[properties.length] = UNIVERSAL_WATERLOGGED;
        return expanded;
    }

    public static synchronized void prepareUniversalStateCarriers(
            IForgeRegistry<Block> registry) {
        if (!FFDConfig.isWaterloggingEnabled()) {
            return;
        }
        List<Block> sources = new ArrayList<>();
        for (Block block : registry.getValuesCollection()) {
            if (hasUniversalWaterloggedProperty(block.getDefaultState())
                    && requiresStateCarrier(block)) {
                sources.add(block);
            }
        }
        for (Block source : sources) {
            if (STATE_CARRIERS.containsKey(source) || source.getRegistryName() == null) {
                continue;
            }
            ResourceLocation sourceName = source.getRegistryName();
            Block carrier = new BlockWaterloggedStateCarrier();
            carrier.setRegistryName(FarmerFutureDelight.MODID,
                    "waterlogged_state/" + sourceName.getResourceDomain() + "/"
                            + sourceName.getResourcePath());
            registry.register(carrier);
            STATE_CARRIERS.put(source, carrier);
        }
    }

    public static synchronized void initializeUniversalStateMappings() {
        WET_LEGACY_IDS.clear();
        WET_STATES_BY_LEGACY_ID.clear();
        if (!FFDConfig.isWaterloggingEnabled()) {
            return;
        }
        for (Block source : net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS) {
            if (!hasUniversalWaterloggedProperty(source.getDefaultState())) {
                continue;
            }
            List<IBlockState> dryStates = persistedDryStates(source);
            List<Integer> freeMetas = freeMetadataValues(source, dryStates);
            Block aliasBlock = freeMetas.size() >= dryStates.size()
                    ? source : STATE_CARRIERS.get(source);
            if (aliasBlock == null) {
                continue;
            }
            int aliasBlockId = Block.getIdFromBlock(aliasBlock);
            for (int index = 0; index < dryStates.size(); index++) {
                IBlockState wetState = dryStates.get(index)
                        .withProperty(UNIVERSAL_WATERLOGGED, true);
                int aliasMeta = aliasBlock == source ? freeMetas.get(index) : index;
                int paletteId = aliasBlockId << 4 | aliasMeta;
                int legacyId = aliasBlockId | aliasMeta << 12;
                int sourceMeta = source.getMetaFromState(dryStates.get(index)) & 15;
                for (IBlockState variant : source.getBlockState().getValidStates()) {
                    if (variant.getValue(UNIVERSAL_WATERLOGGED)
                            && (source.getMetaFromState(variant) & 15) == sourceMeta) {
                        Block.BLOCK_STATE_IDS.put(variant, paletteId);
                        WET_LEGACY_IDS.put(variant, legacyId);
                    }
                }
                Block.BLOCK_STATE_IDS.put(wetState, paletteId);
                WET_LEGACY_IDS.put(wetState, legacyId);
                WET_STATES_BY_LEGACY_ID.put(legacyId, wetState);
            }
        }
    }

    public static synchronized int encodeUniversalStateId(int stateId, IBlockState state) {
        if (!FFDConfig.isWaterloggingEnabled()) {
            return stateId;
        }
        Integer encoded = WET_LEGACY_IDS.get(state);
        return encoded == null ? stateId : encoded;
    }

    public static synchronized IBlockState decodeUniversalStateId(
            IBlockState state, int stateId) {
        if (!FFDConfig.isWaterloggingEnabled()) {
            return state;
        }
        IBlockState decoded = WET_STATES_BY_LEGACY_ID.get(stateId);
        return decoded == null ? state : decoded;
    }

    public static Map<IProperty<?>, Comparable<?>> withoutUniversalWaterloggedProperty(
            Map<IProperty<?>, Comparable<?>> properties) {
        if (properties == null) {
            return properties;
        }
        boolean found = false;
        for (IProperty<?> property : properties.keySet()) {
            if (property == UNIVERSAL_WATERLOGGED) {
                found = true;
                break;
            }
        }
        if (!found) {
            return properties;
        }
        Map<IProperty<?>, Comparable<?>> filtered = new LinkedHashMap<>();
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
            if (entry.getKey() != UNIVERSAL_WATERLOGGED) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    private static boolean supportsUniversalWaterlogging(Block block) {
        if (block instanceof BlockStairs || block instanceof BlockFence
                || block instanceof BlockWall || block instanceof BlockPane
                || block instanceof BlockTrapDoor || block instanceof BlockLadder
                || block instanceof BlockSign || block instanceof BlockChest
                || block instanceof BlockLeaves
                || block instanceof net.minecraft.block.BlockRailBase
                || block instanceof net.minecraft.block.BlockEnderChest
                || block instanceof net.minecraft.block.BlockBarrier
                || block instanceof xy177.farmersfuturedelight.common.block.BlockLight
                || block instanceof xy177.farmersfuturedelight.common.block.BlockPointedDripstone) {
            return true;
        }
        return block instanceof BlockSlab && !((BlockSlab) block).isDouble();
    }

    private static boolean requiresStateCarrier(Block block) {
        List<IBlockState> dryStates = persistedDryStates(block);
        return freeMetadataValues(block, dryStates).size() < dryStates.size();
    }

    private static List<IBlockState> persistedDryStates(Block block) {
        List<IBlockState> states = new ArrayList<>();
        for (int meta = 0; meta < 16; meta++) {
            IBlockState state;
            try {
                state = block.getStateFromMeta(meta);
            } catch (RuntimeException ignored) {
                continue;
            }
            if (hasUniversalWaterloggedProperty(state)) {
                state = state.withProperty(UNIVERSAL_WATERLOGGED, false);
            }
            if (!states.contains(state)) {
                states.add(state);
            }
        }
        return states;
    }

    private static List<Integer> freeMetadataValues(Block block, List<IBlockState> dryStates) {
        boolean[] occupied = new boolean[16];
        for (IBlockState state : dryStates) {
            occupied[block.getMetaFromState(state) & 15] = true;
        }
        List<Integer> values = new ArrayList<>();
        for (int meta = 0; meta < occupied.length; meta++) {
            if (!occupied[meta]) {
                values.add(meta);
            }
        }
        return values;
    }

    public static boolean canBeWaterlogged(IBlockState state) {
        return WaterloggedPlantFluid.withWaterlogged(state, true) != null;
    }

    public static boolean isWaterlogged(IBlockAccess world, BlockPos pos) {
        return isWaterlogged(world.getBlockState(pos)) || FFDStoredFluidStates.has(world, pos);
    }

    public static boolean containsWater(IBlockState state) {
        return WaterloggedPlantFluid.containsWater(state);
    }

    public static boolean containsWater(IBlockAccess world, BlockPos pos) {
        return getContainedFluid(world, pos) == FluidRegistry.WATER;
    }

    public static boolean isWaterSource(IBlockState state) {
        return WaterloggedPlantFluid.isSourceWater(state);
    }

    public static boolean isWaterSource(IBlockAccess world, BlockPos pos) {
        return getSourceFluid(world, pos) == FluidRegistry.WATER;
    }

    public static IBlockState withWaterlogged(IBlockState state, boolean waterlogged) {
        return WaterloggedPlantFluid.withWaterlogged(state, waterlogged);
    }

    public static boolean setContainedFluid(World world, BlockPos pos, IBlockState state,
                                            Fluid fluid, int flags) {
        if (!FFDConfig.isFluidAllowed(world, fluid) || isWaterlogged(world, pos)) {
            return false;
        }
        IBlockState wetState = withWaterlogged(state, true);
        if (wetState == null && FFDConfig.isAdditionalWaterloggingBlock(world, state.getBlock())) {
            FFDStoredFluidStates.set(world, pos, sourceState(fluid));
            world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
            world.scheduleUpdate(pos, state.getBlock(), fluidTickRate(world, fluid));
            return true;
        }
        if (wetState == null) {
            return false;
        }
        if (!world.setBlockState(pos, wetState, flags)) {
            return false;
        }
        recordContainedFluid(world, pos, fluid);
        world.scheduleUpdate(pos, wetState.getBlock(), fluidTickRate(world, fluid));
        return true;
    }

    public static void recordContainedFluid(World world, BlockPos pos, Fluid fluid) {
        FFDStoredFluidStates.remove(world, pos);
        FFDFluidloggedData.set(world, pos, fluid);
    }

    public static Fluid getContainedFluid(IBlockAccess world, BlockPos pos) {
        IBlockState storedState = FFDStoredFluidStates.get(world, pos);
        if (storedState != null) {
            return getFluidForBlock(storedState.getBlock());
        }
        IBlockState state = world.getBlockState(pos);
        if (isWaterlogged(state)) {
            Fluid stored = FFDFluidloggedData.get(world, pos);
            return stored == null ? FluidRegistry.WATER : stored;
        }
        return getFluidForBlock(state.getBlock());
    }

    public static boolean containsFluid(IBlockAccess world, BlockPos pos, Fluid fluid) {
        return fluid != null && getContainedFluid(world, pos) == fluid;
    }

    public static boolean isFluidSource(IBlockAccess world, BlockPos pos) {
        return getSourceFluid(world, pos) != null;
    }

    public static Fluid getSourceFluid(IBlockAccess world, BlockPos pos) {
        IBlockState stored = FFDStoredFluidStates.get(world, pos);
        if (stored != null) {
            return WaterloggedPlantFluid.isSourceFluid(stored)
                    ? getFluidForBlock(stored.getBlock()) : null;
        }
        IBlockState state = world.getBlockState(pos);
        if (isWaterlogged(state)) {
            return getContainedFluid(world, pos);
        }
        Fluid fluid = getFluidForBlock(state.getBlock());
        return fluid != null && WaterloggedPlantFluid.isSourceFluid(state) ? fluid : null;
    }

    public static IBlockState getFluidState(IBlockState state) {
        return WaterloggedPlantFluid.getFluidState(state);
    }

    public static IBlockState getFluidState(IBlockAccess world, BlockPos pos) {
        IBlockState stored = FFDStoredFluidStates.get(world, pos);
        if (stored != null) {
            return stored;
        }
        IBlockState state = world.getBlockState(pos);
        if (isWaterlogged(state)) {
            return sourceState(getContainedFluid(world, pos));
        }
        if (getFluidForBlock(state.getBlock()) != null) return state;
        return getFluidState(state);
    }

    public static IBlockState getReplacementState(IBlockState state) {
        return isWaterlogged(state) ? Blocks.WATER.getDefaultState()
                : Blocks.AIR.getDefaultState();
    }

    public static IBlockState getReplacementState(IBlockAccess world, BlockPos pos,
                                                   IBlockState state) {
        return isWaterlogged(world, pos) ? getFluidState(world, pos)
                : Blocks.AIR.getDefaultState();
    }

    public static boolean restoreFluid(World world, BlockPos pos, IBlockState state, int flags) {
        IBlockState replacement = getReplacementState(world, pos, state);
        boolean changed = world.setBlockState(pos, replacement, flags);
        if (changed) {
            FFDFluidloggedData.set(world, pos, null);
        }
        return changed;
    }

    public static Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        IBlockState state = world.getBlockState(pos);
        if (!isWaterlogged(world, pos)) {
            return containsWater(state)
                    ? Blocks.WATER.modifyAcceleration(world, pos, entity, motion) : motion;
        }
        int currentDepth = getRenderedWaterDepth(getFluidState(world, pos));
        Vec3d flow = Vec3d.ZERO;
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            BlockPos target = pos.offset(direction);
            IBlockState targetState = world.getBlockState(target);
            int targetDepth = getRenderedWaterDepth(getFluidState(world, target));
            if (targetDepth >= 0) {
                flow = addFlow(flow, direction, targetDepth - currentDepth);
            } else if (!targetState.getMaterial().blocksMovement()) {
                int lowerDepth = getRenderedWaterDepth(getFluidState(world, target.down()));
                if (lowerDepth >= 0) {
                    flow = addFlow(flow, direction, lowerDepth - (currentDepth - 8));
                }
            }
        }
        return motion.add(flow.normalize());
    }

    private static int getRenderedWaterDepth(IBlockState state) {
        if (isWaterlogged(state)) {
            return 0;
        }
        if (state == null || state.getMaterial() != net.minecraft.block.material.Material.WATER
                || !state.getPropertyKeys().contains(BlockLiquid.LEVEL)) {
            return -1;
        }
        int depth = state.getValue(BlockLiquid.LEVEL);
        return depth >= 8 ? 0 : depth;
    }

    private static Vec3d addFlow(Vec3d flow, EnumFacing direction, int depthDifference) {
        return flow.addVector(direction.getFrontOffsetX() * depthDifference,
                direction.getFrontOffsetY() * depthDifference,
                direction.getFrontOffsetZ() * depthDifference);
    }

    public static IUnlistedProperty<?>[] extendedProperties() {
        return WaterloggedPlantFluid.extendedProperties();
    }

    public static IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedPlantFluid.getExtendedState(state, world, pos);
    }

    public static void onBlockAdded(World world, BlockPos pos, Block block) {
        WaterloggedPlantFluid.onBlockAdded(world, pos, block);
    }

    public static void onNeighborChanged(World world, BlockPos pos, Block block) {
        WaterloggedPlantFluid.onNeighborChanged(world, pos, block);
    }

    public static void updateTick(World world, BlockPos pos, IBlockState state) {
        WaterloggedPlantFluid.updateTick(world, pos, state);
    }

    public static IBlockState sourceState(Fluid fluid) {
        if (fluid == null || fluid.getBlock() == null) {
            return Blocks.AIR.getDefaultState();
        }
        try {
            return fluid.getBlock().getStateFromMeta(0);
        } catch (RuntimeException ignored) {
            return fluid.getBlock().getDefaultState();
        }
    }

    public static Fluid getFluidForBlock(Block block) {
        if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
            return FluidRegistry.WATER;
        }
        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
            return FluidRegistry.LAVA;
        }
        return FluidRegistry.lookupFluidForBlock(block);
    }

    public static int fluidTickRate(World world, Fluid fluid) {
        if (fluid == null || fluid.getBlock() == null) {
            return 5;
        }
        return Math.max(1, fluid.getBlock().tickRate(world));
    }

    private static final class UniversalWaterloggedProperty extends PropertyBool {
        private UniversalWaterloggedProperty() {
            super("waterlogged");
        }

        @Override
        public Collection<Boolean> getAllowedValues() {
            return Arrays.asList(Boolean.FALSE, Boolean.TRUE);
        }
    }
}
