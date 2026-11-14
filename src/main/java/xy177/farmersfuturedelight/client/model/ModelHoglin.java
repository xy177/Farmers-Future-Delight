package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityHoglin;

public final class ModelHoglin extends ModelBase {
    private final boolean baby;
    private final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer rightEar;
    private final ModelRenderer leftEar;
    private final ModelRenderer rightFrontLeg;
    private final ModelRenderer leftFrontLeg;
    private final ModelRenderer rightHindLeg;
    private final ModelRenderer leftHindLeg;

    public ModelHoglin(boolean baby) {
        this.baby = baby;
        textureWidth = baby ? 64 : 128;
        textureHeight = 64;
        if (baby) {
            head = part(0, 0, 0.0F, 13.0F, -7.0F);
            head.addBox(-5.0F, -2.2605F, -10.547F, 10, 4, 12);
            head.setTextureOffset(44, 29).addBox(-7.0F, -4.0981F, -8.4879F, 2, 5, 2);
            head.setTextureOffset(52, 29).addBox(5.0F, -4.0981F, -8.4879F, 2, 5, 2);
            body = part(0, 16, 0.0F, 24.0F, 0.0F);
            body.addBox(-4.0F, -14.0F, -7.0F, 8, 8, 14, 0.02F);
            body.setTextureOffset(24, 39).addBox(0.0F, -18.0F, -8.0F, 0, 6, 11, 0.02F);
            rightEar = part(32, 5, -5.0F, -1.0F, -1.5F);
            rightEar.addBox(-5.1F, -0.5F, -2.0F, 6, 1, 4);
            leftEar = part(32, 0, 5.0F, -1.0F, -1.5F);
            leftEar.mirror = true;
            leftEar.addBox(-0.9F, -0.5F, -2.0F, 6, 1, 4);
            rightFrontLeg = leg(0, 38, -2.5F, 18.0F, -4.5F, 3, 6);
            leftFrontLeg = leg(12, 38, 2.5F, 18.0F, -4.5F, 3, 6);
            rightHindLeg = leg(0, 47, -2.5F, 18.0F, 4.5F, 3, 6);
            leftHindLeg = leg(12, 47, 2.5F, 18.0F, 4.5F, 3, 6);
        } else {
            body = part(1, 1, 0.0F, 7.0F, 0.0F);
            body.addBox(-8.0F, -7.0F, -13.0F, 16, 14, 26);
            ModelRenderer mane = part(90, 33, 0.0F, -14.0F, -7.0F);
            mane.addBox(0.0F, 0.0F, -9.0F, 0, 10, 19, 0.001F);
            body.addChild(mane);
            head = part(61, 1, 0.0F, 2.0F, -12.0F);
            head.addBox(-7.0F, -3.0F, -19.0F, 14, 6, 19);
            rightEar = part(1, 1, -6.0F, -2.0F, -3.0F);
            rightEar.addBox(-6.0F, -1.0F, -2.0F, 6, 1, 4);
            leftEar = part(1, 6, 6.0F, -2.0F, -3.0F);
            leftEar.addBox(0.0F, -1.0F, -2.0F, 6, 1, 4);
            ModelRenderer rightHorn = part(10, 13, -7.0F, 2.0F, -12.0F);
            rightHorn.addBox(-1.0F, -11.0F, -1.0F, 2, 11, 2);
            head.addChild(rightHorn);
            ModelRenderer leftHorn = part(1, 13, 7.0F, 2.0F, -12.0F);
            leftHorn.addBox(-1.0F, -11.0F, -1.0F, 2, 11, 2);
            head.addChild(leftHorn);
            rightFrontLeg = leg(66, 42, -4.0F, 10.0F, -8.5F, 6, 14);
            leftFrontLeg = leg(41, 42, 4.0F, 10.0F, -8.5F, 6, 14);
            rightHindLeg = leg(21, 45, -5.0F, 13.0F, 10.0F, 5, 11);
            leftHindLeg = leg(0, 45, 5.0F, 13.0F, 10.0F, 5, 11);
        }
        head.addChild(rightEar);
        head.addChild(leftEar);
    }

    private ModelRenderer part(int textureX, int textureY, float posX, float posY, float posZ) {
        ModelRenderer part = new ModelRenderer(this, textureX, textureY);
        part.setRotationPoint(posX, posY, posZ);
        return part;
    }

    private ModelRenderer leg(int textureX, int textureY, float posX, float posY, float posZ,
                              int width, int height) {
        ModelRenderer leg = part(textureX, textureY, posX, posY, posZ);
        leg.addBox(-width / 2.0F, 0.0F, -width / 2.0F, width, height, width);
        return leg;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        body.render(scale);
        head.render(scale);
        rightFrontLeg.render(scale);
        leftFrontLeg.render(scale);
        rightHindLeg.render(scale);
        leftHindLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        EntityHoglin hoglin = (EntityHoglin) entity;
        rightEar.rotateAngleZ = -0.6981317F - limbSwingAmount * MathHelper.sin(limbSwing);
        leftEar.rotateAngleZ = 0.6981317F + limbSwingAmount * MathHelper.sin(limbSwing);
        head.rotateAngleY = netHeadYaw * 0.017453292F;
        float attack = 1.0F - MathHelper.abs(10 - 2 * hoglin.getAttackAnimationRemainingTicks()) / 10.0F;
        head.rotateAngleX = 0.87266463F + attack * (-0.34906584F - 0.87266463F);
        head.rotationPointY = baby ? 13.0F + attack * 2.5F : 2.0F;
        rightFrontLeg.rotateAngleX = MathHelper.cos(limbSwing) * 1.2F * limbSwingAmount;
        leftFrontLeg.rotateAngleX = MathHelper.cos(limbSwing + (float) Math.PI) * 1.2F * limbSwingAmount;
        rightHindLeg.rotateAngleX = leftFrontLeg.rotateAngleX;
        leftHindLeg.rotateAngleX = rightFrontLeg.rotateAngleX;
    }
}
