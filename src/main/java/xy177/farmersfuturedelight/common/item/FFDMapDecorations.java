package xy177.farmersfuturedelight.common.item;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import xy177.farmersfuturedelight.common.network.FFDNetwork;

public final class FFDMapDecorations {
    private static final String DATA_KEY = "FFDMapDecorations";

    private FFDMapDecorations() {
    }

    @Nullable
    public static ResourceLocation texture(String decoration) {
        ResourceLocation id;
        try {
            id = new ResourceLocation(decoration);
        } catch (IllegalArgumentException exception) {
            return null;
        }
        if (!"minecraft".equals(id.getResourceDomain())) return null;
        String asset;
        switch (id.getResourcePath()) {
            case "village_desert": asset = "desert_village"; break;
            case "village_plains": asset = "plains_village"; break;
            case "village_savanna": asset = "savanna_village"; break;
            case "village_snowy": asset = "snowy_village"; break;
            case "village_taiga": asset = "taiga_village"; break;
            case "jungle_temple": asset = "jungle_temple"; break;
            case "swamp_hut": asset = "swamp_hut"; break;
            default: return null;
        }
        return new ResourceLocation("farmers_future_delight", "textures/map/decorations/" + asset + ".png");
    }

    public static void addTarget(ItemStack stack, World world, BlockPos position, String decoration) {
        MapDecoration.Type fallback = "minecraft:mansion".equals(decoration) ? MapDecoration.Type.MANSION
                : "minecraft:monument".equals(decoration) ? MapDecoration.Type.MONUMENT : MapDecoration.Type.TARGET_X;
        MapData.addTargetDecoration(stack, position, "+", fallback);
        if (texture(decoration) == null) return;
        MapData data = ((ItemMap) stack.getItem()).getMapData(stack, world);
        if (data instanceof FFDMapDecorationData) {
            NBTTagCompound decorations = get(data).copy();
            decorations.setString("+", decoration);
            ((FFDMapDecorationData) data).ffd$setDecorations(decorations);
            data.markDirty();
        }
    }

    public static NBTTagCompound get(MapData data) {
        NBTTagCompound decorations = data instanceof FFDMapDecorationData
                ? ((FFDMapDecorationData) data).ffd$getDecorations() : null;
        return decorations == null ? new NBTTagCompound() : decorations;
    }

    public static void read(MapData data, NBTTagCompound tag) {
        ((FFDMapDecorationData) data).ffd$setDecorations(tag.getCompoundTag(DATA_KEY).copy());
    }

    public static NBTTagCompound write(NBTTagCompound tag, MapData data) {
        NBTTagCompound decorations = get(data);
        if (!decorations.hasNoTags()) tag.setTag(DATA_KEY, decorations.copy());
        return tag;
    }

    public static boolean updateTarget(MapData data, World world, String name,
                                    double targetX, double targetZ, double rotation) {
        if (!(data instanceof FFDMapDecorationData)) return false;
        NBTTagCompound decorations = ((FFDMapDecorationData) data).ffd$getDecorations();
        if (decorations == null || !decorations.hasKey(name, 8)
                || texture(decorations.getString(name)) == null) return false;
        int factor = 1 << data.scale;
        float horizontal = (float) (targetX - data.xCenter) / factor;
        float vertical = (float) (targetZ - data.zCenter) / factor;
        if (!data.unlimitedTracking && (horizontal < -63.0F || horizontal > 63.0F
                || vertical < -63.0F || vertical > 63.0F)) {
            data.mapDecorations.remove(name);
            return true;
        }
        byte heading;
        if (data.dimension == -1 && world != null) {
            int time = (int) (world.getTotalWorldTime() / 10L);
            heading = (byte) (time * time * 34187121 + time * 121 >> 15 & 15);
        } else {
            rotation += rotation < 0.0D ? -8.0D : 8.0D;
            heading = (byte) (rotation * 16.0D / 360.0D);
        }
        data.mapDecorations.put(name, new MapDecoration(MapDecoration.Type.TARGET_X,
                coordinate(horizontal), coordinate(vertical), heading));
        return true;
    }

    private static byte coordinate(float position) {
        if (position <= -63.0F) return -128;
        if (position >= 63.0F) return 127;
        return (byte) (position * 2.0F + 0.5D);
    }

    public static Packet<?> sync(@Nullable Packet<?> packet, MapData data, EntityPlayer player) {
        NBTTagCompound decorations = get(data);
        if (packet == null || decorations.hasNoTags() || !(player instanceof EntityPlayerMP)) return packet;
        NBTTagCompound icons = new NBTTagCompound();
        int index = 0;
        for (Map.Entry<String, MapDecoration> entry : data.mapDecorations.entrySet()) {
            String decoration = decorations.getString(entry.getKey());
            if (texture(decoration) != null) icons.setString("icon-" + index, decoration);
            index++;
        }
        FFDNetwork.sendMapDecorations((EntityPlayerMP) player, data.mapName, icons);
        return packet;
    }
}
