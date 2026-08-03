package xy177.farmersfuturedelight.common.worldgen;

import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDWorldgenEvents {
    private FFDWorldgenEvents() {
    }

    @SubscribeEvent
    public static void replaceCaveGenerator(InitMapGenEvent event) {
        if (event.getType() != InitMapGenEvent.EventType.CAVE
                || !FFDItems.isLushCaveEnabled() && !FFDItems.isGlowLichenEnabled()
                || event.getNewGen() instanceof MapGenLushCaves) {
            return;
        }
        event.setNewGen(new MapGenLushCaves(event.getNewGen()));
    }
}
