package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelPiglin;
import xy177.farmersfuturedelight.client.model.ModelPiglinBabyArmor;
import xy177.farmersfuturedelight.common.entity.EntityPiglin;

public final class RenderPiglin<T extends EntityLiving> extends RenderLiving<T> {
    private final ModelBase adultModel=new ModelPiglin(false);
    private final ModelBase babyModel=new ModelPiglin(true);

    public RenderPiglin(RenderManager manager) {
        super(manager,new ModelPiglin(false),.5F);
        addLayer(new LayerRenderer<T>() {
            private final LayerHeldItem adultHeld=new LayerHeldItem(RenderPiglin.this);
            @Override public void doRenderLayer(T entity,float limbSwing,float limbSwingAmount,float partialTicks,float age,float netHeadYaw,float headPitch,float scale) {
                if(!entity.isChild()){adultHeld.doRenderLayer(entity,limbSwing,limbSwingAmount,partialTicks,age,netHeadYaw,headPitch,scale);return;}
                boolean right=entity.getPrimaryHand()==EnumHandSide.RIGHT;
                renderHeld(entity,right?entity.getHeldItemMainhand():entity.getHeldItemOffhand(),EnumHandSide.RIGHT,scale);
                renderHeld(entity,right?entity.getHeldItemOffhand():entity.getHeldItemMainhand(),EnumHandSide.LEFT,scale);
            }
            @Override public boolean shouldCombineTextures(){return false;}
        });
        addLayer(new PiglinArmor());
        addLayer(new LayerRenderer<T>() {
            private final LayerCustomHead adultHead=new LayerCustomHead(((ModelPiglin)adultModel).bipedHead);
            @Override public void doRenderLayer(T entity,float limbSwing,float limbSwingAmount,float partialTicks,float age,float netHeadYaw,float headPitch,float scale) {
                if(entity.isChild())renderBabyHead(entity,scale,limbSwing);
                else adultHead.doRenderLayer(entity,limbSwing,limbSwingAmount,partialTicks,age,netHeadYaw,headPitch,scale);
            }
            @Override public boolean shouldCombineTextures() { return false; }
        });
    }

    private void renderBabyHead(T entity,float scale,float swing) {
        ItemStack head=entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if(head.isEmpty()||head.getItem() instanceof net.minecraft.item.ItemArmor)return;
        GlStateManager.pushMatrix();((ModelPiglin)babyModel).bipedHead.postRender(scale);
        if(head.getItem()==net.minecraft.init.Items.SKULL) {
            GlStateManager.scale(1.1875F,-1.1875F,-1.1875F);
            com.mojang.authlib.GameProfile profile=null;
            net.minecraft.nbt.NBTTagCompound data=head.getTagCompound();
            if(data!=null&&data.hasKey("SkullOwner",10))profile=net.minecraft.nbt.NBTUtil.readGameProfileFromNBT(data.getCompoundTag("SkullOwner"));
            else if(data!=null&&data.hasKey("SkullOwner",8)&&!data.getString("SkullOwner").trim().isEmpty()) {
                profile=net.minecraft.tileentity.TileEntitySkull.updateGameprofile(new com.mojang.authlib.GameProfile(null,data.getString("SkullOwner")));
                data.setTag("SkullOwner",net.minecraft.nbt.NBTUtil.writeGameProfile(new net.minecraft.nbt.NBTTagCompound(),profile));
            }
            net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer.instance.renderSkull(-.5F,0,-.5F,net.minecraft.util.EnumFacing.UP,180,head.getMetadata(),profile,-1,swing);
        } else {
            GlStateManager.translate(0,-.25F,0);GlStateManager.rotate(180,0,1,0);GlStateManager.scale(.625F,-.625F,-.625F);
            Minecraft.getMinecraft().getItemRenderer().renderItem(entity,head,ItemCameraTransforms.TransformType.HEAD);
        }
        GlStateManager.popMatrix();
    }

    private void renderHeld(T entity,ItemStack stack,EnumHandSide hand,float scale) {
        if(stack.isEmpty())return;
        GlStateManager.pushMatrix();((ModelPiglin)babyModel).postRenderArm(scale,hand);
        GlStateManager.rotate(-90,1,0,0);GlStateManager.rotate(180,0,1,0);
        GlStateManager.translate(0,.0625F,-.28125F);
        boolean left=hand==EnumHandSide.LEFT;
        Minecraft.getMinecraft().getItemRenderer().renderItemSide(entity,stack,left?ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND:ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,left);
        GlStateManager.popMatrix();
    }

    private final class PiglinArmor extends LayerBipedArmor {
        private final java.util.Map<EntityEquipmentSlot,ModelBiped> babyArmor=new java.util.EnumMap<>(EntityEquipmentSlot.class);
        private PiglinArmor() {
            super(RenderPiglin.this);
            for(EntityEquipmentSlot slot:EntityEquipmentSlot.values())if(slot.getSlotType()==EntityEquipmentSlot.Type.ARMOR)
                babyArmor.put(slot,new ModelPiglinBabyArmor((ModelPiglin)babyModel,slot));
        }
        @Override public ModelBiped getModelFromSlot(EntityEquipmentSlot slot) {
            return mainModel==babyModel?babyArmor.get(slot):super.getModelFromSlot(slot);
        }
        @Override public ResourceLocation getArmorResource(net.minecraft.entity.Entity entity,ItemStack stack,EntityEquipmentSlot slot,String overlay) {
            if(!(entity instanceof net.minecraft.entity.EntityLivingBase)||!((net.minecraft.entity.EntityLivingBase)entity).isChild())return super.getArmorResource(entity,stack,slot,overlay);
            String material=RenderModernZombieVillager.babyArmorMaterial(stack);
            if(material.isEmpty())return super.getArmorResource(entity,stack,slot,overlay);
            return new ResourceLocation(FarmerFutureDelight.MODID,"textures/entity/equipment/humanoid_baby/"+material+(overlay==null?"":"_"+overlay)+".png");
        }
    }

    @Override
    public void doRender(T entity,double posX,double posY,double posZ,float yaw,float partialTicks) {
        mainModel=entity.isChild()?babyModel:adultModel;shadowSize=entity.isChild()?.25F:.5F;
        super.doRender(entity,posX,posY,posZ,yaw,partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        String name=entity instanceof EntityPigZombie?"zombified_piglin":entity instanceof EntityPiglin&&((EntityPiglin)entity).isBrute()?"piglin_brute":"piglin";
        return new ResourceLocation(FarmerFutureDelight.MODID,"textures/entity/piglin/"+name+(entity.isChild()?"_baby":"")+".png");
    }

    @Override
    protected void applyRotations(T entity,float age,float yaw,float partialTicks) {
        if(entity instanceof EntityPiglin&&((EntityPiglin)entity).isConverting())yaw+=(float)(Math.cos(age*3.25)*Math.PI*.4);
        super.applyRotations(entity,age,yaw,partialTicks);
    }
}
