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
            case "fish":
                return () -> FFDEntities.isLocalCodEnabled()
                        || FFDEntities.isLocalSalmonEnabled()
                        || FFDEntities.isLocalPufferfishEnabled()
                        || FFDEntities.isLocalTropicalFishEnabled();
            case "honey":
                return FFDItems::isHoneyEnabled;
            case "target":
                return () -> FFDItems.isItemRegistered(FFDItems.TARGET);
            case "ancient_debris":
                return () -> FFDItems.isItemRegistered(FFDItems.ANCIENT_DEBRIS);
            case "netherite":
                return () -> FFDItems.isItemRegistered(FFDItems.NETHERITE_CHESTPLATE);
            case "crying_obsidian":
                return () -> FFDItems.isItemRegistered(FFDItems.CRYING_OBSIDIAN);
            case "lodestone":
                return () -> FFDItems.isItemRegistered(FFDItems.LODESTONE);
            case "respawn_anchor":
                return () -> FFDItems.isItemRegistered(FFDItems.RESPAWN_ANCHOR);
            case "strider":
                return () -> FFDEntities.isLocalStriderEnabled()
                        && FFDItems.isItemRegistered(FFDItems.WARPED_FUNGUS_ON_A_STICK);
            case "piglin":
                return FFDEntities::isLocalPiglinEnabled;
            case "modern_nether":
                return () -> xy177.farmersfuturedelight.common.FFDConfig.modernNetherGenerationMode
                        != xy177.farmersfuturedelight.common.FFDConfig.FeatureMode.DISABLED;
            case "bastion":
                return () -> xy177.farmersfuturedelight.common.FFDConfig.bastionRemnantMode
                        != xy177.farmersfuturedelight.common.FFDConfig.FeatureMode.DISABLED;
            case "trident":
                return () -> FFDItems.isItemRegistered(FFDItems.TRIDENT);
            default:
                throw new JsonSyntaxException("Unknown Farmer's Future Delight feature: " + feature);
        }
    }
}
