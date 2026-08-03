package xy177.farmersfuturedelight.common.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemNetherPlant extends ItemBlock {
    private final Block pottedBlock;

    public ItemNetherPlant(Block block, Block pottedBlock) {
        super(block);
        this.pottedBlock = pottedBlock;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing, float hitX,
                                      float hitY, float hitZ) {
        if (world.getBlockState(pos).getBlock() == Blocks.FLOWER_POT) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityFlowerPot
                    && ((TileEntityFlowerPot) tileEntity).getFlowerItemStack().isEmpty()) {
                world.setBlockState(pos, pottedBlock.getDefaultState(), 3);
                player.addStat(StatList.FLOWER_POTTED);
                if (!player.capabilities.isCreativeMode) {
                    player.getHeldItem(hand).shrink(1);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }
}
