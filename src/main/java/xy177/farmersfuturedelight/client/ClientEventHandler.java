package xy177.farmersfuturedelight.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.WaterloggedPlantBakedModel;
import xy177.farmersfuturedelight.client.sound.MovingSoundBee;
import xy177.farmersfuturedelight.common.entity.EntityBee;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class ClientEventHandler {
    private static final Set<String> WATERLOGGED_PLANT_MODELS = new HashSet<>(Arrays.asList(
            "kelp_young", "kelp", "kelp_plant", "seagrass", "tall_seagrass", "sea_pickle",
            "small_dripleaf", "big_dripleaf_stem", "big_dripleaf", "big_dripleaf_waterlogged",
            "hanging_roots", "glow_lichen", "glow_lichen_1", "glow_lichen_2", "glow_lichen_3",
            "glow_lichen_4", "glow_lichen_5", "glow_lichen_6", "glow_lichen_7"));

    private ClientEventHandler() {
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
        event.getMap().registerSprite(id("particle/glow"));
        event.getMap().registerSprite(id("particle/drip_fall"));
        for (int i = 0; i < 8; i++) {
            event.getMap().registerSprite(id("particle/generic_" + i));
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
    }

    @SubscribeEvent
    public static void registerWaterColors(ColorHandlerEvent.Block event) {
        if (FFDItems.isKelpEnabled()) {
            event.getBlockColors().registerBlockColorHandler(ClientEventHandler::waterColor,
                    FFDBlocks.KELP_YOUNG, FFDBlocks.KELP, FFDBlocks.KELP_PLANT);
        }
        if (FFDItems.isSeagrassEnabled()) {
            event.getBlockColors().registerBlockColorHandler(ClientEventHandler::waterColor,
                    FFDBlocks.SEAGRASS, FFDBlocks.TALL_SEAGRASS);
        }
        if (FFDItems.isSeaPickleEnabled()) {
            event.getBlockColors().registerBlockColorHandler(ClientEventHandler::waterColor,
                    FFDBlocks.SEA_PICKLE);
        }
        if (FFDItems.isDripleafEnabled()) {
            event.getBlockColors().registerBlockColorHandler(ClientEventHandler::waterColor,
                    FFDBlocks.SMALL_DRIPLEAF, FFDBlocks.BIG_DRIPLEAF_STEM,
                    FFDBlocks.BIG_DRIPLEAF, FFDBlocks.BIG_DRIPLEAF_WATERLOGGED);
        }
        if (FFDItems.isHangingRootsEnabled()) {
            event.getBlockColors().registerBlockColorHandler(ClientEventHandler::waterColor,
                    FFDBlocks.HANGING_ROOTS);
        }
        if (FFDItems.isGlowLichenEnabled()) {
            event.getBlockColors().registerBlockColorHandler(ClientEventHandler::waterColor,
                    FFDBlocks.GLOW_LICHEN_VARIANTS);
        }
    }

    private static int waterColor(net.minecraft.block.state.IBlockState state, IBlockAccess world,
                                  BlockPos pos, int tintIndex) {
        return tintIndex == 0 && world != null && pos != null
                ? BiomeColorHelper.getWaterColorAtPos(world, pos) : 0xFFFFFF;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, path);
    }
}
