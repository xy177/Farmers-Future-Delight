package xy177.farmersfuturedelight.common.world.terrain;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeSampler;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeZoomer;
import xy177.farmersfuturedelight.common.world.noise.FFDXoroshiroRandom;
import xy177.farmersfuturedelight.common.worldgen.FFDModernStoneProvider;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Evaluates the bundled 26.3 material-rule graph against the extended 1.12
 * chunk. Missing modern blocks are left as stone so optional backport mods can
 * provide them without forcing a hard dependency.
 */
public final class FFDModernSurfaceRules {
    private static final String RESOURCE_ROOT =
            "/assets/farmers_future_delight/worldgen/26_3/";
    private static final Rule NO_RULE = context -> null;

    private final World world;
    private final FFDModernWorldgenData data;
    private final FFDVerticalBiomeSampler verticalBiomeSampler;
    private final FFDModernStoneProvider stones;
    private final long biomeZoomSeed;
    private final Map<String, Rule> rules = new HashMap<>();
    private final Map<String, Condition> conditions = new HashMap<>();
    private final Map<String, IBlockState> blockStates = new HashMap<>();
    private final IBlockState[] clayBands;
    private final FFDXoroshiroRandom.PositionalFactory deepslateRandom;
    private final Rule overworldRule;

    public FFDModernSurfaceRules(World world, FFDModernWorldgenData data,
                                 FFDVerticalBiomeSampler verticalBiomeSampler,
                                 FFDModernStoneProvider stones) {
        this.world = world;
        this.data = data;
        this.verticalBiomeSampler = verticalBiomeSampler;
        this.stones = stones;
        biomeZoomSeed = FFDVerticalBiomeZoomer.obfuscateSeed(world.getSeed());
        clayBands = generateClayBands(data.randomFromHashOf("minecraft:clay_bands"));
        deepslateRandom = data.positionalFactory("minecraft:deepslate");
        overworldRule = ruleReference("minecraft:overworld");
    }

    public IBlockState stoneStateAt(int x, int y, int z) {
        IBlockState deepslate = blockStates.get("minecraft:deepslate");
        return deepslate != null && isDeepslateAt(x, y, z)
                ? deepslate : Blocks.STONE.getDefaultState();
    }

