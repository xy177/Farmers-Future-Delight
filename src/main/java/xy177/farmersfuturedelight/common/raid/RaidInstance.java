package xy177.farmersfuturedelight.common.raid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.api.raid.RaidEventCallbacks.Lifecycle;
import xy177.farmersfuturedelight.api.raid.RaidEventCallbacks.Outcome;
import xy177.farmersfuturedelight.api.raid.RaidEventContext;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Reward;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Wave;
import xy177.farmersfuturedelight.api.raid.RaidEventRegistry;
import xy177.farmersfuturedelight.api.raid.RaidParticipantEvent;
import xy177.farmersfuturedelight.api.raid.RaidScriptRules;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.registry.FFDRaidContent;

final class RaidInstance {
    final FFDRaidManager manager;
    final UUID id;
    final RaidEventDefinition definition;
    final ResourceLocation triggerType;
    final UUID triggerPlayer;
    final EnumDifficulty difficulty;
    final boolean builtIn;
    final boolean defaultWaves;
    private final String definitionJson;
    private final RaidBossBar boss;
    private final Map<UUID, Member> members = new LinkedHashMap<>();
    private final Set<UUID> heroes = new LinkedHashSet<>();
    private final Set<String> rewardsAttempted = new HashSet<>();
    BlockPos center;
    private ResourceLocation biome;
    int omenLevel;
    int wave;
    Outcome outcome = Outcome.CONTINUE;
    boolean retired;
    private long activeTicks;
    private int cooldown;
    private int victoryTicks;
    private int celebrationTicks;
    private float totalHealth;
    private boolean waveOpen;
    private boolean finishFired;
    private boolean dataRewardsAttempted;
    private boolean rewardCallbackAttempted;
    private boolean restorePending;
    private BlockPos spawnPosition;
    private UUID centerPlayer;
    private int waveIteration;
    private boolean repeatPending;
    private final Map<Integer, Long> waveTimes = new LinkedHashMap<>();
    private final Set<UUID> enteredPlayers = new LinkedHashSet<>();

    RaidInstance(FFDRaidManager manager, UUID id, RaidEventDefinition definition, ResourceLocation triggerType,
                 BlockPos center, @Nullable UUID triggerPlayer, int omenLevel) {
        this(manager, id, definition, triggerType, center, triggerPlayer, omenLevel,
                manager.world().getDifficulty(), RaidDefaults.usesDefaultWaves(definition, manager.world().getDifficulty()),
                Objects.requireNonNull(manager.world().getBiome(center).getRegistryName()));
    }

    private RaidInstance(FFDRaidManager manager, UUID id, RaidEventDefinition definition, ResourceLocation triggerType,
                         BlockPos center, @Nullable UUID triggerPlayer, int omenLevel, EnumDifficulty difficulty,
                         boolean defaultWaves, ResourceLocation biome) {
        this.manager = manager;
        this.id = id;
        this.definition = definition;
        this.triggerType = RaidEventDefinition.identifier(triggerType);
        this.center = center.toImmutable();
        this.triggerPlayer = triggerPlayer;
        centerPlayer = triggerPlayer;
        this.omenLevel = omenLevel;
        this.difficulty = difficulty;
        this.builtIn = RaidDefaults.ID.equals(definition.getId());
        this.defaultWaves = defaultWaves;
        this.biome = biome;
        definitionJson = RaidEventConfiguration.serializeDefinition(definition);
        RaidEventConfiguration.parseDefinition(definitionJson);
        cooldown = definition.getTiming().getPreparationTicks();
        RaidEventDefinition.BossBar style = definition.getBossBar();
        boss = new RaidBossBar(style);
        boss.setName(this::bossTitle);
        boss.setVisible(false);
    }

    WorldServer world() { return manager.world(); }
    boolean isOngoing() { return !retired && outcome == Outcome.CONTINUE; }
    boolean isPreparing() { return isOngoing() && (wave == 0 || !waveOpen && hasMoreWaves()); }
    Collection<UUID> memberIds() { return Collections.unmodifiableSet(members.keySet()); }

    RaidEventContext context() {
        return new RaidEventContext(world(), id, definition.getId(), triggerType, center, biome, triggerPlayer,
                omenLevel, wave, activeTicks, members.size(), RaidVillages.isVillage(world(), center), heroes);
    }

    void start() {
        lifecycle(Lifecycle.START);
        if (isOngoing()) notifyEntrants();
    }

    boolean contains(BlockPos position) {
        RaidScriptRules rules = definition.getScriptRules();
        return rules == null ? center.distanceSq(position) < definition.getRadius() * definition.getRadius()
                : rules.contains(center, position, definition.getRadius());
    }

    boolean associated(EntityPlayerMP player) {
        return player.world == world() && (contains(player.getPosition()) || player.getUniqueID().equals(triggerPlayer));
    }

    List<Integer> waveNumbers() {
        RaidScriptRules rules = definition.getScriptRules();
        if (rules != null) return rules.numbers(definition, difficulty, omenLevel);
        int count = definition.getWaves(difficulty).size();
        if (definition.getBonusOmenLevel() > 0 && omenLevel >= definition.getBonusOmenLevel()) count += definition.getBonusWaves(difficulty).size();
        List<Integer> result = new ArrayList<>();
        for (int index = 1; index <= count; index++) result.add(index);
        return result;
    }

