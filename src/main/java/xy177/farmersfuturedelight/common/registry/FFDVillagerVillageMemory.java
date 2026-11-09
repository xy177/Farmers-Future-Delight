package xy177.farmersfuturedelight.common.registry;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.world.WorldServer;
import xy177.farmersfuturedelight.common.raid.RaidVillages;

public final class FFDVillagerVillageMemory {
    public static final String MEMORY_TAG = "farmers_future_delight:village_memory";

    private FFDVillagerVillageMemory() {
    }

    public static void tick(EntityVillager villager) {
        if (villager.getClass() != EntityVillager.class || !(villager.world instanceof WorldServer)) return;
        NBTTagCompound data = villager.getEntityData();
        NBTTagCompound memory = data.getCompoundTag(MEMORY_TAG);
        int refreshInterval = memory.hasKey("dimension", 99)
                && distanceSq(villager, memory) <= 32.0D * 32.0D ? 100 : 20;
        if (Math.floorMod(villager.ticksExisted + villager.getEntityId(), refreshInterval) == 0) {
            NBTTagCompound nearby = nearbyVillage(villager);
            if (nearby != null) {
                memory = nearby;
                data.setTag(MEMORY_TAG, memory);
            }
        }
        if (!memory.hasKey("dimension", 99)) return;
        boolean away = memory.getInteger("dimension") != villager.dimension
                || distanceSq(villager, memory) > 32.0D * 32.0D;
        int awayTicks = away ? memory.getInteger("awayTicks") + 1 : 0;
        memory.setInteger("awayTicks", awayTicks);
        if (awayTicks < 120) return;
        FFDVillagerHomes.release(villager);
        FFDWorkstationLifecycle.clearWorkstation(villager);
        data.removeTag("farmers_future_delight:meeting");
        data.removeTag(MEMORY_TAG);
        if (FFDProfessionWorkstations.getExistingProfessionMode() == 2) {
            FFDVillagerProfessionRetention.release(villager);
        }
        villager.detachHome();
    }

    private static NBTTagCompound nearbyVillage(EntityVillager villager) {
        WorldServer world = (WorldServer) villager.world;
        NBTTagCompound memory = new NBTTagCompound();
        memory.setInteger("dimension", villager.dimension);
        Village village = world.getVillageCollection().getNearestVillage(villager.getPosition(), 32);
        if (village != null && Math.sqrt(villager.getDistanceSq(village.getCenter()))
                > village.getVillageRadius() + 32.0D) village = null;
        if (village != null) {
            memory.setLong("legacyCenter", village.getCenter().toLong());
            memory.setInteger("legacyRadius", village.getVillageRadius());
        }
        NBTTagList sections = new NBTTagList();
        java.util.Set<BlockPos> unique = new java.util.HashSet<>();
        for (BlockPos position : RaidVillages.occupied(world, villager.getPosition(), 3)) {
            BlockPos section = new BlockPos(position.getX() >> 4, position.getY() >> 4, position.getZ() >> 4);
            if (sectionDistanceSq(villager, section) > 32.0D * 32.0D || !unique.add(section)) continue;
            NBTTagCompound source = new NBTTagCompound();
            source.setLong("section", section.toLong());
            sections.appendTag(source);
        }
        memory.setTag("sections", sections);
        return village == null && sections.tagCount() == 0 ? null : memory;
    }

    private static double distanceSq(EntityVillager villager, NBTTagCompound memory) {
        double distance = Double.POSITIVE_INFINITY;
        if (memory.hasKey("legacyCenter", 99)) {
            BlockPos center = BlockPos.fromLong(memory.getLong("legacyCenter"));
            double outside = Math.max(0.0D, Math.sqrt(villager.getDistanceSq(center))
                    - memory.getInteger("legacyRadius"));
            distance = outside * outside;
        }
        NBTTagList sections = memory.getTagList("sections", 10);
        for (int index = 0; index < sections.tagCount(); index++) {
            BlockPos section = BlockPos.fromLong(sections.getCompoundTagAt(index).getLong("section"));
            distance = Math.min(distance, sectionDistanceSq(villager, section));
        }
        return distance;
    }

    private static double sectionDistanceSq(EntityVillager villager, BlockPos section) {
        double offsetX = outside(villager.posX, (section.getX() - 1) * 16.0D, (section.getX() + 2) * 16.0D);
        double offsetY = outside(villager.posY, (section.getY() - 1) * 16.0D, (section.getY() + 2) * 16.0D);
        double offsetZ = outside(villager.posZ, (section.getZ() - 1) * 16.0D, (section.getZ() + 2) * 16.0D);
        return offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;
    }

    private static double outside(double value, double min, double max) {
        return Math.max(0.0D, Math.max(min - value, value - max));
    }
}
