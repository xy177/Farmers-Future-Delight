package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.BlockGlass;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockTintedGlass extends BlockGlass {
    public BlockTintedGlass() {
        super(Material.GLASS, false);
        setRegistryName(FarmerFutureDelight.MODID, "tinted_glass");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".tinted_glass");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.3F);
        setSoundType(SoundType.GLASS);
        setLightOpacity(255);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
                                    EntityLiving.SpawnPlacementType type) {
        return false;
    }
}
