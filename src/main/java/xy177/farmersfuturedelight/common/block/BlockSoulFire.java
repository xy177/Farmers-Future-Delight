package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.core.FFDSoulFireHooks;

public final class BlockSoulFire extends BlockFire {
    public BlockSoulFire() {
        setRegistryName(FarmerFutureDelight.MODID, "soul_fire");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".soul_fire");
        setLightLevel(10.0F / 15.0F);
        setTickRandomly(false);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return FFDSoulFireHooks.isSoulBase(world.getBlockState(pos.down()));
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (world.provider.getDimensionType().getId() <= 0 && Blocks.PORTAL.trySpawnPortal(world, pos)) return;
        if (!canPlaceBlockAt(world, pos)) world.setBlockToAir(pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block block, BlockPos fromPos) {
        if (!canPlaceBlockAt(world, pos)) world.setBlockToAir(pos);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!canPlaceBlockAt(world, pos)) world.setBlockToAir(pos);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getDefaultState();
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos,
                                         @Nullable EntityLiving entity) {
        return PathNodeType.DAMAGE_FIRE;
    }

    @Override
    public boolean canCatchFire(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return true;
    }
}
