package xy177.farmersfuturedelight.client;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import xy177.farmersfuturedelight.common.item.FFDMapDecorationData;
import xy177.farmersfuturedelight.common.item.FFDMapDecorations;

public final class FFDMapDecorationRenderer {
    private FFDMapDecorationRenderer() {
    }

    public static void receive(String mapName, NBTTagCompound decorations) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null) return;
        MapData data = (MapData) minecraft.world.loadData(MapData.class, mapName);
        if (data == null) {
            data = new MapData(mapName);
            minecraft.world.setData(mapName, data);
        }
        ((FFDMapDecorationData) data).ffd$setDecorations(decorations.copy());
    }

    public static void prepare(MapData data) {
        NBTTagCompound decorations = FFDMapDecorations.get(data);
        if (decorations.hasNoTags()) return;
        for (Map.Entry<String, MapDecoration> entry : data.mapDecorations.entrySet()) {
            ResourceLocation texture = FFDMapDecorations.texture(decorations.getString(entry.getKey()));
            if (texture == null) continue;
            MapDecoration original = entry.getValue();
            if (!(original instanceof Decoration) || !texture.equals(((Decoration) original).texture))
                entry.setValue(new Decoration(original, texture));
        }
    }

    private static final class Decoration extends MapDecoration {
        private final ResourceLocation texture;

        private Decoration(MapDecoration original, ResourceLocation texture) {
            super(original.getType(), original.getX(), original.getY(), original.getRotation());
            this.texture = texture;
        }

        @Override
        public boolean renderOnFrame() {
            return true;
        }

        @Override
        public boolean render(int index) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            GlStateManager.pushMatrix();
            GlStateManager.translate(getX() / 2.0F + 64.0F, getY() / 2.0F + 64.0F, -0.02F);
            GlStateManager.rotate(getRotation() * 360.0F / 16.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(4.0F, 4.0F, 3.0F);
            GlStateManager.translate(-0.125F, 0.125F, 0.0F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            double depth = index * -0.001D;
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-1.0D, 1.0D, depth).tex(0.0D, 0.0D).endVertex();
            buffer.pos(1.0D, 1.0D, depth).tex(1.0D, 0.0D).endVertex();
            buffer.pos(1.0D, -1.0D, depth).tex(1.0D, 1.0D).endVertex();
            buffer.pos(-1.0D, -1.0D, depth).tex(0.0D, 1.0D).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
            return true;
        }
    }
}
