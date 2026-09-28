package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
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
    private final ModelRenderer[] animatedParts;

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
        leftHindLeg = flatLeg(20, 14, 2.0F, 1.75F, false);

        rightHindLeg = new ModelRenderer(this);
        rightHindLeg.setRotationPoint(-2.0F, 0.25F, 1.75F);
        rightHindLeg.rotateAngleY = 1.5708F;
        rightHindLeg.rotateAngleZ = 1.5708F;
        ModelRenderer rightHindLegCube = new ModelRenderer(this, 20, 14);
        rightHindLegCube.rotateAngleX = -1.5708F;
        rightHindLegCube.rotateAngleZ = 1.5708F;
        rightHindLegCube.addBox(0.0F, 0.0F, -0.5F, 3, 0, 1, 0.0F);
        rightHindLeg.addChild(rightHindLegCube);
        body.addChild(rightHindLeg);

        tail = new ModelRenderer(this, 10, 9);
        tail.setRotationPoint(0.0F, -0.25F, 3.25F);
        tail.addBox(0.0F, -1.5F, -1.0F, 0, 3, 8, 0.0F);
        body.addChild(tail);

        animatedParts = new ModelRenderer[] {
                root, body, head, topGills, leftGills, rightGills, tail,
                rightFrontLeg, leftFrontLeg, rightHindLeg, leftHindLeg
        };
    }

    private ModelRenderer flatLeg(int u, int v, float x, float z, boolean right) {
        ModelRenderer leg = new ModelRenderer(this, u, v);
        leg.setRotationPoint(x, 0.25F, z);
        leg.addBox(right ? -3.0F : 0.0F, 0.0F, -0.5F, 3, 0, 1, 0.0F);
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
        int animation = axolotl.getBabyAnimationState();
        float time = axolotl.getBabyAnimationTime(
                Math.max(0.0F, Math.min(1.0F, ageInTicks - axolotl.ticksExisted)));
        float weight = 1.0F;
        if (animation == EntityAxolotl.BABY_ANIMATION_WALK) {
            time = limbSwing * 0.75F;
            weight = Math.min(limbSwingAmount * 30.0F, 1.0F);
        }
        BabyAxolotlAnimationSet.apply(animation, time, weight, animatedParts);
    }

    private void resetPose() {
        resetPart(root, 0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        resetPart(body, 0.0F, -1.25F, 1.75F, 0.0F, 0.0F, 0.0F);
        resetPart(head, 0.0F, 0.25F, -2.75F, 0.0F, 0.0F, 0.0F);
        resetPart(topGills, 0.0F, -2.0F, -2.0F, 0.0F, 0.0F, 0.0F);
        resetPart(leftGills, 3.0F, -0.5F, -2.0F, 0.0F, 0.0F, 0.0F);
        resetPart(rightGills, -3.0F, -0.5F, -2.0F, 0.0F, 0.0F, 0.0F);
        resetPart(tail, 0.0F, -0.25F, 3.25F, 0.0F, 0.0F, 0.0F);
        resetPart(rightFrontLeg, -2.0F, 0.25F, -1.25F, 0.0F, 0.0F, 0.0F);
        resetPart(leftFrontLeg, 2.0F, 0.25F, -1.25F, 0.0F, 0.0F, 0.0F);
        resetPart(rightHindLeg, -2.0F, 0.25F, 1.75F,
                0.0F, 1.5708F, 1.5708F);
        resetPart(leftHindLeg, 2.0F, 0.25F, 1.75F, 0.0F, 0.0F, 0.0F);
    }

    private static void resetPart(ModelRenderer part, float x, float y, float z,
                                  float angleX, float angleY, float angleZ) {
        part.setRotationPoint(x, y, z);
        part.rotateAngleX = angleX;
        part.rotateAngleY = angleY;
        part.rotateAngleZ = angleZ;
    }
}
