package xy177.farmersfuturedelight.core;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.MinecraftForgeClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.client.FFDLoadingScreenRenderer;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.BlockSeaPickle;
import xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid;

public final class FFDClientHeightHooks {
    private static final Logger LOGGER = LogManager.getLogger("FFD Height Core");
    private static final Object SHADER_ALIAS_LOCK = new Object();
    private static LoadingScreenRenderer previousIntegratedWorldLoadingScreen;
    private static volatile Object shaderAliasPack;
    private static volatile boolean shaderAliasesResolved;
    private static volatile int shaderDrySeaPickleAlias = -1;
    private static volatile int shaderWaterloggedSeaPickleAlias = -1;
    private static final Semaphore NOTHIRIUM_BUFFER_PERMITS =
            new Semaphore(defaultNothiriumBufferPermits(), true);

    private FFDClientHeightHooks() {
    }

    public static IBlockState shaderWaterloggedState(IBlockState state) {
        if (state != null && MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT
                && WaterloggedPlantFluid.isWaterlogged(state)) {
            return Blocks.WATER.getDefaultState();
        }
        return state;
    }

    public static int shaderBlockAlias(IBlockState state, int mappedId) {
        if (state == null || !(state.getBlock() instanceof BlockSeaPickle)) {
            return mappedId;
        }
        boolean waterlogged = state.getValue(BlockSeaPickle.WATERLOGGED);
        int alias = getSeaPickleShaderAlias(waterlogged);
        return alias >= 0 ? alias : mappedId;
    }

    private static int getSeaPickleShaderAlias(boolean waterlogged) {
        Object pack = getActiveShaderPack();
        if (!shaderAliasesResolved || shaderAliasPack != pack) {
            synchronized (SHADER_ALIAS_LOCK) {
                if (!shaderAliasesResolved || shaderAliasPack != pack) {
                    int[] aliases = readSeaPickleShaderAliases(pack);
                    shaderDrySeaPickleAlias = aliases[0];
                    shaderWaterloggedSeaPickleAlias = aliases[1];
                    shaderAliasPack = pack;
                    shaderAliasesResolved = true;
                    if (aliases[0] >= 0 || aliases[1] >= 0) {
                        LOGGER.info("Resolved shader sea-pickle aliases: dry={}, waterlogged={}",
                                aliases[0], aliases[1]);
                    }
                }
            }
        }
        return waterlogged ? shaderWaterloggedSeaPickleAlias : shaderDrySeaPickleAlias;
    }

