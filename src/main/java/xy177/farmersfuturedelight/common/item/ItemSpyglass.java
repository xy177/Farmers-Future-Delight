package xy177.farmersfuturedelight.common.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;

public class ItemSpyglass extends Item {
    public ItemSpyglass() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1200;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.NONE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player,
                                                    EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        if (!world.isRemote) {
            world.playSound(null, player.getPosition(), FFDSounds.SPYGLASS_USE,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
            player.addStat(StatList.getObjectUseStats(this));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity,
                                     int timeLeft) {
        playStopSound(world, entity);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        playStopSound(world, entity);
        return stack;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
        if (entity instanceof EntityPlayerMP) {
            FFDAdvancements.checkSpyglassTarget((EntityPlayerMP) entity);
        }
    }

    private static void playStopSound(World world, EntityLivingBase entity) {
        if (!world.isRemote) {
            world.playSound(null, entity.getPosition(), FFDSounds.SPYGLASS_STOP_USING,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }
}
