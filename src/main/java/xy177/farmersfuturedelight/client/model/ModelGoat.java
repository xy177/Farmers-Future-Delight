package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.entity.EntityGoat;

@SideOnly(Side.CLIENT)
public class ModelGoat extends ModelBase {
    protected final ModelRenderer head;
    protected final ModelRenderer leftHorn;
    protected final ModelRenderer rightHorn;
    protected final ModelRenderer body;
    protected final ModelRenderer leftHindLeg;
    protected final ModelRenderer rightHindLeg;
    protected final ModelRenderer leftFrontLeg;
    protected final ModelRenderer rightFrontLeg;

    public ModelGoat() {
        textureWidth = 64;
        textureHeight = 64;

        head = new ModelRenderer(this);
        head.setRotationPoint(1.0F, 14.0F, 0.0F);

        ModelRenderer rightEar = new ModelRenderer(this, 2, 61);
        rightEar.addBox(-6.0F, -11.0F, -10.0F, 3, 2, 1);
        head.addChild(rightEar);

        ModelRenderer leftEar = new ModelRenderer(this, 2, 61);
        leftEar.mirror = true;
        leftEar.addBox(2.0F, -11.0F, -10.0F, 3, 2, 1);
        head.addChild(leftEar);

        ModelRenderer goatee = new ModelRenderer(this, 23, 52);
        goatee.addBox(-0.5F, -3.0F, -14.0F, 0, 7, 5);
        head.addChild(goatee);

        leftHorn = new ModelRenderer(this, 12, 55);
        leftHorn.addBox(-0.01F, -16.0F, -10.0F, 2, 7, 2);
        head.addChild(leftHorn);

        rightHorn = new ModelRenderer(this, 12, 55);
        rightHorn.addBox(-2.99F, -16.0F, -10.0F, 2, 7, 2);
        head.addChild(rightHorn);

        ModelRenderer nose = new ModelRenderer(this, 34, 46);
        nose.setRotationPoint(0.0F, -8.0F, -8.0F);
        nose.rotateAngleX = 0.9599F;
        nose.addBox(-3.0F, -4.0F, -8.0F, 5, 7, 10);
        head.addChild(nose);

        body = new ModelRenderer(this, 1, 1);
        body.setRotationPoint(0.0F, 24.0F, 0.0F);
        body.addBox(-4.0F, -17.0F, -7.0F, 9, 11, 16);
        body.setTextureOffset(0, 28);
        body.addBox(-5.0F, -18.0F, -8.0F, 11, 14, 11);

        leftHindLeg = new ModelRenderer(this, 36, 29);
        leftHindLeg.setRotationPoint(1.0F, 14.0F, 4.0F);
        leftHindLeg.addBox(0.0F, 4.0F, 0.0F, 3, 6, 3);

        rightHindLeg = new ModelRenderer(this, 49, 29);
        rightHindLeg.setRotationPoint(-3.0F, 14.0F, 4.0F);
        rightHindLeg.addBox(0.0F, 4.0F, 0.0F, 3, 6, 3);

        leftFrontLeg = new ModelRenderer(this, 49, 2);
        leftFrontLeg.setRotationPoint(1.0F, 14.0F, -6.0F);
        leftFrontLeg.addBox(0.0F, 0.0F, 0.0F, 3, 10, 3);

        rightFrontLeg = new ModelRenderer(this, 35, 2);
        rightFrontLeg.setRotationPoint(-3.0F, 14.0F, -6.0F);
        rightFrontLeg.addBox(0.0F, 0.0F, 0.0F, 3, 10, 3);
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
        head.rotateAngleY = MathHelper.clamp(MathHelper.wrapDegrees(netHeadYaw),
                -15.0F, 15.0F) * 0.017453292F;
        float rammingRotation = goat.getRammingXHeadRot();
        head.rotateAngleX = rammingRotation == 0.0F
                ? headPitch * 0.017453292F : rammingRotation;
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
