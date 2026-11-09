package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ModelModernVillager extends ModelVillager {
    private final ModelRenderer jacket;

    public ModelModernVillager(boolean baby) {
        super(0.0F);
        textureWidth = 64;
        textureHeight = 64;
        villagerHead = new ModelRenderer(this, 0, 0);
        villagerArms = new ModelRenderer(this);
        if (baby) {
            villagerHead.addBox(-4.0F, -8.0F, -3.5F, 8, 8, 7);
            villagerHead.setRotationPoint(0.0F, 16.0F, 0.0F);
            ModelRenderer hat = new ModelRenderer(this, 0, 30);
            hat.addBox(-4.0F, -4.0F, -3.5F, 8, 8, 7, 0.3F);
            hat.setRotationPoint(0.0F, -4.0F, 0.0F);
            villagerHead.addChild(hat);
            ModelRenderer rim = new ModelRenderer(this, 0, 45);
            rim.addBox(-7.0F, -0.5F, -6.0F, 14, 1, 12);
            rim.setRotationPoint(0.0F, -4.5F, 0.0F);
            villagerHead.addChild(rim);
            villagerNose = new ModelRenderer(this, 23, 0);
            villagerNose.addBox(-1.0F, 0.0F, -0.5F, 2, 2, 1);
            villagerNose.setRotationPoint(0.0F, -2.0F, -4.0F);
            villagerHead.addChild(villagerNose);
            villagerBody = new ModelRenderer(this, 0, 15);
            villagerBody.addBox(-2.0F, -2.75F, -1.5F, 4, 5, 3);
            villagerBody.setRotationPoint(0.0F, 18.75F, 0.0F);
            jacket = new ModelRenderer(this, 16, 21);
            jacket.addBox(-2.5F, -8.0F, -1.5F, 4, 6, 3, 0.2F);
            jacket.setRotationPoint(0.5F, 24.0F, 0.0F);
            villagerArms.setRotationPoint(0.0F, 17.5F, 0.0F);
            ModelRenderer hands = new ModelRenderer(this, 36, 15);
            hands.addBox(-1.0F, -2.4925F, -1.8401F, 2, 4, 2);
            hands.setTextureOffset(16, 15).addBox(5.0F, -2.4925F, -1.8401F, 2, 4, 2);
            hands.setRotationPoint(-3.0F, 1.4025F, -0.9599F);
            hands.rotateAngleX = -1.0472F;
            villagerArms.addChild(hands);
            ModelRenderer middle = new ModelRenderer(this, 24, 17);
            middle.addBox(-2.0F, -0.9924F, -0.9825F, 4, 2, 2);
            middle.setRotationPoint(0.0F, 0.9024F, -1.8175F);
            middle.rotateAngleX = -1.0472F;
            villagerArms.addChild(middle);
            rightVillagerLeg = new ModelRenderer(this, 8, 23);
            rightVillagerLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 3, 2);
            rightVillagerLeg.setRotationPoint(-1.0F, 21.5F, 0.0F);
            leftVillagerLeg = new ModelRenderer(this, 0, 23);
            leftVillagerLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 3, 2);
            leftVillagerLeg.setRotationPoint(1.0F, 21.5F, 0.0F);
        } else {
            villagerHead.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8);
            villagerNose = new ModelRenderer(this, 24, 0);
            villagerNose.addBox(-1.0F, -1.0F, -6.0F, 2, 4, 2);
            villagerNose.setRotationPoint(0.0F, -2.0F, 0.0F);
            villagerHead.addChild(villagerNose);
            ModelRenderer hat = new ModelRenderer(this, 32, 0);
            hat.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, 0.51F);
            villagerHead.addChild(hat);
            ModelRenderer rim = new ModelRenderer(this, 30, 47);
            rim.addBox(-8.0F, -8.0F, -6.0F, 16, 16, 1);
            rim.rotateAngleX = -(float) Math.PI / 2.0F;
            hat.addChild(rim);
            villagerBody = new ModelRenderer(this, 16, 20);
            villagerBody.addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6);
            jacket = new ModelRenderer(this, 0, 38);
            jacket.addBox(-4.0F, 0.0F, -3.0F, 8, 20, 6, 0.5F);
            villagerArms.setTextureOffset(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4, 8, 4);
            ModelRenderer leftArm = new ModelRenderer(this, 44, 22);
            leftArm.mirror = true;
            leftArm.addBox(4.0F, -2.0F, -2.0F, 4, 8, 4);
            villagerArms.addChild(leftArm);
            villagerArms.setTextureOffset(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8, 4, 4);
            villagerArms.setRotationPoint(0.0F, 3.0F, -1.0F);
            villagerArms.rotateAngleX = -0.75F;
            rightVillagerLeg = new ModelRenderer(this, 0, 22);
            rightVillagerLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
            rightVillagerLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
            leftVillagerLeg = new ModelRenderer(this, 0, 22);
            leftVillagerLeg.mirror = true;
            leftVillagerLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
            leftVillagerLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float age,
                       float yaw, float pitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, age, yaw, pitch, scale, entity);
        villagerHead.render(scale);
        villagerBody.render(scale);
        villagerArms.render(scale);
        jacket.render(scale);
        rightVillagerLeg.render(scale);
        leftVillagerLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float age,
                                  float yaw, float pitch, float scale, Entity entity) {
        villagerHead.rotateAngleY = yaw * (float) Math.PI / 180.0F;
        villagerHead.rotateAngleX = pitch * (float) Math.PI / 180.0F;
        villagerHead.rotateAngleZ = 0.0F;
        if (entity.getEntityData().getLong("farmers_future_delight:unhappy_until")
                > entity.world.getTotalWorldTime()) {
            villagerHead.rotateAngleZ = 0.3F * MathHelper.sin(0.45F * age);
            villagerHead.rotateAngleX = 0.4F;
        }
        rightVillagerLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 0.7F * limbSwingAmount;
        leftVillagerLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.7F * limbSwingAmount;
        rightVillagerLeg.rotateAngleY = 0.0F;
        leftVillagerLeg.rotateAngleY = 0.0F;
        if (entity.getEntityData().getBoolean("farmers_future_delight:sleeping")) {
            villagerHead.rotateAngleX = 0.0F;
            villagerHead.rotateAngleY = 0.0F;
            rightVillagerLeg.rotateAngleX = 0.0F;
            leftVillagerLeg.rotateAngleX = 0.0F;
        }
    }
}
