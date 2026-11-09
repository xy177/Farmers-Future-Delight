package xy177.farmersfuturedelight.common.raid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import xy177.farmersfuturedelight.api.raid.RaidEventCallbacks.Outcome;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition;
import xy177.farmersfuturedelight.api.raid.RaidEventRegistry;

public final class RaidScriptRuntime {
    private static final Map<EntityPlayerMP, Map<String, Long>> ERRORS = new WeakHashMap<>();

    private RaidScriptRuntime() { }

    public static void error(EntityPlayerMP player, String message) {
        Map<String, Long> messages = ERRORS.computeIfAbsent(player, key -> new java.util.HashMap<>());
        long now = player.getServerWorld().getTotalWorldTime();
        Long previous = messages.put(message, now);
        if (previous == null || now - previous >= 100) {
            TextComponentString text = new TextComponentString("[FFD] " + message);
            text.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(text);
        }
    }

    private static ResourceLocation identifier(@Nullable String id) { return id == null || id.isEmpty() ? null : RaidEventDefinition.identifier(id); }

    public static List<String> events(EntityPlayerMP player, @Nullable String id) {
        List<String> result = new ArrayList<>();
        try {
            for (RaidInstance instance : FFDRaidManager.forPlayer(player, identifier(id))) result.add(instance.definition.getId().toString());
        } catch (IllegalArgumentException exception) { error(player, exception.getMessage()); }
        return result;
    }

    private static List<RaidInstance> instances(EntityPlayerMP player, @Nullable String id) {
        try {
            return FFDRaidManager.forPlayer(player, identifier(id));
        } catch (IllegalArgumentException exception) {
            error(player, exception.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    public static boolean start(EntityPlayerMP player, String id) {
        try {
            ResourceLocation key = RaidEventDefinition.identifier(id);
            RaidEventDefinition definition = RaidEventRegistry.get(key);
            if (definition == null) {
                error(player, "Unknown raid event " + id);
                return false;
            }
            if (RaidScriptEvents.isDispatching()) {
                RaidScriptEvents.defer(player.getServerWorld(), () -> start(player, id));
                return true;
            }
            return FFDRaidManager.trigger(player.getServerWorld(), key, definition.getTrigger().getType(), player.getPosition(),
                    player.getUniqueID(), Math.max(definition.getTrigger().getMinOmenLevel(), key.equals(RaidDefaults.ID) ? 1 : 0)) != null;
        } catch (IllegalArgumentException exception) {
            error(player, exception.getMessage());
            return false;
        }
    }

    public static boolean inRaid(EntityPlayerMP player, @Nullable String id) { return !instances(player, id).isEmpty(); }

    public static int wave(EntityPlayerMP player, @Nullable String id) {
        List<RaidInstance> instances = instances(player, id);
        return instances.isEmpty() ? 0 : instances.get(0).displayedWave();
    }

    public static boolean inWave(EntityPlayerMP player, String id, int minimum, int maximum) {
        if (minimum <= 0 || maximum < minimum) {
            error(player, "Invalid raid wave range " + minimum + ".." + maximum);
            return false;
        }
        if (!validateWave(player, id, minimum) || !validateWave(player, id, maximum)) return false;
        for (RaidInstance instance : instances(player, id)) {
            int wave = instance.displayedWave();
            if (instance.inWave() && wave >= minimum && wave <= maximum) return true;
        }
        return false;
    }

    public static boolean validateWave(EntityPlayerMP player, String id, int number) {
        try {
            RaidEventDefinition definition = RaidEventRegistry.get(RaidEventDefinition.identifier(id));
            if (definition != null) {
                boolean found = definition.getScriptRules() == null
                        ? number > 0 && number <= definition.getWaves(player.world.getDifficulty()).size() + definition.getBonusWaves(player.world.getDifficulty()).size()
                        : definition.getScriptRules().numbers(definition, player.world.getDifficulty(), 255).contains(number);
                if (found) return true;
            }
            error(player, "Undefined raid wave " + id + " / " + number);
        } catch (IllegalArgumentException exception) { error(player, exception.getMessage()); }
        return false;
    }

    public static long elapsed(EntityPlayerMP player, @Nullable String id, int number) {
        if (number < 0) { error(player, "Invalid raid wave " + number); return 0; }
        List<RaidInstance> instances = instances(player, id);
        if (number > 0 && id != null && !id.isEmpty() && !validateWave(player, id, number)) return 0;
        if (instances.isEmpty()) return 0;
        RaidInstance instance = instances.get(0);
        if (number > 0 && !instance.waveNumbers().contains(number)) {
            error(player, "Undefined raid wave " + instance.definition.getId() + " / " + number);
            return 0;
        }
        return instance.elapsed(number);
    }

    public static void endWave(EntityPlayerMP player, @Nullable String id, String remaining) {
        if (!"delete".equals(remaining) && !"keep".equals(remaining) && !"add".equals(remaining)) {
            error(player, "Remaining mobs must be delete, keep or add");
            return;
        }
        List<RaidInstance> targets = new ArrayList<>(instances(player, id));
        Runnable action = () -> { for (RaidInstance instance : targets) instance.endWave(remaining); };
        if (RaidScriptEvents.isDispatching()) RaidScriptEvents.defer(player.getServerWorld(), action);
        else action.run();
    }

    public static void endRaid(EntityPlayerMP player, @Nullable String id, boolean clear, @Nullable String result) {
        Outcome outcome;
        if (result == null || result.equals("over")) outcome = Outcome.STOPPED;
        else if (result.equals("victory")) outcome = Outcome.VICTORY;
        else if (result.equals("defeat")) outcome = Outcome.DEFEAT;
        else { error(player, "Raid result must be victory, defeat or over"); return; }
        List<RaidInstance> targets = new ArrayList<>(instances(player, id));
        Runnable action = () -> {
            for (RaidInstance instance : targets) {
                if (!instance.isOngoing()) continue;
                instance.releaseMembers(clear);
                instance.finish(outcome);
            }
        };
        if (RaidScriptEvents.isDispatching()) RaidScriptEvents.defer(player.getServerWorld(), action);
        else action.run();
    }
}
