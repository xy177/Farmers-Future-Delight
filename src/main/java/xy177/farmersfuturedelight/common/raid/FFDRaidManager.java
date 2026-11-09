package xy177.farmersfuturedelight.common.raid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.raid.RaidEventCallbacks.Outcome;
import xy177.farmersfuturedelight.api.raid.RaidEventContext;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition;
import xy177.farmersfuturedelight.api.raid.RaidEventRegistry;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDRaidManager extends WorldSavedData {
    static final Logger LOGGER = LogManager.getLogger("FFD Raids");
    static final String MEMBER_TAG = "farmers_future_delight:raid_member";
    private static final String DATA_NAME = "farmers_future_delight_raids";
    private static final Map<WorldServer, Boolean> MANAGER_CACHE = new WeakHashMap<>();
    private final Map<UUID, RaidInstance> instances = new LinkedHashMap<>();
    private final Map<UUID, RaidInstance> members = new LinkedHashMap<>();
    private final java.util.Set<UUID> discarded = new java.util.LinkedHashSet<>();
    private NBTTagList pending = new NBTTagList();
    private WorldServer world;

    public FFDRaidManager() { super(DATA_NAME); }
    public FFDRaidManager(String name) { super(name); }

    public static Collection<RaidEventDefinition> defaultDefinitions() {
        RaidEventDefinition definition = RaidDefaults.definition();
        return definition == null ? Collections.emptyList() : Collections.singletonList(definition);
    }

    public static boolean canAcceptOmen(WorldServer world, BlockPos position) {
        requireThread(world);
        Objects.requireNonNull(position, "position");
        if (!RaidEventConfiguration.isLoaded() || !enabled(world) || !world.isBlockLoaded(position)
                || !RaidVillages.isVillage(world, position) || !world.getWorldBorder().contains(position)) return false;
        FFDRaidManager manager = getIfPresent(world);
        RaidInstance existing = manager == null ? null
                : manager.nearby(RaidVillages.center(world, position), RaidDefaults.ID, true);
        if (existing != null) return existing.isOngoing() && existing.omenLevel < 5;
        RaidEventDefinition definition = RaidEventRegistry.get(RaidDefaults.ID);
        if (definition == null) return false;
        int minimum = Math.max(1, definition.getTrigger().getMinOmenLevel());
        return minimum <= Math.min(5, definition.getTrigger().getMaxOmenLevel())
                && !definition.getWaves(world.getDifficulty()).isEmpty()
                && definition.getTrigger().matches(context(world, UUID.randomUUID(), definition,
                        RaidDefaults.OMEN, RaidVillages.center(world, position), null, minimum));
    }

    public static boolean createOrExtend(EntityPlayerMP player, BlockPos position, int omenLevel) {
        Objects.requireNonNull(player, "player");
        WorldServer world = player.getServerWorld();
        requireThread(world);
        if (omenLevel < 1 || omenLevel > 5 || player.isSpectator() || !player.isEntityAlive()
                || !canAcceptOmen(world, position)) return false;
        FFDRaidManager manager = get(world);
        BlockPos center = RaidVillages.center(world, position);
        RaidInstance existing = manager.nearby(center, RaidDefaults.ID, true);
        if (existing != null) {
            int nextLevel = Math.min(5, existing.omenLevel + omenLevel);
            if (!existing.accepts(player.getUniqueID(), nextLevel)) return false;
            existing.omenLevel = nextLevel;
            manager.markDirty();
            return true;
        }
        return trigger(world, RaidDefaults.ID, RaidDefaults.OMEN, center, player.getUniqueID(), omenLevel) != null;
    }

    @Nullable
    public static UUID trigger(WorldServer world, ResourceLocation eventId, ResourceLocation triggerType,
                               BlockPos center, @Nullable UUID triggerPlayer, int omenLevel) {
        requireThread(world);
        RaidEventDefinition.identifier(eventId);
        RaidEventDefinition.identifier(triggerType);
        Objects.requireNonNull(center, "center");
        if (!RaidEventConfiguration.isLoaded() || !enabled(world) || !world.isBlockLoaded(center)
                || !world.getWorldBorder().contains(center) || omenLevel < 0 || omenLevel > 255) return null;
        RaidEventDefinition definition = RaidEventRegistry.get(eventId);
        if (definition == null) return null;
        xy177.farmersfuturedelight.api.raid.RaidScriptRules rules = definition.getScriptRules();
        if (rules != null && rules.hasFixedCenter()) {
            if (rules.hasDimension() && rules.dimension() != world.provider.getDimension()) return null;
            center = rules.fixedCenter();
            if (!world.isBlockLoaded(center) || !world.getWorldBorder().contains(center)) return null;
        }
        FFDRaidManager manager = getIfPresent(world);
        if (manager != null && manager.nearby(center, eventId, RaidDefaults.ID.equals(eventId)) != null) return null;
        UUID id = UUID.randomUUID();
        try {
            if (!definition.canTrigger(context(world, id, definition, triggerType, center, triggerPlayer, omenLevel))) return null;
        } catch (RuntimeException exception) {
            LOGGER.error("Raid trigger callback failed for {} / {}", eventId, id, exception);
            return null;
        }
        manager = get(world);
        if (manager.nearby(center, eventId, RaidDefaults.ID.equals(eventId)) != null) return null;
        RaidInstance instance;
        try {
            instance = new RaidInstance(manager, id, definition, triggerType, center, triggerPlayer, omenLevel);
        } catch (RuntimeException exception) {
            LOGGER.error("Rejected non-persistable raid definition {} / {}", eventId, id, exception);
            return null;
        }
        manager.instances.put(id, instance);
        manager.markDirty();
        try {
            instance.start();
        } catch (RuntimeException exception) {
            instance.fail(exception);
            return null;
        }
        return id;
    }

    public static boolean isParticipant(EntityLivingBase entity) {
        if (entity == null || !(entity.world instanceof WorldServer)) return false;
        WorldServer world = (WorldServer) entity.world;
        requireThread(world);
        FFDRaidManager manager = RaidEventConfiguration.isLoaded() ? getIfPresent(world) : null;
        return manager != null && manager.members.containsKey(entity.getUniqueID());
    }

    public static boolean join(EntityLiving entity) {
        if (entity == null || !(entity.world instanceof WorldServer)) return false;
        WorldServer world = (WorldServer) entity.world;
        requireThread(world);
        if (!RaidEventConfiguration.isLoaded() || entity.getIdleTime() > 2400 || !FFDRaidCaptains.isRaider(entity)) return false;
        FFDRaidManager manager = getIfPresent(world);
        if (manager == null) return false;
        RaidInstance instance = manager.nearby(entity.getPosition(), RaidDefaults.ID, false);
        return instance != null && addParticipant(instance.id, entity, FFDRaidCaptains.isLeader(entity));
    }

    public static boolean addParticipant(UUID instanceId, EntityLiving entity, boolean leader) {
        Objects.requireNonNull(instanceId, "instanceId");
        if (entity == null || !(entity.world instanceof WorldServer)) return false;
        WorldServer world = (WorldServer) entity.world;
        requireThread(world);
        if (!RaidEventConfiguration.isLoaded() || !enabled(world) || !entity.isEntityAlive()
                || world.getEntityFromUuid(entity.getUniqueID()) != entity) return false;
        FFDRaidManager manager = getIfPresent(world);
        if (manager == null) return false;
        RaidInstance instance = manager.instances.get(instanceId);
        if (instance == null || !instance.isOngoing() || instance.wave == 0 || manager.members.containsKey(entity.getUniqueID())) return false;
        try {
            return instance.add(entity, leader);
        } catch (RuntimeException exception) {
            instance.fail(exception);
            return false;
        }
    }

    public static boolean hasActiveRaid(WorldServer world, BlockPos position) {
        requireThread(world);
        if (!RaidEventConfiguration.isLoaded()) return false;
        FFDRaidManager manager = getIfPresent(world);
        RaidInstance instance = manager == null ? null : manager.nearby(position, null, true);
        return instance != null && instance.isOngoing();
    }

    public static boolean hasWonRaid(WorldServer world, BlockPos position) {
        requireThread(world);
        if (!RaidEventConfiguration.isLoaded()) return false;
        FFDRaidManager manager = getIfPresent(world);
        RaidInstance instance = manager == null ? null : manager.nearby(position, null, true);
        return instance != null && instance.outcome == Outcome.VICTORY;
    }

    public static boolean isPreparingRaid(WorldServer world, BlockPos position) {
        requireThread(world);
        if (!RaidEventConfiguration.isLoaded()) return false;
        FFDRaidManager manager = getIfPresent(world);
        RaidInstance instance = manager == null ? null : manager.nearby(position, null, true);
        return instance != null && instance.isOngoing() && instance.isPreparing();
    }

    @Nullable
    public static RaidEventContext getContext(EntityLivingBase entity) {
        if (entity == null || !(entity.world instanceof WorldServer)) return null;
        WorldServer world = (WorldServer) entity.world;
        requireThread(world);
        if (!RaidEventConfiguration.isLoaded()) return null;
        FFDRaidManager manager = getIfPresent(world);
        RaidInstance instance = manager == null ? null : manager.members.get(entity.getUniqueID());
        return instance == null ? null : instance.context();
    }

    public static Map<UUID, RaidEventContext> snapshot(WorldServer world) {
        requireThread(world);
        Map<UUID, RaidEventContext> result = new LinkedHashMap<>();
        if (RaidEventConfiguration.isLoaded()) {
            FFDRaidManager manager = getIfPresent(world);
            if (manager != null) {
                for (RaidInstance instance : manager.instances.values()) result.put(instance.id, instance.context());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    public static boolean stop(WorldServer world, UUID instanceId) {
        requireThread(world);
        if (!RaidEventConfiguration.isLoaded()) return false;
        FFDRaidManager manager = getIfPresent(world);
        RaidInstance instance = manager == null ? null : manager.instances.get(Objects.requireNonNull(instanceId, "instanceId"));
        if (instance == null || !instance.isOngoing()) return false;
        try {
            instance.finish(Outcome.STOPPED);
        } catch (RuntimeException exception) {
            instance.fail(exception);
        }
        return true;
    }

    static void requireThread(WorldServer world) {
        Objects.requireNonNull(world, "world");
        if (!world.isCallingFromMinecraftThread()) throw new IllegalStateException("Raid world access requires the server thread");
    }

    static boolean enabled(WorldServer world) {
        RaidGameRules.ensure(world.getGameRules());
        return world.getGameRules().getBoolean(RaidGameRules.RAIDS) && world.getDifficulty() != EnumDifficulty.PEACEFUL;
    }

    static FFDRaidManager get(WorldServer world) {
        requireThread(world);
        FFDRaidManager data = getIfPresent(world);
        if (data == null) {
            data = new FFDRaidManager();
            data.attach(world);
            world.getPerWorldStorage().setData(DATA_NAME, data);
            MANAGER_CACHE.put(world, true);
        }
        return data;
    }

    @Nullable
    private static FFDRaidManager getIfPresent(WorldServer world) {
        requireThread(world);
        Boolean known = MANAGER_CACHE.get(world);
        if (Boolean.FALSE.equals(known)) return null;
        FFDRaidManager data = (FFDRaidManager) world.getPerWorldStorage().getOrLoadData(FFDRaidManager.class, DATA_NAME);
        if (data == null) {
            MANAGER_CACHE.put(world, false);
            return null;
        }
        MANAGER_CACHE.put(world, true);
        if (data.world == null) data.attach(world);
        return data;
    }

    WorldServer world() { return world; }

    boolean owns(UUID entity) { return members.containsKey(entity); }

    void index(RaidInstance instance, UUID entity) {
        RaidInstance previous = members.get(entity);
        if (previous != null && previous != instance) throw new IllegalStateException("Entity already belongs to another raid " + entity);
        members.put(entity, instance);
        markDirty();
    }

    void unindex(UUID entity) { members.remove(entity); markDirty(); }

    void discard(UUID entity) { discarded.add(entity); markDirty(); }

    static List<RaidInstance> forPlayer(EntityPlayerMP player, @Nullable ResourceLocation eventId) {
        WorldServer world = player.getServerWorld();
        requireThread(world);
        List<RaidInstance> result = new ArrayList<>();
        FFDRaidManager manager = RaidEventConfiguration.isLoaded() ? getIfPresent(world) : null;
        if (manager != null) for (RaidInstance instance : manager.instances.values()) {
            if (instance.isOngoing() && (eventId == null || eventId.equals(instance.definition.getId())) && instance.associated(player)) result.add(instance);
        }
        return result;
    }

    @Nullable
    RaidInstance nearby(BlockPos position, @Nullable ResourceLocation eventId, boolean includeFinished) {
        RaidInstance closest = null;
        double distance = Double.MAX_VALUE;
        for (RaidInstance instance : instances.values()) {
            if (instance.retired || instance.outcome == Outcome.STOPPED || !world.isBlockLoaded(instance.center)
                    || (!includeFinished && !instance.isOngoing())
                    || (eventId != null && !instance.definition.getId().equals(eventId))) continue;
            double candidate = instance.center.distanceSq(position);
            double radius = instance.definition.getRadius();
            if (instance.contains(position) && candidate < distance) {
                closest = instance;
                distance = candidate;
            }
        }
        return closest;
    }

    static boolean isTicking(WorldServer world, BlockPos position) {
        int chunkX = position.getX() >> 4;
        int chunkZ = position.getZ() >> 4;
        ChunkProviderServer provider = world.getChunkProvider();
        Chunk chunk = provider.id2ChunkMap.get(ChunkPos.asLong(chunkX, chunkZ));
        if (chunk == null) return false;
        boolean unloadQueued = chunk.unloadQueued;
        chunk = provider.getLoadedChunk(chunkX, chunkZ);
        if (chunk == null) return false;
        chunk.unloadQueued = unloadQueued;
        return chunk.isLoaded() && chunk.wasTicked() && !chunk.unloadQueued;
    }

    private static RaidEventContext context(WorldServer world, UUID id, RaidEventDefinition definition,
                                            ResourceLocation trigger, BlockPos center, @Nullable UUID player, int level) {
        return new RaidEventContext(world, id, definition.getId(), trigger, center,
                Objects.requireNonNull(world.getBiome(center).getRegistryName()), player, level, 0, 0L, 0,
                RaidVillages.isVillage(world, center), Collections.emptySet());
    }

    private void attach(WorldServer target) {
        world = target;
        for (int index = 0; index < pending.tagCount(); index++) {
            try {
                RaidInstance instance = RaidInstance.read(this, pending.getCompoundTagAt(index));
                if (instances.containsKey(instance.id)) throw new IllegalArgumentException("Duplicate saved raid " + instance.id);
                for (UUID member : instance.memberIds()) if (members.containsKey(member)) throw new IllegalArgumentException("Duplicate saved participant " + member);
                instances.put(instance.id, instance);
                for (UUID member : instance.memberIds()) members.put(member, instance);
            } catch (RuntimeException exception) {
                LOGGER.error("Stopped invalid or missing-definition saved raid at entry {}", index, exception);
                markDirty();
            }
        }
        pending = new NBTTagList();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if (world != null) throw new IllegalStateException("Cannot replace a live raid manager");
        if (tag.getInteger("Version") != 1) {
            LOGGER.error("Unsupported raid save version {}; entries were not loaded", tag.getInteger("Version"));
            return;
        }
        pending = (NBTTagList) tag.getTagList("Instances", 10).copy();
        NBTTagList removed = tag.getTagList("Discarded", 8);
        for (int index = 0; index < removed.tagCount(); index++) {
            try {
                discarded.add(UUID.fromString(removed.getStringTagAt(index)));
            } catch (IllegalArgumentException exception) {
                LOGGER.error("Invalid discarded raid participant", exception);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setInteger("Version", 1);
        NBTTagList entries = new NBTTagList();
        if (world == null) entries = (NBTTagList) pending.copy();
        else for (RaidInstance instance : instances.values()) if (!instance.retired) entries.appendTag(instance.write());
        tag.setTag("Instances", entries);
        NBTTagList removed = new NBTTagList();
        for (UUID entity : discarded) removed.appendTag(new net.minecraft.nbt.NBTTagString(entity.toString()));
        tag.setTag("Discarded", removed);
        return tag;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer) || !RaidEventConfiguration.isLoaded()) return;
        FFDRaidManager manager = getIfPresent((WorldServer) event.world);
        if (manager == null) return;
        for (RaidInstance instance : new ArrayList<>(manager.instances.values())) {
            try {
                instance.tick();
            } catch (RuntimeException exception) {
                instance.fail(exception);
            }
            if (instance.retired) {
                instance.cleanup();
                manager.instances.remove(instance.id);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (!(event.getWorld() instanceof WorldServer) || !(event.getEntity() instanceof EntityLiving)
                || !RaidEventConfiguration.isLoaded()) return;
        EntityLiving entity = (EntityLiving) event.getEntity();
        FFDRaidManager manager = getIfPresent((WorldServer) event.getWorld());
        if (manager != null && manager.discarded.remove(entity.getUniqueID())) {
            entity.setDead();
            event.setCanceled(true);
            manager.markDirty();
            return;
        }
        if (manager == null) {
            entity.getEntityData().removeTag(MEMBER_TAG);
            RaidParticipantAI.remove(entity);
            return;
        }
        RaidInstance instance = manager.members.get(entity.getUniqueID());
        if (instance == null) {
            entity.getEntityData().removeTag(MEMBER_TAG);
            RaidParticipantAI.remove(entity);
            return;
        }
        try {
            instance.bind(entity);
        } catch (RuntimeException exception) {
            instance.fail(exception);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving().world instanceof WorldServer) || !RaidEventConfiguration.isLoaded()) return;
        EntityLivingBase entity = event.getEntityLiving();
        FFDRaidManager manager = getIfPresent((WorldServer) entity.world);
        RaidInstance instance = manager == null ? null : manager.members.get(entity.getUniqueID());
        if (instance != null) {
            Entity killer = event.getSource().getTrueSource();
            instance.recordDeath(entity.getUniqueID(), killer instanceof EntityPlayerMP ? killer.getUniqueID() : null);
        }
    }

    @SubscribeEvent
    public static void onDespawn(LivingSpawnEvent.AllowDespawn event) {
        if (isParticipant(event.getEntityLiving())) event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!(event.getWorld() instanceof WorldServer) || !RaidEventConfiguration.isLoaded()) return;
        WorldServer world = (WorldServer) event.getWorld();
        FFDRaidManager manager = getIfPresent(world);
        if (manager != null) for (RaidInstance instance : manager.instances.values()) instance.hide();
        MANAGER_CACHE.remove(world);
    }
}
