package xy177.farmersfuturedelight.common.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.common.entity.EntityFox;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityAIFoxSleep extends EntityAIBase {
    private final EntityFox fox;
    private int checkCooldown;

    public EntityAIFoxSleep(EntityFox fox) {
        this.fox = fox;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (checkCooldown-- > 0 || !fox.world.isDaytime() || fox.isInWater()
                || fox.getAttackTarget() != null || !fox.onGround) {
            return false;
        }
        checkCooldown = 100 + fox.getRNG().nextInt(100);
        BlockPos above = fox.getPosition().up();
        return !fox.world.canSeeSky(above) && !nearbyThreat();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return fox.world.isDaytime() && !fox.isInWater() && fox.getAttackTarget() == null
                && !nearbyThreat();
    }

    @Override
    public void startExecuting() {
        fox.setSleeping(true);
        fox.setSitting(false);
        fox.getNavigator().clearPath();
        fox.playFoxSound(FFDSounds.FOX_SLEEP);
    }

    @Override
    public void resetTask() {
        fox.setSleeping(false);
    }

    private boolean nearbyThreat() {
        AxisAlignedBB bounds = fox.getEntityBoundingBox().grow(12.0D, 6.0D, 12.0D);
        for (EntityPlayer player : fox.world.getEntitiesWithinAABB(EntityPlayer.class, bounds)) {
            if (!fox.trusts(player) && !player.isCreative() && !player.isSpectator()) {
                return true;
            }
        }
        return false;
    }
}
