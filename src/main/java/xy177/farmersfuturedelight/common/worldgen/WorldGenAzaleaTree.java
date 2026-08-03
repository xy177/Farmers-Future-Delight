package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import xy177.farmersfuturedelight.common.block.BlockAzalea;
import xy177.farmersfuturedelight.common.block.BlockCaveVinesBase;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class WorldGenAzaleaTree extends WorldGenerator {
    public static final WorldGenAzaleaTree INSTANCE = new WorldGenAzaleaTree();

    private WorldGenAzaleaTree() {
        super(false);
    }

    @Override
    public boolean generate(World world, Random random, BlockPos pos) {
        int height = 4 + random.nextInt(3);
        if (pos.getY() < 1 || pos.getY() + height + 4 >= world.getHeight()
                || !BlockAzalea.canGrowOn(world.getBlockState(pos.down()))) {
            return false;
        }

        EnumFacing bend = EnumFacing.getHorizontal(random.nextInt(4));
        List<BlockPos> logs = new ArrayList<>();
        List<BlockPos> foliagePoints = new ArrayList<>();
        BlockPos cursor = pos;
        int logHeight = height - 1;
        for (int index = 0; index <= logHeight; index++) {
            if (index + 1 >= logHeight + random.nextInt(2)) {
                cursor = cursor.offset(bend);
            }
            logs.add(cursor);
            if (index >= 3) {
                foliagePoints.add(cursor);
            }
            cursor = cursor.up();
        }

        int bendLength = 1 + random.nextInt(2);
        for (int index = 0; index <= bendLength; index++) {
            logs.add(cursor);
            foliagePoints.add(cursor);
            cursor = cursor.offset(bend);
        }

        for (BlockPos logPos : logs) {
            if (!canReplace(world, logPos)) {
                return false;
            }
        }
        if (FFDItems.isRootedDirtEnabled()) {
            world.setBlockState(pos.down(), FFDBlocks.ROOTED_DIRT.getDefaultState(), 2);
        }
        for (BlockPos logPos : logs) {
            world.setBlockState(logPos, oakLog(), 2);
        }
        for (BlockPos foliagePoint : foliagePoints) {
            placeRandomFoliage(world, random, foliagePoint);
        }
        return true;
    }

    private static void placeRandomFoliage(World world, Random random, BlockPos origin) {
        for (int attempt = 0; attempt < 50; attempt++) {
            BlockPos leavesPos = origin.add(
                    random.nextInt(3) - random.nextInt(3),
                    random.nextInt(2) - random.nextInt(2),
                    random.nextInt(3) - random.nextInt(3));
            if (!canReplace(world, leavesPos)) {
                continue;
            }
            IBlockState leaves = random.nextInt(4) == 0
                    ? FFDBlocks.FLOWERING_AZALEA_LEAVES.getDefaultState()
                    : FFDBlocks.AZALEA_LEAVES.getDefaultState();
            world.setBlockState(leavesPos, leaves, 2);
        }
    }

    private static IBlockState oakLog() {
        return Blocks.LOG.getDefaultState()
                .withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
                .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y);
    }

    private static boolean canReplace(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return state.getMaterial() != Material.WATER
                && (world.isAirBlock(pos) || block.isReplaceable(world, pos)
                || state.getMaterial() == Material.LEAVES || block == FFDBlocks.HANGING_ROOTS
                || BlockCaveVinesBase.isCaveVine(state));
    }
}
