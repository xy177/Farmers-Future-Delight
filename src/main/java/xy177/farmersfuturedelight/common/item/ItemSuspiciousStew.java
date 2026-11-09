package xy177.farmersfuturedelight.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSoup;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class ItemSuspiciousStew extends ItemSoup {
    public ItemSuspiciousStew() {
        super(6);
        setAlwaysEdible();
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    public ItemStack createStack(Potion effect, int duration) {
        ItemStack stack = new ItemStack(this);
        NBTTagCompound effectTag = new NBTTagCompound();
        effectTag.setString("EffectId", effect.getRegistryName().toString());
        effectTag.setInteger("Duration", duration);
        NBTTagList effects = new NBTTagList();
        effects.appendTag(effectTag);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("Effects", effects);
        stack.setTagCompound(tag);
        return stack;
    }

    public static List<PotionEffect> getEffects(ItemStack stack) {
        List<PotionEffect> result = new ArrayList<>();
        if (stack.getTagCompound() == null) {
            return result;
        }
        NBTTagList effects = stack.getTagCompound().getTagList("Effects", 10);
        for (int index = 0; index < effects.tagCount(); index++) {
            NBTTagCompound entry = effects.getCompoundTagAt(index);
            if (!entry.hasKey("EffectId", 8)) {
                continue;
            }
            Potion effect;
            try {
                effect = Potion.getPotionFromResourceLocation(entry.getString("EffectId"));
            } catch (IllegalArgumentException exception) {
                continue;
            }
            int duration = entry.hasKey("Duration", 99) ? entry.getInteger("Duration") : 160;
            if (effect != null && duration > 0) {
                result.add(new PotionEffect(effect, duration));
            }
        }
        return result;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        super.onFoodEaten(stack, world, player);
        if (world.isRemote) {
            return;
        }
        for (PotionEffect effect : getEffects(stack)) {
            player.addPotionEffect(effect);
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(createStack(MobEffects.SATURATION, 7));
            items.add(createStack(MobEffects.NIGHT_VISION, 100));
            items.add(createStack(MobEffects.FIRE_RESISTANCE, 60));
            items.add(createStack(MobEffects.BLINDNESS, 220));
            items.add(createStack(MobEffects.WEAKNESS, 140));
            items.add(createStack(MobEffects.REGENERATION, 140));
            items.add(createStack(MobEffects.JUMP_BOOST, 100));
            items.add(createStack(MobEffects.POISON, 220));
            items.add(createStack(MobEffects.WITHER, 140));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative) {
            PotionUtils.addPotionTooltip(PotionUtils.appendEffects(new ItemStack(Items.POTIONITEM),
                    getEffects(stack)), tooltip, 1.0F);
        }
    }
}
