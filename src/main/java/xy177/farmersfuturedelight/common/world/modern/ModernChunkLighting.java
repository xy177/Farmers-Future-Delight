package xy177.farmersfuturedelight.common.world.modern;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid;
import java.util.IdentityHashMap;
import java.util.Map;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class ModernChunkLighting {
    private static final ThreadLocal<Map<IBlockState, Boolean>> WATERLOGGED_STATES =
            ThreadLocal.withInitial(IdentityHashMap::new);
    private ModernChunkLighting() {
    }

    public static void checkInitial(Chunk chunk) {
        try (ModernLightAccess access = ModernLightAccess.open(chunk)) {
            ((xy177.farmersfuturedelight.core.FFDChunkLightingAccess) chunk).ffd$checkLight();
        }
    }

    public static boolean checkColumn(Chunk chunk, int x, int z) {
        if (ModernLightAccess.isOpenFor(chunk.getWorld())) return checkColumnLight(chunk, x, z);
        try (ModernLightAccess access = ModernLightAccess.open(chunk)) {
            return checkColumnLight(chunk, x, z);
        }
    }

    private static boolean checkColumnLight(Chunk chunk, int x, int z) {
        World world = chunk.getWorld();
        int minimumY = FFDHeightHooks.minY(world);
        int seaLevel = world.getSeaLevel();
        int topY = FFDHeightHooks.getTopFilledSegment(chunk) + 15;
        int localX = x & 15;
        int localZ = z & 15;
        boolean hasSkyLight = world.provider.hasSkyLight();
        boolean waterloggingEnabled = FFDConfig.isWaterloggingEnabled();
        boolean foundOpaque = false;
        boolean foundFullOpaqueBelowSea = false;
        int sectionY = Integer.MIN_VALUE;
        int sectionIndex = -1;
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                (chunk.x << 4) + x, minimumY, (chunk.z << 4) + z);
        for (int y = topY; y > seaLevel || y > minimumY && !foundFullOpaqueBelowSea; y--) {
            cursor.setY(y);
            if (sectionY != y >> 4) {
                sectionY = y >> 4;
                sectionIndex = FFDHeightHooks.storageIndex(y, world);
            }
            ExtendedBlockStorage section = sectionIndex < 0 || sectionIndex >= storage.length
                    ? Chunk.NULL_BLOCK_STORAGE : storage[sectionIndex];
            IBlockState state = section == Chunk.NULL_BLOCK_STORAGE
                    ? Blocks.AIR.getDefaultState() : section.get(localX, y & 15, localZ);
            int opacity = state.getLightOpacity(world, cursor);
            if (opacity == 255 && y < seaLevel) {
                foundFullOpaqueBelowSea = true;
            }
            if (!foundOpaque && opacity > 0) {
                foundOpaque = true;
            } else if (foundOpaque && (opacity == 0 || state.getMaterial() == Material.WATER
                    || hasStoredLight(chunk, section, cursor, localX, y & 15, localZ, hasSkyLight)
                    || isWaterlogged(state, waterloggingEnabled)) && !world.checkLight(cursor)) {
                return false;
            }
        }
        for (int y = cursor.getY(); y > minimumY; y--) {
            cursor.setY(y);
            if (sectionY != y >> 4) {
                sectionY = y >> 4;
                sectionIndex = FFDHeightHooks.storageIndex(y, world);
            }
            ExtendedBlockStorage section = sectionIndex < 0 || sectionIndex >= storage.length
                    ? Chunk.NULL_BLOCK_STORAGE : storage[sectionIndex];
            IBlockState state = section == Chunk.NULL_BLOCK_STORAGE
                    ? Blocks.AIR.getDefaultState() : section.get(localX, y & 15, localZ);
            if ((state.getLightValue(world, cursor) > 0 || state.getMaterial() == Material.WATER
                    || hasStoredLight(chunk, section, cursor, localX, y & 15, localZ, hasSkyLight)
                    || isWaterlogged(state, waterloggingEnabled)) && !world.checkLight(cursor)) {
                return false;
            }
        }
        return true;
    }

    public static void clearCache() {
        WATERLOGGED_STATES.remove();
    }

    static boolean isWaterlogged(IBlockState state, boolean enabled) {
        if (!enabled) return false;
        if (state.getClass() != BlockStateContainer.StateImplementation.class
                || state.getBlock() instanceof IWaterloggableBlock
                || WaterloggedPlantFluid.hasRegisteredAdapters()) return WaterloggedBlockApi.isWaterlogged(state);
        Map<IBlockState, Boolean> cache = WATERLOGGED_STATES.get();
        Boolean result = cache.get(state);
        if (result == null) {
            result = WaterloggedBlockApi.isWaterlogged(state);
            if (cache.size() >= 4096) cache.clear();
            cache.put(state, result);
        }
        return result;
    }

    private static boolean hasStoredLight(Chunk chunk, ExtendedBlockStorage section,
            BlockPos cursor, int x, int y, int z, boolean hasSkyLight) {
        if (section == Chunk.NULL_BLOCK_STORAGE) {
            return hasSkyLight && chunk.canSeeSky(cursor);
        }
        return section.getBlockLight(x, y, z) > 0
                || hasSkyLight && section.getSkyLight() != null && section.getSkyLight(x, y, z) > 0;
    }
}
