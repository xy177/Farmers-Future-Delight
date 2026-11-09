package xy177.farmersfuturedelight.client;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class FFDCreativeInventory {
    private static final CreativeTabs[] ORIGINALS = {CreativeTabs.BUILDING_BLOCKS, CreativeTabs.DECORATIONS,
            CreativeTabs.REDSTONE, CreativeTabs.TRANSPORTATION, CreativeTabs.MISC, CreativeTabs.TOOLS,
            CreativeTabs.COMBAT, CreativeTabs.FOOD, CreativeTabs.BREWING};
    private static final String[] CATEGORIES = {"building_blocks", "colored_blocks", "natural_blocks",
            "functional_blocks", "redstone_blocks", "tools_and_utilities", "combat", "food_and_drinks",
            "ingredients", "spawn_eggs"};
    private static final CreativeTabs[] MODERN_TABS = {CreativeTabs.BUILDING_BLOCKS, CreativeTabs.DECORATIONS,
            CreativeTabs.TRANSPORTATION, CreativeTabs.MISC, CreativeTabs.REDSTONE, CreativeTabs.TOOLS,
            CreativeTabs.COMBAT, CreativeTabs.FOOD, CreativeTabs.BREWING};
    private static final String[] WOODS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};
    private static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime",
            "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private static final Map<String, List<String>> ORDER = loadOrder("creative_inventory_order.json");
    private static final Map<String, List<String>> CLASSIC_ORDER = loadOrder("classic_creative_inventory_order.json");
    private static final String[] CLASSIC_NAMES = {"building_blocks", "decorations", "redstone", "transportation",
            "misc", "tools", "combat", "food", "brewing"};
    private static CreativeTabs spawnEggs;
    private static boolean gathering;
    private static List<CreativeTabs> extraTabsSnapshot = Collections.emptyList();
    private static CreativeTabs[] creativeTabsSnapshot;
    private static FFDConfig.CreativeInventoryMode snapshotMode;
    private static boolean ffdHasItemsSnapshot;
    private static boolean spawnEggsHasItemsSnapshot;
    private static boolean tabSnapshotReady;

    private FFDCreativeInventory() {
    }

    public static boolean modern() {
        return FFDConfig.creativeInventoryMode == FFDConfig.CreativeInventoryMode.MODERN;
    }

    public static void install() {
        if (modern() && spawnEggs == null) {
            spawnEggs = new CreativeTabs("farmers_future_delight.spawn_eggs") {
                @Override
                public ItemStack getTabIconItem() {
                    ItemStack egg = new ItemStack(Items.SPAWN_EGG);
                    ItemMonsterPlacer.applyEntityIdToItemStack(egg, new ResourceLocation("minecraft", "creeper"));
                    return egg;
                }

                @Override
                public void displayAllRelevantItems(NonNullList<ItemStack> items) {
                    afterDisplay(this, items);
                }
            };
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void open(GuiOpenEvent event) {
        if (event.getGui() != null && event.getGui().getClass() == GuiContainerCreative.class) {
            install();
            refreshTabSnapshot();
            if (modern()) event.setGui(new GuiModernCreativeInventory(Minecraft.getMinecraft().player));
        }
    }

    public static void afterDisplay(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (gathering || FFDConfig.creativeInventoryMode == FFDConfig.CreativeInventoryMode.MOD_TAB) return;
        if (tab == FFDCreativeTab.INSTANCE) {
            items.removeIf(stack -> managed(stack) && (modern() ? !categories(stack).isEmpty()
                    : classicTab(stack) != FFDCreativeTab.INSTANCE));
            return;
        }
        if (modern()) {
            int category = modernIndex(tab);
            if (category < 0) return;
            List<Entry> source = collect(true);
            List<ItemStack> selected = new ArrayList<>();
            for (Entry entry : source) {
                String namespace = entry.stack.getItem().getRegistryName().getResourceDomain();
                List<String> groups = categories(entry.stack);
                if ("minecraft".equals(namespace) || FarmerFutureDelight.MODID.equals(namespace)) {
                    if (groups.contains(CATEGORIES[category]) || groups.isEmpty()
                            && !FarmerFutureDelight.MODID.equals(namespace) && modernDestination(entry.tab) == tab) {
                        addUnique(selected, entry.stack);
                    }
                } else if (displayTab(entry.tab, entry.stack) == tab) {
                    addUnique(selected, entry.stack);
                }
            }
            selected.sort(Comparator.comparingInt(stack -> rank(stack, CATEGORIES[category])));
            items.clear();
            items.addAll(selected);
        } else if (Arrays.asList(ORIGINALS).contains(tab)) {
            List<ItemStack> additions = new ArrayList<>();
            for (Entry entry : collect(false)) if (managed(entry.stack) && classicTab(entry.stack) == tab) additions.add(entry.stack);
            insertClassic(tab, items, additions);
        }
    }

    private static boolean calculateHasItems(CreativeTabs tab) {
        if (tab == null) return false;
        NonNullList<ItemStack> items = NonNullList.create();
        tab.displayAllRelevantItems(items);
        for (ItemStack stack : items) if (!stack.isEmpty()) return true;
        return false;
    }

    public static void refreshTabSnapshot() {
        install();
        ffdHasItemsSnapshot = calculateHasItems(FFDCreativeTab.INSTANCE);
        spawnEggsHasItemsSnapshot = modern() && spawnEggs != null && calculateHasItems(spawnEggs);
        List<CreativeTabs> result = new ArrayList<>();
        for (CreativeTabs tab : CreativeTabs.CREATIVE_TAB_ARRAY) if (tab != null && tab != spawnEggs
                && (tab != FFDCreativeTab.INSTANCE || ffdHasItemsSnapshot)
                && modernIndex(tab) < 0 && tab != CreativeTabs.SEARCH && tab != CreativeTabs.HOTBAR
                && tab != CreativeTabs.INVENTORY) result.add(tab);
        extraTabsSnapshot = result;
        creativeTabsSnapshot = CreativeTabs.CREATIVE_TAB_ARRAY.clone();
        snapshotMode = FFDConfig.creativeInventoryMode;
        tabSnapshotReady = true;
    }

    private static void ensureTabSnapshot() {
        if (!tabSnapshotReady || snapshotMode != FFDConfig.creativeInventoryMode
                || creativeTabsSnapshot == null || creativeTabsSnapshot.length != CreativeTabs.CREATIVE_TAB_ARRAY.length) {
            refreshTabSnapshot();
            return;
        }
        for (int index = 0; index < creativeTabsSnapshot.length; index++) {
            if (creativeTabsSnapshot[index] != CreativeTabs.CREATIVE_TAB_ARRAY[index]) {
                refreshTabSnapshot();
                return;
            }
        }
    }

    private static boolean managed(ItemStack stack) {
        ResourceLocation id = stack.getItem().getRegistryName();
        return id != null && FarmerFutureDelight.MODID.equals(id.getResourceDomain())
                || stack.getItem() == Items.ENCHANTED_BOOK;
    }

    private static List<Entry> collect(boolean vanilla) {
        List<Entry> result = new ArrayList<>();
        gathering = true;
        try {
            if (vanilla) for (CreativeTabs tab : ORIGINALS) collect(tab, result);
            collect(FFDCreativeTab.INSTANCE, result);
        } finally {
            gathering = false;
        }
        return result;
    }

    private static void collect(CreativeTabs tab, List<Entry> result) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        tab.displayAllRelevantItems(stacks);
        for (ItemStack stack : stacks) if (!stack.isEmpty() && stack.getItem().getRegistryName() != null)
            result.add(new Entry(stack, tab));
    }

    private static void addUnique(List<ItemStack> items, ItemStack stack) {
        for (ItemStack existing : items) if (ItemStack.areItemStacksEqual(existing, stack)) return;
        items.add(stack);
    }

    private static void insertClassic(CreativeTabs tab, NonNullList<ItemStack> items, List<ItemStack> additions) {
        List<String> order = CLASSIC_ORDER.getOrDefault(CLASSIC_NAMES[Arrays.asList(ORIGINALS).indexOf(tab)], Collections.emptyList());
        additions.sort(Comparator.comparingInt(stack -> classicRank(stack, order)));
        for (ItemStack addition : additions) {
            boolean exists = false;
            for (ItemStack existing : items) if (ItemStack.areItemStacksEqual(existing, addition)) exists = true;
            if (exists) continue;
            int wanted = classicRank(addition, order);
            if (wanted == Integer.MAX_VALUE) { items.add(addition); continue; }
            int insertion = -1;
            for (int index = 0; index < items.size(); index++) {
                if (classicRank(items.get(index), order) <= wanted) insertion = index + 1;
            }
            if (insertion < 0) for (int index = 0; index < items.size(); index++) {
                if (classicRank(items.get(index), order) < Integer.MAX_VALUE) { insertion = index; break; }
            }
            if (insertion < 0) insertion = items.size();
            items.add(insertion, addition);
        }
    }

    private static int classicRank(ItemStack stack, List<String> order) {
        String id = modernId(stack);
        if (id.equals("iron_chain")) id = "chain";
        int result = order.indexOf(id);
        if (result < 0 && id.contains("deepslate") && id.endsWith("_ore")) result = order.indexOf("deepslate_iron_ore");
        if (result < 0 && id.startsWith("raw_")) result = order.indexOf(id.endsWith("_block") ? "raw_iron_block" : "raw_iron");
        return result < 0 ? Integer.MAX_VALUE : result;
    }

    public static CreativeTabs classicTab(ItemStack stack) {
        String id = modernId(stack);
        Item item = stack.getItem();
        if (item == Items.ENCHANTED_BOOK) return CreativeTabs.COMBAT;
        String classicId = id.equals("iron_chain") ? "chain" : id;
        for (int index = 0; index < ORIGINALS.length; index++)
            if (CLASSIC_ORDER.get(CLASSIC_NAMES[index]).contains(classicId)) return ORIGINALS[index];
        if (item == Items.ENCHANTED_BOOK || item instanceof ItemArmor || item instanceof ItemSword
                || id.equals("trident") || id.equals("crossbow") || id.endsWith("_horse_armor")) return CreativeTabs.COMBAT;
        if (item instanceof ItemFood || categories(stack).contains("food_and_drinks")) return CreativeTabs.FOOD;
        if (item instanceof ItemMonsterPlacer || id.endsWith("_spawn_egg")) return CreativeTabs.MISC;
        if (id.endsWith("_boat")) return CreativeTabs.TRANSPORTATION;
        if (id.endsWith("_door") || id.endsWith("_trapdoor") || id.endsWith("_button")
                || id.endsWith("_pressure_plate") || id.endsWith("_fence_gate") || id.equals("target")
                || id.equals("lightning_rod") || id.equals("lectern")) return CreativeTabs.REDSTONE;
        if (item instanceof ItemTool || id.endsWith("_hoe") || id.equals("spyglass") || id.equals("goat_horn")
                || id.endsWith("_bucket") || id.endsWith("_sign") || id.endsWith("_music_disc") || id.startsWith("music_disc_")) return CreativeTabs.TOOLS;
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock)item).getBlock();
            Material material = block.getDefaultState().getMaterial();
            if (block instanceof BlockBush || material == Material.PLANTS || material == Material.VINE
                    || material == Material.LEAVES || material == Material.CORAL || id.contains("coral")
                    || id.endsWith("_mushroom") || id.endsWith("roots") || id.equals("kelp")
                    || categories(stack).contains("functional_blocks") && !id.equals("tinted_glass")) return CreativeTabs.DECORATIONS;
            if (id.equals("light") || id.equals("jigsaw") || id.startsWith("infested_")) return FFDCreativeTab.INSTANCE;
            return CreativeTabs.BUILDING_BLOCKS;
        }
        if (categories(stack).contains("ingredients")) return CreativeTabs.MISC;
        if (categories(stack).contains("natural_blocks")) return CreativeTabs.DECORATIONS;
        if (categories(stack).contains("building_blocks")) return CreativeTabs.BUILDING_BLOCKS;
        return FFDCreativeTab.INSTANCE;
    }

    public static List<String> categories(ItemStack stack) {
        String shared = sharedCategory(stack);
        if (shared != null) return Collections.singletonList(shared);
        String id = modernId(stack);
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : ORDER.entrySet()) if (entry.getValue().contains(id)) result.add(entry.getKey());
        if (result.isEmpty() && stack.getItem().getRegistryName() != null
                && FarmerFutureDelight.MODID.equals(stack.getItem().getRegistryName().getResourceDomain())) {
            if (id.contains("deepslate") && id.endsWith("_ore") || id.startsWith("raw_") && id.endsWith("_block")) result.add("natural_blocks");
            else if (id.startsWith("raw_")) result.add("ingredients");
            else if (id.endsWith("_banner_pattern")) result.add("ingredients");
            else if (id.equals("goat_horn")) result.add("tools_and_utilities");
            else if (id.endsWith("_spawn_egg")) result.add("spawn_eggs");
        }
        return result;
    }

    private static String sharedCategory(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemMonsterPlacer) return "spawn_eggs";
        if (item instanceof ItemBucket || item instanceof UniversalBucket) return "tools_and_utilities";
        return null;
    }

    private static int rank(ItemStack stack, String category) {
        String id = modernId(stack);
        List<String> order = ORDER.getOrDefault(category, Collections.emptyList());
        int result = order.indexOf(id);
        if (result >= 0) return result * 2;
        if (id.contains("deepslate") && id.endsWith("_ore")) result = order.indexOf("deepslate_iron_ore");
        else if (id.startsWith("raw_")) result = order.indexOf(id.endsWith("_block") ? "raw_iron_block" : "raw_iron");
        return result < 0 ? Integer.MAX_VALUE : result * 2 + 1;
    }

    public static String modernId(ItemStack stack) {
        ResourceLocation resource = stack.getItem().getRegistryName();
        if (resource == null) return "";
        String id = resource.getResourcePath();
        if (!"minecraft".equals(resource.getResourceDomain())) {
            if (id.equals("glowing_item_frame")) return "glow_item_frame";
            if (id.endsWith("_raw_ore")) return "raw_" + id.substring(0, id.length() - 8);
            if (id.endsWith("_raw_ore_block")) return "raw_" + id.substring(0, id.length() - 14) + "_block";
            return id;
        }
        int meta = stack.getMetadata();
        switch (id) {
            case "stone": return variant(meta, "stone", "granite", "polished_granite", "diorite", "polished_diorite", "andesite", "polished_andesite");
            case "dirt": return variant(meta, "dirt", "coarse_dirt", "podzol");
            case "planks": case "sapling": return WOODS[Math.floorMod(meta, 6)] + "_" + (id.equals("planks") ? "planks" : "sapling");
            case "log": case "leaves": return WOODS[meta & 3] + "_" + id;
            case "log2": case "leaves2": return WOODS[4 + (meta & 1)] + "_" + (id.equals("log2") ? "log" : "leaves");
            case "wooden_slab": return WOODS[Math.floorMod(meta, 6)] + "_slab";
            case "sand": return meta == 1 ? "red_sand" : "sand";
            case "sponge": return meta == 1 ? "wet_sponge" : "sponge";
            case "sandstone": return variant(meta, "sandstone", "chiseled_sandstone", "cut_sandstone");
            case "red_sandstone": return variant(meta, "red_sandstone", "chiseled_red_sandstone", "cut_red_sandstone");
            case "stonebrick": return variant(meta, "stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "chiseled_stone_bricks");
            case "monster_egg": return "infested_" + variant(meta, "stone", "cobblestone", "stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "chiseled_stone_bricks");
            case "stone_slab": return variant(meta & 7, "smooth_stone_slab", "sandstone_slab", "petrified_oak_slab", "cobblestone_slab", "brick_slab", "stone_brick_slab", "nether_brick_slab", "quartz_slab");
            case "stone_slab2": return "red_sandstone_slab";
            case "quartz_block": return variant(meta, "quartz_block", "chiseled_quartz_block", "quartz_pillar");
            case "prismarine": return variant(meta, "prismarine", "prismarine_bricks", "dark_prismarine");
            case "cobblestone_wall": return meta == 1 ? "mossy_cobblestone_wall" : "cobblestone_wall";
            case "wool": case "stained_glass": case "stained_glass_pane": case "carpet": case "concrete": case "concrete_powder": case "bed": return COLORS[meta & 15] + "_" + id;
            case "stained_hardened_clay": return COLORS[meta & 15] + "_terracotta";
            case "banner": return COLORS[15 - (meta & 15)] + "_banner";
            case "dye": return variant(meta, "ink_sac", "red_dye", "green_dye", "cocoa_beans", "lapis_lazuli", "purple_dye", "cyan_dye", "light_gray_dye", "gray_dye", "pink_dye", "lime_dye", "yellow_dye", "light_blue_dye", "magenta_dye", "orange_dye", "bone_meal");
            case "red_flower": return variant(meta, "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy");
            case "double_plant": return variant(meta, "sunflower", "lilac", "tall_grass", "large_fern", "rose_bush", "peony");
            case "tallgrass": return variant(meta, "dead_bush", "short_grass", "fern");
            case "fish": return variant(meta, "cod", "salmon", "tropical_fish", "pufferfish");
            case "cooked_fish": return meta == 1 ? "cooked_salmon" : "cooked_cod";
            case "coal": return meta == 1 ? "charcoal" : "coal";
            case "golden_apple": return meta == 1 ? "enchanted_golden_apple" : "golden_apple";
            case "skull": return variant(meta, "skeleton_skull", "wither_skeleton_skull", "zombie_head", "player_head", "creeper_head", "dragon_head");
            case "spawn_egg":
                ResourceLocation entity = ItemMonsterPlacer.getNamedIdFrom(stack);
                return entity == null ? "spawn_egg" : entity.getResourcePath().replace("evocation_illager", "evoker").replace("vindication_illager", "vindicator").replace("zombie_pigman", "zombified_piglin") + "_spawn_egg";
            case "wooden_door": return "oak_door";
            case "trapdoor": return "oak_trapdoor";
            case "wooden_button": return "oak_button";
            case "wooden_pressure_plate": return "oak_pressure_plate";
            case "fence": return "oak_fence";
            case "fence_gate": return "oak_fence_gate";
            case "boat": return "oak_boat";
            case "sign": return "oak_sign";
            case "brick_block": return "bricks";
            case "nether_brick": return "nether_bricks";
            case "red_nether_brick": return "red_nether_bricks";
            case "netherbrick": return "nether_brick";
            case "end_bricks": return "end_stone_bricks";
            case "hardened_clay": return "terracotta";
            case "yellow_flower": return "dandelion";
            case "deadbush": return "dead_bush";
            case "waterlily": return "lily_pad";
            case "reeds": return "sugar_cane";
            case "grass": return "grass_block";
            case "grass_path": return "dirt_path";
            case "snow_layer": return "snow";
            case "snow": return "snow_block";
            case "web": return "cobweb";
            case "slime": return "slime_block";
            case "magma": return "magma_block";
            case "lit_pumpkin": return "jack_o_lantern";
            case "pumpkin": return "carved_pumpkin";
            case "golden_rail": return "powered_rail";
            case "quartz_ore": return "nether_quartz_ore";
            case "noteblock": return "note_block";
            case "silver_shulker_box": return "light_gray_shulker_box";
            case "silver_glazed_terracotta": return "light_gray_glazed_terracotta";
            case "stone_stairs": return "cobblestone_stairs";
            case "stone_brick_stairs": return "stone_brick_stairs";
            case "melon_block": return "melon";
            case "melon": return "melon_slice";
            case "speckled_melon": return "glistering_melon_slice";
            case "fireworks": return "firework_rocket";
            case "firework_charge": return "firework_star";
            case "potion": return "potion";
            case "chorus_fruit_popped": return "popped_chorus_fruit";
            case "slime_ball": return "slime_ball";
            case "record_13": case "record_cat": case "record_blocks": case "record_chirp": case "record_far": case "record_mall": case "record_mellohi": case "record_stal": case "record_strad": case "record_ward": case "record_11": case "record_wait": return "music_disc_" + id.substring(7);
            default: return id;
        }
    }

    private static String variant(int meta, String... values) {
        return values[Math.floorMod(meta, values.length)];
    }

    public static int modernIndex(CreativeTabs tab) {
        if (tab == spawnEggs && tab != null) return 9;
        for (int index = 0; index < MODERN_TABS.length; index++) if (MODERN_TABS[index] == tab) return index;
        return -1;
    }

    private static CreativeTabs modernDestination(CreativeTabs original) {
        if (original == CreativeTabs.DECORATIONS) return CreativeTabs.MISC;
        if (original == CreativeTabs.TRANSPORTATION) return CreativeTabs.TOOLS;
        if (original == CreativeTabs.MISC) return CreativeTabs.BREWING;
        if (original == CreativeTabs.BREWING) return CreativeTabs.FOOD;
        return original;
    }

    public static CreativeTabs displayTab(CreativeTabs original, ItemStack stack) {
        if (FFDConfig.creativeInventoryMode == FFDConfig.CreativeInventoryMode.MOD_TAB) return original;
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null) return original;
        if (!modern()) return FarmerFutureDelight.MODID.equals(id.getResourceDomain()) ? classicTab(stack) : original;
        String shared = sharedCategory(stack);
        if (shared != null && Arrays.asList(ORIGINALS).contains(original)) {
            return "spawn_eggs".equals(shared) ? spawnEggs : CreativeTabs.TOOLS;
        }
        if ("minecraft".equals(id.getResourceDomain()) || FarmerFutureDelight.MODID.equals(id.getResourceDomain())) {
            List<String> groups = categories(stack);
            if (!groups.isEmpty()) {
                int index = Arrays.asList(CATEGORIES).indexOf(groups.get(0));
                return index == 9 ? spawnEggs : MODERN_TABS[index];
            }
        }
        return modernDestination(original);
    }

    public static String tabLabel(String original, CreativeTabs tab) {
        int index = modern() ? modernIndex(tab) : -1;
        return index < 0 ? original : "itemGroup.farmers_future_delight." + CATEGORIES[index];
    }

    public static ItemStack tabIcon(ItemStack original, CreativeTabs tab) {
        if (!modern()) return original;
        switch (modernIndex(tab)) {
            case 0: return new ItemStack(Blocks.BRICK_BLOCK);
            case 1: return new ItemStack(Blocks.WOOL, 1, 9);
            case 2: return new ItemStack(Blocks.GRASS);
            case 3: return new ItemStack(Items.SIGN);
            case 4: return new ItemStack(Items.REDSTONE);
            case 5: return new ItemStack(Items.DIAMOND_PICKAXE);
            case 6:
                Item sword = Item.REGISTRY.getObject(new ResourceLocation(FarmerFutureDelight.MODID, "netherite_sword"));
                return new ItemStack(sword == null || sword == Items.AIR ? Items.DIAMOND_SWORD : sword);
            case 7: return new ItemStack(Items.GOLDEN_APPLE);
            case 8: return new ItemStack(Items.IRON_INGOT);
            default: return original;
        }
    }

    public static List<CreativeTabs> extraTabs() {
        ensureTabSnapshot();
        return extraTabsSnapshot;
    }

    public static int tabPage(int original, CreativeTabs tab) {
        ensureTabSnapshot();
        if (!modern() || spawnEggs == null) return original;
        if (modernIndex(tab) >= 0 || tab == CreativeTabs.SEARCH || tab == CreativeTabs.HOTBAR || tab == CreativeTabs.INVENTORY) return 0;
        int index = extraTabs().indexOf(tab);
        return index < 0 ? original : 1 + index / 10;
    }

    public static Object[] pageTabs(Object[] original, int from, int to) {
        ensureTabSnapshot();
        if (!modern() || spawnEggs == null) {
            CreativeTabs[] result = Arrays.copyOfRange(original, from, to, CreativeTabs[].class);
            if (ffdHasItemsSnapshot) return result;
            List<CreativeTabs> filtered = new ArrayList<>();
            for (CreativeTabs tab : result) if (tab != FFDCreativeTab.INSTANCE) filtered.add(tab);
            return filtered.toArray(new CreativeTabs[0]);
        }
        int page = from / 10;
        if (page == 0) {
            List<CreativeTabs> result = new ArrayList<>(Arrays.asList(MODERN_TABS));
            if (spawnEggsHasItemsSnapshot) result.add(spawnEggs);
            Collections.addAll(result, CreativeTabs.HOTBAR, CreativeTabs.SEARCH, CreativeTabs.INVENTORY);
            return result.toArray(new CreativeTabs[0]);
        }
        List<CreativeTabs> extras = extraTabs();
        int start = Math.min(extras.size(), (page - 1) * 10);
        return extras.subList(start, Math.min(extras.size(), start + 10)).toArray(new CreativeTabs[0]);
    }

    private static Map<String, List<String>> loadOrder(String file) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        try (InputStreamReader reader = new InputStreamReader(FFDCreativeInventory.class.getResourceAsStream(
                "/assets/farmers_future_delight/" + file), StandardCharsets.UTF_8)) {
            JsonObject data = new JsonParser().parse(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                List<String> items = new ArrayList<>();
                for (JsonElement item : entry.getValue().getAsJsonArray()) items.add(item.getAsString());
                result.put(entry.getKey(), items);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load creative inventory ordering", exception);
        }
        return result;
    }

    private static final class Entry {
        private final ItemStack stack;
        private final CreativeTabs tab;

        private Entry(ItemStack stack, CreativeTabs tab) {
            this.stack = stack;
            this.tab = tab;
        }
    }
}
