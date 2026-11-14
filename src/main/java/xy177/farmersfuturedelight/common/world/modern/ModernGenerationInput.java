package xy177.farmersfuturedelight.common.world.modern;

import net.minecraft.world.biome.Biome;

public final class ModernGenerationInput {
    public final int chunkX;
    public final int chunkZ;
    private final Biome[] biomes;
    private final int radius;
    private final int width;

    public ModernGenerationInput(int chunkX, int chunkZ, Biome[] biomes) {
        this(chunkX, chunkZ, 0, biomes);
    }

    public ModernGenerationInput(int chunkX, int chunkZ, int radius, Biome[] biomes) {
        if (radius < 0 || radius > 8) throw new IllegalArgumentException("Modern biome radius must be in 0..8");
        this.radius = radius;
        width = (radius * 2 + 1) * 16;
        if (biomes.length != width * width) throw new IllegalArgumentException("Modern biome snapshot size mismatch");
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.biomes = biomes.clone();
    }

    public Biome[] biomes() {
        Biome[] result = new Biome[256];
        for (int localZ = 0; localZ < 16; localZ++) {
            System.arraycopy(biomes, (localZ + radius * 16) * width + radius * 16, result, localZ * 16, 16);
        }
        return result;
    }

    public Biome biomeAt(int blockX, int blockZ) {
        int localX = blockX - ((chunkX - radius) << 4);
        int localZ = blockZ - ((chunkZ - radius) << 4);
        if (localX < 0 || localX >= width || localZ < 0 || localZ >= width) {
            throw new IllegalArgumentException("Biome query outside modern input snapshot: " + blockX + "," + blockZ);
        }
        return biomes[localZ * width + localX];
    }
}
