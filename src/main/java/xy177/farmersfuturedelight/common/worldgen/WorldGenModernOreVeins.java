package xy177.farmersfuturedelight.common.worldgen;

import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import xy177.farmersfuturedelight.common.world.noise.FFDXoroshiroRandom;
import xy177.farmersfuturedelight.common.world.terrain.FFDDensityFunction;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

import javax.annotation.Nullable;

public final class WorldGenModernOreVeins {
    private static final float VEININESS_THRESHOLD = 0.4F;
    private static final int EDGE_ROUNDOFF_BEGIN = 20;
    private static final double MAX_EDGE_ROUNDOFF = 0.2D;
    private static final float VEIN_SOLIDNESS = 0.7F;
    private static final float MIN_RICHNESS = 0.1F;
    private static final float MAX_RICHNESS = 0.3F;
    private static final float MAX_RICHNESS_THRESHOLD = 0.6F;
    private static final float RAW_ORE_BLOCK_CHANCE = 0.02F;
    private static final float MIN_GAP_NOISE = -0.3F;

    private final FFDModernWorldgenData.VeinDensityComponents density;
    private final FFDXoroshiroRandom.PositionalFactory oreRandom;
    private final VeinType copper;
    private final VeinType iron;

    public WorldGenModernOreVeins(FFDModernWorldgenData data, FFDModernStoneProvider stones) {
        density = data.veinDensityComponents();
        oreRandom = data.positionalFactory("minecraft:ore");

        IBlockState copperOre = stones.normalCopperOre();
        IBlockState rawCopperBlock = stones.rawCopperBlock();
        copper = new VeinType(copperOre, rawCopperBlock,
                Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT,
                        BlockStone.EnumType.GRANITE), 0, 50);

