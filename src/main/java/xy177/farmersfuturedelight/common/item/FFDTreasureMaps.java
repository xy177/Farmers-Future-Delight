package xy177.farmersfuturedelight.common.item;

import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;

public final class FFDTreasureMaps {
    private FFDTreasureMaps() {
    }

    public static ItemStack create(World world, BlockPos target) {
        ItemStack map = ItemMap.setupNewMap(world, target.getX(), target.getZ(),
                (byte) 2, true, true);
        ItemMap.renderBiomePreviewMap(world, map);
        MapData.addTargetDecoration(map, target, "+", MapDecoration.Type.TARGET_X);
        map.setTranslatableName("filled_map.buried_treasure");
        return map;
    }
}
