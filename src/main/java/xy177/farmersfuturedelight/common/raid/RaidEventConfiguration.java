package xy177.farmersfuturedelight.common.raid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.BossBar;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Participant;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Reward;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Timing;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Trigger;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Wave;
import xy177.farmersfuturedelight.api.raid.RaidEventRegistrationEvent;
import xy177.farmersfuturedelight.api.raid.RaidEventRegistry;

public final class RaidEventConfiguration {
    private static final Logger LOGGER = LogManager.getLogger("FFD Raid Events");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int MAX_JSON_CHARACTERS = 1048576;
    private static final List<Runnable> PENDING_SCRIPT_ACTIONS = new ArrayList<>();
    private static Path directory;
    private static boolean loading;
    private static boolean loaded;

    private RaidEventConfiguration() { }

    public static synchronized void configure(File configDirectory) {
        if (loading || loaded) throw new IllegalStateException("Raid events have already started loading");
        Path target = Objects.requireNonNull(configDirectory, "configDirectory").toPath()
                .resolve("farmers_future_delight").resolve("raids");
        directory = target;
        try {
            Files.createDirectories(target);
            createIfMissing(target.resolve("events.json"), "{\"events\":[]}\n");
            createIfMissing(target.resolve("_example.json"), GSON.toJson(parseJson(example())) + "\n");
        } catch (IOException | SecurityException exception) {
            LOGGER.error("Could not initialize raid event configuration at {}; defaults, API and scripts remain available", target, exception);
        }
    }

    public static synchronized void finishLoading(Collection<RaidEventDefinition> defaults) {
        if (loaded) return;
        if (loading) throw new IllegalStateException("Recursive raid event loading");
        if (directory == null) throw new IllegalStateException("Configure raid events before loading");
        List<RaidEventDefinition> defaultCopy = new ArrayList<>(Objects.requireNonNull(defaults, "defaults"));
        for (RaidEventDefinition definition : defaultCopy) Objects.requireNonNull(definition, "default definition");
        loading = true;
        try {
            for (RaidEventDefinition definition : defaultCopy) {
                if (RaidEventRegistry.get(definition.getId()) == null) RaidEventRegistry.register(definition);
            }
            MinecraftForge.EVENT_BUS.post(new RaidEventRegistrationEvent());
            loadPlayerConfiguration();
            while (!PENDING_SCRIPT_ACTIONS.isEmpty()) {
                List<Runnable> actions = new ArrayList<>(PENDING_SCRIPT_ACTIONS);
                PENDING_SCRIPT_ACTIONS.clear();
                for (Runnable action : actions) {
                    try {
                        action.run();
                    } catch (RuntimeException exception) {
                        LOGGER.error("Rejected queued raid event script action; previous definition retained", exception);
                    }
                }
            }
            loaded = true;
        } finally {
            loading = false;
        }
    }

    public static synchronized void applyScriptAction(Runnable action) {
        Objects.requireNonNull(action, "action");
        if (loaded) action.run();
        else PENDING_SCRIPT_ACTIONS.add(action);
    }

    public static RaidEventDefinition parseDefinition(String json) {
        return definition(object(parseJson(json), "event"));
    }

    public static synchronized boolean isLoaded() { return loaded; }

