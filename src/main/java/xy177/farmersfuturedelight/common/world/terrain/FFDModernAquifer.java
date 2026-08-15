package xy177.farmersfuturedelight.common.world.terrain;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import xy177.farmersfuturedelight.common.world.noise.FFDXoroshiroRandom;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class FFDModernAquifer {
    private static final int WAY_BELOW_MIN_Y = -4064;
    private static final double FLOWING_UPDATE_SIMILARITY = similarity(100, 144);
    private static final int[][] SURFACE_OFFSETS = {
            {0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0},
            {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
    };

    private final FFDModernWorldgenData data;
    private final FFDNoiseRouter router;
    private final FFDXoroshiroRandom.PositionalFactory randomFactory;
    private final FluidStatus[] statusCache;
    private final int[] locationX;
    private final int[] locationY;
    private final int[] locationZ;
    private final boolean[] locationInitialized;
    private final int[] distances = new int[4];
    private final int[] closest = new int[4];
    private final double[] barrierNoise = new double[1];
    private final Map<Long, Integer> preliminarySurfaceCache = new HashMap<>();
    private final int minGridX;
    private final int minGridY;
    private final int minGridZ;
    private final int gridSizeX;
    private final int gridSizeZ;
    private final int skipSamplingAboveY;
    private boolean shouldScheduleFluidUpdate;

    public FFDModernAquifer(FFDModernWorldgenData data, int chunkX, int chunkZ) {
        this.data = data;
        router = data.router();
        randomFactory = data.positionalFactory("minecraft:aquifer");
        int minBlockX = chunkX << 4;
        int minBlockZ = chunkZ << 4;
        int maxBlockX = minBlockX + 15;
        int maxBlockZ = minBlockZ + 15;
        minGridX = gridX(minBlockX - 5);
        int maxGridX = gridX(maxBlockX - 5) + 1;
        gridSizeX = maxGridX - minGridX + 1;
        minGridY = gridY(FFDModernWorldgenData.MIN_Y + 1) - 1;
        int maxGridY = gridY(FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT + 1) + 1;
        int gridSizeY = maxGridY - minGridY + 1;
        minGridZ = gridZ(minBlockZ - 5);
        int maxGridZ = gridZ(maxBlockZ - 5) + 1;
        gridSizeZ = maxGridZ - minGridZ + 1;
        int size = gridSizeX * gridSizeY * gridSizeZ;
        statusCache = new FluidStatus[size];
        locationX = new int[size];
        locationY = new int[size];
        locationZ = new int[size];
        locationInitialized = new boolean[size];

        int maxSurface = Integer.MIN_VALUE;
        int minSampleX = fromGridX(minGridX, 0);
        int minSampleZ = fromGridZ(minGridZ, 0);
        int maxSampleX = fromGridX(maxGridX, 9);
        int maxSampleZ = fromGridZ(maxGridZ, 9);
        for (int z = minSampleZ; z <= maxSampleZ; z += 4) {
            for (int x = minSampleX; x <= maxSampleX; x += 4) {
                maxSurface = Math.max(maxSurface, preliminarySurfaceLevel(x, z));
            }
        }
        int skipGridY = gridY(maxSurface + 8 + 12) + 1;
        skipSamplingAboveY = fromGridY(skipGridY, 11) - 1;
    }

    @Nullable
    public IBlockState computeSubstance(int x, int y, int z, double density) {
        if (density > 0.0D) {
            shouldScheduleFluidUpdate = false;
            return null;
        }
        FluidStatus global = globalFluid(y);
        if (y > skipSamplingAboveY) {
            shouldScheduleFluidUpdate = false;
            return global.at(y);
        }
        if (global.at(y).getBlock() == Blocks.LAVA) {
            shouldScheduleFluidUpdate = false;
            return Blocks.LAVA.getDefaultState();
        }

        int anchorX = gridX(x - 5);
        int anchorY = gridY(y + 1);
        int anchorZ = gridZ(z - 5);
        Arrays.fill(distances, Integer.MAX_VALUE);
        barrierNoise[0] = Double.NaN;
        for (int offsetX = 0; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                for (int offsetZ = 0; offsetZ <= 1; offsetZ++) {
                    int gridX = anchorX + offsetX;
                    int gridY = anchorY + offsetY;
                    int gridZ = anchorZ + offsetZ;
                    int index = index(gridX, gridY, gridZ);
                    ensureLocation(index, gridX, gridY, gridZ);
                    int dx = locationX[index] - x;
                    int dy = locationY[index] - y;
                    int dz = locationZ[index] - z;
                    insertClosest(index, dx * dx + dy * dy + dz * dz, closest, distances);
                }
            }
        }

        FluidStatus first = status(closest[0]);
        double similarity12 = similarity(distances[0], distances[1]);
        IBlockState actualFluid = first.at(y);
        if (similarity12 <= 0.0D) {
            shouldScheduleFluidUpdate = similarity12 >= FLOWING_UPDATE_SIMILARITY
                    && !first.equals(status(closest[1]));
            return actualFluid;
        }
        if (actualFluid.getBlock() == Blocks.WATER && globalFluid(y - 1).at(y - 1).getBlock() == Blocks.LAVA) {
            shouldScheduleFluidUpdate = true;
            return actualFluid;
        }

        FluidStatus second = status(closest[1]);
        if (density + similarity12 * pressure(x, y, z, barrierNoise, first, second) > 0.0D) {
            shouldScheduleFluidUpdate = false;
            return null;
        }
        FluidStatus third = status(closest[2]);
        double similarity13 = similarity(distances[0], distances[2]);
        if (similarity13 > 0.0D
                && density + similarity12 * similarity13 * pressure(x, y, z, barrierNoise, first, third) > 0.0D) {
            shouldScheduleFluidUpdate = false;
            return null;
        }
        double similarity23 = similarity(distances[1], distances[2]);
        if (similarity23 > 0.0D
                && density + similarity12 * similarity23 * pressure(x, y, z, barrierNoise, second, third) > 0.0D) {
            shouldScheduleFluidUpdate = false;
            return null;
        }
        shouldScheduleFluidUpdate = !first.equals(second)
                || similarity23 >= FLOWING_UPDATE_SIMILARITY && !second.equals(third)
                || similarity13 >= FLOWING_UPDATE_SIMILARITY && !first.equals(third)
                || similarity13 >= FLOWING_UPDATE_SIMILARITY
                && similarity(distances[0], distances[3]) >= FLOWING_UPDATE_SIMILARITY
                && !first.equals(status(closest[3]));
        return actualFluid;
    }

    public boolean shouldScheduleFluidUpdate() {
        return shouldScheduleFluidUpdate;
    }

    private void ensureLocation(int index, int gridX, int gridY, int gridZ) {
        if (locationInitialized[index]) {
            return;
        }
        FFDXoroshiroRandom random = randomFactory.at(gridX, gridY, gridZ);
        locationX[index] = fromGridX(gridX, random.nextInt(10));
        locationY[index] = fromGridY(gridY, random.nextInt(9));
        locationZ[index] = fromGridZ(gridZ, random.nextInt(10));
        locationInitialized[index] = true;
    }

    private static void insertClosest(int index, int distance, int[] closest, int[] distances) {
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] < distance) {
                continue;
            }
            for (int move = distances.length - 1; move > i; move--) {
                distances[move] = distances[move - 1];
                closest[move] = closest[move - 1];
            }
            distances[i] = distance;
            closest[i] = index;
            return;
        }
    }

    private FluidStatus status(int index) {
        FluidStatus cached = statusCache[index];
        if (cached == null) {
            cached = computeFluid(locationX[index], locationY[index], locationZ[index]);
            statusCache[index] = cached;
        }
        return cached;
    }

    private FluidStatus computeFluid(int x, int y, int z) {
        FluidStatus global = globalFluid(y);
        int lowestSurface = Integer.MAX_VALUE;
        int topOfCell = y + 12;
        int bottomOfCell = y - 12;
        boolean centerUnderGlobalFluid = false;
        for (int[] offset : SURFACE_OFFSETS) {
            int sampleX = x + offset[0] * 16;
            int sampleZ = z + offset[1] * 16;
            int preliminarySurface = preliminarySurfaceLevel(sampleX, sampleZ);
            int adjustedSurface = preliminarySurface + 8;
            boolean center = offset[0] == 0 && offset[1] == 0;
            if (center && bottomOfCell > adjustedSurface) {
                return global;
            }
            boolean aboveSurface = topOfCell > adjustedSurface;
            if ((aboveSurface || center) && globalFluid(adjustedSurface).at(adjustedSurface).getBlock() != Blocks.AIR) {
                if (center) {
                    centerUnderGlobalFluid = true;
                }
                if (aboveSurface) {
                    return globalFluid(adjustedSurface);
                }
            }
            lowestSurface = Math.min(lowestSurface, preliminarySurface);
        }
        int level = computeSurfaceLevel(x, y, z, global, lowestSurface, centerUnderGlobalFluid);
        return new FluidStatus(level, computeFluidType(x, y, z, global, level));
    }

    private int computeSurfaceLevel(int x, int y, int z, FluidStatus global, int lowestSurface,
                                    boolean centerUnderGlobalFluid) {
        double partiallyFlooded;
        double fullyFlooded;
        if (router.erosion.sample(x, y, z) < -0.225F && router.depth.sample(x, y, z) > 0.9F) {
            partiallyFlooded = -1.0D;
            fullyFlooded = -1.0D;
        } else {
            int distanceBelowSurface = lowestSurface + 8 - y;
            double factor = centerUnderGlobalFluid ? clampedMap(distanceBelowSurface, 0.0D, 64.0D, 1.0D, 0.0D) : 0.0D;
            double floodedness = clamp(router.fluidLevelFloodedness.sample(x, y, z), -1.0D, 1.0D);
            double fullyThreshold = map(factor, 1.0D, 0.0D, -0.3D, 0.8D);
            double partiallyThreshold = map(factor, 1.0D, 0.0D, -0.8D, 0.4D);
            partiallyFlooded = floodedness - partiallyThreshold;
            fullyFlooded = floodedness - fullyThreshold;
        }
        if (fullyFlooded > 0.0D) {
            return global.level;
        }
        return partiallyFlooded > 0.0D ? randomizedSurfaceLevel(x, y, z, lowestSurface) : WAY_BELOW_MIN_Y;
    }

    private int randomizedSurfaceLevel(int x, int y, int z, int lowestSurface) {
        int cellX = Math.floorDiv(x, 16);
        int cellY = Math.floorDiv(y, 40);
        int cellZ = Math.floorDiv(z, 16);
        int middleY = cellY * 40 + 20;
        double spread = router.fluidLevelSpread.sample(cellX, cellY, cellZ) * 10.0D;
        int quantized = (int) Math.floor(spread / 3.0D) * 3;
        return Math.min(lowestSurface, middleY + quantized);
    }

    private IBlockState computeFluidType(int x, int y, int z, FluidStatus global, int level) {
        IBlockState type = global.type;
        if (level <= -10 && level != WAY_BELOW_MIN_Y && type.getBlock() != Blocks.LAVA) {
            double lava = router.lava.sample(Math.floorDiv(x, 64), Math.floorDiv(y, 40), Math.floorDiv(z, 64));
            if (Math.abs(lava) > 0.3D) {
                type = Blocks.LAVA.getDefaultState();
            }
        }
        return type;
    }

    private double pressure(int x, int y, int z, double[] barrierNoise, FluidStatus first, FluidStatus second) {
        IBlockState firstType = first.at(y);
        IBlockState secondType = second.at(y);
        if (firstType.getBlock() == Blocks.LAVA && secondType.getBlock() == Blocks.WATER
                || firstType.getBlock() == Blocks.WATER && secondType.getBlock() == Blocks.LAVA) {
            return 2.0D;
        }
        int levelDifference = Math.abs(first.level - second.level);
        if (levelDifference == 0) {
            return 0.0D;
        }
        double averageLevel = 0.5D * (first.level + second.level);
        double aboveAverage = y + 0.5D - averageLevel;
        double distanceFromEdge = levelDifference / 2.0D - Math.abs(aboveAverage);
        double center;
        double gradient;
        if (aboveAverage > 0.0D) {
            center = distanceFromEdge;
            gradient = center > 0.0D ? center / 1.5D : center / 2.5D;
        } else {
            center = 3.0D + distanceFromEdge;
            gradient = center > 0.0D ? center / 3.0D : center / 10.0D;
        }
        double noise;
        if (gradient < -2.0D || gradient > 2.0D) {
            noise = 0.0D;
        } else {
            if (Double.isNaN(barrierNoise[0])) {
                barrierNoise[0] = router.barrier.sample(x, y, z);
            }
            noise = barrierNoise[0];
        }
        return 2.0D * (noise + gradient);
    }

    private int preliminarySurfaceLevel(int x, int z) {
        int quantizedX = Math.floorDiv(x, 4) * 4;
        int quantizedZ = Math.floorDiv(z, 4) * 4;
        long key = ((long) quantizedX << 32) ^ (quantizedZ & 0xFFFFFFFFL);
        Integer cached = preliminarySurfaceCache.get(key);
        if (cached == null) {
            cached = data.preliminarySurfaceLevel(quantizedX, quantizedZ);
            preliminarySurfaceCache.put(key, cached);
        }
        return cached;
    }

    private int index(int gridX, int gridY, int gridZ) {
        int x = gridX - minGridX;
        int y = gridY - minGridY;
        int z = gridZ - minGridZ;
        return (y * gridSizeZ + z) * gridSizeX + x;
    }

    private static FluidStatus globalFluid(int y) {
        return y < -54 ? FluidStatus.LAVA : FluidStatus.WATER;
    }

    private static double similarity(int firstDistance, int secondDistance) {
        return 1.0D - (secondDistance - firstDistance) / 25.0D;
    }

    private static int gridX(int block) {
        return block >> 4;
    }

    private static int gridY(int block) {
        return Math.floorDiv(block, 12);
    }

    private static int gridZ(int block) {
        return block >> 4;
    }

    private static int fromGridX(int grid, int offset) {
        return (grid << 4) + offset;
    }

    private static int fromGridY(int grid, int offset) {
        return grid * 12 + offset;
    }

    private static int fromGridZ(int grid, int offset) {
        return (grid << 4) + offset;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampedMap(double value, double fromMin, double fromMax, double toMin, double toMax) {
        return map(clamp(value, Math.min(fromMin, fromMax), Math.max(fromMin, fromMax)),
                fromMin, fromMax, toMin, toMax);
    }

    private static double map(double value, double fromMin, double fromMax, double toMin, double toMax) {
        return toMin + (value - fromMin) / (fromMax - fromMin) * (toMax - toMin);
    }

    private static final class FluidStatus {
        static final FluidStatus WATER = new FluidStatus(FFDModernWorldgenData.SEA_LEVEL, Blocks.WATER.getDefaultState());
        static final FluidStatus LAVA = new FluidStatus(-54, Blocks.LAVA.getDefaultState());

        final int level;
        final IBlockState type;

        FluidStatus(int level, IBlockState type) {
            this.level = level;
            this.type = type;
        }

        IBlockState at(int y) {
            return y < level ? type : Blocks.AIR.getDefaultState();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof FluidStatus)) {
                return false;
            }
            FluidStatus status = (FluidStatus) other;
            return level == status.level && type == status.type;
        }

        @Override
        public int hashCode() {
            return 31 * level + System.identityHashCode(type);
        }
    }
}
