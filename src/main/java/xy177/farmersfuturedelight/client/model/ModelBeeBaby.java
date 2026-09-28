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
public class ModelBeeBaby extends ModelBase {
    private final ModelRenderer bone;
    private final ModelRenderer leftWing;
    private final ModelRenderer rightWing;
    private final ModelRenderer frontLeg;
    private final ModelRenderer middleLeg;
    private final ModelRenderer backLeg;
    private final ModelRenderer stinger;
    private float rollAmount;
    private boolean angry;

    public ModelBeeBaby() {
        textureWidth = 32;
        textureHeight = 32;

        bone = new ModelRenderer(this);
        bone.setRotationPoint(0.0F, 19.6667F, -1.8567F);
        bone.setTextureOffset(6, 12).addBox(1.0F, -1.6667F, -2.1633F, 1, 2, 2);
        bone.setTextureOffset(0, 12).addBox(-2.0F, -1.6667F, -2.1933F, 1, 2, 2);

        ModelRenderer body = new ModelRenderer(this, 0, 0);
        body.setRotationPoint(0.0F, 1.3333F, 2.3567F);
        body.addBox(-2.0F, -2.0F, -2.5F, 4, 4, 5);
        bone.addChild(body);

        stinger = new ModelRenderer(this, 13, 2);
        stinger.setRotationPoint(0.0F, 0.5F, 2.5F);
        stinger.addBox(0.0F, -0.5F, 0.0F, 0, 1, 1);
        body.addChild(stinger);

        rightWing = new ModelRenderer(this, 3, 9);
        rightWing.setRotationPoint(-1.0F, -0.6667F, 0.8567F);
        rightWing.addBox(-3.0F, 0.0F, 0.0F, 3, 0, 3);
        bone.addChild(rightWing);

        leftWing = new ModelRenderer(this, -3, 9);
        leftWing.mirror = true;
        leftWing.setRotationPoint(1.0F, -0.6667F, 0.8567F);
        leftWing.addBox(0.0F, 0.0F, 0.0F, 3, 0, 3);
        bone.addChild(leftWing);

        frontLeg = leg(1.8567F, 0);
        middleLeg = leg(2.8567F, 1);
        backLeg = leg(3.8567F, 2);
    }

    private ModelRenderer leg(float z, int textureY) {
        ModelRenderer leg = new ModelRenderer(this, 13, textureY);
        leg.setRotationPoint(0.0F, 3.3333F, z);
        leg.addBox(-1.5F, 0.0F, 0.0F, 3, 1, 0);
        bone.addChild(leg);
        return leg;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        bone.render(scale);
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
        boolean flying = !entity.onGround || entity.motionX * entity.motionX
                + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ >= 1.0E-7D;
        bone.rotationPointY = 19.6667F;
        bone.rotateAngleX = 0.0F;
        float wingAngle = flying ? MathHelper.cos(ageInTicks * 2.1F) * 0.4712F : 0.0F;
        rightWing.rotateAngleX = 0.2182F;
        leftWing.rotateAngleX = 0.2182F;
        rightWing.rotateAngleY = flying ? 0.0F : 0.3491F;
        leftWing.rotateAngleY = flying ? 0.0F : -0.3491F;
        rightWing.rotateAngleZ = wingAngle;
        leftWing.rotateAngleZ = -wingAngle;
        float legAngle = flying ? 0.7854F : 0.0F;
        frontLeg.rotateAngleX = legAngle;
        middleLeg.rotateAngleX = legAngle;
        backLeg.rotateAngleX = legAngle;
        if (!angry && flying) {
            float speed = MathHelper.cos(ageInTicks * 0.18F);
            bone.rotateAngleX = 0.1F + speed * (float) Math.PI * 0.025F;
            bone.rotationPointY -= MathHelper.cos(ageInTicks * 0.18F) * 0.9F;
            frontLeg.rotateAngleX = -speed * (float) Math.PI * 0.1F + 0.3926991F;
            backLeg.rotateAngleX = -speed * (float) Math.PI * 0.05F + 0.7853982F;
        }
        bone.rotateAngleX = lerpRotation(rollAmount, bone.rotateAngleX, 3.0915928F);
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
