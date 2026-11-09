package xy177.farmersfuturedelight.client;

import java.io.IOException;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import org.lwjgl.input.Keyboard;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class GuiGameModeSwitcher extends GuiScreen {
    private static final ResourceLocation BACKGROUND = texture("gamemode_switcher");
    private static final ResourceLocation SLOT = texture("gamemode_slot");
    private static final ResourceLocation SELECTION = texture("gamemode_selection");
    private static final ResourceLocation ADVENTURE = texture("gamemode_adventure");
    private static final GameType[] MODES = {GameType.CREATIVE, GameType.SURVIVAL,
            GameType.ADVENTURE, GameType.SPECTATOR};
    private final EntityPlayerSP owner;
    private final GameType initialSelection;
    private GameType selected;
    private int firstMouseX;
    private int firstMouseY;
    private boolean mousePositionSet;

    public GuiGameModeSwitcher(GameType initialSelection) {
        this.initialSelection = initialSelection;
        selected = initialSelection;
        owner = net.minecraft.client.Minecraft.getMinecraft().player;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/gui/" + name + ".png");
    }

    @Override
    public void initGui() {
        selected = initialSelection;
        mousePositionSet = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(width / 2 - 62, height / 2 - 58,
                0, 0, 125, 75, 128, 128);
        drawCenteredString(fontRenderer, I18n.format("gameMode." + selected.getName()),
                width / 2, height / 2 - 51, 0xFFFFFF);
        drawCenteredString(fontRenderer, I18n.format(
                "debug.farmers_future_delight.gamemodes.select_next",
                TextFormatting.AQUA + "F4" + TextFormatting.RESET),
                width / 2, height / 2 + 5, 0xFFFFFF);
        if (!mousePositionSet) {
            firstMouseX = mouseX;
            firstMouseY = mouseY;
            mousePositionSet = true;
        }
        boolean mouseMoved = firstMouseX != mouseX || firstMouseY != mouseY;
        for (int index = 0; index < MODES.length; index++) {
            int slotX = width / 2 - 59 + index * 31;
            int slotY = height / 2 - 31;
            mc.getTextureManager().bindTexture(SLOT);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawModalRectWithCustomSizedTexture(slotX, slotY, 0, 0, 26, 26, 26, 26);
            if (selected == MODES[index]) {
                mc.getTextureManager().bindTexture(SELECTION);
                drawModalRectWithCustomSizedTexture(slotX, slotY, 0, 0, 26, 26, 26, 26);
            }
            if (MODES[index] == GameType.ADVENTURE) {
                mc.getTextureManager().bindTexture(ADVENTURE);
                drawModalRectWithCustomSizedTexture(slotX + 5, slotY + 5,
                        0, 0, 16, 16, 16, 16);
            } else {
                ItemStack icon = MODES[index] == GameType.CREATIVE ? new ItemStack(Blocks.GRASS)
                        : new ItemStack(MODES[index] == GameType.SURVIVAL
                                ? Items.IRON_SWORD : Items.ENDER_EYE);
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemAndEffectIntoGUI(icon, slotX + 5, slotY + 5);
                RenderHelper.disableStandardItemLighting();
            }
            if (mouseMoved && mouseX >= slotX && mouseX < slotX + 26
                    && mouseY >= slotY && mouseY < slotY + 26) selected = MODES[index];
        }
        GlStateManager.disableBlend();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_F4) {
            cycleSelection();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    void cycleSelection() {
        for (int index = 0; index < MODES.length; index++) {
            if (MODES[index] == selected) {
                selected = MODES[(index + 1) % MODES.length];
                mousePositionSet = false;
                return;
            }
        }
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        if (Keyboard.getEventKey() == Keyboard.KEY_F3 && !Keyboard.getEventKeyState()) {
            confirmSelection();
            return;
        }
        super.handleKeyboardInput();
    }

    void confirmSelection() {
        if (mc.player == owner && FFDGameModeSwitcher.canSwitch(mc)
                && selected != mc.playerController.getCurrentGameType()) {
            mc.player.sendChatMessage("/gamemode " + selected.getName());
        }
        mc.displayGuiScreen(null);
    }

    @Override
    public void updateScreen() {
        if (mc.player != owner || !FFDGameModeSwitcher.canSwitch(mc)) mc.displayGuiScreen(null);
    }

    @Override
    public void onGuiClosed() {
        FFDGameModeSwitcher.screenClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
