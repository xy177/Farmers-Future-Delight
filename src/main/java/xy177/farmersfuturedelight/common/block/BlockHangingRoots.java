package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockHangingRoots extends Block {
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private static final AxisAlignedBB ROOTS_AABB =
            new AxisAlignedBB(0.125D, 0.625D, 0.125D, 0.875D, 1.0D, 0.875D);

    public BlockHangingRoots() {
        super(Material.PLANTS);
        setRegistryName(FarmerFutureDelight.MODID, "hanging_roots");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".hanging_roots");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(FFDSounds.HANGING_ROOTS);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(WATERLOGGED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {
                        BlockLiquid.LEVEL, WATERLOGGED},
                WaterloggedPlantFluid.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedPlantFluid.getExtendedState(state, world, pos);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return state.getValue(WATERLOGGED) ? Material.WATER : Material.PLANTS;
    }

    public static boolean isWaterlogged(IBlockState state) {
        return state.getBlock() instanceof BlockHangingRoots && state.getValue(WATERLOGGED);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(WATERLOGGED) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(WATERLOGGED, (meta & 1) != 0);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(WATERLOGGED,
                WaterloggedPlantFluid.isSourceWater(world, pos));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return FFDItems.isHangingRootsEnabled()
                && (world.isAirBlock(pos) || WaterloggedPlantFluid.isSourceWater(world, pos))
                && canStay(world, pos);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedPlantFluid.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        if (!canStay(world, pos)) {
            world.setBlockState(pos, replacementState(state), 3);
            return;
        }
        WaterloggedPlantFluid.onNeighborChanged(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || world.getBlockState(pos).getBlock() != this) {
            return;
        }
        if (!canStay(world, pos)) {
            world.setBlockState(pos, replacementState(state), 3);
        } else if (isWaterlogged(state)) {
            WaterloggedPlantFluid.updateTick(world, pos, state);
        }
    }

    private static boolean canStay(World world, BlockPos pos) {
        return world.getBlockState(pos.up()).isSideSolid(world, pos.up(), EnumFacing.DOWN);
    }

    private static IBlockState replacementState(IBlockState state) {
        return isWaterlogged(state) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        return world.setBlockState(pos, replacementState(state), world.isRemote ? 11 : 3);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, replacementState(state), 3);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity tile, ItemStack tool) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
        if (!world.isRemote && FFDItems.isHangingRootsEnabled() && tool.getItem() == Items.SHEARS) {
            spawnAsEntity(world, pos, FFDItems.effectiveStack(FFDItems.HANGING_ROOTS));
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.HANGING_ROOTS);
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return isWaterlogged(world.getBlockState(pos))
                ? Blocks.WATER.modifyAcceleration(world, pos, entity, motion) : motion;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return ROOTS_AABB;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT
                || state.getValue(WATERLOGGED) && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
                                        EnumFacing side) {
        if (state.getValue(WATERLOGGED)
                && world.getBlockState(pos.offset(side)).getMaterial() == Material.WATER) {
            return false;
        }
        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
                                    EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
}
