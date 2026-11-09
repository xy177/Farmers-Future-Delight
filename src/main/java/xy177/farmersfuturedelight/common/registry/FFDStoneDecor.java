package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockFutureSlab;
import xy177.farmersfuturedelight.common.block.BlockFutureStairs;
import xy177.farmersfuturedelight.common.block.BlockFutureStone;
import xy177.farmersfuturedelight.common.block.BlockModernWall;

public final class FFDStoneDecor {
    private static final List<Block> BLOCKS = new ArrayList<>();
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<Block, ItemStack> WALLS = new java.util.LinkedHashMap<>();
    private static final Map<Block, Item> ITEMS = new IdentityHashMap<>();

    public static final Block SMOOTH_STONE = smooth("smooth_stone", MapColor.STONE);
    public static final Block SMOOTH_SANDSTONE = smooth("smooth_sandstone", MapColor.SAND);
    public static final Block SMOOTH_RED_SANDSTONE = smooth("smooth_red_sandstone", MapColor.ADOBE);
    public static final Block SMOOTH_QUARTZ = smooth("smooth_quartz", MapColor.QUARTZ);

    static {
        family("polished_granite", Blocks.STONE, 2, 1.5F, 6.0F, true);
        family("smooth_red_sandstone", SMOOTH_RED_SANDSTONE, 0, 2.0F, 6.0F, true);
        family("mossy_stone_brick", Blocks.STONEBRICK, 1, 1.5F, 6.0F, true);
        family("polished_diorite", Blocks.STONE, 4, 1.5F, 6.0F, true);
        family("mossy_cobblestone", Blocks.MOSSY_COBBLESTONE, 0, 2.0F, 6.0F, true);
        family("end_stone_brick", Blocks.END_BRICKS, 0, 3.0F, 9.0F, true);
        family("stone", Blocks.STONE, 0, 1.5F, 6.0F, true);
        family("smooth_sandstone", SMOOTH_SANDSTONE, 0, 2.0F, 6.0F, true);
        family("smooth_quartz", SMOOTH_QUARTZ, 0, 2.0F, 6.0F, true);
        family("granite", Blocks.STONE, 1, 1.5F, 6.0F, true);
        family("andesite", Blocks.STONE, 5, 1.5F, 6.0F, true);
        family("red_nether_brick", Blocks.RED_NETHER_BRICK, 0, 2.0F, 6.0F, true);
        family("polished_andesite", Blocks.STONE, 6, 1.5F, 6.0F, true);
        family("diorite", Blocks.STONE, 3, 1.5F, 6.0F, true);
        family("cut_sandstone", Blocks.SANDSTONE, 2, 0.8F, 0.8F, false);
        family("cut_red_sandstone", Blocks.RED_SANDSTONE, 2, 0.8F, 0.8F, false);
        slabFamily("smooth_stone_slab", SMOOTH_STONE, 2.0F, 6.0F);
        wall("brick", Blocks.BRICK_BLOCK, 0, 2.0F, 6.0F);
        wall("prismarine", Blocks.PRISMARINE, 0, 1.5F, 6.0F);
        wall("red_sandstone", Blocks.RED_SANDSTONE, 0, 0.8F, 0.8F);
        wall("mossy_stone_brick", Blocks.STONEBRICK, 1, 1.5F, 6.0F);
        wall("granite", Blocks.STONE, 1, 1.5F, 6.0F);
        wall("stone_brick", Blocks.STONEBRICK, 0, 1.5F, 6.0F);
        wall("nether_brick", Blocks.NETHER_BRICK, 0, new ItemStack(Items.NETHERBRICK),
                2.0F, 6.0F);
        wall("andesite", Blocks.STONE, 5, 1.5F, 6.0F);
        wall("red_nether_brick", Blocks.RED_NETHER_BRICK, 0, 2.0F, 6.0F);
        wall("sandstone", Blocks.SANDSTONE, 0, 0.8F, 0.8F);
        wall("end_stone_brick", Blocks.END_BRICKS, 0, 3.0F, 9.0F);
        wall("diorite", Blocks.STONE, 3, 1.5F, 6.0F);
    }

    private FFDStoneDecor() {
    }

    private static Block smooth(String name, MapColor color) {
        Block block = new BlockFutureStone(name, 2.0F, 6.0F, SoundType.STONE).setMapColor(color);
        BLOCKS.add(block);
        return block;
    }

    private static void family(String name, Block material, int metadata, float hardness,
                               float resistance, boolean hasStairs) {
        IBlockState base = material.getStateFromMeta(metadata);
        BlockFutureStairs stairs = hasStairs
                ? new BlockFutureStairs(name + "_stairs", base, SoundType.STONE) : null;
        BlockFutureSlab.Half slab = new BlockFutureSlab.Half(name + "_slab", SoundType.STONE);
        BlockFutureSlab.Double doubleSlab = new BlockFutureSlab.Double(
                name + "_double_slab", SoundType.STONE, slab);
        if (stairs != null) {
            BLOCKS.add(stairs);
        }
        BLOCKS.add(slab);
        BLOCKS.add(doubleSlab);
        slab.setMapColor(base.getMapColor(null, net.minecraft.util.math.BlockPos.ORIGIN));
        doubleSlab.setMapColor(base.getMapColor(null, net.minecraft.util.math.BlockPos.ORIGIN));
        for (Block block : new Block[] {stairs, slab, doubleSlab}) {
            if (block != null) {
                block.setHardness(hardness);
                block.setResistance(resistance / 3.0F);
            }
        }
        ENTRIES.add(new Entry(material, metadata, stairs, slab, doubleSlab));
    }

