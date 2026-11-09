package xy177.farmersfuturedelight.common.raid;

import net.minecraft.world.GameRules;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class RaidGameRules {
    public static final String RAIDS = "minecraft:raids";
    public static final String PATROLS = "minecraft:spawn_patrols";
    public static final String TRADERS = "minecraft:spawn_wandering_traders";

    private RaidGameRules() {
    }

    public static void ensure(GameRules rules) {
        if (!rules.hasRule(RAIDS)) rules.addGameRule(RAIDS,
                Boolean.toString(!rules.hasRule("disableRaids") || !rules.getBoolean("disableRaids")),
                GameRules.ValueType.BOOLEAN_VALUE);
        if (!rules.hasRule(PATROLS)) rules.addGameRule(PATROLS,
                Boolean.toString(!rules.hasRule("doPatrolSpawning") || rules.getBoolean("doPatrolSpawning")),
                GameRules.ValueType.BOOLEAN_VALUE);
        if (!rules.hasRule(TRADERS)) rules.addGameRule(TRADERS,
                Boolean.toString(!rules.hasRule("doTraderSpawning") || rules.getBoolean("doTraderSpawning")),
                GameRules.ValueType.BOOLEAN_VALUE);
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) ensure(event.getWorld().getGameRules());
    }
}
