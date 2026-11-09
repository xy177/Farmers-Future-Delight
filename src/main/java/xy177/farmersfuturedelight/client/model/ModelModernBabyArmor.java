package xy177.farmersfuturedelight.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;

public final class ModelModernBabyArmor extends ModelModernBaby {
    private final ModelModernBaby parent;
    private final EntityEquipmentSlot slot;

    public ModelModernBabyArmor(String kind, EntityEquipmentSlot slot, ModelModernBaby parent) {
        super(kind + "_armor_" + slot.getName());
        this.parent = parent;
        this.slot = slot;
    }

    @Override
    public void render(Entity entity, float swing, float amount, float age, float yaw, float pitch, float scale) {
        copyPose(parent);
        bipedHead.showModel = slot == EntityEquipmentSlot.HEAD;
        bipedBody.showModel = slot == EntityEquipmentSlot.CHEST;
        bipedRightArm.showModel = slot == EntityEquipmentSlot.CHEST;
        bipedLeftArm.showModel = slot == EntityEquipmentSlot.CHEST;
        bipedRightLeg.showModel = slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET;
        bipedLeftLeg.showModel = bipedRightLeg.showModel;
        part("inner_body").showModel = slot == EntityEquipmentSlot.LEGS;
        part("waist").showModel = slot == EntityEquipmentSlot.LEGS;
        renderPrepared(scale);
    }
}
