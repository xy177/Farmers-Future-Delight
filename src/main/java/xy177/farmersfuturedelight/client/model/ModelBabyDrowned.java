package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBabyDrowned extends ModelDrowned {
    public ModelBabyDrowned(float modelSize) {
        super(modelSize, 0.0F, 64, 64);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-2.0F, -2.5F, -1.0F, 4, 5, 2, modelSize);
        bipedBody.setRotationPoint(0.0F, 17.5F, 0.0F);

        bipedHead = new ModelRenderer(this, 3, 3);
        bipedHead.addBox(-3.0F, -6.25F, -3.0F, 6, 6, 6);
        bipedHead.setTextureOffset(35, 3);
        bipedHead.addBox(-3.0F, -6.15F, -3.0F, 6, 6, 6, 0.25F);
        bipedHead.setRotationPoint(0.0F, 15.25F, 0.0F);

        bipedHeadwear = new ModelRenderer(this, 0, 0);
        bipedHeadwear.setRotationPoint(0.0F, 15.25F, 0.0F);

        bipedRightArm = new ModelRenderer(this, 36, 16);
        bipedRightArm.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2, modelSize);
        bipedRightArm.setRotationPoint(-3.0F, 15.5F, 0.0F);

        bipedLeftArm = new ModelRenderer(this, 28, 16);
        bipedLeftArm.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2, modelSize);
        bipedLeftArm.setRotationPoint(3.0F, 15.5F, 0.0F);

        bipedRightLeg = new ModelRenderer(this, 8, 16);
        bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2, modelSize);
        bipedRightLeg.setRotationPoint(-1.0F, 20.0F, 0.0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2, modelSize);
        bipedLeftLeg.setRotationPoint(1.0F, 20.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        boolean child = isChild;
        isChild = false;
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch, scale);
        isChild = child;
    }
}
