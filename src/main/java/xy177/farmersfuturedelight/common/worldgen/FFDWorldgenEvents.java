package xy177.farmersfuturedelight.common.worldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeHills;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeProvider;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver;
import xy177.farmersfuturedelight.common.world.biome.GenLayerModernOcean;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.common.world.FFDWorldTypes;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

import java.util.Random;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDWorldgenEvents {
    private static final NoiseGeneratorPerlin FROZEN_TEMPERATURE_NOISE =
            new NoiseGeneratorPerlin(new Random(3456L), 3);
    private static final NoiseGeneratorPerlin BIOME_INFO_NOISE =
            new NoiseGeneratorPerlin(new Random(2345L), 1);
    private static final NoiseGeneratorPerlin TEMPERATURE_NOISE =
            new NoiseGeneratorPerlin(new Random(1234L), 1);

    private FFDWorldgenEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addModernOceanTemperatures(WorldTypeEvent.InitBiomeGens event) {
        WorldType type = event.getWorldType();
        if (type == FFDWorldTypes.EXTENDED || type != WorldType.DEFAULT
                && type != WorldType.DEFAULT_1_1 && type != WorldType.LARGE_BIOMES
                && type != WorldType.AMPLIFIED && type != WorldType.CUSTOMIZED) {
            return;
        }
        GenLayer[] layers = event.getNewBiomeGens();
        if (layers == null || layers.length < 2) {
            return;
        }
        GenLayer generation = new GenLayerModernOcean(4071L, event.getSeed(), layers[0], 4);
        GenLayer index = new GenLayerModernOcean(4072L, event.getSeed(), layers[1], 1);
        generation.initWorldGenSeed(event.getSeed());
        index.initWorldGenSeed(event.getSeed());
        layers[0] = generation;
        layers[1] = index;
        if (layers.length > 2) {
            GenLayer riverMix = new GenLayerModernOcean(4073L, event.getSeed(), layers[2], 4);
            riverMix.initWorldGenSeed(event.getSeed());
            layers[2] = riverMix;
        }
        event.setNewBiomeGens(layers);
    }

    @SubscribeEvent
    public static void replaceCaveGenerator(InitMapGenEvent event) {
        if (event.getType() != InitMapGenEvent.EventType.CAVE
                || !FFDItems.isLushCaveEnabled() && !FFDItems.isGlowLichenEnabled()
                || event.getNewGen() instanceof MapGenLushCaves) {
            return;
        }
        event.setNewGen(new MapGenLushCaves(event.getNewGen()));
    }

    @SubscribeEvent
    public static void suppressLegacyOverworldOres(OreGenEvent.GenerateMinable event) {
        if (event.getWorld().provider.getDimension() != 0
                || !FFDHeightHooks.isExtended(event.getWorld())
                || event.getType() == OreGenEvent.GenerateMinable.EventType.CUSTOM
                || event.getType() == OreGenEvent.GenerateMinable.EventType.QUARTZ
                || !isVanillaOverworldOre(event)) {
            return;
        }
        event.setResult(Event.Result.DENY);
    }

    private static boolean isVanillaOverworldOre(OreGenEvent.GenerateMinable event) {
        WorldGenerator generator = event.getGenerator();
        if (generator == null) {
            return false;
        }
        Biome biome = event.getWorld().getBiome(event.getPos());
        if (biome == null) {
            return false;
        }
        BiomeDecorator decorator = biome.decorator;
        if (decorator != null) {
            switch (event.getType()) {
                case DIRT:
                    return generator == decorator.dirtGen;
                case GRAVEL:
                    return generator == decorator.gravelOreGen;
                case DIORITE:
                    return generator == decorator.dioriteGen;
                case GRANITE:
                    return generator == decorator.graniteGen;
                case ANDESITE:
                    return generator == decorator.andesiteGen;
                case COAL:
                    return generator == decorator.coalGen;
                case IRON:
                    return generator == decorator.ironGen;
                case GOLD:
                    return generator == decorator.goldGen;
                case REDSTONE:
                    return generator == decorator.redstoneGen;
                case DIAMOND:
                    return generator == decorator.diamondGen;
                case LAPIS:
                    return generator == decorator.lapisGen;
                default:
                    break;
            }
        }
        if (event.getType() == OreGenEvent.GenerateMinable.EventType.EMERALD
                && generator.getClass().getEnclosingClass() == BiomeHills.class) {
            return true;
        }
        if (event.getType() == OreGenEvent.GenerateMinable.EventType.SILVERFISH
                && biome instanceof BiomeHills) {
            try {
                WorldGenerator silverfish = ObfuscationReflectionHelper.getPrivateValue(
                        BiomeHills.class, (BiomeHills) biome, "field_82915_S");
                return generator == silverfish;
            } catch (RuntimeException ignored) {
                return false;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void suppressLegacyMountainSurfaceDecoration(DecorateBiomeEvent.Decorate event) {
        if (event.getWorld().provider.getDimension() != 0
                || !FFDHeightHooks.isExtended(event.getWorld())
                || !isLegacySurfaceDecoration(event.getType())) {
            return;
        }

        ChunkPos chunk = event.getChunkPos();
        int x = (chunk.x << 4) + 8;
        int z = (chunk.z << 4) + 8;
        int y = FFDHeightHooks.getWorldHeight(event.getWorld(), x, z) - 1;
        BlockPos pos = new BlockPos(x, Math.max(FFDHeightHooks.minY(event.getWorld()), y), z);
        if (FFDVerticalBiomeManager.getBiome(event.getWorld(), pos).isMountain()) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preserveModernFrozenOceanSurface(PopulateChunkEvent.Populate event) {
        if (event.getType() != PopulateChunkEvent.Populate.EventType.ICE
                || event.getWorld().provider.getDimension() != 0
                || !FFDHeightHooks.isExtended(event.getWorld())
                || !(event.getWorld().getBiomeProvider() instanceof FFDModernBiomeProvider)) {
            return;
        }
        FFDModernBiomeResolver resolver = ((FFDModernBiomeProvider) event.getWorld()
                .getBiomeProvider()).resolver();
        int startX = (event.getChunkX() << 4) + 8;
        int startZ = (event.getChunkZ() << 4) + 8;
        boolean hasFrozenOcean = false;
        for (int x = 0; x < 16 && !hasFrozenOcean; x++) {
            for (int z = 0; z < 16; z++) {
                if (isModernFrozenOcean(resolver, startX + x, startZ + z)) {
                    hasFrozenOcean = true;
                    break;
                }
            }
        }
        if (!hasFrozenOcean) {
            return;
        }
        event.setResult(Event.Result.DENY);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos top = event.getWorld().getPrecipitationHeight(
                        new BlockPos(startX + x, 0, startZ + z));
                BlockPos surface = top.down();
                boolean modernFrozenOcean = isModernFrozenOcean(
                        resolver, surface.getX(), surface.getZ());
                boolean cold = !modernFrozenOcean || isColdModernFrozenOcean(
                        resolver, surface, event.getWorld().getSeaLevel());
                if (cold && event.getWorld().canBlockFreezeWater(surface)) {
                    event.getWorld().setBlockState(surface,
                            net.minecraft.init.Blocks.ICE.getDefaultState(), 2);
                }
                if ((!modernFrozenOcean || isColdModernFrozenOcean(
                        resolver, top, event.getWorld().getSeaLevel()))
                        && event.getWorld().canSnowAt(top, true)) {
                    event.getWorld().setBlockState(top,
                            net.minecraft.init.Blocks.SNOW_LAYER.getDefaultState(), 2);
                }
            }
        }
    }

    private static boolean isModernFrozenOcean(FFDModernBiomeResolver resolver, int x, int z) {
        FFDModernBiomeResolver.ModernBiome biome = resolver.sampleFuzzy(x, z).biome;
        return biome == FFDModernBiomeResolver.ModernBiome.FROZEN_OCEAN
                || biome == FFDModernBiomeResolver.ModernBiome.DEEP_FROZEN_OCEAN;
    }

    private static boolean isColdModernFrozenOcean(FFDModernBiomeResolver resolver,
                                                    BlockPos pos, int seaLevel) {
        FFDModernBiomeResolver.ModernBiome biome = resolver.sampleFuzzy(
                pos.getX(), pos.getZ()).biome;
        double temperature = biome == FFDModernBiomeResolver.ModernBiome.FROZEN_OCEAN
                ? 0.0D : 0.5D;
        double largeVariation = FROZEN_TEMPERATURE_NOISE.getValue(
                pos.getX() * 0.05D, pos.getZ() * 0.05D);
        double edgeVariation = BIOME_INFO_NOISE.getValue(
                pos.getX() * 0.2D, pos.getZ() * 0.2D);
        if (largeVariation + edgeVariation < 0.3D
                && BIOME_INFO_NOISE.getValue(pos.getX() * 0.09D,
                pos.getZ() * 0.09D) < 0.8D) {
            temperature = 0.2D;
        }
        int snowLevel = seaLevel + 17;
        if (pos.getY() > snowLevel) {
            double heightNoise = TEMPERATURE_NOISE.getValue(
                    pos.getX() / 8.0D, pos.getZ() / 8.0D) * 8.0D;
            temperature -= (heightNoise + pos.getY() - snowLevel) * 0.05D / 40.0D;
        }
        return temperature < 0.15D;
    }

    private static boolean isLegacySurfaceDecoration(DecorateBiomeEvent.Decorate.EventType type) {
        switch (type) {
            case BIG_SHROOM:
            case CACTUS:
            case DEAD_BUSH:
            case FLOWERS:
            case GRASS:
            case ICE:
            case LILYPAD:
            case PUMPKIN:
            case REED:
            case ROCK:
            case SHROOM:
            case TREE:
                return true;
            default:
                return false;
        }
    }
}
