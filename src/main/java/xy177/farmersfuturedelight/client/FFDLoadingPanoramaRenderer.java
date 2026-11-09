package xy177.farmersfuturedelight.client;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.glu.Project;
import org.lwjgl.opengl.GL11;
import xy177.farmersfuturedelight.FarmerFutureDelight;

final class FFDLoadingPanoramaRenderer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation PANORAMA_OVERLAY = id(
            "textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation BLUR_SHADER = id("shaders/post/loading_blur.json");
    private static final java.lang.reflect.Field SHADER_PASSES = net.minecraftforge.fml.common.ObfuscationReflectionHelper
            .findField(ShaderGroup.class, "field_148031_d");

    private ShaderGroup blurShader;
    private Framebuffer blurTarget;
    private int blurWidth = -1;
    private int blurHeight = -1;
    private boolean blurUnavailable;
    private long lastTime = -1L;
    private static float rotation;

    void render(Minecraft minecraft, int width, int height, Framebuffer target) {
        render(minecraft, width, height, target, true);
    }

    void render(Minecraft minecraft, int width, int height, Framebuffer target, boolean darken) {
        render(minecraft, width, height, target, darken, true);
    }

    void render(Minecraft minecraft, int width, int height, Framebuffer target, boolean darken, boolean blur) {
        advanceRotation();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.viewport(0, 0, target.framebufferWidth, target.framebufferHeight);
        drawCube(minecraft, target.framebufferWidth, target.framebufferHeight);
        setupGuiProjection(width, height);
        drawPanoramaOverlay(minecraft, width, height);
        if (blur) applyBlur(minecraft, target);
        setupGuiProjection(width, height);
        if (darken) Gui.drawRect(0, 0, width, height, 0x40000000);
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    void close() {
        if (blurShader != null) {
            blurShader.deleteShaderGroup();
            blurShader = null;
        }
        blurTarget = null;
        blurWidth = -1;
        blurHeight = -1;
        blurUnavailable = false;
    }

    void renderWorld(Minecraft minecraft, int width, int height, Framebuffer target) {
        applyBlur(minecraft, target);
        setupGuiProjection(width, height);
        GlStateManager.color(1, 1, 1, 1);
    }

    private void advanceRotation() {
        long now = Minecraft.getSystemTime();
        float delta = lastTime < 0L ? 0.0F : (now - lastTime) / 50.0F;
        lastTime = now;
        if (delta > 7.0F) {
            delta = 0.5F;
        }
        rotation += delta * 0.1F;
        if (rotation > 360.0F) {
            rotation -= 360.0F;
        }
    }

    private void drawCube(Minecraft minecraft, int framebufferWidth, int framebufferHeight) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(85.0F, framebufferWidth / (float) framebufferHeight,
                0.05F, 10.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        for (int sample = 0; sample < 4; sample++) {
            GlStateManager.pushMatrix();
            float offsetX = ((sample % 2) / 2.0F - 0.5F) / 256.0F;
            float offsetY = ((sample / 2) / 2.0F - 0.5F) / 256.0F;
            GlStateManager.translate(offsetX, offsetY, 0.0F);
            GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-rotation, 0.0F, 1.0F, 0.0F);
            int alpha = Math.round(255.0F) / (sample + 1);
            for (int face = 0; face < 6; face++) {
                minecraft.getTextureManager().bindTexture(FFDMenuPanoramas.face(face));
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                addFace(buffer, face, alpha);
                tessellator.draw();
            }
            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
    }

    private static void addFace(BufferBuilder buffer, int face, int alpha) {
        switch (face) {
            case 0:
                vertex(buffer, -1, -1, 1, 0, 0, alpha);
                vertex(buffer, -1, 1, 1, 0, 1, alpha);
                vertex(buffer, 1, 1, 1, 1, 1, alpha);
                vertex(buffer, 1, -1, 1, 1, 0, alpha);
                break;
            case 1:
                vertex(buffer, 1, -1, 1, 0, 0, alpha);
                vertex(buffer, 1, 1, 1, 0, 1, alpha);
                vertex(buffer, 1, 1, -1, 1, 1, alpha);
                vertex(buffer, 1, -1, -1, 1, 0, alpha);
                break;
            case 2:
                vertex(buffer, 1, -1, -1, 0, 0, alpha);
                vertex(buffer, 1, 1, -1, 0, 1, alpha);
                vertex(buffer, -1, 1, -1, 1, 1, alpha);
                vertex(buffer, -1, -1, -1, 1, 0, alpha);
                break;
            case 3:
                vertex(buffer, -1, -1, -1, 0, 0, alpha);
                vertex(buffer, -1, 1, -1, 0, 1, alpha);
                vertex(buffer, -1, 1, 1, 1, 1, alpha);
                vertex(buffer, -1, -1, 1, 1, 0, alpha);
                break;
            case 4:
                vertex(buffer, -1, -1, -1, 0, 0, alpha);
                vertex(buffer, -1, -1, 1, 0, 1, alpha);
                vertex(buffer, 1, -1, 1, 1, 1, alpha);
                vertex(buffer, 1, -1, -1, 1, 0, alpha);
                break;
            default:
                vertex(buffer, -1, 1, 1, 0, 0, alpha);
                vertex(buffer, -1, 1, -1, 0, 1, alpha);
                vertex(buffer, 1, 1, -1, 1, 1, alpha);
                vertex(buffer, 1, 1, 1, 1, 0, alpha);
                break;
        }
    }

    private static void vertex(BufferBuilder buffer, double x, double y, double z,
                               double u, double v, int alpha) {
        buffer.pos(x, y, z).tex(u, v).color(255, 255, 255, alpha).endVertex();
    }

    private void drawPanoramaOverlay(Minecraft minecraft, int width, int height) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(PANORAMA_OVERLAY);
        Gui.drawScaledCustomSizeModalRect(0, 0, 0.0F, 0.0F, 16, 128,
                width, height, 16.0F, 128.0F);
        GlStateManager.disableBlend();
    }

    private void applyBlur(Minecraft minecraft, Framebuffer target) {
        int radius = FFDClientOptions.menuBackgroundBlur();
        if (radius == 0) return;
        if (blurUnavailable || !OpenGlHelper.isFramebufferEnabled()
                || !OpenGlHelper.areShadersSupported()) {
            return;
        }
        int previousFilter = target.framebufferFilter;
        GlStateManager.bindTexture(target.framebufferTexture);
        int previousWrapS = GL11.glGetTexParameteri(3553, 10242);
        int previousWrapT = GL11.glGetTexParameteri(3553, 10243);
        GlStateManager.bindTexture(0);
        try {
            if (blurShader == null || blurTarget != target) {
                if (blurShader != null) {
                    blurShader.deleteShaderGroup();
                }
                blurTarget = target;
                blurShader = new ShaderGroup(minecraft.getTextureManager(),
                        minecraft.getResourceManager(), target, BLUR_SHADER);
                blurWidth = -1;
                blurHeight = -1;
            }
            if (blurWidth != target.framebufferWidth
                    || blurHeight != target.framebufferHeight) {
                blurWidth = target.framebufferWidth;
                blurHeight = target.framebufferHeight;
                blurShader.createBindFramebuffers(blurWidth, blurHeight);
                configureBlurFramebuffer(blurShader.getFramebufferRaw("swap"));
            }
            configureBlurFramebuffer(target);
            @SuppressWarnings("unchecked")
            java.util.List<net.minecraft.client.shader.Shader> passes = (java.util.List<net.minecraft.client.shader.Shader>)
                    SHADER_PASSES.get(blurShader);
            for (net.minecraft.client.shader.Shader shader : passes) {
                net.minecraft.client.shader.ShaderUniform uniform = shader.getShaderManager().getShaderUniform("Radius");
                if (uniform != null) uniform.set((float) radius);
            }
            blurShader.render(0.0F);
        } catch (IOException | IllegalAccessException | RuntimeException exception) {
            LOGGER.warn("Unable to initialize the modern loading-screen blur", exception);
            close();
            blurUnavailable = true;
        } finally {
            target.setFramebufferFilter(previousFilter);
            GlStateManager.bindTexture(target.framebufferTexture);
            GlStateManager.glTexParameteri(3553, 10242, previousWrapS);
            GlStateManager.glTexParameteri(3553, 10243, previousWrapT);
            GlStateManager.bindTexture(0);
            target.bindFramebuffer(false);
            GlStateManager.viewport(0, 0, target.framebufferWidth, target.framebufferHeight);
        }
    }

    private static void configureBlurFramebuffer(Framebuffer framebuffer) {
        framebuffer.setFramebufferFilter(9729);
        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        GlStateManager.glTexParameteri(3553, 10242, 33071);
        GlStateManager.glTexParameteri(3553, 10243, 33071);
        GlStateManager.bindTexture(0);
    }

    private static void setupGuiProjection(int width, int height) {
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, path);
    }
}
