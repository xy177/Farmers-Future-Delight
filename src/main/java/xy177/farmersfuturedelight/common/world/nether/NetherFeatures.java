package xy177.farmersfuturedelight.common.world.nether;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.common.world.nether.NetherFeaturePlacement.Context;
import xy177.farmersfuturedelight.common.world.nether.NetherFeaturePlacement.Feature;
import xy177.farmersfuturedelight.common.world.nether.NetherFeaturePlacement.IntProvider;
import xy177.farmersfuturedelight.common.world.nether.NetherFeaturePlacement.Predicate;
import xy177.farmersfuturedelight.common.world.nether.NetherFeaturePlacement.StateProvider;
import xy177.farmersfuturedelight.common.worldgen.WorldGenHugeFungus;

final class NetherFeatures {
    private NetherFeatures() { }

    static Feature compile(JsonObject definition, NetherFeaturePlacement compiler) {
        String type = NetherFeaturePlacement.path(definition.get("type").getAsString());
        switch (type) {
            case "simple_block": {
                StateProvider provider = compiler.provider(definition.get("to_place"));
                return (context, random, pos) -> {
                    IBlockState state = provider.get(context, random, pos);
                    if (state != null && state.getBlock().canPlaceBlockAt(context.world, pos)) context.set(pos, state);
                };
            }
            case "overlay": {
                List<Feature> features = new ArrayList<>();
                for (JsonElement feature : definition.getAsJsonArray("features")) features.add(compiler.placed(feature.getAsJsonObject()));
                return (context, random, pos) -> { for (Feature feature : features) feature.place(context, random, pos); };
            }
            case "weighted_random_selector": {
                List<Feature> features = new ArrayList<>();
                List<Integer> weights = new ArrayList<>();
                for (JsonElement element : definition.getAsJsonArray("features")) {
                    JsonObject entry = element.getAsJsonObject();
                    features.add(compiler.placed(entry.getAsJsonObject("data")));
                    weights.add(entry.get("weight").getAsInt());
                }
                return (context, random, pos) -> features.get(NetherFeaturePlacement.weighted(random, weights)).place(context, random, pos);
            }
            case "huge_fungus": {
                boolean warped = definition.get("valid_base_block").getAsString().endsWith("warped_nylium");
                return (context, random, pos) -> {
                    new WorldGenHugeFungus(warped).generate(context.world, random, pos);
                };
            }
            case "single_block_pillar": return pillar(definition, compiler);
            case "projected_random_patchy_square": return projectedSquare(definition, compiler);
            case "random_neighbor_spread": return neighborSpread(definition, compiler);
            case "block_column": return blockColumn(definition, compiler);
            case "stepped_column_cluster": return columns(definition, compiler);
            case "delta_feature": return delta(definition);
            case "netherrack_replace_blobs": return blobs(definition);
            case "spring_feature": return spring(definition);
            case "ore": return new NetherOreFeature(definition, false);
            case "scattered_ore": return new NetherOreFeature(definition, true);
            default: throw new IllegalArgumentException("Unknown Nether feature: " + type);
        }
    }

    private static Feature pillar(JsonObject definition, NetherFeaturePlacement compiler) {
        StateProvider block = compiler.provider(definition.get("block"));
        Predicate replace = compiler.predicate(definition.getAsJsonObject("can_replace"));
        EnumFacing direction = EnumFacing.byName(definition.get("direction").getAsString());
        float chance = definition.has("chance_to_continue") ? definition.get("chance_to_continue").getAsFloat() : 1;
        Feature cap = definition.has("cap_feature") ? compiler.placed(definition.getAsJsonObject("cap_feature")) : null;
        return (context, random, pos) -> {
            BlockPos cursor = pos;
            while (replace.test(context, cursor) && random.nextFloat() < chance && context.available(cursor)) {
                context.set(cursor, block.get(context, random, cursor));
                cursor = cursor.offset(direction);
            }
            if (cap != null) cap.place(context, random, cursor.offset(direction.getOpposite()));
        };
    }

