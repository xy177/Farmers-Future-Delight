package xy177.farmersfuturedelight.common.world;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDWorldgenFluidUpdates {
    private static final int CHUNKS_PER_TICK = 8;
    private static final Map<World, LinkedHashSet<Long>> PENDING = new WeakHashMap<>();

    private FFDWorldgenFluidUpdates() {
    }

    public static void queue(World world, BlockPos pos) {
        queueChunk(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static synchronized void queueChunk(World world, int x, int z) {
        if (world.isRemote || !FFDHeightHooks.isExtended(world)) {
            return;
        }
        PENDING.computeIfAbsent(world, ignored -> new LinkedHashSet<>())
                .add(((long) z << 32) | (x & 0xFFFFFFFFL));
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote || !FFDHeightHooks.isExtended(world)) {
            return;
        }
        Chunk chunk = event.getChunk();
        for (int x = chunk.x - 1; x <= chunk.x + 1; x++) {
            for (int z = chunk.z - 1; z <= chunk.z + 1; z++) {
                if (x == chunk.x && z == chunk.z
                        || world.getChunkProvider().getLoadedChunk(x, z) != null) {
                    queueChunk(world, x, z);
                }
            }
        }
    }

    @SubscribeEvent
    public static synchronized void onChunkUnload(ChunkEvent.Unload event) {
        LinkedHashSet<Long> pending = PENDING.get(event.getWorld());
        if (pending != null) {
            Chunk chunk = event.getChunk();
            pending.remove(((long) chunk.z << 32) | (chunk.x & 0xFFFFFFFFL));
            if (pending.isEmpty()) {
                PENDING.remove(event.getWorld());
            }
        }
    }

    @SubscribeEvent
    public static synchronized void onWorldUnload(WorldEvent.Unload event) {
        PENDING.remove(event.getWorld());
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase != TickEvent.Phase.END || world.isRemote
                || !FFDHeightHooks.isExtended(world) || FFDHeightHooks.minY(world) >= 0) {
            return;
        }
        for (int i = 0; i < CHUNKS_PER_TICK; i++) {
            Long key = poll(world);
            if (key == null) {
                return;
            }
            Chunk chunk = world.getChunkProvider().getLoadedChunk((int) (long) key,
                    (int) (key >> 32));
            if (chunk != null) {
                resume(chunk);
            }
        }
    }

    private static synchronized Long poll(World world) {
        LinkedHashSet<Long> pending = PENDING.get(world);
        if (pending == null || pending.isEmpty()) {
            return null;
        }
        Iterator<Long> iterator = pending.iterator();
        Long key = iterator.next();
        iterator.remove();
        if (pending.isEmpty()) {
            PENDING.remove(world);
        }
        return key;
    }

    private static void resume(Chunk chunk) {
        World world = chunk.getWorld();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos = new BlockPos((chunk.x << 4) + x, 0, (chunk.z << 4) + z);
                IBlockState state = chunk.getBlockState(pos);
                Block block = state.getBlock();
                boolean water = block == Blocks.WATER || block == Blocks.FLOWING_WATER;
                boolean lava = block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
                if ((!water && !lava) || state.getValue(BlockLiquid.LEVEL) < 8
                        || !world.isAreaLoaded(pos, 4)) {
                    continue;
                }
                IBlockState below = chunk.getBlockState(pos.down());
                if (below.getMaterial().blocksMovement()
                        || below.getMaterial() == state.getMaterial()) {
                    continue;
                }
                Block flowing = water ? Blocks.FLOWING_WATER : Blocks.FLOWING_LAVA;
                if (block != flowing) {
                    world.setBlockState(pos, flowing.getDefaultState()
                            .withProperty(BlockLiquid.LEVEL, state.getValue(BlockLiquid.LEVEL)), 2);
                }
                world.scheduleUpdate(pos, flowing, flowing.tickRate(world));
            }
        }
    }
}
