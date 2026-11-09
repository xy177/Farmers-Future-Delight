package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class PacketMooshroomState implements IMessage {
    private int dimension;
    private int entityId;
    private boolean brown;

    public PacketMooshroomState() {
    }

    public PacketMooshroomState(EntityMooshroom entity, boolean brown) {
        dimension = entity.dimension;
        entityId = entity.getEntityId();
        this.brown = brown;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        dimension = buffer.readInt();
        entityId = buffer.readInt();
        brown = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);
        buffer.writeInt(entityId);
        buffer.writeBoolean(brown);
    }

    public static final class Handler implements IMessageHandler<PacketMooshroomState, IMessage> {
        @Override
        public IMessage onMessage(PacketMooshroomState message, MessageContext context) {
            FarmerFutureDelight.proxy.handleMooshroomState(
                    message.dimension, message.entityId, message.brown);
            return null;
        }
    }
}
