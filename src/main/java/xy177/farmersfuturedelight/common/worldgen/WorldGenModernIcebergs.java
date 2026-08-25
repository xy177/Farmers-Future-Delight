package xy177.farmersfuturedelight.common.worldgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

public final class WorldGenModernIcebergs {
    private static final int WORLDGEN_FLAGS = 2 | 16;
    private static final int MAX_RADIUS = 11;
    private static final long PACKED_SALT = 0x4943454245524750L;
    private static final long BLUE_SALT = 0x4943454245524742L;
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();
    private static final IBlockState PACKED_ICE = Blocks.PACKED_ICE.getDefaultState();
    private static final IBlockState SNOW_BLOCK = Blocks.SNOW.getDefaultState();

    private final long worldSeed;
    private final FFDModernBiomeResolver resolver;

    public WorldGenModernIcebergs(long worldSeed, FFDModernBiomeResolver resolver) {
        this.worldSeed = worldSeed;
        this.resolver = resolver;
    }

    public void generate(World world, int targetChunkX, int targetChunkZ) {
        int minX = targetChunkX << 4;
        int minZ = targetChunkZ << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        for (int sourceX = targetChunkX - 1; sourceX <= targetChunkX + 1; sourceX++) {
            for (int sourceZ = targetChunkZ - 1; sourceZ <= targetChunkZ + 1; sourceZ++) {
                generateCandidate(world, sourceX, sourceZ, minX, minZ, maxX, maxZ,
                        16, PACKED_SALT);
                generateCandidate(world, sourceX, sourceZ, minX, minZ, maxX, maxZ,
                        200, BLUE_SALT);
            }
        }
    }

    private void generateCandidate(World world, int sourceChunkX, int sourceChunkZ,
                                   int minX, int minZ, int maxX, int maxZ,
                                   int chance, long salt) {
        Random random = chunkRandom(sourceChunkX, sourceChunkZ, salt);
        if (random.nextInt(chance) != 0) {
            return;
        }
        int originX = (sourceChunkX << 4) + random.nextInt(16);
        int originZ = (sourceChunkZ << 4) + random.nextInt(16);
        if (originX + MAX_RADIUS < minX || originX - MAX_RADIUS > maxX
                || originZ + MAX_RADIUS < minZ || originZ - MAX_RADIUS > maxZ
                || !isFrozenOcean(originX, originZ)) {
            return;
        }
        IcebergPlan plan = new IcebergPlan(random,
                new BlockPos(originX, FFDModernWorldgenData.SEA_LEVEL, originZ));
        plan.generate();
        plan.apply(world, minX, minZ, maxX, maxZ);
    }

    private boolean isFrozenOcean(int x, int z) {
        FFDModernBiomeResolver.Sample sample = resolver.sampleFuzzy(x, z);
        return resolver.matches(sample, "minecraft:frozen_ocean")
                || resolver.matches(sample, "minecraft:deep_frozen_ocean");
    }

    private Random chunkRandom(int chunkX, int chunkZ, long salt) {
        Random random = new Random(worldSeed);
        long xSeed = random.nextLong() / 2L * 2L + 1L;
        long zSeed = random.nextLong() / 2L * 2L + 1L;
        random.setSeed(((long) chunkX * xSeed + (long) chunkZ * zSeed)
                ^ worldSeed ^ salt);
        return random;
    }

    private static final class IcebergPlan {
        private final Random random;
        private final BlockPos origin;
        private final Map<BlockPos, IBlockState> states = new HashMap<>();

        private IcebergPlan(Random random, BlockPos origin) {
            this.random = random;
            this.origin = origin;
        }

