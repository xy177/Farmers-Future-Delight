package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class NetherForestWorldgenEvents {
    private static final double FOREST_EDGE_BLEND_WIDTH = 0.15D;
    private static long biomeNoiseSeed = Long.MIN_VALUE;
    private static NoiseGeneratorPerlin biomeNoise;
    private static final ThreadLocal<Boolean> GENERATING_FOREST = new ThreadLocal<>();
    private static final Map<World, Map<Long, ForestColumnMap>> FOREST_MAP_CACHE =
            Collections.synchronizedMap(new WeakHashMap<World, Map<Long, ForestColumnMap>>());

    private NetherForestWorldgenEvents() {
    }

    @SubscribeEvent
    public static void assignForestBiome(PopulateChunkEvent.Pre event) {
        World world = event.getWorld();
        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();
        ForestColumnMap forestMap = getForestMap(world, chunkX, chunkZ);
        cacheForestMap(world, chunkX, chunkZ, forestMap);
        if (!forestMap.hasForest()) {
            return;
        }
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
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
        World world = event.getWorld();
        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();
        ForestColumnMap forestMap = takeForestMap(world, chunkX, chunkZ);
        if (forestMap == null) {
            forestMap = getForestMap(world, chunkX, chunkZ);
        }
        if (!forestMap.hasForest()) {
            return;
        }
        GENERATING_FOREST.set(Boolean.TRUE);
        try {
            if (forestMap.hasCrimson()) {
                NetherForestFeatures.generateForest(world, event.getRand(), chunkX, chunkZ,
                        false, forestMap.getStrengths(false));
            }
            if (forestMap.hasWarped()) {
                NetherForestFeatures.generateForest(world, event.getRand(), chunkX, chunkZ,
                        true, forestMap.getStrengths(true));
            }
        } finally {
            GENERATING_FOREST.remove();
        }
    }

    private static void cacheForestMap(World world, int chunkX, int chunkZ,
                                       ForestColumnMap forestMap) {
        synchronized (FOREST_MAP_CACHE) {
            Map<Long, ForestColumnMap> worldCache = FOREST_MAP_CACHE.get(world);
            if (worldCache == null) {
                worldCache = new java.util.LinkedHashMap<Long, ForestColumnMap>(64, 0.75F, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Long, ForestColumnMap> eldest) {
                        return size() > 256;
                    }
                };
                FOREST_MAP_CACHE.put(world, worldCache);
            }
            worldCache.put(chunkKey(chunkX, chunkZ), forestMap);
        }
    }

    private static ForestColumnMap takeForestMap(World world, int chunkX, int chunkZ) {
        synchronized (FOREST_MAP_CACHE) {
            Map<Long, ForestColumnMap> worldCache = FOREST_MAP_CACHE.get(world);
            return worldCache == null ? null : worldCache.remove(chunkKey(chunkX, chunkZ));
        }
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return (chunkX & 0xFFFFFFFFL) | ((long) chunkZ << 32);
    }

    private static ForestColumnMap getForestMap(World world, int chunkX, int chunkZ) {
        ForestColumnMap forestMap = new ForestColumnMap();
        if (world.provider.getDimension() != -1) {
            return forestMap;
        }
        boolean crimson = FFDCompat.shouldGenerateNetherForest(false);
        boolean warped = FFDCompat.shouldGenerateNetherForest(true);
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
