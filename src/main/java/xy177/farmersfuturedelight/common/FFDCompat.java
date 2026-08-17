package xy177.farmersfuturedelight.common;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDCompat {
    public static final String HONEYCOMB_ORE_DICTIONARY = "ffdHoneycomb";
    public static final String GLOW_INK_SAC_ORE_DICTIONARY = "ffdGlowInkSac";

    private static final ResourceLocation FUTURE_MC_HONEYCOMB =
            new ResourceLocation("futuremc", "honeycomb");
    private static final ResourceLocation OCEANIC_EXPANSE_GLOW_INK_SAC =
            new ResourceLocation("oe", "glow_ink_sac");
    private static final ResourceLocation NB_CRIMSON_FOREST =
            new ResourceLocation("nb", "crimson_forest");
    private static final ResourceLocation NB_WARPED_FOREST =
            new ResourceLocation("nb", "warped_forest");
    private static final Logger LOGGER = LogManager.getLogger("FFD Auto Compatibility");
    private static final Set<String> LOGGED_CONTENT = new HashSet<>();
    private static boolean honeycombCompatibilityRegistered;
    private static boolean glowInkCompatibilityRegistered;

    private FFDCompat() {
    }

    public static boolean isEnabled(FFDConfig.FeatureMode mode) {
        if (mode == FFDConfig.FeatureMode.ENABLED) {
            return true;
        }
        if (mode == FFDConfig.FeatureMode.DISABLED) {
            return false;
        }
        return true;
    }

    public static boolean isEnabled(FFDConfig.FeatureMode mode, Feature feature) {
        return mode != FFDConfig.FeatureMode.DISABLED;
    }

    public static boolean isLocalBlockEnabled(FFDConfig.FeatureMode mode, Feature feature,
                                              Block local, String... aliases) {
        if (mode != FFDConfig.FeatureMode.AUTO) {
            return mode == FFDConfig.FeatureMode.ENABLED;
        }
        String[] paths = contentPaths(registryPath(local.getRegistryName()), aliases);
        ExternalBlock external = feature.findBlock(paths);
        logDecision(feature, "block", paths[0], external == null ? null : external.provider);
        return external == null;
    }

    public static boolean isLocalItemEnabled(FFDConfig.FeatureMode mode, Feature feature,
                                             Item local, Block localBlock, String... aliases) {
        if (mode != FFDConfig.FeatureMode.AUTO) {
            return mode == FFDConfig.FeatureMode.ENABLED;
        }
        String[] paths = contentPaths(registryPath(local.getRegistryName()), aliases);
        if (localBlock != null
                && !isLocalBlockEnabled(mode, feature, localBlock, aliases)) {
            return false;
        }
        ExternalItem external = feature.findItem(paths);
        logDecision(feature, "item", paths[0], external == null ? null : external.provider);
        return external == null;
    }

    public static boolean isLocalEntityEnabled(FFDConfig.FeatureMode mode, Feature feature,
                                               ResourceLocation local, String... aliases) {
        if (mode != FFDConfig.FeatureMode.AUTO) {
            return mode == FFDConfig.FeatureMode.ENABLED;
        }
        String[] paths = contentPaths(registryPath(local), aliases);
        ExternalEntity external = feature.findEntity(paths);
        logDecision(feature, "entity", paths[0], external == null ? null : external.provider);
        return external == null;
    }

    public static IBlockState getExternalBlockState(Feature feature, String... paths) {
        ExternalBlock external = feature.findBlock(contentPaths(null, paths));
        return external == null ? null : external.state;
    }

    public static ItemStack getExternalItemStack(Feature feature, String... paths) {
        ExternalItem external = feature.findItem(contentPaths(null, paths));
        return external == null ? ItemStack.EMPTY : external.stack.copy();
    }

    public static EntityEntry getExternalEntityEntry(Feature feature, String... paths) {
        ExternalEntity external = feature.findEntity(contentPaths(null, paths));
        return external == null ? null : external.entity;
    }

    public static boolean shouldGenerateNetherForest(boolean warped) {
        FFDConfig.FeatureMode mode = warped ? FFDConfig.warpedMode : FFDConfig.crimsonMode;
        if (mode != FFDConfig.FeatureMode.AUTO) {
            return mode == FFDConfig.FeatureMode.ENABLED;
        }
        ResourceLocation externalBiome = warped ? NB_WARPED_FOREST : NB_CRIMSON_FOREST;
        Biome biome = ForgeRegistries.BIOMES.getValue(externalBiome);
        boolean externalWorldgen = biome != null && externalBiome.equals(biome.getRegistryName());
        logWorldgenDecision(warped, externalWorldgen ? "Unseens Nether Backport" : null);
        return !externalWorldgen;
    }

    private static void logDecision(Feature feature, String type, String path,
                                    ProviderSet provider) {
        if (!FFDConfig.logAutoCompatibilityDecisions) {
            return;
        }
        String key = feature.name() + ':' + type + ':' + path;
        if (!LOGGED_CONTENT.add(key)) {
            return;
        }
        LOGGER.info("AUTO content {} {}: {}{}", feature.name(), path,
                provider == null ? "using Farmer's Future Delight"
                        : "yielding to external content",
                provider == null ? "" : " (" + provider.name + ")");
    }

    private static void logWorldgenDecision(boolean warped, String provider) {
        if (!FFDConfig.logAutoCompatibilityDecisions) {
            return;
        }
        String forest = warped ? "warped" : "crimson";
        String key = "NETHER_FOREST:worldgen:" + forest;
        if (!LOGGED_CONTENT.add(key)) {
            return;
        }
        LOGGER.info("AUTO Nether forest worldgen {}: {}{}", forest,
                provider == null ? "using Farmer's Future Delight"
                        : "yielding to external biome",
                provider == null ? "" : " (" + provider + ")");
    }

    private static String registryPath(ResourceLocation registryName) {
        return registryName == null ? "" : registryName.getResourcePath();
    }

    private static String[] contentPaths(String localPath, String... aliases) {
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        if (localPath != null && !localPath.isEmpty()) {
            paths.add(localPath);
            addAutomaticAliases(paths, localPath);
        }
        if (aliases != null) {
            for (String alias : aliases) {
                if (alias != null && !alias.isEmpty()) {
                    paths.add(alias);
                    addAutomaticAliases(paths, alias);
                }
            }
        }
        return paths.toArray(new String[0]);
    }

    private static void addAutomaticAliases(Set<String> paths, String path) {
        if ("big_dripleaf_waterlogged".equals(path)) {
            paths.add("big_dripleaf");
        } else if ("kelp_young".equals(path) || "kelp_plant".equals(path)) {
            paths.add("kelp");
        } else if ("tall_seagrass".equals(path)) {
            paths.add("seagrass");
        } else if ("turtle_scute".equals(path)) {
            paths.add("scute");
        } else if ("iron_chain".equals(path)) {
            paths.add("chain");
        } else if ("music_disc_otherside".equals(path)) {
            paths.add("record_otherside");
        } else if ("deepslate_bricks".equals(path)) {
            paths.add("deepslate_brick");
        } else if ("cracked_deepslate_bricks".equals(path)) {
            paths.add("cracked_deepslate_brick");
        } else if ("deepslate_tiles".equals(path)) {
            paths.add("deepslate_tile");
        } else if ("cracked_deepslate_tiles".equals(path)) {
            paths.add("cracked_deepslate_tile");
        }
    }

    public enum Feature {
        SWEET_BERRY(
                blocks("Future MC", "futuremc", "sweet_berry_bush")),
        MOSS(
                blocks("Depths Update", "depthsupdate", "moss_block", "moss_carpet"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "moss_block", "moss_carpet")),
        GLOW_BERRY(
                blocks("Depths Update", "depthsupdate", "cave_vines", "cave_vines_plant"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "cave_vines", "cave_vines_plant")),
        AZALEA(
                blocks("Depths Update", "depthsupdate", "azalea", "flowering_azalea",
                        "azalea_leaves", "flowering_azalea_leaves", "potted_azalea_bush",
                        "potted_flowering_azalea_bush"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "azalea", "flowering_azalea",
                        "azalea_leaves", "flowering_azalea_leaves", "potted_azalea_bush",
                        "potted_flowering_azalea_bush")),
        DRIPLEAF(
                blocks("Depths Update", "depthsupdate", "small_dripleaf", "big_dripleaf",
                        "big_dripleaf_stem"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "small_dripleaf", "big_dripleaf",
                        "big_dripleaf_stem")),
        ROOTED_DIRT(
                blocks("Depths Update", "depthsupdate", "rooted_dirt"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "rooted_dirt")),
        HANGING_ROOTS(
                blocks("Depths Update", "depthsupdate", "hanging_roots"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "hanging_roots")),
        SPORE_BLOSSOM(
                blocks("Depths Update", "depthsupdate", "spore_blossom"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "spore_blossom")),
        HONEY(
                blocks("Future MC", "futuremc", "honey_block", "honeycomb_block",
                        "bee_nest", "beehive"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "honey_block",
                        "honeycomb_block", "bee_nest", "beehive")),
        KELP(
                blocks("Oceanic Expanse", "oe", "kelp", "dried_kelp_block")),
        SEAGRASS(
                blocks("Oceanic Expanse", "oe", "seagrass"),
                blocks("Future MC", "futuremc", "seagrass")),
        SEA_PICKLE(
                blocks("Oceanic Expanse", "oe", "sea_pickle")),
        TURTLE(
                blocks("Oceanic Expanse", "oe", "turtle_egg")),
        GLOW_SQUID(
                items("Oceanic Expanse", "oe", "glow_ink_sac")),
        AXOLOTL(
                items("Caves Not Cliffs", "cavesnotcliffs", "axolotl_bucket")),
        PHANTOM(),
        OTHERSIDE(
                items("Caves Not Cliffs", "cavesnotcliffs", "music_disc_otherside"),
                items("Future MC", "futuremc", "record_otherside")),
        GLOW_ITEM_FRAME(
                items("Oceanic Expanse", "oe", "glow_item_frame")),
        AMETHYST(
                blocks("Depths Update", "depthsupdate", "amethyst_block", "budding_amethyst",
                        "small_amethyst_bud", "medium_amethyst_bud", "large_amethyst_bud",
                        "amethyst_cluster", "calcite", "smooth_basalt", "tinted_glass"),
                deeperDepthsAmethyst(),
                blocks("Unseens Nether Backport", "nb", "smooth_basalt"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "amethyst_block",
                        "budding_amethyst", "small_amethyst_bud", "medium_amethyst_bud",
                        "large_amethyst_bud", "amethyst_cluster", "calcite", "smooth_basalt",
                        "tinted_glass")),
        DEEPSLATE(
                depthsUpdateDeepslate(),
                deeperDepthsDeepslate(),
                cavesNotCliffsDeepslate()),
        RAW_ORE(
                blocks("Depths Update", "depthsupdate", "raw_iron_block", "raw_gold_block"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "raw_iron_block", "raw_gold_block")),
        COPPER(
                copper("Depths Update", "depthsupdate"),
                deeperDepthsCopper(),
                cavesNotCliffsCopper()),
        DRIPSTONE(
                blocks("Depths Update", "depthsupdate", "dripstone_block", "pointed_dripstone"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "dripstone_block",
                        "pointed_dripstone")),
        IRON_CHAIN(
                blocks("Future MC", "futuremc", "chain"),
                netherBackportChain()),
        CANDLE(
                candles("Deeper Depths", "deeperdepths"),
                candles("Caves Not Cliffs", "cavesnotcliffs")),
        POWDER_SNOW(
                content("Caves Not Cliffs", ids("cavesnotcliffs", "powder_snow"),
                        ids("cavesnotcliffs", "powder_snow_bucket"), null)),
        CRIMSON(
                netherPlants("Future MC", "futuremc", true),
                netherBackportPlants(true)),
        WARPED(
                netherPlants("Future MC", "futuremc", false),
                netherBackportPlants(false)),
        CRIMSON_WOOD(
                netherWood("Future MC", "futuremc", true),
                netherBackportWood(true)),
        WARPED_WOOD(
                netherWood("Future MC", "futuremc", false),
                netherBackportWood(false));

        private final ProviderSet[] providers;

        Feature(ProviderSet... providers) {
            this.providers = providers;
        }

        private ExternalBlock findBlock(String... paths) {
            for (ProviderSet provider : providers) {
                IBlockState state = provider.findBlock(paths);
                if (state != null) {
                    return new ExternalBlock(provider, state);
                }
            }
            return null;
        }

        private ExternalItem findItem(String... paths) {
            for (ProviderSet provider : providers) {
                ItemStack stack = provider.findItem(paths);
                if (!stack.isEmpty()) {
                    return new ExternalItem(provider, stack);
                }
            }
            return null;
        }

        private ExternalEntity findEntity(String... paths) {
            for (ProviderSet provider : providers) {
                EntityEntry entity = provider.findEntity(paths);
                if (entity != null) {
                    return new ExternalEntity(provider, entity);
                }
            }
            return null;
        }

    }

    private static ProviderSet blocks(String name, String namespace, String... paths) {
        return content(name, ids(namespace, paths), null, null);
    }

    private static ProviderSet items(String name, String namespace, String... paths) {
        return content(name, null, ids(namespace, paths), null);
    }

    private static ProviderSet entities(String name, String namespace, String... paths) {
        return content(name, null, null, ids(namespace, paths));
    }

    private static ProviderSet content(String name, String[] blocks, String[] items,
                                       String[] entities) {
        return new ProviderSet(name, blocks, items, entities);
    }

    private static ProviderSet content(String name, String[] blocks, String[] items,
                                       String[] entities, ContentVariant[] blockVariants,
                                       ContentVariant[] itemVariants) {
        return new ProviderSet(name, blocks, items, entities, blockVariants, itemVariants);
    }

    private static ContentVariant variant(String path, String namespace,
                                          String registryPath, int metadata) {
        return new ContentVariant(path, namespace + ':' + registryPath, metadata);
    }

    private static ProviderSet depthsUpdateDeepslate() {
        ContentVariant[] blocks = {
                variant("cobbled_deepslate_slab", "depthsupdate", "deepslate_slab_half", 0),
                variant("cobbled_deepslate_double_slab", "depthsupdate", "deepslate_slab_double", 0),
                variant("polished_deepslate_slab", "depthsupdate", "deepslate_slab_half", 1),
                variant("polished_deepslate_double_slab", "depthsupdate", "deepslate_slab_double", 1),
                variant("deepslate_brick_slab", "depthsupdate", "deepslate_slab_half", 2),
                variant("deepslate_brick_double_slab", "depthsupdate", "deepslate_slab_double", 2),
                variant("deepslate_tile_slab", "depthsupdate", "deepslate_slab_half", 3),
                variant("deepslate_tile_double_slab", "depthsupdate", "deepslate_slab_double", 3)
        };
        ContentVariant[] items = {
                variant("cobbled_deepslate_slab", "depthsupdate", "deepslate_slab_half", 0),
                variant("polished_deepslate_slab", "depthsupdate", "deepslate_slab_half", 1),
                variant("deepslate_brick_slab", "depthsupdate", "deepslate_slab_half", 2),
                variant("deepslate_tile_slab", "depthsupdate", "deepslate_slab_half", 3)
        };
        return content("Depths Update", ids("depthsupdate", "deepslate", "tuff",
                        "cobbled_deepslate", "polished_deepslate", "deepslate_bricks",
                        "cracked_deepslate_bricks", "deepslate_tiles",
                        "cracked_deepslate_tiles", "chiseled_deepslate",
                        "infested_deepslate", "cobbled_deepslate_stairs",
                        "polished_deepslate_stairs", "deepslate_brick_stairs",
                        "deepslate_tile_stairs", "cobbled_deepslate_wall",
                        "polished_deepslate_wall", "deepslate_brick_wall",
                        "deepslate_tile_wall", "deepslate_coal_ore",
                        "deepslate_iron_ore", "deepslate_gold_ore",
                        "deepslate_redstone_ore", "deepslate_lapis_ore",
                        "deepslate_diamond_ore", "deepslate_emerald_ore"),
                null, null, blocks, items);
    }

    private static ProviderSet cavesNotCliffsDeepslate() {
        ContentVariant[] blocks = {
                variant("cobbled_deepslate_double_slab", "cavesnotcliffs",
                        "cobbled_deepslate_slab_double", 0),
                variant("polished_deepslate_double_slab", "cavesnotcliffs",
                        "polished_deepslate_slab_double", 0),
                variant("deepslate_brick_double_slab", "cavesnotcliffs",
                        "deepslate_brick_slab_double", 0),
                variant("deepslate_tile_double_slab", "cavesnotcliffs",
                        "deepslate_tile_slab_double", 0)
        };
        return content("Caves Not Cliffs", ids("cavesnotcliffs", "deepslate", "tuff",
                        "cobbled_deepslate", "polished_deepslate", "deepslate_bricks",
                        "cracked_deepslate_bricks", "deepslate_tiles",
                        "cracked_deepslate_tiles", "chiseled_deepslate",
                        "infested_deepslate", "cobbled_deepslate_stairs",
                        "polished_deepslate_stairs", "deepslate_brick_stairs",
                        "deepslate_tile_stairs", "cobbled_deepslate_slab",
                        "cobbled_deepslate_slab_double", "polished_deepslate_slab",
                        "polished_deepslate_slab_double", "deepslate_brick_slab",
                        "deepslate_brick_slab_double", "deepslate_tile_slab",
                        "deepslate_tile_slab_double", "cobbled_deepslate_wall",
                        "polished_deepslate_wall", "deepslate_brick_wall",
                        "deepslate_tile_wall", "deepslate_coal_ore",
                        "deepslate_iron_ore", "deepslate_gold_ore",
                        "deepslate_redstone_ore", "deepslate_lapis_ore",
                        "deepslate_diamond_ore", "deepslate_emerald_ore"),
                null, null, blocks, null);
    }

    private static ProviderSet deeperDepthsDeepslate() {
        ContentVariant[] blocks = {
                variant("tuff", "deeperdepths", "stone", 0),
                variant("cobbled_deepslate", "deeperdepths", "stone", 6),
                variant("chiseled_deepslate", "deeperdepths", "stone", 7),
                variant("polished_deepslate", "deeperdepths", "stone", 8),
                variant("deepslate_bricks", "deeperdepths", "stone", 9),
                variant("cracked_deepslate_bricks", "deeperdepths", "stone", 10),
                variant("deepslate_tiles", "deeperdepths", "stone", 11),
                variant("cracked_deepslate_tiles", "deeperdepths", "stone", 12),
                variant("infested_deepslate", "deeperdepths", "deepslate", 1),
                variant("cobbled_deepslate_slab", "deeperdepths", "stone_slab", 3),
                variant("cobbled_deepslate_double_slab", "deeperdepths", "double_stone_slab", 3),
                variant("polished_deepslate_slab", "deeperdepths", "stone_slab", 4),
                variant("polished_deepslate_double_slab", "deeperdepths", "double_stone_slab", 4),
                variant("deepslate_brick_slab", "deeperdepths", "stone_slab", 5),
                variant("deepslate_brick_double_slab", "deeperdepths", "double_stone_slab", 5),
                variant("deepslate_tile_slab", "deeperdepths", "stone_slab", 6),
                variant("deepslate_tile_double_slab", "deeperdepths", "double_stone_slab", 6),
                variant("cobbled_deepslate_wall", "deeperdepths", "stone_wall", 3),
                variant("polished_deepslate_wall", "deeperdepths", "stone_wall", 4),
                variant("deepslate_brick_wall", "deeperdepths", "stone_wall", 5),
                variant("deepslate_tile_wall", "deeperdepths", "stone_wall", 6)
        };
        ContentVariant[] items = {
                variant("tuff", "deeperdepths", "stone", 0),
                variant("cobbled_deepslate", "deeperdepths", "stone", 6),
                variant("chiseled_deepslate", "deeperdepths", "stone", 7),
                variant("polished_deepslate", "deeperdepths", "stone", 8),
                variant("deepslate_bricks", "deeperdepths", "stone", 9),
                variant("cracked_deepslate_bricks", "deeperdepths", "stone", 10),
                variant("deepslate_tiles", "deeperdepths", "stone", 11),
                variant("cracked_deepslate_tiles", "deeperdepths", "stone", 12),
                variant("infested_deepslate", "deeperdepths", "deepslate", 1),
                variant("cobbled_deepslate_slab", "deeperdepths", "stone_slab", 3),
                variant("polished_deepslate_slab", "deeperdepths", "stone_slab", 4),
                variant("deepslate_brick_slab", "deeperdepths", "stone_slab", 5),
                variant("deepslate_tile_slab", "deeperdepths", "stone_slab", 6),
                variant("cobbled_deepslate_wall", "deeperdepths", "stone_wall", 3),
                variant("polished_deepslate_wall", "deeperdepths", "stone_wall", 4),
                variant("deepslate_brick_wall", "deeperdepths", "stone_wall", 5),
                variant("deepslate_tile_wall", "deeperdepths", "stone_wall", 6)
        };
        return content("Deeper Depths", ids("deeperdepths", "deepslate",
                        "cobbled_deepslate_stairs", "polished_deepslate_stairs",
                        "deepslate_brick_stairs", "deepslate_tile_stairs"), null, null,
                blocks, items);
    }

    private static ProviderSet deeperDepthsAmethyst() {
        ContentVariant[] blocks = {
                variant("calcite", "deeperdepths", "stone", 5)
        };
        ContentVariant[] items = {
                variant("calcite", "deeperdepths", "stone", 5),
                variant("amethyst_shard", "deeperdepths", "material", 1)
        };
        return content("Deeper Depths", ids("deeperdepths", "amethyst_block",
                        "budding_amethyst", "small_amethyst_bud", "medium_amethyst_bud",
                        "large_amethyst_bud", "amethyst_cluster", "tinted_glass"),
                null, null, blocks, items);
    }

    private static ProviderSet deeperDepthsCopper() {
        ContentVariant[] blocks = {
                variant("copper_block", "deeperdepths", "copper_block", 0),
                variant("exposed_copper", "deeperdepths", "copper_block", 1),
                variant("weathered_copper", "deeperdepths", "copper_block", 2),
                variant("oxidized_copper", "deeperdepths", "copper_block", 3),
                variant("waxed_copper_block", "deeperdepths", "copper_block", 4),
                variant("waxed_exposed_copper", "deeperdepths", "copper_block", 5),
                variant("waxed_weathered_copper", "deeperdepths", "copper_block", 6),
                variant("waxed_oxidized_copper", "deeperdepths", "copper_block", 7),
                variant("cut_copper", "deeperdepths", "cut_copper", 0),
                variant("exposed_cut_copper", "deeperdepths", "cut_copper", 1),
                variant("weathered_cut_copper", "deeperdepths", "cut_copper", 2),
                variant("oxidized_cut_copper", "deeperdepths", "cut_copper", 3),
                variant("waxed_cut_copper", "deeperdepths", "cut_copper", 4),
                variant("waxed_exposed_cut_copper", "deeperdepths", "cut_copper", 5),
                variant("waxed_weathered_cut_copper", "deeperdepths", "cut_copper", 6),
                variant("waxed_oxidized_cut_copper", "deeperdepths", "cut_copper", 7),
                variant("cut_copper_slab", "deeperdepths", "cut_copper_slab", 0),
                variant("exposed_cut_copper_slab", "deeperdepths", "cut_copper_slab", 1),
                variant("weathered_cut_copper_slab", "deeperdepths", "cut_copper_slab", 2),
                variant("oxidized_cut_copper_slab", "deeperdepths", "cut_copper_slab", 3),
                variant("waxed_cut_copper_slab", "deeperdepths", "cut_copper_slab", 4),
                variant("waxed_exposed_cut_copper_slab", "deeperdepths", "cut_copper_slab", 5),
                variant("waxed_weathered_cut_copper_slab", "deeperdepths", "cut_copper_slab", 6),
                variant("waxed_oxidized_cut_copper_slab", "deeperdepths", "cut_copper_slab", 7),
                variant("cut_copper_double_slab", "deeperdepths", "double_cut_copper_slab", 0),
                variant("exposed_cut_copper_double_slab", "deeperdepths", "double_cut_copper_slab", 1),
                variant("weathered_cut_copper_double_slab", "deeperdepths", "double_cut_copper_slab", 2),
                variant("oxidized_cut_copper_double_slab", "deeperdepths", "double_cut_copper_slab", 3),
                variant("waxed_cut_copper_double_slab", "deeperdepths", "double_cut_copper_slab", 4),
                variant("waxed_exposed_cut_copper_double_slab", "deeperdepths", "double_cut_copper_slab", 5),
                variant("waxed_weathered_cut_copper_double_slab", "deeperdepths", "double_cut_copper_slab", 6),
                variant("waxed_oxidized_cut_copper_double_slab", "deeperdepths", "double_cut_copper_slab", 7)
        };
        ContentVariant[] items = {
                variant("copper_block", "deeperdepths", "copper_block", 0),
                variant("exposed_copper", "deeperdepths", "copper_block", 1),
                variant("weathered_copper", "deeperdepths", "copper_block", 2),
                variant("oxidized_copper", "deeperdepths", "copper_block", 3),
                variant("waxed_copper_block", "deeperdepths", "copper_block", 4),
                variant("waxed_exposed_copper", "deeperdepths", "copper_block", 5),
                variant("waxed_weathered_copper", "deeperdepths", "copper_block", 6),
                variant("waxed_oxidized_copper", "deeperdepths", "copper_block", 7),
                variant("cut_copper", "deeperdepths", "cut_copper", 0),
                variant("exposed_cut_copper", "deeperdepths", "cut_copper", 1),
                variant("weathered_cut_copper", "deeperdepths", "cut_copper", 2),
                variant("oxidized_cut_copper", "deeperdepths", "cut_copper", 3),
                variant("waxed_cut_copper", "deeperdepths", "cut_copper", 4),
                variant("waxed_exposed_cut_copper", "deeperdepths", "cut_copper", 5),
                variant("waxed_weathered_cut_copper", "deeperdepths", "cut_copper", 6),
                variant("waxed_oxidized_cut_copper", "deeperdepths", "cut_copper", 7),
                variant("cut_copper_slab", "deeperdepths", "cut_copper_slab", 0),
                variant("exposed_cut_copper_slab", "deeperdepths", "cut_copper_slab", 1),
                variant("weathered_cut_copper_slab", "deeperdepths", "cut_copper_slab", 2),
                variant("oxidized_cut_copper_slab", "deeperdepths", "cut_copper_slab", 3),
                variant("waxed_cut_copper_slab", "deeperdepths", "cut_copper_slab", 4),
                variant("waxed_exposed_cut_copper_slab", "deeperdepths", "cut_copper_slab", 5),
                variant("waxed_weathered_cut_copper_slab", "deeperdepths", "cut_copper_slab", 6),
                variant("waxed_oxidized_cut_copper_slab", "deeperdepths", "cut_copper_slab", 7),
                variant("copper_ingot", "deeperdepths", "material", 0)
        };
        return content("Deeper Depths", ids("deeperdepths", "copper_ore",
                        "raw_copper_block", "cut_copper_stairs",
                        "exposed_cut_copper_stairs", "weathered_cut_copper_stairs",
                        "oxidized_cut_copper_stairs", "waxed_cut_copper_stairs",
                        "waxed_exposed_cut_copper_stairs",
                        "waxed_weathered_cut_copper_stairs",
                        "waxed_oxidized_cut_copper_stairs", "lightning_rod",
                        "exposed_lightning_rod", "weathered_lightning_rod",
                        "oxidized_lightning_rod", "waxed_lightning_rod",
                        "waxed_exposed_lightning_rod", "waxed_weathered_lightning_rod",
                        "waxed_oxidized_lightning_rod"),
                null, null, blocks, items);
    }

    private static ProviderSet copper(String name, String namespace) {
        return copper(name, namespace, null);
    }

    private static ProviderSet copper(String name, String namespace,
                                      ContentVariant[] blockVariants) {
        return content(name, ids(namespace, "copper_ore", "raw_copper_block",
                        "copper_block", "exposed_copper", "weathered_copper",
                        "oxidized_copper", "cut_copper", "exposed_cut_copper",
                        "weathered_cut_copper", "oxidized_cut_copper",
                        "cut_copper_stairs", "exposed_cut_copper_stairs",
                        "weathered_cut_copper_stairs", "oxidized_cut_copper_stairs",
                        "cut_copper_slab", "exposed_cut_copper_slab",
                        "weathered_cut_copper_slab", "oxidized_cut_copper_slab",
                        "waxed_copper_block", "waxed_exposed_copper",
                        "waxed_weathered_copper", "waxed_oxidized_copper",
                        "waxed_cut_copper", "waxed_exposed_cut_copper",
                        "waxed_weathered_cut_copper", "waxed_oxidized_cut_copper",
                        "lightning_rod"), null, null, blockVariants, null);
    }

    private static ProviderSet cavesNotCliffsCopper() {
        ContentVariant[] blocks = {
                variant("cut_copper_double_slab", "cavesnotcliffs",
                        "cut_copper_slab_double", 0),
                variant("exposed_cut_copper_double_slab", "cavesnotcliffs",
                        "exposed_cut_copper_slab_double", 0),
                variant("weathered_cut_copper_double_slab", "cavesnotcliffs",
                        "weathered_cut_copper_slab_double", 0),
                variant("oxidized_cut_copper_double_slab", "cavesnotcliffs",
                        "oxidized_cut_copper_slab_double", 0),
                variant("waxed_cut_copper_double_slab", "cavesnotcliffs",
                        "waxed_cut_copper_slab_double", 0),
                variant("waxed_exposed_cut_copper_double_slab", "cavesnotcliffs",
                        "waxed_exposed_cut_copper_slab_double", 0),
                variant("waxed_weathered_cut_copper_double_slab", "cavesnotcliffs",
                        "waxed_weathered_cut_copper_slab_double", 0),
                variant("waxed_oxidized_cut_copper_double_slab", "cavesnotcliffs",
                        "waxed_oxidized_cut_copper_slab_double", 0)
        };
        return copper("Caves Not Cliffs", "cavesnotcliffs", blocks);
    }

    private static ProviderSet candles(String name, String namespace) {
        return blocks(name, namespace, "candle", "white_candle", "orange_candle",
                "magenta_candle", "light_blue_candle", "yellow_candle", "lime_candle",
                "pink_candle", "gray_candle", "light_gray_candle", "cyan_candle",
                "purple_candle", "blue_candle", "brown_candle", "green_candle",
                "red_candle", "black_candle", "candle_cake", "white_candle_cake",
                "orange_candle_cake", "magenta_candle_cake", "light_blue_candle_cake",
                "yellow_candle_cake", "lime_candle_cake", "pink_candle_cake",
                "gray_candle_cake", "light_gray_candle_cake", "cyan_candle_cake",
                "purple_candle_cake", "blue_candle_cake", "brown_candle_cake",
                "green_candle_cake", "red_candle_cake", "black_candle_cake");
    }

    private static ProviderSet netherPlants(String name, String namespace, boolean crimson) {
        return crimson
                ? blocks(name, namespace, "crimson_nylium", "crimson_fungus", "crimson_roots",
                        "weeping_vines", "weeping_vines_plant")
                : blocks(name, namespace, "warped_nylium", "warped_fungus", "warped_roots",
                        "nether_sprouts", "twisting_vines", "twisting_vines_plant");
    }

    private static ProviderSet netherWood(String name, String namespace, boolean crimson) {
        String prefix = crimson ? "crimson" : "warped";
        String wart = crimson ? "nether_wart_block" : "warped_wart_block";
        return blocks(name, namespace, prefix + "_stem", "stripped_" + prefix + "_stem",
                prefix + "_hyphae", "stripped_" + prefix + "_hyphae", wart,
                prefix + "_planks", prefix + "_stairs", prefix + "_slab",
                prefix + "_fence", prefix + "_fence_gate", prefix + "_door",
                prefix + "_trapdoor", prefix + "_button", prefix + "_pressure_plate",
                "shroomlight");
    }

    private static ProviderSet netherBackportPlants(boolean crimson) {
        ContentVariant[] variants = crimson
                ? new ContentVariant[] {
                        variant("crimson_nylium", "nb", "crimson_grass", 0),
                        variant("crimson_fungus", "nb", "crimson_fungus", 0),
                        variant("crimson_roots", "nb", "crimson_roots", 0),
                        variant("weeping_vines", "nb", "crimson_vine", 0),
                        variant("weeping_vines_plant", "nb", "crimson_vine", 0)
                }
                : new ContentVariant[] {
                        variant("warped_nylium", "nb", "warped_grass", 0),
                        variant("warped_fungus", "nb", "warped_fungus", 0),
                        variant("warped_roots", "nb", "warped_roots", 0),
                        variant("nether_sprouts", "nb", "warped_sprout", 0),
                        variant("twisting_vines", "nb", "warped_vine", 0),
                        variant("twisting_vines_plant", "nb", "warped_vine", 0)
                };
        return content("Unseens Nether Backport", null, null, null, variants, variants);
    }

    private static ProviderSet netherBackportChain() {
        ContentVariant[] variants = {
                variant("iron_chain", "nb", "chain_block", 0)
        };
        return content("Unseens Nether Backport", null, null, null, variants, variants);
    }

    private static ProviderSet netherBackportWood(boolean crimson) {
        String prefix = crimson ? "crimson" : "warped";
        String wart = crimson ? "crimson_wart" : "warped_wart";
        ContentVariant[] variants = {
                variant(prefix + "_stem", "nb", prefix + "_stem", 0),
                variant(prefix + "_hyphae", "nb", prefix + "_hyphae", 0),
                variant(crimson ? "nether_wart_block" : "warped_wart_block",
                        "nb", wart, 0),
                variant(prefix + "_planks", "nb", prefix + "_planks", 0),
                variant(prefix + "_stairs", "nb", prefix + "_stairs", 0),
                variant(prefix + "_slab", "nb", prefix + "_slab_half", 0),
                variant(prefix + "_double_slab", "nb", prefix + "_slab_double", 0),
                variant(prefix + "_fence", "nb", prefix + "_fence", 0),
                variant(prefix + "_fence_gate", "nb", prefix + "_gate", 0),
                variant(prefix + "_door", "nb", prefix + "_door", 0),
                variant(prefix + "_trapdoor", "nb", prefix + "_trapdoor", 0),
                variant("shroomlight", "nb", "shroom_light", 0)
        };
        return content("Unseens Nether Backport", null, null, null, variants, variants);
    }

    private static String[] ids(String namespace, String... paths) {
        String[] result = new String[paths.length];
        for (int index = 0; index < paths.length; index++) {
            result[index] = namespace + ":" + paths[index];
        }
        return result;
    }

    private static final class ProviderSet {
        private final String name;
        private final String[] blocks;
        private final String[] items;
        private final String[] entities;
        private final ContentVariant[] blockVariants;
        private final ContentVariant[] itemVariants;
        private final String namespace;

        private ProviderSet(String name, String[] blocks, String[] items, String[] entities) {
            this(name, blocks, items, entities, null, null);
        }

        private ProviderSet(String name, String[] blocks, String[] items, String[] entities,
                            ContentVariant[] blockVariants, ContentVariant[] itemVariants) {
            this.name = name;
            this.blocks = blocks;
            this.items = items;
            this.entities = entities;
            this.blockVariants = blockVariants;
            this.itemVariants = itemVariants;
            this.namespace = namespace(blocks, items, entities);
        }

        private IBlockState findBlock(String... paths) {
            Block explicit = findRegisteredBlock(blocks, paths);
            if (explicit != null) {
                return explicit.getDefaultState();
            }
            IBlockState variant = findRegisteredBlockVariant(blockVariants, paths);
            if (variant != null) {
                return variant;
            }
            Block inferred = findRegisteredBlock(namespace, paths);
            return inferred == null ? null : inferred.getDefaultState();
        }

        private ItemStack findItem(String... paths) {
            Item explicit = findRegisteredItem(items, paths);
            if (explicit != null) {
                return new ItemStack(explicit);
            }
            ItemStack variant = findRegisteredItemVariant(itemVariants, paths);
            if (!variant.isEmpty()) {
                return variant;
            }
            Item inferred = findRegisteredItem(namespace, paths);
            return inferred == null ? ItemStack.EMPTY : new ItemStack(inferred);
        }

        private EntityEntry findEntity(String... paths) {
            EntityEntry explicit = findRegisteredEntity(entities, paths);
            return explicit != null ? explicit : findRegisteredEntity(namespace, paths);
        }

    }

    private static IBlockState findRegisteredBlockVariant(ContentVariant[] variants,
                                                           String... paths) {
        if (variants == null) {
            return null;
        }
        for (ContentVariant variant : variants) {
            if (!containsPath(paths, variant.path)) {
                continue;
            }
            ResourceLocation key = new ResourceLocation(variant.registryName);
            Block block = ForgeRegistries.BLOCKS.getValue(key);
            if (block != null && key.equals(block.getRegistryName())) {
                return block.getStateFromMeta(variant.metadata);
            }
        }
        return null;
    }

    private static ItemStack findRegisteredItemVariant(ContentVariant[] variants,
                                                       String... paths) {
        if (variants == null) {
            return ItemStack.EMPTY;
        }
        for (ContentVariant variant : variants) {
            if (!containsPath(paths, variant.path)) {
                continue;
            }
            ResourceLocation key = new ResourceLocation(variant.registryName);
            Item item = ForgeRegistries.ITEMS.getValue(key);
            if (item != null && key.equals(item.getRegistryName())) {
                return new ItemStack(item, 1, variant.metadata);
            }
        }
        return ItemStack.EMPTY;
    }

    private static Block findRegisteredBlock(String[] ids, String... paths) {
        if (ids == null) {
            return null;
        }
        for (String id : ids) {
            ResourceLocation key = new ResourceLocation(id);
            if (!containsPath(paths, key.getResourcePath())) {
                continue;
            }
            Block block = ForgeRegistries.BLOCKS.getValue(key);
            if (block != null && key.equals(block.getRegistryName())) {
                return block;
            }
        }
        return null;
    }

    private static Block findRegisteredBlock(String namespace, String... paths) {
        if (namespace == null) {
            return null;
        }
        for (String path : paths) {
            ResourceLocation key = new ResourceLocation(namespace, path);
            Block block = ForgeRegistries.BLOCKS.getValue(key);
            if (block != null && key.equals(block.getRegistryName())) {
                return block;
            }
        }
        return null;
    }

    private static Item findRegisteredItem(String[] ids, String... paths) {
        if (ids == null) {
            return null;
        }
        for (String id : ids) {
            ResourceLocation key = new ResourceLocation(id);
            if (!containsPath(paths, key.getResourcePath())) {
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(key);
            if (item != null && key.equals(item.getRegistryName())) {
                return item;
            }
        }
        return null;
    }

    private static Item findRegisteredItem(String namespace, String... paths) {
        if (namespace == null) {
            return null;
        }
        for (String path : paths) {
            ResourceLocation key = new ResourceLocation(namespace, path);
            Item item = ForgeRegistries.ITEMS.getValue(key);
            if (item != null && key.equals(item.getRegistryName())) {
                return item;
            }
        }
        return null;
    }

    private static EntityEntry findRegisteredEntity(String[] ids, String... paths) {
        if (ids == null) {
            return null;
        }
        for (String id : ids) {
            ResourceLocation key = new ResourceLocation(id);
            if (!containsPath(paths, key.getResourcePath())) {
                continue;
            }
            EntityEntry entity = ForgeRegistries.ENTITIES.getValue(key);
            if (entity != null && key.equals(entity.getRegistryName())) {
                return entity;
            }
        }
        return null;
    }

    private static EntityEntry findRegisteredEntity(String namespace, String... paths) {
        if (namespace == null) {
            return null;
        }
        for (String path : paths) {
            ResourceLocation key = new ResourceLocation(namespace, path);
            EntityEntry entity = ForgeRegistries.ENTITIES.getValue(key);
            if (entity != null && key.equals(entity.getRegistryName())) {
                return entity;
            }
        }
        return null;
    }

    private static boolean containsPath(String[] paths, String target) {
        for (String path : paths) {
            if (target.equals(path)) {
                return true;
            }
        }
        return false;
    }

    private static String namespace(String[]... groups) {
        for (String[] group : groups) {
            if (group != null && group.length > 0) {
                return new ResourceLocation(group[0]).getResourceDomain();
            }
        }
        return null;
    }

    private static final class ExternalBlock {
        private final ProviderSet provider;
        private final IBlockState state;

        private ExternalBlock(ProviderSet provider, IBlockState state) {
            this.provider = provider;
            this.state = state;
        }
    }

    private static final class ExternalItem {
        private final ProviderSet provider;
        private final ItemStack stack;

        private ExternalItem(ProviderSet provider, ItemStack stack) {
            this.provider = provider;
            this.stack = stack;
        }
    }

    private static final class ContentVariant {
        private final String path;
        private final String registryName;
        private final int metadata;

        private ContentVariant(String path, String registryName, int metadata) {
            this.path = path;
            this.registryName = registryName;
            this.metadata = metadata;
        }
    }

    private static final class ExternalEntity {
        private final ProviderSet provider;
        private final EntityEntry entity;

        private ExternalEntity(ProviderSet provider, EntityEntry entity) {
            this.provider = provider;
            this.entity = entity;
        }
    }

    public static void registerHoneycombCompatibility() {
        if (honeycombCompatibilityRegistered) {
            return;
        }
        honeycombCompatibilityRegistered = true;
        if (FFDItems.isItemRegistered(FFDItems.HONEYCOMB)) {
            OreDictionary.registerOre(HONEYCOMB_ORE_DICTIONARY, FFDItems.HONEYCOMB);
        }
        registerExternalHoneycomb(FUTURE_MC_HONEYCOMB);
    }

    public static boolean hasCompatibleHoneycomb() {
        return !OreDictionary.getOres(HONEYCOMB_ORE_DICTIONARY).isEmpty();
    }

    public static void registerGlowInkCompatibility() {
        if (glowInkCompatibilityRegistered) {
            return;
        }
        glowInkCompatibilityRegistered = true;
        if (FFDItems.isItemRegistered(FFDItems.GLOW_INK_SAC)) {
            OreDictionary.registerOre(GLOW_INK_SAC_ORE_DICTIONARY, FFDItems.GLOW_INK_SAC);
        }
        registerExternalItem(GLOW_INK_SAC_ORE_DICTIONARY, OCEANIC_EXPANSE_GLOW_INK_SAC);
    }

    public static boolean hasCompatibleGlowInkSac() {
        return !OreDictionary.getOres(GLOW_INK_SAC_ORE_DICTIONARY).isEmpty();
    }

    public static boolean isCompatibleHoneycomb(ItemStack stack) {
        return isInOreDictionary(stack, HONEYCOMB_ORE_DICTIONARY);
    }

    public static boolean isCompatibleGlowInkSac(ItemStack stack) {
        return isInOreDictionary(stack, GLOW_INK_SAC_ORE_DICTIONARY);
    }

    private static boolean isInOreDictionary(ItemStack stack, String oreDictionaryName) {
        if (stack.isEmpty()) {
            return false;
        }
        int targetOreId = OreDictionary.getOreID(oreDictionaryName);
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if (oreId == targetOreId) {
                return true;
            }
        }
        return false;
    }

    private static void registerExternalHoneycomb(ResourceLocation registryName) {
        registerExternalItem(HONEYCOMB_ORE_DICTIONARY, registryName);
    }

    private static void registerExternalItem(String oreDictionaryName,
                                             ResourceLocation registryName) {
        Item item = ForgeRegistries.ITEMS.getValue(registryName);
        if (item != null) {
            OreDictionary.registerOre(oreDictionaryName, item);
        }
    }
}
