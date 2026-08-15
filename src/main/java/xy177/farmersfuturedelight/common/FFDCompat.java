package xy177.farmersfuturedelight.common;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
                                             Item local, String... aliases) {
        if (mode != FFDConfig.FeatureMode.AUTO) {
            return mode == FFDConfig.FeatureMode.ENABLED;
        }
        String[] paths = contentPaths(registryPath(local.getRegistryName()), aliases);
        if (local instanceof ItemBlock) {
            Block block = ((ItemBlock) local).getBlock();
            if (!isLocalBlockEnabled(mode, feature, block, aliases)) {
                return false;
            }
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
        return external == null ? null : external.block.getDefaultState();
    }

    public static ItemStack getExternalItemStack(Feature feature, String... paths) {
        ExternalItem external = feature.findItem(contentPaths(null, paths));
        return external == null ? ItemStack.EMPTY : new ItemStack(external.item);
    }

    public static EntityEntry getExternalEntityEntry(Feature feature, String... paths) {
        ExternalEntity external = feature.findEntity(contentPaths(null, paths));
        return external == null ? null : external.entity;
    }

    private static void logDecision(Feature feature, String type, String path,
                                    ProviderSet provider) {
        String key = feature.name() + ':' + type + ':' + path;
        if (!LOGGED_CONTENT.add(key)) {
            return;
        }
        LOGGER.info("AUTO content {} {}: {}{}", feature.name(), path,
                provider == null ? "using Farmer's Future Delight"
                        : "yielding to external content",
                provider == null ? "" : " (" + provider.name + ")");
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
        } else if ("music_disc_otherside".equals(path)) {
            paths.add("record_otherside");
        }
        if (path.startsWith("potted_")) {
            String unpotted = path.substring("potted_".length());
            if (unpotted.endsWith("_bush")) {
                unpotted = unpotted.substring(0, unpotted.length() - "_bush".length());
            }
            paths.add(unpotted);
        }
        if (path.endsWith("_double_slab")) {
            paths.add(path.substring(0, path.length() - "_double_slab".length()) + "_slab");
        }
        if (path.endsWith("lightning_rod")) {
            paths.add("lightning_rod");
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
                        "bee_nest", "beehive")),
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
                blocks("Deeper Depths", "deeperdepths", "amethyst_block", "budding_amethyst",
                        "small_amethyst_bud", "medium_amethyst_bud", "large_amethyst_bud",
                        "amethyst_cluster", "calcite", "smooth_basalt", "tinted_glass"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "amethyst_block",
                        "budding_amethyst", "small_amethyst_bud", "medium_amethyst_bud",
                        "large_amethyst_bud", "amethyst_cluster", "calcite", "smooth_basalt",
                        "tinted_glass")),
        DEEPSLATE(
                depthsUpdateDeepslate(),
                cavesNotCliffsDeepslate()),
        RAW_ORE(
                blocks("Depths Update", "depthsupdate", "raw_iron_block", "raw_gold_block"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "raw_iron_block", "raw_gold_block")),
        COPPER(
                copper("Depths Update", "depthsupdate"),
                copper("Deeper Depths", "deeperdepths"),
                copper("Caves Not Cliffs", "cavesnotcliffs")),
        DRIPSTONE(
                blocks("Depths Update", "depthsupdate", "dripstone_block", "pointed_dripstone"),
                blocks("Caves Not Cliffs", "cavesnotcliffs", "dripstone_block",
                        "pointed_dripstone")),
        IRON_CHAIN(
                blocks("Future MC", "futuremc", "chain")),
        CANDLE(
                candles("Caves Not Cliffs", "cavesnotcliffs")),
        POWDER_SNOW(
                content("Caves Not Cliffs", ids("cavesnotcliffs", "powder_snow"),
                        ids("cavesnotcliffs", "powder_snow_bucket"), null)),
        CRIMSON(
                netherPlants("Future MC", "futuremc", true)),
        WARPED(
                netherPlants("Future MC", "futuremc", false)),
        CRIMSON_WOOD(
                netherWood("Future MC", "futuremc", true)),
        WARPED_WOOD(
                netherWood("Future MC", "futuremc", false));

        private final ProviderSet[] providers;

        Feature(ProviderSet... providers) {
            this.providers = providers;
        }

        private ExternalBlock findBlock(String... paths) {
            for (ProviderSet provider : providers) {
                Block block = provider.findBlock(paths);
                if (block != null) {
                    return new ExternalBlock(provider, block);
                }
            }
            return null;
        }

        private ExternalItem findItem(String... paths) {
            for (ProviderSet provider : providers) {
                Item item = provider.findItem(paths);
                if (item != null) {
                    return new ExternalItem(provider, item);
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

    private static ProviderSet depthsUpdateDeepslate() {
        return blocks("Depths Update", "depthsupdate", "deepslate", "tuff",
                "cobbled_deepslate", "polished_deepslate", "deepslate_bricks",
                "cracked_deepslate_bricks", "deepslate_tiles", "cracked_deepslate_tiles",
                "chiseled_deepslate", "infested_deepslate", "cobbled_deepslate_stairs",
                "polished_deepslate_stairs", "deepslate_brick_stairs",
                "deepslate_tile_stairs", "deepslate_slab_half", "deepslate_slab_double",
                "cobbled_deepslate_wall", "polished_deepslate_wall",
                "deepslate_brick_wall", "deepslate_tile_wall", "deepslate_coal_ore",
                "deepslate_iron_ore", "deepslate_gold_ore", "deepslate_redstone_ore",
                "deepslate_lapis_ore", "deepslate_diamond_ore", "deepslate_emerald_ore");
    }

    private static ProviderSet cavesNotCliffsDeepslate() {
        return blocks("Caves Not Cliffs", "cavesnotcliffs", "deepslate", "tuff",
                "cobbled_deepslate", "polished_deepslate", "deepslate_bricks",
                "cracked_deepslate_bricks", "deepslate_tiles", "cracked_deepslate_tiles",
                "chiseled_deepslate", "infested_deepslate", "cobbled_deepslate_stairs",
                "polished_deepslate_stairs", "deepslate_brick_stairs",
                "deepslate_tile_stairs", "cobbled_deepslate_slab",
                "cobbled_deepslate_slab_double", "polished_deepslate_slab",
                "polished_deepslate_slab_double", "deepslate_brick_slab",
                "deepslate_brick_slab_double", "deepslate_tile_slab",
                "deepslate_tile_slab_double", "cobbled_deepslate_wall",
                "polished_deepslate_wall", "deepslate_brick_wall", "deepslate_tile_wall",
                "deepslate_coal_ore", "deepslate_iron_ore", "deepslate_gold_ore",
                "deepslate_redstone_ore", "deepslate_lapis_ore", "deepslate_diamond_ore",
                "deepslate_emerald_ore");
    }

    private static ProviderSet copper(String name, String namespace) {
        return blocks(name, namespace, "copper_ore", "raw_copper_block", "copper_block",
                "exposed_copper", "weathered_copper", "oxidized_copper", "cut_copper",
                "exposed_cut_copper", "weathered_cut_copper", "oxidized_cut_copper",
                "cut_copper_stairs", "exposed_cut_copper_stairs",
                "weathered_cut_copper_stairs", "oxidized_cut_copper_stairs",
                "cut_copper_slab", "exposed_cut_copper_slab", "weathered_cut_copper_slab",
                "oxidized_cut_copper_slab", "waxed_copper_block", "waxed_exposed_copper",
                "waxed_weathered_copper", "waxed_oxidized_copper", "waxed_cut_copper",
                "waxed_exposed_cut_copper", "waxed_weathered_cut_copper",
                "waxed_oxidized_cut_copper", "lightning_rod");
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
        private final String namespace;

        private ProviderSet(String name, String[] blocks, String[] items, String[] entities) {
            this.name = name;
            this.blocks = blocks;
            this.items = items;
            this.entities = entities;
            this.namespace = namespace(blocks, items, entities);
        }

        private Block findBlock(String... paths) {
            Block explicit = findRegisteredBlock(blocks, paths);
            return explicit != null ? explicit : findRegisteredBlock(namespace, paths);
        }

        private Item findItem(String... paths) {
            Item explicit = findRegisteredItem(items, paths);
            return explicit != null ? explicit : findRegisteredItem(namespace, paths);
        }

        private EntityEntry findEntity(String... paths) {
            EntityEntry explicit = findRegisteredEntity(entities, paths);
            return explicit != null ? explicit : findRegisteredEntity(namespace, paths);
        }

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
        private final Block block;

        private ExternalBlock(ProviderSet provider, Block block) {
            this.provider = provider;
            this.block = block;
        }
    }

    private static final class ExternalItem {
        private final ProviderSet provider;
        private final Item item;

        private ExternalItem(ProviderSet provider, Item item) {
            this.provider = provider;
            this.item = item;
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
