package xy177.farmersfuturedelight.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class ItemGoatHorn extends Item {
    public static final int INSTRUMENT_COUNT = 8;
    public static final int USE_TICKS = 140;

    private static final String[] INSTRUMENT_NAMES = {
            "ponder_goat_horn",
            "sing_goat_horn",
            "seek_goat_horn",
            "feel_goat_horn",
            "admire_goat_horn",
            "call_goat_horn",
            "yearn_goat_horn",
            "dream_goat_horn"
    };

    public ItemGoatHorn() {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
        addPropertyOverride(new ResourceLocation("tooting"),
                (stack, world, entity) -> entity != null && entity.isHandActive()
                        && entity.getActiveItemStack() == stack ? 1.0F : 0.0F);
    }

    @Override
    public int getMetadata(int damage) {
        return normalizeInstrument(damage);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player,
                                                    EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.getCooldownTracker().hasCooldown(this)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        player.setActiveHand(hand);
        if (!world.isRemote) {
            int instrument = normalizeInstrument(stack.getMetadata());
            world.playSound(null, player.posX, player.posY, player.posZ,
                    FFDSounds.GOAT_HORN_SOUNDS[instrument], SoundCategory.RECORDS,
                    16.0F, 1.0F);
            player.getCooldownTracker().setCooldown(this, USE_TICKS);
            player.addStat(StatList.getObjectUseStats(this));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        return stack;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!FFDItems.isGoatEnabled() || !isInCreativeTab(tab)) {
            return;
        }
        for (int instrument = 0; instrument < INSTRUMENT_COUNT; instrument++) {
            items.add(new ItemStack(this, 1, instrument));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               ITooltipFlag flag) {
        int instrument = normalizeInstrument(stack.getMetadata());
        tooltip.add(TextFormatting.GRAY + I18n.format(
                "instrument.farmers_future_delight." + INSTRUMENT_NAMES[instrument]));
    }

    public static int normalizeInstrument(int instrument) {
        return Math.max(0, Math.min(INSTRUMENT_COUNT - 1, instrument));
    }
}
