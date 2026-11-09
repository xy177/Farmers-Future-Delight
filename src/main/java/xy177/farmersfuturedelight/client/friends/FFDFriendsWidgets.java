package xy177.farmersfuturedelight.client.friends;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class FFDFriendsWidgets {
    private static FFDFriendsFont font;
    private FFDFriendsWidgets() { }

    public static FontRenderer font() {
        Minecraft mc = Minecraft.getMinecraft();
        if (font == null) {
            font = new FFDFriendsFont(mc);
            ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(font);
        }
        font.setUnicodeFlag(mc.gameSettings.forceUnicodeFont);
        font.setBidiFlag(mc.fontRenderer.getBidiFlag());
        return font;
    }

    public static String text(String key, Object... args) {
        return I18n.format("ffd.friends.vanilla." + key, args).replace("\\n", "\n");
    }

    public static ResourceLocation texture(String key) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/gui/friends/" + key + ".png");
    }

    public static void sprite(String key, int x, int y, int w, int h, int sourceW, int sourceH) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture(key));
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Gui.drawScaledCustomSizeModalRect(x, y, 0, 0, sourceW, sourceH, w, h, sourceW, sourceH);
    }

    public static void panel(String key, int x, int y, int width, int height,
            int sourceWidth, int sourceHeight, int border) {
        panel(key, x, y, width, height, sourceWidth, sourceHeight, border, false);
    }

    private static void panel(String key, int x, int y, int width, int height,
            int sourceWidth, int sourceHeight, int border, boolean stretch) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture(key));
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        int[] dx = {x, x + border, x + width - border, x + width};
        int[] dy = {y, y + border, y + height - border, y + height};
        int[] sx = {0, border, sourceWidth - border, sourceWidth};
        int[] sy = {0, border, sourceHeight - border, sourceHeight};
        for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++) {
            int tw = sx[col + 1] - sx[col], th = sy[row + 1] - sy[row];
            if (stretch) {
                Gui.drawScaledCustomSizeModalRect(dx[col], dy[row], sx[col], sy[row], tw, th,
                        dx[col + 1] - dx[col], dy[row + 1] - dy[row], sourceWidth, sourceHeight);
                continue;
            }
            for (int py = dy[row]; py < dy[row + 1]; py += th)
                for (int px = dx[col]; px < dx[col + 1]; px += tw) {
                    int w = Math.min(tw, dx[col + 1] - px), h = Math.min(th, dy[row + 1] - py);
                    Gui.drawScaledCustomSizeModalRect(px, py, sx[col], sy[row], w, h, w, h, sourceWidth, sourceHeight);
                }
        }
    }

    public static void tooltip(GuiScreen screen, String message, int mouseX, int mouseY, GuiButton button) {
        tooltip(screen, message, mouseX, mouseY, button.y, button.height);
    }

    public static void tooltip(GuiScreen screen, String message, int mouseX, int mouseY,
            int widgetY, int widgetHeight) {
        if (message == null || message.isEmpty()) return;
        FontRenderer renderer = font();
        List<String> lines = renderer.listFormattedStringToWidth(message, Math.min(170, Math.max(1, screen.width - 24)));
        int textWidth = 0;
        for (String line : lines) textWidth = Math.max(textWidth, renderer.getStringWidth(line));
        int textHeight = lines.size() * 10 - 2;
        int x = mouseX + 12;
        if (x + textWidth > screen.width - 5) x = Math.max(mouseX - 12 - textWidth, 9);
        int y = mouseY + 3;
        if (widgetY + widgetHeight + 3 + widgetHeight - 3 + textHeight + 6 <= screen.height - 5)
            y += tooltipOffset(y, widgetY, widgetHeight);
        else y -= textHeight + 6 + tooltipOffset(y, widgetY + widgetHeight, widgetHeight);
        y = Math.max(4, Math.min(y, screen.height - textHeight - 4));
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST), lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST), blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D), depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int srcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB), dstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA), dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
        try {
            panel("tooltip_background", x - 12, y - 12, textWidth + 24, textHeight + 24, 100, 100, 9);
            panel("tooltip_frame", x - 12, y - 12, textWidth + 24, textHeight + 24, 100, 100, 10, true);
            for (String line : lines) {
                renderer.drawStringWithShadow(line, x, y, 0xFFFFFF);
                y += 10;
            }
        } finally {
            if (depth) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
            GlStateManager.depthMask(depthMask);
            if (lighting) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
            if (alpha) GlStateManager.enableAlpha(); else GlStateManager.disableAlpha();
            if (blend) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
            if (texture) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    private static int tooltipOffset(int mouseY, int widgetY, int height) {
        float distance = Math.min(Math.abs(mouseY - widgetY), height) / (float) Math.max(1, height);
        return Math.round(height - 3 + distance * (8 - height));
    }

    public static class Button extends GuiButton {
        private final String icon;
        private final int iconW;
        private final int iconH;

        public Button(int id, int x, int y, int width, String text) {
            this(id, x, y, width, text, null, 0, 0);
        }

        public Button(int id, int x, int y, int width, String text, String icon, int iconW, int iconH) {
            super(id, x, y, width, 20, text);
            this.icon = icon; this.iconW = iconW; this.iconH = iconH;
        }

        protected String backgroundKey(String state) { return state; }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!visible) return;
            hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
            String state = !enabled ? "button_disabled" : hovered ? "button_highlighted" : "button";
            panel(backgroundKey(state), x, y, width, height, 200, 20, enabled ? 3 : 1);
            if (icon == null) drawCenteredString(font(), displayString, x + width / 2,
                    y + 6, enabled ? 0xFFFFFF : 0xA0A0A0);
            else {
                String sprite = icon;
                if (hovered && enabled && (icon.equals("accept") || icon.equals("reject"))) sprite += "_highlighted";
                int sourceW = icon.equals("remove") ? 14 : icon.equals("cancel") ? 16 : iconW;
                int sourceH = icon.equals("remove") ? 13 : icon.equals("cancel") ? 16 : iconH;
                sprite(sprite, x + (width - iconW) / 2, y + (height - iconH) / 2, iconW, iconH, sourceW, sourceH);
                if (!enabled) Gui.drawRect(x, y, x + width, y + height, 0x66000000);
            }
        }
    }

    public static final class MenuButton extends Button {
        public MenuButton(int id, int x, int y, int width, String text, String icon, int iconW, int iconH) {
            super(id, x, y, width, text, icon, iconW, iconH);
        }

        @Override
        protected String backgroundKey(String state) { return "menu_" + state; }
    }

    public static final class Tab extends GuiButton {
        public boolean selected;

        public Tab(int id, int x, int y, int width, String text) { super(id, x, y, width, 20, text); }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!visible) return;
            hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
            panel(hovered ? "button_highlighted" : selected ? "button" : "button_disabled",
                    x, y, width, height, 200, 20, !hovered && !selected ? 1 : 3);
            drawCenteredString(font(), displayString, x + width / 2, y + (selected ? 6 : 7), 0xFFFFFF);
            if (selected) {
                int length = Math.min(width - 4, font().getStringWidth(displayString));
                Gui.drawRect(x + (width - length) / 2, y + height - 2,
                        x + (width + length) / 2, y + height - 1, 0xFFFFFFFF);
            }
        }
    }
}
