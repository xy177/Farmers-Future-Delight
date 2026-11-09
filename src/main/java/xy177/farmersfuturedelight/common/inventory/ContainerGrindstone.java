package xy177.farmersfuturedelight.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xy177.farmersfuturedelight.common.tile.TileEntityGrindstone;

public class ContainerGrindstone extends ContainerWorkstation {
    private final TileEntityGrindstone grindstone;
    private final TileEntityGrindstone source;
    private final InventoryBasic output = new InventoryBasic("grindstone_result", false, 1);

    public ContainerGrindstone(InventoryPlayer playerInventory,
                               TileEntityGrindstone grindstone) {
        super(3);
        source = grindstone;
        this.grindstone = new TileEntityGrindstone();
        this.grindstone.setWorld(grindstone.getWorld());
        this.grindstone.setPos(grindstone.getPos());
        addSlotToContainer(new InputSlot(this.grindstone, 0, 49, 19));
        addSlotToContainer(new InputSlot(this.grindstone, 1, 49, 40));
        addSlotToContainer(new ResultSlot());
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

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return source.isUsableByPlayer(player);
    }

    public TileEntityGrindstone getWorkInventory() {
        return grindstone;
    }

    @Override
    public void detectAndSendChanges() {
        refreshResult();
        super.detectAndSendChanges();
    }

    private void refreshResult() {
        if (!grindstone.getWorld().isRemote) {
            output.setInventorySlotContents(0, grindstone.getResult());
        }
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.inventory != output && super.canMergeSlot(stack, slot);
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return slot.slotNumber != 2 && super.canDragIntoSlot(slot);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (index == 2) {
            if (!canFitOutput(stack) || !mergeItemStack(stack, 3, 39, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, original);
            slot.onTake(player, stack);
        } else if (index < 3) {
            if (!mergeItemStack(stack, 3, 39, true)) {
                return ItemStack.EMPTY;
            }
        } else if (grindstone.isItemValidForSlot(0, stack)
                && !mergeItemStack(stack, 0, 2, false)) {
            return ItemStack.EMPTY;
        } else if (!grindstone.isItemValidForSlot(0, stack)) {
            if (index < 30) {
                if (!mergeItemStack(stack, 30, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(stack, 3, 30, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        if (index != 2) {
            slot.onTake(player, stack);
        }
        return original;
    }

    private boolean canFitOutput(ItemStack stack) {
        int remaining = stack.getCount();
        for (int index = 3; index < inventorySlots.size(); index++) {
            Slot slot = inventorySlots.get(index);
            ItemStack existing = slot.getStack();
            int limit = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
            if (existing.isEmpty()) {
                remaining -= limit;
            } else if (ItemStack.areItemsEqual(existing, stack)
                    && ItemStack.areItemStackTagsEqual(existing, stack)) {
                remaining -= Math.max(0, limit - existing.getCount());
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote) {
            clearContainer(player, player.world, grindstone);
            output.clear();
        }
    }

    private class InputSlot extends Slot {
        private final TileEntityGrindstone grindstone;

        private InputSlot(TileEntityGrindstone grindstone, int index, int x, int y) {
            super(grindstone, index, x, y);
            this.grindstone = grindstone;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return grindstone.isItemValidForSlot(getSlotIndex(), stack);
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            refreshResult();
        }
    }

    private class ResultSlot extends Slot {
        private ResultSlot() {
            super(output, 0, 129, 34);
        }

        @Override
        public ItemStack decrStackSize(int amount) {
            return super.decrStackSize(getStack().getCount());
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return player.world.isRemote || !getStack().isEmpty()
                    && ItemStack.areItemStacksEqual(getStack(), grindstone.getResult());
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            grindstone.takeResult(player);
            refreshResult();
            return super.onTake(player, stack);
        }
    }
}
