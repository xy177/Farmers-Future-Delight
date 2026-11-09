package xy177.farmersfuturedelight.common.entity.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRoutines;
import xy177.farmersfuturedelight.common.registry.FFDVillagerTypes;

public final class EntityAIVillagerShowTrades extends EntityAIBase {
    private static final Map<EntityVillager, ItemStack> DISPLAYED_ITEMS = new WeakHashMap<>();
    private final EntityVillager villager;
    private final List<ItemStack> displayItems = new ArrayList<>();
    private EntityPlayer player;
    private ItemStack playerItem;
    private int cycleCounter;
    private int displayIndex;
    private int lookTime;
    private int remaining;
    private long nextSearch;

    public EntityAIVillagerShowTrades(EntityVillager villager) {
        this.villager = villager;
        setMutexBits(2);
    }

    public static ItemStack getDisplayedItem(EntityVillager villager) {
        return villager.world.isRemote
                ? new ItemStack(villager.getEntityData().getCompoundTag(FFDVillagerTypes.CLIENT_STATE)
                        .getCompoundTag("trade_display"))
                : DISPLAYED_ITEMS.getOrDefault(villager, ItemStack.EMPTY);
    }

    private boolean available() {
        if (!villager.isEntityAlive() || villager.isChild() || villager.isAIDisabled()
                || villager.isTrading() || !villager.getHeldItemMainhand().isEmpty()
                || FFDVillagerHomes.isSleeping(villager)) return false;
        FFDVillagerRoutines.Activity activity = FFDVillagerRoutines.getActivity(villager);
        return activity == FFDVillagerRoutines.Activity.IDLE || activity == FFDVillagerRoutines.Activity.WORK
                || activity == FFDVillagerRoutines.Activity.MEET;
    }

    @Override
    public boolean shouldExecute() {
        long now = villager.world.getTotalWorldTime();
        if (now < nextSearch || !available()) return false;
        nextSearch = now + 10L;
        player = null;
        double nearest = 16.0D;
        for (EntityPlayer candidate : villager.world.playerEntities) {
            double distance = villager.getDistanceSq(candidate);
            if (!candidate.isEntityAlive() || candidate.isSpectator() || distance > nearest
                    || !villager.canEntityBeSeen(candidate) || !hasMatchingTrade(candidate)) continue;
            player = candidate;
            nearest = distance;
        }
        return player != null;
    }

    @Override
    public void startExecuting() {
        cycleCounter = 0;
        displayIndex = 0;
        lookTime = 40;
        remaining = 400 + villager.getRNG().nextInt(1201);
        playerItem = null;
        displayItems.clear();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return lookTime > 0 && remaining > 0 && player != null && player.isEntityAlive()
                && !player.isSpectator() && villager.getDistanceSq(player) <= 17.0D
                && villager.canEntityBeSeen(player) && available();
    }

    @Override
    public void updateTask() {
        villager.getLookHelper().setLookPositionWithEntity(player, 30.0F, 30.0F);
        ItemStack held = player.getHeldItemMainhand();
        if (playerItem == null || !sameItem(playerItem, held)) {
            playerItem = held.copy();
            displayItems.clear();
            cycleCounter = 0;
            displayIndex = 0;
            if (!held.isEmpty()) {
                MerchantRecipeList recipes = villager.getRecipes(player);
                if (recipes != null) {
                    for (MerchantRecipe recipe : recipes) {
                        if (!recipe.isRecipeDisabled() && (sameItem(held, recipe.getItemToBuy())
                                || sameItem(held, recipe.getSecondItemToBuy()))
                                && !recipe.getItemToSell().isEmpty()) displayItems.add(recipe.getItemToSell().copy());
                    }
                }
            }
            if (!displayItems.isEmpty()) {
                lookTime = 900;
                display(displayItems.get(0));
            }
        }
        if (displayItems.isEmpty()) {
            display(ItemStack.EMPTY);
            lookTime = Math.min(lookTime, 40);
        } else if (displayItems.size() > 1 && ++cycleCounter >= 40) {
            cycleCounter = 0;
            displayIndex = (displayIndex + 1) % displayItems.size();
            display(displayItems.get(displayIndex));
        }
        --lookTime;
        --remaining;
    }

    private static boolean sameItem(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) return first.isEmpty() && second.isEmpty();
        return first.getItem() == second.getItem();
    }

    private boolean hasMatchingTrade(EntityPlayer candidate) {
        ItemStack held = candidate.getHeldItemMainhand();
        if (held.isEmpty()) return false;
        MerchantRecipeList recipes = villager.getRecipes(candidate);
        if (recipes == null) return false;
        for (MerchantRecipe recipe : recipes) {
            if (!recipe.isRecipeDisabled() && !recipe.getItemToSell().isEmpty()
                    && (sameItem(held, recipe.getItemToBuy())
                    || sameItem(held, recipe.getSecondItemToBuy()))) return true;
        }
        return false;
    }

    private void display(ItemStack stack) {
        if (ItemStack.areItemStacksEqual(DISPLAYED_ITEMS.getOrDefault(villager, ItemStack.EMPTY), stack)) return;
        if (stack.isEmpty()) DISPLAYED_ITEMS.remove(villager);
        else DISPLAYED_ITEMS.put(villager, stack.copy());
        FFDVillagerTypes.sync(villager);
    }

    @Override
    public void resetTask() {
        display(ItemStack.EMPTY);
        player = null;
        playerItem = null;
        displayItems.clear();
    }
}
