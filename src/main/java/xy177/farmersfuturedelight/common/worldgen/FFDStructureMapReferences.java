package xy177.farmersfuturedelight.common.worldgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public final class FFDStructureMapReferences extends WorldSavedData {
    private static final String DATA_NAME = "ffd_structure_map_references";
    private final Map<String, Set<Long>> references = new HashMap<>();

    public FFDStructureMapReferences() {
        this(DATA_NAME);
    }

    public FFDStructureMapReferences(String name) {
        super(name);
    }

    static FFDStructureMapReferences get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        FFDStructureMapReferences data = (FFDStructureMapReferences) storage.getOrLoadData(
                FFDStructureMapReferences.class, DATA_NAME);
        if (data == null) {
            data = new FFDStructureMapReferences();
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    boolean contains(String structure, int chunkX, int chunkZ) {
        Set<Long> claimed = references.get(structure);
        return claimed != null && claimed.contains(ChunkPos.asLong(chunkX, chunkZ));
    }

    void claim(String structure, StructureStart start) {
        if (references.computeIfAbsent(structure, ignored -> new HashSet<>())
                .add(ChunkPos.asLong(start.getChunkPosX(), start.getChunkPosZ()))) markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        references.clear();
        for (String name : tag.getKeySet()) {
            Set<Long> claimed = new HashSet<>();
            NBTTagList positions = tag.getTagList(name, 4);
            for (int index = 0; index < positions.tagCount(); index++)
                claimed.add(((NBTTagLong) positions.get(index)).getLong());
            references.put(name, claimed);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        for (Map.Entry<String, Set<Long>> entry : references.entrySet()) {
            NBTTagList positions = new NBTTagList();
            for (long position : entry.getValue()) positions.appendTag(new NBTTagLong(position));
            tag.setTag(entry.getKey(), positions);
        }
        return tag;
    }
}
