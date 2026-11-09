package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.common.registry.FFDMerchantMenus;

public final class PacketMerchantSelect implements IMessage {
    private int windowId;
    private int selection;

    public PacketMerchantSelect() {
    }

    public PacketMerchantSelect(int windowId, int selection) {
        this.windowId = windowId;
        this.selection = selection;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        windowId = buffer.readInt();
        selection = buffer.readInt();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(windowId);
        buffer.writeInt(selection);
    }

    public static final class Handler implements IMessageHandler<PacketMerchantSelect, IMessage> {
        @Override
        public IMessage onMessage(PacketMerchantSelect message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() ->
                    FFDMerchantMenus.select(player, message.windowId, message.selection));
            return null;
        }
    }
}
