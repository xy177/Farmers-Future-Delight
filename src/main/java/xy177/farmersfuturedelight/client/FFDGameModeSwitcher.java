package xy177.farmersfuturedelight.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class FFDGameModeSwitcher {
    private static NetHandlerPlayClient connection;
    private static GameType currentMode;
    private static GameType previousMode;
    private static boolean closedDebugScreen;

    private FFDGameModeSwitcher() {
    }

    public static boolean handleDebugKey(int key) {
        if (key != Keyboard.KEY_F4 || !FFDConfig.isGameModeSwitcherEnabled()) return false;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null || minecraft.currentScreen != null) {
            return true;
        }
        if (canSwitch(minecraft)) {
            trackMode(minecraft);
            GameType selected = previousMode != null ? previousMode
                    : currentMode == GameType.CREATIVE ? GameType.SURVIVAL : GameType.CREATIVE;
            minecraft.displayGuiScreen(new GuiGameModeSwitcher(selected));
        } else {
            minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("")
                    .appendSibling(new TextComponentTranslation("debug.prefix")
                            .setStyle(new Style().setColor(TextFormatting.YELLOW).setBold(true)))
                    .appendText(" ").appendSibling(new TextComponentTranslation(
                            "debug.farmers_future_delight.gamemodes.error")));
        }
        return true;
    }

    static boolean canSwitch(Minecraft minecraft) {
        return FFDConfig.isGameModeSwitcherEnabled() && minecraft.world != null && minecraft.player != null
                && minecraft.playerController != null && minecraft.getConnection() != null
                && minecraft.player.canUseCommand(2, "gamemode");
    }

    public static void toggleShader(EntityRenderer renderer) {
        if (!FFDConfig.isGameModeSwitcherEnabled() || !Keyboard.isKeyDown(Keyboard.KEY_F3)) renderer.switchUseShader();
    }

    static void screenClosed() {
        closedDebugScreen = true;
    }

    public static boolean consumeDebugRelease() {
        if (!closedDebugScreen || Keyboard.isKeyDown(Keyboard.KEY_F3)) return false;
        closedDebugScreen = false;
        return true;
    }

    @SubscribeEvent
    public static void trackMode(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) trackMode(Minecraft.getMinecraft());
    }

    private static void trackMode(Minecraft minecraft) {
        NetHandlerPlayClient active = minecraft.getConnection();
        GameType mode = minecraft.playerController == null ? null
                : minecraft.playerController.getCurrentGameType();
        if (connection != active || minecraft.player == null) {
            connection = active;
            currentMode = mode;
            previousMode = null;
            return;
        }
        if (currentMode != mode) {
            previousMode = currentMode;
            currentMode = mode;
        }
    }
}
