package xy177.farmersfuturedelight.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public final class CommandStorageApi {
    private static final String DATA_NAME = "ffd_command_storage";

    private CommandStorageApi() {
    }

    public static NBTTagCompound get(MinecraftServer server, ResourceLocation id) {
        return getData(server.worlds[0]).values.getCompoundTag(id.toString()).copy();
    }

    public static void set(MinecraftServer server, ResourceLocation id, NBTTagCompound value) {
        StorageData data = getData(server.worlds[0]);
        data.values.setTag(id.toString(), value.copy());
        data.markDirty();
    }

    public static void merge(MinecraftServer server, ResourceLocation id, NBTTagCompound value) {
        NBTTagCompound current = get(server, id);
        current.merge(value);
        set(server, id, current);
    }

    public static boolean remove(MinecraftServer server, ResourceLocation id) {
        StorageData data = getData(server.worlds[0]);
        if (!data.values.hasKey(id.toString())) {
            return false;
        }
        data.values.removeTag(id.toString());
        data.markDirty();
        return true;
    }

    public static Set<String> getIds(MinecraftServer server) {
        return Collections.unmodifiableSet(new HashSet<>(getData(server.worlds[0]).values.getKeySet()));
    }

    private static StorageData getData(WorldServer world) {
        MapStorage storage = world.getPerWorldStorage();
        StorageData data = (StorageData) storage.getOrLoadData(StorageData.class, DATA_NAME);
        if (data == null) {
            data = new StorageData(DATA_NAME);
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    public static final class StorageData extends WorldSavedData {
        private NBTTagCompound values = new NBTTagCompound();

        public StorageData(String name) {
            super(name);
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            values = compound.getCompoundTag("Values").copy();
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setTag("Values", values.copy());
            return compound;
        }
    }
}
