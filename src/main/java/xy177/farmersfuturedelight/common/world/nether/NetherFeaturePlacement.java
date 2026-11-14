package xy177.farmersfuturedelight.common.world.nether;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;

final class NetherFeaturePlacement {
    interface Feature { void place(Context context, Random random, BlockPos pos); }
    interface Placement { void positions(Context context, Random random, BlockPos pos, Consumer<BlockPos> output); }
    interface Predicate { boolean test(Context context, BlockPos pos); }
    interface StateProvider { IBlockState get(Context context, Random random, BlockPos pos); }
    interface IntProvider { int sample(Random random); }

    static final class Context {
        final World world;
        final NetherBiomeSource biomes;
        final String feature;
        final Map<FFDVerticalBiome, Set<String>> biomeFeatures;

        Context(World world, NetherBiomeSource biomes, String feature, Map<FFDVerticalBiome, Set<String>> biomeFeatures) {
            this.world = world;
            this.biomes = biomes;
            this.feature = feature;
            this.biomeFeatures = biomeFeatures;
        }

        boolean available(BlockPos pos) {
            return pos.getY() >= 0 && pos.getY() < NetherGeneration.BUILD_HEIGHT && world.isBlockLoaded(pos, false);
        }

        IBlockState get(BlockPos pos) { return available(pos) ? world.getBlockState(pos) : Blocks.AIR.getDefaultState(); }
        void set(BlockPos pos, IBlockState state) { if (state != null && available(pos)) world.setBlockState(pos, state, 2 | 16); }

        boolean matchesBiome(BlockPos pos) {
            Set<String> features = biomeFeatures.get(biomes.get(pos.getX(), pos.getY(), pos.getZ()));
            return features != null && features.contains(feature);
        }
    }

    private final Map<String, Feature> features = new HashMap<>();

    Feature placed(JsonObject definition) {
        Feature feature = feature(definition.get("feature"));
        List<Placement> placements = new ArrayList<>();
        for (JsonElement placement : definition.getAsJsonArray("placement")) placements.add(placement(placement.getAsJsonObject()));
        return (context, random, pos) -> place(placements, 0, feature, context, random, pos);
    }

    Feature feature(JsonElement definition) {
        if (definition.isJsonPrimitive()) {
            String name = path(definition.getAsString());
            Feature result = features.get(name);
            if (result == null) {
                result = NetherFeatures.compile(NetherMaterialNoise.resource("feature/" + name), this);
                features.put(name, result);
            }
            return result;
        }
        return NetherFeatures.compile(definition.getAsJsonObject(), this);
    }

    private static void place(List<Placement> placements, int index, Feature feature, Context context, Random random, BlockPos pos) {
        if (index == placements.size()) {
            if (context.available(pos)) feature.place(context, random, pos);
        } else placements.get(index).positions(context, random, pos,
                candidate -> place(placements, index + 1, feature, context, random, candidate));
    }