    int displayedWave() { return wave == 0 ? 0 : waveNumbers().get(wave - 1); }
    boolean inWave() { return waveOpen; }
    long elapsed(int number) { return number == 0 ? activeTicks : waveTimes.getOrDefault(number, 0L); }

    private List<EntityPlayerMP> rangePlayers() {
        return world().getPlayers(EntityPlayerMP.class,
                player -> player.isEntityAlive() && !player.isSpectator() && contains(player.getPosition()));
    }

    private void notifyEntrants() {
        List<EntityPlayerMP> players = rangePlayers();
        Set<UUID> present = new java.util.HashSet<>();
        for (EntityPlayerMP player : players) present.add(player.getUniqueID());
        enteredPlayers.retainAll(present);
        for (EntityPlayerMP player : players) {
            if (enteredPlayers.add(player.getUniqueID())) {
                manager.markDirty();
                RaidScriptEvents.post(player, context(), "enter", displayedWave(), "");
            }
        }
    }

    private void scriptEvent(String phase) {
        String result = outcome == Outcome.VICTORY ? "victory" : outcome == Outcome.DEFEAT ? "defeat" : "over";
        for (EntityPlayerMP player : rangePlayers()) RaidScriptEvents.post(player, context(), phase, displayedWave(), result);
    }

    private void followCenter() {
        RaidScriptRules rules = definition.getScriptRules();
        if (rules == null || !rules.followsPlayer()) return;
        Entity player = centerPlayer == null ? null : world().getEntityFromUuid(centerPlayer);
        if (!(player instanceof EntityPlayerMP) || !player.isEntityAlive() || ((EntityPlayerMP) player).isSpectator()) {
            List<EntityPlayerMP> candidates = rangePlayers();
            candidates.sort(java.util.Comparator.comparingDouble(candidate -> candidate.getDistanceSqToCenter(center)));
            player = candidates.isEmpty() ? null : candidates.get(0);
            centerPlayer = player == null ? null : player.getUniqueID();
        }
        if (player != null && !center.equals(player.getPosition())) {
            center = player.getPosition().toImmutable();
            spawnPosition = null;
            manager.markDirty();
        }
    }

    private boolean hasProtectedTargets() {
        RaidScriptRules rules = definition.getScriptRules();
        if (rules == null || !rules.hasTargets() || builtIn) return true;
        double radius = rules.hasGeometry() ? rules.size() / 2 : definition.getRadius();
        double vertical = rules.hasGeometry() ? rules.height() : radius;
        return !world().getEntitiesWithinAABB(net.minecraft.entity.EntityLivingBase.class,
                new net.minecraft.util.math.AxisAlignedBB(center).grow(radius, vertical, radius),
                entity -> entity.isEntityAlive() && !(entity instanceof net.minecraft.entity.player.EntityPlayer && (((net.minecraft.entity.player.EntityPlayer) entity).isSpectator() || ((net.minecraft.entity.player.EntityPlayer) entity).isCreative()))
                        && contains(entity.getPosition()) && rules.isTarget(entity) && !manager.owns(entity.getUniqueID())).isEmpty();
    }

    private RaidScriptRules.WaveRule currentRule() {
        return definition.getScriptRules() == null || wave == 0 ? null : definition.getScriptRules().rule(displayedWave());
    }

    private int waveDelay() {
        RaidScriptRules.WaveRule rule = currentRule();
        return rule != null && rule.delay >= 0 ? rule.delay : definition.getTiming().getWaveDelayTicks();
    }

    private void closeWave(boolean forced) {
        if (!waveOpen) return;
        waveOpen = false;
        RaidScriptRules.WaveRule rule = currentRule();
        repeatPending = !forced && rule != null && (rule.cycles == 0 || waveIteration < rule.cycles);
        cooldown = waveDelay();
        spawnPosition = null;
        manager.markDirty();
        lifecycle(Lifecycle.WAVE_END);
        scriptEvent("wave_end");
        updateBoss();
    }

    void endWave(String remaining) {
        if (!isOngoing() || wave == 0 || !waveOpen) return;
        if (!"add".equals(remaining)) releaseMembers("delete".equals(remaining));
        closeWave(true);
    }

    void releaseMembers(boolean delete) {
        for (UUID member : new ArrayList<>(members.keySet())) {
            Entity entity = world().getEntityFromUuid(member);
            if (delete && entity == null) manager.discard(member);
            remove(member, true);
            if (delete && entity != null) {
                entity.setDead();
                world().removeEntity(entity);
            }
        }
    }

    boolean accepts(UUID player, int level) {
        if (!isOngoing() || level <= omenLevel) return false;
        try {
            return definition.canTrigger(new RaidEventContext(world(), id, definition.getId(), triggerType, center,
                    biome, player, level, wave, activeTicks, members.size(), RaidVillages.isVillage(world(), center), heroes));
        } catch (RuntimeException exception) {
            fail(exception);
            return false;
        }
    }

