package xy177.farmersfuturedelight.common.biome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeHell;

import xy177.farmersfuturedelight.FarmerFutureDelight;

public class BiomeWarpedForest extends BiomeHell {
    public BiomeWarpedForest() {
        super(new Biome.BiomeProperties("Warped Forest")
                .setTemperature(2.0F)
                .setRainfall(0.0F)
                .setRainDisabled());
        setRegistryName(FarmerFutureDelight.MODID, "warped_forest");
    }
}