        iron = new VeinType(stones.ironOre(), stones.rawIronBlock(), stones.tuff(), -60, -8);
    }

    @Nullable
    public IBlockState stateAt(int x, int y, int z) {
        return stateAt(x, y, z,
                density.toggleFunction().sample(x, y, z),
                density.ridgedFunction().sample(x, y, z));
    }

    public Sampler sampler(int startX, int startZ) {
        return new Sampler(startX, startZ);
    }

    @Nullable
    private IBlockState stateAt(int x, int y, int z, double toggle, double ridged) {
        if (y < -60 || y > 50) {
            return null;
        }

        VeinType type = toggle > 0.0D ? copper : iron;
        int distanceFromTop = type.maxY - y;
        int distanceFromBottom = y - type.minY;
        if (distanceFromBottom < 0 || distanceFromTop < 0 || type.ore == null) {
            return null;
        }

        double veininess = Math.abs(toggle);
        int distanceFromEdge = Math.min(distanceFromTop, distanceFromBottom);
        double edgeRoundoff = clampedMap(distanceFromEdge, 0.0D, EDGE_ROUNDOFF_BEGIN,
                -MAX_EDGE_ROUNDOFF, 0.0D);
        if (veininess + edgeRoundoff < VEININESS_THRESHOLD) {
            return null;
        }

        FFDXoroshiroRandom random = oreRandom.at(x, y, z);
        if (random.nextFloat() > VEIN_SOLIDNESS || ridged >= 0.0D) {
            return null;
        }

        double richness = clampedMap(veininess, VEININESS_THRESHOLD,
                MAX_RICHNESS_THRESHOLD, MIN_RICHNESS, MAX_RICHNESS);
        if (random.nextFloat() < richness && density.gap().sample(x, y, z) > MIN_GAP_NOISE) {
            return random.nextFloat() < RAW_ORE_BLOCK_CHANCE && type.rawOreBlock != null
                    ? type.rawOreBlock : type.ore;
        }
        return type.filler;
    }

    private static double clampedMap(double value, double fromMin, double fromMax,
                                     double toMin, double toMax) {
        if (value <= fromMin) {
            return toMin;
        }
        if (value >= fromMax) {
            return toMax;
        }
        return toMin + (value - fromMin) / (fromMax - fromMin) * (toMax - toMin);
    }

    public final class Sampler {
        private static final int GRID_X = 16 / FFDModernWorldgenData.CELL_WIDTH + 1;
        private static final int GRID_Y = FFDModernWorldgenData.HEIGHT
                / FFDModernWorldgenData.CELL_HEIGHT + 1;
        private static final int GRID_Z = GRID_X;

        private final int startX;
        private final int startZ;
        private final Grid toggle;
        private final Grid ridgeA;
        private final Grid ridgeB;

        private Sampler(int startX, int startZ) {
            this.startX = startX;
            this.startZ = startZ;
            toggle = new Grid(density.toggle(), startX, startZ);
            ridgeA = new Grid(density.ridgeA(), startX, startZ);
            ridgeB = new Grid(density.ridgeB(), startX, startZ);
        }

        @Nullable
        public IBlockState stateAt(int x, int y, int z) {
            if (y < -60 || y > 50) {
                return null;
            }
            int localX = x - startX;
            int localZ = z - startZ;
            double toggleValue = interpolate(toggle, localX, y, localZ);
            double ridgedValue = density.combineRidged(
                    interpolate(ridgeA, localX, y, localZ),
                    interpolate(ridgeB, localX, y, localZ));
            return WorldGenModernOreVeins.this.stateAt(x, y, z, toggleValue, ridgedValue);
        }

        private double interpolate(Grid grid, int localX, int y, int localZ) {
            int cellX = Math.min(localX / FFDModernWorldgenData.CELL_WIDTH, GRID_X - 2);
            int cellY = Math.min(Math.max((y - FFDModernWorldgenData.MIN_Y)
                    / FFDModernWorldgenData.CELL_HEIGHT, 0), GRID_Y - 2);
            int cellZ = Math.min(localZ / FFDModernWorldgenData.CELL_WIDTH, GRID_Z - 2);
            int x0 = cellX * FFDModernWorldgenData.CELL_WIDTH;
            int y0 = FFDModernWorldgenData.MIN_Y + cellY * FFDModernWorldgenData.CELL_HEIGHT;
            int z0 = cellZ * FFDModernWorldgenData.CELL_WIDTH;
            double fx = (localX - x0) / (double) FFDModernWorldgenData.CELL_WIDTH;
            double fy = (y - y0) / (double) FFDModernWorldgenData.CELL_HEIGHT;
            double fz = (localZ - z0) / (double) FFDModernWorldgenData.CELL_WIDTH;
            double x00 = lerp(fx, grid.value(cellX, cellY, cellZ),
                    grid.value(cellX + 1, cellY, cellZ));
            double x10 = lerp(fx, grid.value(cellX, cellY + 1, cellZ),
                    grid.value(cellX + 1, cellY + 1, cellZ));
            double x01 = lerp(fx, grid.value(cellX, cellY, cellZ + 1),
                    grid.value(cellX + 1, cellY, cellZ + 1));
            double x11 = lerp(fx, grid.value(cellX, cellY + 1, cellZ + 1),
                    grid.value(cellX + 1, cellY + 1, cellZ + 1));
            return lerp(fz, lerp(fy, x00, x10), lerp(fy, x01, x11));
        }

        private double lerp(double amount, double first, double second) {
            return first + amount * (second - first);
        }

        private final class Grid {
            private final double[] values = new double[GRID_X * GRID_Y * GRID_Z];
            private final boolean[] sampled = new boolean[values.length];
            private final FFDDensityFunction function;
            private final int gridStartX;
            private final int gridStartZ;

            private Grid(FFDDensityFunction function, int gridStartX, int gridStartZ) {
                this.function = function;
                this.gridStartX = gridStartX;
                this.gridStartZ = gridStartZ;
            }

            private double value(int cellX, int cellY, int cellZ) {
                int index = (cellY * GRID_Z + cellZ) * GRID_X + cellX;
                if (!sampled[index]) {
                    sampled[index] = true;
                    values[index] = function.sample(
                            gridStartX + cellX * FFDModernWorldgenData.CELL_WIDTH,
                            FFDModernWorldgenData.MIN_Y
                                    + cellY * FFDModernWorldgenData.CELL_HEIGHT,
                            gridStartZ + cellZ * FFDModernWorldgenData.CELL_WIDTH);
                }
                return values[index];
            }

        }
    }

    private static final class VeinType {
        @Nullable
        private final IBlockState ore;
        @Nullable
        private final IBlockState rawOreBlock;
        @Nullable
        private final IBlockState filler;
        private final int minY;
        private final int maxY;

        private VeinType(@Nullable IBlockState ore, @Nullable IBlockState rawOreBlock,
                         @Nullable IBlockState filler, int minY, int maxY) {
            this.ore = ore;
            this.rawOreBlock = rawOreBlock;
            this.filler = filler;
            this.minY = minY;
            this.maxY = maxY;
        }
    }
}
