package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureStart;

import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerTypes;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeProvider;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public final class FFDModernVillageGenerator extends MapGenVillage {
    private static final int SEPARATION = 8;
    private static final int SALT = 10387312;
    private int placementSpacing;
    private boolean spacingInitialized;

    public FFDModernVillageGenerator() {
        placementSpacing = Math.max(SEPARATION + 1, FFDConfig.modernVillageSpacing);
    }

    FFDModernVillageGenerator(Map<String, String> settings) {
        placementSpacing = settings != null && settings.containsKey("distance")
                ? MathHelper.getInt(settings.get("distance"), FFDConfig.modernVillageSpacing, SEPARATION + 1)
                : Math.max(SEPARATION + 1, FFDConfig.modernVillageSpacing);
        spacingInitialized = true;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int spacing = spacing();
        int originalX = chunkX;
        int originalZ = chunkZ;
        if (chunkX < 0) {
            chunkX -= spacing - 1;
        }
        if (chunkZ < 0) {
            chunkZ -= spacing - 1;
        }
        int regionX = chunkX / spacing;
        int regionZ = chunkZ / spacing;
        Random random = world.setRandomSeed(regionX, regionZ, SALT);
        int candidateX = regionX * spacing + random.nextInt(spacing - SEPARATION);
        int candidateZ = regionZ * spacing + random.nextInt(spacing - SEPARATION);
        if (originalX != candidateX || originalZ != candidateZ) {
            return false;
        }
        return isVillageBiome(originalX * 16 + 8, originalZ * 16 + 8);
    }

    private boolean isVillageBiome(int blockX, int blockZ) {
        BiomeProvider provider = world.getBiomeProvider();
        if (provider instanceof FFDModernBiomeProvider) {
            Biome biome = provider.getBiome(new BlockPos(blockX, 0, blockZ));
            ResourceLocation identifier = biome.getRegistryName();
            if (identifier != null && "minecraft".equals(identifier.getResourceDomain())) {
                switch (((FFDModernBiomeProvider) provider).resolver().sampleFuzzy(blockX, blockZ).biome) {
                    case PLAINS:
                    case MEADOW:
                    case DESERT:
                    case SAVANNA:
                    case SNOWY_PLAINS:
                    case TAIGA: return true;
                    default: return false;
                }
            }
        }
        return provider.areBiomesViable(blockX, blockZ, 0, villageBiomes());
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
        this.world = worldIn;
        return findNearestStructurePosBySpacing(worldIn, this, pos, spacing(), SEPARATION,
                SALT, false, 100, findUnexplored);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        if (world instanceof WorldServer) {
            java.util.function.IntBinaryOperator heights = FFDStructureTerrain.heights((WorldServer) world);
            if (heights != null) {
                String type = FFDVillagerTypes.byBiome(world.getBiomeProvider().getBiome(
                        new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8)));
                if ("snow".equals(type)) type = "snowy";
                if (!"desert".equals(type) && !"savanna".equals(type) && !"taiga".equals(type) && !"snowy".equals(type)) type = "plains";
                return new FFDJigsawStructureStart((WorldServer) world, rand, chunkX, chunkZ,
                        new ResourceLocation(FarmerFutureDelight.MODID, "village/" + type + "/town_centers"),
                        6, getStructureName(), heights, true);
            }
        }
        return new MapGenVillage.Start(world, rand, chunkX, chunkZ, 0);
    }

    private int spacing() {
        if (!spacingInitialized && world instanceof WorldServer) {
            IChunkGenerator generator = FFDStructureTerrain.generator((WorldServer) world);
            if (generator instanceof ChunkGeneratorFlat) {
                FlatGeneratorInfo settings = ObfuscationReflectionHelper.getPrivateValue(
                        ChunkGeneratorFlat.class, (ChunkGeneratorFlat) generator, "field_82699_e");
                Map<String, String> village = settings.getWorldFeatures().get("village");
                if (village != null && village.containsKey("distance")) {
                    placementSpacing = MathHelper.getInt(village.get("distance"), placementSpacing, SEPARATION + 1);
                }
            }
            spacingInitialized = true;
        }
        return placementSpacing;
    }

    void addTerrain(int chunkX, int chunkZ, FFDStructureBeard.Builder builder) {
        for (int nearbyX = chunkX - range; nearbyX <= chunkX + range; nearbyX++) {
            for (int nearbyZ = chunkZ - range; nearbyZ <= chunkZ + range; nearbyZ++) {
                StructureStart start = structureMap.get(ChunkPos.asLong(nearbyX, nearbyZ));
                if (start instanceof FFDJigsawStructureStart) ((FFDJigsawStructureStart) start).addTerrain(builder);
            }
        }
    }

    static boolean isPlacementChunk(long seed, int chunkX, int chunkZ) {
        int distance = Math.max(SEPARATION + 1, FFDConfig.modernVillageSpacing);
        int regionX = Math.floorDiv(chunkX, distance);
        int regionZ = Math.floorDiv(chunkZ, distance);
        Random placement = new Random((long) regionX * 341873128712L + (long) regionZ * 132897987541L + seed + SALT);
        return chunkX == regionX * distance + placement.nextInt(distance - SEPARATION)
                && chunkZ == regionZ * distance + placement.nextInt(distance - SEPARATION);
    }

    private static List<Biome> villageBiomes() {
        List<Biome> biomes = new ArrayList<>(VILLAGE_SPAWN_BIOMES.size() + 1);
        biomes.addAll(VILLAGE_SPAWN_BIOMES);
        if (!biomes.contains(FFDBiomes.MEADOW)) biomes.add(FFDBiomes.MEADOW);
        if (!biomes.contains(Biomes.ICE_PLAINS)) biomes.add(Biomes.ICE_PLAINS);
        return biomes;
    }
}
