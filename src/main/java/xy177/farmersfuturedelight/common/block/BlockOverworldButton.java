package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockButtonWood;
import net.minecraft.block.SoundType;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockOverworldButton extends BlockButtonWood {
    public BlockOverworldButton(String name) {
        super();
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.5F);
        setSoundType(SoundType.WOOD);
    }
}
