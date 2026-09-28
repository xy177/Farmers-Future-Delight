package xy177.farmersfuturedelight.common.fluid;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.network.FFDNetwork;

public final class FFDStoredFluidStates {
    public static final String TAG = "FFDContainedStates";
    private static final Map<World, Map<Long, Map<Long, NBTTagCompound>>> DATA = new WeakHashMap<>();
    private static final Map<Chunk, Map<Long, NBTTagCompound>> UNLOADED = new WeakHashMap<>();
    private static final Map<World, Set<Long>> DIRTY = new WeakHashMap<>();
    private static final Set<Block> RENDER_HOSTS =
            Collections.newSetFromMap(new ConcurrentHashMap<Block, Boolean>());

    private FFDStoredFluidStates() {}

    private static long key(int x, int z) {
        return x & 0xffffffffL | (long) z << 32;
    }

    @Nullable
    public static synchronized IBlockState get(IBlockAccess access, BlockPos pos) {
        World world = FFDFluidloggedData.resolveWorld(access);
        if (world == null) return null;
        Map<Long, Map<Long, NBTTagCompound>> chunks = DATA.get(world);
        Map<Long, NBTTagCompound> data = chunks == null ? null
                : chunks.get(key(pos.getX() >> 4, pos.getZ() >> 4));
        NBTTagCompound entry = data == null ? null : data.get(pos.toLong());
        if (entry == null || !entry.getString("Host").equals(
                String.valueOf(access.getBlockState(pos).getBlock().getRegistryName()))) return null;
        try {
            IBlockState fluid = NBTUtil.readBlockState(entry.getCompoundTag("State"));
            return WaterloggedBlockApi.getFluidForBlock(fluid.getBlock()) == null
                    ? net.minecraft.init.Blocks.AIR.getDefaultState() : fluid;
        } catch (RuntimeException invalid) {
            return net.minecraft.init.Blocks.AIR.getDefaultState();
        }
    }

    public static boolean has(IBlockAccess access, BlockPos pos) {
        return get(access, pos) != null;
    }

    public static int level(IBlockAccess access, BlockPos pos) {
        IBlockState state = get(access, pos);
        if (state == null) return 0;
        if (state.getPropertyKeys().contains(net.minecraft.block.BlockLiquid.LEVEL))
            return state.getValue(net.minecraft.block.BlockLiquid.LEVEL);
        if (state.getPropertyKeys().contains(net.minecraftforge.fluids.BlockFluidBase.LEVEL))
            return state.getValue(net.minecraftforge.fluids.BlockFluidBase.LEVEL);
        return state.getBlock().getMetaFromState(state);
    }

    public static IBlockState changed(IBlockState old, Chunk chunk, BlockPos pos, IBlockState next) {
        if (old != null && (old.getBlock() != next.getBlock()
                || WaterloggedBlockApi.isWaterlogged(old) && !WaterloggedBlockApi.isWaterlogged(next))) {
            remove(chunk.getWorld(), pos);
            if (!chunk.getWorld().isRemote) FFDFluidMigration.hostChanged(chunk, pos);
        }
        if (old != null && !chunk.getWorld().isRemote
                && WaterloggedBlockApi.getFluidForBlock(old.getBlock()) != null
                && xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid.isSourceFluid(old)
                && xy177.farmersfuturedelight.common.FFDConfig.isAdditionalWaterloggingBlock(next.getBlock())
                && xy177.farmersfuturedelight.common.FFDConfig.isFluidAllowed(
                        WaterloggedBlockApi.getFluidForBlock(old.getBlock()))) {
            put(chunk, pos, old);
            sync(chunk.getWorld(), pos);
            chunk.getWorld().scheduleUpdate(pos, next.getBlock(),
                    WaterloggedBlockApi.fluidTickRate(chunk.getWorld(),
                            WaterloggedBlockApi.getFluidForBlock(old.getBlock())));
        }
        return old;
    }

    public static boolean mayContain(Block block) {
        return RENDER_HOSTS.contains(block)
                || xy177.farmersfuturedelight.common.FFDConfig.isAdditionalWaterloggingBlock(block);
    }

    public static synchronized void put(Chunk chunk, BlockPos pos, IBlockState fluid) {
        NBTTagCompound entry = new NBTTagCompound();
        entry.setLong("Pos", pos.toLong());
        Block host = chunk.getBlockState(pos).getBlock();
        entry.setString("Host", String.valueOf(host.getRegistryName()));
        entry.setTag("State", NBTUtil.writeBlockState(new NBTTagCompound(), fluid));
        DATA.computeIfAbsent(chunk.getWorld(), ignored -> new HashMap<>())
                .computeIfAbsent(key(chunk.x, chunk.z), ignored -> new LinkedHashMap<>())
                .put(pos.toLong(), entry);
        RENDER_HOSTS.add(host);
        chunk.markDirty();
    }

    public static void set(World world, BlockPos pos, IBlockState fluid) {
        put(world.getChunkFromBlockCoords(pos), pos, fluid);
        sync(world, pos);
    }

    public static synchronized void remove(World world, BlockPos pos) {
        Map<Long, Map<Long, NBTTagCompound>> chunks = DATA.get(world);
        Map<Long, NBTTagCompound> data = chunks == null ? null
                : chunks.get(key(pos.getX() >> 4, pos.getZ() >> 4));
        if (data != null && data.remove(pos.toLong()) != null) {
            if (world.isBlockLoaded(pos)) {
                world.getChunkFromBlockCoords(pos).markDirty();
                sync(world, pos);
            }
        }
    }

