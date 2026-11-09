package xy177.farmersfuturedelight.client.friends;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonLanguage;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import xy177.farmersfuturedelight.client.gui.FFDGuiAccessibility;
import xy177.farmersfuturedelight.client.gui.FFDGuiFriendsSettings;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDFriendsMenu {
    private static final int FRIENDS = 19771;
    private static final int ACCESSIBILITY = 19772;
    private static final int SETTINGS = 19773;
    private static final KeyBinding OPEN = new KeyBinding("key.ffd.friends", Keyboard.KEY_O, "key.categories.misc");
    private static boolean installed;

    private FFDFriendsMenu() { }

    public static void install() {
        if (installed) return;
        installed = true;
        FFDFriendsClient.INSTANCE.install();
        ClientRegistry.registerKeyBinding(OPEN);
        MinecraftForge.EVENT_BUS.register(new FFDFriendsMenu());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void layout(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen screen = event.getGui();
        List<GuiButton> buttons = event.getButtonList();
        if (screen.getClass() == GuiOptions.class) {
            GuiButton done = null;
            for (GuiButton button : buttons) if (button.id == 200) done = button;
            if (done == null) return;
            int x = done.x + done.width + 4, y = done.y;
            if (x + 20 > screen.width) return;
            for (GuiButton button : buttons) if (button.visible && overlap(button, x, y, 20, 20)) return;
            buttons.add(new FFDFriendsWidgets.MenuButton(SETTINGS, x, y, 20,
                    I18n.format("ffd.friends.settings"), "friends", 16, 16));
            return;
        }
        if (!FFDConfig.modernMenuVisuals) return;
        if (screen.getClass() == GuiMainMenu.class) {
            GuiButton language = null, options = null, quit = null;
            for (GuiButton button : buttons) {
                if (button.id == 5 && button instanceof GuiButtonLanguage) language = button;
                if (button.id == 0 && button.getClass() == GuiButton.class) options = button;
                if (button.id == 4 && button.getClass() == GuiButton.class) quit = button;
            }
            if (language == null || options == null || quit == null || options.y != quit.y) return;
            int x = screen.width / 2 - 34, y = screen.height / 4 + (Minecraft.getMinecraft().isDemo() ? 96 : 120);
            for (GuiButton button : buttons) {
                if (button == language || button == options || button == quit || !button.visible) continue;
                if (overlap(button, x, y, 68, 20)
                        || overlap(button, options.x, y + 24, options.width, options.height)
                        || overlap(button, quit.x, y + 24, quit.width, quit.height)) return;
            }
            options.y = y + 24;
            quit.y = y + 24;
            buttons.remove(language);
            buttons.add(new FFDFriendsWidgets.MenuButton(FRIENDS, x, y, 20,
                    FFDFriendsWidgets.text("gui.friends.open"), "friends", 16, 16));
            buttons.add(new FFDFriendsWidgets.MenuButton(5, x + 24, y, 20,
                    I18n.format("options.language"), "language", 16, 16));
            buttons.add(new FFDFriendsWidgets.MenuButton(ACCESSIBILITY, x + 48, y, 20,
                    I18n.format("ffd.options.accessibility"), "accessibility", 16, 16));
        } else if (screen.getClass() == GuiIngameMenu.class) {
            int x = 8, y = screen.height - 32;
            for (GuiButton button : buttons) if (button.visible && overlap(button, x, y, 20, 20)) return;
            buttons.add(new FFDFriendsWidgets.MenuButton(FRIENDS, x, y, 20,
                    FFDFriendsWidgets.text("gui.friends.open"), "friends", 16, 16));
        }
    }

    private boolean overlap(GuiButton button, int x, int y, int width, int height) {
        return x < button.x + button.width && x + width > button.x
                && y < button.y + button.height && y + height > button.y;
    }

    @SubscribeEvent
    public void action(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (!(event.getGui() instanceof GuiMainMenu) && !(event.getGui() instanceof GuiIngameMenu)
                && !(event.getGui() instanceof GuiOptions)) return;
        if (!(event.getButton() instanceof FFDFriendsWidgets.Button)) return;
        Minecraft mc = Minecraft.getMinecraft();
        GuiButton button = event.getButton();
        if (button.id == SETTINGS) {
            event.setCanceled(true);
            mc.displayGuiScreen(new FFDGuiFriendsSettings(event.getGui()));
        } else if (button.id == FRIENDS) {
            event.setCanceled(true);
            FFDFriendsClient.INSTANCE.open(event.getGui());
        } else if (button.id == ACCESSIBILITY) {
            event.setCanceled(true);
            mc.displayGuiScreen(new FFDGuiAccessibility(event.getGui()));
        } else if (button.id == 5) {
            event.setCanceled(true);
            mc.displayGuiScreen(new GuiLanguage(event.getGui(), mc.gameSettings, mc.getLanguageManager()));
        }
    }

    @SubscribeEvent
    public void key(InputEvent.KeyInputEvent event) {
        if (OPEN.isPressed() && Minecraft.getMinecraft().currentScreen == null)
            FFDFriendsClient.INSTANCE.open(null);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void hover(GuiScreenEvent.DrawScreenEvent.Post event) {
        GuiScreen screen = event.getGui();
        if (!(screen instanceof GuiMainMenu) && !(screen instanceof GuiIngameMenu) && !(screen instanceof GuiOptions)) return;
        List<GuiButton> buttons = net.minecraftforge.fml.common.ObfuscationReflectionHelper
                .getPrivateValue(GuiScreen.class, screen, "field_146292_n");
        for (GuiButton button : buttons) {
            if (!(button instanceof FFDFriendsWidgets.Button)) continue;
            if (button.id == FRIENDS) {
                FFDFriendsService.Data data = FFDFriendsClient.INSTANCE.data();
                int count = data.enabled ? data.incoming.size() + data.outgoing.size() : 0;
                if (count > 0) {
                    String label = count > 5 ? "5+" : Integer.toString(count);
                    int w = Minecraft.getMinecraft().fontRenderer.getStringWidth(label);
                    Gui.drawRect(button.x + 16 - w / 2, button.y - 4, button.x + 20 + (w + 1) / 2, button.y + 6, 0xFFB02020);
                    Minecraft.getMinecraft().fontRenderer.drawString(label, button.x + 18 - w / 2, button.y - 3, 0xFFFFFF);
                }
            }
            if (button.visible && event.getMouseX() >= button.x && event.getMouseX() < button.x + button.width
                    && event.getMouseY() >= button.y && event.getMouseY() < button.y + button.height)
                FFDFriendsWidgets.tooltip(screen, button.id == FRIENDS ? FFDFriendsWidgets.text("gui.friends.open.tooltip")
                        : button.displayString, event.getMouseX(), event.getMouseY(), button);
        }
    }
}
