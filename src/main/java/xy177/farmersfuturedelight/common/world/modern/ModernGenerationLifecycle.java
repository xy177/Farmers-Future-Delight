package xy177.farmersfuturedelight.common.world.modern;

import java.util.IdentityHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import xy177.farmersfuturedelight.common.world.nether.NetherGeneration;

@Mod.EventBusSubscriber(modid = "farmers_future_delight")
public final class ModernGenerationLifecycle {
    private static final Map<World, ModernGenerationScheduler> SCHEDULERS = new IdentityHashMap<>();

    private ModernGenerationLifecycle() {
    }

    public static void attach(World world, ModernGenerationScheduler scheduler) {
        ModernGenerationScheduler previous = SCHEDULERS.put(world, scheduler);
        if (previous != null && previous != scheduler) previous.close();
    }

    public static boolean isActive(World world) {
        return !world.isRemote && SCHEDULERS.containsKey(world);
    }

    public static void beginPlayerTick(WorldServer world, List<PlayerChunkMapEntry> missing,
                                       List<PlayerChunkMapEntry> sending) {
        ModernGenerationScheduler scheduler = SCHEDULERS.get(world);
        if (scheduler == null) {
            NetherGeneration.beginPlayerTick(world);
            return;
        }
        scheduler.beginPlayerTick();
        Comparator<PlayerChunkMapEntry> closest = Comparator
                .comparingLong((PlayerChunkMapEntry entry) -> nearestPlayerRing(world, entry.getPos()))
                .thenComparingDouble(PlayerChunkMapEntry::getClosestPlayerDistance);
        missing.sort(closest);
        sending.sort(closest);
    }

    private static long nearestPlayerRing(WorldServer world, ChunkPos position) {
        long nearest = Long.MAX_VALUE;
        for (EntityPlayer player : world.playerEntities) {
            int playerX = MathHelper.floor(player.posX) >> 4;
            int playerZ = MathHelper.floor(player.posZ) >> 4;
            nearest = Math.min(nearest, Math.max(Math.abs((long) position.x - playerX),
                    Math.abs((long) position.z - playerZ)));
        }
        return nearest;
    }

    public static Chunk providePlayerChunk(ChunkProviderServer provider, int chunkX, int chunkZ) {
        ModernGenerationScheduler scheduler = SCHEDULERS.get(provider.world);
        if (scheduler == null) {
            return NetherGeneration.canPreparePlayerChunk(provider.world) ? provider.provideChunk(chunkX, chunkZ)
                    : provider.getLoadedChunk(chunkX, chunkZ);
        }
        Chunk loaded = provider.getLoadedChunk(chunkX, chunkZ);
        if (loaded != null) return loaded;
        if (!scheduler.canPreparePlayerChunk()) return null;
        if (!provider.isChunkGeneratedAt(chunkX, chunkZ) && !scheduler.requestReady(chunkX, chunkZ)) return null;
        scheduler.beginPlayerGeneration();
        try {
            return provider.provideChunk(chunkX, chunkZ);
        } finally {
            scheduler.endPlayerGeneration();
        }
    }

    public static boolean deferFluidTick(World world, BlockPos pos, Block block) {
        if (!SCHEDULERS.containsKey(world)
                || world.isAreaLoaded(pos.add(-1, 0, -1), pos.add(1, 0, 1))) return false;
        world.scheduleUpdate(pos, block, Math.max(1, block.tickRate(world)));
        return true;
    }

    public static Chunk cachedChunk(World world, int chunkX, int chunkZ) {
        return isActive(world) ? ((WorldServer) world).getChunkProvider().getLoadedChunk(chunkX, chunkZ)
                : world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    public static void requestChunk(WorldServer world, int chunkX, int chunkZ) {
        ModernGenerationScheduler scheduler = SCHEDULERS.get(world);
        if (scheduler != null && !world.getChunkProvider().isChunkGeneratedAt(chunkX, chunkZ)) {
            scheduler.requestReady(chunkX, chunkZ);
        }
    }

    @SubscribeEvent
    public static void unload(WorldEvent.Unload event) {
        ModernGenerationScheduler scheduler = SCHEDULERS.remove(event.getWorld());
        if (scheduler == null) return;
        try {
            LogManager.getLogger("FFD Modern Worldgen").info("World generation statistics: {}", scheduler.statistics());
        } finally {
            scheduler.close();
            ModernStructurePreparation.close(event.getWorld());
            ModernChunkLighting.clearCache();
        }
    }
}
