package xy177.farmersfuturedelight.client;

import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDModernMenus {
    private static final FFDLoadingPanoramaRenderer RENDERER = new FFDLoadingPanoramaRenderer();
    private static final Set<String> EXCLUDED = new HashSet<>(Arrays.asList("GuiChat", "GuiSleepMP",
            "GuiScreenBook", "GuiEditSign", "GuiCommandBlock", "GuiCommandBlockMinecart", "GuiWinGame",
            "GuiDownloadTerrain", "GuiScreenWorking", "GuiMemoryError", "GuiGameOver", "GuiScreenDemo"));
    private static final Field LEFT = field("field_148152_e");
    private static final Field RIGHT = field("field_148151_d");
    private static final Field TOP = field("field_148153_b");
    private static final Field BOTTOM = field("field_148154_c");
    private static final Field VIDEO_OPTIONS = optionalField(GuiVideoSettings.class, "field_146501_h");
    private static final Field OPTION_ROWS = optionalField(GuiOptionsRowList.class, "field_148184_k");
    private static final IntBuffer SCISSOR = BufferUtils.createIntBuffer(16);
    private static boolean scissoring;
    private static boolean previousScissor;
    private static boolean rendered;
    private static boolean installed;

    private FFDModernMenus() { }

    public static void install() {
        if (installed) return;
        installed = true;
        MinecraftForge.EVENT_BUS.register(new FFDModernMenus());
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(new FFDMenuPanoramas());
    }

    public static void release() {
        RENDERER.close();
        FFDMenuTransitions.release();
        rendered = false;
    }

    public static boolean supported(GuiScreen screen) {
        if (screen == null || screen instanceof net.minecraft.client.gui.inventory.GuiContainer) return false;
        String name = screen.getClass().getName();
        return name.startsWith("net.minecraft.client.gui.") && !EXCLUDED.contains(screen.getClass().getSimpleName())
                || name.equals("xy177.farmersfuturedelight.client.gui.FFDGuiAccessibility")
                || name.equals("xy177.farmersfuturedelight.client.gui.FFDGuiGameRules")
                || name.equals("xy177.farmersfuturedelight.client.gui.FFDGuiFriendsSettings");
    }

    public static boolean background(GuiScreen screen) {
        if (FFDConfig.modernMenuVisuals && screen.getClass() == GuiDownloadTerrain.class)
            return FFDMenuTransitions.vanillaTerrain(screen);
        if (!FFDConfig.modernMenuVisuals || !supported(screen)) return false;
        if (!rendered) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.world == null) RENDERER.render(mc, screen.width, screen.height, mc.getFramebuffer(), false);
            else RENDERER.renderWorld(mc, screen.width, screen.height, mc.getFramebuffer());
            int top = FFDConfig.modernCreateWorldScreen && screen.getClass() == GuiCreateWorld.class ? 24 : 0;
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, top, 0);
            tile("menu_background", 0, 0, screen.width, screen.height - top, 32, 32);
            GlStateManager.popMatrix();
            rendered = true;
        }
        return true;
    }

    public static boolean titleBackground(GuiScreen screen) {
        if (FFDConfig.mainMenuBackgroundBlur && !FFDConfig.modernMenuVisuals && !FFDMenuPanoramas.custom()) return false;
        Minecraft mc = Minecraft.getMinecraft();
        RENDERER.render(mc, screen.width, screen.height, mc.getFramebuffer(), false, FFDConfig.mainMenuBackgroundBlur);
        return true;
    }

    public static void blurOverlay(GuiScreen screen) {
        Minecraft mc = Minecraft.getMinecraft();
        RENDERER.renderWorld(mc, screen.width, screen.height, mc.getFramebuffer());
    }

    private static boolean modernList() {
        return FFDConfig.modernMenuVisuals && supported(Minecraft.getMinecraft().currentScreen);
    }

    public static boolean listBackground(GuiSlot list) {
        if (!modernList()) return false;
        background(Minecraft.getMinecraft().currentScreen);
        int left = value(LEFT, list), right = value(RIGHT, list);
        int top = value(TOP, list), bottom = value(BOTTOM, list);
        tile("menu_list_background", left, top, right, bottom, 32, 32);
        if (!scissoring) {
            previousScissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            SCISSOR.clear();
            GL11.glGetInteger(GL11.GL_SCISSOR_BOX, SCISSOR);
            ScaledResolution size = new ScaledResolution(Minecraft.getMinecraft());
            int scale = size.getScaleFactor();
            int x = left * scale, y = Minecraft.getMinecraft().displayHeight - bottom * scale;
            int w = (right - left) * scale, h = (bottom - top) * scale;
            if (previousScissor) {
                int endX = Math.min(x + w, SCISSOR.get(0) + SCISSOR.get(2));
                int endY = Math.min(y + h, SCISSOR.get(1) + SCISSOR.get(3));
                x = Math.max(x, SCISSOR.get(0));
                y = Math.max(y, SCISSOR.get(1));
                w = Math.max(0, endX - x);
                h = Math.max(0, endY - y);
            }
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(x, y, Math.max(0, w), Math.max(0, h));
            scissoring = true;
        }
        return true;
    }

    public static boolean listOverlay(GuiSlot list, int start, int end, int startAlpha, int endAlpha) {
        if (!modernList()) return false;
        restoreScissor();
        int left = value(LEFT, list), right = value(RIGHT, list);
        int border = start == 0 ? end - 2 : start;
        tile(start == 0 ? "header_separator" : "footer_separator", left, border, right, border + 2, 32, 2);
        return true;
    }

    public static void listFade(Tessellator tessellator) {
        if (modernList()) {
            tessellator.getBuffer().finishDrawing();
            tessellator.getBuffer().reset();
        } else tessellator.draw();
    }

    public static void tile(String texture, int left, int top, int right, int bottom, int tileWidth, int tileHeight) {
        Minecraft mc = Minecraft.getMinecraft();
        String prefix = mc.world != null && !texture.startsWith("tab_") ? "inworld_" : "";
        mc.getTextureManager().bindTexture(new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/gui/" + prefix + texture + ".png"));
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1, 1, 1, 1);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_TEX);
        b.pos(left, bottom, 0).tex((double) left / tileWidth, (double) bottom / tileHeight).endVertex();
        b.pos(right, bottom, 0).tex((double) right / tileWidth, (double) bottom / tileHeight).endVertex();
        b.pos(right, top, 0).tex((double) right / tileWidth, (double) top / tileHeight).endVertex();
        b.pos(left, top, 0).tex((double) left / tileWidth, (double) top / tileHeight).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();
    }

    private static void restoreScissor() {
        if (!scissoring) return;
        GL11.glScissor(SCISSOR.get(0), SCISSOR.get(1), SCISSOR.get(2), SCISSOR.get(3));
        if (!previousScissor) GL11.glDisable(GL11.GL_SCISSOR_TEST);
        scissoring = false;
    }

    private static Field field(String name) {
        return ObfuscationReflectionHelper.findField(GuiSlot.class, name);
    }

    private static Field optionalField(Class<?> type, String name) {
        try { return ObfuscationReflectionHelper.findField(type, name); }
        catch (ReflectionHelper.UnableToFindFieldException ex) { return null; }
    }

    private static int value(Field field, GuiSlot list) {
        try { return field.getInt(list); }
        catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
    }

    public static GuiSlider blurSlider(int id, int x, int y, int width) {
        return new GuiSlider(id, x, y, width, 20, I18n.format("ffd.options.menu_blur") + ": ", "",
                0, 10, FFDClientOptions.menuBackgroundBlur(), false, true,
                slider -> FFDClientOptions.setMenuBackgroundBlur(slider.getValueInt()));
    }

    public static void drawTitle(GuiScreen screen, FontRenderer font, String text, int x, int y, int color) {
        if (FFDConfig.modernMenuVisuals && supported(screen) && !(screen instanceof GuiMainMenu) && y <= 20)
            y = 12;
        screen.drawCenteredString(font, text, x, y, color);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void layout(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen screen = event.getGui();
        if (!FFDConfig.modernMenuVisuals || !supported(screen) || screen instanceof GuiMainMenu
                || screen instanceof GuiCreateWorld) return;
        for (GuiButton button : event.getButtonList())
            if (button.getClass().getName().startsWith("net.minecraft.") && button.y == screen.height - 27)
                button.y = screen.height - 28;
        for (Class<?> type = screen.getClass(); type != GuiScreen.class && type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!GuiSlot.class.isAssignableFrom(field.getType())) continue;
                try {
                    field.setAccessible(true);
                    GuiSlot slot = (GuiSlot) field.get(screen);
                    if (slot != null && value(TOP, slot) == 32 && value(BOTTOM, slot) == screen.height - 32) {
                        TOP.setInt(slot, 33);
                        BOTTOM.setInt(slot, screen.height - 33);
                    }
                } catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void beforeDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        restoreScissor();
        rendered = false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        restoreScissor();
        rendered = false;
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void videoOptions(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!FFDConfig.modernMenuVisuals || !(event.getGui() instanceof GuiVideoSettings)
                || VIDEO_OPTIONS == null || OPTION_ROWS == null) return;
        GuiScreen screen = event.getGui();
        try {
            Object list = VIDEO_OPTIONS.get(screen);
            if (!(list instanceof GuiOptionsRowList)) return;
            Object options = OPTION_ROWS.get(list);
            if (!(options instanceof java.util.List)) return;
            java.util.List<GuiOptionsRowList.Row> rows = (java.util.List<GuiOptionsRowList.Row>) options;
            rows.add(new GuiOptionsRowList.Row(blurSlider(19731, screen.width / 2 - 155, 0, 310), null));
        } catch (IllegalAccessException ex) { return; }
    }
}
