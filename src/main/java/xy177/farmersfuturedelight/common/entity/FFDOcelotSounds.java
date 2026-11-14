package xy177.farmersfuturedelight.common.entity;

import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.util.SoundEvent;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class FFDOcelotSounds {
    private FFDOcelotSounds() {
    }

    private static boolean enabled(EntityOcelot ocelot) {
        return ocelot.getClass() == EntityOcelot.class
                && xy177.farmersfuturedelight.common.FFDConfig.catMode
                        != xy177.farmersfuturedelight.common.FFDConfig.FeatureMode.DISABLED;
    }

    public static SoundEvent sound(SoundEvent original, EntityOcelot ocelot, int type) {
        if (!enabled(ocelot)) return original;
        return type == 0 ? FFDSounds.OCELOT_AMBIENT
                : type == 1 ? FFDSounds.OCELOT_HURT : FFDSounds.OCELOT_DEATH;
    }

    public static float volume(float original, EntityOcelot ocelot) {
        return enabled(ocelot) ? 1.0F : original;
    }

    public static int interval(int original, EntityOcelot ocelot) {
        return enabled(ocelot) ? 900 : original;
    }
}
