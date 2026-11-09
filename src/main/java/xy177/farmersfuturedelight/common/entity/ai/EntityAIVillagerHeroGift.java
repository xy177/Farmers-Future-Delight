package xy177.farmersfuturedelight.common.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import xy177.farmersfuturedelight.common.raid.FFDRaidVillagers;
import xy177.farmersfuturedelight.common.registry.FFDRaidContent;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRoutines;

public final class EntityAIVillagerHeroGift extends EntityAIBase {
    private final EntityVillager villager;
    private EntityPlayer hero;
    private int cooldown = 600;
    private long lastEligibleTick = -1L;
    private long started;
    private boolean given;

    public EntityAIVillagerHeroGift(EntityVillager villager) {
        this.villager = villager;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        hero = nearestVisiblePlayer();
        FFDVillagerRoutines.Activity activity = FFDVillagerRoutines.getActivity(villager);
        if (!available() || hero == null || !hero.isPotionActive(FFDRaidContent.HERO_OF_THE_VILLAGE)
                || activity != FFDVillagerRoutines.Activity.IDLE
                && activity != FFDVillagerRoutines.Activity.WORK
                && activity != FFDVillagerRoutines.Activity.MEET) {
            lastEligibleTick = -1L;
            return false;
        }
        long now = villager.world.getTotalWorldTime();
        cooldown -= lastEligibleTick < 0 ? 1 : (int) Math.max(1L, Math.min(3L, now - lastEligibleTick));
        lastEligibleTick = now;
        return cooldown <= 0;
    }

    private EntityPlayer nearestVisiblePlayer() {
        EntityPlayer nearest = null;
        double distance = 16.0D * 16.0D;
        for (EntityPlayer player : villager.world.playerEntities) {
            double candidate = villager.getDistanceSq(player);
            if (player.isSpectator() || !player.isEntityAlive() || candidate >= distance
                    || !villager.canEntityBeSeen(player)) continue;
            nearest = player;
            distance = candidate;
        }
        return nearest;
    }

    private boolean available() {
        return !villager.isTrading() && !villager.isAIDisabled() && villager.isEntityAlive()
                && !villager.isRiding() && !FFDVillagerHomes.isSleeping(villager)
                && !FFDVillagerRoutines.isPanicking(villager);
    }

    @Override
    public void startExecuting() {
        started = villager.world.getTotalWorldTime();
        given = false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        EntityPlayer nearest = nearestVisiblePlayer();
        return available() && !given && nearest != null && nearest == hero
                && hero.isPotionActive(FFDRaidContent.HERO_OF_THE_VILLAGE)
                && villager.world.getTotalWorldTime() <= started + 100L;
    }

    @Override
    public void updateTask() {
        villager.getLookHelper().setLookPositionWithEntity(hero, 30.0F, 30.0F);
        if (villager.getPosition().distanceSq(hero.getPosition()) < 25.0D) {
            if (villager.world.getTotalWorldTime() - started <= 20L) return;
            for (ItemStack gift : FFDRaidVillagers.gifts(villager, hero)) {
                if (gift.isEmpty()) continue;
                EntityItem dropped = new EntityItem(villager.world, villager.posX,
                        villager.posY + villager.getEyeHeight() - 0.3D, villager.posZ, gift);
                Vec3d velocity = hero.getPositionVector().subtract(villager.getPositionVector())
                        .normalize().scale(0.3D);
                dropped.motionX = velocity.x;
                dropped.motionY = velocity.y + 0.1D;
                dropped.motionZ = velocity.z;
                dropped.setDefaultPickupDelay();
                villager.world.spawnEntity(dropped);
            }
            given = true;
        } else if (villager.ticksExisted % 10 == 0
                && FFDVillagerHomes.canNavigate(villager, hero.getPosition())) {
            villager.getNavigator().tryMoveToEntityLiving(hero, 0.5D);
        }
    }

    @Override
    public void resetTask() {
        cooldown = 600 + villager.getRNG().nextInt(6001);
        lastEligibleTick = -1L;
        hero = null;
        villager.getNavigator().clearPath();
    }
}
