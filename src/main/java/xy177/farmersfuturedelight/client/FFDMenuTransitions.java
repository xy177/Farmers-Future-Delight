package xy177.farmersfuturedelight.client;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderManager;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDMenuTransitions {
    private static final ResourceLocation SKY = resource("textures/environment/end_sky.png");
    private static final ResourceLocation PORTAL = resource("textures/entity/end_portal/end_portal.png");
    private static final FFDLoadingPanoramaRenderer BACKGROUND = new FFDLoadingPanoramaRenderer();
    private static ShaderManager shader;
    private static boolean shaderUnavailable;
    private static int pending;
    private static final java.util.Map<net.minecraft.client.gui.GuiScreen, Integer> REASONS = new java.util.WeakHashMap<>();

    private FFDMenuTransitions() { }

    public static void changeDimension(SPacketRespawn packet) {
        Minecraft mc = Minecraft.getMinecraft();
        int target = packet.getDimensionID();
        if (mc.world == null || mc.player == null || !mc.player.isEntityAlive()
                || mc.world.provider.getDimension() == target) { pending = 0; return; }
        int source = mc.world.provider.getDimension();
        pending = source == -1 || target == -1 ? 1 : source == 1 || target == 1 ? 2 : 0;
    }

    static int consume() { int result = pending; pending = 0; return result; }

    static boolean vanillaTerrain(net.minecraft.client.gui.GuiScreen screen) {
        int reason = REASONS.computeIfAbsent(screen, key -> consume());
        if (!terrain(reason, screen.width, screen.height)) {
            Minecraft mc = Minecraft.getMinecraft();
            BACKGROUND.render(mc, screen.width, screen.height, mc.getFramebuffer(), false);
            FFDModernMenus.tile("menu_background", 0, 0, screen.width, screen.height, 32, 32);
        }
        return true;
    }

    public static void release() {
        BACKGROUND.close();
        if (shader != null) { shader.deleteShader(); shader = null; }
        shaderUnavailable = false;
    }

    public static boolean credits(GuiWinGame screen) {
        if (!FFDConfig.modernMenuVisuals) return false;
        boolean poem = ObfuscationReflectionHelper.getPrivateValue(GuiWinGame.class, screen, "field_193980_h");
        Minecraft mc = Minecraft.getMinecraft();
        if (poem) endPortal(screen.width, screen.height);
        else {
            if (mc.world == null) BACKGROUND.render(mc, screen.width, screen.height, mc.getFramebuffer(), false);
            else BACKGROUND.renderWorld(mc, screen.width, screen.height, mc.getFramebuffer());
            FFDModernMenus.tile("menu_background", 0, 0, screen.width, screen.height, 32, 32);
        }
        return true;
    }

    public static ResourceLocation vignette(ResourceLocation original) {
        return FFDConfig.modernMenuVisuals && original.toString().equals("minecraft:textures/misc/vignette.png")
                ? resource("textures/misc/credits_vignette.png") : original;
    }

    static boolean terrain(int reason, int width, int height) {
        if (!FFDConfig.modernMenuVisuals || reason == 0) return false;
        if (reason == 2) endPortal(width, height);
        else {
            Minecraft mc = Minecraft.getMinecraft();
            TextureAtlasSprite portal = mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/portal");
            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.disableLighting();
            quad(width, height, portal.getMinU(), portal.getMinV(), portal.getMaxU(), portal.getMaxV());
        }
        return true;
    }

    private static void endPortal(int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.color(1, 1, 1, 1);
        if (!shaderUnavailable && OpenGlHelper.areShadersSupported()) {
            try {
                if (shader == null) shader = new ShaderManager(mc.getResourceManager(), FarmerFutureDelight.MODID + ":menu_end_portal");
                mc.getTextureManager().bindTexture(SKY);
                shader.addSamplerTexture("Sky", mc.getTextureManager().getTexture(SKY).getGlTextureId());
                mc.getTextureManager().bindTexture(PORTAL);
                shader.addSamplerTexture("Portal", mc.getTextureManager().getTexture(PORTAL).getGlTextureId());
                shader.getShaderUniformOrDefault("Size").set((float) width, (float) height);
                shader.getShaderUniformOrDefault("GameTime").set((Minecraft.getSystemTime() % 1200000L) / 1200000.0F);
                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                boolean secondTexture = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                try {
                    shader.useShader();
                    quad(width, height, 0, 0, 1, 1);
                } finally {
                    shader.endShader();
                    GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                    if (!secondTexture) GlStateManager.disableTexture2D();
                    GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                }
                GlStateManager.enableAlpha();
                return;
            } catch (IOException | RuntimeException ex) {
                shaderUnavailable = true;
                if (shader != null) { shader.deleteShader(); shader = null; }
                LogManager.getLogger().warn("Unable to initialize end-portal menu shader", ex);
            }
        }
        mc.getTextureManager().bindTexture(SKY);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, 64, 64);
        GlStateManager.enableAlpha();
    }

    private static void quad(int width, int height, double u0, double v0, double u1, double v1) {
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_TEX);
        b.pos(0, height, 0).tex(u0, v1).endVertex();
        b.pos(width, height, 0).tex(u1, v1).endVertex();
        b.pos(width, 0, 0).tex(u1, v0).endVertex();
        b.pos(0, 0, 0).tex(u0, v0).endVertex();
        Tessellator.getInstance().draw();
    }

    private static ResourceLocation resource(String path) { return new ResourceLocation(FarmerFutureDelight.MODID, path); }
}
