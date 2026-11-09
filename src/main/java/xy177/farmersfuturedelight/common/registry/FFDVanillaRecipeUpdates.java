package xy177.farmersfuturedelight.common.registry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeFireworks;
import net.minecraft.item.crafting.RecipeRepairItem;
import net.minecraft.item.crafting.RecipesBanners;
import net.minecraft.item.crafting.ShulkerBoxRecipes;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.oredict.OreDictionary;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDVanillaRecipeUpdates {
    private FFDVanillaRecipeUpdates() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void register(RegistryEvent.Register<IRecipe> event) {
        ItemStack bowl = new ItemStack(Items.BOWL);
        ItemStack rabbit = new ItemStack(Items.COOKED_RABBIT);
        ItemStack carrot = new ItemStack(Items.CARROT);
        ItemStack potato = new ItemStack(Items.BAKED_POTATO);
        ItemStack[] mushrooms = {new ItemStack(Blocks.BROWN_MUSHROOM), new ItemStack(Blocks.RED_MUSHROOM)};
        String[] names = {"brown", "red"};
        for (int index = 0; index < mushrooms.length; index++) {
            ItemStack output = new ItemStack(Items.RABBIT_STEW);
            ShapedRecipes original = new ShapedRecipes("rabbit_stew", 3, 3,
                    ingredients(ItemStack.EMPTY, rabbit, ItemStack.EMPTY,
                            carrot, potato, mushrooms[index], ItemStack.EMPTY, bowl, ItemStack.EMPTY), output);
            ShapelessRecipes replacement = new ShapelessRecipes("rabbit_stew", output,
                    ingredients(potato, rabbit, bowl, carrot, mushrooms[index]));
            replaceUnchanged(event.getRegistry(), "rabbit_stew_from_" + names[index] + "_mushroom",
                    original, replacement);
        }
        ItemStack beetroot = new ItemStack(Items.BEETROOT);
        ItemStack soup = new ItemStack(Items.BEETROOT_SOUP);
        replaceUnchanged(event.getRegistry(), "beetroot_soup",
                shaped(3, 3, soup, beetroot, beetroot, beetroot, beetroot, beetroot, beetroot,
                        ItemStack.EMPTY, bowl, ItemStack.EMPTY),
                new ShapelessRecipes("", soup,
                        ingredients(bowl, beetroot, beetroot, beetroot, beetroot, beetroot, beetroot)));
        ItemStack brick = new ItemStack(Blocks.NETHER_BRICK);
        ItemStack fence = new ItemStack(Blocks.NETHER_BRICK_FENCE, 6);
        replaceUnchanged(event.getRegistry(), "nether_brick_fence",
                shaped(3, 2, fence, brick, brick, brick, brick, brick, brick),
                shaped(3, 2, fence, brick, new ItemStack(Items.NETHERBRICK), brick,
                        brick, new ItemStack(Items.NETHERBRICK), brick));
        if (FFDItems.isLocalCrossbowEnabled()) {
            event.getRegistry().register(shaped(3, 3, new ItemStack(FFDItems.CROSSBOW),
                    new ItemStack(Items.STICK), new ItemStack(Item.getItemFromBlock(Blocks.TRIPWIRE_HOOK)),
                    new ItemStack(Items.STICK), new ItemStack(Items.STRING),
                    new ItemStack(Items.IRON_INGOT), new ItemStack(Items.STRING),
                    ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY)
                    .setRegistryName(FarmerFutureDelight.MODID, "crossbow"));
        }
        updateSeparatedDyeRecipes(event.getRegistry());
        updateStoneDecorRecipes(event.getRegistry());
        updateSignRecipe(event.getRegistry());
        ResourceLocation dyeId = new ResourceLocation("minecraft", "armordye");
        IRecipe armorDye = event.getRegistry().getValue(dyeId);
        if (armorDye != null && armorDye.getClass() == net.minecraft.item.crafting.RecipesArmorDyes.class) {
            event.getRegistry().register(new FFDRecipeArmorDyes(false).setRegistryName(dyeId));
        }
        ResourceLocation repairId = new ResourceLocation("minecraft", "repairitem");
        IRecipe repair = event.getRegistry().getValue(repairId);
        if (repair != null && repair.getClass() == RecipeRepairItem.class) {
            event.getRegistry().register(new FFDRecipeRepairItem().setRegistryName(repairId));
        }
        replaceLegacyDyeRecipe(event.getRegistry(), "banneraddpattern", RecipesBanners.RecipeAddPattern.class);
        replaceLegacyDyeRecipe(event.getRegistry(), "shulkerboxcoloring",
                ShulkerBoxRecipes.ShulkerBoxColoring.class);
        replaceLegacyDyeRecipe(event.getRegistry(), "fireworks", RecipeFireworks.class);
    }

    private static void replaceLegacyDyeRecipe(IForgeRegistry<IRecipe> registry, String path,
                                               Class<? extends IRecipe> expectedClass) {
        ResourceLocation id = new ResourceLocation("minecraft", path);
        IRecipe recipe = registry.getValue(id);
        if (recipe != null && recipe.getClass() == expectedClass) {
            registry.register(new FFDRecipeLegacyDyeGate(recipe).setRegistryName(id));
        }
    }

    private static void updateStoneDecorRecipes(IForgeRegistry<IRecipe> registry) {
        if (FFDConfig.stoneDecorMode == FFDConfig.FeatureMode.DISABLED) {
            return;
        }
        narrowStoneRecipe(registry, "sandstone_slab", Blocks.SANDSTONE, 3,
                new ItemStack(Blocks.STONE_SLAB, 6, 1), false, 0, 1);
        narrowStoneRecipe(registry, "red_sandstone_slab", Blocks.RED_SANDSTONE, 3,
                new ItemStack(Blocks.STONE_SLAB2, 6), false, 0, 1);
        narrowStoneRecipe(registry, "stone_brick_slab", Blocks.STONEBRICK, 4,
                new ItemStack(Blocks.STONE_SLAB, 6, 5), false, 0);
        narrowStoneRecipe(registry, "stone_brick_stairs", Blocks.STONEBRICK, 4,
                new ItemStack(Blocks.STONE_BRICK_STAIRS, 4), true, 0);
    }

    private static void updateSignRecipe(IForgeRegistry<IRecipe> registry) {
        if (!FFDItems.isStrippedWoodEnabled()) {
            return;
        }
        ResourceLocation id = new ResourceLocation("minecraft", "sign");
        IRecipe recipe = registry.getValue(id);
        if (recipe == null || recipe.getClass() != ShapedRecipes.class) {
            return;
        }
        ShapedRecipes original = (ShapedRecipes) recipe;
        if (original.recipeWidth != 3 || original.recipeHeight != 3
                || original.getRecipeOutput().getItem() != Items.SIGN
                || original.getRecipeOutput().getCount() != 3) {
            return;
        }
        for (int slot = 0; slot < 9; slot++) {
            Ingredient ingredient = original.recipeItems.get(slot);
            if (slot < 6) {
                if (!isPlankIngredient(ingredient)) return;
            } else if (slot == 7) {
                if (!isSingleItemIngredient(ingredient, Items.STICK)) return;
            } else if (ingredient.getMatchingStacks().length != 0) {
                return;
            }
        }
        ItemStack oakPlanks = new ItemStack(Blocks.PLANKS, 1, 0);
        NonNullList<Ingredient> oakIngredients = ingredients(
                oakPlanks, oakPlanks, oakPlanks,
                oakPlanks, oakPlanks, oakPlanks,
                ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY);
        registry.register(new ShapedRecipes(original.getGroup(), 3, 3, oakIngredients,
                original.getRecipeOutput()).setRegistryName(id));
    }

    private static boolean isPlankIngredient(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        if (stacks.length == 0) return false;
        Item plankItem = Item.getItemFromBlock(Blocks.PLANKS);
        for (ItemStack stack : stacks) {
            int metadata = stack.getMetadata();
            if (stack.getItem() != plankItem
                    || (metadata != OreDictionary.WILDCARD_VALUE && (metadata < 0 || metadata > 5))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSingleItemIngredient(Ingredient ingredient, Item item) {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        return stacks.length == 1 && stacks[0].getItem() == item && stacks[0].getMetadata() == 0;
    }

    private static void narrowStoneRecipe(IForgeRegistry<IRecipe> registry, String path,
                                           net.minecraft.block.Block material, int originalVariants,
                                           ItemStack output, boolean stairs, int... variants) {
        ItemStack[] originalStacks = new ItemStack[originalVariants];
        for (int index = 0; index < originalVariants; index++) {
            originalStacks[index] = new ItemStack(material, 1, index);
        }
        ItemStack[] newStacks = new ItemStack[variants.length];
        for (int index = 0; index < variants.length; index++) {
            newStacks[index] = new ItemStack(material, 1, variants[index]);
        }
        NonNullList<Ingredient> original = NonNullList.create();
        NonNullList<Ingredient> replacement = NonNullList.create();
        int[] slots = stairs ? new int[] {0, 3, 4, 6, 7, 8} : new int[] {0, 1, 2};
        int size = stairs ? 9 : 3;
        for (int index = 0; index < size; index++) {
            boolean occupied = false;
            for (int slot : slots) occupied |= slot == index;
            original.add(occupied ? Ingredient.fromStacks(originalStacks) : Ingredient.EMPTY);
            replacement.add(occupied ? Ingredient.fromStacks(newStacks) : Ingredient.EMPTY);
        }
        replaceUnchanged(registry, path,
                new ShapedRecipes("", 3, stairs ? 3 : 1, original, output),
                new ShapedRecipes("", 3, stairs ? 3 : 1, replacement, output));
    }

    private static void updateSeparatedDyeRecipes(IForgeRegistry<IRecipe> registry) {
        updateDyeColorRecipes(registry, "black", EnumDyeColor.BLACK, FFDItems.BLACK_DYE);
        updateDyeColorRecipes(registry, "blue", EnumDyeColor.BLUE, FFDItems.BLUE_DYE);
        updateDyeColorRecipes(registry, "brown", EnumDyeColor.BROWN, FFDItems.BROWN_DYE);
        updateDyeColorRecipes(registry, "white", EnumDyeColor.WHITE, FFDItems.WHITE_DYE);
        updateCarpetDyeRecipe(registry, "black", EnumDyeColor.BLACK, FFDItems.BLACK_DYE);
        updateCarpetDyeRecipe(registry, "blue", EnumDyeColor.BLUE, FFDItems.BLUE_DYE);
        updateCarpetDyeRecipe(registry, "brown", EnumDyeColor.BROWN, FFDItems.BROWN_DYE);
        updateCarpetDyeRecipe(registry, "white", EnumDyeColor.WHITE, FFDItems.WHITE_DYE);
        for (EnumDyeColor color : EnumDyeColor.values()) {
            if (color != EnumDyeColor.BLACK && color != EnumDyeColor.BLUE
                    && color != EnumDyeColor.BROWN && color != EnumDyeColor.WHITE) {
                updateLegacyCarpetDyeRecipe(registry, color);
            }
        }
        updateDarkPrismarineRecipe(registry);
        updateDyeMixRecipes(registry);
        restoreDyeMaterialRecipe(registry, "writable_book",
                shapeless("", new ItemStack(Items.WRITABLE_BOOK),
                        new ItemStack(Items.BOOK), legacyDye(EnumDyeColor.BLACK),
                        new ItemStack(Items.FEATHER)), 1);
        restoreDyeMaterialRecipe(registry, "cookie",
                shaped(3, 1, new ItemStack(Items.COOKIE, 8),
                        new ItemStack(Items.WHEAT), legacyDye(EnumDyeColor.BROWN),
                        new ItemStack(Items.WHEAT)), 1);
        ItemStack[] lapis = new ItemStack[9];
        ItemStack[] boneMeal = new ItemStack[9];
        for (int index = 0; index < 9; index++) {
            lapis[index] = legacyDye(EnumDyeColor.BLUE);
            boneMeal[index] = legacyDye(EnumDyeColor.WHITE);
        }
        restoreDyeMaterialRecipe(registry, "lapis_block",
                shaped(3, 3, new ItemStack(Blocks.LAPIS_BLOCK), lapis), 0, 1, 2, 3, 4, 5, 6, 7, 8);
        restoreDyeMaterialRecipe(registry, "bone_block",
                shaped(3, 3, new ItemStack(Blocks.BONE_BLOCK), boneMeal),
                0, 1, 2, 3, 4, 5, 6, 7, 8);
    }

    private static void updateCarpetDyeRecipe(IForgeRegistry<IRecipe> registry, String colorName,
                                               EnumDyeColor color, Item dyeItem) {
        registerCarpetDyeRecipe(registry, colorName, color,
                separatedDyeIngredient(color, dyeItem));
    }

    private static void updateLegacyCarpetDyeRecipe(IForgeRegistry<IRecipe> registry,
                                                     EnumDyeColor color) {
        registerCarpetDyeRecipe(registry, color.getName(), color,
                Ingredient.fromStacks(legacyDye(color)));
    }

    private static void registerCarpetDyeRecipe(IForgeRegistry<IRecipe> registry, String colorName,
                                                 EnumDyeColor color, Ingredient dyeIngredient) {
        ItemStack[] carpetColors = new ItemStack[EnumDyeColor.values().length];
        for (EnumDyeColor carpetColor : EnumDyeColor.values()) {
            carpetColors[carpetColor.getMetadata()] = new ItemStack(
                    Blocks.CARPET, 1, carpetColor.getMetadata());
        }
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.fromStacks(carpetColors));
        ingredients.add(dyeIngredient);
        ShapelessRecipes recipe = new ShapelessRecipes("carpet_dye",
                new ItemStack(Blocks.CARPET, 1, color.getMetadata()), ingredients);
        registry.register(recipe.setRegistryName(FarmerFutureDelight.MODID,
                "carpet_dye_" + colorName));
    }

    private static void updateDarkPrismarineRecipe(IForgeRegistry<IRecipe> registry) {
        ItemStack[] inputs = repeated(9, new ItemStack(Items.PRISMARINE_SHARD));
        inputs[4] = legacyDye(EnumDyeColor.BLACK);
        ShapedRecipes expected = shaped("", 3, 3,
                new ItemStack(Blocks.PRISMARINE, 1, 2), inputs);
        replaceDyeRecipe(registry, "dark_prismarine", expected,
                replaceIngredient(expected, 4,
                        separatedDyeIngredient(EnumDyeColor.BLACK, FFDItems.BLACK_DYE)));
    }

    private static void updateDyeColorRecipes(IForgeRegistry<IRecipe> registry, String color,
                                               EnumDyeColor dyeColor, Item dyeItem) {
        Ingredient dye = separatedDyeIngredient(dyeColor, dyeItem);
        Ingredient legacy = legacyIngredient(dyeColor);
        ItemStack stainedGlass = new ItemStack(Blocks.STAINED_GLASS, 8, dyeColor.getMetadata());
        ItemStack stainedClay = new ItemStack(Blocks.STAINED_HARDENED_CLAY, 8,
                dyeColor.getMetadata());
        ItemStack concrete = new ItemStack(Blocks.CONCRETE_POWDER, 8, dyeColor.getMetadata());
        ItemStack glass = new ItemStack(Blocks.GLASS);
        ItemStack clay = new ItemStack(Blocks.HARDENED_CLAY);
        ItemStack[] glassInputs = repeated(9, glass);
        ItemStack[] clayInputs = repeated(9, clay);
        glassInputs[4] = legacyDye(dyeColor);
        clayInputs[4] = legacyDye(dyeColor);
        IRecipe expectedGlass = shaped("stained_glass", 3, 3, stainedGlass, glassInputs);
        IRecipe expectedClay = shaped("stained_hardened_clay", 3, 3, stainedClay, clayInputs);
        replaceDyeRecipe(registry, color + "_stained_glass", expectedGlass,
                replaceIngredient((ShapedRecipes) expectedGlass, 4, dye));
        replaceDyeRecipe(registry, color + "_stained_hardened_clay", expectedClay,
                replaceIngredient((ShapedRecipes) expectedClay, 4, dye));
        IRecipe expectedConcrete = shapeless("concrete_powder", concrete,
                legacyDye(dyeColor), new ItemStack(Blocks.SAND), new ItemStack(Blocks.SAND),
                new ItemStack(Blocks.SAND), new ItemStack(Blocks.SAND),
                new ItemStack(Blocks.GRAVEL), new ItemStack(Blocks.GRAVEL),
                new ItemStack(Blocks.GRAVEL), new ItemStack(Blocks.GRAVEL));
        replaceDyeRecipe(registry, color + "_concrete_powder", expectedConcrete,
                replaceIngredient((ShapelessRecipes) expectedConcrete, 0, dye));
        if (dyeColor != EnumDyeColor.WHITE) {
            IRecipe expectedBed = shapeless("dyed_bed", new ItemStack(Items.BED, 1,
                    dyeColor.getMetadata()), new ItemStack(Items.BED), legacyDye(dyeColor));
            replaceDyeRecipe(registry, color + "_bed_from_white_bed", expectedBed,
                    replaceIngredient((ShapelessRecipes) expectedBed, 1, dye));
            IRecipe expectedWool = shapeless("wool", new ItemStack(Blocks.WOOL, 1,
                    dyeColor.getMetadata()), legacyDye(dyeColor), new ItemStack(Blocks.WOOL));
            replaceDyeRecipe(registry, color + "_wool", expectedWool,
                    replaceIngredient((ShapelessRecipes) expectedWool, 0, dye));
        }
    }

    private static void updateDyeMixRecipes(IForgeRegistry<IRecipe> registry) {
        replaceDyeMix(registry, "gray_dye", 8, 2,
                new ItemStack(Items.DYE, 1, 0), new ItemStack(Items.DYE, 1, 15));
        replaceDyeMix(registry, "purple_dye", 5, 2,
                new ItemStack(Items.DYE, 1, 4), new ItemStack(Items.DYE, 1, 1));
        replaceDyeMix(registry, "cyan_dye", 6, 2,
                new ItemStack(Items.DYE, 1, 4), new ItemStack(Items.DYE, 1, 2));
        replaceDyeMix(registry, "light_blue_dye_from_lapis_bonemeal", 12, 2,
                new ItemStack(Items.DYE, 1, 4), new ItemStack(Items.DYE, 1, 15));
        replaceDyeMix(registry, "light_gray_dye_from_ink_bonemeal", 7, 3,
                new ItemStack(Items.DYE, 1, 0), new ItemStack(Items.DYE, 1, 15),
                new ItemStack(Items.DYE, 1, 15));
        replaceDyeMix(registry, "light_gray_dye_from_gray_bonemeal", 7, 2,
                new ItemStack(Items.DYE, 1, 8), new ItemStack(Items.DYE, 1, 15));
        replaceDyeMix(registry, "lime_dye", 10, 2,
                new ItemStack(Items.DYE, 1, 2), new ItemStack(Items.DYE, 1, 15));
        replaceDyeMix(registry, "magenta_dye_from_lapis_ink_bonemeal", 13, 4,
                new ItemStack(Items.DYE, 1, 4), new ItemStack(Items.DYE, 1, 1),
                new ItemStack(Items.DYE, 1, 1), new ItemStack(Items.DYE, 1, 15));
        replaceDyeMix(registry, "magenta_dye_from_lapis_red_pink", 13, 3,
                new ItemStack(Items.DYE, 1, 4), new ItemStack(Items.DYE, 1, 1),
                new ItemStack(Items.DYE, 1, 9));
        replaceDyeMix(registry, "pink_dye_from_red_bonemeal", 9, 2,
                new ItemStack(Items.DYE, 1, 1), new ItemStack(Items.DYE, 1, 15));
    }

    private static void replaceDyeMix(IForgeRegistry<IRecipe> registry, String path,
                                      int outputColor, int outputCount, ItemStack... inputs) {
        int separator = path.indexOf("_from");
        String group = separator < 0 ? "" : path.substring(0, separator);
        IRecipe expected = shapeless(group, new ItemStack(Items.DYE, outputCount, outputColor), inputs);
        NonNullList<Ingredient> updated = NonNullList.create();
        for (ItemStack input : inputs) {
            ItemStack separated = separatedDyeForLegacy(input);
            updated.add(separated.isEmpty() ? Ingredient.fromStacks(input)
                    : separatedDyeIngredient(input.getMetadata(), separated.getItem()));
        }
        IRecipe replacement = new ShapelessRecipes(expected.getGroup(), expected.getRecipeOutput(), updated);
        replaceDyeRecipe(registry, path, expected, replacement);
    }

    private static void restoreDyeMaterialRecipe(IForgeRegistry<IRecipe> registry, String path,
                                                  IRecipe expected, int... dyeSlots) {
        NonNullList<Ingredient> updated = NonNullList.create();
        updated.addAll(expected.getIngredients());
        for (int slot : dyeSlots) {
            ItemStack input = expected.getIngredients().get(slot).getMatchingStacks()[0];
            updated.set(slot, Ingredient.fromStacks(input));
        }
        IRecipe replacement;
        if (expected.getClass() == ShapedRecipes.class) {
            ShapedRecipes shaped = (ShapedRecipes) expected;
            replacement = new ShapedRecipes(shaped.getGroup(), shaped.recipeWidth,
                    shaped.recipeHeight, updated, shaped.getRecipeOutput());
        } else {
            replacement = new ShapelessRecipes(expected.getGroup(), expected.getRecipeOutput(), updated);
        }
        replaceDyeRecipe(registry, path, expected, replacement);
    }

    private static void replaceDyeRecipe(IForgeRegistry<IRecipe> registry, String path,
                                         IRecipe expected, IRecipe replacement) {
        ResourceLocation id = new ResourceLocation("minecraft", path);
        IRecipe existing = registry.getValue(id);
        if (!sameRecipe(existing, expected)) {
            return;
        }
        registry.register(replacement.setRegistryName(id));
    }

    private static boolean sameRecipe(IRecipe existing, IRecipe expected) {
        if (existing == null || existing.getClass() != expected.getClass()
                || !ItemStack.areItemStacksEqual(existing.getRecipeOutput(), expected.getRecipeOutput())
                || !existing.getGroup().equals(expected.getGroup())
                || existing.getIngredients().size() != expected.getIngredients().size()) {
            return false;
        }
        if (existing instanceof ShapedRecipes) {
            ShapedRecipes actual = (ShapedRecipes) existing;
            ShapedRecipes original = (ShapedRecipes) expected;
            if (actual.recipeWidth != original.recipeWidth || actual.recipeHeight != original.recipeHeight) {
                return false;
            }
        }
        for (int index = 0; index < expected.getIngredients().size(); index++) {
            ItemStack[] actual = existing.getIngredients().get(index).getMatchingStacks();
            ItemStack[] original = expected.getIngredients().get(index).getMatchingStacks();
            for (ItemStack candidate : actual) {
                boolean allowed = false;
                for (ItemStack stack : original) {
                    if (ItemStack.areItemStacksEqual(stack, candidate)) {
                        allowed = true;
                        break;
                    }
                    ItemStack separated = separatedDyeForLegacy(stack);
                    if (!separated.isEmpty() && ItemStack.areItemStacksEqual(separated, candidate)) {
                        allowed = true;
                        break;
                    }
                }
                if (!allowed) return false;
            }
            for (ItemStack stack : original) {
                boolean found = false;
                for (ItemStack candidate : actual) {
                    if (ItemStack.areItemStacksEqual(stack, candidate)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
        }
        return true;
    }

    private static IRecipe replaceIngredient(ShapedRecipes original, int slot, Ingredient ingredient) {
        NonNullList<Ingredient> inputs = NonNullList.create();
        inputs.addAll(original.recipeItems);
        inputs.set(slot, ingredient);
        return new ShapedRecipes(original.getGroup(), original.recipeWidth, original.recipeHeight,
                inputs, original.getRecipeOutput());
    }

    private static IRecipe replaceIngredient(ShapelessRecipes original, int slot,
                                             Ingredient ingredient) {
        NonNullList<Ingredient> inputs = NonNullList.create();
        inputs.addAll(original.getIngredients());
        inputs.set(slot, ingredient);
        return new ShapelessRecipes(original.getGroup(), original.getRecipeOutput(), inputs);
    }

    private static IRecipe shapeless(String group, ItemStack output, ItemStack... inputs) {
        return new ShapelessRecipes(group, output, ingredients(inputs));
    }

    private static ShapedRecipes shaped(String group, int width, int height, ItemStack output,
                                        ItemStack... inputs) {
        return new ShapedRecipes(group, width, height, ingredients(inputs), output);
    }

    private static Ingredient separatedDyeIngredient(EnumDyeColor color, Item dyeItem) {
        return separatedDyeIngredient(color.getDyeDamage(), dyeItem);
    }

    private static Ingredient separatedDyeIngredient(int legacyMetadata, Item dyeItem) {
        if (FFDConfig.legacyDyeCompatibility) {
            return Ingredient.fromStacks(new ItemStack(dyeItem),
                    new ItemStack(Items.DYE, 1, legacyMetadata));
        }
        return Ingredient.fromStacks(new ItemStack(dyeItem));
    }

    private static Ingredient legacyIngredient(EnumDyeColor color) {
        return Ingredient.fromStacks(legacyDye(color));
    }

    private static ItemStack legacyDye(EnumDyeColor color) {
        return new ItemStack(Items.DYE, 1, color.getDyeDamage());
    }

    private static ItemStack[] repeated(int count, ItemStack stack) {
        ItemStack[] result = new ItemStack[count];
        for (int index = 0; index < count; index++) result[index] = stack.copy();
        return result;
    }

    private static ItemStack separatedDyeForLegacy(ItemStack legacy) {
        if (legacy.getItem() != Items.DYE) return ItemStack.EMPTY;
        int metadata = legacy.getMetadata();
        if (metadata == EnumDyeColor.BLACK.getDyeDamage()) return new ItemStack(FFDItems.BLACK_DYE);
        if (metadata == EnumDyeColor.BLUE.getDyeDamage()) return new ItemStack(FFDItems.BLUE_DYE);
        if (metadata == EnumDyeColor.BROWN.getDyeDamage()) return new ItemStack(FFDItems.BROWN_DYE);
        if (metadata == EnumDyeColor.WHITE.getDyeDamage()) return new ItemStack(FFDItems.WHITE_DYE);
        return ItemStack.EMPTY;
    }

    private static ShapedRecipes shaped(int width, int height, ItemStack output, ItemStack... inputs) {
        return new ShapedRecipes("", width, height, ingredients(inputs), output);
    }

    private static NonNullList<Ingredient> ingredients(ItemStack... inputs) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStack input : inputs) {
            ingredients.add(input.isEmpty() ? Ingredient.EMPTY : Ingredient.fromStacks(input));
        }
        return ingredients;
    }

    private static void replaceUnchanged(IForgeRegistry<IRecipe> registry, String path,
                                         ShapedRecipes original, IRecipe replacement) {
        ResourceLocation id = new ResourceLocation("minecraft", path);
        IRecipe existing = registry.getValue(id);
        if (existing == null || existing.getClass() != ShapedRecipes.class
                || !ItemStack.areItemStacksEqual(existing.getRecipeOutput(), original.getRecipeOutput())) {
            return;
        }
        ShapedRecipes shaped = (ShapedRecipes) existing;
        if (shaped.recipeWidth != original.recipeWidth || shaped.recipeHeight != original.recipeHeight
                || !shaped.getGroup().equals(original.getGroup())) {
            return;
        }
        for (int index = 0; index < original.recipeItems.size(); index++) {
            if (shaped.recipeItems.get(index).getClass() != original.recipeItems.get(index).getClass()) {
                return;
            }
            ItemStack[] expected = original.recipeItems.get(index).getMatchingStacks();
            ItemStack[] actual = shaped.recipeItems.get(index).getMatchingStacks();
            if (expected.length != actual.length) {
                return;
            }
            for (int stackIndex = 0; stackIndex < expected.length; stackIndex++) {
                if (!ItemStack.areItemStacksEqual(expected[stackIndex], actual[stackIndex])) {
                    return;
                }
            }
        }
        registry.register(replacement.setRegistryName(id));
    }
}
