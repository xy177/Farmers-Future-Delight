package xy177.farmersfuturedelight.common.entity.ai;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerReputation;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRoutines;
import xy177.farmersfuturedelight.common.registry.FFDWorkstationLifecycle;

public final class EntityAIVillagerSocial extends EntityAIBase {
    private static final Map<EntityVillager, WeakReference<EntityVillager>> CHASING = new WeakHashMap<>();
    private final EntityVillager villager;
    private EntityVillager target;
    private Vec3d escape;
    private FFDVillagerRoutines.Activity activity;
    private long expires;
    private int nextPath;

    public EntityAIVillagerSocial(EntityVillager villager) {
        this.villager = villager;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        activity = FFDVillagerRoutines.getActivity(villager);
        if (!available(villager) || !villager.getNavigator().noPath()) return false;
        boolean playing = villager.isChild() && activity == FFDVillagerRoutines.Activity.PLAY;
        if (!playing && activity != FFDVillagerRoutines.Activity.IDLE
                && activity != FFDVillagerRoutines.Activity.MEET) return false;
        if (villager.getRNG().nextInt(playing ? 10 : activity == FFDVillagerRoutines.Activity.MEET ? 100 : 10) != 0) return false;
        BlockPos meeting = FFDVillagerRoutines.getMeeting(villager);
        if (!playing && activity == FFDVillagerRoutines.Activity.MEET
                && (meeting == null || villager.getDistanceSqToCenter(meeting) >= 16.0D)) return false;
        double range = playing ? 16.0D : 8.0D;
        List<EntityVillager> peers = villager.world.getEntitiesWithinAABB(EntityVillager.class,
                villager.getEntityBoundingBox().grow(range), other -> other != villager
                        && other.getClass() == EntityVillager.class && available(other)
                        && other.isChild() == villager.isChild() && villager.canEntityBeSeen(other));
        if (!playing && activity == FFDVillagerRoutines.Activity.MEET) {
            peers.removeIf(other -> villager.getDistanceSq(other) > 32.0D);
        }
        if (peers.isEmpty() || !FFDVillagerHomes.canNavigate(villager, villager.getPosition())) return false;
        peers.sort(Comparator.comparingDouble(villager::getDistanceSq));
        target = null;
        escape = null;
        if (playing) {
            for (EntityVillager peer : peers) {
                if (chasedBy(peer) != villager) continue;
                for (int attempt = 0; attempt < 10; attempt++) {
                    Vec3d candidate = RandomPositionGenerator.findRandomTarget(villager, 20, 8);
                    if (candidate != null && nearVillage(new BlockPos(candidate))) {
                        escape = candidate;
                        return true;
                    }
                }
                return false;
            }
            Map<EntityVillager, Integer> chasers = new HashMap<>();
            for (EntityVillager peer : peers) {
                EntityVillager chased = chasedBy(peer);
                if (chased != null && chased != villager && peers.contains(chased)) {
                    chasers.merge(chased, 1, Integer::sum);
                }
            }
            target = chasers.entrySet().stream().filter(entry -> entry.getValue() <= 5)
                    .min(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).orElse(null);
        }
        if (target == null) target = peers.get(0);
        return true;
    }

    private static EntityVillager chasedBy(EntityVillager chaser) {
        WeakReference<EntityVillager> reference = CHASING.get(chaser);
        return reference == null ? null : reference.get();
    }

    private boolean nearVillage(BlockPos position) {
        BlockPos[] remembered = {FFDVillagerHomes.getHome(villager), FFDWorkstationLifecycle.getWorkstation(villager),
                FFDVillagerRoutines.getMeeting(villager)};
        for (BlockPos poi : remembered) {
            if (poi == null) continue;
            int distance = Math.abs((position.getX() >> 4) - (poi.getX() >> 4))
                    + Math.abs((position.getY() >> 4) - (poi.getY() >> 4))
                    + Math.abs((position.getZ() >> 4) - (poi.getZ() >> 4));
            if (distance <= 1) return true;
        }
        return villager.world.getVillageCollection().getNearestVillage(position, 0) != null;
    }

    private static boolean available(EntityVillager entity) {
        return entity.isEntityAlive() && !entity.isAIDisabled() && !entity.isTrading()
                && !entity.isRiding() && !entity.isInWater() && !FFDVillagerHomes.isSleeping(entity)
                && !FFDVillagerRoutines.isPanicking(entity);
    }

    @Override
    public void startExecuting() {
        expires = villager.world.getTotalWorldTime() + (villager.isChild() ? 120L : 60L);
        nextPath = 0;
        if (villager.isChild() && target != null) CHASING.put(villager, new WeakReference<>(target));
    }

    @Override
    public boolean shouldContinueExecuting() {
        return villager.world.getTotalWorldTime() <= expires && available(villager)
                && activity == FFDVillagerRoutines.getActivity(villager)
                && (escape != null ? !villager.getNavigator().noPath()
                        : target != null && available(target) && target.world == villager.world
                        && villager.getDistanceSq(target) <= 256.0D && villager.canEntityBeSeen(target));
    }

    @Override
    public void updateTask() {
        if (target != null) {
            villager.getLookHelper().setLookPositionWithEntity(target, 10.0F, villager.getVerticalFaceSpeed());
            if (!villager.isChild()) FFDVillagerReputation.socialize(villager, target);
        }
        if (--nextPath > 0) return;
        nextPath = 10;
        if (escape != null) {
            villager.getNavigator().tryMoveToXYZ(escape.x, escape.y, escape.z, 0.6D);
        } else if (target != null) {
            double range = villager.isChild() ? 1.0D : 2.0D;
            if (villager.getDistanceSq(target) > range * range) {
                villager.getNavigator().tryMoveToEntityLiving(target, villager.isChild() ? 0.6D : 0.5D);
            } else villager.getNavigator().clearPath();
        }
    }

    @Override
    public void resetTask() {
        CHASING.remove(villager);
        villager.getNavigator().clearPath();
        target = null;
        escape = null;
    }
}
