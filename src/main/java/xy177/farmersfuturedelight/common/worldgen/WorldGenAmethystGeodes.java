package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.BlockAmethystCluster;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.noise.FFDLegacyNormalNoise;

public final class WorldGenAmethystGeodes {
    private static final int SCAN_RADIUS = 16;
    private static final int INVALID_BLOCK_THRESHOLD = 1;
    private static final double FILLING = 1.7D;
    private static final double INNER_LAYER = 2.2D;
    private static final double MIDDLE_LAYER = 3.2D;
    private static final double OUTER_LAYER = 4.2D;
    private static final double BASE_CRACK_SIZE = 2.0D;
    private static final int CRACK_POINT_OFFSET = 2;
    private static final double NOISE_MULTIPLIER = 0.05D;
    private static final long FEATURE_SALT = 0x4F9939F508L;
    private static final EnumFacing[] DIRECTIONS = EnumFacing.values();

    private final long worldSeed;
    private final long xSeed;
    private final long zSeed;
    private final FFDLegacyNormalNoise shapeNoise;
    private final IBlockState amethystBlock;
    private final IBlockState buddingAmethyst;
    private final IBlockState calcite;
    private final IBlockState smoothBasalt;
    private final IBlockState[] crystals;

    public WorldGenAmethystGeodes(long worldSeed) {
        this.worldSeed = worldSeed;
        Random seedRandom = new Random(worldSeed);
        xSeed = odd(seedRandom.nextLong());
        zSeed = odd(seedRandom.nextLong());
        shapeNoise = new FFDLegacyNormalNoise(worldSeed);
        amethystBlock = resolve(FFDBlocks.AMETHYST_BLOCK, "amethyst_block");
        buddingAmethyst = resolve(FFDBlocks.BUDDING_AMETHYST, "budding_amethyst");
        calcite = resolve(FFDBlocks.CALCITE, "calcite");
        smoothBasalt = resolve(FFDBlocks.SMOOTH_BASALT, "smooth_basalt");
        crystals = new IBlockState[] {
                resolve(FFDBlocks.SMALL_AMETHYST_BUD, "small_amethyst_bud"),
                resolve(FFDBlocks.MEDIUM_AMETHYST_BUD, "medium_amethyst_bud"),
                resolve(FFDBlocks.LARGE_AMETHYST_BUD, "large_amethyst_bud"),
                resolve(FFDBlocks.AMETHYST_CLUSTER, "amethyst_cluster")
        };
    }

    public void generateChunk(int targetChunkX, int targetChunkZ, Access access) {
        if (!hasRequiredBlocks()) {
            return;
        }
        for (int sourceChunkX = targetChunkX - 1; sourceChunkX <= targetChunkX + 1; sourceChunkX++) {
            for (int sourceChunkZ = targetChunkZ - 1; sourceChunkZ <= targetChunkZ + 1; sourceChunkZ++) {
                Random random = randomForChunk(sourceChunkX, sourceChunkZ);
                if (random.nextInt(FFDConfig.amethystGeodeRarity) != 0) {
                    continue;
                }
                int originX = (sourceChunkX << 4) + random.nextInt(16);
                int originZ = (sourceChunkZ << 4) + random.nextInt(16);
                int originY = between(random, FFDConfig.amethystGeodeMinY,
                        FFDConfig.amethystGeodeMaxY);
                BlockPos origin = new BlockPos(originX, originY, originZ);
                place(origin, random, access);
            }
        }
    }

