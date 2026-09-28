package xy177.farmersfuturedelight.api;

import net.minecraft.block.state.IBlockState;

public interface IWaterloggableBlock {
    boolean isWaterloggedState(IBlockState state);

    IBlockState setWaterloggedState(IBlockState state, boolean waterlogged);
}
