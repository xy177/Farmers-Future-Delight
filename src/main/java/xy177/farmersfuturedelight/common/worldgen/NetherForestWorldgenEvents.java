package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class NetherForestWorldgenEvents {
    private static final double FOREST_EDGE_BLEND_WIDTH = 0.15D;
    private static long biomeNoiseSeed = Long.MIN_VALUE;
    private static NoiseGeneratorPerlin biomeNoise;
    private static final ThreadLocal<Boolean> GENERATING_FOREST = new ThreadLocal<>();

    private NetherForestWorldgenEvents() {
    }

    @SubscribeEvent
    public static void assignForestBiome(PopulateChunkEvent.Pre event) {
        ForestColumnMap forestMap = getForestMap(event.getWorld(), event.getChunkX(), event.getChunkZ());
        if (!forestMap.hasForest()) {
            return;
        }
        Chunk chunk = event.getWorld().getChunkFromChunkCoords(event.getChunkX(), event.getChunkZ());
        byte[] biomes = chunk.getBiomeArray();
        byte crimsonId = (byte) Biome.getIdForBiome(FFDBiomes.CRIMSON_FOREST);
        byte warpedId = (byte) Biome.getIdForBiome(FFDBiomes.WARPED_FOREST);
        boolean modified = false;
        for (int index = 0; index < biomes.length; index++) {
            ForestType type = forestMap.getType(index);
            if (type == ForestType.NONE) {
                continue;
            }
            byte biomeId = type == ForestType.CRIMSON ? crimsonId : warpedId;
            if (biomes[index] != biomeId) {
                biomes[index] = biomeId;
                modified = true;
            }
        }
        if (modified) {
            chunk.setModified(true);
        }
    }

    @SubscribeEvent
    public static void generateForest(PopulateChunkEvent.Post event) {
        if (Boolean.TRUE.equals(GENERATING_FOREST.get())) {
            return;
        }
        ForestColumnMap forestMap = getForestMap(event.getWorld(), event.getChunkX(), event.getChunkZ());
        if (!forestMap.hasForest()) {
            return;
        }
        GENERATING_FOREST.set(Boolean.TRUE);
        try {
            if (forestMap.hasCrimson()) {
                NetherForestFeatures.generateForest(event.getWorld(), event.getRand(), event.getChunkX(),
                        event.getChunkZ(), false, forestMap.getStrengths(false));
            }
            if (forestMap.hasWarped()) {
                NetherForestFeatures.generateForest(event.getWorld(), event.getRand(), event.getChunkX(),
                        event.getChunkZ(), true, forestMap.getStrengths(true));
            }
        } finally {
            GENERATING_FOREST.remove();
        }
    }

    private static ForestColumnMap getForestMap(World world, int chunkX, int chunkZ) {
        ForestColumnMap forestMap = new ForestColumnMap();
        if (world.provider.getDimension() != -1) {
            return forestMap;
        }
        boolean crimson = FFDItems.isCrimsonEnabled();
        boolean warped = FFDItems.isWarpedEnabled();
        if (!crimson && !warped) {
            return forestMap;
        }

        NoiseGeneratorPerlin noiseGenerator = biomeNoise(world);
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        double threshold = FFDConfig.netherForestBiomeThreshold;
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                double noise = noiseGenerator.getValue((startX + localX) / (double) FFDConfig.netherForestBiomeNoiseScale,
                        (startZ + localZ) / (double) FFDConfig.netherForestBiomeNoiseScale);
                int index = (localZ << 4) | localX;
                if (crimson && noise <= -threshold) {
                    forestMap.set(index, ForestType.CRIMSON, edgeStrength(-threshold - noise));
                } else if (warped && noise >= threshold) {
                    forestMap.set(index, ForestType.WARPED, edgeStrength(noise - threshold));
                }
            }
        }
        return forestMap;
    }

    private static float edgeStrength(double distancePastThreshold) {
        double linear = Math.min(1.0D, distancePastThreshold / FOREST_EDGE_BLEND_WIDTH);
        return (float) (linear * linear * (3.0D - 2.0D * linear));
    }

    private static synchronized NoiseGeneratorPerlin biomeNoise(World world) {
        if (biomeNoise == null || biomeNoiseSeed != world.getSeed()) {
            biomeNoiseSeed = world.getSeed();
            biomeNoise = new NoiseGeneratorPerlin(new Random(world.getSeed() ^ 0x5EED5EEDL), 2);
        }
        return biomeNoise;
    }

    private enum ForestType {
        NONE,
        CRIMSON,
        WARPED
    }

    private static final class ForestColumnMap {
        private final ForestType[] types = new ForestType[256];
        private final float[] crimsonStrengths = new float[256];
        private final float[] warpedStrengths = new float[256];
        private boolean hasCrimson;
        private boolean hasWarped;

        private ForestColumnMap() {
            for (int index = 0; index < types.length; index++) {
                types[index] = ForestType.NONE;
            }
        }

        private void set(int index, ForestType type, float strength) {
            types[index] = type;
            if (type == ForestType.CRIMSON) {
                crimsonStrengths[index] = strength;
                hasCrimson = true;
            } else if (type == ForestType.WARPED) {
                warpedStrengths[index] = strength;
                hasWarped = true;
            }
        }

        private ForestType getType(int index) {
            return types[index];
        }

        private boolean hasForest() {
            return hasCrimson || hasWarped;
        }

        private boolean hasCrimson() {
            return hasCrimson;
        }

        private boolean hasWarped() {
            return hasWarped;
        }

        private float[] getStrengths(boolean warped) {
            return warped ? warpedStrengths : crimsonStrengths;
        }
    }
}
