package xy177.farmersfuturedelight.common.block;

public interface IBlockWoodSign {
    String getWoodName();

    default boolean isNetherWood() {
        return "crimson".equals(getWoodName()) || "warped".equals(getWoodName());
    }
}
