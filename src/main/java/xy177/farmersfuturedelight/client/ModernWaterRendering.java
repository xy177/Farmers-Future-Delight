package xy177.farmersfuturedelight.client;

import java.io.File;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.biome.ModernWaterBiome;
import xy177.farmersfuturedelight.common.registry.FFDPotions;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class ModernWaterRendering {
    private static final int LEGACY_DEFAULT_WATER_COLOR = 0xFFFFFF;
    private static final int MODERN_DEFAULT_WATER_COLOR = 0x3F76E4;
    private static final int MODERN_DEFAULT_FOG_COLOR = 0x050533;
    private static final int MODERN_SWAMP_WATER_COLOR = 0x617B64;
    private static final int MODERN_SWAMP_FOG_COLOR = 0x232317;
    private static final int MODERN_FROZEN_WATER_COLOR = 0x3938C9;
    private static final int LEGACY_PERCEIVED_WATER_COLOR = 0x2B3BF4;
    private static final String PACK_NAME = "farmers_future_delight_modern_water";
    private static final int WATER_BLEND_RADIUS = 2;
    private static EntityPlayer trackedPlayer;
    private static int timeUnderwater;
    private static int targetFogColor = -1;
    private static int previousFogColor = -1;
    private static long fogAdjustTime = -1L;

    private ModernWaterRendering() {
    }

    public static boolean isModernEnabled() {
        return FFDConfig.waterVisualMode == FFDConfig.WaterVisualMode.MODERN;
    }

    public static void installResourcePack(File source) {
        if (!isModernEnabled() || AquaAcrobaticsWaterCompat.isEnabled()) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        List<IResourcePack> packs = ObfuscationReflectionHelper.getPrivateValue(
                Minecraft.class, minecraft, "field_110449_ao");
        for (IResourcePack pack : packs) {
            if (PACK_NAME.equals(pack.getPackName())) {
                return;
            }
        }
        packs.add(new ModernWaterResourcePack(source));
        FMLClientHandler.instance().refreshResources(VanillaResourceType.TEXTURES);
    }

    public static void registerWaterBlockColors(ColorHandlerEvent.Block event) {
        if (!isModernEnabled() || AquaAcrobaticsWaterCompat.isEnabled()) {
            return;
        }
        event.getBlockColors().registerBlockColorHandler(
                (state, world, pos, tintIndex) -> world == null || pos == null
                        ? MODERN_DEFAULT_WATER_COLOR : getWaterColor(world, pos),
                Blocks.WATER, Blocks.FLOWING_WATER);
    }

    public static int getWaterColor(IBlockAccess world, BlockPos pos) {
        if (!isModernEnabled()) {
            return OptiFineWaterColorCompat.getColor(world, pos,
                    BiomeColorHelper.getWaterColorAtPos(world, pos));
        }
        int red = 0;
        int green = 0;
        int blue = 0;
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();
        for (int x = -WATER_BLEND_RADIUS; x <= WATER_BLEND_RADIUS; x++) {
            for (int z = -WATER_BLEND_RADIUS; z <= WATER_BLEND_RADIUS; z++) {
                sample.setPos(pos.getX() + x, pos.getY(), pos.getZ() + z);
                int color = world.getBiome(sample).getWaterColor();
                red += color >> 16 & 255;
                green += color >> 8 & 255;
                blue += color & 255;
            }
        }
        int samples = (WATER_BLEND_RADIUS * 2 + 1) * (WATER_BLEND_RADIUS * 2 + 1);
        int biomeColor = red / samples << 16 | green / samples << 8 | blue / samples;
        return OptiFineWaterColorCompat.getColor(world, pos, biomeColor);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void replaceBiomeWaterColor(BiomeEvent.GetWaterColor event) {
        if (!isModernEnabled() || AquaAcrobaticsWaterCompat.isEnabled()) {
            return;
        }
        event.setNewColor(modernWaterColor(event.getBiome(), event.getNewColor()));
    }

    @SubscribeEvent
    public static void updateWaterVision(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            trackedPlayer = null;
            timeUnderwater = 0;
            return;
        }
        if (player != trackedPlayer) {
            trackedPlayer = player;
            timeUnderwater = 0;
        }
        if (player.isInsideOfMaterial(Material.WATER)) {
            timeUnderwater = MathHelper.clamp(timeUnderwater + (player.isSpectator() ? 10 : 1), 0, 600);
        } else {
            timeUnderwater = MathHelper.clamp(timeUnderwater - 10, 0, 600);
        }
    }

    @SubscribeEvent
    public static void renderWaterFogDensity(EntityViewRenderEvent.FogDensity event) {
        Entity entity = event.getEntity();
        if (!isModernEnabled() || AquaAcrobaticsWaterCompat.isEnabled()
                || event.getState().getMaterial() != Material.WATER
                || entity instanceof EntityPlayer
                && ((EntityPlayer) entity).isPotionActive(MobEffects.BLINDNESS)) {
            return;
        }
        float vision = entity instanceof EntityPlayer ? waterVision((EntityPlayer) entity) : 0.0F;
        float density = 0.05F - vision * vision * 0.03F;
        if (BiomeDictionary.hasType(entity.world.getBiome(entity.getPosition()), BiomeDictionary.Type.SWAMP)) {
            density += 0.005F;
        }
        GlStateManager.setFog(GlStateManager.FogMode.EXP2);
        event.setDensity(density);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderWaterFogColor(EntityViewRenderEvent.FogColors event) {
        Entity entity = event.getEntity();
        if (!isModernEnabled() || AquaAcrobaticsWaterCompat.isEnabled()) {
            return;
        }
        if (event.getState().getMaterial() != Material.WATER || !(entity instanceof EntityPlayer)) {
            fogAdjustTime = -1L;
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        int biomeFogColor = fogColor(player.world.getBiome(player.getPosition()));
        long now = System.nanoTime() / 1000000L;
        if (fogAdjustTime < 0L) {
            targetFogColor = biomeFogColor;
            previousFogColor = biomeFogColor;
            fogAdjustTime = now;
        }
        float progress = MathHelper.clamp((float) (now - fogAdjustTime) / 5000.0F, 0.0F, 1.0F);
        float red = lerp(progress, previousFogColor >> 16 & 255, targetFogColor >> 16 & 255);
        float green = lerp(progress, previousFogColor >> 8 & 255, targetFogColor >> 8 & 255);
        float blue = lerp(progress, previousFogColor & 255, targetFogColor & 255);
        if (targetFogColor != biomeFogColor) {
            targetFogColor = biomeFogColor;
            previousFogColor = MathHelper.floor(red) << 16
                    | MathHelper.floor(green) << 8 | MathHelper.floor(blue);
            fogAdjustTime = now;
        }
        red /= 255.0F;
        green /= 255.0F;
        blue /= 255.0F;
        float vision = waterVision(player);
        float brightness = Math.min(1.0F / Math.max(red, 0.0001F),
                Math.min(1.0F / Math.max(green, 0.0001F),
                        1.0F / Math.max(blue, 0.0001F)));
        red = red * (1.0F - vision) + red * brightness * vision;
        green = green * (1.0F - vision) + green * brightness * vision;
        blue = blue * (1.0F - vision) + blue * brightness * vision;
        PotionEffect blindness = player.getActivePotionEffect(MobEffects.BLINDNESS);
        if (blindness != null) {
            float factor = blindness.getDuration() < 20
                    ? 1.0F - blindness.getDuration() / 20.0F : 0.0F;
            factor *= factor;
            red *= factor;
            green *= factor;
            blue *= factor;
        }
        event.setRed(red);
        event.setGreen(green);
        event.setBlue(blue);
    }

    private static int modernWaterColor(Biome biome, int oldColor) {
        if (biome instanceof ModernWaterBiome) {
            return ((ModernWaterBiome) biome).getModernWaterColor();
        }
        ResourceLocation name = biome.getRegistryName();
        if (name != null && "minecraft".equals(name.getResourceDomain())) {
            String path = name.getResourcePath();
            if ("swampland".equals(path) || "mutated_swampland".equals(path)) {
                return MODERN_SWAMP_WATER_COLOR;
            }
            if ("frozen_ocean".equals(path) || "frozen_river".equals(path)) {
                return MODERN_FROZEN_WATER_COLOR;
            }
        }
        return oldColor == LEGACY_DEFAULT_WATER_COLOR
                ? MODERN_DEFAULT_WATER_COLOR : emulateLegacyColor(oldColor);
    }

    private static int fogColor(Biome biome) {
        if (biome instanceof ModernWaterBiome) {
            return ((ModernWaterBiome) biome).getModernWaterFogColor();
        }
        ResourceLocation name = biome.getRegistryName();
        if (name != null && "minecraft".equals(name.getResourceDomain())) {
            String path = name.getResourcePath();
            if ("swampland".equals(path) || "mutated_swampland".equals(path)) {
                return MODERN_SWAMP_FOG_COLOR;
            }
        }
        return MODERN_DEFAULT_FOG_COLOR;
    }

    private static int emulateLegacyColor(int color) {
        int red = (color >> 16 & 255) * (LEGACY_PERCEIVED_WATER_COLOR >> 16 & 255) / 255;
        int green = (color >> 8 & 255) * (LEGACY_PERCEIVED_WATER_COLOR >> 8 & 255) / 255;
        int blue = (color & 255) * (LEGACY_PERCEIVED_WATER_COLOR & 255) / 255;
        return red << 16 | green << 8 | blue;
    }

    public static boolean hasLightmapVision(EntityLivingBase entity, Potion potion) {
        return entity.isPotionActive(potion)
                || potion == MobEffects.NIGHT_VISION && conduitLightmapVision(entity) > 0.0F;
    }

    public static float conduitLightmapVision(EntityLivingBase entity) {
        return entity instanceof EntityPlayer && FFDPotions.hasConduitPower(entity)
                ? waterVision((EntityPlayer) entity) : 0.0F;
    }

    private static float waterVision(EntityPlayer player) {
        if (player != trackedPlayer || !player.isInsideOfMaterial(Material.WATER)) {
            return 0.0F;
        }
        if (timeUnderwater >= 600) {
            return 1.0F;
        }
        float initial = MathHelper.clamp(timeUnderwater / 100.0F, 0.0F, 1.0F);
        float remaining = timeUnderwater < 100 ? 0.0F
                : MathHelper.clamp((timeUnderwater - 100.0F) / 500.0F, 0.0F, 1.0F);
        return initial * 0.6F + remaining * 0.4F;
    }

    private static float lerp(float amount, float start, float end) {
        return start + amount * (end - start);
    }
}
