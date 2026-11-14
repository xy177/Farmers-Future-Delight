package xy177.farmersfuturedelight.common.world.nether;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = "farmers_future_delight")
public final class NetherChunkLighting {
    private static final String VERSION_TAG = "FFDNetherBlockLighting";
    private static final String DEFERRED_TAG = "FFDNetherBlockLightChecks";
    private static final long TICK_BUDGET_NANOS = 4000000L;
    private static final Set<Chunk> INITIALIZED = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private static final Map<Chunk, IntLinkedOpenHashSet> DEFERRED = new WeakHashMap<>();
    private static final Map<World, LinkedHashSet<Chunk>> PENDING = new IdentityHashMap<>();
    private static final ThreadLocal<Work> WORK = ThreadLocal.withInitial(Work::new);
    private static final int[] NEIGHBOR_X = {-1, 1, 0, 0};
    private static final int[] NEIGHBOR_Z = {0, 0, -1, 1};

    private NetherChunkLighting() { }

    public static void initialize(Chunk chunk) {
        initialize(chunk, false);
    }

    private static void initialize(Chunk chunk, boolean keepSavedLight) {
        Work work = WORK.get();
        work.load(chunk, true, keepSavedLight);
        work.propagate();
        work.store();
        INITIALIZED.add(chunk);
    }

    public static boolean isInitialized(Chunk chunk) {
        return INITIALIZED.contains(chunk);
    }

    @SubscribeEvent
    public static void read(ChunkDataEvent.Load event) {
        if (event.getWorld().isRemote || event.getWorld().provider.getDimension() != -1
                || !NetherGeneration.isModern(event.getWorld())) return;
        if (event.getData().getInteger(VERSION_TAG) >= 1) INITIALIZED.add(event.getChunk());
        int[] checks = event.getData().getIntArray(DEFERRED_TAG);
        if (checks.length != 0) synchronized (DEFERRED) {
            DEFERRED.put(event.getChunk(), new IntLinkedOpenHashSet(checks));
        }
    }

    @SubscribeEvent
    public static void save(ChunkDataEvent.Save event) {
        if (event.getWorld().isRemote || event.getWorld().provider.getDimension() != -1
                || !INITIALIZED.contains(event.getChunk())) return;
        event.getData().setInteger(VERSION_TAG, 1);
        synchronized (DEFERRED) {
            IntLinkedOpenHashSet checks = DEFERRED.get(event.getChunk());
            if (checks != null && !checks.isEmpty()) event.getData().setIntArray(DEFERRED_TAG, checks.toIntArray());
        }
    }

    @SubscribeEvent
    public static void load(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (!(world instanceof WorldServer) || world.provider.getDimension() != -1 || !NetherGeneration.isModern(world)) return;
        Chunk chunk = event.getChunk();
        if (!INITIALIZED.contains(chunk)) {
            initialize(chunk, true);
            chunk.markDirty();
        }
        schedule(chunk);
        scheduleNeighbors(chunk);
        for (int offsetZ = -1; offsetZ <= 1; offsetZ++) for (int offsetX = -1; offsetX <= 1; offsetX++) {
            Chunk nearby = loaded((WorldServer) world, chunk.x + offsetX, chunk.z + offsetZ);
            if (nearby == null || nearby == chunk) continue;
            synchronized (DEFERRED) {
                if (DEFERRED.containsKey(nearby)) schedule(nearby);
            }
        }
    }

    @SubscribeEvent
    public static void watch(ChunkWatchEvent.Watch event) {
        Chunk chunk = event.getChunkInstance();
        if (chunk == null || chunk.getWorld().provider.getDimension() != -1 || !INITIALIZED.contains(chunk)) return;
        int mask = 0;
        ExtendedBlockStorage[] storage = chunk.getBlockStorageArray();
        for (int index = 0; index < storage.length; index++) {
            ExtendedBlockStorage section = storage[index];
            if (section == null || !section.isEmpty()) continue;
            for (byte light : section.getBlockLight().getData()) if (light != 0) {
                mask |= 1 << index;
                break;
            }
        }
        if (mask != 0) event.getPlayer().connection.sendPacket(new SPacketChunkData(chunk, mask));
    }

    public static void defer(World world, EnumSkyBlock type, BlockPos pos) {
        if (type != EnumSkyBlock.BLOCK || !(world instanceof WorldServer) || world.provider.getDimension() != -1
                || pos.getY() < 0 || pos.getY() >= NetherGeneration.BUILD_HEIGHT || !NetherGeneration.isModern(world)) return;
        Chunk chunk = loaded((WorldServer) world, pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) return;
        int local = pos.getY() << 8 | (pos.getZ() & 15) << 4 | pos.getX() & 15;
        synchronized (DEFERRED) {
            if (DEFERRED.computeIfAbsent(chunk, key -> new IntLinkedOpenHashSet()).add(local)) chunk.markDirty();
        }
    }

