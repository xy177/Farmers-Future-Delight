package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.BlockGlowLichen;
import xy177.farmersfuturedelight.common.block.BlockPointedDripstone;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData;
import xy177.farmersfuturedelight.common.world.noise.FFDXoroshiroRandom;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

public final class WorldGenVerticalCaveBiomes {
    private static final Logger LOGGER = LogManager.getLogger("FFD Large Dripstone Trace");
    private static final boolean TRACE_LARGE_DRIPSTONE =
            Boolean.getBoolean("ffd.worldgen.traceLargeDripstone");
    private static final EnumFacing[] DIRECTIONS = EnumFacing.values();
    private static final int MIN_Y = FFDModernWorldgenData.MIN_Y;
    private static final int MAX_Y = FFDModernWorldgenData.MIN_Y
            + FFDModernWorldgenData.HEIGHT - 1;
    private static final int FEATURE_MAX_Y = 256;
    private static final int MIN_SURFACE_DEPTH = 8;
    private static final int LARGE_DRIPSTONE_HORIZONTAL_REACH = 16;
    private static final int DRIPSTONE_CLUSTER_HORIZONTAL_REACH = 8;
    private static final int POINTED_DRIPSTONE_HORIZONTAL_REACH = 13;

    private static final int LOCAL_MODIFICATIONS_STEP = 2;
    private static final int UNDERGROUND_DECORATION_STEP = 7;
    private static final int VEGETAL_DECORATION_STEP = 9;

    private static final int LARGE_DRIPSTONE_INDEX = 3;
    private static final int DRIPSTONE_CLUSTER_INDEX = 4;
    private static final int POINTED_DRIPSTONE_INDEX = 5;
    private static final int GLOW_LICHEN_INDEX = 0;
    private static final int LUSH_TALL_GRASS_INDEX = 29;
    private static final int LUSH_CEILING_INDEX = 30;
    private static final int LUSH_VINES_INDEX = 31;
    private static final int LUSH_CLAY_INDEX = 32;
    private static final int LUSH_FLOOR_INDEX = 33;
    private static final int LUSH_AZALEA_INDEX = 34;
    private static final int LUSH_SPORE_INDEX = 35;
    private static final int LUSH_CLASSIC_VINE_INDEX = 36;

    private final long worldSeed;
    private final FFDModernStoneProvider stones;
    private final long decorationXScale;
    private final long decorationZScale;
    private final NoiseGeneratorPerlin biomeInfoNoise;

    public WorldGenVerticalCaveBiomes(long worldSeed, FFDModernStoneProvider stones) {
        this.worldSeed = worldSeed;
        this.stones = stones;
        FFDXoroshiroRandom decorationRandom = new FFDXoroshiroRandom(worldSeed);
        decorationXScale = decorationRandom.nextLong() | 1L;
        decorationZScale = decorationRandom.nextLong() | 1L;
        biomeInfoNoise = new NoiseGeneratorPerlin(new Random(2345L), 1);
    }

    public void generateLocalModifications(int chunkX, int chunkZ,
                                           FFDVerticalBiomeData biomes, Access access) {
        if (!FFDItems.isDripstoneEnabled()) {
            return;
        }
        int sourceRadius = sourceChunkRadius(LARGE_DRIPSTONE_HORIZONTAL_REACH);
        for (int sourceChunkX = chunkX - sourceRadius;
             sourceChunkX <= chunkX + sourceRadius; sourceChunkX++) {
            for (int sourceChunkZ = chunkZ - sourceRadius;
                 sourceChunkZ <= chunkZ + sourceRadius; sourceChunkZ++) {
                placeLargeDripstone(sourceChunkX, sourceChunkZ, chunkX, chunkZ, access);
            }
        }
    }

    public void generateUndergroundDecoration(int chunkX, int chunkZ,
                                               FFDVerticalBiomeData biomes, Access access) {
        if (!FFDItems.isDripstoneEnabled()) {
            return;
        }
        int horizontalReach = Math.max(DRIPSTONE_CLUSTER_HORIZONTAL_REACH,
                POINTED_DRIPSTONE_HORIZONTAL_REACH);
        int sourceRadius = sourceChunkRadius(horizontalReach);
        for (int sourceChunkX = chunkX - sourceRadius;
             sourceChunkX <= chunkX + sourceRadius; sourceChunkX++) {
            for (int sourceChunkZ = chunkZ - sourceRadius;
                 sourceChunkZ <= chunkZ + sourceRadius; sourceChunkZ++) {
                placeDripstoneClusters(sourceChunkX, sourceChunkZ, chunkX, chunkZ, access);
                placePointedDripstone(sourceChunkX, sourceChunkZ, chunkX, chunkZ, access);
            }
        }
    }

