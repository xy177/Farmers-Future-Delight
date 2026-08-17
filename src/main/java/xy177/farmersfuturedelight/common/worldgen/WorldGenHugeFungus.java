package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.WorldGenerator;

import xy177.farmersfuturedelight.common.block.BlockNetherPlant;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public class WorldGenHugeFungus extends WorldGenerator {
    private static final float HUGE_PROBABILITY = 0.06F;
    public static final int MAX_HORIZONTAL_RADIUS = 4;
    private static final int WORLDGEN_FLAGS = 2 | 16;

    private final boolean warped;
    private final boolean planted;
    private World cachedWorld;
    private Map<Long, Chunk> cachedChunks;
    private WorldgenBlockLight cachedLight;

    public WorldGenHugeFungus(boolean warped) {
        this(warped, false);
    }

    public WorldGenHugeFungus(boolean warped, boolean planted) {
        this.warped = warped;
        this.planted = planted;
    }

    public static boolean canGenerateAt(World world, BlockPos pos, boolean warped) {
        return pos.getY() > FFDHeightHooks.minY(world)
                && pos.getY() < FFDHeightHooks.maxYExclusive(world)
                && blocks().isNylium(world.getBlockState(pos.down()), warped);
    }

    @Override
    public boolean generate(World world, Random random, BlockPos pos) {
        if (!canGenerateAt(world, pos, warped)
                || !planted && pos.getY() >= NetherForestFeatures.getNetherTopY(world)
                || blocks().stem(warped) == null
                || blocks().wart(warped) == null || blocks().shroomlight() == null) {
            return false;
        }

        int height = 4 + random.nextInt(10);
        if (random.nextInt(12) == 0) {
            height *= 2;
        }
        if (!planted && pos.getY() + height + 1
                >= NetherForestFeatures.getNetherTopY(world)) {
            return false;
        }

        boolean huge = !planted && random.nextFloat() < HUGE_PROBABILITY;
        if (world != cachedWorld || cachedChunks == null) {
            cachedWorld = world;
            cachedChunks = new HashMap<Long, Chunk>();
            cachedLight = new WorldgenBlockLight(world, cachedChunks);
        }
        if (planted) {
            world.setBlockToAir(pos);
        }
        placeStem(world, random, pos, height, huge);
        placeHat(world, random, pos, height, huge);
        return true;
    }

    private void placeStem(World world, Random random, BlockPos origin, int height, boolean huge) {
        IBlockState stem = blocks().stem(warped);
        int radius = huge ? 1 : 0;
        BlockPos.MutableBlockPos stemPos = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                boolean corner = huge && Math.abs(x) == radius && Math.abs(z) == radius;
                for (int y = 0; y < height; y++) {
                    stemPos.setPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (!isReplaceable(world, stemPos, true)
                            || corner && random.nextFloat() >= 0.1F) {
                        continue;
                    }
                    place(world, stemPos, stem);
                }
            }
        }
    }

    private void placeHat(World world, Random random, BlockPos origin, int height, boolean huge) {
        IBlockState wart = blocks().wart(warped);
        IBlockState shroomlight = blocks().shroomlight();
        boolean placeVines = !warped;
        int hatHeight = Math.min(random.nextInt(1 + height / 3) + 5, height);
        int hatStartY = height - hatHeight;
        BlockPos.MutableBlockPos hatPos = new BlockPos.MutableBlockPos();

        for (int y = hatStartY; y <= height; y++) {
            int radius = y < height - random.nextInt(3) ? 2 : 1;
            if (hatHeight > 8 && y < hatStartY + 4) {
                radius = 3;
            }
            if (huge) {
                radius++;
            }

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    hatPos.setPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (!isReplaceable(world, hatPos, false)) {
                        continue;
                    }

                    boolean edgeX = x == -radius || x == radius;
                    boolean edgeZ = z == -radius || z == radius;
                    boolean inside = !edgeX && !edgeZ && y != height;
                    boolean corner = edgeX && edgeZ;
                    boolean hatBottom = y < hatStartY + 3;
                    if (hatBottom) {
                        if (!inside) {
                            placeHatDropBlock(world, random, hatPos, wart, placeVines);
                        }
                    } else if (inside) {
                        placeHatBlock(world, random, hatPos, wart, shroomlight,
                                0.1F, 0.2F, placeVines ? 0.1F : 0.0F);
                    } else if (corner) {
                        placeHatBlock(world, random, hatPos, wart, shroomlight,
                                0.01F, 0.7F, placeVines ? 0.083F : 0.0F);
                    } else {
                        placeHatBlock(world, random, hatPos, wart, shroomlight,
                                0.0005F, 0.98F, placeVines ? 0.07F : 0.0F);
                    }
                }
            }
        }
    }

    private void placeHatBlock(World world, Random random, BlockPos pos, IBlockState wart,
                               IBlockState shroomlight, float shroomlightChance,
                               float wartChance, float vineChance) {
        if (random.nextFloat() < shroomlightChance) {
            place(world, pos, shroomlight);
        } else if (random.nextFloat() < wartChance) {
            place(world, pos, wart);
            if (random.nextFloat() < vineChance) {
                tryPlaceWeepingVines(world, random, pos);
            }
        }
    }

    private void placeHatDropBlock(World world, Random random, BlockPos pos,
                                   IBlockState wart, boolean placeVines) {
        if (getBlockState(world, pos.down()).getBlock() == wart.getBlock()) {
            place(world, pos, wart);
        } else if (random.nextFloat() < 0.15F) {
            place(world, pos, wart);
            if (placeVines && random.nextInt(11) == 0) {
                tryPlaceWeepingVines(world, random, pos);
            }
        }
    }

    private void tryPlaceWeepingVines(World world, Random random, BlockPos hatPos) {
        BlockPos start = hatPos.down();
        IBlockState startState = getBlockState(world, start);
        if (!startState.getBlock().isAir(startState, world, start)) {
            return;
        }
        int height = 1 + random.nextInt(5);
        if (random.nextInt(7) == 0) {
            height *= 2;
        }
        NetherForestFeatures.placeWeepingVinesColumn(world, random, start, height, 23, 25,
                planted ? 2 : WORLDGEN_FLAGS);
    }

    private boolean isReplaceable(World world, BlockPos pos, boolean includePlants) {
        if (FFDHeightHooks.isOutsideBuildHeight(world, pos)) {
            return false;
        }
        IBlockState state = getBlockState(world, pos);
        if (state.getBlock().isAir(state, world, pos)) {
            return true;
        }
        if (!includePlants) {
            return false;
        }
        Material material = state.getMaterial();
        return state.getBlock().isReplaceable(world, pos)
                || material == Material.PLANTS || material == Material.VINE
                || state.getBlock() instanceof BlockNetherPlant;
    }

    private IBlockState getBlockState(World world, BlockPos pos) {
        if (world != cachedWorld || cachedChunks == null) {
            return world.getBlockState(pos);
        }
        Chunk chunk = getChunk(world, pos);
        return FFDHeightHooks.getBlockState(chunk, pos.getX(), pos.getY(), pos.getZ());
    }

    private void place(World world, BlockPos pos, IBlockState state) {
        if (planted && !world.isAirBlock(pos) && !world.isAirBlock(pos.down())) {
            world.destroyBlock(pos, true);
        }
        if (planted) {
            setBlockAndNotifyAdequately(world, pos, state);
        } else {
            BlockPos immutable = pos.toImmutable();
            IBlockState oldState = getBlockState(world, immutable);
            int oldLight = oldState.getLightValue(world, immutable);
            int oldOpacity = oldState.getLightOpacity(world, immutable);
            Chunk chunk = getChunk(world, immutable);
            IBlockState replaced = chunk.setBlockState(immutable, state);
            if (replaced == null) {
                return;
            }
            if (state.getLightOpacity(world, immutable) != oldOpacity
                    || state.getLightValue(world, immutable) != oldLight) {
                cachedLight.check(immutable);
            }
            world.markAndNotifyBlock(immutable, chunk, replaced, state, WORLDGEN_FLAGS);
        }
    }

    void clearCache() {
        cachedWorld = null;
        cachedChunks = null;
        cachedLight = null;
    }

    private Chunk getChunk(World world, BlockPos pos) {
        if (world != cachedWorld || cachedChunks == null) {
            cachedWorld = world;
            cachedChunks = new HashMap<Long, Chunk>();
            cachedLight = new WorldgenBlockLight(world, cachedChunks);
        }
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        long key = ((long) chunkX & 0xffffffffL)
                | (((long) chunkZ & 0xffffffffL) << 32);
        Long boxedKey = Long.valueOf(key);
        Chunk chunk = cachedChunks.get(boxedKey);
        if (chunk == null) {
            chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
            cachedChunks.put(boxedKey, chunk);
        }
        return chunk;
    }

    private static FFDNetherBlockProvider blocks() {
        return FFDNetherBlockProvider.get();
    }
}
