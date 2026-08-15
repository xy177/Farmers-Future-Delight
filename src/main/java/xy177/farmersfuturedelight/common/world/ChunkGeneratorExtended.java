package xy177.farmersfuturedelight.common.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import xy177.farmersfuturedelight.common.world.terrain.FFDDensityFunction;
import xy177.farmersfuturedelight.common.world.terrain.FFDBiomeTerrainBridge;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernAquifer;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernSurfaceRules;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeProvider;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeSampler;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeZoomer;
import xy177.farmersfuturedelight.common.block.BlockGlowLichen;
import xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid;
import xy177.farmersfuturedelight.common.worldgen.WorldGenAmethystGeodes;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernOreVeins;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernFossils;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernOres;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernMountainBiomes;
import xy177.farmersfuturedelight.common.worldgen.WorldGenVerticalCaveBiomes;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernCarvers;
import xy177.farmersfuturedelight.common.worldgen.FFDStructureHooks;
import xy177.farmersfuturedelight.common.worldgen.FFDLushCaveBlockProvider;
import xy177.farmersfuturedelight.common.worldgen.FFDModernStoneProvider;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.BitSet;

public final class ChunkGeneratorExtended extends ChunkGeneratorOverworld {
    private static final IBlockState STONE = Blocks.STONE.getDefaultState();
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();
    private static final EnumFacing[] DIRECTIONS = EnumFacing.values();

    private final World world;
    private final WorldGenAmethystGeodes amethystGeodes;
    private final WorldGenModernOreVeins modernOreVeins;
    private final WorldGenModernFossils modernFossils;
    private final WorldGenModernOres modernOres;
    private final WorldGenVerticalCaveBiomes caveBiomes;
    private final WorldGenModernCarvers modernCarvers;
    private final WorldGenModernMountainBiomes mountainBiomes;
    private final FFDModernStoneProvider modernStones;
    private final FFDLushCaveBlockProvider lushBlocks;
    private final FFDModernWorldgenData worldgenData;
    private final FFDBiomeTerrainBridge terrainBridge;
    private final DensityGridCache densityGridCache;
    private final FFDModernSurfaceRules surfaceRules;
    private final FFDVerticalBiomeSampler verticalBiomeSampler;
    private final long biomeZoomSeed;
    private final double[] densityScratch = new double[16 * FFDModernWorldgenData.HEIGHT * 16];
    private final RegionGenerationCache regionGenerationCache = new RegionGenerationCache();
    private final SurfaceHeightCache surfaceHeightCache = new SurfaceHeightCache();
    private final Map<Long, int[]> pendingFluidUpdates = new HashMap<>();
    private Biome[] biomes;

    public ChunkGeneratorExtended(World world, long seed, boolean mapFeaturesEnabled, String generatorOptions) {
        super(world, seed, mapFeaturesEnabled, generatorOptions);
        this.world = world;
        this.amethystGeodes = new WorldGenAmethystGeodes(seed);
        this.modernStones = new FFDModernStoneProvider();
        this.lushBlocks = FFDLushCaveBlockProvider.get();
        this.modernOres = new WorldGenModernOres(seed, modernStones);
        this.caveBiomes = new WorldGenVerticalCaveBiomes(seed, modernStones);
        this.modernCarvers = new WorldGenModernCarvers(seed);
        this.mountainBiomes = new WorldGenModernMountainBiomes(seed);
        FFDModernBiomeProvider biomeProvider = world.getBiomeProvider() instanceof FFDModernBiomeProvider
                ? (FFDModernBiomeProvider) world.getBiomeProvider() : null;
        this.worldgenData = biomeProvider != null
                ? biomeProvider.worldgenData() : new FFDModernWorldgenData(seed);
        this.terrainBridge = new FFDBiomeTerrainBridge(biomeProvider != null
                ? biomeProvider.resolver() : new FFDModernBiomeResolver(worldgenData, seed));
        this.densityGridCache = new DensityGridCache(worldgenData.finalDensityComponents());
        this.modernOreVeins = new WorldGenModernOreVeins(worldgenData, modernStones);
        this.modernFossils = new WorldGenModernFossils(seed, modernStones);
        this.verticalBiomeSampler = new FFDVerticalBiomeSampler(worldgenData);
        this.biomeZoomSeed = FFDVerticalBiomeZoomer.obfuscateSeed(seed);
        this.surfaceRules = new FFDModernSurfaceRules(world, worldgenData,
                verticalBiomeSampler, modernStones);
        FFDStructureHooks.installModernGenerators(this, world, this);
        world.setSeaLevel(FFDModernWorldgenData.SEA_LEVEL);
    }