    public void generateVegetation(int chunkX, int chunkZ,
                                   FFDVerticalBiomeData biomes, Access access) {
        long profileStageStart = FFDWorldgenFeatureProfiler.ENABLED ? System.nanoTime() : 0L;
        long[] profileStages = FFDWorldgenFeatureProfiler.ENABLED ? new long[9] : null;
        ProfiledAccess profiledAccess = FFDWorldgenFeatureProfiler.ENABLED
                ? new ProfiledAccess(access) : null;
        Access featureAccess = profiledAccess == null ? access : profiledAccess;
        if (FFDItems.isGlowLichenEnabled()) {
            if (profiledAccess != null) {
                profiledAccess.stage(0);
            }
            int glowLichenReach = FFDConfig.lushCaveGlowLichenSearchRange + 1;
            int glowLichenSourceRadius = sourceChunkRadius(glowLichenReach);
            for (int sourceChunkX = chunkX - glowLichenSourceRadius;
                 sourceChunkX <= chunkX + glowLichenSourceRadius; sourceChunkX++) {
                for (int sourceChunkZ = chunkZ - glowLichenSourceRadius;
                     sourceChunkZ <= chunkZ + glowLichenSourceRadius; sourceChunkZ++) {
                    placeGlowLichen(sourceChunkX, sourceChunkZ, chunkX, chunkZ, featureAccess);
                }
            }
        }
        if (FFDWorldgenFeatureProfiler.ENABLED) {
            profileStages[0] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        if (!FFDItems.isLushCaveEnabled()) {
            if (FFDWorldgenFeatureProfiler.ENABLED) {
                FFDWorldgenFeatureProfiler.recordVegetation(
                        profileStages, profiledAccess == null ? null : profiledAccess.finish());
            }
            return;
        }
        int tallGrassSourceRadius = sourceChunkRadius(
                FFDConfig.lushCaveTallGrassHorizontalOffset);
        if (profiledAccess != null) {
            profiledAccess.stage(1);
        }
        for (int sourceChunkX = chunkX - tallGrassSourceRadius;
             sourceChunkX <= chunkX + tallGrassSourceRadius; sourceChunkX++) {
            for (int sourceChunkZ = chunkZ - tallGrassSourceRadius;
                 sourceChunkZ <= chunkZ + tallGrassSourceRadius; sourceChunkZ++) {
                placeSurfaceTallGrass(sourceChunkX, sourceChunkZ, chunkX, chunkZ, featureAccess);
            }
        }
        if (FFDWorldgenFeatureProfiler.ENABLED) {
            profileStages[1] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        int lushSourceRadius = sourceChunkRadius(lushHorizontalReach());
        if (lushBlocks().hasMoss()) {
            if (profiledAccess != null) {
                profiledAccess.stage(2);
            }
            for (int sourceChunkX = chunkX - lushSourceRadius;
                 sourceChunkX <= chunkX + lushSourceRadius; sourceChunkX++) {
                for (int sourceChunkZ = chunkZ - lushSourceRadius;
                     sourceChunkZ <= chunkZ + lushSourceRadius; sourceChunkZ++) {
                    placeCeilingMoss(sourceChunkX, sourceChunkZ, chunkX, chunkZ, featureAccess);
                }
            }
        }
        if (FFDWorldgenFeatureProfiler.ENABLED) {
            profileStages[2] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        if (lushBlocks().hasCaveVines()) {
            if (profiledAccess != null) {
                profiledAccess.stage(3);
            }
            placeCaveVines(chunkX, chunkZ, biomes, featureAccess);
        }
        if (FFDWorldgenFeatureProfiler.ENABLED) {
            profileStages[3] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        if (profiledAccess != null) {
            profiledAccess.stage(4);
        }
        for (int sourceChunkX = chunkX - lushSourceRadius;
             sourceChunkX <= chunkX + lushSourceRadius; sourceChunkX++) {
            for (int sourceChunkZ = chunkZ - lushSourceRadius;
                 sourceChunkZ <= chunkZ + lushSourceRadius; sourceChunkZ++) {
                placeLushClay(sourceChunkX, sourceChunkZ, chunkX, chunkZ, featureAccess);
            }
        }
        if (FFDWorldgenFeatureProfiler.ENABLED) {
            profileStages[4] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        if (lushBlocks().hasMoss()) {
            if (profiledAccess != null) {
                profiledAccess.stage(5);
            }
            for (int sourceChunkX = chunkX - lushSourceRadius;
                 sourceChunkX <= chunkX + lushSourceRadius; sourceChunkX++) {
                for (int sourceChunkZ = chunkZ - lushSourceRadius;
                     sourceChunkZ <= chunkZ + lushSourceRadius; sourceChunkZ++) {
                    placeFloorMoss(sourceChunkX, sourceChunkZ, chunkX, chunkZ, featureAccess);
                }
            }
        }
        if (FFDWorldgenFeatureProfiler.ENABLED) {
            profileStages[5] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        if (lushBlocks().hasAzalea()) {
            if (profiledAccess != null) {
                profiledAccess.stage(6);
            }
            for (int sourceChunkX = chunkX - lushSourceRadius;
                 sourceChunkX <= chunkX + lushSourceRadius; sourceChunkX++) {
                for (int sourceChunkZ = chunkZ - lushSourceRadius;
                     sourceChunkZ <= chunkZ + lushSourceRadius; sourceChunkZ++) {
                    placeRootedAzaleaTrees(sourceChunkX, sourceChunkZ, chunkX, chunkZ, featureAccess);
                }
            }
        }
        if (FFDWorldgenFeatureProfiler.ENABLED) {
            profileStages[6] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        if (lushBlocks().hasSporeBlossom()) {
            if (profiledAccess != null) {
                profiledAccess.stage(7);
            }
            placeSporeBlossoms(chunkX, chunkZ, biomes, featureAccess);
        }
        if (FFDWorldgenFeatureProfiler.ENABLED) {
            profileStages[7] = System.nanoTime() - profileStageStart;
            profileStageStart = System.nanoTime();
        }
        if (profiledAccess != null) {
            profiledAccess.stage(8);
        }
        placeClassicVines(chunkX, chunkZ, biomes, featureAccess);
        if (FFDWorldgenFeatureProfiler.ENABLED) {
            profileStages[8] = System.nanoTime() - profileStageStart;
            FFDWorldgenFeatureProfiler.recordVegetation(
                    profileStages, profiledAccess == null ? null : profiledAccess.finish());
        }
    }

    private void placeLargeDripstone(int sourceChunkX, int sourceChunkZ,
                                     int targetChunkX, int targetChunkZ, Access access) {
        Random sourceRandom = randomForFeature(sourceChunkX, sourceChunkZ,
                LARGE_DRIPSTONE_INDEX, LOCAL_MODIFICATIONS_STEP);
        int attempts = between(sourceRandom, FFDConfig.dripstoneLargeMinAttempts,
                FFDConfig.dripstoneLargeMaxAttempts);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = (sourceChunkX << 4) + sourceRandom.nextInt(16);
            int y = sampleFeatureY(sourceRandom);
            int z = (sourceChunkZ << 4) + sourceRandom.nextInt(16);
            long attemptSeed = sourceRandom.nextLong();
            if (!intersectsTargetChunk(x, z, 16, targetChunkX, targetChunkZ)
                    || access.getBiome(x, y, z) != FFDVerticalBiome.DRIPSTONE_CAVES
                    || !isPlanningUnderground(access, x, y, z)) {
                continue;
            }
            LargeDripstonePlan plan = planLargeDripstone(access, x, y, z,
                    new FeatureRandom(attemptSeed));
            if (plan == null) {
                continue;
            }
            LargeDripstonePlacement placement = plan.place(access);
            if (TRACE_LARGE_DRIPSTONE) {
                LOGGER.info("FFD_LARGE_DRIPSTONE source={},{} attempt={} target={},{} "
                                + "fingerprint={} parameters={} expected={} writes={} bits={} "
                                + "downBits={} upBits={}",
                        sourceChunkX, sourceChunkZ, attempt, targetChunkX, targetChunkZ,
                        Long.toUnsignedString(plan.fingerprint, 16), plan.parameters(),
                        placement.expected.cardinality(), placement.writes,
                        Base64.getEncoder().encodeToString(placement.expected.toByteArray()),
                        encodeTraceBits(placement.stalactiteExpected),
                        encodeTraceBits(placement.stalagmiteExpected));
            }
        }
    }

    private LargeDripstonePlan planLargeDripstone(Access access, int x, int y, int z,
                                                   Random random) {
        if (!isEmptyOrWater(access.getPlanningState(x, y, z))) {
            return null;
        }
        Column column = scanPlanningColumn(access, x, y, z, 30, true);
        if (column == null || column.floor == Integer.MIN_VALUE
                || column.ceiling == Integer.MIN_VALUE || column.height() < 4) {
            return null;
        }
        int maxRadius = clamp((int) (column.height() * 0.33F), 3, 16);
        if (maxRadius < 3) {
            return null;
        }
        int radius = between(random, 3, maxRadius);
        double stalactiteBluntness = randomBetween(random, 0.3D, 0.9D);
        double stalagmiteBluntness = randomBetween(random, 0.4D, 1.0D);
        double stalactiteScale = randomBetween(random, 0.4D, 2.0D);
        double stalagmiteScale = randomBetween(random, 0.4D, 2.0D);
        WindOffset wind = radius >= 4 && stalactiteBluntness >= 0.6D
                && stalagmiteBluntness >= 0.6D
                ? new WindOffset(y, random, radius) : WindOffset.NONE;
        LargeDripstone stalactite = fitLargeDripstone(access, x, column.ceiling - 1, z,
                false, radius, stalactiteBluntness, stalactiteScale, random.nextLong(), wind);
        LargeDripstone stalagmite = fitLargeDripstone(access, x, column.floor + 1, z,
                true, radius, stalagmiteBluntness, stalagmiteScale, random.nextLong(), wind);
        return stalactite == null && stalagmite == null ? null
                : new LargeDripstonePlan(stalactite, stalagmite, wind);
    }

    private LargeDripstone fitLargeDripstone(Access access, int rootX, int rootY, int rootZ,
                                               boolean pointingUp, int originalRadius,
                                               double bluntness, double scale, long placementSeed,
                                               WindOffset wind) {
        int radius = originalRadius;
        while (radius > 1) {
            int candidateY = rootY;
            int tries = Math.min(10, largeDripstoneHeightAt(0.0F, radius, scale, bluntness));
            for (int attempt = 0; attempt < tries; attempt++) {
                int[] offset = wind.offset(rootX, candidateY, rootZ);
                if (isLava(access.getPlanningState(offset[0], offset[1], offset[2]))) {
                    return null;
                }
                if (circleEmbedded(access, offset[0], offset[1], offset[2], radius)) {
                    return new LargeDripstone(rootX, candidateY, rootZ, pointingUp,
                            radius, bluntness, scale, placementSeed);
                }
                candidateY += pointingUp ? -1 : 1;
            }
            radius /= 2;
        }
        return null;
    }

    private void placeDripstoneClusters(int sourceChunkX, int sourceChunkZ,
                                        int targetChunkX, int targetChunkZ, Access access) {
        Random random = randomForFeature(sourceChunkX, sourceChunkZ,
                DRIPSTONE_CLUSTER_INDEX, UNDERGROUND_DECORATION_STEP);
        int attempts = between(random, FFDConfig.dripstoneClusterMinAttempts,
                FFDConfig.dripstoneClusterMaxAttempts);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = (sourceChunkX << 4) + random.nextInt(16);
            int y = sampleFeatureY(random);
            int z = (sourceChunkZ << 4) + random.nextInt(16);
            if (!intersectsTargetChunk(x, z, 8, targetChunkX, targetChunkZ)
                    || access.getBiome(x, y, z) != FFDVerticalBiome.DRIPSTONE_CAVES
                    || !isUnderground(access, x, y, z)
                    || !isEmptyOrWater(access.getState(x, y, z))) {
                continue;
            }
            int clusterHeight = between(random, 3, 6);
            float wetness = clampedGaussian(random, 0.1F, 0.3F, 0.1F, 0.9F);
            float density = randomBetween(random, 0.3F, 0.7F);
            int radiusX = between(random, 2, 8);
            int radiusZ = between(random, 2, 8);
            for (int dx = -radiusX; dx <= radiusX; dx++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                    int columnX = x + dx;
                    int columnZ = z + dz;
                    double chance = dripstoneChance(radiusX, radiusZ, dx, dz);
                    placeDripstoneClusterColumn(access, columnX, y, columnZ, dx, dz,
                            clusterHeight, density, wetness, chance, random);
                }
            }
        }
    }

    private void placeDripstoneClusterColumn(Access access, int x, int y, int z,
                                             int dx, int dz, int clusterHeight,
                                             float density, float wetness,
                                             double chance, Random random) {
        Column column = scanColumn(access, x, y, z, 12, false);
        if (column == null || column.floor == Integer.MIN_VALUE
                && column.ceiling == Integer.MIN_VALUE) {
            return;
        }
        if (column.floor != Integer.MIN_VALUE && random.nextFloat() < wetness
                && canPlaceDripstonePool(access, x, column.floor, z)) {
            access.setState(x, column.floor, z, Blocks.WATER.getDefaultState());
            access.markFluidUpdate(x, column.floor, z);
            column.floor--;
        }

        int stalactiteHeight = 0;
        if (column.ceiling != Integer.MIN_VALUE && random.nextDouble() < chance
                && !isLava(access.getState(x, column.ceiling, z))) {
            replaceWithDripstone(access, x, column.ceiling, z,
                    between(random, 2, 4), EnumFacing.UP);
            int maxHeight = column.floor == Integer.MIN_VALUE ? clusterHeight
                    : Math.min(clusterHeight, column.ceiling - column.floor);
            stalactiteHeight = sampleClusterHeight(random, dx, dz, density, maxHeight);
        }

        int stalagmiteHeight = 0;
        if (column.floor != Integer.MIN_VALUE && random.nextDouble() < chance
                && !isLava(access.getState(x, column.floor, z))) {
            replaceWithDripstone(access, x, column.floor, z,
                    between(random, 2, 4), EnumFacing.DOWN);
            stalagmiteHeight = column.ceiling == Integer.MIN_VALUE
                    ? sampleClusterHeight(random, dx, dz, density, clusterHeight)
                    : Math.max(0, stalactiteHeight + between(random, -1, 1));
        }

        int actualStalactite = stalactiteHeight;
        int actualStalagmite = stalagmiteHeight;
        if (column.floor != Integer.MIN_VALUE && column.ceiling != Integer.MIN_VALUE
                && column.ceiling - stalactiteHeight <= column.floor + stalagmiteHeight) {
            int lowestBottom = Math.max(column.ceiling - stalactiteHeight, column.floor + 1);
            int highestTop = Math.min(column.floor + stalagmiteHeight, column.ceiling - 1);
            int stalactiteBottom = between(random, lowestBottom, highestTop + 1);
            int stalagmiteTop = stalactiteBottom - 1;
            actualStalactite = column.ceiling - stalactiteBottom;
            actualStalagmite = stalagmiteTop - column.floor;
        }
        boolean merge = random.nextBoolean() && actualStalactite > 0 && actualStalagmite > 0
                && column.height() != Integer.MIN_VALUE
                && actualStalactite + actualStalagmite == column.height();
        if (column.ceiling != Integer.MIN_VALUE) {
            growPointedDripstone(access, x, column.ceiling - 1, z,
                    EnumFacing.DOWN, actualStalactite, merge);
        }
        if (column.floor != Integer.MIN_VALUE) {
            growPointedDripstone(access, x, column.floor + 1, z,
                    EnumFacing.UP, actualStalagmite, merge);
        }
    }

    private void placePointedDripstone(int sourceChunkX, int sourceChunkZ,
                                       int targetChunkX, int targetChunkZ, Access access) {
        Random random = randomForFeature(sourceChunkX, sourceChunkZ,
                POINTED_DRIPSTONE_INDEX, UNDERGROUND_DECORATION_STEP);
        int attempts = between(random, FFDConfig.dripstonePointedMinAttempts,
                FFDConfig.dripstonePointedMaxAttempts);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int originX = (sourceChunkX << 4) + random.nextInt(16);
            int originY = sampleFeatureY(random);
            int originZ = (sourceChunkZ << 4) + random.nextInt(16);
            if (!intersectsTargetChunk(originX, originZ, 13, targetChunkX, targetChunkZ)) {
                continue;
            }
            int placements = between(random, 1, 5);
            for (int placement = 0; placement < placements; placement++) {
                int x = originX + clampedGaussianInt(random, 0.0D, 3.0D, -10, 10);
                int y = originY + clampedGaussianInt(random, 0.0D, 0.6D, -2, 2);
                int z = originZ + clampedGaussianInt(random, 0.0D, 3.0D, -10, 10);
                if (!validY(y)
                        || access.getBiome(x, y, z) != FFDVerticalBiome.DRIPSTONE_CAVES
                        || !isUnderground(access, x, y, z)) {
                    continue;
                }
                boolean floor = random.nextBoolean();
                EnumFacing searchDirection = floor ? EnumFacing.DOWN : EnumFacing.UP;
                EnumFacing tipDirection = searchDirection.getOpposite();
                int airY = findAirAtSurface(access, x, y, z, searchDirection, 12, true);
                if (airY == Integer.MIN_VALUE || !isUnderground(access, x, airY, z)) {
                    continue;
                }
                int supportY = airY + searchDirection.getDirectionVec().getY();
                createDripstoneBasePatch(access, x, supportY, z, random);
                int height = random.nextFloat() < 0.2F
                        && isEmptyOrWater(access.getState(x,
                        airY + tipDirection.getDirectionVec().getY(), z)) ? 2 : 1;
                growPointedDripstone(access, x, airY, z, tipDirection, height, false);
            }
        }
    }

    private void placeGlowLichen(int sourceChunkX, int sourceChunkZ,
                                 int targetChunkX, int targetChunkZ, Access access) {
        Random random = randomForFeature(sourceChunkX, sourceChunkZ,
                GLOW_LICHEN_INDEX, VEGETAL_DECORATION_STEP);
        int attempts = between(random, FFDConfig.lushCaveGlowLichenMinAttempts,
                FFDConfig.lushCaveGlowLichenMaxAttempts);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = (sourceChunkX << 4) + random.nextInt(16);
            int y = sampleFeatureY(random);
            int z = (sourceChunkZ << 4) + random.nextInt(16);
            if (!intersectsTargetChunk(x, z, FFDConfig.lushCaveGlowLichenSearchRange + 1,
                    targetChunkX, targetChunkZ)) {
                continue;
            }
            if (y > access.getOceanFloorY(x, z) - FFDConfig.glowLichenSurfaceOffset) {
                continue;
            }
            placeGlowLichenFeature(access, x, y, z, random);
        }
    }

    private void placeGlowLichenFeature(Access access, int originX, int originY,
                                        int originZ, Random random) {
        if (!canLichenOccupy(access.getState(originX, originY, originZ))) {
            return;
        }
        List<EnumFacing> searchDirections = lichenDirections();
        Collections.shuffle(searchDirections, random);
        if (tryPlaceLichen(access, originX, originY, originZ, searchDirections, random)) {
            return;
        }
        for (EnumFacing search : searchDirections) {
            List<EnumFacing> placements = lichenDirections();
            placements.remove(search.getOpposite());
            Collections.shuffle(placements, random);
            for (int distance = 1; distance <= FFDConfig.lushCaveGlowLichenSearchRange; distance++) {
                int x = originX + search.getDirectionVec().getX() * distance;
                int y = originY + search.getDirectionVec().getY() * distance;
                int z = originZ + search.getDirectionVec().getZ() * distance;
                if (!validY(y) || !canLichenOccupy(access.getState(x, y, z))) {
                    break;
                }
                if (tryPlaceLichen(access, x, y, z, placements, random)) {
                    return;
                }
            }
        }
    }

    private boolean tryPlaceLichen(Access access, int x, int y, int z,
                                   List<EnumFacing> directions, Random random) {
        for (EnumFacing face : directions) {
            int supportX = x + face.getDirectionVec().getX();
            int supportY = y + face.getDirectionVec().getY();
            int supportZ = z + face.getDirectionVec().getZ();
            if (!validY(supportY) || !isLichenSupport(access.getState(supportX, supportY, supportZ))) {
                continue;
            }
            IBlockState oldState = access.getState(x, y, z);
            int mask = BlockGlowLichen.getFaceMask(oldState) | BlockGlowLichen.faceBit(face);
            boolean waterlogged = BlockGlowLichen.isGlowLichen(oldState)
                    ? BlockGlowLichen.isWaterlogged(oldState) : isSourceWater(oldState);
            access.setState(x, y, z, BlockGlowLichen.stateFor(mask, waterlogged));
            if (waterlogged) {
                access.markFluidUpdate(x, y, z);
            }
            if (random.nextFloat() < FFDConfig.lushCaveGlowLichenSpreadChance) {
                spreadLichen(access, x, y, z, face, random);
            }
            return true;
        }
        return false;
    }

    private void spreadLichen(Access access, int x, int y, int z,
                              EnumFacing sourceFace, Random random) {
        List<EnumFacing> directions = new ArrayList<>();
        Collections.addAll(directions, DIRECTIONS);
        Collections.shuffle(directions, random);
        for (EnumFacing direction : directions) {
            if (direction.getAxis() == sourceFace.getAxis()) {
                continue;
            }
            if (trySpreadLichen(access, x, y, z, direction)
                    || trySpreadLichen(access, x + direction.getDirectionVec().getX(),
                    y + direction.getDirectionVec().getY(), z + direction.getDirectionVec().getZ(), sourceFace)
                    || trySpreadLichen(access, x + direction.getDirectionVec().getX() + sourceFace.getDirectionVec().getX(),
                    y + direction.getDirectionVec().getY() + sourceFace.getDirectionVec().getY(),
                    z + direction.getDirectionVec().getZ() + sourceFace.getDirectionVec().getZ(), sourceFace.getOpposite())) {
                return;
            }
        }
    }

    private boolean trySpreadLichen(Access access, int x, int y, int z, EnumFacing face) {
        if (!validY(y)) {
            return false;
        }
        IBlockState oldState = access.getState(x, y, z);
        if (!canLichenOccupy(oldState) || BlockGlowLichen.hasFace(oldState, face)) {
            return false;
        }
        int supportX = x + face.getDirectionVec().getX();
        int supportY = y + face.getDirectionVec().getY();
        int supportZ = z + face.getDirectionVec().getZ();
        if (!validY(supportY) || !isLichenSupport(access.getState(supportX, supportY, supportZ))) {
            return false;
        }
        boolean waterlogged = BlockGlowLichen.isGlowLichen(oldState)
                ? BlockGlowLichen.isWaterlogged(oldState) : isSourceWater(oldState);
        access.setState(x, y, z, BlockGlowLichen.stateFor(
                BlockGlowLichen.getFaceMask(oldState) | BlockGlowLichen.faceBit(face), waterlogged));
        if (waterlogged) {
            access.markFluidUpdate(x, y, z);
        }
        return true;
    }

    private void placeCeilingMoss(int sourceChunkX, int sourceChunkZ,
                                  int targetChunkX, int targetChunkZ, Access access) {
        Random random = randomForFeature(sourceChunkX, sourceChunkZ,
                LUSH_CEILING_INDEX, VEGETAL_DECORATION_STEP);
        for (int attempt = 0; attempt < FFDConfig.lushCaveMossCeilingAttempts; attempt++) {
            int x = (sourceChunkX << 4) + random.nextInt(16);
            int z = (sourceChunkZ << 4) + random.nextInt(16);
            int reach = FFDConfig.lushCaveMossPatchMaxRadius + 1;
            if (!intersectsTargetChunk(x, z, reach, targetChunkX, targetChunkZ)) {
                continue;
            }
            int airY = findAirAtSurface(access, x, sampleFeatureY(random), z,
                    EnumFacing.UP, 12, false);
            if (airY != Integer.MIN_VALUE
                    && isLushCaveUnderground(access, x, airY, z)) {
                placeCeilingMossPatch(access, x, airY, z, random);
            }
        }
    }

    private void placeSurfaceTallGrass(int sourceChunkX, int sourceChunkZ,
                                       int targetChunkX, int targetChunkZ, Access access) {
        int sourceX = sourceChunkX << 4;
        int sourceZ = sourceChunkZ << 4;
        double noise = biomeInfoNoise.getValue(sourceX / 200.0D, sourceZ / 200.0D);
        if (noise < FFDConfig.lushCaveTallGrassNoiseThreshold) {
            return;
        }
        Random random = randomForFeature(sourceChunkX, sourceChunkZ,
                LUSH_TALL_GRASS_INDEX, VEGETAL_DECORATION_STEP);
        int offset = FFDConfig.lushCaveTallGrassHorizontalOffset;
        for (int candidate = 0; candidate < FFDConfig.lushCaveTallGrassAboveNoiseCount;
             candidate++) {
            if (random.nextInt(FFDConfig.lushCaveTallGrassRarity) != 0) {
                continue;
            }
            int originX = sourceX + random.nextInt(16);
            int originZ = sourceZ + random.nextInt(16);
            if (!intersectsTargetChunk(originX, originZ, offset,
                    targetChunkX, targetChunkZ)) {
                continue;
            }
            int originY = access.getOceanFloorY(originX, originZ) + 1;
            if (!validY(originY)
                    || !isLushCaveUnderground(access, originX, originY, originZ)) {
                continue;
            }
            for (int attempt = 0; attempt < FFDConfig.lushCaveTallGrassPatchAttempts;
                 attempt++) {
                int x = originX + triangle(random,
                        FFDConfig.lushCaveTallGrassHorizontalOffset);
                int y = originY + triangle(random,
                        FFDConfig.lushCaveTallGrassVerticalOffset);
                int z = originZ + triangle(random,
                        FFDConfig.lushCaveTallGrassHorizontalOffset);
                placeTallGrass(access, x, y, z);
            }
        }
    }

    private void placeCeilingMossPatch(Access access, int originX, int airY,
                                       int originZ, Random random) {
        int radiusX = between(random, FFDConfig.lushCaveMossPatchMinRadius,
                FFDConfig.lushCaveMossPatchMaxRadius) + 1;
        int radiusZ = between(random, FFDConfig.lushCaveMossPatchMinRadius,
                FFDConfig.lushCaveMossPatchMaxRadius) + 1;
        Set<BlockPos> vegetation = new HashSet<>();
        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                if (!usePatchColumn(dx, dz, radiusX, radiusZ, random,
                        FFDConfig.lushCaveMossEdgeColumnChance)) {
                    continue;
                }
                int x = originX + dx;
                int z = originZ + dz;
                int surfaceY = findSurfaceNear(access, x, airY, z,
                        EnumFacing.UP, FFDConfig.lushCaveMossPatchVerticalRange);
                if (surfaceY == Integer.MIN_VALUE
                        || !isUnderground(access, x, surfaceY, z)
                        || !isMossReplaceable(access.getState(x, surfaceY + 1, z))) {
                    continue;
                }
                int depth = between(random, FFDConfig.lushCaveMossCeilingMinDepth,
                        FFDConfig.lushCaveMossCeilingMaxDepth);
                boolean placed = false;
                for (int offset = 1; offset <= depth && surfaceY + offset <= MAX_Y; offset++) {
                    int groundY = surfaceY + offset;
                    if (!isMossReplaceable(access.getState(x, groundY, z))) {
                        break;
                    }
                    access.setState(x, groundY, z, lushBlocks().mossBlock());
                    placed = true;
                }
                if (placed) {
                    vegetation.add(new BlockPos(x, surfaceY, z));
                }
            }
        }
        if (lushBlocks().hasCaveVines()) {
            for (BlockPos pos : vegetation) {
                if (random.nextFloat() < FFDConfig.lushCaveMossCeilingVineChance) {
                    placeCaveVineColumn(access, pos.getX(), pos.getY(), pos.getZ(),
                            random.nextInt(6) < 5 ? random.nextInt(4) : 1 + random.nextInt(7), random);
                }
            }
        }
    }

