package xy177.farmersfuturedelight.common.registry;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import xy177.farmersfuturedelight.api.CompostingRules;
import xy177.farmersfuturedelight.api.WorkstationRecipes.Type;

public final class FFDWorkstationScripts {
    private FFDWorkstationScripts() {
    }

    @ZenRegister
    @ZenClass("mods.farmersfuturedelight.Smoker")
    public static final class Smoker {
        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output) {
            addRecipe(input, output, 100, 0.0F);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration) {
            addRecipe(input, output, duration, 0.0F);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration, float experience) {
            FFDWorkstationScriptActions.add(Type.SMOKING, new IIngredient[] {input}, output, duration, experience);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScriptActions.remove(Type.SMOKING, new IIngredient[] {input}, null);
        }

        @ZenMethod
        public static void removeRecipeForInput(IItemStack input) {
            FFDWorkstationScriptActions.remove(Type.SMOKING, new IIngredient[] {input}, null);
        }

        @ZenMethod
        public static void removeRecipeForOutput(IItemStack output) {
            FFDWorkstationScriptActions.remove(Type.SMOKING, null, output);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScriptActions.clear(Type.SMOKING);
        }
    }

    @ZenRegister
    @ZenClass("mods.farmersfuturedelight.BlastFurnace")
    public static final class BlastFurnace {
        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output) {
            addRecipe(input, output, 100, 0.0F);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration) {
            addRecipe(input, output, duration, 0.0F);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration, float experience) {
            FFDWorkstationScriptActions.add(Type.BLASTING, new IIngredient[] {input}, output, duration, experience);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScriptActions.remove(Type.BLASTING, new IIngredient[] {input}, null);
        }

        @ZenMethod
        public static void removeRecipeForInput(IItemStack input) {
            FFDWorkstationScriptActions.remove(Type.BLASTING, new IIngredient[] {input}, null);
        }

        @ZenMethod
        public static void removeRecipeForOutput(IItemStack output) {
            FFDWorkstationScriptActions.remove(Type.BLASTING, null, output);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScriptActions.clear(Type.BLASTING);
        }
    }

    @ZenRegister
    @ZenClass("mods.farmersfuturedelight.Campfire")
    public static final class Campfire {
        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output) {
            addRecipe(input, output, 600, 0.0F);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration) {
            addRecipe(input, output, duration, 0.0F);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration, float experience) {
            FFDWorkstationScriptActions.add(Type.CAMPFIRE_COOKING, new IIngredient[] {input}, output, duration, experience);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScriptActions.remove(Type.CAMPFIRE_COOKING, new IIngredient[] {input}, null);
        }

        @ZenMethod
        public static void removeRecipeForInput(IItemStack input) {
            FFDWorkstationScriptActions.remove(Type.CAMPFIRE_COOKING, new IIngredient[] {input}, null);
        }

        @ZenMethod
        public static void removeRecipeForOutput(IItemStack output) {
            FFDWorkstationScriptActions.remove(Type.CAMPFIRE_COOKING, null, output);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScriptActions.clear(Type.CAMPFIRE_COOKING);
        }
    }

    @ZenRegister
    @ZenClass("mods.farmersfuturedelight.Stonecutter")
    public static final class Stonecutter {
        @ZenMethod
        public static void addOutput(IIngredient input, IItemStack output) {
            FFDWorkstationScriptActions.add(Type.STONECUTTING, new IIngredient[] {input}, output, 1, 0.0F);
        }

        @ZenMethod
        public static void addOutputs(IIngredient input, IItemStack... outputs) {
            for (IItemStack output : outputs) addOutput(input, output);
        }

        @ZenMethod
        public static void removeOutputs(IItemStack input, IItemStack... outputs) {
            for (IItemStack output : outputs) FFDWorkstationScriptActions.remove(Type.STONECUTTING, new IIngredient[] {input}, output);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScriptActions.remove(Type.STONECUTTING, new IIngredient[] {input}, null);
        }

        @ZenMethod
        public static void removeAllOutputsForInput(IItemStack input) {
            removeRecipe(input);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScriptActions.clear(Type.STONECUTTING);
        }
    }

    @ZenRegister
    @ZenClass("mods.farmersfuturedelight.SmithingTable")
    public static final class SmithingTable {
        @ZenMethod
        public static void addRecipe(IIngredient base, IIngredient addition, IItemStack output) {
            addRecipe(null, base, addition, output);
        }

        @ZenMethod
        public static void addRecipe(IIngredient template, IIngredient base, IIngredient addition, IItemStack output) {
            FFDWorkstationScript.addSmithingTransform(FFDWorkstationScriptActions.nextId(), template, FFDWorkstationScriptActions.smithingBase(base), addition, output);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack base, IItemStack addition) {
            removeRecipe(null, base, addition);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack template, IItemStack base, IItemStack addition) {
            FFDWorkstationScriptActions.remove(Type.SMITHING, new IIngredient[] {template, FFDWorkstationScriptActions.smithingBase(base), addition}, null);
        }

        @ZenMethod
        public static void clearDefaults() {
            clearRecipes();
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScriptActions.clear(Type.SMITHING);
        }
    }

    @ZenRegister
    @ZenClass("mods.farmersfuturedelight.Composter")
    public static final class Composter {
        @ZenMethod
        public static void addRecipe(IIngredient input, double chance) {
            addRecipe(input, chance, true);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, double chance, boolean villagerCanCompost) {
            FFDWorkstationScriptActions.compost(input, chance, villagerCanCompost);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScriptActions.removeCompost(input);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScriptActions.apply("Clear composting rules", CompostingRules::clear);
        }

        @ZenMethod
        public static void addValidItem(IIngredient input, int rarity) {
            FFDWorkstationScriptActions.futureCompost(input, rarity);
        }

        @ZenMethod
        public static void removeValidItem(IItemStack input) {
            removeRecipe(input);
        }

        @ZenMethod
        public static void replaceValidItemChance(IIngredient input, int rarity) {
            addValidItem(input, rarity);
        }

        @ZenMethod
        public static void clearValidItems() {
            clearRecipes();
        }
    }

    @ZenRegister
    @ZenClass("mods.farmersfuturedelight.Grindstone")
    public static final class Grindstone {
        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, float experience) {
            FFDWorkstationScriptActions.add(Type.GRINDING_SINGLE, new IIngredient[] {input}, output, 1, experience);
        }

        @ZenMethod
        public static void addRecipe(IIngredient first, IIngredient second, IItemStack output, float experience) {
            FFDWorkstationScriptActions.add(Type.GRINDING, new IIngredient[] {first, second}, output, 1, experience);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScriptActions.remove(Type.GRINDING_SINGLE, new IIngredient[] {input}, null);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack first, IItemStack second) {
            FFDWorkstationScriptActions.remove(Type.GRINDING, new IIngredient[] {first, second}, null);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScriptActions.clear(Type.GRINDING_SINGLE);
            FFDWorkstationScriptActions.clear(Type.GRINDING);
        }
    }
}
