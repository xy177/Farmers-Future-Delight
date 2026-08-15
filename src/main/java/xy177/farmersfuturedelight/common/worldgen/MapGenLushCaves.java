package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.BlockAzalea;
import xy177.farmersfuturedelight.common.block.BlockBigDripleaf;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.BlockCaveVinesBase;
import xy177.farmersfuturedelight.common.block.BlockGlowLichen;
import xy177.farmersfuturedelight.common.block.BlockSmallDripleaf;
import xy177.farmersfuturedelight.common.block.DripleafTilt;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

/**
 * Preserves the original cave carver, marks broad deterministic underground
 * regions as lush, and adds a connected chamber network where 1.12 terrain is
 * too sparse. Region-derived seeds keep both the mask and tunnels continuous at
 * chunk borders.
 */
public final class MapGenLushCaves extends MapGenBase {
    private static final int REGION_SIZE_CHUNKS = 6;
    private static final long CAVERN_SALT = 0x4C55534843415645L;
    private static final long DECORATION_SALT = 0x4445434F52415445L;
    private static final long GLOW_LICHEN_SALT = 0x474C4F574C494348L;
    private final MapGenBase originalGenerator;

    public MapGenLushCaves() {
        this(null);
    }

    public MapGenLushCaves(MapGenBase originalGenerator) {
        this.originalGenerator = originalGenerator;
    }

    @Override
    public void generate(World worldIn, int chunkX, int chunkZ, ChunkPrimer primer) {
        if (originalGenerator != null) {
            originalGenerator.generate(worldIn, chunkX, chunkZ, primer);
        }
        generateLushOnly(worldIn, chunkX, chunkZ, primer);
    }

    public void generateLushOnly(World worldIn, int chunkX, int chunkZ, ChunkPrimer primer) {
        if (worldIn.provider.getDimension() != 0) {
            return;
        }

        List<Cavern> caverns = Collections.emptyList();
        if (FFDItems.isLushCaveEnabled()) {
            caverns = collectIntersectingCaverns(worldIn.getSeed(), chunkX, chunkZ);
            for (Cavern cavern : caverns) {
                carveCavern(primer, chunkX, chunkZ, cavern);
            }
        }
        if (FFDItems.isGlowLichenEnabled()) {
            decorateGlowLichen(worldIn.getSeed(), primer, chunkX, chunkZ);
        }
        if (!caverns.isEmpty()) {
            decorateCaverns(worldIn.getSeed(), primer, chunkX, chunkZ, caverns);
        }
    }

    public static boolean isPositionInLushCave(World world, BlockPos pos) {
        if (world == null || pos == null || world.provider.getDimension() != 0
                || !FFDItems.isLushCaveEnabled()) {
            return false;
        }
        if (FFDHeightHooks.isExtended(world)) {
            return FFDVerticalBiomeManager.isBiome(world, pos, FFDVerticalBiome.LUSH_CAVES);
        }
        List<Cavern> caverns = collectIntersectingCaverns(world.getSeed(),
                Math.floorDiv(pos.getX(), 16), Math.floorDiv(pos.getZ(), 16));
        return containsCavern(caverns, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D);
    }

    private static List<Cavern> collectIntersectingCaverns(long worldSeed, int chunkX, int chunkZ) {
        List<Cavern> caverns = new ArrayList<>();
        int maxReachChunks = (FFDConfig.lushCaveMaxRegionRadius
                + FFDConfig.lushCaveMaxRadius + 15) / 16 + 1;
        int regionReach = maxReachChunks / REGION_SIZE_CHUNKS + 1;
        int baseRegionX = Math.floorDiv(chunkX, REGION_SIZE_CHUNKS);
        int baseRegionZ = Math.floorDiv(chunkZ, REGION_SIZE_CHUNKS);
        for (int regionX = baseRegionX - regionReach; regionX <= baseRegionX + regionReach; regionX++) {
            for (int regionZ = baseRegionZ - regionReach; regionZ <= baseRegionZ + regionReach; regionZ++) {
                Random random = regionRandom(worldSeed, regionX, regionZ, CAVERN_SALT);
                if (random.nextInt(FFDConfig.lushCaveRegionRarity) != 0) {
                    continue;
                }
                for (int index = 0; index < FFDConfig.lushCaveCavernsPerRegion; index++) {
                    int horizontalRadius = between(random, FFDConfig.lushCaveMinRadius,
                            FFDConfig.lushCaveMaxRadius);
                    int verticalRadius = between(random, FFDConfig.lushCaveMinVerticalRadius,
                            FFDConfig.lushCaveMaxVerticalRadius);
                    int regionRadius = between(random, FFDConfig.lushCaveMinRegionRadius,
                            FFDConfig.lushCaveMaxRegionRadius);
                    int centerX = regionX * REGION_SIZE_CHUNKS * 16
                            + random.nextInt(REGION_SIZE_CHUNKS * 16);
                    int centerZ = regionZ * REGION_SIZE_CHUNKS * 16
                            + random.nextInt(REGION_SIZE_CHUNKS * 16);
                    int verticalReach = (int) Math.ceil(verticalRadius * 1.5D);
                    int minimumCenterY = FFDConfig.lushCaveMinY + verticalReach;
                    int maximumCenterY = FFDConfig.lushCaveMaxY - verticalReach;
                    int centerY = maximumCenterY >= minimumCenterY
                            ? between(random, minimumCenterY, maximumCenterY)
                            : (FFDConfig.lushCaveMinY + FFDConfig.lushCaveMaxY) / 2;
                    Cavern cavern = new Cavern(centerX, centerY, centerZ, horizontalRadius,
                            verticalRadius, regionRadius, random);
                    if (cavern.intersectsChunk(chunkX, chunkZ)) {
                        caverns.add(cavern);
                    }
                }
            }
        }
        return caverns;
    }

