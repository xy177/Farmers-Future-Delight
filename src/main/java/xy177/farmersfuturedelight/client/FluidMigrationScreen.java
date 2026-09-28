package xy177.farmersfuturedelight.client;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import xy177.farmersfuturedelight.common.network.FFDNetwork;

public final class FluidMigrationScreen extends GuiScreen {
    private final String token, host, fluid;
    private final boolean convertible;
    private boolean confirmingDiscard;
    private List<String> lines;

    public FluidMigrationScreen(String token, String host, String fluid, boolean convertible) {
        this.token = token; this.host = host; this.fluid = fluid; this.convertible = convertible;
    }
    @Override public void initGui() {
        lines = fontRenderer.listFormattedStringToWidth(
                I18n.format("ffd.migration.explain", host, fluid)
                        + "\n" + I18n.format(convertible ? "ffd.migration.config_hint" : "ffd.migration.missing"),
                Math.min(width - 40, 560));
        buttonList.clear();
        int buttonWidth = Math.min(150, (width - 30) / 2);
        GuiButton convert = new GuiButton(0, width / 2 - buttonWidth - 5, height - 76, buttonWidth, 20,
                I18n.format("ffd.migration.convert"));
        convert.enabled = convertible;
        buttonList.add(convert);
        buttonList.add(new GuiButton(1, width / 2 + 5, height - 76, buttonWidth, 20,
                I18n.format("ffd.migration.discard")));
        buttonList.add(new GuiButton(2, width / 2 - 100, height - 48,
                I18n.format("ffd.migration.later")));
    }
    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 2) {
            mc.displayGuiScreen(null);
        } else if (button.id == 1 && !confirmingDiscard) {
            confirmingDiscard = true;
            button.displayString = I18n.format("ffd.migration.confirm_discard");
        } else {
            FFDNetwork.answerMigration(token, button.id == 0);
            mc.displayGuiScreen(null);
        }
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("ffd.migration.title"), width / 2, 24, 0xffffff);
        int lineHeight = fontRenderer.FONT_HEIGHT + 3;
        float scale = Math.min(1.0F, Math.max(1, height - 126) / (float) (lines.size() * lineHeight));
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(width / 2.0F, 46, 0);
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1);
        int y = 0;
        for (String line : lines) {
            fontRenderer.drawString(line, -Math.min(width - 40, 560) / 2, y, 0xffffff);
            y += lineHeight;
        }
        net.minecraft.client.renderer.GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    @Override public boolean doesGuiPauseGame() { return true; }
}
