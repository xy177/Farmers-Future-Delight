package xy177.farmersfuturedelight.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.WaterloggedPlantBakedModel;
import xy177.farmersfuturedelight.client.model.SpyglassBakedModel;
import xy177.farmersfuturedelight.client.model.CustomRawOreModelLoader;
import xy177.farmersfuturedelight.client.sound.MovingSoundBee;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDPowderSnowEvents;
import xy177.farmersfuturedelight.common.entity.EntityBee;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDCustomRawOres;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class ClientEventHandler {
    private static final ResourceLocation SPYGLASS_SCOPE =
            id("textures/misc/spyglass_scope.png");
    private static final ResourceLocation POWDER_SNOW_OUTLINE =
            id("textures/misc/powder_snow_outline.png");
    private static final ResourceLocation FROZEN_HEART_ICONS =
            id("textures/gui/icons_frozen.png");
    private static float scopeScale = 0.5F;
    private static Float savedMouseSensitivity;
    private static final Map<EntityLivingBase, float[]> FROZEN_ROTATIONS =
            new IdentityHashMap<>();
    private static final Set<String> WATERLOGGED_PLANT_MODELS = new HashSet<>(Arrays.asList(
            "kelp_young", "kelp", "kelp_plant", "seagrass", "tall_seagrass", "sea_pickle",
            "small_dripleaf", "big_dripleaf_stem", "big_dripleaf", "big_dripleaf_waterlogged",
            "hanging_roots", "glow_lichen", "glow_lichen_1", "glow_lichen_2", "glow_lichen_3",
            "glow_lichen_4", "glow_lichen_5", "glow_lichen_6", "glow_lichen_7",
            "small_amethyst_bud", "medium_amethyst_bud", "large_amethyst_bud", "amethyst_cluster"));

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void replaceTerrainLoadingScreen(GuiOpenEvent event) {
        if (!FFDConfig.modernWorldLoadingScreen) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.getGui() instanceof net.minecraft.client.gui.GuiDownloadTerrain
                && !(event.getGui() instanceof FFDDownloadTerrainScreen)) {
            event.setGui(new FFDDownloadTerrainScreen());
            return;
        }
        if (event.getGui() == null
                && minecraft.currentScreen instanceof FFDDownloadTerrainScreen) {
            FFDDownloadTerrainScreen screen =
                    (FFDDownloadTerrainScreen) minecraft.currentScreen;
            if (minecraft.world != null && minecraft.player != null
                    && screen.holdUntilChunksReady()) {
                event.setGui(screen);
            }
        }
    }

    @SubscribeEvent
    public static void startBeeSound(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote && event.getEntity() instanceof EntityBee) {
            Minecraft.getMinecraft().getSoundHandler()
                    .playSound(new MovingSoundBee((EntityBee) event.getEntity()));
        }
    }

    @SubscribeEvent
    public static void stitchParticles(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(id("block/powder_snow"));
        event.getMap().registerSprite(id("particle/glow"));
        event.getMap().registerSprite(id("particle/drip_hang"));
        event.getMap().registerSprite(id("particle/drip_fall"));
        event.getMap().registerSprite(id("particle/drip_land"));
        for (int i = 0; i < 8; i++) {
            event.getMap().registerSprite(id("particle/generic_" + i));
        }
        if (xy177.farmersfuturedelight.common.registry.FFDEntities
                .isLocalGlowItemFrameEnabled()) {
            event.getMap().registerSprite(id("block/glow_item_frame"));
        }
    }

    @SubscribeEvent
    public static void bakeWaterloggedPlantModels(ModelBakeEvent event) {
        for (ModelResourceLocation location : new ArrayList<>(event.getModelRegistry().getKeys())) {
            if (!FarmerFutureDelight.MODID.equals(location.getResourceDomain())
                    || !WATERLOGGED_PLANT_MODELS.contains(location.getResourcePath())) {
                continue;
            }
            IBakedModel model = event.getModelRegistry().getObject(location);
            if (model != null) {
                event.getModelRegistry().putObject(location, new WaterloggedPlantBakedModel(model));
            }
        }
        if (FFDItems.isItemRegistered(FFDItems.SPYGLASS)) {
            ModelResourceLocation flatLocation = new ModelResourceLocation(
                    FarmerFutureDelight.MODID + ":spyglass", "inventory");
            ModelResourceLocation handLocation = new ModelResourceLocation(
                    FarmerFutureDelight.MODID + ":spyglass_in_hand", "inventory");
            IBakedModel flat = event.getModelRegistry().getObject(flatLocation);
            IBakedModel hand = event.getModelRegistry().getObject(handLocation);
            if (flat != null && hand != null) {
                event.getModelRegistry().putObject(flatLocation,
                        new SpyglassBakedModel(flat, hand));
            }
        }
        if (xy177.farmersfuturedelight.common.registry.FFDEntities
                .isLocalGlowItemFrameEnabled()) {
            bakeGlowFrameModel(event, "glow_item_frame", "glow_item_frame", "normal");
            bakeGlowFrameModel(event, "glow_item_frame_map", "glow_item_frame_map", "map");
        }
    }

    private static void bakeGlowFrameModel(ModelBakeEvent event, String sourceModel,
                                            String targetModel, String variant) {
        IModel model = ModelLoaderRegistry.getModelOrMissing(id("block/" + sourceModel));
        IBakedModel baked = model.bake(model.getDefaultState(), DefaultVertexFormats.BLOCK,
                ModelLoader.defaultTextureGetter());
        event.getModelRegistry().putObject(new ModelResourceLocation(
                FarmerFutureDelight.MODID + ":" + targetModel, variant), baked);
    }

    @SubscribeEvent
    public static void registerWaterColors(ColorHandlerEvent.Block event) {
        registerWaterColor(event, FFDBlocks.KELP_YOUNG, FFDBlocks.KELP,
                FFDBlocks.KELP_PLANT);
        registerWaterColor(event, FFDBlocks.SEAGRASS, FFDBlocks.TALL_SEAGRASS);
        registerWaterColor(event, FFDBlocks.SEA_PICKLE);
        registerWaterColor(event,
                FFDBlocks.SMALL_DRIPLEAF, FFDBlocks.BIG_DRIPLEAF_STEM,
                FFDBlocks.BIG_DRIPLEAF, FFDBlocks.BIG_DRIPLEAF_WATERLOGGED);
        registerWaterColor(event, FFDBlocks.HANGING_ROOTS);
        registerWaterColor(event, FFDBlocks.GLOW_LICHEN_VARIANTS);
        registerWaterColor(event, FFDBlocks.SMALL_AMETHYST_BUD,
                FFDBlocks.MEDIUM_AMETHYST_BUD, FFDBlocks.LARGE_AMETHYST_BUD,
                FFDBlocks.AMETHYST_CLUSTER);
        ArrayList<net.minecraft.block.Block> customBlocks = new ArrayList<>();
        for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
            if (entry.isBlockRegistered()) {
                customBlocks.add(entry.block());
            }
        }
        if (!customBlocks.isEmpty()) {
            event.getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
                FFDCustomRawOres.Entry entry = FFDCustomRawOres.find(state.getBlock());
                return entry == null ? 0xFFFFFF
                        : CustomRawOreModelLoader.INSTANCE.color(entry, true, tintIndex);
            }, customBlocks.toArray(new net.minecraft.block.Block[0]));
        }
    }

    @SubscribeEvent
    public static void registerCustomRawOreItemColors(ColorHandlerEvent.Item event) {
        ArrayList<net.minecraft.item.Item> customItems = new ArrayList<>();
        for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
            if (entry.isRegistered()) {
                customItems.add(entry.item());
                customItems.add(entry.blockItem());
            }
        }
        if (!customItems.isEmpty()) {
            event.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
                FFDCustomRawOres.Entry entry = FFDCustomRawOres.find(stack.getItem());
                return entry == null ? 0xFFFFFF : CustomRawOreModelLoader.INSTANCE.color(
                        entry, stack.getItem() == entry.blockItem(), tintIndex);
            }, customItems.toArray(new net.minecraft.item.Item[0]));
        }
    }

    private static void registerWaterColor(ColorHandlerEvent.Block event,
                                           net.minecraft.block.Block... candidates) {
        ArrayList<net.minecraft.block.Block> registered = new ArrayList<>();
        for (net.minecraft.block.Block block : candidates) {
            if (FFDItems.isBlockRegistered(block)) {
                registered.add(block);
            }
        }
        if (!registered.isEmpty()) {
            event.getBlockColors().registerBlockColorHandler(ClientEventHandler::waterColor,
                    registered.toArray(new net.minecraft.block.Block[0]));
        }
    }

    @SubscribeEvent
    public static void modifySpyglassFov(EntityViewRenderEvent.FOVModifier event) {
        if (isUsingSpyglass() && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            event.setFOV(event.getFOV() * 0.1F);
        }
    }

    @SubscribeEvent
    public static void adjustSpyglassMouseSpeed(TickEvent.RenderTickEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.phase == TickEvent.Phase.START) {
            if (savedMouseSensitivity == null && isUsingSpyglass()
                    && minecraft.gameSettings.thirdPersonView == 0) {
                savedMouseSensitivity = minecraft.gameSettings.mouseSensitivity;
                float base = savedMouseSensitivity * 0.6F + 0.2F;
                minecraft.gameSettings.mouseSensitivity = (base * 0.5F - 0.2F) / 0.6F;
            }
        } else if (savedMouseSensitivity != null) {
            minecraft.gameSettings.mouseSensitivity = savedMouseSensitivity;
            savedMouseSensitivity = null;
        }
    }

    @SubscribeEvent
    public static void hideSpyglassHand(RenderHandEvent event) {
        if (isUsingSpyglass() && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void hideSpyglassCrosshair(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS
                && isUsingSpyglass()
                && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void renderSpyglassScope(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        if (!isUsingSpyglass() || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) {
            scopeScale = 0.5F;
            return;
        }

        scopeScale += (1.125F - scopeScale) * Math.min(1.0F, 0.5F * event.getPartialTicks());
        ScaledResolution resolution = event.getResolution();
        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();
        int size = (int) (Math.min(screenWidth, screenHeight) * scopeScale);
        int left = (screenWidth - size) / 2;
        int top = (screenHeight - size) / 2;
        int right = left + size;
        int bottom = top + size;

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(SPYGLASS_SCOPE);
        Gui.drawScaledCustomSizeModalRect(left, top, 0.0F, 0.0F, 256, 256,
                size, size, 256.0F, 256.0F);
        Gui.drawRect(0, bottom, screenWidth, screenHeight, 0xFF000000);
        Gui.drawRect(0, 0, screenWidth, top, 0xFF000000);
        Gui.drawRect(0, top, left, bottom, 0xFF000000);
        Gui.drawRect(right, top, screenWidth, bottom, 0xFF000000);
        GlStateManager.enableDepth();
    }

    @SubscribeEvent
    public static void renderPowderSnowFreeze(RenderGameOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL
                || minecraft.player == null) {
            return;
        }
        float frozen = FFDPowderSnowEvents.getFrozenPercent(minecraft.player);
        if (frozen <= 0.0F) {
            return;
        }

        ScaledResolution resolution = event.getResolution();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, frozen);
        minecraft.getTextureManager().bindTexture(POWDER_SNOW_OUTLINE);
        Gui.drawScaledCustomSizeModalRect(0, 0, 0.0F, 0.0F, 256, 256,
                resolution.getScaledWidth(), resolution.getScaledHeight(), 256.0F, 256.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableDepth();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void bindFrozenHeartIconsBeforeMantle(RenderGameOverlayEvent.Pre event) {
        if (Loader.isModLoaded("mantle")) {
            bindFrozenHeartTexture(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void bindFrozenHeartIcons(RenderGameOverlayEvent.Pre event) {
        if (!Loader.isModLoaded("mantle")) {
            bindFrozenHeartTexture(event);
        }
    }

    private static void bindFrozenHeartTexture(RenderGameOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH
                && minecraft.player != null
                && FFDPowderSnowEvents.getFrozenPercent(minecraft.player) >= 1.0F
                && !minecraft.player.isPotionActive(MobEffects.POISON)
                && !minecraft.player.isPotionActive(MobEffects.WITHER)) {
            minecraft.getTextureManager().bindTexture(FROZEN_HEART_ICONS);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void beginFrozenShake(RenderLivingEvent.Pre<?> event) {
        if (event.isCanceled()) {
            return;
        }
        EntityLivingBase entity = event.getEntity();
        if (FFDPowderSnowEvents.getFrozenPercent(entity) < 1.0F) {
            return;
        }
        float[] rotation = {entity.prevRenderYawOffset, entity.renderYawOffset};
        FROZEN_ROTATIONS.put(entity, rotation);
        float shake = (float) (Math.cos(Math.floor(entity.ticksExisted
                + event.getPartialRenderTick()) * 3.25F) * Math.PI * 0.4D);
        entity.prevRenderYawOffset += shake;
        entity.renderYawOffset += shake;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void endFrozenShake(RenderLivingEvent.Post<?> event) {
        float[] rotation = FROZEN_ROTATIONS.remove(event.getEntity());
        if (rotation != null) {
            event.getEntity().prevRenderYawOffset = rotation[0];
            event.getEntity().renderYawOffset = rotation[1];
        }
    }

    @SubscribeEvent
    public static void renderVerticalBiomeDebug(RenderGameOverlayEvent.Text event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!minecraft.gameSettings.showDebugInfo || minecraft.world == null
                || minecraft.player == null || !FFDHeightHooks.isExtended(minecraft.world)) {
            return;
        }
        BlockPos pos = new BlockPos(minecraft.player.posX,
                minecraft.player.getEntityBoundingBox().minY, minecraft.player.posZ);
        FFDVerticalBiome vertical = FFDVerticalBiomeManager.getBiome(minecraft.world, pos);
        if (vertical == FFDVerticalBiome.NONE || vertical.registryPath() == null) {
            return;
        }

        String verticalId = FarmerFutureDelight.MODID + ":" + vertical.registryPath();
        String verticalName = I18n.format("biome." + FarmerFutureDelight.MODID
                + "." + vertical.registryPath());
        String line = I18n.format("debug.farmers_future_delight.biome",
                verticalName, verticalId);
        int biomeLine = findBiomeLine(event.getLeft());
        if (biomeLine >= 0) {
            event.getLeft().set(biomeLine, line);
        } else {
            event.getLeft().add(line);
            biomeLine = event.getLeft().size() - 1;
        }

        if (vertical.isCave()) {
            Biome surface = minecraft.world.getBiome(pos);
            ResourceLocation surfaceId = surface.getRegistryName();
            String surfaceName = surface.getBiomeName();
            event.getLeft().add(biomeLine + 1,
                    I18n.format("debug.farmers_future_delight.biome_2d", surfaceName,
                            surfaceId == null ? "unknown" : surfaceId.toString()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderPowderSnowFogColor(EntityViewRenderEvent.FogColors event) {
        if (event.getState().getBlock() != FFDBlocks.POWDER_SNOW) {
            return;
        }
        event.setRed(0x9F / 255.0F);
        event.setGreen(0xBB / 255.0F);
        event.setBlue(0xCC / 255.0F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderPowderSnowFog(EntityViewRenderEvent.RenderFogEvent event) {
        if (event.getState().getBlock() != FFDBlocks.POWDER_SNOW) {
            return;
        }
        boolean spectator = event.getEntity() instanceof net.minecraft.entity.player.EntityPlayer
                && ((net.minecraft.entity.player.EntityPlayer) event.getEntity()).isSpectator();
        GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
        GlStateManager.setFogStart(spectator ? -8.0F : 0.0F);
        GlStateManager.setFogEnd(spectator ? event.getFarPlaneDistance() * 0.5F : 2.0F);
    }

    private static boolean isUsingSpyglass() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || !minecraft.player.isHandActive()) {
            return false;
        }
        ItemStack active = minecraft.player.getActiveItemStack();
        return !active.isEmpty() && active.getItem() == FFDItems.SPYGLASS;
    }

    private static int findBiomeLine(java.util.List<String> lines) {
        for (int index = 0; index < lines.size(); index++) {
            if (lines.get(index).startsWith("Biome:")) {
                return index;
            }
        }
        return -1;
    }

    private static int waterColor(net.minecraft.block.state.IBlockState state, IBlockAccess world,
                                  BlockPos pos, int tintIndex) {
        if (tintIndex != 1 || world == null || pos == null) {
            return 0xFFFFFF;
        }
        int biomeColor = BiomeColorHelper.getWaterColorAtPos(world, pos);
        return OptiFineWaterColorCompat.getColor(world, pos, biomeColor);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, path);
    }
}
