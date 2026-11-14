package xy177.farmersfuturedelight.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class ItemNetheriteComponent extends Item implements INetheriteItem {
    public ItemNetheriteComponent() {
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    @Override
    public boolean isBeaconPayment(ItemStack stack) {
        return stack != null && stack.getItem() == FFDItems.NETHERITE_INGOT;
    }

}
