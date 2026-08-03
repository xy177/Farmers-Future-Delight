package xy177.farmersfuturedelight.common.registry;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class FFDRecipes {
    private FFDRecipes() {
    }

    public static void init() {
        if (FFDItems.isKelpEnabled()) {
            GameRegistry.addSmelting(FFDItems.KELP, new ItemStack(FFDItems.DRIED_KELP), 0.1F);
        }
        if (FFDItems.isSeaPickleEnabled()) {
            GameRegistry.addSmelting(FFDItems.SEA_PICKLE,
                    new ItemStack(Items.DYE, 1, EnumDyeColor.LIME.getDyeDamage()), 0.1F);
        }
    }
}
