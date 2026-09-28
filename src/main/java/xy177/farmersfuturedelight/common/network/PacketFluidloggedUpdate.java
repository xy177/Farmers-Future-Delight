package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class PacketFluidloggedUpdate implements IMessage {
    private int dimension;
    private long position;
    private String fluidName;

    public PacketFluidloggedUpdate() {
    }

    public PacketFluidloggedUpdate(int dimension, BlockPos pos, String fluidName) {
        this.dimension = dimension;
        position = pos.toLong();
        this.fluidName = fluidName == null ? "" : fluidName;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        dimension = buffer.readInt();
        position = buffer.readLong();
        fluidName = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);
        buffer.writeLong(position);
        ByteBufUtils.writeUTF8String(buffer, fluidName);
    }

    public static final class Handler implements IMessageHandler<PacketFluidloggedUpdate, IMessage> {
        @Override
        public IMessage onMessage(PacketFluidloggedUpdate message, MessageContext context) {
            FarmerFutureDelight.proxy.handleFluidloggedUpdate(message.dimension,
                    BlockPos.fromLong(message.position), message.fluidName);
            return null;
        }
    }
}
