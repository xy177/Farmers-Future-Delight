package xy177.farmersfuturedelight.common.item;

import com.google.common.collect.Multimap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;

final class NetheriteItemAttributes extends Item {
    private NetheriteItemAttributes() {
    }

    static Multimap<String, AttributeModifier> apply(
            Multimap<String, AttributeModifier> modifiers, EntityEquipmentSlot slot,
            double damage, double speed) {
        if (slot == EntityEquipmentSlot.MAINHAND) {
            modifiers.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
            modifiers.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage - 1.0D, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", speed, 0));
        }
        return modifiers;
    }
}
