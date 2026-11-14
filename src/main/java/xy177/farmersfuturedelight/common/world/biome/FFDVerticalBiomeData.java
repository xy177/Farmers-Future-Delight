package xy177.farmersfuturedelight.common.world.biome;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

public final class FFDVerticalBiomeData {
    public static final int CELL_SIZE = 4;
    public static final int CELLS_X = 16 / CELL_SIZE;
    public static final int CELLS_Z = 16 / CELL_SIZE;
    public static final int CELLS_Y = FFDModernWorldgenData.HEIGHT / CELL_SIZE;
    public static final int ENTRY_COUNT = CELLS_X * CELLS_Y * CELLS_Z;

    private final FFDVerticalBiome[] palette;
    private final String[] names;
    private final byte[] biomes;
    private final int[] wideBiomes;
    private Long zoomSeed;

    public FFDVerticalBiomeData(byte[] legacyBiomes) {
        this(expand(legacyBiomes));
    }

    public FFDVerticalBiomeData(int[] entries) {
        checkLength(entries.length);
        Map<FFDVerticalBiome, Integer> indices = new LinkedHashMap<>();
        int[] cells = new int[entries.length];
        for (int index = 0; index < entries.length; index++) {
            FFDVerticalBiome biome = FFDVerticalBiome.byId(entries[index]);
            Integer paletteIndex = indices.get(biome);
            if (paletteIndex == null) {
                paletteIndex = indices.size();
                indices.put(biome, paletteIndex);
            }
            cells[index] = paletteIndex;
        }
        palette = indices.keySet().toArray(new FFDVerticalBiome[0]);
        names = new String[palette.length];
        for (int index = 0; index < names.length; index++) {
            names[index] = palette[index] == FFDVerticalBiome.NONE ? ""
                    : palette[index].registryName().toString();
        }
        biomes = palette.length <= 256 ? compact(cells) : null;
        wideBiomes = biomes == null ? cells : null;
    }

    private FFDVerticalBiomeData(String[] names, int[] cells) {
        this.names = names;
        palette = new FFDVerticalBiome[names.length];
        for (int index = 0; index < names.length; index++) {
            palette[index] = names[index].isEmpty() ? FFDVerticalBiome.NONE
                    : FFDVerticalBiome.byRegistryName(new ResourceLocation(names[index]));
        }
        biomes = names.length <= 256 ? compact(cells) : null;
        wideBiomes = biomes == null ? cells : null;
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
        return palette[cell(index(cellX, cellY, cellZ))];
    }

    public byte[] toByteArray() {
        byte[] result = new byte[ENTRY_COUNT];
        for (int index = 0; index < result.length; index++) {
            int id = palette[cell(index)].id();
            if (id > 255) {
                throw new IllegalStateException("Use paletted NBT for extended vertical biome IDs");
            }
            result[index] = (byte) id;
        }
        return result;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound result = new NBTTagCompound();
        if (zoomSeed != null) result.setLong("ZoomSeed", zoomSeed);
        NBTTagList entries = new NBTTagList();
        for (String name : names) {
            entries.appendTag(new NBTTagString(name));
        }
        result.setTag("Palette", entries);
        if (biomes != null) {
            result.setByteArray("Cells", Arrays.copyOf(biomes, biomes.length));
        } else {
            result.setIntArray("Cells", Arrays.copyOf(wideBiomes, wideBiomes.length));
        }
        return result;
    }

    public static FFDVerticalBiomeData fromNBT(NBTTagCompound data) {
        if (data == null || !data.hasKey("Palette", 9)) {
            throw new IllegalArgumentException("Missing vertical biome palette");
        }
        NBTTagList entries = data.getTagList("Palette", 8);
        if (entries.tagCount() < 1 || entries.tagCount() > ENTRY_COUNT) {
            throw new IllegalArgumentException("Invalid vertical biome palette size");
        }
        String[] names = new String[entries.tagCount()];
        Set<String> unique = new HashSet<>();
        for (int index = 0; index < names.length; index++) {
            String name = entries.getStringTagAt(index);
            if (name.length() > 256 || !unique.add(name)) {
                throw new IllegalArgumentException("Invalid vertical biome palette entry");
            }
            names[index] = name;
        }
        int[] cells;
        if (data.hasKey("Cells", 7)) {
            cells = expand(data.getByteArray("Cells"));
        } else if (data.hasKey("Cells", 11)) {
            cells = data.getIntArray("Cells").clone();
            checkLength(cells.length);
        } else {
            throw new IllegalArgumentException("Missing vertical biome cells");
        }
        for (int cell : cells) {
            if (cell < 0 || cell >= names.length) {
                throw new IllegalArgumentException("Vertical biome palette index out of range");
            }
        }
        FFDVerticalBiomeData result = new FFDVerticalBiomeData(names, cells);
        if (data.hasKey("ZoomSeed", 4)) result.zoomSeed = data.getLong("ZoomSeed");
        return result;
    }

    public void setZoomSeed(long seed) { zoomSeed = seed; }

    @javax.annotation.Nullable
    public Long getZoomSeed() { return zoomSeed; }

    private int cell(int index) {
        return biomes == null ? wideBiomes[index] : biomes[index] & 255;
    }

    private static int[] expand(byte[] values) {
        checkLength(values.length);
        int[] result = new int[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = values[index] & 255;
        }
        return result;
    }

    private static byte[] compact(int[] values) {
        byte[] result = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (byte) values[index];
        }
        return result;
    }

    private static void checkLength(int length) {
        if (length != ENTRY_COUNT) {
            throw new IllegalArgumentException("Expected " + ENTRY_COUNT
                    + " vertical biome entries, got " + length);
        }
    }

    static int index(int cellX, int cellY, int cellZ) {
        return (cellY * CELLS_Z + cellZ) * CELLS_X + cellX;
    }
}
