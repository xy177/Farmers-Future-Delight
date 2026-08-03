package xy177.farmersfuturedelight.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemDriedKelp extends ItemFood {
    public ItemDriedKelp() {
        super(1, 0.3F, false);
        setMaxStackSize(64);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 16;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (FFDItems.isKelpEnabled()) {
            super.getSubItems(tab, items);
        }
    }
}
