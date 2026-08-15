package xy177.farmersfuturedelight.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;

public final class FFDDownloadTerrainScreen extends GuiDownloadTerrain {
    private static final long MAX_WAIT_MILLIS = 8000L;
    private static final long CENTER_STABLE_MILLIS = 250L;
    private long readySince = -1L;
    private boolean closing;
    private final FFDLoadingPanoramaRenderer panoramaRenderer =
            new FFDLoadingPanoramaRenderer();

    boolean holdUntilChunksReady() {
        if (closing) {
            return false;
        }
        if (readySince < 0L) {
            readySince = Minecraft.getSystemTime();
        }
        return true;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (readySince < 0L || closing) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        FFDLoadingProgress.Snapshot snapshot = FFDLoadingProgress.snapshot(
                minecraft, FFDLoadingProgress.GRID_RADIUS);
        long elapsed = Minecraft.getSystemTime() - readySince;
        if ((snapshot.centerLoaded && elapsed >= CENTER_STABLE_MILLIS)
                || elapsed >= MAX_WAIT_MILLIS) {
            closing = true;
            minecraft.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        panoramaRenderer.render(minecraft, width, height, minecraft.getFramebuffer());
        FFDLoadingProgress.Snapshot snapshot = FFDLoadingProgress.snapshot(
                minecraft, FFDLoadingProgress.GRID_RADIUS);

        int centerX = width / 2;
        int gridCenterY = height / 2;
        FFDLoadingProgress.drawChunkGrid(centerX, gridCenterY, snapshot);
        String percent = Math.round(snapshot.progress() * 100.0F) + "%";
        int percentY = gridCenterY - FFDLoadingProgress.GRID_DIAMETER
                - fontRenderer.FONT_HEIGHT - 2;
        drawCenteredString(fontRenderer, percent, centerX, percentY, 0xFFFFFF);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
