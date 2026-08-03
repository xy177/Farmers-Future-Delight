package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.entity.EntityBee;

@SideOnly(Side.CLIENT)
public class ModelBee extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer leftWing;
    private final ModelRenderer rightWing;
    private final ModelRenderer frontLeg;
    private final ModelRenderer middleLeg;
    private final ModelRenderer backLeg;
    private final ModelRenderer stinger;
    private final ModelRenderer leftAntenna;
    private final ModelRenderer rightAntenna;
    private float rollAmount;
    private boolean angry;

    public ModelBee() {
        textureWidth = 64;
        textureHeight = 64;

        body = new ModelRenderer(this);
        body.setRotationPoint(0.0F, 19.0F, 0.0F);
        ModelRenderer mainBody = new ModelRenderer(this, 0, 0);
        mainBody.addBox(-3.5F, -4.0F, -5.0F, 7, 7, 10, 0.0F);
        body.addChild(mainBody);

        stinger = new ModelRenderer(this, 26, 7);
        stinger.addBox(0.0F, -1.0F, 5.0F, 0, 1, 2, 0.0F);
        mainBody.addChild(stinger);

        leftAntenna = new ModelRenderer(this, 2, 0);
        leftAntenna.setRotationPoint(0.0F, -2.0F, -5.0F);
        leftAntenna.addBox(1.5F, -2.0F, -3.0F, 1, 2, 3, 0.0F);
        mainBody.addChild(leftAntenna);

        rightAntenna = new ModelRenderer(this, 2, 3);
        rightAntenna.setRotationPoint(0.0F, -2.0F, -5.0F);
        rightAntenna.addBox(-2.5F, -2.0F, -3.0F, 1, 2, 3, 0.0F);
        mainBody.addChild(rightAntenna);

        leftWing = new ModelRenderer(this, 0, 18);
        leftWing.setRotationPoint(-1.5F, -4.0F, -3.0F);
        leftWing.addBox(-9.0F, 0.0F, 0.0F, 9, 0, 6, 0.001F);
        body.addChild(leftWing);

        rightWing = new ModelRenderer(this, 0, 18);
        rightWing.setRotationPoint(1.5F, -4.0F, -3.0F);
        rightWing.mirror = true;
        rightWing.addBox(0.0F, 0.0F, 0.0F, 9, 0, 6, 0.001F);
        body.addChild(rightWing);

        frontLeg = leg(-2.0F);
        middleLeg = leg(0.0F);
        backLeg = leg(2.0F);
    }

    private ModelRenderer leg(float z) {
        ModelRenderer leg = new ModelRenderer(this, 26, z < 0.0F ? 1 : z > 0.0F ? 5 : 3);
        leg.setRotationPoint(1.5F, 3.0F, z);
        leg.addBox(-5.0F, 0.0F, 0.0F, 7, 2, 0, 0.0F);
        body.addChild(leg);
        return leg;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        body.render(scale);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing,
                                    float limbSwingAmount, float partialTickTime) {
        if (entity instanceof EntityBee) {
            EntityBee bee = (EntityBee) entity;
            stinger.showModel = !bee.hasStung();
            rollAmount = bee.getRollAmount(partialTickTime);
            angry = bee.getAnger() > 0;
        } else {
            stinger.showModel = true;
            rollAmount = 0.0F;
            angry = false;
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entity) {
        boolean flying = !entity.onGround;
        body.rotationPointY = 19.0F;
        body.rotateAngleX = 0.0F;
        leftAntenna.rotateAngleX = 0.0F;
        rightAntenna.rotateAngleX = 0.0F;
        leftWing.rotateAngleX = 0.0F;
        rightWing.rotateAngleX = 0.0F;
        leftWing.rotateAngleY = flying ? 0.0F : -0.2618F;
        rightWing.rotateAngleY = flying ? 0.0F : 0.2618F;
        float wingAngle = flying ? MathHelper.cos(ageInTicks * 2.1F) * 0.4712F : 0.0F;
        leftWing.rotateAngleZ = wingAngle;
        rightWing.rotateAngleZ = -wingAngle;

        float legAngle = flying ? 0.7854F : 0.0F;
        frontLeg.rotateAngleX = legAngle;
        middleLeg.rotateAngleX = legAngle;
        backLeg.rotateAngleX = legAngle;
        if (!angry && flying) {
            float speed = MathHelper.cos(ageInTicks * 0.18F);
            body.rotateAngleX = 0.1F + speed * (float) Math.PI * 0.025F;
            body.rotationPointY -= MathHelper.cos(ageInTicks * 0.18F) * 0.9F;
            frontLeg.rotateAngleX = -speed * (float) Math.PI * 0.1F + 0.3926991F;
            backLeg.rotateAngleX = -speed * (float) Math.PI * 0.05F + 0.7853982F;
            leftAntenna.rotateAngleX = speed * (float) Math.PI * 0.03F;
            rightAntenna.rotateAngleX = leftAntenna.rotateAngleX;
        }
        body.rotateAngleX = lerpRotation(rollAmount, body.rotateAngleX, 3.0915928F);
    }

    private static float lerpRotation(float amount, float start, float end) {
        float difference = (end - start) % ((float) Math.PI * 2.0F);
        if (difference < -(float) Math.PI) {
            difference += (float) Math.PI * 2.0F;
        } else if (difference >= (float) Math.PI) {
            difference -= (float) Math.PI * 2.0F;
        }
        return start + amount * difference;
    }
}
