package xy177.farmersfuturedelight.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.entity.EntityStrider;

public final class ItemWarpedFungusOnAStick extends Item {
    public ItemWarpedFungusOnAStick() { setMaxStackSize(1); setMaxDamage(100); }

    @Override public boolean isFull3D() { return true; }
    @Override public boolean shouldRotateAroundWhenRendering() { return true; }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (!world.isRemote && player.getRidingEntity() instanceof EntityStrider
                && ((EntityStrider) player.getRidingEntity()).canBeSteered()
                && ((EntityStrider) player.getRidingEntity()).boost()) {
            net.minecraft.nbt.NBTTagCompound data = held.hasTagCompound() ? held.getTagCompound().copy() : null;
            held.damageItem(1, player);
            if (player instanceof net.minecraft.entity.player.EntityPlayerMP) {
                xy177.farmersfuturedelight.common.advancement.FFDAdvancements.RIDE_STRIDER
                        .trigger((net.minecraft.entity.player.EntityPlayerMP) player);
            }
            if (held.isEmpty() || held.getItemDamage() >= held.getMaxDamage()) {
                held = new ItemStack(Items.FISHING_ROD); held.setTagCompound(data);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, held);
        }
        player.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(EnumActionResult.PASS, held);
    }
}
