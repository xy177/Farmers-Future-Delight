package xy177.farmersfuturedelight.common.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class EnchantmentSoulSpeed extends Enchantment {
    public EnchantmentSoulSpeed() {
        super(Rarity.VERY_RARE,EnumEnchantmentType.ARMOR_FEET,new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET});
        setRegistryName(FarmerFutureDelight.MODID,"soul_speed");setName(FarmerFutureDelight.MODID+".soul_speed");
    }
    @Override public int getMaxLevel() {return 3;}
    @Override public int getMinEnchantability(int level) {return level*10;}
    @Override public int getMaxEnchantability(int level) {return getMinEnchantability(level)+15;}
    @Override public boolean isTreasureEnchantment() {return true;}
    @Override public boolean isAllowedOnBooks() {return true;}
}