    private static Feature projectedSquare(JsonObject definition, NetherFeaturePlacement compiler) {
        StateProvider block = compiler.provider(definition.get("block"));
        Predicate through = compiler.predicate(definition.getAsJsonObject("project_through"));
        IntProvider size = NetherFeaturePlacement.integer(definition.get("size"));
        int maxDrop = definition.get("max_projection_height").getAsInt();
        return (context, random, pos) -> {
            int radius = size.sample(random);
            int bound = radius * radius + 1;
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                    if (random.nextInt(bound) >= bound - Math.abs(offsetX) * Math.abs(offsetZ)) continue;
                    BlockPos cursor = pos.add(offsetX, 0, offsetZ);
                    int drop = maxDrop;
                    while (through.test(context, cursor.down()) && drop-- > 0) cursor = cursor.down();
                    context.set(cursor, block.get(context, random, cursor));
                }
            }
        };
    }

    private static Feature neighborSpread(JsonObject definition, NetherFeaturePlacement compiler) {
        StateProvider block = compiler.provider(definition.get("block"));
        Set<Block> accepted = NetherFeaturePlacement.blocks(definition.get("accepted_neighbors"));
        Predicate replace = compiler.predicate(definition.getAsJsonObject("can_replace"));
        IntProvider attempts = NetherFeaturePlacement.integer(definition.get("attempts"));
        IntProvider horizontal = NetherFeaturePlacement.integer(definition.get("xz_offset"));
        IntProvider vertical = NetherFeaturePlacement.integer(definition.get("y_offset"));
        return (context, random, pos) -> {
            context.set(pos, block.get(context, random, pos));
            int count = attempts.sample(random);
            for (int attempt = 0; attempt < count; attempt++) {
                BlockPos candidate = pos.add(horizontal.sample(random), vertical.sample(random), horizontal.sample(random));
                if (!replace.test(context, candidate)) continue;
                int neighbors = 0;
                for (EnumFacing facing : EnumFacing.values()) {
                    if (accepted.contains(context.get(candidate.offset(facing)).getBlock())) neighbors++;
                    if (neighbors > 1) break;
                }
                if (neighbors == 1) context.set(candidate, block.get(context, random, candidate));
            }
        };
    }

    private static Feature blockColumn(JsonObject definition, NetherFeaturePlacement compiler) {
        JsonArray layers = definition.getAsJsonArray("layers");
        List<IntProvider> heights = new ArrayList<>();
        List<StateProvider> blocks = new ArrayList<>();
        for (JsonElement element : layers) {
            JsonObject layer = element.getAsJsonObject();
            heights.add(NetherFeaturePlacement.integer(layer.get("height")));
            blocks.add(compiler.provider(layer.get("provider")));
        }
        EnumFacing direction = EnumFacing.byName(definition.get("direction").getAsString());
        Predicate allowed = compiler.predicate(definition.getAsJsonObject("allowed_placement"));
        boolean tip = definition.has("prioritize_tip") && definition.get("prioritize_tip").getAsBoolean();
        return (context, random, pos) -> {
            int[] counts = new int[heights.size()];
            int total = 0;
            for (int index = 0; index < counts.length; index++) total += counts[index] = heights.get(index).sample(random);
            BlockPos cursor = pos.offset(direction);
            for (int block = 0; block < total; block++) {
                if (!context.available(cursor) || !allowed.test(context, cursor)) {
                    int remaining = total - block;
                    for (int index = tip ? 0 : counts.length - 1; index >= 0 && index < counts.length && remaining > 0; index += tip ? 1 : -1) {
                        int removed = Math.min(counts[index], remaining);
                        counts[index] -= removed;
                        remaining -= removed;
                    }
                    break;
                }
                cursor = cursor.offset(direction);
            }
            cursor = pos;
            for (int index = 0; index < counts.length; index++) for (int block = 0; block < counts[index]; block++) {
                context.set(cursor, blocks.get(index).get(context, random, cursor));
                cursor = cursor.offset(direction);
            }
        };
    }

    private static Feature columns(JsonObject definition, NetherFeaturePlacement compiler) {
        StateProvider block = compiler.provider(definition.get("block"));
        Predicate replace = compiler.predicate(definition.getAsJsonObject("can_replace"));
        Predicate through = compiler.predicate(definition.getAsJsonObject("continue_through"));
        Set<Block> forbidden = NetherFeaturePlacement.blocks(definition.get("cannot_place_on"));
        IntProvider height = NetherFeaturePlacement.integer(definition.get("height"));
        IntProvider reach = NetherFeaturePlacement.integer(definition.get("cluster_reach"));
        IntProvider count = NetherFeaturePlacement.integer(definition.get("column_count"));
        IntProvider columnReach = NetherFeaturePlacement.integer(definition.get("column_reach"));
        return (context, random, pos) -> {
            if (!canPlaceColumn(context, pos, replace, forbidden)) return;
            int columnHeight = height.sample(random);
            int clusterRadius = Math.min(columnHeight, reach.sample(random));
            int attempts = count.sample(random);
            for (int attempt = 0; attempt < attempts; attempt++) {
                BlockPos center = pos.add(random.nextInt(clusterRadius * 2 + 1) - clusterRadius,
                        random.nextInt(1), random.nextInt(clusterRadius * 2 + 1) - clusterRadius);
                int blocks = columnHeight - distance(center, pos);
                if (blocks < 0) continue;
                int radius = columnReach.sample(random);
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                    BlockPos candidate = center.add(offsetX, 0, offsetZ);
                    int limit = Math.abs(offsetX) + Math.abs(offsetZ);
                    BlockPos surface = columnSurface(context, candidate, limit, replace, forbidden);
                    if (surface == null) continue;
                    for (int remaining = blocks - limit / 2; remaining >= 0; remaining--) {
                        if (replace.test(context, surface)) context.set(surface, block.get(context, random, surface));
                        else if (!through.test(context, surface)) break;
                        surface = surface.up();
                    }
                }
            }
        };
    }

    private static boolean canPlaceColumn(Context context, BlockPos pos, Predicate replace, Set<Block> forbidden) {
        IBlockState below = context.get(pos.down());
        return replace.test(context, pos) && below.getMaterial() != Material.AIR && !forbidden.contains(below.getBlock());
    }

    private static BlockPos columnSurface(Context context, BlockPos pos, int limit, Predicate replace, Set<Block> forbidden) {
        boolean descend = replace.test(context, pos);
        BlockPos cursor = pos;
        while (context.available(cursor) && cursor.getY() > 1 && limit-- > 0) {
            if (descend) {
                if (canPlaceColumn(context, cursor, replace, forbidden)) return cursor;
                cursor = cursor.down();
            } else {
                IBlockState state = context.get(cursor);
                if (forbidden.contains(state.getBlock())) return null;
                if (state.getMaterial() == Material.AIR) return cursor;
                cursor = cursor.up();
            }
        }
        return null;
    }

    private static Feature delta(JsonObject definition) {
        IBlockState contents = NetherFeaturePlacement.state(definition.get("contents"));
        IBlockState rim = NetherFeaturePlacement.state(definition.get("rim"));
        IntProvider size = NetherFeaturePlacement.integer(definition.get("size"));
        IntProvider rimSize = NetherFeaturePlacement.integer(definition.get("rim_size"));
        return (context, random, pos) -> {
            boolean border = random.nextDouble() < .9D;
            int rimX = border ? rimSize.sample(random) : 0;
            int rimZ = border ? rimSize.sample(random) : 0;
            boolean hasRim = rimX != 0 && rimZ != 0;
            int radiusX = size.sample(random);
            int radiusZ = size.sample(random);
            int maximum = Math.max(radiusX, radiusZ);
            for (int distance = 0; distance <= maximum; distance++) {
                for (int offsetX = -radiusX; offsetX <= radiusX; offsetX++) for (int offsetZ = -radiusZ; offsetZ <= radiusZ; offsetZ++) {
                    if (Math.abs(offsetX) + Math.abs(offsetZ) != distance) continue;
                    BlockPos candidate = pos.add(offsetX, 0, offsetZ);
                    if (!clearDelta(context, candidate, contents)) continue;
                    if (hasRim) context.set(candidate, rim);
                    BlockPos inside = candidate.add(rimX, 0, rimZ);
                    if (clearDelta(context, inside, contents)) context.set(inside, contents);
                }
            }
        };
    }

    private static boolean clearDelta(Context context, BlockPos pos, IBlockState contents) {
        Block block = context.get(pos).getBlock();
        if (contents == null || block == contents.getBlock() || block == Blocks.BEDROCK || block == Blocks.NETHER_BRICK
                || block == Blocks.NETHER_BRICK_FENCE || block == Blocks.NETHER_BRICK_STAIRS || block == Blocks.NETHER_WART
                || block == Blocks.CHEST || block == Blocks.MOB_SPAWNER) return false;
        for (EnumFacing facing : EnumFacing.values()) if ((context.get(pos.offset(facing)).getMaterial() == Material.AIR) != (facing == EnumFacing.UP)) return false;
        return true;
    }

    private static Feature blobs(JsonObject definition) {
        IBlockState target = NetherFeaturePlacement.state(definition.get("target"));
        IBlockState replacement = NetherFeaturePlacement.state(definition.get("state"));
        IntProvider radius = NetherFeaturePlacement.integer(definition.get("radius"));
        return (context, random, pos) -> {
            if (target == null || replacement == null) return;
            BlockPos center = new BlockPos(pos.getX(), Math.max(1, Math.min(255, pos.getY())), pos.getZ());
            while (center.getY() > 1 && context.get(center).getBlock() != target.getBlock()) center = center.down();
            if (center.getY() <= 1) return;
            int radiusX = radius.sample(random);
            int radiusY = radius.sample(random);
            int radiusZ = radius.sample(random);
            int maximum = Math.max(radiusX, Math.max(radiusY, radiusZ));
            for (int offsetX = -radiusX; offsetX <= radiusX; offsetX++) for (int offsetY = -radiusY; offsetY <= radiusY; offsetY++) for (int offsetZ = -radiusZ; offsetZ <= radiusZ; offsetZ++) {
                if (Math.abs(offsetX) + Math.abs(offsetY) + Math.abs(offsetZ) > maximum) continue;
                BlockPos candidate = center.add(offsetX, offsetY, offsetZ);
                if (context.get(candidate).getBlock() == target.getBlock()) context.set(candidate, replacement);
            }
        };
    }

    private static Feature spring(JsonObject definition) {
        Set<Block> valid = NetherFeaturePlacement.blocks(definition.get("valid_blocks"));
        boolean below = !definition.has("requires_block_below") || definition.get("requires_block_below").getAsBoolean();
        int rocks = definition.has("rock_count") ? definition.get("rock_count").getAsInt() : 4;
        int holes = definition.has("hole_count") ? definition.get("hole_count").getAsInt() : 1;
        return (context, random, pos) -> {
            if (!valid.contains(context.get(pos.up()).getBlock()) || below && !valid.contains(context.get(pos.down()).getBlock())) return;
            IBlockState current = context.get(pos);
            if (current.getMaterial() != Material.AIR && !valid.contains(current.getBlock())) return;
            int rockCount = 0;
            int holeCount = 0;
            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == EnumFacing.UP) continue;
                IBlockState state = context.get(pos.offset(facing));
                if (valid.contains(state.getBlock())) rockCount++;
                if (state.getMaterial() == Material.AIR) holeCount++;
            }
            if (rockCount == rocks && holeCount == holes) {
                context.set(pos, Blocks.FLOWING_LAVA.getDefaultState());
                context.world.scheduleUpdate(pos, Blocks.FLOWING_LAVA, 0);
            }
        };
    }

    private static int distance(BlockPos first, BlockPos second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY()) + Math.abs(first.getZ() - second.getZ());
    }
}
