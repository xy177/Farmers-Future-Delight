package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelLlama;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBabyTraderLlama extends ModelLlama {
    public ModelBabyTraderLlama(float inflation) {
        super(inflation);
        textureWidth = 64;
        textureHeight = 64;
        head = new ModelRenderer(this, 0, 0);
        head.addBox(-3.0F, -9.0F, -4.0F, 6, 11, 4, inflation);
        head.setTextureOffset(0, 15).addBox(-1.5F, -7.0F, -7.0F, 3, 3, 3, inflation);
        head.setTextureOffset(20, 4).addBox(0.5F, -11.0F, -3.0F, 2, 2, 2, inflation);
        head.setTextureOffset(20, 0).addBox(-2.5F, -11.0F, -3.0F, 2, 2, 2, inflation);
        head.setRotationPoint(0.0F, 12.0F, -4.0F);
        body = new ModelRenderer(this, 0, 15);
        body.addBox(-4.0F, -3.0F, -8.5F, 8, 6, 13, inflation);
        body.setRotationPoint(0.0F, 14.0F, 2.5F);
        leg1 = leg(0, 45, -1.4F, -2.5F, 4.5F, inflation);
        leg2 = leg(12, 45, -1.6F, 2.5F, 4.5F, inflation);
        leg3 = leg(0, 34, -1.4F, -2.5F, -3.5F, inflation);
        leg4 = leg(12, 34, -1.6F, 2.5F, -3.5F, inflation);
    }

    private ModelRenderer leg(int textureX, int textureY, float boxX, float pivotX,
                              float pivotZ, float inflation) {
        ModelRenderer leg = new ModelRenderer(this, textureX, textureY);
        leg.addBox(boxX, -0.5F, -1.5F, 3, 8, 3, inflation);
        leg.setRotationPoint(pivotX, 16.5F, pivotZ);
        return leg;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount,
                       float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        head.render(scale);
        body.render(scale);
        leg1.render(scale);
        leg2.render(scale);
        leg3.render(scale);
        leg4.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        head.rotateAngleY = netHeadYaw * (float) Math.PI / 180.0F;
        head.rotateAngleX = headPitch * (float) Math.PI / 180.0F;
        leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        leg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        leg3.rotateAngleX = leg2.rotateAngleX;
        leg4.rotateAngleX = leg1.rotateAngleX;
    }
}
