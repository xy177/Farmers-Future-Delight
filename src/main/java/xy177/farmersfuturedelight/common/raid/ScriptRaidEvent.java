package xy177.farmersfuturedelight.common.raid;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IBlockPos;
import net.minecraft.entity.player.EntityPlayerMP;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import xy177.farmersfuturedelight.api.raid.RaidEventContext;

@ZenRegister
@ZenClass("mods.farmersfuturedelight.RaidEvent")
public final class ScriptRaidEvent {
    private final EntityPlayerMP player;
    private final RaidEventContext context;
    private final String phase;
    private final int wave;
    private final String result;

    ScriptRaidEvent(EntityPlayerMP player, RaidEventContext context, String phase, int wave, String result) {
        this.player = player;
        this.context = context;
        this.phase = phase;
        this.wave = wave;
        this.result = result;
    }

    @ZenGetter("player")
    public IPlayer getPlayer() { return CraftTweakerMC.getIPlayer(player); }
    @ZenGetter("id")
    public String getId() { return context.getEventId().toString(); }
    @ZenGetter("instanceId")
    public String getInstanceId() { return context.getInstanceId().toString(); }
    @ZenGetter("wave")
    public int getWave() { return wave; }
    @ZenGetter("phase")
    public String getPhase() { return phase; }
    @ZenGetter("result")
    public String getResult() { return result; }
    @ZenGetter("elapsedTicks")
    public long getElapsedTicks() { return context.getActiveTicks(); }
    @ZenGetter("center")
    public IBlockPos getCenter() { return CraftTweakerMC.getIBlockPos(context.getCenter()); }
}
