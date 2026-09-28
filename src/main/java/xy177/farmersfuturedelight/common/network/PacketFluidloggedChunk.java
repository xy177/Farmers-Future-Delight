package xy177.farmersfuturedelight.common.network;

import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class PacketFluidloggedChunk implements IMessage {
    private int dimension;
    private int chunkX;
    private int chunkZ;
    private Map<Long, String> fluids = new LinkedHashMap<>();

    public PacketFluidloggedChunk() {
    }

    public PacketFluidloggedChunk(int dimension, int chunkX, int chunkZ,
                                  Map<Long, String> fluids) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.fluids.putAll(fluids);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        dimension = buffer.readInt();
        chunkX = buffer.readInt();
        chunkZ = buffer.readInt();
        int count = ByteBufUtils.readVarInt(buffer, 3);
        if (count < 0 || count > 65536) {
            throw new IllegalArgumentException("Invalid fluidlogged entry count: " + count);
        }
        fluids = new LinkedHashMap<>();
        for (int index = 0; index < count; index++) {
            fluids.put(buffer.readLong(), ByteBufUtils.readUTF8String(buffer));
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        ByteBufUtils.writeVarInt(buffer, fluids.size(), 3);
        for (Map.Entry<Long, String> entry : fluids.entrySet()) {
            buffer.writeLong(entry.getKey());
            ByteBufUtils.writeUTF8String(buffer, entry.getValue());
        }
    }

    public static final class Handler implements IMessageHandler<PacketFluidloggedChunk, IMessage> {
        @Override
        public IMessage onMessage(PacketFluidloggedChunk message, MessageContext context) {
            FarmerFutureDelight.proxy.handleFluidloggedChunk(message.dimension, message.chunkX,
                    message.chunkZ, message.fluids);
            return null;
        }
    }
}
