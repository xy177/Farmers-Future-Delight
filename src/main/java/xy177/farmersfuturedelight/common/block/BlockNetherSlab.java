package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraft.util.math.BlockPos;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public abstract class BlockNetherSlab extends BlockSlab implements IWaterloggableBlock {
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");

    protected BlockNetherSlab(String name) {
        super(Material.WOOD);
        this.useNeighborBrightness = true;
        IBlockState state = blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT);
        if (!isDouble()) {
            state = state.withProperty(HALF, EnumBlockHalf.BOTTOM)
                    .withProperty(WATERLOGGED, false);
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
        if (isDouble()) {
            return state;
        }
        return state.withProperty(HALF, (meta & 8) == 0
                ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP)
                .withProperty(WATERLOGGED, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = !isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP ? 8 : 0;
        return !isDouble() && state.getValue(WATERLOGGED) ? meta | 1 : meta;
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
        if ((tab == FFDCreativeTab.INSTANCE || tab == net.minecraft.creativetab.CreativeTabs.SEARCH)
                && !isDouble()) {
            items.add(new ItemStack(this));
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return isDouble() ? new ExtendedBlockState(this,
                new IProperty<?>[] {VARIANT},
                WaterloggedBlockApi.extendedProperties())
                : new ExtendedBlockState(this,
                        new IProperty<?>[] {HALF, VARIANT, WATERLOGGED},
                        WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            net.minecraft.entity.EntityLivingBase placer) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ,
                meta, placer);
        return isDouble() ? state : state.withProperty(WATERLOGGED,
                WaterloggedBlockApi.containsWater(world, pos));
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return isWaterloggedState(state) ? Material.WATER : super.getMaterial(state);
    }

    @Override
    public boolean isWaterloggedState(IBlockState state) {
        return !isDouble() && state.getBlock() == this && state.getValue(WATERLOGGED);
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        return isDouble() ? (waterlogged ? null : state) : state.withProperty(WATERLOGGED, waterlogged);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedBlockApi.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                net.minecraft.block.Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        WaterloggedBlockApi.onNeighborChanged(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        super.updateTick(world, pos, state, random);
        if (isWaterloggedState(state)) {
            WaterloggedBlockApi.updateTick(world, pos, state);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        return WaterloggedBlockApi.restoreFluid(world, pos, state, world.isRemote ? 11 : 3);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        dropBlockAsItem(world, pos, state, 0);
        WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID
                || isWaterloggedState(state) && layer == BlockRenderLayer.TRANSLUCENT;
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
