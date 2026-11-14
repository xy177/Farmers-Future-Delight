package xy177.farmersfuturedelight.common.world.nether;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;

public final class ChunkGeneratorNether implements IChunkGenerator {
    private final World world;
    private final long seed;
    private final boolean structures;
    private final NetherBiomeSource biomes;
    private final MapGenNetherBridge fortress;
    private final NetherSurface surface;
    private final NetherCarvers carvers;
    private final NetherDecoration decoration;
    private final NetherFossils fossils;

    public ChunkGeneratorNether(World world, boolean structures, long seed, NetherBiomeSource biomes) {
        this.world = world;
        this.structures = structures;
        this.seed = seed;
        this.biomes = biomes;
        surface = new NetherSurface(seed, biomes);
        carvers = new NetherCarvers(seed);
        decoration = new NetherDecoration(world, biomes);
        fossils = new NetherFossils(seed, biomes);
        fortress = (MapGenNetherBridge) TerrainGen.getModdedMapGen(
                xy177.farmersfuturedelight.common.worldgen.FFDNetherStructures.fortress(), InitMapGenEvent.EventType.NETHER_BRIDGE);
        world.setSeaLevel(NetherGeneration.SEA_LEVEL);
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        ChunkPrimer primer = new ChunkPrimer();
        float[] density = biomes.noise().lattice(chunkX, chunkZ);
        float[] column = new float[128];
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                NetherNoise.column(density, localX, localZ, column);
                for (int blockY = 0; blockY < 128; blockY++) {
                    IBlockState state = column[blockY] > 0 ? Blocks.NETHERRACK.getDefaultState()
                            : blockY < 32 ? Blocks.LAVA.getDefaultState() : Blocks.AIR.getDefaultState();
                    if (state.getBlock() != Blocks.AIR) primer.setBlockState(localX, blockY, localZ, state);
                }
            }
        }
        surface.apply(primer, chunkX, chunkZ);
        carvers.apply(primer, chunkX, chunkZ);
        if (structures) fossils.apply(primer, chunkX, chunkZ);
        if (structures) fortress.generate(world, chunkX, chunkZ, primer);
        if (structures) xy177.farmersfuturedelight.common.worldgen.FFDNetherStructures.prepare(world, chunkX, chunkZ);
        Chunk chunk = new Chunk(world, primer, chunkX, chunkZ);
        byte[] legacy = chunk.getBiomeArray();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                legacy[localZ * 16 + localX] = (byte) Biome.getIdForBiome(NetherBiomeSource.legacy(
                        biomes.get(chunkX * 16 + localX, 64, chunkZ * 16 + localZ)));
            }
        }
        FFDVerticalBiomeManager.put(world, chunkX, chunkZ, biomes.sampleChunk(chunkX, chunkZ));
        chunk.generateSkylightMap();
        NetherChunkLighting.initialize(chunk);
        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        Random random = decorationRandom(chunkX, chunkZ);
        boolean previous = BlockFalling.fallInstantly;
        BlockFalling.fallInstantly = true;
        try {
            MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(this, world, random, chunkX, chunkZ, false));
            if (structures) fortress.generateStructure(world, random, new net.minecraft.util.math.ChunkPos(chunkX, chunkZ));
            if (structures) xy177.farmersfuturedelight.common.worldgen.FFDNetherStructures.populate(world, chunkX, chunkZ, random);
            if (structures) xy177.farmersfuturedelight.common.worldgen.MapGenFFDRuinedPortal.generate(world, chunkX, chunkZ);
            decoration.generate(chunkX, chunkZ);
            MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(this, world, random, chunkX, chunkZ, false));
        } finally {
            BlockFalling.fallInstantly = previous;
        }
    }

    private Random decorationRandom(int chunkX, int chunkZ) {
        Random random = new Random(seed);
        long first = random.nextLong() | 1;
        long second = random.nextLong() | 1;
        random.setSeed((chunkX * first + chunkZ * second) ^ seed);
        return random;
    }

    @Override public boolean generateStructures(Chunk chunk, int chunkX, int chunkZ) { return false; }
    @Override public void recreateStructures(Chunk chunk, int chunkX, int chunkZ) {
        if (structures) {
            fortress.generate(world, chunkX, chunkZ, null);
            xy177.farmersfuturedelight.common.worldgen.FFDNetherStructures.prepare(world, chunkX, chunkZ);
        }
    }
    @Override public boolean isInsideStructure(World world, String name, BlockPos pos) {
        if (!structures) return false;
        if (xy177.farmersfuturedelight.common.worldgen.MapGenFFDRuinedPortal.matches(name)) {
            return xy177.farmersfuturedelight.common.worldgen.MapGenFFDRuinedPortal.inside(world, pos);
        }
        if (xy177.farmersfuturedelight.common.worldgen.FFDNetherStructures.isBastion(name)) {
            return xy177.farmersfuturedelight.common.worldgen.FFDNetherStructures.inside(world, pos);
        }
        return "Fortress".equals(name) && fortress.isInsideStructure(pos);
    }
    @Override @Nullable public BlockPos getNearestStructurePos(World world, String name, BlockPos pos, boolean unexplored) {
        if (!structures) return null;
        if (xy177.farmersfuturedelight.common.worldgen.MapGenFFDRuinedPortal.matches(name)) {
            return xy177.farmersfuturedelight.common.worldgen.MapGenFFDRuinedPortal.locate(world, pos, unexplored);
        }
        if (xy177.farmersfuturedelight.common.worldgen.FFDNetherStructures.isBastion(name)) {
            return xy177.farmersfuturedelight.common.worldgen.FFDNetherStructures.locate(world, pos, unexplored);
        }
        return "Fortress".equals(name) ? fortress.getNearestStructurePos(world, pos, unexplored) : null;
    }
    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
        if (type == EnumCreatureType.MONSTER && structures && (fortress.isInsideStructure(pos)
                || fortress.isPositionInStructure(world, pos) && world.getBlockState(pos.down()).getBlock() == Blocks.NETHER_BRICK)) return fortress.getSpawnList();
        return NetherSpawns.get(biomes.get(pos.getX(), pos.getY(), pos.getZ()), type);
    }
}
