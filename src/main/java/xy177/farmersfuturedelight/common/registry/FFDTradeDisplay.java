package xy177.farmersfuturedelight.common.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.village.MerchantRecipe;

public final class FFDTradeDisplay {
    private FFDTradeDisplay() {
    }

    @Nullable
    public static MerchantRecipe preview(JsonObject definition) {
        JsonObject copy = new JsonParser().parse(definition.toString()).getAsJsonObject();
        if (hasModifier(definition, "minecraft:exploration_map")) {
            copy.remove("given_item_modifier");
        }
        return FFDTradeItemModifiers.create(copy, new Random(0L), false);
    }

    public static boolean hasModifier(JsonObject definition, String type) {
        JsonElement modifiers = definition.get("given_item_modifier");
        if (modifiers == null) return false;
        if (modifiers.isJsonArray()) {
            for (JsonElement modifier : modifiers.getAsJsonArray()) {
                if (type.equals(modifier.getAsJsonObject().get("type").getAsString())) return true;
            }
            return false;
        }
        return type.equals(modifiers.getAsJsonObject().get("type").getAsString());
    }
}
