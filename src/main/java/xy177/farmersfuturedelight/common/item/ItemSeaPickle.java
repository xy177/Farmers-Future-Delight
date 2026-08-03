package xy177.farmersfuturedelight.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.SoundType;
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

import xy177.farmersfuturedelight.common.block.BlockSeaPickle;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemSeaPickle extends ItemUnderwaterPlant {
    private final BlockSeaPickle seaPickle;

    public ItemSeaPickle(BlockSeaPickle block) {
        super(block);
        this.seaPickle = block;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return FFDItems.isSeaPickleEnabled();
    }

    @Override
    protected IBlockState getPlacementState(World world, BlockPos pos) {
        return seaPickle.getPlacementState(world, pos);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);
        if (isFeatureEnabled() && state.getBlock() == seaPickle && !player.isSneaking()
                && state.getValue(BlockSeaPickle.PICKLES) < 4) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.isEmpty() || !player.canPlayerEdit(pos, facing, stack)) {
                return EnumActionResult.FAIL;
            }
            IBlockState next = state.withProperty(BlockSeaPickle.PICKLES,
                    state.getValue(BlockSeaPickle.PICKLES) + 1);
            AxisAlignedBB collision = next.getCollisionBoundingBox(world, pos);
            if (collision != null && !world.checkNoEntityCollision(collision.offset(pos))) {
                return EnumActionResult.FAIL;
            }
            if (!world.setBlockState(pos, next, 10)) {
                return EnumActionResult.FAIL;
            }
            SoundType sound = seaPickle.getSoundType(next, world, pos, player);
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
        if (state.getBlock() == seaPickle && state.getValue(BlockSeaPickle.PICKLES) < 4) {
            return isFeatureEnabled();
        }
        return super.canPlaceBlockOnSide(world, pos, side, player, stack);
    }
}
