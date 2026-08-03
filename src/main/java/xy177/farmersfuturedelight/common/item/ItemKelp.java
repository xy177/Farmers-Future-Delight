package xy177.farmersfuturedelight.common.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.block.BlockKelp;
import xy177.farmersfuturedelight.common.block.BlockKelpHead;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemKelp extends ItemUnderwaterPlant {
    public ItemKelp(BlockKelp block) {
        super(block);
    }

    @Override
    protected boolean isFeatureEnabled() {
        return FFDItems.isKelpEnabled();
    }

    @Override
    protected IBlockState getPlacementState(World world, BlockPos pos) {
        return BlockKelpHead.stateForAgeValue(world.rand.nextInt(25));
    }
}
