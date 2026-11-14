package xy177.farmersfuturedelight.common.entity;

import net.minecraft.world.World;

public final class EntityZoglin extends EntityHoglin {
    public EntityZoglin(World world) {
        super(world);
        isImmuneToFire = true;
    }

    @Override
    public boolean isZoglin() {
        return true;
    }
}
