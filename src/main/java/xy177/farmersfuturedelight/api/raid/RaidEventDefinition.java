package xy177.farmersfuturedelight.api.raid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.EnumDifficulty;

public final class RaidEventDefinition {
    public static final int MAX_WAVES = 64;
    public static final int MAX_ENTITIES_PER_WAVE = 1024;
    public static final int MAX_PASSENGER_DEPTH = 16;
    private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    private final ResourceLocation id;
    private final Trigger trigger;
    private final Map<EnumDifficulty, List<Wave>> waves;
    private final Map<EnumDifficulty, List<Wave>> bonusWaves;
    private final int bonusOmenLevel;
    private final double radius;
    private final Timing timing;
    private final BossBar bossBar;
    private final List<Reward> victoryRewards;
    private final List<Reward> defeatRewards;
    private final RaidEventCallbacks callbacks;
    private final RaidScriptRules scriptRules;

    private RaidEventDefinition(Builder builder) {
        id = identifier(builder.id);
        trigger = Objects.requireNonNull(builder.trigger, "trigger");
        waves = copyWaves(builder.waves);
        bonusWaves = copyWaves(builder.bonusWaves);
        if (waves.isEmpty()) throw new IllegalArgumentException("At least one difficulty needs waves");
        bonusOmenLevel = range(builder.bonusOmenLevel, 0, 255, "bonusOmenLevel");
        if (bonusWaves.isEmpty() != (bonusOmenLevel == 0)) {
            throw new IllegalArgumentException("Bonus waves require a positive bonusOmenLevel; otherwise use zero");
        }
        for (EnumDifficulty difficulty : bonusWaves.keySet()) {
            if (!waves.containsKey(difficulty)) throw new IllegalArgumentException("Bonus waves need base waves");
            if (waves.get(difficulty).size() + bonusWaves.get(difficulty).size() > MAX_WAVES) {
                throw new IllegalArgumentException("Too many combined waves");
            }
        }
        if (!Double.isFinite(builder.radius) || builder.radius <= 0.0D || builder.radius > 512.0D) {
            throw new IllegalArgumentException("radius must be finite and in (0,512]");
        }
        radius = builder.radius;
        timing = Objects.requireNonNull(builder.timing, "timing");
        bossBar = Objects.requireNonNull(builder.bossBar, "bossBar");
        victoryRewards = immutableList(builder.victoryRewards);
        defeatRewards = immutableList(builder.defeatRewards);
        callbacks = Objects.requireNonNull(builder.callbacks, "callbacks");
        scriptRules = builder.scriptRules;
    }

    public static ResourceLocation identifier(String value) {
        if (value == null || value.length() > 256 || !IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("Expected an explicit lowercase namespace:path: " + value);
        }
        return new ResourceLocation(value);
    }

    public static ResourceLocation identifier(ResourceLocation value) {
        return identifier(Objects.requireNonNull(value, "identifier").toString());
    }

    public static Builder builder(ResourceLocation id) { return new Builder(id); }
    public Builder toBuilder() { return new Builder(this); }
    public ResourceLocation getId() { return id; }
    public Trigger getTrigger() { return trigger; }
    public Map<EnumDifficulty, List<Wave>> getWaves() { return waves; }
    public Map<EnumDifficulty, List<Wave>> getBonusWaves() { return bonusWaves; }
    public int getBonusOmenLevel() { return bonusOmenLevel; }
    public double getRadius() { return radius; }
    public Timing getTiming() { return timing; }
    public BossBar getBossBar() { return bossBar; }
    public List<Reward> getVictoryRewards() { return victoryRewards; }
    public List<Reward> getDefeatRewards() { return defeatRewards; }
    public RaidEventCallbacks getCallbacks() { return callbacks; }
    public RaidScriptRules getScriptRules() { return scriptRules; }

    public List<Wave> getWaves(EnumDifficulty difficulty) {
        List<Wave> result = waves.get(difficulty);
        return result == null ? Collections.emptyList() : result;
    }

    public List<Wave> getBonusWaves(EnumDifficulty difficulty) {
        List<Wave> result = bonusWaves.get(difficulty);
        return result == null ? Collections.emptyList() : result;
    }

    public boolean canTrigger(RaidEventContext context) {
        return id.equals(context.getEventId()) && !getWaves(context.getDifficulty()).isEmpty()
                && trigger.matches(context) && callbacks.canTrigger(context);
    }