    private void lifecycle(Lifecycle phase) { definition.getCallbacks().onLifecycle(context(), phase); }

    void tick() {
        if (retired) return;
        if (!FFDRaidManager.enabled(world())) {
            if (isOngoing()) finish(Outcome.STOPPED);
            else retire();
            return;
        }
        if (isOngoing()) followCenter();
        RaidScriptRules rules = definition.getScriptRules();
        boolean active = !(isOngoing() && rules != null && rules.followsPlayer() && centerPlayer == null)
                && FFDRaidManager.isTicking(world(), center);
        boss.setVisible(active && definition.getBossBar().isEnabled());
        if (!active) return;
        refreshMembers();
        if (restorePending) {
            restorePending = false;
            lifecycle(Lifecycle.RESTORE);
            if (retired) return;
            updateBoss();
        }
        if (!isOngoing()) {
            deliverRewards();
            if (++celebrationTicks >= definition.getTiming().getCelebrationTicks()) retire();
            else if (celebrationTicks % 20 == 0) updateBoss();
            manager.markDirty();
            return;
        }
        notifyEntrants();
        if (!hasProtectedTargets()) {
            finish(Outcome.DEFEAT);
            return;
        }
        if (definition.getTrigger().requiresVillage() && !RaidVillages.isVillage(world(), center)) {
            relocate();
            if (!RaidVillages.isVillage(world(), center)) {
                finish(wave == 0 ? Outcome.STOPPED : Outcome.DEFEAT);
                return;
            }
        }
        if (++activeTicks >= definition.getTiming().getTimeoutTicks()
                && !(definition.getScriptRules() != null && definition.getScriptRules().waves().values().stream().anyMatch(rule -> rule.cycles == 0))) {
            finish(Outcome.STOPPED);
            return;
        }
        if (waveOpen) waveTimes.put(displayedWave(), waveTimes.getOrDefault(displayedWave(), 0L) + 1);
        lifecycle(Lifecycle.TICK);
        if (!isOngoing()) return;
        Outcome requested = Objects.requireNonNull(definition.getCallbacks().getOutcome(context()), "callback outcome");
        if (!isOngoing()) return;
        if (requested != Outcome.CONTINUE) {
            finish(requested);
            return;
        }
        if (waveOpen && members.isEmpty()) {
            closeWave(false);
            return;
        }
        if (!waveOpen) {
            if (hasMoreWaves()) {
                if (cooldown > 0) {
                    if (spawnPosition != null && !FFDRaidManager.isTicking(world(), spawnPosition)) spawnPosition = null;
                    if (spawnPosition == null && cooldown % 5 == 0) spawnPosition = RaidWaveSpawner.findPosition(this, 8);
                    cooldown--;
                    updateBossProgress();
                }
                if (cooldown == 0) {
                    BlockPos position = spawnPosition;
                    for (int attempt = 0; position == null && attempt < 6; attempt++) position = RaidWaveSpawner.findPosition(this, 20);
                    if (position == null) finish(Outcome.STOPPED);
                    else spawn(position);
                }
            } else if (wave > 0 && members.isEmpty()) {
                if (victoryTicks < definition.getTiming().getVictoryDelayTicks()) victoryTicks++;
                else {
                    finish(Outcome.VICTORY);
                    return;
                }
            }
        }
        if (activeTicks % 20 == 0) updateBoss();
        manager.markDirty();
    }

    int cooldown() { return cooldown; }

    private boolean hasMoreWaves() {
        return repeatPending || wave < waveNumbers().size();
    }

