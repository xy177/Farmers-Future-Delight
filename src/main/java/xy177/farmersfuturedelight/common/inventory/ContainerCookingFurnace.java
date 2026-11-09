package xy177.farmersfuturedelight.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import xy177.farmersfuturedelight.common.tile.TileEntityCookingFurnace;

public class ContainerCookingFurnace extends Container {
    private final TileEntityCookingFurnace furnace;
    private final int[] fields = new int[4];

    public ContainerCookingFurnace(InventoryPlayer playerInventory, TileEntityCookingFurnace furnace) {
        this.furnace = furnace;
        addSlotToContainer(new Slot(furnace, 0, 56, 17));
        addSlotToContainer(new SlotFurnaceFuel(furnace, 1, 56, 53));
        addSlotToContainer(new OutputSlot(playerInventory.player, furnace));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    public TileEntityCookingFurnace getFurnace() {
        return furnace;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, furnace);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int index = 0; index < fields.length; index++) {
            int value = furnace.getField(index);
            if (fields[index] != value) {
                for (IContainerListener listener : listeners) {
                    listener.sendWindowProperty(this, index, value);
                }
                fields[index] = value;
            }
        }
    }

    @Override
    public void updateProgressBar(int index, int value) {
        furnace.setField(index, value);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return furnace.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (index == 2) {
            if (!mergeItemStack(stack, 3, 39, true)) return ItemStack.EMPTY;
            slot.onSlotChange(stack, original);
        } else if (index < 3) {
            if (!mergeItemStack(stack, 3, 39, false)) return ItemStack.EMPTY;
        } else if (furnace.getRecipe(stack) != null) {
            if (!mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (TileEntityCookingFurnace.getFuelTime(stack) > 0) {
            if (!mergeItemStack(stack, 1, 2, false)) return ItemStack.EMPTY;
        } else if (index < 30) {
            if (!mergeItemStack(stack, 30, 39, false)) return ItemStack.EMPTY;
        } else if (!mergeItemStack(stack, 3, 30, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    private static class OutputSlot extends Slot {
        private final EntityPlayer player;
        private final TileEntityCookingFurnace furnace;
        private int removed;

        private OutputSlot(EntityPlayer player, TileEntityCookingFurnace furnace) {
            super(furnace, 2, 116, 35);
            this.player = player;
            this.furnace = furnace;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack decrStackSize(int amount) {
            removed += Math.min(amount, getStack().getCount());
            return super.decrStackSize(amount);
        }

        @Override
        protected void onCrafting(ItemStack stack, int amount) {
            removed += amount;
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            stack.onCrafting(player.world, player, removed);
            if (!player.world.isRemote) {
                if (player instanceof EntityPlayerMP) furnace.grantUsedRecipes((EntityPlayerMP) player);
                furnace.releaseExperience(player.posX, player.posY + 0.5D, player.posZ + 0.5D);
                FMLCommonHandler.instance().firePlayerSmeltedEvent(this.player, stack);
            }
            removed = 0;
            return super.onTake(player, stack);
        }
    }
}
