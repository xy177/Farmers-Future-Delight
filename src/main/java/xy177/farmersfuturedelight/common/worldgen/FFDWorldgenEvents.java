package xy177.farmersfuturedelight.common.worldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeHills;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDWorldgenEvents {
    private FFDWorldgenEvents() {
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

    /**
     * The extended generator supplies the modern vanilla ore pass itself. Only
     * the original 1.12 generators are denied here; external Forge ore events
     * (including standard event types) remain available in the Y=0..255 band.
     */
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