    private Placement placement(JsonObject definition) {
        String type = path(definition.get("type").getAsString());
        switch (type) {
            case "count": {
                IntProvider count = integer(definition.get("count"));
                return (context, random, pos, output) -> {
                    int attempts = count.sample(random);
                    for (int attempt = 0; attempt < attempts; attempt++) output.accept(pos);
                };
            }
            case "in_square": return (context, random, pos, output) -> output.accept(pos.add(random.nextInt(16), 0, random.nextInt(16)));
            case "height_range": {
                IntProvider height = height(definition.getAsJsonObject("height"));
                return (context, random, pos, output) -> output.accept(new BlockPos(pos.getX(), height.sample(random), pos.getZ()));
            }
            case "heightmap": return (context, random, pos, output) -> output.accept(context.world.getHeight(pos));
            case "biome": return (context, random, pos, output) -> { if (context.matchesBiome(pos)) output.accept(pos); };
            case "rarity_filter": {
                int chance = definition.get("chance").getAsInt();
                return (context, random, pos, output) -> { if (random.nextFloat() < 1.0F / chance) output.accept(pos); };
            }
            case "block_predicate_filter": {
                Predicate predicate = predicate(definition.getAsJsonObject("predicate"));
                return (context, random, pos, output) -> { if (predicate.test(context, pos)) output.accept(pos); };
            }
            case "offset": {
                IntProvider offsetX = integer(definition.get("x"));
                IntProvider offsetY = integer(definition.get("y"));
                IntProvider offsetZ = integer(definition.get("z"));
                return (context, random, pos, output) -> output.accept(pos.add(offsetX.sample(random), offsetY.sample(random), offsetZ.sample(random)));
            }
            case "environment_scan": {
                EnumFacing direction = EnumFacing.byName(definition.get("direction_of_search").getAsString());
                int steps = definition.get("max_steps").getAsInt();
                Predicate target = predicate(definition.getAsJsonObject("target_condition"));
                Predicate allowed = definition.has("allowed_search_condition") ? predicate(definition.getAsJsonObject("allowed_search_condition")) : (context, pos) -> true;
                return (context, random, pos, output) -> {
                    BlockPos cursor = pos;
                    if (!allowed.test(context, cursor)) return;
                    for (int step = 0; step < steps; step++) {
                        if (target.test(context, cursor)) { output.accept(cursor); return; }
                        cursor = cursor.offset(direction);
                        if (!context.available(cursor) || !allowed.test(context, cursor)) return;
                    }
                    if (target.test(context, cursor)) output.accept(cursor);
                };
            }
            case "count_on_every_layer": {
                IntProvider count = integer(definition.get("count"));
                return (context, random, pos, output) -> {
                    int layer = 0;
                    boolean found;
                    do {
                        found = false;
                        for (int attempt = 0; attempt < count.sample(random); attempt++) {
                            int blockX = pos.getX() + random.nextInt(16);
                            int blockZ = pos.getZ() + random.nextInt(16);
                            int startY = context.world.getHeight(blockX, blockZ);
                            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(blockX, startY, blockZ);
                            IBlockState current = context.get(cursor);
                            int currentLayer = 0;
                            for (int blockY = startY; blockY > 0; blockY--) {
                                cursor.setY(blockY - 1);
                                IBlockState below = context.get(cursor);
                                if (!empty(below) && empty(current) && below.getBlock() != Blocks.BEDROCK && currentLayer++ == layer) {
                                    output.accept(cursor.up());
                                    found = true;
                                    break;
                                }
                                current = below;
                            }
                        }
                        layer++;
                    } while (found);
                };
            }
            default: throw new IllegalArgumentException("Unknown Nether placement: " + type);
        }
    }

    Predicate predicate(JsonObject definition) {
        String type = path(definition.get("type").getAsString());
        Predicate result;
        switch (type) {
            case "all_of":
            case "any_of": {
                List<Predicate> predicates = new ArrayList<>();
                for (JsonElement entry : definition.getAsJsonArray("predicates")) predicates.add(predicate(entry.getAsJsonObject()));
                boolean all = "all_of".equals(type);
                result = (context, pos) -> {
                    for (Predicate predicate : predicates) if (predicate.test(context, pos) != all) return !all;
                    return all;
                };
                break;
            }
            case "not": {
                Predicate original = predicate(definition.getAsJsonObject("predicate"));
                result = (context, pos) -> !original.test(context, pos);
                break;
            }
            case "matching_blocks": {
                Set<Block> blocks = blocks(definition.get("blocks"));
                result = (context, pos) -> blocks.contains(context.get(pos).getBlock());
                break;
            }
            case "matching_block_tag": {
                String tag = path(definition.get("tag").getAsString());
                if ("air".equals(tag)) result = (context, pos) -> context.get(pos).getMaterial() == Material.AIR;
                else {
                    Set<Block> blocks = tag(tag);
                    result = (context, pos) -> blocks.contains(context.get(pos).getBlock());
                }
                break;
            }
            case "height_range": {
                int minimum = anchor(definition.get("min_inclusive"));
                int maximum = anchor(definition.get("max_inclusive"));
                result = (context, pos) -> pos.getY() >= minimum && pos.getY() <= maximum;
                break;
            }
            case "true": result = (context, pos) -> true; break;
            default: throw new IllegalArgumentException("Unknown Nether block predicate: " + type);
        }
        if (!definition.has("offset")) return result;
        JsonArray offset = definition.getAsJsonArray("offset");
        int offsetX = offset.get(0).getAsInt();
        int offsetY = offset.get(1).getAsInt();
        int offsetZ = offset.get(2).getAsInt();
        Predicate original = result;
        return (context, pos) -> original.test(context, pos.add(offsetX, offsetY, offsetZ));
    }

