package xy177.farmersfuturedelight.client;

import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.math.MathHelper;

public final class FFDLoadingScreenRenderer extends LoadingScreenRenderer {
    private final Minecraft minecraft;
    private final Framebuffer framebuffer;
    private final FFDLoadingPanoramaRenderer panoramaRenderer =
            new FFDLoadingPanoramaRenderer();
    private long lastDrawTime;

    public FFDLoadingScreenRenderer(Minecraft minecraft) {
        super(minecraft);
        this.minecraft = minecraft;
        this.framebuffer = new Framebuffer(minecraft.displayWidth, minecraft.displayHeight, false);
        this.framebuffer.setFramebufferFilter(9728);
    }

    @Override
    public void resetProgressAndMessage(String message) {
        setLoadingProgress(-1);
    }

    @Override
    public void displaySavingString(String message) {
        setLoadingProgress(-1);
    }

    @Override
    public void displayLoadingString(String message) {
        lastDrawTime = 0L;
        setLoadingProgress(-1);
    }

    @Override
    public void setLoadingProgress(int suppliedProgress) {
        long now = Minecraft.getSystemTime();
        if (now - lastDrawTime < 100L) {
            return;
        }
        lastDrawTime = now;

        int progress = suppliedProgress;
        IntegratedServer server = minecraft.getIntegratedServer();
        if (server != null && server.currentTask != null) {
            progress = server.percentDone;
        }
        render(progress);
    }

    private void render(int progress) {
        ScaledResolution resolution = new ScaledResolution(minecraft);
        int scale = resolution.getScaleFactor();
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();

        if (framebuffer.framebufferWidth != minecraft.displayWidth
                || framebuffer.framebufferHeight != minecraft.displayHeight) {
            framebuffer.createBindFramebuffer(minecraft.displayWidth, minecraft.displayHeight);
        }
        if (OpenGlHelper.isFramebufferEnabled()) {
            framebuffer.framebufferClear();
        } else {
            GlStateManager.clear(256);
        }
        framebuffer.bindFramebuffer(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, resolution.getScaledWidth_double(),
                resolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -200.0F);
        if (!OpenGlHelper.isFramebufferEnabled()) {
            GlStateManager.clear(16640);
        }

        panoramaRenderer.render(minecraft, width, height, framebuffer);

        int centerX = width / 2;
        int gridCenterY = height / 2;
        GlStateManager.disableTexture2D();
        FFDLoadingProgress.drawServerProgressGrid(centerX, gridCenterY,
                Math.max(0, progress));
        GlStateManager.enableTexture2D();

        String percent = MathHelper.clamp(progress, 0, 100) + "%";
        int percentY = gridCenterY - FFDLoadingProgress.GRID_DIAMETER
                - minecraft.fontRenderer.FONT_HEIGHT - 2;
        minecraft.fontRenderer.drawStringWithShadow(percent,
                (width - minecraft.fontRenderer.getStringWidth(percent)) / 2.0F,
                percentY, 0xFFFFFF);

        framebuffer.unbindFramebuffer();
        if (OpenGlHelper.isFramebufferEnabled()) {
            framebuffer.framebufferRender(width * scale, height * scale);
        }
        minecraft.updateDisplay();
        Thread.yield();
    }

    @Override
    public void setDoneWorking() {
    }
}
