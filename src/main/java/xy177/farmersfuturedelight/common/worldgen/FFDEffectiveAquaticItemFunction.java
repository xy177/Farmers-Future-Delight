package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDEffectiveAquaticItemFunction extends LootFunction {
    private final String item;

    private FFDEffectiveAquaticItemFunction(LootCondition[] conditions, String item) {
        super(conditions);
        this.item = item;
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
        Item local = "heart_of_the_sea".equals(item) ? FFDItems.HEART_OF_THE_SEA : null;
        ItemStack effective = local == null ? ItemStack.EMPTY : FFDItems.effectiveStack(local);
        return effective.isEmpty() ? stack : effective;
    }

    public static final class Serializer extends LootFunction.Serializer<FFDEffectiveAquaticItemFunction> {
        public Serializer() {
            super(new ResourceLocation(FarmerFutureDelight.MODID, "effective_aquatic_item"),
                    FFDEffectiveAquaticItemFunction.class);
        }

        @Override
        public void serialize(JsonObject object, FFDEffectiveAquaticItemFunction function,
                              JsonSerializationContext context) {
            object.addProperty("item", function.item);
        }

        @Override
        public FFDEffectiveAquaticItemFunction deserialize(JsonObject object,
                                                           JsonDeserializationContext context,
                                                           LootCondition[] conditions) {
            return new FFDEffectiveAquaticItemFunction(conditions,
                    JsonUtils.getString(object, "item"));
        }
    }
}
