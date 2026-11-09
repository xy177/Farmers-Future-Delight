package xy177.farmersfuturedelight.common.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.common.item.ItemSuspiciousStew;
import xy177.farmersfuturedelight.common.worldgen.FFDExplorationMaps;

final class FFDTradeItemModifiers {
    private static final Logger LOGGER = LogManager.getLogger("FFD Trade Data");
    private static final Set<String> TRADED_EQUIPMENT = new HashSet<>();
    private static final List<Enchantment> TRADEABLE_ENCHANTMENTS = new ArrayList<>();
    private static final Set<Enchantment> DOUBLE_PRICE_ENCHANTMENTS = new HashSet<>();
    private static final List<PotionType> TRADEABLE_POTIONS = new ArrayList<>();
    private static final Set<String> REPORTED = new HashSet<>();
    private static boolean loaded;

    private FFDTradeItemModifiers() {
    }

    static ItemStack stack(JsonObject definition) {
        return stack(definition, new Random(0L));
    }

    static ItemStack stack(JsonObject definition, Random random) {
        ItemStack stack = FFDModernItemLookup.resolve(definition.get("id").getAsString());
        if (stack.isEmpty()) return stack;
        int count = chooseInt(definition, "count", random, 1);
        int metadata = chooseInt(definition, "meta", random, stack.getMetadata());
        ItemStack selected = new ItemStack(stack.getItem(), count, metadata);
        if (stack.hasTagCompound()) selected.setTagCompound(stack.getTagCompound().copy());
        stack = selected;
        if (definition.has("nbt")) stack.setTagCompound(chooseNbt(definition.get("nbt"), random));
        if (definition.has("components")) {
            JsonObject components = definition.getAsJsonObject("components");
            if (components.has("minecraft:potion_contents")) {
                String potion = components.getAsJsonObject("minecraft:potion_contents").get("potion").getAsString();
                ResourceLocation id = new ResourceLocation(potion);
                if (!PotionType.REGISTRY.containsKey(id)) return ItemStack.EMPTY;
                PotionUtils.addPotionToItemStack(stack, PotionType.REGISTRY.getObject(id));
            }
        }
        return stack;
    }

    @Nullable
    static MerchantRecipe create(JsonObject definition, Random random, boolean modernVillager) {
        return create(definition, random, modernVillager, null);
    }

