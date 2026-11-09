package xy177.farmersfuturedelight.common.registry;

import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityWanderingTrader;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.network.PacketMerchantState;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDMerchantMenus {
    private static final Map<EntityPlayerMP, Integer> WINDOWS = new WeakHashMap<>();

    private FFDMerchantMenus() {
    }

    @Nullable
    private static IMerchant merchant(EntityPlayerMP player) {
        if (player.openContainer == null || player.openContainer.getClass() != ContainerMerchant.class) return null;
        IMerchant merchant = ObfuscationReflectionHelper.getPrivateValue(ContainerMerchant.class,
                (ContainerMerchant) player.openContainer, "field_75178_e");
        if (merchant == null || merchant.getCustomer() != player) return null;
        if (merchant instanceof EntityWanderingTrader) return merchant;
        return merchant instanceof EntityVillager && FFDVillagerRestocking.isManaged((EntityVillager) merchant)
                ? merchant : null;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        int window = player.openContainer == null ? -1 : player.openContainer.windowId;
        Integer last = WINDOWS.put(player, window);
        if (last == null || last != window) send(player);
    }

    public static void send(EntityPlayerMP player) {
        IMerchant merchant = merchant(player);
        if (merchant == null) return;
        MerchantRecipeList recipes = merchant.getRecipes(player);
        if (recipes != null && recipes.size() <= 255) {
            FFDNetwork.sendMerchantState(player, new PacketMerchantState(
                    player.openContainer.windowId, merchant, recipes));
        }
    }

    public static void select(EntityPlayerMP player, int windowId, int selection) {
        IMerchant merchant = merchant(player);
        if (merchant == null || player.openContainer.windowId != windowId) return;
        MerchantRecipeList recipes = merchant.getRecipes(player);
        if (recipes == null || selection < 0 || selection >= recipes.size()) return;
        ContainerMerchant menu = (ContainerMerchant) player.openContainer;
        menu.setCurrentRecipeIndex(selection);
        InventoryMerchant inventory = menu.getMerchantInventory();
        for (int slot = 0; slot < 2; slot++) {
            ItemStack remaining = inventory.getStackInSlot(slot);
            if (remaining.isEmpty()) continue;
            boolean moved = returnPayment(menu, remaining);
            inventory.setInventorySlotContents(slot, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            if (!moved) {
                menu.detectAndSendChanges();
                return;
            }
        }
        MerchantRecipe offer = recipes.get(selection);
        if (inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(1).isEmpty()) {
            fill(menu, inventory, 0, offer.getItemToBuy());
            fill(menu, inventory, 1, offer.getSecondItemToBuy());
        }
        inventory.resetRecipeAndSlots();
        player.inventory.markDirty();
        menu.detectAndSendChanges();
    }

    private static boolean returnPayment(ContainerMerchant menu, ItemStack payment) {
        int original = payment.getCount();
        for (int pass = 0; pass < 2 && !payment.isEmpty(); pass++) {
            for (int index = 38; index >= 3 && !payment.isEmpty(); index--) {
                net.minecraft.inventory.Slot slot = menu.getSlot(index);
                ItemStack stored = slot.getStack();
                if (pass == 0 && (stored.isEmpty() || !ItemStack.areItemsEqual(stored, payment)
                        || !ItemStack.areItemStackTagsEqual(stored, payment))) continue;
                if (pass == 1 && !stored.isEmpty()) continue;
                int maximum = Math.min(payment.getMaxStackSize(), slot.getItemStackLimit(payment));
                int amount = Math.min(payment.getCount(), maximum - stored.getCount());
                if (amount <= 0) continue;
                if (stored.isEmpty()) slot.putStack(payment.splitStack(amount));
                else {
                    stored.grow(amount);
                    payment.shrink(amount);
                    slot.onSlotChanged();
                }
            }
        }
        return payment.getCount() != original;
    }

    private static void fill(ContainerMerchant menu, InventoryMerchant inventory, int slot, ItemStack cost) {
        if (cost.isEmpty()) return;
        ItemStack payment = ItemStack.EMPTY;
        int available = 0;
        int maximum = Math.min(cost.getMaxStackSize(), inventory.getInventoryStackLimit());
        for (int index = 3; index < 39 && available < maximum; index++) {
            net.minecraft.inventory.Slot source = menu.getSlot(index);
            ItemStack stack = source.getStack();
            if (!ItemStack.areItemsEqual(stack, cost)
                    || !net.minecraft.nbt.NBTUtil.areNBTEquals(cost.getTagCompound(), stack.getTagCompound(), false)
                    || !payment.isEmpty() && !ItemStack.areItemStackTagsEqual(stack, payment)) continue;
            if (payment.isEmpty()) payment = stack.copy();
            int moved = Math.min(maximum - available, stack.getCount());
            source.decrStackSize(moved);
            available += moved;
        }
        if (available > 0) {
            payment.setCount(available);
            inventory.setInventorySlotContents(slot, payment);
        }
    }
}
