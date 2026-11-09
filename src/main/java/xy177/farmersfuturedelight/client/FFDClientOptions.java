package xy177.farmersfuturedelight.client;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class FFDClientOptions {
    private static Configuration configuration;
    private static float textBackgroundOpacity = 0.5F;
    private static boolean backgroundForChatOnly = true;
    private static boolean toggleSneak;
    private static boolean toggleSprint;
    private static int menuBackgroundBlur = 5;
    private static boolean sneakHeld;
    private static boolean sprintHeld;
    private static boolean sneaking;
    private static boolean sprinting;
    private static boolean inputSuspended = true;
    private static boolean restoreSneakAfterSuspension;
    private static boolean restoreSprintAfterSuspension;
    private static EntityPlayer lastPlayer;

    private FFDClientOptions() {
    }

    private static void load() {
        if (configuration != null) {
            return;
        }
        configuration = new Configuration(new File(Minecraft.getMinecraft().mcDataDir,
                "config/farmers_future_delight/client-options.cfg"));
        configuration.load();
        textBackgroundOpacity = configuration.getFloat("textBackgroundOpacity", "accessibility",
                0.5F, 0.0F, 1.0F, "");
        backgroundForChatOnly = configuration.getBoolean("backgroundForChatOnly", "accessibility",
                true, "");
        toggleSneak = configuration.getBoolean("toggleSneak", "controls", false, "");
        toggleSprint = configuration.getBoolean("toggleSprint", "controls", false, "");
        menuBackgroundBlur = configuration.getInt("menuBackgroundBlur", "menus", 5, 0, 10, "");
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static float textBackgroundOpacity() {
        load();
        return textBackgroundOpacity;
    }

    public static boolean backgroundForChatOnly() {
        load();
        return backgroundForChatOnly;
    }

    public static boolean toggleSneak() {
        load();
        return toggleSneak;
    }

    public static boolean toggleSprint() {
        load();
        return toggleSprint;
    }

    public static void setTextBackgroundOpacity(float opacity) {
        load();
        textBackgroundOpacity = MathHelper.clamp(opacity, 0.0F, 1.0F);
        configuration.get("accessibility", "textBackgroundOpacity", 0.5D)
                .set((double) textBackgroundOpacity);
    }

    public static void setBackgroundForChatOnly(boolean chatOnly) {
        load();
        backgroundForChatOnly = chatOnly;
        configuration.get("accessibility", "backgroundForChatOnly", true).set(chatOnly);
    }

    public static void setToggleSneak(boolean enabled) {
        load();
        toggleSneak = enabled;
        sneaking = false;
        restoreSneakAfterSuspension = false;
        sneakHeld = Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown();
        configuration.get("controls", "toggleSneak", false).set(enabled);
    }

    public static void setToggleSprint(boolean enabled) {
        load();
        toggleSprint = enabled;
        sprinting = false;
        restoreSprintAfterSuspension = false;
        sprintHeld = Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown();
        configuration.get("controls", "toggleSprint", false).set(enabled);
    }

    public static void save() {
        load();
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static int menuBackgroundBlur() {
        load();
        return menuBackgroundBlur;
    }

    public static void setMenuBackgroundBlur(int radius) {
        load();
        menuBackgroundBlur = MathHelper.clamp(radius, 0, 10);
        configuration.get("menus", "menuBackgroundBlur", 5).set(menuBackgroundBlur);
        save();
    }

    public static int chatBackgroundColor(int unfadedTextAlpha) {
        return (int) (MathHelper.clamp(unfadedTextAlpha, 0, 255) * textBackgroundOpacity()) << 24;
    }

    public static int textBackgroundColor(int originalColor) {
        return backgroundForChatOnly() ? originalColor
                : (int) (255.0F * textBackgroundOpacity()) << 24;
    }

    public static boolean effectiveKeyState(KeyBinding binding) {
        Minecraft minecraft = Minecraft.getMinecraft();
        boolean held = binding.isKeyDown();
        if (binding != minecraft.gameSettings.keyBindSneak
                && binding != minecraft.gameSettings.keyBindSprint) {
            return held;
        }
        load();
        if (minecraft.player == null || minecraft.currentScreen != null
                || !minecraft.inGameHasFocus) {
            if (!inputSuspended) {
                restoreSneakAfterSuspension = sneaking
                        && minecraft.gameSettings.keyBindSneak.getKeyCode() >= 0;
                restoreSprintAfterSuspension = sprinting
                        && minecraft.gameSettings.keyBindSprint.getKeyCode() >= 0;
                sneaking = false;
                sprinting = false;
            }
            inputSuspended = true;
            return false;
        }
        if (inputSuspended) {
            sneaking = restoreSneakAfterSuspension;
            sprinting = restoreSprintAfterSuspension;
            restoreSneakAfterSuspension = false;
            restoreSprintAfterSuspension = false;
            sneakHeld = minecraft.gameSettings.keyBindSneak.isKeyDown();
            sprintHeld = minecraft.gameSettings.keyBindSprint.isKeyDown();
            inputSuspended = false;
        }
        if (binding == minecraft.gameSettings.keyBindSneak) {
            if (held && !sneakHeld && toggleSneak) {
                sneaking = !sneaking;
            }
            sneakHeld = held;
            return toggleSneak ? sneaking : held;
        }
        if (held && !sprintHeld && toggleSprint) {
            sprinting = !sprinting;
        }
        sprintHeld = held;
        return toggleSprint ? sprinting : held;
    }

    @SubscribeEvent
    public static void updateKeyboard(InputEvent.KeyInputEvent event) {
        updateKeys();
    }

    @SubscribeEvent
    public static void updateMouse(InputEvent.MouseInputEvent event) {
        updateKeys();
    }

    private static void updateKeys() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.gameSettings != null) {
            effectiveKeyState(minecraft.gameSettings.keyBindSneak);
            effectiveKeyState(minecraft.gameSettings.keyBindSprint);
        }
    }

    @SubscribeEvent
    public static void resetOnWorldChange(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (lastPlayer != minecraft.player || minecraft.player != null
                && !minecraft.player.isEntityAlive()) {
            sneaking = false;
            sprinting = false;
            sneakHeld = false;
            sprintHeld = false;
            inputSuspended = true;
            restoreSneakAfterSuspension = false;
            restoreSprintAfterSuspension = false;
            lastPlayer = minecraft.player;
        }
    }
}
