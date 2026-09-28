package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockDriedKelp extends Block {
    public BlockDriedKelp() {
        super(Material.WOOD, MapColor.GREEN);
        setRegistryName(FarmerFutureDelight.MODID, "dried_kelp_block");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".dried_kelp_block");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.5F);
        setResistance(25.0F / 6.0F);
        setSoundType(SoundType.PLANT);
    }
}
