package xy177.farmersfuturedelight.core;

import java.util.List;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;

public final class FFDBedHooks {
    private FFDBedHooks() {
    }

    public static EntityPlayer.SleepResult prepareSleep(EntityPlayer.SleepResult result,
            EntityPlayer player, BlockPos pos) {
        if (result != null || player.world.isRemote) return result;
        if (player.isEntityAlive() && !player.isPlayerSleeping()
                && FFDVillagerHomes.wakeSleepingVillager(player.world, pos)) {
            return EntityPlayer.SleepResult.OTHER_PROBLEM;
        }
        if (!isVanillaBed(player, pos)
                || !player.world.provider.isSurfaceWorld()) {
            return result;
        }
        if (player.isPlayerSleeping() || !player.isEntityAlive()) {
            return EntityPlayer.SleepResult.OTHER_PROBLEM;
        }
        EnumFacing facing = player.world.getBlockState(pos).getValue(BlockHorizontal.FACING);
        if (!reachable(player, pos, facing)) {
            return EntityPlayer.SleepResult.TOO_FAR_AWAY;
        }
        if (obstructed(player, pos.up()) || obstructed(player, pos.offset(facing.getOpposite()).up())) {
            player.sendStatusMessage(new TextComponentTranslation(
                    "farmers_future_delight.bed.obstructed"), true);
            return EntityPlayer.SleepResult.OTHER_PROBLEM;
        }
        BlockPos previous = player.getBedLocation(player.dimension);
        boolean forced = player.isSpawnForced(player.dimension);
        player.setSpawnPoint(pos, false);
        if (pos.equals(player.getBedLocation(player.dimension))
                && (!pos.equals(previous) || forced) && !player.isSpawnForced(player.dimension)) {
            player.sendMessage(new TextComponentTranslation("farmers_future_delight.bed.spawn_set"));
        }
        return null;
    }

    public static boolean bedInRange(boolean original, EntityPlayer player, BlockPos pos) {
        return isVanillaBed(player, pos) ? reachable(player, pos,
                player.world.getBlockState(pos).getValue(BlockHorizontal.FACING)) : original;
    }

    public static AxisAlignedBB monsterBounds(AxisAlignedBB original,
            EntityPlayer player, BlockPos pos) {
        return isVanillaBed(player, pos) ? original.offset(0.5D, 0.0D, 0.5D) : original;
    }

    public static boolean noMonsters(List<?> monsters, EntityPlayer player, BlockPos pos) {
        return monsters.isEmpty() || isVanillaBed(player, pos) && player.capabilities.isCreativeMode;
    }

    private static boolean isVanillaBed(EntityPlayer player, BlockPos pos) {
        return player.world.isBlockLoaded(pos) && player.world.getBlockState(pos).getBlock() == Blocks.BED;
    }

    private static boolean reachable(EntityPlayer player, BlockPos pos, EnumFacing facing) {
        return inRange(player, pos) || facing != null && inRange(player, pos.offset(facing.getOpposite()));
    }

    private static boolean inRange(EntityPlayer player, BlockPos pos) {
        return Math.abs(player.posX - pos.getX() - 0.5D) <= 3.0D
                && Math.abs(player.posY - pos.getY()) <= 2.0D
                && Math.abs(player.posZ - pos.getZ() - 0.5D) <= 3.0D;
    }

    private static boolean obstructed(EntityPlayer player, BlockPos pos) {
        IBlockState state = player.world.getBlockState(pos);
        if (state.getMaterial() == Material.GLASS || state.getBlock() instanceof BlockLeaves) {
            return false;
        }
        return state.getBlock() == Blocks.FARMLAND || state.getBlock() == Blocks.GRASS_PATH
                || state.causesSuffocation();
    }
}
