package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.FFDAgeLock;

public final class PacketAgeLockState implements IMessage {
    private int dimension;
    private int entityId;
    private boolean locked;
    private int timer;

    public PacketAgeLockState() {
    }

    public PacketAgeLockState(EntityLivingBase entity) {
        dimension = entity.dimension;
        entityId = entity.getEntityId();
        locked = FFDAgeLock.isLocked(entity);
        timer = FFDAgeLock.feedbackTicks(entity);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        dimension = buffer.readInt();
        entityId = buffer.readInt();
        locked = buffer.readBoolean();
        timer = buffer.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);
        buffer.writeInt(entityId);
        buffer.writeBoolean(locked);
        buffer.writeByte(timer);
    }

    public static final class Handler implements IMessageHandler<PacketAgeLockState, IMessage> {
        @Override
        public IMessage onMessage(PacketAgeLockState message, MessageContext context) {
            FarmerFutureDelight.proxy.handleAgeLockState(message.dimension, message.entityId,
                    message.locked, message.timer);
            return null;
        }
    }
}
