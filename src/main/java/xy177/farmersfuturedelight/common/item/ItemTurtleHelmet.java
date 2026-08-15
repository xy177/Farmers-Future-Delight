package xy177.farmersfuturedelight.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemTurtleHelmet extends ItemArmor {
    public ItemTurtleHelmet(ArmorMaterial material) {
        super(material, 0, EntityEquipmentSlot.HEAD);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        if (!player.isInsideOfMaterial(net.minecraft.block.material.Material.WATER)) {
            player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 200, 0, false, false));
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        net.minecraft.item.Item scute = FFDItems.effectiveItem(FFDItems.TURTLE_SCUTE);
        return scute != null && repair.getItem() == scute || super.getIsRepairable(toRepair, repair);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return FarmerFutureDelight.MODID + ":textures/models/armor/turtle_scute_layer_1.png";
    }
}
