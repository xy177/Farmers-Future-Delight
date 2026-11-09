package xy177.farmersfuturedelight.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class FFDGuiNavigation {
    private static final Logger LOGGER = LogManager.getLogger("FFD GUI Navigation");
    private static final Set<String> SUPPORTED_SCREENS = new HashSet<>(Arrays.asList(
            "net.minecraft.client.gui.GuiMainMenu", "net.minecraft.client.gui.GuiIngameMenu",
            "net.minecraft.client.gui.GuiCreateWorld",
            "net.minecraft.client.gui.GuiOptions", "net.minecraft.client.gui.GuiVideoSettings",
            "net.minecraft.client.gui.GuiScreenOptionsSounds",
            "net.minecraft.client.gui.GuiCustomizeSkin",
            "xy177.farmersfuturedelight.client.gui.FFDGuiFriendsSettings"));
    private static final Map<GuiScreen, FocusState> STATES = new WeakHashMap<>();
    private static Method actionPerformed;

    private FFDGuiNavigation() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void initialize(GuiScreenEvent.InitGuiEvent.Post event) {
        if (SUPPORTED_SCREENS.contains(event.getGui().getClass().getName())) {
            STATES.put(event.getGui(), new FocusState(event.getButtonList()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        FocusState state = STATES.get(event.getGui());
        if (state == null || !Keyboard.getEventKeyState()) {
            return;
        }
        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_TAB || state.keyboardFocus
                && (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN)) {
            int direction = key == Keyboard.KEY_UP || key == Keyboard.KEY_TAB
                    && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                    || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) ? -1 : 1;
            state.move(direction);
            event.setCanceled(true);
        } else if (state.keyboardFocus && (key == Keyboard.KEY_RETURN
                || key == Keyboard.KEY_NUMPADENTER || key == Keyboard.KEY_SPACE)) {
            GuiButton button = state.focusedButton();
            if (button != null && !(button instanceof GuiOptionSlider)) {
                activate(event.getGui(), state.buttons, button);
                event.setCanceled(true);
            }
        }
    }

    private static void activate(GuiScreen screen, List<GuiButton> buttons, GuiButton button) {
        GuiScreenEvent.ActionPerformedEvent.Pre event =
                new GuiScreenEvent.ActionPerformedEvent.Pre(screen, button, buttons);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        try {
            if (actionPerformed == null) {
                actionPerformed = ObfuscationReflectionHelper.findMethod(GuiScreen.class,
                        "func_146284_a", Void.TYPE, GuiButton.class);
            }
            GuiButton active = event.getButton();
            active.playPressSound(Minecraft.getMinecraft().getSoundHandler());
            actionPerformed.invoke(screen, active);
            if (Minecraft.getMinecraft().currentScreen == screen) {
                MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(
                        screen, active, buttons));
            }
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException exception) {
            LOGGER.warn("Could not activate focused GUI button", exception);
        }
    }

    @SubscribeEvent
    public static void drawFocus(GuiScreenEvent.DrawScreenEvent.Post event) {
        FocusState state = STATES.get(event.getGui());
        if (state == null) {
            return;
        }
        if (event.getMouseX() != state.lastMouseX || event.getMouseY() != state.lastMouseY) {
            state.lastMouseX = event.getMouseX();
            state.lastMouseY = event.getMouseY();
            for (int index = 0; index < state.buttons.size(); index++) {
                GuiButton button = state.buttons.get(index);
                if (button.visible && button.enabled && event.getMouseX() >= button.x
                        && event.getMouseX() < button.x + button.width
                        && event.getMouseY() >= button.y
                        && event.getMouseY() < button.y + button.height) {
                    if (state.index != index) {
                        state.index = index;
                        narrate(button);
                    }
                    state.keyboardFocus = false;
                    break;
                }
            }
        }
        GuiButton focused = state.focusedButton();
        if (focused != null && state.keyboardFocus) {
            int color = 0xFFFFFFFF;
            Gui.drawRect(focused.x - 1, focused.y - 1,
                    focused.x + focused.width + 1, focused.y, color);
            Gui.drawRect(focused.x - 1, focused.y + focused.height,
                    focused.x + focused.width + 1, focused.y + focused.height + 1, color);
            Gui.drawRect(focused.x - 1, focused.y, focused.x, focused.y + focused.height, color);
            Gui.drawRect(focused.x + focused.width, focused.y,
                    focused.x + focused.width + 1, focused.y + focused.height, color);
        }
    }

    private static void narrate(GuiButton button) {
        NarratorChatListener.INSTANCE.say(ChatType.SYSTEM, new TextComponentString(button.displayString));
    }

    private static final class FocusState {
        private final List<GuiButton> buttons;
        private int index = -1;
        private int lastMouseX = Integer.MIN_VALUE;
        private int lastMouseY = Integer.MIN_VALUE;
        private boolean keyboardFocus;

        private FocusState(List<GuiButton> buttons) {
            this.buttons = buttons;
        }

        private GuiButton focusedButton() {
            if (index < 0 || index >= buttons.size()) {
                return null;
            }
            GuiButton button = buttons.get(index);
            return button.enabled && button.visible ? button : null;
        }

        private void move(int direction) {
            if (buttons.isEmpty()) {
                return;
            }
            if (index < 0) {
                index = direction > 0 ? -1 : 0;
            }
            for (int attempt = 0; attempt < buttons.size(); attempt++) {
                index = Math.floorMod(index + direction, buttons.size());
                GuiButton focused = focusedButton();
                if (focused != null) {
                    keyboardFocus = true;
                    narrate(focused);
                    return;
                }
            }
        }
    }
}
