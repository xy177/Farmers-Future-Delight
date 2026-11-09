package xy177.farmersfuturedelight.common.registry;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDVillagerProfessionRetention {
    public static final String STATE_TAG = "farmers_future_delight:profession_retention";
    private static final String ASSIGNED_TAG = "farmers_future_delight:assigned_profession";

    private FFDVillagerProfessionRetention() {
    }

    public static void onExplicitProfession(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem || villager.world == null || villager.world.isRemote) return;
        villager.getEntityData().setTag(STATE_TAG, new NBTTagCompound());
        villager.getEntityData().removeTag(ASSIGNED_TAG);
    }

    public static void restore(EntityVillager villager, NBTTagCompound saved) {
        if (!FFDConfig.modernVillagerSystem) return;
        NBTTagCompound data = villager.getEntityData();
        if (saved.hasKey(STATE_TAG, 10)) data.setTag(STATE_TAG, saved.getCompoundTag(STATE_TAG).copy());
        else {
            NBTTagCompound state = new NBTTagCompound();
            state.setBoolean("released", saved.hasKey(ASSIGNED_TAG, 8)
                    || saved.getBoolean("farmers_future_delight:workstation_unassigned"));
            data.setTag(STATE_TAG, state);
        }
        if (saved.hasKey(ASSIGNED_TAG, 8)) data.setString(ASSIGNED_TAG, saved.getString(ASSIGNED_TAG));
        else data.removeTag(ASSIGNED_TAG);
    }

    public static void initialize(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return;
        NBTTagCompound data = villager.getEntityData();
        if (!data.hasKey(STATE_TAG, 10)) {
            NBTTagCompound state = new NBTTagCompound();
            state.setBoolean("released", data.hasKey(ASSIGNED_TAG, 8)
                    || FFDProfessionWorkstations.isProfessionUnassigned(villager));
            data.setTag(STATE_TAG, state);
        }
        if (FFDProfessionWorkstations.getExistingProfessionMode() == 3
                && !FFDProfessionWorkstations.isProfessionUnassigned(villager)) release(villager);
    }

    public static boolean isProtected(EntityVillager villager) {
        return FFDConfig.modernVillagerSystem && !FFDProfessionWorkstations.isProfessionUnassigned(villager)
                && FFDProfessionWorkstations.getExistingProfessionMode() != 3
                && !villager.getEntityData().getCompoundTag(STATE_TAG).getBoolean("released");
    }

    public static void release(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return;
        NBTTagCompound state = new NBTTagCompound();
        state.setBoolean("released", true);
        villager.getEntityData().setTag(STATE_TAG, state);
        if (!FFDProfessionWorkstations.isProfessionUnassigned(villager)
                && villager.getProfessionForge() != null) {
            villager.getEntityData().setString(ASSIGNED_TAG,
                    villager.getProfessionForge().getRegistryName() + "/"
                            + FFDProfessionWorkstations.getCareerName(villager));
        }
    }
}
