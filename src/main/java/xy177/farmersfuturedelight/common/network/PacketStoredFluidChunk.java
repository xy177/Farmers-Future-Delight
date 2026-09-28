package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class PacketStoredFluidChunk implements IMessage {
    private int dimension, x, z;
    private NBTTagCompound data;
    private boolean replace;
    public PacketStoredFluidChunk() {}
    public PacketStoredFluidChunk(int dimension, int x, int z, NBTTagCompound data, boolean replace) {
        this.dimension = dimension; this.x = x; this.z = z; this.data = data; this.replace = replace;
    }
    @Override public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt(); x = buf.readInt(); z = buf.readInt();
        replace = buf.readBoolean();
        data = ByteBufUtils.readTag(buf);
    }
    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension); buf.writeInt(x); buf.writeInt(z);
        buf.writeBoolean(replace);
        ByteBufUtils.writeTag(buf, data);
    }
    public static final class Handler implements IMessageHandler<PacketStoredFluidChunk, IMessage> {
        @Override public IMessage onMessage(PacketStoredFluidChunk msg, MessageContext ctx) {
            if (msg.data != null) FarmerFutureDelight.proxy.handleStoredFluidChunk(
                    msg.dimension, msg.x, msg.z, msg.data, msg.replace);
            return null;
        }
    }
}
