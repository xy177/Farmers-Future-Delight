package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockNetherStairs extends BlockStairs {
    public BlockNetherStairs(String name, IBlockState modelState) {
        super(modelState);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(2.0F);
        setResistance(3.0F);
        setSoundType(FFDSounds.NETHER_WOOD);
    }
}