    public static String serializeDefinition(RaidEventDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        JsonObject entry = new JsonObject();
        entry.addProperty("id", definition.getId().toString());
        if (definition.getScriptRules() != null) entry.addProperty("scriptRules", definition.getScriptRules().write().toString());
        Trigger source = definition.getTrigger();
        JsonObject trigger = new JsonObject();
        trigger.addProperty("type", source.getType().toString());
        JsonArray dimensions = new JsonArray();
        for (Integer dimension : source.getDimensions()) dimensions.add(new JsonPrimitive(dimension));
        trigger.add("dimensions", dimensions);
        JsonArray biomes = new JsonArray();
        for (ResourceLocation biome : source.getBiomes()) biomes.add(new JsonPrimitive(biome.toString()));
        trigger.add("biomes", biomes);
        trigger.addProperty("requiresVillage", source.requiresVillage());
        trigger.addProperty("minOmenLevel", source.getMinOmenLevel());
        trigger.addProperty("maxOmenLevel", source.getMaxOmenLevel());
        entry.add("trigger", trigger);
        entry.addProperty("radius", definition.getRadius());
        Timing sourceTiming = definition.getTiming();
        JsonObject timing = new JsonObject();
        timing.addProperty("preparationTicks", sourceTiming.getPreparationTicks());
        timing.addProperty("waveDelayTicks", sourceTiming.getWaveDelayTicks());
        timing.addProperty("victoryDelayTicks", sourceTiming.getVictoryDelayTicks());
        timing.addProperty("celebrationTicks", sourceTiming.getCelebrationTicks());
        timing.addProperty("timeoutTicks", sourceTiming.getTimeoutTicks());
        entry.add("timing", timing);
        BossBar sourceBar = definition.getBossBar();
        JsonObject bar = new JsonObject();
        bar.addProperty("enabled", sourceBar.isEnabled());
        bar.addProperty("title", sourceBar.getTitle());
        bar.addProperty("victoryTitle", sourceBar.getVictoryTitle());
        bar.addProperty("defeatTitle", sourceBar.getDefeatTitle());
        bar.addProperty("color", sourceBar.getColor().name());
        bar.addProperty("overlay", sourceBar.getOverlay().name());
        entry.add("bossBar", bar);
        entry.add("waves", serializeWaves(definition.getWaves()));
        entry.add("bonusWaves", serializeWaves(definition.getBonusWaves()));
        entry.addProperty("bonusOmenLevel", definition.getBonusOmenLevel());
        entry.add("victoryRewards", serializeRewards(definition.getVictoryRewards()));
        entry.add("defeatRewards", serializeRewards(definition.getDefeatRewards()));
        return GSON.toJson(entry);
    }

    private static JsonObject serializeWaves(Map<EnumDifficulty, List<Wave>> difficulties) {
        JsonObject result = new JsonObject();
        for (Map.Entry<EnumDifficulty, List<Wave>> difficulty : difficulties.entrySet()) {
            JsonArray waves = new JsonArray();
            for (Wave source : difficulty.getValue()) {
                JsonObject wave = new JsonObject();
                JsonArray participants = new JsonArray();
                for (Participant participant : source.getParticipants()) participants.add(serializeParticipant(participant));
                wave.add("participants", participants);
                waves.add(wave);
            }
            result.add(difficulty.getKey().name().toLowerCase(Locale.ROOT), waves);
        }
        return result;
    }

    private static JsonObject serializeParticipant(Participant source) {
        JsonObject participant = new JsonObject();
        participant.addProperty("entity", source.getEntity().toString());
        participant.addProperty("count", source.getCount());
        participant.addProperty("leader", source.isLeader());
        participant.addProperty("nbt", source.getNbt().toString());
        JsonArray passengers = new JsonArray();
        for (Participant passenger : source.getPassengers()) passengers.add(serializeParticipant(passenger));
        participant.add("passengers", passengers);
        return participant;
    }

    private static JsonArray serializeRewards(List<Reward> sources) {
        JsonArray rewards = new JsonArray();
        for (Reward source : sources) {
            JsonObject reward = new JsonObject();
            reward.addProperty("type", source.getType().name().toLowerCase(Locale.ROOT));
            reward.addProperty("recipient", source.getRecipient().name().toLowerCase(Locale.ROOT));
            if (source.getId() != null) reward.addProperty("id", source.getId().toString());
            if (source.getType() == Reward.Type.EFFECT) {
                reward.addProperty("duration", source.getDuration());
                reward.addProperty("amplifier", source.getAmplifier());
            } else {
                reward.addProperty("amount", source.getAmount());
            }
            if (source.getType() == Reward.Type.ITEM) {
                reward.addProperty("metadata", source.getMetadata());
                reward.addProperty("nbt", source.getNbt().toString());
            }
            rewards.add(reward);
        }
        return rewards;
    }