    private static Chunk loaded(WorldServer world, int chunkX, int chunkZ) {
        return world.getChunkProvider().id2ChunkMap.get(ChunkPos.asLong(chunkX, chunkZ));
    }

    private static boolean neighborsLoaded(WorldServer world, Chunk chunk) {
        for (int offsetZ = -1; offsetZ <= 1; offsetZ++) for (int offsetX = -1; offsetX <= 1; offsetX++) {
            Chunk neighbor = loaded(world, chunk.x + offsetX, chunk.z + offsetZ);
            if (neighbor == null || neighbor.isEmpty()) return false;
        }
        return true;
    }

    private static void replay(WorldServer world, Chunk chunk, long deadline) {
        synchronized (DEFERRED) {
            IntLinkedOpenHashSet checks = DEFERRED.get(chunk);
            if (checks == null || !neighborsLoaded(world, chunk)) return;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            while (!checks.isEmpty() && System.nanoTime() < deadline) {
                int local = checks.removeFirstInt();
                pos.setPos((chunk.x << 4) + (local & 15), local >> 8, (chunk.z << 4) + (local >> 4 & 15));
                if (!world.checkLightFor(EnumSkyBlock.BLOCK, pos)) return;
            }
            chunk.markDirty();
            if (checks.isEmpty()) DEFERRED.remove(chunk);
            else schedule(chunk);
        }
    }

    private static void schedule(Chunk chunk) {
        PENDING.computeIfAbsent(chunk.getWorld(), world -> new LinkedHashSet<>()).add(chunk);
    }

