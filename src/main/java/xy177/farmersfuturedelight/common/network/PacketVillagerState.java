package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDVillagerTypes;
import xy177.farmersfuturedelight.common.registry.FFDZombieVillagerState;

public final class PacketVillagerState implements IMessage {
    private int dimension;
    private int entityId;
    private NBTTagCompound state;

    public PacketVillagerState() {
    }

    public PacketVillagerState(EntityVillager villager) {
        dimension = villager.dimension;
        entityId = villager.getEntityId();
        state = FFDVillagerTypes.clientState(villager);
    }

    public PacketVillagerState(EntityZombieVillager zombie) {
        dimension = zombie.dimension;
        entityId = zombie.getEntityId();
        state = FFDZombieVillagerState.clientState(zombie);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        dimension = buffer.readInt();
        entityId = buffer.readInt();
        state = ByteBufUtils.readTag(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);
        buffer.writeInt(entityId);
        ByteBufUtils.writeTag(buffer, state);
    }

    public static final class Handler implements IMessageHandler<PacketVillagerState, IMessage> {
        @Override
        public IMessage onMessage(PacketVillagerState message, MessageContext context) {
            if (message.state != null) FarmerFutureDelight.proxy.handleVillagerState(
                    message.dimension, message.entityId, message.state);
            return null;
        }
    }
}
