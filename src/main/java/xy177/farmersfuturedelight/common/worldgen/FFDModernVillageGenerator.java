package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureStart;

import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;

/**
 * Keeps the legacy village pieces and biome eligibility, but uses the 26.3
 * random-spread spacing for the extended-height world type.
 */
public final class FFDModernVillageGenerator extends MapGenVillage {
    private static final int SEPARATION = 8;
    private static final int SALT = 10387312;

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
        return world.getBiomeProvider().areBiomesViable(
                originalX * 16 + 8, originalZ * 16 + 8, 0, villageBiomes());
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
        this.world = worldIn;
        return findNearestStructurePosBySpacing(worldIn, this, pos, spacing(), SEPARATION,
                SALT, false, 100, findUnexplored);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new MapGenVillage.Start(world, rand, chunkX, chunkZ, 0);
    }

    private static int spacing() {
        return Math.max(SEPARATION + 1, FFDConfig.modernVillageSpacing);
    }

    private static List<Biome> villageBiomes() {
        if (VILLAGE_SPAWN_BIOMES.contains(FFDBiomes.MEADOW)) {
            return VILLAGE_SPAWN_BIOMES;
        }
        List<Biome> biomes = new ArrayList<>(VILLAGE_SPAWN_BIOMES.size() + 1);
        biomes.addAll(VILLAGE_SPAWN_BIOMES);
        biomes.add(FFDBiomes.MEADOW);
        return biomes;
    }
}