    private void place(BlockPos origin, Random random, Access access) {
        int pointCount = between(random, FFDConfig.amethystGeodeDistributionMin,
                FFDConfig.amethystGeodeDistributionMax);
        double layerAdjustment = pointCount / (double) FFDConfig.amethystGeodeOuterWallMax;
        double innerAir = inverseSqrt(FILLING);
        double amethystLayer = inverseSqrt(INNER_LAYER + layerAdjustment);
        double calciteLayer = inverseSqrt(MIDDLE_LAYER + layerAdjustment);
        double basaltLayer = inverseSqrt(OUTER_LAYER + layerAdjustment);
        double crackSize = inverseSqrt(BASE_CRACK_SIZE + random.nextDouble() / 2.0D
                + (pointCount > 3 ? layerAdjustment : 0.0D));
        boolean generateCrack = random.nextFloat() < FFDConfig.amethystGeodeCrackChance;

        List<Point> points = new ArrayList<>(pointCount);
        int invalidPoints = 0;
        for (int i = 0; i < pointCount; i++) {
            BlockPos pointPos = origin.add(
                    between(random, FFDConfig.amethystGeodeOuterWallMin,
                            FFDConfig.amethystGeodeOuterWallMax),
                    between(random, FFDConfig.amethystGeodeOuterWallMin,
                            FFDConfig.amethystGeodeOuterWallMax),
                    between(random, FFDConfig.amethystGeodeOuterWallMin,
                            FFDConfig.amethystGeodeOuterWallMax));
            if (isInvalid(access.getState(pointPos)) && ++invalidPoints > INVALID_BLOCK_THRESHOLD) {
                return;
            }
            points.add(new Point(pointPos, between(random, FFDConfig.amethystGeodePointOffsetMin,
                    FFDConfig.amethystGeodePointOffsetMax)));
        }

        List<BlockPos> crackPoints = new ArrayList<>(3);
        if (generateCrack) {
            int crackOffset = pointCount * 2 + 1;
            switch (random.nextInt(4)) {
                case 0:
                    addCrackPoints(crackPoints, origin, crackOffset, 0);
                    break;
                case 1:
                    addCrackPoints(crackPoints, origin, 0, crackOffset);
                    break;
                case 2:
                    addCrackPoints(crackPoints, origin, crackOffset, crackOffset);
                    break;
                default:
                    addCrackPoints(crackPoints, origin, 0, 0);
                    break;
            }
        }

        Map<BlockPos, IBlockState> staged = new LinkedHashMap<>();
        Set<BlockPos> openedCracks = new HashSet<>();
        List<BlockPos> potentialCrystals = new ArrayList<>();
        for (int x = origin.getX() - SCAN_RADIUS; x <= origin.getX() + SCAN_RADIUS; x++) {
            for (int y = origin.getY() - SCAN_RADIUS; y <= origin.getY() + SCAN_RADIUS; y++) {
                for (int z = origin.getZ() - SCAN_RADIUS; z <= origin.getZ() + SCAN_RADIUS; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    double noise = shapeNoise.sample(x, y, z) * NOISE_MULTIPLIER;
                    double shellPotential = potential(pos, points, noise);
                    if (shellPotential < basaltLayer) {
                        continue;
                    }
                    if (shellPotential >= innerAir) {
                        stage(access, staged, pos, Blocks.AIR.getDefaultState());
                        continue;
                    }
                    if (generateCrack && potential(pos, crackPoints, CRACK_POINT_OFFSET, noise) >= crackSize) {
                        stage(access, staged, pos, Blocks.AIR.getDefaultState());
                        openedCracks.add(pos);
                        continue;
                    }
                    if (shellPotential >= amethystLayer) {
                        boolean budding = random.nextFloat() < FFDConfig.amethystGeodeBuddingChance;
                        stage(access, staged, pos, budding ? buddingAmethyst : amethystBlock);
                        if (budding && random.nextFloat() < FFDConfig.amethystGeodeBudPlacementChance) {
                            potentialCrystals.add(pos);
                        }
                    } else if (shellPotential >= calciteLayer) {
                        stage(access, staged, pos, calcite);
                    } else {
                        stage(access, staged, pos, smoothBasalt);
                    }
                }
            }
        }

        for (BlockPos supportPos : potentialCrystals) {
            IBlockState crystal = crystal(random.nextInt(4));
            if (crystal == null) {
                continue;
            }
            for (EnumFacing direction : DIRECTIONS) {
                BlockPos placePos = supportPos.offset(direction);
                IBlockState placeState = stateAt(access, staged, placePos);
                if (!canClusterGrowAt(placeState)) {
                    continue;
                }
                IBlockState placed = withProperty(crystal, "level", "0");
                placed = withProperty(placed, "facing", direction.getName());
                placed = withProperty(placed, "waterlogged",
                        Boolean.toString(isSourceWater(placeState)));
                staged.put(placePos, placed);
                break;
            }
        }

        commit(access, staged, openedCracks);
    }

