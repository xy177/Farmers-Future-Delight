package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class PacketRedstoneDots implements IMessage {
    private int dimension;
    private int chunkX;
    private int chunkZ;
    private long[] positions;

    public PacketRedstoneDots() {
    }

    public PacketRedstoneDots(int dimension, int chunkX, int chunkZ, long[] positions) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.positions = positions;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        dimension = buffer.readInt();
        chunkX = buffer.readInt();
        chunkZ = buffer.readInt();
        int count = buffer.readInt();
        if (count < 0 || count > 131072 || count > buffer.readableBytes() / 8)
            throw new IllegalArgumentException("Invalid redstone positions");
        positions = new long[count];
        for (int index = 0; index < count; index++) positions[index] = buffer.readLong();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        buffer.writeInt(positions.length);
        for (long pos : positions) buffer.writeLong(pos);
    }

    public static final class Handler implements IMessageHandler<PacketRedstoneDots, IMessage> {
        @Override
        public IMessage onMessage(PacketRedstoneDots message, MessageContext context) {
            FarmerFutureDelight.proxy.handleRedstoneDots(message.dimension, message.chunkX,
                    message.chunkZ, message.positions);
            return null;
        }
    }
}
