package xy177.farmersfuturedelight.common.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.registry.FFDRaidContent;

public final class ItemOminousBottle extends Item {
    public static final String AMPLIFIER_TAG = "OminousBottleAmplifier";

    public ItemOminousBottle() {
        setCreativeTab(CreativeTabs.BREWING);
    }

    public static ItemStack create(int amplifier) {
        ItemStack stack = new ItemStack(FFDRaidContent.OMINOUS_BOTTLE);
        if (amplifier != 0) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger(AMPLIFIER_TAG, Math.max(0, Math.min(4, amplifier)));
            stack.setTagCompound(tag);
        }
        return stack;
    }

    public static int amplifier(ItemStack stack) {
        return stack.hasTagCompound() ? Math.max(0, Math.min(4,
                stack.getTagCompound().getInteger(AMPLIFIER_TAG))) : 0;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase consumer) {
        if (!world.isRemote) {
            if (consumer instanceof EntityPlayerMP) {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) consumer, stack);
            }
            consumer.addPotionEffect(new PotionEffect(FFDRaidContent.BAD_OMEN,
                    120000, amplifier(stack), false, false));
            world.playSound(null, consumer.posX, consumer.posY, consumer.posZ,
                    FFDRaidContent.OMINOUS_BOTTLE_DISPOSE, SoundCategory.PLAYERS, 1.0F,
                    0.9F + world.rand.nextFloat() * 0.1F);
            if (consumer instanceof EntityPlayer) {
                ((EntityPlayer) consumer).addStat(StatList.getObjectUseStats(this));
            }
        }
        if (!(consumer instanceof EntityPlayer)
                || !((EntityPlayer) consumer).capabilities.isCreativeMode) stack.shrink(1);
        return stack;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (int amplifier = 0; amplifier <= 4; amplifier++) items.add(create(amplifier));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               ITooltipFlag flag) {
        ItemStack effects = stack.copy();
        PotionUtils.appendEffects(effects, java.util.Collections.singletonList(
                new PotionEffect(FFDRaidContent.BAD_OMEN, 120000, amplifier(stack), false, false)));
        PotionUtils.addPotionTooltip(effects, tooltip, 1.0F);
    }
}
