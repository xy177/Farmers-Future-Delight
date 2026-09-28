package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelTropicalFishA extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer tail;
    private final ModelRenderer rightFin;
    private final ModelRenderer leftFin;
    private final ModelRenderer topFin;

    public ModelTropicalFishA() {
        this(0.0F);
    }

    public ModelTropicalFishA(float scale) {
        textureWidth = 32;
        textureHeight = 32;
        body = part(0, 0, -1.0F, -1.5F, -3.0F, 2, 3, 6, scale,
                0.0F, 22.0F, 0.0F);
        tail = part(22, -6, 0.0F, -1.5F, 0.0F, 0, 3, 6, scale,
                0.0F, 22.0F, 3.0F);
        rightFin = part(2, 16, -2.0F, -1.0F, 0.0F, 2, 2, 0, scale,
                -1.0F, 22.5F, 0.0F);
        rightFin.rotateAngleY = 0.7853982F;
        leftFin = part(2, 12, 0.0F, -1.0F, 0.0F, 2, 2, 0, scale,
                1.0F, 22.5F, 0.0F);
        leftFin.rotateAngleY = -0.7853982F;
        topFin = part(10, -5, 0.0F, -3.0F, 0.0F, 0, 3, 6, scale,
                0.0F, 20.5F, -3.0F);
    }

    private ModelRenderer part(int u, int v, float x, float y, float z, int width, int height,
                               int depth, float scale, float pivotX, float pivotY, float pivotZ) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.addBox(x, y, z, width, height, depth, scale);
        part.setRotationPoint(pivotX, pivotY, pivotZ);
        return part;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale,
                entity);
        body.render(scale);
        tail.render(scale);
        rightFin.render(scale);
        leftFin.render(scale);
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
