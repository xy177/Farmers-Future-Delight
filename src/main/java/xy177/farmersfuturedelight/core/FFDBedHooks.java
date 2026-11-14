package xy177.farmersfuturedelight.core;

import java.util.List;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.BlockRespawnAnchor;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDBedHooks {
    private static final String ANCHOR_SPAWN = "FFDAnchorSpawn";
    private static final java.util.Map<EntityPlayerMP, net.minecraft.util.math.Vec3d> ANCHOR_POSITIONS = new java.util.WeakHashMap<>();

    private FFDBedHooks() {
    }

    public static boolean isAnchorSpawn(EntityPlayer player, BlockPos pos) {
        NBTTagCompound data = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        return pos != null && data.hasKey(ANCHOR_SPAWN, 4) && pos.toLong() == data.getLong(ANCHOR_SPAWN)
                && pos.equals(player.getBedLocation(-1)) && !player.isSpawnForced(-1);
    }

    public static void setAnchorSpawn(EntityPlayer player, BlockPos pos) {
        NBTTagCompound data = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        data.setLong(ANCHOR_SPAWN, pos.toLong());
        player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpawnSet(PlayerSetSpawnEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.world.isRemote && (player.dimension != -1 || event.isForced()
                || !isAnchorSpawn(player, event.getNewSpawn()))) {
            player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).removeTag(ANCHOR_SPAWN);
        }
    }

    public static int anchorRespawnDimension(EntityPlayerMP player, int original) {
        ANCHOR_POSITIONS.remove(player);
        BlockPos pos = player.getBedLocation(-1);
        if (!isAnchorSpawn(player, pos)) return original;
        WorldServer nether = player.getServer().getWorld(-1);
        if (nether != null && BlockRespawnAnchor.safePosition(nether, pos) != null) return -1;
        player.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
        return 0;
    }

    public static BlockPos respawnPosition(EntityPlayerMP player, int dimension) {
        return dimension != -1 && isAnchorSpawn(player, player.getBedLocation(-1))
                ? null : player.getBedLocation(dimension);
    }

    public static BlockPos findRespawn(World world, BlockPos pos, boolean forced, EntityPlayerMP player,
                                        boolean conqueredEnd) {
        if (world.provider.getDimension() != -1 || !isAnchorSpawn(player, pos)) {
            return EntityPlayer.getBedSpawnLocation(world, pos, forced);
        }
        net.minecraft.util.math.Vec3d safe = BlockRespawnAnchor.safeLocation(world, pos);
        if (safe != null) ANCHOR_POSITIONS.put(player, safe);
        if (safe != null && !conqueredEnd) BlockRespawnAnchor.consume(world, pos);
        return safe == null ? null : new BlockPos(safe);
    }

    public static void placeRespawn(EntityPlayerMP recreated, double x, double y, double z, float yaw, float pitch,
            EntityPlayerMP previous) {
        net.minecraft.util.math.Vec3d safe = ANCHOR_POSITIONS.remove(previous);
        if (safe == null) recreated.setLocationAndAngles(x, y, z, yaw, pitch);
        else recreated.setLocationAndAngles(safe.x, safe.y, safe.z, yaw, pitch);
    }

    public static EntityPlayer.SleepResult prepareSleep(EntityPlayer.SleepResult result,
            EntityPlayer player, BlockPos pos) {
        if (result != null || player.world.isRemote) return result;
        if (player.isEntityAlive() && !player.isPlayerSleeping()
                && FFDVillagerHomes.wakeSleepingVillager(player.world, pos)) {
            return EntityPlayer.SleepResult.OTHER_PROBLEM;
        }
        if (!FFDConfig.modernBedSpawn || !isVanillaBed(player, pos)
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
        return FFDConfig.modernBedSpawn && isVanillaBed(player, pos) ? reachable(player, pos,
                player.world.getBlockState(pos).getValue(BlockHorizontal.FACING)) : original;
    }

    public static AxisAlignedBB monsterBounds(AxisAlignedBB original,
            EntityPlayer player, BlockPos pos) {
        return FFDConfig.modernBedSpawn && isVanillaBed(player, pos)
                ? original.offset(0.5D, 0.0D, 0.5D) : original;
    }

    public static boolean noMonsters(List<?> monsters, EntityPlayer player, BlockPos pos) {
        return monsters.isEmpty() || FFDConfig.modernBedSpawn
                && isVanillaBed(player, pos) && player.capabilities.isCreativeMode;
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
