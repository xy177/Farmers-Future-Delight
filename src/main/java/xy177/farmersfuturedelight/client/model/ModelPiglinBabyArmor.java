package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;

public final class ModelPiglinBabyArmor extends ModelModernBaby {
    private final ModelPiglin parent;
    private final EntityEquipmentSlot slot;

    public ModelPiglinBabyArmor(ModelPiglin parent,EntityEquipmentSlot slot) {
        super("zombie_pigman_armor_"+slot.getName());
        this.parent=parent;this.slot=slot;
    }

    @Override
    public void render(Entity entity,float swing,float amount,float age,float yaw,float pitch,float scale) {
        ModelBiped.copyModelAngles(parent.bipedHead,bipedHead);
        ModelBiped.copyModelAngles(parent.bipedBody,bipedBody);
        ModelBiped.copyModelAngles(parent.bipedRightArm,bipedRightArm);
        ModelBiped.copyModelAngles(parent.bipedLeftArm,bipedLeftArm);
        ModelBiped.copyModelAngles(parent.bipedRightLeg,bipedRightLeg);
        ModelBiped.copyModelAngles(parent.bipedLeftLeg,bipedLeftLeg);
        bipedHead.showModel=slot==EntityEquipmentSlot.HEAD;
        bipedBody.showModel=slot==EntityEquipmentSlot.CHEST;
        bipedRightArm.showModel=slot==EntityEquipmentSlot.CHEST;
        bipedLeftArm.showModel=slot==EntityEquipmentSlot.CHEST;
        bipedRightLeg.showModel=slot==EntityEquipmentSlot.LEGS||slot==EntityEquipmentSlot.FEET;
        bipedLeftLeg.showModel=bipedRightLeg.showModel;
        part("inner_body").showModel=slot==EntityEquipmentSlot.LEGS;
        part("waist").showModel=slot==EntityEquipmentSlot.LEGS;
        renderPrepared(scale);
    }
}
