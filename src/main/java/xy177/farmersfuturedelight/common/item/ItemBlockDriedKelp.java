package xy177.farmersfuturedelight.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemBlockDriedKelp extends ItemBlock {
    public ItemBlockDriedKelp(net.minecraft.block.Block block) {
        super(block);
        setMaxDamage(0);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (FFDItems.isKelpEnabled()) {
            super.getSubItems(tab, items);
        }
    }

    @Override
    public int getItemBurnTime(ItemStack stack) {
        return FFDItems.isKelpEnabled() ? 4000 : 0;
    }
}
