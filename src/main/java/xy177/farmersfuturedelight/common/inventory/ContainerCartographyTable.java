package xy177.farmersfuturedelight.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.world.storage.MapData;
import xy177.farmersfuturedelight.common.tile.TileEntityCartographyTable;

public class ContainerCartographyTable extends ContainerWorkstation {
    private final TileEntityCartographyTable table;
    private final TileEntityCartographyTable source;
    private final InventoryBasic output = new InventoryBasic("cartography_result", false, 1);
    private final EntityPlayer viewer;
    private int synchronizedMap = -1;

    public ContainerCartographyTable(InventoryPlayer playerInventory,
                                    TileEntityCartographyTable table) {
        super(3);
        viewer = playerInventory.player;
        source = table;
        this.table = new TileEntityCartographyTable();
        this.table.setWorld(table.getWorld());
        this.table.setPos(table.getPos());
        addSlotToContainer(new Slot(this.table, 0, 15, 15) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return ContainerCartographyTable.this.table.isItemValidForSlot(0, stack);
            }

            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                refreshResult();
            }
        });
        addSlotToContainer(new Slot(this.table, 1, 15, 52) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return ContainerCartographyTable.this.table.isItemValidForSlot(1, stack);
            }

            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                refreshResult();
            }
        });
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

    public TileEntityCartographyTable getWorkInventory() {
        return table;
    }

    @Override
    public void detectAndSendChanges() {
        refreshResult();
        super.detectAndSendChanges();
        if (!(viewer instanceof EntityPlayerMP)) return;
        MapData data = table.getMapData();
        if (data == null) {
            synchronizedMap = -1;
            return;
        }
        ItemStack map = table.getStackInSlot(0);
        data.getMapInfo(viewer);
        Packet<?> packet = data.getMapPacket(map, table.getWorld(), viewer);
        if (synchronizedMap != map.getMetadata()) {
            packet = new SPacketMaps(map.getMetadata(), data.scale, data.trackingPosition,
                    data.mapDecorations.values(), data.colors, 0, 0, 128, 128);
            synchronizedMap = map.getMetadata();
        }
        if (packet != null) ((EntityPlayerMP) viewer).connection.sendPacket(packet);
    }

    private void refreshResult() {
        if (!table.getWorld().isRemote) {
            output.setInventorySlotContents(0, table.getResult());
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
            if (!hasRoom(stack) || !table.takeResult(stack)
                    || !mergeItemStack(stack, 3, 39, true)) {
                return ItemStack.EMPTY;
            }
            output.setInventorySlotContents(0, ItemStack.EMPTY);
            slot.onSlotChange(stack, original);
            refreshResult();
            return original;
        }
        if (index < 2) {
            if (!mergeItemStack(stack, 3, 39, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof ItemMap) {
            if (!mergeItemStack(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() == Items.PAPER || stack.getItem() == Items.MAP
                || stack.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.GLASS_PANE)) {
            if (!mergeItemStack(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 30) {
            if (!mergeItemStack(stack, 30, 39, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(stack, 3, 30, false)) {
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

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote) {
            clearContainer(player, player.world, table);
            output.clear();
        }
    }

    private boolean hasRoom(ItemStack output) {
        int required = output.getCount();
        boolean duplicate = table.getOperation() == TileEntityCartographyTable.DUPLICATE;
        for (int index = 3; index < 39; index++) {
            Slot slot = inventorySlots.get(index);
            int limit = Math.min(output.getMaxStackSize(), slot.getSlotStackLimit());
            if (slot.getStack().isEmpty()) {
                required -= limit;
            } else if (duplicate && ItemStack.areItemsEqual(slot.getStack(), output)
                    && ItemStack.areItemStackTagsEqual(slot.getStack(), output)) {
                required -= Math.max(0, limit - slot.getStack().getCount());
            }
            if (required <= 0) {
                return true;
            }
        }
        return false;
    }

    private class ResultSlot extends Slot {
        private ResultSlot() {
            super(output, 0, 145, 39);
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
                    && ItemStack.areItemStacksEqual(getStack(), table.getResult());
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            table.takeResult(stack);
            refreshResult();
            return super.onTake(player, stack);
        }
    }
}
