package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class BlockNetheriteBlock extends Block {
    public BlockNetheriteBlock() {
        super(Material.IRON);
        setRegistryName(FarmerFutureDelight.MODID, "netherite_block");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".netherite_block");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(50.0F);
        setResistance(1200.0F);
        setHarvestLevel("pickaxe", 3);
    }
}
