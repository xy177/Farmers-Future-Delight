package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelGlowSquidBaby extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer[] tentacles = new ModelRenderer[8];

    public ModelGlowSquidBaby() {
        textureWidth = 32;
        textureHeight = 32;
        body = new ModelRenderer(this, 0, 0);
        body.addBox(-4.0F, -5.0F, -4.0F, 8, 10, 8);
        body.setRotationPoint(0.0F, 13.0F, 0.0F);

        for (int i = 0; i < tentacles.length; i++) {
            double angle = i * Math.PI * 2.0D / tentacles.length;
            tentacles[i] = new ModelRenderer(this, 0, 18);
            tentacles[i].addBox(-1.0F, -0.5F, -1.0F, 2, 6, 2);
            tentacles[i].setRotationPoint((float) Math.cos(angle) * 3.0F, 18.5F,
                    (float) Math.sin(angle) * 3.0F);
            tentacles[i].rotateAngleY = (float) (i * Math.PI * -2.0D
                    / tentacles.length + Math.PI / 2.0D);
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        body.render(scale);
        for (ModelRenderer tentacle : tentacles) {
            tentacle.render(scale);
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entity) {
        for (ModelRenderer tentacle : tentacles) {
            tentacle.rotateAngleX = ageInTicks;
        }
    }
}
