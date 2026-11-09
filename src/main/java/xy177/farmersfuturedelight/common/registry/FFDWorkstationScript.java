package xy177.farmersfuturedelight.common.registry;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import crafttweaker.mc1120.item.MCItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import xy177.farmersfuturedelight.api.CompostingRules;
import xy177.farmersfuturedelight.api.WorkstationRecipes;

@ZenRegister
@ZenClass("mods.farmersfuturedelight.Workstations")
public final class FFDWorkstationScript {
    private FFDWorkstationScript() {
    }

    @ZenMethod
    public static void addRecipe(String id, String type, IIngredient[] inputs, IItemStack output,
                                 int processingTime, float experience) {
        WorkstationRecipes.Type recipeType = WorkstationRecipes.Type.valueOf(type.toUpperCase(Locale.ROOT));
        List<Ingredient> ingredients = new ArrayList<>();
        for (int index = 0; index < inputs.length; index++) {
            ingredients.add(toIngredient(inputs[index], recipeType == WorkstationRecipes.Type.SMITHING
                    && (index == 0 || index == 2)));
        }
        Object internalOutput = output.getInternal();
        if (!(internalOutput instanceof ItemStack) || ((ItemStack) internalOutput).isEmpty()) {
            throw new IllegalArgumentException("Workstation output must be a valid item stack");
        }
        ResourceLocation recipeId = new ResourceLocation(id);
        ItemStack result = ((ItemStack) internalOutput).copy();
        CraftTweakerAPI.apply(action("Add " + recipeType.name().toLowerCase(Locale.ROOT)
                + " recipe " + recipeId, () -> FFDWorkstationDefaults.applyScriptAction(
                () -> WorkstationRecipes.register(recipeType, recipeId, ingredients, result,
                        processingTime, experience))));
    }

    @ZenMethod
    public static void addSmithingTransform(String id, @Nullable IIngredient template, IIngredient base,
                                           @Nullable IIngredient addition, IItemStack output) {
        Object internalOutput = output.getInternal();
        if (!(internalOutput instanceof ItemStack) || ((ItemStack) internalOutput).isEmpty()) {
            throw new IllegalArgumentException("Smithing output must be a valid item stack");
        }
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(toIngredient(template, true));
        ingredients.add(toIngredient(base));
        ingredients.add(toIngredient(addition, true));
        ItemStack result = ((ItemStack) internalOutput).copy();
        ResourceLocation recipeId = new ResourceLocation(id);
        CraftTweakerAPI.apply(action("Add smithing transform " + recipeId,
                () -> FFDWorkstationDefaults.applyScriptAction(
                        () -> WorkstationRecipes.register(WorkstationRecipes.Type.SMITHING,
                                recipeId, ingredients,
                                inputs -> WorkstationRecipes.copySmithingData(inputs.get(1), result),
                                1, 0.0F))));
    }

    @ZenMethod
    public static void setRecipeGroup(String type, String id, String group) {
        WorkstationRecipes.Type recipeType = WorkstationRecipes.Type.valueOf(type.toUpperCase(Locale.ROOT));
        ResourceLocation recipeId = new ResourceLocation(id);
        CraftTweakerAPI.apply(action("Group workstation recipe " + recipeId,
                () -> FFDWorkstationDefaults.applyScriptAction(
                        () -> WorkstationRecipes.setGroup(recipeType, recipeId, group))));
    }

    @ZenMethod
    public static void removeRecipe(String type, String id) {
        WorkstationRecipes.Type recipeType = WorkstationRecipes.Type.valueOf(type.toUpperCase(Locale.ROOT));
        ResourceLocation recipeId = new ResourceLocation(id);
        CraftTweakerAPI.apply(action("Remove " + recipeType.name().toLowerCase(Locale.ROOT)
                + " recipe " + recipeId, () -> FFDWorkstationDefaults.applyScriptAction(
                () -> WorkstationRecipes.remove(recipeType, recipeId))));
    }

    @ZenMethod
    public static void addCompostingRule(String id, IIngredient input, double chance,
                                         boolean villagerCanCompost) {
        ResourceLocation ruleId = new ResourceLocation(id);
        Ingredient ingredient = toIngredient(input);
        CraftTweakerAPI.apply(action("Add composting rule " + ruleId,
                () -> FFDWorkstationDefaults.applyScriptAction(
                        () -> CompostingRules.register(ruleId, ingredient, chance, villagerCanCompost))));
    }

    @ZenMethod
    public static void removeCompostingRule(String id) {
        ResourceLocation ruleId = new ResourceLocation(id);
        CraftTweakerAPI.apply(action("Remove composting rule " + ruleId,
                () -> FFDWorkstationDefaults.applyScriptAction(() -> CompostingRules.remove(ruleId))));
    }

    static Ingredient toIngredient(IIngredient input) {
        return toIngredient(input, false);
    }

    static Ingredient toIngredient(@Nullable IIngredient input, boolean allowEmpty) {
        if (input == null) {
            if (allowEmpty) return Ingredient.EMPTY;
            throw new IllegalArgumentException("Workstation input must not be null");
        }
        List<ItemStack> alternatives = new ArrayList<>();
        for (IItemStack item : input.getItems()) {
            Object internal = item.getInternal();
            if (internal instanceof ItemStack && !((ItemStack) internal).isEmpty()) {
                alternatives.add(((ItemStack) internal).copy());
            }
        }
        if (alternatives.isEmpty()) throw new IllegalArgumentException("Workstation input is empty");
        return new Ingredient(alternatives.toArray(new ItemStack[0])) {
            @Override
            public boolean apply(@Nullable ItemStack stack) {
                return stack != null && !stack.isEmpty() && input.matches(new MCItemStack(stack));
            }

            @Override
            public boolean isSimple() {
                return false;
            }
        };
    }

    private static IAction action(String description, Runnable operation) {
        return new IAction() {
            @Override
            public void apply() {
                operation.run();
            }

            @Override
            public String describe() {
                return description;
            }
        };
    }
}
