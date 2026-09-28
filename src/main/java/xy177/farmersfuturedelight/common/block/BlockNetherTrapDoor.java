package xy177.farmersfuturedelight.common.block;

import javax.annotation.Nullable;

import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockNetherTrapDoor extends BlockTrapDoor implements IWaterloggableBlock {
    private final boolean crimson;
    private final boolean waterlogged;

    public BlockNetherTrapDoor(String name) {
        this(name, false);
    }

    public BlockNetherTrapDoor(String name, boolean waterlogged) {
        super(Material.WOOD);
        this.crimson = name.startsWith("crimson_");
        this.waterlogged = waterlogged;
        setRegistryName(FarmerFutureDelight.MODID,
                waterlogged ? name + "_waterlogged" : name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        if (!waterlogged) {
            setCreativeTab(FFDCreativeTab.INSTANCE);
        }
        setHardness(3.0F);
        setResistance(3.0F);
        setSoundType(FFDSounds.NETHER_WOOD);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        IBlockState state = super.getStateForPlacement(world, pos, facing,
                hitX, hitY, hitZ, meta, placer, hand);
        return setWaterloggedState(state, WaterloggedBlockApi.containsWater(world, pos));
    }

    @Override
    protected void playSound(@Nullable EntityPlayer player, World world, BlockPos pos, boolean open) {
        world.playSound(player, pos,
                open ? FFDSounds.NETHER_WOOD_TRAPDOOR_OPEN
                        : FFDSounds.NETHER_WOOD_TRAPDOOR_CLOSE,
                SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {FACING, OPEN, HALF},
                WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return isWaterloggedState(state) ? Material.WATER : super.getMaterial(state);
    }

    @Override
    public boolean isWaterloggedState(IBlockState state) {
        return state.getBlock() == this && waterlogged;
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        BlockNetherTrapDoor target;
        if (crimson) {
            target = waterlogged ? FFDBlocks.CRIMSON_TRAPDOOR_WATERLOGGED
                    : FFDBlocks.CRIMSON_TRAPDOOR;
        } else {
            target = waterlogged ? FFDBlocks.WARPED_TRAPDOOR_WATERLOGGED
                    : FFDBlocks.WARPED_TRAPDOOR;
        }
        return target.getDefaultState()
                .withProperty(FACING, state.getValue(FACING))
                .withProperty(OPEN, state.getValue(OPEN))
                .withProperty(HALF, state.getValue(HALF));
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
    public void updateTick(World world, BlockPos pos, IBlockState state, java.util.Random random) {
        if (isWaterloggedState(state)) {
            WaterloggedBlockApi.updateTick(world, pos, state);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, java.util.Random random, int fortune) {
        return Item.getItemFromBlock(crimson
                ? FFDBlocks.CRIMSON_TRAPDOOR : FFDBlocks.WARPED_TRAPDOOR);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        Item item = getItemDropped(state, world.rand, 0);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        return WaterloggedBlockApi.restoreFluid(world, pos, state,
                world.isRemote ? 11 : 3);
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
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT
                || isWaterloggedState(state) && layer == BlockRenderLayer.TRANSLUCENT;
    }
}
