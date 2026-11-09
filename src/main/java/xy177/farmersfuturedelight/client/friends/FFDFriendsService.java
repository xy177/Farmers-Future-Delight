package xy177.farmersfuturedelight.client.friends;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;

public final class FFDFriendsService implements AutoCloseable {
    public enum Error {
        OFFLINE_SESSION, UNAUTHORIZED, FORBIDDEN, UNKNOWN_PROFILE, RATE_LIMITED,
        UNAVAILABLE, NETWORK, INVALID_RESPONSE, UNSAFE_ENDPOINT, PROFILE_MISMATCH
    }

    public static final class Failure extends Exception {
        public final Error error;
        public final long retryMillis;

        public Failure(Error error) { this(error, 0L); }

        public Failure(Error error, long retryMillis) {
            super(error.name());
            this.error = error;
            this.retryMillis = retryMillis;
        }
    }

    public static final class Friend {
        public final UUID id;
        public final String name;

        public Friend(UUID id, String name) { this.id = id; this.name = name; }
    }

    public static final class Data {
        public final List<Friend> friends;
        public final List<Friend> incoming;
        public final List<Friend> outgoing;
        public final boolean enabled;
        public final boolean allowRequests;

        public Data(List<Friend> friends, List<Friend> incoming, List<Friend> outgoing,
                boolean enabled, boolean allowRequests) {
            this.friends = Collections.unmodifiableList(new ArrayList<>(friends));
            this.incoming = Collections.unmodifiableList(new ArrayList<>(incoming));
            this.outgoing = Collections.unmodifiableList(new ArrayList<>(outgoing));
            this.enabled = enabled;
            this.allowRequests = allowRequests;
        }

        public static Data empty() {
            return new Data(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false);
        }
    }

    static final class Reply {
        final int status;
        final JsonObject body;
        final String etag;
        final long retryMillis;

        Reply(int status, JsonObject body, String etag, long retryMillis) {
            this.status = status; this.body = body; this.etag = etag; this.retryMillis = retryMillis;
        }
    }

    interface Transport {
        Reply request(String method, URI uri, String token, JsonObject body, String etag) throws Failure;
    }

    private final UUID expectedId;
    private final Proxy proxy;
    private final Transport transport;
    private final Map<String, URI> endpoints = new HashMap<>();
    private volatile String token;
    private volatile HttpsURLConnection activeConnection;
    private boolean identified;
    private String friendEtag;
    private String presenceEtag;
    private Data data = Data.empty();
    private Map<UUID, String> presence = Collections.emptyMap();
    private long pollMillis;
    private long presencePollMillis;

    public FFDFriendsService(String token, UUID expectedId, Proxy proxy) {
        this(token, expectedId, proxy, null);
    }

    FFDFriendsService(String token, UUID expectedId, Proxy proxy, Transport transport) {
        this.token = token;
        this.expectedId = expectedId;
        this.proxy = proxy == null ? Proxy.NO_PROXY : proxy;
        this.transport = transport == null ? this::https : transport;
    }

    public static UUID profileId(String value) {
        if (value == null) return null;
        String normalized = value.replace("-", "");
        if (!normalized.matches("[0-9a-fA-F]{32}")) return null;
        return UUID.fromString(normalized.substring(0, 8) + "-" + normalized.substring(8, 12) + "-"
                + normalized.substring(12, 16) + "-" + normalized.substring(16, 20) + "-" + normalized.substring(20));
    }

    static URI allowed(String address, boolean authenticated) throws Failure {
        try {
            URI uri = URI.create(address);
            String host = authenticated ? "api.minecraftservices.com" : "discovery.minecraftservices.com";
            if ("https".equals(uri.getScheme()) && host.equals(uri.getHost())
                    && (uri.getPort() == -1 || uri.getPort() == 443) && uri.getUserInfo() == null
                    && uri.getQuery() == null && uri.getFragment() == null) return uri;
        } catch (IllegalArgumentException ignored) { }
        throw new Failure(Error.UNSAFE_ENDPOINT);
    }

    private Reply request(String method, URI uri, boolean authenticated, JsonObject body, String etag) throws Failure {
        allowed(uri.toString(), authenticated);
        String current = token;
        if (current == null || current.isEmpty() || current.equals("0") || current.equals("null") || expectedId == null)
            throw new Failure(Error.OFFLINE_SESSION);
        Reply reply = transport.request(method, uri, authenticated ? current : null, body, etag);
        if (reply.status == 200 || reply.status == 204 || reply.status == 304) return reply;
        Error error = reply.status == 401 ? Error.UNAUTHORIZED : reply.status == 403 ? Error.FORBIDDEN
                : reply.status == 400 || reply.status == 404 ? Error.UNKNOWN_PROFILE
                : reply.status == 429 ? Error.RATE_LIMITED : Error.UNAVAILABLE;
        throw new Failure(error, reply.retryMillis);
    }

