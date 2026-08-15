package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.worldgen.FFDLushCaveBlockProvider;

public class BlockCaveVinesPlant extends BlockCaveVinesBase {
    public BlockCaveVinesPlant() {
        super("cave_vines_plant");
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        if (!canStay(world, pos)) {
            world.destroyBlock(pos, true);
            return;
        }
        if (!isCaveVine(world.getBlockState(pos.down()))) {
            IBlockState tip = FFDLushCaveBlockProvider.get().caveVines(true, hasBerries(state));
            if (tip != null) {
                world.setBlockState(pos, tip, 2);
                if (tip.getBlock() instanceof BlockCaveVines) {
                    BlockCaveVines.setAge(world, pos, world.rand.nextInt(25));
                }
            }
        }
    }
}
