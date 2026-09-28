package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.entity.EntityGoat;

@SideOnly(Side.CLIENT)
public class ModelGoatBaby extends ModelBase {
    private final ModelRenderer head;
    private final ModelRenderer leftHorn;
    private final ModelRenderer rightHorn;
    private final ModelRenderer body;
    private final ModelRenderer leftHindLeg;
    private final ModelRenderer rightHindLeg;
    private final ModelRenderer leftFrontLeg;
    private final ModelRenderer rightFrontLeg;

    public ModelGoatBaby() {
        textureWidth = 64;
        textureHeight = 64;

        leftHindLeg = new ModelRenderer(this, 29, 12);
        leftHindLeg.setRotationPoint(1.5F, 19.5F, 3.0F);
        leftHindLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2);

        rightHindLeg = new ModelRenderer(this, 21, 12);
        rightHindLeg.setRotationPoint(-1.5F, 19.5F, 3.0F);
        rightHindLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2);

        rightFrontLeg = new ModelRenderer(this, 21, 5);
        rightFrontLeg.setRotationPoint(-1.5F, 19.5F, -2.0F);
        rightFrontLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2);

        leftFrontLeg = new ModelRenderer(this, 29, 5);
        leftFrontLeg.setRotationPoint(1.5F, 19.5F, -2.0F);
        leftFrontLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2);

        body = new ModelRenderer(this, 0, 10);
        body.setRotationPoint(0.0F, 17.8F, 0.0F);
        body.addBox(-3.0F, -2.3F, -4.5F, 6, 5, 9);
        body.setTextureOffset(0, 24);
        body.addBox(-2.5F, -2.2F, -4.0F, 5, 4, 8);

        head = new ModelRenderer(this, 0, 0);
        head.setRotationPoint(0.0F, 15.5F, -3.0F);
        head.rotateAngleX = 0.4363F;
        head.addBox(-2.0F, -3.8126F, -5.1548F, 4, 4, 6);

        rightHorn = new ModelRenderer(this, 24, 0);
        rightHorn.mirror = true;
        rightHorn.setRotationPoint(-1.5F, -1.5F, -1.0F);
        rightHorn.rotateAngleX = -0.3926991F;
        rightHorn.addBox(0.0F, -4.5F, 0.0F, 1, 2, 1);
        head.addChild(rightHorn);

        leftHorn = new ModelRenderer(this, 24, 0);
        leftHorn.mirror = true;
        leftHorn.setRotationPoint(-1.5F, -1.5F, -1.0F);
        leftHorn.rotateAngleX = -0.3926991F;
        leftHorn.addBox(2.0F, -4.5F, 0.0F, 1, 2, 1);
        head.addChild(leftHorn);

        ModelRenderer rightEar = new ModelRenderer(this, 0, 12);
        rightEar.mirror = true;
        rightEar.setRotationPoint(-1.7F, -2.3126F, 0.1452F);
        rightEar.rotateAngleY = -0.5236F;
        rightEar.addBox(-2.0F, -0.5F, -0.5F, 2, 1, 1);
        head.addChild(rightEar);

        ModelRenderer leftEar = new ModelRenderer(this, 0, 12);
        leftEar.setRotationPoint(1.7F, -2.3126F, 0.1452F);
        leftEar.rotateAngleY = 0.5236F;
        leftEar.addBox(0.0F, -0.5F, -0.5F, 2, 1, 1);
        head.addChild(leftEar);

        ModelRenderer headMain = new ModelRenderer(this, 0, 0);
        headMain.setRotationPoint(0.0F, -1.3126F, -1.1548F);
        headMain.addBox(-2.0F, -2.5F, -4.0F, 4, 4, 6);
        head.addChild(headMain);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        head.render(scale);
        body.render(scale);
        leftHindLeg.render(scale);
        rightHindLeg.render(scale);
        leftFrontLeg.render(scale);
        rightFrontLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale,
                                  Entity entity) {
        EntityGoat goat = (EntityGoat) entity;
        head.rotateAngleY = netHeadYaw * 0.017453292F;
        float rammingRotation = goat.getRammingXHeadRot();
        head.rotateAngleX = rammingRotation == 0.0F ? 0.3926991F : rammingRotation;
        rightHindLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F)
                * 1.4F * limbSwingAmount;
        leftHindLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * 1.4F * limbSwingAmount;
        rightFrontLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * 1.4F * limbSwingAmount;
        leftFrontLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F)
                * 1.4F * limbSwingAmount;
        leftHorn.showModel = goat.hasLeftHorn();
        rightHorn.showModel = goat.hasRightHorn();
    }
}
