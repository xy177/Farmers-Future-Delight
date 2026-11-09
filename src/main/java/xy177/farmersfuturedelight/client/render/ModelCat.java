package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityCat;

public class ModelCat extends ModelBase {
    private final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer tail1;
    private final ModelRenderer tail2;
    private final ModelRenderer backLeftLeg;
    private final ModelRenderer backRightLeg;
    private final ModelRenderer frontLeftLeg;
    private final ModelRenderer frontRightLeg;
    private float partialTicks;

    public ModelCat() {
        textureWidth = 64;
        textureHeight = 32;
        setTextureOffset("head.main", 0, 0);
        setTextureOffset("head.nose", 0, 24);
        setTextureOffset("head.ear1", 0, 10);
        setTextureOffset("head.ear2", 6, 10);
        head = new ModelRenderer(this, "head");
        head.addBox("main", -2.5F, -2.0F, -3.0F, 5, 4, 5);
        head.addBox("nose", -1.5F, 0.0F, -4.0F, 3, 2, 2);
        head.addBox("ear1", -2.0F, -3.0F, 0.0F, 1, 1, 2);
        head.addBox("ear2", 1.0F, -3.0F, 0.0F, 1, 1, 2);
        head.setRotationPoint(0.0F, 15.0F, -9.0F);
        body = new ModelRenderer(this, 20, 0);
        body.addBox(-2.0F, 3.0F, -8.0F, 4, 16, 6, 0.0F);
        body.setRotationPoint(0.0F, 12.0F, -10.0F);
        tail1 = new ModelRenderer(this, 0, 15);
        tail1.addBox(-0.5F, 0.0F, 0.0F, 1, 8, 1);
        tail1.rotateAngleX = 0.9F;
        tail1.setRotationPoint(0.0F, 15.0F, 8.0F);
        tail2 = new ModelRenderer(this, 4, 15);
        tail2.addBox(-0.5F, 0.0F, 0.0F, 1, 8, 1);
        tail2.setRotationPoint(0.0F, 20.0F, 14.0F);
        backLeftLeg = new ModelRenderer(this, 8, 13);
        backLeftLeg.addBox(-1.0F, 0.0F, 1.0F, 2, 6, 2);
        backLeftLeg.setRotationPoint(1.1F, 18.0F, 5.0F);
        backRightLeg = new ModelRenderer(this, 8, 13);
        backRightLeg.addBox(-1.0F, 0.0F, 1.0F, 2, 6, 2);
        backRightLeg.setRotationPoint(-1.1F, 18.0F, 5.0F);
        frontLeftLeg = new ModelRenderer(this, 40, 0);
        frontLeftLeg.addBox(-1.0F, 0.0F, 0.0F, 2, 10, 2);
        frontLeftLeg.setRotationPoint(1.2F, 14.1F, -5.0F);
        frontRightLeg = new ModelRenderer(this, 40, 0);
        frontRightLeg.addBox(-1.0F, 0.0F, 0.0F, 2, 10, 2);
        frontRightLeg.setRotationPoint(-1.2F, 14.1F, -5.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                headPitch, scale, entity);
        head.render(scale);
        renderBody(scale);
    }

