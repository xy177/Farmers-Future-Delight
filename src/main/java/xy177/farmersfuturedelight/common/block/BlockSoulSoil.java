package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class BlockSoulSoil extends BlockNetherBlock {
    public BlockSoulSoil() {
        super("soul_soil", Material.SAND, FFDSounds.SOUL_SOIL, 0.5F);
        setResistance(0.5F / 3.0F);
        setHarvestLevel("shovel", 0);
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return Blocks.SOUL_SAND.getDefaultState().getMapColor(world, pos);
    }
}
