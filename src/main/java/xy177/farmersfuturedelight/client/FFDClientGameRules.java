package xy177.farmersfuturedelight.client;

public final class FFDClientGameRules {
    private static boolean immediateRespawn;

    private FFDClientGameRules() {
    }

    public static boolean immediateRespawn() {
        return immediateRespawn;
    }

    public static void setImmediateRespawn(boolean enabled) {
        immediateRespawn = enabled;
    }
}
