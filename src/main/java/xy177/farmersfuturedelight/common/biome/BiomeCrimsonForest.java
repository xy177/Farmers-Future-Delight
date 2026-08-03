package xy177.farmersfuturedelight.common.biome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeHell;

import xy177.farmersfuturedelight.FarmerFutureDelight;

public class BiomeCrimsonForest extends BiomeHell {
    public BiomeCrimsonForest() {
        super(new Biome.BiomeProperties("Crimson Forest")
                .setTemperature(2.0F)
                .setRainfall(0.0F)
                .setRainDisabled());
        setRegistryName(FarmerFutureDelight.MODID, "crimson_forest");
    }
}
