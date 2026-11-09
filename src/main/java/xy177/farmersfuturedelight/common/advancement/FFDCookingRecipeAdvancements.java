package xy177.farmersfuturedelight.common.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.advancements.critereon.ItemPredicates;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.registry.FFDCookingRecipeBook;

public final class FFDCookingRecipeAdvancements {
    private static final Map<WorkstationRecipes.Type, Map<ResourceLocation, RecipeToken>> TOKENS =
            new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RecipeToken> REFERENCES = new ConcurrentHashMap<>();
    private static Set<ResourceLocation> defaults = Collections.emptySet();
    private static final ResourceLocation INGREDIENT = new ResourceLocation(FarmerFutureDelight.MODID,
            "cooking_ingredient");

    private FFDCookingRecipeAdvancements() {
    }

    public static void init() {
        ItemPredicates.register(INGREDIENT, json -> {
            WorkstationRecipes.Type type = WorkstationRecipes.Type.valueOf(json.get("cookingType").getAsString());
            ResourceLocation recipe = new ResourceLocation(json.get("recipe").getAsString());
            if (!FFDCookingRecipeBook.COOKING_TYPES.contains(type)) {
                throw new IllegalArgumentException("Unsupported cooking recipe type " + type);
            }
            return new ItemPredicate() {
                @Override
                public boolean test(ItemStack stack) {
                    WorkstationRecipes.Recipe current = WorkstationRecipes.get(type, recipe);
                    return current != null && !stack.isEmpty() && current.getIngredients().get(0).apply(stack);
                }
            };
        });
    }

