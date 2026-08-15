package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.BlockNetherVine;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class NetherForestFeatures {
    private static final int MIN_NYLIUM_Y = 32;
    private static final int POSITION_NOT_FOUND = Integer.MAX_VALUE;
    private static final int WORLDGEN_FLAGS = 2 | 16;
    private static final int NORMAL_PLACEMENT_FLAGS = 2;

    private NetherForestFeatures() {
    }

    public static void generateForest(World world, Random random, int chunkX, int chunkZ,
                                      boolean warped, float[] columnStrengths) {
        if (warped ? !FFDItems.isWarpedEnabled() : !FFDItems.isCrimsonEnabled()) {
            return;
        }
        if (blocks().nylium(warped) == null) {
            return;
        }

        convertExposedNetherrack(world, random, chunkX, chunkZ, warped, columnStrengths);
        if (warped) {
            generateOnEveryLayer(world, random, chunkX, chunkZ, FFDConfig.warpedFungiAttempts,
                    columnStrengths,
                    pos -> placeHugeFungus(world, random, pos, true));
            generateOnEveryLayer(world, random, chunkX, chunkZ,
                    FFDConfig.warpedForestVegetationAttempts, columnStrengths,
                    pos -> placeVegetationFeature(world, random, pos, true,
                            FFDConfig.netherVegetationSpreadWidth,
                            FFDConfig.netherVegetationSpreadHeight, WORLDGEN_FLAGS));
            generateOnEveryLayer(world, random, chunkX, chunkZ, FFDConfig.netherSproutsAttempts,
                    columnStrengths,
                    pos -> placeNetherSproutsFeature(world, random, pos,
                            FFDConfig.netherVegetationSpreadWidth,
                            FFDConfig.netherVegetationSpreadHeight, WORLDGEN_FLAGS));
            generateCounted(world, random, chunkX, chunkZ, FFDConfig.twistingVinesAttempts,
                    columnStrengths,
                    pos -> placeTwistingVinesFeature(world, random, pos,
                            FFDConfig.twistingVinesSpreadWidth,
                            FFDConfig.twistingVinesSpreadHeight,
                            FFDConfig.twistingVinesMaxHeight, WORLDGEN_FLAGS));
            return;
        }

        if (FFDItems.isCrimsonWoodEnabled()) {
            generateCounted(world, random, chunkX, chunkZ, FFDConfig.weepingVinesAttempts,
                    columnStrengths,
                    pos -> placeWeepingVinesFeature(world, random, pos, WORLDGEN_FLAGS));
        }
        generateOnEveryLayer(world, random, chunkX, chunkZ, FFDConfig.crimsonFungiAttempts,
                columnStrengths,
                pos -> placeHugeFungus(world, random, pos, false));
        generateOnEveryLayer(world, random, chunkX, chunkZ,
                FFDConfig.crimsonForestVegetationAttempts, columnStrengths,
                pos -> placeVegetationFeature(world, random, pos, false,
                        FFDConfig.netherVegetationSpreadWidth,
                        FFDConfig.netherVegetationSpreadHeight, WORLDGEN_FLAGS));
    }

    public static void growNyliumVegetation(World world, Random random, BlockPos nyliumPos,
                                            boolean warped) {
        BlockPos origin = nyliumPos.up();
        placeVegetationFeature(world, random, origin, warped, 3, 1, NORMAL_PLACEMENT_FLAGS);
        if (!warped) {
            return;
        }
        placeNetherSproutsFeature(world, random, origin, 3, 1, NORMAL_PLACEMENT_FLAGS);
        if (random.nextInt(8) == 0) {
            placeTwistingVinesFeature(world, random, origin, 3, 1, 2, NORMAL_PLACEMENT_FLAGS);
        }
    }

    private static void convertExposedNetherrack(World world, Random random, int chunkX, int chunkZ,
                                                  boolean warped, float[] columnStrengths) {
        IBlockState nylium = blocks().nylium(warped);
        int topY = getNetherTopY(world) - 2;
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                if (!acceptColumn(random, columnStrengths, localX, localZ)) {
                    continue;
                }
                int x = chunkX * 16 + localX;
                int z = chunkZ * 16 + localZ;
                for (int y = topY; y >= MIN_NYLIUM_Y; y--) {
                    BlockPos ground = new BlockPos(x, y, z);
                    if (world.getBlockState(ground).getBlock() == Blocks.NETHERRACK
                            && world.isAirBlock(ground.up())) {
                        world.setBlockState(ground, nylium, WORLDGEN_FLAGS);
                    }
                }
            }
        }
    }

    private static void generateOnEveryLayer(World world, Random random, int chunkX, int chunkZ,
                                             int count, float[] columnStrengths,
                                             PositionAction action) {
        int layer = 0;
        boolean foundAny;
        do {
            foundAny = false;
            for (int attempt = 0; attempt < count; attempt++) {
                int localX = random.nextInt(16);
                int localZ = random.nextInt(16);
                if (!acceptColumn(random, columnStrengths, localX, localZ)) {
                    continue;
                }
                int x = chunkX * 16 + localX;
                int z = chunkZ * 16 + localZ;
                int y = findOnGroundYPosition(world, x, getNetherTopY(world) - 1, z, layer);
                if (y == POSITION_NOT_FOUND) {
                    continue;
                }
                action.place(new BlockPos(x, y, z));
                foundAny = true;
            }
            layer++;
        } while (foundAny);
    }

    private static int findOnGroundYPosition(World world, int x, int startY, int z,
                                             int requestedLayer) {
        BlockPos currentPos = new BlockPos(x, startY, z);
        IBlockState currentState = world.getBlockState(currentPos);
        int currentLayer = 0;
        for (int y = startY; y >= 1; y--) {
            BlockPos belowPos = new BlockPos(x, y - 1, z);
            IBlockState belowState = world.getBlockState(belowPos);
            if (!isPlacementEmpty(belowState) && isPlacementEmpty(currentState)
                    && belowState.getBlock() != Blocks.BEDROCK) {
                if (currentLayer == requestedLayer) {
                    return y;
                }
                currentLayer++;
            }
            currentState = belowState;
        }
        return POSITION_NOT_FOUND;
    }

    private static void generateCounted(World world, Random random, int chunkX, int chunkZ,
                                        int count, float[] columnStrengths,
                                        PositionAction action) {
        int topY = getNetherTopY(world);
        for (int attempt = 0; attempt < count; attempt++) {
            int localX = random.nextInt(16);
            int localZ = random.nextInt(16);
            if (acceptColumn(random, columnStrengths, localX, localZ)) {
                action.place(new BlockPos(chunkX * 16 + localX,
                        random.nextInt(topY), chunkZ * 16 + localZ));
            }
        }
    }

    private static boolean acceptColumn(Random random, float[] columnStrengths,
                                        int localX, int localZ) {
        float strength = columnStrengths[(localZ << 4) | localX];
        return strength >= 1.0F || strength > 0.0F && random.nextFloat() < strength;
    }

    private static void placeVegetationFeature(World world, Random random, BlockPos origin,
                                               boolean warped, int spreadWidth,
                                               int spreadHeight, int updateFlags) {
        if (!isLoadedArea(world, origin, 1) || !isAnyNylium(world.getBlockState(origin.down()))
                || !isInsideWorld(world, origin)) {
            return;
        }
        for (int attempt = 0; attempt < spreadWidth * spreadWidth; attempt++) {
            BlockPos candidate = origin.add(random.nextInt(spreadWidth) - random.nextInt(spreadWidth),
                    random.nextInt(spreadHeight) - random.nextInt(spreadHeight),
                    random.nextInt(spreadWidth) - random.nextInt(spreadWidth));
            IBlockState state = selectVegetationState(random, warped);
            if (state != null && isInsideWorld(world, candidate)
                    && isLoadedArea(world, candidate, 1) && world.isAirBlock(candidate)
                    && state.getBlock().canPlaceBlockAt(world, candidate)) {
                world.setBlockState(candidate, state, updateFlags);
            }
        }
    }

    private static IBlockState selectVegetationState(Random random, boolean warped) {
        if (!warped) {
            int roll = random.nextInt(99);
            if (roll < 87) {
                return blocks().roots(false);
            }
            if (roll < 98) {
                return blocks().fungus(false);
            }
            return blocks().fungus(true);
        }

        int roll = random.nextInt(100);
        if (roll < 85) {
            return blocks().roots(true);
        }
        if (roll == 85) {
            return blocks().roots(false);
        }
        if (roll < 99) {
            return blocks().fungus(true);
        }
        return blocks().fungus(false);
    }

    private static void placeNetherSproutsFeature(World world, Random random, BlockPos origin,
                                                  int spreadWidth, int spreadHeight, int updateFlags) {
        IBlockState sprouts = blocks().sprouts();
        if (sprouts == null || !isLoadedArea(world, origin, 1)
                || !isAnyNylium(world.getBlockState(origin.down()))
                || !isInsideWorld(world, origin)) {
            return;
        }
        for (int attempt = 0; attempt < spreadWidth * spreadWidth; attempt++) {
            BlockPos candidate = origin.add(random.nextInt(spreadWidth) - random.nextInt(spreadWidth),
                    random.nextInt(spreadHeight) - random.nextInt(spreadHeight),
                    random.nextInt(spreadWidth) - random.nextInt(spreadWidth));
            if (isInsideWorld(world, candidate) && isLoadedArea(world, candidate, 1)
                    && world.isAirBlock(candidate)
                    && sprouts.getBlock().canPlaceBlockAt(world, candidate)) {
                world.setBlockState(candidate, sprouts, updateFlags);
            }
        }
    }

    private static void placeHugeFungus(World world, Random random, BlockPos origin,
                                        boolean warped) {
        boolean woodEnabled = warped ? FFDItems.isWarpedWoodEnabled()
                : FFDItems.isCrimsonWoodEnabled();
        if (woodEnabled && isLoadedArea(world, origin, WorldGenHugeFungus.MAX_HORIZONTAL_RADIUS + 1)
                && world.isAirBlock(origin)
                && random.nextInt(FFDConfig.netherHugeFungusChanceRoll) == 0
                && WorldGenHugeFungus.canGenerateAt(world, origin, warped)) {
            new WorldGenHugeFungus(warped).generate(world, random, origin);
        }
    }

    private static void placeWeepingVinesFeature(World world, Random random, BlockPos origin,
                                                 int updateFlags) {
        if (!isInsideWorld(world, origin) || !isLoadedArea(world, origin, 1)
                || !world.isAirBlock(origin)
                || !isWeepingVinesSupport(world.getBlockState(origin.up()))) {
            return;
        }
        IBlockState wart = blocks().wart(false);
        if (wart == null || blocks().vine(false, true) == null
                || blocks().vine(false, false) == null) {
            return;
        }
        world.setBlockState(origin, wart, updateFlags);
        placeRoofNetherWart(world, random, origin, updateFlags);
        placeRoofWeepingVines(world, random, origin, updateFlags);
    }

    private static void placeRoofNetherWart(World world, Random random, BlockPos origin,
                                            int updateFlags) {
        for (int attempt = 0; attempt < FFDConfig.weepingVinesWartPatchAttempts; attempt++) {
            BlockPos candidate = origin.add(random.nextInt(6) - random.nextInt(6),
                    random.nextInt(2) - random.nextInt(5),
                    random.nextInt(6) - random.nextInt(6));
            if (!isInsideWorld(world, candidate) || !isLoadedArea(world, candidate, 1)
                    || !world.isAirBlock(candidate)) {
                continue;
            }
            int neighbours = 0;
            for (EnumFacing facing : EnumFacing.values()) {
                if (isWeepingVinesSupport(world.getBlockState(candidate.offset(facing)))) {
                    neighbours++;
                }
                if (neighbours > 1) {
                    break;
                }
            }
            if (neighbours == 1) {
                world.setBlockState(candidate, blocks().wart(false), updateFlags);
            }
        }
    }

    private static void placeRoofWeepingVines(World world, Random random, BlockPos origin,
                                              int updateFlags) {
        for (int attempt = 0; attempt < FFDConfig.weepingVinesColumnAttempts; attempt++) {
            BlockPos candidate = origin.add(random.nextInt(8) - random.nextInt(8),
                    random.nextInt(2) - random.nextInt(7),
                    random.nextInt(8) - random.nextInt(8));
            if (!isInsideWorld(world, candidate) || !isLoadedArea(world, candidate, 1)
                    || !world.isAirBlock(candidate)
                    || !isWeepingVinesSupport(world.getBlockState(candidate.up()))) {
                continue;
            }
            placeWeepingVinesColumn(world, random, candidate, randomVineHeight(random, 8),
                    17, 25, updateFlags);
        }
    }

    public static void placeWeepingVinesColumn(World world, Random random, BlockPos start,
                                               int totalHeight, int minAge, int maxAge) {
        placeWeepingVinesColumn(world, random, start, totalHeight, minAge, maxAge,
                NORMAL_PLACEMENT_FLAGS);
    }

    public static void placeWeepingVinesColumn(World world, Random random, BlockPos start,
                                               int totalHeight, int minAge, int maxAge,
                                               int updateFlags) {
        BlockPos cursor = start;
        for (int height = 0; height <= totalHeight; height++) {
            if (FFDHeightHooks.isOutsideBuildHeight(world, cursor)
                    || (updateFlags == WORLDGEN_FLAGS && !isInsideWorld(world, cursor))
                    || !isLoadedArea(world, cursor, 1)) {
                break;
            }
            if (world.isAirBlock(cursor)) {
                if (height == totalHeight || !world.isAirBlock(cursor.down())) {
                    IBlockState tip = blocks().vine(false, true);
                    if (tip == null) {
                        break;
                    }
                    world.setBlockState(cursor, tip, updateFlags);
                    if (tip.getBlock() instanceof BlockNetherVine) {
                        BlockNetherVine.setAge(world, cursor,
                                randomBetweenInclusive(random, minAge, maxAge));
                    }
                    break;
                }
                IBlockState plant = blocks().vine(false, false);
                if (plant == null) {
                    break;
                }
                world.setBlockState(cursor, plant, updateFlags);
            }
            cursor = cursor.down();
        }
    }

    private static void placeTwistingVinesFeature(World world, Random random, BlockPos origin,
                                                   int spreadWidth, int spreadHeight,
                                                   int maxHeight, int updateFlags) {
        if (!isValidTwistingVinesStart(world, origin)) {
            return;
        }
        for (int attempt = 0; attempt < spreadWidth * spreadWidth; attempt++) {
            BlockPos candidate = origin.add(randomBetweenInclusive(random, -spreadWidth, spreadWidth),
                    randomBetweenInclusive(random, -spreadHeight, spreadHeight),
                    randomBetweenInclusive(random, -spreadWidth, spreadWidth));
            BlockPos groundAir = findFirstAirBlockAboveGround(world, candidate);
            if (groundAir == null || !isValidTwistingVinesStart(world, groundAir)) {
                continue;
            }
            placeTwistingVinesColumn(world, random, groundAir,
                    randomVineHeight(random, maxHeight), 17, 25, updateFlags);
        }
    }

    private static BlockPos findFirstAirBlockAboveGround(World world, BlockPos start) {
        if (!isLoadedArea(world, start, 1)) {
            return null;
        }
        BlockPos cursor = start;
        do {
            cursor = cursor.down();
            if (cursor.getY() < FFDHeightHooks.minY(world)) {
                return null;
            }
        } while (world.isAirBlock(cursor));
        return cursor.up();
    }

    private static void placeTwistingVinesColumn(World world, Random random, BlockPos start,
                                                 int totalHeight, int minAge, int maxAge,
                                                 int updateFlags) {
        BlockPos cursor = start;
        for (int height = 1; height <= totalHeight; height++) {
            if (FFDHeightHooks.isOutsideBuildHeight(world, cursor)
                    || (updateFlags == WORLDGEN_FLAGS && !isInsideWorld(world, cursor))
                    || !isLoadedArea(world, cursor, 1)) {
                break;
            }
            if (world.isAirBlock(cursor)) {
                if (height == totalHeight || !world.isAirBlock(cursor.up())) {
                    IBlockState tip = blocks().vine(true, true);
                    if (tip == null) {
                        break;
                    }
                    world.setBlockState(cursor, tip, updateFlags);
                    if (tip.getBlock() instanceof BlockNetherVine) {
                        BlockNetherVine.setAge(world, cursor,
                                randomBetweenInclusive(random, minAge, maxAge));
                    }
                    break;
                }
                IBlockState plant = blocks().vine(true, false);
                if (plant == null) {
                    break;
                }
                world.setBlockState(cursor, plant, updateFlags);
            }
            cursor = cursor.up();
        }
    }

    private static int randomVineHeight(Random random, int maxHeight) {
        int height = randomBetweenInclusive(random, 1, maxHeight);
        if (random.nextInt(6) == 0) {
            height *= 2;
        }
        if (random.nextInt(5) == 0) {
            height = 1;
        }
        return height;
    }

    private static int randomBetweenInclusive(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static boolean isValidTwistingVinesStart(World world, BlockPos pos) {
        if (!isInsideWorld(world, pos) || !isLoadedArea(world, pos, 1) || !world.isAirBlock(pos)) {
            return false;
        }
        IBlockState below = world.getBlockState(pos.down());
        return below.getBlock() == Blocks.NETHERRACK || blocks().isNylium(below, true)
                || blocks().isWart(below, true);
    }

    private static boolean isWeepingVinesSupport(IBlockState state) {
        return state.getBlock() == Blocks.NETHERRACK || blocks().isWart(state, false);
    }

    private static boolean isAnyNylium(IBlockState state) {
        return blocks().isNylium(state);
    }

    private static boolean isPlacementEmpty(IBlockState state) {
        Material material = state.getMaterial();
        return material == Material.AIR || material == Material.WATER || material == Material.LAVA;
    }

    private static boolean isInsideWorld(World world, BlockPos pos) {
        if (world.provider.getDimension() == -1) {
            return pos.getY() > 0 && pos.getY() < getNetherTopY(world);
        }
        return !FFDHeightHooks.isOutsideBuildHeight(world, pos);
    }

    private static boolean isLoadedArea(World world, BlockPos pos, int radius) {
        return world.isAreaLoaded(pos, radius);
    }

    static int getNetherTopY(World world) {
        return Math.min(128, world.getActualHeight());
    }

    private static FFDNetherBlockProvider blocks() {
        return FFDNetherBlockProvider.get();
    }

    private interface PositionAction {
        void place(BlockPos pos);
    }
}
