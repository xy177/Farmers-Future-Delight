package xy177.farmersfuturedelight.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDModernMerchantRecipe;
import xy177.farmersfuturedelight.common.registry.FFDModernVillagerTrades;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRestocking;

public final class PacketMerchantState implements IMessage {
    private int windowId;
    private int level;
    private int experience;
    private boolean progress;
    private NBTTagCompound offers;

    public PacketMerchantState() {
    }

    public PacketMerchantState(int windowId, IMerchant merchant, MerchantRecipeList recipes) {
        this.windowId = windowId;
        if (merchant instanceof EntityVillager && FFDVillagerRestocking.isManaged((EntityVillager) merchant)
                && FFDModernVillagerTrades.canManage((EntityVillager) merchant)) {
            progress = true;
            level = FFDVillagerRestocking.getLevel((EntityVillager) merchant);
            experience = FFDVillagerRestocking.getExperience((EntityVillager) merchant);
        }
        offers = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (MerchantRecipe recipe : recipes) {
            NBTTagCompound entry = recipe.writeToTags();
            entry.setTag("buy", recipe.getItemToBuy().writeToNBT(new NBTTagCompound()));
            entry.setInteger("ffdBaseCost", recipe instanceof FFDModernMerchantRecipe
                    ? ((FFDModernMerchantRecipe) recipe).getBaseCost().getCount() : recipe.getItemToBuy().getCount());
            entry.setInteger("ffdExperience", recipe instanceof FFDModernMerchantRecipe
                    ? ((FFDModernMerchantRecipe) recipe).getVillagerExperience() : 0);
            list.appendTag(entry);
        }
        offers.setTag("Recipes", list);
    }

    public int getWindowId() {
        return windowId;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public boolean showsProgress() {
        return progress;
    }

    public NBTTagCompound getOffers() {
        return offers;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        windowId = buffer.readInt();
        level = buffer.readUnsignedByte();
        experience = buffer.readInt();
        progress = buffer.readBoolean();
        offers = ByteBufUtils.readTag(buffer);
        if (offers == null || offers.getTagList("Recipes", 10).tagCount() > 255) {
            throw new IllegalArgumentException("Invalid merchant offer payload");
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(windowId);
        buffer.writeByte(level);
        buffer.writeInt(experience);
        buffer.writeBoolean(progress);
        ByteBufUtils.writeTag(buffer, offers);
    }

    public static final class Handler implements IMessageHandler<PacketMerchantState, IMessage> {
        @Override
        public IMessage onMessage(PacketMerchantState message, MessageContext context) {
            FarmerFutureDelight.proxy.handleMerchantState(message);
            return null;
        }
    }
}
