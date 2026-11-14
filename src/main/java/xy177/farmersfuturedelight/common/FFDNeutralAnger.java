package xy177.farmersfuturedelight.common;

import java.lang.reflect.Field;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityBee;
import xy177.farmersfuturedelight.common.entity.EntityPiglin;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDNeutralAnger {
    private static final String DATA = "FFDNeutralAnger";
    private static final Field PIG_TIME = ReflectionHelper.findField(EntityPigZombie.class,
            "angerLevel", "field_70837_d");
    private static final Field PIG_TARGET = ReflectionHelper.findField(EntityPigZombie.class,
            "angerTargetUUID", "field_175459_bn");
    private static final ThreadLocal<Boolean> UPDATING = ThreadLocal.withInitial(() -> false);

    private FFDNeutralAnger() {
    }

    private static boolean supported(EntityLivingBase entity) {
        return entity instanceof EntityPigZombie && FFDPiglinEvents.modernZombified()
                || entity instanceof EntityEnderman || entity instanceof EntityPolarBear
                || entity instanceof EntityIronGolem || entity instanceof EntityBee
                || entity instanceof EntityWolf && !((EntityWolf) entity).isTamed();
    }

    private static NBTTagCompound data(EntityLiving entity) {
        NBTTagCompound root = entity.getEntityData();
        if (!root.hasKey(DATA, 10)) root.setTag(DATA, new NBTTagCompound());
        return root.getCompoundTag(DATA);
    }

    public static int remaining(EntityLiving entity) {
        try {
            if (entity instanceof EntityPigZombie) return PIG_TIME.getInt(entity);
            if (entity instanceof EntityBee) return ((EntityBee) entity).getAnger();
            return data(entity).getInteger("Time");
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static UUID target(EntityLiving entity) {
        try {
            if (entity instanceof EntityPigZombie) return (UUID) PIG_TARGET.get(entity);
            if (entity instanceof EntityBee) return ((EntityBee) entity).getAngerTarget();
            return data(entity).hasUniqueId("Target") ? data(entity).getUniqueId("Target") : null;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void state(EntityLiving entity, int time, UUID target) {
        time = Math.max(0, time);
        try {
            if (entity instanceof EntityPigZombie) {
                PIG_TIME.setInt(entity, time);
                PIG_TARGET.set(entity, target);
            } else if (entity instanceof EntityBee) {
                ((EntityBee) entity).setAnger(time);
                ((EntityBee) entity).setAngerTarget(target);
            } else {
                NBTTagCompound data = data(entity);
                data.setInteger("Time", time);
                if (target == null) data.removeTag("Target");
                else data.setUniqueId("Target", target);
                if (entity instanceof EntityWolf) ((EntityWolf) entity).setAngry(time > 0);
            }
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static boolean rule(EntityLiving entity, String name) {
        FFDPortingGameRules.ensure(entity.world.getGameRules());
        return entity.world.getGameRules().getBoolean(name);
    }

    public static boolean universal(EntityLiving entity) {
        return remaining(entity) > 0 && target(entity) == null
                && data(entity).getBoolean("Universal")
                && rule(entity, FFDPortingGameRules.UNIVERSAL_ANGER);
    }

    private static boolean validPlayer(EntityLiving entity, EntityPlayer player) {
        return player.isEntityAlive() && !player.isSpectator()
                && !player.capabilities.isCreativeMode
                && entity.world.getDifficulty() != EnumDifficulty.PEACEFUL
                && (!(entity instanceof EntityIronGolem)
                    || !((EntityIronGolem) entity).isPlayerCreated());
    }

    private static void attackTarget(EntityLiving entity, EntityLivingBase target) {
        boolean previous = UPDATING.get();
        UPDATING.set(true);
        try {
            entity.setRevengeTarget(null);
            entity.setAttackTarget(target);
        } finally {
            UPDATING.set(previous);
        }
    }

    public static void stop(EntityLiving entity) {
        attackTarget(entity, null);
        state(entity, 0, null);
        entity.getEntityData().removeTag(DATA);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attacked(LivingHurtEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim.world.isRemote || !(victim instanceof EntityLiving) || !supported(victim)
                || !(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityLiving entity = (EntityLiving) victim;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if (!validPlayer(entity, player) || !rule(entity, FFDPortingGameRules.UNIVERSAL_ANGER)) return;
        makeUniversal(entity);
        if (entity instanceof EntityEnderman) return;
        double range = entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
                .getAttributeValue();
        for (EntityLiving ally : entity.world.getEntitiesWithinAABB(EntityLiving.class,
                entity.getEntityBoundingBox().grow(range, 10.0D, range))) {
            if (ally != entity && ally.getClass() == entity.getClass() && supported(ally)
                    && validPlayer(ally, player)) makeUniversal(ally);
        }
    }

    private static void makeUniversal(EntityLiving entity) {
        attackTarget(entity, null);
        data(entity).setBoolean("Universal", true);
        state(entity, 400 + entity.getRNG().nextInt(400), null);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void targeting(LivingSetAttackTargetEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if (UPDATING.get() || living.world.isRemote || !(living instanceof EntityLiving)
                || !supported(living) || event.getTarget() == null) return;
        EntityLiving entity = (EntityLiving) living;
        EntityLivingBase candidate = event.getTarget();
        if (!(candidate instanceof EntityPlayer)) return;
        if (candidate instanceof EntityPlayer && !validPlayer(entity, (EntityPlayer) candidate)) {
            stop(entity);
            return;
        }
        UUID previous = target(entity);
        if (remaining(entity) > 0 && previous != null && !previous.equals(candidate.getUniqueID())
                && entity.getRevengeTarget() != candidate) {
            attackTarget(entity, null);
            return;
        }
        boolean universal = candidate instanceof EntityPlayer
                && rule(entity, FFDPortingGameRules.UNIVERSAL_ANGER);
        UUID chosen = universal ? null : candidate.getUniqueID();
        int time = remaining(entity);
        if (time <= 0 || !universal && !candidate.getUniqueID().equals(previous)) {
            time = 400 + entity.getRNG().nextInt(400);
        }
        state(entity, time, chosen);
        data(entity).setBoolean("Universal", universal);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if (living.world.isRemote || !(living instanceof EntityLiving) || !supported(living)) return;
        EntityLiving entity = (EntityLiving) living;
        if (entity.isAIDisabled()) return;
        if (rule(entity, FFDPortingGameRules.UNIVERSAL_ANGER)
                && entity.getRevengeTarget() instanceof EntityPlayer
                && target(entity) != null) {
            makeUniversal(entity);
        }
        int time = remaining(entity);
        UUID angryAt = target(entity);
        EntityLivingBase current = entity.getAttackTarget();
        if (current != null && (!current.isEntityAlive()
                || current instanceof EntityPlayer && !validPlayer(entity, (EntityPlayer) current))) {
            boolean rememberedPlayer = current instanceof EntityPlayer
                    && angryAt != null && angryAt.equals(current.getUniqueID());
            boolean universalTarget = data(entity).getBoolean("Universal") && angryAt == null;
            if (!current.isEntityAlive() && (rememberedPlayer || universalTarget)
                    && !rule(entity, FFDPortingGameRules.FORGIVE_DEAD_PLAYERS)) {
                attackTarget(entity, null);
            } else if (universalTarget && !current.isEntityAlive()) {
                attackTarget(entity, null);
            } else {
                stop(entity);
            }
            current = null;
        }
        if (time <= 0) {
            if (angryAt != null || data(entity).getBoolean("Universal")) stop(entity);
            return;
        }
        if (angryAt != null && entity.world instanceof WorldServer) {
            Entity remembered = ((WorldServer) entity.world).getEntityFromUuid(angryAt);
            if (remembered instanceof EntityPlayer && ((EntityPlayer) remembered).isEntityAlive()
                    && !validPlayer(entity, (EntityPlayer) remembered)) {
                stop(entity);
                return;
            }
        }
        boolean nativeTimer = entity instanceof EntityPigZombie || entity instanceof EntityBee;
        if (!nativeTimer) {
            state(entity, time - 1, angryAt);
        }
        if (remaining(entity) == 0) {
            stop(entity);
            return;
        }
        if (data(entity).getBoolean("Universal")) {
            if (rule(entity, FFDPortingGameRules.UNIVERSAL_ANGER)) state(entity, remaining(entity), null);
            else if (current != null) {
                entity.getEntityData().removeTag(DATA);
                state(entity, remaining(entity), current.getUniqueID());
            } else {
                stop(entity);
                return;
            }
        }
        if (current == null && entity.ticksExisted % 20 == 0) {
            EntityPlayer nearest = null;
            double range = entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
                    .getAttributeValue();
            for (EntityPlayer player : entity.world.playerEntities) {
                if (!validPlayer(entity, player) || entity.getDistanceSq(player) > range * range
                        || !entity.getEntitySenses().canSee(player)
                        || !universal(entity) && !player.getUniqueID().equals(target(entity))) continue;
                if (nearest == null || entity.getDistanceSq(player) < entity.getDistanceSq(nearest)) {
                    nearest = player;
                }
            }
            if (nearest != null) attackTarget(entity, nearest);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void died(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        FFDPortingGameRules.ensure(player.world.getGameRules());
        if (!player.world.getGameRules().getBoolean(FFDPortingGameRules.FORGIVE_DEAD_PLAYERS)) return;
        for (Entity raw : player.world.loadedEntityList) {
            if (!(raw instanceof EntityLiving)) continue;
            EntityLiving entity = (EntityLiving) raw;
            if (supported(entity) && player.getUniqueID().equals(target(entity))) stop(entity);
            if (entity instanceof EntityPiglin) ((EntityPiglin) entity).forgivePlayer(player);
        }
    }
}
