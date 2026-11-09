package xy177.farmersfuturedelight.common.item;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.EnumHelper;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class ItemLeatherHorseArmor extends Item {
    public static final int DEFAULT_COLOR = 0xA06540;
    private static final HorseArmorType ARMOR_TYPE = EnumHelper.addHorseArmor(
            "FFD_LEATHER", null, 3);

    public ItemLeatherHorseArmor() {
        setMaxStackSize(1);
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    @Override
    public HorseArmorType getHorseArmorType(ItemStack stack) {
        return ARMOR_TYPE;
    }

    @Override
    public String getHorseArmorTexture(EntityLiving wearer, ItemStack stack) {
        return null;
    }

    public static boolean hasColor(ItemStack stack) {
        NBTTagCompound display = stack.getSubCompound("display");
        return display != null && display.hasKey("color", 99);
    }

    public static int getColor(ItemStack stack) {
        return hasColor(stack) ? stack.getSubCompound("display").getInteger("color") & 0xFFFFFF
                : DEFAULT_COLOR;
    }

    public static void setColor(ItemStack stack, int color) {
        stack.getOrCreateSubCompound("display").setInteger("color", color & 0xFFFFFF);
    }

    public static void removeColor(ItemStack stack) {
        NBTTagCompound display = stack.getSubCompound("display");
        if (display != null) display.removeTag("color");
    }
}
