package xy177.farmersfuturedelight.common.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDModernVillagerTrades {
    private static final Logger LOGGER = LogManager.getLogger("FFD Villager Trades");
    private static final Map<String, JsonObject> LEVELS = new LinkedHashMap<>();
    public static final String VARIANT_TAG = "farmers_future_delight:villager_variant";
    private static boolean loaded;

    private FFDModernVillagerTrades() {
    }

    public static boolean populate(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return false;
        if (FFDProfessionWorkstations.isProfessionUnassigned(villager)) {
            ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                    new MerchantRecipeList(), "field_70963_i");
            return true;
        }
        if (!canManage(villager)) return false;
        if (!FFDVillagerRestocking.isManaged(villager) && canManage(villager)) {
            MerchantRecipeList existing = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class,
                    villager, "field_70963_i");
            if (existing == null) FFDVillagerRestocking.enable(villager);
        }
        if (!FFDVillagerRestocking.isManaged(villager)) return false;
        FFDVillagerTypes.ensureType(villager);
        load();
        String profession = getModernProfession(villager);
        if (profession == null) return false;
        NBTTagCompound state = FFDVillagerRestocking.getState(villager);
        int level = Math.max(1, Math.min(5, state.getInteger("level")));
        int generated = state.getInteger("generatedLevel");
        MerchantRecipeList recipes = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class,
                villager, "field_70963_i");
        if (recipes == null) {
            recipes = new MerchantRecipeList();
            generated = 0;
            ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager, recipes, "field_70963_i");
        }
        for (int rank = generated + 1; rank <= level; rank++) {
            JsonObject group = LEVELS.get(profession + "/level_" + rank);
            if (group == null) continue;
            List<JsonObject> candidates = new ArrayList<>();
            for (JsonElement element : group.getAsJsonArray("trades")) {
                JsonObject trade = element.getAsJsonObject();
                JsonObject definition = trade.getAsJsonObject("definition");
                if (trade.has("weight") && !definition.has("weight")) {
                    definition.add("weight", trade.get("weight"));
                }
                if (matchesMerchant(villager, definition)) candidates.add(definition);
            }
            candidates = FFDTradeConfiguration.weightedOrder(candidates, villager.getRNG());
            MerchantRecipeList additions = new MerchantRecipeList();
            int amount = group.get("amount").getAsInt();
            for (JsonObject definition : candidates) {
                MerchantRecipe recipe = FFDTradeItemModifiers.create(definition, villager.getRNG(), true, villager);
                if (recipe == null) continue;
                additions.add(recipe);
                if (additions.size() >= amount) break;
            }
            FFDTradeConfiguration.applyNewLevel(villager, additions, rank);
            recipes.addAll(additions);
        }
        state.setInteger("level", level);
        state.setInteger("generatedLevel", level);
        state.setString("profileKey", FFDTradeConfiguration.getVillagerProfileKey(villager));
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager, level, "field_175562_bw");
        villager.getEntityData().setBoolean(FFDTradeConfiguration.getAppliedTag(), true);
        FFDVillagerRestocking.wrapOffers(villager, recipes);
        return true;
    }

    public static void refreshUntradedProfile(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return;
        if (!canManage(villager) || !FFDVillagerRestocking.isManaged(villager)
                || !FFDTradeConfiguration.shouldUpdateExistingUntradedVillagers()) return;
        NBTTagCompound state = FFDVillagerRestocking.getState(villager);
        if (state.getBoolean("hasTraded") || state.getInteger("experience") != 0) return;
        String key = FFDTradeConfiguration.getVillagerProfileKey(villager);
        if (key.equals(state.getString("profileKey"))) return;
        MerchantRecipeList recipes = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class,
                villager, "field_70963_i");
        if (recipes == null) return;
        for (MerchantRecipe recipe : recipes) {
            if (recipe.getToolUses() != 0 || !(recipe instanceof FFDModernMerchantRecipe)) return;
        }
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager, null, "field_70963_i");
        state.setInteger("generatedLevel", 0);
        populate(villager);
    }

    public static boolean canManage(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem || villager.getClass() != EntityVillager.class || villager.world.isRemote
                || FFDProfessionWorkstations.isProfessionUnassigned(villager)) return false;
        VillagerProfession profession = villager.getProfessionForge();
        if (profession == null || profession.getRegistryName() == null) return false;
        ResourceLocation id = profession.getRegistryName();
        if (FFDVillagerTypes.MASON.equals(id)) return true;
        if (!"minecraft".equals(id.getResourceDomain()) || "nitwit".equals(id.getResourcePath())) return false;
        String career = FFDProfessionWorkstations.getCareerName(villager);
        if (career == null) return true;
        switch (career) {
            case "farmer":
            case "fisherman":
            case "shepherd":
            case "fletcher":
            case "librarian":
            case "cartographer":
            case "cleric":
            case "armor":
            case "weapon":
            case "tool":
            case "butcher":
            case "leather": return true;
            default: return false;
        }
    }

    private static boolean matchesMerchant(EntityVillager villager, JsonObject definition) {
        if (!definition.has("merchant_predicate")) return true;
        String variant = villager.getEntityData().getString(VARIANT_TAG);
        if (variant.isEmpty()) return false;
        JsonObject condition = definition.getAsJsonObject("merchant_predicate");
        if (!"minecraft:entity_properties".equals(condition.get("type").getAsString())) return false;
        JsonObject predicates = condition.getAsJsonObject("predicate").getAsJsonObject("minecraft:predicates");
        if (predicates == null || !predicates.has("minecraft:villager/variant")) return false;
        JsonElement variants = predicates.get("minecraft:villager/variant");
        if (variants.isJsonPrimitive()) return variants.getAsString().equals("minecraft:" + variant);
        for (JsonElement candidate : variants.getAsJsonArray()) {
            if (candidate.getAsString().equals("minecraft:" + variant)) return true;
        }
        return false;
    }

    @Nullable
    public static String getModernProfession(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return null;
        VillagerProfession profession = villager.getProfessionForge();
        if (profession == null || profession.getRegistryName() == null) return null;
        ResourceLocation id = profession.getRegistryName();
        String career = FFDProfessionWorkstations.getCareerName(villager);
        if (career == null) {
            int careerIndex = profession.getRandomCareer(villager.getRNG());
            ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                    careerIndex + 1, "field_175563_bv");
            career = profession.getCareer(careerIndex).getName();
        }
        if ("farmers_future_delight:mason".equals(id.toString())) return "mason";
        if (!"minecraft".equals(id.getResourceDomain())) return null;
        switch (career) {
            case "armor": return "armorer";
            case "weapon": return "weaponsmith";
            case "tool": return "toolsmith";
            case "leather": return "leatherworker";
            case "farmer":
            case "fisherman":
            case "shepherd":
            case "fletcher":
            case "librarian":
            case "cartographer":
            case "cleric":
            case "butcher": return career;
            default: return null;
        }
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        try (InputStream stream = FFDModernVillagerTrades.class.getResourceAsStream(
                "/assets/farmers_future_delight/trades/villager_defaults.json")) {
            if (stream == null) throw new IllegalStateException("Missing villager trade defaults");
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement element : root.getAsJsonArray("groups")) {
                JsonObject group = element.getAsJsonObject();
                LEVELS.put(group.get("id").getAsString(), group);
            }
        } catch (Exception exception) {
            LOGGER.error("Cannot load modern villager trade defaults", exception);
        }
    }

    public static Map<String, JsonObject> getDisplayGroups() {
        load();
        Map<String, JsonObject> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonObject> entry : LEVELS.entrySet()) {
            result.put(entry.getKey(), new JsonParser().parse(entry.getValue().toString()).getAsJsonObject());
        }
        return Collections.unmodifiableMap(result);
    }
}