    @Nullable
    static MerchantRecipe create(JsonObject definition, Random random, boolean modernVillager,
                                 @Nullable EntityVillager merchant) {
        load();
        JsonObject wants = definition.getAsJsonObject("wants");
        ItemStack input = stack(wants, random);
        ItemStack secondary = definition.has("additional_wants")
                ? stack(definition.getAsJsonObject("additional_wants"), random) : ItemStack.EMPTY;
        ItemStack output = stack(definition.getAsJsonObject("gives"), random);
        if (input.isEmpty() || output.isEmpty() || definition.has("additional_wants") && secondary.isEmpty()) return null;
        int additionalCost = 0;
        if (definition.has("given_item_modifier")) {
            JsonElement modifier = definition.get("given_item_modifier");
            JsonArray modifiers;
            if (modifier.isJsonArray()) modifiers = modifier.getAsJsonArray();
            else {
                modifiers = new JsonArray();
                modifiers.add(modifier);
            }
            for (JsonElement element : modifiers) {
                JsonObject rule = element.getAsJsonObject();
                String type = rule.get("type").getAsString();
                if ("minecraft:set_potion".equals(type)) {
                    ResourceLocation id = new ResourceLocation(rule.get("id").getAsString());
                    if (!PotionType.REGISTRY.containsKey(id)) return null;
                    PotionUtils.addPotionToItemStack(output, PotionType.REGISTRY.getObject(id));
                } else if ("minecraft:enchant_with_levels".equals(type)) {
                    JsonObject levels = rule.getAsJsonObject("levels");
                    int minimum = levels.get("min").getAsInt();
                    int level = minimum + random.nextInt(levels.get("max").getAsInt() - minimum + 1);
                    for (EnchantmentData enchantment : enchantments(random, output, level)) {
                        output.addEnchantment(enchantment.enchantment, enchantment.enchantmentLevel);
                    }
                    if (additionalPrice(rule)) additionalCost = level;
                } else if ("minecraft:enchant_randomly".equals(type)) {
                    if (TRADEABLE_ENCHANTMENTS.isEmpty()) return null;
                    Enchantment enchantment = TRADEABLE_ENCHANTMENTS.get(random.nextInt(TRADEABLE_ENCHANTMENTS.size()));
                    int level = enchantment.getMinLevel() + random.nextInt(enchantment.getMaxLevel() - enchantment.getMinLevel() + 1);
                    if (output.getItem() == Items.BOOK || output.getItem() == Items.ENCHANTED_BOOK) {
                        output = new ItemStack(Items.ENCHANTED_BOOK);
                        ItemEnchantedBook.addEnchantment(output, new EnchantmentData(enchantment, level));
                    } else {
                        output.addEnchantment(enchantment, level);
                    }
                    if (additionalPrice(rule)) additionalCost = 2 + random.nextInt(5 + level * 10) + 3 * level;
                    if (definition.has("double_trade_price_enchantments")
                            && DOUBLE_PRICE_ENCHANTMENTS.contains(enchantment)) additionalCost *= 2;
                } else if ("minecraft:set_random_potion".equals(type)) {
                    if (TRADEABLE_POTIONS.isEmpty()) return null;
                    PotionUtils.addPotionToItemStack(output, TRADEABLE_POTIONS.get(random.nextInt(TRADEABLE_POTIONS.size())));
                } else if ("minecraft:set_stew_effect".equals(type)) {
                    if (!(output.getItem() instanceof ItemSuspiciousStew)) return null;
                    JsonArray effects = rule.getAsJsonArray("effects");
                    JsonObject selected = effects.get(random.nextInt(effects.size())).getAsJsonObject();
                    Potion effect = Potion.getPotionFromResourceLocation(selected.get("type").getAsString());
                    if (effect == null) return null;
                    int duration = selected.get("duration").getAsInt();
                    NBTTagCompound entry = new NBTTagCompound();
                    entry.setString("EffectId", effect.getRegistryName().toString());
                    entry.setInteger("Duration", effect.isInstant() ? duration : duration * 20);
                    NBTTagList entries = new NBTTagList();
                    entries.appendTag(entry);
                    output.setTagInfo("Effects", entries);
                } else if ("minecraft:set_random_dyes".equals(type)) {
                    if (!(output.getItem() instanceof ItemArmor)) return null;
                    int dyes = 1 + (random.nextFloat() < 0.75F ? 1 : 0) + (random.nextFloat() < 0.75F ? 1 : 0);
                    dyeArmor(output, random, dyes);
                } else if ("minecraft:exploration_map".equals(type)) {
                    if (merchant == null || !(merchant.world instanceof WorldServer)) return null;
                    BlockPos position = FFDExplorationMaps.find((WorldServer) merchant.world,
                            merchant.getPosition(), rule);
                    if (position == null) return null;
                    byte zoom = (byte) (rule.has("zoom") ? rule.get("zoom").getAsInt() : 2);
                    output = ItemMap.setupNewMap(merchant.world, position.getX(), position.getZ(), zoom, true, true);
                    ItemMap.renderBiomePreviewMap(merchant.world, output);
                    xy177.farmersfuturedelight.common.item.FFDMapDecorations.addTarget(output,
                            merchant.world, position, rule.has("decoration")
                                    ? rule.get("decoration").getAsString() : "minecraft:mansion");
                    String name = definition.getAsJsonObject("gives").get("id").getAsString();
                    output.setTranslatableName("ffd.map." + new ResourceLocation(name).getResourcePath());
                } else if ("minecraft:filtered".equals(type)) {
                    JsonObject predicates = rule.getAsJsonObject("item_filter").getAsJsonObject("predicates");
                    if (predicates.has("minecraft:map_id")) {
                        if (!(output.getItem() instanceof ItemMap)) return null;
                    } else if (predicates.has("minecraft:enchantments")
                            || predicates.has("minecraft:stored_enchantments")) {
                        if (!output.isItemEnchanted() && ItemEnchantedBook.getEnchantments(output).tagCount() == 0) return null;
                    } else {
                        if (REPORTED.add(rule.toString())) LOGGER.warn("Unsupported trade item filter {}; affected offer skipped", rule);
                        return null;
                    }
                } else {
                    if (REPORTED.add(type)) LOGGER.warn("Trade modifier {} still requires an adapter; affected offers are skipped", type);
                    return null;
                }
            }
        }
        int baseCount = input.getCount();
        if (baseCount + additionalCost < 1) return null;
        input.setCount(Math.min(input.getMaxStackSize(), baseCount + additionalCost));
        int maxUses = definition.has("max_uses") ? definition.get("max_uses").getAsInt() : 4;
        MerchantRecipe recipe = new MerchantRecipe(input, secondary, output, 0, maxUses);
        if (!modernVillager) return recipe;
        NBTTagCompound compound = recipe.writeToTags();
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("experience", definition.has("xp") ? definition.get("xp").getAsInt() : 1);
        data.setFloat("priceMultiplier", definition.has("reputation_discount") ? definition.get("reputation_discount").getAsFloat() : 0.0F);
        compound.setTag(FFDModernMerchantRecipe.DATA_TAG, data);
        return new FFDModernMerchantRecipe(compound);
    }

