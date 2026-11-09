package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.common.FFDSignText;

public final class PacketSignText implements IMessage {
    private BlockPos position;
    private boolean back;
    private String[] lines;

    public PacketSignText() {
    }

    public PacketSignText(BlockPos position, boolean back, String[] lines) {
        this.position = position;
        this.back = back;
        this.lines = lines.clone();
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        position = BlockPos.fromLong(buffer.readLong());
        back = buffer.readBoolean();
        lines = new String[4];
        for (int index = 0; index < lines.length; index++) {
            lines[index] = ByteBufUtils.readUTF8String(buffer);
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(position.toLong());
        buffer.writeBoolean(back);
        for (int index = 0; index < 4; index++) {
            ByteBufUtils.writeUTF8String(buffer, index < lines.length ? lines[index] : "");
        }
    }

    public static final class Handler implements IMessageHandler<PacketSignText, IMessage> {
        @Override
        public IMessage onMessage(PacketSignText message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> apply(message, player));
            return null;
        }

        private static void apply(PacketSignText message, EntityPlayerMP player) {
            TileEntity tile = player.world.getTileEntity(message.position);
            if (!(tile instanceof TileEntitySign) || FFDSignText.isWaxed((TileEntitySign) tile)) {
                return;
            }
            TileEntitySign sign = (TileEntitySign) tile;
            if (sign.getPlayer() != player
                    || player.getDistanceSq(message.position.getX() + 0.5D,
                    message.position.getY() + 0.5D, message.position.getZ() + 0.5D) > 64.0D) {
                return;
            }
            ITextComponent[] text = new ITextComponent[4];
            ITextComponent[] previous = message.back ? FFDSignText.getBackText(sign) : sign.signText;
            for (int index = 0; index < text.length; index++) {
                String value = message.lines != null && index < message.lines.length
                        ? message.lines[index] : "";
                text[index] = new TextComponentString(value.length() > 90
                        ? value.substring(0, 90) : value);
                if (previous[index] != null) {
                    text[index].setStyle(previous[index].getStyle().createShallowCopy());
                }
            }
            if (message.back) {
                FFDSignText.setBackText(sign, text);
            } else {
                System.arraycopy(text, 0, sign.signText, 0, sign.signText.length);
                sign.markDirty();
            }
            sign.setPlayer(null);
            player.world.notifyBlockUpdate(message.position,
                    player.world.getBlockState(message.position),
                    player.world.getBlockState(message.position), 3);
        }
    }
}
