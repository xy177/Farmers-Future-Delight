package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelIllager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.item.ItemCrossbow;

@SideOnly(Side.CLIENT)
public class ModelPillager extends ModelIllager {
    private float partialTicks;

    public ModelPillager() {
        super(0.0F, 0.0F, 64, 64);
        body.cubeList.clear();
        body.setTextureOffset(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6, 0.0F);
        body.setTextureOffset(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8, 20, 6, 0.5F);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing,
                                     float limbSwingAmount, float partialTicks) {
        super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        this.partialTicks = partialTicks;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount,
                       float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        head.render(scale);
        body.render(scale);
        leg0.render(scale);
        leg1.render(scale);
        rightArm.render(scale);
        leftArm.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        rightArm.rotateAngleZ = 0.0F;
        leftArm.rotateAngleZ = 0.0F;
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        leg0.rotateAngleY = isRiding ? 0.31415927F : 0.0F;
        leg0.rotateAngleZ = isRiding ? 0.07853982F : 0.0F;
        leg1.rotateAngleY = isRiding ? -0.31415927F : 0.0F;
        leg1.rotateAngleZ = isRiding ? -0.07853982F : 0.0F;
        if (isRiding) {
            leg0.rotateAngleX = -1.4137167F;
            leg1.rotateAngleX = -1.4137167F;
        }
        EntityPillager pillager = (EntityPillager) entity;
        if (pillager.isChargingCrossbow()) {
            rightArm.rotateAngleY = -0.8F;
            rightArm.rotateAngleX = -0.97079635F;
            float duration = ItemCrossbow.getChargeDuration(pillager.getActiveItemStack());
            float progress = MathHelper.clamp((pillager.getItemInUseMaxCount() + partialTicks) / duration, 0.0F, 1.0F);
            leftArm.rotateAngleY = 0.4F + (0.85F - 0.4F) * progress;
            leftArm.rotateAngleX = -0.97079635F + (-1.5707964F + 0.97079635F) * progress;
            leftArm.rotateAngleZ = 0.0F;
        } else if (pillager.getArmPose() == AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
            rightArm.rotateAngleY = -0.3F + head.rotateAngleY;
            leftArm.rotateAngleY = 0.6F + head.rotateAngleY;
            rightArm.rotateAngleX = -1.5707964F + head.rotateAngleX + 0.1F;
            leftArm.rotateAngleX = -1.5F + head.rotateAngleX;
            leftArm.rotateAngleZ = 0.0F;
        } else if (pillager.getArmPose() == AbstractIllager.IllagerArmPose.CROSSED) {
            rightArm.rotateAngleX = isRiding ? -0.62831855F
                    : MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
            leftArm.rotateAngleX = isRiding ? -0.62831855F
                    : MathHelper.cos(limbSwing * 0.6662F) * limbSwingAmount;
            rightArm.rotateAngleY = 0.0F;
            leftArm.rotateAngleY = 0.0F;
        }
    }
}
