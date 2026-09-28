package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityDolphin;

public class ModelDolphin extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer tail;
    private final ModelRenderer tailFin;

    public ModelDolphin() {
        textureWidth = 64;
        textureHeight = 64;
        body = new ModelRenderer(this, 22, 0);
        body.addBox(-4.0F, -7.0F, 0.0F, 8, 7, 13);
        body.setRotationPoint(0.0F, 22.0F, -5.0F);

        ModelRenderer dorsalFin = new ModelRenderer(this, 51, 0);
        dorsalFin.addBox(-0.5F, 0.0F, 8.0F, 1, 4, 5);
        dorsalFin.rotateAngleX = 1.0471976F;
        body.addChild(dorsalFin);

        ModelRenderer rightFin = new ModelRenderer(this, 48, 20);
        rightFin.mirror = true;
        rightFin.addBox(-0.5F, -4.0F, 0.0F, 1, 4, 7);
        rightFin.setRotationPoint(2.0F, -2.0F, 4.0F);
        rightFin.rotateAngleX = 1.0471976F;
        rightFin.rotateAngleZ = 2.0943952F;
        body.addChild(rightFin);

        ModelRenderer leftFin = new ModelRenderer(this, 48, 20);
        leftFin.addBox(-0.5F, -4.0F, 0.0F, 1, 4, 7);
        leftFin.setRotationPoint(-2.0F, -2.0F, 4.0F);
        leftFin.rotateAngleX = 1.0471976F;
        leftFin.rotateAngleZ = -2.0943952F;
        body.addChild(leftFin);

        tail = new ModelRenderer(this, 0, 19);
        tail.addBox(-2.0F, -2.5F, 0.0F, 4, 5, 11);
        tail.setRotationPoint(0.0F, -2.5F, 11.0F);
        tail.rotateAngleX = -0.10471976F;
        body.addChild(tail);

        tailFin = new ModelRenderer(this, 19, 20);
        tailFin.addBox(-5.0F, -0.5F, 0.0F, 10, 1, 6);
        tailFin.setRotationPoint(0.0F, 0.0F, 9.0F);
        tail.addChild(tailFin);

        ModelRenderer head = new ModelRenderer(this, 0, 0);
        head.addBox(-4.0F, -3.0F, -3.0F, 8, 7, 6);
        head.setRotationPoint(0.0F, -4.0F, -3.0F);
        ModelRenderer nose = new ModelRenderer(this, 0, 13);
        nose.addBox(-1.0F, 2.0F, -7.0F, 2, 2, 4);
        head.addChild(nose);
        body.addChild(head);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                scale, entity);
        body.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entity) {
        body.rotateAngleX = headPitch * 0.017453292F;
        body.rotateAngleY = netHeadYaw * 0.017453292F;
        tail.rotateAngleX = -0.10471976F;
        tailFin.rotateAngleX = 0.0F;
        if (entity instanceof EntityDolphin
                && entity.motionX * entity.motionX + entity.motionZ * entity.motionZ > 1.0E-7D) {
            float wave = MathHelper.cos(ageInTicks * 0.3F);
            body.rotateAngleX += -0.05F - 0.05F * wave;
            tail.rotateAngleX = -0.1F * wave;
            tailFin.rotateAngleX = -0.2F * wave;
        }
    }
}
