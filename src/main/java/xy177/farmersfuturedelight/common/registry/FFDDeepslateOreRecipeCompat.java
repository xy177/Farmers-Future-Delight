package xy177.farmersfuturedelight.common.registry;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class FFDDeepslateOreRecipeCompat {
    private static final Logger LOGGER = LogManager.getLogger("FFD Deep Ore Recipe Compatibility");
    private static boolean registered;

    private FFDDeepslateOreRecipeCompat() {
    }

    static void register(List<FFDDeepslateOreCompat.RecipePair> pairs) {
        if (registered || pairs.isEmpty()) {
            return;
        }
        registered = true;
        if (Loader.isModLoaded("mekanism")) {
            registerMekanism(pairs);
        }
        if (Loader.isModLoaded("thermalexpansion")) {
            registerThermalExpansion(pairs);
        }
        if (Loader.isModLoaded("immersiveengineering")) {
            registerImmersiveEngineering(pairs);
        }
        if (Loader.isModLoaded("techreborn")) {
            registerTechReborn(pairs);
        }
    }

    private static void registerMekanism(List<FFDDeepslateOreCompat.RecipePair> pairs) {
        try {
            Class<?> recipeTypeClass = Class.forName("mekanism.common.recipe.RecipeHandler$Recipe");
            Object values = invokeStatic(recipeTypeClass, "values");
            if (!(values instanceof Iterable)) {
                return;
            }
            for (Object recipeType : (Iterable<?>) values) {
                Object mapValue = invoke(recipeType, "get");
                if (!(mapValue instanceof Map)) {
                    continue;
                }
                Map<?, ?> recipes = (Map<?, ?>) mapValue;
                List<Object> snapshot = new ArrayList<>(recipes.values());
                for (Object recipe : snapshot) {
                    for (FFDDeepslateOreCompat.RecipePair pair : pairs) {
                        Object copy = invoke(recipe, "copy");
                        Object input = invoke(copy, "getInput");
                        if (copy == null || input == null
                                || !replaceItemStacks(input, pair.source(), pair.deep())) {
                            continue;
                        }
                        refreshMekanismInput(input);
                        if (!recipes.containsKey(input)) {
                            invoke(recipeType, "put", copy);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Mekanism deep-ore recipe compatibility was unavailable", ignored);
        }
    }

    private static void refreshMekanismInput(Object input) {
        if (!"mekanism.common.recipe.inputs.ItemStackInput".equals(
                input.getClass().getName())) {
            return;
        }
        try {
            Field hash = input.getClass().getDeclaredField("ingredientHash");
            hash.setAccessible(true);
            Object value = invoke(input, "hashIngredients");
            if (value instanceof Integer) {
                hash.setInt(input, (Integer) value);
            }
            Field wild = input.getClass().getDeclaredField("wildVersion");
            wild.setAccessible(true);
            wild.set(input, null);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void registerThermalExpansion(
            List<FFDDeepslateOreCompat.RecipePair> pairs) {
        try {
            Class<?> furnace = Class.forName(
                    "cofh.thermalexpansion.util.managers.machine.FurnaceManager");
            Class<?> pulverizer = Class.forName(
                    "cofh.thermalexpansion.util.managers.machine.PulverizerManager");
            Class<?> smelter = Class.forName(
                    "cofh.thermalexpansion.util.managers.machine.SmelterManager");
            Class<?> crucible = Class.forName(
                    "cofh.thermalexpansion.util.managers.machine.CrucibleManager");
            for (FFDDeepslateOreCompat.RecipePair pair : pairs) {
                copyThermalFurnace(furnace, pair, false);
                copyThermalFurnace(furnace, pair, true);
                copyThermalPulverizer(pulverizer, pair);
                copyThermalSmelter(smelter, pair);
                copyThermalCrucible(crucible, pair);
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Thermal Expansion deep-ore recipe compatibility was unavailable", ignored);
        }
    }

    private static void copyThermalFurnace(Class<?> manager,
                                           FFDDeepslateOreCompat.RecipePair pair,
                                           boolean pyrolysis) {
        ItemStack source = pair.source();
        ItemStack deep = pair.deep();
        Object recipe = invokeStatic(manager, "getRecipe", source, pyrolysis);
        if (recipe == null || isTrue(invokeStatic(manager, "recipeExists", deep, pyrolysis))) {
            return;
        }
        ItemStack input = stack(invoke(recipe, "getInput"));
        ItemStack output = stack(invoke(recipe, "getOutput"));
        if (input.isEmpty() || output.isEmpty()) {
            return;
        }
        deep.setCount(input.getCount());
        int energy = integer(invoke(recipe, "getEnergy"));
        if (pyrolysis) {
            invokeStatic(manager, "addRecipePyrolysis", energy, deep, output,
                    integer(invoke(recipe, "getCreosote")));
        } else {
            invokeStatic(manager, "addRecipe", energy, deep, output);
        }
    }

    private static void copyThermalPulverizer(Class<?> manager,
                                              FFDDeepslateOreCompat.RecipePair pair) {
        ItemStack source = pair.source();
        ItemStack deep = pair.deep();
        Object recipe = invokeStatic(manager, "getRecipe", source);
        if (recipe == null || isTrue(invokeStatic(manager, "recipeExists", deep))) {
            return;
        }
        ItemStack input = stack(invoke(recipe, "getInput"));
        ItemStack primary = stack(invoke(recipe, "getPrimaryOutput"));
        ItemStack secondary = stack(invoke(recipe, "getSecondaryOutput"));
        if (input.isEmpty() || primary.isEmpty()) {
            return;
        }
        deep.setCount(input.getCount());
        invokeStatic(manager, "addRecipe", integer(invoke(recipe, "getEnergy")),
                deep, primary, secondary,
                integer(invoke(recipe, "getSecondaryOutputChance")));
    }

    private static void copyThermalSmelter(Class<?> manager,
                                           FFDDeepslateOreCompat.RecipePair pair) {
        Object recipes = invokeStatic(manager, "getRecipeList");
        if (recipes == null || !recipes.getClass().isArray()) {
            return;
        }
        int length = Array.getLength(recipes);
        for (int index = 0; index < length; index++) {
            Object recipe = Array.get(recipes, index);
            ItemStack primaryInput = stack(invoke(recipe, "getPrimaryInput"));
            ItemStack secondaryInput = stack(invoke(recipe, "getSecondaryInput"));
            boolean replacePrimary = matches(pair.source(), primaryInput);
            boolean replaceSecondary = matches(pair.source(), secondaryInput);
            if (!replacePrimary && !replaceSecondary) {
                continue;
            }
            ItemStack newPrimary = replacePrimary
                    ? sized(pair.deep(), primaryInput.getCount()) : primaryInput;
            ItemStack newSecondary = replaceSecondary
                    ? sized(pair.deep(), secondaryInput.getCount()) : secondaryInput;
            if (isTrue(invokeStatic(manager, "recipeExists", newPrimary, newSecondary))) {
                continue;
            }
            invokeStatic(manager, "addRecipe", integer(invoke(recipe, "getEnergy")),
                    newPrimary, newSecondary,
                    stack(invoke(recipe, "getPrimaryOutput")),
                    stack(invoke(recipe, "getSecondaryOutput")),
                    integer(invoke(recipe, "getSecondaryOutputChance")));
        }
    }

    private static void copyThermalCrucible(Class<?> manager,
                                            FFDDeepslateOreCompat.RecipePair pair) {
        ItemStack source = pair.source();
        ItemStack deep = pair.deep();
        Object recipe = invokeStatic(manager, "getRecipe", source);
        if (recipe == null || isTrue(invokeStatic(manager, "recipeExists", deep))) {
            return;
        }
        ItemStack input = stack(invoke(recipe, "getInput"));
        Object output = invoke(recipe, "getOutput");
        if (input.isEmpty() || output == null) {
            return;
        }
        deep.setCount(input.getCount());
        invokeStatic(manager, "addRecipe", integer(invoke(recipe, "getEnergy")),
                deep, copy(output));
    }

    private static void registerImmersiveEngineering(
            List<FFDDeepslateOreCompat.RecipePair> pairs) {
        try {
            Class<?> crusher = Class.forName(
                    "blusunrize.immersiveengineering.api.crafting.CrusherRecipe");
            for (FFDDeepslateOreCompat.RecipePair pair : pairs) {
                ItemStack source = pair.source();
                ItemStack deep = pair.deep();
                Object recipe = invokeStatic(crusher, "findRecipe", source);
                if (recipe == null || invokeStatic(crusher, "findRecipe", deep) != null) {
                    continue;
                }
                Object input = field(recipe, "input");
                int inputSize = integer(field(input, "inputSize"));
                ItemStack output = stack(field(recipe, "output"));
                if (inputSize <= 0 || output.isEmpty()) {
                    continue;
                }
                deep.setCount(inputSize);
                Object added = invokeStatic(crusher, "addRecipe", output, deep,
                        integer(invoke(recipe, "getTotalProcessEnergy")));
                ItemStack[] secondary = itemStackArray(field(recipe, "secondaryOutput"));
                float[] chances = floatArray(field(recipe, "secondaryChance"));
                if (added != null && secondary.length == chances.length
                        && secondary.length > 0) {
                    Object[] values = new Object[secondary.length * 2];
                    for (int index = 0; index < secondary.length; index++) {
                        values[index * 2] = secondary[index].copy();
                        values[index * 2 + 1] = chances[index];
                    }
                    invoke(added, "addToSecondaryOutput", (Object) values);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Immersive Engineering deep-ore recipe compatibility was unavailable", ignored);
        }
    }

    private static void registerTechReborn(
            List<FFDDeepslateOreCompat.RecipePair> pairs) {
        registerTechRebornGrinder(pairs);
        registerTechRebornIndustrialGrinder(pairs);
    }

    private static void registerTechRebornGrinder(
            List<FFDDeepslateOreCompat.RecipePair> pairs) {
        try {
            Class<?> recipesClass = Class.forName("techreborn.api.recipe.Recipes");
            Object handler = staticField(recipesClass, "grinder");
            Object recipesValue = invoke(handler, "getRecipes");
            if (!(recipesValue instanceof Collection)) {
                return;
            }
            List<Object> recipes = new ArrayList<>((Collection<?>) recipesValue);
            Class<?> itemInput = Class.forName(
                    "reborncore.api.praescriptum.ingredients.input.ItemStackInputIngredient");
            for (Object recipe : recipes) {
                for (FFDDeepslateOreCompat.RecipePair pair : pairs) {
                    List<Object> inputs = copyTechRebornInputs(recipe, pair, itemInput);
                    if (inputs == null || invoke(handler, "getRecipe", inputs) != null) {
                        continue;
                    }
                    Object copy = invoke(handler, "createRecipe");
                    invoke(copy, "withInput", inputs);
                    Object itemOutputs = invoke(recipe, "getItemOutputs");
                    if (itemOutputs != null && itemOutputs.getClass().isArray()) {
                        for (int index = 0; index < Array.getLength(itemOutputs); index++) {
                            invoke(copy, "withOutput", stack(Array.get(itemOutputs, index)));
                        }
                    }
                    Object fluidOutputs = invoke(recipe, "getFluidOutputs");
                    if (fluidOutputs != null && fluidOutputs.getClass().isArray()) {
                        for (int index = 0; index < Array.getLength(fluidOutputs); index++) {
                            invoke(copy, "withOutput", copy(Array.get(fluidOutputs, index)));
                        }
                    }
                    invoke(copy, "withEnergyCostPerTick",
                            integer(invoke(recipe, "getEnergyCostPerTick")));
                    invoke(copy, "withOperationDuration",
                            integer(invoke(recipe, "getOperationDuration")));
                    Object metadata = invoke(recipe, "getMetadata");
                    if (metadata instanceof NBTTagCompound) {
                        invoke(copy, "withMetadata", ((NBTTagCompound) metadata).copy());
                    }
                    invoke(copy, "withNBT", isTrue(invoke(recipe, "shouldUseNBT")));
                    invoke(handler, "addRecipe", copy, false);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Tech Reborn grinder deep-ore compatibility was unavailable", ignored);
        }
    }

    private static List<Object> copyTechRebornInputs(Object recipe,
                                                     FFDDeepslateOreCompat.RecipePair pair,
                                                     Class<?> itemInput) {
        Object value = invoke(recipe, "getInputIngredients");
        if (!(value instanceof Collection)) {
            return null;
        }
        List<Object> inputs = new ArrayList<>();
        boolean replaced = false;
        for (Object ingredient : (Collection<?>) value) {
            Object unspecific = invoke(ingredient, "getUnspecific");
            if (unspecific instanceof ItemStack && matches(pair.source(), (ItemStack) unspecific)) {
                ItemStack deep = sized(pair.deep(), ((ItemStack) unspecific).getCount());
                Object replacement = invokeStatic(itemInput, "copyOf", deep,
                        isTrue(invoke(ingredient, "isConsumable")));
                if (replacement == null) {
                    return null;
                }
                inputs.add(replacement);
                replaced = true;
            } else {
                inputs.add(invoke(ingredient, "copy"));
            }
        }
        return replaced ? inputs : null;
    }

    private static void registerTechRebornIndustrialGrinder(
            List<FFDDeepslateOreCompat.RecipePair> pairs) {
        try {
            Class<?> handler = Class.forName("reborncore.api.recipe.RecipeHandler");
            Object listValue = staticField(handler, "recipeList");
            if (!(listValue instanceof List)) {
                return;
            }
            List<?> liveRecipes = (List<?>) listValue;
            List<Object> snapshot = new ArrayList<>(liveRecipes);
            for (Object recipe : snapshot) {
                if (!"IndustrialGrinder".equals(String.valueOf(
                        invoke(recipe, "getUserFreindlyName")))) {
                    continue;
                }
                for (FFDDeepslateOreCompat.RecipePair pair : pairs) {
                    Object inputsValue = invoke(recipe, "getInputs");
                    if (!(inputsValue instanceof List)) {
                        continue;
                    }
                    List<Object> inputs = replaceTechRebornLegacyInputs(
                            (List<?>) inputsValue, pair);
                    if (inputs == null || containsLegacyRecipe(liveRecipes, recipe, inputs)) {
                        continue;
                    }
                    Object copy = invoke(recipe, "clone");
                    if (copy == null || !setField(copy, "inputs", new ArrayList<>(inputs))) {
                        continue;
                    }
                    invokeStatic(handler, "addRecipe", copy);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Tech Reborn industrial grinder deep-ore compatibility was unavailable", ignored);
        }
    }

    private static List<Object> replaceTechRebornLegacyInputs(
            List<?> inputs, FFDDeepslateOreCompat.RecipePair pair) {
        List<Object> result = new ArrayList<>();
        boolean replaced = false;
        for (Object input : inputs) {
            if (input instanceof ItemStack && matches(pair.source(), (ItemStack) input)) {
                result.add(sized(pair.deep(), ((ItemStack) input).getCount()));
                replaced = true;
            } else if (input instanceof ItemStack) {
                result.add(((ItemStack) input).copy());
            } else {
                result.add(input);
            }
        }
        return replaced ? result : null;
    }

    private static boolean containsLegacyRecipe(List<?> recipes, Object template,
                                                List<Object> inputs) {
        String name = String.valueOf(invoke(template, "getRecipeName"));
        for (Object recipe : recipes) {
            if (!name.equals(String.valueOf(invoke(recipe, "getRecipeName")))) {
                continue;
            }
            Object value = invoke(recipe, "getInputs");
            if (value instanceof List && sameInputs((List<?>) value, inputs)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameInputs(List<?> left, List<?> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            Object first = left.get(index);
            Object second = right.get(index);
            if (first instanceof ItemStack && second instanceof ItemStack) {
                ItemStack a = (ItemStack) first;
                ItemStack b = (ItemStack) second;
                if (!ItemStack.areItemsEqual(a, b)
                        || !ItemStack.areItemStackTagsEqual(a, b)
                        || a.getCount() != b.getCount()) {
                    return false;
                }
            } else if (first == null ? second != null : !first.equals(second)) {
                return false;
            }
        }
        return true;
    }

    private static boolean replaceItemStacks(Object input, ItemStack source, ItemStack deep) {
        boolean replaced = false;
        for (Class<?> type = input.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                        || !ItemStack.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object value = field.get(input);
                    if (value instanceof ItemStack && matches(source, (ItemStack) value)) {
                        field.set(input, sized(deep, ((ItemStack) value).getCount()));
                        replaced = true;
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
        return replaced;
    }

    private static boolean matches(ItemStack source, ItemStack candidate) {
        return !source.isEmpty() && !candidate.isEmpty()
                && (OreDictionary.itemMatches(source, candidate, false)
                || OreDictionary.itemMatches(candidate, source, false))
                && ItemStack.areItemStackTagsEqual(source, candidate);
    }

    private static ItemStack sized(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static ItemStack stack(Object value) {
        return value instanceof ItemStack ? ((ItemStack) value).copy() : ItemStack.EMPTY;
    }

    private static ItemStack[] itemStackArray(Object value) {
        if (!(value instanceof ItemStack[])) {
            return new ItemStack[0];
        }
        ItemStack[] source = (ItemStack[]) value;
        ItemStack[] copy = new ItemStack[source.length];
        for (int index = 0; index < source.length; index++) {
            copy[index] = source[index].copy();
        }
        return copy;
    }

    private static float[] floatArray(Object value) {
        return value instanceof float[] ? ((float[]) value).clone() : new float[0];
    }

    private static int integer(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static boolean isTrue(Object value) {
        return Boolean.TRUE.equals(value);
    }

    private static Object copy(Object value) {
        return invoke(value, "copy");
    }

    private static Object staticField(Class<?> type, String name)
            throws ReflectiveOperationException {
        Field field = type.getField(name);
        return field.get(null);
    }

    private static Object field(Object target, String name) {
        if (target == null) {
            return null;
        }
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static boolean setField(Object target, String name, Object value) {
        if (target == null) {
            return false;
        }
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return true;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return false;
    }

    private static Object invokeStatic(Class<?> type, String name, Object... args) {
        return invokeMethod(null, type, name, args);
    }

    private static Object invoke(Object target, String name, Object... args) {
        return target == null ? null : invokeMethod(target, target.getClass(), name, args);
    }

    private static Object invokeMethod(Object target, Class<?> type, String name, Object[] args) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name)
                        && Modifier.isStatic(method.getModifiers()) == (target == null)
                        && method.getParameterTypes().length == args.length
                        && matches(method.getParameterTypes(), args)) {
                    try {
                        method.setAccessible(true);
                        return method.invoke(target, args);
                    } catch (ReflectiveOperationException ignored) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private static boolean matches(Class<?>[] parameterTypes, Object[] args) {
        for (int index = 0; index < parameterTypes.length; index++) {
            if (args[index] == null) {
                continue;
            }
            Class<?> parameter = parameterTypes[index];
            if (parameter.isPrimitive()) {
                parameter = wrapper(parameter);
            }
            if (!parameter.isInstance(args[index])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> wrapper(Class<?> primitive) {
        if (primitive == int.class) {
            return Integer.class;
        }
        if (primitive == boolean.class) {
            return Boolean.class;
        }
        if (primitive == float.class) {
            return Float.class;
        }
        if (primitive == double.class) {
            return Double.class;
        }
        if (primitive == long.class) {
            return Long.class;
        }
        if (primitive == short.class) {
            return Short.class;
        }
        if (primitive == byte.class) {
            return Byte.class;
        }
        if (primitive == char.class) {
            return Character.class;
        }
        return primitive;
    }
}
