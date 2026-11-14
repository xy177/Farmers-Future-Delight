package xy177.farmersfuturedelight.common.world.modern;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.world.noise.FFDXoroshiroRandom;
import xy177.farmersfuturedelight.common.world.terrain.FFDDensityFunction;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;
import xy177.farmersfuturedelight.common.worldgen.FFDModernStoneProvider;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernOreVeins;

public final class ModernNoiseContext {
    final FFDModernWorldgenData data;
    final WorldGenModernOreVeins veins;
    private final long seed;
    private final FFDModernStoneProvider stones;
    final ModernGenerationCache cache;
    private ModernTerrainPipeline pipeline;
    private final FFDDensityFunction[] fields;
    private final FFDXoroshiroRandom.PositionalFactory deepslateRandom;
    private final IBlockState deepslate;
    private final int transitionMinimum;
    private final int transitionMaximum;
    private final Long2ObjectLinkedOpenHashMap<double[]> nodes = new Long2ObjectLinkedOpenHashMap<>(32768);

    public ModernNoiseContext(long seed, FFDModernStoneProvider stones) {
        this(seed, stones, new ModernGenerationCache());
    }

    ModernNoiseContext(long seed, FFDModernStoneProvider stones, ModernGenerationCache cache) {
        this.seed = seed;
        this.stones = stones;
        this.cache = cache;
        data = new FFDModernWorldgenData(seed, 32768);
        veins = new WorldGenModernOreVeins(data, stones);
        FFDModernWorldgenData.FinalDensityComponents components = data.finalDensityComponents();
        fields = new FFDDensityFunction[] {components.main(), components.noodleToggle(),
                components.noodleThickness(), components.noodleRidgeA(), components.noodleRidgeB()};
        deepslateRandom = data.positionalFactory("minecraft:deepslate");
        deepslate = stones.deepslate();
        transitionMinimum = FFDConfig.deepslateTransitionMinY;
        transitionMaximum = FFDConfig.deepslateTransitionMaxY;
    }

    double[] node(int blockX, int blockY, int blockZ) {
        long gridX = Math.floorDiv(blockX, FFDModernWorldgenData.CELL_WIDTH) & 0x1FFFFFFL;
        long gridZ = Math.floorDiv(blockZ, FFDModernWorldgenData.CELL_WIDTH) & 0x1FFFFFFL;
        long gridY = Math.floorDiv(blockY - FFDModernWorldgenData.MIN_Y, FFDModernWorldgenData.CELL_HEIGHT) & 0x3FL;
        long key = gridX << 31 | gridZ << 6 | gridY;
        double[] result = nodes.get(key);
        if (result != null) return result;
        result = cache.node(key);
        if (result == null) {
            result = new double[fields.length];
            for (int field = 0; field < fields.length; field++) {
                result[field] = fields[field].sample(blockX, blockY, blockZ);
            }
            cache.rememberNode(key, result);
        }
        nodes.put(key, result);
        if (nodes.size() > 32768) nodes.removeFirst();
        return result;
    }

    public ModernNoiseChunk sample(int chunkX, int chunkZ) {
        return cache.noise(this, chunkX, chunkZ);
    }

    void finish(ModernProtoChunk prepared, ModernGenerationInput input, boolean skylight) {
        if (pipeline == null) pipeline = new ModernTerrainPipeline(seed, this, stones);
        pipeline.finish(prepared, input, skylight);
    }

    int preliminaryHeight(int blockX, int blockZ) {
        return cache.preliminaryHeight(this, blockX, blockZ);
    }

    xy177.farmersfuturedelight.common.world.terrain.FFDModernAquifer aquifer(int chunkX, int chunkZ) {
        return new xy177.farmersfuturedelight.common.world.terrain.FFDModernAquifer(
                data, chunkX, chunkZ, this::preliminaryHeight);
    }

    IBlockState stoneAt(int blockX, int blockY, int blockZ) {
        if (deepslate == null) return Blocks.STONE.getDefaultState();
        if (blockY <= transitionMinimum) return deepslate;
        if (blockY >= transitionMaximum || transitionMaximum <= transitionMinimum) return Blocks.STONE.getDefaultState();
        double probability = (transitionMaximum - blockY) / (double) (transitionMaximum - transitionMinimum);
        return deepslateRandom.at(blockX, blockY, blockZ).nextFloat() < probability
                ? deepslate : Blocks.STONE.getDefaultState();
    }
}
