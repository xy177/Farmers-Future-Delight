package xy177.farmersfuturedelight.common.entity;

import net.minecraft.entity.passive.EntityOcelot;

public final class FFDOcelotTrust {
    private static final String TRUSTING = "FFDTrusting";

    private FFDOcelotTrust() {
    }

    public static boolean isTrusting(EntityOcelot ocelot) {
        return ocelot.getEntityData().getBoolean(TRUSTING);
    }

    public static void setTrusting(EntityOcelot ocelot, boolean trusting) {
        ocelot.getEntityData().setBoolean(TRUSTING, trusting);
    }
}
