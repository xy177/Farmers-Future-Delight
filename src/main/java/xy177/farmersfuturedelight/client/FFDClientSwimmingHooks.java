package xy177.farmersfuturedelight.client;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.InputUpdateEvent;

import xy177.farmersfuturedelight.common.FFDRiptide;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.core.FFDGameplayHooks;

public final class FFDClientSwimmingHooks {
    private static final Map<EntityPlayer, AnimationState> ANIMATIONS = new WeakHashMap<>();
    private static final Map<EntityPlayer, CameraState> CAMERAS = new WeakHashMap<>();
    private static final Map<EntityPlayerSP, InputState> INPUTS = new WeakHashMap<>();
    private static final ThreadLocal<Integer> SUPPRESSED_MODELS =
            ThreadLocal.withInitial(() -> Integer.valueOf(0));

    private FFDClientSwimmingHooks() {
    }

    public static void updateSwimmingInput(InputUpdateEvent event) {
        if (!FFDGameplayHooks.isSwimmingSystemEnabled()
                || !(event.getEntityPlayer() instanceof EntityPlayerSP)) {
            return;
        }
        EntityPlayerSP player = (EntityPlayerSP) event.getEntityPlayer();
        if (FFDGameplayHooks.isSwimming(player) && event.getMovementInput().sneak) {
            event.getMovementInput().moveStrafe /= 0.3F;
            event.getMovementInput().moveForward /= 0.3F;
        }
    }

    public static void updateLocalPlayer(EntityPlayerSP player) {
        if (!FFDGameplayHooks.isSwimmingSystemEnabled() || player == null) {
            return;
        }
        InputState state = INPUTS.computeIfAbsent(player, key -> new InputState());
        if (state.sprintTimer > 0) {
            state.sprintTimer--;
        }
        boolean movingForward = player.movementInput.moveForward >= 0.8F;
        boolean canSwim = player.isInWater()
                && player.isInsideOfMaterial(net.minecraft.block.material.Material.WATER)
                && !player.capabilities.isFlying && !player.isRiding();
        boolean canSprint = player.getFoodStats().getFoodLevel() > 6
                || player.capabilities.allowFlying;
        canSprint = canSprint && !player.isHandActive()
                && !player.isPotionActive(MobEffects.BLINDNESS)
                && !player.movementInput.sneak;
        boolean startedMoving = movingForward && !state.wasMovingForward;

        if (canSwim && canSprint && startedMoving) {
            if (state.sprintTimer > 0) {
                state.swimSprinting = true;
            } else {
                state.sprintTimer = 7;
            }
        }
        if (canSwim && canSprint && movingForward
                && Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown()) {
            state.swimSprinting = true;
        }
        if (canSwim && player.isSprinting() && movingForward) {
            state.swimSprinting = true;
        }
        if (!player.isInWater() || !movingForward || !canSprint) {
            state.swimSprinting = false;
        }
        if (state.swimSprinting) {
            player.setSprinting(true);
        }
        FFDGameplayHooks.updateSwimmingState(player);
        if (FFDGameplayHooks.isSwimming(player) && player.movementInput.sneak
                && !player.capabilities.isFlying) {
            player.motionY -= 0.04D * player.getEntityAttribute(
                    EntityLivingBase.SWIM_SPEED).getAttributeValue();
        }
        if (FFDGameplayHooks.isSwimming(player)) {
            player.cameraYaw += (0.0F - player.cameraYaw) * 0.4F;
        }
        state.wasMovingForward = movingForward;
    }

