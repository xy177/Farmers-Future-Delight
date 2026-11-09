package xy177.farmersfuturedelight.common.entity.ai;

import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import xy177.farmersfuturedelight.common.entity.FFDOcelotTrust;

public class EntityAIOcelotAvoidUntrustedPlayer extends EntityAIAvoidEntity<EntityPlayer> {
    public EntityAIOcelotAvoidUntrustedPlayer(final EntityOcelot ocelot) {
        super(ocelot, EntityPlayer.class, 16.0F, 0.8D, 1.33D);
    }

    @Override
    public boolean shouldExecute() {
        return !FFDOcelotTrust.isTrusting((EntityOcelot) entity) && super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !FFDOcelotTrust.isTrusting((EntityOcelot) entity)
                && super.shouldContinueExecuting();
    }
}
