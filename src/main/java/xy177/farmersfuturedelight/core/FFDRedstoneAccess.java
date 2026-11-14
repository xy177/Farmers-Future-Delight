package xy177.farmersfuturedelight.core;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface FFDRedstoneAccess {
    IBlockState ffd$getWireState(IBlockState state, IBlockAccess world, BlockPos pos);
}
