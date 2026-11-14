package xy177.farmersfuturedelight.common.world.nether;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkPrimer;

public final class NetherCarvers {
    private static final float[] SINE = sineTable();
    private final long seed;
    private final Map<Long, List<Path>> plans = new LinkedHashMap<Long, List<Path>>(512, .75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, List<Path>> eldest) { return size() > 512; }
    };

    public NetherCarvers(long seed) { this.seed = seed; }

    public void apply(ChunkPrimer primer, int chunkX, int chunkZ) {
        BitSet mask = mask(chunkX, chunkZ);
        for (int index = mask.nextSetBit(0); index >= 0; index = mask.nextSetBit(index + 1)) {
            int localX = index & 15;
            int localZ = index >> 4 & 15;
            int blockY = index >> 8;
            if (primer.getBlockState(localX, blockY, localZ).getBlock() != Blocks.BEDROCK) {
                primer.setBlockState(localX, blockY, localZ, (blockY < 32 ? Blocks.LAVA : Blocks.AIR).getDefaultState());
            }
        }
    }

    public BitSet mask(int chunkX, int chunkZ) {
        BitSet mask = new BitSet(128 * 256);
        for (int offsetX = -8; offsetX <= 8; offsetX++) {
            for (int offsetZ = -8; offsetZ <= 8; offsetZ++) {
                for (Path path : source(chunkX + offsetX, chunkZ + offsetZ)) {
                    path.apply(mask, chunkX, chunkZ);
                }
            }
        }
        return mask;
    }

    private List<Path> source(int sourceX, int sourceZ) {
        long key = (sourceX & 0xFFFFFFFFL) | (long) sourceZ << 32;
        List<Path> cached = plans.get(key);
        if (cached != null) return cached;
        List<Path> paths = new ArrayList<>();
        Random random = new Random(seed);
        long first = random.nextLong();
        long second = random.nextLong();
        random.setSeed(sourceX * first ^ sourceZ * second ^ seed);
        if (random.nextFloat() <= .2F) {
            int count = random.nextInt(random.nextInt(random.nextInt(10) + 1) + 1);
            for (int cave = 0; cave < count; cave++) {
                double blockX = sourceX * 16 + random.nextInt(16);
                double blockY = random.nextInt(127);
                double blockZ = sourceZ * 16 + random.nextInt(16);
                int tunnels = 1;
                if (random.nextInt(4) == 0) {
                    float thickness = 1.0F + random.nextFloat() * 6.0F;
                    double radius = 1.5D + sin(1.5707964F) * thickness;
                    Path room = new Path();
                    room.shapes.add(new Shape(blockX + 1, blockY, blockZ, radius, radius * .5D,
                            -1, 0, 0, true));
                    paths.add(room);
                    tunnels += random.nextInt(4);
                }
                for (int tunnel = 0; tunnel < tunnels; tunnel++) {
                    float yaw = random.nextFloat() * ((float) Math.PI * 2);
                    float pitch = (random.nextFloat() - .5F) / 4.0F;
                    float thickness = random.nextFloat() * 4.0F + random.nextFloat() * 2.0F;
                    int distance = 112 - random.nextInt(28);
                    paths.add(tunnel(random.nextLong(), blockX, blockY, blockZ, thickness, yaw,
                            pitch, 0, distance, 5.0D));
                }
            }
        }
        plans.put(key, paths);
        return paths;
    }

    private static Path tunnel(long tunnelSeed, double blockX, double blockY,
                               double blockZ, float thickness, float yaw, float pitch,
                               int step, int distance, double scaleY) {
        Random random = new Random(tunnelSeed);
        int split = random.nextInt(distance / 2) + distance / 4;
        boolean steep = random.nextInt(6) == 0;
        float deltaPitch = 0;
        float deltaYaw = 0;
        Path path = new Path();
        for (int current = step; current < distance; current++) {
            double radius = 1.5D + sin((float) Math.PI * current / distance) * thickness;
            blockX += cos(yaw) * cos(pitch);
            blockY += sin(pitch);
            blockZ += sin(yaw) * cos(pitch);
            pitch *= steep ? .92F : .7F;
            pitch += deltaPitch * .1F;
            yaw += deltaYaw * .1F;
            deltaPitch *= .9F;
            deltaYaw *= .75F;
            deltaPitch += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            deltaYaw += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (current == split && thickness > 1.0F) {
                path.children.add(tunnel(random.nextLong(), blockX, blockY, blockZ,
                        random.nextFloat() * .5F + .5F, yaw - 1.5707964F, pitch / 3.0F,
                        current, distance, 1.0D));
                path.children.add(tunnel(random.nextLong(), blockX, blockY, blockZ,
                        random.nextFloat() * .5F + .5F, yaw + 1.5707964F, pitch / 3.0F,
                        current, distance, 1.0D));
                return path;
            }
            if (random.nextInt(4) != 0) {
                path.shapes.add(new Shape(blockX, blockY, blockZ, radius, radius * scaleY,
                        current, distance, thickness, false));
            }
        }
        return path;
    }

    private static float[] sineTable() {
        float[] values = new float[65536];
        for (int index = 0; index < values.length; index++) values[index] = (float) Math.sin(index / 10430.378350470453D);
        return values;
    }

    private static float sin(double value) {
        return SINE[(int) ((long) (value * 10430.378350470453D) & 65535L)];
    }

    private static float cos(double value) {
        return SINE[(int) ((long) (value * 10430.378350470453D + 16384.0D) & 65535L)];
    }

    private static final class Path {
        private final List<Shape> shapes = new ArrayList<>();
        private final List<Path> children = new ArrayList<>(2);

        private void apply(BitSet mask, int chunkX, int chunkZ) {
            for (Shape shape : shapes) {
                if (!shape.room && !shape.canReach(chunkX, chunkZ)) return;
                shape.apply(mask, chunkX, chunkZ);
            }
            for (Path child : children) child.apply(mask, chunkX, chunkZ);
        }
    }

    private static final class Shape {
        private final double blockX;
        private final double blockY;
        private final double blockZ;
        private final double radius;
        private final double radiusY;
        private final int step;
        private final int distance;
        private final float thickness;
        private final boolean room;

        private Shape(double blockX, double blockY, double blockZ, double radius, double radiusY,
                      int step, int distance, float thickness, boolean room) {
            this.blockX = blockX;
            this.blockY = blockY;
            this.blockZ = blockZ;
            this.radius = radius;
            this.radiusY = radiusY;
            this.step = step;
            this.distance = distance;
            this.thickness = thickness;
            this.room = room;
        }

        private boolean canReach(int chunkX, int chunkZ) {
            double deltaX = blockX - (chunkX * 16 + 8);
            double deltaZ = blockZ - (chunkZ * 16 + 8);
            double remaining = distance - step;
            double limit = thickness + 2.0F + 16.0F;
            return deltaX * deltaX + deltaZ * deltaZ - remaining * remaining <= limit * limit;
        }

        private void apply(BitSet mask, int chunkX, int chunkZ) {
            double margin = 16.0D + radius * 2.0D;
            if (Math.abs(blockX - (chunkX * 16 + 8)) > margin
                    || Math.abs(blockZ - (chunkZ * 16 + 8)) > margin) return;
            int minX = Math.max(0, MathHelper.floor(blockX - radius) - chunkX * 16 - 1);
            int maxX = Math.min(15, MathHelper.floor(blockX + radius) - chunkX * 16);
            int minZ = Math.max(0, MathHelper.floor(blockZ - radius) - chunkZ * 16 - 1);
            int maxZ = Math.min(15, MathHelper.floor(blockZ + radius) - chunkZ * 16);
            int minY = Math.max(1, MathHelper.floor(blockY - radiusY) - 1);
            int maxY = Math.min(120, MathHelper.floor(blockY + radiusY) + 1);
            for (int localX = minX; localX <= maxX; localX++) {
                double normalizedX = (chunkX * 16 + localX + .5D - blockX) / radius;
                for (int localZ = minZ; localZ <= maxZ; localZ++) {
                    double normalizedZ = (chunkZ * 16 + localZ + .5D - blockZ) / radius;
                    if (normalizedX * normalizedX + normalizedZ * normalizedZ >= 1.0D) continue;
                    for (int worldY = maxY; worldY > minY; worldY--) {
                        double normalizedY = (worldY - .5D - blockY) / radiusY;
                        if (normalizedY > -.7F && normalizedX * normalizedX + normalizedY * normalizedY
                                + normalizedZ * normalizedZ < 1.0D) {
                            mask.set(worldY * 256 + localZ * 16 + localX);
                        }
                    }
                }
            }
        }
    }
}
