package xy177.farmersfuturedelight.common.world.modern;

import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

public final class ModernNoiseChunk {
    private static final int WIDTH = FFDModernWorldgenData.CELL_WIDTH;
    private static final int HEIGHT = FFDModernWorldgenData.CELL_HEIGHT;
    private static final int GRID_SIDE = 16 / WIDTH + 1;
    private static final int GRID_HEIGHT = FFDModernWorldgenData.HEIGHT / HEIGHT + 1;
    private static final int GRID_SIZE = GRID_SIDE * GRID_HEIGHT * GRID_SIDE;
    private final double[][] values = new double[5][GRID_SIZE];
    public final int chunkX;
    public final int chunkZ;

    ModernNoiseChunk(ModernNoiseContext context, int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        for (int gridX = 0; gridX < GRID_SIDE; gridX++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new java.util.concurrent.CancellationException("Modern noise sampling interrupted");
            }
            for (int gridZ = 0; gridZ < GRID_SIDE; gridZ++) {
                for (int gridY = 0; gridY < GRID_HEIGHT; gridY++) {
                    int index = index(gridX, gridY, gridZ);
                    double[] node = context.node((chunkX << 4) + gridX * WIDTH,
                            FFDModernWorldgenData.MIN_Y + gridY * HEIGHT,
                            (chunkZ << 4) + gridZ * WIDTH);
                    for (int field = 0; field < values.length; field++) values[field][index] = node[field];
                }
            }
        }
    }

    public double sample(int localX, int blockY, int localZ) {
        int cellX = Math.min(localX / WIDTH, GRID_SIDE - 2);
        int cellZ = Math.min(localZ / WIDTH, GRID_SIDE - 2);
        int cellY = Math.min(Math.max(Math.floorDiv(blockY - FFDModernWorldgenData.MIN_Y, HEIGHT), 0), GRID_HEIGHT - 2);
        double fractionX = (localX - cellX * WIDTH) / (double) WIDTH;
        double fractionY = (blockY - FFDModernWorldgenData.MIN_Y - cellY * HEIGHT) / (double) HEIGHT;
        double fractionZ = (localZ - cellZ * WIDTH) / (double) WIDTH;
        double main = interpolate(0, cellX, cellY, cellZ, fractionX, fractionY, fractionZ);
        double toggle = interpolate(1, cellX, cellY, cellZ, fractionX, fractionY, fractionZ);
        if (toggle >= -1000000.0D && toggle < 0.0D) {
            return FFDModernWorldgenData.FinalDensityComponents.combine(main, toggle, 0.0D, 0.0D, 0.0D);
        }
        return FFDModernWorldgenData.FinalDensityComponents.combine(main, toggle,
                interpolate(2, cellX, cellY, cellZ, fractionX, fractionY, fractionZ),
                interpolate(3, cellX, cellY, cellZ, fractionX, fractionY, fractionZ),
                interpolate(4, cellX, cellY, cellZ, fractionX, fractionY, fractionZ));
    }

    public Column column(int localX, int localZ) {
        return new Column(localX, localZ);
    }

    private double interpolate(int field, int cellX, int cellY, int cellZ,
                               double fractionX, double fractionY, double fractionZ) {
        double[] grid = values[field];
        int first = index(cellX, cellY, cellZ);
        double northLow = lerp(fractionX, grid[first], grid[first + 1]);
        double northHigh = lerp(fractionX, grid[first + GRID_SIDE * GRID_SIDE], grid[first + GRID_SIDE * GRID_SIDE + 1]);
        double southLow = lerp(fractionX, grid[first + GRID_SIDE], grid[first + GRID_SIDE + 1]);
        double southHigh = lerp(fractionX, grid[first + GRID_SIDE * GRID_SIDE + GRID_SIDE], grid[first + GRID_SIDE * GRID_SIDE + GRID_SIDE + 1]);
        return lerp(fractionZ, lerp(fractionY, northLow, northHigh), lerp(fractionY, southLow, southHigh));
    }

    private static int index(int gridX, int gridY, int gridZ) {
        return (gridY * GRID_SIDE + gridZ) * GRID_SIDE + gridX;
    }

    private static double lerp(double amount, double first, double second) {
        return first + amount * (second - first);
    }

    public final class Column {
        private final int cellX;
        private final int cellZ;
        private final double fractionX;
        private final double fractionZ;
        private final double[] edges = new double[20];
        private int cellY = Integer.MIN_VALUE;

        private Column(int localX, int localZ) {
            cellX = Math.min(localX / WIDTH, GRID_SIDE - 2);
            cellZ = Math.min(localZ / WIDTH, GRID_SIDE - 2);
            fractionX = (localX - cellX * WIDTH) / (double) WIDTH;
            fractionZ = (localZ - cellZ * WIDTH) / (double) WIDTH;
        }

        public double sample(int blockY) {
            int next = Math.min(Math.max(Math.floorDiv(blockY - FFDModernWorldgenData.MIN_Y, HEIGHT), 0), GRID_HEIGHT - 2);
            if (next != cellY) {
                cellY = next;
                int first = index(cellX, cellY, cellZ);
                for (int field = 0; field < values.length; field++) {
                    double[] grid = values[field];
                    int offset = field * 4;
                    edges[offset] = lerp(fractionX, grid[first], grid[first + 1]);
                    edges[offset + 1] = lerp(fractionX, grid[first + GRID_SIDE * GRID_SIDE], grid[first + GRID_SIDE * GRID_SIDE + 1]);
                    edges[offset + 2] = lerp(fractionX, grid[first + GRID_SIDE], grid[first + GRID_SIDE + 1]);
                    edges[offset + 3] = lerp(fractionX, grid[first + GRID_SIDE * GRID_SIDE + GRID_SIDE], grid[first + GRID_SIDE * GRID_SIDE + GRID_SIDE + 1]);
                }
            }
            double fractionY = (blockY - FFDModernWorldgenData.MIN_Y - cellY * HEIGHT) / (double) HEIGHT;
            double main = interpolateColumn(0, fractionY);
            double toggle = interpolateColumn(1, fractionY);
            if (toggle >= -1000000.0D && toggle < 0.0D) {
                return FFDModernWorldgenData.FinalDensityComponents.combine(main, toggle, 0.0D, 0.0D, 0.0D);
            }
            return FFDModernWorldgenData.FinalDensityComponents.combine(main, toggle,
                    interpolateColumn(2, fractionY), interpolateColumn(3, fractionY), interpolateColumn(4, fractionY));
        }

        private double interpolateColumn(int field, double fractionY) {
            int offset = field * 4;
            return lerp(fractionZ, lerp(fractionY, edges[offset], edges[offset + 1]),
                    lerp(fractionY, edges[offset + 2], edges[offset + 3]));
        }
    }
}
