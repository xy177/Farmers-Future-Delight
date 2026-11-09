package xy177.farmersfuturedelight.common.item;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemSword;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class ItemNetheriteSword extends ItemSword implements INetheriteItem {
    public ItemNetheriteSword(ToolMaterial material) {
        super(material);
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(
            EntityEquipmentSlot slot) {
        return NetheriteItemAttributes.apply(super.getItemAttributeModifiers(slot),
                slot, 8.0D, -2.4D);
    }
}
