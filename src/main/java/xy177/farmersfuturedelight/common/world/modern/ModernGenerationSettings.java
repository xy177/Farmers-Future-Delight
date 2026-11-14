package xy177.farmersfuturedelight.common.world.modern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class ModernGenerationSettings {
    public static final String ENGINE = "modern_v1";
    public final int workers;
    public final int lookahead;
    public final int capacity;

    private ModernGenerationSettings(int workers, int lookahead, int capacity) {
        this.workers = workers;
        this.lookahead = lookahead;
        this.capacity = capacity;
    }

    public static ModernGenerationSettings read(String options) {
        if (options == null || options.trim().isEmpty()) return null;
        JsonElement parsed;
        try {
            parsed = new JsonParser().parse(options);
        } catch (com.google.gson.JsonParseException exception) {
            if (options.contains("ffdGenerator")) throw new IllegalArgumentException("Invalid FFD world generator settings", exception);
            return null;
        }
        if (!parsed.isJsonObject()) return null;
        JsonObject json = parsed.getAsJsonObject();
        if (!json.has("ffdGenerator")) return null;
        String engine = json.get("ffdGenerator").getAsString();
        if ("legacy".equals(engine)) return null;
        if (!ENGINE.equals(engine)) {
            throw new IllegalArgumentException("Unsupported FFD world generator: " + engine);
        }
        int available = Runtime.getRuntime().availableProcessors();
        int workers = integer(json, "ffdGenerationWorkers", Math.max(1, Math.min(4, available - 2)), 0, 8);
        int lookahead = integer(json, "ffdGenerationLookahead", Math.max(1, workers), 0, 32);
        int capacity = integer(json, "ffdGenerationCapacity", Math.max(16, lookahead * 2), 2, 64);
        if (capacity <= workers) {
            throw new IllegalArgumentException("ffdGenerationCapacity must be greater than ffdGenerationWorkers");
        }
        if (lookahead >= capacity) {
            throw new IllegalArgumentException("ffdGenerationLookahead must be smaller than ffdGenerationCapacity");
        }
        return new ModernGenerationSettings(workers, lookahead, capacity);
    }

    private static int integer(JsonObject json, String key, int fallback, int minimum, int maximum) {
        if (!json.has(key)) return fallback;
        JsonElement element = json.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(key + " must be an integer");
        }
        double supplied = element.getAsDouble();
        if (!Double.isFinite(supplied) || supplied != Math.rint(supplied)
                || supplied < minimum || supplied > maximum) {
            throw new IllegalArgumentException(key + " must be in " + minimum + ".." + maximum);
        }
        return (int) supplied;
    }
}
