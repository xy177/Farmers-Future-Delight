package xy177.farmersfuturedelight.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDCreativeTab extends CreativeTabs {
    public static final FFDCreativeTab INSTANCE = new FFDCreativeTab();

    private FFDCreativeTab() {
        super(FarmerFutureDelight.MODID);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(FFDItems.isSweetBerryEnabled() ? FFDItems.SWEET_BERRIES : Items.APPLE);
    }
}
