package xy177.farmersfuturedelight.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDCreativeTab extends CreativeTabs {
    public static final FFDCreativeTab INSTANCE = new FFDCreativeTab();

    private FFDCreativeTab() {
        super(FarmerFutureDelight.MODID);
    }

    @Override
    public ItemStack getTabIconItem() {
        ItemStack berries = FFDItems.effectiveStack(FFDItems.SWEET_BERRIES);
        return berries.isEmpty() ? new ItemStack(Items.APPLE) : berries;
    }

    @Override
    public void displayAllRelevantItems(NonNullList<ItemStack> items) {
        super.displayAllRelevantItems(items);
        items.removeIf(stack -> !FFDItems.shouldDisplayInCreativeTab(stack));
    }
}
