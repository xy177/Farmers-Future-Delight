package xy177.farmersfuturedelight.api.biome;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

public final class VerticalBiomeDefinition {
    public enum Category {
        CAVE, SURFACE, MOUNTAIN, NETHER
    }

    public enum Stage {
        LOCAL_MODIFICATIONS, UNDERGROUND_DECORATION, VEGETATION
    }

    private final ResourceLocation registryName;
    private final String translationKey;
    private final Category category;
    private final int priority;
    private final ToDoubleFunction<VerticalBiomeSample> selectionRule;
    private final Map<Stage, Consumer<VerticalBiomeGenerationContext>> features;

    private VerticalBiomeDefinition(Builder builder) {
        registryName = builder.registryName;
        translationKey = builder.translationKey;
        category = builder.category;
        priority = builder.priority;
        selectionRule = builder.selectionRule;
        features = Collections.unmodifiableMap(new EnumMap<>(builder.features));
    }

    public static Builder builder(ResourceLocation registryName) {
        return new Builder(registryName);
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Category getCategory() {
        return category;
    }

    public int getPriority() {
        return priority;
    }

    @Nullable
    public ToDoubleFunction<VerticalBiomeSample> getSelectionRule() {
        return selectionRule;
    }

    @Nullable
    public Consumer<VerticalBiomeGenerationContext> getFeature(Stage stage) {
        return features.get(stage);
    }

    public static final class Builder {
        private final ResourceLocation registryName;
        private String translationKey;
        private Category category = Category.CAVE;
        private int priority;
        private ToDoubleFunction<VerticalBiomeSample> selectionRule;
        private final Map<Stage, Consumer<VerticalBiomeGenerationContext>> features =
                new EnumMap<>(Stage.class);

        private Builder(ResourceLocation registryName) {
            this.registryName = Objects.requireNonNull(registryName, "registryName");
            if (registryName.toString().length() > 256) {
                throw new IllegalArgumentException("Vertical biome ID exceeds 256 characters");
            }
            translationKey = "biome." + registryName.getResourceDomain()
                    + "." + registryName.getResourcePath();
        }

        public Builder translationKey(String value) {
            if (Objects.requireNonNull(value, "translationKey").isEmpty()) {
                throw new IllegalArgumentException("Translation key must contain text");
            }
            translationKey = value;
            return this;
        }

        public Builder category(Category value) {
            category = Objects.requireNonNull(value, "category");
            return this;
        }

        public Builder priority(int value) {
            priority = value;
            return this;
        }

        public Builder selectionRule(ToDoubleFunction<VerticalBiomeSample> value) {
            selectionRule = Objects.requireNonNull(value, "selectionRule");
            return this;
        }

        public Builder feature(Stage stage, Consumer<VerticalBiomeGenerationContext> value) {
            features.put(Objects.requireNonNull(stage, "stage"),
                    Objects.requireNonNull(value, "feature"));
            return this;
        }

        public VerticalBiomeDefinition build() {
            return new VerticalBiomeDefinition(this);
        }
    }
}