    private static void loadPlayerConfiguration() {
        JsonArray events;
        try {
            JsonObject root = object(parseJson(readFile(directory.resolve("events.json"))), "configuration");
            fields(root, "events");
            events = array(root, "events", true);
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not read raid event configuration; existing definitions retained", exception);
            return;
        }
        int index = 0;
        for (JsonElement element : events) {
            try {
                JsonObject entry = object(element, "event");
                if (entry.has("remove")) {
                    fields(entry, "id", "remove");
                    ResourceLocation id = identifier(entry, "id");
                    if (!bool(entry, "remove")) throw new IllegalArgumentException("remove must be true");
                    RaidEventRegistry.remove(id);
                } else {
                    RaidEventRegistry.registerData(definition(entry));
                }
            } catch (RuntimeException exception) {
                LOGGER.error("Rejected raid event entry {} in events.json; previous definition retained", index, exception);
            }
            index++;
        }
    }

    private static RaidEventDefinition definition(JsonObject entry) {
        fields(entry, "id", "trigger", "radius", "timing", "bossBar", "waves", "bonusWaves",
                "bonusOmenLevel", "victoryRewards", "defeatRewards", "scriptRules");
        RaidEventDefinition.Builder builder = RaidEventDefinition.builder(identifier(entry, "id"));
        if (entry.has("scriptRules")) {
            try {
                builder.scriptRules(new xy177.farmersfuturedelight.api.raid.RaidScriptRules(JsonToNBT.getTagFromJson(string(entry, "scriptRules"))));
            } catch (NBTException exception) {
                throw new IllegalArgumentException("Invalid scriptRules NBT", exception);
            }
        }
        JsonObject trigger = object(required(entry, "trigger"), "trigger");
        fields(trigger, "type", "dimensions", "biomes", "requiresVillage", "minOmenLevel", "maxOmenLevel");
        List<Integer> dimensions = new ArrayList<>();
        for (JsonElement dimension : array(trigger, "dimensions", true)) dimensions.add(integer(dimension, "dimension"));
        List<ResourceLocation> biomes = new ArrayList<>();
        for (JsonElement biome : array(trigger, "biomes", true)) biomes.add(RaidEventDefinition.identifier(string(biome, "biome")));
        builder.trigger(new Trigger(identifier(trigger, "type"), dimensions, biomes,
                bool(trigger, "requiresVillage"), integer(trigger, "minOmenLevel"), integer(trigger, "maxOmenLevel")));
        JsonElement radius = required(entry, "radius");
        if (!radius.isJsonPrimitive() || !radius.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("radius must be a number");
        }
        builder.radius(radius.getAsDouble());
        JsonObject timing = object(required(entry, "timing"), "timing");
        fields(timing, "preparationTicks", "waveDelayTicks", "victoryDelayTicks", "celebrationTicks", "timeoutTicks");
        builder.timing(new Timing(integer(timing, "preparationTicks"), integer(timing, "waveDelayTicks"),
                integer(timing, "victoryDelayTicks"), integer(timing, "celebrationTicks"), integer(timing, "timeoutTicks")));
        JsonObject bar = object(required(entry, "bossBar"), "bossBar");
        fields(bar, "enabled", "title", "victoryTitle", "defeatTitle", "color", "overlay");
        builder.bossBar(new BossBar(bool(bar, "enabled"), string(bar, "title"), string(bar, "victoryTitle"),
                string(bar, "defeatTitle"), BossInfo.Color.valueOf(string(bar, "color").toUpperCase(Locale.ROOT)),
                BossInfo.Overlay.valueOf(string(bar, "overlay").toUpperCase(Locale.ROOT))));
        waves(builder, object(required(entry, "waves"), "waves"), false);
        if (entry.has("bonusWaves")) waves(builder, object(entry.get("bonusWaves"), "bonusWaves"), true);
        builder.bonusOmenLevel(integer(entry, "bonusOmenLevel"));
        builder.victoryRewards(rewards(array(entry, "victoryRewards", true)));
        builder.defeatRewards(rewards(array(entry, "defeatRewards", true)));
        return builder.build();
    }

    private static void waves(RaidEventDefinition.Builder builder, JsonObject difficulties, boolean bonus) {
        fields(difficulties, "easy", "normal", "hard");
        for (Map.Entry<String, JsonElement> difficulty : difficulties.entrySet()) {
            List<Wave> waves = new ArrayList<>();
            if (!difficulty.getValue().isJsonArray()) throw new IllegalArgumentException("Difficulty waves must be an array");
            for (JsonElement waveValue : difficulty.getValue().getAsJsonArray()) {
                JsonObject wave = object(waveValue, "wave");
                fields(wave, "participants");
                List<Participant> participants = new ArrayList<>();
                for (JsonElement participant : array(wave, "participants", true)) {
                    participants.add(participant(object(participant, "participant"), 0));
                }
                waves.add(new Wave(participants));
            }
            EnumDifficulty level = EnumDifficulty.valueOf(difficulty.getKey().toUpperCase(Locale.ROOT));
            if (bonus) builder.bonusWaves(level, waves);
            else builder.waves(level, waves);
        }
    }