    StateProvider provider(JsonElement definition) {
        if (definition.isJsonPrimitive() || !definition.getAsJsonObject().has("type")) {
            IBlockState state = state(definition);
            return (context, random, pos) -> state;
        }
        JsonObject object = definition.getAsJsonObject();
        String type = path(object.get("type").getAsString());
        switch (type) {
            case "weighted": {
                List<StateProvider> providers = new ArrayList<>();
                List<Integer> weights = new ArrayList<>();
                for (JsonElement element : object.getAsJsonArray("entries")) {
                    JsonObject entry = element.getAsJsonObject();
                    providers.add(provider(entry.get("data")));
                    weights.add(entry.get("weight").getAsInt());
                }
                return (context, random, pos) -> providers.get(weighted(random, weights)).get(context, random, pos);
            }
            case "rule_based": {
                List<Predicate> predicates = new ArrayList<>();
                List<StateProvider> providers = new ArrayList<>();
                for (JsonElement element : object.getAsJsonArray("rules")) {
                    JsonObject rule = element.getAsJsonObject();
                    predicates.add(predicate(rule.getAsJsonObject("if_true")));
                    providers.add(provider(rule.get("then")));
                }
                StateProvider fallback = object.has("fallback") ? provider(object.get("fallback")) : (context, random, pos) -> null;
                return (context, random, pos) -> {
                    for (int index = 0; index < predicates.size(); index++) if (predicates.get(index).test(context, pos)) return providers.get(index).get(context, random, pos);
                    return fallback.get(context, random, pos);
                };
            }
            case "randomized_int": {
                StateProvider source = provider(object.get("source"));
                String property = object.get("property").getAsString();
                IntProvider values = integer(object.get("values"));
                return (context, random, pos) -> property(source.get(context, random, pos), property, String.valueOf(values.sample(random)));
            }
            default: throw new IllegalArgumentException("Unknown Nether state provider: " + type);
        }
    }

    static IntProvider integer(JsonElement definition) {
        if (definition.isJsonPrimitive()) {
            int value = definition.getAsInt();
            return random -> value;
        }
        JsonObject object = definition.getAsJsonObject();
        String type = path(object.get("type").getAsString());
        if ("weighted_list".equals(type)) {
            List<IntProvider> providers = new ArrayList<>();
            List<Integer> weights = new ArrayList<>();
            for (JsonElement element : object.getAsJsonArray("distribution")) {
                JsonObject entry = element.getAsJsonObject();
                providers.add(integer(entry.get("data")));
                weights.add(entry.get("weight").getAsInt());
            }
            return random -> providers.get(weighted(random, weights)).sample(random);
        }
        int minimum = object.get(object.has("min") ? "min" : "min_inclusive").getAsInt();
        int maximum = object.get(object.has("max") ? "max" : "max_inclusive").getAsInt();
        if ("uniform".equals(type)) return random -> minimum + random.nextInt(maximum - minimum + 1);
        if ("biased_to_bottom".equals(type)) return random -> minimum + random.nextInt(random.nextInt(maximum - minimum + 1) + 1);
        if ("trapezoid".equals(type)) {
            int plateau = object.has("plateau") ? object.get("plateau").getAsInt() : 0;
            int slope = (maximum - minimum - plateau) / 2;
            int second = maximum - minimum - slope;
            return random -> minimum + random.nextInt(second + 1) + random.nextInt(slope + 1);
        }
        throw new IllegalArgumentException("Unknown Nether integer provider: " + type);
    }

