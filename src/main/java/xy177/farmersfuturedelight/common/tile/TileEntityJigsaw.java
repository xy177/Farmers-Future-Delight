package xy177.farmersfuturedelight.common.tile;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.common.block.BlockJigsaw;

public final class TileEntityJigsaw extends TileEntity {
    private NBTTagCompound definition = defaults();

    private static NBTTagCompound defaults() {
        NBTTagCompound data = new NBTTagCompound();
        data.setString("name", "minecraft:empty");
        data.setString("target", "minecraft:empty");
        data.setString("pool", "minecraft:empty");
        data.setString("final_state", "minecraft:air");
        data.setString("joint", "rollable");
        data.setInteger("selection_priority", 0);
        data.setInteger("placement_priority", 0);
        return data;
    }

    public NBTTagCompound getDefinition() {
        return definition.copy();
    }

    public void configure(NBTTagCompound data) {
        NBTTagCompound result = defaults();
        for (String key : new String[] {"name", "target", "pool"}) {
            String value = data.getString(key);
            if (value.length() > 256 || value.isEmpty()) throw new IllegalArgumentException(key);
            result.setString(key, new ResourceLocation(value).toString());
        }
        String finalState = data.getString("final_state");
        if (finalState.isEmpty() || finalState.length() > 1024) throw new IllegalArgumentException("final_state");
        xy177.farmersfuturedelight.common.worldgen.FFDJigsawPlacement.parseFinalState(finalState);
        result.setString("final_state", finalState);
        String joint = data.getString("joint");
        if (!"aligned".equals(joint) && !"rollable".equals(joint)) throw new IllegalArgumentException("joint");
        result.setString("joint", joint);
        result.setInteger("selection_priority", data.getInteger("selection_priority"));
        result.setInteger("placement_priority", data.getInteger("placement_priority"));
        definition = result;
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.merge(definition);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        NBTTagCompound result = defaults();
        for (String key : result.getKeySet()) {
            if (data.hasKey(key, result.getTagId(key))) result.setTag(key, data.getTag(key).copy());
        }
        if (!data.hasKey("joint", 8) && world != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockJigsaw
                    && state.getValue(BlockJigsaw.ORIENTATION).front.getAxis() != net.minecraft.util.EnumFacing.Axis.Y) {
                result.setString("joint", "aligned");
            }
        }
        definition = result;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }
}
