package xy177.farmersfuturedelight.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.WorldServer;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.entity.EntityPhantom;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class FFDCrossbowArrowHooks {
    private static final String TAG = "FFDCrossbow";
    private static final DataParameter<Byte> CROSSBOW =
            EntityDataManager.createKey(EntityArrow.class, DataSerializers.BYTE);
    private static final Map<EntityArrow, Shot> SHOTS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private FFDCrossbowArrowHooks() {
    }

    public static void initialize() {
    }

    public static void register(EntityArrow arrow) {
        arrow.getDataManager().register(CROSSBOW, (byte) 0);
    }

    public static void configure(EntityArrow arrow, int piercing) {
        arrow.getDataManager().set(CROSSBOW, (byte) (Math.max(0, Math.min(4, piercing)) + 1));
        Shot shot = new Shot();
        shot.shooter = arrow.shootingEntity == null ? null : arrow.shootingEntity.getUniqueID();
        SHOTS.put(arrow, shot);
    }

    public static boolean isCrossbow(EntityArrow arrow) {
        return arrow.getDataManager().get(CROSSBOW) != 0;
    }

    public static int piercing(EntityArrow arrow) {
        return Math.max(0, arrow.getDataManager().get(CROSSBOW) - 1);
    }

    public static EnumParticleTypes spectralParticle(EntityArrow arrow, EnumParticleTypes original) {
        return isCrossbow(arrow) ? EnumParticleTypes.SPELL : original;
    }

    private static Shot shot(EntityArrow arrow) {
        synchronized (SHOTS) {
            return SHOTS.computeIfAbsent(arrow, ignored -> new Shot());
        }
    }

    public static List<Entity> filterTargets(List<Entity> targets, EntityArrow arrow) {
        if (piercing(arrow) == 0) return targets;
        Shot shot = shot(arrow);
        if (shot.pierced.isEmpty()) return targets;
        List<Entity> remaining = new ArrayList<>(targets);
        remaining.removeIf(entity -> shot.pierced.contains(entity.getUniqueID()));
        return remaining;
    }

    public static boolean beginHit(EntityArrow arrow, RayTraceResult hit) {
        if (!isCrossbow(arrow)) return true;
        Shot shot = shot(arrow);
        if (hit.entityHit == null) {
            shot.pierced.clear();
            return true;
        }
        if (piercing(arrow) > 0) {
            if (shot.pierced.contains(hit.entityHit.getUniqueID())) return false;
            if (shot.pierced.size() >= piercing(arrow) + 1) {
                arrow.setDead();
                return false;
            }
            shot.pierced.add(hit.entityHit.getUniqueID());
        }
        shot.targetWasAlive = hit.entityHit.isEntityAlive();
        return true;
    }

    public static void discardAfterHit(EntityArrow arrow) {
        if (piercing(arrow) == 0) arrow.setDead();
    }

    public static void finishHit(EntityArrow arrow, RayTraceResult hit) {
        if (!isCrossbow(arrow) || arrow.world.isRemote || hit.entityHit == null
                || !(arrow.shootingEntity instanceof EntityPlayerMP)) return;
        Shot shot = shot(arrow);
        Entity target = hit.entityHit;
        if (!shot.targetWasAlive || target.isEntityAlive() || !shot.killed.add(target.getUniqueID())) return;
        ResourceLocation type = EntityList.getKey(target);
        if (type != null) shot.killedTypes.add(type);
        if (target instanceof EntityPhantom) shot.killedPhantoms++;
        EntityPlayerMP player = (EntityPlayerMP) arrow.shootingEntity;
        if (shot.killedPhantoms >= 2) FFDAdvancements.TWO_BIRDS_ONE_ARROW.trigger(player);
        if (shot.killedTypes.size() >= 5) FFDAdvancements.ARBALISTIC.trigger(player);
    }

    public static void playHitSound(EntityArrow arrow, SoundEvent sound, float volume, float pitch) {
        arrow.playSound(isCrossbow(arrow) && sound == SoundEvents.ENTITY_ARROW_HIT
                ? FFDSounds.CROSSBOW_HIT : sound, volume, pitch);
    }

    public static void tick(EntityArrow arrow) {
        if (!isCrossbow(arrow) || arrow.world.isRemote || arrow.shootingEntity != null
                || !(arrow.world instanceof WorldServer)) return;
        Shot shot = shot(arrow);
        if (shot.shooter != null) {
            arrow.shootingEntity = ((WorldServer) arrow.world).getEntityFromUuid(shot.shooter);
        }
    }

    public static void write(EntityArrow arrow, NBTTagCompound compound) {
        if (!isCrossbow(arrow)) return;
        Shot shot = shot(arrow);
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("Piercing", piercing(arrow));
        if (shot.shooter != null) data.setUniqueId("Shooter", shot.shooter);
        data.setTag("Pierced", strings(shot.pierced));
        data.setTag("Killed", strings(shot.killed));
        data.setTag("KilledTypes", strings(shot.killedTypes));
        data.setInteger("KilledPhantoms", shot.killedPhantoms);
        compound.setTag(TAG, data);
    }

    public static void read(EntityArrow arrow, NBTTagCompound compound) {
        SHOTS.remove(arrow);
        if (!compound.hasKey(TAG, 10)) {
            arrow.getDataManager().set(CROSSBOW, (byte) 0);
            return;
        }
        NBTTagCompound data = compound.getCompoundTag(TAG);
        configure(arrow, data.getInteger("Piercing"));
        Shot shot = shot(arrow);
        if (data.hasUniqueId("Shooter")) shot.shooter = data.getUniqueId("Shooter");
        readUuids(data.getTagList("Pierced", 8), shot.pierced);
        readUuids(data.getTagList("Killed", 8), shot.killed);
        NBTTagList types = data.getTagList("KilledTypes", 8);
        for (int index = 0; index < types.tagCount() && index < 5; index++) {
            shot.killedTypes.add(new ResourceLocation(types.getStringTagAt(index)));
        }
        shot.killedPhantoms = Math.max(0, Math.min(5, data.getInteger("KilledPhantoms")));
    }

    private static NBTTagList strings(Iterable<?> values) {
        NBTTagList list = new NBTTagList();
        for (Object value : values) list.appendTag(new NBTTagString(value.toString()));
        return list;
    }

    private static void readUuids(NBTTagList list, Set<UUID> values) {
        for (int index = 0; index < list.tagCount() && index < 5; index++) {
            try {
                values.add(UUID.fromString(list.getStringTagAt(index)));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private static final class Shot {
        private final Set<UUID> pierced = new HashSet<>();
        private final Set<UUID> killed = new HashSet<>();
        private final Set<ResourceLocation> killedTypes = new HashSet<>();
        private UUID shooter;
        private int killedPhantoms;
        private boolean targetWasAlive;
    }
}