    private void spawn(BlockPos position) {
        int base = definition.getWaves(difficulty).size();
        int nextIndex = repeatPending ? wave : wave + 1;
        int number = waveNumbers().get(nextIndex - 1);
        RaidScriptRules rules = definition.getScriptRules();
        RaidScriptRules.WaveRule rule = rules == null ? null : rules.rule(number);
        List<RaidEventDefinition.Participant> original = Collections.emptyList();
        if (rules == null || !rules.replacesWaves()) {
            if (number <= base) original = defaultWaves ? RaidDefaults.wave(difficulty, number, false, world().rand).getParticipants()
                    : definition.getWaves(difficulty).get(number - 1).getParticipants();
            else if (number - base <= definition.getBonusWaves(difficulty).size()) original = defaultWaves
                    ? RaidDefaults.wave(difficulty, number, true, world().rand).getParticipants()
                    : definition.getBonusWaves(difficulty).get(number - base - 1).getParticipants();
        }
        List<RaidEventDefinition.Participant> participants = rule == null ? original : rule.draw(world().rand, original, 1024 - members.size());
        int budget = RaidEventDefinition.MAX_ENTITIES_PER_WAVE - members.size();
        List<RaidEventDefinition.Participant> limited = new ArrayList<>();
        for (RaidEventDefinition.Participant participant : participants) {
            RaidEventDefinition.Participant single = new RaidEventDefinition.Participant(participant.getEntity(), 1, participant.isLeader(), participant.getNbt(), participant.getPassengers());
            int cost = new Wave(Collections.singletonList(single)).getEntityCount();
            int count = Math.min(participant.getCount(), budget / cost);
            if (count > 0) {
                limited.add(count == participant.getCount() ? participant : new RaidEventDefinition.Participant(participant.getEntity(), count, participant.isLeader(), participant.getNbt(), participant.getPassengers()));
                budget -= count * cost;
            }
        }
        participants = limited;
        List<RaidWaveSpawner.Spawned> spawned = Collections.emptyList();
        if (!participants.isEmpty()) {
            Wave selected = new Wave(participants);
            for (int attempt = 0; ; attempt++) {
                try {
                    spawned = RaidWaveSpawner.spawn(this, selected, position);
                    break;
                } catch (RaidWaveSpawner.ObstructedSpawn exception) {
                    if (attempt >= 5) throw exception;
                    BlockPos alternative = RaidWaveSpawner.findPosition(this, 20);
                    if (alternative == null) throw exception;
                    position = alternative;
                }
            }
        }
        if (repeatPending) waveIteration++;
        else waveIteration = 1;
        repeatPending = false;
        wave = nextIndex;
        totalHealth = 0.0F;
        for (Member member : members.values()) totalHealth += member.health;
        waveOpen = true;
        spawnPosition = null;
        try {
            for (RaidWaveSpawner.Spawned participant : spawned) register(participant.entity, participant.leader, false);
            for (RaidWaveSpawner.Spawned participant : spawned) announce(participant.entity);
        } catch (RuntimeException exception) {
            for (RaidWaveSpawner.Spawned participant : spawned) {
                remove(participant.entity.getUniqueID(), true);
                participant.entity.setDead();
                world().removeEntity(participant.entity);
            }
            throw exception;
        }
        if (!isOngoing()) return;
        RaidWaveSpawner.horn(this, position);
        lifecycle(Lifecycle.WAVE_START);
        scriptEvent("wave_start");
        updateBoss();
    }

    boolean add(EntityLiving entity, boolean leader) {
        if (members.size() >= RaidEventDefinition.MAX_ENTITIES_PER_WAVE || manager.owns(entity.getUniqueID())) return false;
        register(entity, leader && !hasLeader(wave), true);
        return true;
    }

    private void register(EntityLiving entity, boolean leader, boolean announce) {
        if (manager.owns(entity.getUniqueID())) throw new IllegalArgumentException("Duplicate raid participant " + entity.getUniqueID());
        Member member = new Member(entity.getUniqueID(), wave, leader, entity.getPosition(), entity.getHealth());
        members.put(member.id, member);
        manager.index(this, member.id);
        totalHealth += member.health;
        bind(entity);
        if (announce) announce(entity);
    }

    void bind(EntityLiving entity) {
        Member member = members.get(entity.getUniqueID());
        if (member == null) return;
        member.position = entity.getPosition().toImmutable();
        member.health = entity.getHealth();
        member.missingTicks = 0;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setUniqueId("Instance", id);
        tag.setInteger("Wave", member.wave);
        tag.setBoolean("Leader", member.leader);
        entity.getEntityData().setTag(FFDRaidManager.MEMBER_TAG, tag);
        if (member.leader) FFDRaidCaptains.setLeader(entity, true);
        RaidParticipantAI.install(entity, this);
    }

    private void announce(EntityLiving entity) {
        Member member = members.get(entity.getUniqueID());
        if (member != null) MinecraftForge.EVENT_BUS.post(new RaidParticipantEvent(entity, context(), member.leader));
    }

    boolean hasLeader(int group) {
        for (Member member : members.values()) if (member.wave == group && member.leader) return true;
        return false;
    }

    boolean canSucceed(EntityLiving entity) {
        Member member = members.get(entity.getUniqueID());
        return isOngoing() && member != null && !member.leader && !hasLeader(member.wave) && FFDRaidCaptains.canLead(entity);
    }

    void succeed(EntityLiving entity) {
        if (!canSucceed(entity)) return;
        try {
            Member member = members.get(entity.getUniqueID());
            member.leader = true;
            bind(entity);
            announce(entity);
            manager.markDirty();
        } catch (RuntimeException exception) {
            fail(exception);
        }
    }

    void recordDeath(UUID entity, @Nullable UUID player) {
        Member member = members.get(entity);
        if (member != null) {
            member.deathObserved = true;
            member.killer = player;
            manager.markDirty();
        }
    }