    private void renderBody(float scale) {
        body.render(scale);
        tail1.render(scale);
        tail2.render(scale);
        backLeftLeg.render(scale);
        backRightLeg.render(scale);
        frontLeftLeg.render(scale);
        frontRightLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale,
                                  Entity entity) {
        EntityCat cat = (EntityCat) entity;
        head.rotateAngleX = headPitch * 0.017453292F;
        head.rotateAngleY = netHeadYaw * 0.017453292F;
        if (!cat.isSitting()) {
            body.rotateAngleX = (float) (Math.PI / 2.0D);
            if (cat.isSprinting()) {
                backLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F)
                        * limbSwingAmount;
                backRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 0.3F)
                        * limbSwingAmount;
                frontLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F
                        + (float) Math.PI + 0.3F) * limbSwingAmount;
                frontRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F
                        + (float) Math.PI) * limbSwingAmount;
                tail2.rotateAngleX = 1.7278761F + (float) (Math.PI / 10.0D)
                        * MathHelper.cos(limbSwing) * limbSwingAmount;
            } else {
                backLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F)
                        * limbSwingAmount;
                backRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F
                        + (float) Math.PI) * limbSwingAmount;
                frontLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F
                        + (float) Math.PI) * limbSwingAmount;
                frontRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F)
                        * limbSwingAmount;
                tail2.rotateAngleX = 1.7278761F + (float) (Math.PI / 4.0D)
                        * MathHelper.cos(limbSwing) * limbSwingAmount;
            }
        }
        float lying = cat.getLieDownAmount(partialTicks);
        if (lying > 0.0F) {
            head.rotateAngleZ = lerpAngle(lying, head.rotateAngleZ, -1.2707963F);
            head.rotateAngleY = lerpAngle(lying, head.rotateAngleY, 1.2707963F);
            frontLeftLeg.rotateAngleX = -1.2707963F;
            frontRightLeg.rotateAngleX = -0.47079635F;
            frontRightLeg.rotateAngleZ = -0.2F;
            frontRightLeg.rotationPointX += 1.0F;
            backLeftLeg.rotateAngleX = -0.4F;
            backRightLeg.rotateAngleX = 0.5F;
            backRightLeg.rotateAngleZ = -0.5F;
            backRightLeg.rotationPointX += 0.8F;
            backRightLeg.rotationPointY += 2.0F;
            tail1.rotateAngleX = lerpAngle(cat.getLieDownTailAmount(partialTicks),
                    tail1.rotateAngleX, 0.8F);
            tail2.rotateAngleX = lerpAngle(cat.getLieDownTailAmount(partialTicks),
                    tail2.rotateAngleX, -0.4F);
        }
        float relaxed = cat.getRelaxStateOneAmount(partialTicks);
        head.rotateAngleX = lerpAngle(relaxed, head.rotateAngleX, -0.58177644F);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing,
                                    float limbSwingAmount, float partialTickTime) {
        EntityCat cat = (EntityCat) entity;
        partialTicks = partialTickTime;
        body.rotationPointY = 12.0F;
        body.rotationPointZ = -10.0F;
        head.rotationPointY = 15.0F;
        head.rotationPointZ = -9.0F;
        tail1.rotationPointY = 15.0F;
        tail1.rotationPointZ = 8.0F;
        tail2.rotationPointY = 20.0F;
        tail2.rotationPointZ = 14.0F;
        frontLeftLeg.rotationPointY = 14.1F;
        frontLeftLeg.rotationPointZ = -5.0F;
        frontRightLeg.rotationPointY = 14.1F;
        frontRightLeg.rotationPointZ = -5.0F;
        backLeftLeg.rotationPointY = 18.0F;
        backLeftLeg.rotationPointZ = 5.0F;
        backRightLeg.rotationPointY = 18.0F;
        frontRightLeg.rotationPointX = -1.2F;
        backRightLeg.rotationPointX = -1.1F;
        backRightLeg.rotationPointZ = 5.0F;
        head.rotateAngleY = 0.0F;
        head.rotateAngleZ = 0.0F;
        body.rotateAngleX = (float) (Math.PI / 2.0D);
        tail1.rotateAngleX = 0.9F;
        tail2.rotateAngleX = 0.0F;
        frontLeftLeg.rotateAngleX = 0.0F;
        frontLeftLeg.rotateAngleZ = 0.0F;
        frontRightLeg.rotateAngleX = 0.0F;
        frontRightLeg.rotateAngleZ = 0.0F;
        backLeftLeg.rotateAngleX = 0.0F;
        backRightLeg.rotateAngleX = 0.0F;
        backRightLeg.rotateAngleZ = 0.0F;
        frontLeftLeg.rotationPointX = 1.2F;
        frontRightLeg.rotationPointX = -1.2F;
        backLeftLeg.rotationPointX = 1.1F;
        backRightLeg.rotationPointX = -1.1F;
        backRightLeg.rotationPointY = 18.0F;
        if (cat.isSneaking()) {
            body.rotationPointY += 1.0F;
            head.rotationPointY += 2.0F;
            tail1.rotationPointY += 1.0F;
            tail2.rotationPointY -= 4.0F;
            tail2.rotationPointZ += 2.0F;
            tail1.rotateAngleX = (float) (Math.PI / 2.0D);
            tail2.rotateAngleX = (float) (Math.PI / 2.0D);
        } else if (cat.isSprinting()) {
            tail2.rotationPointY = tail1.rotationPointY;
            tail2.rotationPointZ += 2.0F;
            tail1.rotateAngleX = (float) (Math.PI / 2.0D);
            tail2.rotateAngleX = (float) (Math.PI / 2.0D);
        } else if (cat.isSitting()) {
            body.rotateAngleX = (float) (Math.PI / 4.0D);
            body.rotationPointY -= 4.0F;
            body.rotationPointZ += 5.0F;
            head.rotationPointY -= 3.3F;
            head.rotationPointZ += 1.0F;
            tail1.rotationPointY += 8.0F;
            tail1.rotationPointZ -= 2.0F;
            tail2.rotationPointY += 2.0F;
            tail2.rotationPointZ -= 0.8F;
            tail1.rotateAngleX = 1.7278761F;
            tail2.rotateAngleX = 2.670354F;
            frontLeftLeg.rotateAngleX = -0.15707964F;
            frontLeftLeg.rotationPointY = 16.1F;
            frontLeftLeg.rotationPointZ = -7.0F;
            frontRightLeg.rotateAngleX = -0.15707964F;
            frontRightLeg.rotationPointY = 16.1F;
            frontRightLeg.rotationPointZ = -7.0F;
            backLeftLeg.rotateAngleX = -(float) (Math.PI / 2.0D);
            backLeftLeg.rotationPointY = 21.0F;
            backLeftLeg.rotationPointZ = 1.0F;
            backRightLeg.rotateAngleX = -(float) (Math.PI / 2.0D);
            backRightLeg.rotationPointY = 21.0F;
            backRightLeg.rotationPointZ = 1.0F;
        }
    }

    private static float lerpAngle(float amount, float start, float end) {
        return start + MathHelper.wrapDegrees(end - start) * amount;
    }
}
