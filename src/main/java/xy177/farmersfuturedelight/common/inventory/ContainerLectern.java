package xy177.farmersfuturedelight.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xy177.farmersfuturedelight.common.tile.TileEntityLectern;

public class ContainerLectern extends Container {
    private final TileEntityLectern lectern;
    private final InventoryBasic book = new InventoryBasic("lectern_book", false, 1);
    private int page;
    private int lastPage = -1;

    public ContainerLectern(TileEntityLectern lectern) {
        this.lectern = lectern;
        book.setInventorySlotContents(0, lectern.getBook().copy());
        page = lectern.getPage();
        addSlotToContainer(new Slot(book, 0, -1000, -1000) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeStack(EntityPlayer player) {
                return false;
            }
        });
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return lectern.isUsableByPlayer(player) && !lectern.getBook().isEmpty();
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int button) {
        if (!canInteractWith(player)) {
            return false;
        }
        if (button >= 100) {
            lectern.setPage(button - 100);
        } else if (button == 1 || button == 2) {
            lectern.setPage(lectern.getPage() + (button == 1 ? -1 : 1));
        } else if (button == 3 && player.capabilities.allowEdit) {
            ItemStack removed = lectern.removeBook();
            if (!removed.isEmpty() && !player.inventory.addItemStackToInventory(removed)) {
                player.dropItem(removed, false);
            }
        } else {
            return false;
        }
        detectAndSendChanges();
        return true;
    }

    public ItemStack getBook() {
        return book.getStackInSlot(0);
    }

    public int getPage() {
        return page;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, lectern.getPage());
    }

    @Override
    public void detectAndSendChanges() {
        if (!lectern.getWorld().isRemote) {
            book.setInventorySlotContents(0, lectern.getBook().copy());
            page = lectern.getPage();
        }
        super.detectAndSendChanges();
        if (lastPage != page) {
            for (IContainerListener listener : listeners) {
                listener.sendWindowProperty(this, 0, page);
            }
            lastPage = page;
        }
    }

    @Override
    public void updateProgressBar(int id, int value) {
        if (id == 0) {
            page = value;
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return ItemStack.EMPTY;
    }
}
