package xy177.farmersfuturedelight.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class ItemNetheriteBlock extends ItemBlock implements INetheriteItem {
    public ItemNetheriteBlock(Block block) {
        super(block);
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }

}
