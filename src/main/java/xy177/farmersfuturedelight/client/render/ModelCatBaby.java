package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityCat;

public class ModelCatBaby extends ModelBase {
    private final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer tail;
    private final ModelRenderer leftHindLeg;
    private final ModelRenderer rightHindLeg;
    private final ModelRenderer leftFrontLeg;
    private final ModelRenderer rightFrontLeg;
    private float partialTicks;

    public ModelCatBaby() {
        textureWidth = 32;
        textureHeight = 32;
        head = new ModelRenderer(this, 0, 0);
        head.addBox(-2.5F, -3.0F, -2.875F, 5, 4, 4);
        head.setRotationPoint(0.0F, 20.0F, -3.125F);
        ModelRenderer leftEar = new ModelRenderer(this, 18, 0);
        leftEar.addBox(-2.0F, -4.0F, -0.875F, 1, 1, 2);
        head.addChild(leftEar);
        ModelRenderer rightEar = new ModelRenderer(this, 24, 0);
        rightEar.addBox(1.0F, -4.0F, -0.875F, 1, 1, 2);
        head.addChild(rightEar);
        ModelRenderer nose = new ModelRenderer(this, 18, 3);
        nose.addBox(-1.5F, -1.0F, -3.875F, 3, 2, 1);
        head.addChild(nose);
        leftFrontLeg = leg(18, 18, 1.0F, 22.0F, -1.5F);
        rightFrontLeg = leg(12, 18, -1.0F, 22.0F, -1.5F);
        leftHindLeg = leg(18, 22, 1.0F, 22.0F, 2.5F);
        rightHindLeg = leg(12, 22, -1.0F, 22.0F, 2.5F);
        body = new ModelRenderer(this, 0, 8);
        body.addBox(-2.0F, -1.5F, -3.5F, 4, 3, 7);
        body.setRotationPoint(0.0F, 20.5F, 0.5F);
        tail = new ModelRenderer(this, 0, 18);
        tail.addBox(-0.5F, -0.107F, 0.0849F, 1, 1, 5);
        tail.setRotationPoint(0.0F, 19.107F, 3.9151F);
        tail.rotateAngleX = -0.567232F;
    }