    private static void carveCavern(ChunkPrimer primer, int chunkX, int chunkZ, Cavern cavern) {
        if (!cavern.carvingIntersectsChunk(chunkX, chunkZ)) {
            return;
        }
        int chunkMinX = chunkX * 16;
        int chunkMinZ = chunkZ * 16;
        int startX = Math.max(chunkMinX, cavern.carvingMinX() - 1);
        int endX = Math.min(chunkMinX + 15, cavern.carvingMaxX() + 1);
        int startZ = Math.max(chunkMinZ, cavern.carvingMinZ() - 1);
        int endZ = Math.min(chunkMinZ + 15, cavern.carvingMaxZ() + 1);
        int startY = Math.max(1, cavern.carvingMinY() - 1);
        int endY = Math.min(254, cavern.carvingMaxY() + 1);
        for (int worldX = startX; worldX <= endX; worldX++) {
            int localX = worldX - chunkMinX;
            for (int worldZ = startZ; worldZ <= endZ; worldZ++) {
                int localZ = worldZ - chunkMinZ;
                for (int y = startY; y <= endY; y++) {
                    if (!cavern.shouldCarve(worldX + 0.5D, y + 0.5D, worldZ + 0.5D)) {
                        continue;
                    }
                    IBlockState state = primer.getBlockState(localX, y, localZ);
                    if (canCarve(state)) {
                        primer.setBlockState(localX, y, localZ, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    private static void decorateCaverns(long worldSeed, ChunkPrimer primer, int chunkX, int chunkZ,
                                        List<Cavern> caverns) {
        Random random = regionRandom(worldSeed, chunkX, chunkZ, DECORATION_SALT);
        FFDLushCaveBlockProvider lushBlocks = lushBlocks();
        if (lushBlocks.hasMoss()) {
            for (int attempt = 0; attempt < FFDConfig.lushCaveMossCeilingAttempts; attempt++) {
                int localX = random.nextInt(16);
                int localZ = random.nextInt(16);
                int ceilingY = findCeiling(primer, caverns, chunkX, chunkZ, localX, localZ, sampleY(random));
                if (ceilingY >= 0) {
                    placeCeilingMossPatch(primer, caverns, chunkX, chunkZ, localX, ceilingY, localZ, random);
                }
            }
        }

        if (lushBlocks.hasCaveVines()) {
            for (int attempt = 0; attempt < FFDConfig.lushCaveVineAttempts; attempt++) {
                int localX = random.nextInt(16);
                int localZ = random.nextInt(16);
                int vineY = findCeiling(primer, caverns, chunkX, chunkZ, localX, localZ, sampleY(random));
                if (vineY >= 0) {
                    placeCaveVines(primer, localX, vineY, localZ, random);
                }
            }
        }

        for (int attempt = 0; attempt < FFDConfig.lushCaveClayAttempts; attempt++) {
            int localX = random.nextInt(16);
            int localZ = random.nextInt(16);
            int floorY = findFloor(primer, caverns, chunkX, chunkZ, localX, localZ, sampleY(random));
            if (floorY >= 0) {
                placeClayPatch(primer, caverns, chunkX, chunkZ, localX, floorY, localZ, random);
            }
        }

        if (lushBlocks.hasMoss()) {
            for (int attempt = 0; attempt < FFDConfig.lushCaveMossFloorAttempts; attempt++) {
                int localX = random.nextInt(16);
                int localZ = random.nextInt(16);
                int floorY = findFloor(primer, caverns, chunkX, chunkZ, localX, localZ, sampleY(random));
                if (floorY >= 0) {
                    placeFloorMossPatch(primer, caverns, chunkX, chunkZ, localX, floorY, localZ, random);
                }
            }
        }

        if (lushBlocks.hasAzalea()) {
            int attempts = between(random, FFDConfig.lushCaveAzaleaTreeMin, FFDConfig.lushCaveAzaleaTreeMax);
            for (int attempt = 0; attempt < attempts; attempt++) {
                int localX = random.nextInt(16);
                int localZ = random.nextInt(16);
                int rootY = findCeiling(primer, caverns, chunkX, chunkZ, localX, localZ, sampleY(random));
                if (rootY >= 0) {
                    placeRootedAzaleaTree(primer, caverns, chunkX, chunkZ, localX, rootY, localZ, random);
                }
            }
        }

        if (lushBlocks.hasSporeBlossom()) {
            for (int attempt = 0; attempt < FFDConfig.lushCaveSporeBlossomAttempts; attempt++) {
                int localX = random.nextInt(16);
                int localZ = random.nextInt(16);
                int blossomY = findCeiling(primer, caverns, chunkX, chunkZ, localX, localZ, sampleY(random));
                if (blossomY >= 0 && isAir(primer.getBlockState(localX, blossomY, localZ))) {
                    primer.setBlockState(localX, blossomY, localZ, lushBlocks.sporeBlossom());
                }
            }
        }

        for (int attempt = 0; attempt < FFDConfig.lushCaveClassicVineAttempts; attempt++) {
            placeClassicVine(primer, caverns, chunkX, chunkZ,
                    random.nextInt(16), sampleY(random), random.nextInt(16));
        }
    }

    private static void decorateGlowLichen(long worldSeed, ChunkPrimer primer, int chunkX, int chunkZ) {
        Random random = regionRandom(worldSeed, chunkX, chunkZ, GLOW_LICHEN_SALT);
        int attempts = between(random, FFDConfig.lushCaveGlowLichenMinAttempts,
                FFDConfig.lushCaveGlowLichenMaxAttempts);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int y = random.nextInt(256);
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int oceanFloor = findOceanFloor(primer, x, z);
            if (y <= 0 || y >= 255 || y > oceanFloor - FFDConfig.glowLichenSurfaceOffset) {
                continue;
            }
            placeGlowLichenFeature(primer, null, chunkX, chunkZ, x, y, z, random);
        }
    }

    private static int findOceanFloor(ChunkPrimer primer, int x, int z) {
        for (int y = 255; y > 0; y--) {
            if (primer.getBlockState(x, y, z).getMaterial().isSolid()) {
                return y;
            }
        }
        return 0;
    }

    private static void placeGlowLichenFeature(ChunkPrimer primer, List<Cavern> caverns,
                                                int chunkX, int chunkZ, int originX, int originY,
                                                int originZ, Random random) {
        if (!isInsideGenerationScope(caverns, chunkX, chunkZ, originX, originY, originZ)
                || !canLichenOccupy(primer.getBlockState(originX, originY, originZ))) {
            return;
        }

        List<EnumFacing> searchDirections = lichenWorldgenDirections();
        Collections.shuffle(searchDirections, random);
        if (tryPlaceGeneratedLichen(primer, caverns, chunkX, chunkZ,
                originX, originY, originZ, searchDirections, random)) {
            return;
        }

        for (EnumFacing searchDirection : searchDirections) {
            List<EnumFacing> placementDirections = lichenWorldgenDirections();
            placementDirections.remove(searchDirection.getOpposite());
            Collections.shuffle(placementDirections, random);
            for (int distance = 1; distance <= FFDConfig.lushCaveGlowLichenSearchRange; distance++) {
                int x = originX + searchDirection.getDirectionVec().getX() * distance;
                int y = originY + searchDirection.getDirectionVec().getY() * distance;
                int z = originZ + searchDirection.getDirectionVec().getZ() * distance;
                if (!inChunk(x, z) || y <= 0 || y >= 255
                        || !isInsideGenerationScope(caverns, chunkX, chunkZ, x, y, z)) {
                    break;
                }
                IBlockState state = primer.getBlockState(x, y, z);
                if (!canLichenOccupy(state)) {
                    break;
                }
                if (tryPlaceGeneratedLichen(primer, caverns, chunkX, chunkZ,
                        x, y, z, placementDirections, random)) {
                    return;
                }
            }
        }
    }

    private static boolean tryPlaceGeneratedLichen(ChunkPrimer primer, List<Cavern> caverns,
                                                     int chunkX, int chunkZ, int x, int y, int z,
                                                     List<EnumFacing> placementDirections,
                                                     Random random) {
        for (EnumFacing face : placementDirections) {
            int supportX = x + face.getDirectionVec().getX();
            int supportY = y + face.getDirectionVec().getY();
            int supportZ = z + face.getDirectionVec().getZ();
            if (!inChunk(supportX, supportZ) || supportY <= 0 || supportY >= 255
                    || !isLichenWorldgenSupport(primer.getBlockState(supportX, supportY, supportZ))) {
                continue;
            }
            IBlockState oldState = primer.getBlockState(x, y, z);
            int mask = BlockGlowLichen.getFaceMask(oldState) | BlockGlowLichen.faceBit(face);
            boolean waterlogged = BlockGlowLichen.isGlowLichen(oldState)
                    ? BlockGlowLichen.isWaterlogged(oldState) : isSourceWater(oldState);
            primer.setBlockState(x, y, z, BlockGlowLichen.stateFor(mask, waterlogged));
            if (random.nextFloat() < FFDConfig.lushCaveGlowLichenSpreadChance) {
                spreadGeneratedLichen(primer, caverns, chunkX, chunkZ, x, y, z, face, random);
            }
            return true;
        }
        return false;
    }

    private static void spreadGeneratedLichen(ChunkPrimer primer, List<Cavern> caverns,
                                               int chunkX, int chunkZ, int x, int y, int z,
                                               EnumFacing sourceFace, Random random) {
        List<EnumFacing> directions = new ArrayList<>();
        Collections.addAll(directions, EnumFacing.values());
        Collections.shuffle(directions, random);
        for (EnumFacing direction : directions) {
            if (direction.getAxis() == sourceFace.getAxis()) {
                continue;
            }
            if (tryPlaceSpreadLichen(primer, caverns, chunkX, chunkZ,
                    x, y, z, direction)
                    || tryPlaceSpreadLichen(primer, caverns, chunkX, chunkZ,
                    x + direction.getDirectionVec().getX(), y + direction.getDirectionVec().getY(),
                    z + direction.getDirectionVec().getZ(), sourceFace)
                    || tryPlaceSpreadLichen(primer, caverns, chunkX, chunkZ,
                    x + direction.getDirectionVec().getX() + sourceFace.getDirectionVec().getX(),
                    y + direction.getDirectionVec().getY() + sourceFace.getDirectionVec().getY(),
                    z + direction.getDirectionVec().getZ() + sourceFace.getDirectionVec().getZ(),
                    direction.getOpposite())) {
                return;
            }
        }
    }

    private static boolean tryPlaceSpreadLichen(ChunkPrimer primer, List<Cavern> caverns,
                                                 int chunkX, int chunkZ, int x, int y, int z,
                                                 EnumFacing face) {
        if (!inChunk(x, z) || y <= 0 || y >= 255
                || !isInsideGenerationScope(caverns, chunkX, chunkZ, x, y, z)) {
            return false;
        }
        IBlockState oldState = primer.getBlockState(x, y, z);
        if (!canLichenOccupy(oldState) || BlockGlowLichen.hasFace(oldState, face)) {
            return false;
        }
        int supportX = x + face.getDirectionVec().getX();
        int supportY = y + face.getDirectionVec().getY();
        int supportZ = z + face.getDirectionVec().getZ();
        if (!inChunk(supportX, supportZ) || supportY <= 0 || supportY >= 255
                || !isSolidGround(primer.getBlockState(supportX, supportY, supportZ))) {
            return false;
        }
        boolean waterlogged = BlockGlowLichen.isGlowLichen(oldState)
                ? BlockGlowLichen.isWaterlogged(oldState) : isSourceWater(oldState);
        primer.setBlockState(x, y, z, BlockGlowLichen.stateFor(
                BlockGlowLichen.getFaceMask(oldState) | BlockGlowLichen.faceBit(face), waterlogged));
        return true;
    }

    private static List<EnumFacing> lichenWorldgenDirections() {
        List<EnumFacing> directions = new ArrayList<>();
        directions.add(EnumFacing.UP);
        for (EnumFacing face : EnumFacing.Plane.HORIZONTAL) {
            directions.add(face);
        }
        return directions;
    }

    private static boolean canLichenOccupy(IBlockState state) {
        return isAir(state) || isSourceWater(state) || BlockGlowLichen.isGlowLichen(state);
    }

    private static boolean isLichenWorldgenSupport(IBlockState state) {
        return state.getBlock() == Blocks.STONE;
    }

    private static void placeClassicVine(ChunkPrimer primer, List<Cavern> caverns,
                                         int chunkX, int chunkZ, int x, int y, int z) {
        if (!isInsideCavern(caverns, chunkX, chunkZ, x, y, z)
                || !isAir(primer.getBlockState(x, y, z))) {
            return;
        }
        // The 1.12 vine has no persistent ceiling-only state, so use its four wall faces.
        for (EnumFacing face : EnumFacing.Plane.HORIZONTAL) {
            int supportX = x + face.getDirectionVec().getX();
            int supportZ = z + face.getDirectionVec().getZ();
            if (inChunk(supportX, supportZ)
                    && isSolidGround(primer.getBlockState(supportX, y, supportZ))) {
                primer.setBlockState(x, y, z, Blocks.VINE.getDefaultState()
                        .withProperty(BlockVine.getPropertyFor(face), true));
                return;
            }
        }
    }

    private static boolean isInsideCavern(List<Cavern> caverns, int chunkX, int chunkZ,
                                          int localX, int y, int localZ) {
        return containsCavern(caverns, chunkX * 16 + localX + 0.5D,
                y + 0.5D, chunkZ * 16 + localZ + 0.5D);
    }

    private static boolean isInsideGenerationScope(List<Cavern> caverns, int chunkX, int chunkZ,
                                                    int localX, int y, int localZ) {
        return caverns == null || isInsideCavern(caverns, chunkX, chunkZ, localX, y, localZ);
    }

    private static int findFloor(ChunkPrimer primer, List<Cavern> caverns, int chunkX, int chunkZ,
                                 int localX, int localZ, int startY) {
        int worldX = chunkX * 16 + localX;
        int worldZ = chunkZ * 16 + localZ;
        for (int step = 0; step <= 12; step++) {
            int y = startY - step;
            if (y <= 1) {
                break;
            }
            if (!containsCavern(caverns, worldX + 0.5D, y + 0.5D, worldZ + 0.5D)
                    || !isAir(primer.getBlockState(localX, y, localZ))) {
                continue;
            }
            if (isSolidGround(primer.getBlockState(localX, y - 1, localZ))) {
                return y;
            }
        }
        return -1;
    }

    private static int findCeiling(ChunkPrimer primer, List<Cavern> caverns, int chunkX, int chunkZ,
                                   int localX, int localZ, int startY) {
        int worldX = chunkX * 16 + localX;
        int worldZ = chunkZ * 16 + localZ;
        for (int step = 0; step <= 12; step++) {
            int y = startY + step;
            if (y >= 254) {
                break;
            }
            if (!containsCavern(caverns, worldX + 0.5D, y + 0.5D, worldZ + 0.5D)
                    || !isAir(primer.getBlockState(localX, y, localZ))) {
                continue;
            }
            if (isSolidGround(primer.getBlockState(localX, y + 1, localZ))) {
                return y;
            }
        }
        return -1;
    }

    private static void placeCeilingMossPatch(ChunkPrimer primer, List<Cavern> caverns, int chunkX,
                                              int chunkZ, int localX, int airY, int localZ, Random random) {
        int radiusX = between(random, FFDConfig.lushCaveMossPatchMinRadius,
                FFDConfig.lushCaveMossPatchMaxRadius) + 1;
        int radiusZ = between(random, FFDConfig.lushCaveMossPatchMinRadius,
                FFDConfig.lushCaveMossPatchMaxRadius) + 1;
        for (int offsetX = -radiusX; offsetX <= radiusX; offsetX++) {
            for (int offsetZ = -radiusZ; offsetZ <= radiusZ; offsetZ++) {
                if (!usePatchColumn(offsetX, offsetZ, radiusX, radiusZ, random)) {
                    continue;
                }
                int x = localX + offsetX;
                int z = localZ + offsetZ;
                if (!inChunk(x, z)) {
                    continue;
                }
                int surfaceY = findCeilingNear(primer, caverns, chunkX, chunkZ,
                        x, z, airY, FFDConfig.lushCaveMossPatchVerticalRange);
                if (surfaceY < 0 || !isMossReplaceable(primer.getBlockState(x, surfaceY + 1, z))) {
                    continue;
                }
                int depth = between(random, FFDConfig.lushCaveMossCeilingMinDepth,
                        FFDConfig.lushCaveMossCeilingMaxDepth);
                boolean placedGround = false;
                for (int offset = 1; offset <= depth && surfaceY + offset < 255; offset++) {
                    int groundY = surfaceY + offset;
                    if (!isMossReplaceable(primer.getBlockState(x, groundY, z))) {
                        break;
                    }
                    primer.setBlockState(x, groundY, z, lushBlocks().mossBlock());
                    placedGround = true;
                }
                if (placedGround && lushBlocks().hasCaveVines()
                        && random.nextFloat() < FFDConfig.lushCaveMossCeilingVineChance) {
                    placeCaveVinesInMoss(primer, x, surfaceY, z, random);
                }
            }
        }
    }

    private static void placeCaveVines(ChunkPrimer primer, int localX, int startY, int localZ, Random random) {
        int distribution = random.nextInt(15);
        int bodyLength = distribution < 2 ? random.nextInt(20)
                : distribution < 5 ? random.nextInt(3) : random.nextInt(7);
        placeCaveVineColumn(primer, localX, startY, localZ, bodyLength, random);
    }

    private static void placeCaveVinesInMoss(ChunkPrimer primer, int localX, int startY,
                                             int localZ, Random random) {
        int bodyLength = random.nextInt(6) < 5 ? random.nextInt(4) : 1 + random.nextInt(7);
        placeCaveVineColumn(primer, localX, startY, localZ, bodyLength, random);
    }

    private static void placeCaveVineColumn(ChunkPrimer primer, int localX, int startY,
                                            int localZ, int bodyLength, Random random) {
        int length = bodyLength + 1;
        int placed = 0;
        for (int offset = 0; offset < length && startY - offset > 1; offset++) {
            if (!isAir(primer.getBlockState(localX, startY - offset, localZ))) {
                break;
            }
            placed++;
        }
        for (int offset = 0; offset < placed; offset++) {
            boolean tip = offset == placed - 1;
            boolean berries = random.nextInt(5) == 0;
            IBlockState state = lushBlocks().caveVines(tip, berries);
            primer.setBlockState(localX, startY - offset, localZ, state);
        }
    }

    private static void placeClayPatch(ChunkPrimer primer, List<Cavern> caverns, int chunkX, int chunkZ,
                                       int localX, int floorY, int localZ, Random random) {
        boolean waterlogged = random.nextInt(100) < FFDConfig.lushCaveWaterPoolChance;
        int radiusX = between(random, FFDConfig.lushCaveClayPatchMinRadius,
                FFDConfig.lushCaveClayPatchMaxRadius) + 1;
        int radiusZ = between(random, FFDConfig.lushCaveClayPatchMinRadius,
                FFDConfig.lushCaveClayPatchMaxRadius) + 1;
        int verticalRange = waterlogged ? FFDConfig.lushCaveWaterClayVerticalRange
                : FFDConfig.lushCaveDryClayVerticalRange;
        List<BlockPos> surface = new ArrayList<>();
        for (int offsetX = -radiusX; offsetX <= radiusX; offsetX++) {
            for (int offsetZ = -radiusZ; offsetZ <= radiusZ; offsetZ++) {
                if (!usePatchColumn(offsetX, offsetZ, radiusX, radiusZ, random,
                        FFDConfig.lushCaveClayEdgeColumnChance)) {
                    continue;
                }
                int x = localX + offsetX;
                int z = localZ + offsetZ;
                if (!inChunk(x, z)) {
                    continue;
                }
                int airY = findFloorNear(primer, caverns, chunkX, chunkZ,
                        x, z, floorY, verticalRange);
                if (airY < 0) {
                    continue;
                }
                int depth = FFDConfig.lushCaveClayPatchDepth
                        + (random.nextFloat() < FFDConfig.lushCaveClayExtraBottomChance ? 1 : 0);
                int groundY = airY - 1;
                if (placeClayGround(primer, x, groundY, z, depth)) {
                    surface.add(new BlockPos(x, groundY, z));
                }
            }
        }

        List<BlockPos> vegetationSurface = surface;
        if (waterlogged) {
            vegetationSurface = new ArrayList<>();
            for (BlockPos pos : surface) {
                if (!isClaySurfaceExposed(primer, pos)) {
                    vegetationSurface.add(pos);
                }
            }
            for (BlockPos pos : vegetationSurface) {
                primer.setBlockState(pos.getX(), pos.getY(), pos.getZ(), Blocks.WATER.getDefaultState());
            }
        }

        int dripleafChance = waterlogged ? FFDConfig.lushCaveWaterDripleafChance
                : FFDConfig.lushCaveDryDripleafChance;
        if (lushBlocks().hasDripleaf() && dripleafChance > 0) {
            for (BlockPos pos : vegetationSurface) {
                if (random.nextInt(100) < dripleafChance) {
                    placeDripleaf(primer, pos.getX(), waterlogged ? pos.getY() : pos.getY() + 1,
                            pos.getZ(), random);
                }
            }
        }
    }

    private static boolean placeClayGround(ChunkPrimer primer, int x, int y, int z, int depth) {
        boolean placed = false;
        for (int offset = 0; offset < depth && y - offset > 0; offset++) {
            int groundY = y - offset;
            IBlockState state = primer.getBlockState(x, groundY, z);
            if (state.getBlock() == Blocks.CLAY) {
                return true;
            }
            if (!isLushGroundReplaceable(state)) {
                return placed;
            }
            primer.setBlockState(x, groundY, z, Blocks.CLAY.getDefaultState());
            placed = true;
        }
        return placed;
    }

    private static boolean isClaySurfaceExposed(ChunkPrimer primer, BlockPos pos) {
        if (pos.getY() <= 0
                || !isSolidGround(primer.getBlockState(pos.getX(), pos.getY() - 1, pos.getZ()))) {
            return true;
        }
        for (EnumFacing face : EnumFacing.Plane.HORIZONTAL) {
            int x = pos.getX() + face.getDirectionVec().getX();
            int z = pos.getZ() + face.getDirectionVec().getZ();
            if (!inChunk(x, z) || !isSolidGround(primer.getBlockState(x, pos.getY(), z))) {
                return true;
            }
        }
        return false;
    }

    private static void placeFloorMossPatch(ChunkPrimer primer, List<Cavern> caverns, int chunkX,
                                            int chunkZ, int localX, int floorY, int localZ, Random random) {
        int radiusX = between(random, FFDConfig.lushCaveMossPatchMinRadius,
                FFDConfig.lushCaveMossPatchMaxRadius) + 1;
        int radiusZ = between(random, FFDConfig.lushCaveMossPatchMinRadius,
                FFDConfig.lushCaveMossPatchMaxRadius) + 1;
        for (int offsetX = -radiusX; offsetX <= radiusX; offsetX++) {
            for (int offsetZ = -radiusZ; offsetZ <= radiusZ; offsetZ++) {
                if (!usePatchColumn(offsetX, offsetZ, radiusX, radiusZ, random)) {
                    continue;
                }
                int x = localX + offsetX;
                int z = localZ + offsetZ;
                if (!inChunk(x, z)) {
                    continue;
                }
                int surfaceY = findFloorNear(primer, caverns, chunkX, chunkZ,
                        x, z, floorY, FFDConfig.lushCaveMossPatchVerticalRange);
                if (surfaceY < 0 || !isMossReplaceable(primer.getBlockState(x, surfaceY - 1, z))) {
                    continue;
                }
                primer.setBlockState(x, surfaceY - 1, z, lushBlocks().mossBlock());
                if (random.nextFloat() < FFDConfig.lushCaveMossFloorVegetationChance) {
                    placeMossVegetation(primer, x, surfaceY, z, random);
                }
            }
        }
    }

    private static boolean usePatchColumn(int offsetX, int offsetZ, int radiusX,
                                          int radiusZ, Random random) {
        return usePatchColumn(offsetX, offsetZ, radiusX, radiusZ, random,
                FFDConfig.lushCaveMossEdgeColumnChance);
    }

    private static boolean usePatchColumn(int offsetX, int offsetZ, int radiusX,
                                          int radiusZ, Random random, float edgeChance) {
        boolean xEdge = offsetX == -radiusX || offsetX == radiusX;
        boolean zEdge = offsetZ == -radiusZ || offsetZ == radiusZ;
        if (xEdge && zEdge) {
            return false;
        }
        return !xEdge && !zEdge || random.nextFloat() <= edgeChance;
    }

    private static int findFloorNear(ChunkPrimer primer, List<Cavern> caverns, int chunkX,
                                     int chunkZ, int localX, int localZ, int startY, int range) {
        int y = startY;
        for (int offset = 0; offset < range && y > 1
                && isAir(primer.getBlockState(localX, y, localZ)); offset++) {
            y--;
        }
        for (int offset = 0; offset < range && y < 254
                && !isAir(primer.getBlockState(localX, y, localZ)); offset++) {
            y++;
        }
        return y > 1 && y < 255 && isAir(primer.getBlockState(localX, y, localZ))
                && isSolidGround(primer.getBlockState(localX, y - 1, localZ))
                && isInsideCavern(caverns, chunkX, chunkZ, localX, y, localZ) ? y : -1;
    }

    private static int findCeilingNear(ChunkPrimer primer, List<Cavern> caverns, int chunkX,
                                       int chunkZ, int localX, int localZ, int startY, int range) {
        int y = startY;
        for (int offset = 0; offset < range && y < 254
                && isAir(primer.getBlockState(localX, y, localZ)); offset++) {
            y++;
        }
        for (int offset = 0; offset < range && y > 1
                && !isAir(primer.getBlockState(localX, y, localZ)); offset++) {
            y--;
        }
        return y > 0 && y < 254 && isAir(primer.getBlockState(localX, y, localZ))
                && isSolidGround(primer.getBlockState(localX, y + 1, localZ))
                && isInsideCavern(caverns, chunkX, chunkZ, localX, y, localZ) ? y : -1;
    }

    private static void placeMossVegetation(ChunkPrimer primer, int localX, int y,
                                            int localZ, Random random) {
        if (!isAir(primer.getBlockState(localX, y, localZ))) {
            return;
        }
        int choice = random.nextInt(96);
        if (choice < 4) {
            if (lushBlocks().hasAzalea()) {
                primer.setBlockState(localX, y, localZ, lushBlocks().azalea(true));
            }
        } else if (choice < 11) {
            if (lushBlocks().hasAzalea()) {
                primer.setBlockState(localX, y, localZ, lushBlocks().azalea(false));
            }
        } else if (choice < 36) {
            primer.setBlockState(localX, y, localZ, lushBlocks().mossCarpet());
        } else if (choice < 86) {
            primer.setBlockState(localX, y, localZ, Blocks.TALLGRASS.getDefaultState()
                    .withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS));
        } else if (y + 1 < 255 && isAir(primer.getBlockState(localX, y + 1, localZ))) {
            primer.setBlockState(localX, y, localZ, Blocks.DOUBLE_PLANT.getDefaultState()
                    .withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS)
                    .withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER));
            primer.setBlockState(localX, y + 1, localZ, Blocks.DOUBLE_PLANT.getDefaultState()
                    .withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS)
                    .withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
        }
    }

    private static boolean isMossReplaceable(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.MYCELIUM || lushBlocks().isMossBlock(state)
                || lushBlocks().isRootedDirt(state) || lushBlocks().isCaveVine(state);
    }

    private static boolean isLushGroundReplaceable(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.MYCELIUM || block == Blocks.CLAY
                || block == Blocks.SAND || block == Blocks.GRAVEL
                || lushBlocks().isMossBlock(state) || lushBlocks().isRootedDirt(state);
    }

    private static void placeDripleaf(ChunkPrimer primer, int localX, int baseY, int localZ, Random random) {
        if (!isDripleafGround(primer.getBlockState(localX, baseY - 1, localZ))
                || !isPlantSpace(primer.getBlockState(localX, baseY, localZ))) {
            return;
        }
        EnumFacing facing = EnumFacing.getHorizontal(random.nextInt(4));
        if (random.nextInt(5) == 0) {
            if (!isPlantSpace(primer.getBlockState(localX, baseY + 1, localZ))) {
                return;
            }
            IBlockState lower = lushBlocks().smallDripleaf(facing,
                    BlockDoublePlant.EnumBlockHalf.LOWER,
                    isSourceWater(primer.getBlockState(localX, baseY, localZ)));
            IBlockState upper = lushBlocks().smallDripleaf(facing,
                    BlockDoublePlant.EnumBlockHalf.UPPER,
                    isSourceWater(primer.getBlockState(localX, baseY + 1, localZ)));
            primer.setBlockState(localX, baseY, localZ, lower);
            primer.setBlockState(localX, baseY + 1, localZ, upper);
            return;
        }

        int stemHeight = random.nextInt(3) < 2 ? random.nextInt(5) : 0;
        if (baseY + stemHeight >= 255) {
            return;
        }
        for (int offset = 0; offset <= stemHeight; offset++) {
            if (!isPlantSpace(primer.getBlockState(localX, baseY + offset, localZ))) {
                return;
            }
        }
        for (int offset = 0; offset < stemHeight; offset++) {
            IBlockState oldState = primer.getBlockState(localX, baseY + offset, localZ);
            primer.setBlockState(localX, baseY + offset, localZ,
                    lushBlocks().bigDripleafStem(facing, isSourceWater(oldState)));
        }
        IBlockState oldHead = primer.getBlockState(localX, baseY + stemHeight, localZ);
        primer.setBlockState(localX, baseY + stemHeight, localZ,
                lushBlocks().bigDripleaf(facing, isSourceWater(oldHead)));
    }

    private static void placeRootedAzaleaTree(ChunkPrimer primer, List<Cavern> caverns, int chunkX,
                                               int chunkZ, int originX, int originY, int originZ,
                                               Random random) {
        if (!isAir(primer.getBlockState(originX, originY, originZ))) {
            return;
        }
        int maxY = Math.min(254, originY + FFDConfig.lushCaveRootColumnMaxHeight);
        for (int treeY = originY + 1; treeY <= maxY; treeY++) {
            if (!isAllowedTreePosition(primer, originX, treeY, originZ)
                    || !hasRequiredTreeSpace(primer, originX, treeY, originZ)) {
                continue;
            }
            if (!placeAzaleaTree(primer, originX, treeY, originZ, random)) {
                continue;
            }
            placeRootedDirtColumn(primer, originX, originY, originZ, treeY - 1, random);
            placeHangingRoots(primer, caverns, chunkX, chunkZ, originX, originY, originZ, random);
            return;
        }
    }

    private static boolean isAllowedTreePosition(ChunkPrimer primer, int localX, int y, int localZ) {
        return y > 0 && y < 255 && isTreeReplaceable(primer.getBlockState(localX, y, localZ))
                && isAzaleaGround(primer.getBlockState(localX, y - 1, localZ));
    }

    private static boolean hasRequiredTreeSpace(ChunkPrimer primer, int localX, int treeY, int localZ) {
        for (int offset = 1; offset <= FFDConfig.lushCaveRequiredVerticalSpaceForTree; offset++) {
            int y = treeY + offset;
            if (y >= 255) {
                return false;
            }
            IBlockState state = primer.getBlockState(localX, y, localZ);
            if (isAir(state)) {
                continue;
            }
            int blocksAboveGround = offset + 1;
            if (blocksAboveGround > FFDConfig.lushCaveAllowedVerticalWaterForTree
                    || state.getMaterial() != Material.WATER) {
                return false;
            }
        }
        return true;
    }

    private static boolean placeAzaleaTree(ChunkPrimer primer, int baseX, int baseY, int baseZ,
                                            Random random) {
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
        for (BlockPos logPos : logs) {
            if (!inChunk(logPos.getX(), logPos.getZ()) || logPos.getY() <= 0 || logPos.getY() >= 255
                    || !isTreeReplaceable(primer.getBlockState(logPos.getX(), logPos.getY(), logPos.getZ()))) {
                return false;
            }
        }
        if (lushBlocks().hasRootedDirt()) {
            primer.setBlockState(baseX, baseY - 1, baseZ, lushBlocks().rootedDirt());
        }
        for (BlockPos logPos : logs) {
            primer.setBlockState(logPos.getX(), logPos.getY(), logPos.getZ(), oakLog());
        }
        for (BlockPos foliagePoint : foliagePoints) {
            placeRandomFoliage(primer, random, foliagePoint);
        }
        return true;
    }

    private static void placeRandomFoliage(ChunkPrimer primer, Random random, BlockPos origin) {
        for (int attempt = 0; attempt < 50; attempt++) {
            int x = origin.getX() + random.nextInt(3) - random.nextInt(3);
            int y = origin.getY() + random.nextInt(2) - random.nextInt(2);
            int z = origin.getZ() + random.nextInt(3) - random.nextInt(3);
            if (!inChunk(x, z) || y <= 0 || y >= 255
                    || !isTreeReplaceable(primer.getBlockState(x, y, z))) {
                continue;
            }
            IBlockState leaves = lushBlocks().azaleaLeaves(random.nextInt(4) == 0);
            primer.setBlockState(x, y, z, leaves);
        }
    }

    private static void placeRootedDirtColumn(ChunkPrimer primer, int originX, int originY, int originZ,
                                               int targetY, Random random) {
        if (!lushBlocks().hasRootedDirt()) {
            return;
        }
        for (int y = originY; y < targetY; y++) {
            for (int attempt = 0; attempt < FFDConfig.lushCaveRootPlacementAttempts; attempt++) {
                int x = originX + random.nextInt(FFDConfig.lushCaveRootRadius)
                        - random.nextInt(FFDConfig.lushCaveRootRadius);
                int z = originZ + random.nextInt(FFDConfig.lushCaveRootRadius)
                        - random.nextInt(FFDConfig.lushCaveRootRadius);
                if (inChunk(x, z) && isRootReplaceable(primer.getBlockState(x, y, z))) {
                    primer.setBlockState(x, y, z, lushBlocks().rootedDirt());
                }
            }
        }
    }

    private static void placeHangingRoots(ChunkPrimer primer, List<Cavern> caverns, int chunkX, int chunkZ,
                                          int originX, int originY, int originZ, Random random) {
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
            if (!inChunk(x, z) || y <= 0 || y >= 254 || !isAir(primer.getBlockState(x, y, z))
                    || !isSolidGround(primer.getBlockState(x, y + 1, z))) {
                continue;
            }
            int worldX = chunkX * 16 + x;
            int worldZ = chunkZ * 16 + z;
            if (containsCavern(caverns, worldX + 0.5D, y + 0.5D, worldZ + 0.5D)) {
                primer.setBlockState(x, y, z, lushBlocks().hangingRoots());
            }
        }
    }

    private static boolean canCarve(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.GRAVEL || block == Blocks.CLAY || block == Blocks.SAND
                || block == Blocks.SANDSTONE || block == Blocks.RED_SANDSTONE;
    }

    private static boolean isSolidGround(IBlockState state) {
        return state.getMaterial().isSolid();
    }

    private static boolean isDripleafGround(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.CLAY
                || block == Blocks.MYCELIUM || block == Blocks.FARMLAND
                || lushBlocks().isMossBlock(state) || lushBlocks().isRootedDirt(state);
    }

    private static boolean isRootReplaceable(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.SAND || block == Blocks.GRAVEL || block == Blocks.CLAY
                || block == Blocks.HARDENED_CLAY || block == Blocks.STAINED_HARDENED_CLAY
                || block == Blocks.SNOW || block == Blocks.MYCELIUM
                || lushBlocks().isMossBlock(state) || lushBlocks().isRootedDirt(state);
    }

    private static boolean isTreeReplaceable(IBlockState state) {
        Material material = state.getMaterial();
        return material == Material.AIR || material == Material.PLANTS || material == Material.VINE
                || material == Material.LEAVES || material == Material.WATER
                || state.getBlock() == Blocks.SNOW_LAYER || lushBlocks().isHangingRoots(state)
                || lushBlocks().isCaveVine(state);
    }

    private static boolean isPlantSpace(IBlockState state) {
        return isAir(state) || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER;
    }

    private static boolean isAzaleaGround(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.MYCELIUM
                || block == Blocks.FARMLAND || block == Blocks.CLAY
                || lushBlocks().isMossBlock(state) || lushBlocks().isRootedDirt(state);
    }

    private static FFDLushCaveBlockProvider lushBlocks() {
        return FFDLushCaveBlockProvider.get();
    }

    private static boolean isSourceWater(IBlockState state) {
        return state.getMaterial() == Material.WATER
                && state.getPropertyKeys().contains(BlockLiquid.LEVEL)
                && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    private static boolean isAir(IBlockState state) {
        return state.getMaterial() == Material.AIR;
    }

    private static boolean containsCavern(List<Cavern> caverns, double x, double y, double z) {
        for (Cavern cavern : caverns) {
            if (cavern.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    private static boolean inChunk(int localX, int localZ) {
        return localX >= 0 && localX < 16 && localZ >= 0 && localZ < 16;
    }

    private static int sampleY(Random random) {
        return between(random, FFDConfig.lushCaveMinY, FFDConfig.lushCaveMaxY);
    }

    private static int between(Random random, int minimum, int maximum) {
        return minimum + random.nextInt(maximum - minimum + 1);
    }

    private static Random regionRandom(long worldSeed, int x, int z, long salt) {
        long seed = worldSeed ^ salt;
        seed ^= (long) x * 341873128712L;
        seed ^= (long) z * 132897987541L;
        return new Random(seed);
    }

    private static IBlockState oakLog() {
        return Blocks.LOG.getDefaultState()
                .withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
                .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y);
    }

    private static final class Cavern {
        private final Lobe[] chambers;
        private final Tunnel[] tunnels;
        private final int regionCenterX;
        private final int regionCenterZ;
        private final double regionRadiusX;
        private final double regionRadiusZ;
        private final double regionRotationSin;
        private final double regionRotationCos;
        private final double boundaryPhaseX;
        private final double boundaryPhaseZ;
        private final int minX;
        private final int maxX;
        private final int minY;
        private final int maxY;
        private final int minZ;
        private final int maxZ;
        private final int carvingMinX;
        private final int carvingMaxX;
        private final int carvingMinY;
        private final int carvingMaxY;
        private final int carvingMinZ;
        private final int carvingMaxZ;

        private Cavern(int centerX, int centerY, int centerZ, int horizontalRadius,
                       int verticalRadius, int regionRadius, Random random) {
            regionCenterX = centerX;
            regionCenterZ = centerZ;
            regionRadiusX = regionRadius * (0.88D + random.nextDouble() * 0.24D);
            regionRadiusZ = regionRadius * (0.88D + random.nextDouble() * 0.24D);
            double rotation = random.nextDouble() * Math.PI * 2.0D;
            regionRotationSin = Math.sin(rotation);
            regionRotationCos = Math.cos(rotation);
            boundaryPhaseX = random.nextDouble() * Math.PI * 2.0D;
            boundaryPhaseZ = random.nextDouble() * Math.PI * 2.0D;

            List<Lobe> generatedChambers = new ArrayList<>();
            List<Tunnel> generatedTunnels = new ArrayList<>();
            generatedChambers.add(new Lobe(centerX, centerY, centerZ,
                    Math.max(6, horizontalRadius), verticalRadius,
                    Math.max(6, (int) Math.round(horizontalRadius
                            * (0.82D + random.nextDouble() * 0.28D)))));

            int branchCount = between(random, FFDConfig.lushCaveMinBranches,
                    FFDConfig.lushCaveMaxBranches);
            double baseAngle = random.nextDouble() * Math.PI * 2.0D;
            int minCenterY = FFDConfig.lushCaveMinY + verticalRadius;
            int maxCenterY = FFDConfig.lushCaveMaxY - verticalRadius;
            for (int branch = 0; branch < branchCount; branch++) {
                double angle = baseAngle + Math.PI * 2.0D * branch / branchCount
                        + (random.nextDouble() - 0.5D) * 0.55D;
                int allowedLength = Math.max(8, (int) Math.floor(regionRadius * 0.78D));
                int maximumLength = Math.min(FFDConfig.lushCaveMaxBranchLength, allowedLength);
                int minimumLength = Math.min(FFDConfig.lushCaveMinBranchLength, maximumLength);
                int length = between(random, minimumLength, maximumLength);
                double bend = (random.nextDouble() - 0.5D) * length * 0.36D;
                double perpendicularX = -Math.sin(angle);
                double perpendicularZ = Math.cos(angle);
                double middleX = centerX + Math.cos(angle) * length * 0.48D
                        + perpendicularX * bend;
                double middleZ = centerZ + Math.sin(angle) * length * 0.48D
                        + perpendicularZ * bend;
                double endX = centerX + Math.cos(angle) * length;
                double endZ = centerZ + Math.sin(angle) * length;
                int verticalRange = Math.max(2, Math.min(10,
                        (FFDConfig.lushCaveMaxY - FFDConfig.lushCaveMinY) / 4));
                int endY = clamp(centerY + random.nextInt(verticalRange * 2 + 1) - verticalRange,
                        minCenterY, maxCenterY);
                int middleY = clamp((centerY + endY) / 2
                                + random.nextInt(5) - 2, minCenterY, maxCenterY);
                int tunnelRadius = between(random, FFDConfig.lushCaveMinTunnelRadius,
                        FFDConfig.lushCaveMaxTunnelRadius);
                generatedTunnels.add(new Tunnel(centerX, centerY, centerZ,
                        middleX, middleY, middleZ, tunnelRadius + 1, tunnelRadius));
                generatedTunnels.add(new Tunnel(middleX, middleY, middleZ,
                        endX, endY, endZ, tunnelRadius, Math.max(2, tunnelRadius - 1)));

                int endRadius = Math.max(5, (int) Math.round(horizontalRadius
                        * (0.65D + random.nextDouble() * 0.35D)));
                int endVerticalRadius = Math.max(4, (int) Math.round(verticalRadius
                        * (0.70D + random.nextDouble() * 0.25D)));
                generatedChambers.add(new Lobe((int) Math.round(endX), endY,
                        (int) Math.round(endZ), endRadius, endVerticalRadius,
                        Math.max(5, (int) Math.round(endRadius
                                * (0.80D + random.nextDouble() * 0.35D)))));
                if (branch % 2 == 0) {
                    int middleRadius = Math.max(4, endRadius - 2);
                    generatedChambers.add(new Lobe((int) Math.round(middleX), middleY,
                            (int) Math.round(middleZ), middleRadius,
                            Math.max(3, endVerticalRadius - 2), middleRadius));
                }
            }
            chambers = generatedChambers.toArray(new Lobe[0]);
            tunnels = generatedTunnels.toArray(new Tunnel[0]);

            int foundMinX = Integer.MAX_VALUE;
            int foundMaxX = Integer.MIN_VALUE;
            int foundMinY = Integer.MAX_VALUE;
            int foundMaxY = Integer.MIN_VALUE;
            int foundMinZ = Integer.MAX_VALUE;
            int foundMaxZ = Integer.MIN_VALUE;
            for (Lobe chamber : chambers) {
                foundMinX = Math.min(foundMinX, chamber.minX());
                foundMaxX = Math.max(foundMaxX, chamber.maxX());
                foundMinY = Math.min(foundMinY, chamber.minY());
                foundMaxY = Math.max(foundMaxY, chamber.maxY());
                foundMinZ = Math.min(foundMinZ, chamber.minZ());
                foundMaxZ = Math.max(foundMaxZ, chamber.maxZ());
            }
            for (Tunnel tunnel : tunnels) {
                foundMinX = Math.min(foundMinX, tunnel.minX());
                foundMaxX = Math.max(foundMaxX, tunnel.maxX());
                foundMinY = Math.min(foundMinY, tunnel.minY());
                foundMaxY = Math.max(foundMaxY, tunnel.maxY());
                foundMinZ = Math.min(foundMinZ, tunnel.minZ());
                foundMaxZ = Math.max(foundMaxZ, tunnel.maxZ());
            }
            carvingMinX = foundMinX;
            carvingMaxX = foundMaxX;
            carvingMinY = foundMinY;
            carvingMaxY = foundMaxY;
            carvingMinZ = foundMinZ;
            carvingMaxZ = foundMaxZ;

            int horizontalReach = (int) Math.ceil(Math.max(regionRadiusX, regionRadiusZ) * 1.18D);
            minX = centerX - horizontalReach;
            maxX = centerX + horizontalReach;
            minY = FFDConfig.lushCaveMinY;
            maxY = FFDConfig.lushCaveMaxY;
            minZ = centerZ - horizontalReach;
            maxZ = centerZ + horizontalReach;
        }

        private boolean intersectsChunk(int chunkX, int chunkZ) {
            int chunkMinX = chunkX * 16;
            int chunkMinZ = chunkZ * 16;
            return maxX >= chunkMinX && minX <= chunkMinX + 15
                    && maxZ >= chunkMinZ && minZ <= chunkMinZ + 15;
        }

        private boolean carvingIntersectsChunk(int chunkX, int chunkZ) {
            int chunkMinX = chunkX * 16;
            int chunkMinZ = chunkZ * 16;
            return carvingMaxX >= chunkMinX && carvingMinX <= chunkMinX + 15
                    && carvingMaxZ >= chunkMinZ && carvingMinZ <= chunkMinZ + 15;
        }

        private boolean contains(double x, double y, double z) {
            if (y < minY || y > maxY) {
                return false;
            }
            double offsetX = x - regionCenterX;
            double offsetZ = z - regionCenterZ;
            double rotatedX = offsetX * regionRotationCos + offsetZ * regionRotationSin;
            double rotatedZ = -offsetX * regionRotationSin + offsetZ * regionRotationCos;
            double normalizedX = rotatedX / regionRadiusX;
            double normalizedZ = rotatedZ / regionRadiusZ;
            double boundary = 1.0D
                    + Math.sin(x * 0.065D + boundaryPhaseX) * 0.08D
                    + Math.sin(z * 0.057D + boundaryPhaseZ) * 0.07D;
            return normalizedX * normalizedX + normalizedZ * normalizedZ
                    <= boundary * boundary;
        }

        private boolean shouldCarve(double x, double y, double z) {
            for (Lobe lobe : chambers) {
                if (lobe.contains(x, y, z)) {
                    return true;
                }
            }
            for (Tunnel tunnel : tunnels) {
                if (tunnel.contains(x, y, z)) {
                    return true;
                }
            }
            return false;
        }

        private static int clamp(int value, int minimum, int maximum) {
            if (maximum < minimum) {
                return (minimum + maximum) / 2;
            }
            return Math.max(minimum, Math.min(maximum, value));
        }

        private int minX() {
            return minX;
        }

        private int maxX() {
            return maxX;
        }

        private int minY() {
            return minY;
        }

        private int maxY() {
            return maxY;
        }

        private int minZ() {
            return minZ;
        }

        private int maxZ() {
            return maxZ;
        }

        private int carvingMinX() {
            return carvingMinX;
        }

        private int carvingMaxX() {
            return carvingMaxX;
        }

        private int carvingMinY() {
            return carvingMinY;
        }

        private int carvingMaxY() {
            return carvingMaxY;
        }

        private int carvingMinZ() {
            return carvingMinZ;
        }

        private int carvingMaxZ() {
            return carvingMaxZ;
        }

        private static final class Lobe {
            private final int centerX;
            private final int centerY;
            private final int centerZ;
            private final int radiusX;
            private final int radiusY;
            private final int radiusZ;

            private Lobe(int centerX, int centerY, int centerZ, int radiusX,
                         int radiusY, int radiusZ) {
                this.centerX = centerX;
                this.centerY = centerY;
                this.centerZ = centerZ;
                this.radiusX = radiusX;
                this.radiusY = radiusY;
                this.radiusZ = radiusZ;
            }

            private boolean contains(double x, double y, double z) {
                double normalizedX = (x - centerX) / radiusX;
                double normalizedY = (y - centerY) / radiusY;
                double normalizedZ = (z - centerZ) / radiusZ;
                return normalizedX * normalizedX + normalizedY * normalizedY
                        + normalizedZ * normalizedZ < 1.0D;
            }

            private int minX() {
                return centerX - radiusX;
            }

            private int maxX() {
                return centerX + radiusX;
            }

            private int minY() {
                return centerY - radiusY;
            }

            private int maxY() {
                return centerY + radiusY;
            }

            private int minZ() {
                return centerZ - radiusZ;
            }

            private int maxZ() {
                return centerZ + radiusZ;
            }
        }

        private static final class Tunnel {
            private final double startX;
            private final double startY;
            private final double startZ;
            private final double deltaX;
            private final double deltaY;
            private final double deltaZ;
            private final double lengthSquared;
            private final double startRadius;
            private final double endRadius;

            private Tunnel(double startX, double startY, double startZ,
                           double endX, double endY, double endZ,
                           double startRadius, double endRadius) {
                this.startX = startX;
                this.startY = startY;
                this.startZ = startZ;
                deltaX = endX - startX;
                deltaY = endY - startY;
                deltaZ = endZ - startZ;
                lengthSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                this.startRadius = startRadius;
                this.endRadius = endRadius;
            }

            private boolean contains(double x, double y, double z) {
                double relativeX = x - startX;
                double relativeY = y - startY;
                double relativeZ = z - startZ;
                double progress = lengthSquared <= 0.0D ? 0.0D
                        : (relativeX * deltaX + relativeY * deltaY + relativeZ * deltaZ)
                        / lengthSquared;
                progress = Math.max(0.0D, Math.min(1.0D, progress));
                double nearestX = startX + deltaX * progress;
                double nearestY = startY + deltaY * progress;
                double nearestZ = startZ + deltaZ * progress;
                double radius = startRadius + (endRadius - startRadius) * progress;
                double horizontalX = (x - nearestX) / radius;
                double vertical = (y - nearestY) / Math.max(2.0D, radius * 0.72D);
                double horizontalZ = (z - nearestZ) / radius;
                return horizontalX * horizontalX + vertical * vertical
                        + horizontalZ * horizontalZ < 1.0D;
            }

            private int minX() {
                return (int) Math.floor(Math.min(startX, startX + deltaX)
                        - Math.max(startRadius, endRadius));
            }

            private int maxX() {
                return (int) Math.ceil(Math.max(startX, startX + deltaX)
                        + Math.max(startRadius, endRadius));
            }

            private int minY() {
                return (int) Math.floor(Math.min(startY, startY + deltaY)
                        - Math.max(startRadius, endRadius));
            }

            private int maxY() {
                return (int) Math.ceil(Math.max(startY, startY + deltaY)
                        + Math.max(startRadius, endRadius));
            }

            private int minZ() {
                return (int) Math.floor(Math.min(startZ, startZ + deltaZ)
                        - Math.max(startRadius, endRadius));
            }

            private int maxZ() {
                return (int) Math.ceil(Math.max(startZ, startZ + deltaZ)
                        + Math.max(startRadius, endRadius));
            }
        }
    }
}
