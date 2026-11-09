package xy177.farmersfuturedelight.common.registry;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class FFDModernItemLookup {
    private static final Map<String, ItemStack> LEGACY = new HashMap<>();

    static {
        map("cod", new ItemStack(Items.FISH));
        map("salmon", new ItemStack(Items.FISH, 1, 1));
        map("tropical_fish", new ItemStack(Items.FISH, 1, 2));
        map("pufferfish", new ItemStack(Items.FISH, 1, 3));
        map("cooked_cod", new ItemStack(Items.COOKED_FISH));
        map("cooked_salmon", new ItemStack(Items.COOKED_FISH, 1, 1));
        map("charcoal", new ItemStack(Items.COAL, 1, 1));
        map("sugar_cane", new ItemStack(Items.REEDS));
        map("terracotta", new ItemStack(Blocks.HARDENED_CLAY));
        map("stone_brick_stairs", new ItemStack(Blocks.STONE_BRICK_STAIRS));
        map("cobblestone_stairs", new ItemStack(Blocks.STONE_STAIRS));
        map("mossy_cobblestone", new ItemStack(Blocks.MOSSY_COBBLESTONE));
        map("cobblestone_wall", new ItemStack(Blocks.COBBLESTONE_WALL));
        map("mossy_cobblestone_wall", new ItemStack(Blocks.COBBLESTONE_WALL, 1, 1));
        map("sandstone_slab", new ItemStack(Blocks.STONE_SLAB, 1, 1));
        map("cobblestone_slab", new ItemStack(Blocks.STONE_SLAB, 1, 3));
        map("brick_slab", new ItemStack(Blocks.STONE_SLAB, 1, 4));
        map("stone_brick_slab", new ItemStack(Blocks.STONE_SLAB, 1, 5));
        map("nether_brick_slab", new ItemStack(Blocks.STONE_SLAB, 1, 6));
        map("quartz_slab", new ItemStack(Blocks.STONE_SLAB, 1, 7));
        map("red_sandstone_slab", new ItemStack(Blocks.STONE_SLAB2));
        map("lapis_lazuli", new ItemStack(Items.DYE, 1, 4));
        map("bone_meal", new ItemStack(Items.DYE, 1, 15));
        map("cocoa_beans", new ItemStack(Items.DYE, 1, 3));
        map("nether_brick", new ItemStack(Items.NETHERBRICK));
        map("nether_quartz_ore", new ItemStack(Blocks.QUARTZ_ORE));
        map("bricks", new ItemStack(Blocks.BRICK_BLOCK));
        map("nether_bricks", new ItemStack(Blocks.NETHER_BRICK));
        map("red_nether_bricks", new ItemStack(Blocks.RED_NETHER_BRICK));
        map("stone_bricks", new ItemStack(Blocks.STONEBRICK));
        map("mossy_stone_bricks", new ItemStack(Blocks.STONEBRICK, 1, 1));
        map("cracked_stone_bricks", new ItemStack(Blocks.STONEBRICK, 1, 2));
        map("chiseled_stone_bricks", new ItemStack(Blocks.STONEBRICK, 1, 3));
        map("end_stone_bricks", new ItemStack(Blocks.END_BRICKS));
        map("chiseled_sandstone", new ItemStack(Blocks.SANDSTONE, 1, 1));
        map("cut_sandstone", new ItemStack(Blocks.SANDSTONE, 1, 2));
        map("chiseled_red_sandstone", new ItemStack(Blocks.RED_SANDSTONE, 1, 1));
        map("cut_red_sandstone", new ItemStack(Blocks.RED_SANDSTONE, 1, 2));
        map("prismarine_bricks", new ItemStack(Blocks.PRISMARINE, 1, 1));
        map("dark_prismarine", new ItemStack(Blocks.PRISMARINE, 1, 2));
        map("chiseled_quartz_block", new ItemStack(Blocks.QUARTZ_BLOCK, 1, 1));
        map("quartz_pillar", new ItemStack(Blocks.QUARTZ_BLOCK, 1, 2));
        map("dandelion", new ItemStack(Blocks.YELLOW_FLOWER));
        map("dead_bush", new ItemStack(Blocks.DEADBUSH));
        map("snow_block", new ItemStack(Blocks.SNOW));
        map("short_grass", new ItemStack(Blocks.TALLGRASS, 1, 1));
        map("fern", new ItemStack(Blocks.TALLGRASS, 1, 2));
        map("melon", new ItemStack(Blocks.MELON_BLOCK));
        map("melon_slice", new ItemStack(Items.MELON));
        map("lily_pad", new ItemStack(Blocks.WATERLILY));
        map("carved_pumpkin", new ItemStack(Blocks.PUMPKIN));
        map("podzol", new ItemStack(Blocks.DIRT, 1, 2));
        map("red_sand", new ItemStack(Blocks.SAND, 1, 1));
        map("ocean_monument_map", new ItemStack(Items.FILLED_MAP));
        map("woodland_mansion_map", new ItemStack(Items.FILLED_MAP));
        map("taiga_village_map", new ItemStack(Items.FILLED_MAP));
        map("snowy_village_map", new ItemStack(Items.FILLED_MAP));
        map("savanna_village_map", new ItemStack(Items.FILLED_MAP));
        map("plains_village_map", new ItemStack(Items.FILLED_MAP));
        map("desert_village_map", new ItemStack(Items.FILLED_MAP));
        map("swamp_hut_map", new ItemStack(Items.FILLED_MAP));
        map("jungle_pyramid_map", new ItemStack(Items.FILLED_MAP));
        String[] stones = {"stone", "granite", "polished_granite", "diorite", "polished_diorite",
                "andesite", "polished_andesite"};
        for (int index = 0; index < stones.length; index++) {
            map(stones[index], new ItemStack(Blocks.STONE, 1, index));
        }
        String[] flowers = {"poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip",
                "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy"};
        for (int index = 0; index < flowers.length; index++) {
            map(flowers[index], new ItemStack(Blocks.RED_FLOWER, 1, index));
        }
        String[] doublePlants = {"sunflower", "lilac", "tall_grass", "large_fern", "rose_bush", "peony"};
        for (int index = 0; index < doublePlants.length; index++) {
            map(doublePlants[index], new ItemStack(Blocks.DOUBLE_PLANT, 1, index));
        }
        for (int index = 0; index < FFDBlocks.OVERWORLD_WOOD_NAMES.length; index++) {
            String wood = FFDBlocks.OVERWORLD_WOOD_NAMES[index];
            map(wood + "_leaves", new ItemStack(index < 4 ? Blocks.LEAVES : Blocks.LEAVES2, 1, index % 4));
            map(wood + "_log", new ItemStack(index < 4 ? Blocks.LOG : Blocks.LOG2, 1, index % 4));
            map(wood + "_sapling", new ItemStack(Blocks.SAPLING, 1, index));
            map(wood + "_planks", new ItemStack(Blocks.PLANKS, 1, index));
            map(wood + "_slab", new ItemStack(Blocks.WOODEN_SLAB, 1, index));
        }
        for (EnumDyeColor color : EnumDyeColor.values()) {
            String name = color == EnumDyeColor.SILVER ? "light_gray" : color.getName();
            map(name + "_wool", new ItemStack(Blocks.WOOL, 1, color.getMetadata()));
            map(name + "_carpet", new ItemStack(Blocks.CARPET, 1, color.getMetadata()));
            map(name + "_terracotta", new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, color.getMetadata()));
            if (color != EnumDyeColor.WHITE && color != EnumDyeColor.BLUE
                    && color != EnumDyeColor.BROWN && color != EnumDyeColor.BLACK) {
                map(name + "_dye", new ItemStack(Items.DYE, 1, color.getDyeDamage()));
            }
        }
    }

    private FFDModernItemLookup() {
    }

    private static void map(String name, ItemStack stack) {
        LEGACY.put(name, stack);
    }

    public static ItemStack resolve(String name) {
        ResourceLocation id = new ResourceLocation(name);
        if (!"minecraft".equals(id.getResourceDomain())) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }
        String path = id.getResourcePath();
        if ("stone_stairs".equals(path) || "stone_slab".equals(path)) {
            Item local = FFDItems.localBlockItem(new ResourceLocation(FarmerFutureDelight.MODID, path));
            return local == null ? ItemStack.EMPTY : FFDItems.effectiveStack(local);
        }
        ItemStack mapped = LEGACY.get(path);
        if (mapped != null) {
            return mapped.copy();
        }
        if ("kelp".equals(path)) return FFDItems.effectiveStack(FFDItems.KELP);
        if ("white_dye".equals(path)) return FFDItems.effectiveStack(FFDItems.WHITE_DYE);
        if ("blue_dye".equals(path)) return FFDItems.effectiveStack(FFDItems.BLUE_DYE);
        if ("brown_dye".equals(path)) return FFDItems.effectiveStack(FFDItems.BROWN_DYE);
        if ("black_dye".equals(path)) return FFDItems.effectiveStack(FFDItems.BLACK_DYE);
        if ("tropical_fish_bucket".equals(path)) return FFDItems.effectiveStack(FFDItems.TROPICAL_FISH_BUCKET);
        if ("pufferfish_bucket".equals(path)) return FFDItems.effectiveStack(FFDItems.PUFFERFISH_BUCKET);
        if ("nautilus_shell".equals(path)) return FFDItems.effectiveStack(FFDItems.NAUTILUS_SHELL);
        if ("sea_pickle".equals(path)) return FFDItems.effectiveStack(FFDItems.SEA_PICKLE);
        if ("dried_kelp".equals(path)) return FFDItems.effectiveStack(FFDItems.DRIED_KELP);
        if ("sweet_berries".equals(path)) return FFDItems.effectiveStack(FFDItems.SWEET_BERRIES);
        if ("glow_berries".equals(path)) return FFDItems.effectiveStack(FFDItems.GLOW_BERRIES);
        if ("raw_gold".equals(path)) return FFDItems.effectiveStack(FFDItems.RAW_GOLD);
        if ("raw_iron".equals(path)) return FFDItems.effectiveStack(FFDItems.RAW_IRON);
        if ("raw_copper".equals(path)) return FFDItems.effectiveStack(FFDItems.RAW_COPPER);
        if ("pumpkin".equals(path)) return FFDItems.effectiveStack(FFDItems.PUMPKIN);
        if ("golden_dandelion".equals(path)) return FFDItems.effectiveStack(FFDItems.GOLDEN_DANDELION);
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        ResourceLocation localId = new ResourceLocation(FarmerFutureDelight.MODID, path);
        item = FFDItems.localBlockItem(localId);
        if (item != null) {
            return FFDItems.effectiveStack(item);
        }
        item = ForgeRegistries.ITEMS.getValue(localId);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }
}