    /** Returns the unmodified 26.3 preliminary surface estimate. */
    public int estimateModernSurfaceHeight(int x, int z) {
        return worldgenData.preliminarySurfaceLevel(x, z);
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        long profileChunkStart = FFDWorldgenProfiler.ENABLED ? System.nanoTime() : 0L;
        long profileStageStart = profileChunkStart;
        long[] profileStages = FFDWorldgenProfiler.ENABLED ? new long[12] : null;
        Chunk chunk = new Chunk(world, chunkX, chunkZ);
        ExtendedChunkBuffer baseTerrain = new ExtendedChunkBuffer(world);
        GenerationRegion generationRegion = new GenerationRegion(
                regionGenerationCache, worldgenData, densityGridCache, modernCarvers);
        TerrainSample terrainSample = generationRegion.sample(chunkX, chunkZ);
        FFDModernAquifer aquifer = terrainSample.aquifer;
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        DensitySampler densitySampler = terrainSample.densitySampler;
        densitySampler.fill(densityScratch);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[0] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        WorldGenModernOreVeins.Sampler oreVeinSampler = modernOreVeins.sampler(startX, startZ);
        List<Integer> fluidUpdates = new ArrayList<>();
        int[] oceanFloorY = new int[16 * 16];
        Arrays.fill(oceanFloorY, FFDModernWorldgenData.MIN_Y - 1);

        for (int localX = 0; localX < 16; localX++) {
            int worldX = startX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = startZ + localZ;
                for (int y = FFDModernWorldgenData.MIN_Y;
                     y < FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT; y++) {
                    double density = densityScratch[densityIndex(localX, y, localZ)];
                    IBlockState substance = aquifer.computeSubstance(worldX, y, worldZ, density);
                    IBlockState state = substance;
                    if (state == null) {
                        state = oreVeinSampler.stateAt(worldX, y, worldZ);
                    }
                    if (state == null) {
                        state = y < 0 ? surfaceRules.stoneStateAt(worldX, y, worldZ) : STONE;
                    }
                    if (state.getMaterial().isSolid()) {
                        oceanFloorY[localZ * 16 + localX] = y;
                    }
                    if (aquifer.shouldScheduleFluidUpdate() && isFluid(state)) {
                        fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(localX, y, localZ));
                    }
                    if (!isAir(state)) {
                        baseTerrain.set(localX, y, localZ, state);
                    }
                }
            }
        }
        terrainSample.rememberSurfaceHeights(oceanFloorY);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[1] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        biomes = world.getBiomeProvider().getBiomes(biomes, startX, startZ, 16, 16);
        baseTerrain.commitTo(chunk);
        FFDVerticalBiomeData verticalBiomes = verticalBiomeSampler.sampleChunk(chunkX, chunkZ);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[2] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        surfaceRules.apply(chunk, biomes, verticalBiomes, terrainBridge, startX, startZ);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[3] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        BitSet carvedPositions = modernCarvers.generate(chunkX, chunkZ,
                new WorldGenModernCarvers.Access() {
            @Override
            public IBlockState getState(int localX, int y, int localZ) {
                return chunk.getBlockState(new BlockPos(startX + localX, y, startZ + localZ));
            }

            @Override
            public void setState(int localX, int y, int localZ, IBlockState state) {
                setDirect(chunk, localX, y, localZ, state);
            }

            @Override
            public IBlockState carveState(int localX, int y, int localZ) {
                return aquifer.computeSubstance(startX + localX, y, startZ + localZ, 0.0D);
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return aquifer.shouldScheduleFluidUpdate();
            }

            @Override
            public void scheduleFluidUpdate(int localX, int y, int localZ) {
                fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(localX, y, localZ));
            }

            @Override
            public int surfaceY(int localX, int localZ) {
                return terrainSample.surfaceY(localX, localZ, surfaceRules);
            }
        });
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[4] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        recomputeOceanFloor(chunk, oceanFloorY);
        clearLegacyBedrockLayer(chunk, densitySampler, aquifer, startX, startZ, fluidUpdates);
        Set<Integer> generatedGlowLichen = new HashSet<>();
        NeighborAwareCaveAccess caveAccess = caveBiomeAccess(chunk, chunkX, chunkZ,
                verticalBiomes, fluidUpdates, oceanFloorY, generationRegion,
                generatedGlowLichen);
        applyPostCarverCaveSurfaces(carvedPositions, caveAccess,
                chunkX, chunkZ, generationRegion);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[5] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        generateAmethystGeodes(chunk, chunkX, chunkZ, fluidUpdates, generationRegion);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[6] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        caveBiomes.generateLocalModifications(chunkX, chunkZ, verticalBiomes, caveAccess);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[7] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        generateModernOres(chunk, chunkX, chunkZ, generationRegion);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[8] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        caveBiomes.generateUndergroundDecoration(chunkX, chunkZ, verticalBiomes, caveAccess);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[9] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        caveAccess.setPostCarverNeighbors(false);
        caveBiomes.generateVegetation(chunkX, chunkZ, verticalBiomes, caveAccess);
        caveAccess.setPostCarverNeighbors(true);
        validateGeneratedGlowLichen(caveAccess, generatedGlowLichen, chunkX, chunkZ);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[10] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        FFDVerticalBiomeManager.put(world, chunkX, chunkZ, verticalBiomes);
        rememberFluidUpdates(chunkX, chunkZ, fluidUpdates);

        byte[] biomeIds = chunk.getBiomeArray();
        for (int i = 0; i < biomeIds.length; i++) {
            biomeIds[i] = (byte) Biome.getIdForBiome(biomes[i]);
        }
        long profileStructureStart = FFDWorldgenProfiler.ENABLED ? System.nanoTime() : 0L;
        super.recreateStructures(chunk, chunkX, chunkZ);
        long profileStructureNanos = FFDWorldgenProfiler.ENABLED
                ? System.nanoTime() - profileStructureStart : 0L;
        long profileSkylightStart = FFDWorldgenProfiler.ENABLED ? System.nanoTime() : 0L;
        chunk.generateSkylightMap();
        if (FFDWorldgenProfiler.ENABLED) {
            long profileSkylightNanos = System.nanoTime() - profileSkylightStart;
            profileStages[11] = System.nanoTime() - profileStageStart;
            FFDWorldgenProfiler.record(System.nanoTime() - profileChunkStart, profileStages,
                    profileStructureNanos, profileSkylightNanos);
        }
        return chunk;
    }

    private void generateModernOres(Chunk chunk, int chunkX, int chunkZ,
                                    GenerationRegion generationRegion) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        modernOres.generateChunk(chunkX, chunkZ, new WorldGenModernOres.Access() {
            @Override
            public IBlockState getState(int x, int y, int z) {
                if (y < FFDModernWorldgenData.MIN_Y
                        || y >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                if (x >> 4 == chunkX && z >> 4 == chunkZ) {
                    return chunk.getBlockState(new BlockPos(x, y, z));
                }
                int sampleChunkX = x >> 4;
                int sampleChunkZ = z >> 4;
                TerrainSample sample = generationRegion.sample(sampleChunkX, sampleChunkZ);
                return sample.baseStateAt(x - (sampleChunkX << 4), y,
                        z - (sampleChunkZ << 4), x, z, surfaceRules);
            }

            @Override
            public void setState(int x, int y, int z, IBlockState state) {
                setDirect(chunk, x & 15, y, z & 15, state);
            }

            @Override
            public Biome getBiome(int x, int z) {
                if (x >= startX && x < startX + 16 && z >= startZ && z < startZ + 16) {
                    return biomes[(z - startZ) * 16 + x - startX];
                }
                return world.getBiomeProvider().getBiome(new BlockPos(x, 0, z));
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

    private void generateAmethystGeodes(Chunk chunk, int chunkX, int chunkZ,
                                        List<Integer> fluidUpdates,
                                        GenerationRegion generationRegion) {
        amethystGeodes.generateChunk(chunkX, chunkZ, new WorldGenAmethystGeodes.Access() {
            @Override
            public IBlockState getState(BlockPos pos) {
                int y = pos.getY();
                if (y < FFDModernWorldgenData.MIN_Y
                        || y >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                if (isTargetChunk(pos)) {
                    return chunk.getBlockState(pos);
                }
                int sampleChunkX = pos.getX() >> 4;
                int sampleChunkZ = pos.getZ() >> 4;
                TerrainSample sample = generationRegion.sample(sampleChunkX, sampleChunkZ);
                return sample.baseStateAt(pos.getX() - (sampleChunkX << 4), y,
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

    private NeighborAwareCaveAccess caveBiomeAccess(Chunk chunk, int chunkX, int chunkZ,
                                                    FFDVerticalBiomeData verticalBiomes,
                                                    List<Integer> fluidUpdates,
                                                    int[] oceanFloorY,
                                                    GenerationRegion generationRegion,
                                                    Set<Integer> generatedGlowLichen) {
        Map<Long, IBlockState> decorationOverlay = new HashMap<>();
        Map<Long, FFDVerticalBiomeData> verticalBiomeCache = new HashMap<>();
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
            public IBlockState getState(int x, int y, int z) {
                if (y < FFDModernWorldgenData.MIN_Y
                        || y >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                IBlockState overlayState = decorationOverlay.get(blockKey(x, y, z));
                if (overlayState != null) {
                    return overlayState;
                }
                if (isTargetChunk(x, z)) {
                    return getDirect(chunk, x & 15, y, z & 15);
                }
                int sampleChunkX = x >> 4;
                int sampleChunkZ = z >> 4;
                TerrainSample sample = sampleAt(sampleChunkX, sampleChunkZ);
                int localX = x - (sampleChunkX << 4);
                int localZ = z - (sampleChunkZ << 4);
                return postCarverNeighbors
                        ? sample.postCarverStateAt(localX, y, localZ, x, z, surfaceRules)
                        : sample.baseStateAt(localX, y, localZ, x, z, surfaceRules);
            }

            @Override
            public IBlockState getPlanningState(int x, int y, int z) {
                if (y < FFDModernWorldgenData.MIN_Y
                        || y >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                int sampleChunkX = x >> 4;
                int sampleChunkZ = z >> 4;
                TerrainSample sample = sampleAt(sampleChunkX, sampleChunkZ);
                return sample.postCarverStateAt(x - (sampleChunkX << 4), y,
                        z - (sampleChunkZ << 4), x, z, surfaceRules);
            }

            @Override
            public BlockFaceShape getFaceShape(int x, int y, int z,
                                               net.minecraft.util.EnumFacing face) {
                BlockPos pos = new BlockPos(x, y, z);
                return getState(x, y, z).getBlockFaceShape(world, pos, face);
            }

            @Override
            public boolean isTargetChunk(int x, int z) {
                return x >> 4 == chunkX && z >> 4 == chunkZ;
            }

            @Override
            public FFDVerticalBiome getBiome(int x, int y, int z) {
                return FFDVerticalBiomeZoomer.getBiome(
                        biomeZoomSeed, x, y, z, verticalBiomeSource);
            }

            @Override
            public void setState(int x, int y, int z, IBlockState state) {
                if (y < FFDModernWorldgenData.MIN_Y
                        || y >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return;
                }
                decorationOverlay.put(blockKey(x, y, z), state);
                if (isTargetChunk(x, z)) {
                    int packed = FFDHeightHooks.packLocalBlockChange(x & 15, y, z & 15);
                    if (BlockGlowLichen.isGlowLichen(state)) {
                        generatedGlowLichen.add(packed);
                    } else {
                        generatedGlowLichen.remove(packed);
                    }
                    setDirect(chunk, x & 15, y, z & 15, state);
                }
            }

            @Override
            public void markFluidUpdate(int x, int y, int z) {
                if (isTargetChunk(x, z)) {
                    fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(x & 15, y, z & 15));
                }
            }

            @Override
            public int getOceanFloorY(int x, int z) {
                if (isTargetChunk(x, z)) {
                    return oceanFloorY[(z & 15) * 16 + (x & 15)];
                }
                return surfaceHeightCache.getOrCompute(worldgenData, x, z);
            }

            @Override
            public int getPlanningOceanFloorY(int x, int z) {
                int sampleChunkX = x >> 4;
                int sampleChunkZ = z >> 4;
                TerrainSample sample = sampleAt(sampleChunkX, sampleChunkZ);
                return sample.surfaceY(x - (sampleChunkX << 4), z - (sampleChunkZ << 4),
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
        boolean profile = FFDWorldgenProfiler.ENABLED;
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
            int y = WorldGenModernCarvers.blockYAt(carvedIndex);
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
            if (y > FFDModernWorldgenData.MIN_Y
                    && !carvedPositions.get(carvedIndex - 256)) {
                int floor = carvedIndex - 256;
                candidates.set(floor);
                floors.set(floor);
            }
            if (y < FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1
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
            int y = (index >> 8) + FFDModernWorldgenData.MIN_Y;
            int x = startX + localX;
            int z = startZ + localZ;
            long operationStart = sampleTiming ? System.nanoTime() : 0L;
            IBlockState current = access.getState(x, y, z);
            if (!isCaveSurfaceBase(current, x, y, z)) {
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
            double patch = worldgenData.samplePatchNoise(x, y, z);
            if (sampleTiming) {
                patchNanos += System.nanoTime() - operationStart;
            }
            IBlockState replacement = null;
            boolean biomeReplacementPossible = floor ? patch > -0.12D
                    : ceiling ? patch > -0.08D : patch > 0.30D;
            if (biomeReplacementPossible) {
                biomeQueries++;
                operationStart = sampleTiming ? System.nanoTime() : 0L;
                FFDVerticalBiome biome = access.getBiome(x, y, z);
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
            if (replacement == null && y < 32 && tuff != null && patch > 0.30D) {
                replacement = tuff;
            }
            if (replacement == null && floor) {
                fallbackQueries++;
                operationStart = sampleTiming ? System.nanoTime() : 0L;
                double gravel = worldgenData.sampleGravelLayerNoise(x, y, z);
                if (gravel > 0.62D) {
                    replacement = Blocks.GRAVEL.getDefaultState();
                } else if (worldgenData.sampleSurfaceSecondaryNoise(x, y, z) < -0.50D) {
                    replacement = Blocks.DIRT.getDefaultState();
                }
                if (sampleTiming) {
                    fallbackNanos += System.nanoTime() - operationStart;
                }
            }
            if (replacement != null && replacement.getBlock() != current.getBlock()) {
                access.setState(x, y, z, replacement);
                writes++;
            }
        }
        if (profile) {
            FFDWorldgenProfiler.recordCaveSurface(buildNanos,
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
            int y = (entry & 0x1FF) - 63;
            int neighborLocalX = xAxis ? neighborEdge : edgeOffset;
            int neighborLocalZ = xAxis ? edgeOffset : neighborEdge;
            int worldX = xAxis ? neighborStartX : neighborStartX + edgeOffset;
            int worldZ = xAxis ? neighborStartZ + edgeOffset : neighborStartZ;
            IBlockState neighborState = sample.carvedStateAt(
                    neighborLocalX, y, neighborLocalZ, worldX, worldZ, surfaceRules);
            if (neighborState == null || !isCaveOpening(neighborState)) {
                continue;
            }
            int targetX = xAxis ? targetEdge : edgeOffset;
            int targetZ = xAxis ? edgeOffset : targetEdge;
            candidates.set(caveSurfaceIndex(targetX, y, targetZ));
        }
    }

    private boolean isCaveSurfaceBase(IBlockState state, int x, int y, int z) {
        Block block = state.getBlock();
        return block == Blocks.STONE || modernStones.isDeepBase(state)
                || block == surfaceRules.stoneStateAt(x, y, z).getBlock();
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

    private static int caveSurfaceIndex(int localX, int y, int localZ) {
        return ((y - FFDModernWorldgenData.MIN_Y) * 16 + localZ) * 16 + localX;
    }

    private void validateGeneratedGlowLichen(WorldGenVerticalCaveBiomes.Access access,
                                             Set<Integer> generatedGlowLichen,
                                             int chunkX, int chunkZ) {
        for (int packed : new ArrayList<>(generatedGlowLichen)) {
            int localX = packed >> 12 & 15;
            int localZ = packed >> 8 & 15;
            int y = FFDHeightHooks.unpackLocalBlockY(packed);
            int x = (chunkX << 4) + localX;
            int z = (chunkZ << 4) + localZ;
            IBlockState state = access.getState(x, y, z);
            if (!BlockGlowLichen.isGlowLichen(state)) {
                continue;
            }
            int oldMask = BlockGlowLichen.getFaceMask(state);
            int validMask = oldMask;
            for (EnumFacing face : DIRECTIONS) {
                if ((validMask & BlockGlowLichen.faceBit(face)) == 0) {
                    continue;
                }
                int supportX = x + face.getDirectionVec().getX();
                int supportY = y + face.getDirectionVec().getY();
                int supportZ = z + face.getDirectionVec().getZ();
                BlockPos supportPos = new BlockPos(supportX, supportY, supportZ);
                IBlockState support = access.getState(supportX, supportY, supportZ);
                net.minecraft.util.EnumFacing supportFace = face.getOpposite();
                if (!support.isSideSolid(world, supportPos, supportFace)
                        && access.getFaceShape(supportX, supportY, supportZ,
                        supportFace) != BlockFaceShape.SOLID) {
                    validMask &= ~BlockGlowLichen.faceBit(face);
                }
            }
            if (validMask != oldMask) {
                boolean waterlogged = BlockGlowLichen.isWaterlogged(state);
                access.setState(x, y, z, BlockGlowLichen.stateFor(validMask, waterlogged));
                if (waterlogged && validMask == 0) {
                    access.markFluidUpdate(x, y, z);
                }
            }
        }
    }

    private void clearLegacyBedrockLayer(Chunk chunk, DensitySampler densitySampler, FFDModernAquifer aquifer,
                                         int startX, int startZ, @Nullable List<Integer> fluidUpdates) {
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos pos = new BlockPos(startX + localX, y, startZ + localZ);
                    if (chunk.getBlockState(pos).getBlock() != Blocks.BEDROCK) {
                        continue;
                    }
                    double density = densitySampler.sample(localX, y, localZ);
                    IBlockState substance = aquifer.computeSubstance(pos.getX(), y, pos.getZ(), density);
                    IBlockState replacement = substance == null
                            ? surfaceRules.stoneStateAt(pos.getX(), y, pos.getZ()) : substance;
                    setDirect(chunk, localX, y, localZ, replacement);
                    if (fluidUpdates != null && aquifer.shouldScheduleFluidUpdate() && isFluid(replacement)) {
                        fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(localX, y, localZ));
                    }
                }
            }
        }
    }

    private List<Integer> clearLegacyBedrockAfterPopulate(Chunk chunk, int chunkX, int chunkZ) {
        List<Integer> fluidUpdates = new ArrayList<>();
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        if (!hasLegacyBedrock(chunk, startX, startZ)) {
            return fluidUpdates;
        }
        TerrainSample terrainSample = terrainSample(chunkX, chunkZ);
        DensitySampler densitySampler = terrainSample.densitySampler;
        FFDModernAquifer aquifer = terrainSample.aquifer;
        clearLegacyBedrockLayer(chunk, densitySampler, aquifer, startX, startZ, fluidUpdates);
        chunk.markDirty();
        return fluidUpdates;
    }

    private void rememberFluidUpdates(int chunkX, int chunkZ, List<Integer> updates) {
        long key = chunkKey(chunkX, chunkZ);
        if (updates.isEmpty()) {
            pendingFluidUpdates.remove(key);
            return;
        }
        int[] packed = new int[updates.size()];
        for (int i = 0; i < packed.length; i++) {
            packed[i] = updates.get(i);
        }
        pendingFluidUpdates.put(key, packed);
    }

    private void scheduleFluidUpdates(Chunk chunk, int chunkX, int chunkZ) {
        int[] updates = pendingFluidUpdates.remove(chunkKey(chunkX, chunkZ));
        if (updates == null) {
            return;
        }
        boolean changed = false;
        for (int packed : updates) {
            changed |= scheduleFluidUpdate(chunk, chunkX, chunkZ, packed);
        }
        if (changed) {
            chunk.markDirty();
        }
    }

    private void scheduleFluidUpdates(Chunk chunk, int chunkX, int chunkZ, List<Integer> updates) {
        boolean changed = false;
        for (int packed : updates) {
            changed |= scheduleFluidUpdate(chunk, chunkX, chunkZ, packed);
        }
        if (changed) {
            chunk.markDirty();
        }
    }

    private boolean scheduleFluidUpdate(Chunk chunk, int chunkX, int chunkZ, int packed) {
        int localX = packed >> 12 & 15;
        int localZ = packed >> 8 & 15;
        int y = FFDHeightHooks.unpackLocalBlockY(packed);
        BlockPos pos = new BlockPos((chunkX << 4) + localX, y, (chunkZ << 4) + localZ);
        IBlockState state = chunk.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.WATER || block == Blocks.LAVA) {
            Block flowing = block == Blocks.WATER ? Blocks.FLOWING_WATER : Blocks.FLOWING_LAVA;
            setDirect(chunk, localX, y, localZ, flowing.getDefaultState().withProperty(
                    BlockLiquid.LEVEL, state.getValue(BlockLiquid.LEVEL)));
            world.scheduleUpdate(pos, flowing, flowing.tickRate(world));
            return true;
        } else if (block == Blocks.FLOWING_WATER || block == Blocks.FLOWING_LAVA) {
            world.scheduleUpdate(pos, block, block.tickRate(world));
        } else if (WaterloggedPlantFluid.isWaterlogged(state)) {
            world.scheduleUpdate(pos, block, 5);
        }
        return false;
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return (chunkX & 0xFFFFFFFFL) | (long) chunkZ << 32;
    }

    private static long blockKey(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38
                | ((long) z & 0x3FFFFFFL) << 12
                | (long) y & 0xFFFL;
    }

    private static IBlockState getDirect(Chunk chunk, int localX, int y, int localZ) {
        int index = FFDHeightHooks.storageIndex(y, chunk.getWorld());
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        if (index < 0 || index >= storage.length) {
            return Blocks.AIR.getDefaultState();
        }
        ExtendedBlockStorage section = storage[index];
        return section == Chunk.NULL_BLOCK_STORAGE
                ? Blocks.AIR.getDefaultState() : section.get(localX, y & 15, localZ);
    }

    private static boolean hasLegacyBedrock(Chunk chunk, int startX, int startZ) {
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int y = 0; y < 5; y++) {
                    if (chunk.getBlockState(new BlockPos(startX + localX, y, startZ + localZ)).getBlock()
                            == Blocks.BEDROCK) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isAir(IBlockState state) {
        return state == null || state.getBlock() == Blocks.AIR;
    }

    private static void recomputeOceanFloor(Chunk chunk, int[] oceanFloorY) {
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int found = FFDModernWorldgenData.MIN_Y - 1;
                for (int y = FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1;
                     y >= FFDModernWorldgenData.MIN_Y; y--) {
                    if (chunk.getBlockState(new BlockPos(localX, y, localZ))
                            .getMaterial().isSolid()) {
                        found = y;
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

    private static int densityIndex(int localX, int y, int localZ) {
        return ((localX * 16 + localZ) * FFDModernWorldgenData.HEIGHT)
                + y - FFDModernWorldgenData.MIN_Y;
    }

    private TerrainSample terrainSample(int chunkX, int chunkZ) {
        return regionGenerationCache.get(
                worldgenData, densityGridCache, modernCarvers, chunkX, chunkZ);
    }

    /**
     * Holds the detached 1.12 section objects while the modern base terrain is built.
     * Keeping the section lookup outside the voxel loop removes the legacy primer copy
     * without changing the final Chunk storage or Forge populate path.
     */
    private static final class ExtendedChunkBuffer {
        private final World world;
        private final ExtendedBlockStorage[] sections;

        private ExtendedChunkBuffer(World world) {
            this.world = world;
            this.sections = new ExtendedBlockStorage[FFDHeightHooks.sectionCount(world)];
        }

        private void set(int localX, int y, int localZ, IBlockState state) {
            int index = FFDHeightHooks.storageIndex(y, world);
            if (index < 0 || index >= sections.length) {
                return;
            }
            ExtendedBlockStorage section = sections[index];
            if (section == null) {
                section = new ExtendedBlockStorage(
                        FFDHeightHooks.sectionBaseYForStorageIndex(index, world),
                        world.provider.hasSkyLight());
                sections[index] = section;
            }
            section.set(localX, y & 15, localZ, state);
        }

        private void commitTo(Chunk chunk) {
            ExtendedBlockStorage[] target = chunk.getBlockStorageArray();
            int count = Math.min(target.length, sections.length);
            for (int index = 0; index < count; index++) {
                ExtendedBlockStorage section = sections[index];
                if (section != null) {
                    target[index] = section;
                }
            }
        }
    }

    private static final class TerrainSample {
        private final DensitySampler densitySampler;
        private final FFDModernAquifer aquifer;
        private final WorldGenModernCarvers modernCarvers;
        private final int chunkX;
        private final int chunkZ;
        private final int[] surfaceHeights = new int[16 * 16];
        private WorldGenModernCarvers.CarvingMask carvingMask;

        private TerrainSample(FFDModernWorldgenData data, DensityGridCache densityGridCache,
                              WorldGenModernCarvers modernCarvers, int chunkX, int chunkZ) {
            densitySampler = new DensitySampler(densityGridCache, chunkX << 4, chunkZ << 4);
            aquifer = new FFDModernAquifer(data, chunkX, chunkZ);
            this.modernCarvers = modernCarvers;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            Arrays.fill(surfaceHeights, Integer.MIN_VALUE);
        }

        private IBlockState baseStateAt(int localX, int y, int localZ,
                                        int worldX, int worldZ,
                                        FFDModernSurfaceRules surfaceRules) {
            double density = densitySampler.sample(localX, y, localZ);
            IBlockState substance = aquifer.computeSubstance(worldX, y, worldZ, density);
            return substance == null
                    ? surfaceRules.stoneStateAt(worldX, y, worldZ) : substance;
        }

        private IBlockState postCarverStateAt(int localX, int y, int localZ,
                                              int worldX, int worldZ,
                                              FFDModernSurfaceRules surfaceRules) {
            IBlockState carved = carvedStateAt(
                    localX, y, localZ, worldX, worldZ, surfaceRules);
            if (carved != null) {
                return carved;
            }
            return baseStateAt(localX, y, localZ, worldX, worldZ, surfaceRules);
        }

        @Nullable
        private IBlockState carvedStateAt(int localX, int y, int localZ,
                                          int worldX, int worldZ,
                                          FFDModernSurfaceRules surfaceRules) {
            if (!carvingMask().contains(localX, y, localZ)
                    || y > surfaceY(localX, localZ, surfaceRules) - 4) {
                return null;
            }
            return aquifer.computeSubstance(worldX, y, worldZ, 0.0D);
        }

        private int surfaceY(int localX, int localZ, FFDModernSurfaceRules surfaceRules) {
            int index = (localZ & 15) * 16 + (localX & 15);
            int cached = surfaceHeights[index];
            if (cached != Integer.MIN_VALUE) {
                return cached;
            }
            int worldX = (chunkX << 4) + (localX & 15);
            int worldZ = (chunkZ << 4) + (localZ & 15);
            int found = FFDModernWorldgenData.MIN_Y - 1;
            for (int y = FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1;
                 y >= FFDModernWorldgenData.MIN_Y; y--) {
                if (baseStateAt(localX & 15, y, localZ & 15,
                        worldX, worldZ, surfaceRules).getMaterial().isSolid()) {
                    found = y;
                    break;
                }
            }
            surfaceHeights[index] = found;
            return found;
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

    private static final class GenerationRegion {
        private final RegionGenerationCache sharedCache;
        private final FFDModernWorldgenData data;
        private final DensityGridCache densityGridCache;
        private final WorldGenModernCarvers modernCarvers;
        private final Map<Long, TerrainSample> localSamples = new HashMap<>();

        private GenerationRegion(RegionGenerationCache sharedCache,
                                 FFDModernWorldgenData data,
                                 DensityGridCache densityGridCache,
                                 WorldGenModernCarvers modernCarvers) {
            this.sharedCache = sharedCache;
            this.data = data;
            this.densityGridCache = densityGridCache;
            this.modernCarvers = modernCarvers;
        }

        private TerrainSample sample(int chunkX, int chunkZ) {
            long key = chunkKey(chunkX, chunkZ);
            TerrainSample sample = localSamples.get(key);
            if (sample == null) {
                sample = sharedCache.get(data, densityGridCache, modernCarvers, chunkX, chunkZ);
                localSamples.put(key, sample);
            }
            return sample;
        }
    }

    private static final class RegionGenerationCache {
        private static final int MAX_ENTRIES = 128;
        private final LinkedHashMap<Long, TerrainSample> samples =
                new LinkedHashMap<Long, TerrainSample>(MAX_ENTRIES, 0.75F, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Long, TerrainSample> eldest) {
                        return size() > MAX_ENTRIES;
                    }
                };

        private synchronized TerrainSample get(FFDModernWorldgenData data,
                                                 DensityGridCache densityGridCache,
                                                 WorldGenModernCarvers modernCarvers,
                                                 int chunkX, int chunkZ) {
            long key = chunkKey(chunkX, chunkZ);
            TerrainSample sample = samples.get(key);
            if (sample == null) {
                sample = new TerrainSample(data, densityGridCache, modernCarvers, chunkX, chunkZ);
                samples.put(key, sample);
            }
            return sample;
        }
    }

    private void setDirect(Chunk chunk, int localX, int y, int localZ, IBlockState state) {
        int index = FFDHeightHooks.storageIndex(y, world);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        if (index < 0 || index >= storage.length) {
            return;
        }
        ExtendedBlockStorage section = storage[index];
        if (section == Chunk.NULL_BLOCK_STORAGE) {
            section = new ExtendedBlockStorage(y >> 4 << 4, world.provider.hasSkyLight());
            storage[index] = section;
        }
        section.set(localX, y & 15, localZ, state);
    }

    @Override
    public void populate(int x, int z) {
        try (FFDHeightHooks.WorldgenHeightScope ignored = FFDHeightHooks.enterLegacyWorldgenHeight()) {
            super.populate(x, z);
        }
        mountainBiomes.generate(world, x, z);
        modernFossils.generateChunk(world, x, z);
        Chunk chunk = world.getChunkFromChunkCoords(x, z);
        List<Integer> restoredFluidUpdates = clearLegacyBedrockAfterPopulate(chunk, x, z);
        scheduleFluidUpdates(chunk, x, z);
        scheduleFluidUpdates(chunk, x, z, restoredFluidUpdates);
    }

    @Override
    public boolean generateStructures(Chunk chunk, int x, int z) {
        return super.generateStructures(chunk, x, z);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World world, String structureName, BlockPos position,
                                           boolean findUnexplored) {
        return super.getNearestStructurePos(world, structureName, position, findUnexplored);
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) {
        super.recreateStructures(chunk, x, z);
    }

    @Override
    public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
        return super.isInsideStructure(world, structureName, pos);
    }

    private static final class DensitySampler {
        private static final int GRID_X = 16 / FFDModernWorldgenData.CELL_WIDTH + 1;
        private static final int GRID_Y = FFDModernWorldgenData.HEIGHT / FFDModernWorldgenData.CELL_HEIGHT + 1;
        private static final int GRID_Z = 16 / FFDModernWorldgenData.CELL_WIDTH + 1;

        private final Grid main;
        private final Grid noodleToggle;
        private final Grid noodleThickness;
        private final Grid noodleRidgeA;
        private final Grid noodleRidgeB;

        private DensitySampler(DensityGridCache sharedCache, int startX, int startZ) {
            main = new Grid(sharedCache, DensityGridCache.MAIN, startX, startZ);
            noodleToggle = new Grid(sharedCache, DensityGridCache.NOODLE_TOGGLE, startX, startZ);
            noodleThickness = new Grid(sharedCache, DensityGridCache.NOODLE_THICKNESS, startX, startZ);
            noodleRidgeA = new Grid(sharedCache, DensityGridCache.NOODLE_RIDGE_A, startX, startZ);
            noodleRidgeB = new Grid(sharedCache, DensityGridCache.NOODLE_RIDGE_B, startX, startZ);
        }

        private double sample(int localX, int y, int localZ) {
            return sampleAtY(localX, y, localZ);
        }

        private void fill(double[] target) {
            for (int cellX = 0; cellX < GRID_X - 1; cellX++) {
                for (int cellZ = 0; cellZ < GRID_Z - 1; cellZ++) {
                    for (int cellY = 0; cellY < GRID_Y - 1; cellY++) {
                        fillCell(target, cellX, cellY, cellZ);
                    }
                }
            }
        }

        private void fillCell(double[] target, int cellX, int cellY, int cellZ) {
            double main000 = main.value(cellX, cellY, cellZ);
            double main100 = main.value(cellX + 1, cellY, cellZ);
            double main010 = main.value(cellX, cellY + 1, cellZ);
            double main110 = main.value(cellX + 1, cellY + 1, cellZ);
            double main001 = main.value(cellX, cellY, cellZ + 1);
            double main101 = main.value(cellX + 1, cellY, cellZ + 1);
            double main011 = main.value(cellX, cellY + 1, cellZ + 1);
            double main111 = main.value(cellX + 1, cellY + 1, cellZ + 1);
            double toggle000 = noodleToggle.value(cellX, cellY, cellZ);
            double toggle100 = noodleToggle.value(cellX + 1, cellY, cellZ);
            double toggle010 = noodleToggle.value(cellX, cellY + 1, cellZ);
            double toggle110 = noodleToggle.value(cellX + 1, cellY + 1, cellZ);
            double toggle001 = noodleToggle.value(cellX, cellY, cellZ + 1);
            double toggle101 = noodleToggle.value(cellX + 1, cellY, cellZ + 1);
            double toggle011 = noodleToggle.value(cellX, cellY + 1, cellZ + 1);
            double toggle111 = noodleToggle.value(cellX + 1, cellY + 1, cellZ + 1);

            boolean detailsLoaded = false;
            double thickness000 = 0.0D, thickness100 = 0.0D;
            double thickness010 = 0.0D, thickness110 = 0.0D;
            double thickness001 = 0.0D, thickness101 = 0.0D;
            double thickness011 = 0.0D, thickness111 = 0.0D;
            double ridgeA000 = 0.0D, ridgeA100 = 0.0D;
            double ridgeA010 = 0.0D, ridgeA110 = 0.0D;
            double ridgeA001 = 0.0D, ridgeA101 = 0.0D;
            double ridgeA011 = 0.0D, ridgeA111 = 0.0D;
            double ridgeB000 = 0.0D, ridgeB100 = 0.0D;
            double ridgeB010 = 0.0D, ridgeB110 = 0.0D;
            double ridgeB001 = 0.0D, ridgeB101 = 0.0D;
            double ridgeB011 = 0.0D, ridgeB111 = 0.0D;

            int baseX = cellX * FFDModernWorldgenData.CELL_WIDTH;
            int baseY = FFDModernWorldgenData.MIN_Y
                    + cellY * FFDModernWorldgenData.CELL_HEIGHT;
            int baseZ = cellZ * FFDModernWorldgenData.CELL_WIDTH;
            for (int offsetX = 0; offsetX < FFDModernWorldgenData.CELL_WIDTH; offsetX++) {
                double fx = offsetX / (double) FFDModernWorldgenData.CELL_WIDTH;
                for (int offsetZ = 0; offsetZ < FFDModernWorldgenData.CELL_WIDTH; offsetZ++) {
                    double fz = offsetZ / (double) FFDModernWorldgenData.CELL_WIDTH;
                    for (int offsetY = 0; offsetY < FFDModernWorldgenData.CELL_HEIGHT; offsetY++) {
                        double fy = offsetY / (double) FFDModernWorldgenData.CELL_HEIGHT;
                        double mainValue = interpolate(main000, main100, main010, main110,
                                main001, main101, main011, main111, fx, fy, fz);
                        double toggle = interpolate(toggle000, toggle100, toggle010, toggle110,
                                toggle001, toggle101, toggle011, toggle111, fx, fy, fz);
                        double density;
                        if (toggle >= -1000000.0D && toggle < 0.0D) {
                            density = FFDModernWorldgenData.FinalDensityComponents.combine(
                                    mainValue, toggle, 0.0D, 0.0D, 0.0D);
                        } else {
                            if (!detailsLoaded) {
                                thickness000 = noodleThickness.value(cellX, cellY, cellZ);
                                thickness100 = noodleThickness.value(cellX + 1, cellY, cellZ);
                                thickness010 = noodleThickness.value(cellX, cellY + 1, cellZ);
                                thickness110 = noodleThickness.value(cellX + 1, cellY + 1, cellZ);
                                thickness001 = noodleThickness.value(cellX, cellY, cellZ + 1);
                                thickness101 = noodleThickness.value(cellX + 1, cellY, cellZ + 1);
                                thickness011 = noodleThickness.value(cellX, cellY + 1, cellZ + 1);
                                thickness111 = noodleThickness.value(cellX + 1, cellY + 1, cellZ + 1);
                                ridgeA000 = noodleRidgeA.value(cellX, cellY, cellZ);
                                ridgeA100 = noodleRidgeA.value(cellX + 1, cellY, cellZ);
                                ridgeA010 = noodleRidgeA.value(cellX, cellY + 1, cellZ);
                                ridgeA110 = noodleRidgeA.value(cellX + 1, cellY + 1, cellZ);
                                ridgeA001 = noodleRidgeA.value(cellX, cellY, cellZ + 1);
                                ridgeA101 = noodleRidgeA.value(cellX + 1, cellY, cellZ + 1);
                                ridgeA011 = noodleRidgeA.value(cellX, cellY + 1, cellZ + 1);
                                ridgeA111 = noodleRidgeA.value(cellX + 1, cellY + 1, cellZ + 1);
                                ridgeB000 = noodleRidgeB.value(cellX, cellY, cellZ);
                                ridgeB100 = noodleRidgeB.value(cellX + 1, cellY, cellZ);
                                ridgeB010 = noodleRidgeB.value(cellX, cellY + 1, cellZ);
                                ridgeB110 = noodleRidgeB.value(cellX + 1, cellY + 1, cellZ);
                                ridgeB001 = noodleRidgeB.value(cellX, cellY, cellZ + 1);
                                ridgeB101 = noodleRidgeB.value(cellX + 1, cellY, cellZ + 1);
                                ridgeB011 = noodleRidgeB.value(cellX, cellY + 1, cellZ + 1);
                                ridgeB111 = noodleRidgeB.value(cellX + 1, cellY + 1, cellZ + 1);
                                detailsLoaded = true;
                            }
                            density = FFDModernWorldgenData.FinalDensityComponents.combine(
                                    mainValue, toggle,
                                    interpolate(thickness000, thickness100, thickness010,
                                            thickness110, thickness001, thickness101,
                                            thickness011, thickness111, fx, fy, fz),
                                    interpolate(ridgeA000, ridgeA100, ridgeA010, ridgeA110,
                                            ridgeA001, ridgeA101, ridgeA011, ridgeA111,
                                            fx, fy, fz),
                                    interpolate(ridgeB000, ridgeB100, ridgeB010, ridgeB110,
                                            ridgeB001, ridgeB101, ridgeB011, ridgeB111,
                                            fx, fy, fz));
                        }
                        target[densityIndex(baseX + offsetX, baseY + offsetY,
                                baseZ + offsetZ)] = density;
                    }
                }
            }
        }

        private double sampleAtY(int localX, double sampleY, int localZ) {
            int cellX = Math.min(localX / FFDModernWorldgenData.CELL_WIDTH, GRID_X - 2);
            int cellY = Math.min(Math.max((int) Math.floor((sampleY - FFDModernWorldgenData.MIN_Y)
                    / FFDModernWorldgenData.CELL_HEIGHT), 0), GRID_Y - 2);
            int cellZ = Math.min(localZ / FFDModernWorldgenData.CELL_WIDTH, GRID_Z - 2);
            int x0 = cellX * FFDModernWorldgenData.CELL_WIDTH;
            int y0 = FFDModernWorldgenData.MIN_Y + cellY * FFDModernWorldgenData.CELL_HEIGHT;
            int z0 = cellZ * FFDModernWorldgenData.CELL_WIDTH;
            double fx = (localX - x0) / (double) FFDModernWorldgenData.CELL_WIDTH;
            double fy = (sampleY - y0) / (double) FFDModernWorldgenData.CELL_HEIGHT;
            double fz = (localZ - z0) / (double) FFDModernWorldgenData.CELL_WIDTH;

            double mainValue = interpolate(main, cellX, cellY, cellZ, fx, fy, fz);
            double toggle = interpolate(noodleToggle, cellX, cellY, cellZ, fx, fy, fz);
            if (toggle >= -1000000.0D && toggle < 0.0D) {
                return FFDModernWorldgenData.FinalDensityComponents.combine(
                        mainValue, toggle, 0.0D, 0.0D, 0.0D);
            }
            return FFDModernWorldgenData.FinalDensityComponents.combine(
                    mainValue,
                    toggle,
                    interpolate(noodleThickness, cellX, cellY, cellZ, fx, fy, fz),
                    interpolate(noodleRidgeA, cellX, cellY, cellZ, fx, fy, fz),
                    interpolate(noodleRidgeB, cellX, cellY, cellZ, fx, fy, fz));
        }

        private static double interpolate(Grid grid, int cellX, int cellY, int cellZ,
                                          double fx, double fy, double fz) {
            double x00 = lerp(fx, grid.value(cellX, cellY, cellZ),
                    grid.value(cellX + 1, cellY, cellZ));
            double x10 = lerp(fx, grid.value(cellX, cellY + 1, cellZ),
                    grid.value(cellX + 1, cellY + 1, cellZ));
            double x01 = lerp(fx, grid.value(cellX, cellY, cellZ + 1),
                    grid.value(cellX + 1, cellY, cellZ + 1));
            double x11 = lerp(fx, grid.value(cellX, cellY + 1, cellZ + 1),
                    grid.value(cellX + 1, cellY + 1, cellZ + 1));
            return lerp(fz, lerp(fy, x00, x10), lerp(fy, x01, x11));
        }

        private static double interpolate(double v000, double v100, double v010,
                                          double v110, double v001, double v101,
                                          double v011, double v111,
                                          double fx, double fy, double fz) {
            double x00 = lerp(fx, v000, v100);
            double x10 = lerp(fx, v010, v110);
            double x01 = lerp(fx, v001, v101);
            double x11 = lerp(fx, v011, v111);
            return lerp(fz, lerp(fy, x00, x10), lerp(fy, x01, x11));
        }

        private static int index(int cellX, int cellY, int cellZ) {
            return (cellY * GRID_Z + cellZ) * GRID_X + cellX;
        }

        private static double lerp(double amount, double first, double second) {
            return first + amount * (second - first);
        }

        private static final class Grid {
            private final double[] values = new double[GRID_X * GRID_Y * GRID_Z];
            private final boolean[] sampled = new boolean[values.length];
            private final DensityGridCache sharedCache;
            private final int field;
            private final int startX;
            private final int startZ;

            private Grid(DensityGridCache sharedCache, int field,
                         int startX, int startZ) {
                this.sharedCache = sharedCache;
                this.field = field;
                this.startX = startX;
                this.startZ = startZ;
            }

            private double value(int cellX, int cellY, int cellZ) {
                int index = index(cellX, cellY, cellZ);
                if (!sampled[index]) {
                    sampled[index] = true;
                    values[index] = sharedCache.sample(field,
                            startX + cellX * FFDModernWorldgenData.CELL_WIDTH,
                            FFDModernWorldgenData.MIN_Y
                                    + cellY * FFDModernWorldgenData.CELL_HEIGHT,
                            startZ + cellZ * FFDModernWorldgenData.CELL_WIDTH);
                }
                return values[index];
            }

        }
    }

    private static final class SurfaceHeightCache {
        private static final int CAPACITY = 1 << 18;
        private static final int MASK = CAPACITY - 1;

        private final long[] keys = new long[CAPACITY];
        private final int[] values = new int[CAPACITY];
        private final boolean[] occupied = new boolean[CAPACITY];

        private int getOrCompute(FFDModernWorldgenData data, int x, int z) {
            long key = (x & 0xFFFFFFFFL) | (long) z << 32;
            int index = index(key);
            if (occupied[index] && keys[index] == key) {
                return values[index];
            }
            int value = data.preliminarySurfaceLevel(x, z);
            occupied[index] = true;
            keys[index] = key;
            values[index] = value;
            return value;
        }

        private static int index(long key) {
            long mixed = key ^ key >>> 33;
            mixed *= 0xff51afd7ed558ccdL;
            mixed ^= mixed >>> 33;
            return (int) mixed & MASK;
        }
    }

    private static final class DensityGridCache {
        private static final int MAX_NODES = 32768;
        private static final int MAIN = 0;
        private static final int NOODLE_TOGGLE = 1;
        private static final int NOODLE_THICKNESS = 2;
        private static final int NOODLE_RIDGE_A = 3;
        private static final int NOODLE_RIDGE_B = 4;

        private final FFDDensityFunction[] functions;
        private final Map<Long, DensityNode> nodes = new LinkedHashMap<Long, DensityNode>(
                MAX_NODES, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, DensityNode> eldest) {
                return size() > MAX_NODES;
            }
        };

        private DensityGridCache(FFDModernWorldgenData.FinalDensityComponents components) {
            functions = new FFDDensityFunction[] {
                    components.main(),
                    components.noodleToggle(),
                    components.noodleThickness(),
                    components.noodleRidgeA(),
                    components.noodleRidgeB()
            };
        }

        private synchronized double sample(int field, int x, int y, int z) {
            long key = gridKey(x, y, z);
            DensityNode node = nodes.get(key);
            if (node == null) {
                node = new DensityNode();
                nodes.put(key, node);
            }
            int mask = 1 << field;
            if ((node.sampledMask & mask) == 0) {
                node.values[field] = functions[field].sample(x, y, z);
                node.sampledMask |= mask;
            }
            return node.values[field];
        }

        private static long gridKey(int x, int y, int z) {
            long gridX = Math.floorDiv(x, FFDModernWorldgenData.CELL_WIDTH) & 0x1FFFFFFL;
            long gridZ = Math.floorDiv(z, FFDModernWorldgenData.CELL_WIDTH) & 0x1FFFFFFL;
            long gridY = Math.floorDiv(y - FFDModernWorldgenData.MIN_Y,
                    FFDModernWorldgenData.CELL_HEIGHT) & 0x3FL;
            return gridX << 31 | gridZ << 6 | gridY;
        }

        private static final class DensityNode {
            private final double[] values = new double[5];
            private int sampledMask;
        }
    }

}
