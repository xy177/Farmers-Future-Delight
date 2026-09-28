package xy177.farmersfuturedelight.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side,
                                       EntityPlayer player, ItemStack stack) {
        IBlockState state = world.getBlockState(pos);
        if (!player.isSneaking() && state.getBlock() == candle
                && state.getValue(BlockCandle.CANDLES) < 4) {
            return true;
        }
        if (!player.isSneaking() && !state.getBlock().isReplaceable(world, pos)) {
            IBlockState adjacent = world.getBlockState(pos.offset(side));
            if (adjacent.getBlock() == candle && adjacent.getValue(BlockCandle.CANDLES) < 4) {
                return true;
            }
        }
        return super.canPlaceBlockOnSide(world, pos, side, player, stack);
    }

    @Override
    public int getItemBurnTime(net.minecraft.item.ItemStack stack) {
        return 0;
    }
}
