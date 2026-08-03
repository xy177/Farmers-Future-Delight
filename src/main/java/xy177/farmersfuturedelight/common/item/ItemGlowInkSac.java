package xy177.farmersfuturedelight.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import xy177.farmersfuturedelight.common.registry.FFDEntities;

public class ItemGlowInkSac extends Item {
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (FFDEntities.isGlowSquidEnabled()) {
            super.getSubItems(tab, items);
        }
    }
}