    public static synchronized void sync(World world, BlockPos pos) {
        if (!world.isRemote) {
            DIRTY.computeIfAbsent(world, ignored -> new java.util.HashSet<>())
                    .add(key(pos.getX() >> 4, pos.getZ() >> 4));
            world.checkLight(pos);
        } else {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @SubscribeEvent
    public static synchronized void flush(net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END
                || event.world.isRemote) return;
        Set<Long> dirty = DIRTY.remove(event.world);
        if (dirty != null) for (long key : dirty)
            FFDNetwork.sendStoredFluidChunk(event.world, (int) key, (int) (key >> 32),
                    snapshot(event.world, (int) key, (int) (key >> 32)));
    }

    public static synchronized NBTTagCompound snapshot(World world, int x, int z) {
        Map<Long, Map<Long, NBTTagCompound>> chunks = DATA.get(world);
        return encode(chunks == null ? null : chunks.get(key(x, z)));
    }

    private static NBTTagCompound encode(Map<Long, NBTTagCompound> entries) {
        NBTTagList list = new NBTTagList();
        if (entries != null) for (NBTTagCompound entry : entries.values()) list.appendTag(entry.copy());
        NBTTagCompound result = new NBTTagCompound();
        result.setTag("Entries", list);
        return result;
    }

    private static synchronized void read(World world, int x, int z, NBTTagCompound root, boolean replace) {
        Map<Long, Map<Long, NBTTagCompound>> chunks = DATA.computeIfAbsent(world, ignored -> new HashMap<>());
        Map<Long, NBTTagCompound> entries = replace ? new LinkedHashMap<>()
                : chunks.getOrDefault(key(x, z), new LinkedHashMap<>());
        NBTTagList list = root.getTagList("Entries", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            BlockPos pos = BlockPos.fromLong(entry.getLong("Pos"));
            if ((pos.getX() >> 4) != x || (pos.getZ() >> 4) != z) continue;
            entries.put(pos.toLong(), entry.copy());
            Block block = Block.getBlockFromName(entry.getString("Host"));
            if (block != null) RENDER_HOSTS.add(block);
        }
        chunks.put(key(x, z), entries);
    }

    public static void receive(World world, int x, int z, NBTTagCompound root, boolean replace) {
        if (root.hasKey("Policy", 10))
            xy177.farmersfuturedelight.common.FFDConfig.receiveFluidPolicy(world, root.getCompoundTag("Policy"));
        if (!root.hasKey("Entries", 9)) return;
        read(world, x, z, root, replace);
        world.markBlockRangeForRenderUpdate(x << 4, -64, z << 4, (x << 4) + 15, 319,
                (z << 4) + 15);
    }

    @SubscribeEvent
    public static void load(ChunkDataEvent.Load event) {
        read(event.getWorld(), event.getChunk().x, event.getChunk().z,
                event.getData().getCompoundTag(TAG), true);
        FFDFluidMigration.load(event);
    }

    @SubscribeEvent
    public static synchronized void save(ChunkDataEvent.Save event) {
        if (event.getWorld().isRemote) return;
        Chunk chunk = event.getChunk();
        NBTTagCompound root = UNLOADED.containsKey(chunk)
                ? encode(UNLOADED.get(chunk)) : snapshot(event.getWorld(), chunk.x, chunk.z);
        event.getData().setTag(TAG, root);
        FFDFluidMigration.save(event);
    }

    @SubscribeEvent
    public static void watch(ChunkWatchEvent.Watch event) {
        Chunk chunk = event.getChunkInstance();
        if (chunk != null) FFDNetwork.sendStoredFluidChunk(event.getPlayer(), chunk.x, chunk.z,
                snapshot(chunk.getWorld(), chunk.x, chunk.z));
    }

    @SubscribeEvent
    public static synchronized void unload(ChunkEvent.Unload event) {
        Map<Long, Map<Long, NBTTagCompound>> chunks = DATA.get(event.getWorld());
        if (chunks != null) {
            Map<Long, NBTTagCompound> entries = chunks.remove(key(event.getChunk().x, event.getChunk().z));
            if (entries != null && !event.getWorld().isRemote) UNLOADED.put(event.getChunk(), entries);
        }
    }

    @SubscribeEvent
    public static synchronized void reload(ChunkEvent.Load event) {
        Map<Long, NBTTagCompound> entries = UNLOADED.remove(event.getChunk());
        if (entries != null) DATA.computeIfAbsent(event.getWorld(), ignored -> new HashMap<>())
                .put(key(event.getChunk().x, event.getChunk().z), entries);
        if (!event.getWorld().isRemote) {
            Map<Long, Map<Long, NBTTagCompound>> chunks = DATA.get(event.getWorld());
            Map<Long, NBTTagCompound> loaded = chunks == null ? null
                    : chunks.get(key(event.getChunk().x, event.getChunk().z));
            if (loaded != null) for (long packed : loaded.keySet()) {
                BlockPos pos = BlockPos.fromLong(packed);
                event.getWorld().scheduleUpdate(pos, event.getChunk().getBlockState(pos).getBlock(), 5);
            }
        }
        FFDFluidMigration.queue(event.getChunk());
    }

    @SubscribeEvent
    public static synchronized void unloadWorld(WorldEvent.Unload event) {
        DATA.remove(event.getWorld());
        DIRTY.remove(event.getWorld());
        UNLOADED.keySet().removeIf(chunk -> chunk.getWorld() == event.getWorld());
        FFDFluidMigration.unload(event.getWorld());
    }
}
