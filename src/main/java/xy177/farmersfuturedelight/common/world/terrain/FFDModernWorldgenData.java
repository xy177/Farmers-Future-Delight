package xy177.farmersfuturedelight.common.world.terrain;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xy177.farmersfuturedelight.common.world.noise.FFDBlendedNoise;
import xy177.farmersfuturedelight.common.world.noise.FFDNormalNoise;
import xy177.farmersfuturedelight.common.world.noise.FFDXoroshiroRandom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FFDModernWorldgenData {
    public static final int MIN_Y = -64;
    public static final int HEIGHT = 384;
    public static final int SEA_LEVEL = 63;
    public static final int CELL_WIDTH = 4;
    public static final int CELL_HEIGHT = 8;

    private static final String RESOURCE_ROOT = "/assets/farmers_future_delight/worldgen/26_3/";

    private final FFDXoroshiroRandom.PositionalFactory positionalRandom;
    private final long worldSeed;
    private final Map<String, FFDNormalNoise> noises = new HashMap<>();
    private final Map<String, FFDDensityFunction> densityFunctions = new HashMap<>();
    private final Map<Long, Integer> finalSurfaceCache = new HashMap<>();
    private final FinalDensityComponents finalDensityComponents;
    private final VeinDensityComponents veinDensityComponents;
    private final FFDNoiseRouter router;
    private final FFDNormalNoise patchNoise;
    private final FFDNormalNoise gravelLayerNoise;
    private final FFDNormalNoise surfaceSecondaryNoise;

    public FFDModernWorldgenData(long seed) {
        worldSeed = seed;
        positionalRandom = new FFDXoroshiroRandom(seed).forkPositional();
        JsonObject settings = loadJson("noise_settings/overworld.json").getAsJsonObject();
        JsonObject routerJson = settings.getAsJsonObject("noise_router");
        finalDensityComponents = parseFinalDensityComponents(routerJson.get("final_density"));
        veinDensityComponents = parseVeinDensityComponents(routerJson);
        router = new FFDNoiseRouter(
                parse(routerJson.get("barrier")),
                parse(routerJson.get("fluid_level_floodedness")),
                parse(routerJson.get("fluid_level_spread")),
                parse(routerJson.get("lava")),
                parse(routerJson.get("temperature")),
                parse(routerJson.get("vegetation")),
                parse(routerJson.get("continents")),
                parse(routerJson.get("erosion")),
                parse(routerJson.get("depth")),
                parse(routerJson.get("ridges")),
                parse(routerJson.get("preliminary_surface_level")),
                finalDensityComponents.function(),
                veinDensityComponents.toggleFunction(),
                veinDensityComponents.ridgedFunction(),
                veinDensityComponents.gap()
        );
        patchNoise = noise("minecraft:patch");
        gravelLayerNoise = noise("minecraft:gravel_layer");
        surfaceSecondaryNoise = noise("minecraft:surface_secondary");
    }

    public FFDNoiseRouter router() {
        return router;
    }

    public FinalDensityComponents finalDensityComponents() {
        return finalDensityComponents;
    }

    public VeinDensityComponents veinDensityComponents() {
        return veinDensityComponents;
    }

    public FFDXoroshiroRandom.PositionalFactory positionalFactory(String id) {
        return positionalRandom.fromHashOf(normalizeId(id)).forkPositional();
    }

    public FFDXoroshiroRandom positionalRandomAt(int x, int y, int z) {
        return positionalRandom.at(x, y, z);
    }

    public FFDXoroshiroRandom randomFromHashOf(String id) {
        return positionalRandom.fromHashOf(normalizeId(id));
    }

    public double sampleNoise(String id, int x, int y, int z) {
        return noise(id).sample(x, y, z);
    }

    public double samplePatchNoise(int x, int y, int z) {
        return patchNoise.sample(x, y, z);
    }

    public double sampleGravelLayerNoise(int x, int y, int z) {
        return gravelLayerNoise.sample(x, y, z);
    }

    public double sampleSurfaceSecondaryNoise(int x, int y, int z) {
        return surfaceSecondaryNoise.sample(x, y, z);
    }

    public int preliminarySurfaceLevel(int x, int z) {
        return (int) Math.floor(router.preliminarySurfaceLevel.sample(x, 0, z));
    }

    public int finalDensitySurfaceLevel(int x, int z) {
        int cacheX = Math.floorDiv(x, CELL_WIDTH) * CELL_WIDTH;
        int cacheZ = Math.floorDiv(z, CELL_WIDTH) * CELL_WIDTH;
        long key = (cacheX & 0xFFFFFFFFL) | (long) cacheZ << 32;
        Integer cached = finalSurfaceCache.get(key);
        if (cached != null) {
            return cached;
        }

        int upper = MIN_Y + HEIGHT - 1;
        int sampled = Integer.MIN_VALUE;
        for (int y = upper - Math.floorMod(upper, CELL_HEIGHT); y >= MIN_Y; y -= CELL_HEIGHT) {
            if (router.finalDensity.sample(cacheX, y, cacheZ) > 0.0D) {
                sampled = y;
                break;
            }
        }
        if (sampled == Integer.MIN_VALUE) {
            finalSurfaceCache.put(key, MIN_Y - 1);
            return MIN_Y - 1;
        }

        finalSurfaceCache.put(key, sampled);
        return sampled;
    }

    public FFDDensityFunction densityFunction(String id) {
        String normalized = normalizeId(id);
        FFDDensityFunction cached = densityFunctions.get(normalized);
        if (cached != null) {
            return cached;
        }
        FFDDensityFunction parsed = parse(loadJson("density_function/" + pathOf(normalized) + ".json"));
        densityFunctions.put(normalized, parsed);
        return parsed;
    }

    private FFDNormalNoise noise(String id) {
        String normalized = normalizeId(id);
        FFDNormalNoise cached = noises.get(normalized);
        if (cached != null) {
            return cached;
        }
        JsonObject json = loadJson("noise/" + pathOf(normalized) + ".json").getAsJsonObject();
        JsonArray values = json.getAsJsonArray("amplitudes");
        double[] amplitudes = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            amplitudes[i] = values.get(i).getAsDouble();
        }
        FFDNormalNoise created = new FFDNormalNoise(positionalRandom.fromHashOf(normalized),
                json.get("firstOctave").getAsInt(), amplitudes);
        noises.put(normalized, created);
        return created;
    }

    private FFDDensityFunction parse(JsonElement element) {
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) {
                return constant(element.getAsDouble());
            }
            return densityFunction(element.getAsString());
        }
        JsonObject json = element.getAsJsonObject();
        String type = pathOf(json.get("type").getAsString());
        switch (type) {
            case "abs":
                return mapped(parse(json.get("argument")), MappedType.ABS);
            case "square":
                return mapped(parse(json.get("argument")), MappedType.SQUARE);
            case "cube":
                return mapped(parse(json.get("argument")), MappedType.CUBE);
            case "half_negative":
                return mapped(parse(json.get("argument")), MappedType.HALF_NEGATIVE);
            case "quarter_negative":
                return mapped(parse(json.get("argument")), MappedType.QUARTER_NEGATIVE);
            case "invert":
                return mapped(parse(json.get("argument")), MappedType.INVERT);
            case "squeeze":
                return mapped(parse(json.get("argument")), MappedType.SQUEEZE);
            case "add":
                return binary(parse(json.get("argument1")), parse(json.get("argument2")), BinaryType.ADD);
            case "mul":
                return binary(parse(json.get("argument1")), parse(json.get("argument2")), BinaryType.MUL);
            case "min":
                return binary(parse(json.get("argument1")), parse(json.get("argument2")), BinaryType.MIN);
            case "max":
                return binary(parse(json.get("argument1")), parse(json.get("argument2")), BinaryType.MAX);
            case "clamp":
                return clamp(parse(json.get("input")), json.get("min").getAsDouble(), json.get("max").getAsDouble());
            case "range_choice":
                return rangeChoice(parse(json.get("input")), json.get("min_inclusive").getAsDouble(),
                        json.get("max_exclusive").getAsDouble(), parse(json.get("when_in_range")),
                        parse(json.get("when_out_of_range")));
            case "interval_select":
                return intervalSelect(json);
            case "noise":
                return noiseFunction(json);
            case "shifted_noise":
                return shiftedNoise(json);
            case "shift_a":
                return shift(json.get("argument").getAsString(), true);
            case "shift_b":
                return shift(json.get("argument").getAsString(), false);
            case "old_blended_noise":
                return blendedNoise(json);
            case "y_clamped_gradient":
                return yGradient(json);
            case "spline":
                return parseSpline(json.get("spline"));
            case "find_top_surface":
                return findTopSurface(json);
            case "end_islands":
                return new FFDEndIslandDensity(worldSeed);
            case "blend_alpha":
                return constant(1.0D);
            case "blend_offset":
                return constant(0.0D);
            case "blend_density":
                return parse(json.get("argument"));
            case "cache_2d":
                return cache2d(parse(json.get("argument")));
            case "flat_cache":
                return flatCache(parse(json.get("argument")));
            case "cache_once":
                return cacheOnce(parse(json.get("argument")));
            case "interpolated":
                return interpolated(parse(json.get("argument")));
            default:
                throw new IllegalArgumentException("Unsupported 26.3 density function: " + type);
        }
    }

    private FFDDensityFunction noiseFunction(JsonObject json) {
        final FFDNormalNoise noise = noise(json.get("noise").getAsString());
        final double xzScale = json.get("xz_scale").getAsDouble();
        final double yScale = json.get("y_scale").getAsDouble();
        return (x, y, z) -> noise.sample(x * xzScale, y * yScale, z * xzScale);
    }

    private FinalDensityComponents parseFinalDensityComponents(JsonElement finalDensityJson) {
        JsonObject root = expectType(finalDensityJson, "min");
        JsonObject squeezed = expectType(root.get("argument1"), "squeeze");
        JsonObject mainInterpolated = expectType(squeezed.get("argument"), "interpolated");

        JsonElement noodleReference = root.get("argument2");
        if (noodleReference == null || !noodleReference.isJsonPrimitive()
                || !noodleReference.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Expected final density noodle reference");
        }
        String noodleId = normalizeId(noodleReference.getAsString());
        JsonObject noodle = loadJson("density_function/" + pathOf(noodleId) + ".json").getAsJsonObject();
        JsonObject toggleInterpolated = expectType(noodle.get("input"), "interpolated");
        JsonObject noodleBody = expectType(noodle.get("when_out_of_range"), "add");
        JsonObject thicknessInterpolated = expectType(noodleBody.get("argument1"), "interpolated");
        JsonObject ridgeScale = expectType(noodleBody.get("argument2"), "mul");
        JsonObject ridgeMaximum = expectType(ridgeScale.get("argument2"), "max");
        JsonObject ridgeAAbsolute = expectType(ridgeMaximum.get("argument1"), "abs");
        JsonObject ridgeBAbsolute = expectType(ridgeMaximum.get("argument2"), "abs");
        JsonObject ridgeAInterpolated = expectType(ridgeAAbsolute.get("argument"), "interpolated");
        JsonObject ridgeBInterpolated = expectType(ridgeBAbsolute.get("argument"), "interpolated");

        return new FinalDensityComponents(
                parse(mainInterpolated.get("argument")),
                parse(toggleInterpolated.get("argument")),
                parse(thicknessInterpolated.get("argument")),
                parse(ridgeAInterpolated.get("argument")),
                parse(ridgeBInterpolated.get("argument")));
    }

    private static JsonObject expectType(JsonElement element, String expectedType) {
        if (element == null || !element.isJsonObject()) {
            throw new IllegalArgumentException("Expected density function object: " + expectedType);
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement type = object.get("type");
        if (type == null || !expectedType.equals(pathOf(type.getAsString()))) {
            throw new IllegalArgumentException("Expected density function " + expectedType);
        }
        return object;
    }

    private VeinDensityComponents parseVeinDensityComponents(JsonObject routerJson) {
        JsonObject toggleInterpolated = expectType(routerJson.get("vein_toggle"), "interpolated");
        JsonObject ridged = expectType(routerJson.get("vein_ridged"), "add");
        double ridgedOffset = ridged.get("argument1").getAsDouble();
        JsonObject ridgeMaximum = expectType(ridged.get("argument2"), "max");
        JsonObject ridgeAAbsolute = expectType(ridgeMaximum.get("argument1"), "abs");
        JsonObject ridgeBAbsolute = expectType(ridgeMaximum.get("argument2"), "abs");
        JsonObject ridgeAInterpolated = expectType(ridgeAAbsolute.get("argument"), "interpolated");
        JsonObject ridgeBInterpolated = expectType(ridgeBAbsolute.get("argument"), "interpolated");
        return new VeinDensityComponents(
                parse(toggleInterpolated.get("argument")),
                parse(ridgeAInterpolated.get("argument")),
                parse(ridgeBInterpolated.get("argument")),
                parse(routerJson.get("vein_gap")),
                ridgedOffset);
    }

    private FFDDensityFunction shiftedNoise(JsonObject json) {
        final FFDDensityFunction shiftX = parse(json.get("shift_x"));
        final FFDDensityFunction shiftY = parse(json.get("shift_y"));
        final FFDDensityFunction shiftZ = parse(json.get("shift_z"));
        final FFDNormalNoise noise = noise(json.get("noise").getAsString());
        final double xzScale = json.get("xz_scale").getAsDouble();
        final double yScale = json.get("y_scale").getAsDouble();
        return (x, y, z) -> noise.sample(x * xzScale + shiftX.sample(x, y, z),
                y * yScale + shiftY.sample(x, y, z), z * xzScale + shiftZ.sample(x, y, z));
    }

    private FFDDensityFunction shift(String noiseId, boolean shiftA) {
        final FFDNormalNoise noise = noise(noiseId);
        if (shiftA) {
            return (x, y, z) -> noise.sample(x * 0.25D, 0.0D, z * 0.25D) * 4.0D;
        }
        return (x, y, z) -> noise.sample(z * 0.25D, x * 0.25D, 0.0D) * 4.0D;
    }

    private FFDDensityFunction blendedNoise(JsonObject json) {
        final FFDBlendedNoise noise = new FFDBlendedNoise(positionalRandom.fromHashOf("minecraft:terrain"),
                json.get("xz_scale").getAsDouble(), json.get("y_scale").getAsDouble(),
                json.get("xz_factor").getAsDouble(), json.get("y_factor").getAsDouble(),
                json.get("smear_scale_multiplier").getAsDouble());
        return noise::sample;
    }

    private FFDDensityFunction intervalSelect(JsonObject json) {
        final FFDDensityFunction input = parse(json.get("input"));
        JsonArray thresholdJson = json.getAsJsonArray("thresholds");
        final double[] thresholds = new double[thresholdJson.size()];
        for (int i = 0; i < thresholds.length; i++) {
            thresholds[i] = thresholdJson.get(i).getAsDouble();
        }
        JsonArray functionJson = json.getAsJsonArray("functions");
        final FFDDensityFunction[] functions = new FFDDensityFunction[functionJson.size()];
        for (int i = 0; i < functions.length; i++) {
            functions[i] = parse(functionJson.get(i));
        }
        if (functions.length != thresholds.length + 1) {
            throw new IllegalArgumentException("Interval select needs one more function than threshold");
        }
        return (x, y, z) -> {
            double value = input.sample(x, y, z);
            for (int i = 0; i < thresholds.length; i++) {
                if (value < thresholds[i]) {
                    return functions[i].sample(x, y, z);
                }
            }
            return functions[functions.length - 1].sample(x, y, z);
        };
    }

    private FFDDensityFunction yGradient(JsonObject json) {
        final int fromY = json.get("from_y").getAsInt();
        final int toY = json.get("to_y").getAsInt();
        final double fromValue = json.get("from_value").getAsDouble();
        final double toValue = json.get("to_value").getAsDouble();
        return (x, y, z) -> clampedMap(y, fromY, toY, fromValue, toValue);
    }

    private FFDDensityFunction findTopSurface(JsonObject json) {
        final FFDDensityFunction density = parse(json.get("density"));
        final FFDDensityFunction upperBound = parse(json.get("upper_bound"));
        final int lowerBound = json.get("lower_bound").getAsInt();
        final int cellHeight = json.get("cell_height").getAsInt();
        final ThreadLocal<TopSurfaceCache> cache = ThreadLocal.withInitial(TopSurfaceCache::new);
        return (x, y, z) -> {
            TopSurfaceCache cached = cache.get();
            if (cached.valid && cached.x == x && cached.z == z) {
                return cached.value;
            }
            int topY = (int) Math.floor(upperBound.sample(x, y, z) / cellHeight) * cellHeight;
            int result;
            if (topY <= lowerBound) {
                result = lowerBound;
            } else {
                result = lowerBound;
                for (int sampleY = topY; sampleY >= lowerBound; sampleY -= cellHeight) {
                    if (density.sample(x, sampleY, z) > 0.0D) {
                        result = sampleY;
                        break;
                    }
                }
            }
            cached.valid = true;
            cached.x = x;
            cached.z = z;
            cached.value = result;
            return result;
        };
    }

    private FFDDensityFunction parseSpline(JsonElement element) {
        final Spline spline = parseSplineValue(element);
        return spline::sample;
    }

    private Spline parseSplineValue(JsonElement element) {
        if (element.isJsonPrimitive()) {
            final float value = element.getAsFloat();
            return (x, y, z) -> value;
        }
        JsonObject json = element.getAsJsonObject();
        final FFDDensityFunction coordinate = parse(json.get("coordinate"));
        JsonArray points = json.getAsJsonArray("points");
        final float[] locations = new float[points.size()];
        final float[] derivatives = new float[points.size()];
        final List<Spline> values = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            JsonObject point = points.get(i).getAsJsonObject();
            locations[i] = point.get("location").getAsFloat();
            derivatives[i] = point.get("derivative").getAsFloat();
            values.add(parseSplineValue(point.get("value")));
        }
        return (x, y, z) -> sampleSpline((float) coordinate.sample(x, y, z), locations, derivatives, values, x, y, z);
    }

    private static float sampleSpline(float input, float[] locations, float[] derivatives, List<Spline> values,
                                      int x, int y, int z) {
        int start = -1;
        while (start + 1 < locations.length && input >= locations[start + 1]) {
            start++;
        }
        int last = locations.length - 1;
        if (start < 0) {
            return linearExtend(input, locations, values.get(0).sample(x, y, z), derivatives, 0);
        }
        if (start == last) {
            return linearExtend(input, locations, values.get(last).sample(x, y, z), derivatives, last);
        }
        float firstX = locations[start];
        float secondX = locations[start + 1];
        float amount = (input - firstX) / (secondX - firstX);
        float firstY = values.get(start).sample(x, y, z);
        float secondY = values.get(start + 1).sample(x, y, z);
        float delta = secondX - firstX;
        float firstCurve = derivatives[start] * delta - (secondY - firstY);
        float secondCurve = -derivatives[start + 1] * delta + (secondY - firstY);
        return lerp(amount, firstY, secondY) + amount * (1.0F - amount)
                * lerp(amount, firstCurve, secondCurve);
    }

    private static float linearExtend(float input, float[] locations, float value, float[] derivatives, int index) {
        return value + derivatives[index] * (input - locations[index]);
    }

    private static FFDDensityFunction constant(final double value) {
        return (x, y, z) -> value;
    }

    private static FFDDensityFunction mapped(final FFDDensityFunction input, final MappedType type) {
        return (x, y, z) -> type.apply(input.sample(x, y, z));
    }

    private static FFDDensityFunction binary(final FFDDensityFunction first, final FFDDensityFunction second,
                                             final BinaryType type) {
        return (x, y, z) -> type.apply(first.sample(x, y, z), second.sample(x, y, z));
    }

    private static FFDDensityFunction clamp(final FFDDensityFunction input, final double min, final double max) {
        return (x, y, z) -> Math.max(min, Math.min(max, input.sample(x, y, z)));
    }

    private static FFDDensityFunction rangeChoice(final FFDDensityFunction input, final double min, final double max,
                                                  final FFDDensityFunction inRange,
                                                  final FFDDensityFunction outOfRange) {
        return (x, y, z) -> {
            double value = input.sample(x, y, z);
            return value >= min && value < max ? inRange.sample(x, y, z) : outOfRange.sample(x, y, z);
        };
    }

    private static FFDDensityFunction cacheOnce(final FFDDensityFunction input) {
        final ThreadLocal<SampleCache> cache = ThreadLocal.withInitial(SampleCache::new);
        return (x, y, z) -> {
            SampleCache value = cache.get();
            if (value.valid && value.x == x && value.y == y && value.z == z) {
                return value.value;
            }
            value.valid = true;
            value.x = x;
            value.y = y;
            value.z = z;
            value.value = input.sample(x, y, z);
            return value.value;
        };
    }

    private static FFDDensityFunction cache2d(final FFDDensityFunction input) {
        final ThreadLocal<SampleCache> cache = ThreadLocal.withInitial(SampleCache::new);
        return (x, y, z) -> {
            SampleCache value = cache.get();
            if (value.valid && value.x == x && value.z == z) {
                return value.value;
            }
            value.valid = true;
            value.x = x;
            value.z = z;
            value.value = input.sample(x, 0, z);
            return value.value;
        };
    }

    private static FFDDensityFunction flatCache(final FFDDensityFunction input) {
        final ThreadLocal<SampleCache> cache = ThreadLocal.withInitial(SampleCache::new);
        return (x, y, z) -> {
            int gridX = Math.floorDiv(x, CELL_WIDTH) * CELL_WIDTH;
            int gridZ = Math.floorDiv(z, CELL_WIDTH) * CELL_WIDTH;
            SampleCache value = cache.get();
            if (value.valid && value.x == gridX && value.z == gridZ) {
                return value.value;
            }
            value.valid = true;
            value.x = gridX;
            value.z = gridZ;
            value.value = input.sample(gridX, 0, gridZ);
            return value.value;
        };
    }

    private static FFDDensityFunction interpolated(final FFDDensityFunction input) {
        final ThreadLocal<InterpolationCache> cache = ThreadLocal.withInitial(InterpolationCache::new);
        return (x, y, z) -> {
            int cellX = Math.floorDiv(x, CELL_WIDTH) * CELL_WIDTH;
            int cellY = Math.floorDiv(y, CELL_HEIGHT) * CELL_HEIGHT;
            int cellZ = Math.floorDiv(z, CELL_WIDTH) * CELL_WIDTH;
            if (x == cellX && y == cellY && z == cellZ) {
                return input.sample(x, y, z);
            }
            InterpolationCache values = cache.get();
            if (!values.valid || values.x != cellX || values.y != cellY || values.z != cellZ) {
                values.fill(input, cellX, cellY, cellZ);
            }
            double localX = (x - cellX) / (double) CELL_WIDTH;
            double localY = (y - cellY) / (double) CELL_HEIGHT;
            double localZ = (z - cellZ) / (double) CELL_WIDTH;
            return lerp(localZ,
                    lerp(localY, lerp(localX, values.samples[0], values.samples[1]),
                            lerp(localX, values.samples[2], values.samples[3])),
                    lerp(localY, lerp(localX, values.samples[4], values.samples[5]),
                            lerp(localX, values.samples[6], values.samples[7])));
        };
    }

    private JsonElement loadJson(String path) {
        String resource = RESOURCE_ROOT + path;
        try (InputStream stream = FFDModernWorldgenData.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing bundled 26.3 worldgen resource: " + resource);
            }
            return new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read bundled 26.3 worldgen resource: " + resource, exception);
        }
    }

    private static String normalizeId(String id) {
        return id.indexOf(':') >= 0 ? id : "minecraft:" + id;
    }

    private static String pathOf(String id) {
        int separator = id.indexOf(':');
        return separator >= 0 ? id.substring(separator + 1) : id;
    }

    public static final class FinalDensityComponents {
        private final FFDDensityFunction main;
        private final FFDDensityFunction noodleToggle;
        private final FFDDensityFunction noodleThickness;
        private final FFDDensityFunction noodleRidgeA;
        private final FFDDensityFunction noodleRidgeB;
        private final FFDDensityFunction function;

        private FinalDensityComponents(FFDDensityFunction main,
                                       FFDDensityFunction noodleToggle,
                                       FFDDensityFunction noodleThickness,
                                       FFDDensityFunction noodleRidgeA,
                                       FFDDensityFunction noodleRidgeB) {
            this.main = main;
            this.noodleToggle = noodleToggle;
            this.noodleThickness = noodleThickness;
            this.noodleRidgeA = noodleRidgeA;
            this.noodleRidgeB = noodleRidgeB;
            FFDDensityFunction interpolatedMain = interpolated(main);
            FFDDensityFunction interpolatedToggle = interpolated(noodleToggle);
            FFDDensityFunction interpolatedThickness = interpolated(noodleThickness);
            FFDDensityFunction interpolatedRidgeA = interpolated(noodleRidgeA);
            FFDDensityFunction interpolatedRidgeB = interpolated(noodleRidgeB);
            function = (x, y, z) -> {
                double mainValue = interpolatedMain.sample(x, y, z);
                double toggle = interpolatedToggle.sample(x, y, z);
                if (toggle >= -1000000.0D && toggle < 0.0D) {
                    return combine(mainValue, toggle, 0.0D, 0.0D, 0.0D);
                }
                return combine(mainValue, toggle,
                        interpolatedThickness.sample(x, y, z),
                        interpolatedRidgeA.sample(x, y, z),
                        interpolatedRidgeB.sample(x, y, z));
            };
        }

        public FFDDensityFunction main() {
            return main;
        }

        public FFDDensityFunction noodleToggle() {
            return noodleToggle;
        }

        public FFDDensityFunction noodleThickness() {
            return noodleThickness;
        }

        public FFDDensityFunction noodleRidgeA() {
            return noodleRidgeA;
        }

        public FFDDensityFunction noodleRidgeB() {
            return noodleRidgeB;
        }

        public FFDDensityFunction function() {
            return function;
        }

        public static double combine(double main, double toggle, double thickness,
                                     double ridgeA, double ridgeB) {
            double clampedMain = Math.max(-1.0D, Math.min(1.0D, main));
            double squeezedMain = clampedMain * 0.5D
                    - clampedMain * clampedMain * clampedMain / 24.0D;
            double noodle = toggle >= -1000000.0D && toggle < 0.0D
                    ? 64.0D
                    : thickness + 1.5D * Math.max(Math.abs(ridgeA), Math.abs(ridgeB));
            return Math.min(squeezedMain, noodle);
        }
    }

    public static final class VeinDensityComponents {
        private final FFDDensityFunction toggle;
        private final FFDDensityFunction ridgeA;
        private final FFDDensityFunction ridgeB;
        private final FFDDensityFunction gap;
        private final double ridgedOffset;
        private final FFDDensityFunction toggleFunction;
        private final FFDDensityFunction ridgedFunction;

        private VeinDensityComponents(FFDDensityFunction toggle,
                                      FFDDensityFunction ridgeA,
                                      FFDDensityFunction ridgeB,
                                      FFDDensityFunction gap,
                                      double ridgedOffset) {
            this.toggle = toggle;
            this.ridgeA = ridgeA;
            this.ridgeB = ridgeB;
            this.gap = gap;
            this.ridgedOffset = ridgedOffset;
            toggleFunction = interpolated(toggle);
            FFDDensityFunction interpolatedRidgeA = interpolated(ridgeA);
            FFDDensityFunction interpolatedRidgeB = interpolated(ridgeB);
            ridgedFunction = (x, y, z) -> combineRidged(
                    interpolatedRidgeA.sample(x, y, z),
                    interpolatedRidgeB.sample(x, y, z));
        }

        public FFDDensityFunction toggle() {
            return toggle;
        }

        public FFDDensityFunction ridgeA() {
            return ridgeA;
        }

        public FFDDensityFunction ridgeB() {
            return ridgeB;
        }

        public FFDDensityFunction gap() {
            return gap;
        }

        public FFDDensityFunction toggleFunction() {
            return toggleFunction;
        }

        public FFDDensityFunction ridgedFunction() {
            return ridgedFunction;
        }

        public double combineRidged(double ridgeA, double ridgeB) {
            return ridgedOffset + Math.max(Math.abs(ridgeA), Math.abs(ridgeB));
        }
    }

    private static double clampedMap(double value, double fromMin, double fromMax, double toMin, double toMax) {
        if (value <= fromMin) {
            return toMin;
        }
        if (value >= fromMax) {
            return toMax;
        }
        return toMin + (value - fromMin) / (fromMax - fromMin) * (toMax - toMin);
    }

    private static float lerp(float amount, float first, float second) {
        return first + amount * (second - first);
    }

    private static double lerp(double amount, double first, double second) {
        return first + amount * (second - first);
    }

    private static final class SampleCache {
        boolean valid;
        int x;
        int y;
        int z;
        double value;
    }

    private static final class TopSurfaceCache {
        boolean valid;
        int x;
        int z;
        int value;
    }

    private static final class InterpolationCache {
        boolean valid;
        int x;
        int y;
        int z;
        final double[] samples = new double[8];

        void fill(FFDDensityFunction input, int x, int y, int z) {
            this.valid = true;
            this.x = x;
            this.y = y;
            this.z = z;
            samples[0] = input.sample(x, y, z);
            samples[1] = input.sample(x + CELL_WIDTH, y, z);
            samples[2] = input.sample(x, y + CELL_HEIGHT, z);
            samples[3] = input.sample(x + CELL_WIDTH, y + CELL_HEIGHT, z);
            samples[4] = input.sample(x, y, z + CELL_WIDTH);
            samples[5] = input.sample(x + CELL_WIDTH, y, z + CELL_WIDTH);
            samples[6] = input.sample(x, y + CELL_HEIGHT, z + CELL_WIDTH);
            samples[7] = input.sample(x + CELL_WIDTH, y + CELL_HEIGHT, z + CELL_WIDTH);
        }
    }

    private interface Spline {
        float sample(int x, int y, int z);
    }

    private enum BinaryType {
        ADD, MUL, MIN, MAX;

        double apply(double first, double second) {
            switch (this) {
                case ADD:
                    return first + second;
                case MUL:
                    return first == 0.0D ? 0.0D : first * second;
                case MIN:
                    return Math.min(first, second);
                case MAX:
                    return Math.max(first, second);
                default:
                    throw new AssertionError(this);
            }
        }
    }

    private enum MappedType {
        ABS, SQUARE, CUBE, HALF_NEGATIVE, QUARTER_NEGATIVE, INVERT, SQUEEZE;

        double apply(double value) {
            switch (this) {
                case ABS:
                    return Math.abs(value);
                case SQUARE:
                    return value * value;
                case CUBE:
                    return value * value * value;
                case HALF_NEGATIVE:
                    return value > 0.0D ? value : value * 0.5D;
                case QUARTER_NEGATIVE:
                    return value > 0.0D ? value : value * 0.25D;
                case INVERT:
                    return 1.0D / value;
                case SQUEEZE:
                    double clamped = Math.max(-1.0D, Math.min(1.0D, value));
                    return clamped * 0.5D - clamped * clamped * clamped / 24.0D;
                default:
                    throw new AssertionError(this);
            }
        }
    }
}
