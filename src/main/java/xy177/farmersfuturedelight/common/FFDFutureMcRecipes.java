package xy177.farmersfuturedelight.common;

import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDFutureMcRecipes {
    private static final Logger LOGGER = LogManager.getLogger("Farmer's Future Delight");
    private static final String FUTURE_MC = "futuremc";

    private FFDFutureMcRecipes() {
    }

    public static void register() {
        if (!Loader.isModLoaded(FUTURE_MC)) {
            return;
        }

        try {
            RecipeApi stonecutter = RecipeApi.open(
                    "thedarkcolour.futuremc.recipe.stonecutter.StonecutterRecipes");
            RecipeApi blastFurnace = RecipeApi.open(
                    "thedarkcolour.futuremc.recipe.furnace.BlastFurnaceRecipes");
            int stonecutterRecipes = registerStonecutterRecipes(stonecutter);
            int blastFurnaceRecipes = registerBlastFurnaceRecipes(blastFurnace);
            LOGGER.info("Registered {} Future MC stonecutter recipes and {} blast furnace recipes",
                    stonecutterRecipes, blastFurnaceRecipes);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            LOGGER.warn("Could not register optional Future MC recipes", exception);
        }
    }

    private static int registerStonecutterRecipes(RecipeApi api)
            throws ReflectiveOperationException {
        int added = 0;
        if (FFDItems.isDeepslateEnabled()) {
            Item[] blocks = {
                    FFDItems.COBBLED_DEEPSLATE,
                    FFDItems.POLISHED_DEEPSLATE,
                    FFDItems.DEEPSLATE_BRICKS,
                    FFDItems.DEEPSLATE_TILES
            };
            Item[] stairs = {
                    FFDItems.COBBLED_DEEPSLATE_STAIRS,
                    FFDItems.POLISHED_DEEPSLATE_STAIRS,
                    FFDItems.DEEPSLATE_BRICK_STAIRS,
                    FFDItems.DEEPSLATE_TILE_STAIRS
            };
            Item[] slabs = {
                    FFDItems.COBBLED_DEEPSLATE_SLAB,
                    FFDItems.POLISHED_DEEPSLATE_SLAB,
                    FFDItems.DEEPSLATE_BRICK_SLAB,
                    FFDItems.DEEPSLATE_TILE_SLAB
            };
            Item[] walls = {
                    FFDItems.COBBLED_DEEPSLATE_WALL,
                    FFDItems.POLISHED_DEEPSLATE_WALL,
                    FFDItems.DEEPSLATE_BRICK_WALL,
                    FFDItems.DEEPSLATE_TILE_WALL
            };

            added += addDeepslateConversions(api, FFDItems.DEEPSLATE, 0,
                    blocks, stairs, slabs, walls);
            added += addDeepslateConversions(api, FFDItems.COBBLED_DEEPSLATE, 0,
                    blocks, stairs, slabs, walls);
            added += addDeepslateConversions(api, FFDItems.POLISHED_DEEPSLATE, 1,
                    blocks, stairs, slabs, walls);
            added += addDeepslateConversions(api, FFDItems.DEEPSLATE_BRICKS, 2,
                    blocks, stairs, slabs, walls);
            added += addDeepslateConversions(api, FFDItems.DEEPSLATE_TILES, 3,
                    blocks, stairs, slabs, walls);
            added += addIfLocalOutput(api, FFDItems.DEEPSLATE,
                    FFDItems.CHISELED_DEEPSLATE, 1);
            added += addIfLocalOutput(api, FFDItems.COBBLED_DEEPSLATE,
                    FFDItems.CHISELED_DEEPSLATE, 1);
        }

        if (FFDItems.isCopperEnabled()) {
            added += addCopperConversions(api, FFDItems.COPPER_BLOCK_ITEMS,
                    FFDItems.CUT_COPPER_ITEMS, FFDItems.CUT_COPPER_STAIR_ITEMS,
                    FFDItems.CUT_COPPER_SLAB_ITEMS);
            added += addCopperConversions(api, FFDItems.WAXED_COPPER_BLOCK_ITEMS,
                    FFDItems.WAXED_CUT_COPPER_ITEMS, FFDItems.WAXED_CUT_COPPER_STAIR_ITEMS,
                    FFDItems.WAXED_CUT_COPPER_SLAB_ITEMS);
        }
        return added;
    }

    private static int addDeepslateConversions(RecipeApi api, Item input, int firstFamily,
                                                Item[] blocks, Item[] stairs,
                                                Item[] slabs, Item[] walls)
            throws ReflectiveOperationException {
        int added = 0;
        ItemStack inputStack = FFDItems.effectiveStack(input);
        if (inputStack.isEmpty()) {
            return 0;
        }
        for (int i = firstFamily; i < blocks.length; i++) {
            if (input != blocks[i] && FFDItems.isItemRegistered(blocks[i])) {
                added += api.add(inputStack, new ItemStack(blocks[i]));
            }
            if (FFDItems.isItemRegistered(stairs[i])) {
                added += api.add(inputStack, new ItemStack(stairs[i]));
            }
            if (FFDItems.isItemRegistered(slabs[i])) {
                added += api.add(inputStack, new ItemStack(slabs[i], 2));
            }
            if (FFDItems.isItemRegistered(walls[i])) {
                added += api.add(inputStack, new ItemStack(walls[i]));
            }
        }
        return added;
    }

    private static int addCopperConversions(RecipeApi api, Item[] blocks, Item[] cut,
                                             Item[] stairs, Item[] slabs)
            throws ReflectiveOperationException {
        int added = 0;
        for (int i = 0; i < blocks.length; i++) {
            ItemStack block = FFDItems.effectiveStack(blocks[i]);
            ItemStack cutBlock = FFDItems.effectiveStack(cut[i]);
            if (!block.isEmpty() && FFDItems.isItemRegistered(cut[i])) {
                added += api.add(block, new ItemStack(cut[i], 4));
            }
            if (!block.isEmpty() && FFDItems.isItemRegistered(stairs[i])) {
                added += api.add(block, new ItemStack(stairs[i], 4));
            }
            if (!block.isEmpty() && FFDItems.isItemRegistered(slabs[i])) {
                added += api.add(block, new ItemStack(slabs[i], 8));
            }
            if (!cutBlock.isEmpty() && FFDItems.isItemRegistered(stairs[i])) {
                added += api.add(cutBlock, new ItemStack(stairs[i]));
            }
            if (!cutBlock.isEmpty() && FFDItems.isItemRegistered(slabs[i])) {
                added += api.add(cutBlock, new ItemStack(slabs[i], 2));
            }
        }
        return added;
    }

    private static int registerBlastFurnaceRecipes(RecipeApi api)
            throws ReflectiveOperationException {
        int added = 0;
        if (FFDItems.isDeepslateEnabled()) {
            added += addEffectiveInput(api, FFDItems.DEEPSLATE_COAL_ORE,
                    new ItemStack(Items.COAL));
            added += addEffectiveInput(api, FFDItems.DEEPSLATE_IRON_ORE,
                    new ItemStack(Items.IRON_INGOT));
            added += addEffectiveInput(api, FFDItems.DEEPSLATE_GOLD_ORE,
                    new ItemStack(Items.GOLD_INGOT));
            added += addEffectiveInput(api, FFDItems.DEEPSLATE_REDSTONE_ORE,
                    new ItemStack(Items.REDSTONE));
            added += addEffectiveInput(api, FFDItems.DEEPSLATE_LAPIS_ORE,
                    new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()));
            added += addEffectiveInput(api, FFDItems.DEEPSLATE_DIAMOND_ORE,
                    new ItemStack(Items.DIAMOND));
            added += addEffectiveInput(api, FFDItems.DEEPSLATE_EMERALD_ORE,
                    new ItemStack(Items.EMERALD));
        }
        if (FFDItems.isRawOreEnabled()) {
            added += addEffectiveInput(api, FFDItems.RAW_IRON,
                    new ItemStack(Items.IRON_INGOT));
            added += addEffectiveInput(api, FFDItems.RAW_GOLD,
                    new ItemStack(Items.GOLD_INGOT));
        }
        if (FFDItems.isCopperEnabled() && FFDItems.isItemRegistered(FFDItems.COPPER_INGOT)) {
            ItemStack copperIngot = new ItemStack(FFDItems.COPPER_INGOT);
            added += addEffectiveInput(api, FFDItems.RAW_COPPER, copperIngot);
            added += addEffectiveInput(api, FFDItems.COPPER_ORE, copperIngot);
            if (FFDItems.isDeepslateEnabled()) {
                added += addEffectiveInput(api, FFDItems.DEEPSLATE_COPPER_ORE,
                        copperIngot);
            }
        }
        return added;
    }

    private static int addIfLocalOutput(RecipeApi api, Item input, Item output, int count)
            throws ReflectiveOperationException {
        if (!FFDItems.isItemRegistered(output)) {
            return 0;
        }
        return addEffectiveInput(api, input, new ItemStack(output, count));
    }

    private static int addEffectiveInput(RecipeApi api, Item input, ItemStack output)
            throws ReflectiveOperationException {
        ItemStack effectiveInput = FFDItems.effectiveStack(input);
        return effectiveInput.isEmpty() ? 0 : api.add(effectiveInput, output);
    }

    private static final class RecipeApi {
        private final Object instance;
        private final Method getRecipes;
        private final Method addRecipe;
        private final Method isSameRecipe;
        private final Method getOutput;

        private RecipeApi(Object instance, Method getRecipes, Method addRecipe,
                          Method isSameRecipe, Method getOutput) {
            this.instance = instance;
            this.getRecipes = getRecipes;
            this.addRecipe = addRecipe;
            this.isSameRecipe = isSameRecipe;
            this.getOutput = getOutput;
        }

        private static RecipeApi open(String recipeClassName) throws ReflectiveOperationException {
            Class<?> recipeClass = Class.forName(recipeClassName);
            Class<?> simpleRecipeClass = Class.forName("thedarkcolour.futuremc.recipe.SimpleRecipe");
            Object instance = recipeClass.getField("INSTANCE").get(null);
            return new RecipeApi(instance,
                    recipeClass.getMethod("getRecipes"),
                    recipeClass.getMethod("addRecipe", Ingredient.class, ItemStack.class),
                    simpleRecipeClass.getMethod("isSameRecipe", ItemStack.class, ItemStack.class),
                    simpleRecipeClass.getMethod("getOutput"));
        }

        private int add(ItemStack input, ItemStack output) throws ReflectiveOperationException {
            List<?> recipes = (List<?>) getRecipes.invoke(instance);
            for (Object recipe : recipes) {
                if (Boolean.TRUE.equals(isSameRecipe.invoke(recipe, input, output))) {
                    ItemStack existingOutput = (ItemStack) getOutput.invoke(recipe);
                    if (existingOutput.getCount() == output.getCount()) {
                        return 0;
                    }
                }
            }
            addRecipe.invoke(instance, Ingredient.fromStacks(input.copy()), output.copy());
            return 1;
        }
    }
}
