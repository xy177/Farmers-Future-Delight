package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDRegistryEvents {
    private FFDRegistryEvents() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<net.minecraft.block.Block> event) {
        List<net.minecraft.block.Block> blocks = new ArrayList<>();
        if (FFDItems.isSweetBerryEnabled()) {
            blocks.add(FFDBlocks.SWEET_BERRY_BUSH);
        }
        if (FFDItems.isMossEnabled()) {
            blocks.add(FFDBlocks.MOSS_BLOCK);
            blocks.add(FFDBlocks.MOSS_CARPET);
        }
        if (FFDItems.isGlowBerryEnabled()) {
            blocks.add(FFDBlocks.CAVE_VINES);
            blocks.add(FFDBlocks.CAVE_VINES_PLANT);
        }
        if (FFDItems.isAzaleaEnabled()) {
            blocks.add(FFDBlocks.AZALEA);
            blocks.add(FFDBlocks.FLOWERING_AZALEA);
            blocks.add(FFDBlocks.AZALEA_LEAVES);
            blocks.add(FFDBlocks.FLOWERING_AZALEA_LEAVES);
        }
        if (FFDItems.isDripleafEnabled()) {
            blocks.add(FFDBlocks.SMALL_DRIPLEAF);
            blocks.add(FFDBlocks.BIG_DRIPLEAF_STEM);
            blocks.add(FFDBlocks.BIG_DRIPLEAF);
            blocks.add(FFDBlocks.BIG_DRIPLEAF_WATERLOGGED);
        }
        if (FFDItems.isRootedDirtEnabled()) {
            blocks.add(FFDBlocks.ROOTED_DIRT);
        }
        if (FFDItems.isHangingRootsEnabled()) {
            blocks.add(FFDBlocks.HANGING_ROOTS);
        }
        if (FFDItems.isSporeBlossomEnabled()) {
            blocks.add(FFDBlocks.SPORE_BLOSSOM);
        }
        if (FFDItems.isGlowLichenEnabled()) {
            for (net.minecraft.block.Block block : FFDBlocks.GLOW_LICHEN_VARIANTS) {
                blocks.add(block);
            }
        }
        if (FFDItems.isKelpEnabled()) {
            blocks.add(FFDBlocks.KELP_YOUNG);
            blocks.add(FFDBlocks.KELP);
            blocks.add(FFDBlocks.KELP_PLANT);
            blocks.add(FFDBlocks.DRIED_KELP_BLOCK);
        }
        if (FFDItems.isSeagrassEnabled()) {
            blocks.add(FFDBlocks.SEAGRASS);
            blocks.add(FFDBlocks.TALL_SEAGRASS);
        }
        if (FFDItems.isSeaPickleEnabled()) {
            blocks.add(FFDBlocks.SEA_PICKLE);
        }
        if (FFDItems.isTurtleEnabled()) {
            blocks.add(FFDBlocks.TURTLE_EGG);
        }
        if (FFDItems.isHoneyEnabled()) {
            blocks.add(FFDBlocks.HONEY_BLOCK);
            blocks.add(FFDBlocks.HONEYCOMB_BLOCK);
            blocks.add(FFDBlocks.BEE_NEST);
            blocks.add(FFDBlocks.BEEHIVE);
        }
        if (FFDItems.isCrimsonEnabled()) {
            blocks.add(FFDBlocks.CRIMSON_NYLIUM);
            blocks.add(FFDBlocks.CRIMSON_FUNGUS);
            blocks.add(FFDBlocks.CRIMSON_ROOTS);
            blocks.add(FFDBlocks.POTTED_CRIMSON_FUNGUS);
            blocks.add(FFDBlocks.POTTED_CRIMSON_ROOTS);
            blocks.add(FFDBlocks.WEEPING_VINES);
            blocks.add(FFDBlocks.WEEPING_VINES_PLANT);
        }
        if (FFDItems.isWarpedEnabled()) {
            blocks.add(FFDBlocks.WARPED_NYLIUM);
            blocks.add(FFDBlocks.WARPED_FUNGUS);
            blocks.add(FFDBlocks.WARPED_ROOTS);
            blocks.add(FFDBlocks.POTTED_WARPED_FUNGUS);
            blocks.add(FFDBlocks.POTTED_WARPED_ROOTS);
            blocks.add(FFDBlocks.NETHER_SPROUTS);
            blocks.add(FFDBlocks.TWISTING_VINES);
            blocks.add(FFDBlocks.TWISTING_VINES_PLANT);
        }
        if (FFDItems.isCrimsonWoodEnabled()) {
            blocks.add(FFDBlocks.CRIMSON_STEM);
            blocks.add(FFDBlocks.STRIPPED_CRIMSON_STEM);
            blocks.add(FFDBlocks.CRIMSON_HYPHAE);
            blocks.add(FFDBlocks.STRIPPED_CRIMSON_HYPHAE);
            blocks.add(FFDBlocks.NETHER_WART_BLOCK);
            blocks.add(FFDBlocks.CRIMSON_PLANKS);
            blocks.add(FFDBlocks.CRIMSON_STAIRS);
            blocks.add(FFDBlocks.CRIMSON_SLAB);
            blocks.add(FFDBlocks.CRIMSON_DOUBLE_SLAB);
            blocks.add(FFDBlocks.CRIMSON_FENCE);
            blocks.add(FFDBlocks.CRIMSON_FENCE_GATE);
            blocks.add(FFDBlocks.CRIMSON_DOOR);
            blocks.add(FFDBlocks.CRIMSON_TRAPDOOR);
            blocks.add(FFDBlocks.CRIMSON_BUTTON);
            blocks.add(FFDBlocks.CRIMSON_PRESSURE_PLATE);
        }
        if (FFDItems.isWarpedWoodEnabled()) {
            blocks.add(FFDBlocks.WARPED_STEM);
            blocks.add(FFDBlocks.STRIPPED_WARPED_STEM);
            blocks.add(FFDBlocks.WARPED_HYPHAE);
            blocks.add(FFDBlocks.STRIPPED_WARPED_HYPHAE);
            blocks.add(FFDBlocks.WARPED_WART_BLOCK);
            blocks.add(FFDBlocks.WARPED_PLANKS);
            blocks.add(FFDBlocks.WARPED_STAIRS);
            blocks.add(FFDBlocks.WARPED_SLAB);
            blocks.add(FFDBlocks.WARPED_DOUBLE_SLAB);
            blocks.add(FFDBlocks.WARPED_FENCE);
            blocks.add(FFDBlocks.WARPED_FENCE_GATE);
            blocks.add(FFDBlocks.WARPED_DOOR);
            blocks.add(FFDBlocks.WARPED_TRAPDOOR);
            blocks.add(FFDBlocks.WARPED_BUTTON);
            blocks.add(FFDBlocks.WARPED_PRESSURE_PLATE);
        }
        if (FFDItems.isCrimsonWoodEnabled() || FFDItems.isWarpedWoodEnabled()) {
            blocks.add(FFDBlocks.SHROOMLIGHT);
        }
        event.getRegistry().registerAll(blocks.toArray(new net.minecraft.block.Block[0]));
        registerFlammability();
    }

    private static void registerFlammability() {
        if (FFDItems.isSweetBerryEnabled()) {
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.SWEET_BERRY_BUSH, 60, 100);
        }
        if (FFDItems.isGlowBerryEnabled()) {
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.CAVE_VINES, 15, 60);
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.CAVE_VINES_PLANT, 15, 60);
        }
        if (FFDItems.isAzaleaEnabled()) {
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.AZALEA, 30, 60);
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.FLOWERING_AZALEA, 30, 60);
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.AZALEA_LEAVES, 30, 60);
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.FLOWERING_AZALEA_LEAVES, 30, 60);
        }
        if (FFDItems.isDripleafEnabled()) {
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.SMALL_DRIPLEAF, 60, 100);
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.BIG_DRIPLEAF_STEM, 60, 100);
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.BIG_DRIPLEAF, 60, 100);
        }
        if (FFDItems.isHangingRootsEnabled()) {
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.HANGING_ROOTS, 30, 60);
        }
        if (FFDItems.isSporeBlossomEnabled()) {
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.SPORE_BLOSSOM, 60, 100);
        }
        if (FFDItems.isHoneyEnabled()) {
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.BEE_NEST, 30, 20);
            net.minecraft.init.Blocks.FIRE.setFireInfo(FFDBlocks.BEEHIVE, 5, 20);
        }
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        if (FFDItems.isCrimsonEnabled()) {
            event.getRegistry().register(FFDBiomes.CRIMSON_FOREST);
            BiomeDictionary.addTypes(FFDBiomes.CRIMSON_FOREST, BiomeDictionary.Type.NETHER,
                    BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY, BiomeDictionary.Type.SPOOKY);
        }
        if (FFDItems.isWarpedEnabled()) {
            event.getRegistry().register(FFDBiomes.WARPED_FOREST);
            BiomeDictionary.addTypes(FFDBiomes.WARPED_FOREST, BiomeDictionary.Type.NETHER,
                    BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY, BiomeDictionary.Type.SPOOKY);
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<net.minecraft.item.Item> event) {
        List<net.minecraft.item.Item> items = new ArrayList<>();
        if (FFDItems.isSweetBerryEnabled()) {
            items.add(FFDItems.SWEET_BERRIES);
        }
        if (FFDItems.isMossEnabled()) {
            items.add(FFDItems.MOSS_BLOCK);
            items.add(FFDItems.MOSS_CARPET);
        }
        if (FFDItems.isGlowBerryEnabled()) {
            items.add(FFDItems.GLOW_BERRIES);
        }
        if (FFDItems.isAzaleaEnabled()) {
            items.add(FFDItems.AZALEA);
            items.add(FFDItems.FLOWERING_AZALEA);
            items.add(FFDItems.AZALEA_LEAVES);
            items.add(FFDItems.FLOWERING_AZALEA_LEAVES);
        }
        if (FFDItems.isDripleafEnabled()) {
            items.add(FFDItems.SMALL_DRIPLEAF);
            items.add(FFDItems.BIG_DRIPLEAF);
        }
        if (FFDItems.isRootedDirtEnabled()) {
            items.add(FFDItems.ROOTED_DIRT);
        }
        if (FFDItems.isHangingRootsEnabled()) {
            items.add(FFDItems.HANGING_ROOTS);
        }
        if (FFDItems.isSporeBlossomEnabled()) {
            items.add(FFDItems.SPORE_BLOSSOM);
        }
        if (FFDItems.isGlowLichenEnabled()) {
            items.add(FFDItems.GLOW_LICHEN);
        }
        if (FFDEntities.isGlowSquidEnabled()) {
            items.add(FFDItems.GLOW_INK_SAC);
        }
        if (FFDItems.isKelpEnabled()) {
            items.add(FFDItems.KELP);
            items.add(FFDItems.DRIED_KELP);
            items.add(FFDItems.DRIED_KELP_BLOCK);
        }
        if (FFDItems.isSeagrassEnabled()) {
            items.add(FFDItems.SEAGRASS);
        }
        if (FFDItems.isSeaPickleEnabled()) {
            items.add(FFDItems.SEA_PICKLE);
        }
        if (FFDItems.isTurtleEnabled()) {
            items.add(FFDItems.TURTLE_EGG);
            items.add(FFDItems.TURTLE_SCUTE);
            items.add(FFDItems.TURTLE_HELMET);
        }
        if (FFDItems.isHoneyEnabled()) {
            items.add(FFDItems.HONEY_BOTTLE);
            items.add(FFDItems.HONEYCOMB);
            items.add(FFDItems.HONEY_BLOCK);
            items.add(FFDItems.HONEYCOMB_BLOCK);
            items.add(FFDItems.BEE_NEST);
            items.add(FFDItems.BEEHIVE);
        }
        if (FFDItems.isCrimsonEnabled()) {
            items.add(FFDItems.CRIMSON_NYLIUM);
            items.add(FFDItems.CRIMSON_FUNGUS);
            items.add(FFDItems.CRIMSON_ROOTS);
            items.add(FFDItems.WEEPING_VINES);
        }
        if (FFDItems.isWarpedEnabled()) {
            items.add(FFDItems.WARPED_NYLIUM);
            items.add(FFDItems.WARPED_FUNGUS);
            items.add(FFDItems.WARPED_ROOTS);
            items.add(FFDItems.NETHER_SPROUTS);
            items.add(FFDItems.TWISTING_VINES);
        }
        if (FFDItems.isCrimsonWoodEnabled()) {
            items.add(FFDItems.CRIMSON_STEM);
            items.add(FFDItems.STRIPPED_CRIMSON_STEM);
            items.add(FFDItems.CRIMSON_HYPHAE);
            items.add(FFDItems.STRIPPED_CRIMSON_HYPHAE);
            items.add(FFDItems.NETHER_WART_BLOCK);
            items.add(FFDItems.CRIMSON_PLANKS);
            items.add(FFDItems.CRIMSON_STAIRS);
            items.add(FFDItems.CRIMSON_SLAB);
            items.add(FFDItems.CRIMSON_FENCE);
            items.add(FFDItems.CRIMSON_FENCE_GATE);
            items.add(FFDItems.CRIMSON_DOOR);
            items.add(FFDItems.CRIMSON_TRAPDOOR);
            items.add(FFDItems.CRIMSON_BUTTON);
            items.add(FFDItems.CRIMSON_PRESSURE_PLATE);
        }
        if (FFDItems.isWarpedWoodEnabled()) {
            items.add(FFDItems.WARPED_STEM);
            items.add(FFDItems.STRIPPED_WARPED_STEM);
            items.add(FFDItems.WARPED_HYPHAE);
            items.add(FFDItems.STRIPPED_WARPED_HYPHAE);
            items.add(FFDItems.WARPED_WART_BLOCK);
            items.add(FFDItems.WARPED_PLANKS);
            items.add(FFDItems.WARPED_STAIRS);
            items.add(FFDItems.WARPED_SLAB);
            items.add(FFDItems.WARPED_FENCE);
            items.add(FFDItems.WARPED_FENCE_GATE);
            items.add(FFDItems.WARPED_DOOR);
            items.add(FFDItems.WARPED_TRAPDOOR);
            items.add(FFDItems.WARPED_BUTTON);
            items.add(FFDItems.WARPED_PRESSURE_PLATE);
        }
        if (FFDItems.isCrimsonWoodEnabled() || FFDItems.isWarpedWoodEnabled()) {
            items.add(FFDItems.SHROOMLIGHT);
        }
        event.getRegistry().registerAll(items.toArray(new net.minecraft.item.Item[0]));
        if (FFDItems.isSweetBerryEnabled()) {
            OreDictionary.registerOre("cropSweetBerry", FFDItems.SWEET_BERRIES);
            OreDictionary.registerOre("cropBerrySweet", FFDItems.SWEET_BERRIES);
            OreDictionary.registerOre("processedFruit", FFDItems.SWEET_BERRIES);
        }
        if (FFDItems.isCrimsonWoodEnabled()) {
            registerNetherWoodOreDictionary(true);
        }
        if (FFDItems.isWarpedWoodEnabled()) {
            registerNetherWoodOreDictionary(false);
        }
    }

    private static void registerNetherWoodOreDictionary(boolean crimson) {
        net.minecraft.item.Item stem = crimson ? FFDItems.CRIMSON_STEM : FFDItems.WARPED_STEM;
        net.minecraft.item.Item strippedStem = crimson ? FFDItems.STRIPPED_CRIMSON_STEM
                : FFDItems.STRIPPED_WARPED_STEM;
        net.minecraft.item.Item hyphae = crimson ? FFDItems.CRIMSON_HYPHAE : FFDItems.WARPED_HYPHAE;
        net.minecraft.item.Item strippedHyphae = crimson ? FFDItems.STRIPPED_CRIMSON_HYPHAE
                : FFDItems.STRIPPED_WARPED_HYPHAE;
        OreDictionary.registerOre("logWood", stem);
        OreDictionary.registerOre("logWood", strippedStem);
        OreDictionary.registerOre("logWood", hyphae);
        OreDictionary.registerOre("logWood", strippedHyphae);
        OreDictionary.registerOre("plankWood", crimson ? FFDItems.CRIMSON_PLANKS : FFDItems.WARPED_PLANKS);
        OreDictionary.registerOre("stairWood", crimson ? FFDItems.CRIMSON_STAIRS : FFDItems.WARPED_STAIRS);
        OreDictionary.registerOre("slabWood", crimson ? FFDItems.CRIMSON_SLAB : FFDItems.WARPED_SLAB);
        OreDictionary.registerOre("fenceWood", crimson ? FFDItems.CRIMSON_FENCE : FFDItems.WARPED_FENCE);
        OreDictionary.registerOre("fenceGateWood",
                crimson ? FFDItems.CRIMSON_FENCE_GATE : FFDItems.WARPED_FENCE_GATE);
        OreDictionary.registerOre("doorWood", crimson ? FFDItems.CRIMSON_DOOR : FFDItems.WARPED_DOOR);
        OreDictionary.registerOre("trapdoorWood",
                crimson ? FFDItems.CRIMSON_TRAPDOOR : FFDItems.WARPED_TRAPDOOR);
        OreDictionary.registerOre("buttonWood", crimson ? FFDItems.CRIMSON_BUTTON : FFDItems.WARPED_BUTTON);
        OreDictionary.registerOre("pressurePlateWood",
                crimson ? FFDItems.CRIMSON_PRESSURE_PLATE : FFDItems.WARPED_PRESSURE_PLATE);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(FFDSounds.all());
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        List<IRecipe> recipes = new ArrayList<>();
        if (FFDItems.isMossEnabled()) {
            ResourceLocation mossGroup = new ResourceLocation(FarmerFutureDelight.MODID, "moss");
            recipes.add(new ShapedOreRecipe(mossGroup, new ItemStack(FFDItems.MOSS_CARPET, 3),
                    "##", '#', FFDItems.MOSS_BLOCK)
                    .setRegistryName(FarmerFutureDelight.MODID, "moss_carpet"));
            recipes.add(new ShapelessOreRecipe(mossGroup,
                    new ItemStack(net.minecraft.init.Blocks.MOSSY_COBBLESTONE),
                    net.minecraft.init.Blocks.COBBLESTONE, FFDItems.MOSS_BLOCK)
                    .setRegistryName(FarmerFutureDelight.MODID, "mossy_cobblestone"));
            recipes.add(new ShapelessOreRecipe(mossGroup,
                    new ItemStack(net.minecraft.init.Blocks.STONEBRICK, 1, 1),
                    new ItemStack(net.minecraft.init.Blocks.STONEBRICK), FFDItems.MOSS_BLOCK)
                    .setRegistryName(FarmerFutureDelight.MODID, "mossy_stone_bricks"));
        }
        if (FFDItems.isKelpEnabled()) {
            ResourceLocation kelpGroup = new ResourceLocation(FarmerFutureDelight.MODID, "kelp");
            recipes.add(new ShapedOreRecipe(kelpGroup,
                    new ItemStack(FFDItems.DRIED_KELP_BLOCK),
                    "###", "###", "###", '#', FFDItems.DRIED_KELP)
                    .setRegistryName(FarmerFutureDelight.MODID, "dried_kelp_block"));
            recipes.add(new ShapelessOreRecipe(kelpGroup,
                    new ItemStack(FFDItems.DRIED_KELP, 9), FFDItems.DRIED_KELP_BLOCK)
                    .setRegistryName(FarmerFutureDelight.MODID, "dried_kelp"));
        }
        if (FFDItems.isHoneyEnabled()) {
            ResourceLocation honeyGroup = new ResourceLocation(FarmerFutureDelight.MODID, "honey");
            recipes.add(new ShapedOreRecipe(honeyGroup, new ItemStack(FFDItems.HONEY_BLOCK),
                    "##", "##", '#', FFDItems.HONEY_BOTTLE)
                    .setRegistryName(FarmerFutureDelight.MODID, "honey_block"));
            recipes.add(new ShapelessOreRecipe(honeyGroup, new ItemStack(FFDItems.HONEY_BOTTLE, 4),
                    FFDItems.HONEY_BLOCK, Items.GLASS_BOTTLE, Items.GLASS_BOTTLE,
                    Items.GLASS_BOTTLE, Items.GLASS_BOTTLE)
                    .setRegistryName(FarmerFutureDelight.MODID, "honey_bottles"));
            recipes.add(new ShapedOreRecipe(honeyGroup, new ItemStack(FFDItems.HONEYCOMB_BLOCK),
                    "##", "##", '#', FFDItems.HONEYCOMB)
                    .setRegistryName(FarmerFutureDelight.MODID, "honeycomb_block"));
            recipes.add(new ShapelessOreRecipe(honeyGroup, new ItemStack(Items.SUGAR, 3),
                    FFDItems.HONEY_BOTTLE)
                    .setRegistryName(FarmerFutureDelight.MODID, "sugar_from_honey_bottle"));
            recipes.add(new ShapedOreRecipe(honeyGroup, new ItemStack(FFDItems.BEEHIVE),
                    "PPP", "HHH", "PPP", 'P', "plankWood", 'H', FFDItems.HONEYCOMB)
                    .setRegistryName(FarmerFutureDelight.MODID, "beehive"));
        }
        if (FFDItems.isTurtleEnabled()) {
            ResourceLocation turtleGroup = new ResourceLocation(FarmerFutureDelight.MODID, "turtle_helmet");
            recipes.add(new ShapedOreRecipe(turtleGroup, new ItemStack(FFDItems.TURTLE_HELMET),
                    "XXX", "X X", 'X', FFDItems.TURTLE_SCUTE)
                    .setRegistryName(FarmerFutureDelight.MODID, "turtle_helmet"));
        }
        if (FFDItems.isCrimsonWoodEnabled()) {
            addNetherWoodRecipes(recipes, "crimson", FFDItems.CRIMSON_STEM,
                    FFDItems.STRIPPED_CRIMSON_STEM, FFDItems.CRIMSON_HYPHAE,
                    FFDItems.STRIPPED_CRIMSON_HYPHAE, FFDItems.CRIMSON_PLANKS,
                    FFDItems.CRIMSON_STAIRS, FFDItems.CRIMSON_SLAB, FFDItems.CRIMSON_FENCE,
                    FFDItems.CRIMSON_FENCE_GATE, FFDItems.CRIMSON_DOOR, FFDItems.CRIMSON_TRAPDOOR,
                    FFDItems.CRIMSON_BUTTON, FFDItems.CRIMSON_PRESSURE_PLATE);
        }
        if (FFDItems.isWarpedWoodEnabled()) {
            addNetherWoodRecipes(recipes, "warped", FFDItems.WARPED_STEM,
                    FFDItems.STRIPPED_WARPED_STEM, FFDItems.WARPED_HYPHAE,
                    FFDItems.STRIPPED_WARPED_HYPHAE, FFDItems.WARPED_PLANKS,
                    FFDItems.WARPED_STAIRS, FFDItems.WARPED_SLAB, FFDItems.WARPED_FENCE,
                    FFDItems.WARPED_FENCE_GATE, FFDItems.WARPED_DOOR, FFDItems.WARPED_TRAPDOOR,
                    FFDItems.WARPED_BUTTON, FFDItems.WARPED_PRESSURE_PLATE);
        }
        if (!recipes.isEmpty()) {
            event.getRegistry().registerAll(recipes.toArray(new IRecipe[0]));
        }
    }

    private static void addNetherWoodRecipes(List<IRecipe> recipes, String prefix,
                                              net.minecraft.item.Item stem,
                                              net.minecraft.item.Item strippedStem,
                                              net.minecraft.item.Item hyphae,
                                              net.minecraft.item.Item strippedHyphae,
                                              net.minecraft.item.Item planks,
                                              net.minecraft.item.Item stairs,
                                              net.minecraft.item.Item slab,
                                              net.minecraft.item.Item fence,
                                              net.minecraft.item.Item fenceGate,
                                              net.minecraft.item.Item door,
                                              net.minecraft.item.Item trapdoor,
                                              net.minecraft.item.Item button,
                                              net.minecraft.item.Item pressurePlate) {
        ResourceLocation group = new ResourceLocation(FarmerFutureDelight.MODID, prefix + "_wood");
        recipes.add(new ShapelessOreRecipe(group, new ItemStack(planks, 4), stem)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_planks_from_stem"));
        recipes.add(new ShapelessOreRecipe(group, new ItemStack(planks, 4), strippedStem)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_planks_from_stripped_stem"));
        recipes.add(new ShapelessOreRecipe(group, new ItemStack(planks, 4), hyphae)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_planks_from_hyphae"));
        recipes.add(new ShapelessOreRecipe(group, new ItemStack(planks, 4), strippedHyphae)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_planks_from_stripped_hyphae"));
        recipes.add(new ShapedOreRecipe(group, new ItemStack(hyphae, 3),
                "SS", "SS", 'S', stem)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_hyphae"));
        recipes.add(new ShapedOreRecipe(group, new ItemStack(strippedHyphae, 3),
                "SS", "SS", 'S', strippedStem)
                .setRegistryName(FarmerFutureDelight.MODID, "stripped_" + prefix + "_hyphae"));
        recipes.add(new ShapedOreRecipe(group, new ItemStack(Items.STICK, 4),
                "P", "P", 'P', planks)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_sticks"));
        recipes.add(new ShapedOreRecipe(group, new ItemStack(stairs, 4),
                "P  ", "PP ", "PPP", 'P', planks)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_stairs"));
        recipes.add(new ShapedOreRecipe(group, new ItemStack(slab, 6),
                "PPP", 'P', planks)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_slab"));
        recipes.add(new ShapedOreRecipe(group, new ItemStack(fence, 3),
                "PSP", "PSP", 'P', planks, 'S', Items.STICK)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_fence"));
        recipes.add(new ShapedOreRecipe(group, new ItemStack(fenceGate),
                "SPS", "SPS", 'P', planks, 'S', Items.STICK)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_fence_gate"));
        recipes.add(new ShapedOreRecipe(group, new ItemStack(door, 3),
                "PP", "PP", "PP", 'P', planks)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_door"));
        recipes.add(new ShapedOreRecipe(group, new ItemStack(trapdoor, 2),
                "PPP", "PPP", 'P', planks)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_trapdoor"));
        recipes.add(new ShapelessOreRecipe(group, new ItemStack(button), planks)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_button"));
        recipes.add(new ShapedOreRecipe(group, new ItemStack(pressurePlate),
                "PP", 'P', planks)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_pressure_plate"));
    }
}
