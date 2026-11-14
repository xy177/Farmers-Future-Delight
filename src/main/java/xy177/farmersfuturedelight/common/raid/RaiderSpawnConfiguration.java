package xy177.farmersfuturedelight.common.raid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDEntities;

public final class RaiderSpawnConfiguration {
    private static final Logger LOGGER = LogManager.getLogger("FFD Raider Spawning");
    private static Path directory;
    private static Patrol patrol = new Patrol();
    private static Map<EnumCreatureType, List<Biome.SpawnListEntry>> outpost = Collections.emptyMap();

    private RaiderSpawnConfiguration() { }

    public static void configure(File configDirectory) {
        directory = configDirectory.toPath().resolve(FarmerFutureDelight.MODID).resolve("spawning");
        for (String name : Arrays.asList("patrols.json", "pillager_outpost.json")) {
            try {
                Files.createDirectories(directory);
                Path file = directory.resolve(name);
                if (!Files.exists(file)) {
                    try (InputStream input = resource(name)) {
                        Files.copy(input, file);
                    }
                }
            } catch (IOException | SecurityException exception) {
                LOGGER.error("Could not create spawning configuration {}", name, exception);
            }
        }
    }

    public static void load() {
        patrol = loadPatrol(read("patrols.json"));
        outpost = loadOutpost(read("pillager_outpost.json"));
        FFDPatrolSpawner.clearDelays();
    }

    public static Patrol patrol() {
        return patrol;
    }

    @Nullable
    public static List<Biome.SpawnListEntry> outpostSpawns(EnumCreatureType type) {
        return outpost.get(type);
    }

