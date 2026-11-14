package xy177.farmersfuturedelight.common.world.nether;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.nether.NetherFeaturePlacement.Feature;

final class NetherDecoration {
    private static final FFDVerticalBiome[] BIOMES = {FFDVerticalBiome.NETHER_WASTES, FFDVerticalBiome.SOUL_SAND_VALLEY,
            FFDVerticalBiome.CRIMSON_FOREST, FFDVerticalBiome.WARPED_FOREST, FFDVerticalBiome.BASALT_DELTAS};
    private final Map<FFDVerticalBiome, Set<String>> biomeFeatures = new HashMap<>();
    private final List<List<String>> steps = new ArrayList<>();
    private final Map<String, Feature> features = new HashMap<>();
    private final World world;
    private final NetherBiomeSource biomes;

    NetherDecoration(World world, NetherBiomeSource biomes) {
        this.world = world;
        this.biomes = biomes;
        List<List<List<String>>> lists = new ArrayList<>();
        for (FFDVerticalBiome biome : BIOMES) {
            JsonArray definitions = NetherMaterialNoise.resource("biome/" + biome.registryPath()).getAsJsonArray("features");
            Set<String> names = new HashSet<>();
            List<List<String>> biomeSteps = new ArrayList<>();
            for (JsonElement element : definitions) {
                List<String> entries = new ArrayList<>();
                for (JsonElement feature : element.getAsJsonArray()) {
                    String name = NetherFeaturePlacement.path(feature.getAsString());
                    entries.add(name);
                    names.add(name);
                }
                biomeSteps.add(entries);
            }
            lists.add(biomeSteps);
            biomeFeatures.put(biome, names);
        }
        NetherFeaturePlacement compiler = new NetherFeaturePlacement();
        for (int step = 0; step < 10; step++) {
            Map<String, Set<String>> dependencies = new LinkedHashMap<>();
            for (List<List<String>> biomeSteps : lists) {
                if (step >= biomeSteps.size()) continue;
                String previous = null;
                for (String name : biomeSteps.get(step)) {
                    Set<String> incoming = dependencies.computeIfAbsent(name, ignored -> new LinkedHashSet<>());
                    if (previous != null) incoming.add(previous);
                    previous = name;
                }
            }
            Map<String, Set<String>> outgoing = new LinkedHashMap<>();
            for (String name : dependencies.keySet()) outgoing.put(name, new LinkedHashSet<>());
            for (Map.Entry<String, Set<String>> entry : dependencies.entrySet())
                for (String previous : entry.getValue()) outgoing.get(previous).add(entry.getKey());
            List<String> ordered = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            Set<String> visiting = new HashSet<>();
            for (String name : outgoing.keySet()) visit(name, outgoing, visited, visiting, ordered);
            java.util.Collections.reverse(ordered);
            for (String name : ordered) if (!features.containsKey(name))
                features.put(name, compiler.placed(NetherMaterialNoise.resource("placed_feature/" + name)));
            steps.add(ordered);
        }
    }

    private static void visit(String name, Map<String, Set<String>> outgoing, Set<String> visited,
                              Set<String> visiting, List<String> ordered) {
        if (visited.contains(name)) return;
        if (!visiting.add(name)) throw new IllegalStateException("Cyclic Nether feature ordering: " + name);
        for (String next : outgoing.get(name)) visit(next, outgoing, visited, visiting, ordered);
        visiting.remove(name);
        visited.add(name);
        ordered.add(name);
    }

    void generate(int chunkX, int chunkZ) {
        NetherFeatureRandom random = new NetherFeatureRandom(0);
        long seed = random.decorationSeed(world.getSeed(), chunkX * 16, chunkZ * 16);
        BlockPos origin = new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8);
        for (int step = 0; step < steps.size(); step++) {
            List<String> names = steps.get(step);
            for (int index = 0; index < names.size(); index++) {
                String name = names.get(index);
                random.setSeed(seed + index + step * 10000L);
                features.get(name).place(new NetherFeaturePlacement.Context(world, biomes, name, biomeFeatures), random, origin);
            }
            if (step == 2) NetherBiomeFeatures.generate(world, biomes, chunkX, chunkZ,
                    xy177.farmersfuturedelight.api.biome.VerticalBiomeDefinition.Stage.LOCAL_MODIFICATIONS);
            if (step == 7) NetherBiomeFeatures.generate(world, biomes, chunkX, chunkZ,
                    xy177.farmersfuturedelight.api.biome.VerticalBiomeDefinition.Stage.UNDERGROUND_DECORATION);
            if (step == 9) NetherBiomeFeatures.generate(world, biomes, chunkX, chunkZ,
                    xy177.farmersfuturedelight.api.biome.VerticalBiomeDefinition.Stage.VEGETATION);
        }
    }
}
