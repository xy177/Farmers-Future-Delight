package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.item.FFDTreasureMaps;

public final class FFDBuriedTreasureMapFunction extends LootFunction {
    private static final ThreadLocal<BlockPos> ORIGIN = new ThreadLocal<>();

    private FFDBuriedTreasureMapFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
        BlockPos origin = ORIGIN.get();
        if (origin == null) {
            return stack;
        }
        BlockPos target = FFDOceanStructureLocator.findBuriedTreasure(
                context.getWorld(), origin, 100);
        return target == null ? stack : FFDTreasureMaps.create(context.getWorld(), target);
    }

    static void begin(BlockPos origin) {
        ORIGIN.set(origin);
    }

    static void end() {
        ORIGIN.remove();
    }

    public static final class Serializer extends LootFunction.Serializer<FFDBuriedTreasureMapFunction> {
        public Serializer() {
            super(new ResourceLocation(FarmerFutureDelight.MODID, "buried_treasure_map"),
                    FFDBuriedTreasureMapFunction.class);
        }

        @Override
        public void serialize(JsonObject object, FFDBuriedTreasureMapFunction function,
                              JsonSerializationContext context) {
        }

        @Override
        public FFDBuriedTreasureMapFunction deserialize(JsonObject object,
                                                        JsonDeserializationContext context,
                                                        LootCondition[] conditions) {
            return new FFDBuriedTreasureMapFunction(conditions);
        }
    }
}