        private void generate() {
            boolean snowOnTop = random.nextDouble() > 0.7D;
            double shapeAngle = random.nextDouble() * Math.PI * 2.0D;
            int shapeEllipseA = 11 - random.nextInt(5);
            int shapeEllipseC = 3 + random.nextInt(3);
            boolean ellipse = random.nextDouble() > 0.7D;
            int overWaterHeight = ellipse ? random.nextInt(6) + 6 : random.nextInt(15) + 3;
            if (!ellipse && random.nextDouble() > 0.9D) {
                overWaterHeight += random.nextInt(19) + 7;
            }
            int underWaterHeight = Math.min(overWaterHeight + random.nextInt(11), 18);
            int width = Math.min(overWaterHeight + random.nextInt(7) - random.nextInt(5), 11);
            int a = ellipse ? shapeEllipseA : 11;
            for (int x = -a; x < a; x++) {
                for (int z = -a; z < a; z++) {
                    for (int y = 0; y < overWaterHeight; y++) {
                        int radius = ellipse
                                ? heightDependentRadiusEllipse(y, overWaterHeight, width)
                                : heightDependentRadiusRound(y, overWaterHeight, width);
                        if (!ellipse && x >= radius) {
                            continue;
                        }
                        generateBlock(overWaterHeight, x, y, z, radius, a, ellipse,
                                shapeEllipseC, shapeAngle, snowOnTop);
                    }
                }
            }
            smooth(width, overWaterHeight, ellipse, shapeEllipseA);
            for (int x = -a; x < a; x++) {
                for (int z = -a; z < a; z++) {
                    for (int y = -1; y > -underWaterHeight; y--) {
                        int newA = ellipse
                                ? MathHelper.ceil(a * (1.0F - (float) Math.pow(y, 2.0D)
                                / (underWaterHeight * 8.0F))) : a;
                        int radius = heightDependentRadiusSteep(-y, underWaterHeight, width);
                        if (x >= radius) {
                            continue;
                        }
                        generateBlock(underWaterHeight, x, y, z, radius, newA, ellipse,
                                shapeEllipseC, shapeAngle, snowOnTop);
                    }
                }
            }
            boolean cutOut = ellipse ? random.nextDouble() > 0.1D : random.nextDouble() > 0.7D;
            if (cutOut) {
                generateCutOut(width, overWaterHeight, ellipse, shapeEllipseA,
                        shapeAngle, shapeEllipseC);
            }
        }

        private void generateCutOut(int width, int height, boolean ellipse,
                                    int shapeEllipseA, double shapeAngle, int shapeEllipseC) {
            int signX = random.nextBoolean() ? -1 : 1;
            int signZ = random.nextBoolean() ? -1 : 1;
            int xOff = random.nextInt(Math.max(width / 2 - 2, 1));
            if (random.nextBoolean()) {
                xOff = width / 2 + 1
                        - random.nextInt(Math.max(width - width / 2 - 1, 1));
            }
            int zOff = random.nextInt(Math.max(width / 2 - 2, 1));
            if (random.nextBoolean()) {
                zOff = width / 2 + 1
                        - random.nextInt(Math.max(width - width / 2 - 1, 1));
            }
            if (ellipse) {
                xOff = random.nextInt(Math.max(shapeEllipseA - 5, 1));
                zOff = xOff;
            }
            BlockPos localOrigin = new BlockPos(signX * xOff, 0, signZ * zOff);
            double angle = ellipse ? shapeAngle + Math.PI / 2.0D
                    : random.nextDouble() * Math.PI * 2.0D;
            for (int y = 0; y < height - 3; y++) {
                carve(heightDependentRadiusRound(y, height, width), y, false, angle,
                        localOrigin, shapeEllipseA, shapeEllipseC);
            }
            for (int y = -1; y > -height + random.nextInt(5); y--) {
                carve(heightDependentRadiusSteep(-y, height, width), y, true, angle,
                        localOrigin, shapeEllipseA, shapeEllipseC);
            }
        }

        private void carve(int radius, int y, boolean underwater, double angle,
                           BlockPos localOrigin, int shapeEllipseA, int shapeEllipseC) {
            int a = radius + 1 + shapeEllipseA / 3;
            int c = Math.min(radius - 3, 3) + shapeEllipseC / 2 - 1;
            for (int x = -a; x < a; x++) {
                for (int z = -a; z < a; z++) {
                    if (signedDistanceEllipse(x, z, localOrigin, a, c, angle) >= 0.0D) {
                        continue;
                    }
                    BlockPos pos = origin.add(x, y, z);
                    if (!isIcebergState(stateAt(pos))) {
                        continue;
                    }
                    states.put(pos, underwater ? WATER : AIR);
                }
            }
        }

        private void generateBlock(int height, int x, int y, int z, int radius, int a,
                                   boolean ellipse, int shapeEllipseC, double shapeAngle,
                                   boolean snowOnTop) {
            double distance = ellipse
                    ? signedDistanceEllipse(x, z, BlockPos.ORIGIN, a,
                    getEllipseC(y, height, shapeEllipseC), shapeAngle)
                    : signedDistanceCircle(x, z, radius);
            if (distance >= 0.0D) {
                return;
            }
            double compare = ellipse ? -0.5D : -6.0D - random.nextInt(3);
            if (distance > compare && random.nextDouble() > 0.9D) {
                return;
            }
            boolean randomness = !ellipse || random.nextDouble() > 0.05D;
            int divisor = ellipse ? 3 : 2;
            IBlockState state = snowOnTop
                    && height - y <= random.nextInt(Math.max(1, height / divisor)) + height * 0.6D
                    && randomness ? SNOW_BLOCK : PACKED_ICE;
            states.put(origin.add(x, y, z), state);
        }

