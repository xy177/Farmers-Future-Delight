package xy177.farmersfuturedelight.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

import xy177.farmersfuturedelight.common.block.BlockNetherVine;
import xy177.farmersfuturedelight.common.worldgen.FFDNetherBlockProvider;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public class ItemNetherVine extends ItemBlock {
    private final boolean growsUpward;

    public ItemNetherVine(BlockNetherVine block) {
        super(block);
        growsUpward = block.growsUpward();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing, float hitX,
                                      float hitY, float hitZ) {
        if (isHead(world.getBlockState(pos))) {
            return extendVine(player, world, pos, hand, facing);
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    public static EnumActionResult extendHeldVine(EntityPlayer player, World world,
                                                   BlockPos pos, EnumHand hand,
                                                   EnumFacing facing, boolean growsUpward) {
        ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof ItemNetherVine)
                || ((ItemNetherVine) stack.getItem()).growsUpward != growsUpward) {
            return EnumActionResult.PASS;
        }
        return ((ItemNetherVine) stack.getItem()).extendVine(
                player, world, pos, hand, facing);
    }

    private EnumActionResult extendVine(EntityPlayer player, World world, BlockPos start,
                                        EnumHand hand, EnumFacing facing) {
        IBlockState head = FFDNetherBlockProvider.get().vine(growsUpward, true);
        IBlockState body = FFDNetherBlockProvider.get().vine(growsUpward, false);
        if (head == null || body == null) {
            return EnumActionResult.FAIL;
        }
        if (world.getBlockState(start).getBlock() != head.getBlock()) {
            return EnumActionResult.PASS;
        }

        EnumFacing direction = growsUpward ? EnumFacing.UP : EnumFacing.DOWN;
        BlockPos target = start.offset(direction);
        if (FFDHeightHooks.isOutsideBuildHeight(world, target) || !world.isAirBlock(target)) {
            return EnumActionResult.FAIL;
        }

        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty() || !player.canPlayerEdit(target, direction, stack)
                || !world.mayPlace(head.getBlock(), target, false, direction, player)) {
            return EnumActionResult.FAIL;
        }

        BlockSnapshot targetSnapshot = BlockSnapshot.getBlockSnapshot(world, target);
        BlockSnapshot terminalSnapshot = BlockSnapshot.getBlockSnapshot(world, start);
        if (!world.setBlockState(target, head, 11)) {
            return EnumActionResult.FAIL;
        }
        if (ForgeEventFactory.onPlayerBlockPlace(player, targetSnapshot, facing, hand)
                .isCanceled()) {
            targetSnapshot.restore(true, false);
            terminalSnapshot.restore(true, false);
            return EnumActionResult.FAIL;
        }
        if (!world.setBlockState(start, body, 11)) {
            targetSnapshot.restore(true, false);
            terminalSnapshot.restore(true, false);
            return EnumActionResult.FAIL;
        }

        IBlockState placed = world.getBlockState(target);
        if (placed.getBlock() == head.getBlock()) {
            placed.getBlock().onBlockPlacedBy(world, target, placed, player, stack);
        }
        SoundType sound = placed.getBlock().getSoundType(placed, world, target, player);
        world.playSound(player, target, sound.getPlaceSound(), SoundCategory.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, target, stack);
        }
        player.addStat(StatList.getObjectUseStats(this));
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

    private boolean isHead(IBlockState state) {
        IBlockState head = FFDNetherBlockProvider.get().vine(growsUpward, true);
        return head != null && state.getBlock() == head.getBlock();
    }

    @Override
    public int getItemBurnTime(ItemStack stack) {
        return 0;
    }
}
