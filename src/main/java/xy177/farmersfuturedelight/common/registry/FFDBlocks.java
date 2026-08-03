package xy177.farmersfuturedelight.common.registry;

import xy177.farmersfuturedelight.common.block.BlockAzalea;
import xy177.farmersfuturedelight.common.block.BlockAzaleaLeaves;
import xy177.farmersfuturedelight.common.block.BlockBeehive;
import xy177.farmersfuturedelight.common.block.BlockBigDripleaf;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.BlockCaveVines;
import xy177.farmersfuturedelight.common.block.BlockCaveVinesPlant;
import xy177.farmersfuturedelight.common.block.BlockDriedKelp;
import xy177.farmersfuturedelight.common.block.BlockGlowLichen;
import xy177.farmersfuturedelight.common.block.BlockHoney;
import xy177.farmersfuturedelight.common.block.BlockHangingRoots;
import xy177.farmersfuturedelight.common.block.BlockHoneycomb;
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
import xy177.farmersfuturedelight.common.block.BlockRootedDirt;
import xy177.farmersfuturedelight.common.block.BlockSporeBlossom;
import xy177.farmersfuturedelight.common.block.BlockSeagrass;
import xy177.farmersfuturedelight.common.block.BlockSeaPickle;
import xy177.farmersfuturedelight.common.block.BlockSmallDripleaf;
import xy177.farmersfuturedelight.common.block.BlockSweetBerryBush;
import xy177.farmersfuturedelight.common.block.BlockTallSeagrass;
import xy177.farmersfuturedelight.common.block.BlockTurtleEgg;

public final class FFDBlocks {
    public static final BlockSweetBerryBush SWEET_BERRY_BUSH = new BlockSweetBerryBush();
    public static final BlockMoss MOSS_BLOCK = new BlockMoss();
    public static final BlockMossCarpet MOSS_CARPET = new BlockMossCarpet();
    public static final BlockCaveVines CAVE_VINES = new BlockCaveVines();
    public static final BlockCaveVinesPlant CAVE_VINES_PLANT = new BlockCaveVinesPlant();
    public static final BlockAzalea AZALEA = new BlockAzalea(false);
    public static final BlockAzalea FLOWERING_AZALEA = new BlockAzalea(true);
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
    public static final BlockKelpYoung KELP_YOUNG = new BlockKelpYoung();
    public static final BlockKelp KELP = new BlockKelp();
    public static final BlockKelpPlant KELP_PLANT = new BlockKelpPlant();
    public static final BlockDriedKelp DRIED_KELP_BLOCK = new BlockDriedKelp();
    public static final BlockSeagrass SEAGRASS = new BlockSeagrass();
    public static final BlockTallSeagrass TALL_SEAGRASS = new BlockTallSeagrass();
    public static final BlockSeaPickle SEA_PICKLE = new BlockSeaPickle();
    public static final BlockTurtleEgg TURTLE_EGG = new BlockTurtleEgg();
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
    public static final BlockNetherBlock NETHER_WART_BLOCK = new BlockNetherBlock("nether_wart_block",
            net.minecraft.block.material.Material.GRASS, FFDSounds.WART_BLOCK, 1.0F);
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
    public static final BlockNetherButton CRIMSON_BUTTON = new BlockNetherButton("crimson_button");
    public static final BlockNetherButton WARPED_BUTTON = new BlockNetherButton("warped_button");
    public static final BlockNetherPressurePlate CRIMSON_PRESSURE_PLATE = new BlockNetherPressurePlate("crimson_pressure_plate");
    public static final BlockNetherPressurePlate WARPED_PRESSURE_PLATE = new BlockNetherPressurePlate("warped_pressure_plate");

    static {
        SHROOMLIGHT.setLightLevel(1.0F);
    }

    private FFDBlocks() {
    }
}
