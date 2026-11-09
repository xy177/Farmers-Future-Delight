package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public final class FFDVillagerHomes {
    public static final String HOME_TAG = "farmers_future_delight:home";
    public static final String SLEEPING_TAG = "farmers_future_delight:sleeping";
    public static final String LAST_SLEPT_TAG = "farmers_future_delight:last_slept";
    public static final String LAST_WOKEN_TAG = "farmers_future_delight:last_woken";
    private static final Map<EntityVillager, Map<BlockPos, Long>> FAILED_PATHS = new WeakHashMap<>();

    private FFDVillagerHomes() {
    }

    @Nullable
    public static BlockPos getHome(EntityVillager villager) {
        NBTTagCompound home = villager.getEntityData().getCompoundTag(HOME_TAG);
        if (!home.hasKey("position", 99) || home.getInteger("dimension") != villager.dimension) return null;
        return BlockPos.fromLong(home.getLong("position"));
    }

    public static boolean validateHome(EntityVillager villager) {
        BlockPos home = getHome(villager);
        if (home == null) return false;
        if (!villager.world.isBlockLoaded(home)) return true;
        if (isBedHead(villager.world.getBlockState(home))
                && homes(villager).claim(home, villager.getUniqueID())) return true;
        release(villager);
        return false;
    }

    public static boolean bind(EntityVillager villager, BlockPos bed) {
        if (!villager.world.isBlockLoaded(bed) || !isBedHead(villager.world.getBlockState(bed))
                || !homes(villager).claim(bed, villager.getUniqueID())) return false;
        BlockPos old = getHome(villager);
        if (old != null && !old.equals(bed)) homes(villager).release(old, villager.getUniqueID());
        NBTTagCompound home = new NBTTagCompound();
        home.setInteger("dimension", villager.dimension);
        home.setLong("position", bed.toLong());
        villager.getEntityData().setTag(HOME_TAG, home);
        if (!bed.equals(old)) villager.world.setEntityState(villager, (byte) 14);
        return true;
    }

    public static boolean wakeSleepingVillager(World world, BlockPos position) {
        if (world.isRemote || !world.isBlockLoaded(position)) return false;
        IBlockState bed = world.getBlockState(position);
        if (!(bed.getBlock() instanceof BlockBed)) return false;
        BlockPos head = bed.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD
                ? position : position.offset(bed.getValue(BlockBed.FACING));
        if (!world.isBlockLoaded(head) || !isBedHead(world.getBlockState(head))) return false;
        for (EntityVillager villager : world.getEntitiesWithinAABB(EntityVillager.class,
                new AxisAlignedBB(head), entity -> entity.isEntityAlive()
                        && isSleeping(entity) && head.equals(getHome(entity)))) {
            wake(villager);
            FFDVillagerTypes.sync(villager);
            return true;
        }
        return false;
    }

    @Nullable
    public static BlockPos findVacantBed(EntityVillager villager) {
        return findVacantBed(villager, false);
    }

    @Nullable
    public static BlockPos findVacantBed(EntityVillager villager, boolean requireUnclaimed) {
        WorldServer world = (WorldServer) villager.world;
        BlockPos origin = villager.getPosition();
        List<BlockPos> candidates = new ArrayList<>();
        long now = world.getTotalWorldTime();
        Map<BlockPos, Long> failures = FAILED_PATHS.computeIfAbsent(villager, key -> new HashMap<>());
        failures.values().removeIf(expires -> expires <= now);
        for (int chunkX = (origin.getX() - 48) >> 4; chunkX <= (origin.getX() + 48) >> 4; chunkX++) {
            for (int chunkZ = (origin.getZ() - 48) >> 4; chunkZ <= (origin.getZ() + 48) >> 4; chunkZ++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
                if (chunk == null) continue;
                for (BlockPos position : FFDVillagerBedIndex.getBeds(world, chunk)) {
                    if (origin.distanceSq(position) > 48.0D * 48.0D || failures.containsKey(position)
                            || (requireUnclaimed ? homes(villager).isClaimed(position)
                            : homes(villager).isClaimedByOther(position, villager.getUniqueID()))) continue;
                    IBlockState state = chunk.getBlockState(position);
                    if (isBedHead(state) && !state.getValue(BlockBed.OCCUPIED)) candidates.add(position);
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(villager::getDistanceSqToCenter));
        for (int index = 0; index < Math.min(5, candidates.size()); index++) {
            BlockPos candidate = candidates.get(index);
            if (canReach(villager, candidate, 1.5D)) return candidate;
            failures.put(candidate, now + 40L + villager.getRNG().nextInt(361));
        }
        return null;
    }

    public static boolean canReach(EntityVillager villager, BlockPos position, double range) {
        if (!canNavigate(villager, position)) return false;
        Path path = villager.getNavigator().getPathToPos(position);
        if (path == null || path.getFinalPathPoint() == null) return false;
        PathPoint end = path.getFinalPathPoint();
        double offsetX = end.x - position.getX();
        double offsetY = end.y - position.getY();
        double offsetZ = end.z - position.getZ();
        return offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ <= range * range;
    }

    public static boolean canNavigate(EntityVillager villager, BlockPos position) {
        int radius = (int) (villager.getNavigator().getPathSearchRange() + 8.0F);
        BlockPos origin = villager.getPosition();
        return villager.world.isBlockLoaded(position) && villager.world.isAreaLoaded(
                origin.add(-radius, -radius, -radius), origin.add(radius, radius, radius));
    }

    public static boolean isSleeping(EntityVillager villager) {
        return villager.getEntityData().getBoolean(SLEEPING_TAG);
    }

    public static void clientPose(EntityVillager villager) {
        if (!villager.world.isRemote || villager.getClass() != EntityVillager.class) return;
        boolean sleeping = isSleeping(villager);
        boolean wasSleeping = villager.getEntityData().getBoolean("farmers_future_delight:client_sleeping");
        if (!sleeping && !wasSleeping) return;
        villager.getEntityData().setBoolean("farmers_future_delight:client_sleeping", sleeping);
        float scale = villager.isChild() ? 0.5F : 1.0F;
        villager.width = sleeping ? 0.2F : 0.6F * scale;
        villager.height = sleeping ? 0.2F : 1.95F * scale;
        villager.setPosition(villager.posX, villager.posY, villager.posZ);
    }

    public static void abandonUnreachableHome(EntityVillager villager, BlockPos position) {
        if (!position.equals(getHome(villager)) || isSleeping(villager)
                || !canNavigate(villager, position)) return;
        FAILED_PATHS.computeIfAbsent(villager, key -> new HashMap<>()).put(position,
                villager.world.getTotalWorldTime() + 400L);
        release(villager);
    }

    public static boolean startSleeping(EntityVillager villager) {
        BlockPos home = getHome(villager);
        long now = villager.world.getTotalWorldTime();
        NBTTagCompound data = villager.getEntityData();
        if (home == null || villager.isRiding() || villager.isTrading() || !validateHome(villager)
                || !villager.world.isBlockLoaded(home) || villager.getDistanceSqToCenter(home) >= 4.0D
                || data.hasKey(LAST_WOKEN_TAG, 99) && now - data.getLong(LAST_WOKEN_TAG) < 100L) return false;
        IBlockState bed = villager.world.getBlockState(home);
        if (bed.getValue(BlockBed.OCCUPIED)) return false;
        data.setBoolean("farmers_future_delight:sleep_had_no_gravity", villager.hasNoGravity());
        data.setBoolean(SLEEPING_TAG, true);
        data.setLong(LAST_SLEPT_TAG, now);
        villager.world.setBlockState(home, bed.withProperty(BlockBed.OCCUPIED, true), 3);
        holdSleeping(villager);
        FFDVillagerTypes.sync(villager);
        return true;
    }

    public static void holdSleeping(EntityVillager villager) {
        BlockPos home = getHome(villager);
        if (home == null) return;
        villager.getNavigator().clearPath();
        villager.setJumping(false);
        villager.motionX = 0.0D;
        villager.motionY = 0.0D;
        villager.motionZ = 0.0D;
        villager.setNoGravity(true);
        villager.width = 0.2F;
        villager.height = 0.2F;
        villager.setPosition(home.getX() + 0.5D, home.getY() + 0.6875D, home.getZ() + 0.5D);
        villager.setEntityBoundingBox(new AxisAlignedBB(villager.posX - 0.1D, villager.posY,
                villager.posZ - 0.1D, villager.posX + 0.1D, villager.posY + 0.2D, villager.posZ + 0.1D));
    }

    public static void wake(EntityVillager villager) {
        if (!isSleeping(villager)) return;
        BlockPos home = getHome(villager);
        NBTTagCompound data = villager.getEntityData();
        data.setBoolean(SLEEPING_TAG, false);
        data.setLong(LAST_WOKEN_TAG, villager.world.getTotalWorldTime());
        villager.setNoGravity(data.getBoolean("farmers_future_delight:sleep_had_no_gravity"));
        data.removeTag("farmers_future_delight:sleep_had_no_gravity");
        float scale = villager.isChild() ? 0.5F : 1.0F;
        villager.width = 0.6F * scale;
        villager.height = 1.95F * scale;
        villager.setPosition(villager.posX, villager.posY, villager.posZ);
        FFDVillagerTypes.sync(villager);
        if (home == null || !villager.world.isBlockLoaded(home)) return;
        IBlockState bed = villager.world.getBlockState(home);
        if (!isBedHead(bed)) return;
        if (!hasSleepingPlayer(villager, home)) {
            villager.world.setBlockState(home, bed.withProperty(BlockBed.OCCUPIED, false), 3);
        }
        EnumFacing facing = bed.getValue(BlockBed.FACING);
        EnumFacing[] exits = {facing.rotateY(), facing.rotateYCCW(), facing.getOpposite(), facing};
        for (EnumFacing exit : exits) {
            BlockPos position = home.offset(exit);
            if (!villager.world.isBlockLoaded(position.down())
                    || !villager.world.getBlockState(position.down()).isSideSolid(villager.world,
                            position.down(), EnumFacing.UP)) continue;
            AxisAlignedBB bounds = new AxisAlignedBB(position.getX() + 0.5D - villager.width / 2.0D,
                    position.getY(), position.getZ() + 0.5D - villager.width / 2.0D,
                    position.getX() + 0.5D + villager.width / 2.0D, position.getY() + villager.height,
                    position.getZ() + 0.5D + villager.width / 2.0D);
            if (!villager.world.getCollisionBoxes(villager, bounds).isEmpty()
                    || villager.world.containsAnyLiquid(bounds)) continue;
            villager.setPositionAndUpdate(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
            break;
        }
    }

    private static boolean hasSleepingPlayer(EntityVillager villager, BlockPos position) {
        for (EntityPlayer player : villager.world.playerEntities) {
            if (player.isPlayerSleeping() && position.equals(player.bedLocation)) return true;
        }
        return false;
    }

    public static void release(EntityVillager villager) {
        wake(villager);
        BlockPos home = getHome(villager);
        if (home != null) homes(villager).release(home, villager.getUniqueID());
        villager.getEntityData().removeTag(HOME_TAG);
    }

    public static boolean isBedHead(IBlockState state) {
        return state.getBlock() instanceof BlockBed
                && state.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD;
    }

    private static FFDWorkstationData homes(EntityVillager villager) {
        return FFDWorkstationData.getHomes((WorldServer) villager.world);
    }
}
