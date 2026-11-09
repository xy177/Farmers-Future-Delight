package xy177.farmersfuturedelight.client.friends;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class FFDFriendsSkins {
    private static final String[] DEFAULTS = {"alex", "ari", "efe", "kai", "makena", "noor", "steve", "sunny", "zuri"};
    private static final Map<UUID, Entry> CACHE = new LinkedHashMap<UUID, Entry>(32, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Entry> eldest) { return size() > 256; }
    };
    private static final ThreadPoolExecutor WORKER = new ThreadPoolExecutor(0, 2, 30L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(64), runnable -> {
                Thread thread = new Thread(runnable, "FFD Friend Skins");
                thread.setDaemon(true);
                return thread;
            });

    private FFDFriendsSkins() { }

    public static ResourceLocation skin(FFDFriendsService.Friend friend) {
        Minecraft mc = Minecraft.getMinecraft();
        Entry entry = CACHE.computeIfAbsent(friend.id, id -> new Entry());
        if (entry.skin == null && !entry.pending && System.currentTimeMillis() >= entry.retryAt) {
            entry.pending = true;
            entry.retryAt = System.currentTimeMillis() + 60000L;
            try {
                WORKER.execute(() -> {
                    try {
                        GameProfile profile = mc.getSessionService().fillProfileProperties(new GameProfile(friend.id, friend.name), false);
                        mc.addScheduledTask(() -> {
                            entry.pending = false;
                            if (profile != null && friend.id.equals(profile.getId()))
                                mc.getSkinManager().loadProfileTextures(profile, (type, location, texture) -> {
                                    if (type == MinecraftProfileTexture.Type.SKIN)
                                        mc.addScheduledTask(() -> entry.skin = location);
                                }, false);
                        });
                    } catch (Exception ignored) {
                        mc.addScheduledTask(() -> entry.pending = false);
                    }
                });
            } catch (RejectedExecutionException ignored) { entry.pending = false; }
        }
        return entry.skin == null ? defaultSkin(friend.id) : entry.skin;
    }

    public static ResourceLocation defaultSkin(UUID id) {
        int index = Math.floorMod(id.hashCode(), 18);
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/gui/friends/skins/"
                + (index < 9 ? "slim/" : "wide/") + DEFAULTS[index % 9] + ".png");
    }

    private static final class Entry {
        ResourceLocation skin;
        boolean pending;
        long retryAt;
    }
}
