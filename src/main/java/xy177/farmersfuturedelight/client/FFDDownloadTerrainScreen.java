package xy177.farmersfuturedelight.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.core.FFDHeightHooks;
import xy177.farmersfuturedelight.core.FFDViewFrustumAccess;

public final class FFDDownloadTerrainScreen extends GuiDownloadTerrain {
    private static final long MAX_WAIT_MILLIS = 30000L;
    private static final String LOADING_GLYPHS = "\u52a0\u8f7d\u5730\u5f62\u4e2d\u2026";
    private static final ResourceLocation LOADING_GLYPH_TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/gui/loading/chinese_1_20_6.png");
    private final long createdAt = Minecraft.getSystemTime();
    private final int transition = FFDMenuTransitions.consume();
    private boolean positionReceived;
    private boolean closing;
    private FontRenderer latinFont;
    private final FFDLoadingPanoramaRenderer panoramaRenderer =
            new FFDLoadingPanoramaRenderer();

    @Override
    public void initGui() {
        super.initGui();
        latinFont = new FontRenderer(mc.gameSettings, new ResourceLocation(
                FarmerFutureDelight.MODID, "textures/gui/loading/ascii_1_20_6.png"),
                mc.getTextureManager(), false);
        latinFont.onResourceManagerReload(mc.getResourceManager());
        latinFont.setBidiFlag(fontRenderer.getBidiFlag());
    }

    boolean holdUntilChunksReady() {
        if (closing) {
            return false;
        }
        positionReceived = true;
        return true;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (closing) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if ((positionReceived && isPlayerSectionReady(minecraft))
                || Minecraft.getSystemTime() - createdAt >= MAX_WAIT_MILLIS) {
            closing = true;
            minecraft.displayGuiScreen(null);
        }
    }

    private static boolean isPlayerSectionReady(Minecraft minecraft) {
        if (minecraft.world == null || minecraft.player == null) {
            return false;
        }
        BlockPos position = minecraft.player.getPosition();
        if (FFDHeightHooks.isOutsideBuildHeight(minecraft.world, position)
                || minecraft.player.isSpectator() || !minecraft.player.isEntityAlive()) {
            return true;
        }
        if (minecraft.world.getChunkProvider().getLoadedChunk(
                position.getX() >> 4, position.getZ() >> 4) == null) {
            return false;
        }
        ViewFrustum frustum = ObfuscationReflectionHelper.getPrivateValue(
                RenderGlobal.class, minecraft.renderGlobal, "field_175008_n");
        if (!(frustum instanceof FFDViewFrustumAccess)) {
            return minecraft.renderGlobal.hasNoChunkUpdates();
        }
        RenderChunk chunk = ((FFDViewFrustumAccess) frustum).ffd$getRenderChunk(position);
        return chunk != null && chunk.getCompiledChunk() != CompiledChunk.DUMMY;
    }

    @Override
    public void onGuiClosed() {
        panoramaRenderer.close();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!FFDMenuTransitions.terrain(transition, width, height))
            panoramaRenderer.render(minecraft, width, height, minecraft.getFramebuffer());
        String text = I18n.format("ffd.loading.terrain");
        if (drawLoadingGlyphs(text, width / 2, height / 2 - 50)) {
            return;
        }
        FontRenderer font = !fontRenderer.getUnicodeFlag() && text.chars().allMatch(
                character -> character >= 32 && character <= 126) ? latinFont : fontRenderer;
        drawCenteredString(font, text,
                width / 2, height / 2 - 50, 0xFFFFFF);
    }

    private boolean drawLoadingGlyphs(String text, int centerX, int top) {
        int textWidth = 0;
        for (int index = 0; index < text.length(); index++) {
            int glyph = LOADING_GLYPHS.indexOf(text.charAt(index));
            if (glyph < 0) {
                return false;
            }
            textWidth += glyph == 5 ? 8 : 9;
        }
        mc.getTextureManager().bindTexture(LOADING_GLYPH_TEXTURE);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        for (int pass = 0; pass < 2; pass++) {
            float cursor = centerX - textWidth / 2;
            int color = pass == 0 ? 63 : 255;
            for (int index = 0; index < text.length(); index++) {
                int glyph = LOADING_GLYPHS.indexOf(text.charAt(index));
                float shadow = pass == 0 ? (glyph == 5 ? 1.0F : 0.5F) : 0.0F;
                float left = cursor + shadow;
                float upper = top + shadow;
                float startU = glyph / 6.0F;
                float endU = (glyph + 1) / 6.0F;
                buffer.pos(left, upper + 8, 0).tex(startU, 1).color(color, color, color, 255).endVertex();
                buffer.pos(left + 8, upper + 8, 0).tex(endU, 1).color(color, color, color, 255).endVertex();
                buffer.pos(left + 8, upper, 0).tex(endU, 0).color(color, color, color, 255).endVertex();
                buffer.pos(left, upper, 0).tex(startU, 0).color(color, color, color, 255).endVertex();
                cursor += glyph == 5 ? 8 : 9;
            }
        }
        tessellator.draw();
        GlStateManager.disableBlend();
        return true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
