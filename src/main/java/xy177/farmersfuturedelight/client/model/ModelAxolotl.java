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

        if (axolotl.isPlayingDead()) {
            setPlayDeadPose();
        } else if (axolotl.isInWater()) {
            if (limbSwingAmount > 0.02F) {
                setSwimmingPose(ageInTicks, headPitch);
            } else {
                setHoveringPose(ageInTicks);
            }
        } else if (axolotl.onGround) {
            if (limbSwingAmount > 0.02F) {
                setCrawlingPose(ageInTicks);
            } else {
                setGroundIdlePose(ageInTicks);
            }
        }
        if (!axolotl.isPlayingDead()) {
            head.rotateAngleY += MathHelper.clamp(netHeadYaw, -45.0F, 45.0F) * 0.017453292F;
        }
        mirrorLegs();
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

    private void setSwimmingPose(float age, float headPitch) {
        float time = age * 0.33F;
        float sine = MathHelper.sin(time);
        float cosine = MathHelper.cos(time);
        float bodySway = 0.13F * sine;
        body.rotateAngleX = headPitch * 0.017453292F + bodySway;
        body.rotationPointY -= 0.45F * cosine;
        head.rotateAngleX = -bodySway * 1.8F;
        topGills.rotateAngleX = -0.5F * sine - 0.8F;
        leftGills.rotateAngleY = 0.3F * sine + 0.9F;
        rightGills.rotateAngleY = -leftGills.rotateAngleY;
        tail.rotateAngleY = 0.3F * MathHelper.cos(time * 0.9F);
        leftHindLeg.rotateAngleX = 1.8849558F;
        leftHindLeg.rotateAngleY = -0.4F * sine;
        leftHindLeg.rotateAngleZ = 1.5707964F;
        leftFrontLeg.rotateAngleX = 1.8849558F;
        leftFrontLeg.rotateAngleY = -0.2F * cosine - 0.1F;
        leftFrontLeg.rotateAngleZ = 1.5707964F;
    }

    private void setHoveringPose(float age) {
        float cosine = MathHelper.cos(age * 0.075F);
        float sine = MathHelper.sin(age * 0.075F) * 0.15F;
        body.rotateAngleX = -0.15F + 0.075F * cosine;
        body.rotationPointY -= sine;
        head.rotateAngleX = -body.rotateAngleX;
        topGills.rotateAngleX = 0.2F * cosine;
        leftGills.rotateAngleY = -0.3F * cosine - 0.19F;
        rightGills.rotateAngleY = -leftGills.rotateAngleY;
        leftHindLeg.rotateAngleX = 2.3561945F - cosine * 0.11F;
        leftHindLeg.rotateAngleY = 0.47123894F;
        leftHindLeg.rotateAngleZ = 1.7278761F;
        leftFrontLeg.rotateAngleX = 0.7853982F - cosine * 0.2F;
        leftFrontLeg.rotateAngleY = 2.042035F;
        tail.rotateAngleY = 0.5F * cosine;
    }

    private void setCrawlingPose(float age) {
        float cosine = MathHelper.cos(age * 0.11F);
        float hindSway = (cosine * cosine - 2.0F * cosine) / 5.0F;
        float frontSway = 0.7F * cosine;
        head.rotateAngleY = 0.09F * cosine;
        tail.rotateAngleY = head.rotateAngleY;
        float gill = 0.6F - 0.08F * (cosine * cosine
                + 2.0F * MathHelper.sin(age * 0.11F));
        topGills.rotateAngleX = gill;
        leftGills.rotateAngleY = -gill;
        rightGills.rotateAngleY = gill;
        leftHindLeg.rotateAngleX = 0.9424779F;
        leftHindLeg.rotateAngleY = 1.5F - hindSway;
        leftHindLeg.rotateAngleZ = -0.1F;
        leftFrontLeg.rotateAngleX = 1.0995574F;
        leftFrontLeg.rotateAngleY = 1.5707964F - frontSway;
    }

    private void setGroundIdlePose(float age) {
        float sine = MathHelper.sin(age * 0.09F);
        float cosine = MathHelper.cos(age * 0.09F);
        float movement = sine * sine - 2.0F * sine;
        float movement2 = cosine * cosine - 3.0F * sine;
        head.rotateAngleX = -0.09F * movement;
        head.rotateAngleZ = -0.2F;
        tail.rotateAngleY = -0.1F + 0.1F * movement;
        float gill = 0.6F + 0.05F * movement2;
        topGills.rotateAngleX = gill;
        leftGills.rotateAngleY = -gill;
        rightGills.rotateAngleY = gill;
        leftHindLeg.rotateAngleX = 1.1F;
        leftHindLeg.rotateAngleY = 1.0F;
        leftFrontLeg.rotateAngleX = 0.8F;
        leftFrontLeg.rotateAngleY = 2.3F;
        leftFrontLeg.rotateAngleZ = -0.5F;
    }

    private void setPlayDeadPose() {
        leftHindLeg.rotateAngleX = 1.4137167F;
        leftHindLeg.rotateAngleY = 1.0995574F;
        leftHindLeg.rotateAngleZ = 0.7853982F;
        leftFrontLeg.rotateAngleX = 0.7853982F;
        leftFrontLeg.rotateAngleY = 2.042035F;
        body.rotateAngleX = -0.15F;
        body.rotateAngleZ = 0.35F;
    }

    private void mirrorLegs() {
        rightHindLeg.rotateAngleX = leftHindLeg.rotateAngleX;
        rightHindLeg.rotateAngleY = -leftHindLeg.rotateAngleY;
        rightHindLeg.rotateAngleZ = -leftHindLeg.rotateAngleZ;
        rightFrontLeg.rotateAngleX = leftFrontLeg.rotateAngleX;
        rightFrontLeg.rotateAngleY = -leftFrontLeg.rotateAngleY;
        rightFrontLeg.rotateAngleZ = -leftFrontLeg.rotateAngleZ;
    }
}
