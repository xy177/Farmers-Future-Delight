package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockOverworldPressurePlate extends BlockPressurePlate {
    public BlockOverworldPressurePlate(String name) {
        super(Material.WOOD, Sensitivity.EVERYTHING);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.5F);
        setSoundType(SoundType.WOOD);
    }
}
