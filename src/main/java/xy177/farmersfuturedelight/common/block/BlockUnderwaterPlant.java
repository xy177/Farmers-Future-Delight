package xy177.farmersfuturedelight.common.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

/** Common water-material behavior for backported aquatic blocks. */
public abstract class BlockUnderwaterPlant extends BlockBush {
    protected BlockUnderwaterPlant() {
        super(Material.WATER);
        setHardness(0.0F);
        setSoundType(FFDSounds.WET_GRASS);
        setLightOpacity(Blocks.WATER.getLightOpacity(Blocks.WATER.getDefaultState()));
    }

    protected abstract boolean isFeatureEnabled();

    protected abstract boolean canBlockStayAt(World world, BlockPos pos);

    protected abstract boolean canPlaceInto(World world, BlockPos pos);

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockLiquid.LEVEL);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockLiquid.LEVEL);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockLiquid.LEVEL, meta & 15);
    }

    protected static boolean isSourceWater(IBlockAccess world, BlockPos pos) {
        return WaterloggedPlantFluid.isSourceWater(world, pos);
    }

    protected static boolean isVanillaWaterBlock(IBlockAccess world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
    }

    protected static boolean isWaterOrPlant(IBlockAccess world, BlockPos pos, Block plant) {
        return isSourceWater(world, pos) || world.getBlockState(pos).getBlock() == plant;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return isFeatureEnabled() && canPlaceInto(world, pos) && canBlockStayAt(world, pos);
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return canBlockStayAt(world, pos);
    }

    protected void checkAndRestoreWater(World world, BlockPos pos, IBlockState state) {
        if (!canBlockStayAt(world, pos)) {
            if (isFeatureEnabled()) {
                dropBlockAsItem(world, pos, state, 0);
            }
            world.setBlockState(pos, Blocks.WATER.getDefaultState(), 3);
        }
    }

    @Override
    protected void checkAndDropBlock(World world, BlockPos pos, IBlockState state) {
        checkAndRestoreWater(world, pos, state);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        return world.setBlockState(pos, Blocks.WATER.getDefaultState(), world.isRemote ? 11 : 3);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        world.setBlockState(pos, Blocks.WATER.getDefaultState(), 3);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return false;
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
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
                                        EnumFacing side) {
        if (world.getBlockState(pos.offset(side)).getMaterial() == Material.WATER) {
            return false;
        }
        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
                                    net.minecraft.entity.EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedPlantFluid.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        if (world.getBlockState(pos).getBlock() == this) {
            WaterloggedPlantFluid.onNeighborChanged(world, pos, this);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, java.util.Random random) {
        super.updateTick(world, pos, state, random);
        if (world.getBlockState(pos).getBlock() == this) {
            WaterloggedPlantFluid.updateTick(world, pos, state);
        }
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedPlantFluid.isSourceWater(world, pos)
                ? Blocks.WATER.modifyAcceleration(world, pos, entity, motion)
                : motion;
    }
}
