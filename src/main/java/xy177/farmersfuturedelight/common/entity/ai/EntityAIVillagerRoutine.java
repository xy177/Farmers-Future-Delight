package xy177.farmersfuturedelight.common.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRoutines;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRoutines.Activity;
import xy177.farmersfuturedelight.common.registry.FFDWorkstationLifecycle;

public final class EntityAIVillagerRoutine extends EntityAIBase {
    private final EntityVillager villager;
    private BlockPos target;
    private Activity activity;
    private int nextPath;
    private int remaining;
    private long nextWorkVisit;
    private boolean seekingWork;

    public EntityAIVillagerRoutine(EntityVillager villager) {
        this.villager = villager;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (villager.isTrading() || villager.isAIDisabled() || villager.isInWater() || villager.isRiding()) return false;
        activity = FFDVillagerRoutines.getActivity(villager);
        seekingWork = (activity == Activity.IDLE || activity == Activity.WORK)
                && FFDWorkstationLifecycle.getPotentialWorkstation(villager) != null;
        if (activity == Activity.WORK && villager.world.getTotalWorldTime() < nextWorkVisit) return false;
        target = activity == Activity.REST ? FFDVillagerHomes.getHome(villager)
                : seekingWork ? FFDWorkstationLifecycle.getPotentialWorkstation(villager)
                : activity == Activity.WORK ? FFDWorkstationLifecycle.getWorkstation(villager)
                : activity == Activity.MEET ? FFDVillagerRoutines.getMeeting(villager) : null;
        if (target == null || !villager.world.isBlockLoaded(target)) return false;
        double range = seekingWork ? 2.0D : activity == Activity.MEET ? 6.0D
                : activity == Activity.WORK ? 1.73D : 0.0D;
        return activity == Activity.REST || (seekingWork
                ? villager.getDistanceSqToCenter(target) >= range * range
                : villager.getDistanceSqToCenter(target) > range * range);
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (activity != Activity.WORK && !seekingWork && --remaining <= 0
                || villager.isTrading() || !villager.isEntityAlive() || villager.isInWater()
                || villager.isRiding() || activity != FFDVillagerRoutines.getActivity(villager)) return false;
        BlockPos current = activity == Activity.REST ? FFDVillagerHomes.getHome(villager)
                : seekingWork ? FFDWorkstationLifecycle.getPotentialWorkstation(villager)
                : activity == Activity.WORK ? FFDWorkstationLifecycle.getWorkstation(villager)
                : FFDVillagerRoutines.getMeeting(villager);
        if (!target.equals(current) || !villager.world.isBlockLoaded(target)) return false;
        return activity == Activity.REST || (seekingWork
                ? villager.getDistanceSqToCenter(target) >= 4.0D
                : villager.getDistanceSqToCenter(target)
                        > (activity == Activity.MEET ? 36.0D : 1.73D * 1.73D));
    }

    @Override
    public void startExecuting() {
        remaining = 1200;
        nextPath = 0;
    }

    @Override
    public void updateTask() {
        if (activity == Activity.REST && FFDVillagerHomes.isSleeping(villager)) return;
        if (activity == Activity.REST && FFDVillagerHomes.startSleeping(villager)) return;
        if ((activity == Activity.WORK || seekingWork)
                && !FFDWorkstationLifecycle.tickWorkstationTravel(villager, target)) return;
        if (--nextPath > 0 || !FFDVillagerHomes.canNavigate(villager, target)) return;
        nextPath = 40 + villager.getRNG().nextInt(40);
        if (activity == Activity.WORK || seekingWork) {
            villager.getNavigator().setPath(FFDWorkstationLifecycle.findWorkstationPath(villager, target), 0.5D);
            return;
        }
        villager.getNavigator().tryMoveToXYZ(target.getX() + 0.5D,
                target.getY(), target.getZ() + 0.5D, 0.5D);
    }

    @Override
    public void resetTask() {
        villager.getNavigator().clearPath();
        if (activity == Activity.REST && remaining <= 0 && target != null) {
            FFDVillagerHomes.abandonUnreachableHome(villager, target);
        }
        if (activity == Activity.WORK) nextWorkVisit = villager.world.getTotalWorldTime() + 300L;
        if (activity == Activity.REST && FFDVillagerRoutines.getActivity(villager) != Activity.REST) {
            FFDVillagerHomes.wake(villager);
        }
        target = null;
    }
}
