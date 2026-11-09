package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFollowGolem;
import net.minecraft.entity.ai.EntityAIHarvestFarmland;
import net.minecraft.entity.ai.EntityAIPlay;
import net.minecraft.entity.ai.EntityAIVillagerInteract;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.monster.EntityIllusionIllager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.BlockBell;
import xy177.farmersfuturedelight.common.entity.EntityDrowned;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.entity.EntityRavager;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerBreed;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerRoutine;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerFarm;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerJumpOnBed;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerSocial;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerShowTrades;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDVillagerRoutines {
    private static final Set<EntityVillager> INSTALLED = Collections.newSetFromMap(new WeakHashMap<>());
    private static final String NEXT_HOME_SEARCH = "farmers_future_delight:home_search_at";
    private static final String MEETING_TAG = "farmers_future_delight:meeting";
    private static final String NEXT_MEETING_SEARCH = "farmers_future_delight:meeting_search_at";
    private static final String HURT_UNTIL = "farmers_future_delight:hurt_until";

    public enum Activity {
        IDLE, WORK, MEET, REST, PLAY, PANIC
    }

    private FFDVillagerRoutines() {
    }

    public static float navigationRange(EntityLiving entity, float original) {
        if (!FFDConfig.modernVillagerSystem || entity.getClass() != EntityVillager.class || original >= 48.0F) {
            return original;
        }
        BlockPos position = entity.getPosition();
        return entity.world.isAreaLoaded(position.add(-56, -56, -56), position.add(56, 56, 56))
                ? 48.0F : original;
    }

    public static int navigationBudget(EntityLiving entity, float range) {
        if (!FFDConfig.modernVillagerSystem) return 200;
        return entity.getClass() == EntityVillager.class ? Math.max(256, (int) (range * 16.0F)) : 200;
    }

    public static float navigationStep(PathPoint from, PathPoint to, int budget) {
        if (!FFDConfig.modernVillagerSystem) return from.distanceManhattan(to);
        return budget == 200 ? from.distanceManhattan(to) : from.distanceTo(to);
    }

    public static void onRemoved(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem
                || villager.getClass() != EntityVillager.class || !(villager.world instanceof WorldServer)) return;
        FFDVillagerHomes.release(villager);
        FFDWorkstationLifecycle.clearWorkstation(villager);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && event.getEntity().getClass() == EntityVillager.class) {
            EntityVillager villager = (EntityVillager) event.getEntity();
            if (!FFDConfig.modernVillagerSystem) {
                FFDProfessionWorkstations.restoreLegacyState(villager);
                FFDWorkstationLifecycle.clearWorkstation(villager);
                return;
            }
            install(villager);
        }
    }

    private static void install(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return;
        if (!INSTALLED.add(villager)) return;
        for (EntityAITasks.EntityAITaskEntry task : new ArrayList<>(villager.tasks.taskEntries)) {
            Class<?> type = task.action.getClass();
            if (type == EntityAIMoveIndoors.class || type == EntityAIRestrictOpenDoor.class
                    || type == EntityAIMoveTowardsRestriction.class || type == EntityAIVillagerMate.class
                    || type == EntityAIFollowGolem.class
                    || (type == EntityAIHarvestFarmland.class || type == EntityAIPlay.class)
                    && (FFDModernVillagerTrades.canManage(villager)
                    || FFDProfessionWorkstations.isProfessionUnassigned(villager))
                    || type == EntityAIVillagerInteract.class && FFDVillagerRestocking.isManaged(villager)) {
                villager.tasks.removeTask(task.action);
            }
        }
        villager.tasks.addTask(1, new EntityAIAvoidEntity<>(villager, EntityPillager.class, 15.0F, 0.75D, 0.75D));
        villager.tasks.addTask(1, new EntityAIAvoidEntity<>(villager, EntityRavager.class, 12.0F, 0.75D, 0.75D));
        villager.tasks.addTask(1, new EntityAIAvoidEntity<>(villager, EntityDrowned.class, 8.0F, 0.75D, 0.75D));
        villager.tasks.addTask(1, new EntityAIAvoidEntity<>(villager, EntityIllusionIllager.class, 12.0F, 0.75D, 0.75D));
        villager.tasks.addTask(4, new EntityAIVillagerShowTrades(villager));
        villager.tasks.addTask(5, new EntityAIVillagerRoutine(villager));
        villager.tasks.addTask(6, new EntityAIVillagerBreed(villager));
        villager.tasks.addTask(6, new EntityAIVillagerFarm(villager));
        villager.tasks.addTask(7, new EntityAIVillagerSocial(villager));
        villager.tasks.addTask(8, new EntityAIVillagerJumpOnBed(villager));
    }

    public static Activity getActivity(EntityVillager villager) {
        if (isPanicking(villager)) return Activity.PANIC;
        if (!villager.world.provider.isSurfaceWorld()) return Activity.IDLE;
        long time = Math.floorMod(villager.world.getWorldTime(), 24000L);
        if (time >= 12000L || time < 10L) return Activity.REST;
        if (villager.isChild()) {
            return time >= 3000L && time < 6000L || time >= 10000L ? Activity.PLAY : Activity.IDLE;
        }
        if (time >= 2000L && time < 9000L && !FFDProfessionWorkstations.isProfessionUnassigned(villager)) {
            return Activity.WORK;
        }
        if (time >= 9000L && time < 11000L) return Activity.MEET;
        return Activity.IDLE;
    }

    public static boolean isPanicking(EntityVillager villager) {
        if (FFDVillagerGolems.isHostileNearby(villager)) return true;
        long now = villager.world.getTotalWorldTime();
        if (now < villager.getEntityData().getLong(HURT_UNTIL)) return true;
        EntityLivingBase attacker = villager.getRevengeTarget();
        return attacker != null && attacker.isEntityAlive() && villager.getDistanceSq(attacker) <= 36.0D;
    }

    public static void tick(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem
                || villager.getClass() != EntityVillager.class || !villager.isEntityAlive()) return;
        if (villager.world.isRemote) {
            FFDVillagerHomes.clientPose(villager);
            return;
        }
        if (villager.isAIDisabled()) {
            if (FFDVillagerHomes.isSleeping(villager)) FFDVillagerHomes.wake(villager);
            if (Math.floorMod(villager.ticksExisted + villager.getEntityId(), 20) == 0) FFDVillagerTypes.sync(villager);
            return;
        }
        install(villager);
        long now = villager.world.getTotalWorldTime();
        NBTTagCompound data = villager.getEntityData();
        if (FFDVillagerHomes.isSleeping(villager)) {
            if (getActivity(villager) != Activity.REST || !FFDVillagerHomes.validateHome(villager)
                    || villager.isTrading() || villager.isInWater() || villager.isRiding()) {
                FFDVillagerHomes.wake(villager);
            } else {
                FFDVillagerHomes.holdSleeping(villager);
            }
        }
        if (Math.floorMod(villager.ticksExisted + villager.getEntityId(), 20) != 0) return;
        FFDVillagerTypes.sync(villager);
        if (FFDModernVillagerTrades.canManage(villager) || FFDProfessionWorkstations.isProfessionUnassigned(villager)) {
            for (EntityAITasks.EntityAITaskEntry task : new ArrayList<>(villager.tasks.taskEntries)) {
                if (task.action.getClass() == EntityAIHarvestFarmland.class || task.action.getClass() == EntityAIPlay.class
                        || task.action.getClass() == EntityAIVillagerInteract.class
                        && FFDVillagerRestocking.isManaged(villager)) villager.tasks.removeTask(task.action);
            }
        }
        if (!FFDVillagerHomes.validateHome(villager) && now >= data.getLong(NEXT_HOME_SEARCH)) {
            data.setLong(NEXT_HOME_SEARCH, now + 200L + villager.getRNG().nextInt(200));
            BlockPos bed = FFDVillagerHomes.findVacantBed(villager);
            if (bed != null) FFDVillagerHomes.bind(villager, bed);
        }
        if (getActivity(villager) == Activity.MEET) findMeeting(villager, now);
        FFDVillagerGolems.tick(villager);
    }

    @Nullable
    public static BlockPos getMeeting(EntityVillager villager) {
        NBTTagCompound meeting = villager.getEntityData().getCompoundTag(MEETING_TAG);
        if (!meeting.hasKey("position", 99) || meeting.getInteger("dimension") != villager.dimension) return null;
        BlockPos position = BlockPos.fromLong(meeting.getLong("position"));
        if (!villager.world.isBlockLoaded(position)) return null;
        return villager.world.getBlockState(position).getBlock() instanceof BlockBell ? position : null;
    }

    private static void findMeeting(EntityVillager villager, long now) {
        NBTTagCompound data = villager.getEntityData();
        if (getMeeting(villager) != null || now < data.getLong(NEXT_MEETING_SEARCH)) return;
        data.setLong(NEXT_MEETING_SEARCH, now + 200L + villager.getRNG().nextInt(200));
        WorldServer world = (WorldServer) villager.world;
        BlockPos origin = villager.getPosition();
        BlockPos nearest = null;
        double distance = 48.0D * 48.0D;
        for (int chunkX = (origin.getX() - 48) >> 4; chunkX <= (origin.getX() + 48) >> 4; chunkX++) {
            for (int chunkZ = (origin.getZ() - 48) >> 4; chunkZ <= (origin.getZ() + 48) >> 4; chunkZ++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
                if (chunk == null) continue;
                for (BlockPos position : chunk.getTileEntityMap().keySet()) {
                    double candidateDistance = origin.distanceSq(position);
                    if (candidateDistance >= distance || !(chunk.getBlockState(position).getBlock() instanceof BlockBell)) continue;
                    nearest = position;
                    distance = candidateDistance;
                }
            }
        }
        if (nearest == null || !FFDVillagerHomes.canReach(villager, nearest, 6.0D)) return;
        NBTTagCompound meeting = new NBTTagCompound();
        meeting.setInteger("dimension", villager.dimension);
        meeting.setLong("position", nearest.toLong());
        data.setTag(MEETING_TAG, meeting);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHurt(LivingHurtEvent event) {
        if (event.getAmount() <= 0.0F || event.getEntityLiving().getClass() != EntityVillager.class
                || event.getEntityLiving().world.isRemote) return;
        EntityVillager villager = (EntityVillager) event.getEntityLiving();
        villager.getEntityData().setLong(HURT_UNTIL, villager.world.getTotalWorldTime() + 100L);
        FFDVillagerHomes.wake(villager);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBedBroken(BlockEvent.BreakEvent event) {
        if (!(event.getWorld() instanceof WorldServer) || !(event.getState().getBlock() instanceof BlockBed)) return;
        IBlockState bed = event.getState();
        BlockPos head = bed.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD ? event.getPos()
                : event.getPos().offset(bed.getValue(BlockBed.FACING));
        FFDWorkstationData.getHomes((WorldServer) event.getWorld()).releaseAt(head);
    }
}
