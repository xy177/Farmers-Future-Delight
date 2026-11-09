package xy177.farmersfuturedelight.common.registry;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.mc1120.item.MCItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import xy177.farmersfuturedelight.api.CompostingRules;
import xy177.farmersfuturedelight.api.WorkstationRecipes;

final class FFDWorkstationScriptActions {
    private static final AtomicLong NEXT_ID = new AtomicLong();

    private FFDWorkstationScriptActions() {
    }

    static String nextId() {
        return "farmers_future_delight:script/" + NEXT_ID.incrementAndGet();
    }

    static void apply(String description, Runnable operation) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                FFDWorkstationDefaults.applyScriptAction(operation);
            }

            @Override
            public String describe() {
                return description;
            }
        });
    }

    static void add(WorkstationRecipes.Type type, IIngredient[] inputs, IItemStack output,
                    int time, float experience) {
        FFDWorkstationScript.addRecipe(nextId(), type.name(), inputs, output, time, experience);
    }

    static IIngredient smithingBase(IIngredient base) {
        if (base instanceof IItemStack) {
            IItemStack stack = (IItemStack) base;
            ItemStack internal = (ItemStack) stack.getInternal();
            if (internal.isItemStackDamageable() && internal.getMetadata() == 0) {
                return stack.withDamage(Short.MAX_VALUE);
            }
        }
        return base;
    }

    static void clear(WorkstationRecipes.Type type) {
        apply("Clear " + type + " recipes", () -> WorkstationRecipes.clear(type));
    }

    static void remove(WorkstationRecipes.Type type, IIngredient[] inputs, IItemStack output) {
        Ingredient[] selectors = inputs == null ? null : new Ingredient[inputs.length];
        if (inputs != null) {
            if (inputs.length != type.getInputCount()) throw new IllegalArgumentException("Wrong input count");
            for (int index = 0; index < inputs.length; index++) {
                selectors[index] = FFDWorkstationScript.toIngredient(inputs[index],
                        type == WorkstationRecipes.Type.SMITHING && index != 1);
            }
        }
        apply("Remove matching " + type + " recipes", () -> {
            for (WorkstationRecipes.Recipe recipe : WorkstationRecipes.getRecipes(type)) {
                List<ItemStack> samples = new ArrayList<>();
                boolean matches = true;
                for (int index = 0; index < recipe.getIngredients().size(); index++) {
                    Ingredient ingredient = recipe.getIngredients().get(index);
                    Ingredient selector = selectors == null ? null : selectors[index];
                    ItemStack sample = sample(ingredient, selector);
                    if (selector != null && (ingredient == Ingredient.EMPTY
                            ? selector != Ingredient.EMPTY : sample.isEmpty())) {
                        matches = false;
                        break;
                    }
                    samples.add(sample);
                }
                if (matches && (output == null || output.matches(new MCItemStack(recipe.assemble(samples))))) {
                    WorkstationRecipes.remove(type, recipe.getId());
                }
            }
        });
    }

    private static ItemStack sample(Ingredient ingredient, Ingredient selector) {
        if (ingredient == Ingredient.EMPTY) return ItemStack.EMPTY;
        List<ItemStack> candidates = new ArrayList<>(Arrays.asList(ingredient.getMatchingStacks()));
        if (selector != null) candidates.addAll(Arrays.asList(selector.getMatchingStacks()));
        for (ItemStack candidate : candidates) {
            ItemStack stack = candidate.copy();
            if (stack.getMetadata() == Short.MAX_VALUE) stack.setItemDamage(0);
            if (ingredient.apply(stack) && (selector == null || selector.apply(stack))) return stack;
        }
        return ItemStack.EMPTY;
    }

    static void removeCompost(IIngredient input) {
        Ingredient selector = FFDWorkstationScript.toIngredient(input);
        apply("Remove matching composting rules", () -> {
            for (CompostingRules.Rule rule : CompostingRules.getRules()) {
                if (!sample(rule.getIngredient(), selector).isEmpty()) CompostingRules.remove(rule.getId());
            }
        });
    }

    static void compost(IIngredient input, double chance, boolean villagerCanCompost) {
        if (!Double.isFinite(chance) || chance < 0.0D || chance > 1.0D) {
            throw new IllegalArgumentException("Composting chance must be between 0 and 1");
        }
        FFDWorkstationScript.addCompostingRule(nextId(), input, chance, villagerCanCompost);
    }

    static void futureCompost(IIngredient input, int rarity) {
        if (rarity < 0 || rarity > 100) throw new IllegalArgumentException("Composting rarity must be 0 to 100");
        compost(input, Math.min(100, rarity + 1) / 100.0D, true);
    }
}
