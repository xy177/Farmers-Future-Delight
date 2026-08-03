package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.entity.EntityTurtle;

@SideOnly(Side.CLIENT)
public class ModelTurtle extends ModelBase {
    private static final float HALF_PI = (float) Math.PI / 2.0F;

    private final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer eggBelly;
    private final ModelRenderer frontLeftFlipper;
    private final ModelRenderer frontRightFlipper;
    private final ModelRenderer rearLeftFlipper;
    private final ModelRenderer rearRightFlipper;

    public ModelTurtle() {
        textureWidth = 128;
        textureHeight = 64;

        head = new ModelRenderer(this, 3, 0);
        head.setRotationPoint(0.0F, 19.0F, -10.0F);
        head.addBox(-3.0F, -1.0F, -3.0F, 6, 5, 6, 0.0F);

        body = new ModelRenderer(this, 7, 37);
        body.setRotationPoint(0.0F, 11.0F, -10.0F);
        body.rotateAngleX = HALF_PI;
        body.addBox(-9.5F, 3.0F, -10.0F, 19, 20, 6, 0.0F);
        body.setTextureOffset(31, 1);
        body.addBox(-5.5F, 3.0F, -13.0F, 11, 18, 3, 0.0F);

        eggBelly = new ModelRenderer(this, 70, 33);
        eggBelly.setRotationPoint(0.0F, 11.0F, -10.0F);
        eggBelly.rotateAngleX = HALF_PI;
        eggBelly.addBox(-4.5F, 3.0F, -14.0F, 9, 18, 1, 0.0F);

        rearRightFlipper = new ModelRenderer(this, 1, 23);
        rearRightFlipper.setRotationPoint(-3.5F, 22.0F, 11.0F);
        rearRightFlipper.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 10, 0.0F);

        rearLeftFlipper = new ModelRenderer(this, 1, 12);
        rearLeftFlipper.setRotationPoint(3.5F, 22.0F, 11.0F);
        rearLeftFlipper.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 10, 0.0F);

        frontRightFlipper = new ModelRenderer(this, 27, 30);
        frontRightFlipper.setRotationPoint(-5.0F, 21.0F, -4.0F);
        frontRightFlipper.addBox(-13.0F, 0.0F, -2.0F, 13, 1, 5, 0.0F);

        frontLeftFlipper = new ModelRenderer(this, 27, 24);
        frontLeftFlipper.setRotationPoint(5.0F, 21.0F, -4.0F);
        frontLeftFlipper.addBox(0.0F, 0.0F, -2.0F, 13, 1, 5, 0.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        head.render(scale);
        body.render(scale);
        eggBelly.render(scale);
        rearRightFlipper.render(scale);
        rearLeftFlipper.render(scale);
        frontRightFlipper.render(scale);
        frontLeftFlipper.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        EntityTurtle turtle = (EntityTurtle) entity;
        float verticalOffset = turtle.hasEgg() ? -1.0F : 0.0F;
        head.rotationPointY = 19.0F + verticalOffset;
        body.rotationPointY = 11.0F + verticalOffset;
        eggBelly.rotationPointY = 11.0F + verticalOffset;
        rearRightFlipper.rotationPointY = 22.0F + verticalOffset;
        rearLeftFlipper.rotationPointY = 22.0F + verticalOffset;
        frontRightFlipper.rotationPointY = 21.0F + verticalOffset;
        frontLeftFlipper.rotationPointY = 21.0F + verticalOffset;
        eggBelly.showModel = turtle.hasEgg();

        head.rotateAngleX = headPitch * 0.017453292F;
        head.rotateAngleY = netHeadYaw * 0.017453292F;
        frontLeftFlipper.rotateAngleY = 0.0F;
        frontRightFlipper.rotateAngleY = 0.0F;
        frontLeftFlipper.rotateAngleZ = 0.0F;
        frontRightFlipper.rotateAngleZ = 0.0F;
        rearLeftFlipper.rotateAngleX = 0.0F;
        rearRightFlipper.rotateAngleX = 0.0F;
        rearLeftFlipper.rotateAngleY = 0.0F;
        rearRightFlipper.rotateAngleY = 0.0F;

        if (!turtle.isInWater() && turtle.onGround) {
            float layEggScale = turtle.isLayingEgg() ? 4.0F : 1.0F;
            float layEggAmplitude = turtle.isLayingEgg() ? 2.0F : 1.0F;
            float frontSwing = MathHelper.cos(limbSwing * 5.0F * layEggScale)
                    * 8.0F * limbSwingAmount * layEggAmplitude;
            float rearSwing = MathHelper.cos(limbSwing * 5.0F) * 3.0F * limbSwingAmount;
            frontRightFlipper.rotateAngleY = -frontSwing;
            frontLeftFlipper.rotateAngleY = frontSwing;
            rearRightFlipper.rotateAngleY = -rearSwing;
            rearLeftFlipper.rotateAngleY = rearSwing;
            return;
        }

        float swing = MathHelper.cos(limbSwing * 0.6662F * 0.6F) * 0.5F * limbSwingAmount;
        rearRightFlipper.rotateAngleX = swing;
        rearLeftFlipper.rotateAngleX = -swing;
        frontRightFlipper.rotateAngleZ = -swing;
        frontLeftFlipper.rotateAngleZ = swing;
    }
}
