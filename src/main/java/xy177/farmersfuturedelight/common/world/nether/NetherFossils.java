package xy177.farmersfuturedelight.common.world.nether;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Rotation;
import net.minecraft.world.chunk.ChunkPrimer;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;

final class NetherFossils {
    private static final List<List<Bone>> TEMPLATES = templates();
    private final long seed;
    private final NetherBiomeSource biomes;
    private final Map<Long, Fossil> fossils = new LinkedHashMap<Long, Fossil>(256, .75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, Fossil> eldest) { return size() > 256; }
    };

    NetherFossils(long seed, NetherBiomeSource biomes) { this.seed = seed; this.biomes = biomes; }

    void apply(ChunkPrimer primer, int chunkX, int chunkZ) {
        for (int sourceX = chunkX - 2; sourceX <= chunkX + 2; sourceX++) {
            if ((sourceX & 1) != 0) continue;
            for (int sourceZ = chunkZ - 2; sourceZ <= chunkZ + 2; sourceZ++) {
                if ((sourceZ & 1) != 0) continue;
                long key = (sourceX & 0xFFFFFFFFL) | (long) sourceZ << 32;
                Fossil fossil = fossils.get(key);
                if (fossil == null) { fossil = create(sourceX, sourceZ); fossils.put(key, fossil); }
                if (fossil.bones == null) continue;
                for (Bone bone : fossil.bones) {
                    int x = fossil.x + bone.x;
                    int y = fossil.y + bone.y;
                    int z = fossil.z + bone.z;
                    if (x >> 4 == chunkX && z >> 4 == chunkZ && y >= 0 && y < 128)
                        primer.setBlockState(x & 15, y, z & 15, bone.state);
                }
            }
        }
    }

    private Fossil create(int chunkX, int chunkZ) {
        Random random = new Random(seed);
        long first = random.nextLong();
        long second = random.nextLong();
        random.setSeed((chunkX * first) ^ (chunkZ * second) ^ seed);
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = 32 + random.nextInt(94);
        float[] column = new float[128];
        NetherNoise.column(biomes.noise().lattice(chunkX, chunkZ), x & 15, z & 15, column);
        while (y > 32) {
            boolean air = column[y--] <= 0;
            if (air && column[y] > 0) break;
        }
        if (y <= 32 || biomes.sample(x >> 2, y >> 2, z >> 2) != FFDVerticalBiome.SOUL_SAND_VALLEY)
            return new Fossil(0, 0, 0, null);
        Rotation rotation = Rotation.values()[random.nextInt(4)];
        List<Bone> template = TEMPLATES.get(random.nextInt(TEMPLATES.size()));
        List<Bone> bones = new ArrayList<>(template.size());
        for (Bone bone : template) {
            int rotatedX = rotation == Rotation.CLOCKWISE_90 ? -bone.z
                    : rotation == Rotation.COUNTERCLOCKWISE_90 ? bone.z
                    : rotation == Rotation.CLOCKWISE_180 ? -bone.x : bone.x;
            int rotatedZ = rotation == Rotation.CLOCKWISE_90 ? bone.x
                    : rotation == Rotation.COUNTERCLOCKWISE_90 ? -bone.x
                    : rotation == Rotation.CLOCKWISE_180 ? -bone.z : bone.z;
            bones.add(new Bone(rotatedX, bone.y, rotatedZ, bone.state.withRotation(rotation)));
        }
        return new Fossil(x, y, z, bones);
    }

    private static List<List<Bone>> templates() {
        List<List<Bone>> result = new ArrayList<>();
        for (int index = 1; index <= 14; index++) {
            String path = "/assets/farmers_future_delight/worldgen/26_3/nether/structure/fossil_" + index + ".nbt";
            try (InputStream stream = NetherFossils.class.getResourceAsStream(path)) {
                if (stream == null) throw new IllegalStateException("Missing Nether fossil: " + path);
                NBTTagCompound template = CompressedStreamTools.readCompressed(stream);
                NBTTagList palette = template.getTagList("palette", 10);
                List<IBlockState> states = new ArrayList<>();
                for (int state = 0; state < palette.tagCount(); state++) {
                    NBTTagCompound entry = palette.getCompoundTagAt(state);
                    if (entry.hasKey("id", 8)) entry.setString("Name", entry.getString("id"));
                    if (entry.hasKey("properties", 10)) entry.setTag("Properties", entry.getCompoundTag("properties"));
                    states.add(NBTUtil.readBlockState(entry));
                }
                NBTTagList blocks = template.getTagList("blocks", 10);
                List<Bone> bones = new ArrayList<>();
                for (int block = 0; block < blocks.tagCount(); block++) {
                    NBTTagCompound entry = blocks.getCompoundTagAt(block);
                    IBlockState state = states.get(entry.getInteger("state"));
                    if (state.getBlock() != Blocks.BONE_BLOCK) continue;
                    NBTTagList pos = entry.getTagList("pos", 3);
                    bones.add(new Bone(pos.getIntAt(0), pos.getIntAt(1), pos.getIntAt(2), state));
                }
                result.add(bones);
            } catch (IOException exception) { throw new IllegalStateException("Cannot read Nether fossil: " + path, exception); }
        }
        return result;
    }

    private static final class Fossil {
        final int x;
        final int y;
        final int z;
        final List<Bone> bones;
        Fossil(int x, int y, int z, List<Bone> bones) { this.x = x; this.y = y; this.z = z; this.bones = bones; }
    }

    private static final class Bone {
        final int x;
        final int y;
        final int z;
        final IBlockState state;
        Bone(int x, int y, int z, IBlockState state) { this.x = x; this.y = y; this.z = z; this.state = state; }
    }
}
