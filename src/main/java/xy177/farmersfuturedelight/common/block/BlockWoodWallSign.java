package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockWallSign;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import java.util.Random;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public class BlockWoodWallSign extends BlockWallSign implements IBlockWoodSign {
    private final String woodName;

    public BlockWoodWallSign(String woodName) {
        this.woodName = woodName;
        setRegistryName(FarmerFutureDelight.MODID, woodName + "_wall_sign");
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + woodName + "_wall_sign");
        setHardness(1.0F);
        setSoundType(isNetherWood() ? xy177.farmersfuturedelight.common.registry.FFDSounds.NETHER_WOOD
                : SoundType.WOOD);
    }

    @Override
    public String getWoodName() {
        return woodName;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing facing) {
        return facing.getAxis().isHorizontal()
                && world.getBlockState(pos.offset(facing.getOpposite())).getMaterial().isSolid()
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
