package xy177.farmersfuturedelight.client.model;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class ModernRabbitAnimationState {
    private static final Map<EntityRabbit, ModernRabbitAnimationState> STATES = new WeakHashMap<>();
    private int lastTick = -1;
    private int idleTimeout;
    int idleStart = -1;
    boolean hopping;

    private ModernRabbitAnimationState() {
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().world.isRemote && event.getEntityLiving().getClass() == EntityRabbit.class
                && !event.getEntityLiving().isChild()) get((EntityRabbit) event.getEntityLiving());
    }

    static ModernRabbitAnimationState get(EntityRabbit rabbit) {
        ModernRabbitAnimationState state = STATES.computeIfAbsent(rabbit, ignored -> new ModernRabbitAnimationState());
        if (state.lastTick != rabbit.ticksExisted) {
            if (state.idleTimeout <= 0 && !rabbit.getLeashed() && !rabbit.isAIDisabled()) {
                state.idleTimeout = rabbit.getRNG().nextInt(40) + 180;
                state.idleStart = rabbit.ticksExisted;
            } else if (rabbit.setJumpCompletion(0.0F) > 0.0F) {
                state.hopping = true;
                state.idleStart = -1;
            } else {
                --state.idleTimeout;
                state.hopping = false;
            }
            state.lastTick = rabbit.ticksExisted;
        }
        if (rabbit.getLeashed()) state.idleStart = -1;
        return state;
    }
}
