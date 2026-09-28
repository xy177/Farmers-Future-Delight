package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelTropicalFishB extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer tail;
    private final ModelRenderer rightFin;
    private final ModelRenderer leftFin;
    private final ModelRenderer topFin;
    private final ModelRenderer bottomFin;

    public ModelTropicalFishB() {
        this(0.0F);
    }

    public ModelTropicalFishB(float scale) {
        textureWidth = 32;
        textureHeight = 32;
        body = part(0, 20, -1.0F, -3.0F, -3.0F, 2, 6, 6, scale,
                0.0F, 19.0F, 0.0F);
        tail = part(21, 16, 0.0F, -3.0F, 0.0F, 0, 6, 5, scale,
                0.0F, 19.0F, 3.0F);
        rightFin = part(2, 16, -2.0F, 0.0F, 0.0F, 2, 2, 0, scale,
                -1.0F, 20.0F, 0.0F);
        rightFin.rotateAngleY = 0.7853982F;
        leftFin = part(2, 12, 0.0F, 0.0F, 0.0F, 2, 2, 0, scale,
                1.0F, 20.0F, 0.0F);
        leftFin.rotateAngleY = -0.7853982F;
        topFin = part(20, 11, 0.0F, -4.0F, 0.0F, 0, 4, 6, scale,
                0.0F, 16.0F, -3.0F);
        bottomFin = part(20, 21, 0.0F, 0.0F, 0.0F, 0, 4, 6, scale,
                0.0F, 22.0F, -3.0F);
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
        bottomFin.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entity) {
        float speed = entity.isInWater() ? 1.0F : 1.5F;
        tail.rotateAngleY = -speed * 0.45F * MathHelper.sin(0.6F * ageInTicks);
    }
}
