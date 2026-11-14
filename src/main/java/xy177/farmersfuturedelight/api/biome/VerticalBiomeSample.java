package xy177.farmersfuturedelight.api.biome;

public interface VerticalBiomeSample {
    default int getDimension() {
        return 0;
    }

    long getWorldSeed();

    int getX();

    int getY();

    int getZ();

    int getSurfaceY();

    double getTemperature();

    double getHumidity();

    double getContinentalness();

    double getErosion();

    double getWeirdness();

    double getDepth();
}