    private static JsonObject read(String name) {
        if (directory != null && Files.exists(directory.resolve(name))) {
            Path file = directory.resolve(name);
            try {
                if (Files.size(file) > 1048576L) throw new IllegalArgumentException("JSON exceeds 1 MiB");
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    return new JsonParser().parse(reader).getAsJsonObject();
                }
            } catch (IOException | RuntimeException exception) {
                LOGGER.error("Invalid spawning configuration {}; using defaults", file, exception);
            }
        }
        return defaults(name);
    }

    private static JsonObject defaults(String name) {
        try (Reader reader = new InputStreamReader(resource(name), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException("Missing default spawning configuration " + name, exception);
        }
    }

    private static InputStream resource(String name) throws IOException {
        InputStream input = RaiderSpawnConfiguration.class.getResourceAsStream(
                "/assets/farmers_future_delight/spawning/" + name);
        if (input == null) throw new IOException("Missing spawning resource " + name);
        return input;
    }

    private static Patrol loadPatrol(JsonObject json) {
        try {
            Patrol result = new Patrol();
            result.enabled = bool(json, "enabled", result.enabled);
            result.intervalMin = integer(json, "intervalMinTicks", result.intervalMin, 1, Integer.MAX_VALUE - 1);
            result.intervalMax = integer(json, "intervalMaxTicks", Math.max(result.intervalMin, result.intervalMax), result.intervalMin, Integer.MAX_VALUE - 1);
            result.chance = decimal(json, "spawnChance", result.chance, 0.0D, 1.0D);
            result.minimumWorldTime = json.has("minimumWorldTime")
                    ? json.get("minimumWorldTime").getAsBigDecimal().longValueExact() : result.minimumWorldTime;
            if (result.minimumWorldTime < 0L) throw new IllegalArgumentException("minimumWorldTime must be nonnegative");
            result.daytimeOnly = bool(json, "daytimeOnly", result.daytimeOnly);
            result.villageDistance = integer(json, "villageAvoidanceSections", result.villageDistance, -1, 6);
            result.offsetMin = integer(json, "playerOffsetMin", result.offsetMin, 0, 1024);
            result.offsetMax = integer(json, "playerOffsetMax", Math.max(result.offsetMin, result.offsetMax), result.offsetMin, 1024);
            result.memberSpread = integer(json, "memberSpread", result.memberSpread, 0, 64);
            result.loadedAreaRadius = integer(json, "loadedAreaRadius", result.loadedAreaRadius, 0, 64);
            result.maxBlockLight = integer(json, "maximumBlockLight", result.maxBlockLight, 0, 15);
            if (json.has("dimensions")) {
                result.dimensions.clear();
                for (JsonElement element : json.getAsJsonArray("dimensions")) {
                    result.dimensions.add(element.getAsBigDecimal().intValueExact());
                }
            }
            if (json.has("excludedBiomes")) {
                result.excludedBiomes.clear();
                for (JsonElement element : json.getAsJsonArray("excludedBiomes")) {
                    result.excludedBiomes.add(new ResourceLocation(element.getAsString()));
                }
            }
            JsonElement members = json.has("members") ? json.get("members") : defaults("patrols.json").get("members");
            int index = 0;
            for (JsonElement element : members.getAsJsonArray()) {
                try {
                    JsonObject entry = element.getAsJsonObject();
                    String name = entry.get("entity").getAsString();
                    EntityEntry entity = resolveEntity(name, true);
                    if (entity == null) {
                        index++;
                        continue;
                    }
                    int min = integer(entry, "minCount", 1, 0, 1024);
                    int max = integer(entry, "maxCount", min, min, 1024);
                    boolean difficulty = bool(entry, "addLocalDifficulty", false);
                    boolean leader = bool(entry, "leader", false);
                    NBTTagCompound nbt = entry.has("nbt") ? JsonToNBT.getTagFromJson(entry.get("nbt").getAsString())
                            : new NBTTagCompound();
                    result.members.add(new Member(entity, min, max, difficulty, leader, nbt));
                } catch (Exception exception) {
                    LOGGER.warn("Skipping invalid patrol member at index {}", index, exception);
                }
                index++;
            }
            return result;
        } catch (RuntimeException exception) {
            LOGGER.error("Invalid patrol settings; using defaults", exception);
            if (json.equals(defaults("patrols.json"))) throw exception;
            return loadPatrol(defaults("patrols.json"));
        }
    }

    private static Map<EnumCreatureType, List<Biome.SpawnListEntry>> loadOutpost(JsonObject json) {
        Map<EnumCreatureType, List<Biome.SpawnListEntry>> result = new EnumMap<>(EnumCreatureType.class);
        try {
            for (Map.Entry<String, JsonElement> category : json.getAsJsonObject("spawnLists").entrySet()) {
                EnumCreatureType type;
                try {
                    type = EnumCreatureType.valueOf(category.getKey().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                    LOGGER.warn("Skipping unknown outpost spawn category {}", category.getKey());
                    continue;
                }
                List<Biome.SpawnListEntry> entries = new ArrayList<>();
                long weight = 0L;
                int index = 0;
                for (JsonElement element : category.getValue().getAsJsonArray()) {
                    try {
                        JsonObject entry = element.getAsJsonObject();
                        EntityEntry entity = resolveEntity(entry.get("entity").getAsString(), false);
                        if (entity == null) {
                            index++;
                            continue;
                        }
                        int entryWeight = integer(entry, "weight", 1, 1, Integer.MAX_VALUE);
                        if (weight + entryWeight > Integer.MAX_VALUE) throw new IllegalArgumentException("Total spawn weight exceeds Integer.MAX_VALUE");
                        int min = integer(entry, "minCount", 1, 1, 1024);
                        int max = integer(entry, "maxCount", min, min, 1024);
                        entries.add(new Biome.SpawnListEntry(entity.getEntityClass().asSubclass(EntityLiving.class), entryWeight, min, max));
                        weight += entryWeight;
                    } catch (RuntimeException exception) {
                        LOGGER.warn("Skipping invalid outpost spawn {} at index {}", category.getKey(), index, exception);
                    }
                    index++;
                }
                result.put(type, Collections.unmodifiableList(entries));
            }
        } catch (RuntimeException exception) {
            LOGGER.error("Invalid outpost settings; using defaults", exception);
            if (json.equals(defaults("pillager_outpost.json"))) throw exception;
            return loadOutpost(defaults("pillager_outpost.json"));
        }
        return Collections.unmodifiableMap(result);
    }

    @Nullable
    private static EntityEntry resolveEntity(String name, boolean patrolMember) {
        ResourceLocation id = new ResourceLocation(name);
        EntityEntry entry;
        if (id.equals(new ResourceLocation("minecraft:pillager"))) {
            if (FFDEntities.isLocalPillagerEnabled()) entry = ForgeRegistries.ENTITIES.getValue(FFDEntities.PILLAGER_ID);
            else if (!patrolMember && FFDConfig.pillagerMode == FFDConfig.FeatureMode.AUTO) {
                entry = FFDCompat.getExternalEntityEntry(FFDCompat.Feature.PILLAGER, "pillager");
            } else return null;
        } else entry = ForgeRegistries.ENTITIES.getValue(id);
        Class<? extends Entity> type = entry == null ? null : entry.getEntityClass();
        if (type == null || !EntityLiving.class.isAssignableFrom(type)) {
            LOGGER.warn("Skipping unregistered or non-living spawn entity {}", name);
            return null;
        }
        return entry;
    }

    private static int integer(JsonObject json, String name, int fallback, int min, int max) {
        if (!json.has(name)) return fallback;
        if (!json.get(name).isJsonPrimitive() || !json.get(name).getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(name + " must be an integer");
        }
        int value = json.get(name).getAsBigDecimal().intValueExact();
        if (value < min || value > max) throw new IllegalArgumentException(name + " must be in " + min + ".." + max);
        return value;
    }

    private static double decimal(JsonObject json, String name, double fallback, double min, double max) {
        if (!json.has(name)) return fallback;
        if (!json.get(name).isJsonPrimitive() || !json.get(name).getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(name + " must be a number");
        }
        double value = json.get(name).getAsDouble();
        if (!Double.isFinite(value) || value < min || value > max) throw new IllegalArgumentException(name + " must be in " + min + ".." + max);
        return value;
    }

    private static boolean bool(JsonObject json, String name, boolean fallback) {
        if (!json.has(name)) return fallback;
        if (!json.get(name).isJsonPrimitive() || !json.get(name).getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(name + " must be a boolean");
        }
        return json.get(name).getAsBoolean();
    }

    static int between(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public static final class Patrol {
        public boolean enabled = true;
        public int intervalMin = 12000;
        public int intervalMax = 13199;
        public double chance = 0.2D;
        public long minimumWorldTime = 120000L;
        public boolean daytimeOnly = true;
        public int villageDistance = 2;
        public int offsetMin = 24;
        public int offsetMax = 47;
        public int memberSpread = 4;
        public int loadedAreaRadius = 10;
        public int maxBlockLight = 8;
        public final Set<Integer> dimensions = new HashSet<>(Collections.singletonList(0));
        public final Set<ResourceLocation> excludedBiomes = new HashSet<>(Arrays.asList(
                new ResourceLocation("minecraft:mushroom_island"), new ResourceLocation("minecraft:mushroom_island_shore")));
        public final List<Member> members = new ArrayList<>();
    }

    public static final class Member {
        public final EntityEntry entity;
        public final int minCount;
        public final int maxCount;
        public final boolean addLocalDifficulty;
        public final boolean leader;
        public final NBTTagCompound nbt;

        private Member(EntityEntry entity, int minCount, int maxCount, boolean addLocalDifficulty,
                       boolean leader, NBTTagCompound nbt) {
            this.entity = entity;
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.addLocalDifficulty = addLocalDifficulty;
            this.leader = leader;
            this.nbt = nbt;
        }
    }
}
