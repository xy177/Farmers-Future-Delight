package xy177.farmersfuturedelight.common.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FFDTradeConfiguration {
    private static final Logger LOGGER = LogManager.getLogger("FFD Trades");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String APPLIED_TAG = "farmers_future_delight:trade_config_applied";
    private static final Map<String, JsonObject> VILLAGER_PROFILES = new LinkedHashMap<>();
    private static File configurationDirectory;
    private static JsonObject wanderingTraderProfile;
    private static boolean updateExistingUntradedVillagers;
    private static boolean loaded;

    private FFDTradeConfiguration() {
    }

    public static synchronized void configure(File configDirectory) throws IOException {
        configurationDirectory = new File(new File(configDirectory, "farmers_future_delight"), "trades");
        Files.createDirectories(new File(configurationDirectory, "villagers").toPath());
        createIfMissing("settings.json", "{\"updateExistingUntradedVillagers\":false}\n");
        copyExample("_example_villager.json");
        copyExample("_example_wandering_trader.json");
        createIfMissing("wandering_trader.json", "{\"enabled\":true,\"replaceDefaults\":false,\"remove\":[],\"replace\":[],\"add\":[]}\n");
    }

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        if (configurationDirectory == null) return;
        loadSettings();
        generateVillagerProfiles();
        loadVillagerProfiles();
        JsonObject wanderingProfile = readObject(new File(configurationDirectory, "wandering_trader.json"));
        wanderingTraderProfile = wanderingProfile == null ? null
                : sanitizeProfile(wanderingProfile, "wandering trader");
    }

    public static synchronized boolean hasProfile(EntityVillager villager) {
        load();
        JsonObject profile = getVillagerProfile(villager);
        return profile != null && (!profile.has("enabled") || profile.get("enabled").getAsBoolean());
    }

    public static synchronized boolean shouldUpdateExistingUntradedVillagers() {
        load();
        return updateExistingUntradedVillagers;
    }

    public static synchronized boolean apply(EntityVillager villager, MerchantRecipeList recipes) {
        load();
        JsonObject profile = getVillagerProfile(villager);
        if (profile == null || profile.has("enabled") && !profile.get("enabled").getAsBoolean()) return false;
        int careerLevel = getCareerLevel(villager);
        return applyProfile(profile, recipes, careerLevel, villager.getRNG());
    }

    public static synchronized boolean applyWanderingTrader(MerchantRecipeList recipes, int level) {
        return applyWanderingTrader(recipes, level, new Random());
    }

    public static synchronized boolean applyWanderingTrader(MerchantRecipeList recipes, int level, Random random) {
        load();
        return wanderingTraderProfile != null
                && (!wanderingTraderProfile.has("enabled")
                || wanderingTraderProfile.get("enabled").getAsBoolean())
                && applyProfile(wanderingTraderProfile, recipes, level, random);
    }

    public static synchronized boolean applyNewLevel(EntityVillager villager, MerchantRecipeList recipes, int level) {
        load();
        JsonObject profile = getVillagerProfile(villager);
        if (profile == null || profile.has("enabled") && !profile.get("enabled").getAsBoolean()) return false;
        JsonObject current = new JsonParser().parse(profile.toString()).getAsJsonObject();
        JsonArray additions = new JsonArray();
        if (current.has("add")) {
            for (JsonElement element : current.getAsJsonArray("add")) {
                JsonObject offer = element.getAsJsonObject();
                int firstLevel = offer.has("minCareerLevel") ? offer.get("minCareerLevel").getAsInt() : 1;
                if (firstLevel == level) additions.add(offer);
            }
        }
        current.add("add", additions);
        return applyProfile(current, recipes, level, villager.getRNG());
    }

    public static synchronized String getWanderingTraderProfileKey() {
        load();
        String profile = wanderingTraderProfile == null ? "" : wanderingTraderProfile.toString();
        return com.google.common.hash.Hashing.sha256().hashString(profile, StandardCharsets.UTF_8).toString();
    }

    public static synchronized String getVillagerProfileKey(EntityVillager villager) {
        load();
        JsonObject profile = getVillagerProfile(villager);
        String definition = profile == null ? "" : profile.toString();
        return com.google.common.hash.Hashing.sha256().hashString(definition, StandardCharsets.UTF_8).toString();
    }

    public static String getAppliedTag() {
        return APPLIED_TAG;
    }

    public static synchronized void applyDisplayProfile(ResourceLocation profession, String career,
                                                         MerchantRecipeList recipes, int level) {
        load();
        JsonObject profile = VILLAGER_PROFILES.get(profileKey(profession, career));
        if (profile != null && (!profile.has("enabled") || profile.get("enabled").getAsBoolean())) {
            JsonObject current = new JsonParser().parse(profile.toString()).getAsJsonObject();
            JsonArray additions = new JsonArray();
            if (current.has("add")) {
                for (JsonElement element : current.getAsJsonArray("add")) {
                    JsonObject offer = element.getAsJsonObject();
                    int firstLevel = offer.has("minCareerLevel") ? offer.get("minCareerLevel").getAsInt() : 1;
                    if (firstLevel == level) additions.add(offer);
                }
            }
            current.add("add", additions);
            applyProfile(current, recipes, level, new Random());
        }
    }

    private static void loadSettings() {
        JsonObject settings = readObject(new File(configurationDirectory, "settings.json"));
        if (settings == null || !settings.has("updateExistingUntradedVillagers")) return;
        try {
            updateExistingUntradedVillagers = settings.get("updateExistingUntradedVillagers").getAsBoolean();
        } catch (RuntimeException exception) {
            LOGGER.warn("Ignoring invalid updateExistingUntradedVillagers setting", exception);
        }
    }

    private static void generateVillagerProfiles() {
        for (VillagerProfession profession : ForgeRegistries.VILLAGER_PROFESSIONS.getValuesCollection()) {
            ResourceLocation id = profession.getRegistryName();
            if (id == null) continue;
            File file = profileFile(id);
            boolean exists = file.exists();
            JsonObject root = exists ? readObject(file) : new JsonObject();
            if (root == null) continue;
            JsonElement careersElement = root.get("careers");
            if (careersElement != null && !careersElement.isJsonObject()) {
                LOGGER.warn("Skipping invalid villager careers in {}", file);
                continue;
            }
            JsonObject careers = careersElement == null ? null : careersElement.getAsJsonObject();
            if (careers == null) {
                careers = new JsonObject();
                root.add("careers", careers);
            }
            boolean changed = false;
            List<String> careerNames = FFDProfessionWorkstations.careerNames(profession);
            if (careerNames.isEmpty()) careerNames.add("*");
            for (String career : careerNames) {
                if (careers.has(career)) continue;
                careers.add(career, emptyProfile());
                changed = true;
            }
            if (!exists || changed) writeObject(file, root);
        }
    }

    private static void loadVillagerProfiles() {
        File directory = new File(configurationDirectory, "villagers");
        File[] namespaces = directory.listFiles();
        if (namespaces == null) return;
        for (File namespace : namespaces) {
            if (!namespace.isDirectory()) continue;
            loadVillagerProfiles(namespace, namespace, namespace.getName());
        }
    }

    private static void loadVillagerProfiles(File namespaceDirectory, File currentDirectory, String namespace) {
        File[] children = currentDirectory.listFiles();
        if (children == null) return;
        for (File file : children) {
            if (file.isDirectory()) {
                loadVillagerProfiles(namespaceDirectory, file, namespace);
                continue;
            }
            if (!file.getName().endsWith(".json")) continue;
            JsonObject root = readObject(file);
            if (root == null) continue;
            JsonElement careersElement = root.get("careers");
            if (careersElement == null || !careersElement.isJsonObject()) {
                LOGGER.warn("Skipping invalid villager careers in {}", file);
                continue;
            }
            String relativePath = namespaceDirectory.toPath().relativize(file.toPath()).toString()
                    .replace(File.separatorChar, '/');
            String professionPath = relativePath.substring(0, relativePath.length() - 5);
            ResourceLocation profession;
            try {
                profession = new ResourceLocation(namespace, professionPath);
            } catch (RuntimeException exception) {
                LOGGER.warn("Skipping trade profile with invalid profession path {}", file, exception);
                continue;
            }
            for (Map.Entry<String, JsonElement> entry : careersElement.getAsJsonObject().entrySet()) {
                JsonObject profile = sanitizeProfile(entry.getValue(), profession + " career " + entry.getKey());
                if (profile != null) {
                    VILLAGER_PROFILES.put(profileKey(profession, entry.getKey()), profile);
                }
            }
        }
    }

    @Nullable
    private static JsonObject getVillagerProfile(EntityVillager villager) {
        VillagerProfession profession = villager.getProfessionForge();
        ResourceLocation id = profession == null ? null : profession.getRegistryName();
        String career = FFDProfessionWorkstations.getCareerName(villager);
        return id == null || career == null ? null : VILLAGER_PROFILES.get(profileKey(id, career));
    }

    private static boolean applyProfile(JsonObject profile, MerchantRecipeList recipes, int level, Random random) {
        boolean changed = false;
        if (profile.has("replaceDefaults") && profile.get("replaceDefaults").getAsBoolean()) {
            recipes.clear();
            changed = true;
        }
        changed |= removeRecipes(profile.getAsJsonArray("remove"), recipes);
        changed |= replaceRecipes(profile.getAsJsonArray("replace"), recipes, level, random);
        changed |= addRecipes(profile, recipes, level, random);
        return changed;
    }

    private static boolean removeRecipes(@Nullable JsonArray removals, MerchantRecipeList recipes) {
        if (removals == null) return false;
        boolean changed = false;
        for (JsonElement element : removals) {
            try {
                JsonObject match = element.getAsJsonObject();
                validateTradeMatcher(match);
                for (int index = recipes.size() - 1; index >= 0; index--) {
                    if (matches(recipes.get(index), match)) {
                        recipes.remove(index);
                        changed = true;
                    }
                }
            } catch (RuntimeException exception) {
                LOGGER.warn("Skipping invalid trade removal {}", element, exception);
            }
        }
        return changed;
    }

    private static boolean replaceRecipes(@Nullable JsonArray replacements, MerchantRecipeList recipes,
                                          int level, Random random) {
        if (replacements == null) return false;
        boolean changed = false;
        for (JsonElement element : replacements) {
            try {
                JsonObject replacement = element.getAsJsonObject();
                JsonObject match = replacement.getAsJsonObject("match");
                if (match == null) throw new IllegalArgumentException("Trade replacement has no match");
                validateTradeMatcher(match);
                JsonObject offer = replacement.getAsJsonObject("offer");
                MerchantRecipe recipe = createRecipe(offer, level, random);
                if (recipe != null) {
                    changed |= removeRecipes(singleton(match), recipes);
                    recipes.add(recipe);
                    changed = true;
                }
            } catch (RuntimeException exception) {
                LOGGER.warn("Skipping invalid trade replacement {}", element, exception);
            }
        }
        return changed;
    }

    private static boolean addRecipes(JsonObject profile, MerchantRecipeList recipes, int level,
                                      Random random) {
        JsonArray additions = profile.getAsJsonArray("add");
        if (additions == null) return false;
        boolean changed = false;
        List<JsonObject> candidates = new ArrayList<>();
        for (JsonElement element : additions) {
            JsonObject definition = element.getAsJsonObject();
            int minimum = definition.has("minCareerLevel") ? definition.get("minCareerLevel").getAsInt() : 0;
            int maximum = definition.has("maxCareerLevel") ? definition.get("maxCareerLevel").getAsInt() : Integer.MAX_VALUE;
            if (level >= minimum && level <= maximum) candidates.add(definition);
        }
        if (profile.has("addAmount")) {
            int amount = Math.max(0, profile.get("addAmount").getAsInt());
            candidates = weightedOrder(candidates, random);
            if (candidates.size() > amount) candidates = new ArrayList<>(candidates.subList(0, amount));
        }
        for (JsonObject definition : candidates) {
            try {
                MerchantRecipe recipe = createRecipe(definition, level, random);
                if (recipe != null) {
                    recipes.add(recipe);
                    changed = true;
                }
            } catch (RuntimeException exception) {
                LOGGER.warn("Skipping invalid trade addition {}", definition, exception);
            }
        }
        return changed;
    }

    private static MerchantRecipe createRecipe(JsonObject entry, int level, Random random) {
        int minLevel = entry.has("minCareerLevel") ? entry.get("minCareerLevel").getAsInt() : 0;
        int maxLevel = entry.has("maxCareerLevel") ? entry.get("maxCareerLevel").getAsInt() : Integer.MAX_VALUE;
        if (level < minLevel || level > maxLevel) return null;
        ItemStack buy = readStack(entry.getAsJsonObject("buy"), random);
        ItemStack sell = readStack(entry.getAsJsonObject("sell"), random);
        ItemStack buySecond = entry.has("buySecond")
                ? readStack(entry.getAsJsonObject("buySecond"), random) : ItemStack.EMPTY;
        if (buy.isEmpty() || sell.isEmpty() || entry.has("buySecond") && buySecond.isEmpty()) {
            throw new IllegalArgumentException("Trade stacks must resolve to registered items");
        }
        int maxUses = entry.has("maxUses") ? entry.get("maxUses").getAsInt() : 12;
        if (maxUses < 1 || maxUses > 1000000) {
            throw new IllegalArgumentException("maxUses must be between 1 and 1000000");
        }
        return new MerchantRecipe(buy, buySecond, sell, 0, maxUses);
    }

    private static boolean matches(MerchantRecipe recipe, JsonObject match) {
        return matchesStack(recipe.getItemToBuy(), match.get("buy"))
                && matchesStack(recipe.getSecondItemToBuy(), match.get("buySecond"))
                && matchesStack(recipe.getItemToSell(), match.get("sell"));
    }

    private static boolean matchesStack(ItemStack actual, @Nullable JsonElement requested) {
        if (requested == null) return true;
        if (!requested.isJsonObject()) throw new IllegalArgumentException("Stack matcher must be an object");
        JsonObject matcher = requested.getAsJsonObject();
        if (matcher.has("id")) {
            ResourceLocation id = new ResourceLocation(matcher.get("id").getAsString());
            if (actual.isEmpty() || !id.equals(actual.getItem().getRegistryName())) return false;
        } else if (matcher.has("meta") || matcher.has("count") || matcher.has("minCount")
                || matcher.has("maxCount") || matcher.has("nbt")) {
            throw new IllegalArgumentException("Stack matcher modifiers require an id");
        }
        if (actual.isEmpty()) return false;
        if (matcher.has("meta") && !matchesInt(actual.getMetadata(), matcher.get("meta"))) return false;
        if (matcher.has("count") && !matchesInt(actual.getCount(), matcher.get("count"))) return false;
        if ((matcher.has("minCount") || matcher.has("maxCount"))
                && !matchesRange(actual.getCount(), matcher, "Count")) return false;
        if (matcher.has("nbt") && !matchesNbt(actual.getTagCompound(), matcher.get("nbt"))) return false;
        return true;
    }

    private static void validateTradeMatcher(JsonObject match) {
        if (!match.has("buy") && !match.has("buySecond") && !match.has("sell")) {
            throw new IllegalArgumentException("Trade matcher requires at least one stack matcher");
        }
        validateStackMatcher(match.get("buy"));
        validateStackMatcher(match.get("buySecond"));
        validateStackMatcher(match.get("sell"));
    }

    private static void validateStackMatcher(@Nullable JsonElement element) {
        if (element == null) return;
        if (!element.isJsonObject()) throw new IllegalArgumentException("Stack matcher must be an object");
        JsonObject matcher = element.getAsJsonObject();
        if (!matcher.has("id") && (matcher.has("meta") || matcher.has("count")
                || matcher.has("minCount") || matcher.has("maxCount") || matcher.has("nbt"))) {
            throw new IllegalArgumentException("Stack matcher modifiers require an id");
        }
        if (matcher.has("id")) new ResourceLocation(matcher.get("id").getAsString());
        if (matcher.has("meta")) validateIntChoice(matcher.get("meta"));
        if (matcher.has("count")) validateIntChoice(matcher.get("count"));
        if (matcher.has("minCount")) matcher.get("minCount").getAsInt();
        if (matcher.has("maxCount")) matcher.get("maxCount").getAsInt();
        if (matcher.has("nbt")) validateNbtChoice(matcher.get("nbt"));
    }

    private static ItemStack readStack(JsonObject entry, Random random) {
        if (entry == null || !entry.has("id")) return ItemStack.EMPTY;
        ResourceLocation id = new ResourceLocation(entry.get("id").getAsString());
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) return ItemStack.EMPTY;
        int count = chooseInt(entry, "count", random, 1);
        int metadata = chooseInt(entry, "meta", random, 0);
        if (count < 1 || count > 64 || metadata < 0) {
            throw new IllegalArgumentException("Trade stack has invalid count or metadata");
        }
        ItemStack stack = new ItemStack(item, count, metadata);
        if (entry.has("nbt")) stack.setTagCompound(chooseNbt(entry.get("nbt"), random));
        return stack;
    }

    static List<JsonObject> weightedOrder(List<JsonObject> source, Random random) {
        List<JsonObject> remaining = new ArrayList<>(source);
        List<JsonObject> result = new ArrayList<>();
        while (!remaining.isEmpty()) {
            int total = 0;
            for (JsonObject entry : remaining) total += weight(entry);
            if (total <= 0) break;
            int selected = random.nextInt(Math.max(1, total));
            int index = 0;
            for (; index < remaining.size(); index++) {
                selected -= weight(remaining.get(index));
                if (selected < 0) break;
            }
            result.add(remaining.remove(Math.min(index, remaining.size() - 1)));
        }
        return result;
    }

    static int weight(JsonObject entry) {
        if (!entry.has("weight")) return 1;
        try {
            return Math.max(0, entry.get("weight").getAsInt());
        } catch (RuntimeException exception) {
            return 1;
        }
    }

    private static int chooseInt(JsonObject entry, String key, Random random, int defaultValue) {
        if (entry.has(key)) return chooseIntChoice(entry.get(key), random, defaultValue);
        String minKey = "min" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
        String maxKey = "max" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
        if (entry.has(minKey) || entry.has(maxKey)) {
            int minimum = entry.has(minKey) ? entry.get(minKey).getAsInt() : defaultValue;
            int maximum = entry.has(maxKey) ? entry.get(maxKey).getAsInt() : minimum;
            return chooseInt(minimum, maximum, random);
        }
        return defaultValue;
    }

    private static int chooseIntChoice(JsonElement element, Random random, int defaultValue) {
        if (element.isJsonObject()) {
            JsonObject range = element.getAsJsonObject();
            int minimum = range.has("min") ? range.get("min").getAsInt() : defaultValue;
            int maximum = range.has("max") ? range.get("max").getAsInt() : minimum;
            return chooseInt(minimum, maximum, random);
        }
        if (element.isJsonArray()) {
            JsonArray values = element.getAsJsonArray();
            if (values.size() == 0) throw new IllegalArgumentException("Trade choice set is empty");
            return values.get(random.nextInt(values.size())).getAsInt();
        }
        return element.getAsInt();
    }

    private static int chooseInt(int minimum, int maximum, Random random) {
        if (minimum > maximum) throw new IllegalArgumentException("Trade range minimum exceeds maximum");
        if (maximum - minimum >= Integer.MAX_VALUE) return minimum;
        return minimum + random.nextInt(maximum - minimum + 1);
    }

    private static NBTTagCompound chooseNbt(JsonElement element, Random random) {
        if (element.isJsonArray()) {
            JsonArray values = element.getAsJsonArray();
            if (values.size() == 0) throw new IllegalArgumentException("Trade NBT choice set is empty");
            return parseNbt(values.get(random.nextInt(values.size())).getAsString());
        }
        return parseNbt(element.getAsString());
    }

    private static boolean matchesInt(int actual, JsonElement requested) {
        if (requested.isJsonArray()) {
            for (JsonElement value : requested.getAsJsonArray()) {
                if (matchesInt(actual, value)) return true;
            }
            return false;
        }
        if (requested.isJsonObject()) {
            JsonObject range = requested.getAsJsonObject();
            int minimum = range.has("min") ? range.get("min").getAsInt() : Integer.MIN_VALUE;
            int maximum = range.has("max") ? range.get("max").getAsInt() : Integer.MAX_VALUE;
            return actual >= minimum && actual <= maximum;
        }
        return actual == requested.getAsInt();
    }

    private static boolean matchesNbt(@Nullable NBTTagCompound actual, JsonElement requested) {
        if (requested.isJsonArray()) {
            for (JsonElement value : requested.getAsJsonArray()) {
                if (matchesNbt(actual, value)) return true;
            }
            return false;
        }
        return parseNbt(requested.getAsString()).equals(actual);
    }

    private static boolean matchesRange(int actual, JsonObject object, String suffix) {
        String minimumKey = "min" + suffix;
        String maximumKey = "max" + suffix;
        int minimum = object.has(minimumKey) ? object.get(minimumKey).getAsInt() : Integer.MIN_VALUE;
        int maximum = object.has(maximumKey) ? object.get(maximumKey).getAsInt() : Integer.MAX_VALUE;
        return actual >= minimum && actual <= maximum;
    }

    private static void validateIntChoice(JsonElement element) {
        if (element.isJsonArray()) {
            if (element.getAsJsonArray().size() == 0) throw new IllegalArgumentException("Trade choice set is empty");
            for (JsonElement value : element.getAsJsonArray()) validateIntChoice(value);
        } else if (element.isJsonObject()) {
            JsonObject range = element.getAsJsonObject();
            if (range.has("min")) range.get("min").getAsInt();
            if (range.has("max")) range.get("max").getAsInt();
        } else element.getAsInt();
    }

    private static void validateNbtChoice(JsonElement element) {
        if (element.isJsonArray()) {
            if (element.getAsJsonArray().size() == 0) throw new IllegalArgumentException("Trade NBT choice set is empty");
            for (JsonElement value : element.getAsJsonArray()) parseNbt(value.getAsString());
        } else parseNbt(element.getAsString());
    }

    private static NBTTagCompound parseNbt(String value) {
        try {
            return JsonToNBT.getTagFromJson(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid trade stack NBT", exception);
        }
    }

    private static JsonArray singleton(JsonObject object) {
        JsonArray array = new JsonArray();
        array.add(object);
        return array;
    }

    private static int getCareerLevel(EntityVillager villager) {
        try {
            return ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, villager,
                    "field_175562_bw");
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not read villager career level", exception);
            return 0;
        }
    }

    private static JsonObject emptyProfile() {
        JsonObject profile = new JsonObject();
        profile.addProperty("enabled", true);
        profile.addProperty("replaceDefaults", false);
        profile.add("remove", new JsonArray());
        profile.add("replace", new JsonArray());
        profile.add("add", new JsonArray());
        return profile;
    }

    private static String profileKey(ResourceLocation profession, String career) {
        return profession + "/" + career;
    }

    private static File profileFile(ResourceLocation profession) {
        File directory = new File(new File(configurationDirectory, "villagers"), profession.getResourceDomain());
        return new File(directory, profession.getResourcePath() + ".json");
    }

    @Nullable
    private static JsonObject sanitizeProfile(JsonElement element, String context) {
        if (!element.isJsonObject()) {
            LOGGER.warn("Skipping invalid trade profile for {}: expected an object", context);
            return null;
        }
        JsonObject profile = element.getAsJsonObject();
        validateBoolean(profile, "enabled", false, context);
        validateBoolean(profile, "replaceDefaults", false, context);
        if (profile.has("addAmount")) {
            try {
                if (profile.get("addAmount").getAsInt() < 0) throw new IllegalArgumentException();
            } catch (RuntimeException exception) {
                LOGGER.warn("Ignoring invalid addAmount in trade profile for {}", context);
                profile.remove("addAmount");
            }
        }
        validateArray(profile, "remove", context);
        validateArray(profile, "replace", context);
        validateArray(profile, "add", context);
        return profile;
    }

    private static void validateBoolean(JsonObject object, String key, boolean invalidValue, String context) {
        if (!object.has(key)) return;
        JsonElement value = object.get(key);
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) return;
        LOGGER.warn("Ignoring invalid {} value in trade profile for {}", key, context);
        object.addProperty(key, invalidValue);
    }

    private static void validateArray(JsonObject object, String key, String context) {
        if (!object.has(key) || object.get(key).isJsonArray()) return;
        LOGGER.warn("Ignoring invalid {} list in trade profile for {}", key, context);
        object.add(key, new JsonArray());
    }

    private static void createIfMissing(String relativePath, String contents) throws IOException {
        File file = new File(configurationDirectory, relativePath);
        if (file.exists()) return;
        File parent = file.getParentFile();
        if (parent != null) Files.createDirectories(parent.toPath());
        Files.write(file.toPath(), contents.getBytes(StandardCharsets.UTF_8));
    }

    private static void copyExample(String name) throws IOException {
        File file = new File(configurationDirectory, name);
        if (file.exists()) return;
        String path = "/assets/farmers_future_delight/trades/" + name;
        try (InputStream input = FFDTradeConfiguration.class.getResourceAsStream(path)) {
            if (input == null) throw new IOException("Missing trade example " + path);
            try (FileOutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = input.read(buffer)) != -1) output.write(buffer, 0, length);
            }
        }
    }

    @Nullable
    private static JsonObject readObject(File file) {
        if (!file.exists()) return null;
        try (Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        } catch (Exception exception) {
            LOGGER.error("Skipping invalid trade configuration {}", file, exception);
            return null;
        }
    }

    private static void writeObject(File file, JsonObject object) {
        File parent = file.getParentFile();
        try {
            if (parent != null) Files.createDirectories(parent.toPath());
            Files.write(file.toPath(), GSON.toJson(object).getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            LOGGER.error("Could not write generated trade configuration {}", file, exception);
        }
    }
}
