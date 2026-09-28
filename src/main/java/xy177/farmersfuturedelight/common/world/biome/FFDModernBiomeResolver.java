package xy177.farmersfuturedelight.common.world.biome;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import xy177.farmersfuturedelight.common.biome.BiomeModernOcean;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;
import xy177.farmersfuturedelight.common.world.terrain.FFDNoiseRouter;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FFDModernBiomeResolver {
    private static final int SAMPLE_STEP = FFDModernWorldgenData.CELL_WIDTH;
    private static final int MAX_CACHE_ENTRIES = 32768;
    private static final int[] COAST_CHECK_OFFSETS = {
            -32, 0, 32, 0, 0, -32, 0, 32,
            -24, -24, -24, 24, 24, -24, 24, 24
    };

    private final FFDModernWorldgenData data;
    private final FFDNoiseRouter router;
    private final long zoomSeed;
    private final Map<Long, Sample> cache = new LinkedHashMap<Long, Sample>(MAX_CACHE_ENTRIES, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Sample> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    };

    public FFDModernBiomeResolver(FFDModernWorldgenData data) {
        this(data, 0L);
    }

    public FFDModernBiomeResolver(FFDModernWorldgenData data, long worldSeed) {
        this.data = data;
        router = data.router();
        zoomSeed = FFDVerticalBiomeZoomer.obfuscateSeed(worldSeed);
    }

    public Sample sample(int blockX, int blockZ) {
        int x = Math.floorDiv(blockX, SAMPLE_STEP) * SAMPLE_STEP;
        int z = Math.floorDiv(blockZ, SAMPLE_STEP) * SAMPLE_STEP;
        long key = key(x, z);
        synchronized (cache) {
            Sample cached = cache.get(key);
            if (cached != null) {
                return cached;
            }
        }

        long temperatureValue = FFDModernSurfaceBiomeMap.quantize(
                router.temperature.sample(x, 0, z));
        long humidityValue = FFDModernSurfaceBiomeMap.quantize(
                router.vegetation.sample(x, 0, z));
        long continentalnessValue = FFDModernSurfaceBiomeMap.quantize(
                router.continents.sample(x, 0, z));
        long erosionValue = FFDModernSurfaceBiomeMap.quantize(
                router.erosion.sample(x, 0, z));
        long weirdnessValue = FFDModernSurfaceBiomeMap.quantizeWeirdness(
                router.ridges.sample(x, 0, z));
        double temperature = FFDModernSurfaceBiomeMap.unquantize(temperatureValue);
        double humidity = FFDModernSurfaceBiomeMap.unquantize(humidityValue);
        double continentalness = FFDModernSurfaceBiomeMap.unquantize(continentalnessValue);
        double erosion = FFDModernSurfaceBiomeMap.unquantize(erosionValue);
        double weirdness = FFDModernSurfaceBiomeMap.unquantize(weirdnessValue);
        ModernBiome biome = FFDModernSurfaceBiomeMap.select(
                temperatureValue, humidityValue, continentalnessValue, erosionValue, weirdnessValue);
        if (biome.isCoast() && !isValidCoast(x, z, biome)) {
            long inlandContinentalness = Math.max(continentalnessValue,
                    FFDModernSurfaceBiomeMap.quantize(0.03D));
            biome = FFDModernSurfaceBiomeMap.select(temperatureValue, humidityValue,
                    inlandContinentalness, erosionValue, weirdnessValue);
        }
        TerrainRole role = roleFor(biome);
        Sample created = new Sample(temperature, humidity, continentalness, erosion, weirdness,
                biome, role, x, z);
        synchronized (cache) {
            Sample cached = cache.get(key);
            if (cached != null) {
                return cached;
            }
            cache.put(key, created);
        }
        return created;
    }

    public Sample sampleFuzzy(int blockX, int blockZ) {
        long quart = FFDVerticalBiomeZoomer.nearestQuartXZ(
                zoomSeed, blockX, FFDModernWorldgenData.SEA_LEVEL, blockZ);
        int quartX = (int) (quart >> 32);
        int quartZ = (int) quart;
        return sample(quartX * SAMPLE_STEP, quartZ * SAMPLE_STEP);
    }

    public Biome resolveLegacy(Sample sample, Biome candidate) {
        Biome modern = FFDBiomes.forModernBiome(sample.biome);
        if (modern instanceof BiomeModernOcean) {
            if (candidate != null && !(candidate instanceof BiomeModernOcean)
                    && candidate != Biomes.OCEAN && candidate != Biomes.DEEP_OCEAN
                    && candidate != Biomes.FROZEN_OCEAN && isCompatible(sample, candidate)) {
                return candidate;
            }
            return modern;
        }
        if (modern != null) {
            return isIceAndFireGlacier(candidate) && isCompatible(sample, candidate)
                    ? candidate : modern;
        }
        if (candidate != null && isCompatible(sample, candidate)) {
            return resolveTaigaVariant(sample, candidate);
        }
        return sample.fallbackBiome();
    }

    private static Biome resolveTaigaVariant(Sample sample, Biome candidate) {
        boolean giant = candidate == Biomes.REDWOOD_TAIGA
                || candidate == Biomes.REDWOOD_TAIGA_HILLS
                || candidate == Biomes.MUTATED_REDWOOD_TAIGA
                || candidate == Biomes.MUTATED_REDWOOD_TAIGA_HILLS;
        boolean ordinary = candidate == Biomes.TAIGA || candidate == Biomes.TAIGA_HILLS
                || candidate == Biomes.MUTATED_TAIGA || candidate == Biomes.COLD_TAIGA
                || candidate == Biomes.COLD_TAIGA_HILLS || candidate == Biomes.MUTATED_TAIGA_COLD;
        boolean oldGrowth = sample.biome == ModernBiome.OLD_GROWTH_PINE_TAIGA
                || sample.biome == ModernBiome.OLD_GROWTH_SPRUCE_TAIGA;
        if (!giant && !ordinary && (!oldGrowth || candidate.getRegistryName() == null
                || !"minecraft".equals(candidate.getRegistryName().getResourceDomain()))) {
            return candidate;
        }
        boolean hills = candidate == Biomes.TAIGA_HILLS || candidate == Biomes.COLD_TAIGA_HILLS
                || candidate == Biomes.REDWOOD_TAIGA_HILLS
                || candidate == Biomes.MUTATED_REDWOOD_TAIGA_HILLS;
        switch (sample.biome) {
            case OLD_GROWTH_PINE_TAIGA:
                return hills ? Biomes.REDWOOD_TAIGA_HILLS : Biomes.REDWOOD_TAIGA;
            case OLD_GROWTH_SPRUCE_TAIGA:
                return hills ? Biomes.MUTATED_REDWOOD_TAIGA_HILLS : Biomes.MUTATED_REDWOOD_TAIGA;
            case TAIGA:
                return giant ? hills ? Biomes.TAIGA_HILLS : Biomes.TAIGA : candidate;
            case SNOWY_TAIGA:
                return giant ? hills ? Biomes.COLD_TAIGA_HILLS : Biomes.COLD_TAIGA : candidate;
            default:
                return candidate;
        }
    }

    private static boolean isIceAndFireGlacier(Biome biome) {
        if (biome == null || biome.getRegistryName() == null) {
            return false;
        }
        return "iceandfire".equals(biome.getRegistryName().getResourceDomain())
                && "glacier".equalsIgnoreCase(biome.getRegistryName().getResourcePath());
    }

    public boolean matches(Sample sample, String target) {
        String normalized = target.indexOf(':') >= 0 ? target : "minecraft:" + target;
        if (normalized.equals(sample.biome.id)) {
            return true;
        }
        if ("minecraft:ocean".equals(normalized)) {
            return sample.role == TerrainRole.OCEAN || sample.role == TerrainRole.DEEP_OCEAN;
        }
        if ("minecraft:river".equals(normalized)) {
            return sample.role == TerrainRole.RIVER && !sample.biome.id.endsWith("frozen_river");
        }
        if ("minecraft:frozen_river".equals(normalized)) {
            return sample.biome == ModernBiome.FROZEN_RIVER;
        }
        if ("minecraft:beach".equals(normalized)) {
            return sample.biome == ModernBiome.BEACH;
        }
        if ("minecraft:swamp".equals(normalized) || "minecraft:mangrove_swamp".equals(normalized)) {
            return sample.role == TerrainRole.SWAMP;
        }
        return false;
    }

    public static TerrainRole roleOf(Biome biome) {
        if (biome == null) {
            return TerrainRole.LAND;
        }
        if (biome == Biomes.MUSHROOM_ISLAND || biome == Biomes.MUSHROOM_ISLAND_SHORE) {
            return TerrainRole.MUSHROOM;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.MUSHROOM)) {
            return TerrainRole.MUSHROOM;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)) {
            return TerrainRole.OCEAN;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER)) {
            return TerrainRole.RIVER;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH)) {
            return TerrainRole.COAST;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) {
            return TerrainRole.SWAMP;
        }
        return TerrainRole.LAND;
    }

    static boolean isCompatible(Sample sample, Biome candidate) {
        TerrainRole candidateRole = roleOf(candidate);
        if (sample.role == TerrainRole.MUSHROOM) {
            if (candidateRole != TerrainRole.MUSHROOM) {
                return false;
            }
        } else if (sample.role == TerrainRole.DEEP_OCEAN || sample.role == TerrainRole.OCEAN) {
            if (candidateRole != TerrainRole.OCEAN) {
                return false;
            }
        } else if (sample.role == TerrainRole.RIVER) {
            if (candidateRole != TerrainRole.RIVER) {
                return false;
            }
        } else if (sample.role == TerrainRole.COAST) {
            boolean sandy = BiomeDictionary.hasType(candidate, BiomeDictionary.Type.SANDY);
            if (candidateRole != TerrainRole.COAST
                    && !(sample.biome == ModernBiome.DESERT && sandy)) {
                return false;
            }
        } else if (sample.role == TerrainRole.SWAMP) {
            if (candidateRole != TerrainRole.SWAMP) {
                return false;
            }
        } else if (candidateRole != TerrainRole.LAND) {
            return false;
        }

        boolean badlands = sample.biome == ModernBiome.BADLANDS
                || sample.biome == ModernBiome.WOODED_BADLANDS
                || sample.biome == ModernBiome.ERODED_BADLANDS;
        if (badlands != BiomeDictionary.hasType(candidate, BiomeDictionary.Type.MESA)) {
            return false;
        }

        if ((sample.biome == ModernBiome.FROZEN_OCEAN
                || sample.biome == ModernBiome.DEEP_FROZEN_OCEAN)
                && !BiomeDictionary.hasType(candidate, BiomeDictionary.Type.COLD)
                && !BiomeDictionary.hasType(candidate, BiomeDictionary.Type.SNOWY)) {
            return false;
        }

        int candidateTemperature = candidateTemperatureBand(candidate);
        int modernTemperature = temperatureBand(sample.temperature);
        return Math.abs(candidateTemperature - modernTemperature) <= 1;
    }

    private boolean isValidCoast(int x, int z, ModernBiome biome) {
        int maximumSurface = FFDModernWorldgenData.SEA_LEVEL
                + (biome == ModernBiome.STONY_SHORE ? 20 : 8);
        if (data.preliminarySurfaceLevel(x, z) > maximumSurface) {
            return false;
        }
        for (int index = 0; index < COAST_CHECK_OFFSETS.length; index += 2) {
            if (router.continents.sample(x + COAST_CHECK_OFFSETS[index], 0,
                    z + COAST_CHECK_OFFSETS[index + 1]) <= -0.19D) {
                return true;
            }
        }
        return false;
    }

    private static TerrainRole roleFor(ModernBiome biome) {
        if (biome == ModernBiome.MUSHROOM_FIELDS) {
            return TerrainRole.MUSHROOM;
        }
        if (biome.isDeepOcean()) {
            return TerrainRole.DEEP_OCEAN;
        }
        if (biome.isOcean()) {
            return TerrainRole.OCEAN;
        }
        if (biome.isRiver()) {
            return TerrainRole.RIVER;
        }
        if (biome.isSwamp()) {
            return TerrainRole.SWAMP;
        }
        if (biome.isCoast()) {
            return TerrainRole.COAST;
        }
        return TerrainRole.LAND;
    }

    static int temperatureBand(double value) {
        return index(value, new double[] {-0.45D, -0.15D, 0.2D, 0.55D});
    }

    static int candidateTemperatureBand(Biome biome) {
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY)) {
            return 0;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)) {
            return 1;
        }
        boolean hot = BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT);
        boolean dry = BiomeDictionary.hasType(biome, BiomeDictionary.Type.DRY)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.SANDY);
        if (hot && dry) {
            return 4;
        }
        if (hot) {
            return 3;
        }
        float temperature = biome.getDefaultTemperature();
        if (temperature < 0.15F) {
            return 0;
        }
        if (temperature < 0.4F) {
            return 1;
        }
        if (temperature < 1.0F) {
            return 2;
        }
        return temperature < 1.5F ? 3 : 4;
    }

    private static int index(double value, double[] thresholds) {
        for (int i = 0; i < thresholds.length; i++) {
            if (value < thresholds[i]) {
                return i;
            }
        }
        return thresholds.length;
    }

    private static long key(int x, int z) {
        return (x & 0xFFFFFFFFL) | (long) z << 32;
    }

    public enum TerrainRole {
        MUSHROOM, DEEP_OCEAN, OCEAN, RIVER, COAST, SWAMP, LAND
    }

    public enum ModernBiome {
        MUSHROOM_FIELDS("minecraft:mushroom_fields"),
        DEEP_FROZEN_OCEAN("minecraft:deep_frozen_ocean"),
        DEEP_COLD_OCEAN("minecraft:deep_cold_ocean"),
        DEEP_OCEAN("minecraft:deep_ocean"),
        DEEP_LUKEWARM_OCEAN("minecraft:deep_lukewarm_ocean"),
        WARM_OCEAN("minecraft:warm_ocean"),
        FROZEN_OCEAN("minecraft:frozen_ocean"),
        COLD_OCEAN("minecraft:cold_ocean"),
        OCEAN("minecraft:ocean"),
        LUKEWARM_OCEAN("minecraft:lukewarm_ocean"),
        SNOWY_PLAINS("minecraft:snowy_plains"),
        SNOWY_TAIGA("minecraft:snowy_taiga"),
        TAIGA("minecraft:taiga"),
        PLAINS("minecraft:plains"),
        FOREST("minecraft:forest"),
        OLD_GROWTH_SPRUCE_TAIGA("minecraft:old_growth_spruce_taiga"),
        FLOWER_FOREST("minecraft:flower_forest"),
        BIRCH_FOREST("minecraft:birch_forest"),
        DARK_FOREST("minecraft:dark_forest"),
        SAVANNA("minecraft:savanna"),
        JUNGLE("minecraft:jungle"),
        DESERT("minecraft:desert"),
        ICE_SPIKES("minecraft:ice_spikes"),
        DAPPLED_FOREST("minecraft:dappled_forest"),
        OLD_GROWTH_PINE_TAIGA("minecraft:old_growth_pine_taiga"),
        SUNFLOWER_PLAINS("minecraft:sunflower_plains"),
        OLD_GROWTH_BIRCH_FOREST("minecraft:old_growth_birch_forest"),
        SPARSE_JUNGLE("minecraft:sparse_jungle"),
        BAMBOO_JUNGLE("minecraft:bamboo_jungle"),
        MEADOW("minecraft:meadow"),
        PALE_GARDEN("minecraft:pale_garden"),
        SAVANNA_PLATEAU("minecraft:savanna_plateau"),
        BADLANDS("minecraft:badlands"),
        WOODED_BADLANDS("minecraft:wooded_badlands"),
        CHERRY_GROVE("minecraft:cherry_grove"),
        ERODED_BADLANDS("minecraft:eroded_badlands"),
        WINDSWEPT_GRAVELLY_HILLS("minecraft:windswept_gravelly_hills"),
        WINDSWEPT_HILLS("minecraft:windswept_hills"),
        WINDSWEPT_FOREST("minecraft:windswept_forest"),
        STONY_SHORE("minecraft:stony_shore"),
        SWAMP("minecraft:swamp"),
        MANGROVE_SWAMP("minecraft:mangrove_swamp"),
        BEACH("minecraft:beach"),
        SNOWY_BEACH("minecraft:snowy_beach"),
        WINDSWEPT_SAVANNA("minecraft:windswept_savanna"),
        FROZEN_RIVER("minecraft:frozen_river"),
        RIVER("minecraft:river"),
        JAGGED_PEAKS("minecraft:jagged_peaks"),
        FROZEN_PEAKS("minecraft:frozen_peaks"),
        STONY_PEAKS("minecraft:stony_peaks"),
        GROVE("minecraft:grove"),
        SNOWY_SLOPES("minecraft:snowy_slopes");

        private final String id;

        ModernBiome(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public boolean isOcean() {
            return name().contains("OCEAN") && !isDeepOcean();
        }

        public boolean isDeepOcean() {
            return name().startsWith("DEEP_");
        }

        public boolean isRiver() {
            return this == RIVER || this == FROZEN_RIVER;
        }

        public boolean isSwamp() {
            return this == SWAMP || this == MANGROVE_SWAMP;
        }

        public boolean isCoast() {
            return this == BEACH || this == SNOWY_BEACH || this == STONY_SHORE;
        }
    }

    public final class Sample {
        public final double temperature;
        public final double humidity;
        public final double continentalness;
        public final double erosion;
        public final double weirdness;
        public final ModernBiome biome;
        public final TerrainRole role;
        public final int sampleX;
        public final int sampleZ;

        private Sample(double temperature, double humidity, double continentalness,
                       double erosion, double weirdness, ModernBiome biome, TerrainRole role,
                       int sampleX, int sampleZ) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.continentalness = continentalness;
            this.erosion = erosion;
            this.weirdness = weirdness;
            this.biome = biome;
            this.role = role;
            this.sampleX = sampleX;
            this.sampleZ = sampleZ;
        }

        public String modernId() {
            return biome.id;
        }

        public Biome fallbackBiome() {
            switch (biome) {
                case MUSHROOM_FIELDS: return Biomes.MUSHROOM_ISLAND;
                case DEEP_FROZEN_OCEAN: return Biomes.FROZEN_OCEAN;
                case DEEP_COLD_OCEAN:
                case DEEP_OCEAN:
                case DEEP_LUKEWARM_OCEAN:
                case WARM_OCEAN: return Biomes.DEEP_OCEAN;
                case FROZEN_OCEAN:
                case COLD_OCEAN: return Biomes.FROZEN_OCEAN;
                case OCEAN:
                case LUKEWARM_OCEAN: return Biomes.OCEAN;
                case SNOWY_PLAINS:
                case ICE_SPIKES: return Biomes.ICE_PLAINS;
                case SNOWY_TAIGA: return Biomes.COLD_TAIGA;
                case TAIGA: return Biomes.TAIGA;
                case OLD_GROWTH_SPRUCE_TAIGA: return Biomes.MUTATED_REDWOOD_TAIGA;
                case OLD_GROWTH_PINE_TAIGA: return Biomes.REDWOOD_TAIGA;
                case FOREST:
                case FLOWER_FOREST:
                case DAPPLED_FOREST:
                case BIRCH_FOREST:
                case OLD_GROWTH_BIRCH_FOREST:
                case CHERRY_GROVE: return Biomes.FOREST;
                case DARK_FOREST:
                case PALE_GARDEN: return Biomes.ROOFED_FOREST;
                case SAVANNA:
                case WINDSWEPT_SAVANNA: return Biomes.SAVANNA;
                case SAVANNA_PLATEAU: return Biomes.SAVANNA_PLATEAU;
                case JUNGLE:
                case SPARSE_JUNGLE:
                case BAMBOO_JUNGLE: return Biomes.JUNGLE;
                case DESERT: return Biomes.DESERT;
                case BADLANDS:
                case ERODED_BADLANDS: return Biomes.MESA;
                case WOODED_BADLANDS: return Biomes.MESA_ROCK;
                case STONY_SHORE: return Biomes.STONE_BEACH;
                case SWAMP:
                case MANGROVE_SWAMP: return Biomes.SWAMPLAND;
                case BEACH: return Biomes.BEACH;
                case SNOWY_BEACH: return Biomes.COLD_BEACH;
                case FROZEN_RIVER: return Biomes.FROZEN_RIVER;
                case RIVER: return Biomes.RIVER;
                case JAGGED_PEAKS:
                case STONY_PEAKS:
                case WINDSWEPT_HILLS:
                case WINDSWEPT_FOREST: return Biomes.EXTREME_HILLS;
                case FROZEN_PEAKS:
                case GROVE:
                case SNOWY_SLOPES:
                case MEADOW: return Biomes.ICE_MOUNTAINS;
                case PLAINS:
                case SUNFLOWER_PLAINS:
                default: return Biomes.PLAINS;
            }
        }
    }
}
