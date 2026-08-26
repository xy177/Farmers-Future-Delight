package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public final class FFDRawOreOreDictionaryCompat {
    private static final String[][] SOURCE_MODS = {
            {"copper", "thermalfoundation", "mekanism", "ic2", "ic2-classic-spmod",
                    "immersiveengineering", "enderio", "galacticraftcore", "nuclearcraft",
                    "techguns", "techreborn", "deeperdepths", "mw", "basemetals"},
            {"tin", "thermalfoundation", "mekanism", "ic2", "ic2-classic-spmod", "enderio",
                    "galacticraftcore", "nuclearcraft", "techreborn", "basemetals"},
            {"zinc", "metallurgy", "techreborn", "basemetals", "sakura"},
            {"lead", "thermalfoundation", "ic2", "ic2-classic-spmod",
                    "immersiveengineering", "enderio", "nuclearcraft", "techguns",
                    "techreborn", "basemetals"},
            {"silver", "thermalfoundation", "immersiveengineering", "enderio", "techreborn",
                    "metallurgy", "basemetals"},
            {"cobalt", "tconstruct", "galaxyspace", "metallurgy", "gregtech"},
            {"osmium", "mekanism", "gregtech"},
            {"nickel", "thermalfoundation", "immersiveengineering", "enderio", "nuclearcraft",
                    "metallurgy", "gregtech", "basemetals"},
            {"iridium", "thermalfoundation", "advancedrocketry", "gregtech", "iridiumsource",
                    "techreborn"},
            {"uranium", "ic2", "ic2-classic-spmod", "immersiveengineering", "nuclearcraft",
                    "techguns", "gregtech", "techreborn"},
            {"gallium", "gregtech", "techreborn"},
            {"titanium", "advancedrocketry", "libvulpes", "techguns", "galacticraftplanets",
                    "galaxyspace", "gregtech", "mw"},
            {"platinum", "thermalfoundation", "enderio", "nuclearcraft", "gregtech",
                    "iridiumsource", "techreborn", "basemetals"},
            {"tungsten", "metallurgy", "gregtech", "techreborn", "basemetals"},
            {"aluminium", "thermalfoundation", "immersiveengineering", "galacticraftcore",
                    "advancedrocketry", "libvulpes", "nuclearcraft", "techguns", "gregtech",
                    "techreborn", "basemetals"},
            {"magnesium", "nuclearcraft", "galaxyspace", "gregtech"},
            {"lithium", "nuclearcraft", "gregtech", "industrialupgrade", "techreborn"},
            {"thorium", "nuclearcraft", "ic2-classic-spmod", "ic2c_extras", "gregtech"},
            {"boron", "nuclearcraft", "gregtech"},
            {"vanadium", "gregtech", "metallurgy"},
            {"cadmium", "gregtech", "metallurgy"},
            {"manganese", "gregtech", "metallurgy"},
            {"germanium", "gregtech"},
            {"chromium", "gregtech", "metallurgy", "techreborn"},
            {"arsenic", "gregtech"},
            {"beryllium", "gregtech"},
            {"irradium", "advancedrocketry", "libvulpes", "gregtech"},
            {"palladium", "advancedrocketry", "libvulpes", "gregtech"},
            {"plutonium", "ic2", "nuclearcraft", "gregtech", "techreborn"},
            {"niobium", "gregtech", "mist"},
            {"mithril", "thermalfoundation", "simpleores", "metallurgy", "basemetals"},
            {"rutile", "advancedrocketry", "libvulpes", "gregtech"},
            {"ardite", "tconstruct"},
            {"cerulean", "theaurorian"},
            {"moonstone", "theaurorian"},
            {"octine", "thebetweenlands"},
            {"syrmorite", "thebetweenlands"},
            {"cinnabar", "thaumcraft"},
            {"vulcanite", "vulcanite"},
            {"chasmium", "mm"},
            {"rosegold", "mca"}
    };

    private static final String[][] ENTRIES = {
            {"oreArdite", "tconstruct:ore", "1"},
            {"oreCerulean", "theaurorian:ceruleanore", "0"},
            {"oreMoonstone", "theaurorian:moonstoneore", "0"},
            {"oreOctine", "thebetweenlands:octine_ore", "0"},
            {"oreSyrmorite", "thebetweenlands:syrmorite_ore", "0"},
            {"oreCinnabar", "thaumcraft:ore_cinnabar", "0"},
            {"oreVulcanite", "vulcanite:vulcanite_ore", "0"},
            {"oreVulcanite", "vulcanite:nether_vulcanite_ore", "0"},
            {"oreChasmium", "mm:chasmium_ore", "0"},
            {"oreRosegold", "mca:rose_gold_ore", "0"},
            {"ingotArdite", "tconstruct:ingots", "1"},
            {"ingotCerulean", "theaurorian:ceruleaningot", "0"},
            {"ingotMoonstone", "theaurorian:moonstoneingot", "0"},
            {"ingotOctine", "thebetweenlands:octine_ingot", "0"},
            {"ingotSyrmorite", "thebetweenlands:items_misc", "11"},
            {"ingotCinnabar", "thaumcraft:quicksilver", "0"},
            {"ingotVulcanite", "vulcanite:vulcanite_ingot", "0"},
            {"ingotChasmium", "mm:chasmium_ingot", "0"},
            {"ingotRosegold", "mca:rose_gold_ingot", "0"},
            {"oreGoldDense", "densemetals:dense_gold_ore", "0"},
            {"oreIronDense", "densemetals:dense_iron_ore", "0"},
            {"oreCopperDense", "densemetals:dense_copper_ore", "0"},
            {"oreTinDense", "densemetals:dense_tin_ore", "0"},
            {"oreZincDense", "densemetals:dense_zinc_ore", "0"},
            {"oreLeadDense", "densemetals:dense_lead_ore", "0"},
            {"oreSilverDense", "densemetals:dense_silver_ore", "0"},
            {"oreOsmiumDense", "densemetals:dense_osmium_ore", "0"},
            {"oreNickelDense", "densemetals:dense_nickel_ore", "0"},
            {"oreIridiumDense", "densemetals:dense_iridium_ore", "0"},
            {"oreUraniumDense", "densemetals:dense_uranium_ore", "0"},
            {"orePlatinumDense", "densemetals:dense_platinum_ore", "0"},
            {"oreTungstenDense", "densemetals:dense_tungsten_ore", "0"},
            {"oreAluminiumDense", "densemetals:dense_aluminum_ore", "0"},
            {"oreMagnesiumDense", "densemetals:dense_magnesium_ore", "0"},
            {"dustRareEarth", "ic2:itemmisc", "14"},
            {"oreLithium", "industrialupgrade:baseore1", "0"},
            {"polyPropylene", "industrialupgrade:crafting_elements", "484"},
            {"oreTitanium", "galacticraftplanets:asteroids_block", "4"},
            {"blockCobalt", "galaxyspace:blocksmetals", "0"},
            {"blockNickel", "galaxyspace:blocksmetals", "1"},
            {"blockMagnesium", "galaxyspace:blocksmetals", "2"},
            {"nuggetCobalt", "galaxyspace:nuggets", "0"},
            {"nuggetNickel", "galaxyspace:nuggets", "2"},
            {"nuggetMagnesium", "galaxyspace:nuggets", "1"},
            {"compressedCobalt", "galaxyspace:compressed_plates", "1"},
            {"compressedNickel", "galaxyspace:compressed_plates", "3"},
            {"compressedMagnesium", "galaxyspace:compressed_plates", "2"},
            {"oreGold", "mm:azure_gold_ore", "0"},
            {"oreIron", "mm:azure_iron_ore", "0"},
            {"orePlatinum", "iridiumsource:ore_overworld", "0"},
            {"orePlatinum", "iridiumsource:ore_nether", "0"},
            {"orePlatinum", "iridiumsource:ore_end", "0"},
            {"prillPlatinum", "iridiumsource:prill_platina", "0"},
            {"ingotCopper", "mw:copperingot", "0"},
            {"oreCopper", "mw:copperore", "0"},
            {"oreTin", "mw:tinore", "0"},
            {"oreLead", "mw:leadore", "0"},
            {"oreTitanium", "mw:titaniumore", "0"},
            {"oreAluminium", "mw:bauxiteore", "0"},
            {"oreTantalum", "mw:tantalumore", "0"},
            {"oreCopper", "deeperdepths:copper_ore", "0"},
            {"rawAluminium", "chinjufumod:item_bauxite", "0"},
            {"rawAluminium", "chinjufumod:block_bauxite_ore", "0"},
            {"oreMithril", "simpleores:mythril_ore", "0"},
            {"ingotMithril", "simpleores:mythril_ingot", "0"},
            {"oreTitanium", "techguns:basicore", "3"},
            {"dustRareEarth", "thaumcraft:nugget", "10"},
            {"clusterIron", "thaumcraft:cluster", "0"},
            {"clusterGold", "thaumcraft:cluster", "1"},
            {"clusterCopper", "thaumcraft:cluster", "2"},
            {"clusterTin", "thaumcraft:cluster", "3"},
            {"clusterLead", "thaumcraft:cluster", "5"},
            {"clusterSilver", "thaumcraft:cluster", "4"},
            {"clusterCinnabar", "thaumcraft:cluster", "6"},
            {"crushedCinnabar", "emt:materials_crushedorecinnabar", "0"}
    };

    private static final String[][] ALIASES = {
            {"Aluminum", "Aluminium"},
            {"Chrome", "Chromium"},
            {"Mythril", "Mithril"},
            {"Thorium232", "Thorium"}
    };

    private static final String[] ALIAS_PREFIXES = {
            "ore", "ingot", "block", "nugget", "dust", "shard", "compressed", "plate", "stick"
    };

    private FFDRawOreOreDictionaryCompat() {
    }

    public static void register() {
        for (String[] entry : ENTRIES) {
            register(entry[0], entry[1], Integer.parseInt(entry[2]));
        }
        for (String[] alias : ALIASES) {
            for (String prefix : ALIAS_PREFIXES) {
                merge(prefix + alias[0], prefix + alias[1]);
            }
        }
        copy("nuggetRareEarth", "dustRareEarth");
    }

    public static boolean willRawOreProvide(String... paths) {
        if (!Loader.isModLoaded("suikerawore")) {
            return false;
        }
        String material = rawOreMaterial(paths);
        if (material == null) {
            return false;
        }
        if ("gold".equals(material) || "iron".equals(material)
                || "copper".equals(material)) {
            return true;
        }
        for (String oreName : oreNames(material)) {
            if (!OreDictionary.getOres(oreName).isEmpty() || hasRegisteredSource(oreName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasSourceOre(String material) {
        if ("gold".equals(material) || "iron".equals(material)) {
            return true;
        }
        for (String oreName : oreNames(material)) {
            if (!OreDictionary.getOres(oreName).isEmpty() || hasRegisteredSource(oreName)) {
                return true;
            }
        }
        for (String[] source : SOURCE_MODS) {
            if (!material.equals(source[0])) {
                continue;
            }
            for (int i = 1; i < source.length; i++) {
                if (Loader.isModLoaded(source[i])) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private static String rawOreMaterial(String... paths) {
        if (paths == null) {
            return null;
        }
        for (String path : paths) {
            if (path == null) {
                continue;
            }
            String material = null;
            if (path.startsWith("raw_block_")) {
                material = path.substring("raw_block_".length());
            } else if (path.startsWith("raw_") && path.endsWith("_block")) {
                material = path.substring("raw_".length(), path.length() - "_block".length());
            } else if (path.startsWith("raw_")) {
                material = path.substring("raw_".length());
            }
            if (material != null) {
                for (String name : FFDRawOres.NAMES) {
                    if (name.equals(material)) {
                        return material;
                    }
                }
            }
        }
        return null;
    }

    private static String[] oreNames(String material) {
        if ("aluminium".equals(material)) {
            return new String[] {"oreAluminium", "oreAluminum"};
        }
        if ("chromium".equals(material)) {
            return new String[] {"oreChromium", "oreChrome"};
        }
        if ("mithril".equals(material)) {
            return new String[] {"oreMithril", "oreMythril"};
        }
        if ("thorium".equals(material)) {
            return new String[] {"oreThorium", "oreThorium232"};
        }
        if ("rosegold".equals(material)) {
            return new String[] {"oreRosegold", "oreRoseGold"};
        }
        return new String[] {"ore" + FFDRawOres.capitalize(material)};
    }

    private static boolean hasRegisteredSource(String oreName) {
        for (String[] entry : ENTRIES) {
            if (!oreName.equals(entry[0])) {
                continue;
            }
            ResourceLocation id = new ResourceLocation(entry[1]);
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && item != Items.AIR && id.equals(item.getRegistryName())) {
                return true;
            }
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            if (block != null && id.equals(block.getRegistryName())) {
                return true;
            }
        }
        return false;
    }

    private static void register(String oreName, String registryName, int metadata) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
        if (item == null || item == Items.AIR) {
            return;
        }
        register(oreName, new ItemStack(item, 1, metadata));
    }

    private static void merge(String first, String second) {
        List<ItemStack> stacks = new ArrayList<>();
        collect(stacks, first);
        collect(stacks, second);
        for (ItemStack stack : stacks) {
            register(first, stack);
            register(second, stack);
        }
    }

    private static void copy(String source, String target) {
        List<ItemStack> stacks = new ArrayList<>();
        collect(stacks, source);
        for (ItemStack stack : stacks) {
            register(target, stack);
        }
    }

    private static void collect(List<ItemStack> stacks, String oreName) {
        for (ItemStack stack : OreDictionary.getOres(oreName)) {
            if (!stack.isEmpty() && !contains(stacks, stack)) {
                stacks.add(stack.copy());
            }
        }
    }

    private static void register(String oreName, ItemStack stack) {
        if (!stack.isEmpty() && !contains(OreDictionary.getOres(oreName), stack)) {
            OreDictionary.registerOre(oreName, stack.copy());
        }
    }

    private static boolean contains(List<ItemStack> stacks, ItemStack target) {
        for (ItemStack stack : stacks) {
            if (ItemStack.areItemsEqual(stack, target)
                    && ItemStack.areItemStackTagsEqual(stack, target)) {
                return true;
            }
        }
        return false;
    }
}
