package xy177.farmersfuturedelight.client.gui;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import xy177.farmersfuturedelight.client.friends.FFDFriendsClient;
import xy177.farmersfuturedelight.client.friends.FFDFriendsPreferences;
import xy177.farmersfuturedelight.client.friends.FFDFriendsWidgets;

public final class FFDGuiFriendsSettings extends GuiScreen {
    private final GuiScreen parent;
    private final FFDFriendsClient client = FFDFriendsClient.INSTANCE;
    private static final String ACCOUNT_SETTINGS = "https://aka.ms/MinecraftJavaXboxPrivacyAndSafety";

    public FFDGuiFriendsSettings(GuiScreen parent) { this.parent = parent; }

    private static String text(String key, Object... args) { return FFDFriendsWidgets.text(key, args); }

    public static void confirmEnabled(GuiScreen parent) {
        Minecraft mc = Minecraft.getMinecraft();
        java.util.UUID selected = FFDFriendsClient.INSTANCE.account();
        mc.displayGuiScreen(new GuiYesNo((accepted, id) -> {
            mc.displayGuiScreen(parent);
            if (accepted && selected != null && selected.equals(FFDFriendsClient.INSTANCE.account()))
                FFDFriendsClient.INSTANCE.settings(true, true);
        }, text("options.friendsList.confirm.title"), text("options.friendsList.confirm.message",
                text("options.friendsList.confirm.message.link")),
                text("options.friendsList.confirm.turnOn"), I18n.format("gui.cancel"), 0));
    }

    @Override
    public void initGui() {
        fontRenderer = FFDFriendsWidgets.font();
        buttonList.clear();
        int y = Math.max(36, height / 6);
        for (int i = 0; i < 4; i++) addButton(new FFDFriendsWidgets.Button(i,
                width / 2 - 155 + i % 2 * 160, y + i / 2 * 24, 150, ""));
        addButton(new FFDFriendsWidgets.Button(4, width / 2 - 155, y + 56, 310,
                I18n.format("ffd.friends.manage_account")));
        addButton(new FFDFriendsWidgets.Button(5, width / 2 - 155, y + 80, 310,
                I18n.format("ffd.friends.disconnect")));
        addButton(new FFDFriendsWidgets.Button(6, width / 2 - 100, height - 28, 200, I18n.format("gui.done")));
        updateScreen();
    }

    private String toggle(String key, boolean value) {
        return text(key) + ": " + I18n.format(value ? "options.on" : "options.off");
    }

    @Override
    public void updateScreen() {
        buttonList.get(0).displayString = toggle("options.friendsList", client.data().enabled);
        buttonList.get(1).displayString = toggle("options.allowFriendRequests", client.data().allowRequests);
        buttonList.get(2).displayString = toggle("options.inGameNotification", FFDFriendsPreferences.notifications());
        String visibility = new String[]{"none", "limited", "all"}[FFDFriendsPreferences.sharing()];
        buttonList.get(3).displayString = text("options.sharePresence") + ": " + text("options.sharePresence." + visibility);
        for (int i = 0; i < 4; i++) buttonList.get(i).enabled = client.canAct()
                && (i == 0 || client.data().enabled);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        switch (button.id) {
            case 0:
                if (client.data().enabled) client.settings(false, false);
                else confirmEnabled(this);
                break;
            case 1: client.settings(client.data().enabled, !client.data().allowRequests); break;
            case 2: FFDFriendsPreferences.notifications(!FFDFriendsPreferences.notifications()); break;
            case 3:
                FFDFriendsPreferences.sharing((FFDFriendsPreferences.sharing() + 1) % 3);
                client.presenceChanged();
                break;
            case 4:
                mc.displayGuiScreen(new GuiConfirmOpenLink((accepted, id) -> {
                    mc.displayGuiScreen(this);
                    if (accepted) try { java.awt.Desktop.getDesktop().browse(URI.create(ACCOUNT_SETTINGS)); }
                    catch (Exception ignored) { }
                }, ACCOUNT_SETTINGS, 0, true));
                break;
            case 5:
                client.disconnect();
                mc.displayGuiScreen(parent instanceof FFDGuiFriends ? ((FFDGuiFriends) parent).previous() : parent);
                break;
            case 6: mc.displayGuiScreen(parent); break;
            default: break;
        }
        if (mc.currentScreen == this) updateScreen();
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(c, key);
    }

    @Override
    public void drawScreen(int x, int y, float ticks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, text("gui.friends.open") + " - " + I18n.format("options.title"), width / 2, 16, 0xFFFFFF);
        xy177.farmersfuturedelight.client.friends.FFDFriendsService.Error error = client.error() != null
                ? client.error() : client.actionError();
        if (error != null) fontRenderer.drawSplitString(
                I18n.format("ffd.friends.error." + error.name().toLowerCase(Locale.ROOT)),
                width / 2 - 155, Math.max(36, height / 6) + 108, 310, 0xFF7777);
        super.drawScreen(x, y, ticks);
        for (GuiButton button : buttonList) {
            if (!button.visible || !button.isMouseOver()) continue;
            String key;
            if (button.id == 1) key = "options.allowFriendRequests.tooltip";
            else if (button.id == 2) key = "options.inGameNotification.tooltip";
            else if (button.id == 3) key = "options.sharePresence."
                    + new String[]{"none", "limited", "all"}[FFDFriendsPreferences.sharing()] + ".tooltip";
            else continue;
            FFDFriendsWidgets.tooltip(this, text(key), x, y, button);
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
