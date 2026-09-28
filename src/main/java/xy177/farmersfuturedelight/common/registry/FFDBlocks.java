package xy177.farmersfuturedelight.common.registry;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;

import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.block.BlockAzalea;
import xy177.farmersfuturedelight.common.block.BlockAzaleaLeaves;
import xy177.farmersfuturedelight.common.block.BlockAmethyst;
import xy177.farmersfuturedelight.common.block.BlockAmethystCluster;
import xy177.farmersfuturedelight.common.block.BlockBeehive;
import xy177.farmersfuturedelight.common.block.BlockBigDripleaf;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.BlockBlueIce;
import xy177.farmersfuturedelight.common.block.BlockBubbleColumn;
import xy177.farmersfuturedelight.common.block.BlockBuddingAmethyst;
import xy177.farmersfuturedelight.common.block.BlockCandle;
import xy177.farmersfuturedelight.common.block.BlockCandleCake;
import xy177.farmersfuturedelight.common.block.BlockCaveVines;
import xy177.farmersfuturedelight.common.block.BlockCaveVinesPlant;
import xy177.farmersfuturedelight.common.block.BlockCoralBlock;
import xy177.farmersfuturedelight.common.block.BlockCoralPlant;
import xy177.farmersfuturedelight.common.block.BlockCoralWallFan;
import xy177.farmersfuturedelight.common.block.BlockConduit;
import xy177.farmersfuturedelight.common.block.BlockLightningRod;
import xy177.farmersfuturedelight.common.block.BlockLight;
import xy177.farmersfuturedelight.common.block.BlockDriedKelp;
import xy177.farmersfuturedelight.common.block.BlockDeepslate;
import xy177.farmersfuturedelight.common.block.BlockFutureCauldron;
import xy177.farmersfuturedelight.common.block.BlockFutureStone;
import xy177.farmersfuturedelight.common.block.BlockFutureMetal;
import xy177.farmersfuturedelight.common.block.BlockFutureOre;
import xy177.farmersfuturedelight.common.block.BlockFutureRedstoneOre;
import xy177.farmersfuturedelight.common.block.BlockFutureSlab;
import xy177.farmersfuturedelight.common.block.BlockFutureStairs;
import xy177.farmersfuturedelight.common.block.BlockFutureWall;
import xy177.farmersfuturedelight.common.block.BlockGlowLichen;
import xy177.farmersfuturedelight.common.block.BlockHoney;
import xy177.farmersfuturedelight.common.block.BlockHangingRoots;
import xy177.farmersfuturedelight.common.block.BlockHoneycomb;
import xy177.farmersfuturedelight.common.block.BlockInfestedDeepslate;
import xy177.farmersfuturedelight.common.block.BlockIronChain;
import xy177.farmersfuturedelight.common.block.BlockKelp;
import xy177.farmersfuturedelight.common.block.BlockKelpPlant;
import xy177.farmersfuturedelight.common.block.BlockKelpYoung;
import xy177.farmersfuturedelight.common.block.BlockMoss;
import xy177.farmersfuturedelight.common.block.BlockMossCarpet;
import xy177.farmersfuturedelight.common.block.BlockNetherBlock;
import xy177.farmersfuturedelight.common.block.BlockNetherFungus;
import xy177.farmersfuturedelight.common.block.BlockNetherNylium;
import xy177.farmersfuturedelight.common.block.BlockNetherPlant;
import xy177.farmersfuturedelight.common.block.BlockPottedNetherPlant;
import xy177.farmersfuturedelight.common.block.BlockPointedDripstone;
import xy177.farmersfuturedelight.common.block.BlockPowderSnow;
import xy177.farmersfuturedelight.common.block.BlockNetherStem;
import xy177.farmersfuturedelight.common.block.BlockNetherPlanks;
import xy177.farmersfuturedelight.common.block.BlockNetherStairs;
import xy177.farmersfuturedelight.common.block.BlockNetherSlab;
import xy177.farmersfuturedelight.common.block.BlockNetherFence;
import xy177.farmersfuturedelight.common.block.BlockNetherFenceGate;
import xy177.farmersfuturedelight.common.block.BlockNetherDoor;
import xy177.farmersfuturedelight.common.block.BlockNetherTrapDoor;
import xy177.farmersfuturedelight.common.block.BlockNetherButton;
import xy177.farmersfuturedelight.common.block.BlockNetherPressurePlate;
import xy177.farmersfuturedelight.common.block.BlockNetherVine;
import xy177.farmersfuturedelight.common.block.BlockNetherVinePlant;
import xy177.farmersfuturedelight.common.block.BlockNoFacePumpkin;
import xy177.farmersfuturedelight.common.block.BlockOverworldButton;
import xy177.farmersfuturedelight.common.block.BlockOverworldPressurePlate;
import xy177.farmersfuturedelight.common.block.BlockOverworldTrapDoor;
import xy177.farmersfuturedelight.common.block.BlockRootedDirt;
import xy177.farmersfuturedelight.common.block.BlockSporeBlossom;
import xy177.farmersfuturedelight.common.block.BlockStrippedLog;
import xy177.farmersfuturedelight.common.block.BlockSeagrass;
import xy177.farmersfuturedelight.common.block.BlockSeaPickle;
import xy177.farmersfuturedelight.common.block.BlockSmallDripleaf;
import xy177.farmersfuturedelight.common.block.BlockSweetBerryBush;
import xy177.farmersfuturedelight.common.block.BlockTallSeagrass;
import xy177.farmersfuturedelight.common.block.BlockTintedGlass;
import xy177.farmersfuturedelight.common.block.BlockTurtleEgg;
import xy177.farmersfuturedelight.common.block.BlockWeatheringCopper;
import xy177.farmersfuturedelight.common.block.BlockWeatheringCopperSlab;
import xy177.farmersfuturedelight.common.block.BlockWeatheringCopperStairs;
import xy177.farmersfuturedelight.common.block.CopperWeathering;

