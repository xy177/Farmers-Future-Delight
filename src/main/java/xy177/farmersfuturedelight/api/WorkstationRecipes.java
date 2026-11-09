package xy177.farmersfuturedelight.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class WorkstationRecipes {
    public enum Type {
        SMOKING(1), BLASTING(1), CAMPFIRE_COOKING(1), STONECUTTING(1), SMITHING(3),
        GRINDING_SINGLE(1), GRINDING(2);

        private final int inputs;

        Type(int inputs) {
            this.inputs = inputs;
        }

        public int getInputCount() {
            return inputs;
        }
    }

    public interface Assembler {
        ItemStack assemble(List<ItemStack> inputs);
    }

    private static final Map<Type, LinkedHashMap<ResourceLocation, Recipe>> RECIPES =
            new EnumMap<>(Type.class);
    private static final Map<Type, List<Recipe>> SNAPSHOTS = new EnumMap<>(Type.class);

    static {
        for (Type type : Type.values()) {
            RECIPES.put(type, new LinkedHashMap<>());
            SNAPSHOTS.put(type, Collections.emptyList());
        }
    }

    private WorkstationRecipes() {
    }

    public static synchronized void register(Type type, ResourceLocation id,
                                              List<Ingredient> ingredients, ItemStack output,
                                              int processingTime, float experience) {
        if (output == null || output.isEmpty()) {
            throw new IllegalArgumentException("Workstation output must not be empty");
        }
        ItemStack template = output.copy();
        register(type, id, ingredients, inputs -> template.copy(), processingTime, experience);
    }

    public static synchronized void register(Type type, ResourceLocation id,
                                              List<Ingredient> ingredients, Assembler assembler,
                                              int processingTime, float experience) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(assembler, "assembler");
        if (ingredients == null || ingredients.size() != type.inputs
                || ingredients.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Wrong workstation ingredient count for " + type);
        }
        if (processingTime <= 0 || !Float.isFinite(experience) || experience < 0.0F) {
            throw new IllegalArgumentException("Invalid workstation time or experience");
        }
        LinkedHashMap<ResourceLocation, Recipe> entries = RECIPES.get(type);
        entries.remove(id);
        entries.put(id, new Recipe(type, id, ingredients, assembler, processingTime, experience, ""));
        SNAPSHOTS.put(type, Collections.unmodifiableList(new ArrayList<>(entries.values())));
    }

    public static synchronized void setGroup(Type type, ResourceLocation id, String group) {
        Objects.requireNonNull(group, "group");
        if (group.length() > 256) throw new IllegalArgumentException("Workstation recipe group is too long");
        Recipe recipe = get(type, id);
        if (recipe == null) throw new IllegalArgumentException("Unknown workstation recipe " + id);
        RECIPES.get(type).put(id, new Recipe(type, id, recipe.ingredients, recipe.assembler,
                recipe.processingTime, recipe.experience, group));
        SNAPSHOTS.put(type, Collections.unmodifiableList(new ArrayList<>(RECIPES.get(type).values())));
    }

    public static synchronized boolean remove(Type type, ResourceLocation id) {
        LinkedHashMap<ResourceLocation, Recipe> entries = RECIPES.get(Objects.requireNonNull(type, "type"));
        boolean removed = entries.remove(id) != null;
        if (removed) {
            SNAPSHOTS.put(type, Collections.unmodifiableList(new ArrayList<>(entries.values())));
        }
        return removed;
    }

    public static synchronized void clear(Type type) {
        RECIPES.get(Objects.requireNonNull(type, "type")).clear();
        SNAPSHOTS.put(type, Collections.emptyList());
    }

    public static synchronized List<Recipe> getRecipes(Type type) {
        return SNAPSHOTS.get(Objects.requireNonNull(type, "type"));
    }

    public static ItemStack copySmithingData(ItemStack input, ItemStack result) {
        if (input == null || input.isEmpty() || result == null || result.isEmpty()) {
            return ItemStack.EMPTY;
        }
        NBTTagCompound inputData = input.serializeNBT();
        NBTTagCompound resultData = result.serializeNBT();
        for (String key : inputData.getKeySet()) {
            if (!"id".equals(key) && !"Count".equals(key)) {
                resultData.setTag(key, inputData.getTag(key).copy());
            }
        }
        if (result.hasTagCompound() && input.hasTagCompound()) {
            NBTTagCompound merged = input.getTagCompound().copy();
            merged.merge(result.getTagCompound());
            resultData.setTag("tag", merged);
        }
        return new ItemStack(resultData);
    }

    @Nullable
    public static synchronized Recipe get(Type type, ResourceLocation id) {
        return RECIPES.get(type).get(id);
    }

    @Nullable
    public static Recipe find(Type type, List<ItemStack> inputs) {
        List<Recipe> entries = getRecipes(type);
        for (int index = entries.size() - 1; index >= 0; index--) {
            Recipe recipe = entries.get(index);
            if (recipe.matches(inputs)) {
                return recipe;
            }
        }
        return null;
    }

    public static final class Recipe {
        private final Type type;
        private final ResourceLocation id;
        private final List<Ingredient> ingredients;
        private final Assembler assembler;
        private final int processingTime;
        private final float experience;
        private final String group;

        private Recipe(Type type, ResourceLocation id, List<Ingredient> ingredients,
                       Assembler assembler, int processingTime, float experience, String group) {
            this.type = type;
            this.id = id;
            this.ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
            this.assembler = assembler;
            this.processingTime = processingTime;
            this.experience = experience;
            this.group = group;
        }

        public Type getType() {
            return type;
        }

        public ResourceLocation getId() {
            return id;
        }

        public String getGroup() {
            return group;
        }

        public List<Ingredient> getIngredients() {
            return ingredients;
        }

        public int getProcessingTime() {
            return processingTime;
        }

        public float getExperience() {
            return experience;
        }

        public boolean matches(List<ItemStack> inputs) {
            if (inputs == null || inputs.size() != ingredients.size()) {
                return false;
            }
            for (int index = 0; index < ingredients.size(); index++) {
                if (!ingredients.get(index).apply(inputs.get(index))) {
                    return false;
                }
            }
            return true;
        }

        public ItemStack assemble(List<ItemStack> inputs) {
            if (!matches(inputs)) {
                return ItemStack.EMPTY;
            }
            List<ItemStack> copies = new ArrayList<>();
            for (ItemStack stack : inputs) {
                copies.add(stack.copy());
            }
            ItemStack output = assembler.assemble(Collections.unmodifiableList(copies));
            return output == null ? ItemStack.EMPTY : output.copy();
        }
    }
}
