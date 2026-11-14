package xy177.farmersfuturedelight.common.world.modern;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import xy177.farmersfuturedelight.common.world.terrain.FFDBiomeTerrainBridge;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernAquifer;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeProvider;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeSampler;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeZoomer;
import xy177.farmersfuturedelight.common.block.BlockGlowLichen;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.worldgen.WorldGenAmethystGeodes;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernOreVeins;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernFossils;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernIcebergs;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernOres;
import xy177.farmersfuturedelight.common.worldgen.WorldGenUnderwaterMagma;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernMountainBiomes;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernBamboo;
import xy177.farmersfuturedelight.common.worldgen.WorldGenVerticalCaveBiomes;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernCarvers;
import xy177.farmersfuturedelight.common.worldgen.FFDStructureHooks;
import xy177.farmersfuturedelight.common.worldgen.FFDStructureBeard;
import xy177.farmersfuturedelight.common.worldgen.FFDModernStructures;
import xy177.farmersfuturedelight.common.worldgen.FFDLushCaveBlockProvider;
import xy177.farmersfuturedelight.common.worldgen.FFDModernStoneProvider;
import xy177.farmersfuturedelight.common.worldgen.MapGenLushCaves;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.BitSet;

public final class ModernTerrainPipeline {
    private static final IBlockState STONE = Blocks.STONE.getDefaultState();
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();
    private static final EnumFacing[] DIRECTIONS = EnumFacing.values();
    private final WorldGenAmethystGeodes amethystGeodes;
    private final WorldGenModernOres modernOres;
    private final WorldGenUnderwaterMagma underwaterMagma;
    private final WorldGenVerticalCaveBiomes caveBiomes;
    private final WorldGenModernCarvers modernCarvers;
    private final FFDModernStoneProvider modernStones;
    private final FFDLushCaveBlockProvider lushBlocks;
    private final FFDModernWorldgenData worldgenData;
    private final FFDBiomeTerrainBridge terrainBridge;
    private final ModernSurfaceStage surfaceRules;
    private final FFDVerticalBiomeSampler verticalBiomeSampler;
    private final long biomeZoomSeed;
    private final ModernNoiseContext noiseContext;
    private Biome[] biomes;
    private final Long2ObjectLinkedOpenHashMap<TerrainSample> terrainSamples = new Long2ObjectLinkedOpenHashMap<>(128);
    private final Long2IntLinkedOpenHashMap preliminaryHeights = new Long2IntLinkedOpenHashMap(32768);

    ModernTerrainPipeline(long seed, ModernNoiseContext context, FFDModernStoneProvider stones) {
        noiseContext = context;
        preliminaryHeights.defaultReturnValue(Integer.MIN_VALUE);
        worldgenData = context.data;
        modernStones = stones;
        lushBlocks = FFDLushCaveBlockProvider.get();
        amethystGeodes = new WorldGenAmethystGeodes(seed);
        modernOres = new WorldGenModernOres(seed, stones);
        underwaterMagma = new WorldGenUnderwaterMagma(seed);
        caveBiomes = new WorldGenVerticalCaveBiomes(seed, stones);
        modernCarvers = new WorldGenModernCarvers(seed);
        verticalBiomeSampler = new FFDVerticalBiomeSampler(worldgenData);
        terrainBridge = new FFDBiomeTerrainBridge(new FFDModernBiomeResolver(worldgenData, seed, true));
        surfaceRules = new ModernSurfaceStage(seed, worldgenData, verticalBiomeSampler, stones,
                noiseContext::preliminaryHeight);
        biomeZoomSeed = FFDVerticalBiomeZoomer.obfuscateSeed(seed);
    }

