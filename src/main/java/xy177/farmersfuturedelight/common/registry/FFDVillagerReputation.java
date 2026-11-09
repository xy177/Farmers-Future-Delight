package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDVillagerReputation {
    private static final String GOSSIP_TAG = "farmers_future_delight:gossips";
    private static final String DECAY_TAG = "farmers_future_delight:gossip_decay";
    private static final String SHARED_TAG = "farmers_future_delight:gossip_shared";
    private static final String TRADED_TAG = "farmers_future_delight:traded_player";
    private static final String FOOD_SHARED_TAG = "farmers_future_delight:food_shared_at";

    private FFDVillagerReputation() {
    }

    public enum Type {
        MAJOR_NEGATIVE(-5, 100, 10, 10),
        MINOR_NEGATIVE(-1, 200, 20, 20),
        MINOR_POSITIVE(1, 25, 1, 5),
        MAJOR_POSITIVE(5, 20, 0, 20),
        TRADING(1, 25, 2, 20);

        private final int weight;
        private final int maximum;
        private final int dailyDecay;
        private final int transferDecay;

        Type(int weight, int maximum, int dailyDecay, int transferDecay) {
            this.weight = weight;
            this.maximum = maximum;
            this.dailyDecay = dailyDecay;
            this.transferDecay = transferDecay;
        }
    }

    public static void add(EntityVillager villager, UUID player, Type type, int amount) {
        NBTTagCompound all = gossip(villager);
        String key = player.toString();
        NBTTagCompound entries = all.getCompoundTag(key);
        int value = MathHelper.clamp(entries.getInteger(type.name()) + amount, 0, type.maximum);
        if (value >= 2) entries.setInteger(type.name(), value);
        else entries.removeTag(type.name());
        if (entries.hasNoTags()) all.removeTag(key);
        else all.setTag(key, entries);
        villager.getEntityData().setTag(GOSSIP_TAG, all);
        if (villager.getCustomer() != null && player.equals(villager.getCustomer().getUniqueID())) {
            FFDVillagerRestocking.refreshPrices(villager);
        }
    }

    public static int getReputation(EntityVillager villager, UUID player) {
        NBTTagCompound entries = gossip(villager).getCompoundTag(player.toString());
        int result = 0;
        for (Type type : Type.values()) result += entries.getInteger(type.name()) * type.weight;
        return result;
    }

    private static NBTTagCompound gossip(EntityVillager villager) {
        return villager.getEntityData().getCompoundTag(GOSSIP_TAG);
    }

    public static void traded(EntityVillager villager) {
        if (villager.getCustomer() != null) {
            villager.getEntityData().setUniqueId(TRADED_TAG, villager.getCustomer().getUniqueID());
        }
    }

    public static void tick(EntityVillager villager) {
        if (villager.getClass() != EntityVillager.class || villager.world.isRemote) return;
        NBTTagCompound data = villager.getEntityData();
        if (data.hasUniqueId(TRADED_TAG)) {
            UUID player = data.getUniqueId(TRADED_TAG);
            data.removeTag(TRADED_TAG + "Most");
            data.removeTag(TRADED_TAG + "Least");
            add(villager, player, Type.TRADING, 2);
        }
        long now = villager.world.getTotalWorldTime();
        if (!data.hasKey(DECAY_TAG, 99)) data.setLong(DECAY_TAG, now);
        if (now >= data.getLong(DECAY_TAG) + 24000L) {
            NBTTagCompound all = gossip(villager);
            for (String key : new ArrayList<>(all.getKeySet())) {
                NBTTagCompound entries = all.getCompoundTag(key);
                for (Type type : Type.values()) {
                    int value = entries.getInteger(type.name()) - type.dailyDecay;
                    if (value < 2) entries.removeTag(type.name());
                    else entries.setInteger(type.name(), value);
                }
                if (entries.hasNoTags()) all.removeTag(key);
            }
            data.setLong(DECAY_TAG, now);
            FFDVillagerRestocking.refreshPrices(villager);
        }
        if (villager.isChild() || villager.isTrading() || villager.isAIDisabled()
                || Math.floorMod(villager.ticksExisted + villager.getEntityId(), 80) != 0) return;
        FFDVillagerRoutines.Activity activity = FFDVillagerRoutines.getActivity(villager);
        if (activity != FFDVillagerRoutines.Activity.MEET && activity != FFDVillagerRoutines.Activity.IDLE) return;
        for (EntityVillager other : villager.world.getEntitiesWithinAABB(EntityVillager.class,
                villager.getEntityBoundingBox().grow(4.0D))) {
            if (other == villager || other.getClass() != EntityVillager.class || !other.isEntityAlive()
                    || other.isTrading() || other.isAIDisabled() || other.isChild()
                    || villager.getDistanceSq(other) > 5.0D || !villager.canEntityBeSeen(other)) continue;
            socialize(villager, other);
            break;
        }
    }

    public static void socialize(EntityVillager villager, EntityVillager other) {
        if (villager == other || villager.getClass() != EntityVillager.class
                || other.getClass() != EntityVillager.class || villager.world.isRemote
                || other.world != villager.world || villager.isChild() || other.isChild()
                || villager.isTrading() || other.isTrading() || FFDVillagerHomes.isSleeping(villager)
                || FFDVillagerHomes.isSleeping(other) || villager.getDistanceSq(other) > 5.0D
                || !villager.canEntityBeSeen(other)) return;
        long now = villager.world.getTotalWorldTime();
        NBTTagCompound own = villager.getEntityData();
        NBTTagCompound target = other.getEntityData();
        if (now >= own.getLong(SHARED_TAG) + 1200L && now >= target.getLong(SHARED_TAG) + 1200L) {
            transfer(villager, other);
            FFDVillagerGolems.summon(villager, 5);
            own.setLong(SHARED_TAG, now);
            target.setLong(SHARED_TAG, now);
        }
        if (!own.hasKey(FOOD_SHARED_TAG, 99) || now != own.getLong(FOOD_SHARED_TAG)) {
            FFDVillagerWork.shareFood(villager, other);
            own.setLong(FOOD_SHARED_TAG, now);
        }
    }

    private static void transfer(EntityVillager destination, EntityVillager source) {
        NBTTagCompound original = gossip(source);
        List<Entry> entries = new ArrayList<>();
        int total = 0;
        for (String player : original.getKeySet()) {
            NBTTagCompound values = original.getCompoundTag(player);
            for (Type type : Type.values()) {
                int value = MathHelper.clamp(values.getInteger(type.name()), 0, type.maximum);
                if (value < 2) continue;
                total += Math.abs(value * type.weight);
                entries.add(new Entry(player, type, value, total));
            }
        }
        if (total == 0) return;
        Set<Entry> selected = new HashSet<>();
        for (int attempt = 0; attempt < 10; attempt++) {
            int choice = destination.getRNG().nextInt(total);
            for (Entry entry : entries) {
                if (choice >= entry.upperBound) continue;
                selected.add(entry);
                break;
            }
        }
        NBTTagCompound received = gossip(destination);
        for (Entry entry : selected) {
            int value = entry.value - entry.type.transferDecay;
            if (value < 2) continue;
            NBTTagCompound values = received.getCompoundTag(entry.player);
            values.setInteger(entry.type.name(), Math.max(value, values.getInteger(entry.type.name())));
            received.setTag(entry.player, values);
        }
        destination.getEntityData().setTag(GOSSIP_TAG, received);
        FFDVillagerRestocking.refreshPrices(destination);
    }

    public static void applyPrices(EntityVillager villager, MerchantRecipeList recipes,
                                   @Nullable EntityPlayer player) {
        int reputation = player == null ? 0 : getReputation(villager, player.getUniqueID());
        Potion hero = Potion.getPotionFromResourceLocation(FarmerFutureDelight.MODID + ":hero_of_the_village");
        PotionEffect effect = player == null || hero == null ? null : player.getActivePotionEffect(hero);
        double discount = effect == null ? 0.0D : (double) (0.3F + 0.0625F * effect.getAmplifier());
        for (MerchantRecipe recipe : recipes) {
            if (!(recipe instanceof FFDModernMerchantRecipe)) continue;
            FFDModernMerchantRecipe offer = (FFDModernMerchantRecipe) recipe;
            int adjustment = -MathHelper.floor(reputation * offer.getPriceMultiplier());
            if (discount > 0.0D) adjustment -= Math.max(1,
                    (int) Math.floor(discount * offer.getBaseCost().getCount()));
            offer.setSpecialPrice(adjustment);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVillagerHurt(LivingHurtEvent event) {
        if (event.getAmount() <= 0.0F || event.getEntityLiving().getClass() != EntityVillager.class
                || !(event.getSource().getTrueSource() instanceof EntityPlayer)
                || event.getEntityLiving().world.isRemote) return;
        EntityVillager villager = (EntityVillager) event.getEntityLiving();
        if (FFDVillagerRestocking.isManaged(villager)) {
            add(villager, event.getSource().getTrueSource().getUniqueID(), Type.MINOR_NEGATIVE, 25);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVillagerKilled(LivingDeathEvent event) {
        if (event.getEntityLiving().getClass() != EntityVillager.class
                || !(event.getSource().getTrueSource() instanceof EntityPlayer)
                || event.getEntityLiving().world.isRemote) return;
        EntityVillager victim = (EntityVillager) event.getEntityLiving();
        for (EntityVillager witness : victim.world.getEntitiesWithinAABB(EntityVillager.class,
                victim.getEntityBoundingBox().grow(16.0D))) {
            if (witness == victim || !witness.isEntityAlive() || !FFDVillagerRestocking.isManaged(witness)
                    || !victim.canEntityBeSeen(witness)) continue;
            add(witness, event.getSource().getTrueSource().getUniqueID(), Type.MAJOR_NEGATIVE, 25);
        }
    }

    private static final class Entry {
        private final String player;
        private final Type type;
        private final int value;
        private final int upperBound;

        private Entry(String player, Type type, int value, int upperBound) {
            this.player = player;
            this.type = type;
            this.value = value;
            this.upperBound = upperBound;
        }
    }
}
