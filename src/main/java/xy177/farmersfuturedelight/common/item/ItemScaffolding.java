package xy177.farmersfuturedelight.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.block.BlockScaffolding;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public class ItemScaffolding extends ItemBlock {
    public ItemScaffolding(Block block) {
        super(block);
    }

    @Override
    public int getItemBurnTime(ItemStack stack) {
        return 50;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
            EnumFacing face, float hitX, float hitY, float hitZ) {
        BlockPos destination = placementPosition(world, pos, face, player);
        ItemStack stack = player.getHeldItem(hand);
        if (destination == null || stack.isEmpty() || !player.canPlayerEdit(destination, face, stack)
                || !world.getWorldBorder().contains(destination)) {
            return EnumActionResult.FAIL;
        }
        IBlockState state = block.getStateForPlacement(world, destination, face,
                hitX, hitY, hitZ, 0, player, hand);
        if (!placeBlockAt(stack, player, world, destination, face, hitX, hitY, hitZ, state)) {
            return EnumActionResult.FAIL;
        }
        SoundType sound = block.getSoundType(state, world, destination, player);
        world.playSound(player, destination, sound.getPlaceSound(), SoundCategory.BLOCKS,
                (sound.getVolume() + 1) / 2, sound.getPitch() * 0.8F);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

    public BlockPos placementPosition(World world, BlockPos pos, EnumFacing face, EntityPlayer player) {
        if (world.getBlockState(pos).getBlock() == block) {
            boolean inside = new AxisAlignedBB(pos).contains(player.getPositionEyes(1.0F));
            EnumFacing direction = player.isSneaking() ? (inside ? face.getOpposite() : face)
                    : face == EnumFacing.UP ? player.getHorizontalFacing() : EnumFacing.UP;
            BlockPos destination = pos.offset(direction);
            int horizontalDistance = 0;
            while (horizontalDistance < 7 && !FFDHeightHooks.isOutsideBuildHeight(world, destination)) {
                IBlockState replaced = world.getBlockState(destination);
                if (replaced.getBlock() != block) {
                    return replaced.getBlock().isReplaceable(world, destination) ? destination : null;
                }
                destination = destination.offset(direction);
                if (direction.getAxis().isHorizontal()) {
                    horizontalDistance++;
                }
            }
            if (!world.isRemote && destination.getY() >= FFDHeightHooks.maxYExclusive(world)) {
                player.sendStatusMessage(new TextComponentTranslation("build.tooHigh",
                        FFDHeightHooks.maxYExclusive(world) - 1).setStyle(
                                new net.minecraft.util.text.Style().setColor(TextFormatting.RED)), true);
            }
            return null;
        }
        BlockPos destination = world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                ? pos : pos.offset(face);
        return !FFDHeightHooks.isOutsideBuildHeight(world, destination)
                && world.getBlockState(destination).getBlock().isReplaceable(world, destination)
                && BlockScaffolding.distance(world, destination) < 7 ? destination : null;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing face,
            EntityPlayer player, ItemStack stack) {
        return placementPosition(world, pos, face, player) != null;
    }
}
