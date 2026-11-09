package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityPanda;

public class ModelPanda extends ModelBase {
    public final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer rightHindLeg;
    private final ModelRenderer leftHindLeg;
    private final ModelRenderer rightFrontLeg;
    private final ModelRenderer leftFrontLeg;
    private final boolean baby;
    private float partialTicks;

    public ModelPanda() {
        this(false);
    }

    ModelPanda(boolean baby) {
        this.baby = baby;
        textureWidth = 64;
        textureHeight = 64;
        if (baby) {
            head = new ModelRenderer(this, 0, 0);
            head.addBox(-3.5F, -3.0F, -5.0F, 7, 6, 5);
            head.setTextureOffset(24, 6).addBox(-2.0F, 1.0F, -6.0F, 4, 2, 1);
            head.setTextureOffset(24, 0).addBox(-4.5F, -4.0F, -3.5F, 3, 3, 1);
            head.setTextureOffset(33, 0).addBox(1.5F, -4.0F, -3.5F, 3, 3, 1);
            body = new ModelRenderer(this, 0, 11);
            body.addBox(-4.5F, -3.5F, -5.5F, 9, 7, 11);
        } else {
            head = new ModelRenderer(this, 0, 6);
            head.addBox(-6.5F, -5.0F, -4.0F, 13, 10, 9);
            head.setTextureOffset(45, 16).addBox(-3.5F, 0.0F, -6.0F, 7, 5, 2);
            head.setTextureOffset(52, 25).addBox(3.5F, -8.0F, -1.0F, 5, 4, 1);
            head.setTextureOffset(52, 25).addBox(-8.5F, -8.0F, -1.0F, 5, 4, 1);
            body = new ModelRenderer(this, 0, 25);
            body.addBox(-9.5F, -13.0F, -6.5F, 19, 26, 13);
        }
        rightHindLeg = createLeg(false, false);
        leftHindLeg = createLeg(true, false);
        rightFrontLeg = createLeg(false, true);
        leftFrontLeg = createLeg(true, true);
    }

