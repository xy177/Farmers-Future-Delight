package xy177.farmersfuturedelight.common.item;

import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class ItemNetheriteArmor extends ItemArmor implements INetheriteItem {
    private static final UUID BOOTS_KNOCKBACK = UUID.fromString("d39a168a-2a7d-4a95-b3d0-879d39a00001");
    private static final UUID LEGGINGS_KNOCKBACK = UUID.fromString("d39a168a-2a7d-4a95-b3d0-879d39a00002");
    private static final UUID CHESTPLATE_KNOCKBACK = UUID.fromString("d39a168a-2a7d-4a95-b3d0-879d39a00003");
    private static final UUID HELMET_KNOCKBACK = UUID.fromString("d39a168a-2a7d-4a95-b3d0-879d39a00004");

    public ItemNetheriteArmor(ArmorMaterial material, EntityEquipmentSlot slot) {
        super(material, 0, slot);
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot,
                                  String type) {
        int layer = slot == EntityEquipmentSlot.LEGS ? 2 : 1;
        return FarmerFutureDelight.MODID + ":textures/models/armor/netherite_layer_"
                + layer + ".png";
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot,
                                                                     ItemStack stack) {
        Multimap<String, AttributeModifier> modifiers = super.getAttributeModifiers(slot, stack);
        if (slot == armorType) {
            modifiers.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),
                    new AttributeModifier(knockbackUuid(), "Netherite knockback resistance",
                            0.1D, 0));
        }
        return modifiers;
    }

    private UUID knockbackUuid() {
        switch (armorType) {
            case FEET:
                return BOOTS_KNOCKBACK;
            case LEGS:
                return LEGGINGS_KNOCKBACK;
            case CHEST:
                return CHESTPLATE_KNOCKBACK;
            default:
                return HELMET_KNOCKBACK;
        }
    }
}
