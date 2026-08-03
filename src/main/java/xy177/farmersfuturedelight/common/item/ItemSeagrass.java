package xy177.farmersfuturedelight.common.item;

import xy177.farmersfuturedelight.common.block.BlockSeagrass;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemSeagrass extends ItemUnderwaterPlant {
    public ItemSeagrass(BlockSeagrass block) {
        super(block);
    }

    @Override
    protected boolean isFeatureEnabled() {
        return FFDItems.isSeagrassEnabled();
    }
}