    private void placeFloorMoss(int sourceChunkX, int sourceChunkZ,
                                int targetChunkX, int targetChunkZ, Access access) {
        Random random = randomForFeature(sourceChunkX, sourceChunkZ,
                LUSH_FLOOR_INDEX, VEGETAL_DECORATION_STEP);
        for (int attempt = 0; attempt < FFDConfig.lushCaveMossFloorAttempts; attempt++) {
            int x = (sourceChunkX << 4) + random.nextInt(16);
            int z = (sourceChunkZ << 4) + random.nextInt(16);
            int reach = FFDConfig.lushCaveMossPatchMaxRadius + 1;
            if (!intersectsTargetChunk(x, z, reach, targetChunkX, targetChunkZ)) {
                continue;
            }
            int airY = findAirAtSurface(access, x, sampleFeatureY(random), z,
                    EnumFacing.DOWN, 12, false);
            if (airY != Integer.MIN_VALUE
                    && isLushCaveUnderground(access, x, airY, z)) {
                placeFloorMossPatch(access, x, airY, z, random);
            }
        }
    }

    private void placeFloorMossPatch(Access access, int originX, int airY,
                                     int originZ, Random random) {
        int radiusX = between(random, FFDConfig.lushCaveMossPatchMinRadius,
                FFDConfig.lushCaveMossPatchMaxRadius) + 1;
        int radiusZ = between(random, FFDConfig.lushCaveMossPatchMinRadius,
                FFDConfig.lushCaveMossPatchMaxRadius) + 1;
        Set<BlockPos> vegetation = new HashSet<>();
        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                if (!usePatchColumn(dx, dz, radiusX, radiusZ, random,
                        FFDConfig.lushCaveMossEdgeColumnChance)) {
                    continue;
                }
                int x = originX + dx;
                int z = originZ + dz;
                int surfaceY = findSurfaceNear(access, x, airY, z,
                        EnumFacing.DOWN, FFDConfig.lushCaveMossPatchVerticalRange);
                if (surfaceY == Integer.MIN_VALUE
                        || !isUnderground(access, x, surfaceY, z)
                        || !isMossReplaceable(access.getState(x, surfaceY - 1, z))) {
                    continue;
                }
                access.setState(x, surfaceY - 1, z, lushBlocks().mossBlock());
                vegetation.add(new BlockPos(x, surfaceY, z));
            }
        }
        for (BlockPos pos : vegetation) {
            if (random.nextFloat() < FFDConfig.lushCaveMossFloorVegetationChance) {
                placeMossVegetation(access, pos.getX(), pos.getY(), pos.getZ(), random);
            }
        }
    }

    private void placeLushClay(int sourceChunkX, int sourceChunkZ,
                               int targetChunkX, int targetChunkZ, Access access) {
        Random random = randomForFeature(sourceChunkX, sourceChunkZ,
                LUSH_CLAY_INDEX, VEGETAL_DECORATION_STEP);
        for (int attempt = 0; attempt < FFDConfig.lushCaveClayAttempts; attempt++) {
            int x = (sourceChunkX << 4) + random.nextInt(16);
            int z = (sourceChunkZ << 4) + random.nextInt(16);
            int reach = FFDConfig.lushCaveClayPatchMaxRadius + 1;
            if (!intersectsTargetChunk(x, z, reach, targetChunkX, targetChunkZ)) {
                continue;
            }
            int airY = findAirAtSurface(access, x, sampleFeatureY(random), z,
                    EnumFacing.DOWN, 12, false);
            if (airY != Integer.MIN_VALUE
                    && isLushCaveUnderground(access, x, airY, z)) {
                placeClayPatch(access, x, airY, z, random);
            }
        }
    }

    private void placeClayPatch(Access access, int originX, int airY,
                                int originZ, Random random) {
        boolean waterPool = random.nextInt(100) < FFDConfig.lushCaveWaterPoolChance;
        int radiusX = between(random, FFDConfig.lushCaveClayPatchMinRadius,
                FFDConfig.lushCaveClayPatchMaxRadius) + 1;
        int radiusZ = between(random, FFDConfig.lushCaveClayPatchMinRadius,
                FFDConfig.lushCaveClayPatchMaxRadius) + 1;
        int verticalRange = waterPool ? FFDConfig.lushCaveWaterClayVerticalRange
                : FFDConfig.lushCaveDryClayVerticalRange;
        List<BlockPos> surface = new ArrayList<>();
        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                if (!usePatchColumn(dx, dz, radiusX, radiusZ, random,
                        FFDConfig.lushCaveClayEdgeColumnChance)) {
                    continue;
                }
                int x = originX + dx;
                int z = originZ + dz;
                int surfaceY = findSurfaceNear(access, x, airY, z,
                        EnumFacing.DOWN, verticalRange);
                if (surfaceY == Integer.MIN_VALUE
                        || !isUnderground(access, x, surfaceY, z)) {
                    continue;
                }
                int depth = FFDConfig.lushCaveClayPatchDepth
                        + (random.nextFloat() < FFDConfig.lushCaveClayExtraBottomChance ? 1 : 0);
                int groundY = surfaceY - 1;
                if (placeClayGround(access, x, groundY, z, depth)) {
                    surface.add(new BlockPos(x, groundY, z));
                }
            }
        }

        List<BlockPos> vegetationSurface = surface;
        if (waterPool) {
            vegetationSurface = new ArrayList<>();
            for (BlockPos pos : surface) {
                if (!isClaySurfaceExposed(access, pos)) {
                    vegetationSurface.add(pos);
                }
            }
            for (BlockPos pos : vegetationSurface) {
                access.setState(pos.getX(), pos.getY(), pos.getZ(), Blocks.WATER.getDefaultState());
                access.markFluidUpdate(pos.getX(), pos.getY(), pos.getZ());
            }
        }

        int chance = waterPool ? FFDConfig.lushCaveWaterDripleafChance
                : FFDConfig.lushCaveDryDripleafChance;
        if (lushBlocks().hasDripleaf() && chance > 0) {
            for (BlockPos pos : vegetationSurface) {
                if (random.nextInt(100) < chance) {
                    placeDripleaf(access, pos.getX(), waterPool ? pos.getY() : pos.getY() + 1,
                            pos.getZ(), random);
                }
            }
        }
    }

    private void placeCaveVines(int chunkX, int chunkZ,
                                FFDVerticalBiomeData biomes, Access access) {
        Random random = randomForFeature(chunkX, chunkZ,
                LUSH_VINES_INDEX, VEGETAL_DECORATION_STEP);
        for (int attempt = 0; attempt < FFDConfig.lushCaveVineAttempts; attempt++) {
            int x = (chunkX << 4) + random.nextInt(16);
            int z = (chunkZ << 4) + random.nextInt(16);
            int airY = findAirAtSurface(access, x, sampleFeatureY(random), z,
                    EnumFacing.UP, 12, false);
            if (airY == Integer.MIN_VALUE
                    || !isLushCaveUnderground(access, x, airY, z)) {
                continue;
            }
            int distribution = random.nextInt(15);
            int bodyLength = distribution < 2 ? random.nextInt(20)
                    : distribution < 5 ? random.nextInt(3) : random.nextInt(7);
            placeCaveVineColumn(access, x, airY, z, bodyLength, random);
        }
    }

    private void placeCaveVineColumn(Access access, int x, int startY, int z,
                                     int bodyLength, Random random) {
        int length = bodyLength + 1;
        int placed = 0;
        for (int offset = 0; offset < length && startY - offset >= MIN_Y; offset++) {
            if (!isAir(access.getState(x, startY - offset, z))) {
                break;
            }
            placed++;
        }
        for (int offset = 0; offset < placed; offset++) {
            boolean tip = offset == placed - 1;
            boolean berries = random.nextInt(5) == 0;
            IBlockState state = lushBlocks().caveVines(tip, berries);
            access.setState(x, startY - offset, z, state);
        }
    }

    private void placeRootedAzaleaTrees(int sourceChunkX, int sourceChunkZ,
                                        int targetChunkX, int targetChunkZ, Access access) {
        Random random = randomForFeature(sourceChunkX, sourceChunkZ,
                LUSH_AZALEA_INDEX, VEGETAL_DECORATION_STEP);
        int attempts = between(random, FFDConfig.lushCaveAzaleaTreeMin,
                FFDConfig.lushCaveAzaleaTreeMax);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = (sourceChunkX << 4) + random.nextInt(16);
            int z = (sourceChunkZ << 4) + random.nextInt(16);
            int reach = Math.max(8, Math.max(FFDConfig.lushCaveRootRadius,
                    FFDConfig.lushCaveHangingRootRadius) + 3);
            if (!intersectsTargetChunk(x, z, reach, targetChunkX, targetChunkZ)) {
                continue;
            }
            int airY = findAirAtSurface(access, x, sampleFeatureY(random), z,
                    EnumFacing.UP, 12, false);
            if (airY != Integer.MIN_VALUE
                    && isLushCaveUnderground(access, x, airY, z)) {
                placeRootedAzaleaTree(access, x, airY, z, random);
            }
        }
    }

    private void placeRootedAzaleaTree(Access access, int originX, int originY,
                                       int originZ, Random random) {
        if (!isAir(access.getState(originX, originY, originZ))) {
            return;
        }
        int maxY = Math.min(MAX_Y - 1, originY + FFDConfig.lushCaveRootColumnMaxHeight);
        for (int treeY = originY + 1; treeY <= maxY; treeY++) {
            if (!isAllowedTreePosition(access, originX, treeY, originZ)
                    || !hasRequiredTreeSpace(access, originX, treeY, originZ)
                    || !placeAzaleaTree(access, originX, treeY, originZ, random)) {
                continue;
            }
            placeRootedDirtColumn(access, originX, originY, originZ, treeY - 1, random);
            placeHangingRoots(access, originX, originY, originZ, random);
            return;
        }
    }

    private void placeSporeBlossoms(int chunkX, int chunkZ,
                                    FFDVerticalBiomeData biomes, Access access) {
        Random random = randomForFeature(chunkX, chunkZ,
                LUSH_SPORE_INDEX, VEGETAL_DECORATION_STEP);
        for (int attempt = 0; attempt < FFDConfig.lushCaveSporeBlossomAttempts; attempt++) {
            int x = (chunkX << 4) + random.nextInt(16);
            int z = (chunkZ << 4) + random.nextInt(16);
            int airY = findAirAtSurface(access, x, sampleFeatureY(random), z,
                    EnumFacing.UP, 12, false);
            if (airY != Integer.MIN_VALUE && isAir(access.getState(x, airY, z))
                    && isLushCaveUnderground(access, x, airY, z)) {
                access.setState(x, airY, z, lushBlocks().sporeBlossom());
            }
        }
    }

    private void placeClassicVines(int chunkX, int chunkZ,
                                   FFDVerticalBiomeData biomes, Access access) {
        Random random = randomForFeature(chunkX, chunkZ,
                LUSH_CLASSIC_VINE_INDEX, VEGETAL_DECORATION_STEP);
        for (int attempt = 0; attempt < FFDConfig.lushCaveClassicVineAttempts; attempt++) {
            int x = (chunkX << 4) + random.nextInt(16);
            int y = sampleFeatureY(random);
            int z = (chunkZ << 4) + random.nextInt(16);
            if (!isLushCaveUnderground(access, x, y, z)
                    || !isAir(access.getState(x, y, z))) {
                continue;
            }
            for (EnumFacing face : EnumFacing.Plane.HORIZONTAL) {
                int supportX = x + face.getDirectionVec().getX();
                int supportY = y + face.getDirectionVec().getY();
                int supportZ = z + face.getDirectionVec().getZ();
                if (access.getFaceShape(supportX, supportY, supportZ,
                        face.getOpposite()) == BlockFaceShape.SOLID) {
                    access.setState(x, y, z, Blocks.VINE.getDefaultState()
                            .withProperty(BlockVine.getPropertyFor(face), true));
                    break;
                }
            }
        }
    }

    private static int findAirAtSurface(Access access, int x, int startY, int z,
                                        EnumFacing searchDirection, int maxSteps,
                                        boolean allowWater) {
        if (!validY(startY) || !isSearchSpace(access.getState(x, startY, z), allowWater)) {
            return Integer.MIN_VALUE;
        }
        int y = startY;
        for (int step = 0; step < maxSteps; step++) {
            int supportY = y + searchDirection.getDirectionVec().getY();
            if (!validY(supportY)) {
                return Integer.MIN_VALUE;
            }
            IBlockState support = access.getState(x, supportY, z);
            if (isSolid(support)) {
                return y;
            }
            if (!isSearchSpace(support, allowWater)) {
                return Integer.MIN_VALUE;
            }
            y = supportY;
        }
        return Integer.MIN_VALUE;
    }

    private static int findSurfaceNear(Access access, int x, int startY, int z,
                                       EnumFacing direction,
                                       int range) {
        int y = clamp(startY, MIN_Y + 1, MAX_Y - 1);
        for (int offset = 0; offset < range && validY(y)
                && isAir(access.getState(x, y, z)); offset++) {
            y += direction.getDirectionVec().getY();
        }
        for (int offset = 0; offset < range && validY(y)
                && !isAir(access.getState(x, y, z)); offset++) {
            y -= direction.getDirectionVec().getY();
        }
        int supportY = y + direction.getDirectionVec().getY();
        return validY(y) && validY(supportY) && isAir(access.getState(x, y, z))
                && isSolid(access.getState(x, supportY, z)) ? y : Integer.MIN_VALUE;
    }

    private Column scanColumn(Access access, int x, int y, int z,
                              int range, boolean requireBaseBoundary) {
        if (!isEmptyOrWater(access.getState(x, y, z))) {
            return null;
        }
        int floor = Integer.MIN_VALUE;
        int ceiling = Integer.MIN_VALUE;
        for (int step = 1; step <= range && y - step >= MIN_Y; step++) {
            IBlockState state = access.getState(x, y - step, z);
            if (isEmptyOrWater(state)) {
                continue;
            }
            if (!requireBaseBoundary || isDripstoneBase(state) || isLava(state)) {
                floor = y - step;
            }
            break;
        }
        for (int step = 1; step <= range && y + step <= MAX_Y; step++) {
            IBlockState state = access.getState(x, y + step, z);
            if (isEmptyOrWater(state)) {
                continue;
            }
            if (!requireBaseBoundary || isDripstoneBase(state) || isLava(state)) {
                ceiling = y + step;
            }
            break;
        }
        return floor == Integer.MIN_VALUE && ceiling == Integer.MIN_VALUE
                ? null : new Column(floor, ceiling);
    }

    private Column scanPlanningColumn(Access access, int x, int y, int z,
                                      int range, boolean requireBaseBoundary) {
        if (!isEmptyOrWater(access.getPlanningState(x, y, z))) {
            return null;
        }
        int floor = Integer.MIN_VALUE;
        int ceiling = Integer.MIN_VALUE;
        for (int step = 1; step <= range && y - step >= MIN_Y; step++) {
            IBlockState state = access.getPlanningState(x, y - step, z);
            if (isEmptyOrWater(state)) {
                continue;
            }
            if (!requireBaseBoundary || isDripstoneBase(state) || isLava(state)) {
                floor = y - step;
            }
            break;
        }
        for (int step = 1; step <= range && y + step <= MAX_Y; step++) {
            IBlockState state = access.getPlanningState(x, y + step, z);
            if (isEmptyOrWater(state)) {
                continue;
            }
            if (!requireBaseBoundary || isDripstoneBase(state) || isLava(state)) {
                ceiling = y + step;
            }
            break;
        }
        return floor == Integer.MIN_VALUE && ceiling == Integer.MIN_VALUE
                ? null : new Column(floor, ceiling);
    }

    private boolean canPlaceDripstonePool(Access access, int x, int y, int z) {
        IBlockState state = access.getState(x, y, z);
        if (isSourceWater(state) || stones.isDripstoneBlock(state)
                || stones.isPointedDripstone(state)
                || isSourceWater(access.getState(x, y + 1, z))) {
            return false;
        }
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            if (!canBorderWater(access.getState(x + facing.getDirectionVec().getX(), y,
                    z + facing.getDirectionVec().getZ()))) {
                return false;
            }
        }
        return canBorderWater(access.getState(x, y - 1, z));
    }

    private boolean canBorderWater(IBlockState state) {
        return isDripstoneBase(state) || isSourceWater(state);
    }

    private static int sampleClusterHeight(Random random, int dx, int dz,
                                           float density, int maxHeight) {
        if (maxHeight <= 0 || random.nextFloat() > density) {
            return 0;
        }
        int distance = Math.abs(dx) + Math.abs(dz);
        double mean = clampedMap(distance, 0.0D, 8.0D, maxHeight / 2.0D, 0.0D);
        return (int) clampedGaussian(random, (float) mean, 3.0F, 0.0F, maxHeight);
    }

    private static double dripstoneChance(int radiusX, int radiusZ, int dx, int dz) {
        int distance = Math.min(radiusX - Math.abs(dx), radiusZ - Math.abs(dz));
        return clampedMap(distance, 0.0D, 3.0D, 0.1D, 1.0D);
    }

    private void replaceWithDripstone(Access access, int x, int y, int z,
                                      int count, EnumFacing direction) {
        for (int offset = 0; offset < count; offset++) {
            int targetY = y + direction.getDirectionVec().getY() * offset;
            if (!validY(targetY) || !isDripstoneReplaceable(access.getState(x, targetY, z))) {
                return;
            }
            access.setState(x, targetY, z, stones.dripstoneBlock());
        }
    }

    private void createDripstoneBasePatch(Access access, int x, int y, int z,
                                           Random random) {
        placeDripstoneBase(access, x, y, z);
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            if (random.nextFloat() > 0.7F) {
                continue;
            }
            int x2 = x + facing.getDirectionVec().getX();
            int y2 = y + facing.getDirectionVec().getY();
            int z2 = z + facing.getDirectionVec().getZ();
            placeDripstoneBase(access, x2, y2, z2);
            if (random.nextFloat() > 0.5F) {
                continue;
            }
            EnumFacing spread2 = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            int x3 = x2 + spread2.getDirectionVec().getX();
            int y3 = y2 + spread2.getDirectionVec().getY();
            int z3 = z2 + spread2.getDirectionVec().getZ();
            placeDripstoneBase(access, x3, y3, z3);
            if (random.nextFloat() <= 0.5F) {
                EnumFacing spread3 = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
                placeDripstoneBase(access, x3 + spread3.getDirectionVec().getX(), y3 + spread3.getDirectionVec().getY(),
                        z3 + spread3.getDirectionVec().getZ());
            }
        }
    }

    private void placeDripstoneBase(Access access, int x, int y, int z) {
        if (validY(y) && isDripstoneReplaceable(access.getState(x, y, z))) {
            access.setState(x, y, z, stones.dripstoneBlock());
        }
    }

    private void growPointedDripstone(Access access, int x, int y, int z,
                                      EnumFacing direction, int height, boolean mergedTip) {
        if (height <= 0 || !isDripstoneBase(access.getState(x - direction.getDirectionVec().getX(),
                y - direction.getDirectionVec().getY(), z - direction.getDirectionVec().getZ()))) {
            return;
        }
        List<BlockPointedDripstone.Thickness> states = new ArrayList<>();
        if (height >= 3) {
            states.add(BlockPointedDripstone.Thickness.BASE);
            for (int i = 0; i < height - 3; i++) {
                states.add(BlockPointedDripstone.Thickness.MIDDLE);
            }
        }
        if (height >= 2) {
            states.add(BlockPointedDripstone.Thickness.FRUSTUM);
        }
        states.add(mergedTip ? BlockPointedDripstone.Thickness.TIP_MERGE
                : BlockPointedDripstone.Thickness.TIP);
        for (int index = 0; index < states.size(); index++) {
            int targetX = x + direction.getDirectionVec().getX() * index;
            int targetY = y + direction.getDirectionVec().getY() * index;
            int targetZ = z + direction.getDirectionVec().getZ() * index;
            if (!validY(targetY) || !isAir(access.getState(targetX, targetY, targetZ))) {
                return;
            }
            IBlockState pointed = stones.pointedDripstone(direction,
                    states.get(index).getName());
            if (pointed == null) {
                return;
            }
            access.setState(targetX, targetY, targetZ, pointed);
        }
    }

    private static void placeMossVegetation(Access access, int x, int y, int z, Random random) {
        if (!isAir(access.getState(x, y, z))) {
            return;
        }
        int choice = random.nextInt(96);
        if (choice < 4) {
            if (lushBlocks().hasAzalea()) {
                access.setState(x, y, z, lushBlocks().azalea(true));
            }
        } else if (choice < 11) {
            if (lushBlocks().hasAzalea()) {
                access.setState(x, y, z, lushBlocks().azalea(false));
            }
        } else if (choice < 36) {
            access.setState(x, y, z, lushBlocks().mossCarpet());
        } else if (choice < 86) {
            access.setState(x, y, z, Blocks.TALLGRASS.getDefaultState()
                    .withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS));
        } else if (y + 1 <= MAX_Y && isAir(access.getState(x, y + 1, z))) {
            access.setState(x, y, z, Blocks.DOUBLE_PLANT.getDefaultState()
                    .withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS)
                    .withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER));
            access.setState(x, y + 1, z, Blocks.DOUBLE_PLANT.getDefaultState()
                    .withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS)
                    .withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
        }
    }

    private static void placeTallGrass(Access access, int x, int y, int z) {
        if (!validY(y) || !validY(y + 1)
                || !isAir(access.getState(x, y, z))
                || !isAir(access.getState(x, y + 1, z))
                || !supportsVegetation(access.getState(x, y - 1, z))) {
            return;
        }
        access.setState(x, y, z, Blocks.DOUBLE_PLANT.getDefaultState()
                .withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS)
                .withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER));
        access.setState(x, y + 1, z, Blocks.DOUBLE_PLANT.getDefaultState()
                .withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS)
                .withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
    }

    private static boolean supportsVegetation(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.MYCELIUM
                || block == Blocks.FARMLAND || lushBlocks().isMossBlock(state)
                || lushBlocks().isRootedDirt(state);
    }

    private boolean placeClayGround(Access access, int x, int y, int z, int depth) {
        boolean placed = false;
        for (int offset = 0; offset < depth && y - offset >= MIN_Y; offset++) {
            int groundY = y - offset;
            IBlockState state = access.getState(x, groundY, z);
            if (state.getBlock() == Blocks.CLAY) {
                return true;
            }
            if (!isLushGroundReplaceable(state)) {
                return placed;
            }
            access.setState(x, groundY, z, Blocks.CLAY.getDefaultState());
            placed = true;
        }
        return placed;
    }

    private static boolean isClaySurfaceExposed(Access access, BlockPos pos) {
        if (!isSolid(access.getState(pos.getX(), pos.getY() - 1, pos.getZ()))) {
            return true;
        }
        for (EnumFacing face : EnumFacing.Plane.HORIZONTAL) {
            if (!isSolid(access.getState(pos.getX() + face.getDirectionVec().getX(), pos.getY(),
                    pos.getZ() + face.getDirectionVec().getZ()))) {
                return true;
            }
        }
        return false;
    }

    private static void placeDripleaf(Access access, int x, int baseY, int z, Random random) {
        if (!validY(baseY) || !isDripleafGround(access.getState(x, baseY - 1, z))
                || !isPlantSpace(access.getState(x, baseY, z))) {
            return;
        }
        EnumFacing facing = EnumFacing.getHorizontal(random.nextInt(4));
        if (random.nextInt(5) == 0) {
            if (baseY + 1 > MAX_Y || !isPlantSpace(access.getState(x, baseY + 1, z))) {
                return;
            }
            IBlockState lowerOld = access.getState(x, baseY, z);
            IBlockState upperOld = access.getState(x, baseY + 1, z);
            IBlockState lower = lushBlocks().smallDripleaf(facing,
                    BlockDoublePlant.EnumBlockHalf.LOWER, isSourceWater(lowerOld));
            IBlockState upper = lushBlocks().smallDripleaf(facing,
                    BlockDoublePlant.EnumBlockHalf.UPPER, isSourceWater(upperOld));
            access.setState(x, baseY, z, lower);
            access.setState(x, baseY + 1, z, upper);
            if (isSourceWater(lowerOld)) {
                access.markFluidUpdate(x, baseY, z);
            }
            if (isSourceWater(upperOld)) {
                access.markFluidUpdate(x, baseY + 1, z);
            }
            return;
        }

        int stemHeight = random.nextInt(3) < 2 ? random.nextInt(5) : 0;
        if (baseY + stemHeight > MAX_Y) {
            return;
        }
        for (int offset = 0; offset <= stemHeight; offset++) {
            if (!isPlantSpace(access.getState(x, baseY + offset, z))) {
                return;
            }
        }
        for (int offset = 0; offset < stemHeight; offset++) {
            IBlockState oldState = access.getState(x, baseY + offset, z);
            boolean waterlogged = isSourceWater(oldState);
            access.setState(x, baseY + offset, z,
                    lushBlocks().bigDripleafStem(facing, waterlogged));
            if (waterlogged) {
                access.markFluidUpdate(x, baseY + offset, z);
            }
        }
        IBlockState oldHead = access.getState(x, baseY + stemHeight, z);
        boolean waterloggedHead = isSourceWater(oldHead);
        access.setState(x, baseY + stemHeight, z,
                lushBlocks().bigDripleaf(facing, waterloggedHead));
        if (waterloggedHead) {
            access.markFluidUpdate(x, baseY + stemHeight, z);
        }
    }

    private static boolean isAllowedTreePosition(Access access, int x, int y, int z) {
        return validY(y) && isTreeReplaceable(access.getState(x, y, z))
                && supportsVegetation(access.getState(x, y - 1, z));
    }

    private static boolean hasRequiredTreeSpace(Access access, int x, int treeY, int z) {
        for (int offset = 1; offset <= FFDConfig.lushCaveRequiredVerticalSpaceForTree; offset++) {
            int y = treeY + offset;
            if (y > MAX_Y) {
                return false;
            }
            IBlockState state = access.getState(x, y, z);
            if (isAir(state)) {
                continue;
            }
            if (offset + 1 > FFDConfig.lushCaveAllowedVerticalWaterForTree
                    || state.getMaterial() != Material.WATER) {
                return false;
            }
        }
        return true;
    }

    private static boolean placeAzaleaTree(Access access, int baseX, int baseY,
                                           int baseZ, Random random) {
        int height = 4 + random.nextInt(3);
        EnumFacing bend = EnumFacing.getHorizontal(random.nextInt(4));
        List<BlockPos> logs = new ArrayList<>();
        List<BlockPos> foliagePoints = new ArrayList<>();
        BlockPos cursor = new BlockPos(baseX, baseY, baseZ);
        int logHeight = height - 1;
        for (int index = 0; index <= logHeight; index++) {
            if (index + 1 >= logHeight + random.nextInt(2)) {
                cursor = cursor.offset(bend);
            }
            logs.add(cursor);
            if (index >= 3) {
                foliagePoints.add(cursor);
            }
            cursor = cursor.up();
        }
        int bendLength = 1 + random.nextInt(2);
        for (int index = 0; index <= bendLength; index++) {
            logs.add(cursor);
            foliagePoints.add(cursor);
            cursor = cursor.offset(bend);
        }
        for (BlockPos log : logs) {
            if (!validY(log.getY())
                    || !isTreeReplaceable(access.getState(log.getX(), log.getY(), log.getZ()))) {
                return false;
            }
        }
        if (lushBlocks().hasRootedDirt()) {
            access.setState(baseX, baseY - 1, baseZ, lushBlocks().rootedDirt());
        }
        for (BlockPos log : logs) {
            access.setState(log.getX(), log.getY(), log.getZ(), oakLog());
        }
        for (BlockPos foliage : foliagePoints) {
            placeRandomFoliage(access, random, foliage);
        }
        return true;
    }

    private static void placeRandomFoliage(Access access, Random random, BlockPos origin) {
        for (int attempt = 0; attempt < 50; attempt++) {
            int x = origin.getX() + random.nextInt(3) - random.nextInt(3);
            int y = origin.getY() + random.nextInt(2) - random.nextInt(2);
            int z = origin.getZ() + random.nextInt(3) - random.nextInt(3);
            if (!validY(y) || !isTreeReplaceable(access.getState(x, y, z))) {
                continue;
            }
            access.setState(x, y, z, lushBlocks().azaleaLeaves(random.nextInt(4) == 0));
        }
    }

    private void placeRootedDirtColumn(Access access, int originX, int originY,
                                       int originZ, int targetY, Random random) {
        if (!lushBlocks().hasRootedDirt()) {
            return;
        }
        for (int y = originY; y < targetY; y++) {
            for (int attempt = 0; attempt < FFDConfig.lushCaveRootPlacementAttempts; attempt++) {
                int x = originX + random.nextInt(FFDConfig.lushCaveRootRadius)
                        - random.nextInt(FFDConfig.lushCaveRootRadius);
                int z = originZ + random.nextInt(FFDConfig.lushCaveRootRadius)
                        - random.nextInt(FFDConfig.lushCaveRootRadius);
                if (isRootReplaceable(access.getState(x, y, z))) {
                    access.setState(x, y, z, lushBlocks().rootedDirt());
                }
            }
        }
    }

    private static void placeHangingRoots(Access access, int originX, int originY,
                                          int originZ, Random random) {
        if (!lushBlocks().hasHangingRoots()) {
            return;
        }
        for (int attempt = 0; attempt < FFDConfig.lushCaveHangingRootPlacementAttempts; attempt++) {
            int x = originX + random.nextInt(FFDConfig.lushCaveHangingRootRadius)
                    - random.nextInt(FFDConfig.lushCaveHangingRootRadius);
            int y = originY + random.nextInt(FFDConfig.lushCaveHangingRootsVerticalSpan)
                    - random.nextInt(FFDConfig.lushCaveHangingRootsVerticalSpan);
            int z = originZ + random.nextInt(FFDConfig.lushCaveHangingRootRadius)
                    - random.nextInt(FFDConfig.lushCaveHangingRootRadius);
            if (validY(y) && isAir(access.getState(x, y, z))
                    && isSolid(access.getState(x, y + 1, z))) {
                access.setState(x, y, z, lushBlocks().hangingRoots());
            }
        }
    }

    private static boolean usePatchColumn(int dx, int dz, int radiusX, int radiusZ,
                                          Random random, float edgeChance) {
        boolean xEdge = dx == -radiusX || dx == radiusX;
        boolean zEdge = dz == -radiusZ || dz == radiusZ;
        if (xEdge && zEdge) {
            return false;
        }
        return !xEdge && !zEdge || random.nextFloat() <= edgeChance;
    }

    private static List<EnumFacing> lichenDirections() {
        List<EnumFacing> directions = new ArrayList<>();
        directions.add(EnumFacing.UP);
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            directions.add(facing);
        }
        return directions;
    }

    private static boolean canLichenOccupy(IBlockState state) {
        return isAir(state) || isSourceWater(state) || BlockGlowLichen.isGlowLichen(state);
    }

    private boolean isLichenSupport(IBlockState state) {
        Block block = state.getBlock();
        return isBaseStone(state) || stones.isDripstoneBlock(state)
                || block == FFDBlocks.CALCITE || block == FFDBlocks.TUFF;
    }

    private boolean isMossReplaceable(IBlockState state) {
        Block block = state.getBlock();
        return isBaseStone(state) || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.MYCELIUM || lushBlocks().isMossBlock(state)
                || lushBlocks().isRootedDirt(state) || lushBlocks().isCaveVine(state);
    }

    private boolean isLushGroundReplaceable(IBlockState state) {
        Block block = state.getBlock();
        return isMossReplaceable(state) || block == Blocks.CLAY
                || block == Blocks.SAND || block == Blocks.GRAVEL;
    }

    private static boolean isDripleafGround(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.CLAY
                || block == Blocks.MYCELIUM || block == Blocks.FARMLAND
                || lushBlocks().isMossBlock(state) || lushBlocks().isRootedDirt(state);
    }

    private boolean isRootReplaceable(IBlockState state) {
        Block block = state.getBlock();
        return isBaseStone(state) || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.SAND || block == Blocks.GRAVEL || block == Blocks.CLAY
                || block == Blocks.HARDENED_CLAY || block == Blocks.STAINED_HARDENED_CLAY
                || block == Blocks.SNOW || block == Blocks.MYCELIUM
                || lushBlocks().isMossBlock(state) || lushBlocks().isRootedDirt(state)
                || block == FFDBlocks.POWDER_SNOW;
    }

    private static boolean isTreeReplaceable(IBlockState state) {
        Material material = state.getMaterial();
        return material == Material.AIR || material == Material.PLANTS || material == Material.VINE
                || material == Material.LEAVES || material == Material.WATER
                || state.getBlock() == Blocks.SNOW_LAYER || lushBlocks().isHangingRoots(state)
                || lushBlocks().isCaveVine(state);
    }

    private boolean isDripstoneReplaceable(IBlockState state) {
        return isBaseStone(state);
    }

    private boolean isDripstoneBase(IBlockState state) {
        return stones.isDripstoneBlock(state) || isDripstoneReplaceable(state);
    }

    private boolean isBaseStone(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE || stones.isDeepBase(state);
    }

    private static boolean isPlantSpace(IBlockState state) {
        return isAir(state) || isSourceWater(state);
    }

    private static FFDLushCaveBlockProvider lushBlocks() {
        return FFDLushCaveBlockProvider.get();
    }

    private static boolean isSearchSpace(IBlockState state, boolean allowWater) {
        return isAir(state) || allowWater && isSourceWater(state);
    }

    private static boolean isEmptyOrWater(IBlockState state) {
        return isAir(state) || isSourceWater(state);
    }

    private static boolean isAir(IBlockState state) {
        return state == null || state.getMaterial() == Material.AIR;
    }

    private static boolean isSolid(IBlockState state) {
        return state != null && state.getMaterial().isSolid();
    }

    private static boolean isLava(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
    }

    private static boolean isSourceWater(IBlockState state) {
        if (state == null || state.getMaterial() != Material.WATER
                || !state.getPropertyKeys().contains(BlockLiquid.LEVEL)) {
            return false;
        }
        return state.getValue(BlockLiquid.LEVEL) == 0;
    }

    private static boolean validY(int y) {
        return y >= MIN_Y && y <= MAX_Y;
    }

    private static int sampleFeatureY(Random random) {
        return between(random, MIN_Y, FEATURE_MAX_Y);
    }

    private static boolean isUnderground(Access access, int x, int y, int z) {
        return y <= access.getOceanFloorY(x, z) - MIN_SURFACE_DEPTH;
    }

    private static boolean isPlanningUnderground(Access access, int x, int y, int z) {
        return y <= access.getPlanningOceanFloorY(x, z) - MIN_SURFACE_DEPTH;
    }

    private static boolean isLushCaveUnderground(Access access, int x, int y, int z) {
        return access.getBiome(x, y, z) == FFDVerticalBiome.LUSH_CAVES
                && isUnderground(access, x, y, z);
    }

    private static boolean intersectsTargetChunk(int x, int z, int radius,
                                                  int targetChunkX, int targetChunkZ) {
        int minX = targetChunkX << 4;
        int minZ = targetChunkZ << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        return x + radius >= minX && x - radius <= maxX
                && z + radius >= minZ && z - radius <= maxZ;
    }

    private static int lushHorizontalReach() {
        int mossReach = FFDConfig.lushCaveMossPatchMaxRadius + 1;
        int clayReach = FFDConfig.lushCaveClayPatchMaxRadius + 1;
        int rootReach = Math.max(8, Math.max(FFDConfig.lushCaveRootRadius,
                FFDConfig.lushCaveHangingRootRadius) + 3);
        return Math.max(mossReach, Math.max(clayReach, rootReach));
    }

    private static int sourceChunkRadius(int horizontalReach) {
        return (Math.max(0, horizontalReach) + 15) / 16;
    }

    private Random randomForFeature(int chunkX, int chunkZ, int featureIndex, int decorationStep) {
        long blockX = (long) chunkX << 4;
        long blockZ = (long) chunkZ << 4;
        long decorationSeed = blockX * decorationXScale + blockZ * decorationZScale ^ worldSeed;
        return new FeatureRandom(decorationSeed + featureIndex + 10000L * decorationStep);
    }

    private static final class FeatureRandom extends Random {
        private static final long serialVersionUID = 1L;

        private FFDXoroshiroRandom source;
        private double nextGaussian;
        private boolean hasNextGaussian;

        FeatureRandom(long seed) {
            super(seed);
        }

        @Override
        public synchronized void setSeed(long seed) {
            source = new FFDXoroshiroRandom(seed);
            hasNextGaussian = false;
        }

        @Override
        protected int next(int bits) {
            return (int) (source.nextLong() >>> 64 - bits);
        }

        @Override
        public int nextInt() {
            return source.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            return source.nextInt(bound);
        }

        @Override
        public long nextLong() {
            return source.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            return source.nextBoolean();
        }

        @Override
        public float nextFloat() {
            return source.nextFloat();
        }

        @Override
        public double nextDouble() {
            return source.nextDouble();
        }

        @Override
        public synchronized double nextGaussian() {
            if (hasNextGaussian) {
                hasNextGaussian = false;
                return nextGaussian;
            }
            double x;
            double y;
            double radiusSquared;
            do {
                x = 2.0D * nextDouble() - 1.0D;
                y = 2.0D * nextDouble() - 1.0D;
                radiusSquared = x * x + y * y;
            } while (radiusSquared >= 1.0D || radiusSquared == 0.0D);
            double multiplier = Math.sqrt(-2.0D * Math.log(radiusSquared) / radiusSquared);
            nextGaussian = y * multiplier;
            hasNextGaussian = true;
            return x * multiplier;
        }
    }

    private static int between(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static int triangle(Random random, int radius) {
        return radius <= 0 ? 0 : random.nextInt(radius + 1) - random.nextInt(radius + 1);
    }

    private static float randomBetween(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static double randomBetween(Random random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static float clampedGaussian(Random random, float mean, float deviation,
                                         float min, float max) {
        return (float) clamp(mean + random.nextGaussian() * deviation, min, max);
    }

    private static int clampedGaussianInt(Random random, double mean, double deviation,
                                          int min, int max) {
        return clamp((int) Math.round(mean + random.nextGaussian() * deviation), min, max);
    }

    private static double clampedMap(double value, double fromMin, double fromMax,
                                     double toMin, double toMax) {
        if (fromMin == fromMax) {
            return toMin;
        }
        double amount = clamp((value - fromMin) / (fromMax - fromMin), 0.0D, 1.0D);
        return toMin + amount * (toMax - toMin);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int dripstoneBitIndex(int x, int y, int z) {
        return ((y - MIN_Y) * 16 + (z & 15)) * 16 + (x & 15);
    }

    private static int largeDripstoneHeightAt(float distance, int radius,
                                               double scale, double bluntness) {
        return (int) speleothemHeight(distance, radius, scale, bluntness);
    }

    private static long fingerprint(long hash, long value) {
        hash ^= value;
        return hash * 0x100000001B3L;
    }

    private static long fingerprint(long hash, LargeDripstone dripstone) {
        if (dripstone == null) {
            return fingerprint(hash, 0L);
        }
        hash = fingerprint(hash, 1L);
        hash = fingerprint(hash, dripstone.rootX);
        hash = fingerprint(hash, dripstone.rootY);
        hash = fingerprint(hash, dripstone.rootZ);
        hash = fingerprint(hash, dripstone.pointingUp ? 1L : 0L);
        hash = fingerprint(hash, dripstone.radius);
        hash = fingerprint(hash, Double.doubleToLongBits(dripstone.bluntness));
        hash = fingerprint(hash, Double.doubleToLongBits(dripstone.scale));
        return fingerprint(hash, dripstone.placementSeed);
    }

    private static IBlockState oakLog() {
        return Blocks.LOG.getDefaultState()
                .withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
                .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y);
    }

    private static final class ProfiledAccess implements Access {
        private static final int COUNTERS_PER_STAGE = 8;

        private final Access delegate;
        private final long[] counts = new long[9 * COUNTERS_PER_STAGE];
        private final Set<Long> uniqueNeighborReads = new HashSet<>();
        private final Set<Long> uniqueFloorQueries = new HashSet<>();
        private int offset;

        private ProfiledAccess(Access delegate) {
            this.delegate = delegate;
        }

        private void stage(int stage) {
            flushUniqueCounts();
            offset = stage * COUNTERS_PER_STAGE;
        }

        private long[] finish() {
            flushUniqueCounts();
            return counts;
        }

        private void flushUniqueCounts() {
            counts[offset + 6] += uniqueNeighborReads.size();
            counts[offset + 7] += uniqueFloorQueries.size();
            uniqueNeighborReads.clear();
            uniqueFloorQueries.clear();
        }

        @Override
        public IBlockState getState(int x, int y, int z) {
            boolean target = delegate.isTargetChunk(x, z);
            counts[offset + (target ? 0 : 1)]++;
            if (!target && (offset == 0 || offset == 2 * COUNTERS_PER_STAGE)) {
                uniqueNeighborReads.add(profileBlockKey(x, y, z));
            }
            return delegate.getState(x, y, z);
        }

        @Override
        public IBlockState getPlanningState(int x, int y, int z) {
            return delegate.getPlanningState(x, y, z);
        }

        @Override
        public BlockFaceShape getFaceShape(int x, int y, int z, EnumFacing face) {
            counts[offset + 2]++;
            return delegate.getFaceShape(x, y, z, face);
        }

        @Override
        public boolean isTargetChunk(int x, int z) {
            return delegate.isTargetChunk(x, z);
        }

        @Override
        public FFDVerticalBiome getBiome(int x, int y, int z) {
            counts[offset + 3]++;
            return delegate.getBiome(x, y, z);
        }

        @Override
        public void setState(int x, int y, int z, IBlockState state) {
            counts[offset + 5]++;
            delegate.setState(x, y, z, state);
        }

        @Override
        public void markFluidUpdate(int x, int y, int z) {
            delegate.markFluidUpdate(x, y, z);
        }

        @Override
        public int getOceanFloorY(int x, int z) {
            counts[offset + 4]++;
            if (offset == 0 || offset == 2 * COUNTERS_PER_STAGE) {
                uniqueFloorQueries.add((x & 0xFFFFFFFFL) | (long) z << 32);
            }
            return delegate.getOceanFloorY(x, z);
        }

        @Override
        public int getPlanningOceanFloorY(int x, int z) {
            return delegate.getPlanningOceanFloorY(x, z);
        }

        private static long profileBlockKey(int x, int y, int z) {
            return ((long) x & 0x3FFFFFFL) << 38
                    | ((long) z & 0x3FFFFFFL) << 12
                    | (long) y & 0xFFFL;
        }
    }

    public interface Access {
        IBlockState getState(int x, int y, int z);

        default IBlockState getPlanningState(int x, int y, int z) {
            return getState(x, y, z);
        }

        BlockFaceShape getFaceShape(int x, int y, int z, EnumFacing face);

        boolean isTargetChunk(int x, int z);

        FFDVerticalBiome getBiome(int x, int y, int z);

        void setState(int x, int y, int z, IBlockState state);

        void markFluidUpdate(int x, int y, int z);

        int getOceanFloorY(int x, int z);

        default int getPlanningOceanFloorY(int x, int z) {
            return getOceanFloorY(x, z);
        }
    }

    private static final class Column {
        int floor;
        final int ceiling;

        Column(int floor, int ceiling) {
            this.floor = floor;
            this.ceiling = ceiling;
        }

        int height() {
            return floor == Integer.MIN_VALUE || ceiling == Integer.MIN_VALUE
                    ? Integer.MIN_VALUE : ceiling - floor - 1;
        }
    }

    private final class LargeDripstonePlan {
        private final LargeDripstone stalactite;
        private final LargeDripstone stalagmite;
        private final WindOffset wind;
        private final long fingerprint;

        private LargeDripstonePlan(LargeDripstone stalactite, LargeDripstone stalagmite,
                                   WindOffset wind) {
            this.stalactite = stalactite;
            this.stalagmite = stalagmite;
            this.wind = wind;
            long hash = 0xCBF29CE484222325L;
            hash = fingerprint(hash, stalactite);
            hash = fingerprint(hash, stalagmite);
            hash = fingerprint(hash, wind.originY);
            hash = fingerprint(hash, Double.doubleToLongBits(wind.speedX));
            hash = fingerprint(hash, Double.doubleToLongBits(wind.speedZ));
            hash = fingerprint(hash, wind.maxOffset);
            fingerprint = hash;
        }

        private LargeDripstonePlacement place(Access access) {
            LargeDripstonePlacement result = new LargeDripstonePlacement();
            if (stalactite != null) {
                stalactite.place(access, wind, result, result.stalactiteExpected);
            }
            if (stalagmite != null) {
                stalagmite.place(access, wind, result, result.stalagmiteExpected);
            }
            return result;
        }

        private String parameters() {
            return "wind:" + wind.describe() + ";down:"
                    + describe(stalactite) + ";up:" + describe(stalagmite);
        }

        private String describe(LargeDripstone dripstone) {
            return dripstone == null ? "none" : dripstone.describe();
        }
    }

    private static final class LargeDripstonePlacement {
        private final BitSet expected = new BitSet(16 * 16 * (MAX_Y - MIN_Y + 1));
        private final BitSet stalactiteExpected = TRACE_LARGE_DRIPSTONE
                ? new BitSet(16 * 16 * (MAX_Y - MIN_Y + 1)) : null;
        private final BitSet stalagmiteExpected = TRACE_LARGE_DRIPSTONE
                ? new BitSet(16 * 16 * (MAX_Y - MIN_Y + 1)) : null;
        private int writes;
    }

    private final class LargeDripstone {
        private final int rootX;
        private final int rootY;
        private final int rootZ;
        private final boolean pointingUp;
        private final int radius;
        private final double bluntness;
        private final double scale;
        private final long placementSeed;

        LargeDripstone(int rootX, int rootY, int rootZ, boolean pointingUp,
                       int radius, double bluntness, double scale, long placementSeed) {
            this.rootX = rootX;
            this.rootY = rootY;
            this.rootZ = rootZ;
            this.pointingUp = pointingUp;
            this.radius = radius;
            this.bluntness = bluntness;
            this.scale = scale;
            this.placementSeed = placementSeed;
        }

        void place(Access access, WindOffset wind, LargeDripstonePlacement result,
                   BitSet traceExpected) {
            Random random = new FeatureRandom(placementSeed);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    float currentRadius = (float) Math.sqrt(dx * dx + dz * dz);
                    int height = heightAt(currentRadius);
                    if (currentRadius > radius || height <= 0) {
                        continue;
                    }
                    if (random.nextFloat() < 0.2F) {
                        height = (int) (height * randomBetween(random, 0.8F, 1.0F));
                    }
                    int x = rootX + dx;
                    int y = rootY;
                    int z = rootZ + dz;
                    boolean leftStone = false;
                    int surfaceY = access.getPlanningOceanFloorY(x, z);
                    for (int step = 0; step < height && validY(y)
                            && (!pointingUp || y < surfaceY); step++) {
                        int[] offset = wind.offset(x, y, z);
                        IBlockState state = access.getPlanningState(offset[0], offset[1], offset[2]);
                        if (isAir(state) || isSourceWater(state) || isLava(state)) {
                            leftStone = true;
                            if (access.isTargetChunk(offset[0], offset[2])) {
                                int bitIndex = dripstoneBitIndex(
                                        offset[0], offset[1], offset[2]);
                                result.expected.set(bitIndex);
                                if (traceExpected != null) {
                                    traceExpected.set(bitIndex);
                                }
                                IBlockState actual = access.getState(offset[0], offset[1], offset[2]);
                                if (isAir(actual) || isSourceWater(actual) || isLava(actual)) {
                                    access.setState(offset[0], offset[1], offset[2],
                                            stones.dripstoneBlock());
                                    result.writes++;
                                }
                            }
                        } else if (leftStone && isBaseStone(state)) {
                            break;
                        }
                        y += pointingUp ? 1 : -1;
                    }
                }
            }
        }

        private int heightAt(float distance) {
            return largeDripstoneHeightAt(distance, radius, scale, bluntness);
        }

        private String describe() {
            return rootX + "," + rootY + "," + rootZ + ","
                    + (pointingUp ? "up" : "down") + "," + radius + ","
                    + Long.toUnsignedString(Double.doubleToLongBits(bluntness), 16) + ","
                    + Long.toUnsignedString(Double.doubleToLongBits(scale), 16) + ","
                    + Long.toUnsignedString(placementSeed, 16);
        }
    }

    private static String encodeTraceBits(BitSet bits) {
        return bits == null ? "" : Base64.getEncoder().encodeToString(bits.toByteArray());
    }

    private static final class WindOffset {
        static final WindOffset NONE = new WindOffset();

        private final int originY;
        private final double speedX;
        private final double speedZ;
        private final int maxOffset;
        private final boolean enabled;

        WindOffset(int originY, Random random, int radius) {
            this.originY = originY;
            float speed = randomBetween(random, 0.0F, 0.3F);
            float angle = randomBetween(random, 0.0F, (float) Math.PI);
            speedX = Math.cos(angle) * speed;
            speedZ = Math.sin(angle) * speed;
            maxOffset = 16 - radius;
            enabled = true;
        }

        private WindOffset() {
            originY = 0;
            speedX = 0.0D;
            speedZ = 0.0D;
            maxOffset = 0;
            enabled = false;
        }

        int[] offset(int x, int y, int z) {
            if (!enabled) {
                return new int[] {x, y, z};
            }
            int dy = originY - y;
            int dx = clamp((int) Math.floor(speedX * dy), -maxOffset, maxOffset);
            int dz = clamp((int) Math.floor(speedZ * dy), -maxOffset, maxOffset);
            return new int[] {x + dx, y, z + dz};
        }

        String describe() {
            return originY + "," + Long.toUnsignedString(Double.doubleToLongBits(speedX), 16)
                    + "," + Long.toUnsignedString(Double.doubleToLongBits(speedZ), 16)
                    + "," + maxOffset;
        }
    }

    private static boolean circleEmbedded(Access access, int centerX, int centerY,
                                          int centerZ, int radius) {
        if (isAir(access.getPlanningState(centerX, centerY, centerZ))
                || isSourceWater(access.getPlanningState(centerX, centerY, centerZ))
                || isLava(access.getPlanningState(centerX, centerY, centerZ))) {
            return false;
        }
        float increment = 6.0F / radius;
        for (float angle = 0.0F; angle < Math.PI * 2.0F; angle += increment) {
            int x = centerX + (int) (Math.cos(angle) * radius);
            int z = centerZ + (int) (Math.sin(angle) * radius);
            IBlockState state = access.getPlanningState(x, centerY, z);
            if (isAir(state) || isSourceWater(state) || isLava(state)) {
                return false;
            }
        }
        return true;
    }

    private static double speleothemHeight(double distance, double radius,
                                           double scale, double bluntness) {
        distance = Math.max(distance, bluntness);
        double ratio = distance / radius * 0.384D;
        double part1 = 0.75D * Math.pow(ratio, 4.0D / 3.0D);
        double part2 = Math.pow(ratio, 2.0D / 3.0D);
        double part3 = Math.log(ratio) / 3.0D;
        double relative = Math.max(scale * (part1 - part2 - part3), 0.0D);
        return relative / 0.384D * radius;
    }
}
