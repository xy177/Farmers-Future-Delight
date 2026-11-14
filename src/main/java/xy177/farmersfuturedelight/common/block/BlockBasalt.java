package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MapColor;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class BlockBasalt extends BlockRotatedPillar {
    public BlockBasalt(String name) {
        super(Material.ROCK,MapColor.BLACK);
        setDefaultState(blockState.getBaseState().withProperty(AXIS, net.minecraft.util.EnumFacing.Axis.Y));
        setRegistryName(FarmerFutureDelight.MODID,name);
        setUnlocalizedName(FarmerFutureDelight.MODID+"."+name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(1.25F);setResistance(7.0F);
        setHarvestLevel("pickaxe",0);setSoundType(FFDSounds.BASALT);
    }
}
