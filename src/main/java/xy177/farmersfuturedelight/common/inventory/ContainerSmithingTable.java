package xy177.farmersfuturedelight.common.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;

public class ContainerSmithingTable extends ContainerWorkstation {
    private final InventoryPlayer playerInventory;
    private final World world;
    private final BlockPos pos;
    private final InventoryBasic inputs = new InventoryBasic("smithing", false, 3);
    private final InventoryBasic output = new InventoryBasic("smithing_result", false, 1);

    public ContainerSmithingTable(InventoryPlayer playerInventory, World world, BlockPos pos) {
        super(4);
        this.playerInventory = playerInventory;
        this.world = world;
        this.pos = pos.toImmutable();
        for (int index = 0; index < 3; index++) {
            final int inputIndex = index;
            addSlotToContainer(new Slot(inputs, index, 8 + index * 18, 48) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return isValidInput(inputIndex, stack);
                }

                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    refreshResult();
                }
            });
        }
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
    public void detectAndSendChanges() {
        refreshResult();
        super.detectAndSendChanges();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return world.getBlockState(pos).getBlock() == FFDBlocks.SMITHING_TABLE
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.inventory != output && super.canMergeSlot(stack, slot);
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return slot.slotNumber != 3 && super.canDragIntoSlot(slot);
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
        if (index == 3) {
            if (!mergeItemStack(stack, 4, 40, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, original);
            slot.onTake(player, original);
        } else if (index < 3) {
            if (!mergeItemStack(stack, 4, 40, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (mergeInputStack(stack)) {
                slot.onSlotChanged();
            } else if (index < 31) {
                if (!mergeItemStack(stack, 31, 40, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(stack, 4, 31, false)) {
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
        if (index != 3) {
            slot.onTake(player, stack);
        }
        return original;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote) {
            clearContainer(player, player.world, inputs);
            output.clear();
        }
    }

    private boolean mergeInputStack(ItemStack stack) {
        for (int index = 0; index < inputs.getSizeInventory(); index++) {
            if (isValidInput(index, stack)
                    && mergeItemStack(stack, index, index + 1, false)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidInput(int index, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (WorkstationRecipes.Recipe recipe : WorkstationRecipes.getRecipes(
                WorkstationRecipes.Type.SMITHING)) {
            if (recipe.getIngredients().get(index).apply(stack)) {
                return true;
            }
        }
        return false;
    }

    private void refreshResult() {
        if (world.isRemote) {
            return;
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (int index = 0; index < inputs.getSizeInventory(); index++) {
            stacks.add(inputs.getStackInSlot(index));
        }
        WorkstationRecipes.Recipe recipe = WorkstationRecipes.find(
                WorkstationRecipes.Type.SMITHING, stacks);
        output.setInventorySlotContents(0,
                recipe == null ? ItemStack.EMPTY : recipe.assemble(stacks));
    }

    private final class OutputSlot extends Slot {
        private final EntityPlayer player;

        private OutputSlot(EntityPlayer player) {
            super(output, 0, 98, 48);
            this.player = player;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return !getStack().isEmpty() && canInteractWith(player);
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            ItemStack crafted = stack.copy();
            if (!player.world.isRemote) {
                for (int index = 0; index < inputs.getSizeInventory(); index++) {
                    if (!inputs.getStackInSlot(index).isEmpty()) {
                        inputs.decrStackSize(index, 1);
                    }
                }
                crafted.onCrafting(player.world, player, 1);
                net.minecraftforge.fml.common.FMLCommonHandler.instance()
                        .firePlayerCraftingEvent(this.player, crafted, inputs);
            }
            refreshResult();
            return super.onTake(player, stack);
        }
    }
}
