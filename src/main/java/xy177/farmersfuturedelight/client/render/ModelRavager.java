package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityRavager;

public class ModelRavager extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer neck;
    private final ModelRenderer head;
    private final ModelRenderer mouth;
    private final ModelRenderer rightHindLeg;
    private final ModelRenderer leftHindLeg;
    private final ModelRenderer rightFrontLeg;
    private final ModelRenderer leftFrontLeg;
    private float partialTicks;

    public ModelRavager() {
        textureWidth = 128;
        textureHeight = 128;

        body = new ModelRenderer(this, 0, 55);
        body.addBox(-7.0F, -10.0F, -7.0F, 14, 16, 20);
        body.addBox(-6.0F, 6.0F, -7.0F, 12, 13, 18);
        body.setRotationPoint(0.0F, 1.0F, 2.0F);
        body.rotateAngleX = (float) Math.PI / 2.0F;

        neck = new ModelRenderer(this, 68, 73);
        neck.addBox(-5.0F, -1.0F, -18.0F, 10, 10, 18);
        neck.setRotationPoint(0.0F, -7.0F, 5.5F);

        head = new ModelRenderer(this, 0, 0);
        head.addBox(-8.0F, -20.0F, -14.0F, 16, 20, 16);
        head.addBox(-2.0F, -6.0F, -18.0F, 4, 8, 4);
        head.setRotationPoint(0.0F, 16.0F, -17.0F);
        neck.addChild(head);

        ModelRenderer rightHorn = new ModelRenderer(this, 74, 55);
        rightHorn.addBox(0.0F, -14.0F, -2.0F, 2, 14, 4);
        rightHorn.setRotationPoint(-10.0F, -14.0F, -8.0F);
        rightHorn.rotateAngleX = 1.0995574F;
        head.addChild(rightHorn);

        ModelRenderer leftHorn = new ModelRenderer(this, 74, 55);
        leftHorn.mirror = true;
        leftHorn.addBox(0.0F, -14.0F, -2.0F, 2, 14, 4);
        leftHorn.setRotationPoint(8.0F, -14.0F, -8.0F);
        leftHorn.rotateAngleX = 1.0995574F;
        head.addChild(leftHorn);

        mouth = new ModelRenderer(this, 0, 36);
        mouth.addBox(-8.0F, 0.0F, -16.0F, 16, 3, 16);
        mouth.setRotationPoint(0.0F, -2.0F, 2.0F);
        head.addChild(mouth);

        rightHindLeg = createLeg(96, 0, false, -8.0F, -13.0F, 18.0F);
        leftHindLeg = createLeg(96, 0, true, 8.0F, -13.0F, 18.0F);
        rightFrontLeg = createLeg(64, 0, false, -8.0F, -13.0F, -5.0F);
        leftFrontLeg = createLeg(64, 0, true, 8.0F, -13.0F, -5.0F);
    }

    private ModelRenderer createLeg(int textureX, int textureY, boolean mirror,
                                    float x, float y, float z) {
        ModelRenderer leg = new ModelRenderer(this, textureX, textureY);
        leg.mirror = mirror;
        leg.addBox(-4.0F, 0.0F, -4.0F, 8, 37, 8);
        leg.setRotationPoint(x, y, z);
        return leg;
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing,
                                    float limbSwingAmount, float partialTickTime) {
        partialTicks = partialTickTime;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount,
                       float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch, scale, entity);
        body.render(scale);
        neck.render(scale);
        rightHindLeg.render(scale);
        leftHindLeg.render(scale);
        rightFrontLeg.render(scale);
        leftFrontLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale,
                                  Entity entity) {
        EntityRavager ravager = (EntityRavager) entity;
        float attackTick = Math.max(0.0F, ravager.getAttackTick() - partialTicks);
        float stunTick = Math.max(0.0F, ravager.getStunTick() - partialTicks);
        float roarTick = Math.max(0.0F, ravager.getRoarTick() - partialTicks);

        if (attackTick > 0.0F) {
            float wave = MathHelper.sin((1.0F - attackTick / 10.0F) * (float) Math.PI);
            float headPosition = (float) Math.pow((1.0F + wave) * 0.5F, 3.0D) * 12.0F;
            neck.rotationPointZ = -6.5F + headPosition;
            neck.rotationPointY = -7.0F - headPosition * MathHelper.sin(neck.rotateAngleX);
            mouth.rotateAngleX = attackTick > 5.0F
                    ? MathHelper.sin((-4.0F + attackTick) / 4.0F) * (float) Math.PI * 0.4F
                    : 0.15707964F * MathHelper.sin((float) Math.PI * attackTick / 10.0F);
        } else {
            neck.rotationPointX = 0.0F;
            neck.rotationPointY = -7.0F;
            neck.rotationPointZ = 5.5F;
            boolean stunned = stunTick > 0.0F;
            neck.rotateAngleX = stunned ? 0.21991149F : 0.0F;
            mouth.rotateAngleX = (float) Math.PI * (stunned ? 0.05F : 0.01F);
            if (stunned) {
                neck.rotationPointX = MathHelper.sin(stunTick / 40.0F * 10.0F) * 3.0F;
            } else if (roarTick > 0.0F) {
                float roarProgress = (20.0F - roarTick) / 20.0F;
                mouth.rotateAngleX = (float) (Math.PI / 2.0D)
                        * MathHelper.sin(roarProgress * (float) Math.PI * 0.25F);
            }
        }

        head.rotateAngleX = headPitch * ((float) Math.PI / 180.0F);
        head.rotateAngleY = netHeadYaw * ((float) Math.PI / 180.0F);
        float legRotation = 0.4F * limbSwingAmount;
        rightHindLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * legRotation;
        leftHindLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * legRotation;
        rightFrontLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI)
                * legRotation;
        leftFrontLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * legRotation;
    }
}
