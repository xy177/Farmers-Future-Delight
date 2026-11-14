package xy177.farmersfuturedelight.common.world.biome;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import xy177.farmersfuturedelight.api.biome.VerticalBiomeDefinition;
import xy177.farmersfuturedelight.api.biome.VerticalBiomeGenerationContext;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;
import xy177.farmersfuturedelight.common.worldgen.WorldGenVerticalCaveBiomes;

public final class FFDVerticalBiomeGeneration implements VerticalBiomeGenerationContext {
    private final WorldGenVerticalCaveBiomes generator;
    private final long worldSeed;
    private final int chunkX;
    private final int chunkZ;
    private final FFDVerticalBiomeData biomes;
    private final WorldGenVerticalCaveBiomes.Access access;
    private final VerticalBiomeDefinition biome;
    private final VerticalBiomeDefinition.Stage stage;
    private final Random random;

    private FFDVerticalBiomeGeneration(WorldGenVerticalCaveBiomes generator, long worldSeed,
                                      int chunkX, int chunkZ, FFDVerticalBiomeData biomes,
                                      WorldGenVerticalCaveBiomes.Access access,
                                      VerticalBiomeDefinition biome,
                                      VerticalBiomeDefinition.Stage stage) {
        this.generator = generator;
        this.worldSeed = worldSeed;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.biomes = biomes;
        this.access = access;
        this.biome = biome;
        this.stage = stage;
        long salt = 1125899906842597L;
        String name = biome.getRegistryName().toString();
        for (int index = 0; index < name.length(); index++) {
            salt = salt * 31L + name.charAt(index);
        }
        random = new Random(worldSeed ^ salt ^ chunkX * 341873128712L
                ^ chunkZ * 132897987541L ^ stage.ordinal() * 0x9E3779B97F4A7C15L);
    }

    public static void generate(WorldGenVerticalCaveBiomes generator, long worldSeed,
                                int chunkX, int chunkZ, FFDVerticalBiomeData biomes,
                                WorldGenVerticalCaveBiomes.Access access,
                                VerticalBiomeDefinition.Stage stage) {
        for (FFDVerticalBiome entry : FFDVerticalBiome.featureEntries()) {
            VerticalBiomeDefinition definition = entry.definition();
            Consumer<VerticalBiomeGenerationContext> feature = definition == null ? null
                    : definition.getFeature(stage);
            if (feature != null) {
                feature.accept(new FFDVerticalBiomeGeneration(generator, worldSeed,
                        chunkX, chunkZ, biomes, access, definition, stage));
            }
        }
    }

    public void generateLushVegetation() {
        generator.generateLushVegetation(chunkX, chunkZ, biomes, access);
    }

    @Override
    public long getWorldSeed() { return worldSeed; }

    @Override
    public int getChunkX() { return chunkX; }

    @Override
    public int getChunkZ() { return chunkZ; }

    @Override
    public int getMinY() { return FFDModernWorldgenData.MIN_Y; }

    @Override
    public int getMaxYExclusive() {
        return FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT;
    }

    @Override
    public VerticalBiomeDefinition getBiome() { return biome; }

    @Override
    public VerticalBiomeDefinition.Stage getStage() { return stage; }

    @Override
    public Random getRandom() { return random; }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return validY(y) ? access.getState(x, y, z) : Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean setBlockState(int x, int y, int z, IBlockState state) {
        Objects.requireNonNull(state, "state");
        if (!validY(y) || !isTargetChunk(x, z)) {
            return false;
        }
        access.setState(x, y, z, state);
        Material material = state.getMaterial();
        if (material == Material.WATER || material == Material.LAVA) {
            access.markFluidUpdate(x, y, z);
        }
        return true;
    }

    @Override
    public boolean isTargetChunk(int x, int z) {
        return access.isTargetChunk(x, z);
    }

    @Override
    public VerticalBiomeDefinition getBiome(int x, int y, int z) {
        return validY(y) ? access.getBiome(x, y, z).definition() : null;
    }

    @Override
    public void markFluidUpdate(int x, int y, int z) {
        if (validY(y) && isTargetChunk(x, z)) {
            access.markFluidUpdate(x, y, z);
        }
    }

    @Override
    public int getOceanFloorY(int x, int z) {
        return access.getOceanFloorY(x, z);
    }

    private boolean validY(int y) {
        return y >= getMinY() && y < getMaxYExclusive();
    }
}
