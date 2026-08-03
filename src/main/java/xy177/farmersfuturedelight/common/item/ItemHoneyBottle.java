package xy177.farmersfuturedelight.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemHoneyBottle extends ItemFood {
    public ItemHoneyBottle() {
        super(6, 0.1F, false);
        setMaxStackSize(16);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        return new ItemStack(Items.GLASS_BOTTLE);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase consumer) {
        ItemStack remaining = super.onItemUseFinish(stack, world, consumer);
        if (!world.isRemote) {
            consumer.removePotionEffect(MobEffects.POISON);
        }
        if (remaining.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }

        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (consumer instanceof EntityPlayer
                && !((EntityPlayer) consumer).capabilities.isCreativeMode
                && !((EntityPlayer) consumer).inventory.addItemStackToInventory(bottle)) {
            ((EntityPlayer) consumer).dropItem(bottle, false);
        }
        return remaining;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (FFDItems.isHoneyEnabled()) {
            super.getSubItems(tab, items);
        }
    }
}
