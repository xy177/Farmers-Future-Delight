package xy177.farmersfuturedelight.common.raid;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.farmersfuturedelight.Raids")
public final class RaidEventScripts {
    private RaidEventScripts() { }

    @ZenMethod
    public static ScriptRaidBuilder create(String id) { return new ScriptRaidBuilder(id, false); }

    @ZenMethod
    public static ScriptRaidBuilder get(String id) { return new ScriptRaidBuilder(id, true); }

    @ZenMethod
    public static void onEnter(IRaidScriptHandler handler) { onEnter(null, handler); }
    @ZenMethod
    public static void onEnter(String id, IRaidScriptHandler handler) { subscribe(null, id, 0, "enter", null, handler); }
    @ZenMethod
    public static void onResult(String result, IRaidScriptHandler handler) { onResult(null, result, handler); }
    @ZenMethod
    public static void onResult(String id, String result, IRaidScriptHandler handler) { subscribe(null, id, 0, "result", outcome(result), handler); }
    @ZenMethod
    public static void onWave(int wave, String phase, IRaidScriptHandler handler) { onWave(null, wave, phase, handler); }
    @ZenMethod
    public static void onWave(String id, int wave, String phase, IRaidScriptHandler handler) {
        subscribe(null, id, wave, "start".equals(phase) ? "wave_start" : "over".equals(phase) ? "wave_end" : phase, null, handler);
    }

    static String outcome(String result) {
        if (result == null) return "over";
        return "true".equals(result) ? "victory" : "false".equals(result) ? "defeat" : result;
    }

    static void subscribe(java.util.UUID playerId, String id, int wave, String phase, String result, IRaidScriptHandler handler) {
        if (handler == null) throw new IllegalArgumentException("Raid callback is null");
        if (id != null && !id.isEmpty()) xy177.farmersfuturedelight.api.raid.RaidEventDefinition.identifier(id);
        if (!phase.equals("enter") && !phase.equals("result") && !phase.equals("wave_start") && !phase.equals("wave_end")) {
            throw new IllegalArgumentException("Wave phase must be start or over");
        }
        if (phase.startsWith("wave_") && wave <= 0) throw new IllegalArgumentException("Wave number must be positive");
        if (result != null && !result.equals("victory") && !result.equals("defeat") && !result.equals("over")) {
            throw new IllegalArgumentException("Result must be victory, defeat or over");
        }
        if (phase.startsWith("wave_") && id != null && !id.isEmpty()) RaidScriptValidation.reference(id, wave);
        RaidScriptEvents.listen((player, context, actualPhase, actualWave, actualResult) -> {
            if (playerId != null && !player.getUniqueID().equals(playerId) || id != null && !id.isEmpty() && !context.getEventId().toString().equals(id)
                    || !phase.equals(actualPhase) || phase.startsWith("wave_") && actualWave != wave
                    || result != null && !result.equals(actualResult)) return;
            handler.handle(new ScriptRaidEvent(player, context, actualPhase, actualWave, actualResult));
        });
    }
}