public final class FFDBlocks {
    public static final String[] CANDLE_NAMES = {
            "candle", "white_candle", "orange_candle", "magenta_candle",
            "light_blue_candle", "yellow_candle", "lime_candle", "pink_candle",
            "gray_candle", "light_gray_candle", "cyan_candle", "purple_candle",
            "blue_candle", "brown_candle", "green_candle", "red_candle", "black_candle"
    };
    public static final String[] OVERWORLD_WOOD_NAMES = {
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak"
    };
    private static final MapColor[] STRIPPED_WOOD_MAP_COLORS = {
            MapColor.WOOD, MapColor.OBSIDIAN, MapColor.SAND,
            MapColor.DIRT, MapColor.ADOBE, MapColor.BROWN
    };
    public static final String[] CORAL_NAMES = {
            "tube", "brain", "bubble", "fire", "horn"
    };

    public static final BlockSweetBerryBush SWEET_BERRY_BUSH = new BlockSweetBerryBush();
    public static final BlockMoss MOSS_BLOCK = new BlockMoss();
    public static final BlockMossCarpet MOSS_CARPET = new BlockMossCarpet();
    public static final BlockCaveVines CAVE_VINES = new BlockCaveVines();
    public static final BlockCaveVinesPlant CAVE_VINES_PLANT = new BlockCaveVinesPlant();
    public static final BlockAzalea AZALEA = new BlockAzalea(false);
    public static final BlockAzalea FLOWERING_AZALEA = new BlockAzalea(true);
    public static final BlockPottedNetherPlant POTTED_AZALEA_BUSH =
            new BlockPottedNetherPlant("potted_azalea_bush", AZALEA);
    public static final BlockPottedNetherPlant POTTED_FLOWERING_AZALEA_BUSH =
            new BlockPottedNetherPlant("potted_flowering_azalea_bush", FLOWERING_AZALEA);
    public static final BlockAzaleaLeaves AZALEA_LEAVES = new BlockAzaleaLeaves(false);
    public static final BlockAzaleaLeaves FLOWERING_AZALEA_LEAVES = new BlockAzaleaLeaves(true);
    public static final BlockSmallDripleaf SMALL_DRIPLEAF = new BlockSmallDripleaf();
    public static final BlockBigDripleafStem BIG_DRIPLEAF_STEM = new BlockBigDripleafStem();
    public static final BlockBigDripleaf BIG_DRIPLEAF = new BlockBigDripleaf("big_dripleaf", false);
    public static final BlockBigDripleaf BIG_DRIPLEAF_WATERLOGGED =
            new BlockBigDripleaf("big_dripleaf_waterlogged", true);
    public static final BlockRootedDirt ROOTED_DIRT = new BlockRootedDirt();
    public static final BlockHangingRoots HANGING_ROOTS = new BlockHangingRoots();
    public static final BlockSporeBlossom SPORE_BLOSSOM = new BlockSporeBlossom();
    public static final BlockGlowLichen GLOW_LICHEN = new BlockGlowLichen("glow_lichen", 0);
    public static final BlockGlowLichen[] GLOW_LICHEN_VARIANTS = new BlockGlowLichen[] {
            GLOW_LICHEN,
            new BlockGlowLichen("glow_lichen_1", 1),
            new BlockGlowLichen("glow_lichen_2", 2),
            new BlockGlowLichen("glow_lichen_3", 3),
            new BlockGlowLichen("glow_lichen_4", 4),
            new BlockGlowLichen("glow_lichen_5", 5),
            new BlockGlowLichen("glow_lichen_6", 6),
            new BlockGlowLichen("glow_lichen_7", 7)
    };
    public static final BlockAmethyst AMETHYST_BLOCK =
            new BlockAmethyst("amethyst_block", FFDSounds.AMETHYST);
    public static final BlockBuddingAmethyst BUDDING_AMETHYST = new BlockBuddingAmethyst();
    public static final BlockAmethystCluster SMALL_AMETHYST_BUD =
            new BlockAmethystCluster("small_amethyst_bud", 3.0F, 8.0F, 1,
                    FFDSounds.SMALL_AMETHYST_BUD, false);
    public static final BlockAmethystCluster MEDIUM_AMETHYST_BUD =
            new BlockAmethystCluster("medium_amethyst_bud", 4.0F, 10.0F, 2,
                    FFDSounds.MEDIUM_AMETHYST_BUD, false);
    public static final BlockAmethystCluster LARGE_AMETHYST_BUD =
            new BlockAmethystCluster("large_amethyst_bud", 5.0F, 10.0F, 4,
                    FFDSounds.LARGE_AMETHYST_BUD, false);
    public static final BlockAmethystCluster AMETHYST_CLUSTER =
            new BlockAmethystCluster("amethyst_cluster", 7.0F, 10.0F, 5,
                    FFDSounds.AMETHYST_CLUSTER, true);
    public static final BlockFutureStone CALCITE =
            new BlockFutureStone("calcite", 0.75F, 0.75F, FFDSounds.CALCITE);
    public static final BlockFutureStone SMOOTH_BASALT =
            new BlockFutureStone("smooth_basalt", 1.25F, 4.2F, FFDSounds.BASALT);
    public static final BlockTintedGlass TINTED_GLASS = new BlockTintedGlass();
    public static final BlockFutureStone DRIPSTONE_BLOCK =
            new BlockFutureStone("dripstone_block", 1.5F, 1.0F, FFDSounds.DRIPSTONE_BLOCK);
    public static final BlockPointedDripstone POINTED_DRIPSTONE = new BlockPointedDripstone();
    public static final BlockIronChain IRON_CHAIN = new BlockIronChain();
    public static final BlockLight LIGHT = new BlockLight();
    public static final BlockCandle[] CANDLES = candles();
    public static final BlockCandleCake[] CANDLE_CAKES = candleCakes(CANDLES);
    public static final BlockCandle CANDLE = CANDLES[0];
    public static final BlockCandleCake CANDLE_CAKE = CANDLE_CAKES[0];
    public static final BlockPowderSnow POWDER_SNOW = new BlockPowderSnow();
    public static final BlockFutureCauldron LAVA_CAULDRON = new BlockFutureCauldron(
            "lava_cauldron", BlockFutureCauldron.Content.LAVA);
    public static final BlockFutureCauldron POWDER_SNOW_CAULDRON = new BlockFutureCauldron(
            "powder_snow_cauldron", BlockFutureCauldron.Content.POWDER_SNOW);
    public static final BlockDeepslate DEEPSLATE = new BlockDeepslate();
    public static final BlockFutureStone COBBLED_DEEPSLATE =
            new BlockFutureStone("cobbled_deepslate", 3.5F, 6.0F, FFDSounds.DEEPSLATE);
    public static final BlockFutureStone POLISHED_DEEPSLATE =
            new BlockFutureStone("polished_deepslate", 3.5F, 6.0F, FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureStone DEEPSLATE_BRICKS =
            new BlockFutureStone("deepslate_bricks", 3.5F, 6.0F, FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureStone CRACKED_DEEPSLATE_BRICKS =
            new BlockFutureStone("cracked_deepslate_bricks", 3.5F, 6.0F, FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureStone DEEPSLATE_TILES =
            new BlockFutureStone("deepslate_tiles", 3.5F, 6.0F, FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureStone CRACKED_DEEPSLATE_TILES =
            new BlockFutureStone("cracked_deepslate_tiles", 3.5F, 6.0F, FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureStone CHISELED_DEEPSLATE =
            new BlockFutureStone("chiseled_deepslate", 3.5F, 6.0F, FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureStone TUFF =
            new BlockFutureStone("tuff", 1.5F, 6.0F, FFDSounds.TUFF);
    public static final BlockInfestedDeepslate INFESTED_DEEPSLATE = new BlockInfestedDeepslate();

    public static final BlockFutureStairs COBBLED_DEEPSLATE_STAIRS = new BlockFutureStairs(
            "cobbled_deepslate_stairs", COBBLED_DEEPSLATE.getDefaultState(), FFDSounds.DEEPSLATE);
    public static final BlockFutureStairs POLISHED_DEEPSLATE_STAIRS = new BlockFutureStairs(
            "polished_deepslate_stairs", POLISHED_DEEPSLATE.getDefaultState(), FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureStairs DEEPSLATE_BRICK_STAIRS = new BlockFutureStairs(
            "deepslate_brick_stairs", DEEPSLATE_BRICKS.getDefaultState(), FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureStairs DEEPSLATE_TILE_STAIRS = new BlockFutureStairs(
            "deepslate_tile_stairs", DEEPSLATE_TILES.getDefaultState(), FFDSounds.DEEPSLATE_BRICKS);

    public static final BlockFutureSlab.Half COBBLED_DEEPSLATE_SLAB =
            new BlockFutureSlab.Half("cobbled_deepslate_slab", FFDSounds.DEEPSLATE);
    public static final BlockFutureSlab.Double COBBLED_DEEPSLATE_DOUBLE_SLAB =
            new BlockFutureSlab.Double("cobbled_deepslate_double_slab", FFDSounds.DEEPSLATE,
                    COBBLED_DEEPSLATE_SLAB);
    public static final BlockFutureSlab.Half POLISHED_DEEPSLATE_SLAB =
            new BlockFutureSlab.Half("polished_deepslate_slab", FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureSlab.Double POLISHED_DEEPSLATE_DOUBLE_SLAB =
            new BlockFutureSlab.Double("polished_deepslate_double_slab", FFDSounds.DEEPSLATE_BRICKS,
                    POLISHED_DEEPSLATE_SLAB);
    public static final BlockFutureSlab.Half DEEPSLATE_BRICK_SLAB =
            new BlockFutureSlab.Half("deepslate_brick_slab", FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureSlab.Double DEEPSLATE_BRICK_DOUBLE_SLAB =
            new BlockFutureSlab.Double("deepslate_brick_double_slab", FFDSounds.DEEPSLATE_BRICKS,
                    DEEPSLATE_BRICK_SLAB);
    public static final BlockFutureSlab.Half DEEPSLATE_TILE_SLAB =
            new BlockFutureSlab.Half("deepslate_tile_slab", FFDSounds.DEEPSLATE_BRICKS);
    public static final BlockFutureSlab.Double DEEPSLATE_TILE_DOUBLE_SLAB =
            new BlockFutureSlab.Double("deepslate_tile_double_slab", FFDSounds.DEEPSLATE_BRICKS,
                    DEEPSLATE_TILE_SLAB);

    public static final BlockFutureWall COBBLED_DEEPSLATE_WALL =
            new BlockFutureWall("cobbled_deepslate_wall", COBBLED_DEEPSLATE);
    public static final BlockFutureWall POLISHED_DEEPSLATE_WALL =
            new BlockFutureWall("polished_deepslate_wall", POLISHED_DEEPSLATE);
    public static final BlockFutureWall DEEPSLATE_BRICK_WALL =
            new BlockFutureWall("deepslate_brick_wall", DEEPSLATE_BRICKS);
    public static final BlockFutureWall DEEPSLATE_TILE_WALL =
            new BlockFutureWall("deepslate_tile_wall", DEEPSLATE_TILES);

    public static final BlockFutureOre DEEPSLATE_COAL_ORE =
            new BlockFutureOre("deepslate_coal_ore", BlockFutureOre.Drop.COAL, 0);
    public static final BlockFutureOre DEEPSLATE_IRON_ORE =
            new BlockFutureOre("deepslate_iron_ore", BlockFutureOre.Drop.IRON, 1);
    public static final BlockFutureOre DEEPSLATE_COPPER_ORE =
            new BlockFutureOre("deepslate_copper_ore", BlockFutureOre.Drop.COPPER, 1);
    public static final BlockFutureOre DEEPSLATE_GOLD_ORE =
            new BlockFutureOre("deepslate_gold_ore", BlockFutureOre.Drop.GOLD, 2);
    public static final BlockFutureRedstoneOre DEEPSLATE_REDSTONE_ORE =
            new BlockFutureRedstoneOre();
    public static final BlockFutureOre DEEPSLATE_LAPIS_ORE =
            new BlockFutureOre("deepslate_lapis_ore", BlockFutureOre.Drop.LAPIS, 1);
    public static final BlockFutureOre DEEPSLATE_DIAMOND_ORE =
            new BlockFutureOre("deepslate_diamond_ore", BlockFutureOre.Drop.DIAMOND, 2);
    public static final BlockFutureOre DEEPSLATE_EMERALD_ORE =
            new BlockFutureOre("deepslate_emerald_ore", BlockFutureOre.Drop.EMERALD, 2);
    public static final BlockFutureOre COPPER_ORE = new BlockFutureOre(
            "copper_ore", BlockFutureOre.Drop.COPPER, 1, 3.0F, net.minecraft.block.SoundType.STONE);

    public static final BlockFutureMetal RAW_IRON_BLOCK = new BlockFutureMetal("raw_iron_block", 1);
    public static final BlockFutureMetal RAW_GOLD_BLOCK = new BlockFutureMetal("raw_gold_block", 2);
    public static final BlockFutureMetal RAW_COPPER_BLOCK = new BlockFutureMetal("raw_copper_block", 1);
    public static final BlockFutureMetal[] RAW_ORE_BLOCKS = rawOreBlocks();

    public static final BlockWeatheringCopper[] COPPER_BLOCKS = new BlockWeatheringCopper[] {
            copperBlock("copper_block", CopperWeathering.WeatherState.UNAFFECTED, false),
            copperBlock("exposed_copper", CopperWeathering.WeatherState.EXPOSED, false),
            copperBlock("weathered_copper", CopperWeathering.WeatherState.WEATHERED, false),
            copperBlock("oxidized_copper", CopperWeathering.WeatherState.OXIDIZED, false)
    };
    public static final BlockWeatheringCopper[] WAXED_COPPER_BLOCKS = new BlockWeatheringCopper[] {
            copperBlock("waxed_copper_block", CopperWeathering.WeatherState.UNAFFECTED, true),
            copperBlock("waxed_exposed_copper", CopperWeathering.WeatherState.EXPOSED, true),
            copperBlock("waxed_weathered_copper", CopperWeathering.WeatherState.WEATHERED, true),
            copperBlock("waxed_oxidized_copper", CopperWeathering.WeatherState.OXIDIZED, true)
    };
    public static final BlockWeatheringCopper[] CUT_COPPER_BLOCKS = new BlockWeatheringCopper[] {
            copperBlock("cut_copper", CopperWeathering.WeatherState.UNAFFECTED, false),
            copperBlock("exposed_cut_copper", CopperWeathering.WeatherState.EXPOSED, false),
            copperBlock("weathered_cut_copper", CopperWeathering.WeatherState.WEATHERED, false),
            copperBlock("oxidized_cut_copper", CopperWeathering.WeatherState.OXIDIZED, false)
    };
    public static final BlockWeatheringCopper[] WAXED_CUT_COPPER_BLOCKS = new BlockWeatheringCopper[] {
            copperBlock("waxed_cut_copper", CopperWeathering.WeatherState.UNAFFECTED, true),
            copperBlock("waxed_exposed_cut_copper", CopperWeathering.WeatherState.EXPOSED, true),
            copperBlock("waxed_weathered_cut_copper", CopperWeathering.WeatherState.WEATHERED, true),
            copperBlock("waxed_oxidized_cut_copper", CopperWeathering.WeatherState.OXIDIZED, true)
    };
    public static final BlockWeatheringCopperStairs[] CUT_COPPER_STAIRS = copperStairs(
            new String[] {"cut_copper_stairs", "exposed_cut_copper_stairs",
                    "weathered_cut_copper_stairs", "oxidized_cut_copper_stairs"},
            CUT_COPPER_BLOCKS, false);
    public static final BlockWeatheringCopperStairs[] WAXED_CUT_COPPER_STAIRS = copperStairs(
            new String[] {"waxed_cut_copper_stairs", "waxed_exposed_cut_copper_stairs",
                    "waxed_weathered_cut_copper_stairs", "waxed_oxidized_cut_copper_stairs"},
            WAXED_CUT_COPPER_BLOCKS, true);
    public static final BlockWeatheringCopperSlab.Half[] CUT_COPPER_SLABS = copperSlabs(
            new String[] {"cut_copper_slab", "exposed_cut_copper_slab",
                    "weathered_cut_copper_slab", "oxidized_cut_copper_slab"}, false);
    public static final BlockWeatheringCopperSlab.Half[] WAXED_CUT_COPPER_SLABS = copperSlabs(
            new String[] {"waxed_cut_copper_slab", "waxed_exposed_cut_copper_slab",
                    "waxed_weathered_cut_copper_slab", "waxed_oxidized_cut_copper_slab"}, true);
    public static final BlockWeatheringCopperSlab.Double[] CUT_COPPER_DOUBLE_SLABS = copperDoubleSlabs(
            new String[] {"cut_copper_double_slab", "exposed_cut_copper_double_slab",
                    "weathered_cut_copper_double_slab", "oxidized_cut_copper_double_slab"},
            CUT_COPPER_SLABS, false);
    public static final BlockWeatheringCopperSlab.Double[] WAXED_CUT_COPPER_DOUBLE_SLABS =
            copperDoubleSlabs(new String[] {"waxed_cut_copper_double_slab",
                            "waxed_exposed_cut_copper_double_slab",
                            "waxed_weathered_cut_copper_double_slab",
                            "waxed_oxidized_cut_copper_double_slab"},
                    WAXED_CUT_COPPER_SLABS, true);

    public static final BlockLightningRod[] LIGHTNING_RODS = lightningRods(
            new String[] {"lightning_rod", "exposed_lightning_rod",
                    "weathered_lightning_rod", "oxidized_lightning_rod"}, false);
    public static final BlockLightningRod[] WAXED_LIGHTNING_RODS = lightningRods(
            new String[] {"waxed_lightning_rod", "waxed_exposed_lightning_rod",
                    "waxed_weathered_lightning_rod", "waxed_oxidized_lightning_rod"}, true);
    public static final BlockLightningRod[] LIGHTNING_RODS_WATERLOGGED = lightningRods(
            new String[] {"lightning_rod", "exposed_lightning_rod",
                    "weathered_lightning_rod", "oxidized_lightning_rod"}, false, true);
    public static final BlockLightningRod[] WAXED_LIGHTNING_RODS_WATERLOGGED = lightningRods(
            new String[] {"waxed_lightning_rod", "waxed_exposed_lightning_rod",
                    "waxed_weathered_lightning_rod", "waxed_oxidized_lightning_rod"}, true, true);

    public static final BlockWeatheringCopper COPPER_BLOCK = COPPER_BLOCKS[0];
    public static final BlockWeatheringCopper CUT_COPPER = CUT_COPPER_BLOCKS[0];
    public static final BlockWeatheringCopperStairs CUT_COPPER_STAIR = CUT_COPPER_STAIRS[0];
    public static final BlockWeatheringCopperSlab.Half CUT_COPPER_SLAB = CUT_COPPER_SLABS[0];
    public static final BlockWeatheringCopperSlab.Double CUT_COPPER_DOUBLE_SLAB =
            CUT_COPPER_DOUBLE_SLABS[0];
    public static final BlockLightningRod LIGHTNING_ROD = LIGHTNING_RODS[0];
    public static final BlockKelpYoung KELP_YOUNG = new BlockKelpYoung();
    public static final BlockKelp KELP = new BlockKelp();
    public static final BlockKelpPlant KELP_PLANT = new BlockKelpPlant();
    public static final BlockDriedKelp DRIED_KELP_BLOCK = new BlockDriedKelp();
    public static final BlockSeagrass SEAGRASS = new BlockSeagrass();
    public static final BlockTallSeagrass TALL_SEAGRASS = new BlockTallSeagrass();
    public static final BlockSeaPickle SEA_PICKLE = new BlockSeaPickle();
    public static final BlockCoralBlock[] DEAD_CORAL_BLOCKS = coralBlocks(null);
    public static final BlockCoralBlock[] CORAL_BLOCKS = coralBlocks(DEAD_CORAL_BLOCKS);
    public static final BlockCoralPlant[] DEAD_CORALS = corals(false, null);
    public static final BlockCoralPlant[] CORALS = corals(false, DEAD_CORALS);
    public static final BlockCoralWallFan[] DEAD_CORAL_WALL_FANS = coralWallFans(null);
    public static final BlockCoralWallFan[] CORAL_WALL_FANS = coralWallFans(DEAD_CORAL_WALL_FANS);
    public static final BlockCoralPlant[] DEAD_CORAL_FANS = corals(true, null);
    public static final BlockCoralPlant[] CORAL_FANS = corals(true, DEAD_CORAL_FANS);
    public static final BlockTurtleEgg TURTLE_EGG = new BlockTurtleEgg();
    public static final BlockBlueIce BLUE_ICE = new BlockBlueIce();
    public static final BlockConduit CONDUIT = new BlockConduit();
    public static final BlockBubbleColumn BUBBLE_COLUMN = new BlockBubbleColumn();
    public static final BlockStrippedLog[] STRIPPED_LOGS = strippedLogs(false);
    public static final BlockStrippedLog[] STRIPPED_WOODS = strippedLogs(true);
    public static final BlockOverworldTrapDoor[] OVERWORLD_TRAPDOORS = overworldTrapdoors(false);
    public static final BlockOverworldTrapDoor[] OVERWORLD_TRAPDOORS_WATERLOGGED = overworldTrapdoors(true);
    public static final BlockOverworldButton[] OVERWORLD_BUTTONS = overworldButtons();
    public static final BlockOverworldPressurePlate[] OVERWORLD_PRESSURE_PLATES = overworldPressurePlates();
    public static final BlockNoFacePumpkin PUMPKIN = new BlockNoFacePumpkin();
    public static final BlockFutureStairs[] PRISMARINE_STAIRS = prismarineStairs();
    public static final BlockFutureSlab.Half[] PRISMARINE_SLABS = prismarineSlabs();
    public static final BlockFutureSlab.Double[] PRISMARINE_DOUBLE_SLABS =
            prismarineDoubleSlabs(PRISMARINE_SLABS);
    public static final BlockHoney HONEY_BLOCK = new BlockHoney();
    public static final BlockHoneycomb HONEYCOMB_BLOCK = new BlockHoneycomb();
    public static final BlockBeehive BEE_NEST = new BlockBeehive(true);
    public static final BlockBeehive BEEHIVE = new BlockBeehive(false);
    public static final BlockNetherNylium CRIMSON_NYLIUM = new BlockNetherNylium(false);
    public static final BlockNetherNylium WARPED_NYLIUM = new BlockNetherNylium(true);
    public static final BlockNetherFungus CRIMSON_FUNGUS = new BlockNetherFungus(false);
    public static final BlockNetherFungus WARPED_FUNGUS = new BlockNetherFungus(true);
    public static final BlockNetherPlant CRIMSON_ROOTS = new BlockNetherPlant("crimson_roots");
    public static final BlockNetherPlant WARPED_ROOTS = new BlockNetherPlant("warped_roots");
    public static final BlockNetherPlant NETHER_SPROUTS =
            new BlockNetherPlant("nether_sprouts", 12.0D, 3.0D);
    public static final BlockPottedNetherPlant POTTED_CRIMSON_FUNGUS =
            new BlockPottedNetherPlant("potted_crimson_fungus", CRIMSON_FUNGUS);
    public static final BlockPottedNetherPlant POTTED_WARPED_FUNGUS =
            new BlockPottedNetherPlant("potted_warped_fungus", WARPED_FUNGUS);
    public static final BlockPottedNetherPlant POTTED_CRIMSON_ROOTS =
            new BlockPottedNetherPlant("potted_crimson_roots", CRIMSON_ROOTS);
    public static final BlockPottedNetherPlant POTTED_WARPED_ROOTS =
            new BlockPottedNetherPlant("potted_warped_roots", WARPED_ROOTS);
    public static final BlockNetherVine WEEPING_VINES = new BlockNetherVine("weeping_vines", false, false);
    public static final BlockNetherVinePlant WEEPING_VINES_PLANT = new BlockNetherVinePlant("weeping_vines_plant", false);
    public static final BlockNetherVine TWISTING_VINES = new BlockNetherVine("twisting_vines", true, true);
    public static final BlockNetherVinePlant TWISTING_VINES_PLANT = new BlockNetherVinePlant("twisting_vines_plant", true);
    public static final BlockNetherStem CRIMSON_STEM = new BlockNetherStem("crimson_stem");
    public static final BlockNetherStem STRIPPED_CRIMSON_STEM = new BlockNetherStem("stripped_crimson_stem");
    public static final BlockNetherStem CRIMSON_HYPHAE = new BlockNetherStem("crimson_hyphae");
    public static final BlockNetherStem STRIPPED_CRIMSON_HYPHAE = new BlockNetherStem("stripped_crimson_hyphae");
    public static final BlockNetherStem WARPED_STEM = new BlockNetherStem("warped_stem");
    public static final BlockNetherStem STRIPPED_WARPED_STEM = new BlockNetherStem("stripped_warped_stem");
    public static final BlockNetherStem WARPED_HYPHAE = new BlockNetherStem("warped_hyphae");
    public static final BlockNetherStem STRIPPED_WARPED_HYPHAE = new BlockNetherStem("stripped_warped_hyphae");
    public static final BlockNetherBlock WARPED_WART_BLOCK = new BlockNetherBlock("warped_wart_block",
            net.minecraft.block.material.Material.GRASS, FFDSounds.WART_BLOCK, 1.0F);
    public static final BlockNetherBlock SHROOMLIGHT = new BlockNetherBlock("shroomlight",
            net.minecraft.block.material.Material.GLASS, FFDSounds.SHROOMLIGHT, 1.0F);
    public static final BlockNetherPlanks CRIMSON_PLANKS = new BlockNetherPlanks("crimson_planks");
    public static final BlockNetherPlanks WARPED_PLANKS = new BlockNetherPlanks("warped_planks");
    public static final BlockNetherStairs CRIMSON_STAIRS = new BlockNetherStairs("crimson_stairs",
            CRIMSON_PLANKS.getDefaultState());
    public static final BlockNetherStairs WARPED_STAIRS = new BlockNetherStairs("warped_stairs",
            WARPED_PLANKS.getDefaultState());
    public static final BlockNetherSlab.Half CRIMSON_SLAB = new BlockNetherSlab.Half("crimson_slab", true);
    public static final BlockNetherSlab.Double CRIMSON_DOUBLE_SLAB = new BlockNetherSlab.Double("crimson_double_slab", true);
    public static final BlockNetherSlab.Half WARPED_SLAB = new BlockNetherSlab.Half("warped_slab", false);
    public static final BlockNetherSlab.Double WARPED_DOUBLE_SLAB = new BlockNetherSlab.Double("warped_double_slab", false);
    public static final BlockNetherFence CRIMSON_FENCE = new BlockNetherFence("crimson_fence");
    public static final BlockNetherFence WARPED_FENCE = new BlockNetherFence("warped_fence");
    public static final BlockNetherFenceGate CRIMSON_FENCE_GATE = new BlockNetherFenceGate("crimson_fence_gate");
    public static final BlockNetherFenceGate WARPED_FENCE_GATE = new BlockNetherFenceGate("warped_fence_gate");
    public static final BlockNetherDoor CRIMSON_DOOR = new BlockNetherDoor("crimson_door");
    public static final BlockNetherDoor WARPED_DOOR = new BlockNetherDoor("warped_door");
    public static final BlockNetherTrapDoor CRIMSON_TRAPDOOR = new BlockNetherTrapDoor("crimson_trapdoor");
    public static final BlockNetherTrapDoor WARPED_TRAPDOOR = new BlockNetherTrapDoor("warped_trapdoor");
    public static final BlockNetherTrapDoor CRIMSON_TRAPDOOR_WATERLOGGED =
            new BlockNetherTrapDoor("crimson_trapdoor", true);
    public static final BlockNetherTrapDoor WARPED_TRAPDOOR_WATERLOGGED =
            new BlockNetherTrapDoor("warped_trapdoor", true);
    public static final BlockNetherButton CRIMSON_BUTTON = new BlockNetherButton("crimson_button");
    public static final BlockNetherButton WARPED_BUTTON = new BlockNetherButton("warped_button");
    public static final BlockNetherPressurePlate CRIMSON_PRESSURE_PLATE = new BlockNetherPressurePlate("crimson_pressure_plate");
    public static final BlockNetherPressurePlate WARPED_PRESSURE_PLATE = new BlockNetherPressurePlate("warped_pressure_plate");

    static {
        SHROOMLIGHT.setLightLevel(1.0F);
        registerCopperFamily(COPPER_BLOCKS, WAXED_COPPER_BLOCKS);
        registerCopperFamily(CUT_COPPER_BLOCKS, WAXED_CUT_COPPER_BLOCKS);
        registerCopperFamily(CUT_COPPER_STAIRS, WAXED_CUT_COPPER_STAIRS);
        registerCopperFamily(CUT_COPPER_SLABS, WAXED_CUT_COPPER_SLABS);
        registerCopperFamily(CUT_COPPER_DOUBLE_SLABS, WAXED_CUT_COPPER_DOUBLE_SLABS);
        registerCopperFamily(LIGHTNING_RODS, WAXED_LIGHTNING_RODS);
        registerCopperFamily(LIGHTNING_RODS_WATERLOGGED,
                WAXED_LIGHTNING_RODS_WATERLOGGED);
        registerWaterloggedRendering(AZALEA_LEAVES, FLOWERING_AZALEA_LEAVES,
                KELP_YOUNG, KELP, KELP_PLANT, SEAGRASS,
                TALL_SEAGRASS, SEA_PICKLE, SMALL_DRIPLEAF, BIG_DRIPLEAF_STEM,
                BIG_DRIPLEAF, BIG_DRIPLEAF_WATERLOGGED, HANGING_ROOTS,
                SMALL_AMETHYST_BUD, MEDIUM_AMETHYST_BUD, LARGE_AMETHYST_BUD,
                AMETHYST_CLUSTER, IRON_CHAIN);
        registerWaterloggedRendering(COBBLED_DEEPSLATE_WALL, POLISHED_DEEPSLATE_WALL,
                DEEPSLATE_BRICK_WALL, DEEPSLATE_TILE_WALL);
        registerWaterloggedRendering(CANDLES);
        registerWaterloggedRendering(GLOW_LICHEN_VARIANTS);
        registerWaterloggedRendering(DEAD_CORALS);
        registerWaterloggedRendering(CORALS);
        registerWaterloggedRendering(DEAD_CORAL_FANS);
        registerWaterloggedRendering(CORAL_FANS);
        registerWaterloggedRendering(DEAD_CORAL_WALL_FANS);
        registerWaterloggedRendering(CORAL_WALL_FANS);
        registerWaterloggedRendering(CONDUIT);
        registerWaterloggedRendering(BUBBLE_COLUMN);
        registerWaterloggedRendering(PRISMARINE_STAIRS);
        registerWaterloggedRendering(PRISMARINE_SLABS);
        registerWaterloggedRendering(PRISMARINE_DOUBLE_SLABS);
        registerWaterloggedRendering(COBBLED_DEEPSLATE_STAIRS, POLISHED_DEEPSLATE_STAIRS,
                DEEPSLATE_BRICK_STAIRS, DEEPSLATE_TILE_STAIRS,
                COBBLED_DEEPSLATE_SLAB, POLISHED_DEEPSLATE_SLAB,
                DEEPSLATE_BRICK_SLAB, DEEPSLATE_TILE_SLAB,
                COBBLED_DEEPSLATE_DOUBLE_SLAB, POLISHED_DEEPSLATE_DOUBLE_SLAB,
                DEEPSLATE_BRICK_DOUBLE_SLAB, DEEPSLATE_TILE_DOUBLE_SLAB);
        registerWaterloggedRenderingArrays(CUT_COPPER_STAIRS, WAXED_CUT_COPPER_STAIRS,
                CUT_COPPER_SLABS, WAXED_CUT_COPPER_SLABS,
                CUT_COPPER_DOUBLE_SLABS, WAXED_CUT_COPPER_DOUBLE_SLABS);
        registerWaterloggedRendering(CRIMSON_STAIRS, WARPED_STAIRS,
                CRIMSON_SLAB, WARPED_SLAB, CRIMSON_FENCE, WARPED_FENCE,
                CRIMSON_TRAPDOOR, WARPED_TRAPDOOR,
                CRIMSON_TRAPDOOR_WATERLOGGED, WARPED_TRAPDOOR_WATERLOGGED);
        registerWaterloggedRenderingArrays(LIGHTNING_RODS, WAXED_LIGHTNING_RODS,
                LIGHTNING_RODS_WATERLOGGED, WAXED_LIGHTNING_RODS_WATERLOGGED);
        registerWaterloggedRenderingArrays(OVERWORLD_TRAPDOORS, OVERWORLD_TRAPDOORS_WATERLOGGED);
    }

    private static BlockCoralBlock[] coralBlocks(BlockCoralBlock[] deadVariants) {
        BlockCoralBlock[] blocks = new BlockCoralBlock[CORAL_NAMES.length];
        for (int i = 0; i < blocks.length; i++) {
            String prefix = deadVariants == null ? "dead_" : "";
            blocks[i] = new BlockCoralBlock(prefix + CORAL_NAMES[i] + "_coral_block", i,
                    deadVariants == null ? null : deadVariants[i]);
        }
        return blocks;
    }

    private static BlockCoralPlant[] corals(boolean fan, BlockCoralPlant[] deadVariants) {
        BlockCoralPlant[] blocks = new BlockCoralPlant[CORAL_NAMES.length];
        for (int i = 0; i < blocks.length; i++) {
            String prefix = deadVariants == null ? "dead_" : "";
            String suffix = fan ? "_coral_fan" : "_coral";
            blocks[i] = new BlockCoralPlant(prefix + CORAL_NAMES[i] + suffix, i, fan,
                    deadVariants == null ? null : deadVariants[i]);
        }
        return blocks;
    }

    private static BlockCoralWallFan[] coralWallFans(BlockCoralWallFan[] deadVariants) {
        BlockCoralWallFan[] blocks = new BlockCoralWallFan[CORAL_NAMES.length];
        for (int i = 0; i < blocks.length; i++) {
            String prefix = deadVariants == null ? "dead_" : "";
            blocks[i] = new BlockCoralWallFan(prefix + CORAL_NAMES[i]
                    + "_coral_wall_fan", i, deadVariants == null ? null : deadVariants[i]);
        }
        return blocks;
    }

    private static BlockWeatheringCopper copperBlock(String name,
                                                      CopperWeathering.WeatherState age,
                                                      boolean waxed) {
        return new BlockWeatheringCopper(name, age, waxed);
    }

    private static BlockStrippedLog[] strippedLogs(boolean wood) {
        BlockStrippedLog[] blocks = new BlockStrippedLog[OVERWORLD_WOOD_NAMES.length];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new BlockStrippedLog("stripped_" + OVERWORLD_WOOD_NAMES[i]
                    + (wood ? "_wood" : "_log"), STRIPPED_WOOD_MAP_COLORS[i]);
        }
        return blocks;
    }

    private static BlockOverworldTrapDoor[] overworldTrapdoors(boolean waterlogged) {
        BlockOverworldTrapDoor[] blocks = new BlockOverworldTrapDoor[OVERWORLD_WOOD_NAMES.length];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new BlockOverworldTrapDoor(OVERWORLD_WOOD_NAMES[i] + "_trapdoor", i,
                    waterlogged);
        }
        return blocks;
    }

    private static BlockOverworldButton[] overworldButtons() {
        BlockOverworldButton[] blocks = new BlockOverworldButton[OVERWORLD_WOOD_NAMES.length];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new BlockOverworldButton(OVERWORLD_WOOD_NAMES[i] + "_button");
        }
        return blocks;
    }

    private static BlockOverworldPressurePlate[] overworldPressurePlates() {
        BlockOverworldPressurePlate[] blocks =
                new BlockOverworldPressurePlate[OVERWORLD_WOOD_NAMES.length];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new BlockOverworldPressurePlate(
                    OVERWORLD_WOOD_NAMES[i] + "_pressure_plate");
        }
        return blocks;
    }

    private static BlockFutureStairs[] prismarineStairs() {
        net.minecraft.block.state.IBlockState[] states = {
                net.minecraft.init.Blocks.PRISMARINE.getStateFromMeta(0),
                net.minecraft.init.Blocks.PRISMARINE.getStateFromMeta(1),
                net.minecraft.init.Blocks.PRISMARINE.getStateFromMeta(2)
        };
        String[] names = {"prismarine_stairs", "prismarine_brick_stairs",
                "dark_prismarine_stairs"};
        BlockFutureStairs[] stairs = new BlockFutureStairs[names.length];
        for (int i = 0; i < names.length; i++) {
            stairs[i] = new BlockFutureStairs(names[i], states[i], net.minecraft.block.SoundType.STONE);
            stairs[i].setHardness(1.5F);
            stairs[i].setResistance(10.0F);
        }
        return stairs;
    }

    private static BlockFutureSlab.Half[] prismarineSlabs() {
        String[] names = {"prismarine_slab", "prismarine_brick_slab",
                "dark_prismarine_slab"};
        BlockFutureSlab.Half[] slabs = new BlockFutureSlab.Half[names.length];
        for (int i = 0; i < names.length; i++) {
            slabs[i] = new BlockFutureSlab.Half(names[i], net.minecraft.block.SoundType.STONE);
            slabs[i].setHardness(1.5F);
            slabs[i].setResistance(10.0F);
        }
        return slabs;
    }

    private static BlockFutureSlab.Double[] prismarineDoubleSlabs(
            BlockFutureSlab.Half[] itemSlabs) {
        String[] names = {"prismarine_double_slab", "prismarine_brick_double_slab",
                "dark_prismarine_double_slab"};
        BlockFutureSlab.Double[] slabs = new BlockFutureSlab.Double[names.length];
        for (int i = 0; i < names.length; i++) {
            slabs[i] = new BlockFutureSlab.Double(names[i], net.minecraft.block.SoundType.STONE,
                    itemSlabs[i]);
            slabs[i].setHardness(1.5F);
            slabs[i].setResistance(10.0F);
        }
        return slabs;
    }

    private static BlockFutureMetal[] rawOreBlocks() {
        BlockFutureMetal[] blocks = new BlockFutureMetal[FFDRawOres.NAMES.length];
        blocks[0] = RAW_GOLD_BLOCK;
        blocks[1] = RAW_IRON_BLOCK;
        blocks[2] = RAW_COPPER_BLOCK;
        for (int i = 3; i < blocks.length; i++) {
            blocks[i] = new BlockFutureMetal(FFDRawOres.rawBlockName(FFDRawOres.NAMES[i]), 1);
        }
        return blocks;
    }

    private static BlockWeatheringCopperStairs[] copperStairs(String[] names,
                                                               BlockWeatheringCopper[] modelBlocks,
                                                               boolean waxed) {
        BlockWeatheringCopperStairs[] stairs = new BlockWeatheringCopperStairs[names.length];
        CopperWeathering.WeatherState[] ages = CopperWeathering.WeatherState.values();
        for (int i = 0; i < names.length; i++) {
            stairs[i] = new BlockWeatheringCopperStairs(names[i], modelBlocks[i].getDefaultState(),
                    ages[i], waxed);
        }
        return stairs;
    }

    private static BlockWeatheringCopperSlab.Half[] copperSlabs(String[] names, boolean waxed) {
        BlockWeatheringCopperSlab.Half[] slabs =
                new BlockWeatheringCopperSlab.Half[names.length];
        CopperWeathering.WeatherState[] ages = CopperWeathering.WeatherState.values();
        for (int i = 0; i < names.length; i++) {
            slabs[i] = new BlockWeatheringCopperSlab.Half(names[i], ages[i], waxed);
        }
        return slabs;
    }

    private static BlockWeatheringCopperSlab.Double[] copperDoubleSlabs(
            String[] names, BlockWeatheringCopperSlab.Half[] itemSlabs, boolean waxed) {
        BlockWeatheringCopperSlab.Double[] slabs =
                new BlockWeatheringCopperSlab.Double[names.length];
        CopperWeathering.WeatherState[] ages = CopperWeathering.WeatherState.values();
        for (int i = 0; i < names.length; i++) {
            slabs[i] = new BlockWeatheringCopperSlab.Double(names[i], itemSlabs[i], ages[i], waxed);
        }
        return slabs;
    }

    private static BlockLightningRod[] lightningRods(String[] names, boolean waxed) {
        return lightningRods(names, waxed, false);
    }

    private static BlockLightningRod[] lightningRods(String[] names, boolean waxed,
                                                      boolean waterlogged) {
        BlockLightningRod[] rods = new BlockLightningRod[names.length];
        CopperWeathering.WeatherState[] ages = CopperWeathering.WeatherState.values();
        for (int i = 0; i < names.length; i++) {
            rods[i] = new BlockLightningRod(names[i], ages[i], waxed, waterlogged);
        }
        return rods;
    }

    private static BlockCandle[] candles() {
        BlockCandle[] candles = new BlockCandle[CANDLE_NAMES.length];
        for (int i = 0; i < CANDLE_NAMES.length; i++) {
            candles[i] = new BlockCandle(CANDLE_NAMES[i]);
        }
        return candles;
    }

    private static BlockCandleCake[] candleCakes(BlockCandle[] candles) {
        BlockCandleCake[] cakes = new BlockCandleCake[candles.length];
        for (int i = 0; i < candles.length; i++) {
            cakes[i] = new BlockCandleCake(CANDLE_NAMES[i] + "_cake", candles[i]);
        }
        return cakes;
    }

    private static void registerCopperFamily(Block[] unwaxed, Block[] waxed) {
        CopperWeathering.registerWeatheringSequence(unwaxed);
        for (int i = 0; i < unwaxed.length; i++) {
            CopperWeathering.registerWaxedPair(unwaxed[i], waxed[i]);
        }
    }

    private static void registerWaterloggedRendering(Block... blocks) {
        for (Block block : blocks) {
            WaterloggedBlockApi.registerRenderedBlock(block);
            WaterloggedBlockApi.registerWaterColorBlock(block);
        }
    }

    private static void registerWaterloggedRenderingArrays(Block[]... groups) {
        for (Block[] group : groups) {
            registerWaterloggedRendering(group);
        }
    }

    private FFDBlocks() {
    }
}