    public static void applyModelPose(ModelBiped model, float limbSwing, float limbSwingAmount,
                                      float ageInTicks, Entity entity) {
        if (!(entity instanceof EntityPlayer) || SUPPRESSED_MODELS.get().intValue() > 0) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        applyTridentPose(model, limbSwing, limbSwingAmount, ageInTicks, player);
        if (!FFDGameplayHooks.isSwimmingSystemEnabled()) {
            return;
        }
        float partialTicks = MathHelper.clamp(ageInTicks - player.ticksExisted, 0.0F, 1.0F);
        float animation = getSwimAnimation(player, partialTicks);
        if (animation <= 0.0F) {
            return;
        }

        model.bipedHead.rotateAngleX = rotLerp(animation, model.bipedHead.rotateAngleX,
                -(float) Math.PI / 4.0F);
        float cycle = limbSwing % 26.0F;
        if (cycle < 0.0F) {
            cycle += 26.0F;
        }
        EnumHandSide activeSide = activeSwingingSide(player);
        float rightAnimation = activeSide == EnumHandSide.RIGHT && model.swingProgress > 0.0F
                ? 0.0F : animation;
        float leftAnimation = activeSide == EnumHandSide.LEFT && model.swingProgress > 0.0F
                ? 0.0F : animation;

        if (cycle < 14.0F) {
            applyArm(model.bipedLeftArm, leftAnimation, 0.0F, (float) Math.PI,
                    (float) Math.PI + 1.8707964F * armAngle(cycle) / armAngle(14.0F));
            applyArm(model.bipedRightArm, rightAnimation, 0.0F, (float) Math.PI,
                    (float) Math.PI - 1.8707964F * armAngle(cycle) / armAngle(14.0F));
        } else if (cycle < 22.0F) {
            float progress = (cycle - 14.0F) / 8.0F;
            applyArm(model.bipedLeftArm, leftAnimation, (float) Math.PI / 2.0F * progress,
                    (float) Math.PI, 5.012389F - 1.8707964F * progress);
            applyArm(model.bipedRightArm, rightAnimation,
                    (float) Math.PI / 2.0F * progress, (float) Math.PI,
                    1.2707963F + 1.8707964F * progress);
        } else {
            float progress = (cycle - 22.0F) / 4.0F;
            applyArm(model.bipedLeftArm, leftAnimation,
                    (float) Math.PI / 2.0F * (1.0F - progress),
                    (float) Math.PI, (float) Math.PI);
            applyArm(model.bipedRightArm, rightAnimation,
                    (float) Math.PI / 2.0F * (1.0F - progress),
                    (float) Math.PI, (float) Math.PI);
        }

        model.bipedLeftLeg.rotateAngleX = lerp(animation, model.bipedLeftLeg.rotateAngleX,
                0.3F * MathHelper.cos(limbSwing * 0.33333334F + (float) Math.PI));
        model.bipedRightLeg.rotateAngleX = lerp(animation, model.bipedRightLeg.rotateAngleX,
                0.3F * MathHelper.cos(limbSwing * 0.33333334F));
        ModelBiped.copyModelAngles(model.bipedHead, model.bipedHeadwear);
    }

    private static void applyTridentPose(ModelBiped model, float limbSwing,
                                         float limbSwingAmount, float ageInTicks,
                                         EntityPlayer player) {
        if (!player.isHandActive() || player.getItemInUseCount() <= 0
                || player.getActiveItemStack().isEmpty()
                || player.getActiveItemStack().getItem() != FFDItems.TRIDENT) {
            return;
        }
        float speed = 1.0F;
        if (player.getTicksElytraFlying() > 4) {
            speed = (float) (player.motionX * player.motionX + player.motionY * player.motionY
                    + player.motionZ * player.motionZ) / 0.2F;
            speed = speed * speed * speed;
            speed = Math.max(speed, 1.0F);
        }
        float rightX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * limbSwingAmount / speed;
        float leftX = MathHelper.cos(limbSwing * 0.6662F) * limbSwingAmount / speed;
        if (model.isRiding) {
            rightX -= 0.62831855F;
            leftX -= 0.62831855F;
        }
        EnumHandSide activeSide = player.getActiveHand() == EnumHand.MAIN_HAND
                ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        if (activeSide == EnumHandSide.RIGHT) {
            rightX = rightX * 0.5F - (float) Math.PI;
        } else {
            leftX = leftX * 0.5F - (float) Math.PI;
        }
        if (model.isSneak) {
            if (activeSide == EnumHandSide.RIGHT) {
                rightX += 0.4F;
            } else {
                leftX += 0.4F;
            }
        }
        float idleX = MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        float idleZ = MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        if (activeSide == EnumHandSide.RIGHT) {
            model.bipedRightArm.rotateAngleX = rightX + idleX;
            model.bipedRightArm.rotateAngleY = 0.0F;
            model.bipedRightArm.rotateAngleZ = idleZ;
        } else {
            model.bipedLeftArm.rotateAngleX = leftX - idleX;
            model.bipedLeftArm.rotateAngleY = 0.0F;
            model.bipedLeftArm.rotateAngleZ = -idleZ;
        }
    }

