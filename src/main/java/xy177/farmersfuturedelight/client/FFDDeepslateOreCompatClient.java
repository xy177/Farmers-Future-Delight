package xy177.farmersfuturedelight.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDDeepslateOreCompat;

public final class FFDDeepslateOreCompatClient {
    private FFDDeepslateOreCompatClient() {
    }

    public static void registerModels() {
        for (FFDDeepslateOreCompat.EntryView entry : FFDDeepslateOreCompat.clientEntries()) {
            ModelLoader.setCustomModelResourceLocation(entry.item(), 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":"
                            + entry.modelPath(), "inventory"));
            ModelLoader.setCustomStateMapper(entry.block(), block -> {
                Map<IBlockState, ModelResourceLocation> models = new HashMap<>();
                ModelResourceLocation location = new ModelResourceLocation(
                        FarmerFutureDelight.MODID + ":" + entry.modelPath(), "normal");
                for (IBlockState state : block.getBlockState().getValidStates()) {
                    models.put(state, location);
                }
                return models;
            });
        }
    }
}
