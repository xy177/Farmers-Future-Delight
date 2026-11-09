package xy177.farmersfuturedelight.common.registry;

import crafttweaker.CraftTweakerAPI;
import net.minecraftforge.fml.common.Loader;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

public final class FFDFutureScriptCompat {
    private FFDFutureScriptCompat() {
    }

    public static void register() {
        if (Loader.isModLoaded("futuremc")) return;
        CraftTweakerAPI.registerClass(Smoker.class);
        CraftTweakerAPI.registerClass(BlastFurnace.class);
        CraftTweakerAPI.registerClass(Campfire.class);
        CraftTweakerAPI.registerClass(Stonecutter.class);
        CraftTweakerAPI.registerClass(SmithingTable.class);
        CraftTweakerAPI.registerClass(Composter.class);
    }

    @ZenClass("mods.futuremc.Smoker")
    public static final class Smoker {
        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output) {
            FFDWorkstationScripts.Smoker.addRecipe(input, output);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration) {
            FFDWorkstationScripts.Smoker.addRecipe(input, output, duration);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration, float experience) {
            FFDWorkstationScripts.Smoker.addRecipe(input, output, duration, experience);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScripts.Smoker.removeRecipe(input);
        }

        @ZenMethod
        public static void removeRecipeForInput(IItemStack input) {
            FFDWorkstationScripts.Smoker.removeRecipeForInput(input);
        }

        @ZenMethod
        public static void removeRecipeForOutput(IItemStack output) {
            FFDWorkstationScripts.Smoker.removeRecipeForOutput(output);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScripts.Smoker.clearRecipes();
        }
    }

    @ZenClass("mods.futuremc.BlastFurnace")
    public static final class BlastFurnace {
        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output) {
            FFDWorkstationScripts.BlastFurnace.addRecipe(input, output);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration) {
            FFDWorkstationScripts.BlastFurnace.addRecipe(input, output, duration);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration, float experience) {
            FFDWorkstationScripts.BlastFurnace.addRecipe(input, output, duration, experience);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScripts.BlastFurnace.removeRecipe(input);
        }

        @ZenMethod
        public static void removeRecipeForInput(IItemStack input) {
            FFDWorkstationScripts.BlastFurnace.removeRecipeForInput(input);
        }

        @ZenMethod
        public static void removeRecipeForOutput(IItemStack output) {
            FFDWorkstationScripts.BlastFurnace.removeRecipeForOutput(output);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScripts.BlastFurnace.clearRecipes();
        }
    }

    @ZenClass("mods.futuremc.Campfire")
    public static final class Campfire {
        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output) {
            FFDWorkstationScripts.Campfire.addRecipe(input, output);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration) {
            FFDWorkstationScripts.Campfire.addRecipe(input, output, duration);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, IItemStack output, int duration, float experience) {
            FFDWorkstationScripts.Campfire.addRecipe(input, output, duration, experience);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScripts.Campfire.removeRecipe(input);
        }

        @ZenMethod
        public static void removeRecipeForInput(IItemStack input) {
            FFDWorkstationScripts.Campfire.removeRecipeForInput(input);
        }

        @ZenMethod
        public static void removeRecipeForOutput(IItemStack output) {
            FFDWorkstationScripts.Campfire.removeRecipeForOutput(output);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScripts.Campfire.clearRecipes();
        }
    }

    @ZenClass("mods.futuremc.Stonecutter")
    public static final class Stonecutter {
        @ZenMethod
        public static void addOutput(IIngredient input, IItemStack output) {
            FFDWorkstationScripts.Stonecutter.addOutput(input, output);
        }

        @ZenMethod
        public static void addOutputs(IIngredient input, IItemStack... outputs) {
            FFDWorkstationScripts.Stonecutter.addOutputs(input, outputs);
        }

        @ZenMethod
        public static void removeOutputs(IItemStack input, IItemStack... outputs) {
            FFDWorkstationScripts.Stonecutter.removeOutputs(input, outputs);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScripts.Stonecutter.removeRecipe(input);
        }

        @ZenMethod
        public static void removeAllOutputsForInput(IItemStack input) {
            FFDWorkstationScripts.Stonecutter.removeAllOutputsForInput(input);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScripts.Stonecutter.clearRecipes();
        }
    }

    @ZenClass("mods.futuremc.SmithingTable")
    public static final class SmithingTable {
        @ZenMethod
        public static void addRecipe(IIngredient base, IIngredient addition, IItemStack output) {
            FFDWorkstationScripts.SmithingTable.addRecipe(base, addition, output);
        }

        @ZenMethod
        public static void addRecipe(IIngredient template, IIngredient base, IIngredient addition, IItemStack output) {
            FFDWorkstationScripts.SmithingTable.addRecipe(template, base, addition, output);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack base, IItemStack addition) {
            FFDWorkstationScripts.SmithingTable.removeRecipe(base, addition);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack template, IItemStack base, IItemStack addition) {
            FFDWorkstationScripts.SmithingTable.removeRecipe(template, base, addition);
        }

        @ZenMethod
        public static void clearDefaults() {
            FFDWorkstationScripts.SmithingTable.clearDefaults();
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScripts.SmithingTable.clearRecipes();
        }
    }

    @ZenClass("mods.futuremc.Composter")
    public static final class Composter {
        @ZenMethod
        public static void addRecipe(IIngredient input, double chance) {
            FFDWorkstationScripts.Composter.addRecipe(input, chance);
        }

        @ZenMethod
        public static void addRecipe(IIngredient input, double chance, boolean villagerCanCompost) {
            FFDWorkstationScripts.Composter.addRecipe(input, chance, villagerCanCompost);
        }

        @ZenMethod
        public static void removeRecipe(IItemStack input) {
            FFDWorkstationScripts.Composter.removeRecipe(input);
        }

        @ZenMethod
        public static void clearRecipes() {
            FFDWorkstationScripts.Composter.clearRecipes();
        }

        @ZenMethod
        public static void addValidItem(IIngredient input, int rarity) {
            FFDWorkstationScripts.Composter.addValidItem(input, rarity);
        }

        @ZenMethod
        public static void removeValidItem(IItemStack input) {
            FFDWorkstationScripts.Composter.removeValidItem(input);
        }

        @ZenMethod
        public static void replaceValidItemChance(IIngredient input, int rarity) {
            FFDWorkstationScripts.Composter.replaceValidItemChance(input, rarity);
        }

        @ZenMethod
        public static void clearValidItems() {
            FFDWorkstationScripts.Composter.clearValidItems();
        }
    }
}
