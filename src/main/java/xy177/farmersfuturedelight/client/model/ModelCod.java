package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelCod extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer headFront;
    private final ModelRenderer rightFin;
    private final ModelRenderer leftFin;
    private final ModelRenderer tail;
    private final ModelRenderer topFin;

    public ModelCod() {
        textureWidth = 32;
        textureHeight = 32;
        body = part(0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 7, 0.0F, 22.0F, 0.0F);
        head = part(11, 0, -1.0F, -2.0F, -3.0F, 2, 4, 3, 0.0F, 22.0F, 0.0F);
        headFront = part(0, 0, -1.0F, -2.0F, -1.0F, 2, 3, 1, 0.0F, 22.0F, -3.0F);
        rightFin = part(22, 1, -2.0F, 0.0F, -1.0F, 2, 0, 2, -1.0F, 23.0F, 0.0F);
        rightFin.rotateAngleZ = -0.7853982F;
        leftFin = part(22, 4, 0.0F, 0.0F, -1.0F, 2, 0, 2, 1.0F, 23.0F, 0.0F);
        leftFin.rotateAngleZ = 0.7853982F;
        tail = part(22, 3, 0.0F, -2.0F, 0.0F, 0, 4, 4, 0.0F, 22.0F, 7.0F);
        topFin = part(20, -6, 0.0F, -1.0F, -1.0F, 0, 1, 6, 0.0F, 20.0F, 0.0F);
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
        body.render(scale);
        head.render(scale);
        headFront.render(scale);
        rightFin.render(scale);
        leftFin.render(scale);
        tail.render(scale);
        topFin.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entity) {
        float speed = entity.isInWater() ? 1.0F : 1.5F;
        tail.rotateAngleY = -speed * 0.45F * MathHelper.sin(0.6F * ageInTicks);
    }
}
