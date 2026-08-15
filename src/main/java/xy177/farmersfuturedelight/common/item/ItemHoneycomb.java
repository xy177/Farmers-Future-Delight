package xy177.farmersfuturedelight.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.block.CopperWeathering;

public class ItemHoneycomb extends Item {
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing, float hitX,
                                      float hitY, float hitZ) {
        return CopperWeathering.tryWax(player, world, pos, hand, facing);
    }
}
