package xy177.farmersfuturedelight.common.world.biome;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver.ModernBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver.Sample;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver.TerrainRole;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

final class FFDBiomeCandidateSelector {
    private static final Logger LOGGER = LogManager.getLogger("FFD Biome Candidates");
    private static final int MAX_CELL_CACHE = 32768;
    private static final int DEFAULT_MODDED_WEIGHT = 3;
    private static final int DEFAULT_SPECIAL_WEIGHT = 10;
    private static final int REGION_SIZE = 1024;
    private static final int REGION_CANDIDATES = 12;
    private static final double REGION_JITTER = 0.72D;
    private static final long REGION_SHAPE_SALT = 0x243F6A8885A308D3L;
    private static final long REGION_SELECTION_SALT = 0x13198A2E03707344L;
    private static final double LEGACY_REGION_SCALE = 640.0D;
    private static final double LEGACY_REGION_DETAIL_SCALE = 160.0D;
    private static final double LEGACY_REGION_DETAIL_STRENGTH = 0.25D;
    private static final double LEGACY_REGION_VARIATION = 0.22D;
    private static final double WEIGHT_INFLUENCE = 0.045D;
    private static final double FALLBACK_BONUS = 0.12D;
    private static final double VANILLA_MUTATION_CHANCE = 1.0D / 29.0D;
    private static final double VANILLA_HILLS_CHANCE = 1.0D / 3.0D;
    private static final int VANILLA_VARIANT_REGION_SIZE = 128;
    private static final long VANILLA_VARIANT_SALT = 0xA4093822299F31D0L;
    private static final long VANILLA_MUTATION_SALT = 0x082EFA98EC4E6C89L;

    private static final double[] TEMPERATURE_MIN = {
            -1.0D, -0.45D, -0.15D, 0.2D, 0.55D
    };
    private static final double[] TEMPERATURE_MAX = {
            -0.45D, -0.15D, 0.2D, 0.55D, 1.0D
    };
    private static final double[] HUMIDITY_MIN = {
            -1.0D, -0.35D, -0.1D, 0.1D, 0.3D
    };
    private static final double[] HUMIDITY_MAX = {
            -0.35D, -0.1D, 0.1D, 0.3D, 1.0D
    };