        private void smooth(int width, int height, boolean ellipse, int shapeEllipseA) {
            int a = ellipse ? shapeEllipseA : width / 2;
            for (int x = -a; x <= a; x++) {
                for (int z = -a; z <= a; z++) {
                    for (int y = 0; y <= height; y++) {
                        BlockPos pos = origin.add(x, y, z);
                        if (!isIcebergState(stateAt(pos))) {
                            continue;
                        }
                        if (stateAt(pos.down()).getBlock() == Blocks.AIR) {
                            states.put(pos, AIR);
                            states.put(pos.up(), AIR);
                            continue;
                        }
                        int openSides = 0;
                        if (!isIcebergState(stateAt(pos.west()))) {
                            openSides++;
                        }
                        if (!isIcebergState(stateAt(pos.east()))) {
                            openSides++;
                        }
                        if (!isIcebergState(stateAt(pos.north()))) {
                            openSides++;
                        }
                        if (!isIcebergState(stateAt(pos.south()))) {
                            openSides++;
                        }
                        if (openSides >= 3) {
                            states.put(pos, AIR);
                        }
                    }
                }
            }
        }

        private IBlockState stateAt(BlockPos pos) {
            IBlockState state = states.get(pos);
            if (state != null) {
                return state;
            }
            return pos.getY() <= FFDModernWorldgenData.SEA_LEVEL ? WATER : AIR;
        }

        private double signedDistanceCircle(int x, int z, int radius) {
            float offset = 10.0F * MathHelper.clamp(random.nextFloat(), 0.2F, 0.8F) / radius;
            return offset + x * x + z * z - radius * radius;
        }

        private static double signedDistanceEllipse(int x, int z, BlockPos localOrigin,
                                                    int a, int c, double angle) {
            double translatedX = x - localOrigin.getX();
            double translatedZ = z - localOrigin.getZ();
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            return Math.pow((translatedX * cos - translatedZ * sin) / a, 2.0D)
                    + Math.pow((translatedX * sin + translatedZ * cos) / c, 2.0D) - 1.0D;
        }

        private int heightDependentRadiusRound(int y, int height, int width) {
            float scaleFactor = 3.5F - random.nextFloat();
            float scale = (1.0F - (float) Math.pow(y, 2.0D) / (height * scaleFactor)) * width;
            if (height > 15 + random.nextInt(5)) {
                int adjustedY = y < 3 + random.nextInt(6) ? y / 2 : y;
                scale = (1.0F - adjustedY / (height * scaleFactor * 0.4F)) * width;
            }
            return MathHelper.ceil(scale / 2.0F);
        }

        private static int heightDependentRadiusEllipse(int y, int height, int width) {
            float scale = (1.0F - (float) Math.pow(y, 2.0D) / height) * width;
            return MathHelper.ceil(scale / 2.0F);
        }

        private int heightDependentRadiusSteep(int y, int height, int width) {
            float scaleFactor = 1.0F + random.nextFloat() / 2.0F;
            float scale = (1.0F - y / (height * scaleFactor)) * width;
            return MathHelper.ceil(scale / 2.0F);
        }

        private static int getEllipseC(int y, int height, int shapeEllipseC) {
            int c = shapeEllipseC;
            if (y > 0 && height - y <= 3) {
                c -= 4 - (height - y);
            }
            return c;
        }

        private void apply(World world, int minX, int minZ, int maxX, int maxZ) {
            for (Map.Entry<BlockPos, IBlockState> entry : states.entrySet()) {
                BlockPos pos = entry.getKey();
                if (pos.getX() < minX || pos.getX() > maxX
                        || pos.getZ() < minZ || pos.getZ() > maxZ) {
                    continue;
                }
                IBlockState current = world.getBlockState(pos);
                if (!isReplaceable(current)) {
                    continue;
                }
                IBlockState desired = entry.getValue();
                if (desired.getBlock() == Blocks.SNOW && isWater(current)) {
                    desired = PACKED_ICE;
                }
                if (current != desired) {
                    world.setBlockState(pos, desired, WORLDGEN_FLAGS);
                }
            }
        }

        private static boolean isReplaceable(IBlockState state) {
            Block block = state.getBlock();
            return block == Blocks.AIR || block == Blocks.SNOW || block == Blocks.SNOW_LAYER
                    || block == Blocks.ICE || block == Blocks.PACKED_ICE || isWater(state);
        }

        private static boolean isWater(IBlockState state) {
            Block block = state.getBlock();
            return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
        }

        private static boolean isIcebergState(IBlockState state) {
            Block block = state.getBlock();
            return block == Blocks.PACKED_ICE || block == Blocks.SNOW;
        }
    }
}
