package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.noise.FFDFlowerNoise;
import xy177.farmersfuturedelight.common.world.noise.FFDSimplexNoise;

public final class WorldGenModernFlowers {
    private static final FFDFlowerNoise FLOWER_NOISE = new FFDFlowerNoise(2345L);
    private static final FFDSimplexNoise BIOME_NOISE = new FFDSimplexNoise(new Random(2345L));

    private WorldGenModernFlowers() {
    }

    public static void generate(Random random, int chunkX, int chunkZ, World world, Biome biome) {
        Block cornflower = FFDItems.effectiveBlock(FFDBlocks.CORNFLOWER);
        Block lily = FFDItems.effectiveBlock(FFDBlocks.LILY_OF_THE_VALLEY);
        if (isFlowerForest(biome)) {
            generateFlowerForest(random, chunkX, chunkZ, world, cornflower, lily);
            return;
        }
        if (cornflower != null) {
            generateCornflowers(random, chunkX, chunkZ, world, biome, cornflower);
        }
        if (lily != null) {
            generateLilies(random, chunkX, chunkZ, world, biome, lily);
        }
    }

    private static void generateCornflowers(Random random, int chunkX, int chunkZ, World world,
                                           Biome biome, Block flower) {
        String biomePath = biome.getRegistryName() == null ? "" : biome.getRegistryName().getResourcePath();
        if (isPlains(biome, biomePath)) {
            int noiseCount = (float) BIOME_NOISE.sample(chunkX * 16.0D / 200.0D,
                    chunkZ * 16.0D / 200.0D) < -0.8D ? 15 : 4;
            for (int count = 0; count < noiseCount; count++) {
                if (random.nextFloat() < 1.0F / 32.0F) {
                    BlockPos origin = chunkOrigin(random, chunkX, chunkZ, world);
                    generatePatch(random, origin, chunkX, chunkZ, world, flower, 64, 6, 2, Selection.PLAINS);
                }
            }
            return;
        }
    }

    private static void generateFlowerForest(Random random, int chunkX, int chunkZ, World world,
                                            Block cornflower, Block lily) {
        if (cornflower == null && lily == null) {
            return;
        }
        for (int count = 0; count < 3; count++) {
            if (random.nextFloat() < 0.5F) {
                BlockPos origin = chunkOrigin(random, chunkX, chunkZ, world);
                generateFlowerForestPatch(random, origin, chunkX, chunkZ, world, cornflower, lily);
            }
        }
        if (lily != null && random.nextFloat() < 1.0F / 7.0F) {
            BlockPos origin = chunkOrigin(random, chunkX, chunkZ, world);
            int placements = Math.max(0, Math.min(3, random.nextInt(5) - 1));
            generateForestLilySelectors(random, origin, chunkX, chunkZ, world, placements, lily);
        }
    }

    private static void generateLilies(Random random, int chunkX, int chunkZ, World world,
                                       Biome biome, Block flower) {
        String biomePath = biome.getRegistryName() == null ? "" : biome.getRegistryName().getResourcePath();
        if (!isModernForest(biome, biomePath)) {
            return;
        }
        if (random.nextFloat() < 1.0F / 7.0F) {
            BlockPos origin = chunkOrigin(random, chunkX, chunkZ, world);
            int selectorCount = Math.max(0, Math.min(1, random.nextInt(5) - 3));
            generateForestLilySelectors(random, origin, chunkX, chunkZ, world, selectorCount, flower);
        }
    }

    private static void generateForestLilySelectors(Random random, BlockPos origin, int chunkX, int chunkZ, World world,
                                                    int placements, Block flower) {
        for (int placement = 0; placement < placements; placement++) {
            if (random.nextInt(4) == 3) {
                generatePatch(random, origin, chunkX, chunkZ, world, flower, 96, 7, 3, Selection.ALWAYS);
            } else {
                for (int attempt = 0; attempt < 96; attempt++) {
                    triangleOffset(random, 7);
                    triangleOffset(random, 3);
                    triangleOffset(random, 7);
                }
            }
        }
    }

    private static BlockPos chunkOrigin(Random random, int chunkX, int chunkZ, World world) {
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        return new BlockPos(x, world.getHeight(x, z), z);
    }

