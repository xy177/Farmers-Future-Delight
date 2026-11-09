package xy177.farmersfuturedelight.common.item;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class ItemNetheriteHorseArmor extends Item implements INetheriteItem {
    private static final HorseArmorType ARMOR_TYPE = EnumHelper.addHorseArmor(
            "FFD_NETHERITE", null, 15);

    public ItemNetheriteHorseArmor() {
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
}
