package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockFutureStairs extends BlockStairs {
    public BlockFutureStairs(String name, IBlockState modelState, SoundType sound) {
        super(modelState);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(3.5F);
        setResistance(6.0F / 3.0F);
        setSoundType(sound);
        setHarvestLevel("pickaxe", 0);
    }
}
