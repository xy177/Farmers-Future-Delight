package xy177.farmersfuturedelight.core;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public final class FFDFireworkHooks {
    private static final String PENDING_HIT = "ffdFireworkPendingHit";
    private static final String LEFT_OWNER = "ffdFireworkLeftOwner";
    private static final DataParameter<Boolean> SHOT_AT_ANGLE =
            EntityDataManager.createKey(EntityFireworkRocket.class, DataSerializers.BOOLEAN);
    private static Method explosionDamageMethod;

    private FFDFireworkHooks() {
    }

    public static void initialize() {
    }

    public static void register(EntityFireworkRocket rocket) {
        rocket.getDataManager().register(SHOT_AT_ANGLE, false);
    }

    public static void setShotAtAngle(EntityFireworkRocket rocket, boolean angled) {
        rocket.getDataManager().set(SHOT_AT_ANGLE, angled);
    }

    public static void setShooter(EntityFireworkRocket rocket, EntityLivingBase shooter) {
        rocket.getEntityData().setString("ffdFireworkShooter", shooter.getUniqueID().toString());
    }

    public static Entity getShooter(EntityFireworkRocket rocket) {
        String uuid = rocket.getEntityData().getString("ffdFireworkShooter");
        if (uuid.isEmpty()) {
            return null;
        }
        for (Entity entity : rocket.world.loadedEntityList) {
            if (uuid.equals(entity.getUniqueID().toString())) {
                return entity;
            }
        }
        return null;
    }

    public static boolean isShotAtAngle(EntityFireworkRocket rocket) {
        return rocket.getDataManager().get(SHOT_AT_ANGLE);
    }

    public static double horizontalAcceleration(double original, EntityFireworkRocket rocket) {
        return isShotAtAngle(rocket) ? 1.0D : original;
    }

    public static double verticalAcceleration(double original, EntityFireworkRocket rocket) {
        return isShotAtAngle(rocket) ? 0.0D : original;
    }

    public static void onFlightMove(EntityFireworkRocket rocket) {
        if (rocket.world.isRemote || rocket.isAttachedToEntity()) {
            return;
        }
        rocket.getEntityData().setInteger(PENDING_HIT, findFlightHit(rocket));
    }

    public static void onAttachedFlight(EntityFireworkRocket rocket) {
        if (rocket.world.isRemote || !rocket.isAttachedToEntity()) {
            return;
        }
        rocket.getEntityData().setInteger(PENDING_HIT, findFlightHit(rocket));
    }

    public static boolean finishFlightMove(EntityFireworkRocket rocket) {
        if (rocket.world.isRemote) {
            return false;
        }
        NBTTagCompound data = rocket.getEntityData();
        int hit = data.getInteger(PENDING_HIT);
        data.removeTag(PENDING_HIT);
        if (hit != 2) {
            return false;
        }
        rocket.setPosition(data.getDouble("ffdFireworkHitX"),
                data.getDouble("ffdFireworkHitY"), data.getDouble("ffdFireworkHitZ"));
        rocket.world.setEntityState(rocket, (byte) 17);
        invokeExplosionDamage(rocket);
        rocket.setDead();
        return true;
    }

    private static int findFlightHit(EntityFireworkRocket rocket) {
        if (rocket.noClip) {
            return 0;
        }
        Vec3d start = rocket.getPositionVector();
        Vec3d end = start.addVector(rocket.motionX, rocket.motionY, rocket.motionZ);
        RayTraceResult blockHit = rocket.world.rayTraceBlocks(start, end, false, true, false);
        Vec3d traceEnd = blockHit == null ? end : blockHit.hitVec;
        AxisAlignedBB search = rocket.getEntityBoundingBox()
                .expand(rocket.motionX, rocket.motionY, rocket.motionZ).grow(1.0D);
        double nearestDistance = start.squareDistanceTo(traceEnd);
        Entity shooter = getShooter(rocket);
        boolean leftOwner = hasLeftOwner(rocket, shooter, search);
        float margin = Math.max(0.0F, Math.min(0.3F, (rocket.ticksExisted - 2) / 20.0F));
        List<Entity> candidates = rocket.world.getEntitiesWithinAABBExcludingEntity(rocket, search);
        Vec3d entityHitPosition = null;
        Entity entityHitEntity = null;
        for (Entity candidate : candidates) {
            if (candidate.isDead || candidate.noClip || !candidate.canBeCollidedWith()
                    || shooter != null && !leftOwner && sharesVehicle(shooter, candidate)) {
                continue;
            }
            AxisAlignedBB bounds = candidate.getEntityBoundingBox()
                    .grow(margin + candidate.getCollisionBorderSize());
            RayTraceResult entityHit = bounds.calculateIntercept(start, traceEnd);
            if (entityHit != null && start.squareDistanceTo(entityHit.hitVec) < nearestDistance) {
                nearestDistance = start.squareDistanceTo(entityHit.hitVec);
                entityHitPosition = entityHit.hitVec;
                entityHitEntity = candidate;
            }
        }
        if (entityHitPosition != null && entityHitEntity != null) {
            RayTraceResult entityHit = new RayTraceResult(entityHitEntity, entityHitPosition);
            if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                    new net.minecraftforge.event.entity.ProjectileImpactEvent(rocket, entityHit))) {
                return 0;
            }
            storeHit(rocket, entityHitPosition);
            return 2;
        }
        if (blockHit == null || blockHit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return 0;
        }
        net.minecraftforge.event.entity.ProjectileImpactEvent event =
                new net.minecraftforge.event.entity.ProjectileImpactEvent(rocket, blockHit);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) {
            return 0;
        }
        storeHit(rocket, blockHit.hitVec);
        return hasExplosion(rocket) ? 2 : 1;
    }

    private static void storeHit(EntityFireworkRocket rocket, Vec3d hit) {
        NBTTagCompound data = rocket.getEntityData();
        data.setDouble("ffdFireworkHitX", hit.x);
        data.setDouble("ffdFireworkHitY", hit.y);
        data.setDouble("ffdFireworkHitZ", hit.z);
    }

    private static boolean hasLeftOwner(EntityFireworkRocket rocket, Entity shooter,
            AxisAlignedBB search) {
        NBTTagCompound data = rocket.getEntityData();
        if (data.getBoolean(LEFT_OWNER) || shooter == null) {
            data.setBoolean(LEFT_OWNER, true);
            return true;
        }
        for (Entity candidate : rocket.world.getEntitiesWithinAABBExcludingEntity(rocket, search)) {
            if (sharesVehicle(shooter, candidate)) {
                return false;
            }
        }
        data.setBoolean(LEFT_OWNER, true);
        return true;
    }

    private static boolean sharesVehicle(Entity first, Entity second) {
        return rootVehicle(first) == rootVehicle(second);
    }

    private static Entity rootVehicle(Entity entity) {
        while (entity.isRiding() && entity.getRidingEntity() != null) {
            entity = entity.getRidingEntity();
        }
        return entity;
    }

    private static boolean hasExplosion(EntityFireworkRocket rocket) {
        List<EntityDataManager.DataEntry<?>> entries = rocket.getDataManager().getAll();
        if (entries == null) {
            return false;
        }
        for (EntityDataManager.DataEntry<?> entry : entries) {
            Object value = entry.getValue();
            if (!(value instanceof ItemStack)) {
                continue;
            }
            NBTTagCompound fireworks = ((ItemStack) value).getSubCompound("Fireworks");
            if (fireworks != null && fireworks.getTagList("Explosions", 10).tagCount() > 0) {
                return true;
            }
        }
        return false;
    }

    private static void invokeExplosionDamage(EntityFireworkRocket rocket) {
        try {
            if (explosionDamageMethod == null) {
                for (Method method : EntityFireworkRocket.class.getDeclaredMethods()) {
                    if (Modifier.isPrivate(method.getModifiers())
                            && method.getReturnType() == void.class
                            && method.getParameterTypes().length == 0
                            && ("dealExplosionDamage".equals(method.getName())
                                    || "func_191510_k".equals(method.getName()))) {
                        method.setAccessible(true);
                        explosionDamageMethod = method;
                        break;
                    }
                }
            }
            if (explosionDamageMethod == null) {
                throw new NoSuchMethodException("EntityFireworkRocket.dealExplosionDamage");
            }
            explosionDamageMethod.invoke(rocket);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not detonate a firework rocket", exception);
        }
    }

    public static void write(EntityFireworkRocket rocket, NBTTagCompound tag) {
        tag.setBoolean("ShotAtAngle", isShotAtAngle(rocket));
    }

    public static void read(EntityFireworkRocket rocket, NBTTagCompound tag) {
        setShotAtAngle(rocket, tag.getBoolean("ShotAtAngle"));
    }
}
