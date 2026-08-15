package xy177.farmersfuturedelight.common.world.biome;

import java.util.Arrays;

import net.minecraft.util.math.BlockPos;

import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

public final class FFDVerticalBiomeData {
    public static final int CELL_SIZE = 4;
    public static final int CELLS_X = 16 / CELL_SIZE;
    public static final int CELLS_Z = 16 / CELL_SIZE;
    public static final int CELLS_Y = FFDModernWorldgenData.HEIGHT / CELL_SIZE;
    public static final int ENTRY_COUNT = CELLS_X * CELLS_Y * CELLS_Z;

    private final byte[] biomes;

    public FFDVerticalBiomeData(byte[] biomes) {
        if (biomes.length != ENTRY_COUNT) {
            throw new IllegalArgumentException("Expected " + ENTRY_COUNT
                    + " vertical biome entries, got " + biomes.length);
        }
        this.biomes = Arrays.copyOf(biomes, biomes.length);
    }

    public FFDVerticalBiome get(BlockPos pos) {
        return get(pos.getX(), pos.getY(), pos.getZ());
    }

    public FFDVerticalBiome get(int blockX, int blockY, int blockZ) {
        if (blockY < FFDModernWorldgenData.MIN_Y
                || blockY >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
            return FFDVerticalBiome.NONE;
        }
        int cellX = Math.floorMod(blockX, 16) / CELL_SIZE;
        int cellY = Math.floorDiv(blockY - FFDModernWorldgenData.MIN_Y, CELL_SIZE);
        int cellZ = Math.floorMod(blockZ, 16) / CELL_SIZE;
        return FFDVerticalBiome.byId(biomes[index(cellX, cellY, cellZ)] & 255);
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(biomes, biomes.length);
    }

    static int index(int cellX, int cellY, int cellZ) {
        return (cellY * CELLS_Z + cellZ) * CELLS_X + cellX;
    }
}
