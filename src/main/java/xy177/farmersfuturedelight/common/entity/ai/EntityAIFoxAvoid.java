package xy177.farmersfuturedelight.common.entity.ai;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ai.RandomPositionGenerator;
import xy177.farmersfuturedelight.common.entity.EntityFox;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityAIFoxAvoid extends EntityAIBase {
    private final EntityFox fox;
    private final Class<? extends EntityLivingBase> targetClass;
    private final double distance;
    private final double farSpeed;
    private final double nearSpeed;
    private final Predicate<EntityLivingBase> targetFilter;
    private EntityLivingBase target;
    private Path path;

    public EntityAIFoxAvoid(EntityFox fox, Class<? extends EntityLivingBase> targetClass,
                            double distance, double farSpeed, double nearSpeed,
                            Predicate<EntityLivingBase> targetFilter) {
        this.fox = fox;
        this.targetClass = targetClass;
        this.distance = distance;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        this.targetFilter = targetFilter;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (fox.isDefending() || fox.isSleeping()) {
            return false;
        }
        target = null;
        double nearestDistance = distance * distance;
        AxisAlignedBB bounds = fox.getEntityBoundingBox().grow(distance, 6.0D, distance);
        List<? extends EntityLivingBase> candidates = fox.world.getEntitiesWithinAABB(
                targetClass, bounds);
        for (EntityLivingBase candidate : candidates) {
            double candidateDistance = fox.getDistanceSq(candidate);
            if (!candidate.isEntityAlive() || candidateDistance > nearestDistance
                    || !fox.getEntitySenses().canSee(candidate) || !targetFilter.test(candidate)) {
                continue;
            }
            target = candidate;
            nearestDistance = candidateDistance;
        }
        if (target == null) {
            return false;
        }
        Vec3d away = RandomPositionGenerator.findRandomTargetBlockAwayFrom(fox, 16, 7,
                new Vec3d(target.posX, target.posY, target.posZ));
        if (away == null || target.getDistanceSq(away.x, away.y, away.z)
                < target.getDistanceSq(fox)) {
            return false;
        }
        path = fox.getNavigator().getPathToXYZ(away.x, away.y, away.z);
        return path != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return target != null && !fox.isDefending() && target.isEntityAlive()
                && !fox.getNavigator().noPath()
                && fox.getDistanceSq(target) < distance * distance * 2.0D
                && targetFilter.test(target);
    }

    @Override
    public void startExecuting() {
        fox.setSleeping(false);
        fox.setSitting(false);
        fox.getNavigator().setPath(path, farSpeed);
        if (targetClass == EntityPlayer.class) {
            fox.playFoxSound(FFDSounds.FOX_SCREECH);
        }
    }

    @Override
    public void updateTask() {
        fox.getNavigator().setSpeed(fox.getDistanceSq(target) < 49.0D
                ? nearSpeed : farSpeed);
    }

    @Override
    public void resetTask() {
        target = null;
        path = null;
    }
}
