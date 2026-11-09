package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public final class FFDStructureBeard {
    public static final FFDStructureBeard EMPTY = new FFDStructureBeard(0, 0, 0, 0, new float[0]);
    private static final float[] KERNEL = new float[24 * 24 * 24];

    static {
        for (int localZ = 0; localZ < 24; localZ++) {
            for (int localX = 0; localX < 24; localX++) {
                for (int localY = 0; localY < 24; localY++) {
                    double deltaY = localY - 12 + 0.5D;
                    double distance = (localX - 12) * (localX - 12) + deltaY * deltaY
                            + (localZ - 12) * (localZ - 12);
                    KERNEL[localZ * 24 * 24 + localX * 24 + localY] = (float) Math.pow(Math.E, -distance / 16.0D);
                }
            }
        }
    }

    private final int startX;
    private final int startZ;
    private final int minY;
    private final int height;
    private final float[] values;

    private FFDStructureBeard(int startX, int startZ, int minY, int height, float[] values) {
        this.startX = startX;
        this.startZ = startZ;
        this.minY = minY;
        this.height = height;
        this.values = values;
    }

    public boolean isEmpty() {
        return values.length == 0;
    }

    public float sample(int blockX, int blockY, int blockZ) {
        int localX = blockX - startX;
        int localZ = blockZ - startZ;
        int localY = blockY - minY;
        if (localX < 0 || localX >= 16 || localZ < 0 || localZ >= 16 || localY < 0 || localY >= height) return 0.0F;
        return values[(localZ * 16 + localX) * height + localY];
    }

    private static float contribution(int deltaX, int deltaY, int deltaZ) {
        int kernelX = deltaX + 12;
        int kernelY = deltaY + 12;
        int kernelZ = deltaZ + 12;
        if (kernelX < 0 || kernelX >= 24 || kernelY < 0 || kernelY >= 24 || kernelZ < 0 || kernelZ >= 24) return 0.0F;
        float offsetY = deltaY + 0.5F;
        float distance = deltaX * deltaX + offsetY * offsetY + deltaZ * deltaZ;
        float value = -offsetY * (float) MathHelper.fastInvSqrt(distance / 2.0F) / 2.0F;
        return value * KERNEL[kernelZ * 24 * 24 + kernelX * 24 + kernelY];
    }

    static final class Builder {
        private final int startX;
        private final int startZ;
        private final int worldMinY;
        private final int worldMaxY;
        private final List<StructureBoundingBox> pieces = new ArrayList<>();
        private final List<Integer> grounds = new ArrayList<>();
        private final List<BlockPos> junctions = new ArrayList<>();
        private int minY = Integer.MAX_VALUE;
        private int maxY = Integer.MIN_VALUE;

        Builder(int chunkX, int chunkZ, int worldMinY, int worldMaxY) {
            this.startX = chunkX * 16;
            this.startZ = chunkZ * 16;
            this.worldMinY = worldMinY;
            this.worldMaxY = worldMaxY;
        }

        void piece(StructureBoundingBox bounds, int groundDelta) {
            if (!bounds.intersectsWith(startX - 11, startZ - 11, startX + 26, startZ + 26)) return;
            int ground = bounds.minY + groundDelta;
            pieces.add(bounds);
            grounds.add(ground);
            include(ground);
        }

        void junction(BlockPos position) {
            if (position.getX() < startX - 11 || position.getX() > startX + 26
                    || position.getZ() < startZ - 11 || position.getZ() > startZ + 26) return;
            junctions.add(position);
            include(position.getY());
        }

        private void include(int ground) {
            minY = Math.min(minY, Math.max(worldMinY, ground - 12));
            maxY = Math.max(maxY, Math.min(worldMaxY - 1, ground + 11));
        }

        FFDStructureBeard build() {
            if (minY > maxY) return EMPTY;
            int height = maxY - minY + 1;
            float[] result = new float[256 * height];
            for (int pieceIndex = 0; pieceIndex < pieces.size(); pieceIndex++) {
                StructureBoundingBox bounds = pieces.get(pieceIndex);
                int ground = grounds.get(pieceIndex);
                for (int localZ = 0; localZ < 16; localZ++) {
                    int blockZ = startZ + localZ;
                    int deltaZ = Math.max(0, Math.max(bounds.minZ - blockZ, blockZ - bounds.maxZ));
                    if (deltaZ >= 12) continue;
                    for (int localX = 0; localX < 16; localX++) {
                        int blockX = startX + localX;
                        int deltaX = Math.max(0, Math.max(bounds.minX - blockX, blockX - bounds.maxX));
                        if (deltaX >= 12) continue;
                        int offset = (localZ * 16 + localX) * height;
                        for (int blockY = Math.max(minY, ground - 12); blockY <= Math.min(maxY, ground + 11); blockY++) {
                            result[offset + blockY - minY] += contribution(deltaX, blockY - ground, deltaZ) * 0.8F;
                        }
                    }
                }
            }
            for (BlockPos junction : junctions) {
                for (int localZ = Math.max(0, junction.getZ() - 12 - startZ);
                     localZ <= Math.min(15, junction.getZ() + 11 - startZ); localZ++) {
                    for (int localX = Math.max(0, junction.getX() - 12 - startX);
                         localX <= Math.min(15, junction.getX() + 11 - startX); localX++) {
                        int offset = (localZ * 16 + localX) * height;
                        for (int blockY = Math.max(minY, junction.getY() - 12);
                             blockY <= Math.min(maxY, junction.getY() + 11); blockY++) {
                            result[offset + blockY - minY] += contribution(startX + localX - junction.getX(),
                                    blockY - junction.getY(), startZ + localZ - junction.getZ()) * 0.4F;
                        }
                    }
                }
            }
            return new FFDStructureBeard(startX, startZ, minY, height, result);
        }
    }
}