    private void connect() throws Failure {
        if (identified) return;
        JsonObject discovery = object(request("GET", allowed(
                "https://discovery.minecraftservices.com/minecraft/client", false), false, null, null));
        try {
            JsonObject entries = discovery.getAsJsonObject("discovery").getAsJsonObject("player")
                    .getAsJsonObject("endpoints");
            for (String key : new String[]{"getAttributes", "updateAttributes", "getFriends", "updateFriends", "updatePresence"})
                endpoints.put(key, allowed(entries.getAsJsonObject(key).get("uri").getAsString(), true));
        } catch (RuntimeException invalid) { throw new Failure(Error.INVALID_RESPONSE); }
        JsonObject profile = object(request("GET", allowed(
                "https://api.minecraftservices.com/minecraft/profile", true), true, null, null));
        if (!expectedId.equals(profileId(text(profile, "id")))) throw new Failure(Error.PROFILE_MISMATCH);
        identified = true;
    }

    public Data read() throws Failure {
        connect();
        JsonObject attributes = object(request("GET", endpoints.get("getAttributes"), true, null, null));
        boolean enabled;
        boolean requests;
        try {
            JsonObject prefs = attributes.getAsJsonObject("friendsPreferences");
            enabled = toggle(prefs, "friends");
            requests = toggle(prefs, "acceptInvites");
        } catch (RuntimeException invalid) { throw new Failure(Error.INVALID_RESPONSE); }
        Reply reply = request("GET", endpoints.get("getFriends"), true, null, friendEtag);
        pollMillis = reply.retryMillis;
        if (reply.status != 304) {
            data = parseData(object(reply), enabled, requests);
            friendEtag = reply.etag;
        } else data = new Data(data.friends, data.incoming, data.outgoing, enabled, requests);
        return data;
    }

    public Data change(UUID id, String name, boolean add) throws Failure {
        connect();
        JsonObject payload = new JsonObject();
        if (id != null) payload.addProperty("profileId", id.toString());
        else {
            if (name == null || !name.matches("[A-Za-z0-9_]{1,16}")) throw new Failure(Error.UNKNOWN_PROFILE);
            payload.addProperty("name", name);
        }
        payload.addProperty("updateType", add ? "ADD" : "REMOVE");
        Reply reply = request("PUT", endpoints.get("updateFriends"), true, payload, null);
        friendEtag = null;
        if (reply.body == null) return read();
        data = parseData(reply.body, data.enabled, data.allowRequests);
        return data;
    }

    public Data settings(boolean enabled, boolean requests) throws Failure {
        connect();
        JsonObject prefs = new JsonObject();
        prefs.addProperty("friends", enabled ? "ENABLED" : "DISABLED");
        prefs.addProperty("acceptInvites", enabled && requests ? "ENABLED" : "DISABLED");
        JsonObject payload = new JsonObject();
        payload.add("friendsPreferences", prefs);
        request("POST", endpoints.get("updateAttributes"), true, payload, null);
        return read();
    }

    public Map<UUID, String> presence(String status) throws Failure {
        connect();
        if (!status.matches("ONLINE|OFFLINE|PLAYING_OFFLINE|PLAYING_SERVER|PLAYING_REALMS|PLAYING_HOSTED_SERVER"))
            throw new Failure(Error.INVALID_RESPONSE);
        JsonObject payload = new JsonObject();
        payload.addProperty("status", status);
        Reply reply = request("POST", endpoints.get("updatePresence"), true, payload, presenceEtag);
        presencePollMillis = reply.retryMillis;
        if (reply.status == 304) return presence;
        JsonObject body = object(reply);
        Map<UUID, String> values = new HashMap<>();
        try {
            JsonArray list = body.has("presence") && !body.get("presence").isJsonNull()
                    ? body.getAsJsonArray("presence") : new JsonArray();
            if (list.size() > 10000) throw new IllegalArgumentException();
            for (JsonElement item : list) {
                JsonObject entry = item.getAsJsonObject();
                UUID id = profileId(text(entry, "profileId"));
                String state = text(entry, "status").toUpperCase(Locale.ROOT);
                if (id == null) throw new IllegalArgumentException();
                if (!state.matches("ONLINE|OFFLINE|PLAYING_OFFLINE|PLAYING_SERVER|PLAYING_REALMS|PLAYING_HOSTED_SERVER"))
                    state = "OFFLINE";
                values.put(id, state);
            }
        } catch (RuntimeException invalid) { throw new Failure(Error.INVALID_RESPONSE); }
        presenceEtag = reply.etag;
        presence = Collections.unmodifiableMap(values);
        return presence;
    }

