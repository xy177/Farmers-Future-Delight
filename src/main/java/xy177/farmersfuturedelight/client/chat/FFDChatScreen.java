package xy177.farmersfuturedelight.client.chat;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public final class FFDChatScreen extends GuiChat {
    private FFDChatAutocomplete autocomplete;

    public FFDChatScreen(GuiChat source) {
        super(getInitialText(source));
    }

    @Override
    public void initGui() {
        super.initGui();
        autocomplete = new FFDChatAutocomplete(inputField);
        if (inputField.getText().startsWith("/")) {
            autocomplete.requestUpdate();
        }
    }

    @Override
    public void setCompletions(String... completions) {
        if (autocomplete != null) {
            autocomplete.setCompletions(completions);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (autocomplete != null) {
            autocomplete.render(mouseX, mouseY, fontRenderer);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int scroll = Mouse.getDWheel() / 120;
        if (scroll != 0 && autocomplete != null
                && !autocomplete.onScroll(mouseX, mouseY, -scroll)) {
            if (!isShiftKeyDown()) {
                scroll *= 7;
            }
            mc.ingameGUI.getChatGUI().scroll(scroll);
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (autocomplete != null && autocomplete.onClick(mouseX, mouseY)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (autocomplete != null) {
            autocomplete.requestUpdate();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (autocomplete != null && autocomplete.onKeyPress(keyCode)) {
            return;
        }
        if (keyCode == 1) {
            mc.displayGuiScreen((GuiScreen) null);
            return;
        }
        if (keyCode == 28 || keyCode == 156) {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                sendChatMessage(text);
            }
            mc.displayGuiScreen(null);
            return;
        }
        if (keyCode == Keyboard.KEY_UP) {
            getSentHistory(-1);
            autocomplete.requestUpdate();
        } else if (keyCode == Keyboard.KEY_DOWN) {
            getSentHistory(1);
            autocomplete.requestUpdate();
        } else if (keyCode == Keyboard.KEY_PRIOR) {
            mc.ingameGUI.getChatGUI().scroll(mc.ingameGUI.getChatGUI().getLineCount() - 1);
        } else if (keyCode == Keyboard.KEY_NEXT) {
            mc.ingameGUI.getChatGUI().scroll(-mc.ingameGUI.getChatGUI().getLineCount() + 1);
        } else {
            inputField.textboxKeyTyped(typedChar, keyCode);
            if (autocomplete != null) {
                autocomplete.requestUpdate();
            }
        }
    }

    private static String getInitialText(GuiChat source) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen == null && minecraft.gameSettings.keyBindCommand.isKeyDown()) {
            return "/";
        }
        String text = ReflectionHelper.getPrivateValue(GuiChat.class, source,
                "defaultInputFieldText", "field_146410_g");
        return text == null ? "" : text;
    }
}
