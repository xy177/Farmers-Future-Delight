package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelSalmon extends ModelBase {
    private final ModelRenderer frontBody;
    private final ModelRenderer rearBody;
    private final ModelRenderer head;
    private final ModelRenderer rightFin;
    private final ModelRenderer leftFin;

    public ModelSalmon() {
        textureWidth = 32;
        textureHeight = 32;
        frontBody = part(0, 0, -1.5F, -2.5F, 0.0F, 3, 5, 8,
                0.0F, 20.0F, -7.2F);
        rearBody = part(0, 13, -1.5F, -2.5F, 0.0F, 3, 5, 8,
                0.0F, 20.0F, 0.8F);
        head = part(22, 0, -1.0F, -2.0F, -3.0F, 2, 4, 3,
                0.0F, 20.0F, -7.2F);
        ModelRenderer tail = part(20, 10, 0.0F, -2.5F, 0.0F, 0, 5, 6,
                0.0F, 0.0F, 8.0F);
        rearBody.addChild(tail);
        ModelRenderer frontTopFin = part(2, 1, 0.0F, 0.0F, 0.0F, 0, 2, 3,
                0.0F, -4.5F, 5.0F);
        frontBody.addChild(frontTopFin);
        ModelRenderer rearTopFin = part(0, 2, 0.0F, 0.0F, 0.0F, 0, 2, 4,
                0.0F, -4.5F, -1.0F);
        rearBody.addChild(rearTopFin);
        rightFin = part(-4, 0, -2.0F, 0.0F, 0.0F, 2, 0, 2,
                -1.5F, 21.5F, -7.2F);
        rightFin.rotateAngleZ = -0.7853982F;
        leftFin = part(0, 0, 0.0F, 0.0F, 0.0F, 2, 0, 2,
                1.5F, 21.5F, -7.2F);
        leftFin.rotateAngleZ = 0.7853982F;
    }

    private ModelRenderer part(int u, int v, float x, float y, float z, int width, int height,
                               int depth, float pivotX, float pivotY, float pivotZ) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.addBox(x, y, z, width, height, depth);
        part.setRotationPoint(pivotX, pivotY, pivotZ);
        return part;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale,
                entity);
        frontBody.render(scale);
        rearBody.render(scale);
        head.render(scale);
        rightFin.render(scale);
        leftFin.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entity) {
        float amplitude = entity.isInWater() ? 1.0F : 1.3F;
        float speed = entity.isInWater() ? 1.0F : 1.7F;
        rearBody.rotateAngleY = -amplitude * 0.25F
                * MathHelper.sin(speed * 0.6F * ageInTicks);
    }
}
