package xy177.farmersfuturedelight.common.advancement;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDFeatureConditionFactory implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        String feature = JsonUtils.getString(json, "feature");
        switch (feature) {
            case "axolotl":
                return FFDEntities::isLocalAxolotlEnabled;
            case "copper":
                return FFDItems::isCopperEnabled;
            case "goat":
                return FFDItems::isGoatEnabled;
            case "powder_snow":
                return FFDItems::isPowderSnowEnabled;
            default:
                throw new JsonSyntaxException("Unknown Farmer's Future Delight feature: " + feature);
        }
    }
}
