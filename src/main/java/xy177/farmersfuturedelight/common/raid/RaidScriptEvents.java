package xy177.farmersfuturedelight.common.raid;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.raid.RaidEventContext;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class RaidScriptEvents {
    private static final List<Listener> LISTENERS = new ArrayList<>();
    private static final List<Deferred> DEFERRED = new ArrayList<>();
    private static int dispatchDepth;

    private RaidScriptEvents() { }

    public interface Listener {
        void accept(EntityPlayerMP player, RaidEventContext context, String phase, int wave, String result);
    }

    public static synchronized void listen(Listener listener) { LISTENERS.add(listener); }
    public static boolean isDispatching() { return dispatchDepth > 0; }

    static synchronized void defer(WorldServer world, Runnable action) { DEFERRED.add(new Deferred(world, action)); }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        List<Deferred> pending;
        synchronized (RaidScriptEvents.class) {
            if (DEFERRED.isEmpty()) return;
            pending = new ArrayList<>(DEFERRED);
            DEFERRED.clear();
        }
        net.minecraft.server.MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        for (Deferred deferred : pending) {
            if (server == null || deferred.world.getMinecraftServer() != server
                    || server.getWorld(deferred.world.provider.getDimension()) != deferred.world) continue;
            try {
                deferred.action.run();
            } catch (RuntimeException exception) {
                FFDRaidManager.LOGGER.error("Deferred raid script operation failed", exception);
            }
        }
    }

    private static final class Deferred {
        private final WorldServer world;
        private final Runnable action;

        private Deferred(WorldServer world, Runnable action) { this.world = world; this.action = action; }
    }

    static void post(EntityPlayerMP player, RaidEventContext context, String phase, int wave, String result) {
        List<Listener> listeners;
        synchronized (RaidScriptEvents.class) { listeners = new ArrayList<>(LISTENERS); }
        dispatchDepth++;
        try {
            for (Listener listener : listeners) {
                try {
                    listener.accept(player, context, phase, wave, result);
                } catch (RuntimeException exception) {
                    FFDRaidManager.LOGGER.error("Raid script callback failed for {} / {} / {}", context.getEventId(), context.getInstanceId(), phase, exception);
                }
            }
        } finally {
            dispatchDepth--;
        }
    }
}
