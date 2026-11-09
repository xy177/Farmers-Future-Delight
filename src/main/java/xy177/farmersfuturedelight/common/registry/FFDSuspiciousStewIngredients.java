package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDSuspiciousStewIngredients {
    private static final Logger LOGGER = LogManager.getLogger("FFD Suspicious Stew");
    private static List<Rule> rules;

    private FFDSuspiciousStewIngredients() {
    }

    public static synchronized List<Rule> getRules() {
        if (rules != null) return rules;
        List<Rule> parsed = new ArrayList<>();
        String[] entries = FFDConfig.suspiciousStewIngredients;
        if (entries != null) {
            for (String entry : entries) {
                try {
                    String[] fields = entry.split(";", -1);
                    if (fields.length != 4) throw new IllegalArgumentException("Expected four fields");
                    ResourceLocation id = new ResourceLocation(fields[0].trim());
                    Item item = ForgeRegistries.ITEMS.getValue(id);
                    if ("farmers_future_delight".equals(id.getResourceDomain())) {
                        if ("cornflower".equals(id.getResourcePath())) item = FFDItems.effectiveItem(FFDItems.CORNFLOWER);
                        else if ("lily_of_the_valley".equals(id.getResourcePath())) item = FFDItems.effectiveItem(FFDItems.LILY_OF_THE_VALLEY);
                        else if ("wither_rose".equals(id.getResourcePath())) item = FFDItems.effectiveItem(FFDItems.WITHER_ROSE);
                        else if ("golden_dandelion".equals(id.getResourcePath())) item = FFDItems.effectiveItem(FFDItems.GOLDEN_DANDELION);
                    }
                    int metadata = "*".equals(fields[1].trim()) ? OreDictionary.WILDCARD_VALUE
                            : Integer.parseInt(fields[1].trim());
                    Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(fields[2].trim()));
                    int duration = Integer.parseInt(fields[3].trim());
                    if (item == null || item == net.minecraft.init.Items.AIR || potion == null
                            || metadata < 0 || metadata > OreDictionary.WILDCARD_VALUE || duration <= 0) {
                        throw new IllegalArgumentException("Unknown item/effect or invalid metadata/duration");
                    }
                    for (Rule rule : parsed) {
                        if (rule.item == item && rule.metadata == metadata) {
                            throw new IllegalArgumentException("Duplicate ingredient and metadata");
                        }
                    }
                    parsed.add(new Rule(item, metadata, potion, duration));
                } catch (RuntimeException exception) {
                    LOGGER.warn("Ignoring suspicious stew ingredient rule {}: {}", entry, exception.getMessage());
                }
            }
        }
        rules = Collections.unmodifiableList(parsed);
        return rules;
    }

    public static Rule find(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Rule wildcard = null;
        for (Rule rule : getRules()) {
            if (rule.item != stack.getItem()) continue;
            if (rule.metadata == stack.getMetadata()) return rule;
            if (rule.metadata == OreDictionary.WILDCARD_VALUE) wildcard = rule;
        }
        return wildcard;
    }

    public static PotionEffect getEffect(ItemStack stack) {
        Rule rule = find(stack);
        return rule == null ? null : rule.createEffect();
    }

    public static final class Rule {
        private final Item item;
        private final int metadata;
        private final Potion potion;
        private final int duration;

        private Rule(Item item, int metadata, Potion potion, int duration) {
            this.item = item;
            this.metadata = metadata;
            this.potion = potion;
            this.duration = duration;
        }

        public PotionEffect createEffect() {
            return new PotionEffect(potion, duration);
        }

        public String getRecipeName() {
            if (item == Item.getItemFromBlock(net.minecraft.init.Blocks.YELLOW_FLOWER) && metadata == 0) {
                return "dandelion";
            }
            if (item == Item.getItemFromBlock(net.minecraft.init.Blocks.RED_FLOWER) && metadata >= 0 && metadata <= 8) {
                return new String[]{"poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip",
                        "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy"}[metadata];
            }
            if (item == FFDItems.effectiveItem(FFDItems.CORNFLOWER) && metadata == 0) return "cornflower";
            if (item == FFDItems.effectiveItem(FFDItems.LILY_OF_THE_VALLEY) && metadata == 0) return "lily_of_the_valley";
            if (item == FFDItems.effectiveItem(FFDItems.WITHER_ROSE) && metadata == 0) return "wither_rose";
            ResourceLocation id = item.getRegistryName();
            return "custom/" + id.getResourceDomain() + "/" + id.getResourcePath() + "/" + metadata;
        }

        public Ingredient ingredient() {
            return new Ingredient(new ItemStack(item, 1, metadata)) {
                @Override
                public boolean apply(ItemStack stack) {
                    return stack != null && find(stack) == Rule.this;
                }

                @Override
                public boolean isSimple() {
                    return false;
                }
            };
        }
    }
}
