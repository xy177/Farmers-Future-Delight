package xy177.farmersfuturedelight.api.raid;

public interface RaidEventCallbacks {
    RaidEventCallbacks NONE = new RaidEventCallbacks() { };

    enum Lifecycle { START, RESTORE, TICK, WAVE_START, WAVE_END, FINISH }
    enum Outcome { CONTINUE, VICTORY, DEFEAT, STOPPED }

    default boolean canTrigger(RaidEventContext context) { return true; }
    default void onLifecycle(RaidEventContext context, Lifecycle phase) { }
    default Outcome getOutcome(RaidEventContext context) { return Outcome.CONTINUE; }
    default void onReward(RaidEventContext context, Outcome outcome) { }
}
