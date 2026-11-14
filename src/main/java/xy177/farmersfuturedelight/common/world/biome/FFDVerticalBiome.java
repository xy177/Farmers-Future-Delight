package xy177.farmersfuturedelight.common.world.biome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import xy177.farmersfuturedelight.api.biome.VerticalBiomeClimate;
import xy177.farmersfuturedelight.api.biome.VerticalBiomeDefinition;
import xy177.farmersfuturedelight.api.biome.VerticalBiomeRegistry;

public final class FFDVerticalBiome {
    private static final List<FFDVerticalBiome> BY_ID = new ArrayList<>();
    private static final Map<ResourceLocation, FFDVerticalBiome> BY_NAME = new LinkedHashMap<>();
    private static volatile FFDVerticalBiome[] samplingEntries = new FFDVerticalBiome[0];
    private static volatile FFDVerticalBiome[] featureEntries = new FFDVerticalBiome[0];
    private static boolean frozen;

    public static final FFDVerticalBiome NONE = empty();
    public static final FFDVerticalBiome LUSH_CAVES = builtin(VerticalBiomeDefinition
            .builder(id("lush_caves"))
            .selectionRule(VerticalBiomeClimate.cave().humidity(0.7D, 1.0D).build())
            .feature(VerticalBiomeDefinition.Stage.VEGETATION,
                    context -> ((FFDVerticalBiomeGeneration) context).generateLushVegetation())
            .build());
    public static final FFDVerticalBiome DRIPSTONE_CAVES = builtin(VerticalBiomeDefinition
            .builder(id("dripstone_caves"))
            .selectionRule(VerticalBiomeClimate.cave().continentalness(0.8D, 1.0D).build())
            .build());
    public static final FFDVerticalBiome MEADOW = mountain("meadow");
    public static final FFDVerticalBiome GROVE = mountain("grove");
    public static final FFDVerticalBiome SNOWY_SLOPES = mountain("snowy_slopes");
    public static final FFDVerticalBiome JAGGED_PEAKS = mountain("jagged_peaks");
    public static final FFDVerticalBiome FROZEN_PEAKS = mountain("frozen_peaks");
    public static final FFDVerticalBiome STONY_PEAKS = mountain("stony_peaks");
    public static final FFDVerticalBiome NETHER_WASTES = nether("nether_wastes", 0, 0, 0);
    public static final FFDVerticalBiome SOUL_SAND_VALLEY = nether("soul_sand_valley", 0, -.5D, 0);
    public static final FFDVerticalBiome CRIMSON_FOREST = nether("crimson_forest", .4D, 0, 0);
    public static final FFDVerticalBiome WARPED_FOREST = nether("warped_forest", 0, .5D, .375D);
    public static final FFDVerticalBiome BASALT_DELTAS = nether("basalt_deltas", -.5D, 0, .175D);

    private final int id;
    private final VerticalBiomeDefinition definition;

    private FFDVerticalBiome(int id, VerticalBiomeDefinition definition) {
        this.id = id;
        this.definition = definition;
    }

    private static FFDVerticalBiome empty() {
        FFDVerticalBiome result = new FFDVerticalBiome(0, null);
        BY_ID.add(result);
        return result;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("farmers_future_delight", path);
    }

    private static FFDVerticalBiome mountain(String path) {
        return builtin(VerticalBiomeDefinition.builder(id(path))
                .category(VerticalBiomeDefinition.Category.MOUNTAIN).build());
    }

    private static FFDVerticalBiome nether(String path, double temperature, double humidity, double offset) {
        return builtin(VerticalBiomeDefinition.builder(id(path))
                .category(VerticalBiomeDefinition.Category.NETHER)
                .selectionRule(sample -> {
                    if (sample.getDimension() != -1) return Double.POSITIVE_INFINITY;
                    double deltaTemperature = sample.getTemperature() - temperature;
                    double deltaHumidity = sample.getHumidity() - humidity;
                    return deltaTemperature * deltaTemperature + deltaHumidity * deltaHumidity + offset * offset;
                }).build());
    }

