package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDCookingRecipeBook;

public final class PacketCookingBook implements IMessage {
    private NBTTagCompound data;

    public PacketCookingBook() {
    }

    public PacketCookingBook(NBTTagCompound data) {
        this.data = data.copy();
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        data = ByteBufUtils.readTag(buffer);
        if (data == null) throw new IllegalArgumentException("Missing cooking book payload");
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeTag(buffer, data);
    }

    public static final class ServerHandler implements IMessageHandler<PacketCookingBook, IMessage> {
        @Override
        public IMessage onMessage(PacketCookingBook message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> FFDCookingRecipeBook.handle(player, message.data));
            return null;
        }
    }

    public static final class ClientHandler implements IMessageHandler<PacketCookingBook, IMessage> {
        @Override
        public IMessage onMessage(PacketCookingBook message, MessageContext context) {
            FarmerFutureDelight.proxy.handleCookingBook(message.data);
            return null;
        }
    }
}
