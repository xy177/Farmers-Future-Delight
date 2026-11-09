package xy177.farmersfuturedelight.common.item;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemPickaxe;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class ItemNetheritePickaxe extends ItemPickaxe implements INetheriteItem {
    public ItemNetheritePickaxe(ToolMaterial material) {
        super(material);
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(
            EntityEquipmentSlot slot) {
        return NetheriteItemAttributes.apply(super.getItemAttributeModifiers(slot),
                slot, 6.0D, -2.8D);
    }
}