    private ModelRenderer leg(int textureX, int textureY, float x, float y, float z) {
        ModelRenderer leg = new ModelRenderer(this, textureX, textureY);
        leg.addBox(-0.5F, 0.0F, -1.0F, 1, 2, 2);
        leg.setRotationPoint(x, y, z);
        return leg;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount,
                       float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                headPitch, scale, entity);
        head.render(scale);
        body.render(scale);
        tail.render(scale);
        leftHindLeg.render(scale);
        rightHindLeg.render(scale);
        leftFrontLeg.render(scale);
        rightFrontLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale,
                                  Entity entity) {
        EntityOcelot cat = (EntityOcelot) entity;
        head.rotateAngleX = headPitch * 0.017453292F;
        head.rotateAngleY = netHeadYaw * 0.017453292F;
        if (!cat.isSitting()) {
            leftHindLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F)
                    * limbSwingAmount;
            rightHindLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F
                    + (float) Math.PI) * limbSwingAmount;
            leftFrontLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F
                    + (float) Math.PI) * limbSwingAmount;
            rightFrontLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F)
                    * limbSwingAmount;
            tail.rotateAngleX = -0.567232F
                    + 0.7853982F * MathHelper.cos(limbSwing) * limbSwingAmount;
        }
        float lying = (cat instanceof EntityCat ? ((EntityCat) cat).getLieDownAmount(partialTicks) : 0.0F);
        if (lying > 0.0F) {
            body.rotationPointX += 1.0F;
            head.rotateAngleX = lerpAngle(lying, head.rotateAngleX, 0.17453292F);
            head.rotateAngleZ = lerpAngle(lying, head.rotateAngleZ, -1.3089969F);
            head.rotationPointX += 1.5F;
            head.rotationPointY += 0.75F;
            head.rotationPointZ -= 0.5F;
            rightFrontLeg.rotateAngleX = -0.7853982F;
            rightFrontLeg.rotationPointX += 3.5F;
            rightFrontLeg.rotationPointY -= 0.5F;
            leftFrontLeg.rotateAngleX = -1.5707964F;
            leftFrontLeg.rotationPointX += 1.5F;
            leftFrontLeg.rotationPointY -= 1.0F;
            leftFrontLeg.rotationPointZ -= 2.0F;
            rightHindLeg.rotateAngleX = 0.6981317F;
            rightHindLeg.rotateAngleY = 0.34906584F;
            rightHindLeg.rotateAngleZ = -0.34906584F;
            rightHindLeg.rotationPointX += 2.5F;
            rightHindLeg.rotationPointY -= 0.25F;
            rightHindLeg.rotationPointZ += 0.5F;
            leftHindLeg.rotationPointX += 1.5F;
            leftHindLeg.rotationPointZ -= 1.0F;
            tail.rotateAngleX += lerpAngle((cat instanceof EntityCat ? ((EntityCat) cat).getLieDownTailAmount(partialTicks) : 0.0F),
                    tail.rotateAngleX, -0.5235988F);
            tail.rotateAngleZ -= 0.17453292F * lying;
            tail.rotationPointX += lying;
            tail.rotationPointY += 0.5F * lying;
            tail.rotationPointZ -= 0.25F * lying;
        }
        head.rotateAngleX = lerpAngle((cat instanceof EntityCat ? ((EntityCat) cat).getRelaxStateOneAmount(partialTicks) : 0.0F),
                head.rotateAngleX, -0.58177644F);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing,
                                    float limbSwingAmount, float partialTickTime) {
        EntityOcelot cat = (EntityOcelot) entity;
        partialTicks = partialTickTime;
        head.rotationPointX = 0.0F;
        head.rotationPointY = 20.0F;
        head.rotationPointZ = -3.125F;
        body.rotationPointX = 0.0F;
        body.rotationPointY = 20.5F;
        body.rotationPointZ = 0.5F;
        tail.rotationPointX = 0.0F;
        tail.rotationPointY = 19.107F;
        tail.rotationPointZ = 3.9151F;
        leftFrontLeg.rotationPointX = 1.0F;
        leftFrontLeg.rotationPointY = 22.0F;
        leftFrontLeg.rotationPointZ = -1.5F;
        rightFrontLeg.rotationPointX = -1.0F;
        rightFrontLeg.rotationPointY = 22.0F;
        rightFrontLeg.rotationPointZ = -1.5F;
        leftHindLeg.rotationPointX = 1.0F;
        leftHindLeg.rotationPointY = 22.0F;
        leftHindLeg.rotationPointZ = 2.5F;
        rightHindLeg.rotationPointX = -1.0F;
        rightHindLeg.rotationPointY = 22.0F;
        rightHindLeg.rotationPointZ = 2.5F;
        head.rotateAngleY = 0.0F;
        head.rotateAngleZ = 0.0F;
        body.rotateAngleX = 0.0F;
        tail.rotateAngleX = -0.567232F;
        tail.rotateAngleY = 0.0F;
        tail.rotateAngleZ = 0.0F;
        leftFrontLeg.rotateAngleX = 0.0F;
        rightFrontLeg.rotateAngleX = 0.0F;
        leftHindLeg.rotateAngleX = 0.0F;
        rightHindLeg.rotateAngleX = 0.0F;
        rightHindLeg.rotateAngleY = 0.0F;
        rightHindLeg.rotateAngleZ = 0.0F;
        if (cat.isSitting()) {
            body.rotateAngleX -= 0.43633232F;
            body.rotationPointY += 1.25F;
            head.rotationPointZ += 0.75F;
            tail.rotateAngleX += 0.5454154F;
            tail.rotationPointY += 4.0F;
            tail.rotationPointZ -= 0.9F;
            leftHindLeg.rotationPointZ -= 0.9F;
            rightHindLeg.rotationPointZ -= 0.9F;
        }
    }

    private static float lerpAngle(float amount, float start, float end) {
        return start + MathHelper.wrapDegrees(end - start) * amount;
    }
}
