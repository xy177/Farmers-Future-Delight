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
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockBigDripleaf extends Block implements IGrowable, IWaterloggableBlock {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<DripleafTilt> TILT = PropertyEnum.create("tilt", DripleafTilt.class);
    private static final int UNSTABLE_DELAY = 10;
    private static final int PARTIAL_DELAY = 10;
    private static final int FULL_DELAY = 100;
    private static final AxisAlignedBB NORMAL_AABB =
            new AxisAlignedBB(0.0D, 0.6875D, 0.0D, 1.0D, 0.9375D, 1.0D);
    private static final AxisAlignedBB PARTIAL_AABB =
            new AxisAlignedBB(0.0D, 0.6875D, 0.0D, 1.0D, 0.8125D, 1.0D);
    private static final AxisAlignedBB NORTH_STEM_AABB =
            new AxisAlignedBB(0.3125D, 0.0D, 0.5625D, 0.6875D, 0.8125D, 0.9375D);
    private static final AxisAlignedBB SOUTH_STEM_AABB =
            new AxisAlignedBB(0.3125D, 0.0D, 0.0625D, 0.6875D, 0.8125D, 0.4375D);
    private static final AxisAlignedBB EAST_STEM_AABB =
            new AxisAlignedBB(0.0625D, 0.0D, 0.3125D, 0.4375D, 0.8125D, 0.6875D);
    private static final AxisAlignedBB WEST_STEM_AABB =
            new AxisAlignedBB(0.5625D, 0.0D, 0.3125D, 0.9375D, 0.8125D, 0.6875D);

    private final boolean waterlogged;

    public BlockBigDripleaf(String registryName, boolean waterlogged) {
        super(waterlogged ? Material.WATER : Material.PLANTS);
        this.waterlogged = waterlogged;
        setRegistryName(FarmerFutureDelight.MODID, registryName);
        setUnlocalizedName(FarmerFutureDelight.MODID + ".big_dripleaf");
        setHardness(0.1F);
        setSoundType(FFDSounds.BIG_DRIPLEAF);
        setTickRandomly(false);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(TILT, DripleafTilt.NONE));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {BlockLiquid.LEVEL, FACING, TILT},
                WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(TILT).ordinal() << 2);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        DripleafTilt[] tilts = DripleafTilt.values();
        return getDefaultState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
                .withProperty(TILT, tilts[Math.min(tilts.length - 1, (meta >> 2) & 3)]);
    }

    public static boolean isBigDripleaf(IBlockState state) {
        return state.getBlock() instanceof BlockBigDripleaf;
    }

    public static boolean isWaterlogged(IBlockState state) {
        return state.getBlock() instanceof BlockBigDripleaf
                && ((BlockBigDripleaf) state.getBlock()).waterlogged;
    }

    @Override
    public boolean isWaterloggedState(IBlockState state) {
        return isWaterlogged(state);
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        BlockBigDripleaf target = waterlogged ? FFDBlocks.BIG_DRIPLEAF_WATERLOGGED
                : FFDBlocks.BIG_DRIPLEAF;
        return target.getDefaultState()
                .withProperty(FACING, state.getValue(FACING))
                .withProperty(TILT, state.getValue(TILT));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return FFDItems.isDripleafEnabled() && DripleafPlacement.canPlaceHead(world, pos);
    }

    private static boolean canStay(World world, BlockPos pos) {
        IBlockState below = world.getBlockState(pos.down());
        return isBigDripleaf(below) || below.getBlock() == FFDBlocks.BIG_DRIPLEAF_STEM
                || DripleafPlacement.isBigDripleafGround(below);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedBlockApi.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }

        if (isBigDripleaf(world.getBlockState(pos.up()))) {
            world.setBlockState(pos, DripleafPlacement.stemState(world, pos, state.getValue(FACING)), 3);
            return;
        }
        if (!canStay(world, pos)) {
            dropBlockAsItem(world, pos, state, 0);
            WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
            return;
        }
        if (world.isBlockPowered(pos) && state.getValue(TILT) != DripleafTilt.NONE) {
            resetTilt(world, pos, state);
        }
        WaterloggedBlockApi.onNeighborChanged(world, pos, this);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!world.isRemote && state.getValue(TILT) == DripleafTilt.NONE
                && !world.isBlockPowered(pos) && entity.onGround
                && entity.posY > pos.getY() + 0.6875D) {
            setTilt(world, pos, state, DripleafTilt.UNSTABLE);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || world.getBlockState(pos).getBlock() != this) {
            return;
        }
        if (!canStay(world, pos)) {
            dropBlockAsItem(world, pos, state, 0);
            WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
            return;
        }
        WaterloggedBlockApi.updateTick(world, pos, state);
        if (world.isBlockPowered(pos)) {
            if (state.getValue(TILT) != DripleafTilt.NONE) {
                resetTilt(world, pos, state);
            }
            return;
        }
        switch (state.getValue(TILT)) {
            case UNSTABLE:
                setTilt(world, pos, state, DripleafTilt.PARTIAL, FFDSounds.BIG_DRIPLEAF_TILT_DOWN);
                break;
            case PARTIAL:
                setTilt(world, pos, state, DripleafTilt.FULL, FFDSounds.BIG_DRIPLEAF_TILT_DOWN);
                break;
            case FULL:
                resetTilt(world, pos, state);
                break;
            default:
                break;
        }
    }

    public static void tiltFully(World world, BlockPos pos, IBlockState state) {
        if (isBigDripleaf(state)) {
            setTilt(world, pos, state, DripleafTilt.FULL, FFDSounds.BIG_DRIPLEAF_TILT_DOWN);
        }
    }

    private static void setTilt(World world, BlockPos pos, IBlockState state, DripleafTilt tilt) {
        setTilt(world, pos, state, tilt, null);
    }

    private static void setTilt(World world, BlockPos pos, IBlockState state, DripleafTilt tilt,
                                @Nullable SoundEvent sound) {
        if (state.getValue(TILT) != tilt) {
            world.setBlockState(pos, state.withProperty(TILT, tilt), 3);
        }
        if (sound != null) {
            world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F,
                    0.8F + world.rand.nextFloat() * 0.4F);
        }
        int delay = tilt == DripleafTilt.UNSTABLE ? UNSTABLE_DELAY
                : tilt == DripleafTilt.PARTIAL ? PARTIAL_DELAY
                : tilt == DripleafTilt.FULL ? FULL_DELAY : 0;
        if (delay > 0) {
            world.scheduleUpdate(pos, state.getBlock(), delay);
        }
    }

    private static void resetTilt(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(TILT) != DripleafTilt.NONE) {
            setTilt(world, pos, state, DripleafTilt.NONE, FFDSounds.BIG_DRIPLEAF_TILT_UP);
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return FFDItems.isDripleafEnabled() && DripleafPlacement.canReplace(world, pos.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return FFDItems.isDripleafEnabled();
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        DripleafPlacement.growHead(world, pos, state.getValue(FACING));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        if (FFDItems.isDripleafEnabled()) {
            drops.add(FFDItems.effectiveStack(FFDItems.BIG_DRIPLEAF));
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.BIG_DRIPLEAF);
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
        WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        DripleafTilt tilt = state.getValue(TILT);
        AxisAlignedBB stem = getStemBox(state.getValue(FACING));
        if (tilt == DripleafTilt.FULL) {
            return stem;
        }
        AxisAlignedBB leaf = tilt == DripleafTilt.PARTIAL ? PARTIAL_AABB : NORMAL_AABB;
        return leaf.union(stem);
    }

    private static AxisAlignedBB getStemBox(EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                return SOUTH_STEM_AABB;
            case EAST:
                return EAST_STEM_AABB;
            case WEST:
                return WEST_STEM_AABB;
            default:
                return NORTH_STEM_AABB;
        }
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        DripleafTilt tilt = state.getValue(TILT);
        return tilt == DripleafTilt.FULL ? NULL_AABB
                : tilt == DripleafTilt.PARTIAL ? PARTIAL_AABB : NORMAL_AABB;
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
        return layer == BlockRenderLayer.CUTOUT || waterlogged && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
                                        EnumFacing side) {
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
