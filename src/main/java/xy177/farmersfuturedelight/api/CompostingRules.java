package xy177.farmersfuturedelight.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public final class CompostingRules {
    private static final Map<ResourceLocation, Rule> RULES = new LinkedHashMap<>();
    private static volatile List<Rule> snapshot = Collections.emptyList();

    private CompostingRules() {
    }

    public static synchronized void register(ResourceLocation id, Ingredient ingredient,
                                              double chance, boolean villagerCanCompost) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ingredient, "ingredient");
        if (!Double.isFinite(chance) || chance < 0.0D || chance > 1.0D) {
            throw new IllegalArgumentException("Composting chance must be between 0 and 1");
        }
        RULES.remove(id);
        RULES.put(id, new Rule(id, ingredient, chance, villagerCanCompost));
        snapshot = Collections.unmodifiableList(new ArrayList<>(RULES.values()));
    }

    public static synchronized boolean remove(ResourceLocation id) {
        boolean removed = RULES.remove(id) != null;
        if (removed) {
            snapshot = Collections.unmodifiableList(new ArrayList<>(RULES.values()));
        }
        return removed;
    }

    public static synchronized void clear() {
        RULES.clear();
        snapshot = Collections.emptyList();
    }

    public static List<Rule> getRules() {
        return snapshot;
    }

    @Nullable
    public static synchronized Rule get(ResourceLocation id) {
        return RULES.get(id);
    }

    @Nullable
    public static Rule find(ItemStack input) {
        if (input.isEmpty()) {
            return null;
        }
        List<Rule> rules = getRules();
        for (int index = rules.size() - 1; index >= 0; index--) {
            Rule rule = rules.get(index);
            if (rule.ingredient.apply(input)) {
                return rule;
            }
        }
        return null;
    }

    public static final class Rule {
        private final ResourceLocation id;
        private final Ingredient ingredient;
        private final double chance;
        private final boolean villagerCanCompost;

        private Rule(ResourceLocation id, Ingredient ingredient, double chance,
                     boolean villagerCanCompost) {
            this.id = id;
            this.ingredient = ingredient;
            this.chance = chance;
            this.villagerCanCompost = villagerCanCompost;
        }

        public ResourceLocation getId() {
            return id;
        }

        public Ingredient getIngredient() {
            return ingredient;
        }

        public double getChance() {
            return chance;
        }

        public boolean canVillagerCompost() {
            return villagerCanCompost;
        }
    }
}