    private static Map<EnumDifficulty, List<Wave>> copyWaves(Map<EnumDifficulty, List<Wave>> source) {
        Map<EnumDifficulty, List<Wave>> result = new EnumMap<>(EnumDifficulty.class);
        for (Map.Entry<EnumDifficulty, List<Wave>> entry : source.entrySet()) {
            EnumDifficulty difficulty = Objects.requireNonNull(entry.getKey(), "difficulty");
            List<Wave> value = immutableList(entry.getValue());
            if (difficulty == EnumDifficulty.PEACEFUL || value.isEmpty() || value.size() > MAX_WAVES) {
                throw new IllegalArgumentException("Expected 1-64 waves for a non-peaceful difficulty");
            }
            result.put(difficulty, value);
        }
        return Collections.unmodifiableMap(result);
    }

    private static <Value> List<Value> immutableList(Collection<? extends Value> values) {
        List<Value> copy = new ArrayList<>(Objects.requireNonNull(values, "values"));
        for (Value value : copy) Objects.requireNonNull(value, "list member");
        return Collections.unmodifiableList(copy);
    }

    private static int range(int value, int minimum, int maximum, String name) {
        if (value < minimum || value > maximum) throw new IllegalArgumentException(name + " out of range");
        return value;
    }

    private static String title(String value) {
        if (value == null || value.trim().isEmpty() || value.length() > 256) {
            throw new IllegalArgumentException("Expected a nonempty translation key of at most 256 characters");
        }
        return value;
    }

    public static final class Builder {
        private final ResourceLocation id;
        private Trigger trigger;
        private final Map<EnumDifficulty, List<Wave>> waves = new EnumMap<>(EnumDifficulty.class);
        private final Map<EnumDifficulty, List<Wave>> bonusWaves = new EnumMap<>(EnumDifficulty.class);
        private int bonusOmenLevel;
        private double radius;
        private Timing timing;
        private BossBar bossBar;
        private List<Reward> victoryRewards = Collections.emptyList();
        private List<Reward> defeatRewards = Collections.emptyList();
        private RaidEventCallbacks callbacks = RaidEventCallbacks.NONE;
        private RaidScriptRules scriptRules;

        private Builder(ResourceLocation id) { this.id = identifier(id); }

        private Builder(RaidEventDefinition definition) {
            id = definition.id;
            trigger = definition.trigger;
            waves.putAll(definition.waves);
            bonusWaves.putAll(definition.bonusWaves);
            bonusOmenLevel = definition.bonusOmenLevel;
            radius = definition.radius;
            timing = definition.timing;
            bossBar = definition.bossBar;
            victoryRewards = definition.victoryRewards;
            defeatRewards = definition.defeatRewards;
            callbacks = definition.callbacks;
            scriptRules = definition.scriptRules;
        }

        public Builder trigger(Trigger value) { trigger = Objects.requireNonNull(value); return this; }
        public Builder radius(double value) { radius = value; return this; }
        public Builder timing(Timing value) { timing = Objects.requireNonNull(value); return this; }
        public Builder bossBar(BossBar value) { bossBar = Objects.requireNonNull(value); return this; }
        public Builder bonusOmenLevel(int value) { bonusOmenLevel = value; return this; }
        public Builder callbacks(RaidEventCallbacks value) { callbacks = Objects.requireNonNull(value); return this; }
        public Builder scriptRules(RaidScriptRules value) { scriptRules = value; return this; }

        public Builder waves(EnumDifficulty difficulty, Collection<Wave> value) {
            waves.put(Objects.requireNonNull(difficulty), immutableList(value));
            return this;
        }

        public Builder bonusWaves(EnumDifficulty difficulty, Collection<Wave> value) {
            bonusWaves.put(Objects.requireNonNull(difficulty), immutableList(value));
            return this;
        }

        public Builder victoryRewards(Collection<Reward> value) { victoryRewards = immutableList(value); return this; }
        public Builder defeatRewards(Collection<Reward> value) { defeatRewards = immutableList(value); return this; }
        public RaidEventDefinition build() { return new RaidEventDefinition(this); }
    }

    public static final class Trigger {
        private final ResourceLocation type;
        private final Set<Integer> dimensions;
        private final Set<ResourceLocation> biomes;
        private final boolean requiresVillage;
        private final int minOmenLevel;
        private final int maxOmenLevel;

