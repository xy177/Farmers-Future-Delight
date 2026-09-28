package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.fluid.FFDFluidMigration;

public final class PacketFluidMigration implements IMessage {
    private String token, host, fluid;
    private boolean convert;
    public PacketFluidMigration() {}
    public PacketFluidMigration(String token, String host, String fluid, boolean convert) {
        this.token = token; this.host = host; this.fluid = fluid; this.convert = convert;
    }
    @Override public void fromBytes(ByteBuf buf) {
        token = ByteBufUtils.readUTF8String(buf);
        host = ByteBufUtils.readUTF8String(buf);
        fluid = ByteBufUtils.readUTF8String(buf);
        convert = buf.readBoolean();
        if (token.length() > 36 || host.length() > 256 || fluid.length() > 256)
            throw new IllegalArgumentException("Invalid migration response");
    }
    @Override public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, token);
        ByteBufUtils.writeUTF8String(buf, host);
        ByteBufUtils.writeUTF8String(buf, fluid);
        buf.writeBoolean(convert);
    }
    public static final class ClientHandler implements IMessageHandler<PacketFluidMigration, IMessage> {
        @Override public IMessage onMessage(PacketFluidMigration msg, MessageContext ctx) {
            FarmerFutureDelight.proxy.handleMigrationPrompt(msg.token, msg.host, msg.fluid, msg.convert);
            return null;
        }
    }
    public static final class ServerHandler implements IMessageHandler<PacketFluidMigration, IMessage> {
        @Override public IMessage onMessage(PacketFluidMigration msg, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> player.sendMessage(
                    new TextComponentTranslation(FFDFluidMigration.decide(
                            player.getServer(), player, msg.token, msg.convert))));
            return null;
        }
    }
}
