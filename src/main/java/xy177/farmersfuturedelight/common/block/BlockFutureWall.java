package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockFutureWall extends BlockWall {
    public BlockFutureWall(String name, Block modelBlock) {
        super(modelBlock);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public void getSubBlocks(net.minecraft.creativetab.CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == FFDCreativeTab.INSTANCE) {
            items.add(new ItemStack(this));
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, EnumType.NORMAL);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }
}