    private static void scheduleNeighbors(Chunk chunk) {
        WorldServer world = (WorldServer) chunk.getWorld();
        for (int side = 0; side < 4; side++) {
            Chunk neighbor = loaded(world, chunk.x + NEIGHBOR_X[side], chunk.z + NEIGHBOR_Z[side]);
            if (neighbor != null && INITIALIZED.contains(neighbor)) schedule(neighbor);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)) return;
        LinkedHashSet<Chunk> pending = PENDING.get(event.world);
        if (pending == null || pending.isEmpty()) return;
        WorldServer world = (WorldServer) event.world;
        long deadline = System.nanoTime() + TICK_BUDGET_NANOS;
        do {
            Iterator<Chunk> iterator = pending.iterator();
            Chunk chunk = iterator.next();
            iterator.remove();
            if (loaded(world, chunk.x, chunk.z) != chunk) continue;
            replay(world, chunk, deadline);
            Work work = WORK.get();
            work.load(chunk, false, true);
            work.seedNeighbors();
            work.propagate();
            int changed = work.store();
            if (changed != 0) {
                scheduleNeighbors(chunk);
                PlayerChunkMapEntry entry = world.getPlayerChunkMap().getEntry(chunk.x, chunk.z);
                if (entry != null && entry.isSentToPlayers() && !entry.getWatchingPlayers().isEmpty())
                    entry.sendPacket(new SPacketChunkData(chunk, changed));
            }
        } while (!pending.isEmpty() && System.nanoTime() < deadline);
    }

    @SubscribeEvent
    public static void unload(ChunkEvent.Unload event) {
        if (event.getWorld().isRemote) return;
        LinkedHashSet<Chunk> pending = PENDING.get(event.getWorld());
        if (pending != null) pending.remove(event.getChunk());
    }

    @SubscribeEvent
    public static void unload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) PENDING.remove(event.getWorld());
    }

    private static final class Work {
        private final byte[] light = new byte[65536];
        private final byte[] opacity = new byte[65536];
        private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        private int[] queue = new int[65536];
        private int head;
        private int tail;
        private int dirtySections;
        private Chunk chunk;
        private ExtendedBlockStorage[] storage;

        private void load(Chunk source, boolean seedSources, boolean keepSavedLight) {
            chunk = source;
            storage = chunk.getBlockStorageArray();
            head = tail = 0;
            dirtySections = 0;
            Arrays.fill(light, (byte) 0);
            Arrays.fill(opacity, (byte) 0);
            for (int sectionIndex = 0; sectionIndex < storage.length && sectionIndex < 16; sectionIndex++) {
                ExtendedBlockStorage section = storage[sectionIndex];
                if (section == null) continue;
                int base = sectionIndex << 12;
                if (keepSavedLight) {
                    byte[] stored = section.getBlockLight().getData();
                    for (int packed = 0; packed < stored.length; packed++) {
                        light[base + packed * 2] = (byte) (stored[packed] & 15);
                        light[base + packed * 2 + 1] = (byte) ((stored[packed] & 255) >> 4);
                    }
                } else dirtySections |= 1 << sectionIndex;
                if (!seedSources || !keepSavedLight && section.isEmpty()) continue;
                boolean hasBlocks = !section.isEmpty();
                for (int local = 0; local < 4096; local++) {
                    int index = base + local;
                    if (hasBlocks) {
                        int localX = local & 15, localY = local >> 8, localZ = local >> 4 & 15;
                        IBlockState state = section.get(localX, localY, localZ);
                        if (state.getBlock() != Blocks.AIR) {
                            cursor.setPos((chunk.x << 4) + localX, index >> 8, (chunk.z << 4) + localZ);
                            int emission = Math.min(15, state.getLightValue(chunk.getWorld(), cursor));
                            if (emission > light[index]) {
                                light[index] = (byte) emission;
                                dirtySections |= 1 << sectionIndex;
                            }
                        }
                    }
                    if (light[index] > 1) enqueue(index);
                }
            }
        }

        private void seedNeighbors() {
            WorldServer world = (WorldServer) chunk.getWorld();
            for (int side = 0; side < 4; side++) {
                Chunk neighbor = loaded(world, chunk.x + NEIGHBOR_X[side], chunk.z + NEIGHBOR_Z[side]);
                if (neighbor == null) continue;
                ExtendedBlockStorage[] storage = neighbor.getBlockStorageArray();
                for (int sectionIndex = 0; sectionIndex < storage.length && sectionIndex < 16; sectionIndex++) {
                    ExtendedBlockStorage section = storage[sectionIndex];
                    if (section == null) continue;
                    for (int localY = 0; localY < 16; localY++) for (int edge = 0; edge < 16; edge++) {
                        int localX = side == 0 ? 0 : side == 1 ? 15 : edge;
                        int localZ = side == 2 ? 0 : side == 3 ? 15 : edge;
                        int neighborX = side == 0 ? 15 : side == 1 ? 0 : edge;
                        int neighborZ = side == 2 ? 15 : side == 3 ? 0 : edge;
                        increase((sectionIndex * 16 + localY) << 8 | localZ << 4 | localX,
                                section.getBlockLight(neighborX, localY, neighborZ));
                    }
                }
            }
        }

        private void enqueue(int index) {
            if (tail == queue.length) queue = Arrays.copyOf(queue, queue.length * 2);
            queue[tail++] = index;
        }

        private void increase(int index, int incoming) {
            if (incoming <= light[index] + 1) return;
            int attenuation = opacity[index];
            if (attenuation == 0) {
                int sectionIndex = index >> 12;
                ExtendedBlockStorage section = sectionIndex < storage.length ? storage[sectionIndex] : null;
                attenuation = 1;
                if (section != null && !section.isEmpty()) {
                    int localX = index & 15, localZ = index >> 4 & 15;
                    IBlockState state = section.get(localX, index >> 8 & 15, localZ);
                    if (state.getBlock() != Blocks.AIR) {
                        cursor.setPos((chunk.x << 4) + localX, index >> 8, (chunk.z << 4) + localZ);
                        attenuation = Math.max(1, Math.min(15, state.getLightOpacity(chunk.getWorld(), cursor)));
                    }
                }
                opacity[index] = (byte) attenuation;
            }
            int value = incoming - attenuation;
            if (value > light[index]) {
                light[index] = (byte) value;
                dirtySections |= 1 << (index >> 12);
                enqueue(index);
            }
        }

        private void propagate() {
            while (head < tail) {
                int index = queue[head++];
                int value = light[index];
                if (value <= 1) continue;
                if ((index & 15) != 0) increase(index - 1, value);
                if ((index & 15) != 15) increase(index + 1, value);
                if ((index & 255) >= 16) increase(index - 16, value);
                if ((index & 255) < 240) increase(index + 16, value);
                if (index >= 256) increase(index - 256, value);
                if (index < 65280) increase(index + 256, value);
            }
        }

        private int store() {
            int changed = 0;
            for (int sectionIndex = 0; sectionIndex < storage.length && dirtySections != 0; sectionIndex++) {
                int flag = 1 << sectionIndex;
                if ((dirtySections & flag) == 0) continue;
                dirtySections &= ~flag;
                ExtendedBlockStorage section = storage[sectionIndex];
                if (section == null) storage[sectionIndex] = section = new ExtendedBlockStorage(sectionIndex * 16, false);
                byte[] stored = section.getBlockLight().getData();
                int base = sectionIndex << 12;
                for (int packed = 0; packed < stored.length; packed++) {
                    byte value = (byte) (light[base + packed * 2] | light[base + packed * 2 + 1] << 4);
                    if (stored[packed] == value) continue;
                    stored[packed] = value;
                    changed |= flag;
                }
            }
            if (changed != 0) chunk.markDirty();
            chunk = null;
            storage = null;
            return changed;
        }
    }
}
