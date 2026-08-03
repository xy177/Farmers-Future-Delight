package xy177.farmersfuturedelight.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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

public abstract class ItemUnderwaterPlant extends ItemBlock {
    protected ItemUnderwaterPlant(Block block) {
        super(block);
        setMaxDamage(0);
    }

    protected abstract boolean isFeatureEnabled();

    protected IBlockState getPlacementState(World world, BlockPos pos) {
        return block.getDefaultState();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        return place(player, world, pos.offset(facing), facing, hand);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        RayTraceResult hit = rayTrace(world, player, false);
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            EnumActionResult result = place(player, world, hit.getBlockPos().offset(hit.sideHit),
                    hit.sideHit, hand);
            return new ActionResult<ItemStack>(result, stack);
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    protected EnumActionResult place(EntityPlayer player, World world, BlockPos pos,
                                     EnumFacing side, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!isFeatureEnabled() || stack.isEmpty() || !player.canPlayerEdit(pos, side, stack)
                || !block.canPlaceBlockAt(world, pos)) {
            return EnumActionResult.FAIL;
        }

        IBlockState state = getPlacementState(world, pos);
        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, pos);
        if (!world.setBlockState(pos, state, 11)) {
            return EnumActionResult.FAIL;
        }
        if (ForgeEventFactory.onPlayerBlockPlace(player, snapshot, side, hand).isCanceled()) {
            snapshot.restore(true, false);
            return EnumActionResult.FAIL;
        }
        world.setBlockState(pos, state, 11);

        SoundType sound = state.getBlock().getSoundType(state, world, pos, player);
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

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isFeatureEnabled() && isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
        }
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side,
                                       EntityPlayer player, ItemStack stack) {
        IBlockState clicked = world.getBlockState(pos);
        BlockPos target = clicked.getBlock().isReplaceable(world, pos) ? pos : pos.offset(side);
        return isFeatureEnabled() && block.canPlaceBlockAt(world, target);
    }
}