    private static Object getActiveShaderPack() {
        try {
            Class<?> shaders = Class.forName("net.optifine.shaders.Shaders", false,
                    FFDClientHeightHooks.class.getClassLoader());
            Method getter = shaders.getMethod("getShaderPack");
            return getter.invoke(null);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static int[] readSeaPickleShaderAliases(Object pack) {
        int[] aliases = {-1, -1};
        if (pack == null) {
            return aliases;
        }
        try {
            Method resourceGetter = pack.getClass().getMethod("getResourceAsStream", String.class);
            try (InputStream input = (InputStream) resourceGetter.invoke(pack,
                    "/shaders/block.properties")) {
                if (input == null) {
                    return aliases;
                }
                Properties properties = new Properties();
                properties.load(input);
                aliases[0] = findSeaPickleShaderAlias(properties, false);
                aliases[1] = findSeaPickleShaderAlias(properties, true);
            }
        } catch (ReflectiveOperationException | LinkageError | java.io.IOException ignored) {
            return new int[] {-1, -1};
        }
        return aliases;
    }

    private static int findSeaPickleShaderAlias(Properties properties, boolean waterlogged) {
        int genericAlias = -1;
        String stateProperty = "waterlogged=" + waterlogged;
        for (String propertyName : properties.stringPropertyNames()) {
            if (!propertyName.startsWith("block.")) {
                continue;
            }
            int alias;
            try {
                alias = Integer.parseInt(propertyName.substring("block.".length()));
            } catch (NumberFormatException ignored) {
                continue;
            }
            String value = properties.getProperty(propertyName, "");
            for (String token : value.toLowerCase(Locale.ROOT).split("\\s+")) {
                if (!(token.equals("sea_pickle") || token.startsWith("sea_pickle:")
                        || token.equals("minecraft:sea_pickle")
                        || token.startsWith("minecraft:sea_pickle:"))) {
                    continue;
                }
                if (token.contains(stateProperty)) {
                    return alias;
                }
                if (!token.contains("waterlogged=")) {
                    genericAlias = alias;
                }
            }
        }
        return genericAlias;
    }

    public static void beginIntegratedWorldLoad(Minecraft minecraft) {
        if (!FFDConfig.modernWorldLoadingScreen
                || minecraft.loadingScreen instanceof FFDLoadingScreenRenderer) {
            return;
        }
        previousIntegratedWorldLoadingScreen = minecraft.loadingScreen;
        minecraft.loadingScreen = new FFDLoadingScreenRenderer(minecraft);
    }

    public static void endIntegratedWorldLoad(Minecraft minecraft) {
        if (minecraft.loadingScreen instanceof FFDLoadingScreenRenderer) {
            minecraft.loadingScreen = previousIntegratedWorldLoadingScreen == null
                    ? new LoadingScreenRenderer(minecraft) : previousIntegratedWorldLoadingScreen;
        }
        previousIntegratedWorldLoadingScreen = null;
    }

    public static RenderChunk getRenderChunkOffset(World world, ViewFrustum viewFrustum,
            int renderDistanceChunks, BlockPos playerPos, RenderChunk renderChunkBase, EnumFacing facing) {
        BlockPos offset = renderChunkBase.getBlockPosOffset16(facing);
        if (MathHelper.abs(playerPos.getX() - offset.getX()) > renderDistanceChunks * 16
                || FFDHeightHooks.isOutsideBuildHeight(world, offset)
                || MathHelper.abs(playerPos.getZ() - offset.getZ()) > renderDistanceChunks * 16) {
            return null;
        }
        return ((FFDViewFrustumAccess) viewFrustum).ffd$getRenderChunk(offset);
    }

    public static int storageIndexForClientSectionY(int sectionY) {
        World world = Minecraft.getMinecraft().world;
        return world == null ? Math.max(0, sectionY)
                : FFDHeightHooks.storageIndexForSectionY(sectionY, world);
    }

    public static int currentWorldMinY() {
        return FFDHeightHooks.minY(Minecraft.getMinecraft().world);
    }

    public static int currentWorldMaxYInclusive() {
        return FFDHeightHooks.maxYInclusive(Minecraft.getMinecraft().world);
    }

    public static boolean shouldUseVoxelMapWaypointFallback(int waypointY) {
        World world = Minecraft.getMinecraft().world;
        return !FFDHeightHooks.isExtended(world) && waypointY <= 0;
    }

    public static int decodeXaeroMinimapHeight(int encodedHeight, int caveStart,
            int chunkX, int chunkZ, int localX, int localZ) {
        if (caveStart != -1) {
            return FFDHeightHooks.decodeXaeroHeightNearReference(encodedHeight, caveStart);
        }
        return FFDHeightHooks.decodeXaeroSurfaceHeight(encodedHeight,
                Minecraft.getMinecraft().world, chunkX, chunkZ, localX, localZ);
    }

    public static int resolveXaeroFullMapCaveStart(int debouncedCaveStart,
            boolean isMapScreen, int configuredCaveStart, int detectedCaveStart, World world) {
        if (isMapScreen && configuredCaveStart == Integer.MAX_VALUE
                && FFDHeightHooks.isExtended(world)) {
            return detectedCaveStart;
        }
        return debouncedCaveStart;
    }

    public static int prepareXaeroFullMapAutoCaveStart(int candidateCaveStart,
            boolean isMapScreen, int configuredCaveStart, World world) {
        if (isMapScreen && configuredCaveStart == Integer.MAX_VALUE
                && FFDHeightHooks.isExtended(world)) {
            return Integer.MIN_VALUE;
        }
        return candidateCaveStart;
    }

    public static String resolveXaeroCacheFolderName(String originalName, World world) {
        return FFDHeightHooks.isExtended(world)
                ? originalName + "_ffd_ext_height_v2" : originalName;
    }

    public static void markBlockRangeForRenderUpdate(World world, RenderGlobal renderGlobal,
            ViewFrustum viewFrustum,
            int x1, int y1, int z1, int x2, int y2, int z2) {
        if (FFDHeightHooks.isExtended(world) && viewFrustum != null
                && !hasExtendedVerticalSections(viewFrustum)) {
            renderGlobal.loadRenderers();
            return;
        }
        if (FFDHeightHooks.isExtended(world) && y1 == 0 && (y2 == 255 || y2 == 256)) {
            y1 = FFDHeightHooks.minY(world);
            y2 = FFDHeightHooks.maxYExclusive(world);
        }
        ((FFDRenderGlobalAccess) renderGlobal).ffd$markBlocksForUpdate(
                x1 - 1, y1 - 1, z1 - 1,
                x2 + 1, y2 + 1, z2 + 1, false);
    }

    public static int renderSectionIndex(int sectionY, int countChunksY, World world) {
        if (countChunksY <= 0) {
            return 0;
        }
        if (FFDHeightHooks.isExtended(world)
                && countChunksY == FFDHeightHooks.EXTENDED_SECTION_COUNT) {
            int index = FFDHeightHooks.storageIndexForSectionY(sectionY, world);
            if (index >= 0 && index < countChunksY) {
                return index;
            }
        }
        return Math.floorMod(sectionY, countChunksY);
    }

    private static boolean hasExtendedVerticalSections(ViewFrustum viewFrustum) {
        FFDViewFrustumAccess access = (FFDViewFrustumAccess) viewFrustum;
        RenderChunk bottom = access.ffd$getRenderChunk(new BlockPos(0, FFDHeightHooks.MIN_Y, 0));
        RenderChunk top = access.ffd$getRenderChunk(
                new BlockPos(0, FFDHeightHooks.MAX_Y_EXCLUSIVE - 16, 0));
        return bottom != null && bottom.getPosition().getY() == FFDHeightHooks.MIN_Y
                && top != null
                && top.getPosition().getY() == FFDHeightHooks.MAX_Y_EXCLUSIVE - 16;
    }

    public static int topRenderSectionCenter(World world) {
        return FFDHeightHooks.maxYExclusive(world) - 8;
    }

    public static int bottomRenderSectionCenter(World world) {
        return FFDHeightHooks.minY(world) + 8;
    }

    public static double worldBorderTop(World world) {
        return FFDHeightHooks.maxYExclusive(world);
    }

    public static double worldBorderBottom(World world) {
        return FFDHeightHooks.minY(world);
    }

    public static float worldBorderTextureSpan(World world) {
        return (FFDHeightHooks.maxYExclusive(world) - FFDHeightHooks.minY(world)) * 0.5F;
    }

    public static int getDirectBiomeSkyColour(World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        return biome.getSkyColorByTemp(biome.getTemperature(pos));
    }

    public static ExtendedBlockStorage getNothiriumSection(World world, int sectionX,
            int sectionY, int sectionZ) {
        if (world == null) {
            return null;
        }
        int storageIndex = FFDHeightHooks.storageIndexForSectionY(sectionY, world);
        if (storageIndex < 0) {
            return null;
        }
        Chunk chunk = world.getChunkFromChunkCoords(sectionX, sectionZ);
        if (chunk == null) {
            return null;
        }
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        return storageIndex < storage.length ? storage[storageIndex] : null;
    }

    public static boolean isNothiriumSectionInWorld(int sectionY) {
        World world = Minecraft.getMinecraft().world;
        if (world == null) {
            return sectionY >= 0 && sectionY < FFDHeightHooks.LEGACY_SECTION_COUNT;
        }
        return sectionY >= FFDHeightHooks.minSectionY(world)
                && sectionY < FFDHeightHooks.maxSectionYExclusive(world);
    }

    public static int currentWorldMinSectionY() {
        World world = Minecraft.getMinecraft().world;
        return world == null ? 0 : FFDHeightHooks.minSectionY(world);
    }

    public static int currentWorldMaxSectionYExclusive() {
        World world = Minecraft.getMinecraft().world;
        return world == null ? FFDHeightHooks.LEGACY_SECTION_COUNT
                : FFDHeightHooks.maxSectionYExclusive(world);
    }

    public static int nothiriumVerticalRenderDistance(int horizontalRenderDistance) {
        World world = Minecraft.getMinecraft().world;
        if (!isExtendedHeightWorld(world)) {
            return horizontalRenderDistance;
        }
        return Math.min(horizontalRenderDistance, FFDHeightHooks.EXTENDED_SECTION_COUNT / 2);
    }

    public static int nothiriumTotalSections(int horizontalRenderDistance) {
        int horizontalSize = horizontalRenderDistance * 2 + 1;
        World world = Minecraft.getMinecraft().world;
        if (isExtendedHeightWorld(world)) {
            return horizontalSize * horizontalSize * Math.min(horizontalSize,
                    FFDHeightHooks.EXTENDED_SECTION_COUNT);
        }
        return horizontalSize * horizontalSize * Math.min(horizontalSize,
                FFDHeightHooks.LEGACY_SECTION_COUNT);
    }

    public static boolean acquireNothiriumBufferPermit() {
        try {
            NOTHIRIUM_BUFFER_PERMITS.acquire();
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static void releaseNothiriumBuffer(BlockingQueue<RegionRenderCacheBuilder> queue,
            RegionRenderCacheBuilder buffer) {
        try {
            if (queue != null && buffer != null) {
                queue.add(buffer);
            }
        } finally {
            NOTHIRIUM_BUFFER_PERMITS.release();
        }
    }

    public static int nothiriumBufferPermitLimit() {
        return NOTHIRIUM_BUFFER_PERMITS.availablePermits();
    }

    private static boolean isExtendedHeightWorld(World world) {
        return world != null && FFDHeightHooks.sectionCount(world)
                > FFDHeightHooks.LEGACY_SECTION_COUNT;
    }

    private static int defaultNothiriumBufferPermits() {
        int workers = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);
        return Math.max(2, Math.min(workers, 8));
    }
}
