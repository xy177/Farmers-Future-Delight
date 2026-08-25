package xy177.farmersfuturedelight.core;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FFDWeather2CompatHooks {
    private FFDWeather2CompatHooks() {
    }

    public static IBlockState getLoadedBlockState(World world, BlockPos pos) {
        if (!FFDHeightHooks.isExtended(world) || world.isBlockLoaded(pos, false)) {
            return world.getBlockState(pos);
        }
        return Blocks.AIR.getDefaultState();
    }
}
