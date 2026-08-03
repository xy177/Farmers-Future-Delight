package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockDriedKelp extends Block {
    public BlockDriedKelp() {
        super(Material.WOOD);
        setRegistryName(FarmerFutureDelight.MODID, "dried_kelp_block");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".dried_kelp_block");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.5F);
        setResistance(2.5F);
        setSoundType(SoundType.PLANT);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 20;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }
}
