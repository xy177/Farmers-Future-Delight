package xy177.farmersfuturedelight.common;

import net.minecraftforge.fml.common.Loader;

public final class FFDCompat {
    private FFDCompat() {
    }

    private static boolean isDelightModId(String modId) {
        return "brewinandchewinlegacy".equals(modId) || modId.contains("delight");
    }

    public static boolean isEnabled(FFDConfig.FeatureMode mode, String... providerModIds) {
        if (mode == FFDConfig.FeatureMode.ENABLED) {
            return true;
        }
        if (mode == FFDConfig.FeatureMode.DISABLED) {
            return false;
        }
        for (String modId : providerModIds) {
            if (!isDelightModId(modId) && Loader.isModLoaded(modId)) {
                return false;
            }
        }
        return true;
    }
}
