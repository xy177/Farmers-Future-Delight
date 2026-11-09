package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockNetherrack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;

public final class WorldGenBlackstoneOre {
    private static final int COUNT = 2;
    private static final int SIZE = 33;
    private static final int MIN_Y = 5;
    private static final int MAX_Y = 31;

    public void generate(World world, Random random, int chunkX, int chunkZ) {
        IBlockState target = FFDBlocks.BLACKSTONE.getDefaultState();
        WorldGenMinable generator = new WorldGenMinable(target, SIZE,
                state -> state.getBlock() == Blocks.NETHERRACK
                        || state.getBlock() instanceof BlockNetherrack);
        for (int attempt = 0; attempt < COUNT; attempt++) {
            int x = chunkX * 16 + random.nextInt(16);
            int y = MIN_Y + random.nextInt(MAX_Y - MIN_Y + 1);
            int z = chunkZ * 16 + random.nextInt(16);
            generator.generate(world, random, new BlockPos(x, y, z));
        }
    }
}