        public Trigger(ResourceLocation type, Collection<Integer> dimensions, Collection<ResourceLocation> biomes,
                       boolean requiresVillage, int minOmenLevel, int maxOmenLevel) {
            this.type = identifier(type);
            Set<Integer> dimensionCopy = new LinkedHashSet<>(Objects.requireNonNull(dimensions));
            for (Integer dimension : dimensionCopy) Objects.requireNonNull(dimension, "dimension");
            this.dimensions = Collections.unmodifiableSet(dimensionCopy);
            Set<ResourceLocation> biomeCopy = new LinkedHashSet<>();
            for (ResourceLocation biome : biomes) biomeCopy.add(identifier(biome));
            this.biomes = Collections.unmodifiableSet(biomeCopy);
            this.requiresVillage = requiresVillage;
            this.minOmenLevel = range(minOmenLevel, 0, 255, "minOmenLevel");
            this.maxOmenLevel = range(maxOmenLevel, minOmenLevel, 255, "maxOmenLevel");
        }

        public ResourceLocation getType() { return type; }
        public Set<Integer> getDimensions() { return dimensions; }
        public Set<ResourceLocation> getBiomes() { return biomes; }
        public boolean requiresVillage() { return requiresVillage; }
        public int getMinOmenLevel() { return minOmenLevel; }
        public int getMaxOmenLevel() { return maxOmenLevel; }

        public boolean matches(RaidEventContext context) {
            return type.equals(context.getTriggerType()) && (!requiresVillage || context.isVillage())
                    && (dimensions.isEmpty() || dimensions.contains(context.getDimension()))
                    && (biomes.isEmpty() || biomes.contains(context.getBiome()))
                    && context.getOmenLevel() >= minOmenLevel && context.getOmenLevel() <= maxOmenLevel;
        }
    }

    public static final class Wave {
        private final List<Participant> participants;
        private final int entityCount;

        public Wave(Collection<Participant> participants) {
            this.participants = immutableList(participants);
            if (this.participants.isEmpty() || this.participants.size() > MAX_ENTITIES_PER_WAVE) {
                throw new IllegalArgumentException("Expected a bounded, nonempty participant list");
            }
            long count = 0;
            long leaders = 0;
            for (Participant participant : this.participants) {
                count += participant.countTree(0, false);
                leaders += participant.countTree(0, true);
            }
            if (count > MAX_ENTITIES_PER_WAVE || leaders > 1) {
                throw new IllegalArgumentException("Wave exceeds the entity limit or has multiple leaders");
            }
            entityCount = (int) count;
        }

        public List<Participant> getParticipants() { return participants; }
        public int getEntityCount() { return entityCount; }
    }

    public static final class Participant {
        private final ResourceLocation entity;
        private final int count;
        private final boolean leader;
        private final NBTTagCompound nbt;
        private final List<Participant> passengers;

        public Participant(ResourceLocation entity, int count, boolean leader, NBTTagCompound nbt,
                           Collection<Participant> passengers) {
            this.entity = identifier(entity);
            this.count = range(count, 1, MAX_ENTITIES_PER_WAVE, "participant count");
            this.leader = leader;
            this.nbt = Objects.requireNonNull(nbt, "nbt").copy();
            for (String protectedKey : new String[]{"id", "Pos", "UUID", "UUIDMost", "UUIDLeast",
                    "Passengers", "Riding", "Dimension"}) {
                if (this.nbt.hasKey(protectedKey)) {
                    throw new IllegalArgumentException("Participant NBT cannot override " + protectedKey);
                }
            }
            this.passengers = immutableList(passengers);
            countTree(0, false);
        }

        private long countTree(int depth, boolean leadersOnly) {
            if (depth > MAX_PASSENGER_DEPTH) throw new IllegalArgumentException("Passenger tree is too deep");
            long total = leadersOnly ? (leader ? 1 : 0) : 1;
            for (Participant passenger : passengers) {
                total += passenger.countTree(depth + 1, leadersOnly);
                if (total > MAX_ENTITIES_PER_WAVE) throw new IllegalArgumentException("Passenger tree is too large");
            }
            total *= count;
            if (total > MAX_ENTITIES_PER_WAVE) throw new IllegalArgumentException("Participant tree is too large");
            return total;
        }

        public ResourceLocation getEntity() { return entity; }
        public int getCount() { return count; }
        public boolean isLeader() { return leader; }
        public NBTTagCompound getNbt() { return nbt.copy(); }
        public List<Participant> getPassengers() { return passengers; }
    }

    public static final class Timing {
        private final int preparationTicks;
        private final int waveDelayTicks;
        private final int victoryDelayTicks;
        private final int celebrationTicks;
        private final int timeoutTicks;

