package xy177.farmersfuturedelight.core;

import com.google.common.base.Predicate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.NextTickListEntry;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class FFDHeightHooks {
    public static final String WORLD_TYPE_NAME = "ffd_cac";
    public static final int MIN_Y = -64;
    public static final int MAX_Y_EXCLUSIVE = 320;
    public static final int LEGACY_SECTION_COUNT = 16;
    public static final int EXTENDED_SECTION_COUNT = 24;
    public static final int FULL_SECTION_MASK = 0x00FFFFFF;
    private static final int WORLDGEN_HEIGHT_NORMAL = 0;
    private static final int WORLDGEN_HEIGHT_LEGACY = 1;
    private static final int WORLDGEN_HEIGHT_EXTENDED = 2;
    private static final ThreadLocal<Integer> WORLDGEN_HEIGHT_SCOPE = new ThreadLocal<>();
    private static final ThreadLocal<ArrayDeque<Integer>> COMPAT_WORLDGEN_HEIGHT_SCOPES =
            new ThreadLocal<>();
    private static volatile World voxelMapWorld;

    private FFDHeightHooks() {
    }

    public static boolean isExtended(World world) {
        if (!isOverworld(world) || world.getWorldInfo() == null) {
            return false;
        }
        WorldType type = world.getWorldInfo().getTerrainType();
        return isExtended(type);
    }

    public static boolean isExtended(WorldType type) {
        return type != null && WORLD_TYPE_NAME.equals(type.getName());
    }

    private static boolean isOverworld(World world) {
        return world != null && world.provider != null && world.provider.getDimension() == 0;
    }

    private static boolean hasExtendedWorldType(World world) {
        if (world == null || world.getWorldInfo() == null) {
            return false;
        }
        return isExtended(world.getWorldInfo().getTerrainType());
    }

    private static boolean usesDepthsUpdateHeight(World world) {
        return FFDCoreCompat.isDepthsUpdateHeightCorePresent() && world != null
                && world.provider != null && world.provider.getDimension() == 0;
    }

    private static boolean usesExtendedHeight(World world) {
        return isOverworld(world) && (FFDCoreCompat.isCaveBiomesApiPresent()
                || usesDepthsUpdateHeight(world) || isExtended(world));
    }

    public static int sectionCount(World world) {
        return usesExtendedHeight(world) ? EXTENDED_SECTION_COUNT : LEGACY_SECTION_COUNT;
    }

    public static int minY(World world) {
        return usesExtendedHeight(world) && !usesLegacyWorldgenHeight() ? MIN_Y : 0;
    }

    public static int minY(Chunk chunk) {
        return minY(chunk.getWorld());
    }

    public static int minY(Entity entity) {
        return entity == null ? 0 : minY(entity.world);
    }

    public static int pathableY(double y, Entity entity) {
        return minY(entity) < 0 ? MathHelper.floor(y) : (int) y;
    }

    public static int batFlightMinTargetY(Entity entity) {
        return minY(entity) + 1;
    }

    public static int deadlyWorldFloorMinY(World world) {
        return minY(world) + 5;
    }

    public static void beginCompatStructure(StructureStart start,
            World world, StructureBoundingBox box) {
        String className = start == null ? "" : start.getClass().getName();
        boolean yungMineshaft = className.startsWith(
                "com.yungnickyoung.minecraft.bettermineshafts.");
        boolean deeperDepths = className.equals(
                "com.deeperdepths.common.world.chambers.WorldGenTrialChambers$Start")
                || className.equals(
                "com.deeperdepths.common.world.ancient_cities.WorldGenAncientCities$Start");
        boolean extended = isExtended(world) && (yungMineshaft || deeperDepths);
        if (extended && box.minY > MIN_Y) {
            box.minY = MIN_Y;
        }
        beginCompatExtendedWorldgen(extended);
    }

    public static void beginIceAndFireWorldgen(World world) {
        beginCompatExtendedWorldgen(isExtended(world));
    }

    public static void beginDeeperDepthsStructure(World world) {
        beginCompatExtendedWorldgen(isExtended(world));
    }

    public static void beginDeadlyWorldGeneration(World world) {
        beginCompatExtendedWorldgen(isExtended(world));
    }

    public static void beginTaigaEezoWorldgen(IBlockState replacementBlock, World world) {
        beginCompatExtendedWorldgen(isTaigaEezo(replacementBlock) && isExtended(world));
    }

    public static int adjustTaigaEezoMinY(int original, IBlockState replacementBlock,
            World world) {
        return isTaigaEezo(replacementBlock) && isExtended(world) ? MIN_Y : original;
    }

    public static int adjustTaigaEezoMaxY(int original, IBlockState replacementBlock,
            World world) {
        return isTaigaEezo(replacementBlock) && isExtended(world)
                ? MIN_Y + original : original;
    }

    private static boolean isTaigaEezo(IBlockState state) {
        if (state == null || state.getBlock() == null) {
            return false;
        }
        ResourceLocation id = state.getBlock().getRegistryName();
        return id != null && "taiga".equals(id.getResourceDomain())
                && "eezo_ore".equals(id.getResourcePath());
    }

    public static void endExtendedWorldgenHeight() {
        ArrayDeque<Integer> scopes = COMPAT_WORLDGEN_HEIGHT_SCOPES.get();
        if (scopes == null || scopes.isEmpty()) {
            return;
        }
        int previousMode = scopes.removeLast();
        if (scopes.isEmpty()) {
            COMPAT_WORLDGEN_HEIGHT_SCOPES.remove();
        }
        if (previousMode == WORLDGEN_HEIGHT_NORMAL) {
            WORLDGEN_HEIGHT_SCOPE.remove();
        } else {
            WORLDGEN_HEIGHT_SCOPE.set(previousMode);
        }
    }

    private static void beginCompatExtendedWorldgen(boolean extended) {
        ArrayDeque<Integer> scopes = COMPAT_WORLDGEN_HEIGHT_SCOPES.get();
        if (scopes == null) {
            scopes = new ArrayDeque<>();
            COMPAT_WORLDGEN_HEIGHT_SCOPES.set(scopes);
        }
        Integer previous = WORLDGEN_HEIGHT_SCOPE.get();
        scopes.addLast(previous == null ? WORLDGEN_HEIGHT_NORMAL : previous);
        if (extended) {
            WORLDGEN_HEIGHT_SCOPE.set(WORLDGEN_HEIGHT_EXTENDED);
        }
    }

    public static Property deadlyWorldHeightProperty(Configuration configuration,
            String category, String key, int defaultValue, String comment, int min, int max) {
        if (key != null && (key.endsWith("height_min") || key.endsWith("height_max"))) {
            min = Integer.MIN_VALUE;
        }
        return configuration.get(category, key, defaultValue, comment, min, max);
    }

    public static double corpseMinY(Entity entity) {
        return minY(entity);
    }

    public static int maxYExclusive(World world) {
        return usesExtendedHeight(world) && !usesLegacyWorldgenHeight() ? MAX_Y_EXCLUSIVE : 256;
    }

    public static int maxYExclusive(Chunk chunk) {
        return maxYExclusive(chunk.getWorld());
    }

    public static int playerBuildLimit(World world) {
        return maxYExclusive(world);
    }

    public static int adjustProviderHeight(int originalHeight, World world) {
        return usesExtendedHeight(world) && !usesLegacyWorldgenHeight()
                ? MAX_Y_EXCLUSIVE : originalHeight;
    }

    public static int netherApiGenerationHeight(World world) {
        return world != null && world.provider != null && world.provider.getDimension() == -1
                ? 128 : world.getActualHeight();
    }

    public static int portalSearchHeight(World world) {
        if (world != null && world.provider != null && world.provider.getDimension() == -1
                && hasExtendedWorldType(world)) {
            return 128;
        }
        return world == null ? 256 : world.getActualHeight();
    }

    public static WorldgenHeightScope enterLegacyWorldgenHeight() {
        return new WorldgenHeightScope(WORLDGEN_HEIGHT_LEGACY);
    }

    public static WorldgenHeightScope enterExtendedWorldgenHeight() {
        return new WorldgenHeightScope(WORLDGEN_HEIGHT_EXTENDED);
    }

    public static void generateLegacyWorld(int chunkX, int chunkZ, World world,
            IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        try (WorldgenHeightScope ignored = enterLegacyWorldgenHeight()) {
            GameRegistry.generateWorld(chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        }
    }

    private static boolean usesLegacyWorldgenHeight() {
        Integer mode = WORLDGEN_HEIGHT_SCOPE.get();
        return mode != null && mode == WORLDGEN_HEIGHT_LEGACY;
    }

    public static final class WorldgenHeightScope implements AutoCloseable {
        private final int previousMode;
        private boolean closed;

        private WorldgenHeightScope(int mode) {
            Integer previous = WORLDGEN_HEIGHT_SCOPE.get();
            previousMode = previous == null ? WORLDGEN_HEIGHT_NORMAL : previous;
            WORLDGEN_HEIGHT_SCOPE.set(mode);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (previousMode == WORLDGEN_HEIGHT_NORMAL) {
                WORLDGEN_HEIGHT_SCOPE.remove();
            } else {
                WORLDGEN_HEIGHT_SCOPE.set(previousMode);
            }
        }
    }

    public static double outOfWorldThreshold(double originalThreshold, Entity entity) {
        return usesExtendedHeight(entity.world) ? minY(entity.world) - 64.0D : originalThreshold;
    }

    public static int adjustVoidTriggerY(Entity entity, int originalTriggerY) {
        if (entity == null || !isExtended(entity.world)) {
            return originalTriggerY;
        }
        return originalTriggerY + minY(entity.world);
    }

    public static int fallingBlockMinY(Entity entity) {
        return minY(entity.world) + 1;
    }

    public static int fallingBlockMaxY(Entity entity) {
        return maxYExclusive(entity.world);
    }

    /**
     * Vanilla piston boundary checks use a literal zero for the build-height floor.
     * Keep the complete vanilla decision here and only widen that floor for the
     * extended-height world type.
     */
    public static boolean canPush(IBlockState blockState, World world, BlockPos pos,
            EnumFacing facing, boolean destroyBlocks, EnumFacing pistonFacing) {
        if (blockState == null || world == null || pos == null || facing == null
                || pistonFacing == null) {
            return false;
        }
        Block block = blockState.getBlock();
        if (block == Blocks.OBSIDIAN || !world.getWorldBorder().contains(pos)) {
            return false;
        }
        int minY = minY(world);
        int maxY = maxYExclusive(world) - 1;
        if (pos.getY() < minY || facing == EnumFacing.DOWN && pos.getY() == minY) {
            return false;
        }
        if (pos.getY() > maxY || facing == EnumFacing.UP && pos.getY() == maxY) {
            return false;
        }
        if (block != Blocks.PISTON && block != Blocks.STICKY_PISTON) {
            if (blockState.getBlockHardness(world, pos) == -1.0F) {
                return false;
            }
            EnumPushReaction reaction = blockState.getMobilityFlag();
            switch (reaction) {
                case BLOCK:
                    return false;
                case DESTROY:
                    return destroyBlocks;
                case PUSH_ONLY:
                    return facing == pistonFacing;
                default:
                    break;
            }
        } else if (Boolean.TRUE.equals(blockState.getValue(BlockPistonBase.EXTENDED))) {
            return false;
        }
        return !block.hasTileEntity(blockState);
    }

    public static double playerSpawnCeiling(Entity entity) {
        return maxYExclusive(entity.world) - 1.0D;
    }

    public static double playerTransferCeiling(Entity entity) {
        return maxYExclusive(entity.world);
    }

    public static void prepareEntityToSpawn(Entity entity) {
        if (entity.world == null) {
            return;
        }
        while (entity.posY > minY(entity.world) && entity.posY < maxYExclusive(entity.world)) {
            entity.setPosition(entity.posX, entity.posY, entity.posZ);
            if (entity.world.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty()) {
                break;
            }
            entity.posY++;
        }
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
        entity.rotationPitch = 0.0F;
    }

    public static boolean isOutsideBuildHeight(WorldType type, BlockPos pos) {
        int y = pos.getY();
        return isExtended(type) ? y < MIN_Y || y >= MAX_Y_EXCLUSIVE : y < 0 || y >= 256;
    }

    public static boolean isOutsideBuildHeight(World world, BlockPos pos) {
        int y = pos.getY();
        return y < minY(world) || y >= maxYExclusive(world);
    }

    public static BlockPos parseBlockPos(ICommandSender sender, String[] args, int startIndex,
            boolean centerBlock) throws NumberInvalidException {
        BlockPos origin = sender.getPosition();
        World world = sender.getEntityWorld();
        return new BlockPos(
                CommandBase.parseDouble(origin.getX(), args[startIndex], -30000000, 30000000,
                        centerBlock),
                CommandBase.parseDouble(origin.getY(), args[startIndex + 1], minY(world),
                        maxYExclusive(world), false),
                CommandBase.parseDouble(origin.getZ(), args[startIndex + 2], -30000000, 30000000,
                        centerBlock));
    }

    public static int minCommandY(ICommandSender sender) {
        return minY(sender.getEntityWorld());
    }

    public static int maxCommandY(ICommandSender sender) {
        return maxYExclusive(sender.getEntityWorld());
    }

    public static boolean isAreaLoaded(World world, int xStart, int yStart, int zStart,
            int xEnd, int yEnd, int zEnd, boolean allowEmpty) {
        if (yEnd < minY(world) || yStart >= maxYExclusive(world)) {
            return false;
        }
        int minChunkX = xStart >> 4;
        int minChunkZ = zStart >> 4;
        int maxChunkX = xEnd >> 4;
        int maxChunkZ = zEnd >> 4;
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                probe.setPos(chunkX << 4, minY(world), chunkZ << 4);
                if (!world.isBlockLoaded(probe, allowEmpty)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Keeps vanilla Y=0..255 in slots 0..15 for legacy generators that inspect the array directly.
     */
    public static int storageIndex(int y, World world) {
        if (FFDCoreCompat.isCaveBiomesApiPresent()) {
            return y >= MIN_Y && y < MAX_Y_EXCLUSIVE ? (y - MIN_Y) >> 4 : -1;
        }
        if (usesDepthsUpdateHeight(world)) {
            int sectionY = y >> 4;
            if (sectionY >= 0 && sectionY < LEGACY_SECTION_COUNT) {
                return sectionY;
            }
            if (sectionY >= LEGACY_SECTION_COUNT && sectionY < 20) {
                return sectionY;
            }
            if (sectionY >= -4 && sectionY < 0) {
                return 20 + (-1 - sectionY);
            }
            return -1;
        }
        if (!isExtended(world) || y >= 0 && y < 256) {
            return y >> 4;
        }
        if (y >= MIN_Y && y < 0) {
            return LEGACY_SECTION_COUNT + ((y - MIN_Y) >> 4);
        }
        if (y >= 256 && y < MAX_Y_EXCLUSIVE) {
            return 20 + ((y - 256) >> 4);
        }
        return -1;
    }

    public static int storageIndexForChunk(int y, Chunk chunk) {
        return storageIndex(y, chunk.getWorld());
    }

    public static int storageIndexForSectionY(int sectionY, World world) {
        return storageIndex(sectionY << 4, world);
    }

    public static int storageIndexForSectionY(World world, int sectionY) {
        return storageIndexForSectionY(sectionY, world);
    }

    public static int realisticPhysicsSectionIndex(World world, int sectionY) {
        return usesExtendedHeight(world) ? sectionY - (MIN_Y >> 4) : sectionY;
    }

    public static int realisticPhysicsSectionIndex(Chunk chunk, int sectionY) {
        return chunk == null ? sectionY : realisticPhysicsSectionIndex(chunk.getWorld(), sectionY);
    }

    public static int realisticPhysicsSectionY(World world, int index) {
        return usesExtendedHeight(world) ? index + (MIN_Y >> 4) : index;
    }

    public static int realisticPhysicsSectionY(Chunk chunk, int index) {
        return chunk == null ? index : realisticPhysicsSectionY(chunk.getWorld(), index);
    }

    public static int realisticPhysicsStorageIndex(Chunk chunk, int index) {
        if (chunk == null || !usesExtendedHeight(chunk.getWorld())) {
            return index;
        }
        return storageIndexForSectionY(realisticPhysicsSectionY(chunk, index), chunk.getWorld());
    }

    public static int realisticPhysicsMinSection(World world) {
        return usesExtendedHeight(world) ? MIN_Y >> 4 : 0;
    }

    public static int realisticPhysicsMinSection(Chunk chunk) {
        return chunk == null ? 0 : realisticPhysicsMinSection(chunk.getWorld());
    }

    public static int realisticPhysicsMaxSection(World world) {
        return usesExtendedHeight(world) ? MAX_Y_EXCLUSIVE >> 4 : 16;
    }

    public static int realisticPhysicsMaxSection(Chunk chunk) {
        if (chunk == null) {
            return 15;
        }
        return usesExtendedHeight(chunk.getWorld())
                ? MAX_Y_EXCLUSIVE >> 4 : chunk.getBlockStorageArray().length - 1;
    }

    public static int realisticPhysicsMinY(World world) {
        return usesExtendedHeight(world) ? MIN_Y : 0;
    }

    public static int realisticPhysicsMaxY(World world) {
        return usesExtendedHeight(world) ? MAX_Y_EXCLUSIVE - 1 : 255;
    }

    public static int realisticPhysicsPlayerSectionIndex(EntityPlayer player) {
        if (player == null) {
            return 0;
        }
        return realisticPhysicsSectionIndex(player.world, player.getPosition().getY() >> 4);
    }

    public static int minSectionY(World world) {
        return minY(world) >> 4;
    }

    public static int maxSectionYExclusive(World world) {
        return maxYExclusive(world) >> 4;
    }

    public static int maxYInclusive(World world) {
        return maxYExclusive(world) - 1;
    }

    public static int maxYInclusive(Chunk chunk) {
        return maxYExclusive(chunk) - 1;
    }

    public static int distantHorizonsMinNonEmptyHeight(Chunk chunk) {
        int minHeight = minY(chunk);
        boolean found = false;
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        for (int index = 0; index < storage.length; index++) {
            ExtendedBlockStorage section = storage[index];
            if (section != null && section != Chunk.NULL_BLOCK_STORAGE && !section.isEmpty()) {
                int sectionMinY = sectionBaseYForChunkIndex(index, chunk);
                minHeight = found ? Math.min(minHeight, sectionMinY) : sectionMinY;
                found = true;
            }
        }
        return minHeight;
    }

    public static int distantHorizonsMaxNonEmptyHeight(Chunk chunk) {
        int maxHeight = minY(chunk) + 16;
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        for (int index = 0; index < storage.length; index++) {
            ExtendedBlockStorage section = storage[index];
            if (section != null && section != Chunk.NULL_BLOCK_STORAGE && !section.isEmpty()) {
                maxHeight = Math.max(maxHeight,
                        sectionBaseYForChunkIndex(index, chunk) + 16);
            }
        }
        return maxHeight;
    }

    public static int clampYToWorld(int y, World world) {
        return MathHelper.clamp(y, minY(world), maxYInclusive(world));
    }

    public static int sectionMinYForStorageIndex(int storageIndex, World world) {
        return (minSectionY(world) + storageIndex) << 4;
    }

    public static int decodeXaeroHeight(int encodedHeight, int caveStart) {
        if (caveStart == -1 || caveStart == Integer.MAX_VALUE
                || caveStart == Integer.MIN_VALUE) {
            return encodedHeight & 255;
        }
        return decodeXaeroHeightNearReference(encodedHeight, caveStart);
    }

    public static int decodeXaeroWorldMapHeight(int encodedHeight, int caveStart,
            int surfaceReference) {
        int reference = caveStart == Integer.MAX_VALUE || caveStart == Integer.MIN_VALUE
                ? surfaceReference : caveStart;
        return decodeXaeroHeightNearReference(encodedHeight, reference);
    }

    public static int decodeXaeroHeightNearReference(int encodedHeight, int referenceHeight) {
        int encoded = encodedHeight & 255;
        int base = Math.floorDiv(referenceHeight, 256) * 256;
        int decoded = base + encoded;
        if (decoded > referenceHeight + 128) {
            decoded -= 256;
        } else if (decoded < referenceHeight - 128) {
            decoded += 256;
        }
        return decoded;
    }

    public static int decodeXaeroWorldMapHeightAt(int encodedHeight, int caveStart, World world,
            int chunkX, int chunkZ, int localX, int localZ) {
        if (caveStart != Integer.MAX_VALUE && caveStart != Integer.MIN_VALUE) {
            return decodeXaeroHeightNearReference(encodedHeight, caveStart);
        }
        return decodeXaeroSurfaceHeight(encodedHeight, world, chunkX, chunkZ, localX, localZ);
    }

    public static int decodeXaeroSurfaceHeight(int encodedHeight, World world,
            int chunkX, int chunkZ, int localX, int localZ) {
        if (!isExtended(world)) {
            return encodedHeight & 255;
        }
        Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
        if (chunk == null) {
            return encodedHeight & 255;
        }
        int surfaceHeight = chunk.getHeightValue(localX & 15, localZ & 15);
        return decodeXaeroHeightNearReference(encodedHeight, surfaceHeight);
    }

    public static NBTTagList sortXaeroWorldMapSections(NBTTagList sections, World world) {
        if (!isExtended(world) || sections == null || sections.tagCount() < 2) {
            return sections;
        }
        List<NBTTagCompound> sorted = new ArrayList<>(sections.tagCount());
        boolean alreadySorted = true;
        int previousY = Integer.MIN_VALUE;
        for (int i = 0; i < sections.tagCount(); i++) {
            NBTTagCompound section = sections.getCompoundTagAt(i);
            int sectionY = section.getByte("Y");
            if (sectionY < previousY) {
                alreadySorted = false;
            }
            previousY = sectionY;
            sorted.add(section);
        }
        if (alreadySorted) {
            return sections;
        }
        Collections.sort(sorted, new Comparator<NBTTagCompound>() {
            @Override
            public int compare(NBTTagCompound left, NBTTagCompound right) {
                return Byte.compare(left.getByte("Y"), right.getByte("Y"));
            }
        });
        NBTTagList result = new NBTTagList();
        for (NBTTagCompound section : sorted) {
            result.appendTag(section);
        }
        return result;
    }

    public static ExtendedBlockStorage getStorageForSectionY(Chunk chunk, int sectionY) {
        int index = storageIndexForSectionY(sectionY, chunk.getWorld());
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        return index >= 0 && index < storage.length ? storage[index] : null;
    }

    public static int getSectionBasedHeight(Chunk chunk, int startY) {
        World world = chunk.getWorld();
        int minSectionY = minSectionY(world);
        int maxSectionY = maxSectionYExclusive(world) - 1;
        int startSectionY = MathHelper.clamp(Math.floorDiv(startY, 16), minSectionY,
                maxSectionY);
        for (int sectionY = maxSectionY; sectionY >= startSectionY; sectionY--) {
            ExtendedBlockStorage section = getStorageForSectionY(chunk, sectionY);
            if (section != null && section != Chunk.NULL_BLOCK_STORAGE && !section.isEmpty()) {
                return (sectionY << 4) + 15;
            }
        }
        for (int sectionY = startSectionY - 1; sectionY >= minSectionY; sectionY--) {
            ExtendedBlockStorage section = getStorageForSectionY(chunk, sectionY);
            if (section != null && section != Chunk.NULL_BLOCK_STORAGE && !section.isEmpty()) {
                return (sectionY << 4) + 15;
            }
        }
        return minY(world) - 1;
    }

    public static int getXaeroSurfaceHeight(Chunk chunk, int localX, int localZ) {
        World world = chunk.getWorld();
        if (!isExtended(world)) {
            return chunk.getHeightValue(localX & 15, localZ & 15);
        }

        int x = localX & 15;
        int z = localZ & 15;
        for (int sectionY = maxSectionYExclusive(world) - 1;
                sectionY >= minSectionY(world); sectionY--) {
            ExtendedBlockStorage section = getStorageForSectionY(chunk, sectionY);
            if (section == null || section == Chunk.NULL_BLOCK_STORAGE || section.isEmpty()) {
                continue;
            }
            int sectionBaseY = sectionY << 4;
            for (int offsetY = 15; offsetY >= 0; offsetY--) {
                int y = sectionBaseY + offsetY;
                if (chunk.getBlockLightOpacity(toWorldPos(chunk, x, y, z)) != 0) {
                    return y + 1;
                }
            }
        }
        return minY(world);
    }

    public static int getVoxelMapBlockHeight(boolean nether, boolean caves, World world, int x,
            int z, int playerHeight) {
        int height = world.getChunkFromBlockCoords(new BlockPos(x, playerHeight, z))
                .getHeightValue(x & 15, z & 15);
        if (!nether && !caves || height <= playerHeight) {
            return height;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, playerHeight, z);
        IBlockState state = world.getBlockState(cursor);
        if (state.getLightOpacity() == 0 && state.getMaterial() != Material.LAVA) {
            for (int y = playerHeight - 1; y >= minY(world); y--) {
                cursor.setY(y);
                state = world.getBlockState(cursor);
                if (state.getLightOpacity() > 0 || state.getMaterial() == Material.LAVA) {
                    return y + 1;
                }
            }
            return minY(world);
        }
        int ceiling = nether ? Math.min(127, maxYExclusive(world)) : maxYExclusive(world);
        for (int y = playerHeight + 1; y <= playerHeight + 10 && y < ceiling; y++) {
            cursor.setY(y);
            state = world.getBlockState(cursor);
            if (state.getLightOpacity() == 0 && state.getMaterial() != Material.LAVA) {
                return y;
            }
        }
        return -1;
    }

    public static int[] getVoxelMapSeafloorHeight(World world, int x, int z, int height) {
        int seafloorHeight = height;
        int underwaterTransparentHeight = -1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, seafloorHeight - 1, z);
        IBlockState state = world.getBlockState(cursor);
        while (state.getLightOpacity() < 5 && state.getMaterial() != Material.WATER
                && seafloorHeight > minY(world) + 1) {
            seafloorHeight--;
            cursor.setY(seafloorHeight - 1);
            state = world.getBlockState(cursor);
            Material material = state.getMaterial();
            if (material.blocksMovement() && material != Material.ICE
                    && underwaterTransparentHeight == -1) {
                underwaterTransparentHeight = seafloorHeight;
            }
        }
        return new int[] {seafloorHeight, underwaterTransparentHeight};
    }

    /** Supplies the world context used by VoxelMap's world-less persistent data object. */
    public static void setVoxelMapWorld(World world) {
        voxelMapWorld = world;
    }

    /** Encodes an extended-world height into VoxelMap's unsigned byte cache format. */
    public static int encodeVoxelMapHeight(int height) {
        World world = voxelMapWorld;
        if (!isExtended(world)) {
            return height;
        }
        if (height < minY(world)) {
            return 0;
        }
        return MathHelper.clamp(height - minY(world) + 1, 1, 255);
    }

    /** Decodes an extended-world height from VoxelMap's unsigned byte cache format. */
    public static int decodeVoxelMapHeight(int encoded) {
        World world = voxelMapWorld;
        if (!isExtended(world) || encoded == 0) {
            return encoded;
        }
        return minY(world) + encoded - 1;
    }

    public static boolean isSectionVisuallyEmpty(World world, int chunkX, int sectionY,
            int chunkZ) {
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        if (chunk.isEmpty()) {
            return true;
        }
        ExtendedBlockStorage section = getStorageForSectionY(chunk, sectionY);
        return section == Chunk.NULL_BLOCK_STORAGE || section.isEmpty();
    }

    public static int entitySectionIndexForChunk(int sectionY, Chunk chunk) {
        return storageIndexForSectionY(sectionY, chunk.getWorld());
    }

    public static int sectionYForStorageIndex(int index, World world) {
        if (FFDCoreCompat.isCaveBiomesApiPresent()) {
            return -4 + index;
        }
        if (usesDepthsUpdateHeight(world)) {
            if (index >= 0 && index < 20) {
                return index;
            }
            if (index >= 20 && index < EXTENDED_SECTION_COUNT) {
                return -1 - (index - 20);
            }
            return Integer.MIN_VALUE;
        }
        if (!isExtended(world) || index < LEGACY_SECTION_COUNT) {
            return index;
        }
        if (index < 20) {
            return -4 + index - LEGACY_SECTION_COUNT;
        }
        return 16 + index - 20;
    }

    public static int sectionBaseYForStorageIndex(int index, World world) {
        return sectionYForStorageIndex(index, world) << 4;
    }

    public static int sectionBaseYForChunkIndex(int index, Chunk chunk) {
        return sectionBaseYForStorageIndex(index, chunk.getWorld());
    }

    public static int fullSectionMask(Chunk chunk) {
        return usesExtendedHeight(chunk.getWorld())
                ? FULL_SECTION_MASK : 0x0000FFFF;
    }

    public static int normalizeSectionMask(Chunk chunk, int mask) {
        return usesExtendedHeight(chunk.getWorld())
                && mask == 0x0000FFFF ? FULL_SECTION_MASK : mask;
    }

    public static int packLocalBlockChange(int x, int y, int z) {
        return (x & 15) << 12 | (z & 15) << 8 | y & 255 | (y & 0xF00) << 8;
    }

    public static int unpackLocalBlockY(int packed) {
        int y = packed & 255 | packed >> 8 & 0xF00;
        return y << 20 >> 20;
    }

    public static IBlockState getBlockState(Chunk chunk, int x, int y, int z) {
        World world = chunk.getWorld();
        if (world.getWorldType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            if (y == 60) {
                return Blocks.BARRIER.getDefaultState();
            }
            if (y == 70) {
                IBlockState state = ChunkGeneratorDebug.getBlockStateFor(x, z);
                return state == null ? Blocks.AIR.getDefaultState() : state;
            }
            return Blocks.AIR.getDefaultState();
        }

        int index = storageIndex(y, world);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        if (index < 0 || index >= storage.length) {
            return Blocks.AIR.getDefaultState();
        }
        ExtendedBlockStorage section = storage[index];
        return section == Chunk.NULL_BLOCK_STORAGE
                ? Blocks.AIR.getDefaultState()
                : section.get(x & 15, y & 15, z & 15);
    }

    public static ExtendedBlockStorage getTopStorage(Chunk chunk) {
        ExtendedBlockStorage top = null;
        for (ExtendedBlockStorage section : chunk.getBlockStorageArray()) {
            if (section != Chunk.NULL_BLOCK_STORAGE
                    && (top == null || section.getYLocation() > top.getYLocation())) {
                top = section;
            }
        }
        return top;
    }

    public static int getTopFilledSegment(Chunk chunk) {
        ExtendedBlockStorage top = getTopStorage(chunk);
        return top == null ? minY(chunk.getWorld()) : top.getYLocation();
    }

    public static int getWorldLight(World world, BlockPos pos) {
        if (pos.getY() < minY(world)) {
            return 0;
        }
        if (pos.getY() >= maxYExclusive(world)) {
            pos = new BlockPos(pos.getX(), maxYExclusive(world) - 1, pos.getZ());
        }
        return world.getChunkFromBlockCoords(pos).getLightSubtracted(pos, 0);
    }

    public static int getWorldLight(World world, BlockPos pos, boolean checkNeighbors) {
        if (pos.getX() < -30000000 || pos.getZ() < -30000000
                || pos.getX() >= 30000000 || pos.getZ() >= 30000000) {
            return 15;
        }
        if (checkNeighbors && pos.getY() >= minY(world) && pos.getY() < maxYExclusive(world)
                && world.getBlockState(pos).useNeighborBrightness()) {
            int light = getWorldLight(world, pos.up(), false);
            light = Math.max(light, getWorldLight(world, pos.east(), false));
            light = Math.max(light, getWorldLight(world, pos.west(), false));
            light = Math.max(light, getWorldLight(world, pos.south(), false));
            return Math.max(light, getWorldLight(world, pos.north(), false));
        }
        if (pos.getY() < minY(world)) {
            return 0;
        }
        if (pos.getY() >= maxYExclusive(world)) {
            pos = new BlockPos(pos.getX(), maxYExclusive(world) - 1, pos.getZ());
        }
        return world.getChunkFromBlockCoords(pos).getLightSubtracted(pos, world.getSkylightSubtracted());
    }

    public static int getWorldLightFromNeighborsFor(World world, EnumSkyBlock type, BlockPos pos) {
        if (!world.provider.hasSkyLight() && type == EnumSkyBlock.SKY) {
            return 0;
        }
        if (pos.getY() < minY(world)) {
            pos = new BlockPos(pos.getX(), minY(world), pos.getZ());
        }
        if (!world.isValid(pos) || !world.isBlockLoaded(pos)) {
            return type.defaultLightValue;
        }
        if (world.getBlockState(pos).useNeighborBrightness()) {
            int light = getWorldLightFor(world, type, pos.up());
            light = Math.max(light, getWorldLightFor(world, type, pos.east()));
            light = Math.max(light, getWorldLightFor(world, type, pos.west()));
            light = Math.max(light, getWorldLightFor(world, type, pos.south()));
            return Math.max(light, getWorldLightFor(world, type, pos.north()));
        }
        return world.getChunkFromBlockCoords(pos).getLightFor(type, pos);
    }

    public static int getWorldLightFor(World world, EnumSkyBlock type, BlockPos pos) {
        if (pos.getY() < minY(world)) {
            pos = new BlockPos(pos.getX(), minY(world), pos.getZ());
        }
        if (!world.isValid(pos) || !world.isBlockLoaded(pos)) {
            return type.defaultLightValue;
        }
        return world.getChunkFromBlockCoords(pos).getLightFor(type, pos);
    }

    public static int getWorldHeight(World world, int x, int z) {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
            BlockPos probe = new BlockPos(x, minY(world), z);
            int height = world.isBlockLoaded(probe, true)
                    ? world.getChunkFromChunkCoords(x >> 4, z >> 4).getHeightValue(x & 15, z & 15)
                    : minY(world);
            return usesLegacyWorldgenHeight()
                    ? net.minecraft.util.math.MathHelper.clamp(height, 0, 255) : height;
        }
        return world.getSeaLevel() + 1;
    }

    public static void decorateBiome(Biome biome, World world, Random random, BlockPos pos) {
        try (WorldgenHeightScope ignored = enterLegacyWorldgenHeight()) {
            biome.decorate(world, random, pos);
        }
    }

    public static int getChunksLowestHorizon(World world, int x, int z) {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
            BlockPos probe = new BlockPos(x, minY(world), z);
            return world.isBlockLoaded(probe, true)
                    ? world.getChunkFromChunkCoords(x >> 4, z >> 4).getLowestHeight()
                    : minY(world);
        }
        return world.getSeaLevel() + 1;
    }

    public static BlockPos getTopSolidOrLiquidBlock(World world, BlockPos pos) {
        Chunk chunk = world.getChunkFromBlockCoords(pos);
        int minY = minY(world);
        BlockPos cursor = new BlockPos(pos.getX(), Math.min(maxYExclusive(world),
                chunk.getTopFilledSegment() + 16), pos.getZ());
        while (cursor.getY() > minY) {
            BlockPos below = cursor.down();
            IBlockState state = chunk.getBlockState(below);
            if (state.getMaterial().blocksMovement()
                    && !state.getBlock().isLeaves(state, world, below)
                    && !state.getBlock().isFoliage(world, below)) {
                break;
            }
            cursor = below;
        }
        return cursor;
    }

    public static boolean canBlockFreezeBody(World world, BlockPos pos, boolean noWaterAdj) {
        Biome biome = world.getBiome(pos);
        if (biome.getTemperature(pos) >= 0.15F || isOutsideBuildHeight(world, pos)
                || world.getLightFor(EnumSkyBlock.BLOCK, pos) >= 10) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if ((block != Blocks.WATER && block != Blocks.FLOWING_WATER)
                || state.getValue(BlockLiquid.LEVEL) != 0) {
            return false;
        }
        if (!noWaterAdj) {
            return true;
        }
        return !(world.getBlockState(pos.west()).getMaterial() == Material.WATER
                && world.getBlockState(pos.east()).getMaterial() == Material.WATER
                && world.getBlockState(pos.north()).getMaterial() == Material.WATER
                && world.getBlockState(pos.south()).getMaterial() == Material.WATER);
    }

    public static boolean canSnowAtBody(World world, BlockPos pos, boolean checkLight) {
        Biome biome = world.getBiome(pos);
        if (biome.getTemperature(pos) >= 0.15F) {
            return false;
        }
        if (!checkLight) {
            return true;
        }
        if (isOutsideBuildHeight(world, pos) || world.getLightFor(EnumSkyBlock.BLOCK, pos) >= 10) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isAir(state, world, pos) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos);
    }

    public static IBlockState getChunkCacheBlockState(World world, int chunkX, int chunkZ,
            Chunk[][] chunks, BlockPos pos) {
        if (isOutsideBuildHeight(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        int x = (pos.getX() >> 4) - chunkX;
        int z = (pos.getZ() >> 4) - chunkZ;
        return isChunkCacheIndexValid(chunks, x, z) ? chunks[x][z].getBlockState(pos)
                : Blocks.AIR.getDefaultState();
    }

    public static int getChunkCacheLightForExt(World world, int chunkX, int chunkZ,
            Chunk[][] chunks, EnumSkyBlock type, BlockPos pos) {
        if (type == EnumSkyBlock.SKY && !world.provider.hasSkyLight()) {
            return 0;
        }
        if (isOutsideBuildHeight(world, pos)) {
            return type.defaultLightValue;
        }
        IBlockState state = getChunkCacheBlockState(world, chunkX, chunkZ, chunks, pos);
        if (state.useNeighborBrightness()) {
            int light = 0;
            for (EnumFacing facing : EnumFacing.values()) {
                light = Math.max(light, getChunkCacheLightFor(world, chunkX, chunkZ,
                        chunks, type, pos.offset(facing)));
                if (light >= 15) {
                    return light;
                }
            }
            return light;
        }
        return getChunkCacheLightFor(world, chunkX, chunkZ, chunks, type, pos);
    }

    public static int getChunkCacheLightFor(World world, int chunkX, int chunkZ,
            Chunk[][] chunks, EnumSkyBlock type, BlockPos pos) {
        if (isOutsideBuildHeight(world, pos)) {
            return type.defaultLightValue;
        }
        int x = (pos.getX() >> 4) - chunkX;
        int z = (pos.getZ() >> 4) - chunkZ;
        return isChunkCacheIndexValid(chunks, x, z) ? chunks[x][z].getLightFor(type, pos)
                : type.defaultLightValue;
    }

    public static boolean isChunkCacheSideSolid(World world, int chunkX, int chunkZ,
            Chunk[][] chunks, BlockPos pos, EnumFacing side, boolean defaultValue) {
        if (isOutsideBuildHeight(world, pos)) {
            return defaultValue;
        }
        int x = (pos.getX() >> 4) - chunkX;
        int z = (pos.getZ() >> 4) - chunkZ;
        if (!isChunkCacheIndexValid(chunks, x, z)) {
            return defaultValue;
        }
        IBlockState state = chunks[x][z].getBlockState(pos);
        return state.getBlock().isSideSolid(state, world, pos, side);
    }

    private static boolean isChunkCacheIndexValid(Chunk[][] chunks, int x, int z) {
        return x >= 0 && x < chunks.length && z >= 0 && z < chunks[x].length && chunks[x][z] != null;
    }

    public static BlockPos getRandomChunkPosition(World world, int chunkX, int chunkZ) {
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int x = chunkX * 16 + world.rand.nextInt(16);
        int z = chunkZ * 16 + world.rand.nextInt(16);
        int height = chunk.getHeight(new BlockPos(x, minY(world), z));
        if (!isExtended(world)) {
            int top = net.minecraft.util.math.MathHelper.roundUp(height + 1, 16);
            int y = world.rand.nextInt(top > 0 ? top : chunk.getTopFilledSegment() + 16 - 1);
            return new BlockPos(x, y, z);
        }
        int minY = minY(world);
        int topExclusive = minY + net.minecraft.util.math.MathHelper.roundUp(height + 1 - minY, 16);
        int range = Math.max(1, topExclusive - minY);
        return new BlockPos(x, minY + world.rand.nextInt(range), z);
    }

    public static List<NextTickListEntry> getPendingBlockUpdates(WorldServer world,
            Chunk chunk, boolean remove) {
        int minX = (chunk.x << 4) - 2;
        int maxX = minX + 18;
        int minZ = (chunk.z << 4) - 2;
        int maxZ = minZ + 18;
        return world.getPendingBlockUpdates(new StructureBoundingBox(minX, minY(world), minZ,
                maxX, maxYExclusive(world), maxZ), remove);
    }

    public static int getLightFor(Chunk chunk, EnumSkyBlock type, BlockPos pos) {
        World world = chunk.getWorld();
        if (type == EnumSkyBlock.SKY && !world.provider.hasSkyLight()) {
            return 0;
        }
        int index = storageIndex(pos.getY(), world);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        if (index < 0 || index >= storage.length) {
            return type.defaultLightValue;
        }
        ExtendedBlockStorage section = storage[index];
        if (section == Chunk.NULL_BLOCK_STORAGE) {
            return chunk.canSeeSky(pos) ? type.defaultLightValue : 0;
        }
        return type == EnumSkyBlock.SKY
                ? section.getSkyLight() == null ? 0
                        : section.getSkyLight(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15)
                : type == EnumSkyBlock.BLOCK
                        ? section.getBlockLight(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15)
                        : type.defaultLightValue;
    }

    public static void setLightFor(Chunk chunk, EnumSkyBlock type, BlockPos pos, int value) {
        World world = chunk.getWorld();
        if (type == EnumSkyBlock.SKY && !world.provider.hasSkyLight()) {
            return;
        }
        int index = storageIndex(pos.getY(), world);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        if (index < 0 || index >= storage.length) {
            return;
        }
        ExtendedBlockStorage section = storage[index];
        if (section == Chunk.NULL_BLOCK_STORAGE) {
            section = new ExtendedBlockStorage(pos.getY() >> 4 << 4, world.provider.hasSkyLight());
            storage[index] = section;
            chunk.generateSkylightMap();
        }
        if (type == EnumSkyBlock.SKY) {
            section.setSkyLight(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, value);
        } else if (type == EnumSkyBlock.BLOCK) {
            section.setBlockLight(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, value);
        }
        chunk.markDirty();
    }

    public static int getLightSubtracted(Chunk chunk, BlockPos pos, int amount) {
        World world = chunk.getWorld();
        int index = storageIndex(pos.getY(), world);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        if (index < 0 || index >= storage.length) {
            return 0;
        }
        ExtendedBlockStorage section = storage[index];
        if (section == Chunk.NULL_BLOCK_STORAGE) {
            return world.provider.hasSkyLight() && amount < EnumSkyBlock.SKY.defaultLightValue
                    ? EnumSkyBlock.SKY.defaultLightValue - amount : 0;
        }
        int x = pos.getX() & 15;
        int y = pos.getY() & 15;
        int z = pos.getZ() & 15;
        int sky = world.provider.hasSkyLight() ? section.getSkyLight(x, y, z) - amount : 0;
        return Math.max(sky, section.getBlockLight(x, y, z));
    }

    public static void generateHeightMap(Chunk chunk, int[] precipitationHeightMap) {
        World world = chunk.getWorld();
        int minY = minY(world);
        int topExclusive = Math.min(maxYExclusive(world), getTopFilledSegment(chunk) + 16);
        int[] heightMap = chunk.getHeightMap();
        Arrays.fill(heightMap, minY);
        Arrays.fill(precipitationHeightMap, -999);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = topExclusive; y > minY; y--) {
                    if (chunk.getBlockLightOpacity(toWorldPos(chunk, x, y - 1, z)) != 0) {
                        heightMap[z << 4 | x] = y;
                        break;
                    }
                }
            }
        }
        chunk.setHeightMap(heightMap);
        chunk.markDirty();
    }

    public static void generateSkylightMap(Chunk chunk, int[] precipitationHeightMap) {
        World world = chunk.getWorld();
        int minY = minY(world);
        int topSectionY = getTopFilledSegment(chunk);
        int topExclusive = Math.min(maxYExclusive(world), topSectionY + 16);
        int[] heightMap = chunk.getHeightMap();
        Arrays.fill(heightMap, minY);
        Arrays.fill(precipitationHeightMap, -999);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = topExclusive; y > minY; y--) {
                    if (chunk.getBlockLightOpacity(new BlockPos(x, y - 1, z)) != 0) {
                        heightMap[z << 4 | x] = y;
                        break;
                    }
                }
                if (!world.provider.hasSkyLight()) {
                    continue;
                }
                int skyLight = 15;
                for (int y = topExclusive - 1; y >= minY && skyLight > 0; y--) {
                    int opacity = chunk.getBlockLightOpacity(toWorldPos(chunk, x, y, z));
                    if (opacity == 0 && skyLight != 15) {
                        opacity = 1;
                    }
                    skyLight -= opacity;
                    if (skyLight <= 0) {
                        break;
                    }
                    int index = storageIndex(y, world);
                    ExtendedBlockStorage section = index < 0 ? Chunk.NULL_BLOCK_STORAGE
                            : chunk.getBlockStorageArray()[index];
                    if (section != Chunk.NULL_BLOCK_STORAGE) {
                        section.setSkyLight(x, y & 15, z, skyLight);
                    }
                }
            }
        }
        chunk.setHeightMap(heightMap);
        chunk.markDirty();
    }

    public static void relightBlock(Chunk chunk, int x, int y, int z) {
        World world = chunk.getWorld();
        int minY = minY(world);
        int[] heightMap = chunk.getHeightMap();
        int oldHeight = heightMap[z << 4 | x];
        int newHeight = Math.max(y, oldHeight);
        while (newHeight > minY
                && chunk.getBlockLightOpacity(toWorldPos(chunk, x, newHeight - 1, z)) == 0) {
            newHeight--;
        }
        if (newHeight == oldHeight) {
            return;
        }

        world.markBlocksDirtyVertical(x + chunk.x * 16, z + chunk.z * 16, newHeight, oldHeight);
        heightMap[z << 4 | x] = newHeight;
        int worldX = chunk.x * 16 + x;
        int worldZ = chunk.z * 16 + z;
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        if (world.provider.hasSkyLight()) {
            int from = Math.min(newHeight, oldHeight);
            int to = Math.max(newHeight, oldHeight);
            int light = newHeight < oldHeight ? 15 : 0;
            for (int currentY = from; currentY < to; currentY++) {
                int index = storageIndex(currentY, world);
                if (index >= 0 && storage[index] != Chunk.NULL_BLOCK_STORAGE) {
                    storage[index].setSkyLight(x, currentY & 15, z, light);
                    world.notifyLightSet(new BlockPos(worldX, currentY, worldZ));
                }
            }

            int skyLight = 15;
            for (int currentY = newHeight - 1; currentY >= minY && skyLight > 0; currentY--) {
                int opacity = chunk.getBlockLightOpacity(toWorldPos(chunk, x, currentY, z));
                skyLight -= opacity == 0 ? 1 : opacity;
                skyLight = Math.max(0, skyLight);
                int index = storageIndex(currentY, world);
                if (index >= 0 && storage[index] != Chunk.NULL_BLOCK_STORAGE) {
                    storage[index].setSkyLight(x, currentY & 15, z, skyLight);
                }
            }
        }

        chunk.setHeightMap(heightMap);
        if (world.provider.hasSkyLight()) {
            int from = Math.min(oldHeight, newHeight);
            int to = Math.max(oldHeight, newHeight);
            for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                updateSkylightNeighborHeight(world, worldX + facing.getFrontOffsetX(),
                        worldZ + facing.getFrontOffsetZ(), from, to);
            }
            updateSkylightNeighborHeight(world, worldX, worldZ, from, to);
        }
        chunk.markDirty();
    }

    private static BlockPos toWorldPos(Chunk chunk, int localX, int y, int localZ) {
        return new BlockPos((chunk.x << 4) + localX, y, (chunk.z << 4) + localZ);
    }

    private static void updateSkylightNeighborHeight(World world, int x, int z, int startY, int endY) {
        if (endY <= startY || !world.isAreaLoaded(new BlockPos(x, minY(world), z), 16)) {
            return;
        }
        for (int y = startY; y < endY; y++) {
            world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(x, y, z));
        }
    }

    public static BlockPos getPrecipitationHeight(Chunk chunk, int[] precipitationHeightMap, BlockPos pos) {
        int localX = pos.getX() & 15;
        int localZ = pos.getZ() & 15;
        int key = localX | localZ << 4;
        if (precipitationHeightMap[key] == -999) {
            World world = chunk.getWorld();
            int minY = minY(world);
            int topY = Math.min(maxYExclusive(world) - 1, getTopFilledSegment(chunk) + 15);
            int result = minY;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), topY, pos.getZ());
            for (int y = topY; y >= minY; y--) {
                cursor.setY(y);
                IBlockState state = chunk.getBlockState(cursor);
                Material material = state.getMaterial();
                if (material.blocksMovement() || material.isLiquid()) {
                    result = y + 1;
                    break;
                }
            }
            precipitationHeightMap[key] = result;
        }
        return new BlockPos(pos.getX(), precipitationHeightMap[key], pos.getZ());
    }

    public static boolean isEmptyBetween(Chunk chunk, int startY, int endY) {
        World world = chunk.getWorld();
        int minY = minY(world);
        int maxY = maxYExclusive(world) - 1;
        startY = Math.max(startY, minY);
        endY = Math.min(endY, maxY);
        if (startY > endY) {
            return true;
        }
        int startSectionY = Math.floorDiv(startY, 16);
        int endSectionY = Math.floorDiv(endY, 16);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        for (int sectionY = startSectionY; sectionY <= endSectionY; sectionY++) {
            int index = storageIndexForSectionY(sectionY, world);
            if (index >= 0 && storage[index] != Chunk.NULL_BLOCK_STORAGE && !storage[index].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static int totalRelightChecks(World world) {
        return sectionCount(world) * 16 * 16;
    }

    public static int enqueueRelightChecks(Chunk chunk, int queuedLightChecks) {
        World world = chunk.getWorld();
        int sectionCount = sectionCount(world);
        int totalChecks = totalRelightChecks(world);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        for (int batch = 0; batch < 8 && queuedLightChecks < totalChecks; batch++, queuedLightChecks++) {
            int sectionIndex = queuedLightChecks % sectionCount;
            int localX = queuedLightChecks / sectionCount % 16;
            int localZ = queuedLightChecks / (sectionCount * 16);
            int baseY = sectionBaseYForStorageIndex(sectionIndex, world);
            ExtendedBlockStorage section = storage[sectionIndex];
            for (int localY = 0; localY < 16; localY++) {
                BlockPos pos = new BlockPos((chunk.x << 4) + localX, baseY + localY,
                        (chunk.z << 4) + localZ);
                boolean edge = localY == 0 || localY == 15 || localX == 0 || localX == 15
                        || localZ == 0 || localZ == 15;
                IBlockState state = section == Chunk.NULL_BLOCK_STORAGE
                        ? Blocks.AIR.getDefaultState() : section.get(localX, localY, localZ);
                if (section == Chunk.NULL_BLOCK_STORAGE && edge
                        || section != Chunk.NULL_BLOCK_STORAGE && state.getBlock().isAir(state, world, pos)) {
                    for (EnumFacing facing : EnumFacing.values()) {
                        BlockPos neighbor = pos.offset(facing);
                        if (world.getBlockState(neighbor).getLightValue(world, neighbor) > 0) {
                            world.checkLight(neighbor);
                        }
                    }
                    world.checkLight(pos);
                }
            }
        }
        return queuedLightChecks;
    }

    public static boolean checkChunkColumnLight(Chunk chunk, int x, int z) {
        World world = chunk.getWorld();
        int minY = minY(world);
        int topY = getTopFilledSegment(chunk) + 15;
        boolean foundOpaque = false;
        boolean foundFullOpaqueBelowSea = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos((chunk.x << 4) + x, minY,
                (chunk.z << 4) + z);
        for (int y = topY; y > world.getSeaLevel() || y > minY && !foundFullOpaqueBelowSea; y--) {
            cursor.setY(y);
            int opacity = chunk.getBlockLightOpacity(cursor);
            if (opacity == 255 && y < world.getSeaLevel()) {
                foundFullOpaqueBelowSea = true;
            }
            if (!foundOpaque && opacity > 0) {
                foundOpaque = true;
            } else if (foundOpaque && opacity == 0 && !world.checkLight(cursor)) {
                return false;
            }
        }
        for (int y = cursor.getY(); y > minY; y--) {
            cursor.setY(y);
            IBlockState state = chunk.getBlockState(cursor);
            if (state.getLightValue(world, cursor) > 0) {
                world.checkLight(cursor);
            }
        }
        return true;
    }

    public static void getEntitiesWithinAABBForEntity(Chunk chunk, Entity excluded, AxisAlignedBB bounds,
            List<Entity> output, Predicate<? super Entity> filter) {
        World world = chunk.getWorld();
        int minSectionY = MathHelper.floor((bounds.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
        int maxSectionY = MathHelper.floor((bounds.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
        int worldMinSectionY = minY(world) >> 4;
        int worldMaxSectionY = (maxYExclusive(world) - 1) >> 4;
        minSectionY = MathHelper.clamp(minSectionY, worldMinSectionY, worldMaxSectionY);
        maxSectionY = MathHelper.clamp(maxSectionY, worldMinSectionY, worldMaxSectionY);
        ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
        for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
            int storageIndex = storageIndexForSectionY(sectionY, world);
            if (storageIndex < 0 || storageIndex >= entityLists.length) {
                continue;
            }
            ClassInheritanceMultiMap<Entity> section = entityLists[storageIndex];
            if (section.isEmpty()) {
                continue;
            }
            for (Entity entity : section) {
                if (entity == excluded || !entity.getEntityBoundingBox().intersects(bounds)) {
                    continue;
                }
                if (filter == null || filter.apply(entity)) {
                    output.add(entity);
                }
                Entity[] parts = entity.getParts();
                if (parts == null) {
                    continue;
                }
                for (Entity part : parts) {
                    if (part != excluded && part.getEntityBoundingBox().intersects(bounds)
                            && (filter == null || filter.apply(part))) {
                        output.add(part);
                    }
                }
            }
        }
    }

    public static <T extends Entity> void getEntitiesOfTypeWithinAABB(Chunk chunk, Class<? extends T> type,
            AxisAlignedBB bounds, List<T> output, Predicate<? super T> filter) {
        World world = chunk.getWorld();
        int minSectionY = MathHelper.floor((bounds.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
        int maxSectionY = MathHelper.floor((bounds.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
        int worldMinSectionY = minY(world) >> 4;
        int worldMaxSectionY = (maxYExclusive(world) - 1) >> 4;
        minSectionY = MathHelper.clamp(minSectionY, worldMinSectionY, worldMaxSectionY);
        maxSectionY = MathHelper.clamp(maxSectionY, worldMinSectionY, worldMaxSectionY);
        ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
        for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
            int storageIndex = storageIndexForSectionY(sectionY, world);
            if (storageIndex < 0 || storageIndex >= entityLists.length) {
                continue;
            }
            ClassInheritanceMultiMap<Entity> section = entityLists[storageIndex];
            for (T entity : section.getByClass(type)) {
                if (entity.getEntityBoundingBox().intersects(bounds)
                        && (filter == null || filter.apply(entity))) {
                    output.add(entity);
                }
            }
        }
    }
}
