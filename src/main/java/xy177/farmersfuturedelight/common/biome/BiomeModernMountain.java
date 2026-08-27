package xy177.farmersfuturedelight.common.biome;

import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.world.biome.Biome;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class BiomeModernMountain extends Biome {
    private final int skyColor;
    private final int modernWaterColor;

    public BiomeModernMountain(String registryName, String displayName,
                               float temperature, float rainfall,
                               int skyColor, int waterColor, boolean snow,
                               SpawnProfile spawnProfile) {
        super(properties(displayName, temperature, rainfall, waterColor, snow));
        this.skyColor = skyColor;
        this.modernWaterColor = waterColor;
        setRegistryName(FarmerFutureDelight.MODID, registryName);
        configureCreatureSpawns(spawnProfile);
    }

    private static BiomeProperties properties(String name, float temperature,
                                              float rainfall, int waterColor,
                                              boolean snow) {
        BiomeProperties properties = new BiomeProperties(name)
                .setTemperature(temperature)
                .setRainfall(rainfall)
                .setWaterColor(0xFFFFFF)
                .setBaseHeight(0.1F)
                .setHeightVariation(0.2F);
        return snow ? properties.setSnowEnabled() : properties;
    }

    public int getModernWaterColor() {
        return modernWaterColor;
    }

    private void configureCreatureSpawns(SpawnProfile profile) {
        spawnableCreatureList.clear();
        switch (profile) {
            case MEADOW:
                spawnableCreatureList.add(new SpawnListEntry(EntityDonkey.class, 1, 1, 2));
                spawnableCreatureList.add(new SpawnListEntry(EntityRabbit.class, 2, 2, 6));
                spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 2, 2, 4));
                break;
            case GROVE:
                spawnableCreatureList.add(new SpawnListEntry(EntityWolf.class, 1, 1, 1));
                spawnableCreatureList.add(new SpawnListEntry(EntityRabbit.class, 8, 2, 3));
                break;
            case SNOWY_SLOPES:
                spawnableCreatureList.add(new SpawnListEntry(EntityRabbit.class, 4, 2, 3));
                break;
            default:
                break;
        }
    }

    @Override
    public int getSkyColorByTemp(float currentTemperature) {
        return skyColor;
    }

    public enum SpawnProfile {
        MEADOW,
        GROVE,
        SNOWY_SLOPES,
        EMPTY
    }
}
