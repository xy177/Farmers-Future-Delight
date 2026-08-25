package xy177.farmersfuturedelight.proxy;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.particle.ParticleGlowSquid;
import xy177.farmersfuturedelight.client.particle.ParticleDripstone;
import xy177.farmersfuturedelight.client.particle.ParticleFallingNectar;
import xy177.farmersfuturedelight.client.particle.ParticleHoneyDrip;
import xy177.farmersfuturedelight.client.particle.ParticleSmallFlame;
import xy177.farmersfuturedelight.client.particle.ParticleSporeBlossom;
import xy177.farmersfuturedelight.client.render.RenderBee;
import xy177.farmersfuturedelight.client.render.RenderAxolotl;
import xy177.farmersfuturedelight.client.render.RenderGlowSquid;
import xy177.farmersfuturedelight.client.render.RenderGlowItemFrame;
import xy177.farmersfuturedelight.client.render.RenderGoat;
import xy177.farmersfuturedelight.client.render.RenderPhantom;
import xy177.farmersfuturedelight.client.render.RenderTurtle;
import xy177.farmersfuturedelight.client.render.RenderSign;
import xy177.farmersfuturedelight.common.entity.EntityBee;
import xy177.farmersfuturedelight.common.entity.EntityAxolotl;
import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;
import xy177.farmersfuturedelight.common.entity.EntityGlowItemFrame;
import xy177.farmersfuturedelight.common.entity.EntityGoat;
import xy177.farmersfuturedelight.common.entity.EntityPhantom;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;
import net.minecraft.tileentity.TileEntitySign;
import xy177.farmersfuturedelight.common.block.BlockKelp;
import xy177.farmersfuturedelight.common.block.BlockKelpYoung;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.BlockAmethystCluster;
import xy177.farmersfuturedelight.common.block.BlockSmallDripleaf;
import xy177.farmersfuturedelight.common.block.BlockHangingRoots;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.item.ItemGoatHorn;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public AxisAlignedBB getLightSelectionBox(net.minecraft.world.IBlockAccess world, BlockPos pos) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (player != null && FFDItems.isLightEnabled()
                && (player.getHeldItemMainhand().getItem() == FFDItems.LIGHT
                || player.getHeldItemOffhand().getItem() == FFDItems.LIGHT)) {
            return net.minecraft.block.Block.FULL_BLOCK_AABB;
        }
        return net.minecraft.block.Block.NULL_AABB;
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        registerModel(FFDItems.SWEET_BERRIES, "sweet_berries");
        registerModel(FFDItems.MOSS_BLOCK, "moss_block");
        registerModel(FFDItems.MOSS_CARPET, "moss_carpet");
        registerModel(FFDItems.GLOW_BERRIES, "glow_berries");
        registerModel(FFDItems.AZALEA, "azalea");
        registerModel(FFDItems.FLOWERING_AZALEA, "flowering_azalea");
        registerModel(FFDItems.AZALEA_LEAVES, "azalea_leaves");
        registerModel(FFDItems.FLOWERING_AZALEA_LEAVES, "flowering_azalea_leaves");
        registerModel(FFDItems.SMALL_DRIPLEAF, "small_dripleaf");
        registerModel(FFDItems.BIG_DRIPLEAF, "big_dripleaf");
        registerStateMapper(FFDBlocks.HANGING_ROOTS,
                new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockHangingRoots.WATERLOGGED).build());
        registerModel(FFDItems.ROOTED_DIRT, "rooted_dirt");
        registerModel(FFDItems.HANGING_ROOTS, "hanging_roots_item");
        registerModel(FFDItems.SPORE_BLOSSOM, "spore_blossom");
        if (FFDItems.isGlowLichenEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.GLOW_LICHEN, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":glow_lichen_item", "inventory"));
            for (net.minecraft.block.Block block : FFDBlocks.GLOW_LICHEN_VARIANTS) {
                registerStateMapper(block,
                        new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
            }
        }
        if (FFDEntities.isGlowSquidEnabled()) {
            registerModel(FFDItems.GLOW_INK_SAC, "glow_ink_sac");
        }
        if (FFDItems.isGlowItemFrameEnabled()) {
            registerModel(FFDItems.GLOW_ITEM_FRAME, "glow_item_frame");
        }
        if (FFDItems.isKelpEnabled()) {
            registerModel(FFDItems.KELP, "kelp");
            registerModel(FFDItems.DRIED_KELP, "dried_kelp");
            registerModel(FFDItems.DRIED_KELP_BLOCK, "dried_kelp_block");
        }
        if (FFDItems.isSeagrassEnabled()) {
            registerModel(FFDItems.SEAGRASS, "seagrass");
        }
        if (FFDItems.isSeaPickleEnabled()) {
            registerModel(FFDItems.SEA_PICKLE, "sea_pickle");
        }
        if (FFDItems.isTurtleEnabled()) {
            registerModel(FFDItems.TURTLE_EGG, "turtle_egg");
            registerModel(FFDItems.TURTLE_SCUTE, "turtle_scute");
            registerModel(FFDItems.TURTLE_HELMET, "turtle_helmet");
        }
        if (FFDItems.isAxolotlEnabled()) {
            registerModel(FFDItems.AXOLOTL_BUCKET, "axolotl_bucket");
        }
        if (FFDItems.isItemRegistered(FFDItems.GOAT_HORN)) {
            for (int instrument = 0; instrument < ItemGoatHorn.INSTRUMENT_COUNT; instrument++) {
                ModelLoader.setCustomModelResourceLocation(FFDItems.GOAT_HORN, instrument,
                        new ModelResourceLocation(FarmerFutureDelight.MODID + ":goat_horn", "inventory"));
            }
            ModelBakery.registerItemVariants(FFDItems.GOAT_HORN,
                    new ResourceLocation(FarmerFutureDelight.MODID, "goat_horn"),
                    new ResourceLocation(FarmerFutureDelight.MODID, "tooting_goat_horn"));
        }
        if (FFDItems.isPhantomEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.PHANTOM_MEMBRANE, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":phantom_membrane", "inventory"));
        }
        if (FFDItems.isOthersideEnabled()) {
            registerModel(FFDItems.MUSIC_DISC_OTHERSIDE, "music_disc_otherside");
        }
        if (FFDItems.isAmethystEnabled()) {
            registerModel(FFDItems.AMETHYST_BLOCK, "amethyst_block");
            registerModel(FFDItems.BUDDING_AMETHYST, "budding_amethyst");
            registerModel(FFDItems.SMALL_AMETHYST_BUD, "small_amethyst_bud");
            registerModel(FFDItems.MEDIUM_AMETHYST_BUD, "medium_amethyst_bud");
            registerModel(FFDItems.LARGE_AMETHYST_BUD, "large_amethyst_bud");
            registerModel(FFDItems.AMETHYST_CLUSTER, "amethyst_cluster");
            registerModel(FFDItems.CALCITE, "calcite");
            registerModel(FFDItems.SMOOTH_BASALT, "smooth_basalt");
            registerModel(FFDItems.TINTED_GLASS, "tinted_glass");
            registerModel(FFDItems.AMETHYST_SHARD, "amethyst_shard");
            registerStateMapper(FFDBlocks.SMALL_AMETHYST_BUD,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL,
                            BlockAmethystCluster.WATERLOGGED).build());
            registerStateMapper(FFDBlocks.MEDIUM_AMETHYST_BUD,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL,
                            BlockAmethystCluster.WATERLOGGED).build());
            registerStateMapper(FFDBlocks.LARGE_AMETHYST_BUD,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL,
                            BlockAmethystCluster.WATERLOGGED).build());
            registerStateMapper(FFDBlocks.AMETHYST_CLUSTER,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL,
                            BlockAmethystCluster.WATERLOGGED).build());
        }
        if (FFDItems.isDripstoneEnabled()) {
            registerModel(FFDItems.DRIPSTONE_BLOCK, "dripstone_block");
            registerModel(FFDItems.POINTED_DRIPSTONE, "pointed_dripstone");
        }
        if (FFDItems.isIronChainEnabled()) {
            registerModel(FFDItems.IRON_CHAIN, "iron_chain");
        }
        if (FFDItems.isLightEnabled()) {
            for (int level = 0; level <= 15; level++) {
                String suffix = level < 10 ? "0" + level : Integer.toString(level);
                ModelLoader.setCustomModelResourceLocation(FFDItems.LIGHT, level,
                        new ModelResourceLocation(FarmerFutureDelight.MODID + ":light_" + suffix,
                                "inventory"));
            }
        }
        if (FFDItems.isCandleEnabled()) {
            registerModels(FFDItems.CANDLE_ITEMS);
        }
        if (FFDItems.isPowderSnowEnabled()) {
            registerModel(FFDItems.POWDER_SNOW, "powder_snow");
            registerModel(FFDItems.POWDER_SNOW_BUCKET, "powder_snow_bucket");
        }
        if (FFDItems.isDeepslateEnabled()) {
            registerModel(FFDItems.DEEPSLATE, "deepslate");
            registerModel(FFDItems.COBBLED_DEEPSLATE, "cobbled_deepslate");
            registerModel(FFDItems.POLISHED_DEEPSLATE, "polished_deepslate");
            registerModel(FFDItems.DEEPSLATE_BRICKS, "deepslate_bricks");
            registerModel(FFDItems.CRACKED_DEEPSLATE_BRICKS, "cracked_deepslate_bricks");
            registerModel(FFDItems.DEEPSLATE_TILES, "deepslate_tiles");
            registerModel(FFDItems.CRACKED_DEEPSLATE_TILES, "cracked_deepslate_tiles");
            registerModel(FFDItems.CHISELED_DEEPSLATE, "chiseled_deepslate");
            registerModel(FFDItems.TUFF, "tuff");
            registerModel(FFDItems.INFESTED_DEEPSLATE, "infested_deepslate");
            registerModel(FFDItems.COBBLED_DEEPSLATE_STAIRS, "cobbled_deepslate_stairs");
            registerModel(FFDItems.POLISHED_DEEPSLATE_STAIRS, "polished_deepslate_stairs");
            registerModel(FFDItems.DEEPSLATE_BRICK_STAIRS, "deepslate_brick_stairs");
            registerModel(FFDItems.DEEPSLATE_TILE_STAIRS, "deepslate_tile_stairs");
            registerModel(FFDItems.COBBLED_DEEPSLATE_SLAB, "cobbled_deepslate_slab");
            registerModel(FFDItems.POLISHED_DEEPSLATE_SLAB, "polished_deepslate_slab");
            registerModel(FFDItems.DEEPSLATE_BRICK_SLAB, "deepslate_brick_slab");
            registerModel(FFDItems.DEEPSLATE_TILE_SLAB, "deepslate_tile_slab");
            registerModel(FFDItems.COBBLED_DEEPSLATE_WALL, "cobbled_deepslate_wall");
            registerModel(FFDItems.POLISHED_DEEPSLATE_WALL, "polished_deepslate_wall");
            registerModel(FFDItems.DEEPSLATE_BRICK_WALL, "deepslate_brick_wall");
            registerModel(FFDItems.DEEPSLATE_TILE_WALL, "deepslate_tile_wall");
            registerModel(FFDItems.DEEPSLATE_COAL_ORE, "deepslate_coal_ore");
            registerModel(FFDItems.DEEPSLATE_IRON_ORE, "deepslate_iron_ore");
            registerModel(FFDItems.DEEPSLATE_GOLD_ORE, "deepslate_gold_ore");
            registerModel(FFDItems.DEEPSLATE_REDSTONE_ORE, "deepslate_redstone_ore");
            registerModel(FFDItems.DEEPSLATE_LAPIS_ORE, "deepslate_lapis_ore");
            registerModel(FFDItems.DEEPSLATE_DIAMOND_ORE, "deepslate_diamond_ore");
            registerModel(FFDItems.DEEPSLATE_EMERALD_ORE, "deepslate_emerald_ore");
        }
        if (FFDItems.isRawOreEnabled()) {
            registerModel(FFDItems.RAW_IRON, "raw_iron");
            registerModel(FFDItems.RAW_GOLD, "raw_gold");
            registerModel(FFDItems.RAW_IRON_BLOCK, "raw_iron_block");
            registerModel(FFDItems.RAW_GOLD_BLOCK, "raw_gold_block");
        }
        registerModel(FFDItems.COPPER_ORE, "copper_ore");
        registerModel(FFDItems.RAW_COPPER, "raw_copper");
        registerModel(FFDItems.RAW_COPPER_BLOCK, "raw_copper_block");
        registerModel(FFDItems.COPPER_INGOT, "copper_ingot");
        registerModels(FFDItems.COPPER_BLOCK_ITEMS);
        registerModels(FFDItems.WAXED_COPPER_BLOCK_ITEMS);
        registerModels(FFDItems.CUT_COPPER_ITEMS);
        registerModels(FFDItems.WAXED_CUT_COPPER_ITEMS);
        registerModels(FFDItems.CUT_COPPER_STAIR_ITEMS);
        registerModels(FFDItems.WAXED_CUT_COPPER_STAIR_ITEMS);
        registerModels(FFDItems.CUT_COPPER_SLAB_ITEMS);
        registerModels(FFDItems.WAXED_CUT_COPPER_SLAB_ITEMS);
        registerModels(FFDItems.LIGHTNING_ROD_ITEMS);
        registerModels(FFDItems.WAXED_LIGHTNING_ROD_ITEMS);
        registerModel(FFDItems.SPYGLASS, "spyglass");
        if (FFDItems.isItemRegistered(FFDItems.SPYGLASS)) {
            ModelBakery.registerItemVariants(FFDItems.SPYGLASS,
                    new ResourceLocation(FarmerFutureDelight.MODID, "spyglass_in_hand"));
        }
        if (FFDItems.isDeepslateEnabled()) {
            registerModel(FFDItems.DEEPSLATE_COPPER_ORE, "deepslate_copper_ore");
        }
        if (FFDItems.isHoneyEnabled()) {
            registerModel(FFDItems.HONEY_BOTTLE, "honey_bottle");
            registerModel(FFDItems.HONEYCOMB, "honeycomb");
            registerModel(FFDItems.HONEY_BLOCK, "honey_block");
            registerModel(FFDItems.HONEYCOMB_BLOCK, "honeycomb_block");
            registerModel(FFDItems.BEE_NEST, "bee_nest");
            registerModel(FFDItems.BEEHIVE, "beehive");
        }
        if (FFDItems.isCrimsonEnabled()) {
            registerModel(FFDItems.CRIMSON_NYLIUM, "crimson_nylium");
            registerModel(FFDItems.CRIMSON_FUNGUS, "crimson_fungus");
            registerModel(FFDItems.CRIMSON_ROOTS, "crimson_roots");
            registerModel(FFDItems.WEEPING_VINES, "weeping_vines");
        }
        if (FFDItems.isWarpedEnabled()) {
            registerModel(FFDItems.WARPED_NYLIUM, "warped_nylium");
            registerModel(FFDItems.WARPED_FUNGUS, "warped_fungus");
            registerModel(FFDItems.WARPED_ROOTS, "warped_roots");
            registerModel(FFDItems.NETHER_SPROUTS, "nether_sprouts");
            registerModel(FFDItems.TWISTING_VINES, "twisting_vines");
        }
        if (FFDItems.isCrimsonWoodEnabled()) {
            registerModel(FFDItems.CRIMSON_STEM, "crimson_stem");
            registerModel(FFDItems.STRIPPED_CRIMSON_STEM, "stripped_crimson_stem");
            registerModel(FFDItems.CRIMSON_HYPHAE, "crimson_hyphae");
            registerModel(FFDItems.STRIPPED_CRIMSON_HYPHAE, "stripped_crimson_hyphae");
            registerModel(FFDItems.NETHER_WART_BLOCK, "nether_wart_block");
            registerModel(FFDItems.CRIMSON_PLANKS, "crimson_planks");
            registerModel(FFDItems.CRIMSON_STAIRS, "crimson_stairs");
            registerModel(FFDItems.CRIMSON_SLAB, "crimson_slab");
            registerModel(FFDItems.CRIMSON_FENCE, "crimson_fence");
            registerModel(FFDItems.CRIMSON_FENCE_GATE, "crimson_fence_gate");
            registerModel(FFDItems.CRIMSON_DOOR, "crimson_door");
            registerModel(FFDItems.CRIMSON_TRAPDOOR, "crimson_trapdoor");
            registerModel(FFDItems.CRIMSON_BUTTON, "crimson_button");
            registerModel(FFDItems.CRIMSON_PRESSURE_PLATE, "crimson_pressure_plate");
            registerStateMapper(FFDBlocks.CRIMSON_DOOR,
                    new StateMap.Builder().ignore(BlockDoor.POWERED).build());
            registerStateMapper(FFDBlocks.CRIMSON_FENCE_GATE,
                    new StateMap.Builder().ignore(BlockFenceGate.POWERED).build());
        }
        if (FFDItems.isWarpedWoodEnabled()) {
            registerModel(FFDItems.WARPED_STEM, "warped_stem");
            registerModel(FFDItems.STRIPPED_WARPED_STEM, "stripped_warped_stem");
            registerModel(FFDItems.WARPED_HYPHAE, "warped_hyphae");
            registerModel(FFDItems.STRIPPED_WARPED_HYPHAE, "stripped_warped_hyphae");
            registerModel(FFDItems.WARPED_WART_BLOCK, "warped_wart_block");
            registerModel(FFDItems.WARPED_PLANKS, "warped_planks");
            registerModel(FFDItems.WARPED_STAIRS, "warped_stairs");
            registerModel(FFDItems.WARPED_SLAB, "warped_slab");
            registerModel(FFDItems.WARPED_FENCE, "warped_fence");
            registerModel(FFDItems.WARPED_FENCE_GATE, "warped_fence_gate");
            registerModel(FFDItems.WARPED_DOOR, "warped_door");
            registerModel(FFDItems.WARPED_TRAPDOOR, "warped_trapdoor");
            registerModel(FFDItems.WARPED_BUTTON, "warped_button");
            registerModel(FFDItems.WARPED_PRESSURE_PLATE, "warped_pressure_plate");
            registerStateMapper(FFDBlocks.WARPED_DOOR,
                    new StateMap.Builder().ignore(BlockDoor.POWERED).build());
            registerStateMapper(FFDBlocks.WARPED_FENCE_GATE,
                    new StateMap.Builder().ignore(BlockFenceGate.POWERED).build());
        }
        if (FFDItems.isCrimsonWoodEnabled() || FFDItems.isWarpedWoodEnabled()) {
            registerModel(FFDItems.SHROOMLIGHT, "shroomlight");
        }
        registerStateMapper(FFDBlocks.AZALEA_LEAVES,
                new StateMap.Builder().ignore(BlockLeaves.DECAYABLE, BlockLeaves.CHECK_DECAY).build());
        registerStateMapper(FFDBlocks.FLOWERING_AZALEA_LEAVES,
                new StateMap.Builder().ignore(BlockLeaves.DECAYABLE, BlockLeaves.CHECK_DECAY).build());
        if (FFDItems.isKelpEnabled()) {
            registerStateMapper(FFDBlocks.KELP,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockKelp.AGE).build());
            registerStateMapper(FFDBlocks.KELP_YOUNG,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockKelpYoung.AGE).build());
            registerStateMapper(FFDBlocks.KELP_PLANT,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        }
        if (FFDItems.isSeagrassEnabled()) {
            registerStateMapper(FFDBlocks.SEAGRASS,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
            registerStateMapper(FFDBlocks.TALL_SEAGRASS,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        }
        if (FFDItems.isSeaPickleEnabled()) {
            registerStateMapper(FFDBlocks.SEA_PICKLE,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        }
        registerStateMapper(FFDBlocks.SMALL_DRIPLEAF,
                new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockSmallDripleaf.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.BIG_DRIPLEAF_STEM,
                new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockBigDripleafStem.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.BIG_DRIPLEAF,
                new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        registerStateMapper(FFDBlocks.BIG_DRIPLEAF_WATERLOGGED,
                new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
    }

    private static void registerModel(net.minecraft.item.Item item, String name) {
        if (!FFDItems.isItemRegistered(item)) {
            return;
        }
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(FarmerFutureDelight.MODID + ":" + name, "inventory"));
    }

    private static void registerModels(net.minecraft.item.Item[] items) {
        for (net.minecraft.item.Item item : items) {
            registerModel(item, item.getRegistryName().getResourcePath());
        }
    }

    private static void registerStateMapper(net.minecraft.block.Block block,
                                            net.minecraft.client.renderer.block.statemap.IStateMapper mapper) {
        if (FFDItems.isBlockRegistered(block)) {
            ModelLoader.setCustomStateMapper(block, mapper);
        }
    }

    @Override
    public void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowSquid.class, RenderGlowSquid::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTurtle.class, RenderTurtle::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityAxolotl.class, RenderAxolotl::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGoat.class, RenderGoat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBee.class, RenderBee::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPhantom.class, RenderPhantom::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowItemFrame.class,
                manager -> new RenderGlowItemFrame(manager));
        if (FFDItems.isSignTextEnabled()) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySign.class, new RenderSign());
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void spawnGlowParticle(EntityGlowSquid squid) {
        double x = squid.posX + (squid.getRNG().nextDouble() - 0.5D) * 0.6D;
        double y = squid.posY + squid.getRNG().nextDouble() * squid.height;
        double z = squid.posZ + (squid.getRNG().nextDouble() - 0.5D) * 0.6D;
        Minecraft.getMinecraft().effectRenderer.addEffect(ParticleGlowSquid.ambient(squid.world, x, y, z));
    }

    @Override
    public void spawnGlowInkParticles(EntityGlowSquid squid) {
        float pitch = squid.prevSquidPitch * ((float) Math.PI / 180.0F);
        float yaw = -squid.prevRenderYawOffset * ((float) Math.PI / 180.0F);
        Vec3d originOffset = new Vec3d(0.0D, -1.0D, 0.0D).rotatePitch(pitch).rotateYaw(yaw);
        Vec3d origin = originOffset.addVector(squid.posX, squid.posY + 0.5D, squid.posZ);

        for (int i = 0; i < 30; i++) {
            Vec3d direction = new Vec3d(
                    squid.getRNG().nextFloat() * 0.6F - 0.3F,
                    -1.0D,
                    squid.getRNG().nextFloat() * 0.6F - 0.3F)
                    .rotatePitch(pitch).rotateYaw(yaw);
            double scale = (squid.isChild() ? 0.1D : 0.3D) + squid.getRNG().nextFloat() * 2.0D;
            direction = direction.scale(scale * 0.1D);
            Minecraft.getMinecraft().effectRenderer.addEffect(
                    ParticleGlowSquid.ink(squid.world, origin.x, origin.y, origin.z,
                            direction.x, direction.y, direction.z));
        }
    }

    @Override
    public void spawnSporeBlossomParticle(net.minecraft.world.World world, double x, double y,
                                          double z, boolean ambient) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int particleSetting = minecraft.gameSettings.particleSetting;
        if (particleSetting == 2
                || particleSetting == 1 && world.rand.nextInt(3) == 0) {
            return;
        }
        ParticleSporeBlossom particle = ParticleSporeBlossom.create(world, x, y, z, ambient);
        if (particle != null) {
            minecraft.effectRenderer.addEffect(particle);
        }
    }

    @Override
    public void spawnHoneyDripParticle(net.minecraft.world.World world, double x, double y,
                                       double z) {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                ParticleHoneyDrip.create(world, x, y, z));
    }

    @Override
    public void spawnFallingNectarParticle(net.minecraft.world.World world, double x, double y,
                                           double z) {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                ParticleFallingNectar.create(world, x, y, z));
    }

    @Override
    public void spawnDripstoneParticle(net.minecraft.world.World world, double x, double y,
                                       double z, boolean lava) {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                ParticleDripstone.create(world, x, y, z, lava));
    }

    @Override
    public void spawnSmallFlameParticle(net.minecraft.world.World world, double x, double y,
                                        double z) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int particleSetting = minecraft.gameSettings.particleSetting;
        if (particleSetting == 2
                || particleSetting == 1 && world.rand.nextInt(3) == 0
                || minecraft.getRenderViewEntity() == null
                || minecraft.getRenderViewEntity().getDistanceSq(x, y, z) > 1024.0D) {
            return;
        }
        minecraft.effectRenderer.addEffect(ParticleSmallFlame.create(world, x, y, z));
    }

    @Override
    public void handleDripstoneParticle(double x, double y, double z, boolean lava) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world != null) {
                spawnDripstoneParticle(world, x, y, z, lava);
            }
        });
    }

    @Override
    public void handleVerticalBiomes(int dimension, int chunkX, int chunkZ, byte[] biomes) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world != null && world.provider.getDimension() == dimension) {
                FFDVerticalBiomeManager.receiveClientData(world, chunkX, chunkZ, biomes);
            }
        });
    }
}