    public static ResourceLocation defaultAdvancement(WorkstationRecipes.Type type, ResourceLocation recipe) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "recipes/"
                + type.name().toLowerCase(Locale.ROOT) + "/" + recipe.getResourceDomain() + "/"
                + recipe.getResourcePath());
    }

    public static boolean hasDefault(WorkstationRecipes.Type type, ResourceLocation recipe) {
        return defaults.contains(defaultAdvancement(type, recipe));
    }

    public static void loadAdvancements(AdvancementList list, Map<ResourceLocation, Advancement.Builder> builders) {
        Set<ResourceLocation> present = new HashSet<>();
        for (WorkstationRecipes.Type type : FFDCookingRecipeBook.COOKING_TYPES) {
            for (WorkstationRecipes.Recipe recipe : WorkstationRecipes.getRecipes(type)) {
                ResourceLocation id = defaultAdvancement(type, recipe.getId());
                present.add(id);
                if (builders.containsKey(id)) continue;
                JsonObject json = new JsonObject();
                json.addProperty("parent", "minecraft:recipes/root");
                JsonObject criteria = new JsonObject();
                JsonObject input = new JsonObject();
                input.addProperty("trigger", "minecraft:inventory_changed");
                JsonObject conditions = new JsonObject();
                JsonArray items = new JsonArray();
                JsonObject ingredient = new JsonObject();
                ingredient.addProperty("type", INGREDIENT.toString());
                ingredient.addProperty("cookingType", type.name());
                ingredient.addProperty("recipe", recipe.getId().toString());
                items.add(ingredient);
                conditions.add("items", items);
                input.add("conditions", conditions);
                criteria.add("has_input", input);
                JsonObject unlocked = new JsonObject();
                unlocked.addProperty("trigger", "minecraft:recipe_unlocked");
                JsonObject reference = new JsonObject();
                String alias = token(type, recipe.getId()).name.toString();
                reference.addProperty("recipe", alias);
                unlocked.add("conditions", reference);
                criteria.add("has_the_recipe", unlocked);
                json.add("criteria", criteria);
                JsonArray alternatives = new JsonArray();
                alternatives.add("has_input");
                alternatives.add("has_the_recipe");
                JsonArray requirements = new JsonArray();
                requirements.add(alternatives);
                json.add("requirements", requirements);
                JsonObject rewards = new JsonObject();
                JsonArray recipes = new JsonArray();
                recipes.add(alias);
                rewards.add("recipes", recipes);
                json.add("rewards", rewards);
                builders.put(id, AdvancementManager.GSON.fromJson(json, Advancement.Builder.class));
            }
        }
        defaults = Collections.unmodifiableSet(present);
        list.loadAdvancements(builders);
    }

    @Nullable
    public static IRecipe resolveRecipe(ResourceLocation reference) {
        IRecipe crafting = CraftingManager.getRecipe(reference);
        if (crafting != null) return crafting;
        RecipeToken token = resolveCookingRecipe(reference);
        if (token != null) REFERENCES.put(reference, token);
        return token;
    }

    @Nullable
    private static RecipeToken resolveCookingRecipe(ResourceLocation reference) {
        if (FarmerFutureDelight.MODID.equals(reference.getResourceDomain())
                && reference.getResourcePath().startsWith("cooking/")) {
            String[] parts = reference.getResourcePath().split("/", 4);
            if (parts.length != 4 || parts[2].isEmpty() || parts[3].isEmpty()) return null;
            WorkstationRecipes.Type type;
            if ("smoking".equals(parts[1])) type = WorkstationRecipes.Type.SMOKING;
            else if ("blasting".equals(parts[1])) type = WorkstationRecipes.Type.BLASTING;
            else return null;
            ResourceLocation recipe = new ResourceLocation(parts[2], parts[3]);
            return WorkstationRecipes.get(type, recipe) == null ? null : token(type, recipe);
        }
        boolean smoking = WorkstationRecipes.get(WorkstationRecipes.Type.SMOKING, reference) != null;
        boolean blasting = WorkstationRecipes.get(WorkstationRecipes.Type.BLASTING, reference) != null;
        if (smoking == blasting) return null;
        return token(smoking ? WorkstationRecipes.Type.SMOKING : WorkstationRecipes.Type.BLASTING, reference);
    }

    private static RecipeToken token(WorkstationRecipes.Type type, ResourceLocation recipe) {
        return TOKENS.computeIfAbsent(type, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(recipe, ignored -> new RecipeToken(type, recipe));
    }

    public static void unlockRecipes(EntityPlayerMP player, ResourceLocation[] recipes) {
        List<ResourceLocation> crafting = new ArrayList<>();
        for (ResourceLocation reference : recipes) {
            if (CraftingManager.getRecipe(reference) != null) {
                crafting.add(reference);
                continue;
            }
            RecipeToken token = REFERENCES.get(reference);
            if (token == null) token = resolveCookingRecipe(reference);
            if (token == null) crafting.add(reference);
            else {
                unlockCraftingRecipes(player, crafting);
                FFDCookingRecipeBook.grant(player, token.type, token.recipe);
            }
        }
        unlockCraftingRecipes(player, crafting);
    }

    private static void unlockCraftingRecipes(EntityPlayerMP player, List<ResourceLocation> recipes) {
        if (recipes.isEmpty()) return;
        player.unlockRecipes(recipes.toArray(new ResourceLocation[recipes.size()]));
        recipes.clear();
    }

    public static void trigger(EntityPlayerMP player, WorkstationRecipes.Type type, ResourceLocation recipe) {
        CriteriaTriggers.RECIPE_UNLOCKED.trigger(player, token(type, recipe));
    }

    private static final class RecipeToken implements IRecipe {
        private final WorkstationRecipes.Type type;
        private final ResourceLocation recipe;
        private final ResourceLocation name;

        private RecipeToken(WorkstationRecipes.Type type, ResourceLocation recipe) {
            this.type = type;
            this.recipe = recipe;
            this.name = new ResourceLocation(FarmerFutureDelight.MODID,
                    "cooking/" + type.name().toLowerCase(Locale.ROOT) + "/"
                            + recipe.getResourceDomain() + "/" + recipe.getResourcePath());
        }

        @Override
        public boolean matches(InventoryCrafting inventory, World world) {
            return false;
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inventory) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canFit(int width, int height) {
            return false;
        }

        @Override
        public ItemStack getRecipeOutput() {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        public IRecipe setRegistryName(ResourceLocation name) {
            throw new UnsupportedOperationException("Cooking advancement tokens cannot be registered or renamed");
        }

        @Override
        public ResourceLocation getRegistryName() {
            return name;
        }

        @Override
        public Class<IRecipe> getRegistryType() {
            return IRecipe.class;
        }
    }
}
