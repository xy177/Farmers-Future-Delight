package xy177.farmersfuturedelight.common.entity;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.core.FFDSquidAgeHooks;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDAgeLock {
    private static final String LOCKED = "ffdAgeLocked";
    private static final String TIMER = "ffdAgeLockFeedback";

    private FFDAgeLock() {
    }

    public static boolean isLocked(EntityLivingBase entity) {
        return entity.getEntityData().getBoolean(LOCKED);
    }

    public static int feedbackTicks(EntityLivingBase entity) {
        return entity.getEntityData().getInteger(TIMER);
    }

    public static void applyState(EntityLivingBase entity, boolean locked, int timer) {
        entity.getEntityData().setBoolean(LOCKED, locked);
        entity.getEntityData().setInteger(TIMER, Math.max(0, Math.min(40, timer)));
    }

    public static void advanceAge(EntityAgeable entity, int age) {
        if (!isLocked(entity) || entity.getGrowingAge() >= 0) entity.setGrowingAge(age);
    }

    public static boolean canGrow(EntityAgeable entity) {
        return entity.isChild() && !isLocked(entity);
    }

    public static boolean canLock(EntityLivingBase entity) {
        return entity.isEntityAlive() && entity.isChild() && !entity.isEntityUndead()
                && !(entity instanceof EntityVillager)
                && !(entity instanceof EntityZombieHorse)
                && !(entity instanceof EntitySkeletonHorse)
                && (entity instanceof EntityAgeable || entity.getClass() == EntitySquid.class
                    || entity.getClass() == EntityGlowSquid.class);
    }

    public static void setAge(EntityLivingBase entity, int age) {
        if (entity instanceof EntityAgeable) ((EntityAgeable) entity).setGrowingAge(age);
        else if (entity instanceof EntityGlowSquid) ((EntityGlowSquid) entity).setGrowingAge(age);
        else if (entity.getClass() == EntitySquid.class) FFDSquidAgeHooks.setAge((EntitySquid) entity, age);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof EntityLiving)) return;
        EntityLiving entity = (EntityLiving) event.getTarget();
        ItemStack held = event.getItemStack();
        if (!held.isEmpty() && held.getItem() == FFDItems.effectiveItem(FFDItems.GOLDEN_DANDELION)
                && canLock(entity) && feedbackTicks(entity) == 0) {
            if (!entity.world.isRemote) {
                boolean locked = !isLocked(entity);
                setAge(entity, -24000);
                applyState(entity, locked, 40);
                if (locked) entity.enablePersistence();
                if (!event.getEntityPlayer().capabilities.isCreativeMode) held.shrink(1);
                entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
                        locked ? FFDSounds.GOLDEN_DANDELION_USE : FFDSounds.GOLDEN_DANDELION_UNUSE,
                        SoundCategory.PLAYERS, 1.0F, 1.0F);
                FFDNetwork.sendAgeLockState(entity);
            }
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        } else if (isLocked(entity) && entity instanceof EntityAnimal
                && !(entity instanceof AbstractHorse)
                && !(entity instanceof EntityTameable)
                && ((EntityAnimal) entity).isBreedingItem(held)) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.PASS);
        }
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        int timer = feedbackTicks(entity);
        if (timer > 0) {
            if (entity.world.isRemote && timer % 2 == 0) {
                FarmerFutureDelight.proxy.spawnAgeLockParticle(entity, isLocked(entity));
            }
            entity.getEntityData().setInteger(TIMER, timer - 1);
        }
    }

    @SubscribeEvent
    public static void join(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityLivingBase) event.getEntity().getEntityData().removeTag(TIMER);
    }

    @SubscribeEvent
    public static void track(PlayerEvent.StartTracking event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP && event.getTarget() instanceof EntityLivingBase
                && isLocked((EntityLivingBase) event.getTarget())) {
            FFDNetwork.sendAgeLockState((EntityPlayerMP) event.getEntityPlayer(),
                    (EntityLivingBase) event.getTarget());
        }
    }
}
