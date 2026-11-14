package xy177.farmersfuturedelight.common.world.biome;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;
import xy177.farmersfuturedelight.core.FFDHeightHooks;
import xy177.farmersfuturedelight.common.world.nether.NetherGeneration;

public final class FFDVerticalBiomeManager {
    private static final String NBT_VERSION = "FFDVerticalBiomeVersion";
    private static final String NBT_DATA = "FFDVerticalBiomes";
    private static final int DATA_VERSION = 4;
    private static final Object LOCK = new Object();
    private static final Map<World, Map<Long, FFDVerticalBiomeData>> WORLD_DATA =
            new WeakHashMap<>();
    private static final Map<World, FFDVerticalBiomeSampler> SAMPLERS =
            new WeakHashMap<>();
    private static final Map<World, Long> ZOOM_SEEDS = new WeakHashMap<>();

    private FFDVerticalBiomeManager() {
    }

    private static boolean supported(World world) {
        return FFDHeightHooks.isExtended(world) || NetherGeneration.isModern(world);
    }

    public static void put(World world, int chunkX, int chunkZ,
                           FFDVerticalBiomeData data) {
        synchronized (LOCK) {
            if (world.isRemote && data.getZoomSeed() != null) ZOOM_SEEDS.put(world, data.getZoomSeed());
            else if (!world.isRemote && NetherGeneration.isModern(world)) data.setZoomSeed(zoomSeed(world));
            dataFor(world, true).put(chunkKey(chunkX, chunkZ), data);
        }
    }

    public static FFDVerticalBiome getBiome(World world, BlockPos pos) {
        if (!supported(world)) {
            return FFDVerticalBiome.NONE;
        }
        return FFDVerticalBiomeZoomer.getBiome(zoomSeed(world),
                pos.getX(), pos.getY(), pos.getZ(),
                (quartX, quartY, quartZ) -> rawBiome(world, quartX, quartY, quartZ));
    }

    public static boolean isBiome(World world, BlockPos pos, FFDVerticalBiome biome) {
        return getBiome(world, pos) == biome;
    }

    @SubscribeEvent
    public static void loadChunkData(ChunkDataEvent.Load event) {
        World world = event.getWorld();
        if (!supported(world)) {
            return;
        }
        Chunk chunk = event.getChunk();
        NBTTagCompound nbt = event.getData();
        FFDVerticalBiomeData stored = null;
        if (nbt.getInteger(NBT_VERSION) == DATA_VERSION && nbt.hasKey(NBT_DATA, 10)) {
            try {
                stored = FFDVerticalBiomeData.fromNBT(nbt.getCompoundTag(NBT_DATA));
            } catch (IllegalArgumentException exception) {
                org.apache.logging.log4j.LogManager.getLogger("FFD Vertical Biomes")
                        .warn("Invalid vertical biome data in chunk {}, {}", chunk.x, chunk.z,
                                exception);
            }
        } else if (nbt.getInteger(NBT_VERSION) == 3 && nbt.hasKey(NBT_DATA, 7)) {
            byte[] legacy = nbt.getByteArray(NBT_DATA);
            if (legacy.length == FFDVerticalBiomeData.ENTRY_COUNT) {
                stored = new FFDVerticalBiomeData(legacy);
            }
        }
        if (stored != null) {
            put(world, chunk.x, chunk.z, stored);
        } else if (!world.isRemote) {
            getOrGenerate(world, chunk.x, chunk.z);
        }
    }

    @SubscribeEvent
    public static void saveChunkData(ChunkDataEvent.Save event) {
        World world = event.getWorld();
        if (!supported(world) || world.isRemote) {
            return;
        }
        Chunk chunk = event.getChunk();
        FFDVerticalBiomeData data = getOrGenerate(world, chunk.x, chunk.z);
        if (data != null) {
            event.getData().setInteger(NBT_VERSION, DATA_VERSION);
            event.getData().setTag(NBT_DATA, data.toNBT());
        }
    }

    @SubscribeEvent
    public static void watchChunk(ChunkWatchEvent.Watch event) {
        Chunk chunk = event.getChunkInstance();
        if (chunk == null || !supported(chunk.getWorld())) {
            return;
        }
        FFDVerticalBiomeData data = getOrGenerate(chunk.getWorld(), chunk.x, chunk.z);
        if (data != null) {
            FFDNetwork.sendVerticalBiomes(event.getPlayer(),
                    chunk.getWorld().provider.getDimension(), chunk.x, chunk.z, data);
        }
    }

    @SubscribeEvent
    public static void unloadChunk(ChunkEvent.Unload event) {
        Chunk chunk = event.getChunk();
        synchronized (LOCK) {
            Map<Long, FFDVerticalBiomeData> worldData = dataFor(event.getWorld(), false);
            if (worldData != null) {
                worldData.remove(chunkKey(chunk.x, chunk.z));
            }
        }
    }

