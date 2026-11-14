package xy177.farmersfuturedelight.common.world.modern;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class ModernLightAccess implements AutoCloseable {
    private static final ThreadLocal<ModernLightAccess> CURRENT = new ThreadLocal<>();
    private static final int[] NEIGHBOR_X = {0, 0, 0, 0, -1, 1};
    private static final int[] NEIGHBOR_Y = {-1, 1, 0, 0, 0, 0};
    private static final int[] NEIGHBOR_Z = {0, 0, -1, 1, 0, 0};
    private final ModernLightAccess previous;
    private final WorldServer world;
    private final int minimumY;
    private final int maximumY;
    private final int[] sectionIndices;
    private final int originX;
    private final int originZ;
    private final Chunk[] chunks = new Chunk[25];
    private int loadedMask;

    private ModernLightAccess(Chunk center) {
        previous = CURRENT.get();
        world = (WorldServer) center.getWorld();
        minimumY = FFDHeightHooks.minY(world);
        maximumY = FFDHeightHooks.maxYExclusive(world);
        sectionIndices = new int[(maximumY - minimumY) >> 4];
        for (int section = 0; section < sectionIndices.length; section++) {
            sectionIndices[section] = FFDHeightHooks.storageIndex(minimumY + (section << 4), world);
        }
        originX = center.x - 2;
        originZ = center.z - 2;
        for (int offsetZ = 0; offsetZ < 5; offsetZ++) {
            for (int offsetX = 0; offsetX < 5; offsetX++) {
                chunks[offsetZ * 5 + offsetX] = world.getChunkProvider().id2ChunkMap.get(
                        ChunkPos.asLong(originX + offsetX, originZ + offsetZ));
                if (chunks[offsetZ * 5 + offsetX] != null) loadedMask |= 1 << (offsetZ * 5 + offsetX);
            }
        }
        CURRENT.set(this);
    }

    public static boolean isOpenFor(World world) {
        ModernLightAccess access = CURRENT.get();
        return access != null && access.world == world;
    }

    public static ModernLightAccess open(Chunk chunk) {
        World world = chunk.getWorld();
        return world.getClass() == WorldServer.class
                && ((WorldServer) world).getChunkProvider().getClass() == ChunkProviderServer.class
                && ModernGenerationLifecycle.isActive(world)
                ? new ModernLightAccess(chunk) : null;
    }

    public static int areaLoaded(World world, int startX, int startY, int startZ,
                                 int endX, int endY, int endZ) {
        ModernLightAccess access = CURRENT.get();
        if (access == null || access.world != world) return -1;
        if (endY < access.minimumY || startY >= access.maximumY) return 0;
        int firstX = startX >> 4;
        int firstZ = startZ >> 4;
        int lastX = endX >> 4;
        int lastZ = endZ >> 4;
        if (firstX < access.originX || firstZ < access.originZ
                || lastX >= access.originX + 5 || lastZ >= access.originZ + 5) return -1;
        if (access.loadedMask == 0x1ffffff) return 1;
        for (int chunkX = firstX; chunkX <= lastX; chunkX++) {
            for (int chunkZ = firstZ; chunkZ <= lastZ; chunkZ++) {
                if (access.chunk(chunkX, chunkZ) == null) return 0;
            }
        }
        return 1;
    }

    public static int light(World world, EnumSkyBlock type, BlockPos pos) {
        ModernLightAccess access = CURRENT.get();
        if (access == null || access.world != world) return -1;
        return access.lightAt(type, pos.getX(), pos.getY(), pos.getZ());
    }

    public static int neighborLight(World world, BlockPos pos, EnumSkyBlock type, int value, int opacity) {
        ModernLightAccess access = CURRENT.get();
        if (access == null || access.world != world) return -1;
        int originX = pos.getX();
        int originY = pos.getY();
        int originZ = pos.getZ();
        for (int direction = 0; direction < NEIGHBOR_X.length; direction++) {
            int blockX = originX + NEIGHBOR_X[direction];
            int blockY = originY + NEIGHBOR_Y[direction];
            int blockZ = originZ + NEIGHBOR_Z[direction];
            int light = access.lightAt(type, blockX, blockY, blockZ);
            if (light < 0) light = world.getLightFor(type, new BlockPos(blockX, blockY, blockZ));
            value = Math.max(value, light - opacity);
            if (value >= 14) return value;
        }
        return value;
    }

    private int lightAt(EnumSkyBlock type, int blockX, int blockY, int blockZ) {
        int height = Math.max(blockY, minimumY);
        if (height >= maximumY || blockX < -30000000 || blockZ < -30000000
                || blockX >= 30000000 || blockZ >= 30000000) return type.defaultLightValue;
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        if (chunkX < originX || chunkZ < originZ
                || chunkX >= originX + 5 || chunkZ >= originZ + 5) return -1;
        Chunk chunk = chunk(chunkX, chunkZ);
        if (chunk == null) return type.defaultLightValue;
        if (chunk.getClass() != Chunk.class) return -1;
        if (type == EnumSkyBlock.SKY && !world.provider.hasSkyLight()) return 0;
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        int index = sectionIndices[(height - minimumY) >> 4];
        if (index < 0 || index >= storage.length) return type.defaultLightValue;
        ExtendedBlockStorage section = storage[index];
        if (section == Chunk.NULL_BLOCK_STORAGE) {
            return height >= chunk.getHeightValue(blockX & 15, blockZ & 15) ? type.defaultLightValue : 0;
        }
        return type == EnumSkyBlock.SKY ? section.getSkyLight() == null ? 0
                : section.getSkyLight(blockX & 15, height & 15, blockZ & 15)
                : section.getBlockLight(blockX & 15, height & 15, blockZ & 15);
    }

    public static int containedLight(int original, net.minecraft.world.IBlockAccess world, BlockPos pos) {
        ModernLightAccess access = CURRENT.get();
        if (access == null || access.world != world) return -1;
        return !ModernChunkLighting.isWaterlogged(state(access.world, pos), true)
                && !xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates.has(world, pos)
                ? original : -1;
    }

    public static net.minecraft.block.state.IBlockState state(World world, BlockPos pos) {
        ModernLightAccess access = CURRENT.get();
        if (access == null || access.world != world) return world.getBlockState(pos);
        int height = pos.getY();
        if (height < access.minimumY || height >= access.maximumY) {
            return net.minecraft.init.Blocks.AIR.getDefaultState();
        }
        Chunk chunk = access.at(pos);
        if (chunk == null || chunk.getClass() != Chunk.class) return world.getBlockState(pos);
        int index = access.sectionIndices[(height - access.minimumY) >> 4];
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        ExtendedBlockStorage section = index < 0 || index >= storage.length ? null : storage[index];
        return section == null ? net.minecraft.init.Blocks.AIR.getDefaultState()
                : section.get(pos.getX() & 15, height & 15, pos.getZ() & 15);
    }

    public static boolean skyVisible(World world, BlockPos pos) {
        ModernLightAccess access = CURRENT.get();
        if (access == null || access.world != world) return world.canSeeSky(pos);
        Chunk chunk = access.at(pos);
        return chunk == null || chunk.getClass() != Chunk.class
                ? world.canSeeSky(pos) : chunk.canSeeSky(pos);
    }

    private Chunk at(BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        return chunkX < originX || chunkZ < originZ || chunkX >= originX + 5 || chunkZ >= originZ + 5
                ? null : chunk(chunkX, chunkZ);
    }

    private Chunk chunk(int chunkX, int chunkZ) {
        int index = (chunkZ - originZ) * 5 + chunkX - originX;
        Chunk chunk = chunks[index];
        if (chunk == null) {
            chunk = world.getChunkProvider().id2ChunkMap.get(ChunkPos.asLong(chunkX, chunkZ));
            chunks[index] = chunk;
            if (chunk != null) loadedMask |= 1 << index;
        }
        return chunk;
    }

    @Override
    public void close() {
        if (previous == null) CURRENT.remove();
        else CURRENT.set(previous);
    }
}