    private static boolean additionalPrice(JsonObject modifier) {
        return modifier.has("include_additional_cost_component") && modifier.get("include_additional_cost_component").getAsBoolean();
    }

    private static int chooseInt(JsonObject entry, String key, Random random, int defaultValue) {
        if (entry.has(key)) return chooseIntValue(entry.get(key), random, defaultValue);
        String capitalized = Character.toUpperCase(key.charAt(0)) + key.substring(1);
        String minimumKey = "min" + capitalized;
        String maximumKey = "max" + capitalized;
        if (entry.has(minimumKey) || entry.has(maximumKey)) {
            int minimum = entry.has(minimumKey) ? entry.get(minimumKey).getAsInt() : defaultValue;
            int maximum = entry.has(maximumKey) ? entry.get(maximumKey).getAsInt() : minimum;
            return chooseInt(minimum, maximum, random);
        }
        return defaultValue;
    }

    private static int chooseIntValue(JsonElement element, Random random, int defaultValue) {
        if (element.isJsonArray()) {
            JsonArray values = element.getAsJsonArray();
            if (values.size() == 0) throw new IllegalArgumentException("Trade choice set is empty");
            return values.get(random.nextInt(values.size())).getAsInt();
        }
        if (element.isJsonObject()) {
            JsonObject range = element.getAsJsonObject();
            int minimum = range.has("min") ? range.get("min").getAsInt() : defaultValue;
            int maximum = range.has("max") ? range.get("max").getAsInt() : minimum;
            return chooseInt(minimum, maximum, random);
        }
        return element.getAsInt();
    }

    private static int chooseInt(int minimum, int maximum, Random random) {
        if (minimum > maximum) throw new IllegalArgumentException("Trade range minimum exceeds maximum");
        return minimum + random.nextInt(maximum - minimum + 1);
    }

    private static NBTTagCompound chooseNbt(JsonElement element, Random random) {
        if (element.isJsonArray()) {
            JsonArray values = element.getAsJsonArray();
            if (values.size() == 0) throw new IllegalArgumentException("Trade NBT choice set is empty");
            return parseNbt(values.get(random.nextInt(values.size())).getAsString());
        }
        return parseNbt(element.getAsString());
    }

