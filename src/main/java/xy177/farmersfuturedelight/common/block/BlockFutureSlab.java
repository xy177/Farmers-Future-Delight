package xy177.farmersfuturedelight.common.block;

import javax.annotation.Nullable;

import java.util.Random;

import net.minecraft.block.Block;
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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;

public abstract class BlockFutureSlab extends BlockSlab implements IWaterloggableBlock {
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");

    private final BlockSlab itemSlab;

    protected BlockFutureSlab(String name, SoundType sound, BlockSlab itemSlab) {
        super(Material.ROCK);
        this.useNeighborBrightness = true;
        this.itemSlab = itemSlab == null ? this : itemSlab;
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
        if (isDouble()) {
            return state;
        }
        return state.withProperty(WATERLOGGED, (meta & 1) != 0)
                .withProperty(HALF, (meta & 8) == 0
                        ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
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
        return Item.getItemFromBlock(itemSlab);
    }

    @Override
    public ItemStack getItem(net.minecraft.world.World world, BlockPos pos, IBlockState state) {
        return new ItemStack(itemSlab);
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
                                Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        WaterloggedBlockApi.onNeighborChanged(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, java.util.Random random) {
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
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID
                || isWaterloggedState(state) && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
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
