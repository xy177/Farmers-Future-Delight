package xy177.farmersfuturedelight.common;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import xy177.farmersfuturedelight.common.block.BlockBeehive;
import xy177.farmersfuturedelight.common.block.BlockKelpHead;
import xy177.farmersfuturedelight.common.block.BlockSeaPickle;
import xy177.farmersfuturedelight.common.block.BlockSweetBerryBush;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.tile.TileEntityBeehive;

public class FFDWorldGenerator implements IWorldGenerator {
    private static final int WORLDGEN_FLAGS = 2 | 16;
    private static final NoiseGeneratorPerlin KELP_NOISE =
            new NoiseGeneratorPerlin(new Random(2345L), 1);

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         net.minecraft.world.gen.IChunkGenerator chunkGenerator,
                         net.minecraft.world.chunk.IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) {
            return;
        }
        BlockPos chunkOrigin = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        Biome biome = world.getBiome(chunkOrigin);
        if (FFDItems.isSweetBerryEnabled()
                && BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS)) {
            int rarity = BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY)
                    ? FFDConfig.sweetBerryRareRarity : FFDConfig.sweetBerryCommonRarity;
            if (random.nextInt(rarity) == 0) {
                generateSweetBerryPatch(random, chunkX, chunkZ, world,
                        BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY));
            }
        }

        Biome aquaticBiome = world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
        if (FFDItems.isKelpEnabled()
                && BiomeDictionary.hasType(aquaticBiome, BiomeDictionary.Type.OCEAN)
                && !isFrozenOcean(aquaticBiome)) {
            generateKelp(random, chunkX, chunkZ, world, aquaticBiome);
        }

        SeagrassSettings seagrass = getSeagrassSettings(aquaticBiome);
        if (FFDItems.isSeagrassEnabled() && seagrass != null && !isFrozenOcean(aquaticBiome)) {
            generateSeagrass(random, chunkX, chunkZ, world, seagrass);
        }

        if (FFDItems.isSeaPickleEnabled() && BlockSeaPickle.isSeaPickleBiome(aquaticBiome)
                && random.nextInt(FFDConfig.seaPickleRarity) == 0) {
            generateSeaPickles(random, chunkX, chunkZ, world);
        }

        if (FFDItems.isHoneyEnabled()) {
            generateBeeNests(random, chunkX, chunkZ, world, biome);
        }
    }

    private static void generateSweetBerryPatch(Random random, int chunkX, int chunkZ, World world,
                                                boolean snowy) {
        int baseX = chunkX * 16 + random.nextInt(16);
        int baseZ = chunkZ * 16 + random.nextInt(16);
        Biome baseBiome = world.getBiome(new BlockPos(baseX, 0, baseZ));
        if (!BiomeDictionary.hasType(baseBiome, BiomeDictionary.Type.CONIFEROUS)
                || BiomeDictionary.hasType(baseBiome, BiomeDictionary.Type.SNOWY) != snowy) {
            return;
        }
        int baseY = world.getHeight(baseX, baseZ);
        for (int attempt = 0; attempt < FFDConfig.sweetBerryPatchAttempts; attempt++) {
            BlockPos pos = new BlockPos(
                    baseX + triangleOffset(random, FFDConfig.sweetBerryHorizontalOffset),
                    baseY + triangleOffset(random, FFDConfig.sweetBerryVerticalOffset),
                    baseZ + triangleOffset(random, FFDConfig.sweetBerryHorizontalOffset));
            if (isInChunk(pos, chunkX, chunkZ) && world.isAirBlock(pos)
                    && world.getBlockState(pos.down()).getBlock() == Blocks.GRASS) {
                world.setBlockState(pos, FFDBlocks.SWEET_BERRY_BUSH.getDefaultState()
                        .withProperty(BlockSweetBerryBush.AGE, 3), WORLDGEN_FLAGS);
            }
        }
    }

    private void generateBeeNests(Random random, int chunkX, int chunkZ, World world, Biome biome) {
        float chance = getBeeNestChance(biome);
        if (chance <= 0.0F) {
            return;
        }
        int minX = chunkX * 16;
        int minZ = chunkZ * 16;
        Set<BlockPos> visitedLogs = new HashSet<>();
        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                int topY = world.getHeight(x, z) - 1;
                int bottomY = Math.max(1, topY - 32);
                for (int y = topY; y >= bottomY; y--) {
                    BlockPos trunkTop = new BlockPos(x, y, z);
                    if (!isVerticalLog(world.getBlockState(trunkTop))
                            || isVerticalLog(world.getBlockState(trunkTop.up()))
                            || !hasLeavesNearby(world, trunkTop, chunkX, chunkZ)) {
                        continue;
                    }
                    if (visitedLogs.contains(trunkTop)) {
                        break;
                    }
                    markConnectedTreeLogs(world, trunkTop, chunkX, chunkZ, visitedLogs);
                    if (random.nextFloat() < chance) {
                        placeBeeNest(random, world, trunkTop, chunkX, chunkZ);
                    }
                    break;
                }
            }
        }
    }

    private static void markConnectedTreeLogs(World world, BlockPos start, int chunkX, int chunkZ,
                                              Set<BlockPos> visitedLogs) {
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            if (!isInChunk(current, chunkX, chunkZ) || !isTreeLog(world.getBlockState(current))
                    || !visitedLogs.add(current)) {
                continue;
            }
            for (EnumFacing direction : EnumFacing.values()) {
                pending.addLast(current.offset(direction));
            }
        }
    }

    private static float getBeeNestChance(Biome biome) {
        ResourceLocation id = biome.getRegistryName();
        String path = id == null ? "" : id.getResourcePath();
        if ("mutated_forest".equals(path)) {
            return FFDConfig.beeNestFlowerForestChance;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.PLAINS)
                || "mutated_plains".equals(path)) {
            return FFDConfig.beeNestPlainsChance;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST)) {
            return FFDConfig.beeNestForestChance;
        }
        return 0.0F;
    }

    private static boolean isVerticalLog(IBlockState state) {
        if (!isTreeLog(state)) {
            return false;
        }
        return state.getValue(BlockLog.LOG_AXIS) == BlockLog.EnumAxis.Y;
    }

    private static boolean isTreeLog(IBlockState state) {
        return state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2;
    }

    private static boolean hasLeavesNearby(World world, BlockPos trunkTop, int chunkX, int chunkZ) {
        for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(trunkTop.add(-2, 0, -2),
                trunkTop.add(2, 4, 2))) {
            if (isInChunk(pos, chunkX, chunkZ)
                    && world.getBlockState(pos).getBlock() instanceof BlockLeaves) {
                return true;
            }
        }
        return false;
    }

    private static boolean placeBeeNest(Random random, World world, BlockPos trunkTop,
                                        int chunkX, int chunkZ) {
        EnumFacing[] directions = {EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
        for (int depth = 0; depth < 4; depth++) {
            BlockPos trunk = trunkTop.down(depth);
            if (!isVerticalLog(world.getBlockState(trunk))) {
                continue;
            }
            int start = random.nextInt(directions.length);
            for (int index = 0; index < directions.length; index++) {
                EnumFacing placementSide = directions[(start + index) % directions.length];
                BlockPos nestPos = trunk.offset(placementSide);
                BlockPos entrancePos = nestPos.offset(EnumFacing.SOUTH);
                if (!isInChunk(nestPos, chunkX, chunkZ)
                        || !isInChunk(entrancePos, chunkX, chunkZ)
                        || !world.isAirBlock(nestPos) || !world.isAirBlock(entrancePos)) {
                    continue;
                }
                world.setBlockState(nestPos, FFDBlocks.BEE_NEST.getDefaultState()
                        .withProperty(BlockBeehive.FACING, EnumFacing.SOUTH), WORLDGEN_FLAGS);
                TileEntity tile = world.getTileEntity(nestPos);
                if (tile instanceof TileEntityBeehive) {
                    int count = FFDConfig.beeNestMinBees + random.nextInt(
                            FFDConfig.beeNestMaxBees - FFDConfig.beeNestMinBees + 1);
                    ((TileEntityBeehive) tile).addNewBees(count);
                }
                return true;
            }
        }
        return false;
    }

    private void generateKelp(Random random, int chunkX, int chunkZ, World world, Biome biome) {
        int ratio = isWarmOcean(biome) ? FFDConfig.kelpWarmNoiseRatio : FFDConfig.kelpColdNoiseRatio;
        double noise = KELP_NOISE.getValue(chunkX * 16.0D / FFDConfig.kelpNoiseScale,
                chunkZ * 16.0D / FFDConfig.kelpNoiseScale);
        int attempts = (int) Math.ceil(noise * ratio);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = chunkX * 16 + 8 + random.nextInt(16);
            int z = chunkZ * 16 + 8 + random.nextInt(16);
            Biome candidateBiome = world.getBiome(new BlockPos(x, 0, z));
            if (!BiomeDictionary.hasType(candidateBiome, BiomeDictionary.Type.OCEAN)
                    || isFrozenOcean(candidateBiome)
                    || isWarmOcean(candidateBiome) != isWarmOcean(biome)) {
                continue;
            }
            BlockPos pos = findWaterPlantBase(world, x, z);
            if (pos == null) {
                continue;
            }
            if (pos.getY() <= 0 || !isSourceWater(world, pos) || !isSourceWater(world, pos.up())
                    || !FFDBlocks.KELP.canPlaceBlockAt(world, pos)) {
                continue;
            }

            int requestedBodyHeight = random.nextInt(FFDConfig.kelpWorldgenMaxBodyHeight + 1);
            int waterCells = 0;
            BlockPos cursor = pos;
            while (waterCells < requestedBodyHeight + 1
                    && isSourceWater(world, cursor) && isSourceWater(world, cursor.up())) {
                waterCells++;
                cursor = cursor.up();
            }
            int bodyHeight = Math.min(requestedBodyHeight, waterCells - 1);
            if (bodyHeight < 0) {
                continue;
            }
            for (int i = 0; i < bodyHeight; i++) {
                world.setBlockState(pos.up(i), FFDBlocks.KELP_PLANT.getDefaultState(), WORLDGEN_FLAGS);
            }
            world.setBlockState(pos.up(bodyHeight),
                    BlockKelpHead.stateForAgeValue(20 + random.nextInt(4)), WORLDGEN_FLAGS);
        }
    }

    private static boolean isSourceWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == Blocks.WATER && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    private static BlockPos findWaterPlantBase(World world, int x, int z) {
        BlockPos cursor = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        while (cursor.getY() > 0 && !isSourceWater(world, cursor)) {
            if (world.getBlockState(cursor).getMaterial() != Material.WATER) {
                return null;
            }
            cursor = cursor.down();
        }
        if (!isSourceWater(world, cursor)) {
            return null;
        }
        while (cursor.getY() > 0 && isSourceWater(world, cursor.down())) {
            cursor = cursor.down();
        }
        return cursor;
    }

    private static boolean isWarmOcean(Biome biome) {
        return biome.getDefaultTemperature() > 0.5F;
    }

    private void generateSeagrass(Random random, int chunkX, int chunkZ, World world,
                                  SeagrassSettings settings) {
        int baseX = chunkX * 16 + 8 + random.nextInt(16);
        int baseZ = chunkZ * 16 + 8 + random.nextInt(16);
        for (int attempt = 0; attempt < settings.attempts; attempt++) {
            int x = baseX + triangleOffset(random, 7);
            int z = baseZ + triangleOffset(random, 7);
            if (!isInPopulationArea(x, z, chunkX, chunkZ)) {
                continue;
            }
            Biome candidateBiome = world.getBiome(new BlockPos(x, 0, z));
            SeagrassSettings candidateSettings = getSeagrassSettings(candidateBiome);
            if (candidateSettings == null || isFrozenOcean(candidateBiome)
                    || candidateSettings.type != settings.type) {
                continue;
            }
            BlockPos pos = findWaterPlantBase(world, x, z);
            if (pos == null) {
                continue;
            }
            if (!isSourceWater(world, pos) || !FFDBlocks.SEAGRASS.canPlaceBlockAt(world, pos)) {
                continue;
            }
            if (random.nextInt(100) < settings.tallPercent) {
                if (FFDBlocks.TALL_SEAGRASS.canPlaceBlockAt(world, pos)) {
                    FFDBlocks.TALL_SEAGRASS.placeAt(world, pos, WORLDGEN_FLAGS);
                }
            } else {
                world.setBlockState(pos, FFDBlocks.SEAGRASS.getDefaultState(), WORLDGEN_FLAGS);
            }
        }
    }

    private static SeagrassSettings getSeagrassSettings(Biome biome) {
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER)) {
            return new SeagrassSettings(SeagrassType.RIVER, FFDConfig.seagrassRiverAttempts,
                    FFDConfig.seagrassRiverTallPercent);
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) {
            return new SeagrassSettings(SeagrassType.SWAMP, FFDConfig.seagrassSwampAttempts,
                    FFDConfig.seagrassSwampTallPercent);
        }
        if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)) {
            return null;
        }

        boolean deep = biome.getRegistryName() != null
                && biome.getRegistryName().getResourcePath().contains("deep");
        boolean cold = biome.getDefaultTemperature() < 0.5F
                || biome.getRegistryName() != null
                && biome.getRegistryName().getResourcePath().contains("cold");
        boolean warm = isWarmOcean(biome);
        if (deep && warm) {
            return new SeagrassSettings(SeagrassType.DEEP_WARM, FFDConfig.seagrassDeepWarmAttempts,
                    FFDConfig.seagrassDeepWarmTallPercent);
        }
        if (deep && cold) {
            return new SeagrassSettings(SeagrassType.DEEP_COLD, FFDConfig.seagrassDeepColdAttempts,
                    FFDConfig.seagrassDeepColdTallPercent);
        }
        if (deep) {
            return new SeagrassSettings(SeagrassType.DEEP, FFDConfig.seagrassDeepAttempts,
                    FFDConfig.seagrassDeepTallPercent);
        }
        if (warm) {
            return new SeagrassSettings(SeagrassType.WARM, FFDConfig.seagrassWarmAttempts,
                    FFDConfig.seagrassWarmTallPercent);
        }
        if (cold) {
            return new SeagrassSettings(SeagrassType.COLD, FFDConfig.seagrassColdAttempts,
                    FFDConfig.seagrassColdTallPercent);
        }
        return new SeagrassSettings(SeagrassType.NORMAL, FFDConfig.seagrassNormalAttempts,
                FFDConfig.seagrassNormalTallPercent);
    }

    private void generateSeaPickles(Random random, int chunkX, int chunkZ, World world) {
        int baseX = chunkX * 16 + 8 + random.nextInt(16);
        int baseZ = chunkZ * 16 + 8 + random.nextInt(16);
        for (int attempt = 0; attempt < FFDConfig.seaPickleWorldgenAttempts; attempt++) {
            int x = baseX + triangleOffset(random, FFDConfig.seaPickleWorldgenOffsetRadius);
            int z = baseZ + triangleOffset(random, FFDConfig.seaPickleWorldgenOffsetRadius);
            if (!isInPopulationArea(x, z, chunkX, chunkZ)) {
                continue;
            }
            BlockPos pos = findWaterPlantBase(world, x, z);
            if (pos == null) {
                continue;
            }
            if (!isSourceWater(world, pos) || !BlockSeaPickle.isValidSeaPickleBed(world, pos)
                    || !FFDBlocks.SEA_PICKLE.canPlaceBlockAt(world, pos)) {
                continue;
            }
            world.setBlockState(pos, FFDBlocks.SEA_PICKLE.getDefaultState()
                    .withProperty(BlockSeaPickle.PICKLES, random.nextInt(4) + 1)
                    .withProperty(BlockSeaPickle.WATERLOGGED, true), WORLDGEN_FLAGS);
        }
    }

    private static boolean isInChunk(BlockPos pos, int chunkX, int chunkZ) {
        return isInChunk(pos.getX(), pos.getZ(), chunkX, chunkZ);
    }

    private static boolean isInChunk(int x, int z, int chunkX, int chunkZ) {
        return x >> 4 == chunkX && z >> 4 == chunkZ;
    }

    private static boolean isInPopulationArea(int x, int z, int chunkX, int chunkZ) {
        int minX = chunkX * 16 + 8;
        int minZ = chunkZ * 16 + 8;
        return x >= minX && x < minX + 16 && z >= minZ && z < minZ + 16;
    }

    private static int triangleOffset(Random random, int radius) {
        return radius == 0 ? 0 : random.nextInt(radius + 1) - random.nextInt(radius + 1);
    }

    private static boolean isFrozenOcean(Biome biome) {
        if (biome.getRegistryName() == null) {
            return false;
        }
        return biome.getRegistryName().getResourcePath().contains("frozen");
    }

    private static final class SeagrassSettings {
        private final SeagrassType type;
        private final int attempts;
        private final int tallPercent;

        private SeagrassSettings(SeagrassType type, int attempts, int tallPercent) {
            this.type = type;
            this.attempts = attempts;
            this.tallPercent = tallPercent;
        }
    }

    private enum SeagrassType {
        WARM,
        NORMAL,
        COLD,
        RIVER,
        SWAMP,
        DEEP_WARM,
        DEEP,
        DEEP_COLD
    }
}
