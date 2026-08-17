package xy177.farmersfuturedelight.common.worldgen;

import java.util.Arrays;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Loader;

import xy177.farmersfuturedelight.core.FFDHeightHooks;

final class WorldgenBlockLight {
    private static final EnumFacing[] FACINGS = EnumFacing.values();
    private static final int DEFAULT_LIGHT = EnumSkyBlock.BLOCK.defaultLightValue;

    private final World world;
    private final Map<Long, Chunk> chunks;
    private final int[] updates = new int[32768];
    private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos clamped = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
    private final int minY;
    private final int maxY;
    private final boolean externalLighting;
    private int[] knownChunkX = new int[32];
    private int[] knownChunkZ = new int[32];
    private int[] knownVersion = new int[32];
    private boolean[] knownLoaded = new boolean[32];
    private Chunk[] knownChunks = new Chunk[32];
    private int knownCount;
    private int chunkVersion;
    private int lastChunkX = Integer.MIN_VALUE;
    private int lastChunkZ = Integer.MIN_VALUE;
    private Chunk lastChunk;

    WorldgenBlockLight(World world, Map<Long, Chunk> chunks) {
        this.world = world;
        this.chunks = chunks;
        this.minY = FFDHeightHooks.minY(world);
        this.maxY = FFDHeightHooks.maxYExclusive(world);
        this.externalLighting = Loader.isModLoaded("alfheim");
    }

    boolean check(BlockPos pos) {
        if (externalLighting || world.provider.hasSkyLight()) {
            return world.checkLight(pos);
        }
        if (!isAreaLoaded(pos, 16)) {
            return false;
        }

        int updateRange = isAreaLoaded(pos, 18) ? 17 : 15;
        int checked = 0;
        int count = 0;
        int stored = getLight(world, pos);
        int raw = getRawLight(world, pos);
        int originX = pos.getX();
        int originY = pos.getY();
        int originZ = pos.getZ();

        if (raw > stored) {
            updates[count++] = 133152;
        } else if (raw < stored) {
            updates[count++] = 133152 | stored << 18;

            while (checked < count) {
                int entry = updates[checked++];
                int x = (entry & 63) - 32 + originX;
                int y = (entry >> 6 & 63) - 32 + originY;
                int z = (entry >> 12 & 63) - 32 + originZ;
                int expected = entry >> 18 & 15;
                mutable.setPos(x, y, z);
                int light = getLight(world, mutable);

                if (light == expected) {
                    setLight(world, mutable, 0);

                    if (expected > 0
                            && MathHelper.abs(x - originX)
                            + MathHelper.abs(y - originY)
                            + MathHelper.abs(z - originZ) < updateRange) {
                        for (EnumFacing facing : FACINGS) {
                            int nextX = x + facing.getFrontOffsetX();
                            int nextY = y + facing.getFrontOffsetY();
                            int nextZ = z + facing.getFrontOffsetZ();
                            mutable.setPos(nextX, nextY, nextZ);
                            IBlockState state = getBlockState(world, mutable);
                            int opacity = Math.max(1,
                                    state.getBlock().getLightOpacity(state, world, mutable));
                            light = getLight(world, mutable);

                            if (light == expected - opacity && count < updates.length) {
                                updates[count++] = nextX - originX + 32
                                        | nextY - originY + 32 << 6
                                        | nextZ - originZ + 32 << 12
                                        | expected - opacity << 18;
                            }
                        }
                    }
                }
            }

            checked = 0;
        }

        while (checked < count) {
            int entry = updates[checked++];
            int x = (entry & 63) - 32 + originX;
            int y = (entry >> 6 & 63) - 32 + originY;
            int z = (entry >> 12 & 63) - 32 + originZ;
            mutable.setPos(x, y, z);
            int oldLight = getLight(world, mutable);
            int newLight = getRawLight(world, mutable);

            if (newLight != oldLight) {
                setLight(world, mutable, newLight);

                if (newLight > oldLight
                        && Math.abs(x - originX) + Math.abs(y - originY)
                        + Math.abs(z - originZ) < updateRange
                        && count < updates.length - 6) {
                    mutable.setPos(x - 1, y, z);
                    if (getLight(world, mutable) < newLight) {
                        updates[count++] = x - 1 - originX + 32
                                + (y - originY + 32 << 6)
                                + (z - originZ + 32 << 12);
                    }
                    mutable.setPos(x + 1, y, z);
                    if (getLight(world, mutable) < newLight) {
                        updates[count++] = x + 1 - originX + 32
                                + (y - originY + 32 << 6)
                                + (z - originZ + 32 << 12);
                    }
                    mutable.setPos(x, y - 1, z);
                    if (getLight(world, mutable) < newLight) {
                        updates[count++] = x - originX + 32
                                + (y - 1 - originY + 32 << 6)
                                + (z - originZ + 32 << 12);
                    }
                    mutable.setPos(x, y + 1, z);
                    if (getLight(world, mutable) < newLight) {
                        updates[count++] = x - originX + 32
                                + (y + 1 - originY + 32 << 6)
                                + (z - originZ + 32 << 12);
                    }
                    mutable.setPos(x, y, z - 1);
                    if (getLight(world, mutable) < newLight) {
                        updates[count++] = x - originX + 32
                                + (y - originY + 32 << 6)
                                + (z - 1 - originZ + 32 << 12);
                    }
                    mutable.setPos(x, y, z + 1);
                    if (getLight(world, mutable) < newLight) {
                        updates[count++] = x - originX + 32
                                + (y - originY + 32 << 6)
                                + (z + 1 - originZ + 32 << 12);
                    }
                }
            }
        }

        return true;
    }

