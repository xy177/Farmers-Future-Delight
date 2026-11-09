package xy177.farmersfuturedelight.client.friends;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.client.gui.FFDGuiFriends;

public final class FFDFriendsClient {
    public static final FFDFriendsClient INSTANCE = new FFDFriendsClient();
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(4), runnable -> {
                Thread thread = new Thread(runnable, "FFD Friends");
                thread.setDaemon(true);
                return thread;
            });
    private UUID account;
    private String sessionToken;
    private volatile FFDFriendsService service;
    private FFDFriendsService.Data data = FFDFriendsService.Data.empty();
    private Map<UUID, String> presence = Collections.emptyMap();
    private FFDFriendsService.Error error;
    private FFDFriendsService.Error actionError;
    private boolean ready;
    private boolean busy;
    private boolean installed;
    private int generation;
    private int revision;
    private int tick;
    private int failures;
    private long lastRefresh;
    private long nextRefresh;
    private long nextPresence;
    private long retryNotBefore;
    private String lastPresence;
    private volatile boolean reportOffline;

    private FFDFriendsClient() { executor.allowCoreThreadTimeOut(true); }

    public void install() {
        if (installed) return;
        installed = true;
        MinecraftForge.EVENT_BUS.register(this);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FFDFriendsService current = service;
            executor.shutdownNow();
            if (current != null) {
                try { if (reportOffline) current.presence("OFFLINE"); }
                catch (FFDFriendsService.Failure ignored) { }
                finally { current.close(); }
            }
        }, "FFD Friends Shutdown"));
    }

    public FFDFriendsService.Data data() { return data; }
    public Map<UUID, String> presence() { return presence; }
    public FFDFriendsService.Error error() { return error; }
    public FFDFriendsService.Error actionError() { return actionError; }
    public boolean canAct() { return ready && !busy && error == null && System.currentTimeMillis() >= retryNotBefore; }
    public boolean ready() { return ready; }
    public boolean busy() { return busy; }
    public int revision() { return revision; }
    public UUID account() { session(); return account; }

    private boolean customAuthentication() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            String value = arg.toLowerCase(Locale.ROOT);
            if (value.contains("authlib-injector") || value.contains("authlibinjector")
                    || value.contains("yggdrasil") || value.contains("littleskin") || value.contains("ely.by")) return true;
        }
        return false;
    }

    private void session() {
        Minecraft mc = Minecraft.getMinecraft();
        String token = mc.getSession().getToken();
        UUID id = FFDFriendsService.profileId(mc.getSession().getPlayerID());
        if (java.util.Objects.equals(account, id) && java.util.Objects.equals(sessionToken, token)) return;
        generation++;
        FFDFriendsService old = service;
        service = null;
        if (old != null) old.close();
        executor.getQueue().clear();
        account = id;
        sessionToken = token;
        data = FFDFriendsService.Data.empty();
        presence = Collections.emptyMap();
        ready = busy = false;
        actionError = null;
        failures = 0;
        lastRefresh = nextRefresh = nextPresence = retryNotBefore = 0;
        lastPresence = null;
        reportOffline = false;
        error = token == null || token.isEmpty() || "0".equals(token) || id == null || customAuthentication()
                ? FFDFriendsService.Error.OFFLINE_SESSION : null;
        revision++;
    }

    public void open(GuiScreen parent) {
        session();
        Minecraft mc = Minecraft.getMinecraft();
        if (error == FFDFriendsService.Error.OFFLINE_SESSION || FFDFriendsPreferences.allowed(account)) {
            mc.displayGuiScreen(new FFDGuiFriends(parent));
            refresh();
            return;
        }
        UUID selected = account;
        mc.displayGuiScreen(new GuiYesNo((accepted, id) -> {
            session();
            if (accepted && selected != null && selected.equals(account)) {
                FFDFriendsPreferences.allow(account, true);
                mc.displayGuiScreen(new FFDGuiFriends(parent));
                refresh();
            } else mc.displayGuiScreen(parent);
        }, I18n.format("ffd.friends.connect.title"), I18n.format("ffd.friends.connect.message"),
                I18n.format("ffd.friends.connect.proceed"), I18n.format("gui.cancel"), 0));
    }

    public void refresh() {
        session();
        if (error == FFDFriendsService.Error.OFFLINE_SESSION || !FFDFriendsPreferences.allowed(account)) return;
        long now = System.currentTimeMillis();
        if (!busy && now >= retryNotBefore && now - lastRefresh >= 10000L) nextRefresh = 0L;
    }

    private FFDFriendsService service() {
        if (service == null) service = new FFDFriendsService(sessionToken, account, Minecraft.getMinecraft().getProxy());
        return service;
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ++tick % 20 != 0) return;
        session();
        if (error == FFDFriendsService.Error.OFFLINE_SESSION || !FFDFriendsPreferences.allowed(account) || busy) return;
        long now = System.currentTimeMillis();
        if (now >= nextRefresh) {
            lastRefresh = now;
            submit(FFDFriendsService::read, this::apply, true);
        } else if (ready && data.enabled && !data.friends.isEmpty() && now >= nextPresence) {
            String state = publicPresence();
            submit(current -> current.presence(state), values -> {
                presence = values;
                lastPresence = state;
                reportOffline = !state.equals("OFFLINE");
                nextPresence = System.currentTimeMillis() + Math.max(60000L, service().presencePollMillis());
                revision++;
            });
        }
    }

    private String publicPresence() {
        int sharing = FFDFriendsPreferences.sharing();
        if (sharing == 0) return "OFFLINE";
        if (sharing == 1) return "ONLINE";
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getIntegratedServer() != null)
            return mc.getIntegratedServer().getPublic() ? "PLAYING_HOSTED_SERVER" : "PLAYING_OFFLINE";
        if (mc.world != null && mc.isConnectedToRealms()) return "PLAYING_REALMS";
        if (mc.world != null && mc.getCurrentServerData() != null) return "PLAYING_SERVER";
        return "ONLINE";
    }

    public void presenceChanged() {
        nextPresence = 0L;
    }

    private void apply(FFDFriendsService.Data updated) {
        if (ready && FFDFriendsPreferences.notifications()) {
            notifyAdded(data.friends, updated.friends, "gui.friends.toast.friend_added.message");
            notifyAdded(data.incoming, updated.incoming, "gui.friends.toast.request_received.message");
        }
        boolean newlyEnabled = !data.enabled && updated.enabled;
        boolean firstFriends = data.friends.isEmpty() && !updated.friends.isEmpty();
        data = updated;
        ready = true;
        failures = 0;
        error = null;
        actionError = null;
        retryNotBefore = 0L;
        long interval = Minecraft.getMinecraft().currentScreen instanceof FFDGuiFriends ? 60000L : 300000L;
        nextRefresh = System.currentTimeMillis() + Math.max(interval, service().pollMillis());
        if (!data.enabled) { presence = Collections.emptyMap(); nextPresence = Long.MAX_VALUE; reportOffline = false; }
        else if (newlyEnabled || firstFriends || lastPresence == null) nextPresence = 0;
        revision++;
    }

    private void notifyAdded(java.util.List<FFDFriendsService.Friend> previous,
            java.util.List<FFDFriendsService.Friend> next, String key) {
        Set<UUID> ids = new HashSet<>();
        for (FFDFriendsService.Friend friend : previous) ids.add(friend.id);
        int shown = 0;
        for (FFDFriendsService.Friend friend : next) if (!ids.contains(friend.id) && shown++ < 3)
            Minecraft.getMinecraft().getToastGui().add(new FFDFriendToast(key, friend.name));
    }

    public void change(UUID id, String name, boolean add, Consumer<Boolean> completed) {
        session();
        if (!canAct() || !data.enabled) return;
        submit(current -> current.change(id, name, add), result -> {
            apply(result);
            if (completed != null) completed.accept(true);
        });
    }

    public void settings(boolean enabled, boolean allowRequests) {
        session();
        if (!canAct()) return;
        boolean wasEnabled = data.enabled;
        submit(current -> {
            if (wasEnabled && !enabled && !data.friends.isEmpty())
                try { current.presence("OFFLINE"); } catch (FFDFriendsService.Failure ignored) { }
            return current.settings(enabled, allowRequests);
        }, result -> {
            lastPresence = null;
            apply(result);
        });
    }

    public void disconnect() {
        FFDFriendsPreferences.allow(account, false);
        FFDFriendsService current = service;
        boolean offline = ready && data.enabled && !data.friends.isEmpty();
        generation++;
        service = null;
        reportOffline = false;
        busy = ready = false;
        data = FFDFriendsService.Data.empty();
        presence = Collections.emptyMap();
        error = null;
        actionError = null;
        revision++;
        executor.getQueue().clear();
        if (current != null) {
            try { executor.execute(() -> {
                try { if (offline) current.presence("OFFLINE"); } catch (FFDFriendsService.Failure ignored) { }
                finally { current.close(); }
            }); } catch (RejectedExecutionException ignored) { current.close(); }
        }
    }

    private interface Operation<T> { T run(FFDFriendsService current) throws FFDFriendsService.Failure; }

    private <T> void submit(Operation<T> task, Consumer<T> complete) {
        submit(task, complete, false);
    }

    private <T> void submit(Operation<T> task, Consumer<T> complete, boolean reading) {
        if (busy) return;
        FFDFriendsService selected = service();
        int expected = generation;
        busy = true;
        try {
            executor.execute(() -> {
                T result = null;
                FFDFriendsService.Failure failure = null;
                try { result = task.run(selected); }
                catch (FFDFriendsService.Failure problem) { failure = problem; }
                catch (RuntimeException problem) { failure = new FFDFriendsService.Failure(FFDFriendsService.Error.INVALID_RESPONSE); }
                T value = result;
                FFDFriendsService.Failure problem = failure;
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    session();
                    if (generation != expected || service != selected) return;
                    busy = false;
                    if (problem == null) { error = null; actionError = null; complete.accept(value); }
                    else {
                        if (reading || problem.error == FFDFriendsService.Error.UNAUTHORIZED
                                || problem.error == FFDFriendsService.Error.PROFILE_MISMATCH
                                || problem.error == FFDFriendsService.Error.UNSAFE_ENDPOINT) error = problem.error;
                        else actionError = problem.error;
                        long backoff = Math.max(problem.retryMillis, Math.min(300000L, 30000L << Math.min(4, failures++)));
                        nextRefresh = nextPresence = System.currentTimeMillis() + backoff;
                        if (problem.error == FFDFriendsService.Error.RATE_LIMITED) retryNotBefore = nextRefresh;
                        if (problem.error == FFDFriendsService.Error.UNAUTHORIZED || problem.error == FFDFriendsService.Error.PROFILE_MISMATCH)
                            nextRefresh = nextPresence = Long.MAX_VALUE;
                        revision++;
                    }
                });
            });
        } catch (RejectedExecutionException rejected) {
            busy = false;
            error = FFDFriendsService.Error.UNAVAILABLE;
            nextRefresh = nextPresence = System.currentTimeMillis() + 60000L;
            revision++;
        }
    }
}
