package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public class BlockKelpYoung extends BlockKelpHead {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);

    public BlockKelpYoung() {
        setRegistryName(FarmerFutureDelight.MODID, "kelp_young");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".kelp_young");
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
        return getDefaultState().withProperty(BlockLiquid.LEVEL, 0).withProperty(AGE, Math.max(0, Math.min(15, age)));
    }

    @Override
    protected int ageFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    protected int metadataAge(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    protected IBlockState stateFromMetadataAge(int meta) {
        return stateForAge(meta);
    }
}
