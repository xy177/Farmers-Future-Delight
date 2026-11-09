package xy177.farmersfuturedelight.common.raid;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenExpansion("crafttweaker.player.IPlayer")
public final class RaidPlayerScripts {
    private RaidPlayerScripts() { }

    private static EntityPlayerMP player(IPlayer input) {
        EntityPlayer player = CraftTweakerMC.getPlayer(input);
        return player instanceof EntityPlayerMP ? (EntityPlayerMP) player : null;
    }

    @ZenMethod
    public static boolean startRaid(IPlayer input, String id) {
        EntityPlayerMP player = player(input);
        return player != null && RaidScriptRuntime.start(player, id);
    }

    @ZenMethod
    public static boolean isInRaid(IPlayer input, @Optional String id) {
        EntityPlayerMP player = player(input);
        return player != null && RaidScriptRuntime.inRaid(player, id);
    }

    @ZenMethod
    public static boolean isInRaidWave(IPlayer input, String id, int number) { return isInRaidWave(input, id, number, number); }

    @ZenMethod
    public static boolean isInRaidWave(IPlayer input, String id, int minimum, int maximum) {
        EntityPlayerMP player = player(input);
        return player != null && RaidScriptRuntime.inWave(player, id, minimum, maximum);
    }

    @ZenMethod
    public static int getRaidWave(IPlayer input, @Optional String id) {
        EntityPlayerMP player = player(input);
        return player == null ? 0 : RaidScriptRuntime.wave(player, id);
    }

    @ZenMethod
    public static String[] getRaids(IPlayer input) {
        EntityPlayerMP player = player(input);
        return player == null ? new String[0] : RaidScriptRuntime.events(player, null).toArray(new String[0]);
    }

    @ZenMethod
    public static void endRaidWave(IPlayer input, String id, boolean clear) { endRaidWave(input, id, clear ? "delete" : "keep"); }
    @ZenMethod
    public static void endRaidWave(IPlayer input, boolean clear) { endRaidWave(input, null, clear); }
    @ZenMethod
    public static void endRaidWave(IPlayer input, String remaining) { endRaidWave(input, null, remaining); }
    @ZenMethod
    public static void endRaidWave(IPlayer input, String id, String remaining) {
        EntityPlayerMP player = player(input);
        if (player != null) RaidScriptRuntime.endWave(player, id, remaining);
    }

    @ZenMethod
    public static void endRaid(IPlayer input, String id, boolean clear, boolean victory) { endRaid(input, id, clear, victory ? "victory" : "defeat"); }
    @ZenMethod
    public static void endRaid(IPlayer input, boolean clear, boolean victory) { endRaid(input, null, clear, victory); }
    @ZenMethod
    public static void endRaid(IPlayer input, boolean clear, @Optional String result) { endRaid(input, null, clear, result); }
    @ZenMethod
    public static void endRaid(IPlayer input, String id, boolean clear, @Optional String result) {
        EntityPlayerMP player = player(input);
        if (player != null) RaidScriptRuntime.endRaid(player, id, clear, result);
    }

    @ZenMethod
    public static long getRaidElapsedTicks(IPlayer input, @Optional String id, @Optional int number) {
        EntityPlayerMP player = player(input);
        return player == null ? 0 : RaidScriptRuntime.elapsed(player, id, number);
    }

    @ZenMethod
    public static long getRaidElapsedTicks(IPlayer input, int number) { return getRaidElapsedTicks(input, null, number); }

    @ZenMethod
    public static void onRaidEnter(IPlayer input, IRaidScriptHandler handler) { onRaidEnter(input, null, handler); }
    @ZenMethod
    public static void onRaidEnter(IPlayer input, String id, IRaidScriptHandler handler) {
        EntityPlayerMP player = player(input);
        if (player != null) RaidEventScripts.subscribe(player.getUniqueID(), id, 0, "enter", null, handler);
    }
    @ZenMethod
    public static void onRaidResult(IPlayer input, String result, IRaidScriptHandler handler) { onRaidResult(input, null, result, handler); }
    @ZenMethod
    public static void onRaidResult(IPlayer input, String id, String result, IRaidScriptHandler handler) {
        EntityPlayerMP player = player(input);
        if (player != null) RaidEventScripts.subscribe(player.getUniqueID(), id, 0, "result", RaidEventScripts.outcome(result), handler);
    }
    @ZenMethod
    public static void onRaidWave(IPlayer input, int wave, String phase, IRaidScriptHandler handler) { onRaidWave(input, null, wave, phase, handler); }
    @ZenMethod
    public static void onRaidWave(IPlayer input, String id, int wave, String phase, IRaidScriptHandler handler) {
        EntityPlayerMP player = player(input);
        if (player != null) RaidEventScripts.subscribe(player.getUniqueID(), id, wave,
                "start".equals(phase) ? "wave_start" : "over".equals(phase) ? "wave_end" : phase, null, handler);
    }
}
