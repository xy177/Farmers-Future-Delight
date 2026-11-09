package xy177.farmersfuturedelight.common.item;

import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class FFDMapItemAccess extends ItemMap {
    private FFDMapItemAccess() {
    }

    public static void scaleMap(ItemStack stack, World world) {
        scaleMap(stack, world, 1);
    }
}
