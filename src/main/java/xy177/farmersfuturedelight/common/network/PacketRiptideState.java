package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class PacketRiptideState implements IMessage {
    private int entityId;
    private int ticks;

    public PacketRiptideState() {
    }

    public PacketRiptideState(int entityId, int ticks) {
        this.entityId = entityId;
        this.ticks = ticks;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        entityId = buffer.readInt();
        ticks = buffer.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeByte(ticks);
    }

    public static final class Handler implements IMessageHandler<PacketRiptideState, IMessage> {
        @Override
        public IMessage onMessage(PacketRiptideState message, MessageContext context) {
            FarmerFutureDelight.proxy.handleRiptideState(message.entityId, message.ticks);
            return null;
        }
    }
}
