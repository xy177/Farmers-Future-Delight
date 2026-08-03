package xy177.farmersfuturedelight.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemSweetBerries extends ItemFood {
    public ItemSweetBerries() {
        super(2, 0.1F, false);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!FFDItems.isSweetBerryEnabled()) {
            return EnumActionResult.PASS;
        }
        IBlockState clickedState = world.getBlockState(pos);
        BlockPos plantPos = clickedState.getBlock().isReplaceable(world, pos)
                ? pos : pos.offset(facing);
        IBlockState replacedState = world.getBlockState(plantPos);
        IBlockState state = FFDBlocks.SWEET_BERRY_BUSH.getDefaultState();
        if (!player.canPlayerEdit(plantPos, facing, player.getHeldItem(hand))
                || !replacedState.getBlock().isReplaceable(world, plantPos)
                || !FFDBlocks.SWEET_BERRY_BUSH.canPlaceBlockAt(world, plantPos)) {
            return EnumActionResult.FAIL;
        }
        if (!world.isRemote) {
            world.setBlockState(plantPos, state, 3);
            SoundType sound = state.getBlock().getSoundType(state, world, plantPos, player);
            world.playSound(null, plantPos, sound.getPlaceSound(), SoundCategory.BLOCKS,
                    (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            ItemStack held = player.getHeldItem(hand);
            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, plantPos, held);
            }
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
