package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockFutureMetal extends Block {
    public BlockFutureMetal(String name, int harvestLevel) {
        super(Material.IRON);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(5.0F);
        setResistance(6.0F / 3.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", harvestLevel);
    }
}
