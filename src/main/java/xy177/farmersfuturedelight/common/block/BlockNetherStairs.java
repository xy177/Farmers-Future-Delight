package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.state.IBlockState;

import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockNetherStairs extends BlockFutureStairs {
    public BlockNetherStairs(String name, IBlockState modelState) {
        super(name, modelState, FFDSounds.NETHER_WOOD);
        setHardness(2.0F);
        setResistance(3.0F);
    }
}
