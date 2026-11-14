package xy177.farmersfuturedelight.common.entity;

import net.minecraft.world.World;

public final class EntityPiglinBrute extends EntityPiglin {
    public EntityPiglinBrute(World world) { super(world); enablePersistence(); }
    @Override public boolean isBrute() { return true; }
}
