package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Random;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public class BlockWoodStandingSign extends BlockStandingSign implements IBlockWoodSign {
    private final String woodName;

    public BlockWoodStandingSign(String woodName) {
        this.woodName = woodName;
        setRegistryName(FarmerFutureDelight.MODID, woodName + "_sign");
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + woodName + "_sign");
        setHardness(1.0F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public String getWoodName() {
        return woodName;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).getMaterial().isSolid()
                && super.canPlaceBlockAt(world, pos);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getByNameOrId(FarmerFutureDelight.MODID + ":" + woodName + "_sign");
    }

    @Override
    public ItemStack getItem(World world, BlockPos position, IBlockState state) {
        return new ItemStack(getItemDropped(state, world.rand, 0));
    }
}