    private static void commit(Access access, Map<BlockPos, IBlockState> staged,
                               Set<BlockPos> openedCracks) {
        for (Map.Entry<BlockPos, IBlockState> entry : staged.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!access.isTargetChunk(pos)) {
                continue;
            }
            IBlockState state = entry.getValue();
            access.setState(pos, state);
            if (isWaterlogged(state)) {
                access.markFluidUpdate(pos);
            }
        }
        for (BlockPos crack : openedCracks) {
            for (EnumFacing direction : DIRECTIONS) {
                BlockPos adjacent = crack.offset(direction);
                if (!access.isTargetChunk(adjacent)) {
                    continue;
                }
                IBlockState state = stateAt(access, staged, adjacent);
                if (isFluid(state) || isWaterlogged(state)) {
                    access.markFluidUpdate(adjacent);
                }
            }
        }
    }

    private static void stage(Access access, Map<BlockPos, IBlockState> staged,
                              BlockPos pos, IBlockState state) {
        if (canReplace(stateAt(access, staged, pos))) {
            staged.put(pos, state);
        }
    }

    private static IBlockState stateAt(Access access, Map<BlockPos, IBlockState> staged,
                                       BlockPos pos) {
        IBlockState state = staged.get(pos);
        return state == null ? access.getState(pos) : state;
    }

    private static boolean canReplace(IBlockState state) {
        Block block = state.getBlock();
        return block != Blocks.BEDROCK && block != Blocks.MOB_SPAWNER
                && block != Blocks.CHEST && block != Blocks.END_PORTAL_FRAME;
    }

    private static boolean isInvalid(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.AIR || block == Blocks.BEDROCK
                || block == Blocks.WATER || block == Blocks.FLOWING_WATER
                || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA
                || block == Blocks.ICE || block == Blocks.PACKED_ICE;
    }

    private static boolean canClusterGrowAt(IBlockState state) {
        return state.getBlock() == Blocks.AIR || isSourceWater(state);
    }

    private static boolean isSourceWater(IBlockState state) {
        Block block = state.getBlock();
        return (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
                && state.getPropertyKeys().contains(BlockLiquid.LEVEL)
                && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    private static boolean isFluid(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER
                || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
    }

    private static double potential(BlockPos pos, List<Point> points, double noise) {
        double sum = 0.0D;
        for (Point point : points) {
            double dx = pos.getX() - point.pos.getX();
            double dy = pos.getY() - point.pos.getY();
            double dz = pos.getZ() - point.pos.getZ();
            sum += inverseSqrt(dx * dx + dy * dy + dz * dz + point.offset) + noise;
        }
        return sum;
    }

    private static double potential(BlockPos pos, List<BlockPos> points, int offset, double noise) {
        double sum = 0.0D;
        for (BlockPos point : points) {
            double dx = pos.getX() - point.getX();
            double dy = pos.getY() - point.getY();
            double dz = pos.getZ() - point.getZ();
            sum += inverseSqrt(dx * dx + dy * dy + dz * dz + offset) + noise;
        }
        return sum;
    }

    private static void addCrackPoints(List<BlockPos> points, BlockPos origin, int x, int z) {
        points.add(origin.add(x, 7, z));
        points.add(origin.add(x, 5, z));
        points.add(origin.add(x, 1, z));
    }

    private IBlockState crystal(int index) {
        return crystals[index];
    }

    private boolean hasRequiredBlocks() {
        return amethystBlock != null && buddingAmethyst != null
                && calcite != null && smoothBasalt != null;
    }

    private static IBlockState resolve(Block local, String path) {
        if (FFDConfig.amethystMode == FFDConfig.FeatureMode.DISABLED) {
            return null;
        }
        if (FFDItems.isBlockRegistered(local)) {
            return local.getDefaultState();
        }
        return FFDCompat.getExternalBlockState(FFDCompat.Feature.AMETHYST, path);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withProperty(IBlockState state, String name, String value) {
        for (IProperty property : state.getPropertyKeys()) {
            if (!name.equals(property.getName())) {
                continue;
            }
            Optional parsed = property.parseValue(value);
            if (parsed.isPresent()) {
                return state.withProperty(property, (Comparable) parsed.get());
            }
        }
        return state;
    }

    private static boolean isWaterlogged(IBlockState state) {
        if (BlockAmethystCluster.isWaterlogged(state)) {
            return true;
        }
        for (IProperty<?> property : state.getPropertyKeys()) {
            if ("waterlogged".equals(property.getName())
                    && Boolean.TRUE.equals(state.getValue(property))) {
                return true;
            }
        }
        return false;
    }

    private Random randomForChunk(int chunkX, int chunkZ) {
        long seed = ((long) chunkX * xSeed + (long) chunkZ * zSeed) ^ worldSeed ^ FEATURE_SALT;
        return new Random(seed);
    }

    private static int between(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static long odd(long value) {
        return value / 2L * 2L + 1L;
    }

    private static double inverseSqrt(double value) {
        return 1.0D / Math.sqrt(value);
    }

    public interface Access {
        IBlockState getState(BlockPos pos);

        boolean isTargetChunk(BlockPos pos);

        void setState(BlockPos pos, IBlockState state);

        void markFluidUpdate(BlockPos pos);
    }

    private static final class Point {
        private final BlockPos pos;
        private final int offset;

        private Point(BlockPos pos, int offset) {
            this.pos = pos;
            this.offset = offset;
        }
    }
}
