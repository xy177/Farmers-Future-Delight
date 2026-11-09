package xy177.farmersfuturedelight.common.registry;

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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.CompostingRules;
import xy177.farmersfuturedelight.api.WorkstationRecipes;

public final class FFDWorkstationDefaults {
    private static final Logger LOGGER = LogManager.getLogger("FFD Workstations");
    private static final List<Runnable> PENDING_SCRIPT_ACTIONS = new ArrayList<>();
    private static boolean loaded;
    private static boolean defaultsApplied;
    private static File configurationDirectory;

    private FFDWorkstationDefaults() {
    }

    public static void configure(File configDirectory) {
        configurationDirectory = new File(new File(configDirectory, FarmerFutureDelight.MODID), "workstations");
        try {
            Files.createDirectories(configurationDirectory.toPath());
            FFDProfessionWorkstations.configure(configurationDirectory);
            FFDTradeConfiguration.configure(configDirectory);
            createIfMissing("recipes.json", "{\"recipes\":[],\"remove\":[]}\n");
            createIfMissing("composting.json", "{\"rules\":[],\"remove\":[]}\n");
            copyExample("_example_recipes.json");
            copyExample("_example_composting.json");
            copyExample("_example_workstations.zs");
            copyExample("_example_workstations_simple.zs");
        } catch (IOException exception) {
            LOGGER.error("Could not initialize workstation configuration in {}", configurationDirectory,
                    exception);
        }
    }

    public static synchronized void register() {
        if (loaded) {
            return;
        }
        loaded = true;
        int cooking = 0;
        for (JsonElement element : read("cooking_stonecutting.json")) {
            JsonObject entry = element.getAsJsonObject();
            WorkstationRecipes.Type type = WorkstationRecipes.Type.valueOf(
                    entry.get("type").getAsString().toUpperCase(Locale.ROOT));
            JsonObject result = entry.getAsJsonObject("result");
            ItemStack output = resolveStack(result);
            Ingredient input = ingredient(entry.get("ingredient"));
            if (output.isEmpty() || input == Ingredient.EMPTY) {
                continue;
            }
            output.setCount(result.has("count") ? result.get("count").getAsInt() : 1);
            int time = entry.get("processingTime").getAsInt();
            if (type == WorkstationRecipes.Type.SMOKING || type == WorkstationRecipes.Type.BLASTING) {
                time = Math.max(1, (time + 1) / 2);
            }
            ResourceLocation id = new ResourceLocation(entry.get("id").getAsString());
            if (WorkstationRecipes.get(type, id) == null) {
                WorkstationRecipes.register(type, id, Collections.singletonList(input), output,
                        time, entry.get("experience").getAsFloat());
                if (entry.has("group")) WorkstationRecipes.setGroup(type, id, entry.get("group").getAsString());
                cooking++;
            }
        }
        FFDNetheriteSmithing.registerDefaults();
        int composting = 0;
        for (JsonElement element : read("composting.json")) {
            JsonObject entry = element.getAsJsonObject();
            JsonElement item = entry.get("item");
            ItemStack input = item.isJsonObject() ? resolveStack(item.getAsJsonObject())
                    : FFDModernItemLookup.resolve(item.getAsString());
            if (!input.isEmpty()) {
                String name = item.isJsonObject() && item.getAsJsonObject().has("id")
                        ? item.getAsJsonObject().get("id").getAsString() : item.getAsString();
                ResourceLocation id = new ResourceLocation(name);
                if (CompostingRules.get(id) == null) {
                    CompostingRules.register(id, Ingredient.fromStacks(input),
                            entry.get("chance").getAsDouble(), entry.get("villagerCanCompost").getAsBoolean());
                    composting++;
                }
            }
        }
        LOGGER.info("Loaded {} default workstation recipes and {} composting rules for available items",
                cooking, composting);
    }

