package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;
import java.util.function.IntBinaryOperator;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.BiomeDictionary;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;

public final class MapGenFFDPillagerOutpost extends MapGenStructure {
    private static final int SPACING = 32;
    private static final int SEPARATION = 8;
    private static final int SALT = 165745296;

    @Override
    public String getStructureName() {
        return "FFDPillagerOutpost";
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, SPACING);
        int regionZ = Math.floorDiv(chunkZ, SPACING);
        Random placement = new Random((long) regionX * 341873128712L + (long) regionZ * 132897987541L + world.getSeed() + SALT);
        int candidateX = regionX * SPACING + placement.nextInt(SPACING - SEPARATION);
        int candidateZ = regionZ * SPACING + placement.nextInt(SPACING - SEPARATION);
        if (candidateX != chunkX || candidateZ != chunkZ) return false;
        Random frequency = new Random((long) ((chunkX >> 4) ^ (chunkZ >> 4) << 4) ^ world.getSeed());
        frequency.nextInt();
        if (frequency.nextInt(5) != 0) return false;
        for (int nearbyX = chunkX - 10; nearbyX <= chunkX + 10; nearbyX++) {
            for (int nearbyZ = chunkZ - 10; nearbyZ <= chunkZ + 10; nearbyZ++) {
                if (FFDModernVillageGenerator.isPlacementChunk(world.getSeed(), nearbyX, nearbyZ)) return false;
            }
        }
        Biome biome = world.getBiomeProvider().getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
        return biome == Biomes.DESERT || biome == Biomes.PLAINS || biome == Biomes.SAVANNA
                || biome == Biomes.ICE_PLAINS || biome == Biomes.TAIGA || biome == FFDBiomes.GROVE
                || biome == FFDBiomes.MEADOW || BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        WorldServer server = (WorldServer) world;
        IntBinaryOperator heights = FFDStructureTerrain.heights(server);
        if (heights == null) throw new IllegalArgumentException("Unsupported outpost terrain generator");
        return new FFDJigsawStructureStart(server, rand, chunkX, chunkZ,
                new ResourceLocation(FarmerFutureDelight.MODID, "pillager_outpost/base_plates"),
                7, getStructureName(), heights, false);
    }

    @Override
    public BlockPos getNearestStructurePos(World level, BlockPos position, boolean unexplored) {
        world = level;
        return findNearestStructurePosBySpacing(level, this, position, SPACING, SEPARATION, SALT, false, 100, unexplored);
    }

    void addTerrain(int chunkX, int chunkZ, FFDStructureBeard.Builder builder) {
        for (int nearbyX = chunkX - range; nearbyX <= chunkX + range; nearbyX++) {
            for (int nearbyZ = chunkZ - range; nearbyZ <= chunkZ + range; nearbyZ++) {
                StructureStart start = structureMap.get(ChunkPos.asLong(nearbyX, nearbyZ));
                if (start instanceof FFDJigsawStructureStart) ((FFDJigsawStructureStart) start).addTerrain(builder);
            }
        }
    }
}
