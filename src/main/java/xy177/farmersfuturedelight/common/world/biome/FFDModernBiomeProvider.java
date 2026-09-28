package xy177.farmersfuturedelight.common.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraft.world.storage.WorldInfo;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class FFDModernBiomeProvider extends BiomeProvider {
    private static final int MAX_CHUNK_CACHE = 1024;

    private final BiomeProvider legacyProvider;
    private final FFDBiomeCandidateSelector candidateSelector;
    private final FFDModernWorldgenData worldgenData;
    private final FFDModernBiomeResolver resolver;
    private final long zoomSeed;
    private final Map<Long, Biome[]> chunkCache =
            new LinkedHashMap<Long, Biome[]>(MAX_CHUNK_CACHE, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, Biome[]> eldest) {
                    return size() > MAX_CHUNK_CACHE;
                }
            };

    public FFDModernBiomeProvider(World world) {
        super();
        WorldInfo info = world.getWorldInfo();
        legacyProvider = new BiomeProvider(info);
        worldgenData = new FFDModernWorldgenData(world.getSeed());
        resolver = new FFDModernBiomeResolver(worldgenData, world.getSeed());
        candidateSelector = new FFDBiomeCandidateSelector(world.getSeed());
        zoomSeed = FFDVerticalBiomeZoomer.obfuscateSeed(world.getSeed());
    }

    public FFDModernWorldgenData worldgenData() {
        return worldgenData;
    }

    public FFDModernBiomeResolver resolver() {
        return resolver;
    }

    public BiomeProvider legacyProvider() {
        return legacyProvider;
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return candidateSelector.spawnBiomes();
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return resolve(pos.getX(), pos.getZ());
    }

    @Override
    public Biome getBiome(BlockPos pos, Biome defaultBiome) {
        Biome biome = resolve(pos.getX(), pos.getZ());
        return biome == null ? defaultBiome : biome;
    }

    @Override
    public float getTemperatureAtHeight(float temperature, int height) {
        return temperature;
    }

    @Override
    public Biome[] getBiomesForGeneration(@Nullable Biome[] oldBiomeList,
                                          int x, int z, int width, int height) {
        IntCache.resetIntCache();
        Biome[] result = oldBiomeList == null || oldBiomeList.length < width * height
                ? new Biome[width * height] : oldBiomeList;
        for (int index = 0; index < width * height; index++) {
            int localX = index % width;
            int localZ = index / width;
            int blockX = (x + localX) << 2;
            int blockZ = (z + localZ) << 2;
            FFDModernBiomeResolver.Sample sample = resolver.sample(blockX, blockZ);
            Biome candidate = fixedCandidate(sample, blockX, blockZ);
            result[index] = resolver.resolveLegacy(sample, candidate);
        }
        return result;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z,
                             int width, int length) {
        return getBiomes(oldBiomeList, x, z, width, length, true);
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z,
                             int width, int length, boolean cacheFlag) {
        IntCache.resetIntCache();
        Biome[] result = oldBiomeList == null || oldBiomeList.length < width * length
                ? new Biome[width * length] : oldBiomeList;
        if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0) {
            long key = chunkKey(x >> 4, z >> 4);
            synchronized (chunkCache) {
                Biome[] cached = chunkCache.get(key);
                if (cached != null) {
                    System.arraycopy(cached, 0, result, 0, cached.length);
                    return result;
                }
            }
        }

        for (int index = 0; index < width * length; index++) {
            int localX = index % width;
            int localZ = index / width;
            int blockX = x + localX;
            int blockZ = z + localZ;
            FFDModernBiomeResolver.Sample sample = resolver.sampleFuzzy(blockX, blockZ);
            Biome candidate = fixedCandidate(sample, blockX, blockZ);
            result[index] = resolver.resolveLegacy(sample, candidate);
        }
        if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0) {
            Biome[] copy = new Biome[result.length];
            System.arraycopy(result, 0, copy, 0, result.length);
            synchronized (chunkCache) {
                chunkCache.put(chunkKey(x >> 4, z >> 4), copy);
            }
        }
        return result;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        int minX = x - radius >> 2;
        int minZ = z - radius >> 2;
        int maxX = x + radius >> 2;
        int maxZ = z + radius >> 2;
        for (int sampleZ = minZ; sampleZ <= maxZ; sampleZ++) {
            for (int sampleX = minX; sampleX <= maxX; sampleX++) {
                Biome biome = resolve(sampleX << 2, sampleZ << 2);
                if (!allowed.contains(biome)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    @Override
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> allowed, Random random) {
        if (allowed == candidateSelector.spawnBiomes()) {
            return findSpawnBiomePosition(x, z, range);
        }
        int minX = x - range >> 2;
        int minZ = z - range >> 2;
        int maxX = x + range >> 2;
        int maxZ = z + range >> 2;
        BlockPos found = null;
        int count = 0;
        for (int sampleZ = minZ; sampleZ <= maxZ; sampleZ++) {
            for (int sampleX = minX; sampleX <= maxX; sampleX++) {
                int blockX = sampleX << 2;
                int blockZ = sampleZ << 2;
                if (allowed.contains(resolve(blockX, blockZ))
                        && (found == null || random.nextInt(count + 1) == 0)) {
                    found = new BlockPos(blockX, 0, blockZ);
                    count++;
                }
            }
        }
        return found;
    }

    @Nullable
    private BlockPos findSpawnBiomePosition(int centerX, int centerZ, int range) {
        final int step = 16;
        for (int radius = 0; radius <= range; radius += step) {
            if (radius == 0) {
                BlockPos center = spawnPosition(centerX, centerZ);
                if (center != null) {
                    return center;
                }
                continue;
            }
            for (int offset = -radius; offset <= radius; offset += step) {
                BlockPos north = spawnPosition(centerX + offset, centerZ - radius);
                if (north != null) {
                    return north;
                }
                BlockPos south = spawnPosition(centerX + offset, centerZ + radius);
                if (south != null) {
                    return south;
                }
            }
            for (int offset = -radius + step; offset < radius; offset += step) {
                BlockPos west = spawnPosition(centerX - radius, centerZ + offset);
                if (west != null) {
                    return west;
                }
                BlockPos east = spawnPosition(centerX + radius, centerZ + offset);
                if (east != null) {
                    return east;
                }
            }
        }
        return null;
    }

    @Nullable
    private BlockPos spawnPosition(int blockX, int blockZ) {
        FFDModernBiomeResolver.Sample sample = resolver.sampleFuzzy(blockX, blockZ);
        FFDModernBiomeResolver.TerrainRole role = sample.role;
        if (role == FFDModernBiomeResolver.TerrainRole.DEEP_OCEAN
                || role == FFDModernBiomeResolver.TerrainRole.OCEAN
                || role == FFDModernBiomeResolver.TerrainRole.RIVER) {
            return null;
        }
        Biome biome = resolver.resolveLegacy(sample, fixedCandidate(sample, blockX, blockZ));
        return candidateSelector.spawnBiomes().contains(biome)
                ? new BlockPos(blockX, 0, blockZ) : null;
    }

    @Override
    public void cleanupCache() {
        legacyProvider.cleanupCache();
        candidateSelector.clearCache();
        synchronized (chunkCache) {
            chunkCache.clear();
        }
    }

    @Override
    public boolean isFixedBiome() {
        return legacyProvider.isFixedBiome();
    }

    @Override
    public Biome getFixedBiome() {
        return legacyProvider.getFixedBiome();
    }

    private Biome resolve(int x, int z) {
        FFDModernBiomeResolver.Sample sample = resolver.sampleFuzzy(x, z);
        return resolver.resolveLegacy(sample, fixedCandidate(sample, x, z));
    }

    private Biome fixedCandidate(FFDModernBiomeResolver.Sample sample, int x, int z) {
        if (legacyProvider.isFixedBiome()) {
            Biome fixed = legacyProvider.getFixedBiome();
            if (fixed != null && FFDModernBiomeResolver.isCompatible(sample, fixed)) {
                return fixed;
            }
        }
        return candidateSelector.select(sample, sample.sampleX, sample.sampleZ);
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return (chunkX & 0xFFFFFFFFL) | (long) chunkZ << 32;
    }
}