    public static synchronized void finishLoading() {
        if (defaultsApplied) return;
        register();
        FFDProfessionWorkstations.load();
        FFDTradeConfiguration.load();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new xy177.farmersfuturedelight.api.WorkstationRegistryEvent());
        loadRecipeConfiguration();
        loadCompostingConfiguration();
        defaultsApplied = true;
        for (Runnable action : new ArrayList<>(PENDING_SCRIPT_ACTIONS)) action.run();
        PENDING_SCRIPT_ACTIONS.clear();
        LOGGER.info("Applied workstation configuration and queued CraftTweaker actions");
    }

    public static synchronized void applyScriptAction(Runnable action) {
        if (defaultsApplied) action.run();
        else PENDING_SCRIPT_ACTIONS.add(action);
    }

    private static void loadRecipeConfiguration() {
        JsonObject config = readConfig("recipes.json");
        if (config == null) return;
        removeConfigured(config, "remove", true);
        JsonArray recipes;
        try {
            recipes = config.getAsJsonArray("recipes");
        } catch (RuntimeException exception) {
            LOGGER.error("Skipping invalid recipe list in workstation configuration", exception);
            return;
        }
        if (recipes == null) return;
        for (JsonElement element : recipes) {
            try {
                JsonObject entry = element.getAsJsonObject();
                String group = entry.has("group") ? entry.get("group").getAsString() : "";
                if (group.length() > 256) throw new IllegalArgumentException("Workstation recipe group is too long");
                WorkstationRecipes.Type type = WorkstationRecipes.Type.valueOf(
                        entry.get("type").getAsString().toUpperCase(Locale.ROOT));
                List<Ingredient> ingredients = new ArrayList<>();
                JsonElement ingredient = entry.get("ingredient");
                if (type.getInputCount() == 1) {
                    ingredients.add(ingredient(ingredient));
                } else if (ingredient.isJsonArray()) {
                    for (JsonElement value : ingredient.getAsJsonArray()) ingredients.add(ingredient(value));
                }
                JsonObject result = entry.getAsJsonObject("result");
                ItemStack output = resolveStack(result);
                if (output.isEmpty() || ingredients.size() != type.getInputCount()) continue;
                output.setCount(result.has("count") ? result.get("count").getAsInt() : 1);
                ResourceLocation id = new ResourceLocation(entry.get("id").getAsString());
                int copyInput = entry.has("copyInput") ? entry.get("copyInput").getAsInt() : -1;
                if (type == WorkstationRecipes.Type.SMITHING && copyInput >= 0
                        && copyInput < ingredients.size()) {
                    ItemStack resultTemplate = output.copy();
                    WorkstationRecipes.register(type, id, ingredients,
                            inputs -> WorkstationRecipes.copySmithingData(
                                    inputs.get(copyInput), resultTemplate),
                            entry.get("processingTime").getAsInt(),
                            entry.has("experience") ? entry.get("experience").getAsFloat() : 0.0F);
                } else {
                    WorkstationRecipes.register(type, id, ingredients, output,
                            entry.get("processingTime").getAsInt(),
                            entry.has("experience") ? entry.get("experience").getAsFloat() : 0.0F);
                }
                if (!group.isEmpty()) WorkstationRecipes.setGroup(type, id, group);
            } catch (RuntimeException exception) {
                LOGGER.error("Skipping invalid workstation recipe entry {}", element, exception);
            }
        }
    }

    private static void loadCompostingConfiguration() {
        JsonObject config = readConfig("composting.json");
        if (config == null) return;
        removeConfigured(config, "remove", false);
        JsonArray rules;
        try {
            rules = config.getAsJsonArray("rules");
        } catch (RuntimeException exception) {
            LOGGER.error("Skipping invalid composting rule list", exception);
            return;
        }
        if (rules == null) return;
        for (JsonElement element : rules) {
            try {
                JsonObject entry = element.getAsJsonObject();
                JsonElement item = entry.get("item");
                Ingredient input = ingredient(item);
                if (input == Ingredient.EMPTY) continue;
                String name = entry.has("id") ? entry.get("id").getAsString()
                        : item.isJsonObject() ? item.getAsJsonObject().get("id").getAsString()
                        : item.getAsString();
                ResourceLocation id = new ResourceLocation(name);
                CompostingRules.register(id, input,
                        entry.get("chance").getAsDouble(), entry.has("villagerCanCompost")
                                && entry.get("villagerCanCompost").getAsBoolean());
            } catch (RuntimeException exception) {
                LOGGER.error("Skipping invalid composting rule {}", element, exception);
            }
        }
    }

    private static void removeConfigured(JsonObject config, String key, boolean recipes) {
        JsonArray entries;
        try {
            entries = config.getAsJsonArray(key);
        } catch (RuntimeException exception) {
            LOGGER.error("Skipping invalid removal list {}", key, exception);
            return;
        }
        if (entries == null) return;
        for (JsonElement element : entries) {
            try {
                JsonObject entry = element.getAsJsonObject();
                ResourceLocation id = new ResourceLocation(entry.get("id").getAsString());
                if (recipes) {
                    WorkstationRecipes.remove(WorkstationRecipes.Type.valueOf(
                            entry.get("type").getAsString().toUpperCase(Locale.ROOT)), id);
                } else {
                    CompostingRules.remove(id);
                }
            } catch (RuntimeException exception) {
                LOGGER.error("Skipping invalid workstation entry removal {}", element, exception);
            }
        }
    }

    @Nullable
    private static JsonObject readConfig(String name) {
        if (configurationDirectory == null) return null;
        File file = new File(configurationDirectory, name);
        try (Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        } catch (Exception exception) {
            LOGGER.error("Skipping invalid workstation configuration {}", file, exception);
            return null;
        }
    }

    private static void createIfMissing(String name, String contents) throws IOException {
        File file = new File(configurationDirectory, name);
        if (!file.exists()) Files.write(file.toPath(), contents.getBytes(StandardCharsets.UTF_8));
    }

    private static void copyExample(String name) throws IOException {
        File file = new File(configurationDirectory, name);
        if (file.exists()) return;
        String path = "/assets/" + FarmerFutureDelight.MODID + "/workstations/" + name;
        try (InputStream input = FFDWorkstationDefaults.class.getResourceAsStream(path)) {
            if (input == null) throw new IOException("Missing workstation example " + path);
            try (FileOutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = input.read(buffer)) != -1) output.write(buffer, 0, length);
            }
        }
    }

    private static Ingredient ingredient(JsonElement element) {
        List<Ingredient> alternatives = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        if (element.isJsonArray()) {
            for (JsonElement value : element.getAsJsonArray()) {
                Ingredient candidate = ingredient(value);
                if (candidate != Ingredient.EMPTY) alternatives.add(candidate);
            }
        } else {
            if (element.isJsonObject() && element.getAsJsonObject().has("ore")) {
                return new net.minecraftforge.oredict.OreIngredient(
                        element.getAsJsonObject().get("ore").getAsString());
            }
            ItemStack stack = element.isJsonObject() ? resolveStack(element.getAsJsonObject())
                    : FFDModernItemLookup.resolve(element.getAsString());
            if (stack.isEmpty()) return Ingredient.EMPTY;
            return element.isJsonObject() && element.getAsJsonObject().has("nbt")
                    ? new net.minecraftforge.common.crafting.IngredientNBT(stack) { }
                    : Ingredient.fromStacks(stack);
        }
        for (Ingredient alternative : alternatives) {
            Collections.addAll(stacks, alternative.getMatchingStacks());
        }
        if (alternatives.isEmpty()) return Ingredient.EMPTY;
        return new Ingredient(stacks.toArray(new ItemStack[0])) {
            @Override
            public boolean apply(@Nullable ItemStack stack) {
                for (Ingredient alternative : alternatives) {
                    if (alternative.apply(stack)) return true;
                }
                return false;
            }

            @Override
            public boolean isSimple() {
                return false;
            }
        };
    }

    private static ItemStack resolveStack(JsonObject object) {
        ItemStack stack = FFDModernItemLookup.resolve(object.get("id").getAsString());
        if (stack.isEmpty()) return stack;
        if (object.has("meta")) stack.setItemDamage(object.get("meta").getAsInt());
        if (object.has("nbt")) {
            try {
                NBTTagCompound tag = JsonToNBT.getTagFromJson(object.get("nbt").getAsString());
                stack.setTagCompound(tag);
            } catch (Exception exception) {
                throw new IllegalArgumentException("Invalid NBT in workstation stack " + object, exception);
            }
        }
        return stack;
    }

    private static JsonArray read(String name) {
        String path = "/assets/" + FarmerFutureDelight.MODID + "/workstations/" + name;
        InputStream stream = FFDWorkstationDefaults.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing workstation defaults: " + path);
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read workstation defaults: " + path, exception);
        }
    }
}
