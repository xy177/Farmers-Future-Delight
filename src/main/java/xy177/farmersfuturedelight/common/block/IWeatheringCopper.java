package xy177.farmersfuturedelight.common.block;

public interface IWeatheringCopper {
    CopperWeathering.WeatherState getWeatherState();

    boolean isWaxed();
}
