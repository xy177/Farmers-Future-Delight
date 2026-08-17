package xy177.farmersfuturedelight.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.block.BlockCandle;

public class ItemCandle extends ItemBlock {
    private final BlockCandle candle;

    public ItemCandle(BlockCandle block) {
        super(block);
        candle = block;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing, float hitX,
                                      float hitY, float hitZ) {
        EnumActionResult stacked = StackableBlockItemPlacement.tryStack(player, world, pos,
                facing, hand, candle, BlockCandle.CANDLES, 4, 11);
        return stacked == EnumActionResult.PASS
                ? super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ)
                : stacked;
    }

    @Override
    public int getItemBurnTime(net.minecraft.item.ItemStack stack) {
        return 0;
    }
}
