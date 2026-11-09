package xy177.farmersfuturedelight.common.registry;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public final class FFDWorkstationData extends WorldSavedData {
    private static final String DATA_NAME = "farmers_future_delight_workstations";
    private final Map<BlockPos, UUID> claims = new HashMap<>();
    private final Map<BlockPos, Set<BlockPos>> sections = new HashMap<>();

    public FFDWorkstationData(String name) {
        super(name);
    }

    public static FFDWorkstationData get(WorldServer world) {
        return get(world, DATA_NAME);
    }

    public static FFDWorkstationData getHomes(WorldServer world) {
        return get(world, "farmers_future_delight_villager_homes");
    }

    private static FFDWorkstationData get(WorldServer world, String name) {
        MapStorage storage = world.getPerWorldStorage();
        FFDWorkstationData data = (FFDWorkstationData) storage.getOrLoadData(
                FFDWorkstationData.class, name);
        if (data == null) {
            data = new FFDWorkstationData(name);
            storage.setData(name, data);
        }
        return data;
    }

    public synchronized boolean isClaimedByOther(BlockPos position, UUID villager) {
        UUID owner = claims.get(position);
        return owner != null && !owner.equals(villager);
    }

    public synchronized boolean isClaimed(BlockPos position) {
        return claims.containsKey(position);
    }

    public synchronized List<BlockPos> getClaimedPositions(BlockPos origin, int sectionRadius) {
        if (sectionRadius < 0 || sectionRadius > 6) {
            throw new IllegalArgumentException("Section radius must be between zero and six");
        }
        List<BlockPos> positions = new ArrayList<>();
        BlockPos center = section(origin);
        for (int offsetX = -sectionRadius; offsetX <= sectionRadius; offsetX++) {
            for (int offsetY = -sectionRadius; offsetY <= sectionRadius; offsetY++) {
                for (int offsetZ = -sectionRadius; offsetZ <= sectionRadius; offsetZ++) {
                    Set<BlockPos> entries = sections.get(center.add(offsetX, offsetY, offsetZ));
                    if (entries != null) positions.addAll(entries);
                }
            }
        }
        return positions;
    }

    private static BlockPos section(BlockPos position) {
        return new BlockPos(position.getX() >> 4, position.getY() >> 4, position.getZ() >> 4);
    }

    private void index(BlockPos position) {
        sections.computeIfAbsent(section(position), ignored -> new HashSet<>()).add(position);
    }

    private void unindex(BlockPos position) {
        BlockPos section = section(position);
        Set<BlockPos> entries = sections.get(section);
        if (entries == null) return;
        entries.remove(position);
        if (entries.isEmpty()) sections.remove(section);
    }

    public synchronized boolean claim(BlockPos position, UUID villager) {
        BlockPos immutablePosition = position.toImmutable();
        UUID owner = claims.get(immutablePosition);
        if (owner != null && !owner.equals(villager)) return false;
        if (owner == null) {
            claims.put(immutablePosition, villager);
            index(immutablePosition);
            markDirty();
        }
        return true;
    }

    public synchronized void release(BlockPos position, UUID villager) {
        UUID owner = claims.get(position);
        if (villager.equals(owner)) {
            claims.remove(position);
            unindex(position);
            markDirty();
        }
    }

    public synchronized void releaseAt(BlockPos position) {
        if (claims.remove(position) != null) {
            unindex(position);
            markDirty();
        }
    }

    @Override
    public synchronized void readFromNBT(NBTTagCompound compound) {
        claims.clear();
        sections.clear();
        NBTTagList savedClaims = compound.getTagList("Claims", 10);
        for (int index = 0; index < savedClaims.tagCount(); index++) {
            NBTTagCompound entry = savedClaims.getCompoundTagAt(index);
            try {
                BlockPos position = new BlockPos(entry.getInteger("X"), entry.getInteger("Y"),
                        entry.getInteger("Z"));
                UUID owner = new UUID(entry.getLong("Most"), entry.getLong("Least"));
                claims.put(position, owner);
                index(position);
            } catch (RuntimeException ignored) {
            }
        }
    }

    @Override
    public synchronized NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList savedClaims = new NBTTagList();
        for (Map.Entry<BlockPos, UUID> claim : claims.entrySet()) {
            NBTTagCompound entry = new NBTTagCompound();
            BlockPos position = claim.getKey();
            UUID owner = claim.getValue();
            entry.setInteger("X", position.getX());
            entry.setInteger("Y", position.getY());
            entry.setInteger("Z", position.getZ());
            entry.setLong("Most", owner.getMostSignificantBits());
            entry.setLong("Least", owner.getLeastSignificantBits());
            savedClaims.appendTag(entry);
        }
        compound.setTag("Claims", savedClaims);
        return compound;
    }
}
