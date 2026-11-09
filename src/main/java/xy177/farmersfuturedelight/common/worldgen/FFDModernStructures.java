package xy177.farmersfuturedelight.common.worldgen;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.core.FFDHeightHooks;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.world.ChunkGeneratorExtended;
import xy177.farmersfuturedelight.common.world.FFDWorldTypes;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDModernStructures {
    private static final Map<World, MapGenFFDPillagerOutpost> OUTPOSTS = new WeakHashMap<>();
    private static final Map<World, Set<FFDJigsawStructureStart>> PENDING = new WeakHashMap<>();
    private static final Set<World> CHANGED = Collections.newSetFromMap(new WeakHashMap<World, Boolean>());
    private static Biome.SpawnListEntry outpostSpawn;
    private static boolean initialized;

    private FFDModernStructures() {
    }

    @Nullable
    private static Class<? extends EntityLiving> pillagerType() {
        if (FFDEntities.isLocalPillagerEnabled()) return EntityPillager.class;
        if (FFDConfig.pillagerMode != FFDConfig.FeatureMode.AUTO) return null;
        EntityEntry external = FFDCompat.getExternalEntityEntry(FFDCompat.Feature.PILLAGER, "pillager");
        Class<? extends Entity> type = external == null ? null : external.getEntityClass();
        return type != null && EntityLiving.class.isAssignableFrom(type) ? type.asSubclass(EntityLiving.class) : null;
    }

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        MapGenStructureIO.registerStructure(FFDJigsawStructureStart.class, "FFDJigsawStructure");
        MapGenStructureIO.registerStructureComponent(FFDJigsawPlacement.SavedPiece.class, "FFDJigsawPiece");
    }

    private static boolean supported(World world) {
        if (!(world instanceof WorldServer) || world.provider.getDimension() != 0
                || !world.getWorldInfo().isMapFeaturesEnabled()) return false;
        return supported(world, FFDStructureTerrain.generator((WorldServer) world));
    }

    static boolean supported(World world, IChunkGenerator generator) {
        if (!(world instanceof WorldServer) || world.provider.getDimension() != 0
                || !world.getWorldInfo().isMapFeaturesEnabled() || generator == null) return false;
        WorldType type = world.getWorldType();
        boolean standardWorldType = type == WorldType.DEFAULT || type == WorldType.DEFAULT_1_1
                || type == WorldType.LARGE_BIOMES || type == WorldType.AMPLIFIED
                || type == WorldType.CUSTOMIZED || type == FFDWorldTypes.EXTENDED;
        boolean standardGenerator = generator.getClass() == ChunkGeneratorOverworld.class
                || generator instanceof ChunkGeneratorExtended;
        if (standardWorldType && standardGenerator) return true;
        return "BIOMESOP".equals(type.getName()) && FFDStructureTerrain.supportsBiomesOPlenty(generator);
    }

    public static FFDStructureBeard prepareTerrain(World world, ChunkGeneratorOverworld generator, int chunkX, int chunkZ) {
        if (!supported(world, generator)) return FFDStructureBeard.EMPTY;
        FFDStructureBeard.Builder builder = new FFDStructureBeard.Builder(chunkX, chunkZ,
                FFDHeightHooks.minY(world), FFDHeightHooks.maxYExclusive(world));
        FFDModernVillageGenerator villages = FFDStructureHooks.villages(generator);
        if (villages != null) {
            villages.generate(world, chunkX, chunkZ, null);
            villages.addTerrain(chunkX, chunkZ, builder);
        }
        if (pillagerType() != null) {
            MapGenFFDPillagerOutpost outposts = OUTPOSTS.computeIfAbsent(world, ignored -> new MapGenFFDPillagerOutpost());
            outposts.generate(world, chunkX, chunkZ, null);
            outposts.addTerrain(chunkX, chunkZ, builder);
        }
        return builder.build();
    }

    public static boolean isOutpost(String name) {
        return "Pillager_Outpost".equals(name) || "FFDPillagerOutpost".equals(name)
                || "farmers_future_delight:pillager_outpost".equals(name);
    }

    public static BlockPos locateOutpost(World world, BlockPos position, boolean unexplored) {
        if (!supported(world) || pillagerType() == null) return null;
        return OUTPOSTS.computeIfAbsent(world, ignored -> new MapGenFFDPillagerOutpost())
                .getNearestStructurePos(world, position, unexplored);
    }

    public static boolean insideOutpost(World world, BlockPos position) {
        if (!supported(world) || pillagerType() == null) return false;
        MapGenFFDPillagerOutpost outposts = OUTPOSTS.computeIfAbsent(world, ignored -> new MapGenFFDPillagerOutpost());
        outposts.generate(world, position.getX() >> 4, position.getZ() >> 4, null);
        return outposts.isInsideStructure(position);
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (!supported(event.getWorld())) return;
        IChunkGenerator generator = FFDStructureTerrain.generator((WorldServer) event.getWorld());
        if (generator instanceof ChunkGeneratorOverworld) {
            FFDStructureHooks.installModernVillage((ChunkGeneratorOverworld) generator);
        }
        if (pillagerType() != null) OUTPOSTS.computeIfAbsent(event.getWorld(), ignored -> new MapGenFFDPillagerOutpost());
    }

    public static void generate(World world, int chunkX, int chunkZ) {
        if (!supported(world) || pillagerType() == null) return;
        MapGenFFDPillagerOutpost generator = OUTPOSTS.computeIfAbsent(world, ignored -> new MapGenFFDPillagerOutpost());
        generator.generate(world, chunkX, chunkZ, null);
        generator.generateStructure(world, new Random(world.getSeed() ^ (long) chunkX * 341873128712L
                ^ (long) chunkZ * 132897987541L ^ 165745296L), new ChunkPos(chunkX, chunkZ));
    }

    static void defer(World world, FFDJigsawStructureStart start) {
        PENDING.computeIfAbsent(world, ignored -> new HashSet<>()).add(start);
        CHANGED.add(world);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getWorld().isRemote && PENDING.containsKey(event.getWorld())) CHANGED.add(event.getWorld());
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer) || !CHANGED.remove(event.world)) return;
        Set<FFDJigsawStructureStart> pending = PENDING.get(event.world);
        if (pending == null) return;
        Iterator<FFDJigsawStructureStart> iterator = pending.iterator();
        while (iterator.hasNext()) {
            FFDJigsawStructureStart start = iterator.next();
            if (start.resume((WorldServer) event.world)) {
                MapGenStructureData data = (MapGenStructureData) event.world.getPerWorldStorage()
                        .getOrLoadData(MapGenStructureData.class, start.storage());
                if (data != null) {
                    data.writeInstance(start.writeStructureComponentsToNBT(start.getChunkPosX(), start.getChunkPosZ()),
                            start.getChunkPosX(), start.getChunkPosZ());
                    data.markDirty();
                }
            }
            if (!start.hasPending()) iterator.remove();
        }
        if (pending.isEmpty()) PENDING.remove(event.world);
    }

    @SubscribeEvent
    public static void onPotentialSpawns(WorldEvent.PotentialSpawns event) {
        MapGenFFDPillagerOutpost outpost = OUTPOSTS.get(event.getWorld());
        Class<? extends EntityLiving> pillager = pillagerType();
        if (outpost == null || event.getType() != EnumCreatureType.MONSTER || pillager == null
                || !outpost.isPositionInStructure(event.getWorld(), event.getPos())) return;
        if (outpostSpawn == null || outpostSpawn.entityClass != pillager) {
            outpostSpawn = new Biome.SpawnListEntry(pillager, 1, 1, 1);
        }
        event.getList().clear();
        event.getList().add(outpostSpawn);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        OUTPOSTS.remove(event.getWorld());
        PENDING.remove(event.getWorld());
        CHANGED.remove(event.getWorld());
    }
}
