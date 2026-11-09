package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.common.tile.TileEntityJigsaw;
import xy177.farmersfuturedelight.common.worldgen.FFDJigsawPlacement;

public final class PacketJigsawEdit implements IMessage {
    private BlockPos position;
    private NBTTagCompound definition;
    private boolean generate;
    private boolean keepJigsaws;
    private int levels;

    public PacketJigsawEdit() {
    }

    public PacketJigsawEdit(BlockPos position, NBTTagCompound definition,
                            boolean generate, boolean keepJigsaws, int levels) {
        this.position = position;
        this.definition = definition.copy();
        this.generate = generate;
        this.keepJigsaws = keepJigsaws;
        this.levels = levels;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(position.toLong());
        ByteBufUtils.writeTag(buffer, definition);
        buffer.writeBoolean(generate);
        buffer.writeBoolean(keepJigsaws);
        buffer.writeInt(levels);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        position = BlockPos.fromLong(buffer.readLong());
        definition = ByteBufUtils.readTag(buffer);
        generate = buffer.readBoolean();
        keepJigsaws = buffer.readBoolean();
        levels = buffer.readInt();
    }

    public static final class Handler implements IMessageHandler<PacketJigsawEdit, IMessage> {
        @Override
        public IMessage onMessage(PacketJigsawEdit message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> apply(player, message));
            return null;
        }

        private static void apply(EntityPlayerMP player, PacketJigsawEdit message) {
            if (!player.canUseCommandBlock() || message.definition == null
                    || message.levels < 0 || message.levels > 20
                    || player.getDistanceSqToCenter(message.position) > 64.0D
                    || !player.world.isBlockLoaded(message.position)) return;
            TileEntity tile = player.world.getTileEntity(message.position);
            if (!(tile instanceof TileEntityJigsaw)) return;
            try {
                ((TileEntityJigsaw) tile).configure(message.definition);
                if (message.generate) {
                    int pieces = FFDJigsawPlacement.generate(player.getServerWorld(),
                            (TileEntityJigsaw) tile, message.levels, message.keepJigsaws);
                    player.sendStatusMessage(new TextComponentTranslation(
                            "gui.farmers_future_delight.jigsaw.generated", pieces), false);
                }
            } catch (IllegalArgumentException exception) {
                player.sendStatusMessage(new TextComponentTranslation(
                        "gui.farmers_future_delight.jigsaw.error", exception.getMessage()), false);
            }
        }
    }
}
