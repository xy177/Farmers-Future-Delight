package xy177.farmersfuturedelight.common.biome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeJungle;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class BiomeBambooJungle extends BiomeJungle {
    public BiomeBambooJungle() {
        super(false, new Biome.BiomeProperties("Bamboo Jungle")
                .setTemperature(0.95F)
                .setRainfall(0.9F)
                .setWaterColor(0x3F76E4));
        setRegistryName(FarmerFutureDelight.MODID, "bamboo_jungle");
    }
}
