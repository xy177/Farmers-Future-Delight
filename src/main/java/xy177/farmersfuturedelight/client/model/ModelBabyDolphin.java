package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelBabyDolphin extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer tail;
    private final ModelRenderer tailFin;

    public ModelBabyDolphin() {
        textureWidth = 64;
        textureHeight = 64;

        body = new ModelRenderer(this, 20, 0);
        body.addBox(-3.0F, -2.5F, -4.0F, 6, 5, 8);
        body.setRotationPoint(0.0F, 21.5F, 0.0F);

        ModelRenderer head = new ModelRenderer(this, 0, 0);
        head.addBox(-3.0F, -3.5F, -4.0F, 6, 5, 4);
        head.setRotationPoint(0.0F, 1.0F, -4.0F);
        body.addChild(head);

        ModelRenderer nose = new ModelRenderer(this, 0, 9);
        nose.addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2);
        nose.setRotationPoint(0.0F, 0.5F, -4.0F);
        head.addChild(nose);

        ModelRenderer leftFin = new ModelRenderer(this, 34, 18);
        leftFin.addBox(-0.5F, -1.5F, -0.5F, 1, 3, 6);
        leftFin.setRotationPoint(1.8F, 0.85F, -2.6F);
        leftFin.rotateAngleX = 0.8727F;
        leftFin.rotateAngleZ = 1.7017F;
        body.addChild(leftFin);

        ModelRenderer rightFin = new ModelRenderer(this, 48, 18);
        rightFin.mirror = true;
        rightFin.addBox(-0.5F, -1.5F, -0.5F, 1, 3, 6);
        rightFin.setRotationPoint(-1.8F, 0.85F, -2.6F);
        rightFin.rotateAngleX = 0.8727F;
        rightFin.rotateAngleZ = -1.7017F;
        body.addChild(rightFin);

        tail = new ModelRenderer(this, 0, 13);
        tail.addBox(-2.0F, -1.5F, 0.0F, 4, 3, 7);
        tail.setRotationPoint(0.0F, 1.0F, 4.0F);
        body.addChild(tail);

        tailFin = new ModelRenderer(this, 22, 13);
        tailFin.addBox(-4.0F, -0.5F, -1.0F, 8, 1, 4);
        tailFin.setRotationPoint(0.0F, 0.0F, 6.0F);
        tail.addChild(tailFin);

        ModelRenderer dorsalFin = new ModelRenderer(this, 42, 0);
        dorsalFin.addBox(-0.5F, -1.0F, 1.0F, 1, 3, 4);
        dorsalFin.setRotationPoint(0.0F, -1.0F, -2.7F);
        dorsalFin.rotateAngleX = 0.8727F;
        body.addChild(dorsalFin);
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
        tail.rotateAngleX = 0.0F;
        tailFin.rotateAngleX = 0.0F;
        if (entity.motionX * entity.motionX + entity.motionZ * entity.motionZ > 1.0E-7D) {
            float wave = MathHelper.cos(ageInTicks * 0.3F);
            body.rotateAngleX += -0.05F - 0.05F * wave;
            tail.rotateAngleX = -0.1F * wave;
            tailFin.rotateAngleX = -0.2F * wave;
        }
    }
}
