package xy177.farmersfuturedelight.common.registry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FFDBannerPatterns {
    public static final BannerPattern GLOBE = globe();
    private static final Logger LOGGER = LogManager.getLogger("FFD Banner Patterns");
    private static Method quarkLayerCount;
    private static boolean checkedQuark;

    private FFDBannerPatterns() {
    }

    public static List<BannerPattern> ingredientPatterns(ItemStack stack) {
        List<BannerPattern> result = new ArrayList<>();
        if (stack.isEmpty()) return result;
        for (BannerPattern pattern : BannerPattern.values()) {
            if (pattern == BannerPattern.CREEPER || pattern == BannerPattern.SKULL
                    || pattern == BannerPattern.FLOWER || pattern == BannerPattern.MOJANG
                    || pattern == BannerPattern.BRICKS || pattern == BannerPattern.CURLY_BORDER) continue;
            if (pattern.hasPatternItem() && stack.isItemEqual(pattern.getPatternItem())) result.add(pattern);
        }
        return result;
    }

    public static int getLayerLimit() {
        if (!checkedQuark) {
            checkedQuark = true;
            if (Loader.isModLoaded("quark")) {
                try {
                    quarkLayerCount = Class.forName("vazkii.quark.decoration.feature.MoreBannerLayers")
                            .getMethod("getLayerCount");
                } catch (ReflectiveOperationException | LinkageError exception) {
                    LOGGER.warn("Cannot read Quark banner layer limit; using six layers", exception);
                }
            }
        }
        if (quarkLayerCount != null) {
            try {
                return Math.max(0, Math.min(32767, ((Number) quarkLayerCount.invoke(null)).intValue()));
            } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
                LOGGER.warn("Cannot read Quark banner layer limit; using six layers", exception);
                quarkLayerCount = null;
            }
        }
        return 6;
    }

    private static BannerPattern globe() {
        for (BannerPattern pattern : BannerPattern.values()) {
            if ("glb".equals(pattern.getHashname())) {
                return pattern;
            }
        }
        return EnumHelper.addEnum(BannerPattern.class, "FFD_GLOBE",
                new Class<?>[] {String.class, String.class, ItemStack.class},
                "globe", "glb", ItemStack.EMPTY);
    }
}