    private int getRawLight(World world, BlockPos pos) {
        IBlockState state = getBlockState(world, pos);
        int light = state.getBlock().getLightValue(state, world, pos);
        int opacity = state.getBlock().getLightOpacity(state, world, pos);
        if (opacity < 1) {
            opacity = 1;
        }
        if (opacity >= 15 || light >= 14) {
            return light;
        }

        for (EnumFacing facing : FACINGS) {
            neighbor.setPos(pos).move(facing);
            int neighboring = getLight(world, neighbor) - opacity;
            if (neighboring > light) {
                light = neighboring;
            }
            if (light >= 14) {
                return light;
            }
        }
        return light;
    }

    private IBlockState getBlockState(World world, BlockPos pos) {
        if (FFDHeightHooks.isOutsideBuildHeight(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        Chunk chunk = getChunk(world, pos);
        return FFDHeightHooks.getBlockState(chunk, pos.getX(), pos.getY(), pos.getZ());
    }

    private int getLight(World world, BlockPos pos) {
        if (pos.getY() < 0) {
            clamped.setPos(pos.getX(), 0, pos.getZ());
            pos = clamped;
        }
        if (!isValid(pos)) {
            return DEFAULT_LIGHT;
        }
        return getChunk(world, pos).getLightFor(EnumSkyBlock.BLOCK, pos);
    }

    private void setLight(World world, BlockPos pos, int value) {
        if (isValid(pos)) {
            getChunk(world, pos).setLightFor(EnumSkyBlock.BLOCK, pos, value);
            world.notifyLightSet(pos);
        }
    }

    private boolean isValid(BlockPos pos) {
        return pos.getY() >= minY && pos.getY() < maxY
                && pos.getX() >= -30000000 && pos.getZ() >= -30000000
                && pos.getX() < 30000000 && pos.getZ() < 30000000;
    }

    private boolean isAreaLoaded(BlockPos pos, int radius) {
        if (pos.getY() + radius < minY || pos.getY() - radius >= maxY) {
            return false;
        }
        int minChunkX = pos.getX() - radius >> 4;
        int maxChunkX = pos.getX() + radius >> 4;
        int minChunkZ = pos.getZ() - radius >> 4;
        int maxChunkZ = pos.getZ() + radius >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!isChunkLoaded(chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isChunkLoaded(int chunkX, int chunkZ) {
        int index = findChunk(chunkX, chunkZ);
        if (index >= 0 && knownLoaded[index]) {
            return true;
        }
        if (index >= 0 && knownVersion[index] == chunkVersion) {
            long key = (chunkX & 0xffffffffL) | ((long) chunkZ << 32);
            Chunk chunk = chunks.get(Long.valueOf(key));
            if (chunk == null) {
                return false;
            }
            knownLoaded[index] = true;
            knownChunks[index] = chunk;
            return true;
        }
        probe.setPos(chunkX << 4, minY, chunkZ << 4);
        boolean loaded = world.isBlockLoaded(probe, false);
        index = index >= 0 ? index : addChunk(chunkX, chunkZ);
        knownLoaded[index] = loaded;
        knownVersion[index] = chunkVersion;
        return loaded;
    }

    private Chunk getChunk(World world, BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        if (chunkX == lastChunkX && chunkZ == lastChunkZ) {
            return lastChunk;
        }
        int index = findChunk(chunkX, chunkZ);
        if (index >= 0 && knownChunks[index] != null) {
            lastChunkX = chunkX;
            lastChunkZ = chunkZ;
            lastChunk = knownChunks[index];
            return lastChunk;
        }
        long key = (chunkX & 0xffffffffL) | ((long) chunkZ << 32);
        Long boxedKey = Long.valueOf(key);
        Chunk chunk = chunks.get(boxedKey);
        if (chunk == null) {
            chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
            chunks.put(boxedKey, chunk);
            chunkVersion++;
        }
        index = index >= 0 ? index : addChunk(chunkX, chunkZ);
        knownLoaded[index] = true;
        knownVersion[index] = chunkVersion;
        knownChunks[index] = chunk;
        lastChunkX = chunkX;
        lastChunkZ = chunkZ;
        lastChunk = chunk;
        return chunk;
    }

    private int findChunk(int chunkX, int chunkZ) {
        for (int i = 0; i < knownCount; i++) {
            if (knownChunkX[i] == chunkX && knownChunkZ[i] == chunkZ) {
                return i;
            }
        }
        return -1;
    }

    private int addChunk(int chunkX, int chunkZ) {
        if (knownCount == knownChunkX.length) {
            int length = knownCount << 1;
            knownChunkX = Arrays.copyOf(knownChunkX, length);
            knownChunkZ = Arrays.copyOf(knownChunkZ, length);
            knownVersion = Arrays.copyOf(knownVersion, length);
            knownLoaded = Arrays.copyOf(knownLoaded, length);
            knownChunks = Arrays.copyOf(knownChunks, length);
        }
        int index = knownCount++;
        knownChunkX[index] = chunkX;
        knownChunkZ[index] = chunkZ;
        return index;
    }
}
