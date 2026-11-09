package xy177.farmersfuturedelight.api.raid;

import java.util.Objects;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class RaidParticipantEvent extends Event {
    private final EntityLiving entity;
    private final RaidEventContext context;
    private final boolean leader;

    public RaidParticipantEvent(EntityLiving entity, RaidEventContext context, boolean leader) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.context = Objects.requireNonNull(context, "context");
        this.leader = leader;
    }

    public EntityLiving getEntity() { return entity; }
    public RaidEventContext getContext() { return context; }
    public boolean isLeader() { return leader; }
}
