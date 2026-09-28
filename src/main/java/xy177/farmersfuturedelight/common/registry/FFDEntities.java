package xy177.farmersfuturedelight.common.registry;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.EnumHelper;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.entity.EntityGoat;
import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;
import xy177.farmersfuturedelight.common.entity.EntityCod;
import xy177.farmersfuturedelight.common.entity.EntitySalmon;
import xy177.farmersfuturedelight.common.entity.EntityPufferfish;
import xy177.farmersfuturedelight.common.entity.EntityTropicalFish;
import xy177.farmersfuturedelight.common.entity.EntityDolphin;
import xy177.farmersfuturedelight.common.entity.EntityDrowned;

public final class FFDEntities {
    public static final EnumCreatureType UNDERGROUND_WATER_CREATURE =
            EnumHelper.addCreatureType("FFD_UNDERGROUND_WATER_CREATURE",
                    EntityGlowSquid.class, 5, Material.WATER, true, false);
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
    public static final ResourceLocation COD_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "cod");
    public static final ResourceLocation SALMON_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "salmon");
    public static final ResourceLocation PUFFERFISH_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "pufferfish");
    public static final ResourceLocation TROPICAL_FISH_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "tropical_fish");
    public static final ResourceLocation DOLPHIN_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "dolphin");
    public static final ResourceLocation DROWNED_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "drowned");
    public static final ResourceLocation TRIDENT_ID =
            new ResourceLocation(FarmerFutureDelight.MODID, "trident");
    private FFDEntities() {
    }

    public static void registerSpawns() {
        registerSquidSpawns();
        if (isLocalGlowSquidEnabled() && FFDConfig.glowSquidSpawnWeight > 0) {
            Set<Biome> overworld = new LinkedHashSet<>();
            for (Biome biome : net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES) {
                Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
                if (biome != Biomes.VOID
                        && !types.contains(BiomeDictionary.Type.NETHER)
                        && !types.contains(BiomeDictionary.Type.END)
                        && !types.contains(BiomeDictionary.Type.VOID)) {
                    overworld.add(biome);
                }
            }
            EntityRegistry.addSpawn(EntityGlowSquid.class,
                    FFDConfig.glowSquidSpawnWeight,
                    FFDConfig.glowSquidMinGroupSize,
                    FFDConfig.glowSquidMaxGroupSize,
                    UNDERGROUND_WATER_CREATURE, overworld.toArray(new Biome[0]));
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
        registerFishSpawns();
        registerDolphinSpawns();
        registerDrownedSpawns();
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

    public static boolean isLocalCodEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.fishMode,
                FFDCompat.Feature.FISH, COD_ID, "cod");
    }

    public static boolean isLocalSalmonEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.fishMode,
                FFDCompat.Feature.FISH, SALMON_ID, "salmon");
    }

    public static boolean isLocalPufferfishEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.fishMode,
                FFDCompat.Feature.FISH, PUFFERFISH_ID, "pufferfish");
    }

    public static boolean isLocalTropicalFishEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.fishMode,
                FFDCompat.Feature.FISH, TROPICAL_FISH_ID, "tropical_fish");
    }

    public static boolean isLocalDolphinEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.dolphinMode,
                FFDCompat.Feature.DOLPHIN, DOLPHIN_ID, "dolphin");
    }

    public static boolean isDrownedEnabled() {
        return FFDCompat.isEnabled(FFDConfig.drownedMode, FFDCompat.Feature.DROWNED);
    }

    public static boolean isLocalDrownedEnabled() {
        return FFDCompat.isLocalEntityEnabled(FFDConfig.drownedMode,
                FFDCompat.Feature.DROWNED, DROWNED_ID, "drowned");
    }

    public static boolean isLocalTridentProjectileEnabled() {
        return isLocalDrownedEnabled() || FFDItems.isTridentEnabled();
    }

    private static void registerSquidSpawns() {
        for (Biome biome : net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES) {
            ResourceLocation id = biome.getRegistryName();
            if (id == null || !("minecraft".equals(id.getResourceDomain())
                    || FarmerFutureDelight.MODID.equals(id.getResourceDomain()))) {
                continue;
            }
            EntityRegistry.removeSpawn(EntitySquid.class, EnumCreatureType.WATER_CREATURE, biome);
            Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
            String path = id.getResourcePath().toLowerCase(Locale.ROOT);
            if (types.contains(BiomeDictionary.Type.RIVER)
                    || biome == Biomes.RIVER || biome == Biomes.FROZEN_RIVER) {
                EntityRegistry.addSpawn(EntitySquid.class, 2, 1, 4,
                        EnumCreatureType.WATER_CREATURE, biome);
                continue;
            }
            if (!types.contains(BiomeDictionary.Type.OCEAN)
                    && biome != Biomes.OCEAN && biome != Biomes.DEEP_OCEAN
                    && biome != Biomes.FROZEN_OCEAN) {
                continue;
            }
            boolean deep = path.contains("deep") || biome == Biomes.DEEP_OCEAN;
            boolean lukewarm = path.contains("lukewarm");
            boolean frozen = path.contains("frozen") || biome == Biomes.FROZEN_OCEAN
                    || types.contains(BiomeDictionary.Type.SNOWY);
            boolean cold = path.contains("cold") || types.contains(BiomeDictionary.Type.COLD);
            boolean warm = path.contains("warm") || types.contains(BiomeDictionary.Type.HOT);
            int weight = lukewarm ? deep ? 8 : 10 : frozen ? 1 : cold ? 3 : warm ? 10 : 1;
            int min = warm && !lukewarm && !frozen && !cold ? 4 : 1;
            int max = lukewarm && !deep ? 2 : 4;
            EntityRegistry.addSpawn(EntitySquid.class, weight, min, max,
                    EnumCreatureType.WATER_CREATURE, biome);
        }
    }

    private static void registerFishSpawns() {
        Set<Biome> neutralOceans = new LinkedHashSet<>();
        Set<Biome> coldOceans = new LinkedHashSet<>();
        Set<Biome> frozenOceans = new LinkedHashSet<>();
        Set<Biome> shallowWarmOceans = new LinkedHashSet<>();
        Set<Biome> deepWarmOceans = new LinkedHashSet<>();
        Set<Biome> shallowLukewarmOceans = new LinkedHashSet<>();
        Set<Biome> deepLukewarmOceans = new LinkedHashSet<>();
        Set<Biome> rivers = new LinkedHashSet<>();
        for (Biome biome : net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES) {
            ResourceLocation id = biome.getRegistryName();
            String path = id == null ? "" : id.getResourcePath().toLowerCase(Locale.ROOT);
            Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
            boolean ocean = types.contains(BiomeDictionary.Type.OCEAN)
                    || biome == Biomes.OCEAN || biome == Biomes.DEEP_OCEAN
                    || biome == Biomes.FROZEN_OCEAN;
            boolean river = types.contains(BiomeDictionary.Type.RIVER)
                    || biome == Biomes.RIVER || biome == Biomes.FROZEN_RIVER;
            if (river) {
                rivers.add(biome);
            }
            if (!ocean) {
                continue;
            }
            boolean deep = path.contains("deep") || biome == Biomes.DEEP_OCEAN;
            boolean lukewarm = path.contains("lukewarm");
            boolean frozen = path.contains("frozen") || biome == Biomes.FROZEN_OCEAN
                    || types.contains(BiomeDictionary.Type.SNOWY);
            boolean cold = path.contains("cold") || types.contains(BiomeDictionary.Type.COLD);
            boolean warm = path.contains("warm") || types.contains(BiomeDictionary.Type.HOT);
            if (lukewarm) {
                (deep ? deepLukewarmOceans : shallowLukewarmOceans).add(biome);
            } else if (frozen) {
                frozenOceans.add(biome);
            } else if (cold) {
                coldOceans.add(biome);
            } else if (warm) {
                (deep ? deepWarmOceans : shallowWarmOceans).add(biome);
            } else {
                neutralOceans.add(biome);
            }
        }
        if (isLocalCodEnabled()) {
            addSpawn(EntityCod.class, 10, 3, 6, neutralOceans);
            addSpawn(EntityCod.class, 15, 3, 6, coldOceans);
            addSpawn(EntityCod.class, 15, 3, 6, shallowLukewarmOceans);
            addSpawn(EntityCod.class, 8, 3, 6, deepLukewarmOceans);
        }
        if (isLocalSalmonEnabled()) {
            addSpawn(EntitySalmon.class, 15, 1, 5, coldOceans);
            addSpawn(EntitySalmon.class, 15, 1, 5, frozenOceans);
            addSpawn(EntitySalmon.class, 5, 1, 5, rivers);
        }
        if (isLocalPufferfishEnabled()) {
            addSpawn(EntityPufferfish.class, 15, 1, 3, shallowWarmOceans);
            addSpawn(EntityPufferfish.class, 5, 1, 3, shallowLukewarmOceans);
            addSpawn(EntityPufferfish.class, 5, 1, 3, deepLukewarmOceans);
        }
        if (isLocalTropicalFishEnabled()) {
            Set<Biome> tropical = new LinkedHashSet<>(shallowWarmOceans);
            tropical.addAll(deepWarmOceans);
            tropical.addAll(shallowLukewarmOceans);
            tropical.addAll(deepLukewarmOceans);
            addSpawn(EntityTropicalFish.class, 25, 8, 8, tropical);
        }
    }

    private static void registerDolphinSpawns() {
        if (!isLocalDolphinEnabled() || FFDConfig.dolphinSpawnWeight <= 0) {
            return;
        }
        Set<Biome> neutral = new LinkedHashSet<>();
        Set<Biome> warm = new LinkedHashSet<>();
        for (Biome biome : net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES) {
            ResourceLocation id = biome.getRegistryName();
            String path = id == null ? "" : id.getResourcePath().toLowerCase(Locale.ROOT);
            Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
            boolean ocean = types.contains(BiomeDictionary.Type.OCEAN)
                    || biome == Biomes.OCEAN || biome == Biomes.DEEP_OCEAN
                    || biome == Biomes.FROZEN_OCEAN;
            if (!ocean || path.contains("frozen") || path.contains("cold")
                    || biome == Biomes.FROZEN_OCEAN
                    || types.contains(BiomeDictionary.Type.SNOWY)
                    || types.contains(BiomeDictionary.Type.COLD)) {
                continue;
            }
            if (path.contains("warm") || path.contains("lukewarm")
                    || types.contains(BiomeDictionary.Type.HOT)) {
                warm.add(biome);
            } else {
                neutral.add(biome);
            }
        }
        addSpawn(EntityDolphin.class, FFDConfig.dolphinSpawnWeight,
                FFDConfig.dolphinMinGroupSize, FFDConfig.dolphinMaxGroupSize, neutral);
        addSpawn(EntityDolphin.class, FFDConfig.dolphinSpawnWeight * 2,
                FFDConfig.dolphinMinGroupSize, FFDConfig.dolphinMaxGroupSize, warm);
    }

    private static void registerDrownedSpawns() {
        if (!isLocalDrownedEnabled()) {
            return;
        }
        Set<Biome> oceans = new LinkedHashSet<>();
        Set<Biome> rivers = new LinkedHashSet<>();
        Set<Biome> frozenRivers = new LinkedHashSet<>();
        for (Biome biome : net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES) {
            ResourceLocation id = biome.getRegistryName();
            String path = id == null ? "" : id.getResourcePath().toLowerCase(Locale.ROOT);
            Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
            if (types.contains(BiomeDictionary.Type.RIVER)
                    || biome == Biomes.RIVER || biome == Biomes.FROZEN_RIVER) {
                if (biome == Biomes.FROZEN_RIVER || path.contains("frozen")
                        || types.contains(BiomeDictionary.Type.SNOWY)) {
                    frozenRivers.add(biome);
                } else {
                    rivers.add(biome);
                }
            } else if (types.contains(BiomeDictionary.Type.OCEAN)
                    || biome == Biomes.OCEAN || biome == Biomes.DEEP_OCEAN
                    || biome == Biomes.FROZEN_OCEAN) {
                oceans.add(biome);
            }
        }
        addMonsterSpawn(EntityDrowned.class, FFDConfig.drownedOceanSpawnWeight, oceans);
        addMonsterSpawn(EntityDrowned.class, FFDConfig.drownedRiverSpawnWeight, rivers);
        addMonsterSpawn(EntityDrowned.class, FFDConfig.drownedFrozenRiverSpawnWeight, frozenRivers);
    }

    private static void addSpawn(Class<? extends net.minecraft.entity.EntityLiving> type,
                                 int weight, int min, int max, Set<Biome> biomes) {
        if (!biomes.isEmpty()) {
            EntityRegistry.addSpawn(type, weight, min, max, EnumCreatureType.WATER_CREATURE,
                    biomes.toArray(new Biome[0]));
        }
    }

    private static void addMonsterSpawn(Class<? extends net.minecraft.entity.EntityLiving> type,
                                        int weight, Set<Biome> biomes) {
        if (weight > 0 && !biomes.isEmpty()) {
            EntityRegistry.addSpawn(type, weight, 1, 1, EnumCreatureType.MONSTER,
                    biomes.toArray(new Biome[0]));
        }
    }
}