    private static IntProvider height(JsonObject definition) {
        int minimum = anchor(definition.get("min_inclusive"));
        int maximum = anchor(definition.get("max_inclusive"));
        String type = path(definition.get("type").getAsString());
        if ("very_biased_to_bottom".equals(type)) {
            int inner = definition.has("inner") ? definition.get("inner").getAsInt() : 1;
            return random -> minimum + random.nextInt(random.nextInt(random.nextInt(maximum - minimum - inner + 1) + inner) + inner);
        }
        JsonObject integer = new JsonObject();
        integer.addProperty("type", type);
        integer.addProperty("min_inclusive", minimum);
        integer.addProperty("max_inclusive", maximum);
        if (definition.has("plateau")) integer.add("plateau", definition.get("plateau"));
        return integer(integer);
    }

    private static int anchor(JsonElement definition) {
        if (definition.isJsonPrimitive()) return definition.getAsInt();
        JsonObject object = definition.getAsJsonObject();
        if (object.has("absolute")) return object.get("absolute").getAsInt();
        if (object.has("above_bottom")) return object.get("above_bottom").getAsInt();
        if (object.has("relative_to_sea_level")) return NetherGeneration.SEA_LEVEL + object.get("relative_to_sea_level").getAsInt();
        return NetherGeneration.GENERATION_HEIGHT - 1 - object.get("below_top").getAsInt();
    }

    static int weighted(Random random, List<Integer> weights) {
        int total = 0;
        for (int weight : weights) total = Math.addExact(total, weight);
        int choice = random.nextInt(total);
        for (int index = 0; index < weights.size(); index++) if ((choice -= weights.get(index)) < 0) return index;
        throw new IllegalStateException("Empty Nether weighted list");
    }

    static Set<Block> blocks(JsonElement definition) {
        if (definition.isJsonPrimitive() && definition.getAsString().startsWith("#")) return tag(path(definition.getAsString().substring(1)));
        Set<Block> result = new HashSet<>();
        if (definition.isJsonArray()) {
            for (JsonElement block : definition.getAsJsonArray()) {
                IBlockState state = state(block);
                if (state != null) result.add(state.getBlock());
            }
        } else {
            IBlockState state = state(definition);
            if (state != null) result.add(state.getBlock());
        }
        return result;
    }

    private static Set<Block> tag(String name) { return blocks(NetherMaterialNoise.resource("tags/" + name).get("values")); }

    static IBlockState state(JsonElement definition) {
        String name = definition.isJsonPrimitive() ? definition.getAsString() : definition.getAsJsonObject().get("id").getAsString();
        ResourceLocation id = new ResourceLocation(name);
        String path = id.getResourcePath();
        if ("magma_block".equals(path)) id = new ResourceLocation("magma");
        if ("nether_bricks".equals(path)) id = new ResourceLocation("nether_brick");
        if ("nether_quartz_ore".equals(path)) id = new ResourceLocation("quartz_ore");
        if ("spawner".equals(path)) id = new ResourceLocation("mob_spawner");
        Block block = Block.REGISTRY.containsKey(id) ? Block.REGISTRY.getObject(id) : FFDItems.localBlock(new ResourceLocation("farmers_future_delight", path));
        IBlockState state = block == null ? null : FFDItems.effectiveBlockState(block);
        if (state == null || definition.isJsonPrimitive()) return state;
        JsonObject object = definition.getAsJsonObject();
        if (object.has("properties")) for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("properties").entrySet()) state = property(state, entry.getKey(), entry.getValue().getAsString());
        return state;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static IBlockState property(IBlockState state, String name, String value) {
        if (state == null) return null;
        IProperty property = state.getBlock().getBlockState().getProperty(name);
        if (property == null || !property.parseValue(value).isPresent()) return state;
        return state.withProperty(property, (Comparable) property.parseValue(value).get());
    }

    static boolean empty(IBlockState state) {
        Material material = state.getMaterial();
        return material == Material.AIR || material == Material.WATER || material == Material.LAVA;
    }

    static String path(String value) { return new ResourceLocation(value).getResourcePath(); }
}
