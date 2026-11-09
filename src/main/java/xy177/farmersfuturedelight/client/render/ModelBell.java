package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

public class ModelBell extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer base;

    public ModelBell() {
        textureWidth = 32;
        textureHeight = 32;
        body = new ModelRenderer(this, 0, 0);
        body.addBox(-3.0F, -6.0F, -3.0F, 6, 7, 6);
        body.setRotationPoint(8.0F, 12.0F, 8.0F);
        base = new ModelRenderer(this, 0, 13);
        base.addBox(4.0F, 4.0F, 4.0F, 8, 2, 8);
        base.setRotationPoint(-8.0F, -12.0F, -8.0F);
        body.addChild(base);
    }

    public void render(float ticks, EnumFacing direction, float scale) {
        body.rotateAngleX = 0.0F;
        body.rotateAngleZ = 0.0F;
        if (direction != null) {
            float angle = MathHelper.sin(ticks / (float) Math.PI)
                    / (4.0F + ticks / 3.0F);
            if (direction == EnumFacing.NORTH) body.rotateAngleX = -angle;
            if (direction == EnumFacing.SOUTH) body.rotateAngleX = angle;
            if (direction == EnumFacing.EAST) body.rotateAngleZ = -angle;
            if (direction == EnumFacing.WEST) body.rotateAngleZ = angle;
        }
        body.render(scale);
    }
}
