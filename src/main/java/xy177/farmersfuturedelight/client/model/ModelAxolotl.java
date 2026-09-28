package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.entity.EntityAxolotl;

@SideOnly(Side.CLIENT)
public class ModelAxolotl extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer topGills;
    private final ModelRenderer leftGills;
    private final ModelRenderer rightGills;
    private final ModelRenderer rightHindLeg;
    private final ModelRenderer leftHindLeg;
    private final ModelRenderer rightFrontLeg;
    private final ModelRenderer leftFrontLeg;
    private final ModelRenderer tail;

    public ModelAxolotl() {
        textureWidth = 64;
        textureHeight = 64;

        body = new ModelRenderer(this, 0, 11);
        body.setRotationPoint(0.0F, 19.5F, 5.0F);
        body.addBox(-4.0F, -2.0F, -9.0F, 8, 4, 10, 0.0F);
        body.setTextureOffset(2, 17);
        body.addBox(0.0F, -3.0F, -8.0F, 0, 5, 9, 0.001F);

        head = new ModelRenderer(this, 0, 1);
        head.setRotationPoint(0.0F, 0.0F, -9.0F);
        head.addBox(-4.0F, -3.0F, -5.0F, 8, 5, 5, 0.001F);
        body.addChild(head);

        topGills = new ModelRenderer(this, 3, 37);
        topGills.setRotationPoint(0.0F, -3.0F, -1.0F);
        topGills.addBox(-4.0F, -3.0F, 0.0F, 8, 3, 0, 0.001F);
        head.addChild(topGills);

        leftGills = new ModelRenderer(this, 0, 40);
        leftGills.setRotationPoint(-4.0F, 0.0F, -1.0F);
        leftGills.addBox(-3.0F, -5.0F, 0.0F, 3, 7, 0, 0.001F);
        head.addChild(leftGills);

        rightGills = new ModelRenderer(this, 11, 40);
        rightGills.setRotationPoint(4.0F, 0.0F, -1.0F);
        rightGills.addBox(0.0F, -5.0F, 0.0F, 3, 7, 0, 0.001F);
        head.addChild(rightGills);

        rightHindLeg = rightLeg(-3.5F, -1.0F);
        leftHindLeg = leftLeg(3.5F, -1.0F);
        rightFrontLeg = rightLeg(-3.5F, -8.0F);
        leftFrontLeg = leftLeg(3.5F, -8.0F);

        tail = new ModelRenderer(this, 2, 19);
        tail.setRotationPoint(0.0F, 0.0F, 1.0F);
        tail.addBox(0.0F, -3.0F, 0.0F, 0, 5, 12, 0.0F);
        body.addChild(tail);
    }

    private ModelRenderer rightLeg(float x, float z) {
        ModelRenderer leg = new ModelRenderer(this, 2, 13);
        leg.setRotationPoint(x, 1.0F, z);
        leg.addBox(-2.0F, 0.0F, 0.0F, 3, 5, 0, 0.001F);
        body.addChild(leg);
        return leg;
    }

    private ModelRenderer leftLeg(float x, float z) {
        ModelRenderer leg = new ModelRenderer(this, 2, 13);
        leg.setRotationPoint(x, 1.0F, z);
        leg.addBox(-1.0F, 0.0F, 0.0F, 3, 5, 0, 0.001F);
        body.addChild(leg);
        return leg;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        body.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entity) {
        EntityAxolotl axolotl = (EntityAxolotl) entity;
        resetPose();
        float partialTicks = MathHelper.clamp(ageInTicks - axolotl.ticksExisted, 0.0F, 1.0F);
        float playingDeadFactor = axolotl.getPlayingDeadAnimationFactor(partialTicks);
        float inWaterFactor = axolotl.getInWaterAnimationFactor(partialTicks);
        float onGroundFactor = axolotl.getOnGroundAnimationFactor(partialTicks);
        float movingFactor = axolotl.getMovingAnimationFactor(partialTicks);
        float notMovingFactor = 1.0F - movingFactor;
        float mirroredLegsFactor = 1.0F - Math.min(onGroundFactor, movingFactor);

        body.rotateAngleY += netHeadYaw * 0.017453292F;
        setSwimmingPose(ageInTicks, headPitch, Math.min(movingFactor, inWaterFactor));
        setHoveringPose(ageInTicks, Math.min(notMovingFactor, inWaterFactor));
        setCrawlingPose(ageInTicks, Math.min(movingFactor, onGroundFactor));
        setGroundIdlePose(ageInTicks, Math.min(notMovingFactor, onGroundFactor));
        setPlayDeadPose(playingDeadFactor);
        mirrorLegs(mirroredLegsFactor);
    }

    private void resetPose() {
        body.rotationPointY = 19.5F;
        body.rotateAngleX = 0.0F;
        body.rotateAngleY = 0.0F;
        body.rotateAngleZ = 0.0F;
        head.rotateAngleX = 0.0F;
        head.rotateAngleY = 0.0F;
        head.rotateAngleZ = 0.0F;
        topGills.rotateAngleX = 0.0F;
        leftGills.rotateAngleY = 0.0F;
        rightGills.rotateAngleY = 0.0F;
        tail.rotateAngleY = 0.0F;
        resetLeg(leftHindLeg);
        resetLeg(rightHindLeg);
        resetLeg(leftFrontLeg);
        resetLeg(rightFrontLeg);
    }

    private static void resetLeg(ModelRenderer leg) {
        leg.rotateAngleX = 0.0F;
        leg.rotateAngleY = 0.0F;
        leg.rotateAngleZ = 0.0F;
    }

    private void setSwimmingPose(float age, float headPitch, float factor) {
        if (factor <= 1.0E-5F) {
            return;
        }
        float time = age * 0.33F;
        float sine = MathHelper.sin(time);
        float cosine = MathHelper.cos(time);
        float bodySway = 0.13F * sine;
        body.rotateAngleX += (headPitch * 0.017453292F + bodySway) * factor;
        body.rotationPointY -= 0.45F * cosine * factor;
        head.rotateAngleX -= bodySway * 1.8F * factor;
        topGills.rotateAngleX += (-0.5F * sine - 0.8F) * factor;
        float gillY = (0.3F * sine + 0.9F) * factor;
        leftGills.rotateAngleY += gillY;
        rightGills.rotateAngleY -= gillY;
        tail.rotateAngleY += 0.3F * MathHelper.cos(time * 0.9F) * factor;
        leftHindLeg.rotateAngleX += 1.8849558F * factor;
        leftHindLeg.rotateAngleY -= 0.4F * sine * factor;
        leftHindLeg.rotateAngleZ += 1.5707964F * factor;
        leftFrontLeg.rotateAngleX += 1.8849558F * factor;
        leftFrontLeg.rotateAngleY += (-0.2F * cosine - 0.1F) * factor;
        leftFrontLeg.rotateAngleZ += 1.5707964F * factor;
    }

    private void setHoveringPose(float age, float factor) {
        if (factor <= 1.0E-5F) {
            return;
        }
        float cosine = MathHelper.cos(age * 0.075F);
        float sine = MathHelper.sin(age * 0.075F) * 0.15F;
        float bodyX = (-0.15F + 0.075F * cosine) * factor;
        body.rotateAngleX += bodyX;
        body.rotationPointY -= sine * factor;
        head.rotateAngleX -= bodyX;
        topGills.rotateAngleX += 0.2F * cosine * factor;
        float gillY = (-0.3F * cosine - 0.19F) * factor;
        leftGills.rotateAngleY += gillY;
        rightGills.rotateAngleY -= gillY;
        leftHindLeg.rotateAngleX += (2.3561945F - cosine * 0.11F) * factor;
        leftHindLeg.rotateAngleY += 0.47123894F * factor;
        leftHindLeg.rotateAngleZ += 1.7278761F * factor;
        leftFrontLeg.rotateAngleX += (0.7853982F - cosine * 0.2F) * factor;
        leftFrontLeg.rotateAngleY += 2.042035F * factor;
        tail.rotateAngleY += 0.5F * cosine * factor;
    }

    private void setCrawlingPose(float age, float factor) {
        if (factor <= 1.0E-5F) {
            return;
        }
        float cosine = MathHelper.cos(age * 0.11F);
        float hindSway = (cosine * cosine - 2.0F * cosine) / 5.0F;
        float frontSway = 0.7F * cosine;
        float headAndTailY = 0.09F * cosine * factor;
        head.rotateAngleY += headAndTailY;
        tail.rotateAngleY += headAndTailY;
        float gill = 0.6F - 0.08F * (cosine * cosine
                + 2.0F * MathHelper.sin(age * 0.11F));
        topGills.rotateAngleX += gill * factor;
        leftGills.rotateAngleY -= gill * factor;
        rightGills.rotateAngleY += gill * factor;
        leftHindLeg.rotateAngleX += 0.9424779F * factor;
        leftHindLeg.rotateAngleY += (1.5F - hindSway) * factor;
        leftHindLeg.rotateAngleZ -= 0.1F * factor;
        leftFrontLeg.rotateAngleX += 1.0995574F * factor;
        leftFrontLeg.rotateAngleY += (1.5707964F - frontSway) * factor;
        rightHindLeg.rotateAngleX += 0.9424779F * factor;
        rightHindLeg.rotateAngleY += (-1.0F - hindSway) * factor;
        rightFrontLeg.rotateAngleX += 1.0995574F * factor;
        rightFrontLeg.rotateAngleY += (-1.5707964F - frontSway) * factor;
    }

    private void setGroundIdlePose(float age, float factor) {
        if (factor <= 1.0E-5F) {
            return;
        }
        float sine = MathHelper.sin(age * 0.09F);
        float cosine = MathHelper.cos(age * 0.09F);
        float movement = sine * sine - 2.0F * sine;
        float movement2 = cosine * cosine - 3.0F * sine;
        head.rotateAngleX -= 0.09F * movement * factor;
        head.rotateAngleZ -= 0.2F * factor;
        tail.rotateAngleY += (-0.1F + 0.1F * movement) * factor;
        float gill = (0.6F + 0.05F * movement2) * factor;
        topGills.rotateAngleX += gill;
        leftGills.rotateAngleY -= gill;
        rightGills.rotateAngleY += gill;
        leftHindLeg.rotateAngleX += 1.1F * factor;
        leftHindLeg.rotateAngleY += 1.0F * factor;
        leftFrontLeg.rotateAngleX += 0.8F * factor;
        leftFrontLeg.rotateAngleY += 2.3F * factor;
        leftFrontLeg.rotateAngleZ -= 0.5F * factor;
    }

    private void setPlayDeadPose(float factor) {
        if (factor <= 1.0E-5F) {
            return;
        }
        leftHindLeg.rotateAngleX += 1.4137167F * factor;
        leftHindLeg.rotateAngleY += 1.0995574F * factor;
        leftHindLeg.rotateAngleZ += 0.7853982F * factor;
        leftFrontLeg.rotateAngleX += 0.7853982F * factor;
        leftFrontLeg.rotateAngleY += 2.042035F * factor;
        body.rotateAngleX -= 0.15F * factor;
        body.rotateAngleZ += 0.35F * factor;
    }

    private void mirrorLegs(float factor) {
        if (factor <= 1.0E-5F) {
            return;
        }
        rightHindLeg.rotateAngleX += leftHindLeg.rotateAngleX * factor;
        rightHindLeg.rotateAngleY -= leftHindLeg.rotateAngleY * factor;
        rightHindLeg.rotateAngleZ -= leftHindLeg.rotateAngleZ * factor;
        rightFrontLeg.rotateAngleX += leftFrontLeg.rotateAngleX * factor;
        rightFrontLeg.rotateAngleY -= leftFrontLeg.rotateAngleY * factor;
        rightFrontLeg.rotateAngleZ -= leftFrontLeg.rotateAngleZ * factor;
    }
}
