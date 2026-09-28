package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.worldgen.FFDLushCaveBlockProvider;

public final class DripleafPlacement {
    private DripleafPlacement() {
    }

    public static boolean canReplace(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return world.isAirBlock(pos) || WaterloggedBlockApi.containsWater(state)
                || FFDLushCaveBlockProvider.get().isSmallDripleaf(state);
    }

    public static boolean isSmallDripleafGround(IBlockState state) {
        return state.getBlock() == Blocks.CLAY
                || FFDLushCaveBlockProvider.get().isMossBlock(state);
    }

    public static boolean isBigDripleafGround(IBlockState state) {
        Block block = state.getBlock();
        return isSmallDripleafGround(state) || block == Blocks.GRASS || block == Blocks.DIRT
                || block == Blocks.MYCELIUM || block == Blocks.FARMLAND
                || FFDLushCaveBlockProvider.get().isRootedDirt(state);
    }

    public static boolean canPlaceHead(World world, BlockPos pos) {
        IBlockState below = world.getBlockState(pos.down());
        return headState(world, pos, EnumFacing.NORTH) != null && canReplace(world, pos)
                && (FFDLushCaveBlockProvider.get().isBigDripleaf(below)
                || FFDLushCaveBlockProvider.get().isBigDripleafStem(below)
                || isBigDripleafGround(below));
    }

    public static boolean placeHead(World world, BlockPos pos, EnumFacing facing) {
        if (!canPlaceHead(world, pos)) {
            return false;
        }
        return world.setBlockState(pos, headState(world, pos, facing), 3);
    }

    public static boolean canPlaceBig(World world, BlockPos pos, int stemHeight) {
        if (stemHeight < 0 || headState(world, pos.up(stemHeight), EnumFacing.NORTH) == null
                || stemHeight > 0 && stemState(world, pos, EnumFacing.NORTH) == null
                || !isBigDripleafGround(world.getBlockState(pos.down()))) {
            return false;
        }
        for (int y = 0; y <= stemHeight; y++) {
            if (!canReplace(world, pos.up(y))) {
                return false;
            }
        }
        return true;
    }

    public static boolean placeBig(World world, BlockPos pos, EnumFacing facing, int stemHeight) {
        if (!canPlaceBig(world, pos, stemHeight)) {
            return false;
        }
        for (int y = 0; y < stemHeight; y++) {
            BlockPos stemPos = pos.up(y);
            world.setBlockState(stemPos, stemState(world, stemPos, facing), 3);
        }
        BlockPos headPos = pos.up(stemHeight);
        world.setBlockState(headPos, headState(world, headPos, facing), 3);
        return true;
    }

    public static boolean placeWithRandomHeight(World world, Random random, BlockPos pos,
                                                EnumFacing facing) {
        if (headState(world, pos, facing) == null || stemState(world, pos, facing) == null
                || !isBigDripleafGround(world.getBlockState(pos.down()))) {
            return false;
        }
        int desiredHeight = 2 + random.nextInt(4);
        int height = 0;
        while (height < desiredHeight && canReplace(world, pos.up(height))) {
            height++;
        }
        if (height == 0) {
            return false;
        }
        for (int y = 0; y < height - 1; y++) {
            BlockPos stemPos = pos.up(y);
            world.setBlockState(stemPos, stemState(world, stemPos, facing), 3);
        }
        BlockPos headPos = pos.up(height - 1);
        world.setBlockState(headPos, headState(world, headPos, facing), 3);
        return true;
    }

    public static boolean growHead(World world, BlockPos headPos, EnumFacing facing) {
        if (!FFDLushCaveBlockProvider.get().isBigDripleaf(world.getBlockState(headPos))
                || !canReplace(world, headPos.up())) {
            return false;
        }
        IBlockState stem = stemState(world, headPos, facing);
        IBlockState head = headState(world, headPos.up(), facing);
        if (stem == null || head == null) {
            return false;
        }
        world.setBlockState(headPos, stem, 2);
        world.setBlockState(headPos.up(), head, 3);
        return true;
    }

    public static IBlockState stemState(World world, BlockPos pos, EnumFacing facing) {
        return FFDLushCaveBlockProvider.get().bigDripleafStem(facing,
                WaterloggedBlockApi.containsWater(world, pos));
    }

    public static IBlockState headState(World world, BlockPos pos, EnumFacing facing) {
        return FFDLushCaveBlockProvider.get().bigDripleaf(facing,
                WaterloggedBlockApi.containsWater(world, pos));
    }
}
