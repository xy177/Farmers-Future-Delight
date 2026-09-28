package xy177.farmersfuturedelight.common.worldgen;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.core.FFDHeightHooks;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDOceanStructures implements FFDOceanStructureLocator.Locator {
    public static final ResourceLocation SHIPWRECK_MAP_LOOT = loot("chests/shipwreck_map");
    public static final ResourceLocation SHIPWRECK_SUPPLY_LOOT = loot("chests/shipwreck_supply");
    public static final ResourceLocation SHIPWRECK_TREASURE_LOOT = loot("chests/shipwreck_treasure");
    public static final ResourceLocation OCEAN_RUIN_SMALL_LOOT = loot("chests/underwater_ruin_small");
    public static final ResourceLocation OCEAN_RUIN_BIG_LOOT = loot("chests/underwater_ruin_big");
    public static final ResourceLocation BURIED_TREASURE_LOOT = loot("chests/buried_treasure");

    private static final FFDOceanStructures INSTANCE = new FFDOceanStructures();
    private static final Map<World, Generators> GENERATORS = new WeakHashMap<>();
    private static final Map<World, Boolean> LOADED_WORLDS = new WeakHashMap<>();
    private static boolean initialized;

    private FFDOceanStructures() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        MapGenStructureIO.registerStructure(MapGenFFDShipwreck.Start.class, "FFDShipwreck");
        MapGenStructureIO.registerStructureComponent(MapGenFFDShipwreck.Piece.class, "FFDShipwreckPiece");
        MapGenStructureIO.registerStructure(MapGenFFDOceanRuin.Start.class, "FFDOceanRuin");
        MapGenStructureIO.registerStructureComponent(MapGenFFDOceanRuin.Piece.class, "FFDOceanRuinPiece");
        MapGenStructureIO.registerStructure(MapGenFFDBuriedTreasure.Start.class, "FFDBuriedTreasure");
        MapGenStructureIO.registerStructureComponent(MapGenFFDBuriedTreasure.Piece.class,
                "FFDBuriedTreasurePiece");
        LootFunctionManager.registerFunction(new FFDBuriedTreasureMapFunction.Serializer());
        LootFunctionManager.registerFunction(new FFDEffectiveAquaticItemFunction.Serializer());
        FFDOceanStructureLocator.setLocator(INSTANCE);
    }

    public static void generate(World world, int chunkX, int chunkZ) {
        if (world == null || world.isRemote || world.provider.getDimension() != 0
                || !world.getWorldInfo().isMapFeaturesEnabled()) {
            return;
        }
        Generators generators = generators(world);
        generators.prepare(world, chunkX, chunkZ);
        generators.generate(world, chunkX, chunkZ);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (!world.isRemote && world.provider.getDimension() == 0) {
            synchronized (LOADED_WORLDS) {
                LOADED_WORLDS.put(world, Boolean.TRUE);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }
        synchronized (LOADED_WORLDS) {
            if (LOADED_WORLDS.remove(event.world) == null) {
                return;
            }
        }
        if (event.world.getWorldInfo().isMapFeaturesEnabled()
                && FFDCompat.shouldGenerateShipwreck()) {
            generators(event.world).shipwreck.resumePending(event.world);
        }
    }

    @Override
    @Nullable
    public BlockPos findDolphinTreasure(World world, BlockPos origin, int radius) {
        Generators generators = generators(world);
        if (!FFDCompat.shouldGenerateShipwreck()) {
            return withOceanFloor(world,
                    generators.ruin.findNearest(world, origin, false, radius));
        }
        boolean ruinFirst = world.rand.nextBoolean();
        BlockPos result = ruinFirst
                ? generators.ruin.findNearest(world, origin, false, radius)
                : generators.shipwreck.findNearest(world, origin, false, radius);
        if (result == null) {
            result = ruinFirst
                    ? generators.shipwreck.findNearest(world, origin, false, radius)
                    : generators.ruin.findNearest(world, origin, false, radius);
        }
        return withOceanFloor(world, result);
    }

    @Override
    @Nullable
    public BlockPos findBuriedTreasure(World world, BlockPos origin, int radius) {
        return generators(world).buriedTreasure.findNearest(world, origin, false, radius);
    }

    static boolean isOcean(Biome biome) {
        return biome != null && BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN);
    }

    static boolean isBeach(Biome biome) {
        return biome != null && BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH);
    }

    static boolean isWarmOcean(Biome biome) {
        if (biome instanceof xy177.farmersfuturedelight.common.biome.BiomeModernOcean) {
            xy177.farmersfuturedelight.common.biome.BiomeModernOcean.Type type =
                    ((xy177.farmersfuturedelight.common.biome.BiomeModernOcean) biome).getType();
            return type == xy177.farmersfuturedelight.common.biome.BiomeModernOcean.Type.WARM
                    || type == xy177.farmersfuturedelight.common.biome.BiomeModernOcean.Type.LUKEWARM
                    || type == xy177.farmersfuturedelight.common.biome.BiomeModernOcean.Type.DEEP_LUKEWARM;
        }
        ResourceLocation id = biome == null ? null : biome.getRegistryName();
        String path = id == null ? "" : id.getResourcePath();
        return path.contains("warm") && !path.contains("cold")
                || biome != null && biome.getDefaultTemperature() > 0.5F;
    }

    public static boolean isOceanLootTable(ResourceLocation table) {
        return SHIPWRECK_MAP_LOOT.equals(table) || SHIPWRECK_SUPPLY_LOOT.equals(table)
                || SHIPWRECK_TREASURE_LOOT.equals(table)
                || OCEAN_RUIN_SMALL_LOOT.equals(table) || OCEAN_RUIN_BIG_LOOT.equals(table)
                || BURIED_TREASURE_LOOT.equals(table);
    }

    static int oceanFloorY(World world, int x, int z) {
        if (!world.isBlockLoaded(new BlockPos(x, 0, z), false)) {
            return world.getSeaLevel();
        }
        for (int y = maxY(world); y >= minY(world); y--) {
            if (world.getBlockState(new BlockPos(x, y, z)).getMaterial().isSolid()) {
                return y;
            }
        }
        return world.getSeaLevel();
    }

    static int minY(World world) {
        return FFDHeightHooks.minY(world);
    }

    static int maxY(World world) {
        return FFDHeightHooks.maxYExclusive(world) - 1;
    }

    static void waterlogStructureBlocks(World world, StructureBoundingBox structureBounds,
                                        StructureBoundingBox generationBounds, boolean underwater) {
        if (!underwater || world == null || structureBounds == null || generationBounds == null) {
            return;
        }
        int minX = Math.max(structureBounds.minX, generationBounds.minX);
        int minY = Math.max(structureBounds.minY, generationBounds.minY);
        int minZ = Math.max(structureBounds.minZ, generationBounds.minZ);
        int maxX = Math.min(structureBounds.maxX, generationBounds.maxX);
        int maxY = Math.min(Math.min(structureBounds.maxY, generationBounds.maxY),
                world.getSeaLevel() - 1);
        int maxZ = Math.min(structureBounds.maxZ, generationBounds.maxZ);
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return;
        }
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    if (WaterloggedBlockApi.isWaterlogged(state)) {
                        continue;
                    }
                    IBlockState waterlogged = WaterloggedBlockApi.withWaterlogged(state, true);
                    if (waterlogged != null && waterlogged != state) {
                        world.setBlockState(pos, waterlogged, 2);
                    }
                }
            }
        }
    }

    static BlockPos pivotOffset(Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_90:
                return new BlockPos(19, 0, 11);
            case CLOCKWISE_180:
                return new BlockPos(8, 0, 30);
            case COUNTERCLOCKWISE_90:
                return new BlockPos(-11, 0, 19);
            default:
                return BlockPos.ORIGIN;
        }
    }

    @Nullable
    static EntityLiving createDrowned(World world) {
        if (!FFDEntities.isDrownedEnabled()) {
            return null;
        }
        ResourceLocation id = FFDEntities.isLocalDrownedEnabled() ? FFDEntities.DROWNED_ID : null;
        if (id == null) {
            EntityEntry entry = FFDCompat.getExternalEntityEntry(FFDCompat.Feature.DROWNED, "drowned");
            id = entry == null ? null : entry.getRegistryName();
        }
        Entity entity = id == null ? null : EntityList.createEntityByIDFromName(id, world);
        return entity instanceof EntityLiving ? (EntityLiving) entity : null;
    }

    static void fillTreasureMapLoot(TileEntityChest chest, ResourceLocation table,
                                    BlockPos origin, long seed) {
        FFDBuriedTreasureMapFunction.begin(origin);
        try {
            chest.setLootTable(table, seed);
            chest.fillWithLoot(null);
        } finally {
            FFDBuriedTreasureMapFunction.end();
        }
    }

    private static ResourceLocation loot(String path) {
        return LootTableList.register(new ResourceLocation(FarmerFutureDelight.MODID, path));
    }

    private static synchronized Generators generators(World world) {
        Generators generators = GENERATORS.get(world);
        if (generators == null) {
            generators = new Generators();
            GENERATORS.put(world, generators);
        }
        return generators;
    }

    @Nullable
    private static BlockPos withOceanFloor(World world, @Nullable BlockPos pos) {
        return pos == null ? null : new BlockPos(pos.getX(), oceanFloorY(world, pos.getX(), pos.getZ()),
                pos.getZ());
    }

    private static final class Generators {
        private final MapGenFFDShipwreck shipwreck = new MapGenFFDShipwreck();
        private final MapGenFFDOceanRuin ruin = new MapGenFFDOceanRuin();
        private final MapGenFFDBuriedTreasure buriedTreasure = new MapGenFFDBuriedTreasure();

        private void prepare(World world, int chunkX, int chunkZ) {
            if (FFDCompat.shouldGenerateShipwreck()) {
                shipwreck.generate(world, chunkX, chunkZ, null);
            }
            ruin.generate(world, chunkX, chunkZ, null);
            buriedTreasure.generate(world, chunkX, chunkZ, null);
        }

        private void generate(World world, int chunkX, int chunkZ) {
            ChunkPos chunk = new ChunkPos(chunkX, chunkZ);
            if (FFDCompat.shouldGenerateShipwreck()) {
                shipwreck.generateStructure(world,
                        random(world, chunkX, chunkZ, 165745295L), chunk);
            }
            ruin.generateStructure(world, random(world, chunkX, chunkZ, 14357621L), chunk);
            buriedTreasure.generateStructure(world,
                    random(world, chunkX, chunkZ, 10387320L), chunk);
        }

        private static Random random(World world, int chunkX, int chunkZ, long salt) {
            return new Random(world.getSeed() ^ (long) chunkX * 341873128712L
                    ^ (long) chunkZ * 132897987541L ^ salt);
        }
    }
}