        public Timing(int preparationTicks, int waveDelayTicks, int victoryDelayTicks,
                      int celebrationTicks, int timeoutTicks) {
            this.preparationTicks = range(preparationTicks, 0, Integer.MAX_VALUE, "preparationTicks");
            this.waveDelayTicks = range(waveDelayTicks, 0, Integer.MAX_VALUE, "waveDelayTicks");
            this.victoryDelayTicks = range(victoryDelayTicks, 0, Integer.MAX_VALUE, "victoryDelayTicks");
            this.celebrationTicks = range(celebrationTicks, 0, Integer.MAX_VALUE, "celebrationTicks");
            this.timeoutTicks = range(timeoutTicks, 1, Integer.MAX_VALUE, "timeoutTicks");
        }

        public int getPreparationTicks() { return preparationTicks; }
        public int getWaveDelayTicks() { return waveDelayTicks; }
        public int getVictoryDelayTicks() { return victoryDelayTicks; }
        public int getCelebrationTicks() { return celebrationTicks; }
        public int getTimeoutTicks() { return timeoutTicks; }
    }

    public static final class BossBar {
        private final boolean enabled;
        private final String title;
        private final String victoryTitle;
        private final String defeatTitle;
        private final BossInfo.Color color;
        private final BossInfo.Overlay overlay;

        public BossBar(boolean enabled, String title, String victoryTitle, String defeatTitle,
                       BossInfo.Color color, BossInfo.Overlay overlay) {
            this.enabled = enabled;
            this.title = RaidEventDefinition.title(title);
            this.victoryTitle = RaidEventDefinition.title(victoryTitle);
            this.defeatTitle = RaidEventDefinition.title(defeatTitle);
            this.color = Objects.requireNonNull(color, "color");
            this.overlay = Objects.requireNonNull(overlay, "overlay");
        }

        public boolean isEnabled() { return enabled; }
        public String getTitle() { return title; }
        public String getVictoryTitle() { return victoryTitle; }
        public String getDefeatTitle() { return defeatTitle; }
        public BossInfo.Color getColor() { return color; }
        public BossInfo.Overlay getOverlay() { return overlay; }
    }

    public static final class Reward {
        public enum Type { ITEM, LOOT_TABLE, EFFECT, EXPERIENCE }
        public enum Recipient { HEROES, TRIGGER_PLAYER }
        private final Type type;
        private final Recipient recipient;
        private final ResourceLocation id;
        private final int amount;
        private final int metadata;
        private final int duration;
        private final int amplifier;
        private final NBTTagCompound nbt;

        private Reward(Type type, Recipient recipient, @Nullable ResourceLocation id, int amount, int metadata,
                       int duration, int amplifier, NBTTagCompound nbt) {
            this.type = Objects.requireNonNull(type);
            this.recipient = Objects.requireNonNull(recipient);
            this.id = id == null ? null : identifier(id);
            if (type != Type.EXPERIENCE && id == null) throw new IllegalArgumentException("Reward needs an id");
            this.amount = range(amount, 1, type == Type.EXPERIENCE ? 1000000 : 1024, "reward amount");
            this.metadata = range(metadata, 0, 32767, "metadata");
            this.duration = range(duration, type == Type.EFFECT ? 1 : 0, Integer.MAX_VALUE, "duration");
            this.amplifier = range(amplifier, 0, 255, "amplifier");
            this.nbt = Objects.requireNonNull(nbt).copy();
        }

        public static Reward item(Recipient recipient, ResourceLocation id, int count, NBTTagCompound nbt) {
            return item(recipient, id, count, 0, nbt);
        }

        public static Reward item(Recipient recipient, ResourceLocation id, int count, int metadata,
                                  NBTTagCompound nbt) {
            return new Reward(Type.ITEM, recipient, id, count, metadata, 0, 0, nbt);
        }

        public static Reward lootTable(Recipient recipient, ResourceLocation id, int rolls) {
            return new Reward(Type.LOOT_TABLE, recipient, id, rolls, 0, 0, 0, new NBTTagCompound());
        }

        public static Reward effect(Recipient recipient, ResourceLocation id, int duration, int amplifier) {
            return new Reward(Type.EFFECT, recipient, id, 1, 0, duration, amplifier, new NBTTagCompound());
        }

        public static Reward experience(Recipient recipient, int amount) {
            return new Reward(Type.EXPERIENCE, recipient, null, amount, 0, 0, 0, new NBTTagCompound());
        }

        public Type getType() { return type; }
        public Recipient getRecipient() { return recipient; }
        @Nullable
        public ResourceLocation getId() { return id; }
        public int getAmount() { return amount; }
        public int getMetadata() { return metadata; }
        public int getDuration() { return duration; }
        public int getAmplifier() { return amplifier; }
        public NBTTagCompound getNbt() { return nbt.copy(); }
    }
}
