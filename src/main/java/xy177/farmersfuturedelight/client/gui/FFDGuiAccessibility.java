package xy177.farmersfuturedelight.client.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Keyboard;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.FFDClientOptions;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public class FFDGuiAccessibility extends GuiScreen {
    private static final int OPTIONS_BUTTON_ID = 19001;
    private static final int NARRATOR_BUTTON_ID = 0;
    private static final int TEXT_BACKGROUND_BUTTON_ID = 1;
    private static final int BACKGROUND_SCOPE_BUTTON_ID = 2;
    private static final int SNEAK_BUTTON_ID = 3;
    private static final int SPRINT_BUTTON_ID = 4;
    private static final int DONE_BUTTON_ID = 5;
    private final GuiScreen parent;
    private int focusedButton;

    public FFDGuiAccessibility(GuiScreen parent) {
        this.parent = parent;
    }

    @SubscribeEvent
    public static void addOptionsButton(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiOptions) {
            int[] position = findOptionsPosition(event.getGui(), event.getButtonList());
            if (position != null) {
                event.getButtonList().add(new GuiButton(OPTIONS_BUTTON_ID,
                        position[0], position[1], 150, 20,
                        I18n.translateToLocal("ffd.options.accessibility")));
            }
        }
    }

    private static int[] findOptionsPosition(GuiScreen screen, java.util.List<GuiButton> buttons) {
        int centerX = screen.width / 2;
        int[] columns = {centerX - 155, centerX + 5};
        int minimumY = screen.height / 6 + 12;
        int maximumY = screen.height - 24;
        for (int y = minimumY; y <= maximumY; y += 2) {
            for (int x : columns) {
                if (isFreePosition(buttons, x, y, 150, 20)) {
                    return new int[]{x, y};
                }
            }
        }
        int[] edgeColumns = {5, screen.width - 155};
        for (int y = 5; y <= maximumY; y += 2) {
            for (int x : edgeColumns) {
                if (isFreePosition(buttons, x, y, 150, 20)) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static boolean isFreePosition(java.util.List<GuiButton> buttons,
                                          int x, int y, int width, int height) {
        for (GuiButton button : buttons) {
            if (x < button.x + button.width && x + width > button.x
                    && y < button.y + button.height && y + height > button.y) {
                return false;
            }
        }
        return true;
    }

    @SubscribeEvent
    public static void openAccessibilityScreen(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.getGui() instanceof GuiOptions
                && event.getButton().id == OPTIONS_BUTTON_ID) {
            event.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(
                    new FFDGuiAccessibility(event.getGui()));
        }
    }

    @Override
    public void initGui() {
        int centerX = width / 2;
        int firstY = Math.max(34, height / 6);
        buttonList.clear();
        buttonList.add(new GuiButton(NARRATOR_BUTTON_ID, centerX - 100, firstY,
                200, 20, mc.gameSettings.getKeyBinding(GameSettings.Options.NARRATOR)));
        buttonList.add(new GuiSlider(TEXT_BACKGROUND_BUTTON_ID, centerX - 155, firstY + 26,
                310, 20, I18n.translateToLocal("ffd.options.text_background_opacity") + ": ",
                "%", 0.0D, 100.0D, FFDClientOptions.textBackgroundOpacity() * 100.0D,
                false, true, slider -> FFDClientOptions.setTextBackgroundOpacity(
                        (float) slider.getValue() / 100.0F)));
        buttonList.add(new GuiButton(BACKGROUND_SCOPE_BUTTON_ID, centerX - 155, firstY + 52,
                310, 20, backgroundScopeLabel()));
        buttonList.add(new GuiButton(SNEAK_BUTTON_ID, centerX - 155, firstY + 78,
                150, 20, inputLabel("key.sneak", FFDClientOptions.toggleSneak())));
        buttonList.add(new GuiButton(SPRINT_BUTTON_ID, centerX + 5, firstY + 78,
                150, 20, inputLabel("key.sprint", FFDClientOptions.toggleSprint())));
        if (xy177.farmersfuturedelight.common.FFDConfig.modernMenuVisuals)
            buttonList.add(xy177.farmersfuturedelight.client.FFDModernMenus.blurSlider(
                    6, centerX - 155, firstY + 104, 310));
        buttonList.add(new GuiButton(DONE_BUTTON_ID, centerX - 100, height - 28,
                I18n.translateToLocal("gui.done")));
        focusedButton = 0;
        narrateFocusedButton();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        if (button.id == NARRATOR_BUTTON_ID) {
            mc.gameSettings.setOptionValue(GameSettings.Options.NARRATOR, 1);
            mc.gameSettings.saveOptions();
            button.displayString = mc.gameSettings.getKeyBinding(GameSettings.Options.NARRATOR);
        } else if (button.id == BACKGROUND_SCOPE_BUTTON_ID) {
            FFDClientOptions.setBackgroundForChatOnly(!FFDClientOptions.backgroundForChatOnly());
            button.displayString = backgroundScopeLabel();
        } else if (button.id == SNEAK_BUTTON_ID) {
            FFDClientOptions.setToggleSneak(!FFDClientOptions.toggleSneak());
            button.displayString = inputLabel("key.sneak", FFDClientOptions.toggleSneak());
        } else if (button.id == SPRINT_BUTTON_ID) {
            FFDClientOptions.setToggleSprint(!FFDClientOptions.toggleSprint());
            button.displayString = inputLabel("key.sprint", FFDClientOptions.toggleSprint());
        } else if (button.id == DONE_BUTTON_ID) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_TAB && !buttonList.isEmpty()) {
            int step = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                    || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? -1 : 1;
            moveFocus(step);
            return;
        }
        if ((keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_DOWN)
                && !buttonList.isEmpty()) {
            moveFocus(keyCode == Keyboard.KEY_DOWN ? 1 : -1);
            return;
        }
        if ((keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER
                || keyCode == Keyboard.KEY_SPACE) && focusedButton < buttonList.size()) {
            actionPerformed(buttonList.get(focusedButton));
            return;
        }
        if (focusedButton < buttonList.size()
                && (buttonList.get(focusedButton).id == TEXT_BACKGROUND_BUTTON_ID
                || buttonList.get(focusedButton).id == 6)
                && (keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT)) {
            GuiSlider slider = (GuiSlider) buttonList.get(focusedButton);
            double amount = slider.id == 6 ? 0.1D : 0.05D;
            slider.sliderValue += keyCode == Keyboard.KEY_RIGHT ? amount : -amount;
            slider.updateSlider();
            narrateFocusedButton();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int previousFocus = focusedButton;
        for (int index = 0; index < buttonList.size(); index++) {
            GuiButton button = buttonList.get(index);
            if (button.visible && mouseX >= button.x && mouseX < button.x + button.width
                    && mouseY >= button.y && mouseY < button.y + button.height) {
                focusedButton = index;
                break;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (focusedButton != previousFocus) {
            narrateFocusedButton();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRenderer, I18n.translateToLocal("ffd.options.accessibility.title"),
                width / 2, 20, 0xFFFFFF);
        if (focusedButton < buttonList.size()) {
            GuiButton button = buttonList.get(focusedButton);
            int color = 0xFFFFFFFF;
            drawRect(button.x - 2, button.y - 2, button.x + button.width + 2, button.y,
                    color);
            drawRect(button.x - 2, button.y + button.height,
                    button.x + button.width + 2, button.y + button.height + 2, color);
            drawRect(button.x - 2, button.y, button.x, button.y + button.height, color);
            drawRect(button.x + button.width, button.y,
                    button.x + button.width + 2, button.y + button.height, color);
        }
    }

    private void narrateFocusedButton() {
        if (focusedButton < buttonList.size()) {
            NarratorChatListener.INSTANCE.say(ChatType.SYSTEM,
                    new TextComponentString(buttonList.get(focusedButton).displayString));
        }
    }

    private void moveFocus(int direction) {
        for (int attempt = 0; attempt < buttonList.size(); attempt++) {
            focusedButton = (focusedButton + direction + buttonList.size()) % buttonList.size();
            GuiButton button = buttonList.get(focusedButton);
            if (button.visible && button.enabled) {
                narrateFocusedButton();
                return;
            }
        }
    }

    private String backgroundScopeLabel() {
        return I18n.translateToLocal("ffd.options.text_background") + ": "
                + I18n.translateToLocal(FFDClientOptions.backgroundForChatOnly()
                        ? "ffd.options.text_background.chat" : "ffd.options.text_background.everywhere");
    }

    private String inputLabel(String key, boolean toggle) {
        return I18n.translateToLocal(key) + ": " + I18n.translateToLocal(toggle
                ? "ffd.options.key.toggle" : "ffd.options.key.hold");
    }

    @Override
    public void onGuiClosed() {
        FFDClientOptions.save();
        mc.gameSettings.saveOptions();
        super.onGuiClosed();
    }
}
