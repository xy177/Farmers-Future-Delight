package xy177.farmersfuturedelight.common.registry;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDWorkstationLifecycle {
    private static final String WORKSITE_TAG = FarmerFutureDelight.MODID + ".workstation";
    private static final String NEXT_SEARCH_TAG = FarmerFutureDelight.MODID + ".workstationSearchAt";
    private static final int SEARCH_RADIUS = 48;
    private static final int SEARCH_INTERVAL = 20;
    private static final int TRAVEL_LIMIT = 1200;
    private static final Map<EntityVillager, Map<BlockPos, Long>> FAILED_PATHS = new WeakHashMap<>();

    private FFDWorkstationLifecycle() {
    }

    @SubscribeEvent
    public static void onVillagerBorn(BabyEntitySpawnEvent event) {
        if (event.getChild() != null && event.getParentA().getClass() == EntityVillager.class
                && event.getParentB().getClass() == EntityVillager.class
                && event.getChild().getClass() == EntityVillager.class
                && !event.getChild().world.isRemote) {
            if (FFDConfig.modernVillagerSystem) {
                FFDProfessionWorkstations.markProfessionUnassigned((EntityVillager) event.getChild());
            }
            FFDVillagerTypes.inheritType((EntityVillager) event.getChild(),
                    (EntityVillager) event.getParentA(), (EntityVillager) event.getParentB());
        }
    }

    @SubscribeEvent
    public static void onVillagerUpdate(LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityVillager)) return;
        EntityVillager villager = (EntityVillager) event.getEntityLiving();
        if (!FFDConfig.modernVillagerSystem) return;
        FFDVillagerRoutines.tick(villager);
        if (!villager.world.isRemote && villager.isEntityAlive()) FFDVillagerReputation.tick(villager);
        if (villager.world.isRemote || villager.isChild() || !villager.isEntityAlive()) {
            return;
        }
        FFDVillagerProfessionRetention.initialize(villager);
        FFDVillagerVillageMemory.tick(villager);
        validateWorkstation(villager);
        BlockPos workstation = getWorkstation(villager);
        FFDVillagerRestocking.tickWork(villager, workstation);
        if (Math.floorMod(villager.ticksExisted + villager.getEntityId(), 20) != 0) return;
        BlockPos potential = getPotentialWorkstation(villager);
        if (potential != null) {
            if (!villager.world.isBlockLoaded(potential)) return;
            FFDProfessionWorkstations.WorkstationMapping mapping = null;
            NBTTagCompound binding = villager.getEntityData().getCompoundTag(
                    "farmers_future_delight:workstation_binding");
            for (FFDProfessionWorkstations.WorkstationMapping candidate
                    : FFDProfessionWorkstations.findMappings(villager.world.getBlockState(potential))) {
                if (candidate.getProfession().toString().equals(binding.getString("profession"))
                        && candidate.getCareer().equals(binding.getString("career"))) {
                    mapping = candidate;
                    break;
                }
            }
            boolean unassigned = FFDProfessionWorkstations.isProfessionUnassigned(villager);
            if (mapping == null || !unassigned && (!mapping.getProfession().equals(
                    villager.getProfessionForge().getRegistryName())
                    || !mapping.matchesCareer(FFDProfessionWorkstations.getCareerName(villager)))
                    || !workstationData(villager.world).claim(potential, villager.getUniqueID())) {
                clearWorkstation(villager);
            } else if (!villager.isAIDisabled() && !villager.isTrading()
                    && !FFDVillagerRoutines.isPanicking(villager)
                    && villager.getDistanceSqToCenter(potential) < 4.0D) {
                if (unassigned) FFDProfessionWorkstations.assignProfession(villager, mapping, villager.getRNG());
                else FFDProfessionWorkstations.completeExistingBinding(villager, mapping);
                storeWorkstation(villager, potential, mapping, false);
                FAILED_PATHS.remove(villager);
            }
            return;
        }
        if (workstation != null && !villager.world.isBlockLoaded(workstation)) return;
        if (workstation != null && isWorkstationValid(villager, workstation)) {
            if (workstationData(villager.world).claim(workstation, villager.getUniqueID())) {
                return;
            }
        }
        loseWorkstation(villager);
        FFDProfessionWorkstations.resetUntradedAssignedProfession(villager);
        long worldTime = villager.world.getTotalWorldTime();
        NBTTagCompound data = villager.getEntityData();
        if (worldTime < data.getLong(NEXT_SEARCH_TAG)) return;
        data.setLong(NEXT_SEARCH_TAG, worldTime + SEARCH_INTERVAL + villager.getRNG().nextInt(SEARCH_INTERVAL));
        findWorkstation(villager);
    }

    public static void validateWorkstation(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem || villager.world.isRemote || !villager.isEntityAlive()) return;
        BlockPos position = getWorkstation(villager);
        if (position == null || !villager.world.isBlockLoaded(position) || isWorkstationValid(villager, position)) return;
        loseWorkstation(villager);
        FFDProfessionWorkstations.resetUntradedAssignedProfession(villager);
        FFDVillagerTypes.sync(villager);
    }

    private static void findWorkstation(EntityVillager villager) {
        World world = villager.world;
        BlockPos origin = villager.getPosition();
        List<BlockPos> chestCandidates = new ArrayList<>();
        List<BlockPos> candidates = new ArrayList<>();
        long now = world.getTotalWorldTime();
        Map<BlockPos, Long> failures = FAILED_PATHS.computeIfAbsent(villager, ignored -> new HashMap<>());
        failures.values().removeIf(expires -> expires <= now);
        Map<IBlockState, List<FFDProfessionWorkstations.WorkstationMapping>> stateMappings = new HashMap<>();
        for (int chunkX = (origin.getX() - SEARCH_RADIUS) >> 4;
                chunkX <= (origin.getX() + SEARCH_RADIUS) >> 4; chunkX++) {
            for (int chunkZ = (origin.getZ() - SEARCH_RADIUS) >> 4;
                    chunkZ <= (origin.getZ() + SEARCH_RADIUS) >> 4; chunkZ++) {
                Chunk chunk = ((WorldServer) world).getChunkProvider().getLoadedChunk(chunkX, chunkZ);
                if (chunk == null) continue;
                for (BlockPos position : FFDWorkstationIndex.getWorkstations((WorldServer) world, chunk)) {
                    if (origin.distanceSq(position) > SEARCH_RADIUS * SEARCH_RADIUS
                            || failures.containsKey(position)) continue;
                    IBlockState state = world.getBlockState(position);
                    if (isClaimedByOther(villager, position)) continue;
                    List<FFDProfessionWorkstations.WorkstationMapping> mappings = stateMappings.computeIfAbsent(state, candidate -> {
                        if (FFDProfessionWorkstations.isProfessionUnassigned(villager)) {
                            return FFDProfessionWorkstations.findMappings(candidate);
                        }
                        FFDProfessionWorkstations.WorkstationMapping mapping =
                                FFDProfessionWorkstations.findMapping(villager, candidate);
                        return mapping == null ? java.util.Collections.emptyList()
                                : java.util.Collections.singletonList(mapping);
                    });
                    if (mappings.isEmpty()) continue;
                    if (state.getBlock() == Blocks.CHEST) chestCandidates.add(position);
                    else candidates.add(position);
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(villager::getDistanceSqToCenter));
        chestCandidates.sort(Comparator.comparingDouble(villager::getDistanceSqToCenter));
        if (candidates.isEmpty()) candidates = chestCandidates;
        for (int index = 0; index < Math.min(5, candidates.size()); index++) {
            BlockPos position = candidates.get(index);
            if (tryBind(villager, position, world.getBlockState(position))) return;
            failures.put(position, now + 40L + villager.getRNG().nextInt(361));
        }
    }

    private static boolean tryBind(EntityVillager villager, BlockPos position, IBlockState state,
                                    int population, int chestFallbackPopulation) {
        return tryBind(villager, position, state);
    }

    private static boolean tryBind(EntityVillager villager, BlockPos position, IBlockState state) {
        FFDProfessionWorkstations.WorkstationMapping mapping =
                FFDProfessionWorkstations.isProfessionUnassigned(villager)
                        ? FFDProfessionWorkstations.getOrChooseBinding(villager, position, state, villager.getRNG())
                        : FFDProfessionWorkstations.findMapping(villager, state);
        if (mapping == null || !workstationData(villager.world).claim(position, villager.getUniqueID())) return false;
        boolean unassigned = FFDProfessionWorkstations.isProfessionUnassigned(villager);
        if (unassigned || FFDVillagerProfessionRetention.isProtected(villager)) {
            if (villager.getDistanceSqToCenter(position) >= 4.0D) {
                FFDProfessionWorkstations.rememberBinding(villager, position, mapping);
                storeWorkstation(villager, position, mapping, true);
                return true;
            }
            if (unassigned) FFDProfessionWorkstations.assignProfession(villager, mapping, villager.getRNG());
            else FFDProfessionWorkstations.completeExistingBinding(villager, mapping);
        }
        FFDProfessionWorkstations.rememberBinding(villager, position, mapping);
        storeWorkstation(villager, position, mapping, false);
        FAILED_PATHS.remove(villager);
        return true;
    }

    @Nullable
    public static Path findWorkstationPath(EntityVillager villager, BlockPos position) {
        if (!FFDConfig.modernVillagerSystem) return null;
        if (!FFDVillagerHomes.canNavigate(villager, position)) return null;
        Path path = villager.getNavigator().getPathToPos(position.up());
        if (reachesWorkstation(path, position)) return path;
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            path = villager.getNavigator().getPathToPos(position.offset(facing));
            if (reachesWorkstation(path, position)) return path;
        }
        return null;
    }

    private static boolean reachesWorkstation(@Nullable Path path, BlockPos position) {
        if (path == null || path.getFinalPathPoint() == null) return false;
        net.minecraft.pathfinding.PathPoint end = path.getFinalPathPoint();
        double offsetX = end.x + 0.5D - (position.getX() + 0.5D);
        double offsetY = end.y - (position.getY() + 0.5D);
        double offsetZ = end.z + 0.5D - (position.getZ() + 0.5D);
        return offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ < 1.73D * 1.73D;
    }

    private static boolean isClaimedByOther(EntityVillager villager, BlockPos position) {
        return workstationData(villager.world).isClaimedByOther(position,
                villager.getUniqueID());
    }

    private static FFDWorkstationData workstationData(World world) {
        return FFDWorkstationData.get((net.minecraft.world.WorldServer) world);
    }

    public static BlockPos getWorkstation(EntityVillager villager) {
        NBTTagCompound site = villager.getEntityData().getCompoundTag(WORKSITE_TAG);
        if (!site.hasKey("x") || site.getInteger("dimension") != villager.dimension
                || site.getBoolean("pending")) return null;
        return new BlockPos(site.getInteger("x"), site.getInteger("y"), site.getInteger("z"));
    }

    public static BlockPos getPotentialWorkstation(EntityVillager villager) {
        NBTTagCompound site = villager.getEntityData().getCompoundTag(WORKSITE_TAG);
        if (!site.hasKey("x") || site.getInteger("dimension") != villager.dimension
                || !site.getBoolean("pending")) return null;
        return new BlockPos(site.getInteger("x"), site.getInteger("y"), site.getInteger("z"));
    }

    static boolean isWorkstationValid(EntityVillager villager, BlockPos position) {
        IBlockState state = villager.world.getBlockState(position);
        FFDProfessionWorkstations.WorkstationMapping mapping =
                FFDProfessionWorkstations.findMapping(villager, state);
        if (mapping == null) return false;
        NBTTagCompound binding = villager.getEntityData().getCompoundTag(
                "farmers_future_delight:workstation_binding");
        return binding.getInteger("dimension") == villager.dimension
                && binding.getInteger("x") == position.getX()
                && binding.getInteger("y") == position.getY()
                && binding.getInteger("z") == position.getZ()
                && binding.getString("profession").equals(mapping.getProfession().toString())
                && binding.getString("career").equals(mapping.getCareer());
    }

    private static void storeWorkstation(EntityVillager villager, BlockPos position,
                                         FFDProfessionWorkstations.WorkstationMapping mapping, boolean pending) {
        boolean newlyBound = !pending && !position.equals(getWorkstation(villager));
        NBTTagCompound site = new NBTTagCompound();
        site.setInteger("dimension", villager.dimension);
        site.setInteger("x", position.getX());
        site.setInteger("y", position.getY());
        site.setInteger("z", position.getZ());
        site.setBoolean("chestFallback", mapping.usesOrdinaryChestFallback());
        site.setBoolean("pending", pending);
        villager.getEntityData().setTag(WORKSITE_TAG, site);
        if (newlyBound) villager.world.setEntityState(villager, (byte) 14);
        FFDVillagerTypes.sync(villager);
    }

    public static boolean tickWorkstationTravel(EntityVillager villager, BlockPos position) {
        if (!FFDConfig.modernVillagerSystem) return false;
        BlockPos target = getPotentialWorkstation(villager);
        boolean pending = target != null;
        if (!pending) target = getWorkstation(villager);
        if (!position.equals(target)) return false;
        NBTTagCompound site = villager.getEntityData().getCompoundTag(WORKSITE_TAG);
        if (villager.getDistanceSqToCenter(position) < (pending ? 4.0D : 1.73D * 1.73D)) {
            site.removeTag("travelTicks");
            return true;
        }
        if (!FFDVillagerHomes.canNavigate(villager, position)) return true;
        int travelTicks = site.getInteger("travelTicks") + 1;
        if (travelTicks < TRAVEL_LIMIT) {
            site.setInteger("travelTicks", travelTicks);
            return true;
        }
        long retryAt = villager.world.getTotalWorldTime() + 400L;
        Map<BlockPos, Long> failures = FAILED_PATHS.computeIfAbsent(villager, ignored -> new HashMap<>());
        failures.replaceAll((failed, expires) -> Math.max(expires, retryAt));
        failures.put(position.toImmutable(), retryAt);
        loseWorkstation(villager);
        FFDProfessionWorkstations.resetUntradedAssignedProfession(villager);
        villager.getNavigator().clearPath();
        return false;
    }

    private static void loseWorkstation(EntityVillager villager) {
        boolean bound = getWorkstation(villager) != null;
        clearWorkstation(villager);
        if (bound && villager.isEntityAlive()
                && xy177.farmersfuturedelight.common.FFDConfig.villagerWorkstationLossParticles) {
            villager.world.setEntityState(villager, (byte) 13);
        }
    }

    public static void clearWorkstation(EntityVillager villager) {
        NBTTagCompound data = villager.getEntityData();
        if (!data.hasKey(WORKSITE_TAG)) return;
        BlockPos position = getWorkstation(villager);
        if (position == null) position = getPotentialWorkstation(villager);
        if (position != null) {
            workstationData(villager.world).release(position, villager.getUniqueID());
        }
        data.removeTag(WORKSITE_TAG);
        FFDProfessionWorkstations.clearBinding(villager);
    }

    @SubscribeEvent(priority = net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST)
    public static void onWorkstationBroken(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote && event.getWorld() instanceof World) {
            World world = (World) event.getWorld();
            workstationData(world).releaseAt(event.getPos());
        }
    }

    @SubscribeEvent(priority = net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST)
    public static void onVillagerDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityVillager)
                || event.getEntityLiving().world.isRemote) return;
        EntityVillager villager = (EntityVillager) event.getEntityLiving();
        if (villager.getClass() == EntityVillager.class) FFDVillagerHomes.release(villager);
        clearWorkstation(villager);
    }

    @SubscribeEvent(priority = net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST)
    public static void onVillagerTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof EntityVillager && !event.getEntity().world.isRemote) {
            if (event.getEntity().getClass() == EntityVillager.class) {
                FFDVillagerHomes.release((EntityVillager) event.getEntity());
            }
            clearWorkstation((EntityVillager) event.getEntity());
        }
    }
}
