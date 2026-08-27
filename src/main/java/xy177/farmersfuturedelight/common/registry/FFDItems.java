package xy177.farmersfuturedelight.common.registry;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.common.util.EnumHelper;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.item.ItemAxolotlBucket;
import xy177.farmersfuturedelight.common.item.ItemBigDripleaf;
import xy177.farmersfuturedelight.common.item.ItemCandle;
import xy177.farmersfuturedelight.common.item.ItemBlockDriedKelp;
import xy177.farmersfuturedelight.common.item.ItemDriedKelp;
import xy177.farmersfuturedelight.common.item.ItemGlowBerries;
import xy177.farmersfuturedelight.common.item.ItemGlowInkSac;
import xy177.farmersfuturedelight.common.item.ItemGlowItemFrame;
import xy177.farmersfuturedelight.common.item.ItemGoatHorn;
import xy177.farmersfuturedelight.common.item.ItemHoneyBottle;
import xy177.farmersfuturedelight.common.item.ItemHoneycomb;
import xy177.farmersfuturedelight.common.item.ItemHangingRoots;
import xy177.farmersfuturedelight.common.item.ItemKelp;
import xy177.farmersfuturedelight.common.item.ItemLightBlock;
import xy177.farmersfuturedelight.common.item.ItemMusicDisc;
import xy177.farmersfuturedelight.common.item.ItemNetherPlant;
import xy177.farmersfuturedelight.common.item.ItemNetherVine;
import xy177.farmersfuturedelight.common.item.ItemPowderSnowBucket;
import xy177.farmersfuturedelight.common.item.ItemSeagrass;
import xy177.farmersfuturedelight.common.item.ItemSeaPickle;
import xy177.farmersfuturedelight.common.item.ItemSmallDripleaf;
import xy177.farmersfuturedelight.common.item.ItemSweetBerries;
import xy177.farmersfuturedelight.common.item.ItemSpyglass;
import xy177.farmersfuturedelight.common.item.ItemTurtleEgg;
import xy177.farmersfuturedelight.common.item.ItemTurtleHelmet;

public final class FFDItems {
    private static final Map<Item, Block> LOCAL_ITEM_BLOCKS = new IdentityHashMap<>();
    private static final Map<net.minecraft.util.ResourceLocation, Item> LOCAL_BLOCK_ITEMS_BY_NAME =
            new HashMap<>();

    public static final ArmorMaterial TURTLE_SCUTE_ARMOR = EnumHelper.addArmorMaterial(
            "turtle_scute", FarmerFutureDelight.MODID + ":turtle_scute", 25,
            new int[] {2, 5, 6, 2}, 9, FFDSounds.TURTLE_ARMOR_EQUIP, 0.0F);

