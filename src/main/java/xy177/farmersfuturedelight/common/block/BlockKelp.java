package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public class BlockKelp extends BlockKelpHead {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 9);

    public BlockKelp() {
        setRegistryName(FarmerFutureDelight.MODID, "kelp");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".kelp");
    }

    @Override
    protected PropertyInteger getAgeProperty() {
        return AGE;
    }

    @Override
    protected int getDefaultAgeValue() {
        return 0;
    }

    @Override
    public IBlockState stateForAge(int age) {
        return getDefaultState().withProperty(BlockLiquid.LEVEL, 0).withProperty(AGE,
                Math.max(0, Math.min(9, age - 16)));
    }

    @Override
    protected int ageFromState(IBlockState state) {
        return state.getValue(AGE) + 16;
    }

    @Override
    protected int metadataAge(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    protected IBlockState stateFromMetadataAge(int meta) {
        return stateForAge(16 + Math.max(0, Math.min(9, meta)));
    }
}
