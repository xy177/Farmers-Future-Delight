package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockBigDripleafStem extends Block implements IGrowable {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private static final AxisAlignedBB NORTH_AABB =
            new AxisAlignedBB(0.3125D, 0.0D, 0.5625D, 0.6875D, 1.0D, 0.9375D);
    private static final AxisAlignedBB SOUTH_AABB =
            new AxisAlignedBB(0.3125D, 0.0D, 0.0625D, 0.6875D, 1.0D, 0.4375D);
    private static final AxisAlignedBB EAST_AABB =
            new AxisAlignedBB(0.0625D, 0.0D, 0.3125D, 0.4375D, 1.0D, 0.6875D);
    private static final AxisAlignedBB WEST_AABB =
            new AxisAlignedBB(0.5625D, 0.0D, 0.3125D, 0.9375D, 1.0D, 0.6875D);

    public BlockBigDripleafStem() {
        super(Material.PLANTS);
        setRegistryName(FarmerFutureDelight.MODID, "big_dripleaf_stem");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".big_dripleaf_stem");
        setHardness(0.1F);
        setSoundType(FFDSounds.BIG_DRIPLEAF);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(WATERLOGGED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockLiquid.LEVEL, FACING, WATERLOGGED);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return state.getValue(WATERLOGGED) ? Material.WATER : Material.PLANTS;
    }

    public static boolean isWaterlogged(IBlockState state) {
        return state.getBlock() instanceof BlockBigDripleafStem && state.getValue(WATERLOGGED);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(WATERLOGGED) ? 4 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
                .withProperty(WATERLOGGED, (meta & 4) != 0);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return FFDItems.isDripleafEnabled() && canStay(world, pos);
    }

    private static boolean canStay(World world, BlockPos pos) {
        IBlockState below = world.getBlockState(pos.down());
        IBlockState above = world.getBlockState(pos.up());
        boolean supportedBelow = below.getBlock() == FFDBlocks.BIG_DRIPLEAF_STEM
                || DripleafPlacement.isBigDripleafGround(below);
        return supportedBelow && (above.getBlock() == FFDBlocks.BIG_DRIPLEAF_STEM
                || BlockBigDripleaf.isBigDripleaf(above));
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
            dropBlockAsItem(world, pos, state, 0);
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
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockState(pos, replacementState(state), 3);
            return;
        }
        if (isWaterlogged(state)) {
            WaterloggedPlantFluid.updateTick(world, pos, state);
        }
    }

    private static BlockPos findHead(World world, BlockPos pos) {
        BlockPos cursor = pos;
        while (world.getBlockState(cursor).getBlock() == FFDBlocks.BIG_DRIPLEAF_STEM) {
            cursor = cursor.up();
        }
        return BlockBigDripleaf.isBigDripleaf(world.getBlockState(cursor)) ? cursor : null;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        BlockPos head = findHead(world, pos);
        return FFDItems.isDripleafEnabled() && head != null
                && DripleafPlacement.canReplace(world, head.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return FFDItems.isDripleafEnabled();
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        BlockPos head = findHead(world, pos);
        if (head != null) {
            DripleafPlacement.growHead(world, head, state.getValue(FACING));
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        if (FFDItems.isDripleafEnabled()) {
            drops.add(new ItemStack(FFDItems.BIG_DRIPLEAF));
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(FFDItems.BIG_DRIPLEAF);
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
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return isWaterlogged(world.getBlockState(pos))
                ? Blocks.WATER.modifyAcceleration(world, pos, entity, motion) : motion;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case SOUTH:
                return SOUTH_AABB;
            case EAST:
                return EAST_AABB;
            case WEST:
                return WEST_AABB;
            default:
                return NORTH_AABB;
        }
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
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return false;
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

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
