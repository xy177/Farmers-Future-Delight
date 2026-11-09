package xy177.farmersfuturedelight.client.render;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelModernZombieVillager extends ModelBiped {
    protected final boolean baby;
    private final Map<ModelRenderer, float[]> pivots = new LinkedHashMap<>();

    public ModelModernZombieVillager(boolean baby) {
        super(0.0F, 0.0F, 64, 64);
        this.baby = baby;
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHeadwear = new ModelRenderer(this);
        if (baby) {
            bipedHead.addBox(-4.0F, -8.0F, -3.5F, 8, 8, 7);
            bipedHead.setRotationPoint(0.0F, 16.0F, 0.0F);
            ModelRenderer hat = new ModelRenderer(this, 0, 31);
            hat.addBox(-4.0F, -4.0F, -3.5F, 8, 8, 7, 0.3F);
            hat.setRotationPoint(0.0F, -4.0F, 0.0F);
            bipedHead.addChild(hat);
            ModelRenderer rim = new ModelRenderer(this, 0, 46);
            rim.addBox(-7.0F, -0.5F, -6.0F, 14, 1, 12);
            rim.setRotationPoint(0.0F, -4.5F, 0.0F);
            bipedHead.addChild(rim);
            ModelRenderer nose = new ModelRenderer(this, 23, 0);
            nose.addBox(-1.0F, -1.0F, -0.5F, 2, 2, 1);
            nose.setRotationPoint(0.0F, -1.0F, -4.0F);
            bipedHead.addChild(nose);
            bipedBody = new ModelRenderer(this, 0, 15);
            bipedBody.addBox(-2.0F, -2.75F, -1.5F, 4, 5, 3);
            bipedBody.setTextureOffset(16, 22).addBox(-2.0F, -2.75F, -1.5F, 4, 6, 3, 0.1F);
            bipedBody.setRotationPoint(0.0F, 18.75F, 0.0F);
            bipedRightArm = new ModelRenderer(this, 24, 15);
            bipedRightArm.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2);
            bipedRightArm.setRotationPoint(-3.0F, 15.5F, 0.0F);
            bipedLeftArm = new ModelRenderer(this, 16, 15);
            bipedLeftArm.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2);
            bipedLeftArm.setRotationPoint(3.0F, 15.5F, 0.0F);
            bipedRightLeg = new ModelRenderer(this, 8, 23);
            bipedRightLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 3, 2);
            bipedRightLeg.setRotationPoint(-1.0F, 21.5F, 0.0F);
            bipedLeftLeg = new ModelRenderer(this, 0, 23);
            bipedLeftLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 3, 2);
            bipedLeftLeg.setRotationPoint(1.0F, 21.5F, 0.0F);
        } else {
            bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8);
            bipedHead.setTextureOffset(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2, 4, 2);
            ModelRenderer hat = new ModelRenderer(this, 32, 0);
            hat.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, 0.5F);
            bipedHead.addChild(hat);
            ModelRenderer rim = new ModelRenderer(this, 30, 47);
            rim.addBox(-8.0F, -8.0F, -6.0F, 16, 16, 1);
            rim.rotateAngleX = -(float) Math.PI / 2.0F;
            hat.addChild(rim);
            bipedBody = new ModelRenderer(this, 16, 20);
            bipedBody.addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6);
            bipedBody.setTextureOffset(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8, 20, 6, 0.05F);
            bipedRightArm = new ModelRenderer(this, 44, 22);
            bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4);
            bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
            bipedLeftArm = new ModelRenderer(this, 44, 22);
            bipedLeftArm.mirror = true;
            bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4);
            bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            bipedRightLeg = new ModelRenderer(this, 0, 22);
            bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
            bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
            bipedLeftLeg = new ModelRenderer(this, 0, 22);
            bipedLeftLeg.mirror = true;
            bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
            bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        }
        rememberPivots();
    }

    protected final void rememberPivots() {
        pivots.clear();
        for (ModelRenderer part : new ModelRenderer[] {bipedHead, bipedHeadwear, bipedBody,
                bipedRightArm, bipedLeftArm, bipedRightLeg, bipedLeftLeg}) {
            pivots.put(part, new float[] {part.rotationPointX, part.rotationPointY, part.rotationPointZ});
        }
    }

    private void resetPivots() {
        for (Map.Entry<ModelRenderer, float[]> entry : pivots.entrySet()) {
            float[] position = entry.getValue();
            entry.getKey().setRotationPoint(position[0], position[1], position[2]);
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbAmount, float age,
                       float yaw, float pitch, float scale) {
        setRotationAngles(limbSwing, limbAmount, age, yaw, pitch, scale, entity);
        bipedHead.render(scale);
        bipedBody.render(scale);
        bipedRightArm.render(scale);
        bipedLeftArm.render(scale);
        bipedRightLeg.render(scale);
        bipedLeftLeg.render(scale);
        bipedHeadwear.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbAmount, float age,
                                  float yaw, float pitch, float scale, Entity entity) {
        resetPivots();
        isSneak = entity.isSneaking();
        super.setRotationAngles(limbSwing, limbAmount, age, yaw, pitch, scale, entity);
        resetPivots();
        EntityZombieVillager zombie = (EntityZombieVillager) entity;
        if (!isRiding) {
            bipedRightLeg.rotateAngleY = 0.005F;
            bipedLeftLeg.rotateAngleY = -0.005F;
            bipedRightLeg.rotateAngleZ = 0.005F;
            bipedLeftLeg.rotateAngleZ = -0.005F;
        }
        if (swingProgress > 0.0F) {
            float armScale = baby ? 2.5F : 5.0F;
            bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * armScale;
            bipedRightArm.rotationPointX = -MathHelper.cos(bipedBody.rotateAngleY) * armScale;
            bipedLeftArm.rotationPointZ = -MathHelper.sin(bipedBody.rotateAngleY) * armScale;
            bipedLeftArm.rotationPointX = MathHelper.cos(bipedBody.rotateAngleY) * armScale;
        }
        boolean raised = !baby || zombie.getHeldItemMainhand().isEmpty();
        float attackYaw = (raised ? 1.0F : -1.0F) * MathHelper.sin(swingProgress * (float) Math.PI);
        float attackPitch = MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress))
                * (float) Math.PI);
        float armPitch = (raised ? -(float) Math.PI / (zombie.isArmsRaised() ? 1.5F : 2.25F) : 0.0F)
                + attackYaw * 1.2F - attackPitch * 0.4F;
        float armYaw = 0.1F - attackYaw * 0.6F;
        bipedRightArm.rotateAngleX = armPitch;
        bipedLeftArm.rotateAngleX = armPitch;
        bipedRightArm.rotateAngleY = raised ? -armYaw : armYaw;
        bipedLeftArm.rotateAngleY = raised ? armYaw : -armYaw;
        bipedRightArm.rotateAngleZ = MathHelper.cos(age * 0.09F) * 0.05F + 0.05F;
        bipedLeftArm.rotateAngleZ = -MathHelper.cos(age * 0.09F) * 0.05F - 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(age * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(age * 0.067F) * 0.05F;
        if (entity.isSneaking()) {
            bipedHead.rotationPointY += 4.2F;
            bipedBody.rotationPointY += 3.2F;
            bipedRightArm.rotationPointY += 3.2F;
            bipedLeftArm.rotationPointY += 3.2F;
            bipedRightLeg.rotationPointZ += 4.0F;
            bipedLeftLeg.rotationPointZ += 4.0F;
            bipedRightArm.rotateAngleX += 0.4F;
            bipedLeftArm.rotateAngleX += 0.4F;
        }
        copyModelAngles(bipedHead, bipedHeadwear);
    }
}
