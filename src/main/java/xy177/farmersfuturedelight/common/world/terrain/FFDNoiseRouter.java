package xy177.farmersfuturedelight.common.world.terrain;

public final class FFDNoiseRouter {
    public final FFDDensityFunction barrier;
    public final FFDDensityFunction fluidLevelFloodedness;
    public final FFDDensityFunction fluidLevelSpread;
    public final FFDDensityFunction lava;
    public final FFDDensityFunction temperature;
    public final FFDDensityFunction vegetation;
    public final FFDDensityFunction continents;
    public final FFDDensityFunction erosion;
    public final FFDDensityFunction depth;
    public final FFDDensityFunction ridges;
    public final FFDDensityFunction preliminarySurfaceLevel;
    public final FFDDensityFunction finalDensity;
    public final FFDDensityFunction veinToggle;
    public final FFDDensityFunction veinRidged;
    public final FFDDensityFunction veinGap;

    FFDNoiseRouter(FFDDensityFunction barrier, FFDDensityFunction fluidLevelFloodedness,
                   FFDDensityFunction fluidLevelSpread, FFDDensityFunction lava,
                   FFDDensityFunction temperature, FFDDensityFunction vegetation,
                   FFDDensityFunction continents, FFDDensityFunction erosion,
                   FFDDensityFunction depth, FFDDensityFunction ridges,
                   FFDDensityFunction preliminarySurfaceLevel, FFDDensityFunction finalDensity,
                   FFDDensityFunction veinToggle, FFDDensityFunction veinRidged,
                   FFDDensityFunction veinGap) {
        this.barrier = barrier;
        this.fluidLevelFloodedness = fluidLevelFloodedness;
        this.fluidLevelSpread = fluidLevelSpread;
        this.lava = lava;
        this.temperature = temperature;
        this.vegetation = vegetation;
        this.continents = continents;
        this.erosion = erosion;
        this.depth = depth;
        this.ridges = ridges;
        this.preliminarySurfaceLevel = preliminarySurfaceLevel;
        this.finalDensity = finalDensity;
        this.veinToggle = veinToggle;
        this.veinRidged = veinRidged;
        this.veinGap = veinGap;
    }
}
