package xy177.farmersfuturedelight.common.item;

import net.minecraft.item.Item;
import net.minecraft.tileentity.BannerPattern;

public class ItemBannerPattern extends Item {
    private final BannerPattern pattern;

    public ItemBannerPattern(BannerPattern pattern) {
        this.pattern = pattern;
        setMaxStackSize(1);
    }

    public BannerPattern getPattern() {
        return pattern;
    }
}
