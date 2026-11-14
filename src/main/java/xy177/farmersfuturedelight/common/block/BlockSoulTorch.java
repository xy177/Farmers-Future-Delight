package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class BlockSoulTorch extends BlockTorch {
    public BlockSoulTorch() {
        setRegistryName(FarmerFutureDelight.MODID, "soul_torch");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".soul_torch");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(net.minecraft.block.SoundType.WOOD);
        setLightLevel(10.0F / 15.0F);
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        EnumFacing facing = state.getValue(FACING);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + 0.5D;
        if (facing.getAxis().isHorizontal()) {
            x -= facing.getFrontOffsetX() * 0.27D;
            y += 0.22D;
            z -= facing.getFrontOffsetZ() * 0.27D;
        }
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D);
        FarmerFutureDelight.proxy.spawnSoulFlameParticle(world, x, y, z);
    }
}
