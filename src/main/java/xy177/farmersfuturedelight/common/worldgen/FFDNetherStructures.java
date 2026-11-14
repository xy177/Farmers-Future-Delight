package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.nether.NetherGeneration;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDNetherStructures {
    private static final int SPACING = 27;
    private static final int SEPARATION = 4;
    private static final int SALT = 30084232;
    private static final Map<World, Bastion> BASTIONS = new WeakHashMap<>();
    private static final Set<ResourceLocation> LOOT_TABLES = new HashSet<>();

    private FFDNetherStructures() {
    }

    public static void init() {
        for (String name : new String[] {"bastion_bridge", "bastion_hoglin_stable", "bastion_other", "bastion_treasure", "ruined_portal"}) {
            LOOT_TABLES.add(LootTableList.register(new ResourceLocation(FarmerFutureDelight.MODID, "chests/" + name)));
        }
    }

    public static boolean isLootTable(ResourceLocation id) { return LOOT_TABLES.contains(id); }

    private static boolean bastionsEnabled() {
        return FFDConfig.bastionRemnantMode != FFDConfig.FeatureMode.DISABLED
                && FFDItems.effectiveBlockState(FFDBlocks.BLACKSTONE) != null
                && FFDItems.effectiveBlockState(FFDBlocks.BASALT) != null;
    }

    public static boolean generatesBastions(World world) {
        return world instanceof WorldServer && NetherGeneration.isModern(world) && bastionsEnabled()
                && world.getWorldInfo().isMapFeaturesEnabled();
    }

    private static boolean candidate(World world, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, SPACING);
        int regionZ = Math.floorDiv(chunkZ, SPACING);
        Random random = new Random((long) regionX * 341873128712L + (long) regionZ * 132897987541L + world.getSeed() + SALT);
        return regionX * SPACING + random.nextInt(SPACING - SEPARATION) == chunkX
                && regionZ * SPACING + random.nextInt(SPACING - SEPARATION) == chunkZ;
    }

    private static int firstChoice(World world, int chunkX, int chunkZ) {
        Random random = new Random(world.getSeed());
        long first = random.nextLong();
        long second = random.nextLong();
        random.setSeed((long) chunkX * first ^ (long) chunkZ * second ^ world.getSeed());
        return random.nextInt(5);
    }

    private static boolean bastionBiome(World world, int chunkX, int chunkZ) {
        return NetherGeneration.source(world).get(chunkX * 16 + 8, 33, chunkZ * 16 + 8) != FFDVerticalBiome.BASALT_DELTAS;
    }

    public static MapGenNetherBridge fortress() { return new Fortress(); }

    public static void prepare(World world, int chunkX, int chunkZ) {
        if (bastionsEnabled()) BASTIONS.computeIfAbsent(world, ignored -> new Bastion()).generate(world, chunkX, chunkZ, null);
    }

    public static void populate(World world, int chunkX, int chunkZ, Random random) {
        if (bastionsEnabled()) BASTIONS.computeIfAbsent(world, ignored -> new Bastion())
                .generateStructure(world, random, new ChunkPos(chunkX, chunkZ));
    }

    public static boolean isBastion(String name) {
        return "Bastion_Remnant".equals(name) || "FFDBastionRemnant".equals(name)
                || "bastion_remnant".equals(name) || "farmers_future_delight:bastion_remnant".equals(name);
    }

    @Nullable public static BlockPos locate(World world, BlockPos pos, boolean unexplored) {
        if (!generatesBastions(world)) return null;
        return BASTIONS.computeIfAbsent(world, ignored -> new Bastion()).getNearestStructurePos(world, pos, unexplored);
    }

    public static boolean inside(World world, BlockPos pos) {
        if (!generatesBastions(world)) return false;
        Bastion bastion = BASTIONS.computeIfAbsent(world, ignored -> new Bastion());
        bastion.generate(world, pos.getX() >> 4, pos.getZ() >> 4, null);
        return bastion.isInsideStructure(pos);
    }

    @SubscribeEvent public static void unload(WorldEvent.Unload event) { BASTIONS.remove(event.getWorld()); }

    private static final class Fortress extends MapGenNetherBridge {
        @Override protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
            return candidate(world, chunkX, chunkZ) && (!bastionsEnabled() || firstChoice(world, chunkX, chunkZ) < 2
                    || !bastionBiome(world, chunkX, chunkZ));
        }

        @Override public BlockPos getNearestStructurePos(World level, BlockPos pos, boolean unexplored) {
            world = level;
            return findNearestStructurePosBySpacing(level, this, pos, SPACING, SEPARATION, SALT, false, 100, unexplored);
        }
    }

    private static final class Bastion extends MapGenStructure {
        @Override public String getStructureName() { return "FFDBastionRemnant"; }
        @Override protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
            return candidate(world, chunkX, chunkZ) && firstChoice(world, chunkX, chunkZ) >= 2 && bastionBiome(world, chunkX, chunkZ);
        }
        @Override protected StructureStart getStructureStart(int chunkX, int chunkZ) {
            return new FFDJigsawStructureStart((WorldServer) world, rand, chunkX, chunkZ,
                    new ResourceLocation(FarmerFutureDelight.MODID, "bastion/starts"), 6, getStructureName(), 33, 80);
        }
        @Override public BlockPos getNearestStructurePos(World level, BlockPos pos, boolean unexplored) {
            world = level;
            return findNearestStructurePosBySpacing(level, this, pos, SPACING, SEPARATION, SALT, false, 100, unexplored);
        }
    }
}
