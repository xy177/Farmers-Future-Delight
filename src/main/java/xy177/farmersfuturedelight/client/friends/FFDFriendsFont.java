package xy177.farmersfuturedelight.client.friends;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;

final class FFDFriendsFont extends FontRenderer {
    private static final ResourceLocation ASCII = resource("ascii.png");
    private static final ResourceLocation UNICODE = resource("unicode.png");
    private static final ResourceLocation METRICS = resource("glyphs.json");
    private final Minecraft mc;
    private final Map<Character, Glyph> glyphs = new HashMap<>();
    private int atlasWidth;
    private int atlasHeight;

    FFDFriendsFont(Minecraft mc) {
        super(mc.gameSettings, ASCII, mc.getTextureManager(), mc.gameSettings.forceUnicodeFont);
        this.mc = mc;
    }

    private static ResourceLocation resource(String file) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/gui/friends/font/" + file);
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        super.onResourceManagerReload(manager);
        glyphs.clear();
        try (IResource resource = manager.getResource(METRICS);
                InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject data = new JsonParser().parse(reader).getAsJsonObject();
            atlasWidth = data.get("width").getAsInt();
            atlasHeight = data.get("height").getAsInt();
            for (Map.Entry<String, JsonElement> item : data.getAsJsonObject("glyphs").entrySet()) {
                char character = (char) Integer.parseInt(item.getKey());
                com.google.gson.JsonArray values = item.getValue().getAsJsonArray();
                Glyph glyph = new Glyph(values.get(0).getAsInt(), values.get(1).getAsInt(),
                        values.get(2).getAsInt(), values.get(3).getAsInt());
                glyphs.put(character, glyph);
                glyphWidth[character] = (byte) (glyph.left << 4 | glyph.right);
            }
        } catch (java.io.IOException | RuntimeException ignored) { glyphs.clear(); }
    }

    @Override
    protected float renderUnicodeChar(char character, boolean italic) {
        Glyph glyph = glyphs.get(character);
        if (glyph == null) return super.renderUnicodeChar(character, italic);
        mc.getTextureManager().bindTexture(UNICODE);
        float skew = italic ? 1 : 0;
        float width = (glyph.right - glyph.left + 1) / 2.0F;
        float u0 = (glyph.x + glyph.left) / (float) atlasWidth;
        float u1 = (glyph.x + glyph.right + 1 - 0.02F) / atlasWidth;
        float v0 = glyph.y / (float) atlasHeight;
        float v1 = (glyph.y + 15.98F) / atlasHeight;
        GlStateManager.glBegin(5);
        GlStateManager.glTexCoord2f(u0, v0); GlStateManager.glVertex3f(posX + skew, posY, 0);
        GlStateManager.glTexCoord2f(u0, v1); GlStateManager.glVertex3f(posX - skew, posY + 7.99F, 0);
        GlStateManager.glTexCoord2f(u1, v0); GlStateManager.glVertex3f(posX + width - 0.01F + skew, posY, 0);
        GlStateManager.glTexCoord2f(u1, v1); GlStateManager.glVertex3f(posX + width - 0.01F - skew, posY + 7.99F, 0);
        GlStateManager.glEnd();
        return (glyph.right - glyph.left + 1) / 2 + 1;
    }

    private static final class Glyph {
        final int x, y, left, right;
        Glyph(int x, int y, int left, int right) { this.x = x; this.y = y; this.left = left; this.right = right; }
    }
}
