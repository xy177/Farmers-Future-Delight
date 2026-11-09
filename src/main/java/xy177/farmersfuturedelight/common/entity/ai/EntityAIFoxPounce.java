package xy177.farmersfuturedelight.common.entity.ai;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityFox;

public class EntityAIFoxPounce extends EntityAIBase {
    private final EntityFox fox;
    @Nullable
    private EntityLivingBase target;
    private int ticks;
    private boolean launched;

    public EntityAIFoxPounce(EntityFox fox) {
        this.fox = fox;
        setMutexBits(5);
    }

    @Override
    public boolean shouldExecute() {
        target = fox.getAttackTarget();
        return target != null && target.isEntityAlive() && fox.onGround && !fox.isInWater()
                && fox.getDistanceSq(target) > 4.0D && fox.getDistanceSq(target) < 144.0D
                && fox.getRNG().nextInt(10) == 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return target != null && target.isEntityAlive() && fox.getAttackTarget() == target
                && ticks < 40 && (!launched || !fox.onGround);
    }

    @Override
    public void startExecuting() {
        ticks = 0;
        launched = false;
        fox.setSleeping(false);
        fox.setSitting(false);
        fox.setCrouching(true);
        fox.setInterested(true);
        fox.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (target == null) {
            return;
        }
        ticks++;
        fox.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        if (launched && fox.getDistanceSq(target) <= 4.0D) {
            fox.attackEntityAsMob(target);
        }
        if (!launched && (ticks >= 20 || fox.getDistanceSq(target) < 9.0D)) {
            double x = target.posX - fox.posX;
            double z = target.posZ - fox.posZ;
            double length = Math.sqrt(x * x + z * z);
            if (length > 1.0E-5D) {
                fox.motionX = x / length * 0.8D;
                fox.motionZ = z / length * 0.8D;
                fox.motionY = 0.9D;
                fox.velocityChanged = true;
                fox.setPouncing(true);
                launched = true;
            }
        } else if (launched && fox.onGround) {
            resetTask();
        }
    }

    @Override
    public void resetTask() {
        fox.setPouncing(false);
        fox.setCrouching(false);
        fox.setInterested(false);
        target = null;
        ticks = 0;
        launched = false;
    }
}