    void finish(ModernProtoChunk prepared, ModernGenerationInput input, boolean skylight) {
        long stageStart = beginStage();
        int chunkX = input.chunkX;
        int chunkZ = input.chunkZ;
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        ModernTerrainBuffer chunk = new ModernTerrainBuffer(prepared.sections(), input, skylight);
        TerrainSample terrainSample = remember(prepared.noise);
        terrainSample.rememberSurfaceHeights(prepared.surfaceHeights());
        FFDModernAquifer aquifer = terrainSample.aquifer();
        GenerationRegion generationRegion = new GenerationRegion();
        int[] oceanFloorY = prepared.surfaceHeights();
        List<Integer> fluidUpdates = new ArrayList<>();
        for (int packed : prepared.fluidUpdates()) fluidUpdates.add(packed);
        biomes = input.biomes();
        FFDVerticalBiomeData verticalBiomes = verticalBiomeSampler.sampleChunk(chunkX, chunkZ);
        surfaceRules.apply(chunk, biomes, verticalBiomes, terrainBridge, startX, startZ);
        ModernTerrainProfiler.record(0, System.nanoTime() - stageStart);
        stageStart = beginStage();
        BitSet carvedPositions = modernCarvers.generate(chunkX, chunkZ, new WorldGenModernCarvers.Access() {
            @Override
            public IBlockState getState(int localX, int blockY, int localZ) { return chunk.stateAt(localX, blockY, localZ); }
            @Override
            public void setState(int localX, int blockY, int localZ, IBlockState state) { chunk.setState(localX, blockY, localZ, state); }
            @Override
            public IBlockState carveState(int localX, int blockY, int localZ) {
                return aquifer.computeSubstance(startX + localX, blockY, startZ + localZ, 0.0D);
            }
            @Override
            public boolean shouldScheduleFluidUpdate() { return aquifer.shouldScheduleFluidUpdate(); }
            @Override
            public void scheduleFluidUpdate(int localX, int blockY, int localZ) {
                fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(localX, blockY, localZ));
            }
            @Override
            public int surfaceY(int localX, int localZ) { return oceanFloorY[localZ * 16 + localX]; }
        });
        ModernTerrainProfiler.record(1, System.nanoTime() - stageStart);
        stageStart = beginStage();
        recomputeOceanFloor(chunk, oceanFloorY);
        clearLegacyBedrock(chunk, prepared.noise, aquifer, startX, startZ, fluidUpdates);
        Set<Integer> generatedGlowLichen = new HashSet<>();
        NeighborAwareCaveAccess caveAccess = caveBiomeAccess(chunk, chunkX, chunkZ, verticalBiomes,
                fluidUpdates, oceanFloorY, generationRegion, generatedGlowLichen);
        chunk.setNeighborReader(caveAccess::getState);
        applyPostCarverCaveSurfaces(carvedPositions, caveAccess, chunkX, chunkZ, generationRegion);
        ModernTerrainProfiler.record(2, System.nanoTime() - stageStart);
        stageStart = beginStage();
        generateAmethystGeodes(chunk, chunkX, chunkZ, fluidUpdates, generationRegion);
        caveBiomes.generateLocalModifications(chunkX, chunkZ, verticalBiomes, caveAccess);
        ModernTerrainProfiler.record(3, System.nanoTime() - stageStart);
        stageStart = beginStage();
        generateModernOres(chunk, chunkX, chunkZ, generationRegion);
        generateUnderwaterMagma(chunk, chunkX, chunkZ, generationRegion);
        ModernTerrainProfiler.record(4, System.nanoTime() - stageStart);
        stageStart = beginStage();
        caveBiomes.generateUndergroundDecoration(chunkX, chunkZ, verticalBiomes, caveAccess);
        ModernTerrainProfiler.record(5, System.nanoTime() - stageStart);
        stageStart = beginStage();
        caveAccess.setPostCarverNeighbors(false);
        caveBiomes.generateVegetation(chunkX, chunkZ, verticalBiomes, caveAccess);
        caveAccess.setPostCarverNeighbors(true);
        validateGeneratedGlowLichen(chunk, caveAccess, generatedGlowLichen, chunkX, chunkZ);
        ModernTerrainProfiler.record(6, System.nanoTime() - stageStart);
        prepared.completeTerrain(verticalBiomes, fluidUpdates);
    }

    private static long beginStage() {
        if (Thread.currentThread().isInterrupted()) {
            throw new java.util.concurrent.CancellationException("Modern terrain generation interrupted");
        }
        return System.nanoTime();
    }

    private void generateModernOres(ModernTerrainBuffer chunk, int chunkX, int chunkZ,
                                    GenerationRegion generationRegion) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        modernOres.generateChunk(chunkX, chunkZ, new WorldGenModernOres.Access() {
            @Override
            public IBlockState getState(int blockX, int blockY, int blockZ) {
                if (blockY < FFDModernWorldgenData.MIN_Y
                        || blockY >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                if (blockX >> 4 == chunkX && blockZ >> 4 == chunkZ) {
                    return chunk.stateAt(blockX, blockY, blockZ);
                }
                int sampleChunkX = blockX >> 4;
                int sampleChunkZ = blockZ >> 4;
                TerrainSample sample = generationRegion.sample(sampleChunkX, sampleChunkZ);
                return sample.baseStateAt(blockX - (sampleChunkX << 4), blockY,
                        blockZ - (sampleChunkZ << 4), blockX, blockZ, surfaceRules);
            }

            @Override
            public void setState(int blockX, int blockY, int blockZ, IBlockState state) {
                setDirect(chunk, blockX & 15, blockY, blockZ & 15, state);
            }

            @Override
            public Biome getBiome(int blockX, int blockZ) {
                if (blockX >= startX && blockX < startX + 16 && blockZ >= startZ && blockZ < startZ + 16) {
                    return biomes[(blockZ - startZ) * 16 + blockX - startX];
                }
                return chunk.getBiome(new BlockPos(blockX, 0, blockZ));
            }

            @Override
            public int targetMinX() {
                return startX;
            }

            @Override
            public int targetMaxX() {
                return startX + 15;
            }

            @Override
            public int targetMinZ() {
                return startZ;
            }

            @Override
            public int targetMaxZ() {
                return startZ + 15;
            }
        });
    }

    private void generateUnderwaterMagma(ModernTerrainBuffer chunk, int chunkX, int chunkZ,
                                         GenerationRegion generationRegion) {
        underwaterMagma.generateChunk(chunkX, chunkZ, new WorldGenUnderwaterMagma.Access() {
            @Override
            public IBlockState getState(int blockX, int blockY, int blockZ) {
                if (blockY < FFDModernWorldgenData.MIN_Y
                        || blockY >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                if (blockX >> 4 == chunkX && blockZ >> 4 == chunkZ) {
                    return chunk.stateAt(blockX, blockY, blockZ);
                }
                int sampleChunkX = blockX >> 4;
                int sampleChunkZ = blockZ >> 4;
                TerrainSample sample = generationRegion.sample(sampleChunkX, sampleChunkZ);
                return sample.baseStateAt(blockX - (sampleChunkX << 4), blockY,
                        blockZ - (sampleChunkZ << 4), blockX, blockZ, surfaceRules);
            }

            @Override
            public void setState(int blockX, int blockY, int blockZ, IBlockState state) {
                setDirect(chunk, blockX & 15, blockY, blockZ & 15, state);
            }

            @Override
            public int surfaceY(int blockX, int blockZ) {
                int sampleChunkX = blockX >> 4;
                int sampleChunkZ = blockZ >> 4;
                TerrainSample sample = generationRegion.sample(sampleChunkX, sampleChunkZ);
                return sample.surfaceY(blockX - (sampleChunkX << 4),
                        blockZ - (sampleChunkZ << 4), surfaceRules);
            }

            @Override
            public boolean isBelowSurface(int blockX, int blockY, int blockZ) {
                int sampleChunkX = blockX >> 4;
                int sampleChunkZ = blockZ >> 4;
                TerrainSample sample = generationRegion.sample(sampleChunkX, sampleChunkZ);
                return sample.isBelowSurface(blockX & 15, blockY, blockZ & 15, surfaceRules);
            }

            @Override
            public BlockFaceShape getFaceShape(int blockX, int blockY, int blockZ, EnumFacing face) {
                BlockPos pos = new BlockPos(blockX, blockY, blockZ);
                return getState(blockX, blockY, blockZ).getBlockFaceShape(chunk, pos, face);
            }
        });
    }

    private void generateAmethystGeodes(ModernTerrainBuffer chunk, int chunkX, int chunkZ,
                                        List<Integer> fluidUpdates,
                                        GenerationRegion generationRegion) {
        amethystGeodes.generateChunk(chunkX, chunkZ, new WorldGenAmethystGeodes.Access() {
            @Override
            public IBlockState getState(BlockPos pos) {
                int blockY = pos.getY();
                if (blockY < FFDModernWorldgenData.MIN_Y
                        || blockY >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                if (isTargetChunk(pos)) {
                    return chunk.getBlockState(pos);
                }
                int sampleChunkX = pos.getX() >> 4;
                int sampleChunkZ = pos.getZ() >> 4;
                TerrainSample sample = generationRegion.sample(sampleChunkX, sampleChunkZ);
                return sample.baseStateAt(pos.getX() - (sampleChunkX << 4), blockY,
                        pos.getZ() - (sampleChunkZ << 4), pos.getX(), pos.getZ(), surfaceRules);
            }

            @Override
            public boolean isTargetChunk(BlockPos pos) {
                return pos.getX() >> 4 == chunkX && pos.getZ() >> 4 == chunkZ;
            }

            @Override
            public void setState(BlockPos pos, IBlockState state) {
                setDirect(chunk, pos.getX() & 15, pos.getY(), pos.getZ() & 15, state);
            }

            @Override
            public void markFluidUpdate(BlockPos pos) {
                fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(
                        pos.getX() & 15, pos.getY(), pos.getZ() & 15));
            }
        });
    }

    private NeighborAwareCaveAccess caveBiomeAccess(ModernTerrainBuffer chunk, int chunkX, int chunkZ,
                                                    FFDVerticalBiomeData verticalBiomes,
                                                    List<Integer> fluidUpdates,
                                                    int[] oceanFloorY,
                                                    GenerationRegion generationRegion,
                                                    Set<Integer> generatedGlowLichen) {
        Long2ObjectOpenHashMap<IBlockState> decorationOverlay = new Long2ObjectOpenHashMap<>();
        Long2ObjectOpenHashMap<FFDVerticalBiomeData> verticalBiomeCache = new Long2ObjectOpenHashMap<>();
        verticalBiomeCache.put(chunkKey(chunkX, chunkZ), verticalBiomes);
        FFDVerticalBiomeZoomer.Source verticalBiomeSource = (quartX, quartY, quartZ) -> {
            int sampleChunkX = Math.floorDiv(quartX, FFDVerticalBiomeData.CELLS_X);
            int sampleChunkZ = Math.floorDiv(quartZ, FFDVerticalBiomeData.CELLS_Z);
            long key = chunkKey(sampleChunkX, sampleChunkZ);
            FFDVerticalBiomeData data = verticalBiomeCache.get(key);
            if (data == null) {
                data = verticalBiomeSampler.sampleChunk(sampleChunkX, sampleChunkZ);
                verticalBiomeCache.put(key, data);
            }
            return data.get(quartX * FFDVerticalBiomeData.CELL_SIZE,
                    quartY * FFDVerticalBiomeData.CELL_SIZE,
                    quartZ * FFDVerticalBiomeData.CELL_SIZE);
        };
        return new NeighborAwareCaveAccess() {
            private boolean postCarverNeighbors = true;
            private int lastSampleChunkX = Integer.MIN_VALUE;
            private int lastSampleChunkZ = Integer.MIN_VALUE;
            private TerrainSample lastSample;

            @Override
            public void setPostCarverNeighbors(boolean enabled) {
                postCarverNeighbors = enabled;
            }

            @Override
            public IBlockState getState(int blockX, int blockY, int blockZ) {
                if (blockY < FFDModernWorldgenData.MIN_Y
                        || blockY >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                IBlockState overlayState = decorationOverlay.get(blockKey(blockX, blockY, blockZ));
                if (overlayState != null) {
                    return overlayState;
                }
                if (isTargetChunk(blockX, blockZ)) {
                    return getDirect(chunk, blockX & 15, blockY, blockZ & 15);
                }
                int sampleChunkX = blockX >> 4;
                int sampleChunkZ = blockZ >> 4;
                TerrainSample sample = sampleAt(sampleChunkX, sampleChunkZ);
                int localX = blockX - (sampleChunkX << 4);
                int localZ = blockZ - (sampleChunkZ << 4);
                return postCarverNeighbors
                        ? sample.postCarverStateAt(localX, blockY, localZ, blockX, blockZ, surfaceRules)
                        : sample.baseStateAt(localX, blockY, localZ, blockX, blockZ, surfaceRules);
            }

            @Override
            public IBlockState getPlanningState(int blockX, int blockY, int blockZ) {
                if (blockY < FFDModernWorldgenData.MIN_Y
                        || blockY >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                int sampleChunkX = blockX >> 4;
                int sampleChunkZ = blockZ >> 4;
                TerrainSample sample = sampleAt(sampleChunkX, sampleChunkZ);
                return sample.postCarverStateAt(blockX - (sampleChunkX << 4), blockY,
                        blockZ - (sampleChunkZ << 4), blockX, blockZ, surfaceRules);
            }

            @Override
            public BlockFaceShape getFaceShape(int blockX, int blockY, int blockZ,
                                               net.minecraft.util.EnumFacing face) {
                BlockPos pos = new BlockPos(blockX, blockY, blockZ);
                return getState(blockX, blockY, blockZ).getBlockFaceShape(chunk, pos, face);
            }

            @Override
            public boolean isTargetChunk(int blockX, int blockZ) {
                return blockX >> 4 == chunkX && blockZ >> 4 == chunkZ;
            }

            @Override
            public FFDVerticalBiome getBiome(int blockX, int blockY, int blockZ) {
                return FFDVerticalBiomeZoomer.getBiome(
                        biomeZoomSeed, blockX, blockY, blockZ, verticalBiomeSource);
            }

            @Override
            public void setState(int blockX, int blockY, int blockZ, IBlockState state) {
                if (blockY < FFDModernWorldgenData.MIN_Y
                        || blockY >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return;
                }
                decorationOverlay.put(blockKey(blockX, blockY, blockZ), state);
                if (isTargetChunk(blockX, blockZ)) {
                    int packed = FFDHeightHooks.packLocalBlockChange(blockX & 15, blockY, blockZ & 15);
                    if (BlockGlowLichen.isGlowLichen(state)) {
                        generatedGlowLichen.add(packed);
                    } else {
                        generatedGlowLichen.remove(packed);
                    }
                    setDirect(chunk, blockX & 15, blockY, blockZ & 15, state);
                }
            }

            @Override
            public void markFluidUpdate(int blockX, int blockY, int blockZ) {
                if (isTargetChunk(blockX, blockZ)) {
                    fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(blockX & 15, blockY, blockZ & 15));
                }
            }

            @Override
            public int getOceanFloorY(int blockX, int blockZ) {
                if (isTargetChunk(blockX, blockZ)) {
                    return oceanFloorY[(blockZ & 15) * 16 + (blockX & 15)];
                }
                long key = chunkKey(blockX, blockZ);
                int height = preliminaryHeights.getAndMoveToLast(key);
                if (height == Integer.MIN_VALUE) {
                    height = noiseContext.preliminaryHeight(blockX, blockZ);
                    preliminaryHeights.put(key, height);
                    if (preliminaryHeights.size() > 32768) preliminaryHeights.removeFirstInt();
                }
                return height;
            }

            @Override
            public int getPlanningOceanFloorY(int blockX, int blockZ) {
                int sampleChunkX = blockX >> 4;
                int sampleChunkZ = blockZ >> 4;
                TerrainSample sample = sampleAt(sampleChunkX, sampleChunkZ);
                return sample.surfaceY(blockX - (sampleChunkX << 4), blockZ - (sampleChunkZ << 4),
                        surfaceRules);
            }

            private TerrainSample sampleAt(int sampleChunkX, int sampleChunkZ) {
                if (lastSample == null || sampleChunkX != lastSampleChunkX
                        || sampleChunkZ != lastSampleChunkZ) {
                    lastSampleChunkX = sampleChunkX;
                    lastSampleChunkZ = sampleChunkZ;
                    lastSample = generationRegion.sample(sampleChunkX, sampleChunkZ);
                }
                return lastSample;
            }
        };
    }

    private interface NeighborAwareCaveAccess extends WorldGenVerticalCaveBiomes.Access {
        void setPostCarverNeighbors(boolean enabled);
    }

    private void applyPostCarverCaveSurfaces(BitSet carvedPositions,
                                             WorldGenVerticalCaveBiomes.Access access,
                                             int chunkX, int chunkZ,
                                             GenerationRegion generationRegion) {
        boolean profile = ModernTerrainProfiler.ENABLED;
        long buildStart = profile ? System.nanoTime() : 0L;
        BitSet candidates = new BitSet(16 * 16 * FFDModernWorldgenData.HEIGHT);
        BitSet floors = new BitSet(16 * 16 * FFDModernWorldgenData.HEIGHT);
        BitSet ceilings = new BitSet(16 * 16 * FFDModernWorldgenData.HEIGHT);
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        for (int carvedIndex = carvedPositions.nextSetBit(0); carvedIndex >= 0;
             carvedIndex = carvedPositions.nextSetBit(carvedIndex + 1)) {
            int localX = WorldGenModernCarvers.localXAt(carvedIndex);
            int localZ = WorldGenModernCarvers.localZAt(carvedIndex);
            int blockY = WorldGenModernCarvers.blockYAt(carvedIndex);
            if (localX > 0 && !carvedPositions.get(carvedIndex - 1)) {
                candidates.set(carvedIndex - 1);
            }
            if (localX < 15 && !carvedPositions.get(carvedIndex + 1)) {
                candidates.set(carvedIndex + 1);
            }
            if (localZ > 0 && !carvedPositions.get(carvedIndex - 16)) {
                candidates.set(carvedIndex - 16);
            }
            if (localZ < 15 && !carvedPositions.get(carvedIndex + 16)) {
                candidates.set(carvedIndex + 16);
            }
            if (blockY > FFDModernWorldgenData.MIN_Y
                    && !carvedPositions.get(carvedIndex - 256)) {
                int floor = carvedIndex - 256;
                candidates.set(floor);
                floors.set(floor);
            }
            if (blockY < FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1
                    && !carvedPositions.get(carvedIndex + 256)) {
                int ceiling = carvedIndex + 256;
                candidates.set(ceiling);
                ceilings.set(ceiling);
            }
        }
        addCrossChunkCarverWalls(candidates, generationRegion,
                chunkX - 1, chunkZ, 15, true, 0, startX - 1, startZ);
        addCrossChunkCarverWalls(candidates, generationRegion,
                chunkX + 1, chunkZ, 0, true, 15, startX + 16, startZ);
        addCrossChunkCarverWalls(candidates, generationRegion,
                chunkX, chunkZ - 1, 15, false, 0, startX, startZ - 1);
        addCrossChunkCarverWalls(candidates, generationRegion,
                chunkX, chunkZ + 1, 0, false, 15, startX, startZ + 16);
        long buildNanos = profile ? System.nanoTime() - buildStart : 0L;

        IBlockState tuff = caveTuffState();
        IBlockState dripstone = modernStones.dripstoneBlock();
        long stateNanos = 0L;
        long patchNanos = 0L;
        long biomeNanos = 0L;
        long fallbackNanos = 0L;
        int candidateCount = 0;
        int baseCount = 0;
        int biomeQueries = 0;
        int fallbackQueries = 0;
        int writes = 0;
        for (int index = candidates.nextSetBit(0); index >= 0;
             index = candidates.nextSetBit(index + 1)) {
            boolean sampleTiming = profile && (candidateCount++ & 15) == 0;
            int localX = index & 15;
            int localZ = index >> 4 & 15;
            int blockY = (index >> 8) + FFDModernWorldgenData.MIN_Y;
            int blockX = startX + localX;
            int blockZ = startZ + localZ;
            long operationStart = sampleTiming ? System.nanoTime() : 0L;
            IBlockState current = access.getState(blockX, blockY, blockZ);
            if (!isCaveSurfaceBase(current, blockX, blockY, blockZ)) {
                if (sampleTiming) {
                    stateNanos += System.nanoTime() - operationStart;
                }
                continue;
            }
            if (sampleTiming) {
                stateNanos += System.nanoTime() - operationStart;
            }
            baseCount++;

            boolean floor = floors.get(index);
            boolean ceiling = ceilings.get(index);
            operationStart = sampleTiming ? System.nanoTime() : 0L;
            double patch = worldgenData.samplePatchNoise(blockX, blockY, blockZ);
            if (sampleTiming) {
                patchNanos += System.nanoTime() - operationStart;
            }
            IBlockState replacement = null;
            boolean biomeReplacementPossible = floor ? patch > -0.12D
                    : ceiling ? patch > -0.08D : patch > 0.30D;
            if (biomeReplacementPossible) {
                biomeQueries++;
                operationStart = sampleTiming ? System.nanoTime() : 0L;
                FFDVerticalBiome biome = access.getBiome(blockX, blockY, blockZ);
                if (sampleTiming) {
                    biomeNanos += System.nanoTime() - operationStart;
                }
                if (biome == FFDVerticalBiome.LUSH_CAVES) {
                    if (floor && patch > -0.12D && lushBlocks.hasMoss()) {
                        replacement = lushBlocks.mossBlock();
                    } else if ((ceiling || !floor) && patch > 0.34D
                            && lushBlocks.hasRootedDirt()) {
                        replacement = lushBlocks.rootedDirt();
                    }
                } else if (biome == FFDVerticalBiome.DRIPSTONE_CAVES && dripstone != null) {
                    double threshold = floor || ceiling ? -0.08D : 0.30D;
                    if (patch > threshold) {
                        replacement = dripstone;
                    }
                }
            }
            if (replacement == null && blockY < 32 && tuff != null && patch > 0.30D) {
                replacement = tuff;
            }
            if (replacement == null && floor) {
                fallbackQueries++;
                operationStart = sampleTiming ? System.nanoTime() : 0L;
                double gravel = worldgenData.sampleGravelLayerNoise(blockX, blockY, blockZ);
                if (gravel > 0.62D) {
                    replacement = Blocks.GRAVEL.getDefaultState();
                } else if (worldgenData.sampleSurfaceSecondaryNoise(blockX, blockY, blockZ) < -0.50D) {
                    replacement = Blocks.DIRT.getDefaultState();
                }
                if (sampleTiming) {
                    fallbackNanos += System.nanoTime() - operationStart;
                }
            }
            if (replacement != null && replacement.getBlock() != current.getBlock()) {
                access.setState(blockX, blockY, blockZ, replacement);
                writes++;
            }
        }
        if (profile) {
            ModernTerrainProfiler.recordCaveSurface(buildNanos,
                    stateNanos * 16L, patchNanos * 16L, biomeNanos * 16L,
                    fallbackNanos * 16L, candidateCount, baseCount,
                    biomeQueries, fallbackQueries, writes);
        }
    }

    private void addCrossChunkCarverWalls(BitSet candidates, GenerationRegion generationRegion,
                                          int neighborChunkX, int neighborChunkZ,
                                          int neighborEdge, boolean xAxis, int targetEdge,
                                          int neighborStartX, int neighborStartZ) {
        TerrainSample sample = generationRegion.sample(neighborChunkX, neighborChunkZ);
        WorldGenModernCarvers.CarvingMask mask = sample.carvingMask();
        int[] entries = mask.edgeEntries(neighborEdge, xAxis);
        for (int entry : entries) {
            int edgeOffset = entry >> 9;
            int blockY = (entry & 0x1FF) - 63;
            int neighborLocalX = xAxis ? neighborEdge : edgeOffset;
            int neighborLocalZ = xAxis ? edgeOffset : neighborEdge;
            int worldX = xAxis ? neighborStartX : neighborStartX + edgeOffset;
            int worldZ = xAxis ? neighborStartZ + edgeOffset : neighborStartZ;
            IBlockState neighborState = sample.carvedStateAt(
                    neighborLocalX, blockY, neighborLocalZ, worldX, worldZ, surfaceRules);
            if (neighborState == null || !isCaveOpening(neighborState)) {
                continue;
            }
            int targetX = xAxis ? targetEdge : edgeOffset;
            int targetZ = xAxis ? edgeOffset : targetEdge;
            candidates.set(caveSurfaceIndex(targetX, blockY, targetZ));
        }
    }

    private boolean isCaveSurfaceBase(IBlockState state, int blockX, int blockY, int blockZ) {
        Block block = state.getBlock();
        return block == Blocks.STONE || modernStones.isDeepBase(state)
                || block == surfaceRules.stoneStateAt(blockX, blockY, blockZ).getBlock();
    }

    private static boolean isCaveOpening(IBlockState state) {
        return state.getMaterial() == net.minecraft.block.material.Material.AIR
                || state.getMaterial() == net.minecraft.block.material.Material.WATER
                || state.getMaterial() == net.minecraft.block.material.Material.LAVA;
    }

    @Nullable
    private IBlockState caveTuffState() {
        return modernStones.tuff();
    }

    private static int caveSurfaceIndex(int localX, int blockY, int localZ) {
        return ((blockY - FFDModernWorldgenData.MIN_Y) * 16 + localZ) * 16 + localX;
    }

    private void validateGeneratedGlowLichen(ModernTerrainBuffer chunk, WorldGenVerticalCaveBiomes.Access access,
                                             Set<Integer> generatedGlowLichen,
                                             int chunkX, int chunkZ) {
        for (int packed : new ArrayList<>(generatedGlowLichen)) {
            int localX = packed >> 12 & 15;
            int localZ = packed >> 8 & 15;
            int blockY = FFDHeightHooks.unpackLocalBlockY(packed);
            int blockX = (chunkX << 4) + localX;
            int blockZ = (chunkZ << 4) + localZ;
            IBlockState state = access.getState(blockX, blockY, blockZ);
            if (!BlockGlowLichen.isGlowLichen(state)) {
                continue;
            }
            int oldMask = BlockGlowLichen.getFaceMask(state);
            int validMask = oldMask;
            for (EnumFacing face : DIRECTIONS) {
                if ((validMask & BlockGlowLichen.faceBit(face)) == 0) {
                    continue;
                }
                int supportX = blockX + face.getDirectionVec().getX();
                int supportY = blockY + face.getDirectionVec().getY();
                int supportZ = blockZ + face.getDirectionVec().getZ();
                BlockPos supportPos = new BlockPos(supportX, supportY, supportZ);
                IBlockState support = access.getState(supportX, supportY, supportZ);
                net.minecraft.util.EnumFacing supportFace = face.getOpposite();
                if (!support.isSideSolid(chunk, supportPos, supportFace)
                        && access.getFaceShape(supportX, supportY, supportZ,
                        supportFace) != BlockFaceShape.SOLID) {
                    validMask &= ~BlockGlowLichen.faceBit(face);
                }
            }
            if (validMask != oldMask) {
                boolean waterlogged = BlockGlowLichen.isWaterlogged(state);
                access.setState(blockX, blockY, blockZ, BlockGlowLichen.stateFor(validMask, waterlogged));
                if (waterlogged && validMask == 0) {
                    access.markFluidUpdate(blockX, blockY, blockZ);
                }
            }
        }
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return (chunkX & 0xFFFFFFFFL) | (long) chunkZ << 32;
    }

    private void clearLegacyBedrock(ModernTerrainBuffer chunk, ModernNoiseChunk noise,
                                    FFDModernAquifer aquifer, int startX, int startZ,
                                    List<Integer> fluidUpdates) {
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int blockY = 0; blockY < 5; blockY++) {
                    if (chunk.stateAt(localX, blockY, localZ).getBlock() != Blocks.BEDROCK) continue;
                    int blockX = startX + localX;
                    int blockZ = startZ + localZ;
                    IBlockState state = aquifer.computeSubstance(blockX, blockY, blockZ, noise.sample(localX, blockY, localZ));
                    chunk.setState(localX, blockY, localZ, state == null ? surfaceRules.stoneStateAt(blockX, blockY, blockZ) : state);
                    if (aquifer.shouldScheduleFluidUpdate() && state != null && isFluid(state)) {
                        fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(localX, blockY, localZ));
                    }
                }
            }
        }
    }

    private static long blockKey(int blockX, int blockY, int blockZ) {
        return ((long) blockX & 0x3FFFFFFL) << 38
                | ((long) blockZ & 0x3FFFFFFL) << 12
                | (long) blockY & 0xFFFL;
    }

    private static IBlockState getDirect(ModernTerrainBuffer chunk, int localX, int blockY, int localZ) {
        return chunk.stateAt(localX, blockY, localZ);
    }

    private static boolean isAir(IBlockState state) {
        return state == null || state.getBlock() == Blocks.AIR;
    }

    private static void recomputeOceanFloor(ModernTerrainBuffer chunk, int[] oceanFloorY) {
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int found = FFDModernWorldgenData.MIN_Y - 1;
                for (int blockY = FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1;
                     blockY >= FFDModernWorldgenData.MIN_Y; blockY--) {
                    if (chunk.stateAt(localX, blockY, localZ)
                            .getMaterial().isSolid()) {
                        found = blockY;
                        break;
                    }
                }
                oceanFloorY[localZ * 16 + localX] = found;
            }
        }
    }

    private static boolean isFluid(IBlockState state) {
        return state != null && isFluid(state.getBlock());
    }

    private static boolean isFluid(Block block) {
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER
                || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
    }

    private static int densityIndex(int localX, int blockY, int localZ) {
        return ((localX * 16 + localZ) * FFDModernWorldgenData.HEIGHT)
                + blockY - FFDModernWorldgenData.MIN_Y;
    }

    private TerrainSample terrainSample(int chunkX, int chunkZ) {
        long key = chunkKey(chunkX, chunkZ);
        TerrainSample sample = terrainSamples.getAndMoveToLast(key);
        if (sample == null) sample = remember(noiseContext.sample(chunkX, chunkZ));
        return sample;
    }

    private TerrainSample remember(ModernNoiseChunk noise) {
        long key = chunkKey(noise.chunkX, noise.chunkZ);
        TerrainSample cached = terrainSamples.getAndMoveToLast(key);
        if (cached != null) return cached;
        TerrainSample sample = new TerrainSample(noiseContext, noise, modernCarvers, noise.chunkX, noise.chunkZ);
        terrainSamples.put(key, sample);
        if (terrainSamples.size() > 128) terrainSamples.removeFirst();
        return sample;
    }

    private final class GenerationRegion {
        private final Long2ObjectOpenHashMap<TerrainSample> local = new Long2ObjectOpenHashMap<>();

        private TerrainSample sample(int chunkX, int chunkZ) {
            long key = chunkKey(chunkX, chunkZ);
            TerrainSample sample = local.get(key);
            if (sample == null) {
                sample = terrainSample(chunkX, chunkZ);
                local.put(key, sample);
            }
            return sample;
        }
    }

    private static final class TerrainSample {
        private final ModernNoiseContext context;
        private final ModernNeighborStates baseStates;
        private final ModernNoiseChunk densitySampler;
        private FFDModernAquifer aquifer;
        private final WorldGenModernCarvers modernCarvers;
        private final int chunkX;
        private final int chunkZ;
        private final int[] surfaceHeights = new int[16 * 16];
        private final int[] surfaceLowerBounds = new int[16 * 16];
        private final int[] surfaceUpperBounds = new int[16 * 16];
        private final ModernNoiseChunk.Column[] densityColumns = new ModernNoiseChunk.Column[256];
        private WorldGenModernCarvers.CarvingMask carvingMask;

        private TerrainSample(ModernNoiseContext context, ModernNoiseChunk noise,
                              WorldGenModernCarvers modernCarvers, int chunkX, int chunkZ) {
            this.context = context;
            baseStates = context.cache.states(chunkX, chunkZ);
            densitySampler = noise;
            this.modernCarvers = modernCarvers;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            Arrays.fill(surfaceHeights, Integer.MIN_VALUE);
            Arrays.fill(surfaceLowerBounds, FFDModernWorldgenData.MIN_Y - 1);
            Arrays.fill(surfaceUpperBounds, FFDModernWorldgenData.MIN_Y
                    + FFDModernWorldgenData.HEIGHT - 1);
        }

        private IBlockState baseStateAt(int localX, int blockY, int localZ,
                                        int worldX, int worldZ,
                                        ModernSurfaceStage surfaceRules) {
            IBlockState state = baseStates.get(localX, blockY, localZ);
            if (state == null) {
                state = baseStateForDensity(worldX, blockY, worldZ,
                        densityColumn(localX, localZ).sample(blockY), surfaceRules);
                baseStates.remember(localX, blockY, localZ, state);
            }
            return state;
        }

        private ModernNoiseChunk.Column densityColumn(int localX, int localZ) {
            int index = (localZ & 15) * 16 + (localX & 15);
            ModernNoiseChunk.Column column = densityColumns[index];
            if (column == null) {
                column = densitySampler.column(localX & 15, localZ & 15);
                densityColumns[index] = column;
            }
            return column;
        }

        private IBlockState baseStateForDensity(int worldX, int blockY, int worldZ, double density,
                                               ModernSurfaceStage surfaceRules) {
            IBlockState substance = aquifer().computeSubstance(worldX, blockY, worldZ, density);
            return substance == null
                    ? surfaceRules.stoneStateAt(worldX, blockY, worldZ) : substance;
        }

        private IBlockState postCarverStateAt(int localX, int blockY, int localZ,
                                              int worldX, int worldZ,
                                              ModernSurfaceStage surfaceRules) {
            IBlockState carved = carvedStateAt(
                    localX, blockY, localZ, worldX, worldZ, surfaceRules);
            if (carved != null) {
                return carved;
            }
            return baseStateAt(localX, blockY, localZ, worldX, worldZ, surfaceRules);
        }

        @Nullable
        private IBlockState carvedStateAt(int localX, int blockY, int localZ,
                                          int worldX, int worldZ,
                                          ModernSurfaceStage surfaceRules) {
            if (!carvingMask().contains(localX, blockY, localZ)
                    || blockY > surfaceY(localX, localZ, surfaceRules) - 4) {
                return null;
            }
            return aquifer().computeSubstance(worldX, blockY, worldZ, 0.0D);
        }

        private FFDModernAquifer aquifer() {
            if (aquifer == null) aquifer = context.aquifer(chunkX, chunkZ);
            return aquifer;
        }

        private int surfaceY(int localX, int localZ, ModernSurfaceStage surfaceRules) {
            int index = (localZ & 15) * 16 + (localX & 15);
            int cached = surfaceHeights[index];
            if (cached != Integer.MIN_VALUE) {
                return cached;
            }
            int worldX = (chunkX << 4) + (localX & 15);
            int worldZ = (chunkZ << 4) + (localZ & 15);
            int found = FFDModernWorldgenData.MIN_Y - 1;
            ModernNoiseChunk.Column column = densityColumn(localX, localZ);
            for (int blockY = FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1;
                 blockY >= FFDModernWorldgenData.MIN_Y; blockY--) {
                if (baseStateForDensity(worldX, blockY, worldZ,
                        column.sample(blockY), surfaceRules).getMaterial().isSolid()) {
                    found = blockY;
                    break;
                }
            }
            surfaceHeights[index] = found;
            return found;
        }

        private boolean isBelowSurface(int localX, int blockY, int localZ,
                                       ModernSurfaceStage surfaceRules) {
            int index = (localZ & 15) * 16 + (localX & 15);
            int cached = surfaceHeights[index];
            if (cached != Integer.MIN_VALUE) {
                return blockY <= cached - 2;
            }
            int threshold = blockY + 2;
            if (threshold <= surfaceLowerBounds[index]) {
                return true;
            }
            if (threshold > surfaceUpperBounds[index]) {
                return false;
            }
            int worldX = (chunkX << 4) + (localX & 15);
            int worldZ = (chunkZ << 4) + (localZ & 15);
            ModernNoiseChunk.Column column = densityColumn(localX, localZ);
            for (int sampleY = threshold; sampleY <= surfaceUpperBounds[index]; sampleY++) {
                if (baseStateForDensity(worldX, sampleY, worldZ,
                        column.sample(sampleY), surfaceRules).getMaterial().isSolid()) {
                    surfaceLowerBounds[index] = sampleY;
                    if (sampleY == surfaceUpperBounds[index]) {
                        surfaceHeights[index] = sampleY;
                    }
                    return true;
                }
            }
            surfaceUpperBounds[index] = threshold - 1;
            if (surfaceUpperBounds[index] == surfaceLowerBounds[index]) {
                surfaceHeights[index] = surfaceLowerBounds[index];
            }
            return false;
        }

        private void rememberSurfaceHeights(int[] heights) {
            System.arraycopy(heights, 0, surfaceHeights, 0,
                    Math.min(heights.length, surfaceHeights.length));
        }

        private WorldGenModernCarvers.CarvingMask carvingMask() {
            if (carvingMask == null) {
                carvingMask = modernCarvers.maskForChunk(chunkX, chunkZ);
            }
            return carvingMask;
        }
    }

    private static void setDirect(ModernTerrainBuffer chunk, int localX, int blockY, int localZ, IBlockState state) {
        chunk.setState(localX, blockY, localZ, state);
    }
}
