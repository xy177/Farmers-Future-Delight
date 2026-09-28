package xy177.farmersfuturedelight.common.worldgen;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class WorldGenModernCarvers {
    private static final int SOURCE_RANGE = 8;
    private static final int CARVER_RANGE = 4;
    private static final int MAX_DISTANCE = (CARVER_RANGE * 2 - 1) * 16;
    private static final int MIN_Y = -63;
    private static final int MAX_Y = 312;
    private static final int HEIGHT = MAX_Y - MIN_Y + 1;
    private static final int SURFACE_PROTECTION_DEPTH = 4;

    private static final float CAVE_PROBABILITY = 0.15F;
    private static final float EXTRA_CAVE_PROBABILITY = 0.07F;
    private static final float CANYON_PROBABILITY = 0.01F;

    private final long worldSeed;
    private final Map<Long, SourcePlan> sourcePlans = new LinkedHashMap<Long, SourcePlan>(512, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, SourcePlan> eldest) {
            return size() > 512;
        }
    };
    private final Map<Long, CarvingMask> carvingMasks =
            new LinkedHashMap<Long, CarvingMask>(128, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, CarvingMask> eldest) {
                    return size() > 128;
                }
            };

    public WorldGenModernCarvers(long worldSeed) {
        this.worldSeed = worldSeed;
    }

    public BitSet generate(int chunkX, int chunkZ, Access access) {
        return applyMask(maskForChunk(chunkX, chunkZ).bits, access);
    }

    public synchronized CarvingMask maskForChunk(int chunkX, int chunkZ) {
        long key = ((long) chunkX & 0xFFFFFFFFL) | (long) chunkZ << 32;
        CarvingMask cached = carvingMasks.get(key);
        if (cached != null) {
            return cached;
        }
        BitSet mask = new BitSet(16 * 16 * HEIGHT);
        for (int sourceX = chunkX - SOURCE_RANGE; sourceX <= chunkX + SOURCE_RANGE; sourceX++) {
            for (int sourceZ = chunkZ - SOURCE_RANGE; sourceZ <= chunkZ + SOURCE_RANGE; sourceZ++) {
                for (PlannedFeature feature : sourcePlan(sourceX, sourceZ).features) {
                    feature.apply(mask, chunkX, chunkZ);
                }
            }
        }
        CarvingMask created = new CarvingMask(mask);
        carvingMasks.put(key, created);
        return created;
    }

    private synchronized SourcePlan sourcePlan(int sourceX, int sourceZ) {
        long key = ((long) sourceX & 0xFFFFFFFFL) | (long) sourceZ << 32;
        SourcePlan cached = sourcePlans.get(key);
        if (cached != null) {
            return cached;
        }
        List<PlannedFeature> features = new ArrayList<>();
        planCaves(features, sourceX, sourceZ,
                largeFeatureRandom(worldSeed, sourceX, sourceZ), CAVE_PROBABILITY,
                -56, 180);
        planCaves(features, sourceX, sourceZ,
                largeFeatureRandom(worldSeed + 1L, sourceX, sourceZ),
                EXTRA_CAVE_PROBABILITY, -56, 47);
        planCanyon(features, sourceX, sourceZ,
                largeFeatureRandom(worldSeed + 2L, sourceX, sourceZ));
        SourcePlan created = new SourcePlan(features);
        sourcePlans.put(key, created);
        return created;
    }

    private static void planCaves(List<PlannedFeature> features, int sourceX, int sourceZ,
                                  Random random, float probability, int minY, int maxY) {
        if (random.nextFloat() > probability) {
            return;
        }
        int caveCount = veryBiasedToBottom(random, 0, 14);
        for (int cave = 0; cave < caveCount; cave++) {
            double x = sourceX * 16 + random.nextInt(16);
            double y = betweenInclusive(random, minY, maxY);
            double z = sourceZ * 16 + random.nextInt(16);
            double horizontalMultiplier = uniform(random, 0.7F, 1.4F);
            double verticalMultiplier = uniform(random, 0.8F, 1.3F);
            double floorLevel = uniform(random, -1.0F, -0.4F);

            int tunnelCount = 1;
            if (random.nextInt(4) == 0) {
                double roomVerticalMultiplier = uniform(random, 0.1F, 0.9F);
                float roomThickness = 1.0F + random.nextFloat() * 6.0F;
                double horizontalRadius = 1.5D + MathHelper.sin(1.5707964F) * roomThickness;
                features.add(new SingleShapeFeature(Shape.cave(x + 1.0D, y, z,
                        horizontalRadius, horizontalRadius * roomVerticalMultiplier,
                        floorLevel)));
                tunnelCount += random.nextInt(4);
            }

            for (int tunnel = 0; tunnel < tunnelCount; tunnel++) {
                float horizontalRotation = random.nextFloat() * ((float) Math.PI * 2.0F);
                float verticalRotation = (random.nextFloat() - 0.5F) / 4.0F;
                float thickness = caveThickness(random);
                int distance = MAX_DISTANCE - random.nextInt(MAX_DISTANCE / 4);
                features.add(planTunnel(random.nextLong(), x, y, z,
                        horizontalMultiplier, verticalMultiplier, thickness,
                        horizontalRotation, verticalRotation, 0, distance, 1.0D,
                        floorLevel));
            }
        }
    }

    private static float caveThickness(Random random) {
        float thickness = trapezoid(random, 0.0F, 3.0F, 1.0F);
        if (random.nextInt(10) == 0) {
            thickness *= random.nextFloat() * random.nextFloat() * 3.0F + 1.0F;
        }
        return thickness;
    }

    private static TunnelFeature planTunnel(long tunnelSeed, double x, double y, double z,
                                            double horizontalMultiplier, double verticalMultiplier,
                                            float thickness, float horizontalRotation,
                                            float verticalRotation, int step, int distance,
                                            double yScale, double floorLevel) {
        Random random = new Random(tunnelSeed);
        int splitPoint = random.nextInt(distance / 2) + distance / 4;
        boolean steep = random.nextInt(6) == 0;
        float verticalDelta = 0.0F;
        float horizontalDelta = 0.0F;
        List<Shape> shapes = new ArrayList<>();

        for (int currentStep = step; currentStep < distance; currentStep++) {
            double horizontalRadius = 1.5D + MathHelper.sin(
                    (float) Math.PI * currentStep / distance) * thickness;
            double verticalRadius = horizontalRadius * yScale;
            float verticalCos = MathHelper.cos(verticalRotation);
            x += MathHelper.cos(horizontalRotation) * verticalCos;
            y += MathHelper.sin(verticalRotation);
            z += MathHelper.sin(horizontalRotation) * verticalCos;
            verticalRotation *= steep ? 0.92F : 0.7F;
            verticalRotation += horizontalDelta * 0.1F;
            horizontalRotation += verticalDelta * 0.1F;
            horizontalDelta *= 0.9F;
            verticalDelta *= 0.75F;
            horizontalDelta += (random.nextFloat() - random.nextFloat())
                    * random.nextFloat() * 2.0F;
            verticalDelta += (random.nextFloat() - random.nextFloat())
                    * random.nextFloat() * 4.0F;

            if (currentStep == splitPoint && thickness > 1.0F) {
                TunnelFeature firstBranch = planTunnel(random.nextLong(), x, y, z,
                        horizontalMultiplier, verticalMultiplier,
                        random.nextFloat() * 0.5F + 0.5F,
                        horizontalRotation - 1.5707964F, verticalRotation / 3.0F,
                        currentStep, distance, 1.0D, floorLevel);
                TunnelFeature secondBranch = planTunnel(random.nextLong(), x, y, z,
                        horizontalMultiplier, verticalMultiplier,
                        random.nextFloat() * 0.5F + 0.5F,
                        horizontalRotation + 1.5707964F, verticalRotation / 3.0F,
                        currentStep, distance, 1.0D, floorLevel);
                return new TunnelFeature(shapes, firstBranch, secondBranch);
            }
            if (random.nextInt(4) == 0) {
                continue;
            }
            shapes.add(Shape.caveTunnel(x, y, z,
                        horizontalRadius * horizontalMultiplier,
                        verticalRadius * verticalMultiplier, floorLevel,
                        currentStep, distance, thickness));
        }
        return new TunnelFeature(shapes, null, null);
    }

    private static void planCanyon(List<PlannedFeature> features, int sourceX, int sourceZ,
                                   Random random) {
        if (random.nextFloat() > CANYON_PROBABILITY) {
            return;
        }
        double x = sourceX * 16 + random.nextInt(16);
        double y = betweenInclusive(random, 10, 67);
        double z = sourceZ * 16 + random.nextInt(16);
        float horizontalRotation = random.nextFloat() * ((float) Math.PI * 2.0F);
        float verticalRotation = uniform(random, -0.125F, 0.125F);
        float thickness = trapezoid(random, 0.0F, 6.0F, 2.0F);
        int distance = (int) (MAX_DISTANCE * uniform(random, 0.75F, 1.0F));
        features.add(planCanyonTunnel(random.nextLong(), x, y, z,
                thickness, horizontalRotation, verticalRotation, distance));
    }

    private static TunnelFeature planCanyonTunnel(long tunnelSeed, double x, double y, double z,
                                                  float thickness, float horizontalRotation,
                                                  float verticalRotation, int distance) {
        Random random = new Random(tunnelSeed);
        float[] widthFactors = canyonWidthFactors(random);
        float verticalDelta = 0.0F;
        float horizontalDelta = 0.0F;
        List<Shape> shapes = new ArrayList<>();

        for (int currentStep = 0; currentStep < distance; currentStep++) {
            double horizontalRadius = 1.5D + MathHelper.sin(
                    (float) Math.PI * currentStep / distance) * thickness;
            double verticalRadius = horizontalRadius * 3.0D;
            horizontalRadius *= uniform(random, 0.75F, 1.0F);
            verticalRadius *= uniform(random, 0.75F, 1.0F);
            float verticalCos = MathHelper.cos(verticalRotation);
            x += MathHelper.cos(horizontalRotation) * verticalCos;
            y += MathHelper.sin(verticalRotation);
            z += MathHelper.sin(horizontalRotation) * verticalCos;
            verticalRotation *= 0.7F;
            verticalRotation += horizontalDelta * 0.05F;
            horizontalRotation += verticalDelta * 0.05F;
            horizontalDelta *= 0.8F;
            verticalDelta *= 0.5F;
            horizontalDelta += (random.nextFloat() - random.nextFloat())
                    * random.nextFloat() * 2.0F;
            verticalDelta += (random.nextFloat() - random.nextFloat())
                    * random.nextFloat() * 4.0F;

            if (random.nextInt(4) == 0) {
                continue;
            }
            shapes.add(Shape.canyon(x, y, z, horizontalRadius, verticalRadius, widthFactors,
                    currentStep, distance, thickness));
        }
        return new TunnelFeature(shapes, null, null);
    }

    private static float[] canyonWidthFactors(Random random) {
        float[] factors = new float[384];
        float factor = 1.0F;
        for (int index = 0; index < factors.length; index++) {
            if (index == 0 || random.nextInt(3) == 0) {
                factor = 1.0F + random.nextFloat() * random.nextFloat();
            }
            factors[index] = factor * factor;
        }
        return factors;
    }

    private static void applyShape(BitSet mask, int chunkX, int chunkZ, Shape shape) {
        double x = shape.x;
        double y = shape.y;
        double z = shape.z;
        double horizontalRadius = shape.horizontalRadius;
        double verticalRadius = shape.verticalRadius;
        double centerX = chunkX * 16 + 8.0D;
        double centerZ = chunkZ * 16 + 8.0D;
        double maxDelta = 16.0D + horizontalRadius * 2.0D;
        if (Math.abs(x - centerX) > maxDelta || Math.abs(z - centerZ) > maxDelta) {
            return;
        }

        int chunkMinX = chunkX * 16;
        int chunkMinZ = chunkZ * 16;
        int minX = Math.max(MathHelper.floor(x - horizontalRadius) - chunkMinX - 1, 0);
        int maxX = Math.min(MathHelper.floor(x + horizontalRadius) - chunkMinX, 15);
        int minY = Math.max(MathHelper.floor(y - verticalRadius) - 1, MIN_Y);
        int maxY = Math.min(MathHelper.floor(y + verticalRadius) + 1, MAX_Y);
        int minZ = Math.max(MathHelper.floor(z - horizontalRadius) - chunkMinZ - 1, 0);
        int maxZ = Math.min(MathHelper.floor(z + horizontalRadius) - chunkMinZ, 15);

        for (int localX = minX; localX <= maxX; localX++) {
            int worldX = chunkMinX + localX;
            double normalizedX = (worldX + 0.5D - x) / horizontalRadius;
            for (int localZ = minZ; localZ <= maxZ; localZ++) {
                int worldZ = chunkMinZ + localZ;
                double normalizedZ = (worldZ + 0.5D - z) / horizontalRadius;
                if (normalizedX * normalizedX + normalizedZ * normalizedZ >= 1.0D) {
                    continue;
                }
                for (int worldY = maxY; worldY > minY; worldY--) {
                    double normalizedY = (worldY - 0.5D - y) / verticalRadius;
                    if (!shape.shouldSkip(normalizedX, normalizedY, normalizedZ, worldY)) {
                        mask.set(maskIndex(localX, worldY, localZ));
                    }
                }
            }
        }
    }

    private static BitSet applyMask(BitSet mask, Access access) {
        BitSet carvedPositions = new BitSet(16 * 16 * HEIGHT);
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int y = MAX_Y; y >= MIN_Y; y--) {
                    if (!mask.get(maskIndex(localX, y, localZ))) {
                        continue;
                    }
                    if (y > access.surfaceY(localX, localZ) - SURFACE_PROTECTION_DEPTH) {
                        continue;
                    }
                    IBlockState previous = access.getState(localX, y, localZ);
                    IBlockState carved = access.carveState(localX, y, localZ);
                    if (carved == null) {
                        continue;
                    }
                    access.setState(localX, y, localZ, carved);
                    carvedPositions.set(maskIndex(localX, y, localZ));
                    if (access.shouldScheduleFluidUpdate()) {
                        access.scheduleFluidUpdate(localX, y, localZ);
                    }
                    if ((previous.getBlock() == Blocks.GRASS
                            || previous.getBlock() == Blocks.MYCELIUM) && y > MIN_Y) {
                        IBlockState below = access.getState(localX, y - 1, localZ);
                        if (below.getBlock() == Blocks.DIRT) {
                            access.setState(localX, y - 1, localZ, previous);
                        }
                    }
                }
            }
        }
        return carvedPositions;
    }

    private static boolean canReach(int chunkX, int chunkZ, double x, double z,
                                    int currentStep, int totalSteps, float thickness) {
        double offsetX = x - (chunkX * 16 + 8.0D);
        double offsetZ = z - (chunkZ * 16 + 8.0D);
        double remaining = totalSteps - currentStep;
        double radius = thickness + 18.0F;
        return offsetX * offsetX + offsetZ * offsetZ - remaining * remaining
                <= radius * radius;
    }

    private static int veryBiasedToBottom(Random random, int min, int max) {
        return min + random.nextInt(random.nextInt(random.nextInt(max - min + 1) + 1) + 1);
    }

    private static int betweenInclusive(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static float uniform(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static float trapezoid(Random random, float min, float max, float plateau) {
        float range = max - min;
        float firstSlope = (range - plateau) / 2.0F;
        float secondSlope = range - firstSlope;
        return min + random.nextFloat() * secondSlope + random.nextFloat() * firstSlope;
    }

    private static Random largeFeatureRandom(long seed, int chunkX, int chunkZ) {
        Random random = new Random(seed);
        long xScale = random.nextLong();
        long zScale = random.nextLong();
        random.setSeed(chunkX * xScale ^ chunkZ * zScale ^ seed);
        return random;
    }

    private static int maskIndex(int localX, int y, int localZ) {
        return ((y - MIN_Y) * 16 + localZ) * 16 + localX;
    }

    public static int localXAt(int maskIndex) {
        return maskIndex & 15;
    }

    public static int localZAt(int maskIndex) {
        return maskIndex >> 4 & 15;
    }

    public static int blockYAt(int maskIndex) {
        return (maskIndex >> 8) + MIN_Y;
    }

    public static final class CarvingMask {
        private final BitSet bits;
        private int[][] edgeEntries;

        private CarvingMask(BitSet bits) {
            this.bits = bits;
        }

        public boolean contains(int localX, int y, int localZ) {
            return y >= MIN_Y && y <= MAX_Y
                    && bits.get(maskIndex(localX & 15, y, localZ & 15));
        }

        public synchronized int[] edgeEntries(int edge, boolean xAxis) {
            if (edgeEntries == null) {
                edgeEntries = new int[4][];
            }
            int cacheIndex = (xAxis ? 0 : 1) * 2 + (edge & 1);
            if (edgeEntries[cacheIndex] == null) {
                List<Integer> values = new ArrayList<>();
                int fixed = edge & 15;
                for (int offset = 0; offset < 16; offset++) {
                    for (int y = MIN_Y; y <= MAX_Y; y++) {
                        int localX = xAxis ? fixed : offset;
                        int localZ = xAxis ? offset : fixed;
                        if (bits.get(maskIndex(localX, y, localZ))) {
                            values.add((offset << 9) | (y - MIN_Y));
                        }
                    }
                }
                int[] result = new int[values.size()];
                for (int i = 0; i < result.length; i++) {
                    result[i] = values.get(i);
                }
                edgeEntries[cacheIndex] = result;
            }
            return edgeEntries[cacheIndex];
        }
    }

    private static final class SourcePlan {
        private final List<PlannedFeature> features;

        private SourcePlan(List<PlannedFeature> features) {
            this.features = features;
        }
    }

    private interface PlannedFeature {
        void apply(BitSet mask, int chunkX, int chunkZ);
    }

    private static final class SingleShapeFeature implements PlannedFeature {
        private final Shape shape;

        private SingleShapeFeature(Shape shape) {
            this.shape = shape;
        }

        @Override
        public void apply(BitSet mask, int chunkX, int chunkZ) {
            applyShape(mask, chunkX, chunkZ, shape);
        }
    }

    private static final class TunnelFeature implements PlannedFeature {
        private final List<Shape> shapes;
        private final TunnelFeature firstBranch;
        private final TunnelFeature secondBranch;

        private TunnelFeature(List<Shape> shapes, TunnelFeature firstBranch,
                              TunnelFeature secondBranch) {
            this.shapes = shapes;
            this.firstBranch = firstBranch;
            this.secondBranch = secondBranch;
        }

        @Override
        public void apply(BitSet mask, int chunkX, int chunkZ) {
            for (Shape shape : shapes) {
                if (!canReach(chunkX, chunkZ, shape.x, shape.z,
                        shape.currentStep, shape.totalSteps, shape.thickness)) {
                    return;
                }
                applyShape(mask, chunkX, chunkZ, shape);
            }
            if (firstBranch != null) {
                firstBranch.apply(mask, chunkX, chunkZ);
                secondBranch.apply(mask, chunkX, chunkZ);
            }
        }
    }

    private static final class Shape {
        private static final int CAVE = 0;
        private static final int CANYON = 1;

        private final double x;
        private final double y;
        private final double z;
        private final double horizontalRadius;
        private final double verticalRadius;
        private final int type;
        private final double floorLevel;
        private final float[] widthFactors;
        private final boolean limitedReach;
        private final int currentStep;
        private final int totalSteps;
        private final float thickness;

        private Shape(double x, double y, double z, double horizontalRadius,
                      double verticalRadius, int type, double floorLevel,
                      float[] widthFactors, boolean limitedReach,
                      int currentStep, int totalSteps, float thickness) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.horizontalRadius = horizontalRadius;
            this.verticalRadius = verticalRadius;
            this.type = type;
            this.floorLevel = floorLevel;
            this.widthFactors = widthFactors;
            this.limitedReach = limitedReach;
            this.currentStep = currentStep;
            this.totalSteps = totalSteps;
            this.thickness = thickness;
        }

        private static Shape cave(double x, double y, double z, double horizontalRadius,
                                  double verticalRadius, double floorLevel) {
            return new Shape(x, y, z, horizontalRadius, verticalRadius,
                    CAVE, floorLevel, null, false, 0, 0, 0.0F);
        }

        private static Shape caveTunnel(double x, double y, double z,
                                        double horizontalRadius, double verticalRadius,
                                        double floorLevel, int currentStep,
                                        int totalSteps, float thickness) {
            return new Shape(x, y, z, horizontalRadius, verticalRadius,
                    CAVE, floorLevel, null, true, currentStep, totalSteps, thickness);
        }

        private static Shape canyon(double x, double y, double z, double horizontalRadius,
                                    double verticalRadius, float[] widthFactors,
                                    int currentStep, int totalSteps, float thickness) {
            return new Shape(x, y, z, horizontalRadius, verticalRadius,
                    CANYON, 0.0D, widthFactors, true, currentStep, totalSteps, thickness);
        }

        private boolean shouldSkip(double normalizedX, double normalizedY,
                                   double normalizedZ, int worldY) {
            if (type == CAVE) {
                return normalizedY <= floorLevel
                        || normalizedX * normalizedX + normalizedY * normalizedY
                        + normalizedZ * normalizedZ >= 1.0D;
            }
            int index = worldY - MIN_Y;
            return index < 0 || index >= widthFactors.length
                    || (normalizedX * normalizedX + normalizedZ * normalizedZ)
                    * widthFactors[index] + normalizedY * normalizedY / 6.0D >= 1.0D;
        }
    }

    public interface Access {
        IBlockState getState(int localX, int y, int localZ);

        void setState(int localX, int y, int localZ, IBlockState state);

        @Nullable
        IBlockState carveState(int localX, int y, int localZ);

        boolean shouldScheduleFluidUpdate();

        void scheduleFluidUpdate(int localX, int y, int localZ);

        int surfaceY(int localX, int localZ);
    }
}
