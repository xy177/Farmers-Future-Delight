package xy177.farmersfuturedelight.common.registry;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import xy177.farmersfuturedelight.common.block.BlockWoodStandingSign;
import xy177.farmersfuturedelight.common.block.BlockWoodWallSign;
import net.minecraft.item.Item.ToolMaterial;
import xy177.farmersfuturedelight.common.item.ItemScaffolding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.common.util.EnumHelper;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.item.ItemAxolotlBucket;
import xy177.farmersfuturedelight.common.item.ItemBigDripleaf;
import xy177.farmersfuturedelight.common.item.ItemCandle;
import xy177.farmersfuturedelight.common.item.ItemCrossbow;
import xy177.farmersfuturedelight.common.item.ItemBlockDriedKelp;
import xy177.farmersfuturedelight.common.item.ItemWoodSign;
import xy177.farmersfuturedelight.common.item.ItemCoralFan;
import xy177.farmersfuturedelight.common.item.ItemDriedKelp;
import xy177.farmersfuturedelight.common.item.ItemFishBucket;
import xy177.farmersfuturedelight.common.item.ItemGlowBerries;
import xy177.farmersfuturedelight.common.item.ItemGlowInkSac;
import xy177.farmersfuturedelight.common.item.ItemGlowItemFrame;
import xy177.farmersfuturedelight.common.item.ItemGoatHorn;
import xy177.farmersfuturedelight.common.item.ItemHeartOfTheSea;
import xy177.farmersfuturedelight.common.item.ItemHoneyBottle;
import xy177.farmersfuturedelight.common.item.ItemHoneycomb;
import xy177.farmersfuturedelight.common.item.ItemHangingRoots;
import xy177.farmersfuturedelight.common.item.ItemKelp;
import xy177.farmersfuturedelight.common.item.ItemLightBlock;
import xy177.farmersfuturedelight.common.item.ItemMusicDisc;
import xy177.farmersfuturedelight.common.item.ItemNetherPlant;
import xy177.farmersfuturedelight.common.item.ItemNetheriteAxe;
import xy177.farmersfuturedelight.common.item.ItemNetheriteArmor;
import xy177.farmersfuturedelight.common.item.ItemNetheriteBlock;
import xy177.farmersfuturedelight.common.item.ItemNetheriteComponent;
import xy177.farmersfuturedelight.common.item.ItemNetheriteHoe;
import xy177.farmersfuturedelight.common.item.ItemNetheriteHorseArmor;
import xy177.farmersfuturedelight.common.item.ItemNetheritePickaxe;
import xy177.farmersfuturedelight.common.item.ItemNetheriteShovel;
import xy177.farmersfuturedelight.common.item.ItemNetheriteSword;
import xy177.farmersfuturedelight.common.item.ItemNetherVine;
import xy177.farmersfuturedelight.common.item.ItemPowderSnowBucket;
import xy177.farmersfuturedelight.common.item.ItemSeagrass;
import xy177.farmersfuturedelight.common.item.ItemSeaPickle;
import xy177.farmersfuturedelight.common.item.ItemSmallDripleaf;
import xy177.farmersfuturedelight.common.item.ItemSweetBerries;
import xy177.farmersfuturedelight.common.item.ItemSpyglass;
import xy177.farmersfuturedelight.common.item.ItemSuspiciousStew;
import xy177.farmersfuturedelight.common.item.ItemLeatherHorseArmor;
import xy177.farmersfuturedelight.common.item.ItemTurtleEgg;
import xy177.farmersfuturedelight.common.item.ItemTurtleHelmet;
import xy177.farmersfuturedelight.common.item.ItemTrident;
import xy177.farmersfuturedelight.common.item.ItemModernDye;
import xy177.farmersfuturedelight.common.item.ItemBannerPattern;
import net.minecraft.tileentity.BannerPattern;

public final class FFDItems {
    private static final Map<Item, Block> LOCAL_ITEM_BLOCKS = new IdentityHashMap<>();
    private static final Map<ResourceLocation, Block> LOCAL_BLOCKS_BY_NAME = new HashMap<>();
    private static final Map<net.minecraft.util.ResourceLocation, Item> LOCAL_BLOCK_ITEMS_BY_NAME =
            new HashMap<>();

    public static final ArmorMaterial TURTLE_SCUTE_ARMOR = EnumHelper.addArmorMaterial(
            "turtle_scute", FarmerFutureDelight.MODID + ":turtle_scute", 25,
            new int[] {2, 5, 6, 2}, 9, FFDSounds.TURTLE_ARMOR_EQUIP, 0.0F);
    public static final ToolMaterial NETHERITE_TOOL_MATERIAL = EnumHelper.addToolMaterial(
            "FFD_NETHERITE", 4, 2031, 9.0F, 4.0F, 15);
    public static final ArmorMaterial NETHERITE_ARMOR_MATERIAL = EnumHelper.addArmorMaterial(
            "FFD_NETHERITE", FarmerFutureDelight.MODID + ":netherite", 37,
            new int[] {3, 6, 8, 3},
            15, FFDSounds.NETHERITE_ARMOR_EQUIP, 3.0F);

