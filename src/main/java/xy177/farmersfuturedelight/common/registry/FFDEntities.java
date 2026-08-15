package xy177.farmersfuturedelight.common.registry;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.entity.EntityGoat;
import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;

public final class FFDEntities {
    public static final ResourceLocation GLOW_SQUID_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "glow_squid");
    public static final ResourceLocation TURTLE_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "turtle");
    public static final ResourceLocation AXOLOTL_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "axolotl");
    public static final ResourceLocation GOAT_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "goat");
    public static final ResourceLocation BEE_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "bee");
    public static final ResourceLocation PHANTOM_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "phantom");
    public static final ResourceLocation GLOW_ITEM_FRAME_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "glow_item_frame");
    private FFDEntities() {
    }

    public static void registerSpawns() {
        if (isLocalGlowSquidEnabled() && FFDConfig.glowSquidSpawnWeight > 0) {
            EntityRegistry.addSpawn(EntityGlowSquid.class,
                    FFDConfig.glowSquidSpawnWeight,
                    FFDConfig.glowSquidMinGroupSize,
                    FFDConfig.glowSquidMaxGroupSize,
                    EnumCreatureType.WATER_CREATURE,
                    net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES.getValuesCollection().toArray(new net.minecraft.world.biome.Biome[0]));
        }
        if (isLocalTurtleEnabled() && FFDConfig.turtleSpawnWeight > 0) {
            EntityRegistry.addSpawn(EntityTurtle.class,
                    FFDConfig.turtleSpawnWeight,
                    FFDConfig.turtleMinGroupSize,
                    FFDConfig.turtleMaxGroupSize,
                    EnumCreatureType.CREATURE,
                    Biomes.BEACH);
        }
        if (isLocalGoatEnabled() && FFDConfig.goatSpawnWeight > 0) {
            EntityRegistry.addSpawn(EntityGoat.class,
                    FFDConfig.goatSpawnWeight,
                    FFDConfig.goatMinGroupSize,
                    FFDConfig.goatMaxGroupSize,
                    EnumCreatureType.CREATURE,
                    FFDBiomes.SNOWY_SLOPES,
                    FFDBiomes.JAGGED_PEAKS,
                    FFDBiomes.FROZEN_PEAKS);
        }
    }

    public static boolean isGlowSquidEnabled() {
        return FFDCompat.isEnabled(FFDConfig.glowSquidMode, FFDCompat.Feature.GLOW_SQUID);
    }

    public static boolean isTurtleEnabled() {
        return FFDItems.isTurtleEnabled();
    }

    public static boolean isAxolotlEnabled() {
        return FFDItems.isAxolotlEnabled();
    }

    public static boolean isGoatEnabled() {
        return FFDItems.isGoatEnabled();
    }

    public static boolean isPhantomEnabled() {
        return FFDItems.isPhantomEnabled();
    }

    public static boolean isLocalGlowSquidEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.glowSquidMode,
                FFDCompat.Feature.GLOW_SQUID, GLOW_SQUID_ID, "glow_squid");
    }

    public static boolean isLocalTurtleEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.turtleMode,
                FFDCompat.Feature.TURTLE, TURTLE_ID, "turtle");
    }

    public static boolean isLocalAxolotlEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.axolotlMode,
                FFDCompat.Feature.AXOLOTL, AXOLOTL_ID, "axolotl");
    }

    public static boolean isLocalGoatEnabled() {
        return FFDCompat.isEnabled(FFDConfig.goatMode);
    }

    public static boolean isLocalBeeEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.honeyMode,
                FFDCompat.Feature.HONEY, BEE_ID, "bee");
    }

    public static boolean isLocalPhantomEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.phantomMode,
                FFDCompat.Feature.PHANTOM, PHANTOM_ID, "phantom");
    }

    public static boolean isLocalGlowItemFrameEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.glowItemFrameMode,
                FFDCompat.Feature.GLOW_ITEM_FRAME, GLOW_ITEM_FRAME_ID,
                "glow_item_frame");
    }
}
