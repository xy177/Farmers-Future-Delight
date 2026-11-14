package xy177.farmersfuturedelight.common.world.nether;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.entity.EntityHoglin;
import xy177.farmersfuturedelight.common.entity.EntityPiglin;
import xy177.farmersfuturedelight.common.entity.EntityStrider;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class NetherSpawns {
    private static final Map<FFDVerticalBiome, Table> TABLES = new HashMap<>();
    private static final FFDVerticalBiome[] BIOMES = {FFDVerticalBiome.NETHER_WASTES, FFDVerticalBiome.SOUL_SAND_VALLEY,
            FFDVerticalBiome.CRIMSON_FOREST, FFDVerticalBiome.WARPED_FOREST, FFDVerticalBiome.BASALT_DELTAS};
    private static final Map<World, Potential> POTENTIALS = new WeakHashMap<>();
    private static final Set<Class<?>> REPLACED = new HashSet<>();
    private static EntityLiving.SpawnPlacementType lavaPlacement;

    static {
        java.util.Collections.addAll(REPLACED, EntityGhast.class, EntityPigZombie.class,
                EntityMagmaCube.class, EntityEnderman.class, EntitySkeleton.class,
                EntityPiglin.class, EntityHoglin.class, EntityStrider.class);
    }

    private NetherSpawns() { }

    public static EntityLiving.SpawnPlacementType lavaPlacement() {
        if (lavaPlacement == null) lavaPlacement = EnumHelper.addSpawnPlacementType("FFD_IN_LAVA",
                (world, pos) -> world.getBlockState(pos).getMaterial() == Material.LAVA);
        return lavaPlacement;
    }

    public static List<Biome.SpawnListEntry> get(FFDVerticalBiome biome, EnumCreatureType type) {
        Table table = table(biome);
        if (table == null) return new ArrayList<>(NetherBiomeSource.legacy(biome).getSpawnableList(type));
        List<Biome.SpawnListEntry> result = new ArrayList<>();
        List<Biome.SpawnListEntry> modern = table.spawns.get(type);
        if (modern != null) result.addAll(modern);
        for (Biome.SpawnListEntry entry : NetherBiomeSource.legacy(biome).getSpawnableList(type)) {
            if (!REPLACED.contains(entry.entityClass)) result.add(entry);
        }
        return result;
    }

    private static Table table(FFDVerticalBiome biome) {
        if (TABLES.isEmpty()) for (FFDVerticalBiome entry : BIOMES) TABLES.put(entry, load(entry));
        return TABLES.get(biome);
    }

    private static Table load(FFDVerticalBiome biome) {
        Table result = new Table();
        JsonObject definition = NetherMaterialNoise.resource("biome/" + biome.registryPath())
                .getAsJsonObject("attributes").getAsJsonObject("minecraft:gameplay/natural_mob_spawns")
                .getAsJsonObject("argument");
        for (Map.Entry<String, JsonElement> category : definition.getAsJsonObject("spawns_by_category").entrySet()) {
            EnumCreatureType type = EnumCreatureType.valueOf(category.getKey().toUpperCase(java.util.Locale.ROOT));
            List<Biome.SpawnListEntry> entries = new ArrayList<>();
            for (JsonElement element : category.getValue().getAsJsonArray()) {
                JsonObject entry = element.getAsJsonObject();
                Class<? extends EntityLiving> entity = entity(entry.get("type").getAsString());
                if (entity == null) continue;
                JsonElement count = entry.get("count");
                int minimum = count.isJsonPrimitive() ? count.getAsInt() : count.getAsJsonObject().get("min_inclusive").getAsInt();
                int maximum = count.isJsonPrimitive() ? minimum : count.getAsJsonObject().get("max_inclusive").getAsInt();
                entries.add(new Biome.SpawnListEntry(entity, entry.get("weight").getAsInt(), minimum, maximum));
                REPLACED.add(entity);
            }
            result.spawns.put(type, entries);
        }
        if (definition.has("spawn_costs")) for (Map.Entry<String, JsonElement> entry : definition.getAsJsonObject("spawn_costs").entrySet()) {
            Class<? extends EntityLiving> entity = entity(entry.getKey());
            JsonObject cost = entry.getValue().getAsJsonObject();
            if (entity != null) result.costs.put(entity, new Cost(cost.get("charge").getAsDouble(), cost.get("energy_budget").getAsDouble()));
        }
        return result;
    }

    private static Class<? extends EntityLiving> entity(String name) {
        String path = new ResourceLocation(name).getResourcePath();
        if ("zombified_piglin".equals(path)) return EntityPigZombie.class;
        EntityEntry entry;
        if ("piglin".equals(path)) {
            if (FFDEntities.isLocalPiglinEnabled()) return EntityPiglin.class;
            entry = FFDCompat.getExternalEntityEntry(FFDCompat.Feature.PIGLIN, path);
        } else if ("hoglin".equals(path)) {
            if (FFDEntities.isLocalHoglinEnabled()) return EntityHoglin.class;
            entry = FFDCompat.getExternalEntityEntry(FFDCompat.Feature.HOGLIN, path);
        } else if ("strider".equals(path)) {
            if (FFDEntities.isLocalStriderEnabled()) return EntityStrider.class;
            entry = FFDCompat.getExternalEntityEntry(FFDCompat.Feature.STRIDER, path);
        } else entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name));
        return entry != null && EntityLiving.class.isAssignableFrom(entry.getEntityClass())
                ? entry.getEntityClass().asSubclass(EntityLiving.class) : null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onNaturalSpawn(LivingSpawnEvent.CheckSpawn event) {
        World world = event.getWorld();
        if (world.isRemote || event.isSpawner() || event.getResult() == Event.Result.DENY || !NetherGeneration.isModern(world)) return;
        BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
        Cost cost = cost(world, event.getEntityLiving().getClass(), pos);
        if (cost == null) return;
        Potential potential = POTENTIALS.get(world);
        if (potential == null || potential.tick != world.getTotalWorldTime()) {
            potential = new Potential(world.getTotalWorldTime());
            for (Entity entity : world.loadedEntityList) potential.add(world, entity);
            POTENTIALS.put(world, potential);
        }
        double energy = 0;
        for (Point point : potential.points) {
            double distance = point.pos.distanceSq(pos);
            if (distance == 0) { event.setResult(Event.Result.DENY); return; }
            energy += point.charge / Math.sqrt(distance);
        }
        if (energy * cost.charge > cost.budget) event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !NetherGeneration.isModern(event.getWorld())) return;
        Potential potential = POTENTIALS.get(event.getWorld());
        if (potential != null && potential.tick == event.getWorld().getTotalWorldTime()) potential.add(event.getWorld(), event.getEntity());
    }

    private static Cost cost(World world, Class<?> type, BlockPos pos) {
        Table table = table(NetherGeneration.source(world).get(pos.getX(), pos.getY(), pos.getZ()));
        return table == null ? null : table.costs.get(type);
    }

    private static final class Table {
        final Map<EnumCreatureType, List<Biome.SpawnListEntry>> spawns = new EnumMap<>(EnumCreatureType.class);
        final Map<Class<?>, Cost> costs = new HashMap<>();
    }

    private static final class Cost {
        final double charge;
        final double budget;
        Cost(double charge, double budget) { this.charge = charge; this.budget = budget; }
    }

    private static final class Point {
        final BlockPos pos;
        final double charge;
        Point(BlockPos pos, double charge) { this.pos = pos; this.charge = charge; }
    }

    private static final class Potential {
        final long tick;
        final List<Point> points = new ArrayList<>();
        Potential(long tick) { this.tick = tick; }
        void add(World world, Entity entity) {
            if (!(entity instanceof EntityLiving) || ((EntityLiving) entity).isNoDespawnRequired()) return;
            BlockPos pos = entity.getPosition();
            Cost cost = cost(world, entity.getClass(), pos);
            if (cost != null && cost.charge != 0) points.add(new Point(pos, cost.charge));
        }
    }
}
