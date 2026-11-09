package xy177.farmersfuturedelight.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;

public abstract class ContainerWorkstation extends Container {
    private final int workstationSlots;

    protected ContainerWorkstation(int workstationSlots) {
        this.workstationSlots = workstationSlots;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        ItemStack result = super.slotClick(slotId, dragType, clickType, player);
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
            for (int index = 0; index < workstationSlots; index++) {
                serverPlayer.connection.sendPacket(new SPacketSetSlot(windowId, index,
                        inventorySlots.get(index).getStack()));
            }
        }
        return result;
    }
}
