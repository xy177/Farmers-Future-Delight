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
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;

public abstract class BlockUnderwaterPlant extends BlockBush implements IWaterloggableBlock {
    protected BlockUnderwaterPlant() {
        super(Material.WATER);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(FFDSounds.WET_GRASS);
    }

    @Override
    public int getLightOpacity(IBlockState state) {
        return Blocks.WATER.getDefaultState().getLightOpacity();
    }

    protected abstract boolean isFeatureEnabled();

    protected abstract boolean canBlockStayAt(World world, BlockPos pos);

    protected abstract boolean canPlaceInto(World world, BlockPos pos);

    @Override
    public boolean isWaterloggedState(IBlockState state) {
        return true;
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        return waterlogged ? state : null;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {BlockLiquid.LEVEL},
                WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
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
        return WaterloggedBlockApi.isWaterSource(world, pos);
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
            WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
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
        return WaterloggedBlockApi.restoreFluid(world, pos, state, world.isRemote ? 11 : 3);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        WaterloggedBlockApi.restoreFluid(world, pos, world.getBlockState(pos), 3);
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
    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        return Blocks.WATER.getPackedLightmapCoords(Blocks.WATER.getDefaultState(), source, pos);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
                                    net.minecraft.entity.EntityLiving.SpawnPlacementType type) {
        return false;
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
        if (world.getBlockState(pos).getBlock() == this) {
            WaterloggedBlockApi.onNeighborChanged(world, pos, this);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, java.util.Random random) {
        super.updateTick(world, pos, state, random);
        if (world.getBlockState(pos).getBlock() == this) {
            WaterloggedBlockApi.updateTick(world, pos, state);
        }
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }
}
