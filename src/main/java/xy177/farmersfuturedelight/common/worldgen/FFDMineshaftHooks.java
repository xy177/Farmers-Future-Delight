package xy177.farmersfuturedelight.common.worldgen;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureMineshaftPieces;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.ChunkGeneratorExtended;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class FFDMineshaftHooks {
    private static final ResourceLocation FUTURE_MC_CHAIN = new ResourceLocation("futuremc", "chain");
    private static final String YUNG_START_CLASS =
            "com.yungnickyoung.minecraft.bettermineshafts.world.MapGenBetterMineshaft$Start";
    private static final String YUNG_BIG_TUNNEL_CLASS =
            "com.yungnickyoung.minecraft.bettermineshafts.world.generator.pieces.BigTunnel";
    private static final String YUNG_SMALL_TUNNEL_CLASS =
            "com.yungnickyoung.minecraft.bettermineshafts.world.generator.pieces.SmallTunnel";
    private static final int YUNG_MAX_DOWN = 48;
    private static final int YUNG_MAX_UP = 64;
    private static final Map<String, Field> YUNG_FIELDS = new ConcurrentHashMap<>();

    private FFDMineshaftHooks() {
    }

    public static void adjustModernMineshaftHeight(StructureMineshaftStart start, World world,
                                                   java.util.Random random,
                                                   net.minecraft.world.gen.structure.MapGenMineshaft.Type type,
                                                   ChunkGeneratorExtended extendedGenerator) {
        if (start == null || world == null || random == null || !FFDHeightHooks.isExtended(world)) {
            return;
        }
        StructureBoundingBox bounds = start.getBoundingBox();
        if (bounds == null) {
            return;
        }

        int offset;
        if (type == net.minecraft.world.gen.structure.MapGenMineshaft.Type.MESA) {
            int centerX = (bounds.minX + bounds.maxX) >> 1;
            int centerZ = (bounds.minZ + bounds.maxZ) >> 1;
            int surface = extendedGenerator == null
                    ? FFDStructureHooks.estimateModernSurfaceHeight(world, centerX, centerZ)
                    : extendedGenerator.estimateModernSurfaceHeight(centerX, centerZ);
            int seaLevel = world.getSeaLevel();
            int target = surface <= seaLevel
                    ? seaLevel : seaLevel + random.nextInt(surface - seaLevel + 1);
            int centerY = (bounds.minY + bounds.maxY) >> 1;
            offset = target - centerY;
        } else {
            int seaLevelMinusMargin = world.getSeaLevel() - 10;
            int minimumMaxY = FFDHeightHooks.minY(world) + bounds.getYSize() + 1;
            int targetMaxY = minimumMaxY;
            if (targetMaxY < seaLevelMinusMargin) {
                targetMaxY += random.nextInt(seaLevelMinusMargin - targetMaxY);
            }
            offset = targetMaxY - bounds.maxY;
        }

        bounds.offset(0, offset, 0);
        for (StructureComponent component : start.getComponents()) {
            component.offset(0, offset, 0);
        }
    }

    public static void addModernMineshaftSupports(StructureStart start, World world,
                                                   StructureBoundingBox chunkBounds) {
        if (!FFDHeightHooks.isExtended(world) || start == null || chunkBounds == null) {
            return;
        }
        if (start instanceof StructureMineshaftStart) {
            for (StructureComponent component : start.getComponents()) {
                if (component instanceof StructureMineshaftPieces.Corridor) {
                    addModernCorridorSupports(component, world, chunkBounds);
                }
            }
            return;
        }
        if (YUNG_START_CLASS.equals(start.getClass().getName())) {
            addYungMineshaftSupports(start, world, chunkBounds);
        }
    }

    private static void addYungMineshaftSupports(StructureStart start, World world,
                                                   StructureBoundingBox chunkBounds) {
        for (StructureComponent component : start.getComponents()) {
            String className = component.getClass().getName();
            if (YUNG_BIG_TUNNEL_CLASS.equals(className)) {
                for (Integer support : readYungSupports(component, "bigSupports")) {
                    addYungSupport(component, world, chunkBounds, 1, support + 1, 4);
                    addYungSupport(component, world, chunkBounds, 7, support + 1, 4);
                }
                for (Integer support : readYungSupports(component, "smallSupports")) {
                    addYungSupport(component, world, chunkBounds, 2, support, 3);
                    addYungSupport(component, world, chunkBounds, 6, support, 3);
                }
            } else if (YUNG_SMALL_TUNNEL_CLASS.equals(className)) {
                for (Integer support : readYungSupports(component, "supports")) {
                    addYungSupport(component, world, chunkBounds, 1, support, 3);
                    addYungSupport(component, world, chunkBounds, 3, support, 3);
                }
            }
        }
    }

    private static void addYungSupport(StructureComponent component, World world,
                                       StructureBoundingBox chunkBounds, int localX, int localZ,
                                       int roofLocalY) {
        BlockPos floorAnchor = toWorldPos(component, localX, 0, localZ);
        if (!isInsideChunkXZ(chunkBounds, floorAnchor)) {
            return;
        }
        IBlockState anchorState = world.getBlockState(floorAnchor);
        if (isStructureReplaceable(world, floorAnchor, anchorState)) {
            return;
        }
        IBlockState verticalState = world.getBlockState(toWorldPos(component, localX, 1, localZ));
        SupportMaterial material = SupportMaterial.forStructure(anchorState, verticalState);
        BlockPos roofAnchor = toWorldPos(component, localX, roofLocalY, localZ);
        fillYungPillarDownOrChainUp(world, floorAnchor, roofAnchor, material,
                getChainState());
    }

    private static void fillYungPillarDownOrChainUp(World world, BlockPos floorAnchor,
                                                     BlockPos roofAnchor,
                                                     SupportMaterial material,
                                                     IBlockState chainState) {
        boolean checkBelow = true;
        boolean checkAbove = chainState != null;
        for (int distance = 1; checkBelow || checkAbove; distance++) {
            if (checkBelow) {
                BlockPos belowPos = floorAnchor.down(distance);
                IBlockState belowState = world.getBlockState(belowPos);
                boolean emptyBelow = isStructureReplaceable(world, belowPos, belowState)
                        && belowState.getBlock() != Blocks.LAVA
                        && belowState.getBlock() != Blocks.FLOWING_LAVA;
                if (!emptyBelow && belowState.isSideSolid(world, belowPos, EnumFacing.UP)) {
                    fillColumn(world, floorAnchor.getX(), floorAnchor.getZ(),
                            floorAnchor.getY() - distance + 1, floorAnchor.getY(),
                            material.logState);
                    return;
                }
                checkBelow = distance <= YUNG_MAX_DOWN && emptyBelow
                        && belowPos.getY() > FFDHeightHooks.minY(world);
            }

            if (checkAbove) {
                BlockPos abovePos = roofAnchor.up(distance);
                IBlockState aboveState = world.getBlockState(abovePos);
                boolean emptyAbove = isStructureReplaceable(world, abovePos, aboveState);
                if (!emptyAbove && !(aboveState.getBlock() instanceof BlockFalling)
                        && aboveState.isSideSolid(world, abovePos, EnumFacing.DOWN)) {
                    fillColumn(world, roofAnchor.getX(), roofAnchor.getZ(),
                            roofAnchor.getY() + 1, roofAnchor.getY() + distance, chainState);
                    return;
                }
                checkAbove = distance <= YUNG_MAX_UP && emptyAbove
                        && abovePos.getY() < FFDHeightHooks.maxYExclusive(world);
            }
        }
    }

    private static List<Integer> readYungSupports(StructureComponent component, String fieldName) {
        try {
            Field field = YUNG_FIELDS.get(fieldKey(component.getClass(), fieldName));
            if (field == null) {
                field = component.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Field previous = YUNG_FIELDS.putIfAbsent(
                        fieldKey(component.getClass(), fieldName), field);
                if (previous != null) {
                    field = previous;
                }
            }
            Object value = field.get(component);
            if (value instanceof List<?>) {
                java.util.ArrayList<Integer> result = new java.util.ArrayList<>();
                for (Object entry : (List<?>) value) {
                    if (entry instanceof Number) {
                        result.add(((Number) entry).intValue());
                    }
                }
                return result;
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
        }
        return java.util.Collections.emptyList();
    }

    private static String fieldKey(Class<?> type, String fieldName) {
        return type.getName() + '#' + fieldName;
    }

    public static void addModernCorridorSupports(StructureComponent corridor, World world,
                                                  StructureBoundingBox chunkBounds) {
        if (!FFDHeightHooks.isExtended(world) || corridor == null || chunkBounds == null
                || corridor.getBoundingBox() == null || corridor.getCoordBaseMode() == null) {
            return;
        }

        StructureBoundingBox corridorBounds = corridor.getBoundingBox();
        int length = (corridor.getCoordBaseMode().getAxis() == EnumFacing.Axis.Z
                ? corridorBounds.getZSize() : corridorBounds.getXSize()) - 1;
        addDoubleSupport(corridor, world, chunkBounds, 2);
        if (length > 4) {
            addDoubleSupport(corridor, world, chunkBounds, length - 2);
        }
    }

    private static void addDoubleSupport(StructureComponent corridor, World world,
                                         StructureBoundingBox chunkBounds, int localZ) {
        addSupport(corridor, world, chunkBounds, 0, localZ);
        addSupport(corridor, world, chunkBounds, 2, localZ);
    }

    private static void addSupport(StructureComponent corridor, World world,
                                   StructureBoundingBox chunkBounds, int localX, int localZ) {
        BlockPos anchor = toWorldPos(corridor, localX, -1, localZ);
        if (!isInsideChunkXZ(chunkBounds, anchor)) {
            return;
        }

        IBlockState anchorState = world.getBlockState(anchor);
        SupportMaterial material = SupportMaterial.forPlanks(anchorState);
        if (material == null) {
            return;
        }
        fillPillarDownOrChainUp(world, anchor, material, getChainState(), 20, 50);
    }

    private static void fillPillarDownOrChainUp(World world, BlockPos anchor,
                                                 SupportMaterial material,
                                                 IBlockState chainState,
                                                 int maxDown, int maxUp) {
        int anchorY = anchor.getY();
        int distance = 1;
        boolean checkBelow = true;
        boolean checkAbove = chainState != null;

        while (checkBelow || checkAbove) {
            if (checkBelow) {
                BlockPos belowPos = new BlockPos(anchor.getX(), anchorY - distance, anchor.getZ());
                IBlockState belowState = world.getBlockState(belowPos);
                boolean emptyBelow = isStructureReplaceable(world, belowPos, belowState)
                        && belowState.getBlock() != Blocks.LAVA
                        && belowState.getBlock() != Blocks.FLOWING_LAVA;
                if (!emptyBelow && belowState.isSideSolid(world, belowPos, EnumFacing.UP)) {
                    fillColumn(world, anchor.getX(), anchor.getZ(), anchorY - distance + 1,
                            anchorY, material.logState);
                    return;
                }
                checkBelow = distance <= maxDown && emptyBelow
                        && belowPos.getY() > FFDHeightHooks.minY(world);
            }

            if (checkAbove) {
                BlockPos abovePos = new BlockPos(anchor.getX(), anchorY + distance, anchor.getZ());
                IBlockState aboveState = world.getBlockState(abovePos);
                boolean emptyAbove = isStructureReplaceable(world, abovePos, aboveState);
                if (!emptyAbove && !(aboveState.getBlock() instanceof BlockFalling)
                        && aboveState.isSideSolid(world, abovePos, EnumFacing.DOWN)) {
                    world.setBlockState(anchor.up(), material.fenceState, 2);
                    fillColumn(world, anchor.getX(), anchor.getZ(), anchorY + 2,
                            anchorY + distance, chainState);
                    return;
                }
                checkAbove = distance <= maxUp && emptyAbove
                        && abovePos.getY() < FFDHeightHooks.maxYExclusive(world);
            }
            distance++;
        }
    }

    private static void fillColumn(World world, int x, int z, int bottomInclusive,
                                   int topExclusive, IBlockState state) {
        for (int y = bottomInclusive; y < topExclusive; y++) {
            world.setBlockState(new BlockPos(x, y, z), state, 2);
        }
    }

    private static boolean isInsideChunkXZ(StructureBoundingBox chunkBounds, BlockPos pos) {
        return pos.getX() >= chunkBounds.minX && pos.getX() <= chunkBounds.maxX
                && pos.getZ() >= chunkBounds.minZ && pos.getZ() <= chunkBounds.maxZ;
    }

    private static boolean isStructureReplaceable(World world, BlockPos pos, IBlockState state) {
        Material material = state.getMaterial();
        return material == Material.AIR || material.isLiquid()
                || state.getBlock().isReplaceable(world, pos);
    }

    private static IBlockState getChainState() {
        if (FFDItems.isIronChainEnabled()) {
            return FFDBlocks.IRON_CHAIN.getDefaultState();
        }
        Block externalChain = ForgeRegistries.BLOCKS.getValue(FUTURE_MC_CHAIN);
        return externalChain == null || externalChain == Blocks.AIR
                ? null : externalChain.getDefaultState();
    }

    private static BlockPos toWorldPos(StructureComponent component, int x, int y, int z) {
        StructureBoundingBox bounds = component.getBoundingBox();
        EnumFacing facing = component.getCoordBaseMode();
        int worldX;
        int worldZ;
        switch (facing) {
            case NORTH:
                worldX = bounds.minX + x;
                worldZ = bounds.maxZ - z;
                break;
            case SOUTH:
                worldX = bounds.minX + x;
                worldZ = bounds.minZ + z;
                break;
            case WEST:
                worldX = bounds.maxX - z;
                worldZ = bounds.minZ + x;
                break;
            case EAST:
                worldX = bounds.minX + z;
                worldZ = bounds.minZ + x;
                break;
            default:
                worldX = x;
                worldZ = z;
                break;
        }
        return new BlockPos(worldX, bounds.minY + y, worldZ);
    }

    private static final class SupportMaterial {
        private final IBlockState logState;
        private final IBlockState fenceState;

        private SupportMaterial(IBlockState logState, IBlockState fenceState) {
            this.logState = logState;
            this.fenceState = fenceState;
        }

        private static SupportMaterial forPlanks(IBlockState state) {
            return state.getBlock() == Blocks.PLANKS
                    ? forStructure(state, Blocks.AIR.getDefaultState()) : null;
        }

        private static SupportMaterial forStructure(IBlockState anchorState,
                                                     IBlockState verticalState) {
            if (anchorState.getBlock() == Blocks.PLANKS) {
                BlockPlanks.EnumType type = anchorState.getValue(BlockPlanks.VARIANT);
                Block log = type.ordinal() >= BlockPlanks.EnumType.ACACIA.ordinal()
                        ? Blocks.LOG2 : Blocks.LOG;
                Block fence;
                switch (type) {
                    case SPRUCE: fence = Blocks.SPRUCE_FENCE; break;
                    case BIRCH: fence = Blocks.BIRCH_FENCE; break;
                    case JUNGLE: fence = Blocks.JUNGLE_FENCE; break;
                    case ACACIA: fence = Blocks.ACACIA_FENCE; break;
                    case DARK_OAK: fence = Blocks.DARK_OAK_FENCE; break;
                    default: fence = Blocks.OAK_FENCE;
                }
                if (log == Blocks.LOG2) {
                    return new SupportMaterial(
                            log.getDefaultState()
                                    .withProperty(BlockNewLog.VARIANT, type)
                                    .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y),
                            fence.getDefaultState());
                }
                return new SupportMaterial(
                        log.getDefaultState()
                                .withProperty(BlockOldLog.VARIANT, type)
                                .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y),
                        fence.getDefaultState());
            }
            IBlockState supportState = verticalState.getMaterial() == Material.AIR
                    || verticalState.getMaterial().isLiquid() ? anchorState : verticalState;
            return new SupportMaterial(anchorState, supportState);
        }
    }
}
