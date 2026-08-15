package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public abstract class BlockFutureSlab extends BlockSlab {
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    private final BlockSlab itemSlab;

    protected BlockFutureSlab(String name, SoundType sound, BlockSlab itemSlab) {
        super(Material.ROCK);
        this.itemSlab = itemSlab == null ? this : itemSlab;
        IBlockState state = blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT);
        if (!isDouble()) {
            state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);
        }
        setDefaultState(state);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        if (!isDouble()) {
            setCreativeTab(FFDCreativeTab.INSTANCE);
        }
        setHardness(3.5F);
        setResistance(6.0F / 3.0F);
        setSoundType(sound);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public String getUnlocalizedName(int meta) {
        return getUnlocalizedName();
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState().withProperty(VARIANT, Variant.DEFAULT);
        return isDouble() ? state : state.withProperty(HALF,
                (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return !isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP ? 8 : 0;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(itemSlab);
    }

    @Override
    public ItemStack getItem(net.minecraft.world.World world, BlockPos pos, IBlockState state) {
        return new ItemStack(itemSlab);
    }

    @Override
    public void getSubBlocks(net.minecraft.creativetab.CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == FFDCreativeTab.INSTANCE && !isDouble()) {
            items.add(new ItemStack(this));
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return isDouble() ? new BlockStateContainer(this, VARIANT)
                : new BlockStateContainer(this, HALF, VARIANT);
    }

    public enum Variant implements IStringSerializable {
        DEFAULT("default");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static final class Half extends BlockFutureSlab {
        public Half(String name, SoundType sound) {
            super(name, sound, null);
        }

        @Override
        public boolean isDouble() {
            return false;
        }
    }

    public static final class Double extends BlockFutureSlab {
        public Double(String name, SoundType sound, BlockSlab itemSlab) {
            super(name, sound, itemSlab);
        }

        @Override
        public boolean isDouble() {
            return true;
        }
    }
}
