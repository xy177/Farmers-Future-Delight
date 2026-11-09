package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDRaidContent;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class RaidItemModels {
    private RaidItemModels() {
    }

    @SubscribeEvent
    public static void register(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(FFDRaidContent.OMINOUS_BOTTLE, 0,
                new ModelResourceLocation(FFDRaidContent.OMINOUS_BOTTLE.getRegistryName(), "inventory"));
    }
}
