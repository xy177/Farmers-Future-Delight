package xy177.farmersfuturedelight.common.world.modern;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;

@Mod.EventBusSubscriber(modid = "farmers_future_delight")
public final class ModernSpawnPreparation {
    private static final int PREPARE_RADIUS = 3;
    private static final Map<World, ArrayDeque<ChunkPos>> PENDING = new IdentityHashMap<>();

    private ModernSpawnPreparation() {
    }

    public static boolean prepare(MinecraftServer server) {
        WorldServer world = server.getWorld(0);
        if (world == null || !ModernGenerationLifecycle.isActive(world)) return false;
        BlockPos spawn = world.getSpawnPoint();
        int centerX = spawn.getX() >> 4;
        int centerZ = spawn.getZ() >> 4;
        long started = System.nanoTime();
        int loaded = 0;
        List<ChunkPos> positions = new ArrayList<>();
        for (int radius = 0; radius <= PREPARE_RADIUS + 1 && server.isServerRunning(); radius++) {
            for (int offsetX = -radius; offsetX <= radius && server.isServerRunning(); offsetX++) {
                for (int offsetZ = -radius; offsetZ <= radius && server.isServerRunning(); offsetZ++) {
                    if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != radius) continue;
                    positions.add(new ChunkPos(centerX + offsetX, centerZ + offsetZ));
                }
            }
        }
        for (int index = 0; index < positions.size() && server.isServerRunning(); index++) {
            for (int pending = index; pending < Math.min(positions.size(), index + 12); pending++) {
                ChunkPos requested = positions.get(pending);
                ModernGenerationLifecycle.requestChunk(world, requested.x, requested.z);
            }
            ChunkPos position = positions.get(index);
            world.getChunkProvider().provideChunk(position.x, position.z);
            loaded++;
        }
        int ready = 0;
        for (int offsetX = -PREPARE_RADIUS; offsetX <= PREPARE_RADIUS && server.isServerRunning(); offsetX++) {
            for (int offsetZ = -PREPARE_RADIUS; offsetZ <= PREPARE_RADIUS && server.isServerRunning(); offsetZ++) {
                Chunk chunk = world.getChunkProvider().provideChunk(centerX + offsetX, centerZ + offsetZ);
                if (!chunk.isLightPopulated()) chunk.checkLight();
                if (chunk.isTerrainPopulated() && chunk.isLightPopulated()) ready++;
            }
        }
        LogManager.getLogger("FFD Modern Worldgen").info("Prepared {} complete initial spawn chunks with {} loaded chunks in {} ms", ready, loaded,
                (System.nanoTime() - started) / 1000000L);
        if (!server.isDedicatedServer()) {
            ArrayDeque<ChunkPos> pending = new ArrayDeque<>();
            int radius = PREPARE_RADIUS + 2;
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                    if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) == radius) {
                        pending.add(new ChunkPos(centerX + offsetX, centerZ + offsetZ));
                    }
                }
            }
            PENDING.put(world, pending);
        }
        return true;
    }

    @SubscribeEvent
    public static void prepareIdleSpawn(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !(event.world instanceof WorldServer)) return;
        WorldServer world = (WorldServer) event.world;
        ArrayDeque<ChunkPos> pending = PENDING.get(world);
        if (pending == null) return;
        if (!world.playerEntities.isEmpty()) {
            PENDING.remove(world);
            return;
        }
        ModernGenerationLifecycle.beginPlayerTick(world, Collections.emptyList(), Collections.emptyList());
        int queued = 0;
        for (ChunkPos position : pending) {
            if (queued++ >= 12) break;
            ModernGenerationLifecycle.requestChunk(world, position.x, position.z);
        }
        while (!pending.isEmpty()) {
            ChunkPos position = pending.peek();
            Chunk chunk = ModernGenerationLifecycle.providePlayerChunk(world.getChunkProvider(), position.x, position.z);
            if (chunk == null) break;
            pending.remove();
        }
        if (pending.isEmpty()) PENDING.remove(world);
    }

    @SubscribeEvent
    public static void unload(WorldEvent.Unload event) {
        PENDING.remove(event.getWorld());
    }
}
