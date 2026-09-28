package xy177.farmersfuturedelight.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelPufferfish extends ModelBase {
    private final List<ModelRenderer> parts = new ArrayList<>();
    private ModelRenderer rightFin;
    private ModelRenderer leftFin;

    public ModelPufferfish(int state) {
        textureWidth = 32;
        textureHeight = 32;
        if (state == 0) {
            small();
        } else if (state == 1) {
            medium();
        } else {
            large();
        }
    }

    private void small() {
        part(0, 27, -1.5F, -2.0F, -1.5F, 3, 2, 3, 0.0F, 23.0F, 0.0F);
        part(24, 6, -1.5F, 0.0F, -1.5F, 1, 1, 1, 0.0F, 20.0F, 0.0F);
        part(28, 6, 0.5F, 0.0F, -1.5F, 1, 1, 1, 0.0F, 20.0F, 0.0F);
        part(-3, 0, -1.5F, 0.0F, 0.0F, 3, 0, 3, 0.0F, 22.0F, 1.5F);
        rightFin = part(25, 0, -1.0F, 0.0F, 0.0F, 1, 0, 2, -1.5F, 22.0F, -1.5F);
        leftFin = part(25, 0, 0.0F, 0.0F, 0.0F, 1, 0, 2, 1.5F, 22.0F, -1.5F);
    }

    private void medium() {
        part(12, 22, -2.5F, -5.0F, -2.5F, 5, 5, 5, 0.0F, 22.0F, 0.0F);
        rightFin = part(24, 0, -2.0F, 0.0F, 0.0F, 2, 0, 2, -2.5F, 18.0F, -1.5F);
        leftFin = part(24, 3, 0.0F, 0.0F, 0.0F, 2, 0, 2, 2.5F, 18.0F, -1.5F);
        rotated(19, 17, -2.5F, -1.0F, 0.0F, 5, 1, 0, 0.0F, 17.0F, -2.5F,
                0.7853982F, 0.0F);
        rotated(11, 17, -2.5F, -1.0F, 0.0F, 5, 1, 0, 0.0F, 17.0F, 2.5F,
                -0.7853982F, 0.0F);
        rotated(5, 17, -1.0F, -5.0F, 0.0F, 1, 5, 0, -2.5F, 22.0F, -2.5F,
                0.0F, -0.7853982F);
        rotated(9, 17, -1.0F, -5.0F, 0.0F, 1, 5, 0, -2.5F, 22.0F, 2.5F,
                0.0F, 0.7853982F);
        rotated(1, 17, 0.0F, -5.0F, 0.0F, 1, 5, 0, 2.5F, 22.0F, 2.5F,
                0.0F, -0.7853982F);
        rotated(1, 17, 0.0F, -5.0F, 0.0F, 1, 5, 0, 2.5F, 22.0F, -2.5F,
                0.0F, 0.7853982F);
        rotated(18, 20, 0.0F, 0.0F, 0.0F, 5, 1, 0, -2.5F, 22.0F, 2.5F,
                0.7853982F, 0.0F);
        rotated(17, 19, -2.5F, 0.0F, 0.0F, 5, 1, 1, 0.0F, 22.0F, -2.5F,
                -0.7853982F, 0.0F);
    }

    private void large() {
        part(0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, 22.0F, 0.0F);
        rightFin = part(24, 0, -2.0F, 0.0F, -1.0F, 2, 1, 2, -4.0F, 15.0F, -2.0F);
        leftFin = part(24, 3, 0.0F, 0.0F, -1.0F, 2, 1, 2, 4.0F, 15.0F, -2.0F);
        rotated(15, 17, -4.0F, -1.0F, 0.0F, 8, 1, 0, 0.0F, 14.0F, -4.0F,
                0.7853982F, 0.0F);
        part(14, 16, -4.0F, -1.0F, 0.0F, 8, 1, 1, 0.0F, 14.0F, 0.0F);
        rotated(23, 18, -4.0F, -1.0F, 0.0F, 8, 1, 0, 0.0F, 14.0F, 4.0F,
                -0.7853982F, 0.0F);
        rotated(5, 17, -1.0F, -8.0F, 0.0F, 1, 8, 0, -4.0F, 22.0F, -4.0F,
                0.0F, -0.7853982F);
        rotated(1, 17, 0.0F, -8.0F, 0.0F, 1, 8, 0, 4.0F, 22.0F, -4.0F,
                0.0F, 0.7853982F);
        rotated(15, 20, -4.0F, 0.0F, 0.0F, 8, 1, 0, 0.0F, 22.0F, -4.0F,
                -0.7853982F, 0.0F);
        part(15, 20, -4.0F, 0.0F, 0.0F, 8, 1, 0, 0.0F, 22.0F, 0.0F);
        rotated(15, 20, -4.0F, 0.0F, 0.0F, 8, 1, 0, 0.0F, 22.0F, 4.0F,
                0.7853982F, 0.0F);
        rotated(9, 17, -1.0F, -8.0F, 0.0F, 1, 8, 0, -4.0F, 22.0F, 4.0F,
                0.0F, 0.7853982F);
        rotated(9, 17, 0.0F, -8.0F, 0.0F, 1, 8, 0, 4.0F, 22.0F, 4.0F,
                0.0F, -0.7853982F);
    }

    private ModelRenderer part(int u, int v, float x, float y, float z, int width, int height,
                               int depth, float pivotX, float pivotY, float pivotZ) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.addBox(x, y, z, width, height, depth);
        part.setRotationPoint(pivotX, pivotY, pivotZ);
        parts.add(part);
        return part;
    }

    private ModelRenderer rotated(int u, int v, float x, float y, float z, int width, int height,
                                  int depth, float pivotX, float pivotY, float pivotZ,
                                  float rotateX, float rotateY) {
        ModelRenderer part = part(u, v, x, y, z, width, height, depth, pivotX, pivotY, pivotZ);
        part.rotateAngleX = rotateX;
        part.rotateAngleY = rotateY;
        return part;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale,
                entity);
        for (ModelRenderer part : parts) {
            part.render(scale);
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entity) {
        rightFin.rotateAngleZ = -0.2F + 0.4F * MathHelper.sin(ageInTicks * 0.2F);
        leftFin.rotateAngleZ = 0.2F - 0.4F * MathHelper.sin(ageInTicks * 0.2F);
    }
}