    private static NBTTagCompound parseNbt(String value) {
        try {
            return JsonToNBT.getTagFromJson(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid trade stack NBT", exception);
        }
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        try (InputStream stream = FFDTradeItemModifiers.class.getResourceAsStream(
                "/assets/farmers_future_delight/trades/trade_tags.json")) {
            if (stream == null) throw new IllegalStateException("Missing trade tags");
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement element : root.getAsJsonArray("tradedEquipment")) TRADED_EQUIPMENT.add(element.getAsString());
            for (JsonElement element : root.getAsJsonArray("enchantments")) {
                Enchantment enchantment = resolveEnchantment(element.getAsString());
                if (enchantment != null) TRADEABLE_ENCHANTMENTS.add(enchantment);
            }
            for (JsonElement element : root.getAsJsonArray("doublePrice")) {
                Enchantment enchantment = resolveEnchantment(element.getAsString());
                if (enchantment != null) DOUBLE_PRICE_ENCHANTMENTS.add(enchantment);
            }
            for (JsonElement element : root.getAsJsonArray("potions")) {
                ResourceLocation id = new ResourceLocation(element.getAsString());
                if (PotionType.REGISTRY.containsKey(id)) TRADEABLE_POTIONS.add(PotionType.REGISTRY.getObject(id));
            }
        } catch (Exception exception) {
            LOGGER.error("Cannot load vanilla trade tags", exception);
        }
    }

    @Nullable
    private static Enchantment resolveEnchantment(String name) {
        ResourceLocation id = new ResourceLocation(name);
        if ("minecraft:sweeping_edge".equals(name)) id = new ResourceLocation("sweeping");
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(id);
        return enchantment != null ? enchantment : ForgeRegistries.ENCHANTMENTS.getValue(
                new ResourceLocation("farmers_future_delight", id.getResourcePath()));
    }

    private static List<EnchantmentData> enchantments(Random random, ItemStack stack, int level) {
        List<EnchantmentData> result = new ArrayList<>();
        int enchantability = stack.getItem().getItemEnchantability(stack);
        if (enchantability <= 0) return result;
        level += 1 + random.nextInt(enchantability / 4 + 1) + random.nextInt(enchantability / 4 + 1);
        float variation = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        level = MathHelper.clamp(Math.round(level + level * variation), 1, Integer.MAX_VALUE);
        List<EnchantmentData> candidates = EnchantmentHelper.getEnchantmentDatas(level, stack, true);
        candidates.removeIf(enchantment -> {
            ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment.enchantment);
            String name = id == null ? "" : id.toString();
            if ("minecraft:sweeping".equals(name)) name = "minecraft:sweeping_edge";
            return !TRADED_EQUIPMENT.contains(name);
        });
        if (candidates.isEmpty()) return result;
        result.add(WeightedRandom.getRandomItem(random, candidates));
        while (random.nextInt(50) <= level) {
            EnchantmentHelper.removeIncompatible(candidates, result.get(result.size() - 1));
            if (candidates.isEmpty()) break;
            result.add(WeightedRandom.getRandomItem(random, candidates));
            level /= 2;
        }
        return result;
    }

    private static void dyeArmor(ItemStack stack, Random random, int count) {
        int red = 0;
        int green = 0;
        int blue = 0;
        int brightness = 0;
        for (int index = 0; index < count; index++) {
            float[] color = EnumDyeColor.byMetadata(random.nextInt(16)).getColorComponentValues();
            int colorRed = (int) (color[0] * 255.0F);
            int colorGreen = (int) (color[1] * 255.0F);
            int colorBlue = (int) (color[2] * 255.0F);
            red += colorRed;
            green += colorGreen;
            blue += colorBlue;
            brightness += Math.max(colorRed, Math.max(colorGreen, colorBlue));
        }
        red /= count;
        green /= count;
        blue /= count;
        float maximum = Math.max(red, Math.max(green, blue));
        float intensity = brightness / (float) count;
        red = (int) (red * intensity / maximum);
        green = (int) (green * intensity / maximum);
        blue = (int) (blue * intensity / maximum);
        ((ItemArmor) stack.getItem()).setColor(stack, red << 16 | green << 8 | blue);
    }
}
