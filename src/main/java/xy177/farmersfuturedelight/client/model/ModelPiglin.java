package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityPiglin;

public final class ModelPiglin extends ModelBiped {
    private final boolean baby;
    private final ModelRenderer leftEar;
    private final ModelRenderer rightEar;
    private final ModelRenderer[] outerLayers;

    public ModelPiglin(boolean baby) {
        super(0,0,64,64);
        this.baby=baby;
        if(baby) {
            bipedBody=part(0,13,0,18,-.5F);bipedBody.addBox(-3,-3,-1,6,5,3);
            bipedHead=part(0,0,0,15,0);bipedHead.addBox(-4.5F,-6,-3.5F,9,6,7);
            bipedHead.setTextureOffset(21,30).addBox(-1.5F,-3,-4.5F,3,3,1);
            leftEar=part(0,0,4.2F,-4,0);rightEar=part(0,0,-4.2F,-4,0);
            ModelRenderer leftShape=part(0,21,1,1.75F,0);leftShape.addBox(-.5F,-3,-2,1,6,4);leftShape.rotateAngleZ=-.6109F;leftEar.addChild(leftShape);
            ModelRenderer rightShape=part(18,13,-1,1.75F,0);rightShape.addBox(-.5F,-3,-2,1,6,4);rightShape.rotateAngleZ=.6109F;rightEar.addChild(rightShape);
            bipedLeftArm=part(28,13,4,15,0);bipedLeftArm.addBox(-1,0,-1.5F,2,5,3);
            bipedRightArm=part(10,30,-4,15,0);bipedRightArm.addBox(-1,0,-1.5F,2,5,3);
            bipedRightLeg=part(22,23,-1.5F,20,0);bipedRightLeg.addBox(-1.5F,0,-1.5F,3,4,3);
            bipedLeftLeg=part(10,23,1.5F,20,0);bipedLeftLeg.addBox(-1.5F,0,-1.5F,3,4,3);
            outerLayers=new ModelRenderer[0];
        } else {
            bipedHead=part(0,0,0,0,0);bipedHead.addBox(-5,-8,-4,10,8,8);
            bipedHead.setTextureOffset(31,1).addBox(-2,-4,-5,4,4,1);
            bipedHead.setTextureOffset(2,4).addBox(2,-2,-5,1,2,1);
            bipedHead.setTextureOffset(2,0).addBox(-3,-2,-5,1,2,1);
            leftEar=part(51,6,4.5F,-6,0);leftEar.addBox(0,0,-2,1,5,4);
            rightEar=part(39,6,-4.5F,-6,0);rightEar.addBox(-1,0,-2,1,5,4);
            bipedLeftArm=part(32,48,5,2,0);bipedLeftArm.addBox(-1,-2,-2,4,12,4);
            bipedLeftLeg=part(16,48,1.9F,12,0);bipedLeftLeg.addBox(-2,0,-2,4,12,4);
            ModelRenderer jacket=part(16,32,0,0,0);jacket.addBox(-4,0,-2,8,12,4,.25F);
            ModelRenderer rightSleeve=part(40,32,-5,2,0);rightSleeve.addBox(-3,-2,-2,4,12,4,.25F);
            ModelRenderer leftSleeve=part(48,48,5,2,0);leftSleeve.addBox(-1,-2,-2,4,12,4,.25F);
            ModelRenderer rightPants=part(0,32,-1.9F,12,0);rightPants.addBox(-2,0,-2,4,12,4,.25F);
            ModelRenderer leftPants=part(0,48,1.9F,12,0);leftPants.addBox(-2,0,-2,4,12,4,.25F);
            outerLayers=new ModelRenderer[]{jacket,rightSleeve,leftSleeve,rightPants,leftPants};
        }
        bipedHead.addChild(leftEar);bipedHead.addChild(rightEar);
        bipedHeadwear.showModel=false;
    }

    private ModelRenderer part(int textureX,int textureY,float posX,float posY,float posZ) {
        ModelRenderer part=new ModelRenderer(this,textureX,textureY);part.setRotationPoint(posX,posY,posZ);return part;
    }

    @Override
    public void render(Entity entity,float swing,float amount,float age,float headYaw,float pitch,float scale) {
        setRotationAngles(swing,amount,age,headYaw,pitch,scale,entity);
        bipedHead.render(scale);bipedBody.render(scale);bipedRightArm.render(scale);bipedLeftArm.render(scale);bipedRightLeg.render(scale);bipedLeftLeg.render(scale);
        if(!baby) {
            copyModelAngles(bipedBody,outerLayers[0]);copyModelAngles(bipedRightArm,outerLayers[1]);copyModelAngles(bipedLeftArm,outerLayers[2]);
            copyModelAngles(bipedRightLeg,outerLayers[3]);copyModelAngles(bipedLeftLeg,outerLayers[4]);
            for(ModelRenderer layer:outerLayers)layer.render(scale);
        }
    }

