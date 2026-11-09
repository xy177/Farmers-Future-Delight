package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class PacketMapDecorations implements IMessage {
    private String mapName;
    private NBTTagCompound decorations;

    public PacketMapDecorations() {
    }

    public PacketMapDecorations(String mapName, NBTTagCompound decorations) {
        this.mapName = mapName;
        this.decorations = decorations;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        mapName = ByteBufUtils.readUTF8String(buffer);
        decorations = ByteBufUtils.readTag(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, mapName);
        ByteBufUtils.writeTag(buffer, decorations);
    }

    public static final class Handler implements IMessageHandler<PacketMapDecorations, IMessage> {
        @Override
        public IMessage onMessage(PacketMapDecorations message, MessageContext context) {
            if (message.decorations != null) FarmerFutureDelight.proxy.handleMapDecorations(
                    message.mapName, message.decorations);
            return null;
        }
    }
}
