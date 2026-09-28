package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.entity.EntityTurtle;

@SideOnly(Side.CLIENT)
public class ModelTurtleBaby extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer rightHindLeg;
    private final ModelRenderer leftHindLeg;
    private final ModelRenderer rightFrontLeg;
    private final ModelRenderer leftFrontLeg;

    public ModelTurtleBaby() {
        textureWidth = 16;
        textureHeight = 16;
        body = new ModelRenderer(this, 0, 0);
        body.setRotationPoint(0.0F, 22.9F, 1.0F);
        body.addBox(-2.0F, -1.0F, -2.0F, 4, 2, 4, 0.0F);
        head = new ModelRenderer(this, 0, 6);
        head.setRotationPoint(0.0F, 22.9F, -1.0F);
        head.addBox(-1.5F, -2.0F, -3.0F, 3, 3, 3, 0.0F);
        rightHindLeg = new ModelRenderer(this, -1, 0);
        rightHindLeg.setRotationPoint(-2.0F, 23.9F, 2.5F);
        rightHindLeg.addBox(-2.0F, 0.0F, -0.5F, 2, 0, 1, 0.0F);
        leftHindLeg = new ModelRenderer(this, -1, 1);
        leftHindLeg.setRotationPoint(2.0F, 23.9F, 2.5F);
        leftHindLeg.addBox(0.0F, 0.0F, -0.5F, 2, 0, 1, 0.0F);
        rightFrontLeg = new ModelRenderer(this, 8, 6);
        rightFrontLeg.setRotationPoint(-2.0F, 23.9F, -0.5F);
        rightFrontLeg.addBox(-2.0F, 0.0F, -0.5F, 2, 0, 1, 0.0F);
        leftFrontLeg = new ModelRenderer(this, 8, 7);
        leftFrontLeg.setRotationPoint(2.0F, 23.9F, -0.5F);
        leftFrontLeg.addBox(0.0F, 0.0F, -0.5F, 2, 0, 1, 0.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        body.render(scale);
        head.render(scale);
        rightHindLeg.render(scale);
        leftHindLeg.render(scale);
        rightFrontLeg.render(scale);
        leftFrontLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        EntityTurtle turtle = (EntityTurtle) entity;
        rightHindLeg.rotateAngleX = 0.0F;
        leftHindLeg.rotateAngleX = 0.0F;
        rightHindLeg.rotateAngleY = 0.0F;
        leftHindLeg.rotateAngleY = 0.0F;
        rightFrontLeg.rotateAngleY = 0.0F;
        leftFrontLeg.rotateAngleY = 0.0F;
        rightFrontLeg.rotateAngleZ = 0.0F;
        leftFrontLeg.rotateAngleZ = 0.0F;
        if (!turtle.isInWater() && turtle.onGround) {
            float frontSwing = MathHelper.cos(limbSwing * 5.0F)
                    * 8.0F * limbSwingAmount;
            float rearSwing = MathHelper.cos(limbSwing * 5.0F)
                    * 3.0F * limbSwingAmount;
            rightFrontLeg.rotateAngleY = -frontSwing;
            leftFrontLeg.rotateAngleY = frontSwing;
            rightHindLeg.rotateAngleY = -rearSwing;
            leftHindLeg.rotateAngleY = rearSwing;
            head.rotateAngleX = headPitch * 0.017453292F;
            head.rotateAngleY = netHeadYaw * 0.017453292F;
            return;
        }
        float swing = MathHelper.cos(limbSwing * 0.6662F * 0.6F)
                * 0.5F * limbSwingAmount;
        rightHindLeg.rotateAngleX = swing;
        leftHindLeg.rotateAngleX = -swing;
        rightFrontLeg.rotateAngleZ = -swing;
        leftFrontLeg.rotateAngleZ = swing;
        head.rotateAngleX = headPitch * 0.017453292F;
        head.rotateAngleY = netHeadYaw * 0.017453292F;
    }
}
