package xy177.farmersfuturedelight.common.item;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public final class FFDMapLockData extends WorldSavedData {
    private static final String DATA_NAME = "ffd_locked_maps";
    private static final String STACK_LOCK_KEY = "FFDLockedMap";
    private static final Map<MapStorage, FFDMapLockData> CACHE = new WeakHashMap<>();
    private final Set<Integer> lockedMaps = new HashSet<>();

    public FFDMapLockData() {
        this(DATA_NAME);
    }

    public FFDMapLockData(String name) {
        super(name);
    }

    public static boolean isLocked(World world, MapData mapData) {
        if (world == null || world.isRemote || mapData == null) {
            return false;
        }
        int mapId = mapId(mapData.mapName);
        FFDMapLockData data = get(world);
        return mapId >= 0 && data != null && data.lockedMaps.contains(mapId);
    }

    public static boolean isLocked(World world, ItemStack stack, MapData mapData) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.getBoolean(STACK_LOCK_KEY)) {
            return true;
        }
        return isLocked(world, mapData);
    }

    public static boolean lockCopy(World world, ItemStack result) {
        if (world == null || world.isRemote || result.isEmpty()
                || !(result.getItem() instanceof ItemMap)) {
            return false;
        }
        MapData original = ((ItemMap) result.getItem()).getMapData(result, world);
        if (original == null) {
            return false;
        }
        int mapId = world.getUniqueDataId("map");
        MapData lockedCopy = new MapData("map_" + mapId);
        lockedCopy.readFromNBT(original.writeToNBT(new NBTTagCompound()).copy());
        lockedCopy.mapDecorations.putAll(original.mapDecorations);
        lockedCopy.markDirty();
        world.setData(lockedCopy.mapName, lockedCopy);
        result.setItemDamage(mapId);
        FFDMapLockData data = get(world);
        if (data != null && data.lockedMaps.add(mapId)) {
            data.markDirty();
        }
        NBTTagCompound stackTag = result.getTagCompound();
        if (stackTag == null) {
            stackTag = new NBTTagCompound();
        }
        stackTag.setBoolean(STACK_LOCK_KEY, true);
        result.setTagCompound(stackTag);
        return true;
    }

    public static boolean shouldSkipUpdate(World world, MapData mapData) {
        return isLocked(world, mapData);
    }

    private static synchronized FFDMapLockData get(World world) {
        MapStorage storage = world.getMapStorage();
        if (storage == null) {
            return null;
        }
        FFDMapLockData data = CACHE.get(storage);
        if (data == null) {
            data = (FFDMapLockData) storage.getOrLoadData(FFDMapLockData.class, DATA_NAME);
            if (data == null) {
                data = new FFDMapLockData();
                storage.setData(DATA_NAME, data);
            }
            CACHE.put(storage, data);
        }
        return data;
    }

    private static int mapId(String name) {
        if (name == null || !name.startsWith("map_")) {
            return -1;
        }
        try {
            return Integer.parseInt(name.substring(4));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        lockedMaps.clear();
        NBTTagList maps = tag.getTagList("maps", 3);
        for (int index = 0; index < maps.tagCount(); index++) {
            lockedMaps.add(maps.getIntAt(index));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList maps = new NBTTagList();
        for (int mapId : lockedMaps) {
            maps.appendTag(new NBTTagInt(mapId));
        }
        tag.setTag("maps", maps);
        return tag;
    }
}
