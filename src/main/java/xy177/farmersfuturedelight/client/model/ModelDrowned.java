package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityDrowned;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ModelDrowned extends ModelBiped {
    private float swimAmount;

    public ModelDrowned(float modelSize, float yOffset, int textureWidth,
                        int textureHeight) {
        super(modelSize, yOffset, textureWidth, textureHeight);
        bipedLeftArm = new ModelRenderer(this, 32, 48);
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F + yOffset, 0.0F);
        bipedLeftLeg = new ModelRenderer(this, 16, 48);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F + yOffset, 0.0F);
    }

    public ModelDrowned(float modelSize, boolean armor) {
        super(modelSize, 0.0F, 64, armor ? 32 : 64);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing,
                                    float limbSwingAmount, float partialTickTime) {
        super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
        swimAmount = entity instanceof EntityDrowned
                ? ((EntityDrowned) entity).getSwimAmount(partialTickTime) : 0.0F;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount,
                                  float ageInTicks, float netHeadYaw, float headPitch,
                                  float scaleFactor, Entity entity) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                headPitch, scaleFactor, entity);
        if (!(entity instanceof EntityDrowned)) {
            return;
        }
        EntityDrowned drowned = (EntityDrowned) entity;
        boolean throwing = FFDItems.isTridentStack(drowned.getHeldItemMainhand())
                && drowned.isArmsRaised();
        boolean throwingRight = throwing && drowned.getPrimaryHand() == EnumHandSide.RIGHT;
        boolean throwingLeft = throwing && drowned.getPrimaryHand() == EnumHandSide.LEFT;
        restorePivots(drowned);
        if (throwingRight || throwingLeft) {
            prepareThrowingArm(drowned, throwingRight, limbSwing, limbSwingAmount);
        }
        applySwimmingCycle(drowned, limbSwing);
        animateZombieArms(drowned, throwingRight, throwingLeft);
        if (isSneak) {
            bipedRightArm.rotateAngleX += 0.4F;
            bipedLeftArm.rotateAngleX += 0.4F;
        }
        bobArms(ageInTicks);
        if (throwingRight) {
            bipedRightArm.rotateAngleX = bipedRightArm.rotateAngleX * 0.5F
                    - (float) Math.PI;
            bipedRightArm.rotateAngleY = 0.0F;
        }
        if (throwingLeft) {
            bipedLeftArm.rotateAngleX = bipedLeftArm.rotateAngleX * 0.5F
                    - (float) Math.PI;
            bipedLeftArm.rotateAngleY = 0.0F;
        }
        if (swimAmount > 0.0F) {
            float wave = swimAmount * 0.35F * MathHelper.sin(0.1F * ageInTicks);
            bipedRightArm.rotateAngleX = rotLerp(swimAmount, bipedRightArm.rotateAngleX,
                    -2.5132742F) + wave;
            bipedLeftArm.rotateAngleX = rotLerp(swimAmount, bipedLeftArm.rotateAngleX,
                    -2.5132742F) - wave;
            bipedRightArm.rotateAngleZ = rotLerp(swimAmount, bipedRightArm.rotateAngleZ,
                    -0.15F);
            bipedLeftArm.rotateAngleZ = rotLerp(swimAmount, bipedLeftArm.rotateAngleZ,
                    0.15F);
            float legWave = swimAmount * 0.55F * MathHelper.sin(0.1F * ageInTicks);
            bipedLeftLeg.rotateAngleX -= legWave;
            bipedRightLeg.rotateAngleX += legWave;
            bipedHead.rotateAngleX = 0.0F;
        }
        copyModelAngles(bipedHead, bipedHeadwear);
    }

    private void prepareThrowingArm(EntityDrowned drowned, boolean right, float limbSwing,
                                    float limbSwingAmount) {
        float speed = 1.0F;
        if (drowned.getTicksElytraFlying() > 4) {
            speed = (float) (drowned.motionX * drowned.motionX
                    + drowned.motionY * drowned.motionY
                    + drowned.motionZ * drowned.motionZ) / 0.2F;
            speed = Math.max(speed * speed * speed, 1.0F);
        }
        ModelRenderer arm = right ? bipedRightArm : bipedLeftArm;
        float phase = right ? (float) Math.PI : 0.0F;
        arm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + phase)
                * limbSwingAmount / speed;
        if (isRiding) {
            arm.rotateAngleX -= (float) Math.PI / 5.0F;
        }
        if (swingProgress > 0.0F) {
            EnumHandSide swingingSide = drowned.swingingHand == EnumHand.MAIN_HAND
                    ? drowned.getPrimaryHand() : drowned.getPrimaryHand().opposite();
            float bodyYaw = bipedBody.rotateAngleY;
            arm.rotateAngleY += bodyYaw;
            if (!right) {
                arm.rotateAngleX += bodyYaw;
            }
            if ((right && swingingSide == EnumHandSide.RIGHT)
                    || (!right && swingingSide == EnumHandSide.LEFT)) {
                float eased = 1.0F - swingProgress;
                eased *= eased;
                eased *= eased;
                eased = MathHelper.sin((1.0F - eased) * (float) Math.PI);
                float head = MathHelper.sin(swingProgress * (float) Math.PI)
                        * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
                arm.rotateAngleX -= eased * 1.2F + head;
                arm.rotateAngleY += bodyYaw * 2.0F;
                arm.rotateAngleZ -= MathHelper.sin(swingProgress * (float) Math.PI) * 0.4F;
            }
        }
    }

    private void animateZombieArms(EntityDrowned drowned, boolean throwingRight,
                                   boolean throwingLeft) {
        float swing = MathHelper.sin(swingProgress * (float) Math.PI);
        float eased = MathHelper.sin((1.0F - (1.0F - swingProgress)
                * (1.0F - swingProgress)) * (float) Math.PI);
        boolean raised = !drowned.isChild() || drowned.getHeldItemMainhand().isEmpty();
        float direction = raised ? 1.0F : -1.0F;
        float attack = direction * swing;
        float drop = raised
                ? -(float) Math.PI / (drowned.isArmsRaised() ? 1.5F : 2.25F)
                : 0.0F;
        float x = drop + attack * 1.2F - eased * 0.4F;
        float y = 0.1F - attack * 0.6F;
        if (!throwingRight) {
            bipedRightArm.rotateAngleX = x;
            bipedRightArm.rotateAngleY = raised ? -y : y;
            bipedRightArm.rotateAngleZ = 0.0F;
        }
        if (!throwingLeft) {
            bipedLeftArm.rotateAngleX = x;
            bipedLeftArm.rotateAngleY = raised ? y : -y;
            bipedLeftArm.rotateAngleZ = 0.0F;
        }
    }

    private void bobArms(float ageInTicks) {
        float z = MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        float x = MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        bipedRightArm.rotateAngleZ += z;
        bipedLeftArm.rotateAngleZ -= z;
        bipedRightArm.rotateAngleX += x;
        bipedLeftArm.rotateAngleX -= x;
    }

    private void applySwimmingCycle(EntityDrowned drowned, float limbSwing) {
        if (swimAmount <= 0.0F || drowned.isHandActive()) {
            if (swimAmount > 0.0F) {
                applySwimmingLegs(limbSwing);
            }
            return;
        }
        float cycle = limbSwing % 26.0F;
        if (cycle < 0.0F) {
            cycle += 26.0F;
        }
        EnumHandSide swingingSide = swingProgress > 0.0F
                ? (drowned.swingingHand == EnumHand.MAIN_HAND
                ? drowned.getPrimaryHand() : drowned.getPrimaryHand().opposite()) : null;
        float rightAmount = swingingSide == EnumHandSide.RIGHT ? 0.0F : swimAmount;
        float leftAmount = swingingSide == EnumHandSide.LEFT ? 0.0F : swimAmount;
        if (cycle < 14.0F) {
            applySwimArm(bipedLeftArm, leftAmount, 0.0F, (float) Math.PI,
                    (float) Math.PI + 1.8707964F * armAngle(cycle) / armAngle(14.0F), true);
            applySwimArm(bipedRightArm, rightAmount, 0.0F, (float) Math.PI,
                    (float) Math.PI - 1.8707964F * armAngle(cycle) / armAngle(14.0F), false);
        } else if (cycle < 22.0F) {
            float progress = (cycle - 14.0F) / 8.0F;
            applySwimArm(bipedLeftArm, leftAmount, (float) Math.PI / 2.0F * progress,
                    (float) Math.PI, 5.012389F - 1.8707964F * progress, true);
            applySwimArm(bipedRightArm, rightAmount, (float) Math.PI / 2.0F * progress,
                    (float) Math.PI, 1.2707963F + 1.8707964F * progress, false);
        } else {
            float progress = (cycle - 22.0F) / 4.0F;
            applySwimArm(bipedLeftArm, leftAmount,
                    (float) Math.PI / 2.0F * (1.0F - progress),
                    (float) Math.PI, (float) Math.PI, true);
            applySwimArm(bipedRightArm, rightAmount,
                    (float) Math.PI / 2.0F * (1.0F - progress),
                    (float) Math.PI, (float) Math.PI, false);
        }
        applySwimmingLegs(limbSwing);
    }

    private void applySwimmingLegs(float limbSwing) {
        bipedLeftLeg.rotateAngleX = lerp(swimAmount, bipedLeftLeg.rotateAngleX,
                0.3F * MathHelper.cos(limbSwing * 0.33333334F + (float) Math.PI));
        bipedRightLeg.rotateAngleX = lerp(swimAmount, bipedRightLeg.rotateAngleX,
                0.3F * MathHelper.cos(limbSwing * 0.33333334F));
    }

    private static void applySwimArm(ModelRenderer arm, float amount, float x, float y,
                                     float z, boolean rotational) {
        arm.rotateAngleX = rotational ? rotLerp(amount, arm.rotateAngleX, x)
                : lerp(amount, arm.rotateAngleX, x);
        arm.rotateAngleY = rotational ? rotLerp(amount, arm.rotateAngleY, y)
                : lerp(amount, arm.rotateAngleY, y);
        arm.rotateAngleZ = rotational ? rotLerp(amount, arm.rotateAngleZ, z)
                : lerp(amount, arm.rotateAngleZ, z);
    }

    private void restorePivots(EntityDrowned drowned) {
        boolean child = drowned.isChild();
        float ageScale = child ? 0.5F : 1.0F;
        float headY = child ? 15.25F : 0.0F;
        float bodyY = child ? 17.5F : 0.0F;
        float armY = child ? 15.5F : 2.0F;
        float legY = child ? 20.0F : 12.0F;
        bipedHead.rotationPointY = headY + (isSneak ? 4.2F : 0.0F);
        bipedBody.rotationPointY = bodyY + (isSneak ? 3.2F : 0.0F);
        bipedRightArm.rotationPointY = armY + (isSneak ? 3.2F : 0.0F);
        bipedLeftArm.rotationPointY = armY + (isSneak ? 3.2F : 0.0F);
        bipedRightLeg.rotationPointY = legY;
        bipedLeftLeg.rotationPointY = legY;
        bipedRightLeg.rotationPointZ = isSneak ? 4.0F : 0.0F;
        bipedLeftLeg.rotationPointZ = isSneak ? 4.0F : 0.0F;
        if (swingProgress > 0.0F) {
            float radius = 5.0F * ageScale;
            bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * radius;
            bipedRightArm.rotationPointX = -MathHelper.cos(bipedBody.rotateAngleY) * radius;
            bipedLeftArm.rotationPointZ = -MathHelper.sin(bipedBody.rotateAngleY) * radius;
            bipedLeftArm.rotationPointX = MathHelper.cos(bipedBody.rotateAngleY) * radius;
        } else {
            bipedRightArm.rotationPointZ = 0.0F;
            bipedLeftArm.rotationPointZ = 0.0F;
            bipedRightArm.rotationPointX = child ? -3.0F : -5.0F;
            bipedLeftArm.rotationPointX = child ? 3.0F : 5.0F;
        }
    }

    private static float armAngle(float value) {
        return -65.0F * value + value * value;
    }

    private static float rotLerp(float amount, float from, float to) {
        float delta = (to - from) % ((float) Math.PI * 2.0F);
        if (delta < -(float) Math.PI) {
            delta += (float) Math.PI * 2.0F;
        }
        if (delta >= (float) Math.PI) {
            delta -= (float) Math.PI * 2.0F;
        }
        return from + amount * delta;
    }

    private static float lerp(float amount, float from, float to) {
        return from + amount * (to - from);
    }
}
