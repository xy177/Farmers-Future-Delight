package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData;

public final class PacketVerticalBiomes implements IMessage {
    private int dimension;
    private int chunkX;
    private int chunkZ;
    private byte[] biomes;

    public PacketVerticalBiomes() {
    }

    public PacketVerticalBiomes(int dimension, int chunkX, int chunkZ,
                                FFDVerticalBiomeData data) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        biomes = data.toByteArray();
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        dimension = buffer.readInt();
        chunkX = buffer.readInt();
        chunkZ = buffer.readInt();
        int length = ByteBufUtils.readVarInt(buffer, 3);
        if (length != FFDVerticalBiomeData.ENTRY_COUNT || length > buffer.readableBytes()) {
            throw new IllegalArgumentException("Invalid vertical biome payload length: " + length);
        }
        biomes = new byte[length];
        buffer.readBytes(biomes);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        ByteBufUtils.writeVarInt(buffer, biomes.length, 3);
        buffer.writeBytes(biomes);
    }

    public static final class Handler implements IMessageHandler<PacketVerticalBiomes, IMessage> {
        @Override
        public IMessage onMessage(PacketVerticalBiomes message, MessageContext context) {
            FarmerFutureDelight.proxy.handleVerticalBiomes(message.dimension, message.chunkX,
                    message.chunkZ, message.biomes);
            return null;
        }
    }
}
