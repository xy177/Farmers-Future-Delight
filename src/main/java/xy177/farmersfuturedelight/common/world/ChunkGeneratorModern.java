package xy177.farmersfuturedelight.common.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.BitSet;

import xy177.farmersfuturedelight.common.world.modern.*;

public final class ChunkGeneratorModern extends ChunkGeneratorOverworld implements FFDWorldgenTerrain {
    private static final IBlockState STONE = Blocks.STONE.getDefaultState();
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();
    private static final EnumFacing[] DIRECTIONS = EnumFacing.values();

    private final World world;
    private final WorldGenAmethystGeodes amethystGeodes;
    private final WorldGenModernOreVeins modernOreVeins;
    private final WorldGenModernFossils modernFossils;
    private final WorldGenModernOres modernOres;
    private final WorldGenUnderwaterMagma underwaterMagma;
    private final WorldGenVerticalCaveBiomes caveBiomes;
    private final WorldGenModernCarvers modernCarvers;
    private final WorldGenModernIcebergs modernIcebergs;
    private final WorldGenModernMountainBiomes mountainBiomes;
    private final FFDModernStoneProvider modernStones;
    private final FFDLushCaveBlockProvider lushBlocks;
    private final FFDModernWorldgenData worldgenData;
    private final FFDBiomeTerrainBridge terrainBridge;
    private final FFDModernSurfaceRules surfaceRules;
    private final FFDVerticalBiomeSampler verticalBiomeSampler;
    private final long biomeZoomSeed;
    private final Map<Long, int[]> pendingFluidUpdates = new HashMap<>();
    private final Map<Long, Integer> preliminaryHeights = new LinkedHashMap<Long, Integer>(32768, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Integer> eldest) {
            return size() > 32768;
        }
    };
    private Biome[] biomes;
    private long structureTerrainChunks;


    private final ModernNoiseContext noiseContext;
    private final ModernGenerationScheduler scheduler;
    private final Map<Long, TerrainSample> terrainSamples = new LinkedHashMap<Long, TerrainSample>(128, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, TerrainSample> eldest) {
            return size() > 128;
        }
    };

    public ChunkGeneratorModern(World world, long seed, boolean mapFeaturesEnabled, String generatorOptions, ModernGenerationSettings settings) {
        super(world, seed, mapFeaturesEnabled, generatorOptions);
        this.world = world;
        this.amethystGeodes = new WorldGenAmethystGeodes(seed);
        this.modernStones = new FFDModernStoneProvider();
        this.lushBlocks = FFDLushCaveBlockProvider.get();
        this.modernOres = new WorldGenModernOres(seed, modernStones);
        this.underwaterMagma = new WorldGenUnderwaterMagma(seed);
        this.caveBiomes = new WorldGenVerticalCaveBiomes(seed, modernStones);
        this.modernCarvers = new WorldGenModernCarvers(seed);
        this.mountainBiomes = new WorldGenModernMountainBiomes(seed);
        FFDModernBiomeProvider biomeProvider = world.getBiomeProvider() instanceof FFDModernBiomeProvider
                ? (FFDModernBiomeProvider) world.getBiomeProvider() : null;
        this.worldgenData = biomeProvider != null
                ? biomeProvider.worldgenData() : new FFDModernWorldgenData(seed);
        FFDModernBiomeResolver resolver = biomeProvider != null
                ? biomeProvider.resolver() : new FFDModernBiomeResolver(worldgenData, seed, true);
        this.terrainBridge = new FFDBiomeTerrainBridge(resolver);
        this.modernIcebergs = new WorldGenModernIcebergs(seed, resolver);
        this.modernOreVeins = new WorldGenModernOreVeins(worldgenData, modernStones);
        this.modernFossils = new WorldGenModernFossils(seed, modernStones);
        this.verticalBiomeSampler = new FFDVerticalBiomeSampler(worldgenData);
        this.biomeZoomSeed = FFDVerticalBiomeZoomer.obfuscateSeed(seed);
        this.surfaceRules = new FFDModernSurfaceRules(world, worldgenData,
                verticalBiomeSampler, modernStones);
        noiseContext = new ModernNoiseContext(seed, modernStones);
        scheduler = new ModernGenerationScheduler(seed, world.provider.hasSkyLight(), modernStones, settings,
                this::generationInput);
        ModernGenerationLifecycle.attach(world, scheduler);
        org.apache.logging.log4j.LogManager.getLogger("FFD Modern Worldgen").info(
                "Using {} with workers={}, lookahead={}, capacity={}",
                ModernGenerationSettings.ENGINE, settings.workers, settings.lookahead, settings.capacity);
        FFDStructureHooks.installModernGenerators(this, world, this);
        world.setSeaLevel(FFDModernWorldgenData.SEA_LEVEL);
    }

    public int estimateModernSurfaceHeight(int blockX, int blockZ) {
        return worldgenData.preliminarySurfaceLevel(blockX, blockZ);
    }

    private ModernGenerationInput generationInput(int chunkX, int chunkZ) {
        int radius = WorldGenModernOres.originRadius();
        int width = (radius * 2 + 1) * 16;
        Biome[] snapshot = new Biome[width * width];
        for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                Biome[] tile = world.getBiomeProvider().getBiomes(null,
                        (chunkX + offsetX) << 4, (chunkZ + offsetZ) << 4, 16, 16);
                for (int localZ = 0; localZ < 16; localZ++) {
                    System.arraycopy(tile, localZ * 16, snapshot,
                            ((offsetZ + radius) * 16 + localZ) * width + (offsetX + radius) * 16, 16);
                }
            }
        }
        return new ModernGenerationInput(chunkX, chunkZ, radius, snapshot);
    }

    public int structureSurfaceHeight(int blockX, int blockZ) {
        return structureSurfaceHeight(blockX, blockZ, FFDStructureBeard.EMPTY);
    }

    public int structureSurfaceHeight(int blockX, int blockZ, FFDStructureBeard beard) {
        return structureHeight(blockX, blockZ, beard, false);
    }

    @Override
    public int structureFloorHeight(int blockX, int blockZ) {
        return structureHeight(blockX, blockZ, FFDStructureBeard.EMPTY, true);
    }

    @Override
    public IBlockState[] structureBaseColumn(int blockX, int blockZ) {
        TerrainSample sample = new GenerationRegion().sample(blockX >> 4, blockZ >> 4);
        ModernNoiseChunk.Column column = sample.densitySampler.column(blockX & 15, blockZ & 15);
        IBlockState[] states = new IBlockState[FFDModernWorldgenData.HEIGHT];
        for (int offset = 0; offset < states.length; offset++) {
            int height = FFDModernWorldgenData.MIN_Y + offset;
            states[offset] = sample.baseStateForDensity(blockX, height, blockZ, column.sample(height), surfaceRules);
        }
        return states;
    }

    private int structureHeight(int blockX, int blockZ, FFDStructureBeard beard, boolean solid) {
        GenerationRegion region = new GenerationRegion();
        TerrainSample sample = region.sample(blockX >> 4, blockZ >> 4);
        ModernNoiseChunk.Column column = sample.densitySampler.column(blockX & 15, blockZ & 15);
        for (int height = FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1;
             height >= FFDModernWorldgenData.MIN_Y; height--) {
            net.minecraft.block.material.Material material = sample.baseStateForDensity(blockX, height, blockZ,
                    column.sample(height) + beard.sample(blockX, height, blockZ), surfaceRules)
                    .getMaterial();
            if (solid ? material.blocksMovement() : material != net.minecraft.block.material.Material.AIR) return height + 1;
        }
        return FFDModernWorldgenData.MIN_Y;
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        long profileChunkStart = FFDWorldgenProfiler.ENABLED ? System.nanoTime() : 0L;
        long profileStageStart = profileChunkStart;
        long[] profileStages = FFDWorldgenProfiler.ENABLED ? new long[12] : null;
        ModernProtoChunk prepared = scheduler.take(chunkX, chunkZ);
        scheduler.prefetch(chunkX, chunkZ);
        Chunk chunk = new Chunk(world, chunkX, chunkZ);
        TerrainSample terrainSample = remember(prepared.noise);
        FFDModernAquifer aquifer = terrainSample.aquifer;
        ModernNoiseChunk densitySampler = prepared.noise;
        GenerationRegion generationRegion = new GenerationRegion();
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        FFDStructureBeard structureBeard = FFDModernStructures.prepareTerrain(world, this, chunkX, chunkZ);
        if (!structureBeard.isEmpty()) structureTerrainChunks++;
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[0] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        List<Integer> fluidUpdates = new ArrayList<>();
        if (structureBeard.isEmpty() && prepared.hasTerrain()) {
            prepared.commitTo(chunk);
            terrainSample.rememberSurfaceHeights(prepared.surfaceHeights());
            for (int packed : prepared.fluidUpdates()) fluidUpdates.add(packed);
            biomes = world.getBiomeProvider().getBiomes(biomes, startX, startZ, 16, 16);
            return finishChunk(chunk, chunkX, chunkZ, prepared.verticalBiomes(), fluidUpdates,
                    profileChunkStart, profileStages);
        }
        int[] oceanFloorY;
        if (structureBeard.isEmpty()) {
            prepared.commitTo(chunk);
            oceanFloorY = prepared.surfaceHeights();
            for (int packed : prepared.fluidUpdates()) fluidUpdates.add(packed);
            terrainSample.rememberSurfaceHeights(oceanFloorY);
        } else {
            oceanFloorY = new int[256];
            Arrays.fill(oceanFloorY, FFDModernWorldgenData.MIN_Y - 1);
            WorldGenModernOreVeins.Sampler oreVeins = modernOreVeins.sampler(startX, startZ);
            for (int localX = 0; localX < 16; localX++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    ModernNoiseChunk.Column column = densitySampler.column(localX, localZ);
                    for (int blockY = FFDModernWorldgenData.MIN_Y;
                         blockY < FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT; blockY++) {
                        int blockX = startX + localX;
                        int blockZ = startZ + localZ;
                        IBlockState state = aquifer.computeSubstance(blockX, blockY, blockZ,
                                column.sample(blockY) + structureBeard.sample(blockX, blockY, blockZ));
                        if (state == null) state = oreVeins.stateAt(blockX, blockY, blockZ);
                        if (state == null) state = blockY < 0 ? surfaceRules.stoneStateAt(blockX, blockY, blockZ) : STONE;
                        if (state.getMaterial().isSolid()) oceanFloorY[localZ * 16 + localX] = blockY;
                        if (aquifer.shouldScheduleFluidUpdate() && isFluid(state)) {
                            fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(localX, blockY, localZ));
                        }
                        if (!isAir(state)) setDirect(chunk, localX, blockY, localZ, state);
                    }
                }
            }
        }
        biomes = world.getBiomeProvider().getBiomes(biomes, startX, startZ, 16, 16);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[1] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
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
            public IBlockState getState(int localX, int blockY, int localZ) {
                return chunk.getBlockState(new BlockPos(startX + localX, blockY, startZ + localZ));
            }

            @Override
            public void setState(int localX, int blockY, int localZ, IBlockState state) {
                setDirect(chunk, localX, blockY, localZ, state);
            }

            @Override
            public IBlockState carveState(int localX, int blockY, int localZ) {
                return aquifer.computeSubstance(startX + localX, blockY, startZ + localZ, 0.0D);
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return aquifer.shouldScheduleFluidUpdate();
            }

            @Override
            public void scheduleFluidUpdate(int localX, int blockY, int localZ) {
                fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(localX, blockY, localZ));
            }

            @Override
            public int surfaceY(int localX, int localZ) {
                return structureBeard.isEmpty() ? terrainSample.surfaceY(localX, localZ, surfaceRules)
                        : oceanFloorY[localZ * 16 + localX];
            }
        });
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[4] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        recomputeOceanFloor(chunk, oceanFloorY);
        clearLegacyBedrockLayer(chunk, densitySampler, aquifer, structureBeard, startX, startZ, fluidUpdates);
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
        generateUnderwaterMagma(chunk, chunkX, chunkZ, generationRegion);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[9] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        caveBiomes.generateUndergroundDecoration(chunkX, chunkZ, verticalBiomes, caveAccess);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[10] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        caveAccess.setPostCarverNeighbors(false);
        caveBiomes.generateVegetation(chunkX, chunkZ, verticalBiomes, caveAccess);
        caveAccess.setPostCarverNeighbors(true);
        validateGeneratedGlowLichen(caveAccess, generatedGlowLichen, chunkX, chunkZ);
        if (FFDWorldgenProfiler.ENABLED) {
            profileStages[11] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        return finishChunk(chunk, chunkX, chunkZ, verticalBiomes, fluidUpdates, profileChunkStart, profileStages);
    }

    private Chunk finishChunk(Chunk chunk, int chunkX, int chunkZ,
                              FFDVerticalBiomeData verticalBiomes, List<Integer> fluidUpdates,
                              long profileChunkStart, long[] profileStages) {
        long finalizeStart = FFDWorldgenProfiler.ENABLED ? System.nanoTime() : 0L;
        FFDVerticalBiomeManager.put(world, chunkX, chunkZ, verticalBiomes);
        rememberFluidUpdates(chunkX, chunkZ, fluidUpdates);

        byte[] biomeIds = chunk.getBiomeArray();
        for (int index = 0; index < biomeIds.length; index++) {
            biomeIds[index] = (byte) Biome.getIdForBiome(biomes[index]);
        }
        long profileStructureStart = FFDWorldgenProfiler.ENABLED ? System.nanoTime() : 0L;
        super.recreateStructures(chunk, chunkX, chunkZ);
        long profileStructureNanos = FFDWorldgenProfiler.ENABLED
                ? System.nanoTime() - profileStructureStart : 0L;
        long profileSkylightStart = FFDWorldgenProfiler.ENABLED ? System.nanoTime() : 0L;
        chunk.generateSkylightMap();
        if (FFDWorldgenProfiler.ENABLED) {
            long profileSkylightNanos = System.nanoTime() - profileSkylightStart;
            profileStages[11] += System.nanoTime() - finalizeStart;
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
            public IBlockState getState(int blockX, int blockY, int blockZ) {
                if (blockY < FFDModernWorldgenData.MIN_Y
                        || blockY >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                if (blockX >> 4 == chunkX && blockZ >> 4 == chunkZ) {
                    return chunk.getBlockState(new BlockPos(blockX, blockY, blockZ));
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
                return world.getBiomeProvider().getBiome(new BlockPos(blockX, 0, blockZ));
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

    private void generateUnderwaterMagma(Chunk chunk, int chunkX, int chunkZ,
                                         GenerationRegion generationRegion) {
        underwaterMagma.generateChunk(chunkX, chunkZ, new WorldGenUnderwaterMagma.Access() {
            @Override
            public IBlockState getState(int blockX, int blockY, int blockZ) {
                if (blockY < FFDModernWorldgenData.MIN_Y
                        || blockY >= FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT) {
                    return Blocks.AIR.getDefaultState();
                }
                if (blockX >> 4 == chunkX && blockZ >> 4 == chunkZ) {
                    return chunk.getBlockState(new BlockPos(blockX, blockY, blockZ));
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
                return getState(blockX, blockY, blockZ).getBlockFaceShape(world, pos, face);
            }
        });
    }

    private void generateAmethystGeodes(Chunk chunk, int chunkX, int chunkZ,
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

    private NeighborAwareCaveAccess caveBiomeAccess(Chunk chunk, int chunkX, int chunkZ,
                                                    FFDVerticalBiomeData verticalBiomes,
                                                    List<Integer> fluidUpdates,
                                                    int[] oceanFloorY,
                                                    GenerationRegion generationRegion,
                                                    Set<Integer> generatedGlowLichen) {
        Map<Long, IBlockState> decorationOverlay = new HashMap<>();
        Map<Long, IBlockState> neighborStateCache = new HashMap<>();
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
                if (postCarverNeighbors) {
                    return sample.postCarverStateAt(localX, blockY, localZ, blockX, blockZ, surfaceRules);
                }
                long key = blockKey(blockX, blockY, blockZ);
                IBlockState cachedState = neighborStateCache.get(key);
                if (cachedState != null) {
                    return cachedState;
                }
                IBlockState state = sample.baseStateAt(localX, blockY, localZ, blockX, blockZ, surfaceRules);
                neighborStateCache.put(key, state);
                return state;
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
                return getState(blockX, blockY, blockZ).getBlockFaceShape(world, pos, face);
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
                Integer height = preliminaryHeights.get(key);
                if (height == null) {
                    height = worldgenData.preliminarySurfaceLevel(blockX, blockZ);
                    preliminaryHeights.put(key, height);
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

    private void validateGeneratedGlowLichen(WorldGenVerticalCaveBiomes.Access access,
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
                if (!support.isSideSolid(world, supportPos, supportFace)
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

    private void clearLegacyBedrockLayer(Chunk chunk, ModernNoiseChunk densitySampler, FFDModernAquifer aquifer,
                                         FFDStructureBeard structureBeard, int startX, int startZ,
                                         @Nullable List<Integer> fluidUpdates) {
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int blockY = 0; blockY < 5; blockY++) {
                    BlockPos pos = new BlockPos(startX + localX, blockY, startZ + localZ);
                    if (chunk.getBlockState(pos).getBlock() != Blocks.BEDROCK) {
                        continue;
                    }
                    double density = densitySampler.sample(localX, blockY, localZ)
                            + structureBeard.sample(pos.getX(), blockY, pos.getZ());
                    IBlockState substance = aquifer.computeSubstance(pos.getX(), blockY, pos.getZ(), density);
                    IBlockState replacement = substance == null
                            ? surfaceRules.stoneStateAt(pos.getX(), blockY, pos.getZ()) : substance;
                    setDirect(chunk, localX, blockY, localZ, replacement);
                    if (fluidUpdates != null && aquifer.shouldScheduleFluidUpdate() && isFluid(replacement)) {
                        fluidUpdates.add(FFDHeightHooks.packLocalBlockChange(localX, blockY, localZ));
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
        ModernNoiseChunk densitySampler = terrainSample.densitySampler;
        FFDModernAquifer aquifer = terrainSample.aquifer;
        FFDStructureBeard structureBeard = FFDModernStructures.prepareTerrain(world, this, chunkX, chunkZ);
        clearLegacyBedrockLayer(chunk, densitySampler, aquifer, structureBeard, startX, startZ, fluidUpdates);
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
        for (int index = 0; index < packed.length; index++) {
            packed[index] = updates.get(index);
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
        int blockY = FFDHeightHooks.unpackLocalBlockY(packed);
        BlockPos pos = new BlockPos((chunkX << 4) + localX, blockY, (chunkZ << 4) + localZ);
        IBlockState state = chunk.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.WATER || block == Blocks.LAVA) {
            Block flowing = block == Blocks.WATER ? Blocks.FLOWING_WATER : Blocks.FLOWING_LAVA;
            setDirect(chunk, localX, blockY, localZ, flowing.getDefaultState().withProperty(
                    BlockLiquid.LEVEL, state.getValue(BlockLiquid.LEVEL)));
            world.scheduleUpdate(pos, flowing, flowing.tickRate(world));
            return true;
        } else if (block == Blocks.FLOWING_WATER || block == Blocks.FLOWING_LAVA) {
            world.scheduleUpdate(pos, block, block.tickRate(world));
        } else if (WaterloggedBlockApi.isWaterlogged(state)) {
            world.scheduleUpdate(pos, block, 5);
        }
        return false;
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return (chunkX & 0xFFFFFFFFL) | (long) chunkZ << 32;
    }

    private static long blockKey(int blockX, int blockY, int blockZ) {
        return ((long) blockX & 0x3FFFFFFL) << 38
                | ((long) blockZ & 0x3FFFFFFL) << 12
                | (long) blockY & 0xFFFL;
    }

    private static IBlockState getDirect(Chunk chunk, int localX, int blockY, int localZ) {
        int index = FFDHeightHooks.storageIndex(blockY, chunk.getWorld());
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        if (index < 0 || index >= storage.length) {
            return Blocks.AIR.getDefaultState();
        }
        ExtendedBlockStorage section = storage[index];
        return section == Chunk.NULL_BLOCK_STORAGE
                ? Blocks.AIR.getDefaultState() : section.get(localX, blockY & 15, localZ);
    }

    private static boolean hasLegacyBedrock(Chunk chunk, int startX, int startZ) {
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int blockY = 0; blockY < 5; blockY++) {
                    if (chunk.getBlockState(new BlockPos(startX + localX, blockY, startZ + localZ)).getBlock()
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
                for (int blockY = FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1;
                     blockY >= FFDModernWorldgenData.MIN_Y; blockY--) {
                    if (chunk.getBlockState(new BlockPos(localX, blockY, localZ))
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
        TerrainSample sample = terrainSamples.get(key);
        if (sample == null) sample = remember(noiseContext.sample(chunkX, chunkZ));
        return sample;
    }

    private TerrainSample remember(ModernNoiseChunk noise) {
        TerrainSample sample = new TerrainSample(worldgenData, noise, modernCarvers, noise.chunkX, noise.chunkZ);
        terrainSamples.put(chunkKey(noise.chunkX, noise.chunkZ), sample);
        return sample;
    }

    public Map<String, Long> generationStatistics() {
        Map<String, Long> result = scheduler.statistics();
        result.put("structureTerrainChunks", structureTerrainChunks);
        return result;
    }

    private final class GenerationRegion {
        private final Map<Long, TerrainSample> local = new HashMap<>();

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
        private final ModernNoiseChunk densitySampler;
        private final FFDModernAquifer aquifer;
        private final WorldGenModernCarvers modernCarvers;
        private final int chunkX;
        private final int chunkZ;
        private final int[] surfaceHeights = new int[16 * 16];
        private final int[] surfaceLowerBounds = new int[16 * 16];
        private final int[] surfaceUpperBounds = new int[16 * 16];
        private WorldGenModernCarvers.CarvingMask carvingMask;

        private TerrainSample(FFDModernWorldgenData data, ModernNoiseChunk noise,
                              WorldGenModernCarvers modernCarvers, int chunkX, int chunkZ) {
            densitySampler = noise;
            aquifer = new FFDModernAquifer(data, chunkX, chunkZ);
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
                                        FFDModernSurfaceRules surfaceRules) {
            return baseStateForDensity(worldX, blockY, worldZ,
                    densitySampler.sample(localX, blockY, localZ), surfaceRules);
        }

        private IBlockState baseStateForDensity(int worldX, int blockY, int worldZ, double density,
                                               FFDModernSurfaceRules surfaceRules) {
            IBlockState substance = aquifer.computeSubstance(worldX, blockY, worldZ, density);
            return substance == null
                    ? surfaceRules.stoneStateAt(worldX, blockY, worldZ) : substance;
        }

        private IBlockState postCarverStateAt(int localX, int blockY, int localZ,
                                              int worldX, int worldZ,
                                              FFDModernSurfaceRules surfaceRules) {
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
                                          FFDModernSurfaceRules surfaceRules) {
            if (!carvingMask().contains(localX, blockY, localZ)
                    || blockY > surfaceY(localX, localZ, surfaceRules) - 4) {
                return null;
            }
            return aquifer.computeSubstance(worldX, blockY, worldZ, 0.0D);
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
            ModernNoiseChunk.Column column = densitySampler.column(localX & 15, localZ & 15);
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
                                       FFDModernSurfaceRules surfaceRules) {
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
            ModernNoiseChunk.Column column = densitySampler.column(localX & 15, localZ & 15);
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

    private void setDirect(Chunk chunk, int localX, int blockY, int localZ, IBlockState state) {
        int index = FFDHeightHooks.storageIndex(blockY, world);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        if (index < 0 || index >= storage.length) {
            return;
        }
        ExtendedBlockStorage section = storage[index];
        if (section == Chunk.NULL_BLOCK_STORAGE) {
            section = new ExtendedBlockStorage(blockY >> 4 << 4, world.provider.hasSkyLight());
            storage[index] = section;
        }
        section.set(localX, blockY & 15, localZ, state);
    }

    @Override
    public void populate(int blockX, int blockZ) {
        modernIcebergs.generate(world, blockX, blockZ);
        try (FFDHeightHooks.WorldgenHeightScope ignored = FFDHeightHooks.enterLegacyWorldgenHeight()) {
            super.populate(blockX, blockZ);
        }
        WorldGenModernBamboo.generate(world, blockX, blockZ);
        mountainBiomes.generate(world, blockX, blockZ);
        modernFossils.generateChunk(world, blockX, blockZ);
        Chunk chunk = world.getChunkFromChunkCoords(blockX, blockZ);
        List<Integer> restoredFluidUpdates = clearLegacyBedrockAfterPopulate(chunk, blockX, blockZ);
        scheduleFluidUpdates(chunk, blockX, blockZ);
        scheduleFluidUpdates(chunk, blockX, blockZ, restoredFluidUpdates);
        validateGeneratedVines(blockX, blockZ);
        xy177.farmersfuturedelight.common.worldgen.GlowLichenSupport
                .validatePopulatedRegion(world, blockX, blockZ);
    }

    private void validateGeneratedVines(int chunkX, int chunkZ) {
        int startX = (chunkX << 4) + 8;
        int startZ = (chunkZ << 4) + 8;
        if (!world.isAreaLoaded(new BlockPos(startX - 1, FFDModernWorldgenData.MIN_Y, startZ - 1),
                new BlockPos(startX + 16, FFDModernWorldgenData.MIN_Y, startZ + 16))) {
            return;
        }
        Map<Chunk, boolean[]> sectionsWithVines = new java.util.IdentityHashMap<>();
        for (int blockX = startX; blockX < startX + 16; blockX++) {
            for (int blockZ = startZ; blockZ < startZ + 16; blockZ++) {
                Chunk chunk = world.getChunkFromChunkCoords(blockX >> 4, blockZ >> 4);
                boolean[] scanSections = sectionsWithVines.computeIfAbsent(chunk, current -> {
                    boolean[] matches = new boolean[FFDModernWorldgenData.HEIGHT >> 4];
                    ExtendedBlockStorage[] storage = current.getBlockStorageArray();
                    for (int logical = 0; logical < matches.length; logical++) {
                        int index = FFDHeightHooks.storageIndex(FFDModernWorldgenData.MIN_Y + (logical << 4), world);
                        matches[logical] = index >= 0 && index < storage.length
                                && ModernSectionContents.mayContain(storage[index], state -> state.getBlock() == Blocks.VINE);
                    }
                    return matches;
                });
                for (int blockY = FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1;
                     blockY >= FFDModernWorldgenData.MIN_Y; blockY--) {
                    if (!scanSections[(blockY - FFDModernWorldgenData.MIN_Y) >> 4]) {
                        blockY &= -16;
                        continue;
                    }
                    IBlockState state = getDirect(chunk, blockX & 15, blockY, blockZ & 15);
                    if (state.getBlock() != Blocks.VINE) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(blockX, blockY, blockZ);
                    if (!FFDVerticalBiomeManager.isBiome(world, pos, FFDVerticalBiome.LUSH_CAVES)) {
                        continue;
                    }
                    IBlockState validState = state;
                    IBlockState above = world.getBlockState(pos.up());
                    for (EnumFacing face : EnumFacing.Plane.HORIZONTAL) {
                        if (state.getValue(BlockVine.getPropertyFor(face))
                                && !MapGenLushCaves.isClassicVineSupport(world,
                                pos.offset(face), face.getOpposite())
                                && !(above.getBlock() == Blocks.VINE
                                && above.getValue(BlockVine.getPropertyFor(face)))) {
                            validState = validState.withProperty(BlockVine.getPropertyFor(face), false);
                        }
                    }
                    if (BlockVine.getNumGrownFaces(validState) == 0) {
                        validState = Blocks.AIR.getDefaultState();
                    }
                    if (validState != state) {
                        world.setBlockState(pos, validState, 2);
                    }
                }
            }
        }
    }

    @Override
    public boolean generateStructures(Chunk chunk, int blockX, int blockZ) {
        return super.generateStructures(chunk, blockX, blockZ);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World world, String structureName, BlockPos position,
                                           boolean findUnexplored) {
        return super.getNearestStructurePos(world, structureName, position, findUnexplored);
    }

    @Override
    public void recreateStructures(Chunk chunk, int blockX, int blockZ) {
        super.recreateStructures(chunk, blockX, blockZ);
    }

    @Override
    public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
        return super.isInsideStructure(world, structureName, pos);
    }

}
