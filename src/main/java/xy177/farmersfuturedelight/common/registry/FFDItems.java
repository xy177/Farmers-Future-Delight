package xy177.farmersfuturedelight.common.registry;

import net.minecraft.block.Block;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.common.util.EnumHelper;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.item.ItemBigDripleaf;
import xy177.farmersfuturedelight.common.item.ItemBlockDriedKelp;
import xy177.farmersfuturedelight.common.item.ItemDriedKelp;
import xy177.farmersfuturedelight.common.item.ItemGlowBerries;
import xy177.farmersfuturedelight.common.item.ItemGlowInkSac;
import xy177.farmersfuturedelight.common.item.ItemHoneyBottle;
import xy177.farmersfuturedelight.common.item.ItemHangingRoots;
import xy177.farmersfuturedelight.common.item.ItemKelp;
import xy177.farmersfuturedelight.common.item.ItemNetherPlant;
import xy177.farmersfuturedelight.common.item.ItemSeagrass;
import xy177.farmersfuturedelight.common.item.ItemSeaPickle;
import xy177.farmersfuturedelight.common.item.ItemSmallDripleaf;
import xy177.farmersfuturedelight.common.item.ItemSweetBerries;
import xy177.farmersfuturedelight.common.item.ItemTurtleEgg;
import xy177.farmersfuturedelight.common.item.ItemTurtleHelmet;

public final class FFDItems {
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
    public static final Item AZALEA = new ItemBlock(FFDBlocks.AZALEA)
            .setRegistryName(FFDBlocks.AZALEA.getRegistryName())
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".azalea")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item FLOWERING_AZALEA = new ItemBlock(FFDBlocks.FLOWERING_AZALEA)
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
    public static final Item HONEY_BOTTLE = new ItemHoneyBottle()
            .setRegistryName(FarmerFutureDelight.MODID, "honey_bottle")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".honey_bottle")
            .setCreativeTab(FFDCreativeTab.INSTANCE);
    public static final Item HONEYCOMB = new Item()
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
    public static final Item WEEPING_VINES = blockItem(FFDBlocks.WEEPING_VINES);
    public static final Item TWISTING_VINES = blockItem(FFDBlocks.TWISTING_VINES);
    public static final Item CRIMSON_STEM = blockItem(FFDBlocks.CRIMSON_STEM);
    public static final Item STRIPPED_CRIMSON_STEM = blockItem(FFDBlocks.STRIPPED_CRIMSON_STEM);
    public static final Item CRIMSON_HYPHAE = blockItem(FFDBlocks.CRIMSON_HYPHAE);
    public static final Item STRIPPED_CRIMSON_HYPHAE = blockItem(FFDBlocks.STRIPPED_CRIMSON_HYPHAE);
    public static final Item WARPED_STEM = blockItem(FFDBlocks.WARPED_STEM);
    public static final Item STRIPPED_WARPED_STEM = blockItem(FFDBlocks.STRIPPED_WARPED_STEM);
    public static final Item WARPED_HYPHAE = blockItem(FFDBlocks.WARPED_HYPHAE);
    public static final Item STRIPPED_WARPED_HYPHAE = blockItem(FFDBlocks.STRIPPED_WARPED_HYPHAE);
    public static final Item NETHER_WART_BLOCK = blockItem(FFDBlocks.NETHER_WART_BLOCK);
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

    public static boolean isSweetBerryEnabled() {
        return FFDCompat.isEnabled(FFDConfig.sweetBerryMode,
                "futuremc", "brewinandchewinlegacy", "fruits_delight_legacy", "da");
    }

    public static boolean isMossEnabled() {
        return FFDCompat.isEnabled(FFDConfig.mossMode, "depthsupdate");
    }

    public static boolean isGlowBerryEnabled() {
        return FFDCompat.isEnabled(FFDConfig.glowBerryMode,
                "depthsupdate", "fruits_delight_legacy", "da", "brewinandchewinlegacy");
    }

    public static boolean isLushCaveEnabled() {
        return FFDCompat.isEnabled(FFDConfig.lushCaveMode, "depthsupdate");
    }

    public static boolean isAzaleaEnabled() {
        return FFDCompat.isEnabled(FFDConfig.azaleaMode, "depthsupdate");
    }

    public static boolean isDripleafEnabled() {
        return FFDCompat.isEnabled(FFDConfig.dripleafMode, "depthsupdate");
    }

    public static boolean isRootedDirtEnabled() {
        return FFDCompat.isEnabled(FFDConfig.rootedDirtMode, "depthsupdate");
    }

    public static boolean isHangingRootsEnabled() {
        return FFDCompat.isEnabled(FFDConfig.hangingRootsMode, "depthsupdate");
    }

    public static boolean isSporeBlossomEnabled() {
        return FFDCompat.isEnabled(FFDConfig.sporeBlossomMode, "depthsupdate");
    }

    public static boolean isGlowLichenEnabled() {
        return FFDCompat.isEnabled(FFDConfig.glowLichenMode, "depthsupdate");
    }

    public static boolean isHoneyEnabled() {
        return FFDCompat.isEnabled(FFDConfig.honeyMode, "futuremc");
    }

    public static boolean isKelpEnabled() {
        return FFDCompat.isEnabled(FFDConfig.kelpMode, "oe", "brewinandchewinlegacy");
    }

    public static boolean isSeagrassEnabled() {
        return FFDCompat.isEnabled(FFDConfig.seagrassMode,
                "oe", "futuremc", "brewinandchewinlegacy");
    }

    public static boolean isSeaPickleEnabled() {
        return FFDCompat.isEnabled(FFDConfig.seaPickleMode, "oe", "brewinandchewinlegacy");
    }

    public static boolean isTurtleEnabled() {
        return FFDCompat.isEnabled(FFDConfig.turtleMode, "oe", "brewinandchewinlegacy");
    }

    public static boolean isCrimsonEnabled() {
        return FFDCompat.isEnabled(FFDConfig.crimsonMode,
                "futuremc", "netherized", "nb", "brewinandchewinlegacy");
    }

    public static boolean isWarpedEnabled() {
        return FFDCompat.isEnabled(FFDConfig.warpedMode,
                "futuremc", "netherized", "nb", "brewinandchewinlegacy");
    }

    public static boolean isCrimsonWoodEnabled() {
        return FFDCompat.isEnabled(FFDConfig.crimsonWoodMode, "futuremc", "netherized", "nb");
    }

    public static boolean isWarpedWoodEnabled() {
        return FFDCompat.isEnabled(FFDConfig.warpedWoodMode, "futuremc", "netherized", "nb");
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
        return item;
    }

    private static ItemNetherPlant netherPlantItem(Block block, Block pottedBlock) {
        ItemNetherPlant item = new ItemNetherPlant(block, pottedBlock);
        item.setRegistryName(block.getRegistryName());
        item.setUnlocalizedName(FarmerFutureDelight.MODID + "."
                + block.getRegistryName().getResourcePath());
        item.setCreativeTab(FFDCreativeTab.INSTANCE);
        return item;
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
        return item;
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
        return item;
    }

    private FFDItems() {
    }
}
