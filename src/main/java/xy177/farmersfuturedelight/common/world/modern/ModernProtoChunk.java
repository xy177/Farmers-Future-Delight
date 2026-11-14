package xy177.farmersfuturedelight.common.world.modern;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernAquifer;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernOreVeins;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class ModernProtoChunk {
    public final ModernNoiseChunk noise;
    private final ExtendedBlockStorage[] sections;
    private final int[] surfaceHeights;
    private int[] fluidUpdates;
    private FFDVerticalBiomeData verticalBiomes;
    private boolean committed;

    private ModernProtoChunk(ModernNoiseChunk noise, ExtendedBlockStorage[] sections,
                             int[] surfaceHeights, int[] fluidUpdates) {
        this.noise = noise;
        this.sections = sections;
        this.surfaceHeights = surfaceHeights;
        this.fluidUpdates = fluidUpdates;
    }

    static ModernProtoChunk generate(ModernNoiseContext context, int chunkX, int chunkZ, boolean skylight) {
        ModernNoiseChunk noise = context.sample(chunkX, chunkZ);
        ExtendedBlockStorage[] sections = new ExtendedBlockStorage[FFDModernWorldgenData.HEIGHT >> 4];
        int[] surfaces = new int[256];
        Arrays.fill(surfaces, FFDModernWorldgenData.MIN_Y - 1);
        IntArrayList fluids = new IntArrayList();
        FFDModernAquifer aquifer = context.aquifer(chunkX, chunkZ);
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        WorldGenModernOreVeins.Sampler veins = context.veins.sampler(startX, startZ);
        for (int localX = 0; localX < 16; localX++) {
            if (Thread.currentThread().isInterrupted()) throw new CancellationException("Modern generation interrupted");
            for (int localZ = 0; localZ < 16; localZ++) {
                int blockX = startX + localX;
                int blockZ = startZ + localZ;
                ModernNoiseChunk.Column density = noise.column(localX, localZ);
                for (int blockY = FFDModernWorldgenData.MIN_Y;
                     blockY < FFDModernWorldgenData.MIN_Y + FFDModernWorldgenData.HEIGHT; blockY++) {
                    IBlockState state = aquifer.computeSubstance(blockX, blockY, blockZ, density.sample(blockY));
                    if (state == null) state = veins.stateAt(blockX, blockY, blockZ);
                    if (state == null) state = blockY < 0 ? context.stoneAt(blockX, blockY, blockZ) : Blocks.STONE.getDefaultState();
                    if (state.getMaterial().isSolid()) surfaces[localZ * 16 + localX] = blockY;
                    if (aquifer.shouldScheduleFluidUpdate() && isFluid(state)) {
                        fluids.add(FFDHeightHooks.packLocalBlockChange(localX, blockY, localZ));
                    }
                    if (state.getMaterial() == net.minecraft.block.material.Material.AIR) continue;
                    int sectionIndex = (blockY - FFDModernWorldgenData.MIN_Y) >> 4;
                    ExtendedBlockStorage section = sections[sectionIndex];
                    if (section == null) {
                        section = new ExtendedBlockStorage(FFDModernWorldgenData.MIN_Y + (sectionIndex << 4), skylight);
                        sections[sectionIndex] = section;
                    }
                    section.set(localX, blockY & 15, localZ, state);
                }
            }
        }
        return new ModernProtoChunk(noise, sections, surfaces, fluids.toIntArray());
    }

    public int[] surfaceHeights() {
        return surfaceHeights.clone();
    }

    ExtendedBlockStorage[] sections() {
        if (committed) throw new IllegalStateException("Modern prototype chunk was already committed");
        return sections;
    }

    void completeTerrain(FFDVerticalBiomeData biomes, java.util.List<Integer> updates) {
        if (committed || verticalBiomes != null) throw new IllegalStateException("Modern terrain was already completed");
        verticalBiomes = java.util.Objects.requireNonNull(biomes);
        fluidUpdates = new int[updates.size()];
        for (int index = 0; index < fluidUpdates.length; index++) fluidUpdates[index] = updates.get(index);
    }

    public boolean hasTerrain() {
        return verticalBiomes != null;
    }

    public FFDVerticalBiomeData verticalBiomes() {
        if (verticalBiomes == null) throw new IllegalStateException("Modern terrain is incomplete");
        return verticalBiomes;
    }

    public int[] fluidUpdates() {
        return fluidUpdates.clone();
    }

    public void commitTo(Chunk chunk) {
        if (committed) throw new IllegalStateException("Modern prototype chunk was already committed");
        ExtendedBlockStorage[] target = chunk.getBlockStorageArray();
        if (target.length != sections.length) throw new IllegalStateException("Modern chunk section count mismatch");
        for (int logical = 0; logical < sections.length; logical++) {
            int blockY = FFDModernWorldgenData.MIN_Y + (logical << 4);
            int storage = FFDHeightHooks.storageIndexForChunk(blockY, chunk);
            if (storage < 0 || storage >= target.length) {
                throw new IllegalStateException("Modern chunk storage mapping is invalid at " + blockY);
            }
            target[storage] = sections[logical];
        }
        committed = true;
    }

    private static boolean isFluid(IBlockState state) {
        net.minecraft.block.material.Material material = state.getMaterial();
        return material == net.minecraft.block.material.Material.WATER || material == net.minecraft.block.material.Material.LAVA;
    }
}
