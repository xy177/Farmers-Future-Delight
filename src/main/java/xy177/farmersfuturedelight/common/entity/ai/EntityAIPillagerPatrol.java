package xy177.farmersfuturedelight.common.entity.ai;

import java.util.List;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.raid.FFDRaidCaptains;
import xy177.farmersfuturedelight.common.raid.FFDRaidManager;

public final class EntityAIPillagerPatrol extends EntityAIBase {
    private final EntityPillager pillager;
    private long cooldownUntil = -1L;

    public EntityAIPillagerPatrol(EntityPillager pillager) {
        this.pillager = pillager;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return pillager.isPatrolling() && pillager.getPatrolTarget() != null
                && pillager.getAttackTarget() == null && pillager.getControllingPassenger() == null
                && !FFDRaidManager.isParticipant(pillager)
                && pillager.world.getTotalWorldTime() >= cooldownUntil;
    }

    @Override
    public void updateTask() {
        if (!pillager.getNavigator().noPath()) return;
        List<EntityPillager> companions = pillager.world.getEntitiesWithinAABB(EntityPillager.class,
                pillager.getEntityBoundingBox().grow(16.0D), candidate -> candidate != pillager
                        && candidate.isEntityAlive() && !FFDRaidManager.isParticipant(candidate));
        if (companions.isEmpty()) {
            pillager.setPatrolling(false);
            return;
        }
        boolean leader = FFDRaidCaptains.isLeader(pillager);
        BlockPos patrolTarget = pillager.getPatrolTarget();
        if (leader && pillager.getDistanceSqToCenter(patrolTarget) < 100.0D) {
            pillager.findPatrolTarget();
            return;
        }
        Vec3d destination = new Vec3d(patrolTarget.getX() + 0.5D,
                patrolTarget.getY(), patrolTarget.getZ() + 0.5D);
        Vec3d current = pillager.getPositionVector();
        Vec3d offset = current.subtract(destination);
        destination = offset.rotateYaw(90.0F).scale(0.4D).add(destination);
        Vec3d movement = destination.subtract(current).normalize().scale(10.0D).add(current);
        BlockPos column = new BlockPos(movement);
        if (!pillager.world.isBlockLoaded(column)) return;
        BlockPos pathTarget = pillager.world.getTopSolidOrLiquidBlock(column);
        if (!pillager.getNavigator().tryMoveToXYZ(pathTarget.getX(), pathTarget.getY(),
                pathTarget.getZ(), leader ? 0.595D : 0.7D)) {
            BlockPos alternative = pillager.getPosition().add(-8 + pillager.getRNG().nextInt(16),
                    0, -8 + pillager.getRNG().nextInt(16));
            if (pillager.world.isBlockLoaded(alternative)) {
                BlockPos randomTarget = pillager.world.getTopSolidOrLiquidBlock(alternative);
                pillager.getNavigator().tryMoveToXYZ(randomTarget.getX(), randomTarget.getY(),
                        randomTarget.getZ(), 0.7D);
            }
            cooldownUntil = pillager.world.getTotalWorldTime() + 200L;
        } else if (leader) {
            for (EntityPillager companion : companions) companion.setPatrolTarget(pathTarget);
        }
    }
}
