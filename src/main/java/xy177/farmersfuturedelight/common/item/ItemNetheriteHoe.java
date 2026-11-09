package xy177.farmersfuturedelight.common.item;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemHoe;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class ItemNetheriteHoe extends ItemHoe implements INetheriteItem {
    public ItemNetheriteHoe(ToolMaterial material) {
        super(material);
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(
            EntityEquipmentSlot slot) {
        return NetheriteItemAttributes.apply(super.getItemAttributeModifiers(slot),
                slot, 1.0D, 0.0D);
    }
}
