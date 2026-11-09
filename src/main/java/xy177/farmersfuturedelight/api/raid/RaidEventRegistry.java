package xy177.farmersfuturedelight.api.raid;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Participant;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Reward;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Wave;

public final class RaidEventRegistry {
    private static volatile Map<ResourceLocation, RaidEventDefinition> definitions = Collections.emptyMap();

    private RaidEventRegistry() { }

    public static synchronized void register(RaidEventDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        validateReferences(definition);
        Map<ResourceLocation, RaidEventDefinition> next = new LinkedHashMap<>(definitions);
        next.put(definition.getId(), definition);
        definitions = Collections.unmodifiableMap(next);
    }

    public static synchronized void registerData(RaidEventDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        RaidEventDefinition previous = definitions.get(definition.getId());
        register(previous == null ? definition : definition.toBuilder().callbacks(previous.getCallbacks()).build());
    }

    public static synchronized boolean remove(ResourceLocation id) {
        ResourceLocation key = RaidEventDefinition.identifier(id);
        if (!definitions.containsKey(key)) return false;
        Map<ResourceLocation, RaidEventDefinition> next = new LinkedHashMap<>(definitions);
        next.remove(key);
        definitions = Collections.unmodifiableMap(next);
        return true;
    }

    @Nullable
    public static RaidEventDefinition get(ResourceLocation id) {
        return definitions.get(RaidEventDefinition.identifier(id));
    }

    public static Map<ResourceLocation, RaidEventDefinition> snapshot() { return definitions; }

    private static void validateReferences(RaidEventDefinition definition) {
        if (definition.getScriptRules() != null) definition.getScriptRules().validateReferences(definition);
        for (java.util.List<Wave> waves : definition.getWaves().values()) validateWaves(waves);
        for (java.util.List<Wave> waves : definition.getBonusWaves().values()) validateWaves(waves);
        for (ResourceLocation biome : definition.getTrigger().getBiomes()) {
            if (!ForgeRegistries.BIOMES.containsKey(biome)) {
                throw new IllegalArgumentException("Unknown trigger biome " + biome);
            }
        }
        for (Reward reward : definition.getVictoryRewards()) validateReward(reward);
        for (Reward reward : definition.getDefeatRewards()) validateReward(reward);
    }

    private static void validateWaves(java.util.List<Wave> waves) {
        for (Wave wave : waves) {
            for (Participant participant : wave.getParticipants()) validateParticipant(participant);
        }
    }

    private static void validateParticipant(Participant participant) {
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(participant.getEntity());
        if (entry == null || entry.getEntityClass() == null
                || !EntityLiving.class.isAssignableFrom(entry.getEntityClass())) {
            throw new IllegalArgumentException("Expected a registered living mob: " + participant.getEntity());
        }
        for (Participant passenger : participant.getPassengers()) validateParticipant(passenger);
    }

    private static void validateReward(Reward reward) {
        if (reward.getType() == Reward.Type.ITEM) {
            Item item = ForgeRegistries.ITEMS.getValue(reward.getId());
            if (item == null || item == Items.AIR) throw new IllegalArgumentException("Unknown reward item " + reward.getId());
        } else if (reward.getType() == Reward.Type.EFFECT
                && !ForgeRegistries.POTIONS.containsKey(reward.getId())) {
            throw new IllegalArgumentException("Unknown reward effect " + reward.getId());
        }
    }
}