    @SubscribeEvent
    public static void unloadWorld(WorldEvent.Unload event) {
        synchronized (LOCK) {
            WORLD_DATA.remove(event.getWorld());
            SAMPLERS.remove(event.getWorld());
            ZOOM_SEEDS.remove(event.getWorld());
        }
    }

    public static void receiveClientData(World world, int chunkX, int chunkZ, byte[] data) {
        NetherGeneration.activateClient(world);
        if (supported(world)
                && data.length == FFDVerticalBiomeData.ENTRY_COUNT) {
            put(world, chunkX, chunkZ, new FFDVerticalBiomeData(data));
        }
    }

    public static void receiveClientData(World world, int chunkX, int chunkZ,
                                         FFDVerticalBiomeData data) {
        NetherGeneration.activateClient(world);
        if (supported(world)) {
            put(world, chunkX, chunkZ, data);
        }
    }

    @Nullable
    private static FFDVerticalBiomeData getOrGenerate(World world, int chunkX, int chunkZ) {
        synchronized (LOCK) {
            Map<Long, FFDVerticalBiomeData> worldData = dataFor(world, true);
            long key = chunkKey(chunkX, chunkZ);
            FFDVerticalBiomeData existing = worldData.get(key);
            if (existing != null || world.isRemote) {
                return existing;
            }
            if (NetherGeneration.isModern(world)) {
                FFDVerticalBiomeData generated = NetherGeneration.source(world).sampleChunk(chunkX, chunkZ);
                generated.setZoomSeed(zoomSeed(world));
                worldData.put(key, generated);
                return generated;
            }
            FFDVerticalBiomeSampler sampler = SAMPLERS.get(world);
            if (sampler == null) {
                sampler = new FFDVerticalBiomeSampler(new FFDModernWorldgenData(world.getSeed()));
                SAMPLERS.put(world, sampler);
            }
            FFDVerticalBiomeData generated = sampler.sampleChunk(chunkX, chunkZ);
            worldData.put(key, generated);
            return generated;
        }
    }

    @Nullable
    private static Map<Long, FFDVerticalBiomeData> dataFor(World world, boolean create) {
        Map<Long, FFDVerticalBiomeData> data = WORLD_DATA.get(world);
        if (data == null && create) {
            data = new HashMap<>();
            WORLD_DATA.put(world, data);
        }
        return data;
    }

    private static long zoomSeed(World world) {
        synchronized (LOCK) {
            Long seed = ZOOM_SEEDS.get(world);
            if (seed == null) {
                seed = FFDVerticalBiomeZoomer.obfuscateSeed(world.getSeed());
                ZOOM_SEEDS.put(world, seed);
            }
            return seed;
        }
    }

    private static FFDVerticalBiome rawBiome(World world, int quartX, int quartY, int quartZ) {
        if (NetherGeneration.isModern(world)) quartY = Math.max(0, Math.min(63, quartY));
        int chunkX = Math.floorDiv(quartX, FFDVerticalBiomeData.CELLS_X);
        int chunkZ = Math.floorDiv(quartZ, FFDVerticalBiomeData.CELLS_Z);
        FFDVerticalBiomeData data;
        FFDVerticalBiomeSampler sampler;
        synchronized (LOCK) {
            Map<Long, FFDVerticalBiomeData> worldData = dataFor(world, false);
            data = worldData == null ? null : worldData.get(chunkKey(chunkX, chunkZ));
            if (data != null) {
                return data.get(quartX * FFDVerticalBiomeData.CELL_SIZE,
                        quartY * FFDVerticalBiomeData.CELL_SIZE,
                        quartZ * FFDVerticalBiomeData.CELL_SIZE);
            }
            if (world.isRemote) {
                return FFDVerticalBiome.NONE;
            }
            if (NetherGeneration.isModern(world)) return NetherGeneration.source(world).sample(quartX, quartY, quartZ);
            sampler = SAMPLERS.get(world);
            if (sampler == null) {
                sampler = new FFDVerticalBiomeSampler(new FFDModernWorldgenData(world.getSeed()));
                SAMPLERS.put(world, sampler);
            }
        }
        data = sampler.sampleChunk(chunkX, chunkZ);
        return data == null ? FFDVerticalBiome.NONE : data.get(
                quartX * FFDVerticalBiomeData.CELL_SIZE,
                quartY * FFDVerticalBiomeData.CELL_SIZE,
                quartZ * FFDVerticalBiomeData.CELL_SIZE);
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return chunkX & 0xFFFFFFFFL | (long) chunkZ << 32;
    }
}
