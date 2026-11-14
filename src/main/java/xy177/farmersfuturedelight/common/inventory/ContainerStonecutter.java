package xy177.farmersfuturedelight.common.inventory;

import java.util.Collections;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.tile.TileEntityStonecutter;

public class ContainerStonecutter extends ContainerWorkstation {
    private final TileEntityStonecutter stonecutter;
    private final TileEntityStonecutter source;
    private final InventoryBasic output = new InventoryBasic("stonecutter_result", false, 1);
    private int selectedRecipeIndex = -1;
    private long lastSoundTick = Long.MIN_VALUE;

    public ContainerStonecutter(InventoryPlayer playerInventory, TileEntityStonecutter stonecutter) {
        super(2);
        source = stonecutter;
        this.stonecutter = new TileEntityStonecutter();
        this.stonecutter.setWorld(stonecutter.getWorld());
        this.stonecutter.setPos(stonecutter.getPos());
        addSlotToContainer(new Slot(this.stonecutter, 0, 20, 33) {
            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                refreshResult();
            }
        });
        addSlotToContainer(new OutputSlot(playerInventory.player));
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
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, stonecutter.getSelectedRecipeIndex());
    }

    @Override
    public void detectAndSendChanges() {
        refreshResult();
        super.detectAndSendChanges();
        int value = stonecutter.getSelectedRecipeIndex();
        if (selectedRecipeIndex != value) {
            for (IContainerListener listener : listeners) {
                listener.sendWindowProperty(this, 0, value);
            }
            selectedRecipeIndex = value;
        }
    }

    @Override
    public void updateProgressBar(int id, int value) {
        stonecutter.setField(id, value);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return source.isUsableByPlayer(player);
    }

    public TileEntityStonecutter getWorkInventory() {
        return stonecutter;
    }

    private void refreshResult() {
        if (!stonecutter.getWorld().isRemote) {
            output.setInventorySlotContents(0, stonecutter.getSelectedResult());
        }
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.inventory != output && super.canMergeSlot(stack, slot);
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int index) {
        if (!canInteractWith(player) || !stonecutter.canSelectRecipe(index)) {
            return false;
        }
        stonecutter.selectRecipe(index);
        refreshResult();
        return true;
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return slot.slotNumber != 1 && super.canDragIntoSlot(slot);
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
        if (index == 1) {
            if (!canFitOutput(stack)) {
                return ItemStack.EMPTY;
            }
            if (!mergeItemStack(stack, 2, 38, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, original);
        } else if (index == 0) {
            if (!mergeItemStack(stack, 2, 38, false)) {
                return ItemStack.EMPTY;
            }
        } else if (WorkstationRecipes.find(WorkstationRecipes.Type.STONECUTTING,
                Collections.singletonList(stack)) != null) {
            if (!mergeItemStack(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 29) {
            if (!mergeItemStack(stack, 29, 38, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(stack, 2, 29, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    private boolean canFitOutput(ItemStack output) {
        int remaining = output.getCount();
        for (int index = 2; index < 38; index++) {
            ItemStack existing = inventorySlots.get(index).getStack();
            if (existing.isEmpty()) {
                remaining -= Math.min(output.getMaxStackSize(),
                        inventorySlots.get(index).getSlotStackLimit());
            } else if (ItemStack.areItemsEqual(existing, output)
                    && ItemStack.areItemStackTagsEqual(existing, output)) {
                remaining -= Math.max(0, Math.min(existing.getMaxStackSize(),
                        inventorySlots.get(index).getSlotStackLimit()) - existing.getCount());
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
            clearContainer(player, player.world, stonecutter);
            output.clear();
        }
    }

    private class OutputSlot extends Slot {
        private final EntityPlayer player;
        private int removed;

        private OutputSlot(EntityPlayer player) {
            super(output, 0, 143, 33);
            this.player = player;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack decrStackSize(int amount) {
            removed += getStack().getCount();
            return super.decrStackSize(getStack().getCount());
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return player.world.isRemote || ItemStack.areItemStacksEqual(
                    getStack(), stonecutter.getSelectedResult()) && !getStack().isEmpty();
        }

        @Override
        protected void onCrafting(ItemStack stack, int amount) {
            removed += amount;
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            ItemStack crafted = stonecutter.getSelectedResult();
            crafted.onCrafting(player.world, player, removed);
            if (!player.world.isRemote) {
                stonecutter.consumeInput();
                FMLCommonHandler.instance().firePlayerCraftingEvent(this.player, crafted,
                        stonecutter);
                long tick = player.world.getTotalWorldTime();
                if (tick != lastSoundTick) {
                    lastSoundTick = tick;
                    player.world.playSound(null, stonecutter.getPos(),
                            xy177.farmersfuturedelight.common.registry.FFDSounds.STONECUTTER_TAKE_RESULT,
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            }
            removed = 0;
            refreshResult();
            return super.onTake(player, stack);
        }
    }
}
