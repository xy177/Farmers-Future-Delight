package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.entity.EntityAxolotl;

@SideOnly(Side.CLIENT)
public class ModelAxolotlBaby extends ModelBase {
    private final ModelRenderer root;
    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer topGills;
    private final ModelRenderer leftGills;
    private final ModelRenderer rightGills;
    private final ModelRenderer tail;
    private final ModelRenderer rightFrontLeg;
    private final ModelRenderer leftFrontLeg;
    private final ModelRenderer rightHindLeg;
    private final ModelRenderer leftHindLeg;

    public ModelAxolotlBaby() {
        textureWidth = 32;
        textureHeight = 32;
        root = new ModelRenderer(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);

        body = new ModelRenderer(this, 0, 0);
        body.setRotationPoint(0.0F, -1.25F, 1.75F);
        body.addBox(-2.0F, -0.75F, -2.75F, 4, 2, 6, 0.0F);
        body.setTextureOffset(0, 12);
        body.addBox(0.0F, -1.75F, -2.75F, 0, 3, 5, 0.0F);
        root.addChild(body);

        head = new ModelRenderer(this, 0, 8);
        head.setRotationPoint(0.0F, 0.25F, -2.75F);
        head.addBox(-3.0F, -2.0F, -4.0F, 6, 3, 4, 0.0F);
        body.addChild(head);

        topGills = new ModelRenderer(this, 20, 0);
        topGills.setRotationPoint(0.0F, -2.0F, -2.0F);
        topGills.addBox(-3.0F, -3.0F, 0.0F, 6, 3, 0, 0.0F);
        head.addChild(topGills);
        leftGills = new ModelRenderer(this, 20, 8);
        leftGills.setRotationPoint(3.0F, -0.5F, -2.0F);
        leftGills.addBox(0.0F, -3.5F, 0.0F, 3, 5, 0, 0.0F);
        head.addChild(leftGills);
        rightGills = new ModelRenderer(this, 20, 3);
        rightGills.setRotationPoint(-3.0F, -0.5F, -2.0F);
        rightGills.addBox(-3.0F, -3.5F, 0.0F, 3, 5, 0, 0.0F);
        head.addChild(rightGills);

        rightFrontLeg = flatLeg(20, 16, -2.0F, -1.25F, true);
        leftFrontLeg = flatLeg(20, 13, 2.0F, -1.25F, false);
        rightHindLeg = flatLeg(20, 14, -2.0F, 1.75F, true);
        leftHindLeg = flatLeg(20, 14, 2.0F, 1.75F, false);

        tail = new ModelRenderer(this, 10, 9);
        tail.setRotationPoint(0.0F, -0.25F, 3.25F);
        tail.addBox(0.0F, -1.5F, -1.0F, 0, 3, 8, 0.0F);
        body.addChild(tail);
    }

    private ModelRenderer flatLeg(int u, int v, float x, float z, boolean right) {
        ModelRenderer leg = new ModelRenderer(this, u, v);
        leg.setRotationPoint(x, 0.25F, z);
        leg.addBox(right ? -3.0F : 0.0F, 0.0F, -0.5F, 3, 0, 1, 0.01F);
        body.addChild(leg);
        return leg;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        root.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entity) {
        EntityAxolotl axolotl = (EntityAxolotl) entity;
        resetPose();
        if (axolotl.isPlayingDead()) {
            body.rotateAngleX = -0.15F;
            body.rotateAngleZ = 0.35F;
            leftHindLeg.rotateAngleZ = 0.8F;
            leftFrontLeg.rotateAngleY = 1.8F;
            mirrorLegs();
            return;
        }
        if (axolotl.isInWater()) {
            float time = ageInTicks * (limbSwingAmount > 0.02F ? 0.42F : 0.12F);
            float sway = MathHelper.sin(time);
            body.rotateAngleX = headPitch * 0.017453292F + sway * 0.12F;
            tail.rotateAngleY = MathHelper.cos(time * 0.9F) * 0.55F;
            topGills.rotateAngleX = -0.55F * sway - 0.7F;
            leftGills.rotateAngleY = 0.35F * sway + 0.8F;
            rightGills.rotateAngleY = -leftGills.rotateAngleY;
            leftFrontLeg.rotateAngleZ = 1.2F;
            leftHindLeg.rotateAngleZ = 1.4F;
        } else if (axolotl.onGround) {
            float sway = MathHelper.cos(ageInTicks * 0.18F);
            tail.rotateAngleY = sway * 0.18F;
            head.rotateAngleY = sway * 0.12F;
            leftFrontLeg.rotateAngleY = 1.2F + sway * 0.5F;
            leftHindLeg.rotateAngleY = 1.0F - sway * 0.35F;
        }
        head.rotateAngleY += MathHelper.clamp(netHeadYaw, -45.0F, 45.0F) * 0.017453292F;
        mirrorLegs();
    }

    private void resetPose() {
        body.rotateAngleX = 0.0F;
        body.rotateAngleY = 0.0F;
        body.rotateAngleZ = 0.0F;
        head.rotateAngleX = 0.0F;
        head.rotateAngleY = 0.0F;
        head.rotateAngleZ = 0.0F;
        topGills.rotateAngleX = 0.0F;
        leftGills.rotateAngleY = 0.0F;
        rightGills.rotateAngleY = 0.0F;
        tail.rotateAngleY = 0.0F;
        resetLeg(leftFrontLeg);
        resetLeg(rightFrontLeg);
        resetLeg(leftHindLeg);
        resetLeg(rightHindLeg);
    }

    private static void resetLeg(ModelRenderer leg) {
        leg.rotateAngleX = 0.0F;
        leg.rotateAngleY = 0.0F;
        leg.rotateAngleZ = 0.0F;
    }

    private void mirrorLegs() {
        rightFrontLeg.rotateAngleX = leftFrontLeg.rotateAngleX;
        rightFrontLeg.rotateAngleY = -leftFrontLeg.rotateAngleY;
        rightFrontLeg.rotateAngleZ = -leftFrontLeg.rotateAngleZ;
        rightHindLeg.rotateAngleX = leftHindLeg.rotateAngleX;
        rightHindLeg.rotateAngleY = -leftHindLeg.rotateAngleY;
        rightHindLeg.rotateAngleZ = -leftHindLeg.rotateAngleZ;
    }
}
