package xy177.farmersfuturedelight.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityPiglin;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@Mod.EventBusSubscriber(modid=FarmerFutureDelight.MODID)
public final class FFDPiglinEvents {
    private FFDPiglinEvents() { }

    public static boolean modernZombified() {
        if(FFDConfig.zombifiedPiglinMode==null||FFDConfig.zombifiedPiglinMode==FFDConfig.FeatureMode.DISABLED)return false;
        if(FFDConfig.zombifiedPiglinMode==FFDConfig.FeatureMode.ENABLED)return true;
        for(String provider:new String[]{"nb","netherized"})if(net.minecraftforge.fml.common.Loader.isModLoaded(provider)
                &&FFDConfig.isAutoCompatibilityEnabled("zombified_piglin",provider))return false;
        return true;
    }

    public static float eyeHeight(float original,net.minecraft.entity.monster.EntityPigZombie entity) {
        return modernZombified()?(entity.isChild()?.78F:1.79F):original;
    }

    @SubscribeEvent
    public static void updateZombified(LivingEvent.LivingUpdateEvent event) {
        if(!(event.getEntityLiving() instanceof net.minecraft.entity.monster.EntityPigZombie)||!modernZombified())return;
        net.minecraft.entity.monster.EntityPigZombie entity=(net.minecraft.entity.monster.EntityPigZombie)event.getEntityLiving();
        float width=entity.isChild()?.49F:.6F;
        float height=entity.isChild()?.98F:1.95F;
        if(entity.width==width&&entity.height==height)return;
        entity.width=width;entity.height=height;
        double radius=width/2.0D;
        entity.setEntityBoundingBox(new AxisAlignedBB(entity.posX-radius,entity.posY,entity.posZ-radius,entity.posX+radius,entity.posY+height,entity.posZ+radius));
    }

    public static void angerNearby(EntityPlayer player,boolean sight) {
        if(player.world.isRemote||player.isCreative()||player.isSpectator())return;
        for(EntityPiglin piglin:player.world.getEntitiesWithinAABB(EntityPiglin.class,player.getEntityBoundingBox().grow(16),entity->!entity.isChild())) {
            if(!sight||piglin.getEntitySenses().canSee(player))piglin.angerAt(player,true);
        }
    }

    @SubscribeEvent
    public static void open(PlayerContainerEvent.Open event) {
        if(event.getEntityPlayer().world.isRemote)return;
        if(event.getContainer() instanceof net.minecraft.inventory.ContainerChest
                ||event.getContainer() instanceof net.minecraft.inventory.ContainerShulkerBox)angerNearby(event.getEntityPlayer(),false);
    }

    @SubscribeEvent
    public static void mine(BlockEvent.BreakEvent event) {
        if(event.isCanceled()||event.getWorld().isRemote)return;
        ResourceLocation id=event.getState().getBlock().getRegistryName();if(id==null)return;
        String path=id.getResourcePath();
        if(path.contains("gold_ore")||path.equals("gold_block")||path.equals("gilded_blackstone")||path.contains("chest")||path.equals("barrel")||path.contains("shulker_box"))angerNearby(event.getPlayer(),false);
    }

    @SubscribeEvent
    public static void sound(PlaySoundAtEntityEvent event) {
        if(!(event.getEntity() instanceof net.minecraft.entity.monster.EntityPigZombie)||!modernZombified()||event.getSound()==null)return;
        String path=event.getSound().getSoundName().getResourcePath();
        if(path.equals("entity.zombie_pig.ambient"))event.setSound(FFDSounds.ZOMBIFIED_PIGLIN_AMBIENT);
        else if(path.equals("entity.zombie_pig.angry"))event.setSound(FFDSounds.ZOMBIFIED_PIGLIN_ANGRY);
        else if(path.equals("entity.zombie_pig.hurt"))event.setSound(FFDSounds.ZOMBIFIED_PIGLIN_HURT);
        else if(path.equals("entity.zombie_pig.death"))event.setSound(FFDSounds.ZOMBIFIED_PIGLIN_DEATH);
    }
}
