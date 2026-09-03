package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.entity.EntityPhantom;

@SideOnly(Side.CLIENT)
public class ModelPhantom extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer leftWingBase;
    private final ModelRenderer leftWingTip;
    private final ModelRenderer rightWingBase;
    private final ModelRenderer rightWingTip;
    private final ModelRenderer tailBase;
    private final ModelRenderer tailTip;

    public ModelPhantom() {
        textureWidth = 64;
        textureHeight = 64;

        body = new ModelRenderer(this, 0, 8);
        body.addBox(-3.0F, -2.0F, -8.0F, 5, 3, 9, 0.0F);
        body.rotateAngleX = -0.1F;

        tailBase = new ModelRenderer(this, 3, 20);
        tailBase.setRotationPoint(0.0F, -2.0F, 1.0F);
        tailBase.addBox(-2.0F, 0.0F, 0.0F, 3, 2, 6, 0.0F);
        body.addChild(tailBase);

        tailTip = new ModelRenderer(this, 4, 29);
        tailTip.setRotationPoint(0.0F, 0.5F, 6.0F);
        tailTip.addBox(-1.0F, 0.0F, 0.0F, 1, 1, 6, 0.0F);
        tailBase.addChild(tailTip);

        leftWingBase = new ModelRenderer(this, 23, 12);
        leftWingBase.setRotationPoint(2.0F, -2.0F, -8.0F);
        leftWingBase.addBox(0.0F, 0.0F, 0.0F, 6, 2, 9, 0.0F);
        body.addChild(leftWingBase);

        leftWingTip = new ModelRenderer(this, 16, 24);
        leftWingTip.setRotationPoint(6.0F, 0.0F, 0.0F);
        leftWingTip.addBox(0.0F, 0.0F, 0.0F, 13, 1, 9, 0.0F);
        leftWingBase.addChild(leftWingTip);

        rightWingBase = new ModelRenderer(this, 23, 12);
        rightWingBase.mirror = true;
        rightWingBase.setRotationPoint(-3.0F, -2.0F, -8.0F);
        rightWingBase.addBox(-6.0F, 0.0F, 0.0F, 6, 2, 9, 0.0F);
        body.addChild(rightWingBase);

        rightWingTip = new ModelRenderer(this, 16, 24);
        rightWingTip.mirror = true;
        rightWingTip.setRotationPoint(-6.0F, 0.0F, 0.0F);
        rightWingTip.addBox(-13.0F, 0.0F, 0.0F, 13, 1, 9, 0.0F);
        rightWingBase.addChild(rightWingTip);

        ModelRenderer head = new ModelRenderer(this, 0, 0);
        head.setRotationPoint(0.0F, 1.0F, -7.0F);
        head.addBox(-4.0F, -2.0F, -5.0F, 7, 3, 5, 0.0F);
        head.rotateAngleX = 0.2F;
        body.addChild(head);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        body.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entity) {
        EntityPhantom phantom = (EntityPhantom) entity;
        float partialTicks = MathHelper.clamp(ageInTicks - phantom.ticksExisted, 0.0F, 1.0F);
        body.rotateAngleX = -0.1F + phantom.getRenderPitch(partialTicks) * 0.017453292F;
        float animation = (phantom.getUniqueFlapTickOffset() + ageInTicks)
                * 7.448451F * 0.017453292F;
        float wingAngle = MathHelper.cos(animation) * 16.0F * 0.017453292F;
        leftWingBase.rotateAngleZ = wingAngle;
        leftWingTip.rotateAngleZ = wingAngle;
        rightWingBase.rotateAngleZ = -wingAngle;
        rightWingTip.rotateAngleZ = -wingAngle;
        float tailAngle = -(5.0F + MathHelper.cos(animation * 2.0F) * 5.0F) * 0.017453292F;
        tailBase.rotateAngleX = tailAngle;
        tailTip.rotateAngleX = tailAngle;
    }
}