    private ModelRenderer createLeg(boolean left, boolean front) {
        ModelRenderer leg = new ModelRenderer(this, baby ? left ? 12 : 0 : 40,
                baby ? front ? 29 : 34 : 0);
        if (baby) {
            leg.addBox(-1.5F, 0.0F, -1.5F, 3, 2, 3);
        } else {
            leg.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6);
        }
        return leg;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                headPitch, scale, entity);
        head.render(scale);
        body.render(scale);
        rightHindLeg.render(scale);
        leftHindLeg.render(scale);
        rightFrontLeg.render(scale);
        leftFrontLeg.render(scale);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing,
                                    float limbSwingAmount, float partialTickTime) {
        partialTicks = partialTickTime;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale,
                                  Entity entity) {
        EntityPanda panda = (EntityPanda) entity;
        float sitting = panda.getSitAmount(partialTicks);
        float onBack = panda.getOnBackAmount(partialTicks);
        float rolling = baby ? 0.0F : panda.getRollAmount(partialTicks);
        head.setRotationPoint(0.0F, baby ? 19.0F : 11.5F, baby ? -3.0F : -17.0F);
        body.setRotationPoint(0.0F, baby ? 18.5F : 10.0F, baby ? 2.5F : 0.0F);
        body.rotateAngleX = baby ? 0.0F : 1.5707964F;
        rightHindLeg.setRotationPoint(baby ? -3.0F : -5.5F, baby ? 22.0F : 15.0F,
                baby ? 6.5F : 9.0F);
        leftHindLeg.setRotationPoint(baby ? 3.0F : 5.5F, baby ? 22.0F : 15.0F,
                baby ? 6.5F : 9.0F);
        rightFrontLeg.setRotationPoint(baby ? -3.0F : -5.5F, baby ? 22.0F : 15.0F,
                baby ? -1.5F : -9.0F);
        leftFrontLeg.setRotationPoint(baby ? 3.0F : 5.5F, baby ? 22.0F : 15.0F,
                baby ? -1.5F : -9.0F);
        head.rotateAngleX = headPitch * 0.017453292F;
        head.rotateAngleY = netHeadYaw * 0.017453292F;
        head.rotateAngleZ = 0.0F;
        float walk = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        rightHindLeg.rotateAngleX = walk;
        leftHindLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * 1.4F * limbSwingAmount;
        rightFrontLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * 1.4F * limbSwingAmount;
        leftFrontLeg.rotateAngleX = walk;
        rightHindLeg.rotateAngleZ = 0.0F;
        leftHindLeg.rotateAngleZ = 0.0F;
        rightFrontLeg.rotateAngleZ = 0.0F;
        leftFrontLeg.rotateAngleZ = 0.0F;
        if (panda.isSneezing()) {
            int sneezeTicks = panda.getSneezeTicks();
            if (sneezeTicks < 15) {
                head.rotateAngleX = -0.7853982F * sneezeTicks / 14.0F;
            } else if (sneezeTicks < 20) {
                head.rotateAngleX = -0.7853982F
                        + 0.7853982F * (sneezeTicks - 15) / 5.0F;
            }
        }
        if (panda.getUnhappyTicks() > 0) {
            head.rotateAngleY = 0.35F * MathHelper.sin(0.6F * ageInTicks);
            head.rotateAngleZ = 0.35F * MathHelper.sin(0.6F * ageInTicks);
            rightFrontLeg.rotateAngleX = -0.75F * MathHelper.sin(0.3F * ageInTicks);
            leftFrontLeg.rotateAngleX = 0.75F * MathHelper.sin(0.3F * ageInTicks);
        }
        if (sitting > 0.0F) {
            body.rotateAngleX = lerp(sitting, body.rotateAngleX, baby ? 0.17453292F : 1.7407963F);
            if (baby) {
                body.rotationPointZ = lerp(sitting, body.rotationPointZ, -1.5F);
                head.rotationPointZ = lerp(sitting, head.rotationPointZ, -11.5F);
                head.rotationPointY = lerp(sitting, head.rotationPointY, 17.5F);
                rightFrontLeg.rotationPointZ = lerp(sitting, rightFrontLeg.rotationPointZ, -5.0F);
                leftFrontLeg.rotationPointZ = lerp(sitting, leftFrontLeg.rotationPointZ, -5.0F);
                rightHindLeg.rotationPointZ = lerp(sitting, rightHindLeg.rotationPointZ, 3.0F);
                leftHindLeg.rotationPointZ = lerp(sitting, leftHindLeg.rotationPointZ, 3.0F);
            } else {
                head.rotateAngleX = lerp(sitting, head.rotateAngleX, 1.5707964F);
            }
            rightFrontLeg.rotateAngleZ = -0.27079642F;
            leftFrontLeg.rotateAngleZ = 0.27079642F;
            rightHindLeg.rotateAngleZ = 0.5707964F;
            leftHindLeg.rotateAngleZ = -0.5707964F;
            if (panda.isEating()) {
                head.rotateAngleX = 1.5707964F + 0.2F * MathHelper.sin(ageInTicks * 0.6F);
                rightFrontLeg.rotateAngleX = -0.4F - 0.2F * MathHelper.sin(ageInTicks * 0.6F);
                leftFrontLeg.rotateAngleX = -0.4F - 0.2F * MathHelper.sin(ageInTicks * 0.6F);
            }
            if (panda.isScared()) {
                head.rotateAngleX = 2.1707964F;
                rightFrontLeg.rotateAngleX = -0.9F;
                leftFrontLeg.rotateAngleX = -0.9F;
            }
        }
        if (onBack > 0.0F) {
            float movement = MathHelper.sin(ageInTicks * 0.15F);
            rightHindLeg.rotateAngleX = -0.6F * movement;
            leftHindLeg.rotateAngleX = 0.6F * movement;
            rightFrontLeg.rotateAngleX = 0.3F * MathHelper.sin(ageInTicks * 0.25F);
            leftFrontLeg.rotateAngleX = -0.3F * MathHelper.sin(ageInTicks * 0.25F);
            head.rotateAngleX = lerp(onBack, head.rotateAngleX, 1.5707964F);
        }
        if (rolling > 0.0F) {
            head.rotateAngleX = lerp(rolling, head.rotateAngleX, 2.0561945F);
            float movement = MathHelper.sin(ageInTicks * 0.5F);
            rightHindLeg.rotateAngleX = -0.5F * movement;
            leftHindLeg.rotateAngleX = 0.5F * movement;
            rightFrontLeg.rotateAngleX = 0.5F * movement;
            leftFrontLeg.rotateAngleX = -0.5F * movement;
        }
    }

    private static float lerp(float amount, float start, float end) {
        return start + (end - start) * amount;
    }
}
