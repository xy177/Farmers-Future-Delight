package xy177.farmersfuturedelight.common.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.item.ItemCrossbow;

public class EntityAIPillagerCrossbowAttack extends EntityAIBase {
    private final EntityPillager pillager;
    private final double moveSpeed;
    private final float rangeSquared;
    private CrossbowState state = CrossbowState.UNCHARGED;
    private int attackDelay;
    private int visibilityTicks;
    private int updatePathDelay;

    private enum CrossbowState {
        UNCHARGED, CHARGING, CHARGED, READY_TO_ATTACK
    }

    public EntityAIPillagerCrossbowAttack(EntityPillager pillager, double moveSpeed,
                                          float range) {
        this.pillager = pillager;
        this.moveSpeed = moveSpeed;
        this.rangeSquared = range * range;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase target = pillager.getAttackTarget();
        return target != null && target.isEntityAlive() && holdsCrossbow();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }

    @Override
    public void startExecuting() {
        state = CrossbowState.UNCHARGED;
        visibilityTicks = 0;
        attackDelay = 0;
        updatePathDelay = 0;
        pillager.setChargingCrossbow(false);
    }

    @Override
    public void resetTask() {
        pillager.setChargingCrossbow(false);
        pillager.setAttackTarget(null);
        if (pillager.isHandActive()) {
            pillager.resetActiveHand();
        }
        pillager.getNavigator().clearPath();
        state = CrossbowState.UNCHARGED;
        visibilityTicks = 0;
        attackDelay = 0;
        updatePathDelay = 0;
    }

    @Override
    public void updateTask() {
        EntityLivingBase target = pillager.getAttackTarget();
        if (target == null) {
            return;
        }
        boolean visible = pillager.getEntitySenses().canSee(target);
        if (visible != (visibilityTicks > 0)) {
            visibilityTicks = 0;
        }
        visibilityTicks += visible ? 1 : -1;
        double distanceSquared = pillager.getDistanceSq(target);
        boolean needsToMove = (distanceSquared > rangeSquared || visibilityTicks < 5)
                && attackDelay == 0;
        if (needsToMove) {
            if (--updatePathDelay <= 0) {
                pillager.getNavigator().tryMoveToEntityLiving(target,
                        state == CrossbowState.UNCHARGED ? moveSpeed : moveSpeed * 0.5D);
                updatePathDelay = 20 + pillager.getRNG().nextInt(21);
            }
        } else {
            updatePathDelay = 0;
            pillager.getNavigator().clearPath();
        }
        pillager.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        if (state == CrossbowState.UNCHARGED) {
            if (!needsToMove) {
                pillager.setActiveHand(pillager.getHeldItemMainhand().getItem() instanceof ItemCrossbow
                        ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
                state = CrossbowState.CHARGING;
                pillager.setChargingCrossbow(true);
            }
        } else if (state == CrossbowState.CHARGING) {
            if (!pillager.isHandActive()) {
                state = CrossbowState.UNCHARGED;
                pillager.setChargingCrossbow(false);
            } else {
                ItemStack weapon = pillager.getActiveItemStack();
                if (ItemCrossbow.isCharged(weapon)) {
                    pillager.stopActiveHand();
                    state = CrossbowState.CHARGED;
                    attackDelay = 20 + pillager.getRNG().nextInt(20);
                    pillager.setChargingCrossbow(false);
                }
            }
        } else if (state == CrossbowState.CHARGED) {
            if (--attackDelay == 0) {
                state = CrossbowState.READY_TO_ATTACK;
            }
        } else if (visible) {
            pillager.fireCrossbow();
            state = CrossbowState.UNCHARGED;
        }
    }

    private boolean holdsCrossbow() {
        return pillager.getHeldItemMainhand().getItem() instanceof ItemCrossbow
                || pillager.getHeldItemOffhand().getItem() instanceof ItemCrossbow;
    }
}
