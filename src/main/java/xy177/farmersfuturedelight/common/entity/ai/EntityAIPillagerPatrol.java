package xy177.farmersfuturedelight.common.entity.ai;

import java.util.List;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.raid.FFDRaidCaptains;
import xy177.farmersfuturedelight.common.raid.FFDRaidManager;

public final class EntityAIPillagerPatrol extends EntityAIBase {
    private static final String PATROL_TAG = "farmers_future_delight:configured_patrol";
    private final EntityLiving pillager;
    private long cooldownUntil = -1L;

    public EntityAIPillagerPatrol(EntityPillager pillager) {
        this((EntityLiving) pillager);
    }

    public EntityAIPillagerPatrol(EntityLiving pillager) {
        this.pillager = pillager;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return isPatrolling(pillager) && getPatrolTarget(pillager) != null
                && pillager.getAttackTarget() == null && pillager.getControllingPassenger() == null
                && !FFDRaidManager.isParticipant(pillager)
                && pillager.world.getTotalWorldTime() >= cooldownUntil;
    }

    @Override
    public void updateTask() {
        if (!pillager.getNavigator().noPath()) return;
        List<EntityLiving> companions = pillager.world.getEntitiesWithinAABB(EntityLiving.class,
                pillager.getEntityBoundingBox().grow(16.0D), candidate -> candidate != pillager
                        && (candidate instanceof EntityPillager || isPatrolling(candidate))
                        && candidate.isEntityAlive() && !FFDRaidManager.isParticipant(candidate));
        if (companions.isEmpty()) {
            setPatrolling(pillager, false);
            return;
        }
        boolean leader = FFDRaidCaptains.isLeader(pillager);
        BlockPos patrolTarget = getPatrolTarget(pillager);
        if (leader && pillager.getDistanceSqToCenter(patrolTarget) < 100.0D) {
            findPatrolTarget(pillager);
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
            for (EntityLiving companion : companions) setPatrolTarget(companion, pathTarget);
        }
    }

    public static boolean isPatrolling(EntityLiving entity) {
        return entity instanceof EntityPillager ? ((EntityPillager) entity).isPatrolling()
                : entity.getEntityData().getCompoundTag(PATROL_TAG).getBoolean("active");
    }

    public static BlockPos getPatrolTarget(EntityLiving entity) {
        if (entity instanceof EntityPillager) return ((EntityPillager) entity).getPatrolTarget();
        NBTTagCompound data = entity.getEntityData().getCompoundTag(PATROL_TAG);
        return data.hasKey("target", 4) ? BlockPos.fromLong(data.getLong("target")) : null;
    }

    public static void setPatrolTarget(EntityLiving entity, BlockPos target) {
        if (entity instanceof EntityPillager) {
            ((EntityPillager) entity).setPatrolTarget(target);
        } else {
            NBTTagCompound data = entity.getEntityData().getCompoundTag(PATROL_TAG);
            data.setLong("target", target.toLong());
            data.setBoolean("active", true);
            entity.getEntityData().setTag(PATROL_TAG, data);
        }
    }

    public static void findPatrolTarget(EntityLiving entity) {
        if (entity instanceof EntityPillager) ((EntityPillager) entity).findPatrolTarget();
        else setPatrolTarget(entity, entity.getPosition().add(-500 + entity.getRNG().nextInt(1000), 0,
                -500 + entity.getRNG().nextInt(1000)));
    }

    private static void setPatrolling(EntityLiving entity, boolean active) {
        if (entity instanceof EntityPillager) ((EntityPillager) entity).setPatrolling(active);
        else {
            NBTTagCompound data = entity.getEntityData().getCompoundTag(PATROL_TAG);
            data.setBoolean("active", active);
            entity.getEntityData().setTag(PATROL_TAG, data);
        }
    }
}
