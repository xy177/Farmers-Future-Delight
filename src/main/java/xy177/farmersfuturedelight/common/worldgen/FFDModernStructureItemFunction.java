package xy177.farmersfuturedelight.common.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDModernItemLookup;

public final class FFDModernStructureItemFunction extends LootFunction {
    private final String item;
    private final List<String> enchantments;
    private final int[] instruments;

    private FFDModernStructureItemFunction(LootCondition[] conditions, String item,
                                            List<String> enchantments, int[] instruments) {
        super(conditions);
        this.item = item;
        this.enchantments = enchantments;
        this.instruments = instruments;
    }

    @Override
    public ItemStack apply(ItemStack stack, Random random, LootContext context) {
        if (!item.isEmpty()) {
            int count = stack.getCount();
            stack = FFDModernItemLookup.resolve(item);
            if (stack.isEmpty()) return stack;
            stack.setCount(count);
        }
        if (stack.isEmpty()) return stack;
        if (!enchantments.isEmpty()) {
            List<Enchantment> eligible = new ArrayList<>();
            for (String name : enchantments) {
                ResourceLocation id = new ResourceLocation("minecraft:sweeping_edge".equals(name) ? "minecraft:sweeping" : name);
                Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(id);
                if (enchantment == null && "minecraft".equals(id.getResourceDomain())) {
                    enchantment = ForgeRegistries.ENCHANTMENTS.getValue(
                            new ResourceLocation(FarmerFutureDelight.MODID, id.getResourcePath()));
                }
                if (enchantment != null && (stack.getItem() == Items.BOOK || enchantment.canApply(stack))) eligible.add(enchantment);
            }
            if (!eligible.isEmpty()) {
                Enchantment enchantment = eligible.get(random.nextInt(eligible.size()));
                int level = enchantment.getMinLevel()
                        + random.nextInt(enchantment.getMaxLevel() - enchantment.getMinLevel() + 1);
                if (stack.getItem() == Items.BOOK) stack = ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(enchantment, level));
                else stack.addEnchantment(enchantment, level);
            }
        }
        if (instruments.length > 0 && stack.getItem() instanceof xy177.farmersfuturedelight.common.item.ItemGoatHorn) {
            stack.setItemDamage(instruments[random.nextInt(instruments.length)]);
        }
        return stack;
    }

    public static final class Serializer extends LootFunction.Serializer<FFDModernStructureItemFunction> {
        public Serializer() {
            super(new ResourceLocation(FarmerFutureDelight.MODID, "modern_structure_item"), FFDModernStructureItemFunction.class);
        }

        @Override
        public void serialize(JsonObject object, FFDModernStructureItemFunction function, JsonSerializationContext context) {
            if (!function.item.isEmpty()) object.addProperty("item", function.item);
            if (!function.enchantments.isEmpty()) {
                JsonArray entries = new JsonArray();
                for (String name : function.enchantments) entries.add(name);
                object.add("enchantments", entries);
            }
            if (function.instruments.length > 0) {
                JsonArray entries = new JsonArray();
                for (int instrument : function.instruments) entries.add(instrument);
                object.add("instruments", entries);
            }
        }

        @Override
        public FFDModernStructureItemFunction deserialize(JsonObject object, JsonDeserializationContext context,
                                                           LootCondition[] conditions) {
            String item = object.has("item") ? object.get("item").getAsString() : "";
            List<String> enchantments = new ArrayList<>();
            if (object.has("enchantments")) {
                for (JsonElement entry : object.getAsJsonArray("enchantments")) {
                    String value = entry.getAsString();
                    new ResourceLocation(value);
                    enchantments.add(value);
                }
            }
            int[] instruments = new int[object.has("instruments") ? object.getAsJsonArray("instruments").size() : 0];
            for (int index = 0; index < instruments.length; index++) {
                instruments[index] = object.getAsJsonArray("instruments").get(index).getAsInt();
                if (instruments[index] < 0 || instruments[index] >= xy177.farmersfuturedelight.common.item.ItemGoatHorn.INSTRUMENT_COUNT) {
                    throw new com.google.gson.JsonSyntaxException("Invalid goat horn instrument");
                }
            }
            return new FFDModernStructureItemFunction(conditions, item, enchantments, instruments);
        }
    }
}