    private static FFDVerticalBiome builtin(VerticalBiomeDefinition definition) {
        VerticalBiomeRegistry.register(definition);
        return byRegistryName(definition.getRegistryName());
    }

    public static synchronized void register(VerticalBiomeDefinition definition) {
        if (frozen) {
            throw new IllegalStateException("Vertical biome registration has finished");
        }
        ResourceLocation name = definition.getRegistryName();
        if (BY_NAME.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate vertical biome ID: " + name);
        }
        FFDVerticalBiome biome = new FFDVerticalBiome(BY_ID.size(), definition);
        BY_ID.add(biome);
        BY_NAME.put(name, biome);
        List<FFDVerticalBiome> samples = new ArrayList<>();
        List<FFDVerticalBiome> features = new ArrayList<>();
        for (FFDVerticalBiome entry : BY_NAME.values()) {
            if (entry.definition.getSelectionRule() != null) {
                samples.add(entry);
            }
            for (VerticalBiomeDefinition.Stage stage : VerticalBiomeDefinition.Stage.values()) {
                if (entry.definition.getFeature(stage) != null) {
                    features.add(entry);
                    break;
                }
            }
        }
        samples.sort(Comparator.comparing(entry -> entry.registryName().toString()));
        features.sort(Comparator.comparing(entry -> entry.registryName().toString()));
        samplingEntries = samples.toArray(new FFDVerticalBiome[0]);
        featureEntries = features.toArray(new FFDVerticalBiome[0]);
    }

    public static synchronized void freeze() {
        frozen = true;
    }

    public int id() {
        return id;
    }

    public VerticalBiomeDefinition definition() {
        return definition;
    }

    public ResourceLocation registryName() {
        return definition == null ? null : definition.getRegistryName();
    }

    public String registryPath() {
        return definition == null ? null : registryName().getResourcePath();
    }

    public static FFDVerticalBiome byId(int id) {
        return id >= 0 && id < BY_ID.size() ? BY_ID.get(id) : NONE;
    }

    public static FFDVerticalBiome byRegistryName(ResourceLocation id) {
        FFDVerticalBiome biome = BY_NAME.get(id);
        return biome == null ? NONE : biome;
    }

    public static FFDVerticalBiome[] values() {
        return BY_ID.toArray(new FFDVerticalBiome[0]);
    }

    public static FFDVerticalBiome[] samplingEntries() {
        return samplingEntries;
    }

    public static FFDVerticalBiome[] featureEntries() {
        return featureEntries;
    }

    public static Collection<VerticalBiomeDefinition> definitions() {
        List<VerticalBiomeDefinition> result = new ArrayList<>();
        for (FFDVerticalBiome biome : BY_NAME.values()) {
            result.add(biome.definition);
        }
        return Collections.unmodifiableList(result);
    }

    public boolean isMountain() {
        return definition != null
                && definition.getCategory() == VerticalBiomeDefinition.Category.MOUNTAIN;
    }

    public boolean isCave() {
        return definition != null
                && definition.getCategory() == VerticalBiomeDefinition.Category.CAVE;
    }

    public static FFDVerticalBiome adaptSurface(FFDVerticalBiome candidate, Biome legacyBiome,
                                                BlockPos pos) {
        if (!candidate.isMountain()) {
            return candidate;
        }
        if (BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.OCEAN)
                || BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.RIVER)
                || BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.BEACH)
                || BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.SWAMP)) {
            return NONE;
        }
        float temperature = legacyBiome.getTemperature(pos);
        boolean cold = BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.SNOWY)
                || BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.COLD)
                || temperature < 0.2F;
        boolean hotAndDry = BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.SANDY)
                || BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.HOT)
                && BiomeDictionary.hasType(legacyBiome, BiomeDictionary.Type.DRY)
                || temperature >= 1.0F;
        if (candidate == GROVE || candidate == SNOWY_SLOPES) {
            return cold ? candidate : hotAndDry ? NONE : MEADOW;
        }
        if (candidate == JAGGED_PEAKS || candidate == FROZEN_PEAKS) {
            return cold ? candidate : STONY_PEAKS;
        }
        return candidate == MEADOW && hotAndDry ? NONE : candidate;
    }
}
