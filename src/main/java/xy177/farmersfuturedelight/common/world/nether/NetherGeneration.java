package xy177.farmersfuturedelight.common.world.nether;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class NetherGeneration {
    public static final int MIN_Y = 0;
    public static final int BUILD_HEIGHT = 256;
    public static final int GENERATION_HEIGHT = 128;
    public static final int SEA_LEVEL = 32;
    private static final String DATA_NAME = "ffd_nether_generation";
    private static final String ENGINE = "modern_nether_v1";
    private static final long PLAYER_CHUNK_BUDGET_NANOS = 10000000L;
    private static final Map<World, NetherBiomeSource> SOURCES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<World, Long> PLAYER_DEADLINES = new WeakHashMap<>();

    private NetherGeneration() { }

    public static IChunkGenerator create(World world, boolean structures, long seed) {
        if (FFDConfig.modernNetherGenerationMode == FFDConfig.FeatureMode.DISABLED
                || world.provider.getClass() != WorldProviderHell.class
                || FFDConfig.modernNetherGenerationMode == FFDConfig.FeatureMode.AUTO
                && net.minecraftforge.fml.common.Loader.isModLoaded("nether_api")) {
            return new ChunkGeneratorHell(world, structures, seed);
        }
        MapStorage storage = new MapStorage(world.getSaveHandler());
        GenerationData data = (GenerationData) storage.getOrLoadData(GenerationData.class, DATA_NAME);
        if (data == null) {
            data = new GenerationData(DATA_NAME);
            File region = new File(world.getSaveHandler().getWorldDirectory(), "DIM-1/region");
            File[] existing = region.listFiles((directory, name) -> name.endsWith(".mca"));
            data.engine = existing == null || existing.length == 0 ? ENGINE : "legacy";
            storage.setData(DATA_NAME, data);
            data.markDirty();
            storage.saveAllData();
        }
        if (!ENGINE.equals(data.engine) && FFDConfig.modernNetherGenerationMode != FFDConfig.FeatureMode.ENABLED) {
            return new ChunkGeneratorHell(world, structures, seed);
        }
        if (!ENGINE.equals(data.engine)) {
            data.engine = ENGINE;
            data.markDirty();
            storage.saveAllData();
        }
        NetherBiomeSource source = new NetherBiomeSource(seed);
        SOURCES.put(world, source);
        return new ChunkGeneratorNether(world, structures, seed, source);
    }

    public static boolean isModern(World world) {
        return world != null && SOURCES.containsKey(world);
    }

    public static NetherBiomeSource source(World world) {
        return SOURCES.get(world);
    }

    public static void beginPlayerTick(World world) {
        if (world.provider.getDimension() == -1 && isModern(world)) {
            PLAYER_DEADLINES.put(world, System.nanoTime() + PLAYER_CHUNK_BUDGET_NANOS);
        }
    }

    public static boolean canPreparePlayerChunk(World world) {
        if (world.provider.getDimension() != -1) return true;
        Long deadline = PLAYER_DEADLINES.get(world);
        return deadline == null || System.nanoTime() < deadline;
    }

    public static void activateClient(World world) {
        if (world.isRemote && world.provider.getDimension() == -1) {
            synchronized (SOURCES) {
                if (!SOURCES.containsKey(world)) SOURCES.put(world, new NetherBiomeSource(world.getSeed()));
            }
        }
    }

    public static final class GenerationData extends WorldSavedData {
        private String engine;

        public GenerationData(String name) { super(name); }
        @Override public void readFromNBT(NBTTagCompound data) { engine = data.getString("Engine"); }
        @Override public NBTTagCompound writeToNBT(NBTTagCompound data) { data.setString("Engine", engine); return data; }
    }
}