    @Override
    public void setRotationAngles(float swing,float amount,float age,float headYaw,float pitch,float scale,Entity entity) {
        isChild=false;isSneak=false;
        super.setRotationAngles(swing,amount,age,headYaw,pitch,scale,entity);
        bipedHead.setRotationPoint(0,baby?15:0,0);bipedBody.setRotationPoint(0,baby?18:0,baby?-.5F:0);
        bipedRightArm.setRotationPoint(baby?-4:-5,baby?15:2,0);bipedLeftArm.setRotationPoint(baby?4:5,baby?15:2,0);
        bipedRightLeg.setRotationPoint(baby?-1.5F:-1.9F,baby?20:12,0);bipedLeftLeg.setRotationPoint(baby?1.5F:1.9F,baby?20:12,0);
        float angle=baby?.08726646F:.5235988F;float frequency=age*.1F+swing*.5F;float amplitude=.08F+amount*.4F;
        leftEar.rotateAngleZ=-angle-MathHelper.cos(frequency*1.2F)*amplitude;
        rightEar.rotateAngleZ=angle+MathHelper.cos(frequency)*amplitude;
        if(entity instanceof EntityPiglin) {
            EntityPiglin piglin=(EntityPiglin)entity;
            if(piglin.isDancing()) {
                float phase=age/60.0F;bipedHead.rotationPointX+=MathHelper.sin(phase*10);bipedHead.rotationPointY+=MathHelper.sin(phase*40)+.4F;
                bipedRightArm.rotateAngleZ=.017453292F*(70+MathHelper.cos(phase*40)*10);bipedLeftArm.rotateAngleZ=-bipedRightArm.rotateAngleZ;
                bipedRightArm.rotationPointY+=MathHelper.sin(phase*40)*.5F-.5F;bipedLeftArm.rotationPointY+=MathHelper.sin(phase*40)*.5F+.5F;
                bipedBody.rotationPointY+=MathHelper.sin(phase*40)*.35F;
            } else if(piglin.isAdmiring()) {
                bipedHead.rotateAngleX=.5F;bipedHead.rotateAngleY=0;bipedLeftArm.rotateAngleY=.5F;bipedLeftArm.rotateAngleX=-.9F;
            } else if(piglin.hasCrossbow()&&(piglin.isChargingCrossbow()||xy177.farmersfuturedelight.common.item.ItemCrossbow.isCharged(piglin.getHeldItemMainhand()))) {
                if(piglin.isChargingCrossbow()) {
                    bipedRightArm.rotateAngleY=-.8F;bipedRightArm.rotateAngleX=-.97079635F;bipedLeftArm.rotateAngleX=bipedRightArm.rotateAngleX;
                    float partialTicks=MathHelper.clamp(age-piglin.ticksExisted,0,1);
                    int duration=Math.max(5,xy177.farmersfuturedelight.common.item.ItemCrossbow.getChargeDuration(piglin.getHeldItemMainhand()));
                    float fraction=MathHelper.clamp((piglin.getItemInUseMaxCount()+partialTicks)/duration,0,1);bipedLeftArm.rotateAngleY=.4F+.45F*fraction;bipedLeftArm.rotateAngleX=-.97079635F-(float)Math.PI*.191F*fraction;
                } else {
                    bipedRightArm.rotateAngleY=-.3F+bipedHead.rotateAngleY;bipedLeftArm.rotateAngleY=.6F+bipedHead.rotateAngleY;
                    bipedRightArm.rotateAngleX=-(float)Math.PI/2+bipedHead.rotateAngleX+.1F;bipedLeftArm.rotateAngleX=-1.5F+bipedHead.rotateAngleX;
                }
            } else if(piglin.isAggressive() && swingProgress==0) {
                if(piglin.getPrimaryHand()==net.minecraft.util.EnumHandSide.LEFT)bipedLeftArm.rotateAngleX=-1.8F;
                else bipedRightArm.rotateAngleX=-1.8F;
            }
        } else if(entity instanceof EntityPigZombie) {
            EntityPigZombie zombie=(EntityPigZombie)entity;
            boolean raised=!baby||zombie.getHeldItemMainhand().isEmpty();
            float attack=MathHelper.sin(swingProgress*(float)Math.PI)*(raised?1:-1);
            float recovery=MathHelper.sin((1-(1-swingProgress)*(1-swingProgress))*(float)Math.PI);
            float angleX=(raised?-(float)Math.PI/(zombie.isArmsRaised()?1.5F:2.25F):0)+attack*1.2F-recovery*.4F;
            bipedRightArm.rotateAngleX=angleX;bipedLeftArm.rotateAngleX=angleX;
            bipedRightArm.rotateAngleY=(.1F-attack*.6F)*(raised?-1:1);bipedLeftArm.rotateAngleY=-bipedRightArm.rotateAngleY;
            bipedRightArm.rotateAngleZ=0;bipedLeftArm.rotateAngleZ=0;
        }
    }
}
