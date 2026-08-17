package xy177.farmersfuturedelight.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class StackableBlockItemPlacement {
    private StackableBlockItemPlacement() {
    }

    static EnumActionResult tryStack(EntityPlayer player, World world, BlockPos clickedPos,
                                     EnumFacing facing, EnumHand hand, Block block,
                                     PropertyInteger countProperty, int maximum, int updateFlags) {
        if (player.isSneaking()) {
            return EnumActionResult.PASS;
        }
        EnumActionResult clicked = tryStackAt(player, world, clickedPos, facing, hand, block,
                countProperty, maximum, updateFlags);
        if (clicked != EnumActionResult.PASS) {
            return clicked;
        }

        IBlockState clickedState = world.getBlockState(clickedPos);
        BlockPos target = clickedState.getBlock().isReplaceable(world, clickedPos)
                ? clickedPos : clickedPos.offset(facing);
        if (target.equals(clickedPos)) {
            return EnumActionResult.PASS;
        }
        return tryStackAt(player, world, target, facing, hand, block, countProperty,
                maximum, updateFlags);
    }

    static EnumActionResult tryStackAt(EntityPlayer player, World world, BlockPos pos,
                                       EnumFacing facing, EnumHand hand, Block block,
                                       PropertyInteger countProperty, int maximum,
                                       int updateFlags) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != block) {
            return EnumActionResult.PASS;
        }

        int count = state.getValue(countProperty);
        if (count >= maximum) {
            return EnumActionResult.PASS;
        }

        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty() || !player.canPlayerEdit(pos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        IBlockState next = state.withProperty(countProperty, count + 1);
        AxisAlignedBB collision = next.getCollisionBoundingBox(world, pos);
        if (collision != null && !world.checkNoEntityCollision(collision.offset(pos))) {
            return EnumActionResult.FAIL;
        }
        if (!world.setBlockState(pos, next, updateFlags)) {
            return EnumActionResult.FAIL;
        }

        SoundType sound = block.getSoundType(next, world, pos, player);
        world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
        }
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }
}
