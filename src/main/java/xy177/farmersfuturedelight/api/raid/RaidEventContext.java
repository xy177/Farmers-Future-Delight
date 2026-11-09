package xy177.farmersfuturedelight.api.raid;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;

public final class RaidEventContext {
    private final WorldServer world;
    private final UUID instanceId;
    private final ResourceLocation eventId;
    private final ResourceLocation triggerType;
    private final BlockPos center;
    private final ResourceLocation biome;
    private final UUID triggerPlayerId;
    private final EnumDifficulty difficulty;
    private final int dimension;
    private final int omenLevel;
    private final int wave;
    private final long activeTicks;
    private final int remainingParticipants;
    private final boolean village;
    private final Set<UUID> heroes;

    public RaidEventContext(WorldServer world, UUID instanceId, ResourceLocation eventId,
                            ResourceLocation triggerType, BlockPos center, ResourceLocation biome,
                            @Nullable UUID triggerPlayerId, int omenLevel, int wave, long activeTicks,
                            int remainingParticipants, boolean village, Collection<UUID> heroes) {
        this.world = Objects.requireNonNull(world, "world");
        this.instanceId = Objects.requireNonNull(instanceId, "instanceId");
        this.eventId = RaidEventDefinition.identifier(eventId);
        this.triggerType = RaidEventDefinition.identifier(triggerType);
        this.center = Objects.requireNonNull(center, "center").toImmutable();
        this.biome = RaidEventDefinition.identifier(biome);
        this.triggerPlayerId = triggerPlayerId;
        difficulty = world.getDifficulty();
        dimension = world.provider.getDimension();
        if (omenLevel < 0 || omenLevel > 255 || wave < 0 || wave > RaidEventDefinition.MAX_WAVES
                || activeTicks < 0 || remainingParticipants < 0) {
            throw new IllegalArgumentException("Invalid raid context counters");
        }
        this.omenLevel = omenLevel;
        this.wave = wave;
        this.activeTicks = activeTicks;
        this.remainingParticipants = remainingParticipants;
        this.village = village;
        Set<UUID> heroCopy = new LinkedHashSet<>(Objects.requireNonNull(heroes, "heroes"));
        for (UUID hero : heroCopy) Objects.requireNonNull(hero, "hero");
        this.heroes = Collections.unmodifiableSet(heroCopy);
    }

    public WorldServer getWorld() { return world; }
    public UUID getInstanceId() { return instanceId; }
    public ResourceLocation getEventId() { return eventId; }
    public ResourceLocation getTriggerType() { return triggerType; }
    public BlockPos getCenter() { return center; }
    public ResourceLocation getBiome() { return biome; }
    @Nullable
    public UUID getTriggerPlayerId() { return triggerPlayerId; }
    public EnumDifficulty getDifficulty() { return difficulty; }
    public int getDimension() { return dimension; }
    public int getOmenLevel() { return omenLevel; }
    public int getWave() { return wave; }
    public long getActiveTicks() { return activeTicks; }
    public int getRemainingParticipants() { return remainingParticipants; }
    public boolean isVillage() { return village; }
    public Set<UUID> getHeroes() { return heroes; }
}
