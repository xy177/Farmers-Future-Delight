package xy177.farmersfuturedelight.client;

import net.minecraft.block.BlockWallSign;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDSignText;
import xy177.farmersfuturedelight.common.block.IBlockWoodSign;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.network.PacketSignText;

public final class GuiSignEditor extends GuiScreen {
    private static final float TEXT_SCALE = 0.9765628F;
    private final TileEntitySign sign;
    private final boolean back;
    private final String[] text = new String[4];
    private final TextFormatting[] colors = new TextFormatting[4];
    private final GuiTextField[] fields = new GuiTextField[4];
    private int line;
    private int updateCount;
    private boolean sent;

    public GuiSignEditor(TileEntitySign sign, boolean back) {
        this.sign = sign;
        this.back = back;
        ITextComponent[] components = back ? FFDSignText.getBackText(sign) : sign.signText;
        for (int index = 0; index < text.length; index++) {
            ITextComponent component = components[index];
            text[index] = component == null ? "" : component.getUnformattedText();
            colors[index] = component == null ? null : component.getStyle().getColor();
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        buttonList.add(new GuiModernButton(0, width / 2 - 100, height / 4 + 144,
                200, I18n.format("gui.done")));
        for (int index = 0; index < fields.length; index++) {
            GuiTextField field = new GuiTextField(index, fontRenderer, 0, 0, 90, 10);
            field.setMaxStringLength(90);
            field.setValidator(value -> fontRenderer.getStringWidth(value) <= 90);
            field.setText(text[index]);
            field.setFocused(index == line);
            fields[index] = field;
        }
    }

    @Override
    public void updateScreen() {
        updateCount++;
        if (mc.player == null || sign.isInvalid()
                || mc.player.getDistanceSq(sign.getPos()) > 64.0D) mc.displayGuiScreen(null);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) mc.displayGuiScreen(null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
        } else if (keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_DOWN
                || keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            fields[line].setFocused(false);
            line = (line + (keyCode == Keyboard.KEY_UP ? -1 : 1)) & 3;
            fields[line].setFocused(true);
            fields[line].setCursorPositionEnd();
            updateCount = 0;
        } else {
            fields[line].textboxKeyTyped(typedChar, keyCode);
            text[line] = fields[line].getText();
            updateCount = 0;
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        if (!sent) {
            sent = true;
            FFDNetwork.sendSignText(new PacketSignText(sign.getPos(), back, text));
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("gui.farmers_future_delight.sign.edit"), width / 2, 40, 0xFFFFFF);
        String wood = sign.getBlockType() instanceof IBlockWoodSign
                ? ((IBlockWoodSign) sign.getBlockType()).getWoodName() : "oak";
        mc.getTextureManager().bindTexture(new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/gui/signs/" + wood + ".png"));
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2.0F, 117.0F, 0.0F);
        GlStateManager.scale(3.9F, 3.9F, 1.0F);
        drawModalRectWithCustomSizedTexture(-12, -13, 0, 0, 24,
                sign.getBlockType() instanceof BlockWallSign ? 12 : 26, 24, 26);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2.0F, 90.0F, 0.0F);
        GlStateManager.scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        for (int index = 0; index < text.length; index++) {
            int left = -fontRenderer.getStringWidth(text[index]) / 2;
            int top = index * 10 - 20;
            int color = colors[index] == null ? 0
                    : fontRenderer.getColorCode(colors[index].toString().charAt(1));
            if (!FFDSignText.isGlowing(sign, back)) {
                color = (int) ((color >> 16 & 255) * 0.4F) << 16
                        | (int) ((color >> 8 & 255) * 0.4F) << 8
                        | (int) ((color & 255) * 0.4F);
            }
            fontRenderer.drawString(text[index], left, top, color);
            if (index != line) continue;
            int cursor = fields[index].getCursorPosition();
            int selection = fields[index].getSelectionEnd();
            int cursorX = left + fontRenderer.getStringWidth(text[index].substring(0, cursor));
            if (updateCount / 6 % 2 == 0) {
                if (cursor < text[index].length()) drawRect(cursorX, top,
                        cursorX + 1, top + 10, 0xFF000000 | color);
                else fontRenderer.drawString("_", cursorX, top, color);
            }
            if (cursor != selection) {
                int selectionX = left + fontRenderer.getStringWidth(text[index].substring(0, selection));
                GuiModernTextSelection.draw(Math.min(cursorX, selectionX), top,
                        Math.max(cursorX, selectionX), top + 10);
            }
        }
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