    public static void applyPlayerRotations(AbstractClientPlayer player, float partialTicks) {
        if (player == null
                || player.isElytraFlying()) {
            return;
        }
        if (FFDRiptide.isActive(player)) {
            GlStateManager.rotate(-90.0F - player.rotationPitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((player.ticksExisted + partialTicks) * -75.0F,
                    0.0F, 1.0F, 0.0F);
            return;
        }
        if (!FFDGameplayHooks.isSwimmingSystemEnabled()) {
            return;
        }
        float animation = getSwimAnimation(player, partialTicks);
        if (animation <= 0.0F) {
            return;
        }
        float target = player.isInWater() ? -90.0F - player.rotationPitch : -90.0F;
        GlStateManager.rotate(lerp(animation, 0.0F, target), 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, -animation, 0.3F * animation);
    }

    public static float smoothCameraEyeHeight(Entity entity, float eyeHeight, float partialTicks) {
        if (!FFDGameplayHooks.isSwimmingSystemEnabled() || !(entity instanceof EntityPlayer)) {
            return eyeHeight;
        }
        EntityPlayer player = (EntityPlayer) entity;
        CameraState state = CAMERAS.computeIfAbsent(player,
                key -> new CameraState(player.ticksExisted, eyeHeight));
        int elapsed = Math.max(0, player.ticksExisted - state.lastTick);
        if (elapsed > 20) {
            state.previous = eyeHeight;
            state.current = eyeHeight;
        } else {
            for (int tick = 0; tick < elapsed; tick++) {
                state.previous = state.current;
                state.current += (eyeHeight - state.current) * 0.5F;
            }
        }
        state.lastTick = player.ticksExisted;
        return lerp(MathHelper.clamp(partialTicks, 0.0F, 1.0F),
                state.previous, state.current);
    }

    public static void beginFirstPersonArm() {
        SUPPRESSED_MODELS.set(Integer.valueOf(SUPPRESSED_MODELS.get().intValue() + 1));
    }

    public static void endFirstPersonArm() {
        SUPPRESSED_MODELS.set(Integer.valueOf(Math.max(0,
                SUPPRESSED_MODELS.get().intValue() - 1)));
    }

    private static float getSwimAnimation(EntityPlayer player, float partialTicks) {
        AnimationState state = ANIMATIONS.computeIfAbsent(player,
                key -> new AnimationState(player.ticksExisted));
        int elapsed = Math.max(0, player.ticksExisted - state.lastTick);
        if (elapsed > 0) {
            boolean swimming = FFDGameplayHooks.isSwimming(player);
            for (int tick = 0; tick < Math.min(elapsed, 20); tick++) {
                state.previous = state.current;
                state.current = swimming ? Math.min(1.0F, state.current + 0.09F)
                        : Math.max(0.0F, state.current - 0.09F);
            }
            if (elapsed > 20) {
                state.previous = swimming ? 1.0F : 0.0F;
                state.current = state.previous;
            }
            state.lastTick = player.ticksExisted;
        }
        return lerp(partialTicks, state.previous, state.current);
    }

    private static EnumHandSide activeSwingingSide(EntityPlayer player) {
        EnumHandSide side = player.getPrimaryHand();
        return player.swingingHand == EnumHand.MAIN_HAND ? side : side.opposite();
    }

    private static void applyArm(ModelRenderer arm, float animation, float x, float y, float z) {
        arm.rotateAngleX = rotLerp(animation, arm.rotateAngleX, x);
        arm.rotateAngleY = rotLerp(animation, arm.rotateAngleY, y);
        arm.rotateAngleZ = rotLerp(animation, arm.rotateAngleZ, z);
    }

    private static float armAngle(float value) {
        return -65.0F * value + value * value;
    }

    private static float rotLerp(float amount, float current, float target) {
        float delta = (target - current) % ((float) Math.PI * 2.0F);
        if (delta < -(float) Math.PI) {
            delta += (float) Math.PI * 2.0F;
        }
        if (delta >= (float) Math.PI) {
            delta -= (float) Math.PI * 2.0F;
        }
        return current + amount * delta;
    }

    private static float lerp(float amount, float start, float end) {
        return start + amount * (end - start);
    }

    private static final class AnimationState {
        private int lastTick;
        private float previous;
        private float current;

        private AnimationState(int lastTick) {
            this.lastTick = lastTick;
        }
    }

    private static final class InputState {
        private int sprintTimer;
        private boolean wasMovingForward;
        private boolean swimSprinting;
    }

    private static final class CameraState {
        private int lastTick;
        private float previous;
        private float current;

        private CameraState(int lastTick, float eyeHeight) {
            this.lastTick = lastTick;
            this.previous = eyeHeight;
            this.current = eyeHeight;
        }
    }
}
