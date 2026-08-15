package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class PacketDripstoneParticle implements IMessage {
    private double x;
    private double y;
    private double z;
    private boolean lava;

    public PacketDripstoneParticle() {
    }

    public PacketDripstoneParticle(double x, double y, double z, boolean lava) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.lava = lava;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        lava = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeBoolean(lava);
    }

    public static final class Handler implements IMessageHandler<PacketDripstoneParticle, IMessage> {
        @Override
        public IMessage onMessage(PacketDripstoneParticle message, MessageContext context) {
            FarmerFutureDelight.proxy.handleDripstoneParticle(
                    message.x, message.y, message.z, message.lava);
            return null;
        }
    }
}
