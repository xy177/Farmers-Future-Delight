package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public final class ModelStrider extends ModelBase {
    private final boolean baby;
    private final ModelRenderer body;
    private final ModelRenderer rightLeg;
    private final ModelRenderer leftLeg;
    private final ModelRenderer[] bristles;

    public ModelStrider(boolean baby, float inflate) {
        this.baby = baby;
        textureWidth = baby ? 32 : 64;
        textureHeight = baby ? 32 : 128;
        if (baby) {
            body = part(0, 0, 0, 16.75F, 0);
            body.addBox(-3.5F, -3.75F, -4.0F, 7, 7, 8, inflate);
            rightLeg = part(0, 24, -1.5F, 20, 0);
            rightLeg.addBox(-1, 0, -1, 2, 4, 2, inflate);
            leftLeg = part(8, 24, 1.5F, 20, 0);
            leftLeg.addBox(-1, 0, -1, 2, 4, 2, inflate);
            bristles = new ModelRenderer[3];
            for (int index = 0; index < bristles.length; index++) {
                bristles[index] = part(0, 15 + index * 3, 0, -4.25F, -2 + index * 2);
                bristles[index].addBox(-3.5F, -2.5F, 0, 7, 3, 0);
                body.addChild(bristles[index]);
            }
        } else {
            body = part(0, 0, 0, 1, 0);
            body.addBox(-8, -6, -8, 16, 14, 16, inflate);
            rightLeg = part(0, 32, -4, 8, 0);
            rightLeg.addBox(-2, 0, -2, 4, 16, 4, inflate);
            leftLeg = part(0, 55, 4, 8, 0);
            leftLeg.addBox(-2, 0, -2, 4, 16, 4, inflate);
            bristles = new ModelRenderer[6];
            int[] offsets = {33, 49, 65};
            for (int side = 0; side < 2; side++) for (int index = 0; index < 3; index++) {
                ModelRenderer bristle = part(16, offsets[index], side == 0 ? -8 : 8,
                        (side == 0 ? -5 : -6) + index * 4.5F, -8);
                bristle.mirror = side == 0;
                bristle.addBox(side == 0 ? -12 : 0, 0, 0, 12, 0, 16);
                body.addChild(bristle);
                bristles[side * 3 + index] = bristle;
            }
            bristles[1].rotationPointY = -1; bristles[2].rotationPointY = 4;
            bristles[4].rotationPointY = -2; bristles[5].rotationPointY = 3;
        }
    }

    private ModelRenderer part(int textureX, int textureY, float posX, float posY, float posZ) {
        ModelRenderer part = new ModelRenderer(this, textureX, textureY);
        part.setRotationPoint(posX, posY, posZ);
        return part;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        body.render(scale); rightLeg.render(scale); leftLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        float speed = Math.min(limbSwingAmount, 0.25F);
        body.rotateAngleX = entity.isBeingRidden() ? 0 : headPitch * 0.017453292F;
        body.rotateAngleY = entity.isBeingRidden() ? 0 : netHeadYaw * 0.017453292F;
        body.rotateAngleZ = 0.1F * MathHelper.sin(limbSwing * 1.5F) * 4.0F * speed;
        leftLeg.rotateAngleX = MathHelper.sin(limbSwing * 0.75F) * 2.0F * speed;
        rightLeg.rotateAngleX = MathHelper.sin(limbSwing * 0.75F + (float) Math.PI) * 2.0F * speed;
        leftLeg.rotateAngleZ = 0.17453292F * MathHelper.cos(limbSwing * 0.75F) * speed;
        rightLeg.rotateAngleZ = 0.17453292F * MathHelper.cos(limbSwing * 0.75F + (float) Math.PI) * speed;
        body.rotationPointY = (baby ? 17.25F : 2.0F) - (baby ? 1 : 2) * MathHelper.cos(limbSwing * 1.5F) * 2 * speed;
        leftLeg.rotationPointY = (baby ? 20 : 8) + 2 * MathHelper.sin(limbSwing * 0.75F + (float) Math.PI) * 2 * speed;
        rightLeg.rotationPointY = (baby ? 20 : 8) + 2 * MathHelper.sin(limbSwing * 0.75F) * 2 * speed;
        float flow = MathHelper.cos(limbSwing * 1.5F + (float) Math.PI) * speed;
        float[] positions = {0.87266463F, 1.134464F, 1.2217305F};
        float[] amplitudes = {0.6F, 1.2F, 1.3F};
        float[] rates = {0.4F, 0.2F, -0.4F};
        for (int index = 0; index < bristles.length; index++) {
            int partIndex = index % 3;
            float motion = flow * amplitudes[partIndex] + (partIndex == 2 ? 0.05F : 0.1F)
                    * MathHelper.sin(ageInTicks * rates[partIndex]);
            if (baby) bristles[index].rotateAngleX = motion;
            else bristles[index].rotateAngleZ = (index < 3 ? -1 : 1) * positions[partIndex] + motion;
        }
    }
}
