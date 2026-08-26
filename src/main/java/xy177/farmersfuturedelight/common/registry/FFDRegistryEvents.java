package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.block.CopperWeathering;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDRegistryEvents {
    private FFDRegistryEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
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
            blocks.add(FFDBlocks.POTTED_AZALEA_BUSH);
            blocks.add(FFDBlocks.POTTED_FLOWERING_AZALEA_BUSH);
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
        if (FFDItems.isAmethystEnabled()) {
            blocks.add(FFDBlocks.AMETHYST_BLOCK);
            blocks.add(FFDBlocks.BUDDING_AMETHYST);
            blocks.add(FFDBlocks.SMALL_AMETHYST_BUD);
            blocks.add(FFDBlocks.MEDIUM_AMETHYST_BUD);
            blocks.add(FFDBlocks.LARGE_AMETHYST_BUD);
            blocks.add(FFDBlocks.AMETHYST_CLUSTER);
            blocks.add(FFDBlocks.CALCITE);
            blocks.add(FFDBlocks.SMOOTH_BASALT);
            blocks.add(FFDBlocks.TINTED_GLASS);
        }
        if (FFDItems.isDripstoneEnabled()) {
            blocks.add(FFDBlocks.DRIPSTONE_BLOCK);
            blocks.add(FFDBlocks.POINTED_DRIPSTONE);
        }
        if (FFDItems.isIronChainEnabled()) {
            blocks.add(FFDBlocks.IRON_CHAIN);
        }
        if (FFDItems.isLightEnabled()) {
            blocks.add(FFDBlocks.LIGHT);
        }
        if (FFDItems.isCandleEnabled()) {
            Collections.addAll(blocks, FFDBlocks.CANDLES);
            Collections.addAll(blocks, FFDBlocks.CANDLE_CAKES);
        }
        if (FFDItems.isPowderSnowEnabled()) {
            blocks.add(FFDBlocks.POWDER_SNOW);
        }
        blocks.add(FFDBlocks.LAVA_CAULDRON);
        blocks.add(FFDBlocks.POWDER_SNOW_CAULDRON);
        if (FFDItems.isDeepslateEnabled()) {
            addDeepslateBlocks(blocks);
        }
        if (FFDItems.isRawOreEnabled()) {
            addRawOreBlocks(blocks, false);
        }
        if (FFDItems.isCopperEnabled()) {
            blocks.add(FFDBlocks.COPPER_ORE);
            addRawOreBlocks(blocks, true);
            Collections.addAll(blocks, FFDBlocks.COPPER_BLOCKS);
            Collections.addAll(blocks, FFDBlocks.WAXED_COPPER_BLOCKS);
            Collections.addAll(blocks, FFDBlocks.CUT_COPPER_BLOCKS);
            Collections.addAll(blocks, FFDBlocks.WAXED_CUT_COPPER_BLOCKS);
            Collections.addAll(blocks, FFDBlocks.CUT_COPPER_STAIRS);
            Collections.addAll(blocks, FFDBlocks.WAXED_CUT_COPPER_STAIRS);
            Collections.addAll(blocks, FFDBlocks.CUT_COPPER_SLABS);
            Collections.addAll(blocks, FFDBlocks.WAXED_CUT_COPPER_SLABS);
            Collections.addAll(blocks, FFDBlocks.CUT_COPPER_DOUBLE_SLABS);
            Collections.addAll(blocks, FFDBlocks.WAXED_CUT_COPPER_DOUBLE_SLABS);
            Collections.addAll(blocks, FFDBlocks.LIGHTNING_RODS);
            Collections.addAll(blocks, FFDBlocks.WAXED_LIGHTNING_RODS);
            if (FFDItems.isDeepslateEnabled()) {
                blocks.add(FFDBlocks.DEEPSLATE_COPPER_ORE);
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
        for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
            if (entry.isEnabled()) {
                blocks.add(entry.block());
            }
        }
        blocks.removeIf(block -> !FFDItems.shouldRegisterBlock(block));
        event.getRegistry().registerAll(blocks.toArray(new net.minecraft.block.Block[0]));
        CopperWeathering.rebuildEffectiveMappings();
        registerFlammability();
    }

    @SubscribeEvent
    public static void remapRawOreBlocks(
            RegistryEvent.MissingMappings<net.minecraft.block.Block> event) {
        if (Loader.isModLoaded("suikerawore")) {
            return;
        }
        for (RegistryEvent.MissingMappings.Mapping<net.minecraft.block.Block> mapping
                : event.getAllMappings()) {
            int index = rawOreMigrationIndex(mapping.key, "raw_block_");
            if (index >= 0 && FFDItems.isBlockRegistered(FFDBlocks.RAW_ORE_BLOCKS[index])) {
                mapping.remap(FFDBlocks.RAW_ORE_BLOCKS[index]);
            }
        }
    }

    @SubscribeEvent
    public static void remapRawOreItems(
            RegistryEvent.MissingMappings<net.minecraft.item.Item> event) {
        if (Loader.isModLoaded("suikerawore")) {
            return;
        }
        for (RegistryEvent.MissingMappings.Mapping<net.minecraft.item.Item> mapping
                : event.getAllMappings()) {
            int rawIndex = rawOreMigrationIndex(mapping.key, "raw_");
            if (rawIndex >= 0 && FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[rawIndex])) {
                mapping.remap(FFDItems.RAW_ORE_ITEMS[rawIndex]);
                continue;
            }
            int blockIndex = rawOreMigrationIndex(mapping.key, "raw_block_");
            if (blockIndex >= 0
                    && FFDItems.isItemRegistered(FFDItems.RAW_ORE_BLOCK_ITEMS[blockIndex])) {
                mapping.remap(FFDItems.RAW_ORE_BLOCK_ITEMS[blockIndex]);
            }
        }
    }

    private static int rawOreMigrationIndex(ResourceLocation key, String prefix) {
        if (key == null || !"suikerawore".equals(key.getResourceDomain())
                || !key.getResourcePath().startsWith(prefix)) {
            return -1;
        }
        String name = key.getResourcePath().substring(prefix.length());
        for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
            if (FFDRawOres.NAMES[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        List<EntityEntry> entities = new ArrayList<>();
        if (FFDEntities.isLocalGlowSquidEnabled()) {
            entities.add(entity(FFDEntities.GLOW_SQUID_ID,
                    xy177.farmersfuturedelight.common.entity.EntityGlowSquid.class,
                    "glow_squid", 0, 64, 1, 0x095B71, 0x85F1FF));
            net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(
                    xy177.farmersfuturedelight.common.entity.EntityGlowSquid.class,
                    net.minecraft.entity.EntityLiving.SpawnPlacementType.IN_WATER);
        }
        if (FFDEntities.isLocalTurtleEnabled()) {
            entities.add(entity(FFDEntities.TURTLE_ID,
                    xy177.farmersfuturedelight.common.entity.EntityTurtle.class,
                    "turtle", 1, 80, 3, 0x3B6C55, 0xA6E8AD));
            net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(
                    xy177.farmersfuturedelight.common.entity.EntityTurtle.class,
                    net.minecraft.entity.EntityLiving.SpawnPlacementType.ON_GROUND);
        }
        if (FFDEntities.isLocalAxolotlEnabled()) {
            entities.add(entity(FFDEntities.AXOLOTL_ID,
                    xy177.farmersfuturedelight.common.entity.EntityAxolotl.class,
                    "axolotl", 5, 80, 3, 0xFBC1E3, 0xA62D74));
            net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(
                    xy177.farmersfuturedelight.common.entity.EntityAxolotl.class,
                    net.minecraft.entity.EntityLiving.SpawnPlacementType.IN_WATER);
        }
        if (FFDEntities.isLocalGoatEnabled()) {
            entities.add(entity(FFDEntities.GOAT_ID,
                    xy177.farmersfuturedelight.common.entity.EntityGoat.class,
                    "goat", 6, 80, 3, 0xAFAFAD, 0x876C57));
            net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(
                    xy177.farmersfuturedelight.common.entity.EntityGoat.class,
                    net.minecraft.entity.EntityLiving.SpawnPlacementType.ON_GROUND);
        }
        if (FFDEntities.isLocalBeeEnabled()) {
            entities.add(entity(FFDEntities.BEE_ID,
                    xy177.farmersfuturedelight.common.entity.EntityBee.class,
                    "bee", 2, 64, 3, 0xE5D5A0, 0xA45E32));
        }
        if (FFDEntities.isLocalPhantomEnabled()) {
            entities.add(entity(FFDEntities.PHANTOM_ID,
                    xy177.farmersfuturedelight.common.entity.EntityPhantom.class,
                    "phantom", 3, 80, 1, 0x43518A, 0x88FF00));
        }
        if (FFDEntities.isLocalGlowItemFrameEnabled()) {
            entities.add(entity(FFDEntities.GLOW_ITEM_FRAME_ID,
                    xy177.farmersfuturedelight.common.entity.EntityGlowItemFrame.class,
                    "glow_item_frame", 4, 64, 1));
        }
        event.getRegistry().registerAll(entities.toArray(new EntityEntry[0]));
    }

    private static <E extends net.minecraft.entity.Entity> EntityEntry entity(
            ResourceLocation id, Class<? extends E> type, String name, int networkId,
            int trackingRange, int updateFrequency) {
        return EntityEntryBuilder.<E>create()
                .entity(type)
                .id(id, networkId)
                .name(FarmerFutureDelight.MODID + "." + name)
                .tracker(trackingRange, updateFrequency, true)
                .build();
    }

    private static <E extends net.minecraft.entity.Entity> EntityEntry entity(
            ResourceLocation id, Class<? extends E> type, String name, int networkId,
            int trackingRange, int updateFrequency, int primaryEgg, int secondaryEgg) {
        return EntityEntryBuilder.<E>create()
                .entity(type)
                .id(id, networkId)
                .name(FarmerFutureDelight.MODID + "." + name)
                .tracker(trackingRange, updateFrequency, true)
                .egg(primaryEgg, secondaryEgg)
                .build();
    }

    private static void addDeepslateBlocks(List<net.minecraft.block.Block> blocks) {
        blocks.add(FFDBlocks.DEEPSLATE);
        blocks.add(FFDBlocks.COBBLED_DEEPSLATE);
        blocks.add(FFDBlocks.POLISHED_DEEPSLATE);
        blocks.add(FFDBlocks.DEEPSLATE_BRICKS);
        blocks.add(FFDBlocks.CRACKED_DEEPSLATE_BRICKS);
        blocks.add(FFDBlocks.DEEPSLATE_TILES);
        blocks.add(FFDBlocks.CRACKED_DEEPSLATE_TILES);
        blocks.add(FFDBlocks.CHISELED_DEEPSLATE);
        blocks.add(FFDBlocks.TUFF);
        blocks.add(FFDBlocks.INFESTED_DEEPSLATE);
        blocks.add(FFDBlocks.COBBLED_DEEPSLATE_STAIRS);
        blocks.add(FFDBlocks.POLISHED_DEEPSLATE_STAIRS);
        blocks.add(FFDBlocks.DEEPSLATE_BRICK_STAIRS);
        blocks.add(FFDBlocks.DEEPSLATE_TILE_STAIRS);
        blocks.add(FFDBlocks.COBBLED_DEEPSLATE_SLAB);
        blocks.add(FFDBlocks.COBBLED_DEEPSLATE_DOUBLE_SLAB);
        blocks.add(FFDBlocks.POLISHED_DEEPSLATE_SLAB);
        blocks.add(FFDBlocks.POLISHED_DEEPSLATE_DOUBLE_SLAB);
        blocks.add(FFDBlocks.DEEPSLATE_BRICK_SLAB);
        blocks.add(FFDBlocks.DEEPSLATE_BRICK_DOUBLE_SLAB);
        blocks.add(FFDBlocks.DEEPSLATE_TILE_SLAB);
        blocks.add(FFDBlocks.DEEPSLATE_TILE_DOUBLE_SLAB);
        blocks.add(FFDBlocks.COBBLED_DEEPSLATE_WALL);
        blocks.add(FFDBlocks.POLISHED_DEEPSLATE_WALL);
        blocks.add(FFDBlocks.DEEPSLATE_BRICK_WALL);
        blocks.add(FFDBlocks.DEEPSLATE_TILE_WALL);
        blocks.add(FFDBlocks.DEEPSLATE_COAL_ORE);
        blocks.add(FFDBlocks.DEEPSLATE_IRON_ORE);
        blocks.add(FFDBlocks.DEEPSLATE_GOLD_ORE);
        blocks.add(FFDBlocks.DEEPSLATE_REDSTONE_ORE);
        blocks.add(FFDBlocks.DEEPSLATE_LAPIS_ORE);
        blocks.add(FFDBlocks.DEEPSLATE_DIAMOND_ORE);
        blocks.add(FFDBlocks.DEEPSLATE_EMERALD_ORE);
    }

    private static void registerFlammability() {
        setFireInfoIfRegistered(FFDBlocks.SWEET_BERRY_BUSH, 60, 100);
        setFireInfoIfRegistered(FFDBlocks.CAVE_VINES, 15, 60);
        setFireInfoIfRegistered(FFDBlocks.CAVE_VINES_PLANT, 15, 60);
        setFireInfoIfRegistered(FFDBlocks.AZALEA, 30, 60);
        setFireInfoIfRegistered(FFDBlocks.FLOWERING_AZALEA, 30, 60);
        setFireInfoIfRegistered(FFDBlocks.AZALEA_LEAVES, 30, 60);
        setFireInfoIfRegistered(FFDBlocks.FLOWERING_AZALEA_LEAVES, 30, 60);
        setFireInfoIfRegistered(FFDBlocks.SMALL_DRIPLEAF, 60, 100);
        setFireInfoIfRegistered(FFDBlocks.BIG_DRIPLEAF_STEM, 60, 100);
        setFireInfoIfRegistered(FFDBlocks.BIG_DRIPLEAF, 60, 100);
        setFireInfoIfRegistered(FFDBlocks.HANGING_ROOTS, 30, 60);
        setFireInfoIfRegistered(FFDBlocks.SPORE_BLOSSOM, 60, 100);
        setFireInfoIfRegistered(FFDBlocks.BEE_NEST, 30, 20);
        setFireInfoIfRegistered(FFDBlocks.BEEHIVE, 5, 20);
    }

    private static void setFireInfoIfRegistered(net.minecraft.block.Block block,
                                                int encouragement, int flammability) {
        if (FFDItems.isBlockRegistered(block)) {
            net.minecraft.init.Blocks.FIRE.setFireInfo(block, encouragement, flammability);
        }
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        event.getRegistry().registerAll(FFDBiomes.MOUNTAIN_BIOMES);
        BiomeDictionary.addTypes(FFDBiomes.MEADOW, BiomeDictionary.Type.MOUNTAIN,
                BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.LUSH);
        BiomeDictionary.addTypes(FFDBiomes.GROVE, BiomeDictionary.Type.MOUNTAIN,
                BiomeDictionary.Type.FOREST, BiomeDictionary.Type.CONIFEROUS,
                BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY);
        BiomeDictionary.addTypes(FFDBiomes.SNOWY_SLOPES, BiomeDictionary.Type.MOUNTAIN,
                BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.SPARSE);
        BiomeDictionary.addTypes(FFDBiomes.JAGGED_PEAKS, BiomeDictionary.Type.MOUNTAIN,
                BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.SPARSE);
        BiomeDictionary.addTypes(FFDBiomes.FROZEN_PEAKS, BiomeDictionary.Type.MOUNTAIN,
                BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.SPARSE);
        BiomeDictionary.addTypes(FFDBiomes.STONY_PEAKS, BiomeDictionary.Type.MOUNTAIN,
                BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY, BiomeDictionary.Type.SPARSE);
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerItems(RegistryEvent.Register<net.minecraft.item.Item> event) {
        FFDRawOreOreDictionaryCompat.register();
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
        if (FFDItems.isGlowItemFrameEnabled()) {
            items.add(FFDItems.GLOW_ITEM_FRAME);
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
        if (FFDItems.isAxolotlEnabled()) {
            items.add(FFDItems.AXOLOTL_BUCKET);
        }
        if (FFDItems.isGoatEnabled()) {
            items.add(FFDItems.GOAT_HORN);
        }
        if (FFDItems.isPhantomEnabled()) {
            items.add(FFDItems.PHANTOM_MEMBRANE);
        }
        if (FFDItems.isOthersideEnabled()) {
            items.add(FFDItems.MUSIC_DISC_OTHERSIDE);
        }
        if (FFDItems.isAmethystEnabled()) {
            items.add(FFDItems.AMETHYST_BLOCK);
            items.add(FFDItems.BUDDING_AMETHYST);
            items.add(FFDItems.SMALL_AMETHYST_BUD);
            items.add(FFDItems.MEDIUM_AMETHYST_BUD);
            items.add(FFDItems.LARGE_AMETHYST_BUD);
            items.add(FFDItems.AMETHYST_CLUSTER);
            items.add(FFDItems.CALCITE);
            items.add(FFDItems.SMOOTH_BASALT);
            items.add(FFDItems.TINTED_GLASS);
            items.add(FFDItems.AMETHYST_SHARD);
        }
        if (FFDItems.isDripstoneEnabled()) {
            items.add(FFDItems.DRIPSTONE_BLOCK);
            items.add(FFDItems.POINTED_DRIPSTONE);
        }
        if (FFDItems.isIronChainEnabled()) {
            items.add(FFDItems.IRON_CHAIN);
        }
        if (FFDItems.isLightEnabled()) {
            items.add(FFDItems.LIGHT);
        }
        if (FFDItems.isCandleEnabled()) {
            Collections.addAll(items, FFDItems.CANDLE_ITEMS);
        }
        if (FFDItems.isPowderSnowEnabled()) {
            items.add(FFDItems.POWDER_SNOW);
            items.add(FFDItems.POWDER_SNOW_BUCKET);
        }
        if (FFDItems.isDeepslateEnabled()) {
            addDeepslateItems(items);
        }
        if (FFDItems.isRawOreEnabled()) {
            addRawOreItems(items, false);
        }
        if (FFDItems.isCopperEnabled()) {
            items.add(FFDItems.COPPER_ORE);
            addRawOreItems(items, true);
            items.add(FFDItems.COPPER_INGOT);
            Collections.addAll(items, FFDItems.COPPER_BLOCK_ITEMS);
            Collections.addAll(items, FFDItems.WAXED_COPPER_BLOCK_ITEMS);
            Collections.addAll(items, FFDItems.CUT_COPPER_ITEMS);
            Collections.addAll(items, FFDItems.WAXED_CUT_COPPER_ITEMS);
            Collections.addAll(items, FFDItems.CUT_COPPER_STAIR_ITEMS);
            Collections.addAll(items, FFDItems.WAXED_CUT_COPPER_STAIR_ITEMS);
            Collections.addAll(items, FFDItems.CUT_COPPER_SLAB_ITEMS);
            Collections.addAll(items, FFDItems.WAXED_CUT_COPPER_SLAB_ITEMS);
            Collections.addAll(items, FFDItems.LIGHTNING_ROD_ITEMS);
            Collections.addAll(items, FFDItems.WAXED_LIGHTNING_ROD_ITEMS);
            items.add(FFDItems.SPYGLASS);
            if (FFDItems.isDeepslateEnabled()) {
                items.add(FFDItems.DEEPSLATE_COPPER_ORE);
            }
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
        for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
            if (entry.isEnabled()) {
                items.add(entry.item());
                items.add(entry.blockItem());
            }
        }
        items.removeIf(item -> !FFDItems.shouldRegisterItem(item));
        event.getRegistry().registerAll(items.toArray(new net.minecraft.item.Item[0]));
        if (FFDItems.isSweetBerryEnabled()) {
            registerOre("seedSweetBerry", FFDItems.SWEET_BERRIES);
            registerOre("cropSweetBerry", FFDItems.SWEET_BERRIES);
            registerOre("cropBerrySweet", FFDItems.SWEET_BERRIES);
            registerOre("processedFruit", FFDItems.SWEET_BERRIES);
            registerOre("listAllfruit", FFDItems.SWEET_BERRIES);
            registerOre("listAllberry", FFDItems.SWEET_BERRIES);
        }
        if (FFDItems.isGlowBerryEnabled()) {
            registerOre("cropGlowBerry", FFDItems.GLOW_BERRIES);
            registerOre("cropGlowberry", FFDItems.GLOW_BERRIES);
            registerOre("listAllfruit", FFDItems.GLOW_BERRIES);
            registerOre("listAllberry", FFDItems.GLOW_BERRIES);
        }
        if (FFDItems.isAzaleaEnabled()) {
            registerOre("listAllflower", FFDItems.FLOWERING_AZALEA);
        }
        if (FFDItems.isSporeBlossomEnabled()) {
            registerOre("listAllflower", FFDItems.SPORE_BLOSSOM);
        }
        if (FFDItems.isKelpEnabled()) {
            registerOre("cropKelp", FFDItems.KELP);
            registerOre("cropSeaweed", FFDItems.KELP);
            registerOre("listAllveggie", FFDItems.KELP);
            registerOre("foodKelp", FFDItems.DRIED_KELP);
            registerOre("foodSeaweed", FFDItems.DRIED_KELP);
            registerOre("listAllveggie", FFDItems.DRIED_KELP);
        }
        if (FFDItems.isSeagrassEnabled()) {
            registerOre("cropSeaweed", FFDItems.SEAGRASS);
        }
        if (FFDItems.isHoneyEnabled()) {
            registerOre("wax", FFDItems.HONEYCOMB);
        }
        if (FFDItems.isCrimsonWoodEnabled()) {
            registerNetherWoodOreDictionary(true);
        }
        if (FFDItems.isWarpedWoodEnabled()) {
            registerNetherWoodOreDictionary(false);
        }
        registerModernOreDictionary();
        registerCustomRawOreDictionary();
    }

    private static void addDeepslateItems(List<net.minecraft.item.Item> items) {
        items.add(FFDItems.DEEPSLATE);
        items.add(FFDItems.COBBLED_DEEPSLATE);
        items.add(FFDItems.POLISHED_DEEPSLATE);
        items.add(FFDItems.DEEPSLATE_BRICKS);
        items.add(FFDItems.CRACKED_DEEPSLATE_BRICKS);
        items.add(FFDItems.DEEPSLATE_TILES);
        items.add(FFDItems.CRACKED_DEEPSLATE_TILES);
        items.add(FFDItems.CHISELED_DEEPSLATE);
        items.add(FFDItems.TUFF);
        items.add(FFDItems.INFESTED_DEEPSLATE);
        items.add(FFDItems.COBBLED_DEEPSLATE_STAIRS);
        items.add(FFDItems.POLISHED_DEEPSLATE_STAIRS);
        items.add(FFDItems.DEEPSLATE_BRICK_STAIRS);
        items.add(FFDItems.DEEPSLATE_TILE_STAIRS);
        items.add(FFDItems.COBBLED_DEEPSLATE_SLAB);
        items.add(FFDItems.POLISHED_DEEPSLATE_SLAB);
        items.add(FFDItems.DEEPSLATE_BRICK_SLAB);
        items.add(FFDItems.DEEPSLATE_TILE_SLAB);
        items.add(FFDItems.COBBLED_DEEPSLATE_WALL);
        items.add(FFDItems.POLISHED_DEEPSLATE_WALL);
        items.add(FFDItems.DEEPSLATE_BRICK_WALL);
        items.add(FFDItems.DEEPSLATE_TILE_WALL);
        items.add(FFDItems.DEEPSLATE_COAL_ORE);
        items.add(FFDItems.DEEPSLATE_IRON_ORE);
        items.add(FFDItems.DEEPSLATE_GOLD_ORE);
        items.add(FFDItems.DEEPSLATE_REDSTONE_ORE);
        items.add(FFDItems.DEEPSLATE_LAPIS_ORE);
        items.add(FFDItems.DEEPSLATE_DIAMOND_ORE);
        items.add(FFDItems.DEEPSLATE_EMERALD_ORE);
    }

    private static void registerModernOreDictionary() {
        if (FFDItems.isAmethystEnabled()) {
            registerOre("gemAmethyst", FFDItems.AMETHYST_SHARD);
            registerOre("blockAmethyst", FFDItems.AMETHYST_BLOCK);
            registerOre("blockCalcite", FFDItems.CALCITE);
            registerOre("stoneCalcite", FFDItems.CALCITE);
        }
        if (FFDItems.isDeepslateEnabled()) {
            registerOre("blockDeepslate", FFDItems.DEEPSLATE);
            registerOre("stoneDeepslate", FFDItems.DEEPSLATE);
            registerOre("stone", FFDItems.DEEPSLATE);
            registerOre("cobblestoneDeepslate", FFDItems.COBBLED_DEEPSLATE);
            registerOre("cobblestone", FFDItems.COBBLED_DEEPSLATE);
            registerOre("blockTuff", FFDItems.TUFF);
            registerOre("stoneTuff", FFDItems.TUFF);
            registerOre("stone", FFDItems.TUFF);
            registerOre("oreCoal", FFDItems.DEEPSLATE_COAL_ORE);
            registerOre("oreIron", FFDItems.DEEPSLATE_IRON_ORE);
            registerOre("oreGold", FFDItems.DEEPSLATE_GOLD_ORE);
            registerOre("oreRedstone", FFDItems.DEEPSLATE_REDSTONE_ORE);
            registerOre("oreLapis", FFDItems.DEEPSLATE_LAPIS_ORE);
            registerOre("oreDiamond", FFDItems.DEEPSLATE_DIAMOND_ORE);
            registerOre("oreEmerald", FFDItems.DEEPSLATE_EMERALD_ORE);
        }
        if (FFDItems.isRawOreEnabled()) {
            registerRawOreDictionary(false);
        }
        if (FFDItems.isCopperEnabled()) {
            registerOre("oreCopper", FFDItems.COPPER_ORE);
            registerOre("ingotCopper", FFDItems.COPPER_INGOT);
            registerRawOreDictionary(true);
            registerOre("blockCopper", FFDItems.COPPER_BLOCK);
            registerOre("blockCopperCut", FFDItems.CUT_COPPER);
            if (FFDItems.isDeepslateEnabled()) {
                registerOre("oreCopper", FFDItems.DEEPSLATE_COPPER_ORE);
            }
        }
    }

    private static void registerNetherWoodOreDictionary(boolean crimson) {
        net.minecraft.item.Item stem = crimson ? FFDItems.CRIMSON_STEM : FFDItems.WARPED_STEM;
        net.minecraft.item.Item strippedStem = crimson ? FFDItems.STRIPPED_CRIMSON_STEM
                : FFDItems.STRIPPED_WARPED_STEM;
        net.minecraft.item.Item hyphae = crimson ? FFDItems.CRIMSON_HYPHAE : FFDItems.WARPED_HYPHAE;
        net.minecraft.item.Item strippedHyphae = crimson ? FFDItems.STRIPPED_CRIMSON_HYPHAE
                : FFDItems.STRIPPED_WARPED_HYPHAE;
        registerOre("logWood", stem);
        registerOre("logWood", strippedStem);
        registerOre("logWood", hyphae);
        registerOre("logWood", strippedHyphae);
        registerOre("plankWood", crimson ? FFDItems.CRIMSON_PLANKS : FFDItems.WARPED_PLANKS);
        registerOre("stairWood", crimson ? FFDItems.CRIMSON_STAIRS : FFDItems.WARPED_STAIRS);
        registerOre("slabWood", crimson ? FFDItems.CRIMSON_SLAB : FFDItems.WARPED_SLAB);
        registerOre("fenceWood", crimson ? FFDItems.CRIMSON_FENCE : FFDItems.WARPED_FENCE);
        registerOre("fenceGateWood",
                crimson ? FFDItems.CRIMSON_FENCE_GATE : FFDItems.WARPED_FENCE_GATE);
        registerOre("doorWood", crimson ? FFDItems.CRIMSON_DOOR : FFDItems.WARPED_DOOR);
        registerOre("trapdoorWood",
                crimson ? FFDItems.CRIMSON_TRAPDOOR : FFDItems.WARPED_TRAPDOOR);
        registerOre("buttonWood", crimson ? FFDItems.CRIMSON_BUTTON : FFDItems.WARPED_BUTTON);
        registerOre("pressurePlateWood",
                crimson ? FFDItems.CRIMSON_PRESSURE_PLATE : FFDItems.WARPED_PRESSURE_PLATE);
    }

    private static void registerOre(String name, net.minecraft.item.Item local) {
        ItemStack stack = FFDItems.effectiveStack(local);
        if (!stack.isEmpty()) {
            OreDictionary.registerOre(name, stack);
        }
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(FFDSounds.all());
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        FFDCompat.registerHoneycombCompatibility();
        FFDCompat.registerGlowInkCompatibility();
        List<IRecipe> recipes = new ArrayList<>();
        if (FFDItems.isMossEnabled()) {
            ResourceLocation mossGroup = new ResourceLocation(FarmerFutureDelight.MODID, "moss");
            ItemStack moss = FFDItems.effectiveStack(FFDItems.MOSS_BLOCK);
            if (!moss.isEmpty() && FFDItems.isItemRegistered(FFDItems.MOSS_CARPET)) {
                recipes.add(new ShapedOreRecipe(mossGroup,
                        new ItemStack(FFDItems.MOSS_CARPET, 3), "##", '#', moss)
                        .setRegistryName(FarmerFutureDelight.MODID, "moss_carpet"));
            }
            if (!moss.isEmpty()) {
                recipes.add(new ShapelessOreRecipe(mossGroup,
                        new ItemStack(net.minecraft.init.Blocks.MOSSY_COBBLESTONE),
                        net.minecraft.init.Blocks.COBBLESTONE, moss)
                        .setRegistryName(FarmerFutureDelight.MODID, "mossy_cobblestone"));
                recipes.add(new ShapelessOreRecipe(mossGroup,
                        new ItemStack(net.minecraft.init.Blocks.STONEBRICK, 1, 1),
                        new ItemStack(net.minecraft.init.Blocks.STONEBRICK), moss)
                        .setRegistryName(FarmerFutureDelight.MODID, "mossy_stone_bricks"));
            }
        }
        if (FFDItems.isAmethystEnabled()) {
            ResourceLocation amethystGroup = new ResourceLocation(FarmerFutureDelight.MODID, "amethyst");
            if (FFDItems.isItemRegistered(FFDItems.AMETHYST_BLOCK)) {
                recipes.add(new ShapedOreRecipe(amethystGroup,
                        new ItemStack(FFDItems.AMETHYST_BLOCK), "##", "##", '#', "gemAmethyst")
                        .setRegistryName(FarmerFutureDelight.MODID, "amethyst_block"));
            }
            if (FFDItems.isItemRegistered(FFDItems.TINTED_GLASS)) {
                recipes.add(new ShapedOreRecipe(amethystGroup,
                        new ItemStack(FFDItems.TINTED_GLASS, 2),
                        " S ", "SGS", " S ", 'S', "gemAmethyst",
                        'G', net.minecraft.init.Blocks.GLASS)
                        .setRegistryName(FarmerFutureDelight.MODID, "tinted_glass"));
            }
        }
        if (FFDItems.isDripstoneEnabled()) {
            ResourceLocation dripstoneGroup =
                    new ResourceLocation(FarmerFutureDelight.MODID, "dripstone");
            ItemStack pointed = FFDItems.effectiveStack(FFDItems.POINTED_DRIPSTONE);
            if (FFDItems.isItemRegistered(FFDItems.DRIPSTONE_BLOCK) && !pointed.isEmpty()) {
                recipes.add(new ShapedOreRecipe(dripstoneGroup,
                        new ItemStack(FFDItems.DRIPSTONE_BLOCK),
                        "##", "##", '#', pointed)
                        .setRegistryName(FarmerFutureDelight.MODID, "dripstone_block"));
            }
        }
        if (FFDItems.isItemRegistered(FFDItems.IRON_CHAIN)) {
            ResourceLocation chainGroup =
                    new ResourceLocation(FarmerFutureDelight.MODID, "iron_chain");
            recipes.add(new ShapedOreRecipe(chainGroup, new ItemStack(FFDItems.IRON_CHAIN),
                    "N", "I", "N", 'N', Items.IRON_NUGGET, 'I', Items.IRON_INGOT)
                    .setRegistryName(FarmerFutureDelight.MODID, "iron_chain"));
        }
        if (FFDItems.isCandleEnabled()) {
            addCandleRecipes(recipes);
        }
        if (FFDItems.isItemRegistered(FFDItems.GLOW_ITEM_FRAME)
                && FFDCompat.hasCompatibleGlowInkSac()) {
            ResourceLocation frameGroup = new ResourceLocation(
                    FarmerFutureDelight.MODID, "glow_item_frame");
            recipes.add(new ShapelessOreRecipe(frameGroup,
                    new ItemStack(FFDItems.GLOW_ITEM_FRAME), Items.ITEM_FRAME,
                    FFDCompat.GLOW_INK_SAC_ORE_DICTIONARY)
                    .setRegistryName(FarmerFutureDelight.MODID, "glow_item_frame"));
        }
        if (FFDItems.isDeepslateEnabled()) {
            addDeepslateRecipes(recipes);
        }
        if (FFDItems.isRawOreEnabled()) {
            addRawMaterialRecipes(recipes, false);
        }
        if (FFDItems.isCopperEnabled()) {
            addRawMaterialRecipes(recipes, true);
            addCopperRecipes(recipes);
        }
        addCustomRawMaterialRecipes(recipes);
        if (FFDItems.isKelpEnabled()) {
            ResourceLocation kelpGroup = new ResourceLocation(FarmerFutureDelight.MODID, "kelp");
            ItemStack dried = FFDItems.effectiveStack(FFDItems.DRIED_KELP);
            ItemStack driedBlock = FFDItems.effectiveStack(FFDItems.DRIED_KELP_BLOCK);
            if (FFDItems.isItemRegistered(FFDItems.DRIED_KELP_BLOCK) && !dried.isEmpty()) {
                recipes.add(new ShapedOreRecipe(kelpGroup,
                        new ItemStack(FFDItems.DRIED_KELP_BLOCK),
                        "###", "###", "###", '#', dried)
                        .setRegistryName(FarmerFutureDelight.MODID, "dried_kelp_block"));
            }
            if (FFDItems.isItemRegistered(FFDItems.DRIED_KELP) && !driedBlock.isEmpty()) {
                recipes.add(new ShapelessOreRecipe(kelpGroup,
                        new ItemStack(FFDItems.DRIED_KELP, 9), driedBlock)
                        .setRegistryName(FarmerFutureDelight.MODID, "dried_kelp"));
            }
        }
        if (FFDItems.isHoneyEnabled()) {
            ResourceLocation honeyGroup = new ResourceLocation(FarmerFutureDelight.MODID, "honey");
            ItemStack bottle = FFDItems.effectiveStack(FFDItems.HONEY_BOTTLE);
            ItemStack honeyBlock = FFDItems.effectiveStack(FFDItems.HONEY_BLOCK);
            ItemStack honeycomb = FFDItems.effectiveStack(FFDItems.HONEYCOMB);
            if (FFDItems.isItemRegistered(FFDItems.HONEY_BLOCK) && !bottle.isEmpty()) {
                recipes.add(new ShapedOreRecipe(honeyGroup, new ItemStack(FFDItems.HONEY_BLOCK),
                        "##", "##", '#', bottle)
                        .setRegistryName(FarmerFutureDelight.MODID, "honey_block"));
            }
            if (FFDItems.isItemRegistered(FFDItems.HONEY_BOTTLE) && !honeyBlock.isEmpty()) {
                recipes.add(new ShapelessOreRecipe(honeyGroup,
                        new ItemStack(FFDItems.HONEY_BOTTLE, 4), honeyBlock,
                        Items.GLASS_BOTTLE, Items.GLASS_BOTTLE,
                        Items.GLASS_BOTTLE, Items.GLASS_BOTTLE)
                        .setRegistryName(FarmerFutureDelight.MODID, "honey_bottles"));
            }
            if (FFDItems.isItemRegistered(FFDItems.HONEYCOMB_BLOCK) && !honeycomb.isEmpty()) {
                recipes.add(new ShapedOreRecipe(honeyGroup,
                        new ItemStack(FFDItems.HONEYCOMB_BLOCK),
                        "##", "##", '#', honeycomb)
                        .setRegistryName(FarmerFutureDelight.MODID, "honeycomb_block"));
            }
            if (!bottle.isEmpty()) {
                recipes.add(new ShapelessOreRecipe(honeyGroup, new ItemStack(Items.SUGAR, 3),
                        bottle).setRegistryName(FarmerFutureDelight.MODID,
                        "sugar_from_honey_bottle"));
            }
            if (FFDItems.isItemRegistered(FFDItems.BEEHIVE) && !honeycomb.isEmpty()) {
                recipes.add(new ShapedOreRecipe(honeyGroup, new ItemStack(FFDItems.BEEHIVE),
                        "PPP", "HHH", "PPP", 'P', "plankWood", 'H', honeycomb)
                        .setRegistryName(FarmerFutureDelight.MODID, "beehive"));
            }
        }
        if (FFDItems.isItemRegistered(FFDItems.TURTLE_HELMET)) {
            ResourceLocation turtleGroup = new ResourceLocation(FarmerFutureDelight.MODID, "turtle_helmet");
            ItemStack scute = FFDItems.effectiveStack(FFDItems.TURTLE_SCUTE);
            if (!scute.isEmpty()) {
                recipes.add(new ShapedOreRecipe(turtleGroup,
                        new ItemStack(FFDItems.TURTLE_HELMET),
                        "XXX", "X X", 'X', scute)
                        .setRegistryName(FarmerFutureDelight.MODID, "turtle_helmet"));
            }
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

    private static void addCandleRecipes(List<IRecipe> recipes) {
        ResourceLocation group = new ResourceLocation(FarmerFutureDelight.MODID, "candle");
        if (FFDItems.isItemRegistered(FFDItems.CANDLE)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(FFDItems.CANDLE),
                    "S", "H", 'S', Items.STRING,
                    'H', FFDCompat.HONEYCOMB_ORE_DICTIONARY)
                    .setRegistryName(FarmerFutureDelight.MODID, "candle"));
        }

        EnumDyeColor[] colors = {
                EnumDyeColor.WHITE, EnumDyeColor.ORANGE, EnumDyeColor.MAGENTA,
                EnumDyeColor.LIGHT_BLUE, EnumDyeColor.YELLOW, EnumDyeColor.LIME,
                EnumDyeColor.PINK, EnumDyeColor.GRAY, EnumDyeColor.SILVER,
                EnumDyeColor.CYAN, EnumDyeColor.PURPLE, EnumDyeColor.BLUE,
                EnumDyeColor.BROWN, EnumDyeColor.GREEN, EnumDyeColor.RED,
                EnumDyeColor.BLACK
        };
        ItemStack candle = FFDItems.effectiveStack(FFDItems.CANDLE);
        for (int i = 1; i < FFDItems.CANDLE_ITEMS.length; i++) {
            if (!FFDItems.isItemRegistered(FFDItems.CANDLE_ITEMS[i]) || candle.isEmpty()) {
                continue;
            }
            recipes.add(new ShapelessOreRecipe(group,
                    new ItemStack(FFDItems.CANDLE_ITEMS[i]), candle,
                    new ItemStack(Items.DYE, 1, colors[i - 1].getDyeDamage()))
                    .setRegistryName(FarmerFutureDelight.MODID, FFDBlocks.CANDLE_NAMES[i]));
        }
    }

    private static void addDeepslateRecipes(List<IRecipe> recipes) {
        ResourceLocation group = new ResourceLocation(FarmerFutureDelight.MODID, "deepslate");
        addTwoByTwoRecipe(recipes, group, "polished_deepslate", FFDItems.COBBLED_DEEPSLATE,
                FFDItems.POLISHED_DEEPSLATE);
        addTwoByTwoRecipe(recipes, group, "deepslate_bricks", FFDItems.POLISHED_DEEPSLATE,
                FFDItems.DEEPSLATE_BRICKS);
        addTwoByTwoRecipe(recipes, group, "deepslate_tiles", FFDItems.DEEPSLATE_BRICKS,
                FFDItems.DEEPSLATE_TILES);
        ItemStack cobbledSlab = FFDItems.effectiveStack(FFDItems.COBBLED_DEEPSLATE_SLAB);
        if (FFDItems.isItemRegistered(FFDItems.CHISELED_DEEPSLATE)
                && !cobbledSlab.isEmpty()) {
            recipes.add(new ShapedOreRecipe(group,
                    new ItemStack(FFDItems.CHISELED_DEEPSLATE),
                    "S", "S", 'S', cobbledSlab)
                    .setRegistryName(FarmerFutureDelight.MODID, "chiseled_deepslate"));
        }
        addStoneFamilyRecipes(recipes, group, "cobbled_deepslate", FFDItems.COBBLED_DEEPSLATE,
                FFDItems.COBBLED_DEEPSLATE_STAIRS, FFDItems.COBBLED_DEEPSLATE_SLAB,
                FFDItems.COBBLED_DEEPSLATE_WALL);
        addStoneFamilyRecipes(recipes, group, "polished_deepslate", FFDItems.POLISHED_DEEPSLATE,
                FFDItems.POLISHED_DEEPSLATE_STAIRS, FFDItems.POLISHED_DEEPSLATE_SLAB,
                FFDItems.POLISHED_DEEPSLATE_WALL);
        addStoneFamilyRecipes(recipes, group, "deepslate_brick", FFDItems.DEEPSLATE_BRICKS,
                FFDItems.DEEPSLATE_BRICK_STAIRS, FFDItems.DEEPSLATE_BRICK_SLAB,
                FFDItems.DEEPSLATE_BRICK_WALL);
        addStoneFamilyRecipes(recipes, group, "deepslate_tile", FFDItems.DEEPSLATE_TILES,
                FFDItems.DEEPSLATE_TILE_STAIRS, FFDItems.DEEPSLATE_TILE_SLAB,
                FFDItems.DEEPSLATE_TILE_WALL);
    }

    private static void addTwoByTwoRecipe(List<IRecipe> recipes, ResourceLocation group,
                                           String name, net.minecraft.item.Item input,
                                           net.minecraft.item.Item output) {
        ItemStack effectiveInput = FFDItems.effectiveStack(input);
        if (FFDItems.isItemRegistered(output) && !effectiveInput.isEmpty()) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(output, 4),
                    "##", "##", '#', effectiveInput)
                    .setRegistryName(FarmerFutureDelight.MODID, name));
        }
    }

    private static void addStoneFamilyRecipes(List<IRecipe> recipes, ResourceLocation group,
                                               String name, net.minecraft.item.Item material,
                                               net.minecraft.item.Item stairs,
                                               net.minecraft.item.Item slab,
                                               net.minecraft.item.Item wall) {
        ItemStack effectiveMaterial = FFDItems.effectiveStack(material);
        if (effectiveMaterial.isEmpty()) {
            return;
        }
        if (FFDItems.isItemRegistered(stairs)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(stairs, 4),
                    "#  ", "## ", "###", '#', effectiveMaterial)
                    .setRegistryName(FarmerFutureDelight.MODID, name + "_stairs"));
        }
        if (FFDItems.isItemRegistered(slab)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(slab, 6),
                    "###", '#', effectiveMaterial)
                    .setRegistryName(FarmerFutureDelight.MODID, name + "_slab"));
        }
        if (FFDItems.isItemRegistered(wall)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(wall, 6),
                    "###", "###", '#', effectiveMaterial)
                    .setRegistryName(FarmerFutureDelight.MODID, name + "_wall"));
        }
    }

    private static void addRawMaterialRecipes(List<IRecipe> recipes, boolean copperOnly) {
        ResourceLocation group = new ResourceLocation(FarmerFutureDelight.MODID, "raw_materials");
        for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
            if (FFDRawOres.isCopper(FFDRawOres.NAMES[i]) != copperOnly) {
                continue;
            }
            net.minecraft.item.Item raw = FFDItems.RAW_ORE_ITEMS[i];
            net.minecraft.item.Item block = FFDItems.RAW_ORE_BLOCK_ITEMS[i];
            ItemStack effectiveRaw = FFDItems.effectiveStack(raw);
            ItemStack effectiveBlock = FFDItems.effectiveStack(block);
            if (FFDItems.isItemRegistered(block) && !effectiveRaw.isEmpty()) {
                recipes.add(new ShapedOreRecipe(group, new ItemStack(block),
                        "###", "###", "###", '#', effectiveRaw)
                        .setRegistryName(FarmerFutureDelight.MODID,
                                FFDRawOres.rawBlockName(FFDRawOres.NAMES[i])));
            }
            if (FFDItems.isItemRegistered(raw) && !effectiveBlock.isEmpty()) {
                recipes.add(new ShapelessOreRecipe(group, new ItemStack(raw, 9), effectiveBlock)
                        .setRegistryName(FarmerFutureDelight.MODID,
                                FFDRawOres.rawItemName(FFDRawOres.NAMES[i])));
            }
        }
    }

    private static void addCustomRawMaterialRecipes(List<IRecipe> recipes) {
        ResourceLocation group = new ResourceLocation(FarmerFutureDelight.MODID,
                "custom_raw_materials");
        for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
            ItemStack raw = entry.rawStack();
            ItemStack block = entry.blockStack();
            if (raw.isEmpty() || block.isEmpty()) {
                continue;
            }
            recipes.add(new ShapedOreRecipe(group, block.copy(),
                    "###", "###", "###", '#', raw.copy())
                    .setRegistryName(FarmerFutureDelight.MODID,
                            "custom_raw_" + entry.material() + "_block"));
            recipes.add(new ShapelessOreRecipe(group, copyWithCount(raw, 9), block.copy())
                    .setRegistryName(FarmerFutureDelight.MODID,
                            "custom_raw_" + entry.material()));
        }
    }

    private static ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static void registerCustomRawOreDictionary() {
        for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
            ItemStack raw = entry.rawStack();
            ItemStack block = entry.blockStack();
            if (!raw.isEmpty()) {
                OreDictionary.registerOre(entry.rawOreName(), raw);
            }
            if (!block.isEmpty()) {
                OreDictionary.registerOre(entry.rawBlockOreName(), block);
            }
        }
    }

    private static void addRawOreBlocks(List<net.minecraft.block.Block> blocks,
                                        boolean copperOnly) {
        for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
            String material = FFDRawOres.NAMES[i];
            if (FFDItems.isRawOreMaterialEnabled(material)
                    && FFDRawOres.isCopper(material) == copperOnly
                    && (copperOnly
                    || FFDRawOreOreDictionaryCompat.hasSourceOre(material))) {
                blocks.add(FFDBlocks.RAW_ORE_BLOCKS[i]);
            }
        }
    }

    private static void addRawOreItems(List<net.minecraft.item.Item> items,
                                       boolean copperOnly) {
        for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
            String material = FFDRawOres.NAMES[i];
            if (FFDItems.isRawOreMaterialEnabled(material)
                    && FFDRawOres.isCopper(material) == copperOnly
                    && (copperOnly
                    || FFDRawOreOreDictionaryCompat.hasSourceOre(material))) {
                items.add(FFDItems.RAW_ORE_ITEMS[i]);
                items.add(FFDItems.RAW_ORE_BLOCK_ITEMS[i]);
            }
        }
    }

    private static void registerRawOreDictionary(boolean copperOnly) {
        for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
            if (FFDRawOres.isCopper(FFDRawOres.NAMES[i]) != copperOnly) {
                continue;
            }
            if (FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[i])) {
                registerOre(FFDRawOres.rawOreName(FFDRawOres.NAMES[i]),
                        FFDItems.RAW_ORE_ITEMS[i]);
            }
            if (FFDItems.isItemRegistered(FFDItems.RAW_ORE_BLOCK_ITEMS[i])) {
                registerOre(FFDRawOres.rawBlockOreName(FFDRawOres.NAMES[i]),
                        FFDItems.RAW_ORE_BLOCK_ITEMS[i]);
            }
        }
    }

    private static void addCopperRecipes(List<IRecipe> recipes) {
        ResourceLocation group = new ResourceLocation(FarmerFutureDelight.MODID, "copper");
        if (FFDItems.isItemRegistered(FFDItems.COPPER_BLOCK)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(FFDItems.COPPER_BLOCK),
                    "###", "###", "###", '#', "ingotCopper")
                    .setRegistryName(FarmerFutureDelight.MODID, "copper_block"));
        }
        ItemStack copperBlock = FFDItems.effectiveStack(FFDItems.COPPER_BLOCK);
        ItemStack waxedCopperBlock = FFDItems.effectiveStack(
                FFDItems.WAXED_COPPER_BLOCK_ITEMS[0]);
        if (FFDItems.isItemRegistered(FFDItems.COPPER_INGOT) && !copperBlock.isEmpty()) {
            recipes.add(new ShapelessOreRecipe(group,
                    new ItemStack(FFDItems.COPPER_INGOT, 9), copperBlock)
                    .setRegistryName(FarmerFutureDelight.MODID, "copper_ingot"));
        }
        if (FFDItems.isItemRegistered(FFDItems.COPPER_INGOT)
                && !waxedCopperBlock.isEmpty()) {
            recipes.add(new ShapelessOreRecipe(group,
                    new ItemStack(FFDItems.COPPER_INGOT, 9), waxedCopperBlock)
                    .setRegistryName(FarmerFutureDelight.MODID,
                            "copper_ingot_from_waxed_copper_block"));
        }

        for (int i = 0; i < FFDItems.COPPER_BLOCK_ITEMS.length; i++) {
            addCutCopperRecipes(recipes, group, FFDItems.COPPER_BLOCK_ITEMS[i],
                    FFDItems.CUT_COPPER_ITEMS[i], FFDItems.CUT_COPPER_STAIR_ITEMS[i],
                    FFDItems.CUT_COPPER_SLAB_ITEMS[i]);
            addCutCopperRecipes(recipes, group, FFDItems.WAXED_COPPER_BLOCK_ITEMS[i],
                    FFDItems.WAXED_CUT_COPPER_ITEMS[i],
                    FFDItems.WAXED_CUT_COPPER_STAIR_ITEMS[i],
                    FFDItems.WAXED_CUT_COPPER_SLAB_ITEMS[i]);
        }

        if (FFDItems.isItemRegistered(FFDItems.LIGHTNING_ROD)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(FFDItems.LIGHTNING_ROD),
                    "#", "#", "#", '#', "ingotCopper")
                    .setRegistryName(FarmerFutureDelight.MODID, "lightning_rod"));
        }
        if (FFDItems.isItemRegistered(FFDItems.SPYGLASS)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(FFDItems.SPYGLASS),
                    " A ", " C ", " C ", 'A', "gemAmethyst", 'C', "ingotCopper")
                    .setRegistryName(FarmerFutureDelight.MODID, "spyglass"));
        }

        if (FFDCompat.hasCompatibleHoneycomb()) {
            addWaxingRecipes(recipes, group, FFDItems.COPPER_BLOCK_ITEMS,
                    FFDItems.WAXED_COPPER_BLOCK_ITEMS);
            addWaxingRecipes(recipes, group, FFDItems.CUT_COPPER_ITEMS,
                    FFDItems.WAXED_CUT_COPPER_ITEMS);
            addWaxingRecipes(recipes, group, FFDItems.CUT_COPPER_STAIR_ITEMS,
                    FFDItems.WAXED_CUT_COPPER_STAIR_ITEMS);
            addWaxingRecipes(recipes, group, FFDItems.CUT_COPPER_SLAB_ITEMS,
                    FFDItems.WAXED_CUT_COPPER_SLAB_ITEMS);
            addWaxingRecipes(recipes, group, FFDItems.LIGHTNING_ROD_ITEMS,
                    FFDItems.WAXED_LIGHTNING_ROD_ITEMS);
        }
    }

    private static void addCutCopperRecipes(List<IRecipe> recipes, ResourceLocation group,
                                             net.minecraft.item.Item block,
                                             net.minecraft.item.Item cut,
                                             net.minecraft.item.Item stairs,
                                             net.minecraft.item.Item slab) {
        ItemStack effectiveBlock = FFDItems.effectiveStack(block);
        ItemStack effectiveCut = FFDItems.effectiveStack(cut);
        if (FFDItems.isItemRegistered(cut) && !effectiveBlock.isEmpty()) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(cut, 4),
                    "##", "##", '#', effectiveBlock)
                    .setRegistryName(FarmerFutureDelight.MODID,
                            cut.getRegistryName().getResourcePath()));
        }
        if (FFDItems.isItemRegistered(stairs) && !effectiveCut.isEmpty()) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(stairs, 4),
                    "#  ", "## ", "###", '#', effectiveCut)
                    .setRegistryName(FarmerFutureDelight.MODID,
                            stairs.getRegistryName().getResourcePath()));
        }
        if (FFDItems.isItemRegistered(slab) && !effectiveCut.isEmpty()) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(slab, 6),
                    "###", '#', effectiveCut)
                    .setRegistryName(FarmerFutureDelight.MODID,
                            slab.getRegistryName().getResourcePath()));
        }
    }

    private static void addWaxingRecipes(List<IRecipe> recipes, ResourceLocation group,
                                          net.minecraft.item.Item[] unwaxed,
                                          net.minecraft.item.Item[] waxed) {
        for (int i = 0; i < unwaxed.length; i++) {
            ItemStack effectiveUnwaxed = FFDItems.effectiveStack(unwaxed[i]);
            if (FFDItems.isItemRegistered(waxed[i]) && !effectiveUnwaxed.isEmpty()) {
                recipes.add(new ShapelessOreRecipe(group, new ItemStack(waxed[i]),
                        effectiveUnwaxed, FFDCompat.HONEYCOMB_ORE_DICTIONARY)
                        .setRegistryName(FarmerFutureDelight.MODID,
                                waxed[i].getRegistryName().getResourcePath()
                                        + "_from_honeycomb"));
            }
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
        ItemStack stemStack = FFDItems.effectiveStack(stem);
        ItemStack strippedStemStack = FFDItems.effectiveStack(strippedStem);
        ItemStack hyphaeStack = FFDItems.effectiveStack(hyphae);
        ItemStack strippedHyphaeStack = FFDItems.effectiveStack(strippedHyphae);
        ItemStack plankStack = FFDItems.effectiveStack(planks);
        if (FFDItems.isItemRegistered(planks)) {
            addShapelessConversion(recipes, group, planks, 4, stemStack,
                    prefix + "_planks_from_stem");
            addShapelessConversion(recipes, group, planks, 4, strippedStemStack,
                    prefix + "_planks_from_stripped_stem");
            addShapelessConversion(recipes, group, planks, 4, hyphaeStack,
                    prefix + "_planks_from_hyphae");
            addShapelessConversion(recipes, group, planks, 4, strippedHyphaeStack,
                    prefix + "_planks_from_stripped_hyphae");
        }
        if (FFDItems.isItemRegistered(hyphae) && !stemStack.isEmpty()) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(hyphae, 3),
                    "SS", "SS", 'S', stemStack)
                    .setRegistryName(FarmerFutureDelight.MODID, prefix + "_hyphae"));
        }
        if (FFDItems.isItemRegistered(strippedHyphae) && !strippedStemStack.isEmpty()) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(strippedHyphae, 3),
                    "SS", "SS", 'S', strippedStemStack)
                    .setRegistryName(FarmerFutureDelight.MODID,
                            "stripped_" + prefix + "_hyphae"));
        }
        if (plankStack.isEmpty()) {
            return;
        }
        recipes.add(new ShapedOreRecipe(group, new ItemStack(Items.STICK, 4),
                "P", "P", 'P', plankStack)
                .setRegistryName(FarmerFutureDelight.MODID, prefix + "_sticks"));
        addWoodShapeRecipes(recipes, group, prefix, plankStack, stairs, slab, fence,
                fenceGate, door, trapdoor, button, pressurePlate);
    }

    private static void addShapelessConversion(List<IRecipe> recipes, ResourceLocation group,
                                                net.minecraft.item.Item output, int count,
                                                ItemStack input, String name) {
        if (!input.isEmpty()) {
            recipes.add(new ShapelessOreRecipe(group, new ItemStack(output, count), input)
                    .setRegistryName(FarmerFutureDelight.MODID, name));
        }
    }

    private static void addWoodShapeRecipes(List<IRecipe> recipes, ResourceLocation group,
                                            String prefix, ItemStack planks,
                                            net.minecraft.item.Item stairs,
                                            net.minecraft.item.Item slab,
                                            net.minecraft.item.Item fence,
                                            net.minecraft.item.Item fenceGate,
                                            net.minecraft.item.Item door,
                                            net.minecraft.item.Item trapdoor,
                                            net.minecraft.item.Item button,
                                            net.minecraft.item.Item pressurePlate) {
        if (FFDItems.isItemRegistered(stairs)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(stairs, 4),
                    "P  ", "PP ", "PPP", 'P', planks)
                    .setRegistryName(FarmerFutureDelight.MODID, prefix + "_stairs"));
        }
        if (FFDItems.isItemRegistered(slab)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(slab, 6),
                    "PPP", 'P', planks)
                    .setRegistryName(FarmerFutureDelight.MODID, prefix + "_slab"));
        }
        if (FFDItems.isItemRegistered(fence)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(fence, 3),
                    "PSP", "PSP", 'P', planks, 'S', Items.STICK)
                    .setRegistryName(FarmerFutureDelight.MODID, prefix + "_fence"));
        }
        if (FFDItems.isItemRegistered(fenceGate)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(fenceGate),
                    "SPS", "SPS", 'P', planks, 'S', Items.STICK)
                    .setRegistryName(FarmerFutureDelight.MODID, prefix + "_fence_gate"));
        }
        if (FFDItems.isItemRegistered(door)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(door, 3),
                    "PP", "PP", "PP", 'P', planks)
                    .setRegistryName(FarmerFutureDelight.MODID, prefix + "_door"));
        }
        if (FFDItems.isItemRegistered(trapdoor)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(trapdoor, 2),
                    "PPP", "PPP", 'P', planks)
                    .setRegistryName(FarmerFutureDelight.MODID, prefix + "_trapdoor"));
        }
        if (FFDItems.isItemRegistered(button)) {
            recipes.add(new ShapelessOreRecipe(group, new ItemStack(button), planks)
                    .setRegistryName(FarmerFutureDelight.MODID, prefix + "_button"));
        }
        if (FFDItems.isItemRegistered(pressurePlate)) {
            recipes.add(new ShapedOreRecipe(group, new ItemStack(pressurePlate),
                    "PP", 'P', planks)
                    .setRegistryName(FarmerFutureDelight.MODID,
                            prefix + "_pressure_plate"));
        }
    }
}
