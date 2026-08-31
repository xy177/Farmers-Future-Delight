package xy177.farmersfuturedelight.common.worldgen;

import java.util.Collections;
import java.util.Random;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import xy177.farmersfuturedelight.common.registry.FFDBiomes;

public final class FFDModernScatteredFeatureGenerator extends MapGenScatteredFeature {
    private static final int MIN_DISTANCE = 8;
    private static final int SALT = 14357617;

    private final int maxDistance;

    public FFDModernScatteredFeatureGenerator(MapGenScatteredFeature original) {
        this(readMaxDistance(original));
        if (original != null) {
            getMonsters().clear();
            getMonsters().addAll(original.getMonsters());
        }
    }

    private FFDModernScatteredFeatureGenerator(int maxDistance) {
        super(Collections.singletonMap("distance", Integer.toString(maxDistance)));
        this.maxDistance = maxDistance;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        if (super.canSpawnStructureAtCoords(chunkX, chunkZ)) {
            return true;
        }
        int originalX = chunkX;
        int originalZ = chunkZ;
        if (chunkX < 0) {
            chunkX -= maxDistance - 1;
        }
        if (chunkZ < 0) {
            chunkZ -= maxDistance - 1;
        }
        int regionX = chunkX / maxDistance;
        int regionZ = chunkZ / maxDistance;
        Random random = world.setRandomSeed(regionX, regionZ, SALT);
        int candidateX = regionX * maxDistance
                + random.nextInt(maxDistance - MIN_DISTANCE);
        int candidateZ = regionZ * maxDistance
                + random.nextInt(maxDistance - MIN_DISTANCE);
        if (originalX != candidateX || originalZ != candidateZ) {
            return false;
        }
        Biome biome = world.getBiomeProvider().getBiome(
                new BlockPos(originalX * 16 + 8, 0, originalZ * 16 + 8));
        return biome == FFDBiomes.SNOWY_SLOPES;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        Biome biome = world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
        if (biome == FFDBiomes.SNOWY_SLOPES) {
            return new MapGenScatteredFeature.Start(
                    world, rand, chunkX, chunkZ, Biomes.ICE_PLAINS);
        }
        return super.getStructureStart(chunkX, chunkZ);
    }

    private static int readMaxDistance(MapGenScatteredFeature original) {
        if (original == null) {
            return 32;
        }
        try {
            Integer value = ObfuscationReflectionHelper.getPrivateValue(
                    MapGenScatteredFeature.class, original, "field_82669_g");
            return value == null ? 32 : Math.max(MIN_DISTANCE + 1, value);
        } catch (RuntimeException exception) {
            return 32;
        }
    }
}