    public long pollMillis() { return pollMillis; }
    public long presencePollMillis() { return presencePollMillis; }

    private static boolean toggle(JsonObject object, String key) throws Failure {
        String value = text(object, key);
        if ("ENABLED".equals(value)) return true;
        if ("DISABLED".equals(value)) return false;
        throw new Failure(Error.INVALID_RESPONSE);
    }

    private static JsonObject object(Reply reply) throws Failure {
        if (reply.body == null) throw new Failure(Error.INVALID_RESPONSE);
        return reply.body;
    }

    private static String text(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }

    static Data parseData(JsonObject body, boolean enabled, boolean requests) throws Failure {
        if (!body.has("friends") && !body.has("incomingRequests") && !body.has("outgoingRequests"))
            throw new Failure(Error.INVALID_RESPONSE);
        return new Data(friends(body, "friends"), friends(body, "incomingRequests"),
                friends(body, "outgoingRequests"), enabled, requests);
    }

    private static List<Friend> friends(JsonObject body, String key) throws Failure {
        List<Friend> result = new ArrayList<>();
        if (!body.has(key) || body.get(key).isJsonNull()) return result;
        Set<UUID> seen = new HashSet<>();
        try {
            JsonArray entries = body.getAsJsonArray(key);
            if (entries.size() > 10000) throw new IllegalArgumentException();
            for (JsonElement element : entries) {
                JsonObject entry = element.getAsJsonObject();
                UUID id = profileId(text(entry, "profileId"));
                String name = text(entry, "name");
                if (id == null || !name.matches("[A-Za-z0-9_]{1,16}")) throw new IllegalArgumentException();
                if (seen.add(id)) result.add(new Friend(id, name));
            }
        } catch (RuntimeException invalid) { throw new Failure(Error.INVALID_RESPONSE); }
        return result;
    }

    private Reply https(String method, URI uri, String credential, JsonObject body, String etag) throws Failure {
        HttpsURLConnection connection = null;
        try {
            if (Thread.currentThread().isInterrupted()) throw new Failure(Error.NETWORK);
            connection = (HttpsURLConnection) uri.toURL().openConnection(proxy);
            activeConnection = connection;
            connection.setRequestMethod(method);
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "FarmerFutureDelight-Friends/1.4.7");
            connection.setRequestProperty("Accept", "application/json");
            if (credential != null) connection.setRequestProperty("Authorization", "Bearer " + credential);
            if (etag != null) connection.setRequestProperty("If-None-Match", etag);
            if (body != null) {
                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream output = connection.getOutputStream()) { output.write(bytes); }
            }
            int code = connection.getResponseCode();
            long retry = 0L;
            String header = connection.getHeaderField("Retry-After");
            try { if (header != null) retry = Math.min(86400L, Math.max(0L, Long.parseLong(header))) * 1000L; }
            catch (NumberFormatException ignored) { }
            JsonObject object = null;
            if (code >= 200 && code < 300 && code != 204) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (InputStream input = connection.getInputStream()) {
                    byte[] buffer = new byte[4096];
                    for (int count; (count = input.read(buffer)) != -1;) {
                        if (bytes.size() + count > 2097152) throw new Failure(Error.INVALID_RESPONSE);
                        bytes.write(buffer, 0, count);
                    }
                }
                if (bytes.size() > 0) {
                    JsonElement parsed = new JsonParser().parse(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
                    if (!parsed.isJsonObject()) throw new Failure(Error.INVALID_RESPONSE);
                    object = parsed.getAsJsonObject();
                }
            }
            return new Reply(code, object, connection.getHeaderField("ETag"), retry);
        } catch (Failure failure) { throw failure; }
        catch (com.google.gson.JsonParseException | IllegalStateException invalid) { throw new Failure(Error.INVALID_RESPONSE); }
        catch (Exception failure) { throw new Failure(Error.NETWORK); }
        finally {
            activeConnection = null;
            if (connection != null) connection.disconnect();
        }
    }

    @Override
    public void close() {
        token = null;
        HttpsURLConnection connection = activeConnection;
        if (connection != null) connection.disconnect();
    }
}
