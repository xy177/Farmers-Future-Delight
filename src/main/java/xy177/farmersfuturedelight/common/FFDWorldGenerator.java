package xy177.farmersfuturedelight.common;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.properties.IProperty;
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
import xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid;
import xy177.farmersfuturedelight.common.biome.BiomeModernOcean;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.tile.TileEntityBeehive;
import xy177.farmersfuturedelight.common.worldgen.WorldGenCoralReefs;
import xy177.farmersfuturedelight.common.worldgen.WorldGenModernIcebergs;
import xy177.farmersfuturedelight.common.worldgen.FFDOceanStructures;
import xy177.farmersfuturedelight.common.worldgen.BeeNestLeaves;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

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
        try (FFDHeightHooks.WorldgenHeightScope ignored = FFDHeightHooks.enterExtendedWorldgenHeight();
             WaterloggedPlantFluid.WorldgenScope ignoredFluid = WaterloggedPlantFluid.enterWorldgen()) {
            BlockPos chunkOrigin = new BlockPos(chunkX * 16, 0, chunkZ * 16);
            Biome biome = world.getBiome(chunkOrigin);
            FFDOceanStructures.generate(world, chunkX, chunkZ);
            if (!FFDHeightHooks.isExtended(world)) {
                new WorldGenModernIcebergs(world.getSeed()).generate(world, chunkX, chunkZ);
            }
            if (FFDItems.isBlockRegistered(FFDBlocks.SWEET_BERRY_BUSH)
                    && BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS)) {
                int rarity = BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY)
                        ? FFDConfig.sweetBerryRareRarity : FFDConfig.sweetBerryCommonRarity;
                if (random.nextInt(rarity) == 0) {
                    generateSweetBerryPatch(random, chunkX, chunkZ, world,
                            BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY));
                }
            }

            Biome aquaticBiome = world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
            WorldGenCoralReefs.generate(random, chunkX, chunkZ, world);
            if (FFDItems.isBlockRegistered(FFDBlocks.KELP)
                    && BiomeDictionary.hasType(aquaticBiome, BiomeDictionary.Type.OCEAN)
                    && !isFrozenOcean(aquaticBiome) && !isWarmOceanOnly(aquaticBiome)) {
                generateKelp(random, chunkX, chunkZ, world, aquaticBiome);
            }

            SeagrassSettings seagrass = getSeagrassSettings(aquaticBiome);
            if (FFDItems.isBlockRegistered(FFDBlocks.SEAGRASS)
                    && seagrass != null && !isFrozenOcean(aquaticBiome)) {
                generateSeagrass(random, chunkX, chunkZ, world, seagrass);
            }

            if (FFDItems.isBlockRegistered(FFDBlocks.SEA_PICKLE)
                    && WorldGenCoralReefs.isWarmOceanClimate(world,
                    chunkX * 16 + 8, chunkZ * 16 + 8)
                    && random.nextInt(FFDConfig.seaPickleRarity) == 0) {
                generateSeaPickles(random, chunkX, chunkZ, world);
            }

            if (FFDItems.isBlockRegistered(FFDBlocks.BEE_NEST)) {
                generateBeeNests(random, chunkX, chunkZ, world, biome);
            }
            if (seagrass != null) {
                for (int x = chunkX - 1; x <= chunkX + 1; x++) {
                    for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                        net.minecraft.world.chunk.Chunk chunk = chunkProvider.getLoadedChunk(x, z);
                        if (chunk != null) {
                            chunk.setLightPopulated(false);
                        }
                    }
                }
            }
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
                int bottomY = Math.max(FFDHeightHooks.minY(world) + 1, topY - 32);
                for (int y = topY; y >= bottomY; y--) {
                    BlockPos trunkTop = new BlockPos(x, y, z);
                    if (!isVerticalLog(world, trunkTop)
                            || isVerticalLog(world, trunkTop.up())
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
            if (!isInChunk(current, chunkX, chunkZ) || !isTreeLog(world, current)
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

    private static boolean isVerticalLog(World world, BlockPos pos) {
        if (!isTreeLog(world, pos)) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        IProperty<?> axis = state.getBlock().getBlockState().getProperty("axis");
        return axis == null || "y".equalsIgnoreCase(state.getProperties().get(axis).toString());
    }

    private static boolean isTreeLog(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().isWood(world, pos);
    }

    private static boolean hasLeavesNearby(World world, BlockPos trunkTop, int chunkX, int chunkZ) {
        IBlockState trunk = world.getBlockState(trunkTop);
        for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(trunkTop.add(-2, 0, -2),
                trunkTop.add(2, 4, 2))) {
            if (!isInChunk(pos, chunkX, chunkZ)) {
                continue;
            }
            IBlockState leaves = world.getBlockState(pos);
            if (leaves.getBlock().isLeaves(leaves, world, pos)
                    && BeeNestLeaves.matches(leaves) && sameVanillaTree(trunk, leaves)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameVanillaTree(IBlockState trunk, IBlockState leaves) {
        BlockPlanks.EnumType logType = trunk.getBlock() == Blocks.LOG
                ? trunk.getValue(BlockOldLog.VARIANT) : trunk.getBlock() == Blocks.LOG2
                ? trunk.getValue(BlockNewLog.VARIANT) : null;
        BlockPlanks.EnumType leafType = leaves.getBlock() == Blocks.LEAVES
                ? leaves.getValue(BlockOldLeaf.VARIANT) : leaves.getBlock() == Blocks.LEAVES2
                ? leaves.getValue(BlockNewLeaf.VARIANT) : null;
        return logType == null || leafType == null || logType == leafType;
    }

    private static boolean placeBeeNest(Random random, World world, BlockPos trunkTop,
                                        int chunkX, int chunkZ) {
        EnumFacing[] directions = {EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
        for (int depth = 0; depth < 4; depth++) {
            BlockPos trunk = trunkTop.down(depth);
            if (!isVerticalLog(world, trunk)) {
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

    public static boolean addGuaranteedBeeNest(Random random, World world, BlockPos trunkBase) {
        if (!FFDItems.isBlockRegistered(FFDBlocks.BEE_NEST)
                || !isVerticalLog(world, trunkBase)) {
            return false;
        }
        BlockPos trunkTop = trunkBase;
        while (trunkTop.getY() + 1 < FFDHeightHooks.maxYExclusive(world)
                && isVerticalLog(world, trunkTop.up())) {
            trunkTop = trunkTop.up();
        }
        if (!hasLeavesNearby(world, trunkTop, trunkBase.getX() >> 4, trunkBase.getZ() >> 4)) {
            return false;
        }
        return placeBeeNest(random, world, trunkTop,
                trunkBase.getX() >> 4, trunkBase.getZ() >> 4);
    }

    private void generateKelp(Random random, int chunkX, int chunkZ, World world, Biome biome) {
        int ratio = isWarmOcean(biome) ? FFDConfig.kelpWarmNoiseRatio : FFDConfig.kelpColdNoiseRatio;
        double noise = KELP_NOISE.getValue(chunkX * 16.0D / FFDConfig.kelpNoiseScale,
                chunkZ * 16.0D / FFDConfig.kelpNoiseScale);
        int attempts = (int) Math.ceil(noise * ratio);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
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
            if (pos.getY() <= FFDHeightHooks.minY(world)
                    || !isSourceWater(world, pos) || !isSourceWater(world, pos.up())
                    || !FFDBlocks.KELP.canPlaceBlockAt(world, pos)) {
                continue;
            }

            int requestedBodyHeight = FFDConfig.kelpWorldgenMaxBodyHeight <= 0 ? 0
                    : 1 + random.nextInt(FFDConfig.kelpWorldgenMaxBodyHeight);
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
                    BlockKelpHead.stateForAgeValue(random.nextInt(23)), WORLDGEN_FLAGS);
        }
    }

    private static boolean isSourceWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER)
                && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    private static BlockPos findWaterPlantBase(World world, int x, int z) {
        BlockPos cursor = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        int minY = FFDHeightHooks.minY(world);
        while (cursor.getY() > minY && !isSourceWater(world, cursor)) {
            if (world.getBlockState(cursor).getMaterial() != Material.WATER) {
                return null;
            }
            cursor = cursor.down();
        }
        if (!isSourceWater(world, cursor)) {
            return null;
        }
        while (cursor.getY() > minY && isSourceWater(world, cursor.down())) {
            cursor = cursor.down();
        }
        return cursor;
    }

    private static boolean isWarmOcean(Biome biome) {
        if (biome instanceof BiomeModernOcean) {
            BiomeModernOcean.Type type = ((BiomeModernOcean) biome).getType();
            return type == BiomeModernOcean.Type.WARM
                    || type == BiomeModernOcean.Type.LUKEWARM
                    || type == BiomeModernOcean.Type.DEEP_LUKEWARM;
        }
        return biome.getDefaultTemperature() > 0.5F;
    }

    private static boolean isWarmOceanOnly(Biome biome) {
        return biome instanceof BiomeModernOcean
                && ((BiomeModernOcean) biome).getType() == BiomeModernOcean.Type.WARM;
    }

    private void generateSeagrass(Random random, int chunkX, int chunkZ, World world,
                                  SeagrassSettings settings) {
        int baseX = chunkX * 16 + random.nextInt(16);
        int baseZ = chunkZ * 16 + random.nextInt(16);
        for (int attempt = 0; attempt < settings.attempts; attempt++) {
            int x = baseX + triangleOffset(random, 7);
            int z = baseZ + triangleOffset(random, 7);
            if (!isInChunk(x, z, chunkX, chunkZ)) {
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
        int baseX = chunkX * 16 + random.nextInt(16);
        int baseZ = chunkZ * 16 + random.nextInt(16);
        for (int attempt = 0; attempt < FFDConfig.seaPickleWorldgenAttempts; attempt++) {
            int x = baseX + triangleOffset(random, FFDConfig.seaPickleWorldgenOffsetRadius);
            int z = baseZ + triangleOffset(random, FFDConfig.seaPickleWorldgenOffsetRadius);
            if (!isInChunk(x, z, chunkX, chunkZ)) {
                continue;
            }
            BlockPos pos = findWaterPlantBase(world, x, z);
            if (pos == null) {
                continue;
            }
            if (!isSourceWater(world, pos)
                    || !BlockSeaPickle.isSeaPickleBiome(world.getBiome(pos))
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
