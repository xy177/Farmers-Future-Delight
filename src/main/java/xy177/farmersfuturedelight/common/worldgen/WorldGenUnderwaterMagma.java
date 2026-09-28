package xy177.farmersfuturedelight.common.worldgen;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.common.world.noise.FFDXoroshiroRandom;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

public final class WorldGenUnderwaterMagma {
    private static final int MIN_Y = FFDModernWorldgenData.MIN_Y;
    private static final int MAX_Y = FFDModernWorldgenData.MIN_Y
            + FFDModernWorldgenData.HEIGHT - 1;
    private static final int FLOOR_SEARCH_RANGE = 5;
    private static final int PLACEMENT_RADIUS = 1;
    private static final float PLACEMENT_PROBABILITY = 0.5F;

    private final long worldSeed;

    public WorldGenUnderwaterMagma(long worldSeed) {
        this.worldSeed = worldSeed;
    }

    public void generateChunk(int chunkX, int chunkZ, Access access) {
        for (int originChunkX = chunkX - 1; originChunkX <= chunkX + 1; originChunkX++) {
            for (int originChunkZ = chunkZ - 1; originChunkZ <= chunkZ + 1; originChunkZ++) {
                generateFromOrigin(originChunkX, originChunkZ, chunkX, chunkZ, access);
            }
        }
    }

    private void generateFromOrigin(int originChunkX, int originChunkZ,
                                    int targetChunkX, int targetChunkZ, Access access) {
        FFDXoroshiroRandom random = random(originChunkX, originChunkZ);
        int count = 44 + random.nextInt(9);
        int startX = originChunkX << 4;
        int startZ = originChunkZ << 4;
        for (int attempt = 0; attempt < count; attempt++) {
            int x = startX + random.nextInt(16);
            int z = startZ + random.nextInt(16);
            int y = MIN_Y + random.nextInt(MAX_Y - MIN_Y + 1);
            if (y > access.surfaceY(x, z) - 2) {
                continue;
            }
            placeAt(x, y, z, targetChunkX, targetChunkZ, access, random);
        }
    }

    private void placeAt(int x, int y, int z, int targetChunkX, int targetChunkZ,
                         Access access, FFDXoroshiroRandom random) {
        if (!isWater(access.getState(x, y, z))) {
            return;
        }
        int floorY = scanFloor(x, y, z, access);
        if (floorY < MIN_Y || floorY > MAX_Y) {
            return;
        }
        for (int dx = -PLACEMENT_RADIUS; dx <= PLACEMENT_RADIUS; dx++) {
            for (int dy = -PLACEMENT_RADIUS; dy <= PLACEMENT_RADIUS; dy++) {
                for (int dz = -PLACEMENT_RADIUS; dz <= PLACEMENT_RADIUS; dz++) {
                    if (random.nextFloat() >= PLACEMENT_PROBABILITY) {
                        continue;
                    }
                    int targetX = x + dx;
                    int targetY = floorY + dy;
                    int targetZ = z + dz;
                    if ((targetX >> 4) != targetChunkX || (targetZ >> 4) != targetChunkZ
                            || targetY < MIN_Y || targetY > MAX_Y
                            || !isValidPlacement(targetX, targetY, targetZ, access)) {
                        continue;
                    }
                    access.setState(targetX, targetY, targetZ, Blocks.MAGMA.getDefaultState());
                }
            }
        }
    }

    private int scanFloor(int x, int y, int z, Access access) {
        int currentY = y;
        for (int i = 1; i < FLOOR_SEARCH_RANGE
                && isWater(access.getState(x, currentY, z)); i++) {
            currentY--;
        }
        return !isWater(access.getState(x, currentY, z)) ? currentY : Integer.MIN_VALUE;
    }

    private boolean isValidPlacement(int x, int y, int z, Access access) {
        if (isWaterOrAir(access.getState(x, y, z))
                || isVisibleFromOutside(x, y - 1, z, EnumFacing.UP, access)) {
            return false;
        }
        for (EnumFacing direction : EnumFacing.HORIZONTALS) {
            if (isVisibleFromOutside(x + direction.getFrontOffsetX(),
                    y + direction.getFrontOffsetY(),
                    z + direction.getFrontOffsetZ(),
                    direction.getOpposite(), access)) {
                return false;
            }
        }
        return true;
    }

    private boolean isVisibleFromOutside(int x, int y, int z,
                                         EnumFacing coveredDirection, Access access) {
        IBlockState state = access.getState(x, y, z);
        return access.getFaceShape(x, y, z, coveredDirection) != BlockFaceShape.SOLID;
    }

    private static boolean isWaterOrAir(IBlockState state) {
        return isWater(state) || state.getBlock() == Blocks.AIR;
    }

    private static boolean isWater(IBlockState state) {
        return state != null && state.getBlock() == Blocks.WATER;
    }

    private FFDXoroshiroRandom random(int chunkX, int chunkZ) {
        long mixed = mix64(worldSeed ^ 0x4F1BBCDCBFA54001L
                ^ (long) chunkX * 341873128712L
                ^ (long) chunkZ * 132897987541L);
        return new FFDXoroshiroRandom(mixed);
    }

    private static long mix64(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    public interface Access {
        IBlockState getState(int x, int y, int z);

        void setState(int x, int y, int z, IBlockState state);

        int surfaceY(int x, int z);

        BlockFaceShape getFaceShape(int x, int y, int z, EnumFacing face);
    }
}
