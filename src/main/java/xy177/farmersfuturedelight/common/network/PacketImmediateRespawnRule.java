package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class PacketImmediateRespawnRule implements IMessage {
    private boolean enabled;

    public PacketImmediateRespawnRule() {
    }

    public PacketImmediateRespawnRule(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        enabled = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeBoolean(enabled);
    }

    public static final class Handler implements IMessageHandler<PacketImmediateRespawnRule, IMessage> {
        @Override
        public IMessage onMessage(PacketImmediateRespawnRule message, MessageContext context) {
            FarmerFutureDelight.proxy.handleImmediateRespawnRule(message.enabled);
            return null;
        }
    }
}
