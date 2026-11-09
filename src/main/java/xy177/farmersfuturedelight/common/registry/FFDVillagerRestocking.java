package xy177.farmersfuturedelight.common.registry;

import io.netty.buffer.Unpooled;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDVillagerRestocking {
    private static final Method POPULATE_OFFERS = ObfuscationReflectionHelper.findMethod(
            EntityVillager.class, "func_175554_cu", void.class);
    static final String STATE_TAG = "farmers_future_delight:modern_trades";
    public static final String LAST_WORKED_TAG = "farmers_future_delight:last_worked";

    private FFDVillagerRestocking() {
    }

    public static void enable(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return;
        if (villager.getClass() != EntityVillager.class) return;
        NBTTagCompound state = state(villager);
        state.setBoolean("enabled", true);
        if (!state.hasKey("level", 99)) state.setInteger("level", 1);
        villager.getEntityData().setTag(STATE_TAG, state);
        wrapOffers(villager, currentOffers(villager));
    }

    public static boolean isManaged(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem || !supportsOffers(villager)) return false;
        if (!state(villager).getBoolean("enabled") && !FFDModernVillagerTrades.canManage(villager)) {
            enable(villager);
        }
        return state(villager).getBoolean("enabled");
    }

    private static boolean supportsOffers(EntityVillager villager) {
        return villager.getClass() == EntityVillager.class && !villager.world.isRemote
                && !FFDProfessionWorkstations.isProfessionUnassigned(villager)
                && villager.getProfessionForge() != null
                && villager.getProfessionForge().getRegistryName() != null
                && !"minecraft:nitwit".equals(villager.getProfessionForge().getRegistryName().toString());
    }

    public static int getLevel(EntityVillager villager) {
        return Math.max(1, Math.min(5, state(villager).getInteger("level")));
    }

    public static int getExperience(EntityVillager villager) {
        return Math.max(0, state(villager).getInteger("experience"));
    }

    private static NBTTagCompound state(EntityVillager villager) {
        return villager.getEntityData().getCompoundTag(STATE_TAG);
    }

    static NBTTagCompound getState(EntityVillager villager) {
        return state(villager);
    }

    @Nullable
    private static MerchantRecipeList currentOffers(EntityVillager villager) {
        return ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, villager, "field_70963_i");
    }

    @Nullable
    public static MerchantRecipeList wrapOffers(EntityVillager villager, @Nullable MerchantRecipeList recipes) {
        if (!FFDConfig.modernVillagerSystem) return recipes;
        if (recipes != null && !recipes.isEmpty() && supportsOffers(villager)
                && !FFDModernVillagerTrades.canManage(villager) && !state(villager).getBoolean("enabled")) {
            NBTTagCompound state = state(villager);
            state.setBoolean("enabled", true);
            villager.getEntityData().setTag(STATE_TAG, state);
        }
        if (recipes == null || !isManaged(villager)) return recipes;
        MerchantRecipeList stored = currentOffers(villager);
        Map<MerchantRecipe, MerchantRecipe> replacements = new IdentityHashMap<>();
        if (stored != null) {
            for (int index = 0; index < stored.size(); index++) {
                MerchantRecipe recipe = stored.get(index);
                if (recipe.getClass() != MerchantRecipe.class) continue;
                MerchantRecipe replacement = new FFDModernMerchantRecipe(recipe.writeToTags());
                replacements.put(recipe, replacement);
                stored.set(index, replacement);
            }
        }
        for (int index = 0; index < recipes.size(); index++) {
            MerchantRecipe recipe = recipes.get(index);
            if (recipe.getClass() == MerchantRecipe.class) {
                MerchantRecipe replacement = replacements.get(recipe);
                recipes.set(index, replacement == null ? new FFDModernMerchantRecipe(recipe.writeToTags()) : replacement);
            }
        }
        FFDVillagerReputation.applyPrices(villager, recipes, villager.getCustomer());
        return recipes;
    }

    public static boolean useRecipe(EntityVillager villager, MerchantRecipe recipe) {
        if (!FFDConfig.modernVillagerSystem) return false;
        if (!villager.world.isRemote) {
            villager.getEntityData().setBoolean("farmers_future_delight:has_traded", true);
        }
        if (!isManaged(villager)) return false;
        recipe.incrementToolUses();
        villager.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1.0F,
                1.0F + (villager.getRNG().nextFloat() - villager.getRNG().nextFloat()) * 0.2F);
        NBTTagCompound state = state(villager);
        int experience = recipe instanceof FFDModernMerchantRecipe
                ? ((FFDModernMerchantRecipe) recipe).getVillagerExperience() : 1;
        state.setInteger("experience", state.getInteger("experience") + experience);
        state.setBoolean("hasTraded", true);
        int level = Math.max(1, state.getInteger("level"));
        int[] thresholds = {0, 10, 70, 150, 250};
        boolean increased = FFDModernVillagerTrades.canManage(villager)
                && level < 5 && state.getInteger("experience") >= thresholds[level];
        if (increased) {
            state.setInteger("level", level + 1);
            FFDModernVillagerTrades.populate(villager);
            onCustomerChanged(villager);
            villager.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.REGENERATION, 200, 0));
            MerchantRecipeList recipes = currentOffers(villager);
            if (recipes != null) resendOffers(villager, recipes);
        }
        if (recipe.getRewardsExp()) {
            villager.world.spawnEntity(new EntityXPOrb(villager.world, villager.posX,
                    villager.posY + 0.5D, villager.posZ, 3 + villager.getRNG().nextInt(4) + (increased ? 5 : 0)));
        }
        if (villager.getCustomer() instanceof EntityPlayerMP) {
            FFDVillagerReputation.traded(villager);
            CriteriaTriggers.VILLAGER_TRADE.trigger((EntityPlayerMP) villager.getCustomer(),
                    villager, recipe.getItemToSell());
            FFDMerchantMenus.send((EntityPlayerMP) villager.getCustomer());
        }
        return true;
    }

    public static void tickWork(EntityVillager villager, @Nullable BlockPos position) {
        if (position == null || !isManaged(villager) || villager.isAIDisabled()
                || villager.isTrading() || villager.isChild() || FFDVillagerHomes.isSleeping(villager)
                || FFDVillagerRoutines.isPanicking(villager)) return;
        long dayTime = Math.floorMod(villager.world.getWorldTime(), 24000L);
        if (dayTime < 2000L || dayTime >= 9000L) return;
        NBTTagCompound data = state(villager);
        long gameTime = villager.world.getTotalWorldTime();
        if (gameTime - data.getLong("lastWorkCheck") < 300L || villager.getRNG().nextInt(2) != 0) return;
        data.setLong("lastWorkCheck", gameTime);
        if (!villager.world.isBlockLoaded(position)
                || !FFDWorkstationLifecycle.isWorkstationValid(villager, position)
                || villager.getDistanceSqToCenter(position) >= 1.73D * 1.73D
                || !FFDWorkstationData.get((net.minecraft.world.WorldServer) villager.world)
                        .claim(position, villager.getUniqueID())) return;
        villager.getEntityData().setLong(LAST_WORKED_TAG, gameTime);
        FFDVillagerWork.use(villager, position);
        villager.getLookHelper().setLookPosition(position.getX() + 0.5D, position.getY() + 0.5D,
                position.getZ() + 0.5D, 10.0F, villager.getVerticalFaceSpeed());
        MerchantRecipeList recipes = wrapOffers(villager, currentOffers(villager));
        if (recipes != null && shouldRestock(villager, recipes)) restock(villager, recipes);
    }

    private static boolean shouldRestock(EntityVillager villager, MerchantRecipeList recipes) {
        NBTTagCompound data = state(villager);
        long gameTime = villager.world.getTotalWorldTime();
        long lastRestock = data.getLong("lastRestock");
        long currentDay = Math.floorDiv(villager.world.getWorldTime(), 24000L);
        long checkedDay = data.getLong("lastCheckDay");
        int restocks = data.getInteger("restocksToday");
        if (gameTime > lastRestock + 12000L || checkedDay > 0L && currentDay > checkedDay) {
            int missedUpdates = 2 - restocks;
            if (missedUpdates > 0) {
                for (MerchantRecipe recipe : recipes) {
                    if (recipe instanceof FFDModernMerchantRecipe) ((FFDModernMerchantRecipe) recipe).resetUses();
                }
            }
            for (int update = 0; update < missedUpdates; update++) updateDemand(recipes);
            data.setLong("lastRestock", gameTime);
            data.setInteger("restocksToday", 0);
            lastRestock = gameTime;
            restocks = 0;
            resendOffers(villager, recipes);
        }
        data.setLong("lastCheckDay", currentDay);
        if (restocks != 0 && (restocks >= 2 || gameTime <= lastRestock + 2400L)) return false;
        for (MerchantRecipe recipe : recipes) {
            if (recipe.getToolUses() > 0) return true;
        }
        return false;
    }

    private static void updateDemand(MerchantRecipeList recipes) {
        for (MerchantRecipe recipe : recipes) {
            if (recipe instanceof FFDModernMerchantRecipe) ((FFDModernMerchantRecipe) recipe).updateDemand();
        }
    }

    private static void restock(EntityVillager villager, MerchantRecipeList recipes) {
        updateDemand(recipes);
        for (MerchantRecipe recipe : recipes) {
            if (recipe instanceof FFDModernMerchantRecipe) ((FFDModernMerchantRecipe) recipe).resetUses();
            else if (recipe.isRecipeDisabled()) {
                recipe.increaseMaxTradeUses(villager.getRNG().nextInt(6) + villager.getRNG().nextInt(6) + 2);
            }
        }
        if (!FFDModernVillagerTrades.canManage(villager)) {
            try {
                POPULATE_OFFERS.invoke(villager);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Cannot advance external villager offers", exception);
            }
            recipes = wrapOffers(villager, currentOffers(villager));
        }
        NBTTagCompound data = state(villager);
        data.setLong("lastRestock", villager.world.getTotalWorldTime());
        data.setInteger("restocksToday", data.getInteger("restocksToday") + 1);
        resendOffers(villager, recipes);
    }

    private static void resendOffers(EntityVillager villager, MerchantRecipeList recipes) {
        if (!(villager.getCustomer() instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) villager.getCustomer();
        if (!(player.openContainer instanceof ContainerMerchant)) return;
        PacketBuffer packet = new PacketBuffer(Unpooled.buffer());
        packet.writeInt(player.openContainer.windowId);
        recipes.writeToBuf(packet);
        player.connection.sendPacket(new SPacketCustomPayload("MC|TrList", packet));
        FFDMerchantMenus.send(player);
    }

    public static void onCustomerChanged(EntityVillager villager) {
        MerchantRecipeList recipes = wrapOffers(villager, currentOffers(villager));
        if (!isManaged(villager)) return;
        if (recipes != null) FFDVillagerReputation.applyPrices(villager, recipes, villager.getCustomer());
    }

    public static void refreshPrices(EntityVillager villager) {
        if (!isManaged(villager) || villager.getCustomer() == null) return;
        onCustomerChanged(villager);
        MerchantRecipeList recipes = currentOffers(villager);
        if (recipes != null) resendOffers(villager, recipes);
    }

    public static void prepareRead(NBTTagCompound compound) {
        if (compound.hasKey("ForgeData", 10)) {
            compound.setTag("ForgeData", compound.getCompoundTag("ForgeData").copy());
        }
    }

    public static void readVillager(EntityVillager villager, NBTTagCompound compound) {
        FFDProfessionWorkstations.restoreUnassigned(villager, compound);
        if (!FFDConfig.modernVillagerSystem) return;
        if (!FFDProfessionWorkstations.isProfessionUnassigned(villager)
                && compound.hasKey("Offers", 10) && !compound.hasKey(STATE_TAG, 10)
                && !compound.getCompoundTag("ForgeData").hasKey(FFDVillagerProfessionRetention.STATE_TAG, 10)
                && !villager.getEntityData().hasKey("farmers_future_delight:has_traded", 1)) {
            villager.getEntityData().setBoolean("farmers_future_delight:legacy_trade_history_unknown", true);
        }
        if (!compound.hasKey(STATE_TAG, 10)) return;
        villager.getEntityData().setTag(STATE_TAG, compound.getCompoundTag(STATE_TAG).copy());
        if (!isManaged(villager)) return;
        MerchantRecipeList recipes = currentOffers(villager);
        NBTTagList saved = compound.getCompoundTag("Offers").getTagList("Recipes", 10);
        if (recipes == null) return;
        for (int index = 0; index < Math.min(recipes.size(), saved.tagCount()); index++) {
            NBTTagCompound entry = saved.getCompoundTagAt(index);
            if (!entry.hasKey(FFDModernMerchantRecipe.DATA_TAG, 10)
                    || recipes.get(index).getClass() != MerchantRecipe.class) continue;
            NBTTagCompound current = recipes.get(index).writeToTags();
            if (current.getCompoundTag("buy").equals(entry.getCompoundTag("buy"))
                    && current.getCompoundTag("buyB").equals(entry.getCompoundTag("buyB"))
                    && current.getCompoundTag("sell").equals(entry.getCompoundTag("sell"))) {
                recipes.set(index, new FFDModernMerchantRecipe(entry));
            }
        }
    }

    public static void writeVillager(EntityVillager villager, NBTTagCompound compound) {
        if (isManaged(villager)) compound.setTag(STATE_TAG, state(villager).copy());
    }
}
