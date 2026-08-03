package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockHoneycomb extends Block {
    public BlockHoneycomb() {
        super(Material.WOOD);
        setRegistryName(FarmerFutureDelight.MODID, "honeycomb_block");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".honeycomb_block");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.6F);
        setSoundType(FFDSounds.CORAL);
    }
}
