package xy177.farmersfuturedelight.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

final class GuiModernTextSelection {
    private GuiModernTextSelection() {
    }

    static void draw(int left, int top, int right, int bottom) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(775, 769, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        quad(left, top, right, bottom);
        GlStateManager.tryBlendFuncSeparate(1, 1, 1, 1);
        GlStateManager.color(0.0F, 0.0F, 1.0F, 1.0F);
        quad(left, top, right, bottom);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void quad(int left, int top, int right, int bottom) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION);
        buffer.pos(left, bottom, 0.0D).endVertex();
        buffer.pos(right, bottom, 0.0D).endVertex();
        buffer.pos(right, top, 0.0D).endVertex();
        buffer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
    }
}
