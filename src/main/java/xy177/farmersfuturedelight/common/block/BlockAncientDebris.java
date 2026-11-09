package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class BlockAncientDebris extends Block {
    public BlockAncientDebris() {
        super(Material.ROCK);
        setRegistryName(FarmerFutureDelight.MODID, "ancient_debris");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".ancient_debris");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(30.0F);
        setResistance(1200.0F);
        setHarvestLevel("pickaxe", 3);
        setSoundType(FFDSounds.ANCIENT_DEBRIS);
    }
}