    private void refreshMembers() {
        for (Member member : new ArrayList<>(members.values())) {
            Entity loaded = world().getEntityFromUuid(member.id);
            if (loaded instanceof EntityLiving) {
                EntityLiving entity = (EntityLiving) loaded;
                if (!entity.isEntityAlive()) {
                    if (member.killer != null) heroes.add(member.killer);
                    remove(member.id, false);
                    continue;
                }
                member.deathObserved = false;
                member.killer = null;
                if (!entity.getEntityData().getCompoundTag(FFDRaidManager.MEMBER_TAG).hasUniqueId("Instance")) bind(entity);
                member.position = entity.getPosition().toImmutable();
                member.health = entity.getHealth();
                member.missingTicks = 0;
                double radius = definition.getRadius() + 16.0D;
                RaidScriptRules rules = definition.getScriptRules();
                if (entity.dimension != world().provider.getDimension() || (rules == null || !rules.hasGeometry() && !rules.followsPlayer()) && member.position.distanceSq(center) >= radius * radius) {
                    remove(member.id, true);
                } else if (builtIn && activeTicks % 20 == 0 && entity.ticksExisted > 600 && entity.getIdleTime() > 2400
                        && !RaidVillages.isVillage(world(), member.position) && ++member.outsideChecks >= 30) {
                    remove(member.id, true);
                }
            } else if (member.deathObserved) {
                if (member.killer != null) heroes.add(member.killer);
                remove(member.id, false);
            } else if (FFDRaidManager.isTicking(world(), member.position)) {
                if (++member.missingTicks >= 100) {
                    FFDRaidManager.LOGGER.warn("Removing unresolved raid participant {} / {} after loaded-chunk grace period", id, member.id);
                    remove(member.id, true);
                }
            } else {
                member.missingTicks = 0;
            }
        }
    }

    private void remove(UUID entityId, boolean subtractHealth) {
        Member member = members.remove(entityId);
        if (member == null) return;
        if (subtractHealth) totalHealth = Math.max(0.0F, totalHealth - member.health);
        manager.unindex(entityId);
        Entity loaded = world().getEntityFromUuid(entityId);
        if (loaded instanceof EntityLiving) {
            EntityLiving entity = (EntityLiving) loaded;
            entity.getEntityData().removeTag(FFDRaidManager.MEMBER_TAG);
            RaidParticipantAI.remove(entity);
        }
    }

    private void relocate() {
        BlockPos closest = null;
        double distance = Double.MAX_VALUE;
        for (BlockPos occupied : RaidVillages.occupied(world(), center, 2)) {
            BlockPos candidate = new BlockPos((occupied.getX() >> 4) * 16 + 8,
                    (occupied.getY() >> 4) * 16 + 8, (occupied.getZ() >> 4) * 16 + 8);
            double candidateDistance = candidate.distanceSq(center);
            if (candidateDistance < distance && world().isBlockLoaded(candidate) && RaidVillages.isVillage(world(), candidate)) {
                closest = candidate;
                distance = candidateDistance;
            }
        }
        if (closest != null) {
            center = closest;
            biome = Objects.requireNonNull(world().getBiome(center).getRegistryName());
            spawnPosition = null;
            manager.markDirty();
        }
    }

    void finish(Outcome result) {
        if (!isOngoing() || result == Outcome.CONTINUE) return;
        outcome = result;
        manager.markDirty();
        scriptEvent("result");
        if (!finishFired) {
            finishFired = true;
            lifecycle(Lifecycle.FINISH);
        }
        if (result == Outcome.STOPPED) retire();
        else {
            deliverRewards();
            updateBoss();
        }
    }

    private void deliverRewards() {
        if (outcome != Outcome.VICTORY && outcome != Outcome.DEFEAT) return;
        if (!dataRewardsAttempted) {
            deliverDataRewards();
            dataRewardsAttempted = true;
            manager.markDirty();
        }
        if (!rewardCallbackAttempted) {
            rewardCallbackAttempted = true;
            manager.markDirty();
            definition.getCallbacks().onReward(context(), outcome);
        }
    }

    private void deliverDataRewards() {
        List<Reward> rewards = outcome == Outcome.VICTORY ? definition.getVictoryRewards() : definition.getDefeatRewards();
        for (int index = 0; index < rewards.size(); index++) {
            Reward reward = rewards.get(index);
            Collection<UUID> recipients = reward.getRecipient() == Reward.Recipient.HEROES ? new ArrayList<>(heroes)
                    : triggerPlayer == null ? Collections.emptyList() : Collections.singletonList(triggerPlayer);
            for (UUID recipient : recipients) {
                Entity loaded = world().getEntityFromUuid(recipient);
                if (!(loaded instanceof EntityPlayerMP) || !loaded.isEntityAlive()
                        || ((EntityPlayerMP) loaded).isSpectator()) continue;
                EntityPlayerMP player = (EntityPlayerMP) loaded;
                attemptReward(index + ":" + recipient, () -> give(player, reward));
            }
        }
        if (builtIn && outcome == Outcome.VICTORY) {
            for (UUID recipient : new ArrayList<>(heroes)) {
                Entity loaded = world().getEntityFromUuid(recipient);
                if (!(loaded instanceof EntityPlayerMP) || !loaded.isEntityAlive()
                        || ((EntityPlayerMP) loaded).isSpectator()) continue;
                EntityPlayerMP player = (EntityPlayerMP) loaded;
                attemptReward("hero:" + recipient, () -> {
                    player.addPotionEffect(new PotionEffect(FFDRaidContent.HERO_OF_THE_VILLAGE, 48000,
                            Math.max(0, omenLevel - 1), false, false));
                    FFDAdvancements.HERO_OF_THE_VILLAGE.trigger(player);
                });
            }
        }
    }

