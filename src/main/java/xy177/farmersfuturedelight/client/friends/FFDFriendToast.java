package xy177.farmersfuturedelight.client.friends;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;

public final class FFDFriendToast implements IToast {
    private final String key;
    private final String name;

    public FFDFriendToast(String key, String name) { this.key = key; this.name = name; }

    @Override
    public Visibility draw(GuiToast manager, long elapsed) {
        FFDFriendsWidgets.sprite("toast_background", 0, 0, 160, 32, 160, 32);
        FFDFriendsWidgets.sprite("friends", 7, 8, 16, 16, 16, 16);
        Minecraft mc = manager.getMinecraft();
        mc.fontRenderer.drawString(FFDFriendsWidgets.text("gui.friends.open"), 28, 5, 0xFFFFFF);
        String message = FFDFriendsWidgets.text(key, name);
        mc.fontRenderer.drawSplitString(message, 28, 15, 126, 0xCCCCCC);
        return elapsed < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }
}