    private static Participant participant(JsonObject value, int depth) {
        if (depth > RaidEventDefinition.MAX_PASSENGER_DEPTH) throw new IllegalArgumentException("Passenger tree is too deep");
        fields(value, "entity", "count", "leader", "nbt", "passengers");
        List<Participant> passengers = new ArrayList<>();
        for (JsonElement passenger : array(value, "passengers", false)) {
            passengers.add(participant(object(passenger, "passenger"), depth + 1));
        }
        return new Participant(identifier(value, "entity"), integer(value, "count"),
                value.has("leader") && bool(value, "leader"), nbt(value), passengers);
    }

    private static List<Reward> rewards(JsonArray values) {
        List<Reward> rewards = new ArrayList<>();
        for (JsonElement value : values) {
            JsonObject reward = object(value, "reward");
            Reward.Type type = Reward.Type.valueOf(string(reward, "type").toUpperCase(Locale.ROOT));
            Reward.Recipient recipient = reward.has("recipient")
                    ? Reward.Recipient.valueOf(string(reward, "recipient").toUpperCase(Locale.ROOT)) : Reward.Recipient.HEROES;
            switch (type) {
                case ITEM:
                    fields(reward, "type", "recipient", "id", "amount", "metadata", "nbt");
                    rewards.add(Reward.item(recipient, identifier(reward, "id"), integer(reward, "amount"),
                            reward.has("metadata") ? integer(reward, "metadata") : 0, nbt(reward)));
                    break;
                case LOOT_TABLE:
                    fields(reward, "type", "recipient", "id", "amount");
                    rewards.add(Reward.lootTable(recipient, identifier(reward, "id"), integer(reward, "amount")));
                    break;
                case EFFECT:
                    fields(reward, "type", "recipient", "id", "duration", "amplifier");
                    rewards.add(Reward.effect(recipient, identifier(reward, "id"), integer(reward, "duration"),
                            integer(reward, "amplifier")));
                    break;
                case EXPERIENCE:
                    fields(reward, "type", "recipient", "amount");
                    rewards.add(Reward.experience(recipient, integer(reward, "amount")));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown reward type");
            }
        }
        return rewards;
    }

    private static NBTTagCompound nbt(JsonObject value) {
        if (!value.has("nbt")) return new NBTTagCompound();
        try {
            return JsonToNBT.getTagFromJson(string(value, "nbt"));
        } catch (NBTException exception) {
            throw new IllegalArgumentException("Invalid SNBT", exception);
        }
    }

    private static ResourceLocation identifier(JsonObject object, String key) {
        return RaidEventDefinition.identifier(string(object, key));
    }

