package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityFox;

public class ModelFox extends ModelBase {
    final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer tail;
    private final ModelRenderer[] legs = new ModelRenderer[4];
    private final boolean baby;
    private float partialTicks;
    private float babyLegStretch = 1.0F;

    public ModelFox() {
        this(false);
    }

    ModelFox(boolean baby) {
        this.baby = baby;
        textureWidth = baby ? 32 : 48;
        textureHeight = 32;
        if (baby) {
            head = new ModelRenderer(this, 0, 0);
            head.addBox(-3.0F, -2.125F, -5.125F, 6, 5, 5);
            head.setTextureOffset(18, 20).addBox(-1.0F, 0.875F, -7.125F, 2, 2, 2);
            head.setTextureOffset(22, 8).addBox(-3.0F, -4.125F, -4.125F, 2, 2, 1);
            head.setTextureOffset(22, 11).addBox(1.0F, -4.125F, -4.125F, 2, 2, 1);
            body = new ModelRenderer(this, 0, 10);
            body.addBox(-2.5F, -2.0F, -3.0F, 5, 4, 6);
            tail = new ModelRenderer(this, 0, 20);
            tail.addBox(-1.5F, -1.48F, -1.0F, 3, 3, 6);
        } else {
            head = new ModelRenderer(this, 1, 5);
            head.addBox(-3.0F, -2.0F, -5.0F, 8, 6, 6);
            head.setTextureOffset(8, 1).addBox(-3.0F, -4.0F, -4.0F, 2, 2, 1);
            head.setTextureOffset(15, 1).addBox(3.0F, -4.0F, -4.0F, 2, 2, 1);
            head.setTextureOffset(6, 18).addBox(-1.0F, 2.01F, -8.0F, 4, 2, 3);
            body = new ModelRenderer(this, 24, 15);
            body.addBox(-3.0F, 3.999F, -3.5F, 6, 11, 6);
            tail = new ModelRenderer(this, 30, 0);
            tail.addBox(2.0F, 0.0F, -1.0F, 4, 9, 5);
        }
        body.addChild(tail);
        for (int index = 0; index < legs.length; index++) {
            if (baby) {
                legs[index] = new ModelRenderer(this);
                ModelRenderer foot = new BabyLeg(22, index % 2 == 0 ? 4 : 0);
                foot.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2);
                legs[index].addChild(foot);
            } else {
                legs[index] = new ModelRenderer(this, index % 2 == 0 ? 13 : 4, 24);
                legs[index].addBox(2.0F, 0.5F, -1.0F, 2, 6, 2, 0.001F);
            }
        }
        resetPose();
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                scale, entity);
        head.render(scale);
        body.render(scale);
        for (ModelRenderer leg : legs) {
            leg.render(scale);
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        EntityFox fox = (EntityFox) entity;
        resetPose();
        head.rotateAngleZ = fox.getInterestedAngle(partialTicks) * 0.11F * (float) Math.PI;
        if (baby) {
            animateBabyWalk(limbSwing, limbSwingAmount);
        } else {
            float gait = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            legs[0].rotateAngleX = gait;
            legs[1].rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                    * 1.4F * limbSwingAmount;
            legs[2].rotateAngleX = legs[1].rotateAngleX;
            legs[3].rotateAngleX = gait;
        }
        float crouch = fox.getCrouchAmount(partialTicks);
        if (fox.isCrouching()) {
            body.rotateAngleX += 0.10471976F;
            body.rotationPointY += baby ? crouch / 6.0F : crouch;
            head.rotationPointY += crouch * (baby ? 0.5F : 1.0F);
            float wiggle = MathHelper.cos(ageInTicks) * 0.05F;
            body.rotateAngleY = wiggle;
            for (int index = 0; index < legs.length; index++) {
                legs[index].rotateAngleZ = index < 2 ? wiggle : wiggle / 2.0F;
            }
        } else if (fox.isSleeping()) {
            body.rotateAngleZ = -1.5707963F;
            if (baby) {
                body.rotateAngleX = -0.17453292F;
                body.rotationPointY += 1.5F;
                body.rotationPointZ -= 1.5F;
                body.rotationPointX -= 1.5F;
                tail.rotateAngleX = -2.1816616F;
                tail.rotationPointX -= 0.7F;
                tail.rotationPointZ += 0.6F;
                tail.rotationPointY += 0.9F;
                head.rotationPointX -= 2.0F;
                head.rotationPointY += 2.8F;
                head.rotationPointZ -= 4.0F;
            } else {
                body.rotationPointY += 5.0F;
                tail.rotateAngleX = -2.6179938F;
                head.rotationPointX += 2.0F;
                head.rotationPointY += 2.99F;
            }
            for (ModelRenderer leg : legs) {
                leg.showModel = false;
            }
        } else if (fox.isSitting()) {
            if (baby) {
                body.rotateAngleX = -0.959931F;
                body.rotationPointZ -= 2.25F;
                body.rotationPointY += 1.5F;
                tail.rotationPointY -= 0.6F;
                tail.rotationPointZ -= 1.0F;
                tail.rotateAngleX = 0.95993114F;
                head.rotationPointY -= 0.75F;
                for (int index = 0; index < legs.length; index++) {
                    legs[index].rotationPointZ -= index < 2 ? 3.75F : 1.5F;
                    legs[index].rotationPointX += index % 2 == 0 ? 0.01F : -0.01F;
                }
            } else {
                body.rotateAngleX = 0.5235988F;
                body.rotationPointY -= 7.0F;
                body.rotationPointZ += 3.0F;
                tail.rotateAngleX = 0.7853982F;
                tail.rotationPointZ -= 1.0F;
                head.rotationPointY -= 6.5F;
                head.rotationPointZ += 2.75F;
                for (int index = 0; index < 2; index++) {
                    legs[index].rotateAngleX = -1.3089969F;
                    legs[index].rotationPointY += 4.0F;
                    legs[index].rotationPointZ -= 0.25F;
                }
            }
            legs[2].rotateAngleX = -0.2617994F;
            legs[3].rotateAngleX = -0.2617994F;
        }
        if (fox.isPouncing() && !baby) {
            body.rotationPointY -= crouch / 2.0F;
            head.rotationPointY -= crouch / 2.0F;
        }
        if (!fox.isSleeping() && !fox.isCrouching()) {
            head.rotateAngleY = netHeadYaw * 0.017453292F;
            head.rotateAngleX = headPitch * 0.017453292F;
        }
        if (fox.isSleeping()) {
            head.rotateAngleY = -2.0943952F;
            head.rotateAngleZ = MathHelper.cos(ageInTicks * 0.027F) / 22.0F;
        }
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing,
                                    float limbSwingAmount, float partialTickTime) {
        partialTicks = partialTickTime;
    }

    private void animateBabyWalk(float limbSwing, float limbSwingAmount) {
        float weight = Math.min(limbSwingAmount * 2.5F, 1.0F);
        float elapsed = (long) (limbSwing * 50.0F) / 1000.0F % 0.5F;
        float segment = elapsed <= 0.25F ? elapsed / 0.25F : (elapsed - 0.25F) / 0.25F;
        float angle = 35.0F * 0.017453292F * (elapsed <= 0.25F
                ? -1.0F + 2.0F * segment
                : 1.0F - 5.0F * segment * segment + 3.0F * segment * segment * segment);
        legs[0].rotateAngleX = angle * weight;
        legs[1].rotateAngleX = -angle * weight;
        legs[2].rotateAngleX = -angle * weight;
        legs[3].rotateAngleX = angle * weight;
        for (int index = 0; index < legs.length; index++) {
            legs[index].rotationPointX += (index % 2 == 0 ? 0.05F : -0.05F) * weight;
            legs[index].rotationPointY -= 0.6F * weight;
            legs[index].rotationPointZ -= (index < 2 ? 0.02F : 0.4F) * weight;
        }
        babyLegStretch = 1.0F + 0.15F * weight;
        head.rotationPointY += 1.025F * weight;
        tail.rotateAngleX -= 2.5F * 0.017453292F * weight;
        tail.rotationPointY += 0.05F * weight;
    }

    private void resetPose() {
        babyLegStretch = 1.0F;
        head.setRotationPoint(baby ? 0.0F : -1.0F, baby ? 18.125F : 16.5F,
                baby ? 0.125F : -3.0F);
        body.setRotationPoint(0.0F, baby ? 20.0F : 16.0F, baby ? 2.0F : -6.0F);
        tail.setRotationPoint(baby ? 0.0F : -4.0F, baby ? -0.5F : 15.0F,
                baby ? 3.0F : -1.0F);
        head.rotateAngleX = head.rotateAngleY = head.rotateAngleZ = 0.0F;
        body.rotateAngleX = baby ? 0.0F : 1.5707964F;
        body.rotateAngleY = body.rotateAngleZ = 0.0F;
        tail.rotateAngleX = baby ? 0.0F : -0.05235988F;
        tail.rotateAngleY = tail.rotateAngleZ = 0.0F;
        for (int index = 0; index < legs.length; index++) {
            legs[index].setRotationPoint(baby ? index % 2 == 0 ? -1.5F : 1.5F
                    : index % 2 == 0 ? -5.0F : -1.0F, baby ? 22.0F : 17.5F,
                    index < 2 ? baby ? 4.0F : 7.0F : 0.0F);
            legs[index].rotateAngleX = legs[index].rotateAngleY = legs[index].rotateAngleZ = 0.0F;
            legs[index].showModel = true;
        }
    }

    private final class BabyLeg extends ModelRenderer {
        private BabyLeg(int textureX, int textureY) {
            super(ModelFox.this, textureX, textureY);
        }

        @Override
        public void render(float scale) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F, babyLegStretch, 1.0F);
            super.render(scale);
            GlStateManager.popMatrix();
        }
    }
}
