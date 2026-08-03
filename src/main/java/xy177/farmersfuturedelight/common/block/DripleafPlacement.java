package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.registry.FFDBlocks;

public final class DripleafPlacement {
    private DripleafPlacement() {
    }

    public static boolean canReplace(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return world.isAirBlock(pos) || WaterloggedPlantFluid.isSourceWater(state)
                || state.getBlock() == FFDBlocks.SMALL_DRIPLEAF;
    }

    public static boolean isSmallDripleafGround(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.CLAY || block == FFDBlocks.MOSS_BLOCK;
    }

    public static boolean isBigDripleafGround(IBlockState state) {
        Block block = state.getBlock();
        return isSmallDripleafGround(state) || block == Blocks.GRASS || block == Blocks.DIRT
                || block == Blocks.MYCELIUM || block == Blocks.FARMLAND
                || block == FFDBlocks.ROOTED_DIRT;
    }

    public static boolean canPlaceHead(World world, BlockPos pos) {
        IBlockState below = world.getBlockState(pos.down());
        return canReplace(world, pos) && (BlockBigDripleaf.isBigDripleaf(below)
                || below.getBlock() == FFDBlocks.BIG_DRIPLEAF_STEM
                || isBigDripleafGround(below));
    }

    public static boolean placeHead(World world, BlockPos pos, EnumFacing facing) {
        if (!canPlaceHead(world, pos)) {
            return false;
        }
        return world.setBlockState(pos, headState(world, pos, facing), 3);
    }

    public static boolean canPlaceBig(World world, BlockPos pos, int stemHeight) {
        if (stemHeight < 0 || !isBigDripleafGround(world.getBlockState(pos.down()))) {
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
        if (!isBigDripleafGround(world.getBlockState(pos.down()))) {
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
        if (!BlockBigDripleaf.isBigDripleaf(world.getBlockState(headPos))
                || !canReplace(world, headPos.up())) {
            return false;
        }
        IBlockState stem = stemState(world, headPos, facing);
        IBlockState head = headState(world, headPos.up(), facing);
        world.setBlockState(headPos, stem, 2);
        world.setBlockState(headPos.up(), head, 3);
        return true;
    }

    public static IBlockState stemState(World world, BlockPos pos, EnumFacing facing) {
        return FFDBlocks.BIG_DRIPLEAF_STEM.getDefaultState()
                .withProperty(BlockBigDripleafStem.FACING, facing)
                .withProperty(BlockBigDripleafStem.WATERLOGGED,
                        WaterloggedPlantFluid.isSourceWater(world, pos));
    }

    public static IBlockState headState(World world, BlockPos pos, EnumFacing facing) {
        BlockBigDripleaf block = WaterloggedPlantFluid.isSourceWater(world, pos)
                ? FFDBlocks.BIG_DRIPLEAF_WATERLOGGED : FFDBlocks.BIG_DRIPLEAF;
        return block.getDefaultState().withProperty(BlockBigDripleaf.FACING, facing)
                .withProperty(BlockBigDripleaf.TILT, DripleafTilt.NONE);
    }
}
