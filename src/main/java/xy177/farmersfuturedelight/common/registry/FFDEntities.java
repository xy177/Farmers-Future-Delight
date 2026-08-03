package xy177.farmersfuturedelight.common.registry;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.entity.EntityBee;
import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;

public final class FFDEntities {
    public static final ResourceLocation GLOW_SQUID_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "glow_squid");
    public static final ResourceLocation TURTLE_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "turtle");
    public static final ResourceLocation BEE_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "bee");
    private static final String[] GLOW_SQUID_PROVIDERS = {"oe", "brewinandchewinlegacy"};
    private static final String[] TURTLE_PROVIDERS = {"oe", "brewinandchewinlegacy"};

    private FFDEntities() {
    }

    public static void register() {
        if (isGlowSquidEnabled()) {
            EntityRegistry.registerModEntity(GLOW_SQUID_ID, EntityGlowSquid.class,
                    FarmerFutureDelight.MODID + ".glow_squid", 0, FarmerFutureDelight.instance,
                    64, 3, true);
            EntitySpawnPlacementRegistry.setPlacementType(
                    EntityGlowSquid.class, EntityLiving.SpawnPlacementType.IN_WATER);
            EntityRegistry.registerEgg(GLOW_SQUID_ID, 0x095B71, 0x85F1FF);
        }
        if (isTurtleEnabled()) {
            EntityRegistry.registerModEntity(TURTLE_ID, EntityTurtle.class,
                    FarmerFutureDelight.MODID + ".turtle", 1, FarmerFutureDelight.instance,
                    80, 3, true);
            EntitySpawnPlacementRegistry.setPlacementType(
                    EntityTurtle.class, EntityLiving.SpawnPlacementType.ON_GROUND);
            EntityRegistry.registerEgg(TURTLE_ID, 0x3B6C55, 0xA6E8AD);
        }
        if (FFDItems.isHoneyEnabled()) {
            EntityRegistry.registerModEntity(BEE_ID, EntityBee.class,
                    FarmerFutureDelight.MODID + ".bee", 2, FarmerFutureDelight.instance,
                    64, 3, true);
            EntityRegistry.registerEgg(BEE_ID, 0xE5D5A0, 0xA45E32);
        }
    }

    public static void registerSpawns() {
        if (isGlowSquidEnabled() && FFDConfig.glowSquidSpawnWeight > 0) {
            EntityRegistry.addSpawn(EntityGlowSquid.class,
                    FFDConfig.glowSquidSpawnWeight,
                    FFDConfig.glowSquidMinGroupSize,
                    FFDConfig.glowSquidMaxGroupSize,
                    EnumCreatureType.WATER_CREATURE,
                    net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES.getValuesCollection().toArray(new net.minecraft.world.biome.Biome[0]));
        }
        if (isTurtleEnabled() && FFDConfig.turtleSpawnWeight > 0) {
            EntityRegistry.addSpawn(EntityTurtle.class,
                    FFDConfig.turtleSpawnWeight,
                    FFDConfig.turtleMinGroupSize,
                    FFDConfig.turtleMaxGroupSize,
                    EnumCreatureType.CREATURE,
                    Biomes.BEACH);
        }
    }

    public static boolean isGlowSquidEnabled() {
        return FFDCompat.isEnabled(FFDConfig.glowSquidMode, GLOW_SQUID_PROVIDERS);
    }

    public static boolean isTurtleEnabled() {
        return FFDCompat.isEnabled(FFDConfig.turtleMode, TURTLE_PROVIDERS);
    }
}