    public static final Item SWEET_BERRIES = new ItemSweetBerries()
            .setRegistryName(FarmerFutureDelight.MODID, "sweet_berries")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".sweet_berries")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item SUSPICIOUS_STEW = new ItemSuspiciousStew()
            .setRegistryName(FarmerFutureDelight.MODID, "suspicious_stew")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".suspicious_stew");
    public static final Item LEATHER_HORSE_ARMOR = new ItemLeatherHorseArmor()
            .setRegistryName(FarmerFutureDelight.MODID, "leather_horse_armor")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".leather_horse_armor");
    public static final Item ANCIENT_DEBRIS = netheriteBlockItem(FFDBlocks.ANCIENT_DEBRIS);
    public static final Item NETHERITE_BLOCK = netheriteBlockItem(FFDBlocks.NETHERITE_BLOCK);
    public static final Item NETHERITE_SCRAP = netheriteComponent("netherite_scrap");
    public static final Item NETHERITE_INGOT = netheriteComponent("netherite_ingot");
    public static final Item NETHERITE_UPGRADE_SMITHING_TEMPLATE =
            netheriteComponent("netherite_upgrade_smithing_template");
    public static final Item NETHERITE_SWORD = netheriteGear(
            new ItemNetheriteSword(NETHERITE_TOOL_MATERIAL), "netherite_sword");
    public static final Item NETHERITE_PICKAXE = netheriteGear(
            new ItemNetheritePickaxe(NETHERITE_TOOL_MATERIAL), "netherite_pickaxe");
    public static final Item NETHERITE_AXE = netheriteGear(
            new ItemNetheriteAxe(NETHERITE_TOOL_MATERIAL), "netherite_axe");
    public static final Item NETHERITE_SHOVEL = netheriteGear(
            new ItemNetheriteShovel(NETHERITE_TOOL_MATERIAL), "netherite_shovel");
    public static final Item NETHERITE_HOE = netheriteGear(
            new ItemNetheriteHoe(NETHERITE_TOOL_MATERIAL), "netherite_hoe");
    public static final Item NETHERITE_HELMET = netheriteArmor(
            net.minecraft.inventory.EntityEquipmentSlot.HEAD, "netherite_helmet");
    public static final Item NETHERITE_CHESTPLATE = netheriteArmor(
            net.minecraft.inventory.EntityEquipmentSlot.CHEST, "netherite_chestplate");
    public static final Item NETHERITE_LEGGINGS = netheriteArmor(
            net.minecraft.inventory.EntityEquipmentSlot.LEGS, "netherite_leggings");
    public static final Item NETHERITE_BOOTS = netheriteArmor(
            net.minecraft.inventory.EntityEquipmentSlot.FEET, "netherite_boots");
    public static final Item NETHERITE_HORSE_ARMOR = netheriteGear(
            new ItemNetheriteHorseArmor(), "netherite_horse_armor");
    public static final Item[] NETHERITE_ITEMS = {
            ANCIENT_DEBRIS, NETHERITE_BLOCK, NETHERITE_SCRAP, NETHERITE_INGOT,
            NETHERITE_UPGRADE_SMITHING_TEMPLATE, NETHERITE_SWORD, NETHERITE_PICKAXE,
            NETHERITE_AXE, NETHERITE_SHOVEL, NETHERITE_HOE, NETHERITE_HELMET,
            NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS,
            NETHERITE_HORSE_ARMOR
    };

    static {
        NETHERITE_TOOL_MATERIAL.setRepairItem(new ItemStack(NETHERITE_INGOT));
        NETHERITE_ARMOR_MATERIAL.setRepairItem(new ItemStack(NETHERITE_INGOT));
    }
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
    public static final Item MUSIC_DISC_PIGSTEP = new ItemMusicDisc("pigstep", FFDSounds.MUSIC_DISC_PIGSTEP);
    public static final Item AMETHYST_BLOCK = blockItem(FFDBlocks.AMETHYST_BLOCK);
    public static final Item BUDDING_AMETHYST = blockItem(FFDBlocks.BUDDING_AMETHYST);
    public static final Item SMALL_AMETHYST_BUD = blockItem(FFDBlocks.SMALL_AMETHYST_BUD);
    public static final Item MEDIUM_AMETHYST_BUD = blockItem(FFDBlocks.MEDIUM_AMETHYST_BUD);
    public static final Item LARGE_AMETHYST_BUD = blockItem(FFDBlocks.LARGE_AMETHYST_BUD);
    public static final Item AMETHYST_CLUSTER = blockItem(FFDBlocks.AMETHYST_CLUSTER);
    public static final Item CALCITE = blockItem(FFDBlocks.CALCITE);
    public static final Item BASALT = blockItem(FFDBlocks.BASALT);
    public static final Item POLISHED_BASALT = blockItem(FFDBlocks.POLISHED_BASALT);
    public static final Item NETHER_GOLD_ORE = blockItem(FFDBlocks.NETHER_GOLD_ORE);
    public static final Item SMOOTH_BASALT = blockItem(FFDBlocks.SMOOTH_BASALT);
    public static final Item TINTED_GLASS = blockItem(FFDBlocks.TINTED_GLASS);
    public static final Item DRIPSTONE_BLOCK = blockItem(FFDBlocks.DRIPSTONE_BLOCK);
    public static final Item POINTED_DRIPSTONE = blockItem(FFDBlocks.POINTED_DRIPSTONE);
    public static final Item IRON_CHAIN = blockItem(FFDBlocks.IRON_CHAIN);
    public static final Item LANTERN = blockItem(FFDBlocks.LANTERN);
    public static final Item SMOKER = blockItem(FFDBlocks.SMOKER);
    public static final Item BLAST_FURNACE = blockItem(FFDBlocks.BLAST_FURNACE);
    public static final Item BARREL = blockItem(FFDBlocks.BARREL);
    public static final Item CAMPFIRE = blockItem(FFDBlocks.CAMPFIRE);
    public static final Item SOUL_SOIL = blockItem(FFDBlocks.SOUL_SOIL);
    public static final Item SOUL_TORCH = blockItem(FFDBlocks.SOUL_TORCH);
    public static final Item SOUL_LANTERN = blockItem(FFDBlocks.SOUL_LANTERN);
    public static final Item SOUL_CAMPFIRE = blockItem(FFDBlocks.SOUL_CAMPFIRE);
    public static final Item COMPOSTER = blockItem(FFDBlocks.COMPOSTER);
    public static final Item STONECUTTER = blockItem(FFDBlocks.STONECUTTER);
    public static final Item GRINDSTONE = blockItem(FFDBlocks.GRINDSTONE);
    public static final Item CARTOGRAPHY_TABLE = blockItem(FFDBlocks.CARTOGRAPHY_TABLE);
    public static final Item LOOM = blockItem(FFDBlocks.LOOM);
    public static final Item LECTERN = blockItem(FFDBlocks.LECTERN);
    public static final Item FLETCHING_TABLE = blockItem(FFDBlocks.FLETCHING_TABLE);
    public static final Item SMITHING_TABLE = blockItem(FFDBlocks.SMITHING_TABLE);
    public static final Item BELL = blockItem(FFDBlocks.BELL);
    public static final Item JIGSAW = blockItem(FFDBlocks.JIGSAW);
    public static final Item FLOWER_BANNER_PATTERN = bannerPatternItem("flower_banner_pattern", BannerPattern.FLOWER);
    public static final Item CREEPER_BANNER_PATTERN = bannerPatternItem("creeper_banner_pattern", BannerPattern.CREEPER);
    public static final Item SKULL_BANNER_PATTERN = bannerPatternItem("skull_banner_pattern", BannerPattern.SKULL);
    public static final Item MOJANG_BANNER_PATTERN = bannerPatternItem("mojang_banner_pattern", BannerPattern.MOJANG);
    public static final Item GLOBE_BANNER_PATTERN = bannerPatternItem("globe_banner_pattern", FFDBannerPatterns.GLOBE);
    public static final Item PIGLIN_BANNER_PATTERN = bannerPatternItem("piglin_banner_pattern", FFDBannerPatterns.PIGLIN);
    public static final Item FIELD_MASONED_BANNER_PATTERN = bannerPatternItem("field_masoned_banner_pattern", BannerPattern.BRICKS);
    public static final Item BORDURE_INDENTED_BANNER_PATTERN = bannerPatternItem("bordure_indented_banner_pattern", BannerPattern.CURLY_BORDER);
    public static final Item CORNFLOWER = flowerItem(FFDBlocks.CORNFLOWER, FFDBlocks.POTTED_CORNFLOWER);
    public static final Item LILY_OF_THE_VALLEY = flowerItem(FFDBlocks.LILY_OF_THE_VALLEY, FFDBlocks.POTTED_LILY_OF_THE_VALLEY);
    public static final Item WITHER_ROSE = flowerItem(FFDBlocks.WITHER_ROSE, FFDBlocks.POTTED_WITHER_ROSE);
    public static final Item GOLDEN_DANDELION = flowerItem(FFDBlocks.GOLDEN_DANDELION, FFDBlocks.POTTED_GOLDEN_DANDELION);
    public static final Item WHITE_DYE = dyeItem("white_dye", EnumDyeColor.WHITE);
    public static final Item BLACK_DYE = dyeItem("black_dye", EnumDyeColor.BLACK);
    public static final Item BLUE_DYE = dyeItem("blue_dye", EnumDyeColor.BLUE);
    public static final Item BROWN_DYE = dyeItem("brown_dye", EnumDyeColor.BROWN);
    public static final Item SCAFFOLDING = new ItemScaffolding(FFDBlocks.SCAFFOLDING)
            .setRegistryName(FFDBlocks.SCAFFOLDING.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".scaffolding")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item BAMBOO = new ItemNetherPlant(FFDBlocks.BAMBOO, FFDBlocks.POTTED_BAMBOO) {
        @Override
        public int getItemBurnTime(ItemStack stack) {
            return 50;
        }
    }.setRegistryName(FFDBlocks.BAMBOO.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".bamboo")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
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
    public static final Item BLACKSTONE = blockItem(FFDBlocks.BLACKSTONE);
    public static final Item POLISHED_BLACKSTONE = blockItem(FFDBlocks.POLISHED_BLACKSTONE);
    public static final Item POLISHED_BLACKSTONE_BRICKS = blockItem(FFDBlocks.POLISHED_BLACKSTONE_BRICKS);
    public static final Item CRACKED_POLISHED_BLACKSTONE_BRICKS = blockItem(FFDBlocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
    public static final Item CHISELED_POLISHED_BLACKSTONE = blockItem(FFDBlocks.CHISELED_POLISHED_BLACKSTONE);
    public static final Item GILDED_BLACKSTONE = blockItem(FFDBlocks.GILDED_BLACKSTONE);
    public static final Item BLACKSTONE_STAIRS = blockItem(FFDBlocks.BLACKSTONE_STAIRS);
    public static final Item POLISHED_BLACKSTONE_STAIRS = blockItem(FFDBlocks.POLISHED_BLACKSTONE_STAIRS);
    public static final Item POLISHED_BLACKSTONE_BRICK_STAIRS = blockItem(FFDBlocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
    public static final Item BLACKSTONE_WALL = blockItem(FFDBlocks.BLACKSTONE_WALL);
    public static final Item POLISHED_BLACKSTONE_WALL = blockItem(FFDBlocks.POLISHED_BLACKSTONE_WALL);
    public static final Item POLISHED_BLACKSTONE_BRICK_WALL = blockItem(FFDBlocks.POLISHED_BLACKSTONE_BRICK_WALL);
    public static final Item POLISHED_BLACKSTONE_BUTTON = blockItem(FFDBlocks.POLISHED_BLACKSTONE_BUTTON);
    public static final Item POLISHED_BLACKSTONE_PRESSURE_PLATE = blockItem(FFDBlocks.POLISHED_BLACKSTONE_PRESSURE_PLATE);
    public static final Item BLACKSTONE_SLAB = slabItem(
            FFDBlocks.BLACKSTONE_SLAB, FFDBlocks.BLACKSTONE_DOUBLE_SLAB);
    public static final Item POLISHED_BLACKSTONE_SLAB = slabItem(
            FFDBlocks.POLISHED_BLACKSTONE_SLAB, FFDBlocks.POLISHED_BLACKSTONE_DOUBLE_SLAB);
    public static final Item POLISHED_BLACKSTONE_BRICK_SLAB = slabItem(
            FFDBlocks.POLISHED_BLACKSTONE_BRICK_SLAB, FFDBlocks.POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB);
    public static final Item QUARTZ_BRICKS = blockItem(FFDBlocks.QUARTZ_BRICKS);
    public static final Item CRYING_OBSIDIAN = blockItem(FFDBlocks.CRYING_OBSIDIAN);
    public static final Item TARGET = blockItem(FFDBlocks.TARGET);
    public static final Item LODESTONE = blockItem(FFDBlocks.LODESTONE);
    public static final Item RESPAWN_ANCHOR = blockItem(FFDBlocks.RESPAWN_ANCHOR);
    public static final Item CHISELED_NETHER_BRICKS = blockItem(FFDBlocks.CHISELED_NETHER_BRICKS);
    public static final Item CRACKED_NETHER_BRICKS = blockItem(FFDBlocks.CRACKED_NETHER_BRICKS);
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
    public static final Item HONEYCOMB_BLOCK = blockItem(FFDBlocks.HONEYCOMB_BLOCK);
    public static final Item BEE_NEST = blockItem(FFDBlocks.BEE_NEST);
    public static final Item BEEHIVE = blockItem(FFDBlocks.BEEHIVE);
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
    public static final Item[] CORAL_BLOCK_ITEMS = blockItems(FFDBlocks.CORAL_BLOCKS);
    public static final Item[] DEAD_CORAL_BLOCK_ITEMS = blockItems(FFDBlocks.DEAD_CORAL_BLOCKS);
    public static final Item[] CORAL_ITEMS = blockItems(FFDBlocks.CORALS);
    public static final Item[] DEAD_CORAL_ITEMS = blockItems(FFDBlocks.DEAD_CORALS);
    public static final Item[] CORAL_FAN_ITEMS = coralFanItems(
            FFDBlocks.CORAL_FANS, FFDBlocks.CORAL_WALL_FANS);
    public static final Item[] DEAD_CORAL_FAN_ITEMS = coralFanItems(
            FFDBlocks.DEAD_CORAL_FANS, FFDBlocks.DEAD_CORAL_WALL_FANS);
    public static final Item BLUE_ICE = blockItem(FFDBlocks.BLUE_ICE);
    public static final Item[] WOOD_ITEMS = blockItems(FFDBlocks.WOODS);
    public static final Item[] STRIPPED_LOG_ITEMS = blockItems(FFDBlocks.STRIPPED_LOGS);
    public static final Item[] STRIPPED_WOOD_ITEMS = blockItems(FFDBlocks.STRIPPED_WOODS);
    public static final Item[] OVERWORLD_TRAPDOOR_ITEMS =
            blockItems(FFDBlocks.OVERWORLD_TRAPDOORS);
    public static final Item[] OVERWORLD_BUTTON_ITEMS =
            blockItems(FFDBlocks.OVERWORLD_BUTTONS);
    public static final Item[] OVERWORLD_PRESSURE_PLATE_ITEMS =
            blockItems(FFDBlocks.OVERWORLD_PRESSURE_PLATES);
    public static final Item[] WOOD_SIGN_ITEMS = woodSignItems();
    public static final Item CRIMSON_SIGN = woodSignItem(FFDBlocks.CRIMSON_SIGN, FFDBlocks.CRIMSON_WALL_SIGN);
    public static final Item WARPED_SIGN = woodSignItem(FFDBlocks.WARPED_SIGN, FFDBlocks.WARPED_WALL_SIGN);
    public static final Item PUMPKIN = blockItem(FFDBlocks.PUMPKIN);
    public static final Item[] PRISMARINE_STAIR_ITEMS = blockItems(FFDBlocks.PRISMARINE_STAIRS);
    public static final Item[] PRISMARINE_SLAB_ITEMS = slabItems(
            FFDBlocks.PRISMARINE_SLABS, FFDBlocks.PRISMARINE_DOUBLE_SLABS);
    public static final Item NAUTILUS_SHELL = simpleItem("nautilus_shell");
    public static final Item HEART_OF_THE_SEA = new ItemHeartOfTheSea()
            .setRegistryName(FarmerFutureDelight.MODID, "heart_of_the_sea")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".heart_of_the_sea")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item TRIDENT = new ItemTrident()
            .setRegistryName(FarmerFutureDelight.MODID, "trident")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".trident")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item WARPED_FUNGUS_ON_A_STICK = new xy177.farmersfuturedelight.common.item.ItemWarpedFungusOnAStick()
            .setRegistryName(FarmerFutureDelight.MODID, "warped_fungus_on_a_stick")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".warped_fungus_on_a_stick")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item CROSSBOW = new ItemCrossbow()
            .setRegistryName(FarmerFutureDelight.MODID, "crossbow")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".crossbow")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item CONDUIT = blockItem(FFDBlocks.CONDUIT);
    public static final Item COD_BUCKET = fishBucket("cod");
    public static final Item SALMON_BUCKET = fishBucket("salmon");
    public static final Item PUFFERFISH_BUCKET = fishBucket("pufferfish");
    public static final Item TROPICAL_FISH_BUCKET = fishBucket("tropical_fish");
    public static final Item[] FISH_BUCKETS = {
            COD_BUCKET, SALMON_BUCKET, PUFFERFISH_BUCKET, TROPICAL_FISH_BUCKET
    };
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

    public static boolean isCoralEnabled() {
        return FFDCompat.isEnabled(FFDConfig.coralMode, FFDCompat.Feature.CORAL);
    }

    public static boolean isBlueIceEnabled() {
        return FFDCompat.isEnabled(FFDConfig.blueIceMode, FFDCompat.Feature.AQUATIC_DECOR);
    }

    public static boolean isStrippedWoodEnabled() {
        return FFDCompat.isEnabled(FFDConfig.strippedWoodMode,
                FFDCompat.Feature.AQUATIC_DECOR);
    }

    public static boolean isPumpkinEnabled() {
        return FFDCompat.isEnabled(FFDConfig.pumpkinMode,
                FFDCompat.Feature.AQUATIC_DECOR);
    }

    public static boolean isPrismarineDecorEnabled() {
        return FFDCompat.isEnabled(FFDConfig.prismarineDecorMode,
                FFDCompat.Feature.AQUATIC_DECOR);
    }

    public static boolean isConduitEnabled() {
        return FFDCompat.isEnabled(FFDConfig.conduitMode, FFDCompat.Feature.AQUATIC_DECOR);
    }

    public static boolean isBubbleColumnEnabled() {
        return FFDCompat.isEnabled(FFDConfig.bubbleColumnMode, FFDCompat.Feature.AQUATIC_DECOR);
    }

    public static boolean isFishEnabled() {
        return FFDCompat.isEnabled(FFDConfig.fishMode, FFDCompat.Feature.FISH);
    }

    public static boolean isTridentEnabled() {
        return FFDCompat.isEnabled(FFDConfig.tridentMode, FFDCompat.Feature.TRIDENT);
    }

    public static boolean isLocalCrossbowEnabled() {
        return FFDCompat.isLocalItemEnabled(FFDConfig.crossbowMode,
                FFDCompat.Feature.CROSSBOW, CROSSBOW, null, "crossbow");
    }

    public static boolean isCrossbowItem(Item item) {
        if (item == null || item.getRegistryName() == null) {
            return false;
        }
        net.minecraft.util.ResourceLocation id = item.getRegistryName();
        return item == CROSSBOW || "futuremc".equals(id.getResourceDomain())
                && "crossbow".equals(id.getResourcePath());
    }

    public static boolean isTridentStack(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return false;
        }
        return isTridentItem(stack.getItem());
    }

    public static boolean isTridentItem(Item item) {
        if (item == null || item.getRegistryName() == null) {
            return false;
        }
        net.minecraft.util.ResourceLocation id = item.getRegistryName();
        return item == TRIDENT
                || ("futuremc".equals(id.getResourceDomain()) && "trident".equals(id.getResourcePath()))
                || ("oe".equals(id.getResourceDomain()) && "trident".equals(id.getResourcePath()));
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

    public static boolean isNetheriteEnabled() {
        return FFDCompat.isEnabled(FFDConfig.netheriteMode, FFDCompat.Feature.NETHERITE);
    }

    public static boolean isBlackstoneEnabled() {
        return FFDCompat.isEnabled(FFDConfig.blackstoneMode, FFDCompat.Feature.BLACKSTONE);
    }

    public static boolean isQuartzBricksEnabled() {
        return FFDCompat.isEnabled(FFDConfig.quartzBricksMode, FFDCompat.Feature.QUARTZ_BRICKS);
    }

    public static boolean isCryingObsidianEnabled() {
        return FFDCompat.isEnabled(FFDConfig.cryingObsidianMode,
                FFDCompat.Feature.CRYING_OBSIDIAN);
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

    public static boolean isLanternEnabled() {
        return FFDCompat.isEnabled(FFDConfig.lanternMode, FFDCompat.Feature.LANTERN);
    }

    private static Item flowerItem(Block flower, Block pot) {
        Item item = new ItemNetherPlant(flower, pot).setRegistryName(flower.getRegistryName())
                .setUnlocalizedName(flower.getUnlocalizedName().substring(5))
                .setCreativeTab(FFDCreativeTab.INSTANCE);
        LOCAL_ITEM_BLOCKS.put(item, flower);
        LOCAL_BLOCK_ITEMS_BY_NAME.put(item.getRegistryName(), item);
        return item;
    }

    public static boolean isFlowersEnabled() {
        return FFDCompat.isEnabled(FFDConfig.flowersMode, FFDCompat.Feature.FLOWERS);
    }

    public static boolean isBambooEnabled() {
        return FFDCompat.isEnabled(FFDConfig.bambooMode, FFDCompat.Feature.BAMBOO);
    }

    public static boolean isScaffoldingEnabled() {
        return FFDCompat.isEnabled(FFDConfig.scaffoldingMode, FFDCompat.Feature.SCAFFOLDING);
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
        if (block.getRegistryName() != null) LOCAL_BLOCKS_BY_NAME.put(block.getRegistryName(), block);
        if (block == FFDBlocks.BAMBOO_SAPLING && !shouldRegisterBlock(FFDBlocks.BAMBOO)) {
            return false;
        }
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
        if (stack.getItem() == POWDER_SNOW) {
            return false;
        }
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

    public static Item localBlockItem(ResourceLocation name) {
        return LOCAL_BLOCK_ITEMS_BY_NAME.get(name);
    }

    public static Block localBlock(ResourceLocation name) {
        Block block = LOCAL_BLOCKS_BY_NAME.get(name);
        if (block != null) return block;
        Item item = localBlockItem(name);
        block = LOCAL_ITEM_BLOCKS.get(item);
        if (block != null) return block;
        for (Block candidate : FFDStoneDecor.blocks()) {
            if (name.equals(candidate.getRegistryName())) return candidate;
        }
        for (Block candidate : com.google.common.collect.Iterables.concat(
                java.util.Arrays.asList(FFDBlocks.WOOD_WALL_SIGNS),
                java.util.Arrays.asList(FFDBlocks.CRIMSON_WALL_SIGN, FFDBlocks.WARPED_WALL_SIGN))) {
            if (name.equals(candidate.getRegistryName())) return candidate;
        }
        return null;
    }

    public static Block effectiveBlock(Block local) {
        IBlockState state = effectiveBlockState(local);
        return state == null ? null : state.getBlock();
    }

    public static IBlockState effectiveBlockState(Block local) {
        String rawOreMaterial = rawOreMaterial(local);
        if (rawOreMaterial != null && !FFDConfig.isRawOreMaterialEnabled(rawOreMaterial)) {
            return null;
        }
        if (isBlockRegistered(local)) {
            return local.getDefaultState();
        }
        FeatureBinding binding = blockBinding(local);
        if (binding == null || binding.mode != FFDConfig.FeatureMode.AUTO
                || local.getRegistryName() == null) {
            return null;
        }
        return FFDCompat.getExternalBlockState(binding.feature, contentPaths(local));
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
        if (block == FFDBlocks.SOUL_CAMPFIRE) {
            return binding(FFDConfig.soulCampfireMode, FFDCompat.Feature.SOUL_FIRE);
        }
        if (oneOf(block, FFDBlocks.SOUL_SOIL, FFDBlocks.SOUL_FIRE, FFDBlocks.SOUL_TORCH,
                FFDBlocks.SOUL_LANTERN)) {
            return binding(FFDConfig.soulFireMode, FFDCompat.Feature.SOUL_FIRE);
        }
        if (block == FFDBlocks.ANCIENT_DEBRIS || block == FFDBlocks.NETHERITE_BLOCK) {
            return binding(FFDConfig.netheriteMode, FFDCompat.Feature.NETHERITE);
        }
        if (block == FFDBlocks.CAMPFIRE) {
            return binding(FFDConfig.campfireMode, FFDCompat.Feature.CAMPFIRE);
        }
        if (block == FFDBlocks.SMOKER) {
            return binding(FFDConfig.smokerMode, FFDCompat.Feature.SMOKER);
        }
        if (block == FFDBlocks.BLAST_FURNACE) {
            return binding(FFDConfig.blastFurnaceMode, FFDCompat.Feature.BLAST_FURNACE);
        }
        if (block == FFDBlocks.COMPOSTER) {
            return binding(FFDConfig.composterMode, FFDCompat.Feature.COMPOSTER);
        }
        if (block == FFDBlocks.BARREL) {
            return binding(FFDConfig.barrelMode, FFDCompat.Feature.BARREL);
        }
        if (block == FFDBlocks.STONECUTTER) {
            return binding(FFDConfig.stonecutterMode, FFDCompat.Feature.STONECUTTER);
        }
        if (block == FFDBlocks.GRINDSTONE) {
            return binding(FFDConfig.grindstoneMode, FFDCompat.Feature.GRINDSTONE);
        }
        if (block == FFDBlocks.CARTOGRAPHY_TABLE) {
            return binding(FFDConfig.cartographyTableMode, FFDCompat.Feature.CARTOGRAPHY_TABLE);
        }
        if (block == FFDBlocks.LECTERN) {
            return binding(FFDConfig.lecternMode, FFDCompat.Feature.LECTERN);
        }
        if (block == FFDBlocks.FLETCHING_TABLE) {
            return binding(FFDConfig.fletchingTableMode, FFDCompat.Feature.FLETCHING_TABLE);
        }
        if (block == FFDBlocks.SMITHING_TABLE) {
            return binding(FFDConfig.smithingTableMode, FFDCompat.Feature.SMITHING_TABLE);
        }
        if (block == FFDBlocks.BELL) {
            return binding(FFDConfig.bellMode, FFDCompat.Feature.BELL);
        }
        if (block == FFDBlocks.LOOM) {
            return binding(FFDConfig.loomMode, FFDCompat.Feature.LOOM);
        }
        if (FFDStoneDecor.contains(block)) {
            return binding(FFDConfig.stoneDecorMode, FFDCompat.Feature.STONE_DECOR);
        }
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
        if (block == FFDBlocks.LANTERN) {
            return binding(FFDConfig.lanternMode, FFDCompat.Feature.LANTERN);
        }
        if (oneOf(block, FFDBlocks.CORNFLOWER, FFDBlocks.LILY_OF_THE_VALLEY, FFDBlocks.WITHER_ROSE,
                FFDBlocks.POTTED_CORNFLOWER, FFDBlocks.POTTED_LILY_OF_THE_VALLEY, FFDBlocks.POTTED_WITHER_ROSE,
                FFDBlocks.GOLDEN_DANDELION, FFDBlocks.POTTED_GOLDEN_DANDELION)) {
            return binding(FFDConfig.flowersMode, FFDCompat.Feature.FLOWERS);
        }
        if (oneOf(block, FFDBlocks.BAMBOO, FFDBlocks.BAMBOO_SAPLING, FFDBlocks.POTTED_BAMBOO)) {
            return binding(FFDConfig.bambooMode, FFDCompat.Feature.BAMBOO);
        }
        if (block == FFDBlocks.SCAFFOLDING) {
            return binding(FFDConfig.scaffoldingMode, FFDCompat.Feature.SCAFFOLDING);
        }
        if (arrayContains(block, FFDBlocks.CANDLES)
                || arrayContains(block, FFDBlocks.CANDLE_CAKES)) {
            return binding(FFDConfig.candleMode, FFDCompat.Feature.CANDLE);
        }
        if (block == FFDBlocks.POWDER_SNOW) {
            return binding(FFDConfig.powderSnowMode, FFDCompat.Feature.POWDER_SNOW);
        }
        if (block != null && block.getRegistryName() != null
                && FarmerFutureDelight.MODID.equals(block.getRegistryName().getResourceDomain())
                && block.getRegistryName().getResourcePath().contains("blackstone")) {
            return binding(FFDConfig.blackstoneMode, FFDCompat.Feature.BLACKSTONE);
        }
        if (block == FFDBlocks.BASALT || block == FFDBlocks.POLISHED_BASALT) {
            return binding(FFDConfig.basaltMode, FFDCompat.Feature.BASALT);
        }
        if (block == FFDBlocks.NETHER_GOLD_ORE) {
            return binding(FFDConfig.netherGoldOreMode, FFDCompat.Feature.NETHER_GOLD_ORE);
        }
        if (block == FFDBlocks.QUARTZ_BRICKS) {
            return binding(FFDConfig.quartzBricksMode, FFDCompat.Feature.QUARTZ_BRICKS);
        }
        if (block == FFDBlocks.CRYING_OBSIDIAN) {
            return binding(FFDConfig.cryingObsidianMode, FFDCompat.Feature.CRYING_OBSIDIAN);
        }
        if (block == FFDBlocks.CHISELED_NETHER_BRICKS || block == FFDBlocks.CRACKED_NETHER_BRICKS) {
            return binding(FFDConfig.netherBricksMode, FFDCompat.Feature.NETHER_COMPLETION);
        }
        if (block == FFDBlocks.TARGET) return binding(FFDConfig.targetMode, FFDCompat.Feature.NETHER_DEVICES);
        if (block == FFDBlocks.LODESTONE) return binding(FFDConfig.lodestoneMode, FFDCompat.Feature.NETHER_DEVICES);
        if (block == FFDBlocks.RESPAWN_ANCHOR) return binding(FFDConfig.respawnAnchorMode, FFDCompat.Feature.NETHER_DEVICES);
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
        if (arrayContains(block, FFDBlocks.CORAL_BLOCKS)
                || arrayContains(block, FFDBlocks.DEAD_CORAL_BLOCKS)
                || arrayContains(block, FFDBlocks.CORALS)
                || arrayContains(block, FFDBlocks.DEAD_CORALS)
                || arrayContains(block, FFDBlocks.CORAL_FANS)
                || arrayContains(block, FFDBlocks.DEAD_CORAL_FANS)
                || arrayContains(block, FFDBlocks.CORAL_WALL_FANS)
                || arrayContains(block, FFDBlocks.DEAD_CORAL_WALL_FANS)) {
            return binding(FFDConfig.coralMode, FFDCompat.Feature.CORAL);
        }
        if (block == FFDBlocks.BLUE_ICE) {
            return binding(FFDConfig.blueIceMode, FFDCompat.Feature.AQUATIC_DECOR);
        }
        if (block == FFDBlocks.CONDUIT) {
            return binding(FFDConfig.conduitMode, FFDCompat.Feature.AQUATIC_DECOR);
        }
        if (block == FFDBlocks.BUBBLE_COLUMN) {
            return binding(FFDConfig.bubbleColumnMode, FFDCompat.Feature.AQUATIC_DECOR);
        }
        if (arrayContains(block, FFDBlocks.WOODS)
                || arrayContains(block, FFDBlocks.STRIPPED_LOGS)
                || arrayContains(block, FFDBlocks.STRIPPED_WOODS)
                || arrayContains(block, FFDBlocks.OVERWORLD_TRAPDOORS)
                || arrayContains(block, FFDBlocks.OVERWORLD_TRAPDOORS_WATERLOGGED)
                || arrayContains(block, FFDBlocks.OVERWORLD_BUTTONS)
                || arrayContains(block, FFDBlocks.OVERWORLD_PRESSURE_PLATES)
                || arrayContains(block, FFDBlocks.WOOD_STANDING_SIGNS)
                || arrayContains(block, FFDBlocks.WOOD_WALL_SIGNS)) {
            return binding(FFDConfig.strippedWoodMode, FFDCompat.Feature.AQUATIC_DECOR);
        }
        if (block == FFDBlocks.PUMPKIN) {
            return binding(FFDConfig.pumpkinMode, FFDCompat.Feature.AQUATIC_DECOR);
        }
        if (arrayContains(block, FFDBlocks.PRISMARINE_STAIRS)
                || arrayContains(block, FFDBlocks.PRISMARINE_SLABS)
                || arrayContains(block, FFDBlocks.PRISMARINE_DOUBLE_SLABS)) {
            return binding(FFDConfig.prismarineDecorMode, FFDCompat.Feature.AQUATIC_DECOR);
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
        if (item == WARPED_FUNGUS_ON_A_STICK) return binding(FFDConfig.striderMode, FFDCompat.Feature.STRIDER);
        if (arrayContains(item, NETHERITE_ITEMS)) {
            return binding(FFDConfig.netheriteMode, FFDCompat.Feature.NETHERITE);
        }
        if (item == FLOWER_BANNER_PATTERN || item == CREEPER_BANNER_PATTERN
                || item == SKULL_BANNER_PATTERN || item == MOJANG_BANNER_PATTERN
                || item == GLOBE_BANNER_PATTERN || item == FIELD_MASONED_BANNER_PATTERN
                || item == BORDURE_INDENTED_BANNER_PATTERN || item == PIGLIN_BANNER_PATTERN) {
            return binding(FFDConfig.loomMode, FFDCompat.Feature.LOOM);
        }
        if (item == LEATHER_HORSE_ARMOR) {
            return binding(FFDConfig.leatherHorseArmorMode, FFDCompat.Feature.LEATHER_HORSE_ARMOR);
        }
        if (item == SUSPICIOUS_STEW) {
            return binding(FFDConfig.suspiciousStewMode, FFDCompat.Feature.SUSPICIOUS_STEW);
        }
        if (item == BAMBOO) {
            return binding(FFDConfig.bambooMode, FFDCompat.Feature.BAMBOO);
        }
        if (item == SCAFFOLDING) {
            return binding(FFDConfig.scaffoldingMode, FFDCompat.Feature.SCAFFOLDING);
        }
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
        if (item == NAUTILUS_SHELL || item == HEART_OF_THE_SEA) {
            return binding(FFDConfig.conduitMode, FFDCompat.Feature.AQUATIC_DECOR);
        }
        if (item == TRIDENT) {
            return binding(FFDConfig.tridentMode, FFDCompat.Feature.TRIDENT);
        }
        if (item == CROSSBOW) {
            return binding(FFDConfig.crossbowMode, FFDCompat.Feature.CROSSBOW);
        }
        if (arrayContains(item, FISH_BUCKETS)) {
            return binding(FFDConfig.fishMode, FFDCompat.Feature.FISH);
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
        if (item == MUSIC_DISC_PIGSTEP) {
            return binding(FFDConfig.pigstepMode, FFDCompat.Feature.NETHER_COMPLETION);
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
                FFDBlocks.CRIMSON_PRESSURE_PLATE, FFDBlocks.CRIMSON_SIGN, FFDBlocks.CRIMSON_WALL_SIGN);
    }

    private static boolean isWarpedWoodBlock(Block block) {
        return oneOf(block, FFDBlocks.WARPED_STEM, FFDBlocks.STRIPPED_WARPED_STEM,
                FFDBlocks.WARPED_HYPHAE, FFDBlocks.STRIPPED_WARPED_HYPHAE,
                FFDBlocks.WARPED_WART_BLOCK, FFDBlocks.WARPED_PLANKS,
                FFDBlocks.WARPED_STAIRS, FFDBlocks.WARPED_SLAB,
                FFDBlocks.WARPED_DOUBLE_SLAB, FFDBlocks.WARPED_FENCE,
                FFDBlocks.WARPED_FENCE_GATE, FFDBlocks.WARPED_DOOR,
                FFDBlocks.WARPED_TRAPDOOR, FFDBlocks.WARPED_BUTTON,
                FFDBlocks.WARPED_PRESSURE_PLATE, FFDBlocks.WARPED_SIGN, FFDBlocks.WARPED_WALL_SIGN);
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

    static ItemBlock blockItem(Block block) {
        ItemBlock item = new ItemBlock(block) {
            @Override
            public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world,
                                        BlockPos pos, EnumFacing side, float hitX,
                                        float hitY, float hitZ, IBlockState state) {
                if (block == FFDBlocks.BELL && state == null) {
                    return false;
                }
                return super.placeBlockAt(stack, player, world, pos, side,
                        hitX, hitY, hitZ, state);
            }

            @Override
            public int getItemBurnTime(ItemStack stack) {
                if (arrayContains(block, FFDBlocks.OVERWORLD_BUTTONS)) {
                    return 100;
                }
                if (arrayContains(block, FFDBlocks.WOODS)
                        || arrayContains(block, FFDBlocks.STRIPPED_LOGS)
                        || arrayContains(block, FFDBlocks.STRIPPED_WOODS)
                        || arrayContains(block, FFDBlocks.OVERWORLD_TRAPDOORS)
                        || arrayContains(block, FFDBlocks.OVERWORLD_PRESSURE_PLATES)) {
                    return 300;
                }
                if (block == FFDBlocks.BARREL || block == FFDBlocks.COMPOSTER
                        || block == FFDBlocks.LOOM || block == FFDBlocks.LECTERN
                        || block == FFDBlocks.CARTOGRAPHY_TABLE || block == FFDBlocks.FLETCHING_TABLE
                        || block == FFDBlocks.SMITHING_TABLE) {
                    return 300;
                }
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

    private static Item netheriteComponent(String name) {
        return netheriteGear(new ItemNetheriteComponent(), name);
    }

    private static Item netheriteGear(Item item, String name) {
        item.setRegistryName(FarmerFutureDelight.MODID, name);
        item.setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        item.setCreativeTab(FFDCreativeTab.INSTANCE);
        return item;
    }

    private static Item netheriteArmor(net.minecraft.inventory.EntityEquipmentSlot slot,
                                       String name) {
        return netheriteGear(new ItemNetheriteArmor(NETHERITE_ARMOR_MATERIAL, slot), name);
    }

    private static Item netheriteBlockItem(Block block) {
        Item item = new ItemNetheriteBlock(block);
        item.setRegistryName(block.getRegistryName());
        item.setUnlocalizedName(FarmerFutureDelight.MODID + "."
                + block.getRegistryName().getResourcePath());
        return rememberBlockItem(item, block);
    }

    private static Item dyeItem(String name, EnumDyeColor color) {
        return new ItemModernDye(color)
                .setRegistryName(FarmerFutureDelight.MODID, name)
                .setUnlocalizedName(FarmerFutureDelight.MODID + "." + name)
                .setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    private static Item bannerPatternItem(String name, BannerPattern pattern) {
        return new ItemBannerPattern(pattern)
                .setRegistryName(FarmerFutureDelight.MODID, name)
                .setUnlocalizedName(FarmerFutureDelight.MODID + "." + name)
                .setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    private static Item fishBucket(String entityPath) {
        String name = entityPath + "_bucket";
        return new ItemFishBucket(entityPath)
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

    private static Item[] coralFanItems(
            xy177.farmersfuturedelight.common.block.BlockCoralPlant[] floorFans,
            xy177.farmersfuturedelight.common.block.BlockCoralWallFan[] wallFans) {
        Item[] items = new Item[floorFans.length];
        for (int i = 0; i < items.length; i++) {
            ItemCoralFan item = new ItemCoralFan(floorFans[i], wallFans[i]);
            item.setRegistryName(floorFans[i].getRegistryName());
            item.setUnlocalizedName(FarmerFutureDelight.MODID + "."
                    + floorFans[i].getRegistryName().getResourcePath());
            item.setCreativeTab(FFDCreativeTab.INSTANCE);
            items[i] = rememberBlockItem(item, floorFans[i]);
        }
        return items;
    }

    private static Item[] woodSignItems() {
        Item[] items = new Item[FFDBlocks.WOOD_SIGN_NAMES.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = woodSignItem(FFDBlocks.WOOD_STANDING_SIGNS[i], FFDBlocks.WOOD_WALL_SIGNS[i]);
        }
        return items;
    }

    private static Item woodSignItem(BlockWoodStandingSign standing, BlockWoodWallSign wall) {
        ItemWoodSign item = new ItemWoodSign(standing, wall);
        item.setRegistryName(standing.getRegistryName());
        item.setUnlocalizedName(FarmerFutureDelight.MODID + "." + standing.getWoodName() + "_sign");
        item.setCreativeTab(FFDCreativeTab.INSTANCE);
        return rememberBlockItem(item, standing);
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

    static ItemSlab slabItem(net.minecraft.block.BlockSlab slab, net.minecraft.block.BlockSlab doubleSlab) {
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

    public static final Item[] STONE_DECOR_ITEMS = FFDStoneDecor.createItems();

    private FFDItems() {
    }
}
