package xy177.farmersfuturedelight.common.registry;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.DifficultyInstance;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDVillagerConversion {
    private static final String SNAPSHOT_TAG = "farmers_future_delight:villager_snapshot";
    private static final ThreadLocal<CureInitialization> CURING = new ThreadLocal<>();
    private static final String[] VILLAGER_FIELDS = {
            "Profession", "ProfessionName", "Career", "CareerLevel", "Offers",
            "farmers_future_delight:modern_trades"
    };
    private static final String[] ENTITY_FIELDS = {
            FFDModernVillagerTrades.VARIANT_TAG, "farmers_future_delight:modern_trades",
            "farmers_future_delight:gossips", "farmers_future_delight:gossip_decay",
            "farmers_future_delight:has_traded",
            "farmers_future_delight:legacy_trade_history_unknown",
            "farmers_future_delight:assigned_profession",
            FFDVillagerProfessionRetention.STATE_TAG, FFDVillagerVillageMemory.MEMORY_TAG,
            "farmers_future_delight:food_level",
            "farmers_future_delight:workstation_unassigned", FFDTradeConfiguration.getAppliedTag()
    };

    private FFDVillagerConversion() {
    }

    public static Entity onInfection(Entity replacement, EntityLivingBase original) {
        if (!FFDConfig.modernVillagerSystem || replacement.getClass() != EntityZombieVillager.class
                || original.getClass() != EntityVillager.class || original.world.isRemote) return replacement;
        EntityVillager villager = (EntityVillager) original;
        FFDZombieVillagerState.capture(villager, (EntityZombieVillager) replacement);
        NBTTagCompound saved = new NBTTagCompound();
        villager.writeEntityToNBT(saved);
        NBTTagCompound snapshot = new NBTTagCompound();
        for (String field : VILLAGER_FIELDS) copy(saved, snapshot, field);
        NBTTagCompound data = new NBTTagCompound();
        for (String field : ENTITY_FIELDS) copy(villager.getEntityData(), data, field);
        snapshot.setTag("ForgeData", data);
        replacement.getEntityData().setTag(SNAPSHOT_TAG, snapshot);
        return replacement;
    }

    public static Entity onCure(Entity replacement, EntityZombieVillager original) {
        if (!FFDConfig.modernVillagerSystem || replacement.getClass() != EntityVillager.class
                || original.getClass() != EntityZombieVillager.class || original.world.isRemote) return replacement;
        EntityVillager villager = (EntityVillager) replacement;
        NBTTagCompound snapshot = getMatchingSnapshot(original);
        if (snapshot != null) {
            NBTTagCompound saved = new NBTTagCompound();
            villager.writeEntityToNBT(saved);
            saved.merge(snapshot);
            villager.readEntityFromNBT(saved);
            NBTTagCompound data = snapshot.getCompoundTag("ForgeData");
            for (String field : ENTITY_FIELDS) copy(data, villager.getEntityData(), field);
        }
        FFDVillagerTypes.ensureType(villager);
        NBTTagCompound zombie = new NBTTagCompound();
        original.writeEntityToNBT(zombie);
        if (zombie.hasUniqueId("ConversionPlayer")
                && original.world.getPlayerEntityByUUID(zombie.getUniqueId("ConversionPlayer")) instanceof EntityPlayerMP) {
            java.util.UUID player = zombie.getUniqueId("ConversionPlayer");
            FFDVillagerReputation.add(villager, player, FFDVillagerReputation.Type.MAJOR_POSITIVE, 20);
            FFDVillagerReputation.add(villager, player, FFDVillagerReputation.Type.MINOR_POSITIVE, 25);
        }
        return replacement;
    }

    public static IEntityLivingData initializeCure(EntityVillager villager, DifficultyInstance difficulty,
                                                   IEntityLivingData livingData, boolean randomProfession,
                                                   EntityZombieVillager original) {
        if (!FFDConfig.modernVillagerSystem) {
            return villager.finalizeMobSpawn(difficulty, livingData, randomProfession);
        }
        if (villager.getClass() != EntityVillager.class || original.getClass() != EntityZombieVillager.class
                || original.world.isRemote) return villager.finalizeMobSpawn(difficulty, livingData, randomProfession);
        FFDZombieVillagerState.prepareCure(villager, original);
        CureInitialization previous = CURING.get();
        CURING.set(new CureInitialization(villager, getMatchingSnapshot(original) != null,
                FFDZombieVillagerState.ensureState(original).getBoolean("modern")));
        try {
            return villager.finalizeMobSpawn(difficulty, livingData, randomProfession);
        } finally {
            if (previous == null) CURING.remove();
            else CURING.set(previous);
        }
    }

    public static boolean preserveCureOffers(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return false;
        CureInitialization current = CURING.get();
        return current != null && current.villager == villager && current.snapshot;
    }

    public static boolean useLegacyCureOffers(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return true;
        CureInitialization current = CURING.get();
        return current != null && current.villager == villager && !current.modern;
    }

    public static int selectCureCareer(VillagerProfession profession, Random random, EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return profession.getRandomCareer(random);
        CureInitialization current = CURING.get();
        if (current != null && current.villager == villager) {
            int career = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, villager,
                    "field_175563_bv");
            if (career > 0) return career - 1;
        }
        return FFDProfessionWorkstations.selectBoundCareer(profession, random, villager);
    }

    static NBTTagCompound getMatchingSnapshot(EntityZombieVillager zombie) {
        if (!zombie.getEntityData().hasKey(SNAPSHOT_TAG, 10)) return null;
        NBTTagCompound snapshot = zombie.getEntityData().getCompoundTag(SNAPSHOT_TAG);
        if (snapshot.hasKey("ProfessionName", 8)) {
            if (zombie.getForgeProfession() == null) return null;
            return snapshot.getString("ProfessionName").equals(String.valueOf(
                    zombie.getForgeProfession().getRegistryName())) ? snapshot : null;
        }
        return snapshot.getInteger("Profession") == zombie.getProfession() ? snapshot : null;
    }

    private static void copy(NBTTagCompound source, NBTTagCompound target, String field) {
        NBTBase value = source.getTag(field);
        if (value != null) target.setTag(field, value.copy());
    }

    private static final class CureInitialization {
        private final EntityVillager villager;
        private final boolean snapshot;
        private final boolean modern;

        private CureInitialization(EntityVillager villager, boolean snapshot, boolean modern) {
            this.villager = villager;
            this.snapshot = snapshot;
            this.modern = modern;
        }
    }
}
