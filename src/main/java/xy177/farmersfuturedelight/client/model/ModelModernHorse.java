package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.util.math.MathHelper;

public final class ModelModernHorse extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer neck;
    private final ModelRenderer leftHind;
    private final ModelRenderer rightHind;
    private final ModelRenderer leftFront;
    private final ModelRenderer rightFront;
    private final ModelRenderer tail;
    private ModelRenderer leftRein;
    private ModelRenderer rightRein;
    private float partialTicks;

    public ModelModernHorse(float inflation, boolean saddle) {
        textureWidth = textureHeight = 64;
        body = box(0, 32, -5, -8, -17, 10, 10, 22, 0.05F, false, 0, 11, 5);
        neck = box(0, 35, -2.05F, -6, -2, 4, 12, 7, 0, false, 0, 4, -12);
        ModelRenderer head = box(0, 13, -3, -11, -2, 6, 5, 7, inflation, false, 0, 0, 0);
        neck.addChild(head);
        neck.addChild(box(56, 36, -1, -11, 5.01F, 2, 16, 2, inflation, false, 0, 0, 0));
        neck.addChild(box(0, 25, -2, -11, -7, 4, 5, 5, inflation, false, 0, 0, 0));
        head.addChild(box(19, 16, 0.55F, -13, 4, 2, 3, 1, -0.001F, false, 0, 0, 0));
        head.addChild(box(19, 16, -2.55F, -13, 4, 2, 3, 1, -0.001F, false, 0, 0, 0));
        leftHind = box(48, 21, -3, -1.01F, -1, 4, 11, 4, inflation, true, 4, 14, 7);
        rightHind = box(48, 21, -1, -1.01F, -1, 4, 11, 4, inflation, false, -4, 14, 7);
        leftFront = box(48, 21, -3, -1.01F, -1.9F, 4, 11, 4, inflation, true, 4, 14, -10);
        rightFront = box(48, 21, -1, -1.01F, -1.9F, 4, 11, 4, inflation, false, -4, 14, -10);
        tail = box(42, 36, -1.5F, 0, 0, 3, 14, 4, inflation, false, 0, -5, 2);
        body.addChild(tail);
        if (saddle) {
            body.addChild(box(26, 0, -5, -8, -9, 10, 9, 9, 0.5F, false, 0, 0, 0));
            neck.addChild(box(29, 5, 2, -9, -6, 1, 2, 2, 0, false, 0, 0, 0));
            neck.addChild(box(29, 5, -3, -9, -6, 1, 2, 2, 0, false, 0, 0, 0));
            leftRein = box(32, 2, 3.1F, -6, -8, 0, 3, 16, 0, false, 0, 0, 0);
            rightRein = box(32, 2, -3.1F, -6, -8, 0, 3, 16, 0, false, 0, 0, 0);
            leftRein.rotateAngleX = rightRein.rotateAngleX = -0.5235988F;
            neck.addChild(leftRein);
            neck.addChild(rightRein);
            neck.addChild(box(1, 1, -3, -11, -1.9F, 6, 5, 6, 0.22F, false, 0, 0, 0));
            neck.addChild(box(19, 0, -2, -11, -4, 4, 5, 2, 0.2F, false, 0, 0, 0));
        }
    }

    private ModelRenderer box(int textureX, int textureY, float x, float y, float z,
                              int width, int height, int depth, float inflation, boolean mirror,
                              float pivotX, float pivotY, float pivotZ) {
        ModelRenderer part = new ModelRenderer(this, textureX, textureY);
        part.mirror = mirror;
        part.addBox(x, y, z, width, height, depth, inflation);
        part.setRotationPoint(pivotX, pivotY, pivotZ);
        return part;
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float swing, float amount, float partial) {
        partialTicks = partial;
    }

    @Override
    public void setRotationAngles(float swing, float amount, float age, float yaw, float pitch, float scale, Entity entity) {
        AbstractHorse horse = (AbstractHorse) entity;
        float eating = horse.getGrassEatingAmount(partialTicks);
        float standing = horse.getRearingAmount(partialTicks);
        float feeding = horse.getMouthOpennessAngle(partialTicks);
        float inverse = 1.0F - standing;
        float headPitch = pitch * 0.017453292F;
        if (amount > 0.2F) headPitch += MathHelper.cos(swing * 0.8F) * 0.15F * amount;
        float headYaw = MathHelper.clamp(yaw, -20.0F, 20.0F) * 0.017453292F;
        neck.rotateAngleX = standing * (0.2617994F + headPitch)
                + eating * (2.1816616F + MathHelper.sin(age) * 0.05F)
                + (1.0F - Math.max(standing, eating)) * (0.5235988F + headPitch + feeding * MathHelper.sin(age) * 0.05F);
        neck.rotateAngleY = (standing + 1.0F - Math.max(standing, eating)) * headYaw;
        neck.rotationPointY = 4.0F - 8.0F * standing * (1.0F - eating) + 7.0F * eating;
        neck.rotationPointZ = -12.0F + 8.0F * standing;
        body.rotateAngleX = -0.7853982F * standing;
        leftFront.rotationPointY = rightFront.rotationPointY = 14.0F - 12.0F * standing;
        leftFront.rotationPointZ = rightFront.rotationPointZ = -10.0F + 4.0F * standing;
        float leg = MathHelper.cos((horse.isInWater() ? 0.2F : 1.0F) * swing * 0.6662F + (float) Math.PI);
        float bob = MathHelper.cos(age * 0.6F + (float) Math.PI);
        leftHind.rotateAngleX = 0.2617994F * standing - leg * 0.5F * amount * inverse;
        rightHind.rotateAngleX = 0.2617994F * standing + leg * 0.5F * amount * inverse;
        leftFront.rotateAngleX = (-1.0471976F + bob) * standing + leg * 0.8F * amount * inverse;
        rightFront.rotateAngleX = (-1.0471976F - bob) * standing - leg * 0.8F * amount * inverse;
        tail.rotateAngleX = 0.5235988F + amount * 0.75F;
        tail.rotationPointY = -5.0F + amount;
        tail.rotationPointZ = 2.0F + amount * 2.0F;
        tail.rotateAngleY = horse.tailCounter > 0 ? MathHelper.cos(age * 0.7F) : 0.0F;
        if (leftRein != null) {
            leftRein.showModel = rightRein.showModel = horse.isBeingRidden();
        }
    }

    @Override
    public void render(Entity entity, float swing, float amount, float age, float yaw, float pitch, float scale) {
        setRotationAngles(swing, amount, age, yaw, pitch, scale, entity);
        body.render(scale);
        neck.render(scale);
        leftHind.render(scale);
        rightHind.render(scale);
        leftFront.render(scale);
        rightFront.render(scale);
    }
}
