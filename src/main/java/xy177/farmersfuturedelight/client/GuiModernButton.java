package xy177.farmersfuturedelight.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class GuiModernButton extends GuiButton {
    public GuiModernButton(int id, int left, int top, int width, String label) {
        super(id, left, top, width, 20, label);
    }

    @Override
    public void drawButton(Minecraft client, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        String suffix = !enabled ? "_disabled" : hovered ? "_highlighted" : "";
        client.getTextureManager().bindTexture(new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/gui/sprites/widget/button" + suffix + ".png"));
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        int half = width / 2;
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, half, height, 200, 20);
        drawModalRectWithCustomSizedTexture(x + half, y, 200 - width + half, 0,
                width - half, height, 200, 20);
        drawCenteredString(client.fontRenderer, displayString, x + width / 2,
                y + (height - 8) / 2, enabled ? 0xFFFFFF : 0xA0A0A0);
        mouseDragged(client, mouseX, mouseY);
    }
}
