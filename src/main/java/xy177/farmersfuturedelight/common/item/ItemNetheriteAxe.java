package xy177.farmersfuturedelight.common.item;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemAxe;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class ItemNetheriteAxe extends ItemAxe implements INetheriteItem {
    public ItemNetheriteAxe(ToolMaterial material) {
        super(material, 6.0F, -3.0F);
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(
            EntityEquipmentSlot slot) {
        return NetheriteItemAttributes.apply(super.getItemAttributeModifiers(slot),
                slot, 10.0D, -3.0D);
    }
}