    private void attemptReward(String key, Runnable action) {
        if (!rewardsAttempted.add(key)) return;
        manager.markDirty();
        try {
            action.run();
        } catch (RuntimeException exception) {
            FFDRaidManager.LOGGER.error("Raid reward failed; attempt retained for {} / {} / {}", definition.getId(), id, key, exception);
        }
    }

    private void give(EntityPlayerMP player, Reward reward) {
        switch (reward.getType()) {
            case ITEM:
                Item item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(reward.getId()), "reward item");
                int remaining = reward.getAmount();
                while (remaining > 0) {
                    ItemStack stack = new ItemStack(item, 1, reward.getMetadata());
                    stack.setTagCompound(reward.getNbt());
                    int count = Math.min(remaining, stack.getMaxStackSize());
                    if (count <= 0) throw new IllegalArgumentException("Invalid reward stack limit");
                    stack.setCount(count);
                    giveStack(player, stack);
                    remaining -= count;
                }
                break;
            case LOOT_TABLE:
                LootTable table = world().getLootTableManager().getLootTableFromLocation(reward.getId());
                if (table == LootTable.EMPTY_LOOT_TABLE) throw new IllegalArgumentException("Missing or invalid reward loot table " + reward.getId());
                LootContext loot = new LootContext.Builder(world()).withLuck(player.getLuck()).withLootedEntity(player).withPlayer(player).build();
                for (int roll = 0; roll < reward.getAmount(); roll++) {
                    for (ItemStack stack : table.generateLootForPools(world().rand, loot)) giveStack(player, stack);
                }
                break;
            case EFFECT:
                Potion potion = Objects.requireNonNull(ForgeRegistries.POTIONS.getValue(reward.getId()), "reward effect");
                player.addPotionEffect(new PotionEffect(potion, reward.getDuration(), reward.getAmplifier()));
                break;
            case EXPERIENCE:
                player.addExperience(reward.getAmount());
                break;
            default:
                throw new IllegalArgumentException("Unsupported raid reward " + reward.getType());
        }
        player.inventoryContainer.detectAndSendChanges();
    }

    private static void giveStack(EntityPlayerMP player, ItemStack source) {
        ItemStack remaining = source.copy();
        while (!remaining.isEmpty()) {
            int count = Math.min(remaining.getCount(), remaining.getMaxStackSize());
            if (count <= 0) throw new IllegalArgumentException("Invalid loot stack limit");
            ItemStack stack = remaining.splitStack(count);
            player.inventory.addItemStackToInventory(stack);
            if (!stack.isEmpty()) player.dropItem(stack, false);
        }
    }

    private void updateBoss() {
        Set<EntityPlayerMP> viewers = new HashSet<>();
        if (definition.getBossBar().isEnabled()) {
            for (EntityPlayerMP player : world().getPlayers(EntityPlayerMP.class, player -> player.isEntityAlive())) {
                if (manager.nearby(player.getPosition(), null, true) == this) viewers.add(player);
            }
        }
        for (EntityPlayerMP player : new ArrayList<>(boss.getPlayers())) if (!viewers.contains(player)) boss.removePlayer(player);
        for (EntityPlayerMP player : viewers) boss.addPlayer(player);
        updateBossProgress();
    }

    private void updateBossProgress() {
        float progress;
        if (outcome == Outcome.VICTORY || outcome == Outcome.DEFEAT) {
            progress = outcome == Outcome.VICTORY ? 0.0F : healthProgress();
        } else if (isPreparing()) {
            int duration = wave == 0 ? definition.getTiming().getPreparationTicks() : waveDelay();
            progress = duration == 0 ? 1.0F : 1.0F - (float) cooldown / duration;
        } else {
            progress = healthProgress();
        }
        boss.setName(this::bossTitle);
        boss.setPercent(MathHelper.clamp(progress, 0.0F, 1.0F));
    }

    private ITextComponent bossTitle(String language) {
        if (outcome == Outcome.VICTORY || outcome == Outcome.DEFEAT) {
            return new TextComponentTranslation(outcome == Outcome.VICTORY
                    ? definition.getBossBar().getVictoryTitle() : definition.getBossBar().getDefeatTitle());
        }
        RaidScriptRules rules = definition.getScriptRules();
        ITextComponent title = rules == null ? new TextComponentTranslation(definition.getBossBar().getTitle())
                : rules.displayName(language, definition.getBossBar().getTitle());
        if (!isPreparing() && !members.isEmpty() && members.size() <= 2) title.appendSibling(new TextComponentString(" - "))
                .appendSibling(new TextComponentTranslation("event.minecraft.raid.raiders_remaining", members.size()));
        return title;
    }

    private float healthProgress() {
        float health = 0.0F;
        for (Member member : members.values()) health += member.health;
        return totalHealth <= 0.0F ? 0.0F : MathHelper.clamp(health / totalHealth, 0.0F, 1.0F);
    }

    void hide() {
        boss.setVisible(false);
        for (EntityPlayerMP player : new ArrayList<>(boss.getPlayers())) boss.removePlayer(player);
    }

    private void retire() {
        retired = true;
        hide();
        manager.markDirty();
    }

    void cleanup() {
        for (UUID member : new ArrayList<>(members.keySet())) remove(member, false);
        manager.markDirty();
    }

    void fail(RuntimeException exception) {
        FFDRaidManager.LOGGER.error("Stopped faulting raid {} / {}", definition.getId(), id, exception);
        outcome = Outcome.STOPPED;
        finishFired = true;
        retire();
    }

    NBTTagCompound write() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setUniqueId("Id", id);
        tag.setString("Definition", definitionJson);
        tag.setString("Trigger", triggerType.toString());
        if (triggerPlayer != null) tag.setUniqueId("Player", triggerPlayer);
        tag.setLong("Center", center.toLong());
        tag.setString("Biome", biome.toString());
        tag.setString("Difficulty", difficulty.name());
        tag.setBoolean("DefaultWaves", defaultWaves);
        tag.setInteger("Omen", omenLevel);
        tag.setInteger("Wave", wave);
        tag.setInteger("WaveIteration", waveIteration);
        tag.setBoolean("RepeatPending", repeatPending);
        if (centerPlayer != null) tag.setUniqueId("CenterPlayer", centerPlayer);
        NBTTagList elapsed = new NBTTagList();
        for (Map.Entry<Integer, Long> entry : waveTimes.entrySet()) {
            NBTTagCompound value = new NBTTagCompound();
            value.setInteger("Wave", entry.getKey());
            value.setLong("Ticks", entry.getValue());
            elapsed.appendTag(value);
        }
        tag.setTag("WaveTimes", elapsed);
        NBTTagList entered = new NBTTagList();
        for (UUID player : enteredPlayers) entered.appendTag(new NBTTagString(player.toString()));
        tag.setTag("EnteredPlayers", entered);
        tag.setString("Outcome", outcome.name());
        tag.setLong("ActiveTicks", activeTicks);
        tag.setInteger("Cooldown", cooldown);
        tag.setInteger("VictoryTicks", victoryTicks);
        tag.setInteger("CelebrationTicks", celebrationTicks);
        tag.setFloat("TotalHealth", totalHealth);
        tag.setBoolean("WaveOpen", waveOpen);
        tag.setBoolean("FinishFired", finishFired);
        tag.setBoolean("DataRewardsAttempted", dataRewardsAttempted);
        tag.setBoolean("RewardCallbackAttempted", rewardCallbackAttempted);
        NBTTagList participants = new NBTTagList();
        for (Member member : members.values()) {
            Entity entity = world().getEntityFromUuid(member.id);
            if (entity instanceof EntityLiving) {
                member.position = entity.getPosition().toImmutable();
                member.health = ((EntityLiving) entity).getHealth();
            }
            participants.appendTag(member.write());
        }
        tag.setTag("Members", participants);
        NBTTagList heroTags = new NBTTagList();
        for (UUID hero : heroes) heroTags.appendTag(new NBTTagString(hero.toString()));
        tag.setTag("Heroes", heroTags);
        NBTTagList attempted = new NBTTagList();
        for (String reward : rewardsAttempted) attempted.appendTag(new NBTTagString(reward));
        tag.setTag("RewardsAttempted", attempted);
        return tag;
    }

    static RaidInstance read(FFDRaidManager manager, NBTTagCompound tag) {
        if (!tag.hasUniqueId("Id")) throw new IllegalArgumentException("Missing saved raid UUID");
        RaidEventDefinition saved = RaidEventConfiguration.parseDefinition(tag.getString("Definition"));
        RaidEventDefinition current = RaidEventRegistry.get(saved.getId());
        if (current == null) throw new IllegalArgumentException("Missing saved raid definition " + saved.getId());
        RaidEventDefinition definition = saved.toBuilder().callbacks(current.getCallbacks()).build();
        EnumDifficulty difficulty = EnumDifficulty.valueOf(tag.getString("Difficulty"));
        if (definition.getWaves(difficulty).isEmpty()) throw new IllegalArgumentException("Missing saved raid difficulty");
        boolean defaultWaves = tag.getBoolean("DefaultWaves");
        if (defaultWaves && !RaidDefaults.ID.equals(saved.getId())) throw new IllegalArgumentException("Invalid built-in raid wave snapshot");
        int omen = tag.getInteger("Omen");
        boolean builtIn = RaidDefaults.ID.equals(saved.getId());
        if (omen < 0 || omen > 255 || builtIn && (omen < 1 || omen > 5)) throw new IllegalArgumentException("Invalid saved omen");
        RaidInstance instance = new RaidInstance(manager, tag.getUniqueId("Id"), definition,
                RaidEventDefinition.identifier(tag.getString("Trigger")), BlockPos.fromLong(tag.getLong("Center")),
                tag.hasUniqueId("Player") ? tag.getUniqueId("Player") : null, omen, difficulty, defaultWaves,
                RaidEventDefinition.identifier(tag.getString("Biome")));
        instance.wave = tag.getInteger("Wave");
        instance.waveIteration = tag.hasKey("WaveIteration") ? tag.getInteger("WaveIteration") : 1;
        instance.repeatPending = tag.getBoolean("RepeatPending");
        instance.centerPlayer = tag.hasUniqueId("CenterPlayer") ? tag.getUniqueId("CenterPlayer") : null;
        NBTTagList elapsed = tag.getTagList("WaveTimes", 10);
        for (int index = 0; index < elapsed.tagCount(); index++) {
            NBTTagCompound value = elapsed.getCompoundTagAt(index);
            if (value.getInteger("Wave") <= 0 || value.getLong("Ticks") < 0) throw new IllegalArgumentException("Invalid wave timer");
            instance.waveTimes.put(value.getInteger("Wave"), value.getLong("Ticks"));
        }
        NBTTagList entered = tag.getTagList("EnteredPlayers", 8);
        for (int index = 0; index < entered.tagCount(); index++) instance.enteredPlayers.add(UUID.fromString(entered.getStringTagAt(index)));
        instance.outcome = Outcome.valueOf(tag.getString("Outcome"));
        instance.activeTicks = tag.getLong("ActiveTicks");
        instance.cooldown = tag.getInteger("Cooldown");
        instance.victoryTicks = tag.getInteger("VictoryTicks");
        instance.celebrationTicks = tag.getInteger("CelebrationTicks");
        instance.totalHealth = tag.getFloat("TotalHealth");
        instance.waveOpen = tag.getBoolean("WaveOpen");
        instance.finishFired = tag.getBoolean("FinishFired");
        instance.dataRewardsAttempted = tag.getBoolean("DataRewardsAttempted");
        instance.rewardCallbackAttempted = tag.getBoolean("RewardCallbackAttempted");
        int maximum = instance.waveNumbers().size();
        if (instance.wave < 0 || instance.wave > maximum || instance.waveIteration < 0 || instance.activeTicks < 0 || instance.cooldown < 0
                || instance.victoryTicks < 0 || instance.celebrationTicks < 0 || !Float.isFinite(instance.totalHealth)
                || instance.totalHealth < 0.0F || instance.outcome == Outcome.STOPPED
                || instance.outcome != Outcome.CONTINUE && !instance.finishFired) throw new IllegalArgumentException("Invalid saved raid state");
        NBTTagList members = tag.getTagList("Members", 10);
        if (members.tagCount() > RaidEventDefinition.MAX_ENTITIES_PER_WAVE) throw new IllegalArgumentException("Too many saved raid participants");
        for (int index = 0; index < members.tagCount(); index++) {
            Member member = Member.read(members.getCompoundTagAt(index));
            if (member.wave < 1 || member.wave > instance.wave || instance.members.put(member.id, member) != null) {
                throw new IllegalArgumentException("Invalid saved raid participant");
            }
        }
        NBTTagList heroes = tag.getTagList("Heroes", 8);
        for (int index = 0; index < heroes.tagCount(); index++) instance.heroes.add(UUID.fromString(heroes.getStringTagAt(index)));
        NBTTagList attempts = tag.getTagList("RewardsAttempted", 8);
        for (int index = 0; index < attempts.tagCount(); index++) instance.rewardsAttempted.add(attempts.getStringTagAt(index));
        instance.restorePending = true;
        return instance;
    }

    private static final class Member {
        final UUID id;
        final int wave;
        boolean leader;
        BlockPos position;
        float health;
        int outsideChecks;
        int missingTicks;
        boolean deathObserved;
        UUID killer;

        Member(UUID id, int wave, boolean leader, BlockPos position, float health) {
            this.id = id;
            this.wave = wave;
            this.leader = leader;
            this.position = position.toImmutable();
            this.health = health;
        }

        NBTTagCompound write() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("Id", id);
            tag.setInteger("Wave", wave);
            tag.setBoolean("Leader", leader);
            tag.setLong("Position", position.toLong());
            tag.setFloat("Health", health);
            tag.setInteger("OutsideChecks", outsideChecks);
            tag.setInteger("MissingTicks", missingTicks);
            tag.setBoolean("DeathObserved", deathObserved);
            if (killer != null) tag.setUniqueId("Killer", killer);
            return tag;
        }

        static Member read(NBTTagCompound tag) {
            if (!tag.hasUniqueId("Id")) throw new IllegalArgumentException("Missing participant UUID");
            Member member = new Member(tag.getUniqueId("Id"), tag.getInteger("Wave"), tag.getBoolean("Leader"),
                    BlockPos.fromLong(tag.getLong("Position")), tag.getFloat("Health"));
            member.outsideChecks = tag.getInteger("OutsideChecks");
            member.missingTicks = tag.getInteger("MissingTicks");
            member.deathObserved = tag.getBoolean("DeathObserved");
            member.killer = tag.hasUniqueId("Killer") ? tag.getUniqueId("Killer") : null;
            if (!Float.isFinite(member.health) || member.health < 0.0F || member.outsideChecks < 0 || member.missingTicks < 0) {
                throw new IllegalArgumentException("Invalid saved participant counters");
            }
            member.missingTicks = 0;
            return member;
        }
    }
}
