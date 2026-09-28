package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;

public final class BlockWaterloggedStateCarrier extends Block {
    public BlockWaterloggedStateCarrier() {
        super(Material.BARRIER);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        disableStats();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }
}
