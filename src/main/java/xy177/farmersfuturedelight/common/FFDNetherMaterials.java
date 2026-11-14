package xy177.farmersfuturedelight.common;

import java.util.UUID;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDEnchantments;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@Mod.EventBusSubscriber(modid=FarmerFutureDelight.MODID)
public final class FFDNetherMaterials {
    private static final UUID SOUL_SPEED_ID=UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");
    private FFDNetherMaterials() { }

    public static boolean basalt(World world,BlockPos pos,IBlockState state) {
        if(world.isRemote||state.getMaterial()!=Material.LAVA||FFDConfig.basaltMode==FFDConfig.FeatureMode.DISABLED)return false;
        IBlockState soil=world.getBlockState(pos.down());
        if(!path(soil,"soul_soil"))return false;
        for(EnumFacing side:new EnumFacing[]{EnumFacing.UP,EnumFacing.NORTH,EnumFacing.SOUTH,EnumFacing.WEST,EnumFacing.EAST}) {
            IBlockState neighbor=world.getBlockState(pos.offset(side));
            if(neighbor.getMaterial()==Material.WATER)return false;
            if(!path(neighbor,"blue_ice"))continue;
            net.minecraft.block.Block block=FFDItems.effectiveBlock(FFDBlocks.BASALT);
            if(block==null||block==Blocks.AIR)return false;
            IBlockState replacement=net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(world,pos,pos,block.getDefaultState());
            world.setBlockState(pos,replacement,3);world.playEvent(1501,pos,0);return true;
        }
        return false;
    }

    private static boolean path(IBlockState state,String name) {
        ResourceLocation id=state.getBlock().getRegistryName();return id!=null&&name.equals(id.getResourcePath());
    }

    public static int soulSpeed(EntityLivingBase entity) {
        if(FFDConfig.soulSpeedMode==FFDConfig.FeatureMode.DISABLED)return 0;
        int level=0;
        for(java.util.Map.Entry<Enchantment,Integer> entry:EnchantmentHelper.getEnchantments(entity.getItemStackFromSlot(EntityEquipmentSlot.FEET)).entrySet())
            if(entry.getKey()==FFDEnchantments.SOUL_SPEED)level=Math.max(level,entry.getValue());
        return level;
    }

    public static boolean soulGround(EntityLivingBase entity) {
        IBlockState ground=entity.world.getBlockState(new BlockPos(entity.posX,entity.getEntityBoundingBox().minY-.2D,entity.posZ));
        return ground.getBlock()==Blocks.SOUL_SAND||path(ground,"soul_soil");
    }

    public static boolean ignoreSoulSand(Entity entity) { return entity instanceof EntityLivingBase&&soulSpeed((EntityLivingBase)entity)>0; }

    @SubscribeEvent
    public static void living(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity=event.getEntityLiving();
        int level=soulSpeed(entity);
        IAttributeInstance speed=entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if(speed==null)return;
        AttributeModifier old=speed.getModifier(SOUL_SPEED_ID);
        boolean flying=entity.isElytraFlying()||entity instanceof net.minecraft.entity.player.EntityPlayer&&((net.minecraft.entity.player.EntityPlayer)entity).capabilities.isFlying;
        boolean ground=soulGround(entity);
        boolean active=level>0&&!entity.isRiding()&&!flying&&(ground||old!=null&&!entity.onGround);
        boolean moving=entity.motionX*entity.motionX+entity.motionZ*entity.motionZ>=1.0E-10D;
        if(entity.world.isRemote) {
            if(level>0&&!flying&&ground&&entity.onGround&&moving&&entity.ticksExisted%5==0)FarmerFutureDelight.proxy.spawnSoulParticle(entity);
            return;
        }
        double amount=.03D*(1.0D+level*.35D);
        if(old!=null&&(!active||old.getAmount()!=amount))speed.removeModifier(old);
        if(active&&speed.getModifier(SOUL_SPEED_ID)==null)speed.applyModifier(new AttributeModifier(SOUL_SPEED_ID,"Soul speed",amount,0).setSaved(false));
        if(!active||!entity.onGround||!ground)return;
        if(entity.getRNG().nextFloat()<.04F)entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).damageItem(1,entity);
        if(moving&&entity.ticksExisted%5==0&&entity.getRNG().nextFloat()<.35F) {
            entity.playSound(FFDSounds.SOUL_ESCAPE,.6F,.6F+entity.getRNG().nextFloat()*.4F);
        }
    }
}
