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
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public abstract class BlockNetherSlab extends BlockSlab {
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    protected BlockNetherSlab(String name) {
        super(Material.WOOD);
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
        setHardness(2.0F);
        setResistance(3.0F);
        setSoundType(FFDSounds.NETHER_WOOD);
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
        Item item = FFDItems.effectiveItem(isCrimson()
                ? FFDItems.CRIMSON_SLAB : FFDItems.WARPED_SLAB);
        return item == null ? Item.getItemFromBlock(net.minecraft.init.Blocks.AIR) : item;
    }

    @Override
    public ItemStack getItem(net.minecraft.world.World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(isCrimson()
                ? FFDItems.CRIMSON_SLAB : FFDItems.WARPED_SLAB);
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

    protected abstract boolean isCrimson();

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

    public static final class Half extends BlockNetherSlab {
        private final boolean crimson;

        public Half(String name, boolean crimson) {
            super(name);
            this.crimson = crimson;
        }

        @Override
        public boolean isDouble() {
            return false;
        }

        @Override
        protected boolean isCrimson() {
            return crimson;
        }
    }

    public static final class Double extends BlockNetherSlab {
        private final boolean crimson;

        public Double(String name, boolean crimson) {
            super(name);
            this.crimson = crimson;
        }

        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        protected boolean isCrimson() {
            return crimson;
        }
    }
}
