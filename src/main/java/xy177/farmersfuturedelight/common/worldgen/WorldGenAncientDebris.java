package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;

public final class WorldGenAncientDebris {
    private static final int FLAGS = 2 | 16;
    private static final long LARGE_SALT = 0x4F1BBCDCBFA53E0AL;
    private static final long SMALL_SALT = 0x6A09E667F3BCC909L;

    public void generate(World world, int chunkX, int chunkZ) {
        if (world.provider.getDimension() != -1) {
            return;
        }
        Chunk targetChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        for (int sourceX = chunkX - 1; sourceX <= chunkX + 1; sourceX++) {
            for (int sourceZ = chunkZ - 1; sourceZ <= chunkZ + 1; sourceZ++) {
                generateLarge(world, targetChunk, sourceX, sourceZ);
                generateSmall(world, targetChunk, sourceX, sourceZ);
            }
        }
    }

    private static void generateLarge(World world, Chunk targetChunk, int chunkX, int chunkZ) {
        Random random = newRandom(world, chunkX, chunkZ, LARGE_SALT);
        BlockPos origin = new BlockPos(chunkX * 16 + random.nextInt(16),
                8 + random.nextInt(9) + random.nextInt(9),
                chunkZ * 16 + random.nextInt(16));
        placeScattered(world, targetChunk, random, origin, 3);
    }

    private static void generateSmall(World world, Chunk targetChunk, int chunkX, int chunkZ) {
        Random random = newRandom(world, chunkX, chunkZ, SMALL_SALT);
        int maximumY = Math.min(119, world.getActualHeight() - 9);
        if (maximumY < 8) {
            return;
        }
        BlockPos origin = new BlockPos(chunkX * 16 + random.nextInt(16),
                8 + random.nextInt(maximumY - 7),
                chunkZ * 16 + random.nextInt(16));
        placeScattered(world, targetChunk, random, origin, 2);
    }

    private static Random newRandom(World world, int chunkX, int chunkZ, long salt) {
        return new Random(world.getSeed() ^ ((long) chunkX * 341873128712L)
                ^ ((long) chunkZ * 132897987541L) ^ salt);
    }

    private static void placeScattered(World world, Chunk targetChunk, Random random,
                                       BlockPos origin, int size) {
        int placements = random.nextInt(size + 1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int index = 0; index < placements; index++) {
            int offsetX = Math.round((random.nextFloat() - random.nextFloat()) * index);
            int offsetY = Math.round((random.nextFloat() - random.nextFloat()) * index);
            int offsetZ = Math.round((random.nextFloat() - random.nextFloat()) * index);
            pos.setPos(origin.getX() + offsetX, origin.getY() + offsetY,
                    origin.getZ() + offsetZ);
            if ((pos.getX() >> 4) != targetChunk.x || (pos.getZ() >> 4) != targetChunk.z) {
                continue;
            }
            IBlockState state = targetChunk.getBlockState(pos);
            if (isNetherBaseStone(state.getBlock()) && !isAirExposed(world, pos)) {
                world.setBlockState(pos, FFDBlocks.ANCIENT_DEBRIS.getDefaultState(), FLAGS);
            }
        }
    }

    private static boolean isNetherBaseStone(Block block) {
        if (block.getRegistryName() == null) {
            return false;
        }
        if (!"minecraft".equals(block.getRegistryName().getResourceDomain())) {
            return false;
        }
        String path = block.getRegistryName().getResourcePath();
        return "netherrack".equals(path) || "basalt".equals(path)
                || "blackstone".equals(path);
    }

    private static boolean isAirExposed(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);
            if (!world.isBlockLoaded(neighbor, false)) {
                continue;
            }
            IBlockState state = world.getBlockState(neighbor);
            if (state.getBlock().isAir(state, world, neighbor)) {
                return true;
            }
        }
        return false;
    }
}
