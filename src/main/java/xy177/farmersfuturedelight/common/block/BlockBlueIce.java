package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockBlueIce extends Block {
    public BlockBlueIce() {
        super(Material.PACKED_ICE);
        setRegistryName(FarmerFutureDelight.MODID, "blue_ice");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".blue_ice");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(2.8F);
        setResistance(14.0F / 3.0F);
        setSoundType(SoundType.GLASS);
        setHarvestLevel("pickaxe", 0);
        slipperiness = 0.989F;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }
}
