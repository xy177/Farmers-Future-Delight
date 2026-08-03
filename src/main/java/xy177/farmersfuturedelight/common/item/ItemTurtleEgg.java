package xy177.farmersfuturedelight.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.block.BlockTurtleEgg;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemTurtleEgg extends ItemBlock {
    private final BlockTurtleEgg turtleEgg;

    public ItemTurtleEgg(BlockTurtleEgg block) {
        super(block);
        this.turtleEgg = block;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);
        if (FFDItems.isTurtleEnabled() && state.getBlock() == turtleEgg && !player.isSneaking()
                && state.getValue(BlockTurtleEgg.EGGS) < 4) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.isEmpty() || !player.canPlayerEdit(pos, facing, stack)) {
                return EnumActionResult.FAIL;
            }
            IBlockState nextState = state.withProperty(BlockTurtleEgg.EGGS,
                    state.getValue(BlockTurtleEgg.EGGS) + 1);
            AxisAlignedBB collision = nextState.getCollisionBoundingBox(world, pos);
            if (collision != null && !world.checkNoEntityCollision(collision.offset(pos))) {
                return EnumActionResult.FAIL;
            }
            if (!world.setBlockState(pos, nextState, 10)) {
                return EnumActionResult.FAIL;
            }
            SoundType sound = turtleEgg.getSoundType(nextState, world, pos, player);
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
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side,
                                       EntityPlayer player, ItemStack stack) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == turtleEgg && state.getValue(BlockTurtleEgg.EGGS) < 4) {
            return FFDItems.isTurtleEnabled();
        }
        return super.canPlaceBlockOnSide(world, pos, side, player, stack);
    }
}