    private static JsonElement required(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) throw new IllegalArgumentException("Missing " + key);
        return value;
    }

    private static JsonObject object(JsonElement value, String name) {
        if (value == null || !value.isJsonObject()) throw new IllegalArgumentException(name + " must be an object");
        return value.getAsJsonObject();
    }

    private static JsonArray array(JsonObject object, String key, boolean mandatory) {
        if (!mandatory && !object.has(key)) return new JsonArray();
        JsonElement value = required(object, key);
        if (!value.isJsonArray()) throw new IllegalArgumentException(key + " must be an array");
        return value.getAsJsonArray();
    }

    private static String string(JsonObject object, String key) { return string(required(object, key), key); }

    private static String string(JsonElement value, String name) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(name + " must be a string");
        }
        return value.getAsString();
    }

    private static int integer(JsonObject object, String key) { return integer(required(object, key), key); }

    private static int integer(JsonElement value, String name) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(name + " must be an integer");
        }
        return new BigDecimal(value.getAsString()).intValueExact();
    }

    private static boolean bool(JsonObject object, String key) {
        JsonElement value = required(object, key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(key + " must be a boolean");
        }
        return value.getAsBoolean();
    }

    private static void fields(JsonObject object, String... names) {
        Set<String> allowed = new HashSet<>(Arrays.asList(names));
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (!allowed.contains(entry.getKey())) throw new IllegalArgumentException("Unknown field " + entry.getKey());
        }
    }

    private static JsonElement parseJson(String text) {
        if (text == null || text.length() > MAX_JSON_CHARACTERS) throw new IllegalArgumentException("JSON is too large");
        try (JsonReader reader = new JsonReader(new StringReader(text))) {
            reader.setLenient(false);
            JsonElement value = readJson(reader, 0);
            if (reader.peek() != JsonToken.END_DOCUMENT) throw new IllegalArgumentException("Trailing JSON data");
            return value;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid strict JSON", exception);
        }
    }

    private static JsonElement readJson(JsonReader reader, int depth) throws IOException {
        if (depth > 64) throw new IllegalArgumentException("JSON nesting is too deep");
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                JsonObject object = new JsonObject();
                reader.beginObject();
                while (reader.hasNext()) {
                    String key = reader.nextName();
                    if (object.has(key)) throw new IllegalArgumentException("Duplicate JSON field " + key);
                    object.add(key, readJson(reader, depth + 1));
                }
                reader.endObject();
                return object;
            case BEGIN_ARRAY:
                JsonArray array = new JsonArray();
                reader.beginArray();
                while (reader.hasNext()) {
                    if (array.size() >= 1024) throw new IllegalArgumentException("JSON array is too large");
                    array.add(readJson(reader, depth + 1));
                }
                reader.endArray();
                return array;
            case STRING:
                return new JsonPrimitive(reader.nextString());
            case NUMBER:
                return new JsonPrimitive(new BigDecimal(reader.nextString()));
            case BOOLEAN:
                return new JsonPrimitive(reader.nextBoolean());
            case NULL:
                reader.nextNull();
                return JsonNull.INSTANCE;
            default:
                throw new IllegalArgumentException("Expected a JSON value");
        }
    }

    private static String readFile(Path path) throws IOException {
        StringBuilder result = new StringBuilder();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            char[] buffer = new char[4096];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                if (result.length() + length > MAX_JSON_CHARACTERS) throw new IOException("JSON is too large");
                result.append(buffer, 0, length);
            }
        }
        return result.toString();
    }

    private static void createIfMissing(Path path, String text) throws IOException {
        try {
            Files.write(path, text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        } catch (FileAlreadyExistsException exception) {
            if (!Files.isRegularFile(path)) throw exception;
        }
    }

    private static String example() {
        return String.join("\n",
                "{\"events\":[{",
                "\"id\":\"example:night_watch\",",
                "\"trigger\":{\"type\":\"farmers_future_delight:manual\",\"dimensions\":[0],\"biomes\":[],",
                "\"requiresVillage\":false,\"minOmenLevel\":0,\"maxOmenLevel\":5},",
                "\"radius\":48,",
                "\"timing\":{\"preparationTicks\":100,\"waveDelayTicks\":100,\"victoryDelayTicks\":20,",
                "\"celebrationTicks\":100,\"timeoutTicks\":12000},",
                "\"bossBar\":{\"enabled\":true,\"title\":\"example.raid.title\",",
                "\"victoryTitle\":\"example.raid.victory\",\"defeatTitle\":\"example.raid.defeat\",",
                "\"color\":\"RED\",\"overlay\":\"PROGRESS\"},",
                "\"waves\":{\"normal\":[{\"participants\":[{\"entity\":\"minecraft:zombie\",",
                "\"count\":2,\"leader\":false,\"nbt\":\"{}\",\"passengers\":[]}]}]},",
                "\"bonusWaves\":{\"normal\":[{\"participants\":[{\"entity\":\"minecraft:zombie\",\"count\":1}]}]},",
                "\"bonusOmenLevel\":2,",
                "\"victoryRewards\":[{\"type\":\"item\",\"id\":\"minecraft:emerald\",\"amount\":1,\"recipient\":\"heroes\"}],",
                "\"defeatRewards\":[]",
                "},{\"id\":\"example:removed_event\",\"remove\":true}]}");
    }
}
