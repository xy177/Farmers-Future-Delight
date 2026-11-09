package xy177.farmersfuturedelight.client.friends;

import java.io.File;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

public final class FFDFriendsPreferences {
    private static Configuration config;

    private FFDFriendsPreferences() { }

    private static Configuration config() {
        if (config == null) {
            config = new Configuration(new File(Minecraft.getMinecraft().mcDataDir,
                    "config/farmers_future_delight/friends.cfg"));
            config.load();
        }
        return config;
    }

    public static boolean allowed(UUID id) {
        return id != null && config().hasKey("accounts", id.toString())
                && config().get("accounts", id.toString(), false).getBoolean();
    }

    public static void allow(UUID id, boolean enabled) {
        if (id == null) return;
        config().get("accounts", id.toString(), false).set(enabled);
        config().save();
    }

    public static boolean notifications() {
        return config().getBoolean("notifications", "local", false, "");
    }

    public static void notifications(boolean enabled) {
        config().get("local", "notifications", false).set(enabled);
        config().save();
    }

    public static int sharing() {
        return config().getInt("sharePresence", "local", 2, 0, 2, "");
    }

    public static void sharing(int value) {
        config().get("local", "sharePresence", 2).set(Math.max(0, Math.min(2, value)));
        config().save();
    }
}
