package xy177.farmersfuturedelight.common;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityIllusionIllager;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.entity.EntityRavager;
import xy177.farmersfuturedelight.common.entity.EntityWanderingTrader;
import xy177.farmersfuturedelight.common.registry.FFDEntities;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDWanderingTraderEvents {
    private FFDWanderingTraderEvents() {
    }

    @SubscribeEvent
    public static void onTraderTravel(EntityTravelToDimensionEvent event) {
        if (!event.getEntity().world.isRemote && event.getEntity() instanceof EntityWanderingTrader) {
            ((EntityWanderingTrader) event.getEntity()).setCustomer(null);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !FFDEntities.isLocalWanderingTraderEnabled()
                || !(event.getEntity() instanceof EntityLiving)) return;
        EntityLiving mob = (EntityLiving) event.getEntity();
        boolean zombie = mob instanceof EntityZombie && !(mob instanceof EntityPigZombie);
        if (!zombie && !(mob instanceof EntityEvoker) && !(mob instanceof EntityVindicator)
                && !(mob instanceof EntityIllusionIllager) && !(mob instanceof EntityPillager)
                && !(mob instanceof EntityRavager)) return;
        for (net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry entry : mob.targetTasks.taskEntries) {
            if (entry.action instanceof TraderTargetGoal) return;
        }
        mob.targetTasks.addTask(mob instanceof EntityRavager ? 4 : 3, new TraderTargetGoal(mob));
    }

    private static final class TraderTargetGoal extends EntityAINearestAttackableTarget<EntityWanderingTrader> {
        private TraderTargetGoal(EntityLiving mob) {
            super((net.minecraft.entity.EntityCreature) mob, EntityWanderingTrader.class,
                    mob instanceof EntityVindicator || mob instanceof EntityRavager);
        }
    }
}
