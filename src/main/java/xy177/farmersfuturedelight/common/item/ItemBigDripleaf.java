package xy177.farmersfuturedelight.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

import xy177.farmersfuturedelight.common.block.BlockBigDripleaf;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.DripleafPlacement;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemBigDripleaf extends Item {
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos plantPos = DripleafPlacement.canReplace(world, pos) ? pos : pos.offset(facing);
        ItemStack stack = player.getHeldItem(hand);
        if (!FFDItems.isDripleafEnabled() || stack.isEmpty()
                || !player.canPlayerEdit(plantPos, facing, stack)
                || !DripleafPlacement.canPlaceHead(world, plantPos)) {
            return EnumActionResult.FAIL;
        }

        IBlockState below = world.getBlockState(plantPos.down());
        EnumFacing plantFacing = player.getHorizontalFacing().getOpposite();
        if (BlockBigDripleaf.isBigDripleaf(below)) {
            plantFacing = below.getValue(BlockBigDripleaf.FACING);
        } else if (below.getBlock() == FFDBlocks.BIG_DRIPLEAF_STEM) {
            plantFacing = below.getValue(BlockBigDripleafStem.FACING);
        }

        IBlockState placedState = DripleafPlacement.headState(world, plantPos, plantFacing);
        BlockSnapshot targetSnapshot = BlockSnapshot.getBlockSnapshot(world, plantPos);
        BlockSnapshot belowSnapshot = BlockSnapshot.getBlockSnapshot(world, plantPos.down());
        if (!world.setBlockState(plantPos, placedState, 11)) {
            return EnumActionResult.FAIL;
        }
        if (ForgeEventFactory.onPlayerBlockPlace(player, targetSnapshot, facing, hand).isCanceled()) {
            targetSnapshot.restore(true, false);
            belowSnapshot.restore(true, false);
            return EnumActionResult.FAIL;
        }
        world.setBlockState(plantPos, placedState, 11);

        IBlockState current = world.getBlockState(plantPos);
        SoundType sound = current.getBlock().getSoundType(current, world, plantPos, player);
        world.playSound(player, plantPos, sound.getPlaceSound(), SoundCategory.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, plantPos, stack);
        }
        player.addStat(StatList.getObjectUseStats(this));
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        RayTraceResult hit = rayTrace(world, player, false);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }
        EnumActionResult result = onItemUse(player, world, hit.getBlockPos(), hand, hit.sideHit,
                0.5F, 0.5F, 0.5F);
        return new ActionResult<ItemStack>(result, stack);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (FFDItems.isDripleafEnabled() && isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
        }
    }
}
