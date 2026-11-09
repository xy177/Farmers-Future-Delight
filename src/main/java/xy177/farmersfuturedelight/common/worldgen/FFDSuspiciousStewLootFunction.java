package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDSuspiciousStewLootFunction extends LootFunction {
    private static final Effect[] EFFECTS = {
            new Effect("night_vision", 7, 10),
            new Effect("jump_boost", 7, 10),
            new Effect("weakness", 6, 8),
            new Effect("blindness", 5, 7),
            new Effect("poison", 10, 20),
            new Effect("saturation", 7, 10)
    };

    public FFDSuspiciousStewLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    public ItemStack apply(ItemStack stack, Random random, LootContext context) {
        Effect selected = EFFECTS[random.nextInt(EFFECTS.length)];
        int duration = selected.minimum + random.nextInt(selected.maximum - selected.minimum + 1);
        if (!"saturation".equals(selected.id)) {
            duration *= 20;
        }
        net.minecraft.potion.Potion potion =
                net.minecraft.potion.Potion.getPotionFromResourceLocation(selected.id);
        ItemStack stew = FFDItems.effectiveStack(FFDItems.SUSPICIOUS_STEW);
        if (stew.isEmpty() || potion == null) {
            return stack;
        }
        if (stew.getItem() == FFDItems.SUSPICIOUS_STEW) {
            return ((xy177.farmersfuturedelight.common.item.ItemSuspiciousStew) stew.getItem())
                    .createStack(potion, duration);
        }
        return PotionUtils.appendEffects(stew, java.util.Collections.singletonList(
                new PotionEffect(potion, duration)));
    }

    public static final class Serializer extends LootFunction.Serializer<FFDSuspiciousStewLootFunction> {
        public Serializer() {
            super(new ResourceLocation(FarmerFutureDelight.MODID, "suspicious_stew_effect"),
                    FFDSuspiciousStewLootFunction.class);
        }

        @Override
        public void serialize(JsonObject object, FFDSuspiciousStewLootFunction function,
                              JsonSerializationContext context) {
        }

        @Override
        public FFDSuspiciousStewLootFunction deserialize(JsonObject object,
                                                          JsonDeserializationContext context,
                                                          LootCondition[] conditions) {
            return new FFDSuspiciousStewLootFunction(conditions);
        }
    }

    private static final class Effect {
        private final String id;
        private final int minimum;
        private final int maximum;

        private Effect(String id, int minimum, int maximum) {
            this.id = id;
            this.minimum = minimum;
            this.maximum = maximum;
        }
    }
}