    private static void slabFamily(String name, Block material, float hardness,
                                   float resistance) {
        BlockFutureSlab.Half slab = new BlockFutureSlab.Half(name, SoundType.STONE);
        BlockFutureSlab.Double doubleSlab = new BlockFutureSlab.Double(
                name.replace("_slab", "_double_slab"), SoundType.STONE, slab);
        BLOCKS.add(slab);
        BLOCKS.add(doubleSlab);
        IBlockState base = material.getDefaultState();
        slab.setMapColor(base.getMapColor(null, net.minecraft.util.math.BlockPos.ORIGIN));
        doubleSlab.setMapColor(base.getMapColor(null, net.minecraft.util.math.BlockPos.ORIGIN));
        slab.setHardness(hardness);
        slab.setResistance(resistance / 3.0F);
        doubleSlab.setHardness(hardness);
        doubleSlab.setResistance(resistance / 3.0F);
        ENTRIES.add(new Entry(material, 0, null, slab, doubleSlab));
    }

    private static void wall(String name, Block material, int metadata, float hardness, float resistance) {
        wall(name, material, metadata, new ItemStack(material, 1, metadata), hardness, resistance);
    }

    private static void wall(String name, Block material, int metadata, ItemStack recipeMaterial,
                             float hardness, float resistance) {
        Block block = new BlockModernWall(name + "_wall", material.getStateFromMeta(metadata), hardness, resistance);
        BLOCKS.add(block);
        WALLS.put(block, recipeMaterial);
    }

    public static List<Block> blocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    public static List<Entry> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static boolean contains(Block block) {
        return BLOCKS.contains(block);
    }

    public static Item[] createItems() {
        List<Item> items = new ArrayList<>();
        for (Block block : new Block[] {SMOOTH_STONE, SMOOTH_SANDSTONE,
                SMOOTH_RED_SANDSTONE, SMOOTH_QUARTZ}) {
            items.add(remember(block, FFDItems.blockItem(block)));
        }
        for (Entry entry : ENTRIES) {
            if (entry.stairs != null) {
                items.add(remember(entry.stairs, FFDItems.blockItem(entry.stairs)));
            }
            items.add(remember(entry.slab, FFDItems.slabItem(entry.slab, entry.doubleSlab)));
        }
        for (Block wall : WALLS.keySet()) {
            items.add(remember(wall, FFDItems.blockItem(wall)));
        }
        return items.toArray(new Item[0]);
    }

    private static Item remember(Block block, Item item) {
        ITEMS.put(block, item);
        return item;
    }

    public static ItemStack materialStack(Entry entry) {
        Item local = ITEMS.get(entry.material);
        return local == null ? new ItemStack(entry.material, 1, entry.metadata)
                : FFDItems.effectiveStack(local);
    }

    public static void addRecipes(List<IRecipe> recipes) {
        ResourceLocation group = new ResourceLocation(FarmerFutureDelight.MODID, "stone_decor");
        for (Entry entry : ENTRIES) {
            ItemStack material = materialStack(entry);
            if (material.isEmpty()) {
                continue;
            }
            if (entry.stairs != null) {
                addRecipe(recipes, group, ITEMS.get(entry.stairs), 4, material,
                        "#  ", "## ", "###");
            }
            addRecipe(recipes, group, ITEMS.get(entry.slab), 6, material, "###");
        }
        for (Map.Entry<Block, ItemStack> wall : WALLS.entrySet()) {
            addRecipe(recipes, group, ITEMS.get(wall.getKey()), 6, wall.getValue(), "###", "###");
        }
    }

    private static void addRecipe(List<IRecipe> recipes, ResourceLocation group, Item output,
                                  int count, ItemStack material, String... rows) {
        if (!FFDItems.isItemRegistered(output)) {
            return;
        }
        Object[] definition = new Object[rows.length + 2];
        System.arraycopy(rows, 0, definition, 0, rows.length);
        definition[rows.length] = '#';
        definition[rows.length + 1] = material;
        recipes.add(new ShapedOreRecipe(group, new ItemStack(output, count), definition)
                .setRegistryName(output.getRegistryName()));
    }

    public static void registerSmelting() {
        Block[] inputs = {Blocks.STONE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.QUARTZ_BLOCK};
        Block[] outputs = {SMOOTH_STONE, SMOOTH_SANDSTONE, SMOOTH_RED_SANDSTONE, SMOOTH_QUARTZ};
        for (int index = 0; index < inputs.length; index++) {
            Item output = ITEMS.get(outputs[index]);
            ItemStack input = new ItemStack(inputs[index], 1, 0);
            if (FFDItems.isItemRegistered(output)
                    && FurnaceRecipes.instance().getSmeltingResult(input).isEmpty()) {
                GameRegistry.addSmelting(input, new ItemStack(output), 0.1F);
            }
        }
    }

    public static final class Entry {
        public final Block material;
        public final int metadata;
        public final BlockFutureStairs stairs;
        public final BlockFutureSlab.Half slab;
        public final BlockFutureSlab.Double doubleSlab;

        private Entry(Block material, int metadata, BlockFutureStairs stairs,
                      BlockFutureSlab.Half slab, BlockFutureSlab.Double doubleSlab) {
            this.material = material;
            this.metadata = metadata;
            this.stairs = stairs;
            this.slab = slab;
            this.doubleSlab = doubleSlab;
        }
    }
}