    private static void generatePatch(Random random, BlockPos origin, int chunkX, int chunkZ, World world,
                                      Block flower, int attempts, int horizontalRadius, int verticalRadius,
                                      Selection selection) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = origin.getX() + triangleOffset(random, horizontalRadius);
            int y = origin.getY() + triangleOffset(random, verticalRadius);
            int z = origin.getZ() + triangleOffset(random, horizontalRadius);
            if (x >> 4 != chunkX || z >> 4 != chunkZ) {
                continue;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (!world.isAirBlock(pos)) {
                continue;
            }
            Biome candidateBiome = world.getBiome(pos);
            String candidatePath = candidateBiome.getRegistryName() == null
                    ? "" : candidateBiome.getRegistryName().getResourcePath();
            if ((selection == Selection.PLAINS && !isPlains(candidateBiome, candidatePath))
                    || (selection == Selection.ALWAYS && !isModernForest(candidateBiome, candidatePath)
                    && !isFlowerForest(candidateBiome))
                    || !isSelectedFlower(random, pos, selection) || !flower.canPlaceBlockAt(world, pos)) {
                continue;
            }
            world.setBlockState(pos, flower.getDefaultState(), 2 | 16);
        }
    }

    private static boolean isSelectedFlower(Random random, BlockPos pos, Selection selection) {
        if (selection == Selection.ALWAYS) {
            return true;
        }
        float noise = FLOWER_NOISE.sample(pos.getX() * (double) 0.005F,
                pos.getY() * (double) 0.005F, pos.getZ() * (double) 0.005F);
        if (noise < -0.8F) {
            random.nextInt(4);
            return false;
        }
        return random.nextFloat() < 1.0F / 3.0F && random.nextInt(4) == 3;
    }

    private static void generateFlowerForestPatch(Random random, BlockPos origin, int chunkX, int chunkZ,
                                                 World world, Block cornflower, Block lily) {
        for (int attempt = 0; attempt < 96; attempt++) {
            int x = origin.getX() + triangleOffset(random, 6);
            int y = origin.getY() + triangleOffset(random, 2);
            int z = origin.getZ() + triangleOffset(random, 6);
            if (x >> 4 != chunkX || z >> 4 != chunkZ) {
                continue;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (!world.isAirBlock(pos) || !isFlowerForest(world.getBiome(pos))) {
                continue;
            }
            float noise = FLOWER_NOISE.sample(x * (double) 0.020833334F,
                    y * (double) 0.020833334F, z * (double) 0.020833334F);
            int selected = (int) (Math.max(0.0F, Math.min(0.9999F, (1.0F + noise) / 2.0F)) * 11.0F);
            Block flower = selected == 9 ? cornflower : selected == 10 ? lily : null;
            if (flower == null) {
                continue;
            }
            if (flower.canPlaceBlockAt(world, pos)) {
                world.setBlockState(pos, flower.getDefaultState(), 2 | 16);
            }
        }
    }

    private static int triangleOffset(Random random, int radius) {
        return random.nextInt(radius + 1) - random.nextInt(radius + 1);
    }

    private static boolean isPlains(Biome biome, String path) {
        return biome.getRegistryName() != null && "minecraft".equals(biome.getRegistryName().getResourceDomain())
                && ("plains".equals(path) || "mutated_plains".equals(path));
    }

    private static boolean isModernForest(Biome biome, String path) {
        return biome.getRegistryName() != null && "minecraft".equals(biome.getRegistryName().getResourceDomain())
                && ("forest".equals(path) || "forest_hills".equals(path)
                || "birch_forest".equals(path) || "birch_forest_hills".equals(path)
                || "roofed_forest".equals(path) || "mutated_birch_forest".equals(path)
                || "mutated_birch_forest_hills".equals(path) || "mutated_roofed_forest".equals(path));
    }

    private static boolean isFlowerForest(Biome biome) {
        return biome.getRegistryName() != null && "minecraft".equals(biome.getRegistryName().getResourceDomain())
                && "mutated_forest".equals(biome.getRegistryName().getResourcePath());
    }

    private enum Selection {
        ALWAYS,
        PLAINS
    }
}
