package xy177.farmersfuturedelight.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.block.BlockHangingRoots;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemHangingRoots extends ItemBlock {
    public ItemHangingRoots(BlockHangingRoots block) {
        super(block);
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
        if (FFDItems.isHangingRootsEnabled() && isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
        }
    }
}
