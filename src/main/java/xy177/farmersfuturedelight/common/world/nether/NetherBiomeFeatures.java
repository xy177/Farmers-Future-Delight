package xy177.farmersfuturedelight.common.world.nether;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.api.biome.VerticalBiomeDefinition;
import xy177.farmersfuturedelight.api.biome.VerticalBiomeGenerationContext;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;

final class NetherBiomeFeatures implements VerticalBiomeGenerationContext {
    private final World world;
    private final NetherBiomeSource source;
    private final int chunkX;
    private final int chunkZ;
    private final VerticalBiomeDefinition biome;
    private final VerticalBiomeDefinition.Stage stage;
    private final Random random;

    private NetherBiomeFeatures(World world, NetherBiomeSource source, int chunkX, int chunkZ,
                                VerticalBiomeDefinition biome, VerticalBiomeDefinition.Stage stage) {
        this.world = world;
        this.source = source;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.biome = biome;
        this.stage = stage;
        long salt = 1125899906842597L;
        String name = biome.getRegistryName().toString();
        for (int index = 0; index < name.length(); index++) salt = salt * 31L + name.charAt(index);
        random = new Random(world.getSeed() ^ salt ^ chunkX * 341873128712L
                ^ chunkZ * 132897987541L ^ stage.ordinal() * 0x9E3779B97F4A7C15L);
    }

    static void generate(World world, NetherBiomeSource source, int chunkX, int chunkZ,
                         VerticalBiomeDefinition.Stage stage) {
        for (FFDVerticalBiome entry : FFDVerticalBiome.featureEntries()) {
            VerticalBiomeDefinition definition = entry.definition();
            if (definition == null || definition.getCategory() != VerticalBiomeDefinition.Category.NETHER) continue;
            Consumer<VerticalBiomeGenerationContext> feature = definition.getFeature(stage);
            if (feature != null) feature.accept(new NetherBiomeFeatures(world, source, chunkX, chunkZ, definition, stage));
        }
    }

    @Override public long getWorldSeed() { return world.getSeed(); }
    @Override public int getChunkX() { return chunkX; }
    @Override public int getChunkZ() { return chunkZ; }
    @Override public int getMinY() { return 0; }
    @Override public int getMaxYExclusive() { return NetherGeneration.BUILD_HEIGHT; }
    @Override public VerticalBiomeDefinition getBiome() { return biome; }
    @Override public VerticalBiomeDefinition.Stage getStage() { return stage; }
    @Override public Random getRandom() { return random; }
    @Override public boolean isTargetChunk(int x, int z) { return x >> 4 == chunkX && z >> 4 == chunkZ; }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return validY(y) && world.isBlockLoaded(pos, false) ? world.getBlockState(pos) : Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean setBlockState(int x, int y, int z, IBlockState state) {
        Objects.requireNonNull(state, "state");
        BlockPos pos = new BlockPos(x, y, z);
        if (!validY(y) || !isTargetChunk(x, z) || !world.isBlockLoaded(pos, false)) return false;
        boolean changed = world.setBlockState(pos, state, 2 | 16);
        if (changed && (state.getMaterial() == Material.LAVA || state.getMaterial() == Material.WATER)) markFluidUpdate(x, y, z);
        return changed;
    }

    @Override
    public VerticalBiomeDefinition getBiome(int x, int y, int z) {
        return validY(y) ? source.get(x, y, z).definition() : null;
    }

    @Override
    public void markFluidUpdate(int x, int y, int z) {
        if (validY(y) && isTargetChunk(x, z)) {
            IBlockState state = getBlockState(x, y, z);
            world.scheduleUpdate(new BlockPos(x, y, z), state.getBlock(), state.getBlock().tickRate(world));
        }
    }

    @Override
    public int getOceanFloorY(int x, int z) {
        for (int y = getMaxYExclusive() - 1; y >= 0; y--) {
            IBlockState state = getBlockState(x, y, z);
            if (state.getMaterial().blocksMovement()) return y + 1;
        }
        return 0;
    }

    private boolean validY(int y) { return y >= 0 && y < getMaxYExclusive(); }
}
