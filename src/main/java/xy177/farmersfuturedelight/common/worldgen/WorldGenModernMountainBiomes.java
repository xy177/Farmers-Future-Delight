package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenerator;

import xy177.farmersfuturedelight.common.FFDWorldGenerator;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class WorldGenModernMountainBiomes {
    private static final int WORLDGEN_FLAGS = 2 | 16;
    private static final long RANDOM_SALT = 0x4D4F554E5441494EL;

    private final long worldSeed;
    private final NoiseGeneratorPerlin biomeInfoNoise;
    private final NoiseGeneratorPerlin meadowFlowerNoise;
    private final NoiseGeneratorPerlin temperatureNoise;

    public WorldGenModernMountainBiomes(long worldSeed) {
        this.worldSeed = worldSeed;
        biomeInfoNoise = new NoiseGeneratorPerlin(new Random(2345L), 1);
        meadowFlowerNoise = new NoiseGeneratorPerlin(new Random(2345L ^ 0x5DEECE66DL), 1);
        temperatureNoise = new NoiseGeneratorPerlin(new Random(1234L), 1);
    }

    public void generate(World world, int chunkX, int chunkZ) {
        Random random = chunkRandom(chunkX, chunkZ);
        generateMeadow(world, random, chunkX, chunkZ);
        generateGrove(world, random, chunkX, chunkZ);
        freezeTopLayer(world, chunkX, chunkZ);
    }

    private void generateMeadow(World world, Random random, int chunkX, int chunkZ) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        double noise = biomeInfoNoise.getValue(startX / 200.0D, startZ / 200.0D);

        if (noise > -0.8D && random.nextInt(32) < 7) {
            generatePlantPatch(world, random, startX, startZ, 96, 7, 3,
                    FFDVerticalBiome.MEADOW, Blocks.DOUBLE_PLANT.getStateFromMeta(2));
        }

        int grassGroups = noise < -0.8D ? 5 : 10;
        for (int group = 0; group < grassGroups; group++) {
            generatePlantPatch(world, random, startX, startZ, 16, 7, 3,
                    FFDVerticalBiome.MEADOW, Blocks.TALLGRASS.getStateFromMeta(1));
        }

        int flowerX = startX + random.nextInt(16);
        int flowerZ = startZ + random.nextInt(16);
        BlockPos flowerOrigin = surfacePosition(world, flowerX, flowerZ);
        for (int attempt = 0; attempt < 96; attempt++) {
            BlockPos pos = flowerOrigin.add(triangle(random, 6), triangle(random, 2),
                    triangle(random, 6));
            if (isInChunk(pos, chunkX, chunkZ)) {
                placeMeadowPlant(world, pos);
            }
        }

        if (random.nextInt(100) == 0) {
            int treeX = startX + 4 + random.nextInt(8);
            int treeZ = startZ + 4 + random.nextInt(8);
            BlockPos treePos = surfacePosition(world, treeX, treeZ);
            if (isSurfaceBiome(world, treePos, FFDVerticalBiome.MEADOW)
                    && canGrowTreeOn(world.getBlockState(treePos.down()))) {
                IBlockState snow = removeSnowLayer(world, treePos);
                WorldGenerator tree = random.nextBoolean()
                        ? new WorldGenBigTree(false) : new WorldGenBirchTree(false, true);
                if (tree.generate(world, random, treePos)) {
                    FFDWorldGenerator.addGuaranteedBeeNest(random, world, treePos);
                } else {
                    restoreSnowLayer(world, treePos, snow);
                }
            }
        }
    }

    private void generateGrove(World world, Random random, int chunkX, int chunkZ) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        int treeCount = 10 + (random.nextInt(10) == 0 ? 1 : 0);
        for (int attempt = 0; attempt < treeCount; attempt++) {
            int x = startX + 4 + random.nextInt(8);
            int z = startZ + 4 + random.nextInt(8);
            BlockPos treePos = surfacePosition(world, x, z);
            if (!isSurfaceBiome(world, treePos, FFDVerticalBiome.GROVE)) {
                continue;
            }
            IBlockState surface = world.getBlockState(treePos.down());
            if (!isSnowSurface(surface)) {
                continue;
            }

            IBlockState snow = removeSnowLayer(world, treePos);
            world.setBlockState(treePos.down(), Blocks.DIRT.getDefaultState(), WORLDGEN_FLAGS);
            WorldGenerator tree = random.nextInt(3) == 0
                    ? new WorldGenTaiga2(false) : new WorldGenTaiga1();
            if (!tree.generate(world, random, treePos)) {
                world.setBlockState(treePos.down(), surface, WORLDGEN_FLAGS);
                restoreSnowLayer(world, treePos, snow);
            }
        }

        if (random.nextInt(300) == 0) {
            int x = startX + random.nextInt(16);
            int z = startZ + random.nextInt(16);
            BlockPos pos = surfacePosition(world, x, z);
            FFDVerticalBiome biome = surfaceBiome(world, pos.down());
            if ((biome == FFDVerticalBiome.GROVE || biome == FFDVerticalBiome.SNOWY_SLOPES)
                    && world.getBlockState(pos.down()).getBlock() == Blocks.GRASS) {
                new WorldGenPumpkin().generate(world, random, pos);
            }
        }
    }

    private void freezeTopLayer(World world, int chunkX, int chunkZ) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        for (int offsetX = 0; offsetX < 16; offsetX++) {
            for (int offsetZ = 0; offsetZ < 16; offsetZ++) {
                BlockPos top = surfacePosition(world, startX + offsetX, startZ + offsetZ);
                FFDVerticalBiome biome = surfaceBiome(world, top.down());
                if (!biome.isMountain()) {
                    continue;
                }

                BlockPos surface = top.down();
                IBlockState state = world.getBlockState(surface);
                if (temperatureAt(biome, top) >= 0.15D) {
                    clearSnowLayer(world, top);
                    if (state.getBlock() == Blocks.ICE) {
                        world.setBlockState(surface, Blocks.WATER.getDefaultState(),
                                WORLDGEN_FLAGS);
                    }
                    continue;
                }
                if (state.getBlock() == Blocks.WATER
                        && state.getValue(BlockLiquid.LEVEL) == 0) {
                    world.setBlockState(surface, Blocks.ICE.getDefaultState(), WORLDGEN_FLAGS);
                }
                if (world.isAirBlock(top) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, top)) {
                    world.setBlockState(top, Blocks.SNOW_LAYER.getDefaultState(), WORLDGEN_FLAGS);
                }
            }
        }
    }

    private void generatePlantPatch(World world, Random random, int startX, int startZ,
                                    int attempts, int horizontalOffset, int verticalOffset,
                                    FFDVerticalBiome biome, IBlockState plant) {
        int originX = startX + random.nextInt(16);
        int originZ = startZ + random.nextInt(16);
        BlockPos origin = surfacePosition(world, originX, originZ);
        for (int attempt = 0; attempt < attempts; attempt++) {
            BlockPos pos = origin.add(triangle(random, horizontalOffset),
                    triangle(random, verticalOffset), triangle(random, horizontalOffset));
            if (isInChunk(pos, startX >> 4, startZ >> 4)
                    && isSurfaceBiome(world, pos, biome)) {
                placePlant(world, pos, plant);
            }
        }
    }

    private void placeMeadowPlant(World world, BlockPos pos) {
        if (!isSurfaceBiome(world, pos, FFDVerticalBiome.MEADOW)) {
            return;
        }
        double noise = meadowFlowerNoise.getValue(pos.getX() / 8.0D, pos.getZ() / 8.0D);
        int selection = Math.floorMod((int) Math.floor((noise + 1.0D) * 4.0D), 7);
        IBlockState state;
        switch (selection) {
            case 0:
                state = Blocks.TALLGRASS.getStateFromMeta(1);
                break;
            case 1:
                state = Blocks.RED_FLOWER.getStateFromMeta(2);
                break;
            case 2:
                state = Blocks.RED_FLOWER.getStateFromMeta(0);
                break;
            case 3:
                state = Blocks.RED_FLOWER.getStateFromMeta(3);
                break;
            case 4:
                state = Blocks.YELLOW_FLOWER.getDefaultState();
                break;
            case 5:
                state = Blocks.RED_FLOWER.getStateFromMeta(8);
                break;
            default:
                state = Blocks.TALLGRASS.getStateFromMeta(1);
                break;
        }
        placePlant(world, pos, state);
    }

    private static void placePlant(World world, BlockPos pos, IBlockState state) {
        IBlockState replaced = world.getBlockState(pos);
        if (!world.isAirBlock(pos) && replaced.getBlock() != Blocks.SNOW_LAYER
                || !canSupportPlant(world.getBlockState(pos.down()))) {
            return;
        }
        IBlockState snow = removeSnowLayer(world, pos);
        if (!state.getBlock().canPlaceBlockAt(world, pos)) {
            restoreSnowLayer(world, pos, snow);
            return;
        }
        if (state.getBlock() == Blocks.DOUBLE_PLANT) {
            if (!world.isAirBlock(pos.up())) {
                restoreSnowLayer(world, pos, snow);
                return;
            }
            Blocks.DOUBLE_PLANT.placeAt(world, pos, BlockDoublePlant.EnumPlantType.GRASS,
                    WORLDGEN_FLAGS);
            return;
        }
        if (!world.setBlockState(pos, state, WORLDGEN_FLAGS)) {
            restoreSnowLayer(world, pos, snow);
        }
    }

    private static boolean canSupportPlant(IBlockState state) {
        return state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.DIRT;
    }

    private static boolean canGrowTreeOn(IBlockState state) {
        return canSupportPlant(state);
    }

    private static void clearSnowLayer(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), WORLDGEN_FLAGS);
        }
    }

    private static IBlockState removeSnowLayer(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == Blocks.SNOW_LAYER
                && world.setBlockState(pos, Blocks.AIR.getDefaultState(), WORLDGEN_FLAGS)) {
            return state;
        }
        return null;
    }

    private static void restoreSnowLayer(World world, BlockPos pos, IBlockState state) {
        if (state != null && world.isAirBlock(pos)) {
            world.setBlockState(pos, state, WORLDGEN_FLAGS);
        }
    }

    private static boolean isSnowSurface(IBlockState state) {
        net.minecraft.block.Block powderSnow = FFDItems.effectiveBlock(FFDBlocks.POWDER_SNOW);
        return state.getBlock() == Blocks.SNOW
                || powderSnow != null && state.getBlock() == powderSnow;
    }

    private static boolean isSurfaceBiome(World world, BlockPos airPos, FFDVerticalBiome biome) {
        return surfaceBiome(world, airPos.down()) == biome;
    }

    private static FFDVerticalBiome surfaceBiome(World world, BlockPos pos) {
        FFDVerticalBiome sampled = FFDVerticalBiomeManager.getBiome(world, pos);
        if (!sampled.isMountain()) {
            return sampled;
        }
        Biome legacyBiome = world.getBiome(pos);
        return FFDVerticalBiome.adaptSurface(sampled, legacyBiome, pos);
    }

    private static BlockPos surfacePosition(World world, int x, int z) {
        int y = FFDHeightHooks.getWorldHeight(world, x, z);
        y = Math.max(FFDHeightHooks.minY(world) + 1,
                Math.min(FFDHeightHooks.maxYExclusive(world) - 1, y));
        return new BlockPos(x, y, z);
    }

    private Random chunkRandom(int chunkX, int chunkZ) {
        Random random = new Random(worldSeed);
        long xSeed = random.nextLong() / 2L * 2L + 1L;
        long zSeed = random.nextLong() / 2L * 2L + 1L;
        random.setSeed(((long) chunkX * xSeed + (long) chunkZ * zSeed)
                ^ worldSeed ^ RANDOM_SALT);
        return random;
    }

    private static int triangle(Random random, int radius) {
        return random.nextInt(radius + 1) - random.nextInt(radius + 1);
    }

    private static boolean isInChunk(BlockPos pos, int chunkX, int chunkZ) {
        return (pos.getX() >> 4) == chunkX && (pos.getZ() >> 4) == chunkZ;
    }

    private double temperatureAt(FFDVerticalBiome biome, BlockPos pos) {
        double temperature;
        switch (biome) {
            case MEADOW:
                temperature = 0.5D;
                break;
            case GROVE:
                temperature = -0.2D;
                break;
            case SNOWY_SLOPES:
                temperature = -0.3D;
                break;
            case JAGGED_PEAKS:
            case FROZEN_PEAKS:
                temperature = -0.7D;
                break;
            case STONY_PEAKS:
                temperature = 1.0D;
                break;
            default:
                return 1.0D;
        }
        int snowLevel = FFDModernWorldgenData.SEA_LEVEL + 17;
        if (pos.getY() <= snowLevel) {
            return temperature;
        }
        double noise = temperatureNoise.getValue(pos.getX() / 8.0D, pos.getZ() / 8.0D) * 8.0D;
        return temperature - (noise + pos.getY() - snowLevel) * 0.05D / 40.0D;
    }
}