    public static final Item SWEET_BERRIES = new ItemSweetBerries()
            .setRegistryName(FarmerFutureDelight.MODID, "sweet_berries")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".sweet_berries")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item MOSS_BLOCK = new ItemBlock(FFDBlocks.MOSS_BLOCK)
            .setRegistryName(FFDBlocks.MOSS_BLOCK.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".moss_block")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item MOSS_CARPET = new ItemBlock(FFDBlocks.MOSS_CARPET)
            .setRegistryName(FFDBlocks.MOSS_CARPET.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".moss_carpet")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item GLOW_BERRIES = new ItemGlowBerries()
            .setRegistryName(FarmerFutureDelight.MODID, "glow_berries")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".glow_berries")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item AZALEA = new ItemNetherPlant(FFDBlocks.AZALEA,
            FFDBlocks.POTTED_AZALEA_BUSH)
            .setRegistryName(FFDBlocks.AZALEA.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".azalea")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item FLOWERING_AZALEA = new ItemNetherPlant(FFDBlocks.FLOWERING_AZALEA,
            FFDBlocks.POTTED_FLOWERING_AZALEA_BUSH)
            .setRegistryName(FFDBlocks.FLOWERING_AZALEA.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".flowering_azalea")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item AZALEA_LEAVES = new ItemBlock(FFDBlocks.AZALEA_LEAVES)
            .setRegistryName(FFDBlocks.AZALEA_LEAVES.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".azalea_leaves")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item FLOWERING_AZALEA_LEAVES = new ItemBlock(FFDBlocks.FLOWERING_AZALEA_LEAVES)
            .setRegistryName(FFDBlocks.FLOWERING_AZALEA_LEAVES.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".flowering_azalea_leaves")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item SMALL_DRIPLEAF = new ItemSmallDripleaf(FFDBlocks.SMALL_DRIPLEAF)
            .setRegistryName(FFDBlocks.SMALL_DRIPLEAF.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".small_dripleaf")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item BIG_DRIPLEAF = new ItemBigDripleaf()
            .setRegistryName(FarmerFutureDelight.MODID, "big_dripleaf")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".big_dripleaf")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item ROOTED_DIRT = new ItemBlock(FFDBlocks.ROOTED_DIRT)
            .setRegistryName(FFDBlocks.ROOTED_DIRT.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".rooted_dirt")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item HANGING_ROOTS = new ItemHangingRoots(FFDBlocks.HANGING_ROOTS)
            .setRegistryName(FFDBlocks.HANGING_ROOTS.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".hanging_roots")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item SPORE_BLOSSOM = new ItemBlock(FFDBlocks.SPORE_BLOSSOM)
            .setRegistryName(FFDBlocks.SPORE_BLOSSOM.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".spore_blossom")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item GLOW_LICHEN = new ItemBlock(FFDBlocks.GLOW_LICHEN)
            .setRegistryName(FFDBlocks.GLOW_LICHEN.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".glow_lichen")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item GLOW_INK_SAC = new ItemGlowInkSac()
            .setRegistryName(FarmerFutureDelight.MODID, "glow_ink_sac")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".glow_ink_sac")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item GLOW_ITEM_FRAME = new ItemGlowItemFrame()
            .setRegistryName(FarmerFutureDelight.MODID, "glow_item_frame")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".glow_item_frame")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item PHANTOM_MEMBRANE = new Item()
            .setRegistryName(FarmerFutureDelight.MODID, "phantom_membrane")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".phantom_membrane")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item MUSIC_DISC_OTHERSIDE =
            new ItemMusicDisc("otherside", FFDSounds.MUSIC_DISC_OTHERSIDE);
    public static final Item AMETHYST_BLOCK = blockItem(FFDBlocks.AMETHYST_BLOCK);
    public static final Item BUDDING_AMETHYST = blockItem(FFDBlocks.BUDDING_AMETHYST);
    public static final Item SMALL_AMETHYST_BUD = blockItem(FFDBlocks.SMALL_AMETHYST_BUD);
    public static final Item MEDIUM_AMETHYST_BUD = blockItem(FFDBlocks.MEDIUM_AMETHYST_BUD);
    public static final Item LARGE_AMETHYST_BUD = blockItem(FFDBlocks.LARGE_AMETHYST_BUD);
    public static final Item AMETHYST_CLUSTER = blockItem(FFDBlocks.AMETHYST_CLUSTER);
    public static final Item CALCITE = blockItem(FFDBlocks.CALCITE);
    public static final Item SMOOTH_BASALT = blockItem(FFDBlocks.SMOOTH_BASALT);
    public static final Item TINTED_GLASS = blockItem(FFDBlocks.TINTED_GLASS);
    public static final Item DRIPSTONE_BLOCK = blockItem(FFDBlocks.DRIPSTONE_BLOCK);
    public static final Item POINTED_DRIPSTONE = blockItem(FFDBlocks.POINTED_DRIPSTONE);
    public static final Item IRON_CHAIN = blockItem(FFDBlocks.IRON_CHAIN);
    public static final Item LIGHT = new ItemLightBlock(FFDBlocks.LIGHT)
            .setRegistryName(FFDBlocks.LIGHT.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".light");
    public static final Item[] CANDLE_ITEMS = candleItems(FFDBlocks.CANDLES);
    public static final Item CANDLE = CANDLE_ITEMS[0];
    public static final Item POWDER_SNOW = blockItem(FFDBlocks.POWDER_SNOW);
    public static final Item POWDER_SNOW_BUCKET = new ItemPowderSnowBucket()
            .setRegistryName(FarmerFutureDelight.MODID, "powder_snow_bucket")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".powder_snow_bucket")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item AMETHYST_SHARD = new Item()
            .setRegistryName(FarmerFutureDelight.MODID, "amethyst_shard")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".amethyst_shard")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item DEEPSLATE = blockItem(FFDBlocks.DEEPSLATE);
    public static final Item COBBLED_DEEPSLATE = blockItem(FFDBlocks.COBBLED_DEEPSLATE);
    public static final Item POLISHED_DEEPSLATE = blockItem(FFDBlocks.POLISHED_DEEPSLATE);
    public static final Item DEEPSLATE_BRICKS = blockItem(FFDBlocks.DEEPSLATE_BRICKS);
    public static final Item CRACKED_DEEPSLATE_BRICKS = blockItem(FFDBlocks.CRACKED_DEEPSLATE_BRICKS);
    public static final Item DEEPSLATE_TILES = blockItem(FFDBlocks.DEEPSLATE_TILES);
    public static final Item CRACKED_DEEPSLATE_TILES = blockItem(FFDBlocks.CRACKED_DEEPSLATE_TILES);
    public static final Item CHISELED_DEEPSLATE = blockItem(FFDBlocks.CHISELED_DEEPSLATE);
    public static final Item TUFF = blockItem(FFDBlocks.TUFF);
    public static final Item INFESTED_DEEPSLATE = blockItem(FFDBlocks.INFESTED_DEEPSLATE);
    public static final Item COBBLED_DEEPSLATE_STAIRS = blockItem(FFDBlocks.COBBLED_DEEPSLATE_STAIRS);
    public static final Item POLISHED_DEEPSLATE_STAIRS = blockItem(FFDBlocks.POLISHED_DEEPSLATE_STAIRS);
    public static final Item DEEPSLATE_BRICK_STAIRS = blockItem(FFDBlocks.DEEPSLATE_BRICK_STAIRS);
    public static final Item DEEPSLATE_TILE_STAIRS = blockItem(FFDBlocks.DEEPSLATE_TILE_STAIRS);
    public static final Item COBBLED_DEEPSLATE_SLAB = slabItem(
            FFDBlocks.COBBLED_DEEPSLATE_SLAB, FFDBlocks.COBBLED_DEEPSLATE_DOUBLE_SLAB);
    public static final Item POLISHED_DEEPSLATE_SLAB = slabItem(
            FFDBlocks.POLISHED_DEEPSLATE_SLAB, FFDBlocks.POLISHED_DEEPSLATE_DOUBLE_SLAB);
    public static final Item DEEPSLATE_BRICK_SLAB = slabItem(
            FFDBlocks.DEEPSLATE_BRICK_SLAB, FFDBlocks.DEEPSLATE_BRICK_DOUBLE_SLAB);
    public static final Item DEEPSLATE_TILE_SLAB = slabItem(
            FFDBlocks.DEEPSLATE_TILE_SLAB, FFDBlocks.DEEPSLATE_TILE_DOUBLE_SLAB);
    public static final Item COBBLED_DEEPSLATE_WALL = blockItem(FFDBlocks.COBBLED_DEEPSLATE_WALL);
    public static final Item POLISHED_DEEPSLATE_WALL = blockItem(FFDBlocks.POLISHED_DEEPSLATE_WALL);
    public static final Item DEEPSLATE_BRICK_WALL = blockItem(FFDBlocks.DEEPSLATE_BRICK_WALL);
    public static final Item DEEPSLATE_TILE_WALL = blockItem(FFDBlocks.DEEPSLATE_TILE_WALL);
    public static final Item DEEPSLATE_COAL_ORE = blockItem(FFDBlocks.DEEPSLATE_COAL_ORE);
    public static final Item DEEPSLATE_IRON_ORE = blockItem(FFDBlocks.DEEPSLATE_IRON_ORE);
    public static final Item DEEPSLATE_COPPER_ORE = blockItem(FFDBlocks.DEEPSLATE_COPPER_ORE);
    public static final Item DEEPSLATE_GOLD_ORE = blockItem(FFDBlocks.DEEPSLATE_GOLD_ORE);
    public static final Item DEEPSLATE_REDSTONE_ORE = blockItem(FFDBlocks.DEEPSLATE_REDSTONE_ORE);
    public static final Item DEEPSLATE_LAPIS_ORE = blockItem(FFDBlocks.DEEPSLATE_LAPIS_ORE);
    public static final Item DEEPSLATE_DIAMOND_ORE = blockItem(FFDBlocks.DEEPSLATE_DIAMOND_ORE);
    public static final Item DEEPSLATE_EMERALD_ORE = blockItem(FFDBlocks.DEEPSLATE_EMERALD_ORE);
    public static final Item COPPER_ORE = blockItem(FFDBlocks.COPPER_ORE);
    public static final Item RAW_IRON_BLOCK = blockItem(FFDBlocks.RAW_IRON_BLOCK);
    public static final Item RAW_GOLD_BLOCK = blockItem(FFDBlocks.RAW_GOLD_BLOCK);
    public static final Item RAW_COPPER_BLOCK = blockItem(FFDBlocks.RAW_COPPER_BLOCK);
    public static final Item RAW_IRON = simpleItem("raw_iron");
    public static final Item RAW_GOLD = simpleItem("raw_gold");
    public static final Item RAW_COPPER = simpleItem("raw_copper");
    public static final Item[] RAW_ORE_ITEMS = rawOreItems();
    public static final Item[] RAW_ORE_BLOCK_ITEMS = rawOreBlockItems();
    public static final Item COPPER_INGOT = simpleItem("copper_ingot");
    public static final Item[] COPPER_BLOCK_ITEMS = blockItems(FFDBlocks.COPPER_BLOCKS);
    public static final Item[] WAXED_COPPER_BLOCK_ITEMS = blockItems(FFDBlocks.WAXED_COPPER_BLOCKS);
    public static final Item[] CUT_COPPER_ITEMS = blockItems(FFDBlocks.CUT_COPPER_BLOCKS);
    public static final Item[] WAXED_CUT_COPPER_ITEMS = blockItems(FFDBlocks.WAXED_CUT_COPPER_BLOCKS);
    public static final Item[] CUT_COPPER_STAIR_ITEMS = blockItems(FFDBlocks.CUT_COPPER_STAIRS);
    public static final Item[] WAXED_CUT_COPPER_STAIR_ITEMS =
            blockItems(FFDBlocks.WAXED_CUT_COPPER_STAIRS);
    public static final Item[] CUT_COPPER_SLAB_ITEMS = slabItems(
            FFDBlocks.CUT_COPPER_SLABS, FFDBlocks.CUT_COPPER_DOUBLE_SLABS);
    public static final Item[] WAXED_CUT_COPPER_SLAB_ITEMS = slabItems(
            FFDBlocks.WAXED_CUT_COPPER_SLABS, FFDBlocks.WAXED_CUT_COPPER_DOUBLE_SLABS);
    public static final Item[] LIGHTNING_ROD_ITEMS = blockItems(FFDBlocks.LIGHTNING_RODS);
    public static final Item[] WAXED_LIGHTNING_ROD_ITEMS = blockItems(FFDBlocks.WAXED_LIGHTNING_RODS);
    public static final Item COPPER_BLOCK = COPPER_BLOCK_ITEMS[0];
    public static final Item CUT_COPPER = CUT_COPPER_ITEMS[0];
    public static final Item CUT_COPPER_STAIRS = CUT_COPPER_STAIR_ITEMS[0];
    public static final Item CUT_COPPER_SLAB = CUT_COPPER_SLAB_ITEMS[0];
    public static final Item LIGHTNING_ROD = LIGHTNING_ROD_ITEMS[0];
    public static final Item SPYGLASS = new ItemSpyglass()
            .setRegistryName(FarmerFutureDelight.MODID, "spyglass")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".spyglass")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item HONEY_BOTTLE = new ItemHoneyBottle()
            .setRegistryName(FarmerFutureDelight.MODID, "honey_bottle")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".honey_bottle")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item HONEYCOMB = new ItemHoneycomb()
            .setRegistryName(FarmerFutureDelight.MODID, "honeycomb")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".honeycomb")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item HONEY_BLOCK = new ItemBlock(FFDBlocks.HONEY_BLOCK)
            .setRegistryName(FFDBlocks.HONEY_BLOCK.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".honey_block")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item HONEYCOMB_BLOCK = new ItemBlock(FFDBlocks.HONEYCOMB_BLOCK)
            .setRegistryName(FFDBlocks.HONEYCOMB_BLOCK.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".honeycomb_block")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item BEE_NEST = new ItemBlock(FFDBlocks.BEE_NEST)
            .setRegistryName(FFDBlocks.BEE_NEST.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".bee_nest")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item BEEHIVE = new ItemBlock(FFDBlocks.BEEHIVE)
            .setRegistryName(FFDBlocks.BEEHIVE.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".beehive")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item KELP = new ItemKelp(FFDBlocks.KELP)
            .setRegistryName(FarmerFutureDelight.MODID, "kelp")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".kelp")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item DRIED_KELP = new ItemDriedKelp()
            .setRegistryName(FarmerFutureDelight.MODID, "dried_kelp")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".dried_kelp")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item DRIED_KELP_BLOCK = new ItemBlockDriedKelp(FFDBlocks.DRIED_KELP_BLOCK)
            .setRegistryName(FFDBlocks.DRIED_KELP_BLOCK.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".dried_kelp_block")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item SEAGRASS = new ItemSeagrass(FFDBlocks.SEAGRASS)
            .setRegistryName(FarmerFutureDelight.MODID, "seagrass")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".seagrass")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item SEA_PICKLE = new ItemSeaPickle(FFDBlocks.SEA_PICKLE)
            .setRegistryName(FarmerFutureDelight.MODID, "sea_pickle")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".sea_pickle")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item TURTLE_EGG = new ItemTurtleEgg(FFDBlocks.TURTLE_EGG)
            .setRegistryName(FFDBlocks.TURTLE_EGG.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".turtle_egg")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item TURTLE_SCUTE = new Item()
            .setRegistryName(FarmerFutureDelight.MODID, "turtle_scute")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".turtle_scute")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item TURTLE_HELMET = new ItemTurtleHelmet(TURTLE_SCUTE_ARMOR)
            .setRegistryName(FarmerFutureDelight.MODID, "turtle_helmet")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".turtle_helmet")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item AXOLOTL_BUCKET = new ItemAxolotlBucket()
            .setRegistryName(FarmerFutureDelight.MODID, "axolotl_bucket")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".axolotl_bucket")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item GOAT_HORN = new ItemGoatHorn()
            .setRegistryName(FarmerFutureDelight.MODID, "goat_horn")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".goat_horn")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item CRIMSON_NYLIUM = blockItem(FFDBlocks.CRIMSON_NYLIUM);
    public static final Item WARPED_NYLIUM = blockItem(FFDBlocks.WARPED_NYLIUM);
    public static final Item CRIMSON_FUNGUS = netherPlantItem(FFDBlocks.CRIMSON_FUNGUS,
            FFDBlocks.POTTED_CRIMSON_FUNGUS);
    public static final Item WARPED_FUNGUS = netherPlantItem(FFDBlocks.WARPED_FUNGUS,
            FFDBlocks.POTTED_WARPED_FUNGUS);
    public static final Item CRIMSON_ROOTS = netherPlantItem(FFDBlocks.CRIMSON_ROOTS,
            FFDBlocks.POTTED_CRIMSON_ROOTS);
    public static final Item WARPED_ROOTS = netherPlantItem(FFDBlocks.WARPED_ROOTS,
            FFDBlocks.POTTED_WARPED_ROOTS);
    public static final Item NETHER_SPROUTS = blockItem(FFDBlocks.NETHER_SPROUTS);
    public static final Item WEEPING_VINES = netherVineItem(FFDBlocks.WEEPING_VINES);
    public static final Item TWISTING_VINES = netherVineItem(FFDBlocks.TWISTING_VINES);
    public static final Item CRIMSON_STEM = blockItem(FFDBlocks.CRIMSON_STEM);
    public static final Item STRIPPED_CRIMSON_STEM = blockItem(FFDBlocks.STRIPPED_CRIMSON_STEM);
    public static final Item CRIMSON_HYPHAE = blockItem(FFDBlocks.CRIMSON_HYPHAE);
    public static final Item STRIPPED_CRIMSON_HYPHAE = blockItem(FFDBlocks.STRIPPED_CRIMSON_HYPHAE);
    public static final Item WARPED_STEM = blockItem(FFDBlocks.WARPED_STEM);
    public static final Item STRIPPED_WARPED_STEM = blockItem(FFDBlocks.STRIPPED_WARPED_STEM);
    public static final Item WARPED_HYPHAE = blockItem(FFDBlocks.WARPED_HYPHAE);
    public static final Item STRIPPED_WARPED_HYPHAE = blockItem(FFDBlocks.STRIPPED_WARPED_HYPHAE);
    public static final Item WARPED_WART_BLOCK = blockItem(FFDBlocks.WARPED_WART_BLOCK);
    public static final Item SHROOMLIGHT = blockItem(FFDBlocks.SHROOMLIGHT);
    public static final Item CRIMSON_PLANKS = blockItem(FFDBlocks.CRIMSON_PLANKS);
    public static final Item WARPED_PLANKS = blockItem(FFDBlocks.WARPED_PLANKS);
    public static final Item CRIMSON_STAIRS = blockItem(FFDBlocks.CRIMSON_STAIRS);
    public static final Item WARPED_STAIRS = blockItem(FFDBlocks.WARPED_STAIRS);
    public static final Item CRIMSON_SLAB = slabItem(FFDBlocks.CRIMSON_SLAB, FFDBlocks.CRIMSON_DOUBLE_SLAB);
    public static final Item WARPED_SLAB = slabItem(FFDBlocks.WARPED_SLAB, FFDBlocks.WARPED_DOUBLE_SLAB);
    public static final Item CRIMSON_FENCE = blockItem(FFDBlocks.CRIMSON_FENCE);
    public static final Item WARPED_FENCE = blockItem(FFDBlocks.WARPED_FENCE);
    public static final Item CRIMSON_FENCE_GATE = blockItem(FFDBlocks.CRIMSON_FENCE_GATE);
    public static final Item WARPED_FENCE_GATE = blockItem(FFDBlocks.WARPED_FENCE_GATE);
    public static final Item CRIMSON_DOOR = doorItem(FFDBlocks.CRIMSON_DOOR);
    public static final Item WARPED_DOOR = doorItem(FFDBlocks.WARPED_DOOR);
    public static final Item CRIMSON_TRAPDOOR = blockItem(FFDBlocks.CRIMSON_TRAPDOOR);
    public static final Item WARPED_TRAPDOOR = blockItem(FFDBlocks.WARPED_TRAPDOOR);
    public static final Item CRIMSON_BUTTON = blockItem(FFDBlocks.CRIMSON_BUTTON);
    public static final Item WARPED_BUTTON = blockItem(FFDBlocks.WARPED_BUTTON);
    public static final Item CRIMSON_PRESSURE_PLATE = blockItem(FFDBlocks.CRIMSON_PRESSURE_PLATE);
    public static final Item WARPED_PRESSURE_PLATE = blockItem(FFDBlocks.WARPED_PRESSURE_PLATE);

    static {
        rememberBlockItem(MOSS_BLOCK, FFDBlocks.MOSS_BLOCK);
        rememberBlockItem(MOSS_CARPET, FFDBlocks.MOSS_CARPET);
        rememberBlockItem(AZALEA, FFDBlocks.AZALEA);
        rememberBlockItem(FLOWERING_AZALEA, FFDBlocks.FLOWERING_AZALEA);
        rememberBlockItem(AZALEA_LEAVES, FFDBlocks.AZALEA_LEAVES);
        rememberBlockItem(FLOWERING_AZALEA_LEAVES, FFDBlocks.FLOWERING_AZALEA_LEAVES);
        rememberBlockItem(SMALL_DRIPLEAF, FFDBlocks.SMALL_DRIPLEAF);
        rememberBlockItem(ROOTED_DIRT, FFDBlocks.ROOTED_DIRT);
        rememberBlockItem(HANGING_ROOTS, FFDBlocks.HANGING_ROOTS);
        rememberBlockItem(SPORE_BLOSSOM, FFDBlocks.SPORE_BLOSSOM);
        rememberBlockItem(GLOW_LICHEN, FFDBlocks.GLOW_LICHEN);
        rememberBlockItem(LIGHT, FFDBlocks.LIGHT);
        rememberBlockItem(HONEY_BLOCK, FFDBlocks.HONEY_BLOCK);
        rememberBlockItem(HONEYCOMB_BLOCK, FFDBlocks.HONEYCOMB_BLOCK);
        rememberBlockItem(BEE_NEST, FFDBlocks.BEE_NEST);
        rememberBlockItem(BEEHIVE, FFDBlocks.BEEHIVE);
        rememberBlockItem(KELP, FFDBlocks.KELP);
        rememberBlockItem(DRIED_KELP_BLOCK, FFDBlocks.DRIED_KELP_BLOCK);
        rememberBlockItem(SEAGRASS, FFDBlocks.SEAGRASS);
        rememberBlockItem(SEA_PICKLE, FFDBlocks.SEA_PICKLE);
        rememberBlockItem(TURTLE_EGG, FFDBlocks.TURTLE_EGG);
    }

    public static boolean isSweetBerryEnabled() {
        return FFDCompat.isEnabled(FFDConfig.sweetBerryMode, FFDCompat.Feature.SWEET_BERRY);
    }

    public static boolean isMossEnabled() {
        return FFDCompat.isEnabled(FFDConfig.mossMode, FFDCompat.Feature.MOSS);
    }

    public static boolean isMossWorldgenEnabled() {
        return FFDConfig.mossMode != FFDConfig.FeatureMode.DISABLED;
    }

    public static boolean isGlowBerryEnabled() {
        return FFDCompat.isEnabled(FFDConfig.glowBerryMode, FFDCompat.Feature.GLOW_BERRY);
    }

    public static boolean isGlowBerryWorldgenEnabled() {
        return FFDConfig.glowBerryMode != FFDConfig.FeatureMode.DISABLED;
    }

    public static boolean isLushCaveEnabled() {
        return FFDConfig.lushCaveMode != FFDConfig.FeatureMode.DISABLED;
    }

    public static boolean isAzaleaEnabled() {
        return FFDCompat.isEnabled(FFDConfig.azaleaMode, FFDCompat.Feature.AZALEA);
    }

    public static boolean isAzaleaWorldgenEnabled() {
        return FFDConfig.azaleaMode != FFDConfig.FeatureMode.DISABLED;
    }

    public static boolean isDripleafEnabled() {
        return FFDCompat.isEnabled(FFDConfig.dripleafMode, FFDCompat.Feature.DRIPLEAF);
    }

    public static boolean isDripleafWorldgenEnabled() {
        return FFDConfig.dripleafMode != FFDConfig.FeatureMode.DISABLED;
    }

    public static boolean isRootedDirtEnabled() {
        return FFDCompat.isEnabled(FFDConfig.rootedDirtMode, FFDCompat.Feature.ROOTED_DIRT);
    }

    public static boolean isRootedDirtWorldgenEnabled() {
        return FFDConfig.rootedDirtMode != FFDConfig.FeatureMode.DISABLED;
    }

    public static boolean isHangingRootsEnabled() {
        return FFDCompat.isEnabled(FFDConfig.hangingRootsMode, FFDCompat.Feature.HANGING_ROOTS);
    }

    public static boolean isHangingRootsWorldgenEnabled() {
        return FFDConfig.hangingRootsMode != FFDConfig.FeatureMode.DISABLED;
    }

    public static boolean isSporeBlossomEnabled() {
        return FFDCompat.isEnabled(FFDConfig.sporeBlossomMode, FFDCompat.Feature.SPORE_BLOSSOM);
    }

    public static boolean isSporeBlossomWorldgenEnabled() {
        return FFDConfig.sporeBlossomMode != FFDConfig.FeatureMode.DISABLED;
    }

    public static boolean isGlowLichenEnabled() {
        return FFDCompat.isEnabled(FFDConfig.glowLichenMode);
    }

    public static boolean isHoneyEnabled() {
        return FFDCompat.isEnabled(FFDConfig.honeyMode, FFDCompat.Feature.HONEY);
    }

    public static boolean isKelpEnabled() {
        return FFDCompat.isEnabled(FFDConfig.kelpMode, FFDCompat.Feature.KELP);
    }

    public static boolean isSeagrassEnabled() {
        return FFDCompat.isEnabled(FFDConfig.seagrassMode, FFDCompat.Feature.SEAGRASS);
    }

    public static boolean isSeaPickleEnabled() {
        return FFDCompat.isEnabled(FFDConfig.seaPickleMode, FFDCompat.Feature.SEA_PICKLE);
    }

    public static boolean isTurtleEnabled() {
        return FFDCompat.isEnabled(FFDConfig.turtleMode, FFDCompat.Feature.TURTLE);
    }

    public static boolean isAxolotlEnabled() {
        return FFDCompat.isEnabled(FFDConfig.axolotlMode, FFDCompat.Feature.AXOLOTL);
    }

    public static boolean isGoatEnabled() {
        return FFDCompat.isEnabled(FFDConfig.goatMode);
    }

    public static boolean isPhantomEnabled() {
        return FFDCompat.isEnabled(FFDConfig.phantomMode, FFDCompat.Feature.PHANTOM);
    }

    public static boolean isOthersideEnabled() {
        return FFDCompat.isEnabled(FFDConfig.othersideMode, FFDCompat.Feature.OTHERSIDE);
    }

    public static boolean isGlowItemFrameEnabled() {
        return FFDCompat.isEnabled(FFDConfig.glowItemFrameMode,
                FFDCompat.Feature.GLOW_ITEM_FRAME);
    }

    public static boolean isSignTextEnabled() {
        return FFDCompat.isEnabled(FFDConfig.signTextMode);
    }

    public static boolean isAmethystEnabled() {
        return FFDCompat.isEnabled(FFDConfig.amethystMode, FFDCompat.Feature.AMETHYST);
    }

    public static boolean isDeepslateEnabled() {
        return FFDCompat.isEnabled(FFDConfig.deepslateMode, FFDCompat.Feature.DEEPSLATE);
    }

    public static boolean isRawOreEnabled() {
        return FFDCompat.isEnabled(FFDConfig.rawOreMode, FFDCompat.Feature.RAW_ORE);
    }

    public static boolean isRawOreMaterialEnabled(String material) {
        return FFDConfig.isRawOreMaterialEnabled(material)
                && (FFDRawOres.isCopper(material) ? isCopperEnabled() : isRawOreEnabled());
    }

    public static boolean isCopperEnabled() {
        return FFDCompat.isEnabled(FFDConfig.copperMode, FFDCompat.Feature.COPPER);
    }

    public static boolean isDripstoneEnabled() {
        return FFDCompat.isEnabled(FFDConfig.dripstoneMode, FFDCompat.Feature.DRIPSTONE);
    }

    public static boolean isIronChainEnabled() {
        return FFDCompat.isEnabled(FFDConfig.ironChainMode, FFDCompat.Feature.IRON_CHAIN);
    }

    public static boolean isLightEnabled() {
        return FFDCompat.isEnabled(FFDConfig.lightBlockMode);
    }

    public static boolean isCandleEnabled() {
        return FFDCompat.isEnabled(FFDConfig.candleMode, FFDCompat.Feature.CANDLE);
    }

    public static int getCandleIndex(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < CANDLE_ITEMS.length; i++) {
            ItemStack effective = effectiveStack(CANDLE_ITEMS[i]);
            if (!effective.isEmpty() && stack.getItem() == effective.getItem()
                    && stack.getMetadata() == effective.getMetadata()) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isPowderSnowEnabled() {
        return FFDCompat.isEnabled(FFDConfig.powderSnowMode, FFDCompat.Feature.POWDER_SNOW);
    }

    public static boolean isCrimsonEnabled() {
        return FFDCompat.isEnabled(FFDConfig.crimsonMode, FFDCompat.Feature.CRIMSON);
    }

    public static boolean isWarpedEnabled() {
        return FFDCompat.isEnabled(FFDConfig.warpedMode, FFDCompat.Feature.WARPED);
    }

    public static boolean isCrimsonWoodEnabled() {
        return FFDCompat.isEnabled(FFDConfig.crimsonWoodMode, FFDCompat.Feature.CRIMSON_WOOD);
    }

    public static boolean isWarpedWoodEnabled() {
        return FFDCompat.isEnabled(FFDConfig.warpedWoodMode, FFDCompat.Feature.WARPED_WOOD);
    }

    public static boolean shouldRegisterBlock(Block block) {
        String rawOreMaterial = rawOreMaterial(block);
        if (rawOreMaterial != null && !isRawOreMaterialEnabled(rawOreMaterial)) {
            return false;
        }
        FeatureBinding binding = blockBinding(block);
        return binding == null || FFDCompat.isLocalBlockEnabled(
                binding.mode, binding.feature, block, rawBlockAliases(block));
    }

    public static boolean shouldRegisterItem(Item item) {
        String rawOreMaterial = rawOreMaterial(item);
        if (rawOreMaterial != null && !isRawOreMaterialEnabled(rawOreMaterial)) {
            return false;
        }
        FeatureBinding binding = itemBinding(item);
        return binding == null || FFDCompat.isLocalItemEnabled(
                binding.mode, binding.feature, item, LOCAL_ITEM_BLOCKS.get(item),
                rawBlockAliases(LOCAL_ITEM_BLOCKS.get(item)));
    }

    public static boolean isBlockRegistered(Block block) {
        return block.getRegistryName() != null
                && ForgeRegistries.BLOCKS.getValue(block.getRegistryName()) == block;
    }

    public static boolean isItemRegistered(Item item) {
        return item.getRegistryName() != null
                && ForgeRegistries.ITEMS.getValue(item.getRegistryName()) == item;
    }

    public static boolean shouldDisplayInCreativeTab(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return true;
        }
        Item local = LOCAL_BLOCK_ITEMS_BY_NAME.get(stack.getItem().getRegistryName());
        return local == null || shouldRegisterItem(local);
    }

    public static ItemStack effectiveStack(Item local) {
        String rawOreMaterial = rawOreMaterial(local);
        if (rawOreMaterial != null && !FFDConfig.isRawOreMaterialEnabled(rawOreMaterial)) {
            return ItemStack.EMPTY;
        }
        if (isItemRegistered(local)) {
            return new ItemStack(local);
        }
        FeatureBinding binding = itemBinding(local);
        return binding == null || binding.mode != FFDConfig.FeatureMode.AUTO
                || local.getRegistryName() == null ? ItemStack.EMPTY
                : FFDCompat.getExternalItemStack(binding.feature,
                        contentPaths(local));
    }

    public static Block effectiveBlock(Block local) {
        String rawOreMaterial = rawOreMaterial(local);
        if (rawOreMaterial != null && !FFDConfig.isRawOreMaterialEnabled(rawOreMaterial)) {
            return null;
        }
        if (isBlockRegistered(local)) {
            return local;
        }
        FeatureBinding binding = blockBinding(local);
        if (binding == null || binding.mode != FFDConfig.FeatureMode.AUTO
                || local.getRegistryName() == null) {
            return null;
        }
        net.minecraft.block.state.IBlockState external = FFDCompat.getExternalBlockState(
                binding.feature, contentPaths(local));
        return external == null ? null : external.getBlock();
    }

    public static Item effectiveItem(Item local) {
        ItemStack stack = effectiveStack(local);
        return stack.isEmpty() ? null : stack.getItem();
    }

    public static ItemStack effectiveStack(Item local, int count) {
        ItemStack stack = effectiveStack(local);
        if (!stack.isEmpty()) {
            stack.setCount(count);
        }
        return stack;
    }

    private static FeatureBinding blockBinding(Block block) {
        if (block == FFDBlocks.SWEET_BERRY_BUSH) {
            return binding(FFDConfig.sweetBerryMode, FFDCompat.Feature.SWEET_BERRY);
        }
        if (block == FFDBlocks.MOSS_BLOCK || block == FFDBlocks.MOSS_CARPET) {
            return binding(FFDConfig.mossMode, FFDCompat.Feature.MOSS);
        }
        if (block == FFDBlocks.CAVE_VINES || block == FFDBlocks.CAVE_VINES_PLANT) {
            return binding(FFDConfig.glowBerryMode, FFDCompat.Feature.GLOW_BERRY);
        }
        if (block == FFDBlocks.AZALEA || block == FFDBlocks.FLOWERING_AZALEA
                || block == FFDBlocks.POTTED_AZALEA_BUSH
                || block == FFDBlocks.POTTED_FLOWERING_AZALEA_BUSH
                || block == FFDBlocks.AZALEA_LEAVES
                || block == FFDBlocks.FLOWERING_AZALEA_LEAVES) {
            return binding(FFDConfig.azaleaMode, FFDCompat.Feature.AZALEA);
        }
        if (block == FFDBlocks.SMALL_DRIPLEAF || block == FFDBlocks.BIG_DRIPLEAF
                || block == FFDBlocks.BIG_DRIPLEAF_STEM
                || block == FFDBlocks.BIG_DRIPLEAF_WATERLOGGED) {
            return binding(FFDConfig.dripleafMode, FFDCompat.Feature.DRIPLEAF);
        }
        if (block == FFDBlocks.ROOTED_DIRT) {
            return binding(FFDConfig.rootedDirtMode, FFDCompat.Feature.ROOTED_DIRT);
        }
        if (block == FFDBlocks.HANGING_ROOTS) {
            return binding(FFDConfig.hangingRootsMode, FFDCompat.Feature.HANGING_ROOTS);
        }
        if (block == FFDBlocks.SPORE_BLOSSOM) {
            return binding(FFDConfig.sporeBlossomMode, FFDCompat.Feature.SPORE_BLOSSOM);
        }
        if (oneOf(block, FFDBlocks.AMETHYST_BLOCK, FFDBlocks.BUDDING_AMETHYST,
                FFDBlocks.SMALL_AMETHYST_BUD, FFDBlocks.MEDIUM_AMETHYST_BUD,
                FFDBlocks.LARGE_AMETHYST_BUD, FFDBlocks.AMETHYST_CLUSTER,
                FFDBlocks.CALCITE, FFDBlocks.SMOOTH_BASALT, FFDBlocks.TINTED_GLASS)) {
            return binding(FFDConfig.amethystMode, FFDCompat.Feature.AMETHYST);
        }
        if (block == FFDBlocks.DRIPSTONE_BLOCK || block == FFDBlocks.POINTED_DRIPSTONE) {
            return binding(FFDConfig.dripstoneMode, FFDCompat.Feature.DRIPSTONE);
        }
        if (block == FFDBlocks.IRON_CHAIN) {
            return binding(FFDConfig.ironChainMode, FFDCompat.Feature.IRON_CHAIN);
        }
        if (arrayContains(block, FFDBlocks.CANDLES)
                || arrayContains(block, FFDBlocks.CANDLE_CAKES)) {
            return binding(FFDConfig.candleMode, FFDCompat.Feature.CANDLE);
        }
        if (block == FFDBlocks.POWDER_SNOW) {
            return binding(FFDConfig.powderSnowMode, FFDCompat.Feature.POWDER_SNOW);
        }
        if (isDeepslateBlock(block)) {
            return binding(FFDConfig.deepslateMode, FFDCompat.Feature.DEEPSLATE);
        }
        if (isRawOreBlock(block)) {
            if (FFDRawOres.isCopper(block.getRegistryName().getResourcePath()
                    .replace("raw_", "").replace("_block", ""))) {
                return binding(FFDConfig.copperMode, FFDCompat.Feature.COPPER);
            }
            return binding(FFDConfig.rawOreMode, FFDCompat.Feature.RAW_ORE);
        }
        if (isCopperBlock(block)) {
            return binding(FFDConfig.copperMode, FFDCompat.Feature.COPPER);
        }
        if (oneOf(block, FFDBlocks.KELP_YOUNG, FFDBlocks.KELP,
                FFDBlocks.KELP_PLANT, FFDBlocks.DRIED_KELP_BLOCK)) {
            return binding(FFDConfig.kelpMode, FFDCompat.Feature.KELP);
        }
        if (block == FFDBlocks.SEAGRASS || block == FFDBlocks.TALL_SEAGRASS) {
            return binding(FFDConfig.seagrassMode, FFDCompat.Feature.SEAGRASS);
        }
        if (block == FFDBlocks.SEA_PICKLE) {
            return binding(FFDConfig.seaPickleMode, FFDCompat.Feature.SEA_PICKLE);
        }
        if (block == FFDBlocks.TURTLE_EGG) {
            return binding(FFDConfig.turtleMode, FFDCompat.Feature.TURTLE);
        }
        if (oneOf(block, FFDBlocks.HONEY_BLOCK, FFDBlocks.HONEYCOMB_BLOCK,
                FFDBlocks.BEE_NEST, FFDBlocks.BEEHIVE)) {
            return binding(FFDConfig.honeyMode, FFDCompat.Feature.HONEY);
        }
        if (oneOf(block, FFDBlocks.CRIMSON_NYLIUM, FFDBlocks.CRIMSON_FUNGUS,
                FFDBlocks.CRIMSON_ROOTS, FFDBlocks.POTTED_CRIMSON_FUNGUS,
                FFDBlocks.POTTED_CRIMSON_ROOTS, FFDBlocks.WEEPING_VINES,
                FFDBlocks.WEEPING_VINES_PLANT)) {
            return binding(FFDConfig.crimsonMode, FFDCompat.Feature.CRIMSON);
        }
        if (oneOf(block, FFDBlocks.WARPED_NYLIUM, FFDBlocks.WARPED_FUNGUS,
                FFDBlocks.WARPED_ROOTS, FFDBlocks.POTTED_WARPED_FUNGUS,
                FFDBlocks.POTTED_WARPED_ROOTS, FFDBlocks.NETHER_SPROUTS,
                FFDBlocks.TWISTING_VINES, FFDBlocks.TWISTING_VINES_PLANT)) {
            return binding(FFDConfig.warpedMode, FFDCompat.Feature.WARPED);
        }
        if (block == FFDBlocks.SHROOMLIGHT) {
            return binding(combinedMode(FFDConfig.crimsonWoodMode, FFDConfig.warpedWoodMode),
                    FFDCompat.Feature.CRIMSON_WOOD);
        }
        if (isCrimsonWoodBlock(block)) {
            return binding(FFDConfig.crimsonWoodMode, FFDCompat.Feature.CRIMSON_WOOD);
        }
        if (isWarpedWoodBlock(block)) {
            return binding(FFDConfig.warpedWoodMode, FFDCompat.Feature.WARPED_WOOD);
        }
        return null;
    }

    private static FeatureBinding itemBinding(Item item) {
        Block localBlock = LOCAL_ITEM_BLOCKS.get(item);
        if (localBlock != null) {
            FeatureBinding block = blockBinding(localBlock);
            if (block != null) {
                return block;
            }
        }
        if (item == SWEET_BERRIES) {
            return binding(FFDConfig.sweetBerryMode, FFDCompat.Feature.SWEET_BERRY);
        }
        if (item == GLOW_BERRIES) {
            return binding(FFDConfig.glowBerryMode, FFDCompat.Feature.GLOW_BERRY);
        }
        if (item == BIG_DRIPLEAF) {
            return binding(FFDConfig.dripleafMode, FFDCompat.Feature.DRIPLEAF);
        }
        if (item == GLOW_INK_SAC) {
            return binding(FFDConfig.glowSquidMode, FFDCompat.Feature.GLOW_SQUID);
        }
        if (item == GLOW_ITEM_FRAME) {
            return binding(FFDConfig.glowItemFrameMode, FFDCompat.Feature.GLOW_ITEM_FRAME);
        }
        if (item == KELP || item == DRIED_KELP) {
            return binding(FFDConfig.kelpMode, FFDCompat.Feature.KELP);
        }
        if (item == SEAGRASS) {
            return binding(FFDConfig.seagrassMode, FFDCompat.Feature.SEAGRASS);
        }
        if (item == SEA_PICKLE) {
            return binding(FFDConfig.seaPickleMode, FFDCompat.Feature.SEA_PICKLE);
        }
        if (item == TURTLE_SCUTE || item == TURTLE_HELMET) {
            return binding(FFDConfig.turtleMode, FFDCompat.Feature.TURTLE);
        }
        if (item == AXOLOTL_BUCKET) {
            return binding(FFDConfig.axolotlMode, FFDCompat.Feature.AXOLOTL);
        }
        if (item == PHANTOM_MEMBRANE) {
            return binding(FFDConfig.phantomMode, FFDCompat.Feature.PHANTOM);
        }
        if (item == MUSIC_DISC_OTHERSIDE) {
            return binding(FFDConfig.othersideMode, FFDCompat.Feature.OTHERSIDE);
        }
        if (item == AMETHYST_SHARD) {
            return binding(FFDConfig.amethystMode, FFDCompat.Feature.AMETHYST);
        }
        if (arrayContains(item, RAW_ORE_ITEMS)) {
            if (item == RAW_COPPER) {
                return binding(FFDConfig.copperMode, FFDCompat.Feature.COPPER);
            }
            return binding(FFDConfig.rawOreMode, FFDCompat.Feature.RAW_ORE);
        }
        if (item == RAW_COPPER || item == COPPER_INGOT || item == SPYGLASS) {
            return binding(FFDConfig.copperMode, FFDCompat.Feature.COPPER);
        }
        if (item == HONEY_BOTTLE || item == HONEYCOMB) {
            return binding(FFDConfig.honeyMode, FFDCompat.Feature.HONEY);
        }
        if (item == POWDER_SNOW_BUCKET) {
            return binding(FFDConfig.powderSnowMode, FFDCompat.Feature.POWDER_SNOW);
        }
        return null;
    }

    private static boolean isDeepslateBlock(Block block) {
        return oneOf(block, FFDBlocks.DEEPSLATE, FFDBlocks.COBBLED_DEEPSLATE,
                FFDBlocks.POLISHED_DEEPSLATE, FFDBlocks.DEEPSLATE_BRICKS,
                FFDBlocks.CRACKED_DEEPSLATE_BRICKS, FFDBlocks.DEEPSLATE_TILES,
                FFDBlocks.CRACKED_DEEPSLATE_TILES, FFDBlocks.CHISELED_DEEPSLATE,
                FFDBlocks.TUFF, FFDBlocks.INFESTED_DEEPSLATE,
                FFDBlocks.COBBLED_DEEPSLATE_STAIRS, FFDBlocks.POLISHED_DEEPSLATE_STAIRS,
                FFDBlocks.DEEPSLATE_BRICK_STAIRS, FFDBlocks.DEEPSLATE_TILE_STAIRS,
                FFDBlocks.COBBLED_DEEPSLATE_SLAB, FFDBlocks.COBBLED_DEEPSLATE_DOUBLE_SLAB,
                FFDBlocks.POLISHED_DEEPSLATE_SLAB, FFDBlocks.POLISHED_DEEPSLATE_DOUBLE_SLAB,
                FFDBlocks.DEEPSLATE_BRICK_SLAB, FFDBlocks.DEEPSLATE_BRICK_DOUBLE_SLAB,
                FFDBlocks.DEEPSLATE_TILE_SLAB, FFDBlocks.DEEPSLATE_TILE_DOUBLE_SLAB,
                FFDBlocks.COBBLED_DEEPSLATE_WALL, FFDBlocks.POLISHED_DEEPSLATE_WALL,
                FFDBlocks.DEEPSLATE_BRICK_WALL, FFDBlocks.DEEPSLATE_TILE_WALL,
                FFDBlocks.DEEPSLATE_COAL_ORE, FFDBlocks.DEEPSLATE_IRON_ORE,
                FFDBlocks.DEEPSLATE_GOLD_ORE, FFDBlocks.DEEPSLATE_REDSTONE_ORE,
                FFDBlocks.DEEPSLATE_LAPIS_ORE, FFDBlocks.DEEPSLATE_DIAMOND_ORE,
                FFDBlocks.DEEPSLATE_EMERALD_ORE);
    }

    private static boolean isRawOreBlock(Block block) {
        return arrayContains(block, FFDBlocks.RAW_ORE_BLOCKS);
    }

    private static String rawOreMaterial(Block block) {
        for (int i = 0; i < FFDBlocks.RAW_ORE_BLOCKS.length; i++) {
            if (FFDBlocks.RAW_ORE_BLOCKS[i] == block) {
                return FFDRawOres.NAMES[i];
            }
        }
        return null;
    }

    private static String rawOreMaterial(Item item) {
        for (int i = 0; i < RAW_ORE_ITEMS.length; i++) {
            if (RAW_ORE_ITEMS[i] == item || RAW_ORE_BLOCK_ITEMS[i] == item) {
                return FFDRawOres.NAMES[i];
            }
        }
        return null;
    }

    private static String[] rawBlockAliases(Block block) {
        if (block == null) {
            return new String[0];
        }
        for (int i = 0; i < FFDBlocks.RAW_ORE_BLOCKS.length; i++) {
            if (FFDBlocks.RAW_ORE_BLOCKS[i] == block) {
                return new String[] {FFDRawOres.externalRawBlockName(FFDRawOres.NAMES[i])};
            }
        }
        return new String[0];
    }

    private static String[] contentPaths(Item local) {
        if (local == null || local.getRegistryName() == null) {
            return new String[0];
        }
        return concat(new String[] {local.getRegistryName().getResourcePath()},
                rawBlockAliases(LOCAL_ITEM_BLOCKS.get(local)));
    }

    private static String[] contentPaths(Block local) {
        if (local == null || local.getRegistryName() == null) {
            return new String[0];
        }
        return concat(new String[] {local.getRegistryName().getResourcePath()},
                rawBlockAliases(local));
    }

    private static String[] concat(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static boolean isCopperBlock(Block block) {
        return block == FFDBlocks.COPPER_ORE || block == FFDBlocks.DEEPSLATE_COPPER_ORE
                || block == FFDBlocks.RAW_COPPER_BLOCK
                || arrayContains(block, FFDBlocks.COPPER_BLOCKS)
                || arrayContains(block, FFDBlocks.WAXED_COPPER_BLOCKS)
                || arrayContains(block, FFDBlocks.CUT_COPPER_BLOCKS)
                || arrayContains(block, FFDBlocks.WAXED_CUT_COPPER_BLOCKS)
                || arrayContains(block, FFDBlocks.CUT_COPPER_STAIRS)
                || arrayContains(block, FFDBlocks.WAXED_CUT_COPPER_STAIRS)
                || arrayContains(block, FFDBlocks.CUT_COPPER_SLABS)
                || arrayContains(block, FFDBlocks.WAXED_CUT_COPPER_SLABS)
                || arrayContains(block, FFDBlocks.CUT_COPPER_DOUBLE_SLABS)
                || arrayContains(block, FFDBlocks.WAXED_CUT_COPPER_DOUBLE_SLABS)
                || arrayContains(block, FFDBlocks.LIGHTNING_RODS)
                || arrayContains(block, FFDBlocks.WAXED_LIGHTNING_RODS);
    }

    private static boolean isCrimsonWoodBlock(Block block) {
        return oneOf(block, FFDBlocks.CRIMSON_STEM, FFDBlocks.STRIPPED_CRIMSON_STEM,
                FFDBlocks.CRIMSON_HYPHAE, FFDBlocks.STRIPPED_CRIMSON_HYPHAE,
                FFDBlocks.CRIMSON_PLANKS,
                FFDBlocks.CRIMSON_STAIRS, FFDBlocks.CRIMSON_SLAB,
                FFDBlocks.CRIMSON_DOUBLE_SLAB, FFDBlocks.CRIMSON_FENCE,
                FFDBlocks.CRIMSON_FENCE_GATE, FFDBlocks.CRIMSON_DOOR,
                FFDBlocks.CRIMSON_TRAPDOOR, FFDBlocks.CRIMSON_BUTTON,
                FFDBlocks.CRIMSON_PRESSURE_PLATE);
    }

    private static boolean isWarpedWoodBlock(Block block) {
        return oneOf(block, FFDBlocks.WARPED_STEM, FFDBlocks.STRIPPED_WARPED_STEM,
                FFDBlocks.WARPED_HYPHAE, FFDBlocks.STRIPPED_WARPED_HYPHAE,
                FFDBlocks.WARPED_WART_BLOCK, FFDBlocks.WARPED_PLANKS,
                FFDBlocks.WARPED_STAIRS, FFDBlocks.WARPED_SLAB,
                FFDBlocks.WARPED_DOUBLE_SLAB, FFDBlocks.WARPED_FENCE,
                FFDBlocks.WARPED_FENCE_GATE, FFDBlocks.WARPED_DOOR,
                FFDBlocks.WARPED_TRAPDOOR, FFDBlocks.WARPED_BUTTON,
                FFDBlocks.WARPED_PRESSURE_PLATE);
    }

    private static boolean oneOf(Object target, Object... values) {
        for (Object value : values) {
            if (target == value) {
                return true;
            }
        }
        return false;
    }

    private static boolean arrayContains(Object target, Object[] values) {
        return oneOf(target, values);
    }

    private static FFDConfig.FeatureMode combinedMode(FFDConfig.FeatureMode first,
                                                      FFDConfig.FeatureMode second) {
        if (first == FFDConfig.FeatureMode.ENABLED || second == FFDConfig.FeatureMode.ENABLED) {
            return FFDConfig.FeatureMode.ENABLED;
        }
        if (first == FFDConfig.FeatureMode.AUTO || second == FFDConfig.FeatureMode.AUTO) {
            return FFDConfig.FeatureMode.AUTO;
        }
        return FFDConfig.FeatureMode.DISABLED;
    }

    private static FeatureBinding binding(FFDConfig.FeatureMode mode,
                                          FFDCompat.Feature feature) {
        return new FeatureBinding(mode, feature);
    }

    private static final class FeatureBinding {
        private final FFDConfig.FeatureMode mode;
        private final FFDCompat.Feature feature;

        private FeatureBinding(FFDConfig.FeatureMode mode, FFDCompat.Feature feature) {
            this.mode = mode;
            this.feature = feature;
        }
    }

    private static ItemBlock blockItem(Block block) {
        ItemBlock item = new ItemBlock(block) {
            @Override
            public int getItemBurnTime(ItemStack stack) {
                return 0;
            }
        };
        item.setRegistryName(block.getRegistryName());
        item.setUnlocalizedName(FarmerFutureDelight.MODID + "." + block.getRegistryName().getResourcePath());
        item.setCreativeTab(FFDCreativeTab.INSTANCE);
        return rememberBlockItem(item, block);
    }

    private static Item simpleItem(String name) {
        return new Item()
                .setRegistryName(FarmerFutureDelight.MODID, name)
                .setUnlocalizedName(FarmerFutureDelight.MODID + "." + name)
                .setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    private static Item[] blockItems(Block[] blocks) {
        Item[] items = new Item[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            items[i] = blockItem(blocks[i]);
        }
        return items;
    }

    private static Item[] rawOreItems() {
        Item[] items = new Item[FFDRawOres.NAMES.length];
        items[0] = RAW_GOLD;
        items[1] = RAW_IRON;
        items[2] = RAW_COPPER;
        for (int i = 3; i < items.length; i++) {
            items[i] = simpleItem(FFDRawOres.rawItemName(FFDRawOres.NAMES[i]));
        }
        return items;
    }

    private static Item[] rawOreBlockItems() {
        Item[] items = new Item[FFDRawOres.NAMES.length];
        items[0] = RAW_GOLD_BLOCK;
        items[1] = RAW_IRON_BLOCK;
        items[2] = RAW_COPPER_BLOCK;
        for (int i = 3; i < items.length; i++) {
            items[i] = blockItem(FFDBlocks.RAW_ORE_BLOCKS[i]);
        }
        return items;
    }

    private static Item[] candleItems(xy177.farmersfuturedelight.common.block.BlockCandle[] blocks) {
        Item[] items = new Item[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            ItemCandle item = new ItemCandle(blocks[i]);
            item.setRegistryName(blocks[i].getRegistryName());
            item.setUnlocalizedName(FarmerFutureDelight.MODID + "."
                    + blocks[i].getRegistryName().getResourcePath());
            item.setCreativeTab(FFDCreativeTab.INSTANCE);
            items[i] = rememberBlockItem(item, blocks[i]);
        }
        return items;
    }

    private static Item[] slabItems(net.minecraft.block.BlockSlab[] slabs,
                                    net.minecraft.block.BlockSlab[] doubleSlabs) {
        Item[] items = new Item[slabs.length];
        for (int i = 0; i < slabs.length; i++) {
            items[i] = slabItem(slabs[i], doubleSlabs[i]);
        }
        return items;
    }

    private static ItemNetherPlant netherPlantItem(Block block, Block pottedBlock) {
        ItemNetherPlant item = new ItemNetherPlant(block, pottedBlock);
        item.setRegistryName(block.getRegistryName());
        item.setUnlocalizedName(FarmerFutureDelight.MODID + "."
                + block.getRegistryName().getResourcePath());
        item.setCreativeTab(FFDCreativeTab.INSTANCE);
        return rememberBlockItem(item, block);
    }

    private static ItemNetherVine netherVineItem(
            xy177.farmersfuturedelight.common.block.BlockNetherVine block) {
        ItemNetherVine item = new ItemNetherVine(block);
        item.setRegistryName(block.getRegistryName());
        item.setUnlocalizedName(FarmerFutureDelight.MODID + "."
                + block.getRegistryName().getResourcePath());
        item.setCreativeTab(FFDCreativeTab.INSTANCE);
        return rememberBlockItem(item, block);
    }

    private static ItemSlab slabItem(net.minecraft.block.BlockSlab slab, net.minecraft.block.BlockSlab doubleSlab) {
        ItemSlab item = new ItemSlab(slab, slab, doubleSlab) {
            @Override
            public int getItemBurnTime(ItemStack stack) {
                return 0;
            }
        };
        item.setRegistryName(slab.getRegistryName());
        item.setUnlocalizedName(FarmerFutureDelight.MODID + "." + slab.getRegistryName().getResourcePath());
        item.setCreativeTab(FFDCreativeTab.INSTANCE);
        return rememberBlockItem(item, slab);
    }

    private static ItemDoor doorItem(Block door) {
        ItemDoor item = new ItemDoor(door) {
            @Override
            public int getItemBurnTime(ItemStack stack) {
                return 0;
            }
        };
        item.setRegistryName(door.getRegistryName());
        item.setUnlocalizedName(FarmerFutureDelight.MODID + "." + door.getRegistryName().getResourcePath());
        item.setCreativeTab(FFDCreativeTab.INSTANCE);
        return rememberBlockItem(item, door);
    }

    private static <T extends Item> T rememberBlockItem(T item, Block block) {
        LOCAL_ITEM_BLOCKS.put(item, block);
        LOCAL_BLOCK_ITEMS_BY_NAME.put(item.getRegistryName(), item);
        return item;
    }

    private FFDItems() {
    }
}