    private final long seed;
    private final boolean biomesOPlentyLoaded;
    private final List<Entry> entries;
    private final List<Biome> spawnBiomes;
    private final Entry iceAndFireGlacier;
    private final double iceAndFireGlacierShare;
    private final ThreadLocal<CandidateScratch> candidateScratch =
            new ThreadLocal<CandidateScratch>() {
                @Override
                protected CandidateScratch initialValue() {
                    return new CandidateScratch();
                }
            };
    private final Map<Long, Biome> cellCache =
            new LinkedHashMap<Long, Biome>(MAX_CELL_CACHE, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, Biome> eldest) {
                    return size() > MAX_CELL_CACHE;
                }
            };

    FFDBiomeCandidateSelector(long seed) {
        this.seed = seed;
        biomesOPlentyLoaded = Loader.isModLoaded("biomesoplenty");
        entries = loadEntries();
        spawnBiomes = buildSpawnBiomes(entries);
        iceAndFireGlacier = findIceAndFireGlacier(entries);
        iceAndFireGlacierShare = sourcePoolShare(
                iceAndFireGlacier == null ? null : iceAndFireGlacier.biome);
        LOGGER.info("Loaded {} unified climate-biome candidates for ffd_cac", entries.size());
    }

    Biome select(Sample sample, int blockX, int blockZ) {
        int cellX = Math.floorDiv(blockX, FFDModernWorldgenData.CELL_WIDTH)
                * FFDModernWorldgenData.CELL_WIDTH;
        int cellZ = Math.floorDiv(blockZ, FFDModernWorldgenData.CELL_WIDTH)
                * FFDModernWorldgenData.CELL_WIDTH;
        long key = cellKey(cellX, cellZ);
        synchronized (cellCache) {
            if (cellCache.containsKey(key)) {
                return cellCache.get(key);
            }
        }

        Biome selected = selectUncached(sample, cellX, cellZ);
        synchronized (cellCache) {
            cellCache.put(key, selected);
        }
        return selected;
    }

    void clearCache() {
        synchronized (cellCache) {
            cellCache.clear();
        }
    }

    List<Biome> spawnBiomes() {
        return spawnBiomes;
    }

    private Biome selectUncached(Sample sample, int blockX, int blockZ) {
        if (useIceAndFireGlacier(sample, blockX, blockZ)) {
            return iceAndFireGlacier.biome;
        }
        Biome exactMountain = FFDBiomes.forModernBiome(sample.biome);
        if (exactMountain != null) {
            return exactMountain;
        }

        Biome fallback = sample.fallbackBiome();
        Family targetFamily = familyOf(sample.biome);
        Biome selected;
        if (!biomesOPlentyLoaded) {
            selected = selectLegacy(sample, blockX, blockZ, fallback, targetFamily);
            return applyVanillaSubBiomeVariant(selected, blockX, blockZ);
        }
        CandidateScratch scratch = candidateScratch.get();
        Entry[] candidates = scratch.entries;
        double[] scores = scratch.scores;
        int candidateCount = 0;
        for (Entry entry : entries) {
            if (!FFDModernBiomeResolver.isCompatible(sample, entry.biome)) {
                continue;
            }
            double score = climateScore(entry, sample, targetFamily, fallback);
            int insertion = candidateCount;
            while (insertion > 0 && score < scores[insertion - 1]) {
                insertion--;
            }
            if (insertion >= REGION_CANDIDATES) {
                continue;
            }
            int copyLength = Math.min(candidateCount, REGION_CANDIDATES - 1) - insertion;
            if (copyLength > 0) {
                System.arraycopy(candidates, insertion, candidates, insertion + 1, copyLength);
                System.arraycopy(scores, insertion, scores, insertion + 1, copyLength);
            }
            candidates[insertion] = entry;
            scores[insertion] = score;
            if (candidateCount < REGION_CANDIDATES) {
                candidateCount++;
            }
        }
        if (candidateCount == 0) {
            return applyVanillaSubBiomeVariant(fallback, blockX, blockZ);
        }

        long region = regionSeed(blockX, blockZ, targetFamily,
                FFDModernBiomeResolver.temperatureBand(sample.temperature),
                climateBand(sample.humidity, HUMIDITY_MAX));
        double totalWeight = 0.0D;
        for (int i = 0; i < candidateCount; i++) {
            totalWeight += selectionWeight(candidates[i], scores[i], scores[0]);
        }
        double selectedWeight = unit01(region) * totalWeight;
        for (int i = 0; i < candidateCount; i++) {
            selectedWeight -= selectionWeight(candidates[i], scores[i], scores[0]);
            if (selectedWeight <= 0.0D) {
                selected = candidates[i].biome;
                return applyVanillaSubBiomeVariant(selected, blockX, blockZ);
            }
        }
        selected = candidates[candidateCount - 1].biome;
        return applyVanillaSubBiomeVariant(selected, blockX, blockZ);
    }

    private Biome selectLegacy(Sample sample, int blockX, int blockZ,
                               Biome fallback, Family targetFamily) {
        Entry best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entry entry : entries) {
            if (!FFDModernBiomeResolver.isCompatible(sample, entry.biome)) {
                continue;
            }
            double score = climateScore(entry, sample, targetFamily, fallback);
            score -= legacyRegionalVariation(entry.salt, blockX, blockZ)
                    * LEGACY_REGION_VARIATION;
            if (score < bestScore) {
                bestScore = score;
                best = entry;
            }
        }
        return best == null ? fallback : best.biome;
    }

    private static double climateScore(Entry entry, Sample sample, Family targetFamily,
                                       Biome fallback) {
        double score = entry.profile.fitness(sample, targetFamily);
        score -= Math.log1p(entry.weight) * WEIGHT_INFLUENCE;
        if (entry.biome == fallback) {
            score -= FALLBACK_BONUS;
        }
        return score;
    }

    private static double selectionWeight(Entry entry, double score, double bestScore) {
        return Math.max(1, entry.weight) / (1.0D + Math.max(0.0D, score - bestScore) * 8.0D);
    }

    private double legacyRegionalVariation(long salt, int blockX, int blockZ) {
        double broad = valueNoise(blockX, blockZ, LEGACY_REGION_SCALE, salt);
        double detail = valueNoise(blockX, blockZ, LEGACY_REGION_DETAIL_SCALE,
                salt ^ 0x9E3779B97F4A7C15L);
        return (broad + detail * LEGACY_REGION_DETAIL_STRENGTH)
                / (1.0D + LEGACY_REGION_DETAIL_STRENGTH);
    }

    private double valueNoise(int blockX, int blockZ, double scale, long salt) {
        double latticeX = blockX / scale;
        double latticeZ = blockZ / scale;
        int cellX = (int) Math.floor(latticeX);
        int cellZ = (int) Math.floor(latticeZ);
        double localX = fade(latticeX - cellX);
        double localZ = fade(latticeZ - cellZ);
        double x0 = unitSigned(mix(seed ^ salt, cellX, cellZ)) * 2.0D;
        double x1 = unitSigned(mix(seed ^ salt, cellX + 1, cellZ)) * 2.0D;
        double x2 = unitSigned(mix(seed ^ salt, cellX, cellZ + 1)) * 2.0D;
        double x3 = unitSigned(mix(seed ^ salt, cellX + 1, cellZ + 1)) * 2.0D;
        return lerp(lerp(x0, x1, localX), lerp(x2, x3, localX), localZ);
    }

    private long regionSeed(int blockX, int blockZ, Family family,
                            int temperature, int humidity) {
        long climate = REGION_SELECTION_SALT
                ^ (long) family.ordinal() * 0x9E3779B97F4A7C15L
                ^ (long) temperature * 0xC2B2AE3D27D4EB4FL
                ^ (long) humidity * 0x165667B19E3779F9L;
        return regionSeed(blockX, blockZ, REGION_SIZE, REGION_SHAPE_SALT, climate);
    }

    private long regionSeed(int blockX, int blockZ, int regionSize,
                            long shapeSalt, long selectionSalt) {
        int cellX = Math.floorDiv(blockX, regionSize);
        int cellZ = Math.floorDiv(blockZ, regionSize);
        int selectedX = cellX;
        int selectedZ = cellZ;
        double bestDistance = Double.MAX_VALUE;
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                int candidateX = cellX + offsetX;
                int candidateZ = cellZ + offsetZ;
                long shape = mix(seed ^ shapeSalt, candidateX, candidateZ);
                double centerX = (candidateX + 0.5D) * regionSize
                        + (unit01(shape) - 0.5D) * regionSize * REGION_JITTER;
                double centerZ = (candidateZ + 0.5D) * regionSize
                        + (unit01(mix(shape ^ shapeSalt, candidateZ, candidateX)) - 0.5D)
                        * regionSize * REGION_JITTER;
                double deltaX = blockX - centerX;
                double deltaZ = blockZ - centerZ;
                double distance = deltaX * deltaX + deltaZ * deltaZ;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    selectedX = candidateX;
                    selectedZ = candidateZ;
                }
            }
        }
        return mix(seed ^ selectionSalt, selectedX, selectedZ);
    }

    private static List<Entry> loadEntries() {
        Map<Biome, Integer> weights = new LinkedHashMap<>();
        for (BiomeManager.BiomeType type : BiomeManager.BiomeType.values()) {
            for (BiomeManager.BiomeEntry entry : BiomeManager.getBiomes(type)) {
                mergeWeight(weights, entry.biome, entry.itemWeight);
            }
        }
        for (Biome biome : BiomeManager.oceanBiomes) {
            mergeWeight(weights, biome, DEFAULT_SPECIAL_WEIGHT);
        }

        Map<Biome, Integer> bopWeights = loadBiomesOPlentyWeights();
        for (Map.Entry<Biome, Integer> entry : bopWeights.entrySet()) {
            mergeWeight(weights, entry.getKey(), entry.getValue());
        }
        loadConfiguredCandidates(weights);
        loadVanillaSubBiomeCandidates(weights);

        List<Entry> loaded = new ArrayList<>();
        for (Map.Entry<Biome, Integer> entry : weights.entrySet()) {
            if (isCandidate(entry.getKey()) && entry.getValue() > 0) {
                loaded.add(new Entry(entry.getKey(), entry.getValue()));
            }
        }
        Collections.sort(loaded, Comparator.comparing(Entry::registryName));
        return Collections.unmodifiableList(loaded);
    }

    private static void loadVanillaSubBiomeCandidates(Map<Biome, Integer> weights) {
        Biome[] hills = {
                Biomes.DESERT_HILLS,
                Biomes.FOREST_HILLS,
                Biomes.TAIGA_HILLS,
                Biomes.EXTREME_HILLS_EDGE,
                Biomes.JUNGLE_HILLS,
                Biomes.BIRCH_FOREST_HILLS,
                Biomes.COLD_TAIGA_HILLS,
                Biomes.REDWOOD_TAIGA_HILLS,
                Biomes.EXTREME_HILLS_WITH_TREES,
                Biomes.SAVANNA_PLATEAU,
                Biomes.ICE_MOUNTAINS
        };
        Biome[] mutations = {
                Biomes.MUTATED_PLAINS,
                Biomes.MUTATED_DESERT,
                Biomes.MUTATED_EXTREME_HILLS,
                Biomes.MUTATED_FOREST,
                Biomes.MUTATED_TAIGA,
                Biomes.MUTATED_SWAMPLAND,
                Biomes.MUTATED_ICE_FLATS,
                Biomes.MUTATED_JUNGLE,
                Biomes.MUTATED_JUNGLE_EDGE,
                Biomes.MUTATED_BIRCH_FOREST,
                Biomes.MUTATED_BIRCH_FOREST_HILLS,
                Biomes.MUTATED_ROOFED_FOREST,
                Biomes.MUTATED_TAIGA_COLD,
                Biomes.MUTATED_REDWOOD_TAIGA,
                Biomes.MUTATED_REDWOOD_TAIGA_HILLS,
                Biomes.MUTATED_EXTREME_HILLS_WITH_TREES,
                Biomes.MUTATED_SAVANNA,
                Biomes.MUTATED_SAVANNA_ROCK,
                Biomes.MUTATED_MESA,
                Biomes.MUTATED_MESA_ROCK,
                Biomes.MUTATED_MESA_CLEAR_ROCK
        };
        for (Biome biome : hills) {
            mergeWeight(weights, biome, DEFAULT_MODDED_WEIGHT);
        }
        for (Biome biome : mutations) {
            mergeWeight(weights, biome, 1);
        }
    }

    private Biome applyVanillaSubBiomeVariant(Biome selected, int blockX, int blockZ) {
        if (selected == null || isVanillaSubBiome(selected)) {
            return selected;
        }
        long variantSeed = regionSeed(blockX, blockZ, VANILLA_VARIANT_REGION_SIZE,
                VANILLA_VARIANT_SALT, VANILLA_VARIANT_SALT);
        Biome mutation = Biome.getMutationForBiome(selected);
        if (mutation != null
                && unit01(mix(variantSeed ^ VANILLA_MUTATION_SALT, 0, 0))
                < VANILLA_MUTATION_CHANCE) {
            return mutation;
        }
        Biome hills = vanillaHillsBiome(selected);
        if (hills != null) {
            double chance = selected == Biomes.PLAINS
                    ? VANILLA_HILLS_CHANCE / 3.0D : VANILLA_HILLS_CHANCE;
            if (unit01(variantSeed) < chance) {
                return hills;
            }
        }
        return selected;
    }

    private static Biome vanillaHillsBiome(Biome biome) {
        if (biome == Biomes.PLAINS) {
            return Biomes.FOREST_HILLS;
        }
        if (biome == Biomes.DESERT) {
            return Biomes.DESERT_HILLS;
        }
        if (biome == Biomes.FOREST) {
            return Biomes.FOREST_HILLS;
        }
        if (biome == Biomes.BIRCH_FOREST) {
            return Biomes.BIRCH_FOREST_HILLS;
        }
        if (biome == Biomes.TAIGA) {
            return Biomes.TAIGA_HILLS;
        }
        if (biome == Biomes.REDWOOD_TAIGA) {
            return Biomes.REDWOOD_TAIGA_HILLS;
        }
        if (biome == Biomes.COLD_TAIGA) {
            return Biomes.COLD_TAIGA_HILLS;
        }
        if (biome == Biomes.JUNGLE) {
            return Biomes.JUNGLE_HILLS;
        }
        if (biome == Biomes.ICE_PLAINS) {
            return Biomes.ICE_MOUNTAINS;
        }
        if (biome == Biomes.EXTREME_HILLS) {
            return Biomes.EXTREME_HILLS_WITH_TREES;
        }
        if (biome == Biomes.SAVANNA) {
            return Biomes.SAVANNA_PLATEAU;
        }
        return null;
    }

    private static boolean isVanillaSubBiome(Biome biome) {
        return biome == Biomes.DESERT_HILLS
                || biome == Biomes.FOREST_HILLS
                || biome == Biomes.TAIGA_HILLS
                || biome == Biomes.EXTREME_HILLS_EDGE
                || biome == Biomes.JUNGLE_HILLS
                || biome == Biomes.BIRCH_FOREST_HILLS
                || biome == Biomes.COLD_TAIGA_HILLS
                || biome == Biomes.REDWOOD_TAIGA_HILLS
                || biome == Biomes.EXTREME_HILLS_WITH_TREES
                || biome == Biomes.SAVANNA_PLATEAU
                || biome == Biomes.ICE_MOUNTAINS
                || biome == Biomes.MUTATED_PLAINS
                || biome == Biomes.MUTATED_DESERT
                || biome == Biomes.MUTATED_EXTREME_HILLS
                || biome == Biomes.MUTATED_FOREST
                || biome == Biomes.MUTATED_TAIGA
                || biome == Biomes.MUTATED_SWAMPLAND
                || biome == Biomes.MUTATED_ICE_FLATS
                || biome == Biomes.MUTATED_JUNGLE
                || biome == Biomes.MUTATED_JUNGLE_EDGE
                || biome == Biomes.MUTATED_BIRCH_FOREST
                || biome == Biomes.MUTATED_BIRCH_FOREST_HILLS
                || biome == Biomes.MUTATED_ROOFED_FOREST
                || biome == Biomes.MUTATED_TAIGA_COLD
                || biome == Biomes.MUTATED_REDWOOD_TAIGA
                || biome == Biomes.MUTATED_REDWOOD_TAIGA_HILLS
                || biome == Biomes.MUTATED_EXTREME_HILLS_WITH_TREES
                || biome == Biomes.MUTATED_SAVANNA
                || biome == Biomes.MUTATED_SAVANNA_ROCK
                || biome == Biomes.MUTATED_MESA
                || biome == Biomes.MUTATED_MESA_ROCK
                || biome == Biomes.MUTATED_MESA_CLEAR_ROCK;
    }

    private static void loadConfiguredCandidates(Map<Biome, Integer> weights) {
        String[] configuredNames = FFDConfig.cavesAndCliffsAdditionalBiomeCandidates;
        if (configuredNames == null) {
            return;
        }
        for (String configuredName : configuredNames) {
            String trimmed = configuredName == null ? "" : configuredName.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            ResourceLocation name;
            try {
                name = new ResourceLocation(trimmed);
            } catch (RuntimeException exception) {
                logConfiguredCandidate(trimmed, "invalid registry name");
                continue;
            }
            Biome biome = ForgeRegistries.BIOMES.getValue(name);
            if (biome == null) {
                logConfiguredCandidate(trimmed, "biome is not registered");
                continue;
            }
            if (!isCandidate(biome)) {
                logConfiguredCandidate(trimmed, "biome is not eligible for the Overworld candidate pool");
                continue;
            }
            int weight = DEFAULT_MODDED_WEIGHT;
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.RARE)) {
                weight = Math.max(1, weight / 3);
            }
            mergeWeight(weights, biome, weight);
            logConfiguredCandidate(trimmed, "included with weight " + weight);
        }
    }

    private static void logConfiguredCandidate(String name, String decision) {
        if (FFDConfig.logAutoCompatibilityDecisions) {
            LOGGER.info("ffd_cac additional biome candidate {}: {}", name, decision);
        }
    }

    private static List<Biome> buildSpawnBiomes(List<Entry> entries) {
        List<Biome> biomes = new ArrayList<>();
        for (Entry entry : entries) {
            TerrainRole role = FFDModernBiomeResolver.roleOf(entry.biome);
            if (role != TerrainRole.OCEAN && role != TerrainRole.RIVER) {
                biomes.add(entry.biome);
            }
        }
        for (Biome biome : FFDBiomes.MOUNTAIN_BIOMES) {
            if (biome != null && !biomes.contains(biome)) {
                biomes.add(biome);
            }
        }
        return Collections.unmodifiableList(biomes);
    }

    private static void mergeWeight(Map<Biome, Integer> weights, Biome biome, int weight) {
        if (!isCandidate(biome) || weight <= 0) {
            return;
        }
        Integer old = weights.get(biome);
        if (old == null || weight > old) {
            weights.put(biome, weight);
        }
    }

    private static boolean isCandidate(Biome biome) {
        if (biome == null || biome.getRegistryName() == null || isOtherDimensionBiome(biome)) {
            return false;
        }
        ResourceLocation name = biome.getRegistryName();
        if (!FarmerFutureDelight.MODID.equals(name.getResourceDomain())) {
            return true;
        }
        for (Biome mountain : FFDBiomes.MOUNTAIN_BIOMES) {
            if (biome == mountain) {
                return false;
            }
        }
        return false;
    }

    private boolean useIceAndFireGlacier(Sample sample, int blockX, int blockZ) {
        if (iceAndFireGlacier == null || iceAndFireGlacierShare <= 0.0D
                || !FFDModernBiomeResolver.isCompatible(sample, iceAndFireGlacier.biome)) {
            return false;
        }
        long region = regionSeed(blockX, blockZ, Family.GENERIC, 0, 0);
        return unit01(region ^ iceAndFireGlacier.salt) < iceAndFireGlacierShare;
    }

    private static Entry findIceAndFireGlacier(List<Entry> entries) {
        for (Entry entry : entries) {
            ResourceLocation name = entry.biome.getRegistryName();
            if (name != null && "iceandfire".equals(name.getResourceDomain())
                    && "glacier".equalsIgnoreCase(name.getResourcePath())) {
                return entry;
            }
        }
        return null;
    }

    private static double sourcePoolShare(Biome target) {
        if (target == null) {
            return 0.0D;
        }
        double share = 0.0D;
        for (BiomeManager.BiomeType type : BiomeManager.BiomeType.values()) {
            int totalWeight = 0;
            int targetWeight = 0;
            for (BiomeManager.BiomeEntry entry : BiomeManager.getBiomes(type)) {
                totalWeight += Math.max(0, entry.itemWeight);
                if (entry.biome == target) {
                    targetWeight += Math.max(0, entry.itemWeight);
                }
            }
            if (targetWeight > 0 && totalWeight > 0) {
                share = Math.max(share, targetWeight / (double) totalWeight);
            }
        }
        return share;
    }

    private static Map<Biome, Integer> loadBiomesOPlentyWeights() {
        if (!Loader.isModLoaded("biomesoplenty")) {
            return Collections.emptyMap();
        }
        try {
            Class<?> climatesClass = Class.forName("biomesoplenty.api.enums.BOPClimates");
            Method valuesMethod = climatesClass.getMethod("values");
            Field landBiomesField = climatesClass.getDeclaredField("landBiomes");
            landBiomesField.setAccessible(true);
            Map<Biome, Integer> weights = new LinkedHashMap<>();
            for (Object climate : (Object[]) valuesMethod.invoke(null)) {
                for (Object weightedEntry : (List<?>) landBiomesField.get(climate)) {
                    Field biomeField = weightedEntry.getClass().getField("biome");
                    Field weightField = weightedEntry.getClass().getField("weight");
                    Biome biome = (Biome) biomeField.get(weightedEntry);
                    int weight = weightField.getInt(weightedEntry);
                    if (!isCandidate(biome) || weight <= 0) {
                        continue;
                    }
                    weights.put(biome, weights.getOrDefault(biome, 0) + weight);
                }
            }
            LOGGER.info("Loaded {} Biomes O' Plenty climate weights for ffd_cac",
                    weights.size());
            return weights;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            LOGGER.warn("Could not read optional Biomes O' Plenty biome weights", exception);
            return Collections.emptyMap();
        }
    }

    private static boolean isOtherDimensionBiome(Biome biome) {
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.END)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.VOID);
    }

    private static Family familyOf(ModernBiome biome) {
        switch (biome) {
            case MUSHROOM_FIELDS:
                return Family.MUSHROOM;
            case DEEP_FROZEN_OCEAN:
            case DEEP_COLD_OCEAN:
            case DEEP_OCEAN:
            case DEEP_LUKEWARM_OCEAN:
            case WARM_OCEAN:
            case FROZEN_OCEAN:
            case COLD_OCEAN:
            case OCEAN:
            case LUKEWARM_OCEAN:
                return Family.OCEAN;
            case FROZEN_RIVER:
            case RIVER:
                return Family.RIVER;
            case BEACH:
            case SNOWY_BEACH:
            case STONY_SHORE:
                return Family.COAST;
            case SWAMP:
            case MANGROVE_SWAMP:
                return Family.SWAMP;
            case TAIGA:
            case SNOWY_TAIGA:
            case OLD_GROWTH_PINE_TAIGA:
            case OLD_GROWTH_SPRUCE_TAIGA:
                return Family.TAIGA;
            case FOREST:
            case FLOWER_FOREST:
            case BIRCH_FOREST:
            case OLD_GROWTH_BIRCH_FOREST:
            case DARK_FOREST:
            case DAPPLED_FOREST:
            case PALE_GARDEN:
            case CHERRY_GROVE:
                return Family.FOREST;
            case JUNGLE:
            case SPARSE_JUNGLE:
            case BAMBOO_JUNGLE:
                return Family.JUNGLE;
            case SAVANNA:
            case SAVANNA_PLATEAU:
            case WINDSWEPT_SAVANNA:
                return Family.SAVANNA;
            case DESERT:
                return Family.DESERT;
            case BADLANDS:
            case WOODED_BADLANDS:
            case ERODED_BADLANDS:
                return Family.BADLANDS;
            case WINDSWEPT_GRAVELLY_HILLS:
            case WINDSWEPT_HILLS:
            case WINDSWEPT_FOREST:
                return Family.HILLS;
            case JAGGED_PEAKS:
            case FROZEN_PEAKS:
            case STONY_PEAKS:
            case GROVE:
            case SNOWY_SLOPES:
            case MEADOW:
                return Family.MOUNTAIN;
            case SNOWY_PLAINS:
            case PLAINS:
            case ICE_SPIKES:
            case SUNFLOWER_PLAINS:
            default:
                return Family.PLAINS;
        }
    }

    private static Family familyOf(Biome biome) {
        TerrainRole role = FFDModernBiomeResolver.roleOf(biome);
        if (role == TerrainRole.MUSHROOM) {
            return Family.MUSHROOM;
        }
        if (role == TerrainRole.OCEAN) {
            return Family.OCEAN;
        }
        if (role == TerrainRole.RIVER) {
            return Family.RIVER;
        }
        if (role == TerrainRole.COAST) {
            return Family.COAST;
        }
        if (role == TerrainRole.SWAMP) {
            return Family.SWAMP;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.MESA)) {
            return Family.BADLANDS;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN)) {
            return Family.MOUNTAIN;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.HILLS)) {
            return Family.HILLS;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) {
            return Family.JUNGLE;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SAVANNA)) {
            return Family.SAVANNA;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS)) {
            return Family.TAIGA;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST)) {
            return Family.FOREST;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SANDY)) {
            return Family.DESERT;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.PLAINS)) {
            return Family.PLAINS;
        }
        return Family.GENERIC;
    }

    private static int humidityBand(Biome biome) {
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.LUSH)) {
            return 4;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.DRY)) {
            return 0;
        }
        float rainfall = biome.getRainfall();
        if (rainfall < 0.2F) {
            return 0;
        }
        if (rainfall < 0.4F) {
            return 1;
        }
        if (rainfall < 0.65F) {
            return 2;
        }
        if (rainfall < 0.85F) {
            return 3;
        }
        return 4;
    }

    private static double familyPenalty(Family candidate, Family target) {
        if (candidate == target) {
            return 0.0D;
        }
        if (candidate == Family.GENERIC || target == Family.GENERIC) {
            return 0.3D;
        }
        if (candidate.isWooded() && target.isWooded()) {
            return 0.18D;
        }
        if (candidate.isDry() && target.isDry()) {
            return 0.22D;
        }
        if (candidate.isRelief() && target.isRelief()) {
            return 0.15D;
        }
        if (candidate == Family.PLAINS && target == Family.FOREST
                || candidate == Family.FOREST && target == Family.PLAINS) {
            return 0.35D;
        }
        return 0.9D;
    }

    private static double rangeDistance(double value, double minimum, double maximum) {
        if (value < minimum) {
            return minimum - value;
        }
        return value > maximum ? value - maximum : 0.0D;
    }

    private static int climateBand(double value, double[] maximums) {
        for (int index = 0; index < maximums.length; index++) {
            if (value < maximums[index]) {
                return index;
            }
        }
        return maximums.length - 1;
    }

    private static double fade(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private static double lerp(double first, double second, double progress) {
        return first + (second - first) * progress;
    }

    private static double unitSigned(long value) {
        return (value >>> 11) * 0x1.0p-53 - 0.5D;
    }

    private static double unit01(long value) {
        return (value >>> 11) * 0x1.0p-53;
    }

    private static long mix(long seed, int x, int z) {
        long value = seed ^ x * 341873128712L ^ z * 132897987541L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        return value ^ value >>> 33;
    }

    private static long stableSalt(ResourceLocation name) {
        long value = 0xcbf29ce484222325L;
        String text = name.toString();
        for (int i = 0; i < text.length(); i++) {
            value ^= text.charAt(i);
            value *= 0x100000001b3L;
        }
        return value;
    }

    private static long cellKey(int x, int z) {
        return (x & 0xFFFFFFFFL) | (long) z << 32;
    }

    private enum Family {
        MUSHROOM,
        OCEAN,
        RIVER,
        COAST,
        SWAMP,
        PLAINS,
        FOREST,
        TAIGA,
        JUNGLE,
        SAVANNA,
        DESERT,
        BADLANDS,
        HILLS,
        MOUNTAIN,
        GENERIC;

        boolean isWooded() {
            return this == FOREST || this == TAIGA || this == JUNGLE;
        }

        boolean isDry() {
            return this == SAVANNA || this == DESERT || this == BADLANDS;
        }

        boolean isRelief() {
            return this == HILLS || this == MOUNTAIN;
        }
    }

    private static final class Profile {
        private final Family family;
        private final double temperatureMin;
        private final double temperatureMax;
        private final double humidityMin;
        private final double humidityMax;
        private final double erosionMin;
        private final double erosionMax;
        private final double weirdnessMin;
        private final double weirdnessMax;

        private Profile(Biome biome) {
            family = familyOf(biome);
            int temperature = FFDModernBiomeResolver.candidateTemperatureBand(biome);
            int humidity = humidityBand(biome);
            temperatureMin = TEMPERATURE_MIN[temperature];
            temperatureMax = TEMPERATURE_MAX[temperature];
            humidityMin = HUMIDITY_MIN[humidity];
            humidityMax = HUMIDITY_MAX[humidity];

            switch (family) {
                case MOUNTAIN:
                    erosionMin = -1.0D;
                    erosionMax = -0.2225D;
                    weirdnessMin = 0.4D;
                    weirdnessMax = 0.93333334D;
                    break;
                case HILLS:
                    erosionMin = -0.78D;
                    erosionMax = 0.05D;
                    weirdnessMin = 0.26666668D;
                    weirdnessMax = 0.93333334D;
                    break;
                case PLAINS:
                    erosionMin = 0.05D;
                    erosionMax = 1.0D;
                    weirdnessMin = 0.0D;
                    weirdnessMax = 0.56666666D;
                    break;
                case SWAMP:
                    erosionMin = 0.45D;
                    erosionMax = 1.0D;
                    weirdnessMin = 0.0D;
                    weirdnessMax = 0.26666668D;
                    break;
                case BADLANDS:
                    erosionMin = -0.375D;
                    erosionMax = 0.55D;
                    weirdnessMin = 0.05D;
                    weirdnessMax = 1.0D;
                    break;
                default:
                    erosionMin = -1.0D;
                    erosionMax = 1.0D;
                    weirdnessMin = 0.0D;
                    weirdnessMax = 1.0D;
                    break;
            }
        }

        private double fitness(Sample sample, Family targetFamily) {
            double temperature = rangeDistance(sample.temperature,
                    temperatureMin, temperatureMax);
            double humidity = rangeDistance(sample.humidity, humidityMin, humidityMax);
            double erosion = rangeDistance(sample.erosion, erosionMin, erosionMax);
            double weirdness = rangeDistance(Math.abs(sample.weirdness),
                    weirdnessMin, weirdnessMax);
            return temperature * temperature * 4.0D
                    + humidity * humidity * 1.75D
                    + erosion * erosion * 1.25D
                    + weirdness * weirdness * 0.75D
                    + familyPenalty(family, targetFamily);
        }
    }

    private static final class Entry {
        private final Biome biome;
        private final int weight;
        private final Profile profile;
        private final long salt;

        private Entry(Biome biome, int weight) {
            this.biome = biome;
            this.weight = weight;
            profile = new Profile(biome);
            salt = stableSalt(biome.getRegistryName());
        }

        private String registryName() {
            return biome.getRegistryName().toString();
        }
    }

    private static final class CandidateScratch {
        private final Entry[] entries = new Entry[REGION_CANDIDATES];
        private final double[] scores = new double[REGION_CANDIDATES];
    }
}