    public void apply(Chunk chunk, Biome[] biomes, FFDVerticalBiomeData verticalBiomes,
                      FFDBiomeTerrainBridge terrainBridge, int startX, int startZ) {
        SurfaceStateReader stateReader = new SurfaceStateReader(chunk);
        int[] heights = worldSurfaceHeights(stateReader);
        Context context = new Context(chunk, biomes, verticalBiomes, terrainBridge,
                heights, startX, startZ);

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                context.updateXZ(localX, localZ);
                int stoneDepthAbove = 0;
                int waterHeight = Integer.MIN_VALUE;
                int nextCeilingStoneY = Integer.MAX_VALUE;
                int top = heights[index(localX, localZ)] + 1;

                for (int y = top; y >= FFDModernWorldgenData.MIN_Y; y--) {
                    IBlockState old = stateReader.get(localX, y, localZ);
                    if (isAir(old)) {
                        stoneDepthAbove = 0;
                        waterHeight = Integer.MIN_VALUE;
                        continue;
                    }
                    if (isFluid(old)) {
                        if (waterHeight == Integer.MIN_VALUE) {
                            waterHeight = y + 1;
                        }
                        continue;
                    }
                    if (nextCeilingStoneY >= y) {
                        nextCeilingStoneY = FFDModernWorldgenData.MIN_Y - 4096;
                        for (int lookaheadY = y - 1; lookaheadY >= FFDModernWorldgenData.MIN_Y - 1;
                             lookaheadY--) {
                            IBlockState below = stateReader.get(localX, lookaheadY, localZ);
                            if (isStoneLike(below)) {
                                continue;
                            }
                            nextCeilingStoneY = lookaheadY + 1;
                            break;
                        }
                    }

                    int stoneDepthBelow = y - nextCeilingStoneY + 1;
                    context.updateY(++stoneDepthAbove, stoneDepthBelow, waterHeight, y);
                    if (!isSurfaceBase(old)) {
                        continue;
                    }
                    IBlockState replacement = overworldRule.apply(context);
                    if (replacement != null && replacement != old) {
                        setDirect(chunk, localX, y, localZ, replacement);
                    }
                }
            }
        }
    }

    private Rule parseRule(JsonElement element) {
        if (element.isJsonPrimitive()) {
            return ruleReference(element.getAsString());
        }
        JsonObject json = element.getAsJsonObject();
        String type = pathOf(json.get("type").getAsString());
        switch (type) {
            case "sequence": {
                JsonArray sequence = json.getAsJsonArray("sequence");
                List<Rule> parsed = new ArrayList<>(sequence.size());
                for (JsonElement child : sequence) {
                    parsed.add(parseRule(child));
                }
                return context -> {
                    for (Rule rule : parsed) {
                        IBlockState state = rule.apply(context);
                        if (state != null) {
                            return state;
                        }
                    }
                    return null;
                };
            }
            case "condition": {
                Condition condition = parseCondition(json.get("if_true"));
                Rule nested = parseRule(json.get("then_run"));
                return context -> condition.test(context) ? nested.apply(context) : null;
            }
            case "block": {
                IBlockState state = resolveBlockState(json.getAsJsonObject("result_state"));
                return state == null ? NO_RULE : context -> state;
            }
            case "bandlands":
                return context -> band(context.blockX, context.blockY, context.blockZ);
            default:
                throw new IllegalArgumentException("Unsupported 26.3 material rule: " + type);
        }
    }

    private Condition parseCondition(JsonElement element) {
        if (element.isJsonPrimitive()) {
            return conditionReference(element.getAsString());
        }
        JsonObject json = element.getAsJsonObject();
        String type = pathOf(json.get("type").getAsString());
        switch (type) {
            case "biome": {
                Set<String> targets = new HashSet<>();
                JsonElement target = json.get("biome_is");
                if (target.isJsonArray()) {
                    for (JsonElement entry : target.getAsJsonArray()) {
                        targets.add(normalizeId(entry.getAsString()));
                    }
                } else {
                    targets.add(normalizeId(target.getAsString()));
                }
                return context -> context.matchesAnyBiome(targets);
            }
            case "noise_threshold": {
                String noise = normalizeId(json.get("noise").getAsString());
                double min = json.get("min_threshold").getAsDouble();
                double max = json.get("max_threshold").getAsDouble();
                boolean is3d = json.has("is_3d") && json.get("is_3d").getAsBoolean();
                return context -> {
                    double value = context.noise(noise, is3d);
                    return value >= min && value <= max;
                };
            }
            case "y_above": {
                int anchor = resolveAnchor(json.getAsJsonObject("anchor"));
                int multiplier = json.get("surface_depth_multiplier").getAsInt();
                boolean addStoneDepth = json.get("add_stone_depth").getAsBoolean();
                return context -> context.blockY + (addStoneDepth ? context.stoneDepthAbove : 0)
                        >= anchor + context.surfaceDepth * multiplier;
            }
            case "water": {
                int offset = json.get("offset").getAsInt();
                int multiplier = json.get("surface_depth_multiplier").getAsInt();
                boolean addStoneDepth = json.get("add_stone_depth").getAsBoolean();
                return context -> context.waterHeight == Integer.MIN_VALUE
                        || context.blockY + (addStoneDepth ? context.stoneDepthAbove : 0)
                        >= context.waterHeight + offset + context.surfaceDepth * multiplier;
            }
            case "stone_depth": {
                int offset = json.get("offset").getAsInt();
                boolean addSurfaceDepth = json.get("add_surface_depth").getAsBoolean();
                int secondaryRange = json.get("secondary_depth_range").getAsInt();
                boolean ceiling = "ceiling".equals(pathOf(json.get("surface_type").getAsString()));
                return context -> {
                    int stoneDepth = ceiling ? context.stoneDepthBelow : context.stoneDepthAbove;
                    int surfaceDepth = addSurfaceDepth ? context.surfaceDepth : 0;
                    int secondaryDepth = secondaryRange == 0 ? 0
                            : (int) ((context.surfaceSecondary + 1.0D) * 0.5D * secondaryRange);
                    return stoneDepth <= 1 + offset + surfaceDepth + secondaryDepth;
                };
            }
            case "vertical_gradient": {
                String randomName = normalizeId(json.get("random_name").getAsString());
                if ("minecraft:deepslate".equals(randomName)) {
                    return context -> isDeepslateAt(context.blockX, context.blockY, context.blockZ);
                }
                int trueAtAndBelow = resolveAnchor(json.getAsJsonObject("true_at_and_below"));
                int falseAtAndAbove = resolveAnchor(json.getAsJsonObject("false_at_and_above"));
                FFDXoroshiroRandom.PositionalFactory randomFactory = data.positionalFactory(randomName);
                return context -> {
                    if (context.blockY <= trueAtAndBelow) {
                        return true;
                    }
                    if (context.blockY >= falseAtAndAbove) {
                        return false;
                    }
                    double probability = (falseAtAndAbove - context.blockY)
                            / (double) (falseAtAndAbove - trueAtAndBelow);
                    return randomFactory.at(context.blockX, context.blockY, context.blockZ).nextFloat()
                            < probability;
                };
            }
            case "not": {
                Condition inverted = parseCondition(json.get("invert"));
                return context -> !inverted.test(context);
            }
            case "above_preliminary_surface":
                return context -> context.blockY >= context.minSurfaceLevel;
            case "hole":
                return context -> context.surfaceDepth <= 0;
            case "steep":
                return Context::isSteep;
            case "temperature":
                return context -> context.biome.getTemperature(
                        new BlockPos(context.blockX, context.blockY, context.blockZ)) < 0.15F;
            default:
                throw new IllegalArgumentException("Unsupported 26.3 material condition: " + type);
        }
    }

    private Rule ruleReference(String id) {
        String normalized = normalizeId(id);
        if (normalized.contains("sulfur")) {
            return NO_RULE;
        }
        if ("minecraft:overworld/biome_surface/default".equals(normalized)) {
            return context -> context.stoneDepthAbove == 1 && context.notUnderwater()
                    ? context.defaultTopState() : context.defaultFillerState();
        }
        if ("minecraft:overworld/under_biome_surface/default".equals(normalized)) {
            return Context::defaultFillerState;
        }
        Rule cached = rules.get(normalized);
        if (cached != null) {
            return cached;
        }
        Rule parsed = parseRule(loadJson("material_rule/" + pathOf(normalized) + ".json"));
        rules.put(normalized, parsed);
        return parsed;
    }

    private Condition conditionReference(String id) {
        String normalized = normalizeId(id);
        Condition cached = conditions.get(normalized);
        if (cached != null) {
            return cached;
        }
        Condition parsed = parseCondition(loadJson("material_condition/" + pathOf(normalized) + ".json"));
        conditions.put(normalized, parsed);
        return parsed;
    }

    @Nullable
    private IBlockState resolveBlockState(JsonObject json) {
        String id = normalizeId(json.get("Name").getAsString());
        IBlockState cached = blockStates.get(id);
        if (cached != null) {
            return cached;
        }

        IBlockState state;
        switch (pathOf(id)) {
            case "air":
                state = Blocks.AIR.getDefaultState();
                break;
            case "bedrock":
                state = Blocks.BEDROCK.getDefaultState();
                break;
            case "stone":
                state = Blocks.STONE.getDefaultState();
                break;
            case "grass_block":
                state = Blocks.GRASS.getDefaultState();
                break;
            case "dirt":
                state = Blocks.DIRT.getDefaultState();
                break;
            case "coarse_dirt":
                state = Blocks.DIRT.getStateFromMeta(1);
                break;
            case "podzol":
                state = Blocks.DIRT.getStateFromMeta(2);
                break;
            case "mycelium":
                state = Blocks.MYCELIUM.getDefaultState();
                break;
            case "gravel":
                state = Blocks.GRAVEL.getDefaultState();
                break;
            case "sand":
                state = Blocks.SAND.getDefaultState();
                break;
            case "red_sand":
                state = Blocks.SAND.getStateFromMeta(1);
                break;
            case "sandstone":
                state = Blocks.SANDSTONE.getDefaultState();
                break;
            case "red_sandstone":
                state = Blocks.RED_SANDSTONE.getDefaultState();
                break;
            case "terracotta":
                state = Blocks.HARDENED_CLAY.getDefaultState();
                break;
            case "white_terracotta":
                state = dyedTerracotta(0);
                break;
            case "orange_terracotta":
                state = dyedTerracotta(1);
                break;
            case "snow_block":
                state = Blocks.SNOW.getDefaultState();
                break;
            case "ice":
                state = Blocks.ICE.getDefaultState();
                break;
            case "packed_ice":
                state = Blocks.PACKED_ICE.getDefaultState();
                break;
            case "water":
                state = Blocks.WATER.getDefaultState();
                break;
            case "deepslate":
                state = stones.deepslate();
                break;
            case "calcite":
                state = FFDItems.isBlockRegistered(FFDBlocks.CALCITE)
                        ? FFDBlocks.CALCITE.getDefaultState()
                        : FFDCompat.getExternalBlockState(FFDCompat.Feature.AMETHYST,
                                "calcite");
                break;
            case "mud":
                state = optionalState("deeperdepths:mud", "depthsupdate:mud", "futuremc:mud");
                break;
            case "powder_snow":
                state = FFDItems.isBlockRegistered(FFDBlocks.POWDER_SNOW)
                        ? FFDBlocks.POWDER_SNOW.getDefaultState()
                        : FFDCompat.getExternalBlockState(FFDCompat.Feature.POWDER_SNOW,
                                "powder_snow");
                break;
            case "sulfur":
            case "cinnabar":
                state = null;
                break;
            default:
                state = registryState(id);
                break;
        }
        if (state != null) {
            blockStates.put(id, state);
        }
        return state;
    }

    private boolean isDeepslateAt(int x, int y, int z) {
        int minimum = FFDConfig.deepslateTransitionMinY;
        int maximum = FFDConfig.deepslateTransitionMaxY;
        if (y <= minimum) {
            return true;
        }
        if (y >= maximum || maximum <= minimum) {
            return false;
        }
        double probability = (maximum - y) / (double) (maximum - minimum);
        return deepslateRandom.at(x, y, z).nextFloat() < probability;
    }

    private boolean isSurfaceBase(IBlockState state) {
        if (state.getBlock() == Blocks.STONE) {
            return true;
        }
        return stones.isDeepBase(state);
    }

    @Nullable
    private static IBlockState optionalState(String... ids) {
        for (String id : ids) {
            IBlockState state = registryState(id);
            if (state != null) {
                return state;
            }
        }
        return null;
    }

    @Nullable
    private static IBlockState registryState(String id) {
        ResourceLocation key = new ResourceLocation(id);
        Block block = Block.REGISTRY.getObject(key);
        ResourceLocation registered = Block.REGISTRY.getNameForObject(block);
        return key.equals(registered) ? block.getDefaultState() : null;
    }

    private IBlockState band(int x, int y, int z) {
        int offset = (int) Math.round(data.sampleNoise("minecraft:clay_bands_offset", x, 0, z) * 4.0D);
        return clayBands[Math.floorMod(y + offset, clayBands.length)];
    }

    private static IBlockState[] generateClayBands(FFDXoroshiroRandom random) {
        IBlockState terracotta = Blocks.HARDENED_CLAY.getDefaultState();
        IBlockState[] bands = new IBlockState[192];
        Arrays.fill(bands, terracotta);
        for (int i = 0; i < bands.length; i++) {
            i += random.nextInt(5) + 1;
            if (i < bands.length) {
                bands[i] = dyedTerracotta(1);
            }
        }
        makeBands(random, bands, 1, dyedTerracotta(4));
        makeBands(random, bands, 2, dyedTerracotta(12));
        makeBands(random, bands, 1, dyedTerracotta(14));
        int whiteBandCount = 9 + random.nextInt(7);
        for (int i = 0, start = 0; i < whiteBandCount && start < bands.length;
             i++, start += random.nextInt(16) + 4) {
            bands[start] = dyedTerracotta(0);
            if (start - 1 > 0 && random.nextBoolean()) {
                bands[start - 1] = dyedTerracotta(8);
            }
            if (start + 1 < bands.length && random.nextBoolean()) {
                bands[start + 1] = dyedTerracotta(8);
            }
        }
        return bands;
    }

    private static void makeBands(FFDXoroshiroRandom random, IBlockState[] bands,
                                  int baseWidth, IBlockState state) {
        int count = 6 + random.nextInt(10);
        for (int i = 0; i < count; i++) {
            int width = baseWidth + random.nextInt(3);
            int start = random.nextInt(bands.length);
            for (int p = 0; start + p < bands.length && p < width; p++) {
                bands[start + p] = state;
            }
        }
    }

    private static IBlockState dyedTerracotta(int metadata) {
        return Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(metadata);
    }

    private int resolveAnchor(JsonObject anchor) {
        if (anchor.has("absolute")) {
            return anchor.get("absolute").getAsInt();
        }
        if (anchor.has("above_bottom")) {
            return FFDModernWorldgenData.MIN_Y + anchor.get("above_bottom").getAsInt();
        }
        if (anchor.has("below_top")) {
            return FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1
                    - anchor.get("below_top").getAsInt();
        }
        throw new IllegalArgumentException("Unsupported vertical anchor: " + anchor);
    }

    private JsonElement loadJson(String path) {
        String resource = RESOURCE_ROOT + path;
        try (InputStream stream = FFDModernSurfaceRules.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing bundled 26.3 material resource: " + resource);
            }
            return new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read bundled 26.3 material resource: " + resource,
                    exception);
        }
    }

    private static int[] worldSurfaceHeights(SurfaceStateReader stateReader) {
        int[] heights = new int[256];
        int maxY = FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT - 1;
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int height = FFDModernWorldgenData.MIN_Y - 1;
                for (int y = maxY; y >= FFDModernWorldgenData.MIN_Y; y--) {
                    if (!isAir(stateReader.get(localX, y, localZ))) {
                        height = y;
                        break;
                    }
                }
                heights[index(localX, localZ)] = height;
            }
        }
        return heights;
    }

    private void setDirect(Chunk chunk, int localX, int y, int localZ, IBlockState state) {
        int storageIndex = FFDHeightHooks.storageIndex(y, world);
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        if (storageIndex < 0 || storageIndex >= storage.length) {
            return;
        }
        ExtendedBlockStorage section = storage[storageIndex];
        if (section == Chunk.NULL_BLOCK_STORAGE) {
            section = new ExtendedBlockStorage(y >> 4 << 4, world.provider.hasSkyLight());
            storage[storageIndex] = section;
        }
        section.set(localX, y & 15, localZ, state);
    }

    private static final class SurfaceStateReader {
        private static final int SECTION_COUNT = FFDModernWorldgenData.HEIGHT >> 4;

        private final ExtendedBlockStorage[] sections = new ExtendedBlockStorage[SECTION_COUNT];

        private SurfaceStateReader(Chunk chunk) {
            ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
            for (int logicalSection = 0; logicalSection < SECTION_COUNT; logicalSection++) {
                int y = FFDModernWorldgenData.MIN_Y + (logicalSection << 4);
                int storageIndex = FFDHeightHooks.storageIndexForChunk(y, chunk);
                if (storageIndex >= 0 && storageIndex < storage.length) {
                    sections[logicalSection] = storage[storageIndex];
                }
            }
        }

        private IBlockState get(int localX, int y, int localZ) {
            int logicalSection = (y - FFDModernWorldgenData.MIN_Y) >> 4;
            if (logicalSection < 0 || logicalSection >= sections.length) {
                return Blocks.AIR.getDefaultState();
            }
            ExtendedBlockStorage section = sections[logicalSection];
            return section == Chunk.NULL_BLOCK_STORAGE
                    ? Blocks.AIR.getDefaultState()
                    : section.get(localX, y & 15, localZ);
        }
    }

    private static boolean isAir(IBlockState state) {
        return state == null || state.getBlock() == Blocks.AIR;
    }

    private static boolean isFluid(IBlockState state) {
        if (state == null) {
            return false;
        }
        Block block = state.getBlock();
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER
                || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
    }

    private static boolean isStoneLike(IBlockState state) {
        return !isAir(state) && !isFluid(state);
    }

    private static int index(int localX, int localZ) {
        return localX + (localZ << 4);
    }

    private static String normalizeId(String id) {
        return id.indexOf(':') >= 0 ? id : "minecraft:" + id;
    }

    private static String pathOf(String id) {
        int separator = id.indexOf(':');
        return separator >= 0 ? id.substring(separator + 1) : id;
    }

    private interface Rule {
        @Nullable
        IBlockState apply(Context context);
    }

    private interface Condition {
        boolean test(Context context);
    }

    private final class Context {
        private final Chunk chunk;
        private final Biome[] biomes;
        private final FFDVerticalBiomeData verticalBiomes;
        private final FFDBiomeTerrainBridge terrainBridge;
        private final int[] heights;
        private final int[] preliminarySurfaceCache = new int[4];
        private final Map<Long, FFDVerticalBiomeData> verticalBiomeChunks = new HashMap<>();
        private final FFDVerticalBiomeZoomer.Source verticalBiomeSource;
        private final int startX;
        private final int startZ;
        private final Map<String, Double> noise2d = new HashMap<>();
        private final Map<String, Double> noise3d = new HashMap<>();

        private int localX;
        private int localZ;
        private int blockX;
        private int blockZ;
        private int blockY;
        private int surfaceDepth;
        private double surfaceSecondary;
        private int minSurfaceLevel;
        private int waterHeight;
        private int stoneDepthBelow;
        private int stoneDepthAbove;
        private Biome biome;
        private String biomeId;
        private FFDVerticalBiome verticalBiome;
        private int preliminarySurfaceCellX = Integer.MIN_VALUE;
        private int preliminarySurfaceCellZ = Integer.MIN_VALUE;

        private Context(Chunk chunk, Biome[] biomes, FFDVerticalBiomeData verticalBiomes,
                        FFDBiomeTerrainBridge terrainBridge, int[] heights,
                        int startX, int startZ) {
            this.chunk = chunk;
            this.biomes = biomes;
            this.verticalBiomes = verticalBiomes;
            this.terrainBridge = terrainBridge;
            this.heights = heights;
            this.startX = startX;
            this.startZ = startZ;
            verticalBiomeChunks.put(chunkKey(startX >> 4, startZ >> 4), verticalBiomes);
            verticalBiomeSource = this::rawVerticalBiome;
        }

        private void updateXZ(int localX, int localZ) {
            this.localX = localX;
            this.localZ = localZ;
            blockX = startX + localX;
            blockZ = startZ + localZ;
            biome = biomes[index(localX, localZ)];
            ResourceLocation registryName = biome.getRegistryName();
            biomeId = registryName == null ? "" : registryName.toString();
            noise2d.clear();
            noise3d.clear();
            double surface = data.sampleNoise("minecraft:surface", blockX, 0, blockZ);
            surfaceDepth = (int) (surface * 2.75D + 3.0D
                    + data.positionalRandomAt(blockX, 0, blockZ).nextDouble() * 0.25D);
            surfaceSecondary = data.sampleNoise("minecraft:surface_secondary", blockX, 0, blockZ);
            minSurfaceLevel = interpolatedPreliminarySurface(blockX, blockZ) + surfaceDepth - 8;
        }

        private void updateY(int stoneDepthAbove, int stoneDepthBelow, int waterHeight, int blockY) {
            this.stoneDepthAbove = stoneDepthAbove;
            this.stoneDepthBelow = stoneDepthBelow;
            this.waterHeight = waterHeight;
            this.blockY = blockY;
            FFDVerticalBiome sampledBiome = FFDVerticalBiomeZoomer.getBiome(
                    biomeZoomSeed, blockX, blockY, blockZ, verticalBiomeSource);
            verticalBiome = sampledBiome;
            noise3d.clear();
        }

        private FFDVerticalBiome rawVerticalBiome(int quartX, int quartY, int quartZ) {
            int chunkX = Math.floorDiv(quartX, FFDVerticalBiomeData.CELLS_X);
            int chunkZ = Math.floorDiv(quartZ, FFDVerticalBiomeData.CELLS_Z);
            long key = chunkKey(chunkX, chunkZ);
            FFDVerticalBiomeData data = verticalBiomeChunks.get(key);
            if (data == null) {
                data = verticalBiomeSampler.sampleChunk(chunkX, chunkZ);
                verticalBiomeChunks.put(key, data);
            }
            return data.get(quartX * FFDVerticalBiomeData.CELL_SIZE,
                    quartY * FFDVerticalBiomeData.CELL_SIZE,
                    quartZ * FFDVerticalBiomeData.CELL_SIZE);
        }

        private int interpolatedPreliminarySurface(int x, int z) {
            int cellX = x >> 4;
            int cellZ = z >> 4;
            if (cellX != preliminarySurfaceCellX || cellZ != preliminarySurfaceCellZ) {
                preliminarySurfaceCellX = cellX;
                preliminarySurfaceCellZ = cellZ;
                int cornerX = cellX << 4;
                int cornerZ = cellZ << 4;
                preliminarySurfaceCache[0] = data.preliminarySurfaceLevel(cornerX, cornerZ);
                preliminarySurfaceCache[1] = data.preliminarySurfaceLevel(cornerX + 16, cornerZ);
                preliminarySurfaceCache[2] = data.preliminarySurfaceLevel(cornerX, cornerZ + 16);
                preliminarySurfaceCache[3] = data.preliminarySurfaceLevel(cornerX + 16, cornerZ + 16);
            }
            double fractionX = (x & 15) / 16.0F;
            double fractionZ = (z & 15) / 16.0F;
            double north = lerp(fractionX, preliminarySurfaceCache[0], preliminarySurfaceCache[1]);
            double south = lerp(fractionX, preliminarySurfaceCache[2], preliminarySurfaceCache[3]);
            return (int) Math.floor(lerp(fractionZ, north, south));
        }

        private double noise(String id, boolean is3d) {
            Map<String, Double> cache = is3d ? noise3d : noise2d;
            Double value = cache.get(id);
            if (value == null) {
                value = data.sampleNoise(id, blockX, is3d ? blockY : 0, blockZ);
                cache.put(id, value);
            }
            return value;
        }

        private boolean notUnderwater() {
            return waterHeight == Integer.MIN_VALUE || blockY >= waterHeight - 1;
        }

        private IBlockState defaultTopState() {
            return verticalBiome == FFDVerticalBiome.MEADOW
                    ? Blocks.GRASS.getDefaultState() : biome.topBlock;
        }

        private IBlockState defaultFillerState() {
            return verticalBiome == FFDVerticalBiome.MEADOW
                    ? Blocks.DIRT.getDefaultState() : biome.fillerBlock;
        }

        private boolean isSteep() {
            int north = heights[index(localX, Math.max(localZ - 1, 0))];
            int south = heights[index(localX, Math.min(localZ + 1, 15))];
            if (south >= north + 4) {
                return true;
            }
            int west = heights[index(Math.max(localX - 1, 0), localZ)];
            int east = heights[index(Math.min(localX + 1, 15), localZ)];
            return west >= east + 4;
        }

        private boolean matchesAnyBiome(Set<String> targets) {
            for (String target : targets) {
                if (matchesBiome(target)) {
                    return true;
                }
            }
            return false;
        }

        private boolean matchesBiome(String target) {
            if (target.contains("sulfur")) {
                return false;
            }
            FFDVerticalBiome targetBiome = verticalBiome(target);
            if (targetBiome != null) {
                return verticalBiome == targetBiome;
            }
            return target.startsWith("minecraft:")
                    ? terrainBridge.matchesModernBiome(blockX, blockZ, target)
                    : target.equals(biomeId);
        }

        @Nullable
        private FFDVerticalBiome verticalBiome(String target) {
            if (!target.startsWith("minecraft:")) {
                return null;
            }
            switch (pathOf(target)) {
                case "lush_caves":
                    return FFDVerticalBiome.LUSH_CAVES;
                case "dripstone_caves":
                    return FFDVerticalBiome.DRIPSTONE_CAVES;
                case "meadow":
                    return FFDVerticalBiome.MEADOW;
                case "grove":
                    return FFDVerticalBiome.GROVE;
                case "snowy_slopes":
                    return FFDVerticalBiome.SNOWY_SLOPES;
                case "jagged_peaks":
                    return FFDVerticalBiome.JAGGED_PEAKS;
                case "frozen_peaks":
                    return FFDVerticalBiome.FROZEN_PEAKS;
                case "stony_peaks":
                    return FFDVerticalBiome.STONY_PEAKS;
                default:
                    return null;
            }
        }
    }

    private static double lerp(double amount, double first, double second) {
        return first + amount * (second - first);
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return chunkX & 0xFFFFFFFFL | (long) chunkZ << 32;
    }
}
