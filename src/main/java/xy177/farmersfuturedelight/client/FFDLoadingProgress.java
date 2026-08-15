package xy177.farmersfuturedelight.client;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.MathHelper;

final class FFDLoadingProgress {
    static final int GRID_RADIUS = 15;
    static final int GRID_DIAMETER = GRID_RADIUS * 2 + 1;
    private static final int TARGET_RADIUS = 3;
    private static final int TARGET_DIAMETER = TARGET_RADIUS * 2 + 1;
    private static final int TARGET_COUNT = TARGET_DIAMETER * TARGET_DIAMETER;
    private static final int CELL_SIZE = 2;
    private static final int PIPELINE_LEAD = 9;
    private static final int[][] TARGET_ORDER = createTargetOrder();

    private FFDLoadingProgress() {
    }

    static Snapshot snapshot(Minecraft minecraft, int radius) {
        int diameter = radius * 2 + 1;
        if (minecraft.world == null || minecraft.player == null
                || !(minecraft.world.getChunkProvider() instanceof ChunkProviderClient)) {
            return new Snapshot(radius, TARGET_COUNT, 0, false, null);
        }

        ChunkProviderClient provider = (ChunkProviderClient) minecraft.world.getChunkProvider();
        int centerX = MathHelper.floor(minecraft.player.posX / 16.0D);
        int centerZ = MathHelper.floor(minecraft.player.posZ / 16.0D);
        boolean[] loaded = new boolean[diameter * diameter];
        int loadedCount = 0;
        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                int index = (z + radius) * diameter + x + radius;
                loaded[index] = provider.getLoadedChunk(centerX + x, centerZ + z) != null;
                if (loaded[index]
                        && Math.max(Math.abs(x), Math.abs(z)) <= TARGET_RADIUS) {
                    loadedCount++;
                }
            }
        }
        return new Snapshot(radius, TARGET_COUNT, loadedCount,
                provider.getLoadedChunk(centerX, centerZ) != null, loaded);
    }

    static void drawChunkGrid(int centerX, int centerY, Snapshot snapshot) {
        int diameter = snapshot.radius * 2 + 1;
        int size = diameter * CELL_SIZE;
        int left = centerX - size / 2;
        int top = centerY - size / 2;
        for (int z = 0; z < diameter; z++) {
            for (int x = 0; x < diameter; x++) {
                int color = clientCellColor(snapshot, x, z);
                Gui.drawRect(left + x * CELL_SIZE, top + z * CELL_SIZE,
                        left + (x + 1) * CELL_SIZE,
                        top + (z + 1) * CELL_SIZE, color);
            }
        }
    }

    static void drawServerProgressGrid(int centerX, int centerY, int percent) {
        Stage[] stages = serverStages(percent);
        int size = GRID_DIAMETER * CELL_SIZE;
        int left = centerX - size / 2;
        int top = centerY - size / 2;
        for (int z = 0; z < GRID_DIAMETER; z++) {
            for (int x = 0; x < GRID_DIAMETER; x++) {
                Stage stage = stages[z * GRID_DIAMETER + x];
                Gui.drawRect(left + x * CELL_SIZE, top + z * CELL_SIZE,
                        left + (x + 1) * CELL_SIZE,
                        top + (z + 1) * CELL_SIZE, stage.color);
            }
        }
    }

    private static Stage[] serverStages(int suppliedPercent) {
        int percent = MathHelper.clamp(suppliedPercent, 0, 100);
        int fullCount = MathHelper.floor(percent * TARGET_COUNT / 100.0F);
        if (percent >= 100) {
            fullCount = TARGET_COUNT;
        }
        int activeCount = Math.min(TARGET_COUNT, Math.max(1, fullCount + PIPELINE_LEAD));
        Stage[] targetStages = new Stage[TARGET_COUNT];
        Arrays.fill(targetStages, Stage.NONE);
        for (int rank = 0; rank < activeCount; rank++) {
            targetStages[rank] = targetStage(rank - fullCount);
        }

        Stage[] result = new Stage[GRID_DIAMETER * GRID_DIAMETER];
        Arrays.fill(result, Stage.NONE);
        for (int z = -GRID_RADIUS; z <= GRID_RADIUS; z++) {
            for (int x = -GRID_RADIUS; x <= GRID_RADIUS; x++) {
                Stage best = Stage.NONE;
                for (int rank = 0; rank < activeCount; rank++) {
                    int[] target = TARGET_ORDER[rank];
                    int distance = Math.max(Math.abs(x - target[0]),
                            Math.abs(z - target[1]));
                    Stage required = requiredStage(distance);
                    Stage candidate = Stage.minimum(targetStages[rank], required);
                    if (candidate.ordinal() > best.ordinal()) {
                        best = candidate;
                    }
                }
                result[(z + GRID_RADIUS) * GRID_DIAMETER + x + GRID_RADIUS] = best;
            }
        }
        return result;
    }

    private static Stage targetStage(int rankAhead) {
        if (rankAhead < 0) {
            return Stage.FULL;
        }
        switch (rankAhead) {
            case 0:
                return Stage.SPAWN;
            case 1:
                return Stage.LIGHT;
            case 2:
                return Stage.INITIALIZE_LIGHT;
            case 3:
                return Stage.FEATURES;
            case 4:
                return Stage.CARVERS;
            case 5:
                return Stage.SURFACE;
            case 6:
                return Stage.NOISE;
            case 7:
                return Stage.BIOMES;
            case 8:
                return Stage.STRUCTURE_REFERENCES;
            default:
                return Stage.STRUCTURE_STARTS;
        }
    }

    private static Stage clientCellColorStage(Snapshot snapshot, int x, int z) {
        int diameter = snapshot.radius * 2 + 1;
        int index = z * diameter + x;
        if (snapshot.loaded != null && snapshot.loaded[index]) {
            return Stage.FULL;
        }
        int distance = nearestLoadedDistance(snapshot, x, z, 11);
        return requiredStage(distance);
    }

    private static int clientCellColor(Snapshot snapshot, int x, int z) {
        return clientCellColorStage(snapshot, x, z).color;
    }

    private static int nearestLoadedDistance(Snapshot snapshot, int x, int z, int limit) {
        if (snapshot.loaded == null) {
            return limit + 1;
        }
        int diameter = snapshot.radius * 2 + 1;
        for (int distance = 1; distance <= limit; distance++) {
            int minX = Math.max(0, x - distance);
            int maxX = Math.min(diameter - 1, x + distance);
            int minZ = Math.max(0, z - distance);
            int maxZ = Math.min(diameter - 1, z + distance);
            for (int checkZ = minZ; checkZ <= maxZ; checkZ++) {
                for (int checkX = minX; checkX <= maxX; checkX++) {
                    if (Math.max(Math.abs(checkX - x), Math.abs(checkZ - z)) == distance
                            && snapshot.loaded[checkZ * diameter + checkX]) {
                        return distance;
                    }
                }
            }
        }
        return limit + 1;
    }

    private static Stage requiredStage(int distance) {
        if (distance == 0) {
            return Stage.FULL;
        }
        if (distance == 1) {
            return Stage.INITIALIZE_LIGHT;
        }
        if (distance == 2) {
            return Stage.CARVERS;
        }
        if (distance == 3) {
            return Stage.BIOMES;
        }
        if (distance <= 11) {
            return Stage.STRUCTURE_STARTS;
        }
        return Stage.NONE;
    }

    private static int[][] createTargetOrder() {
        Integer[] indices = new Integer[TARGET_COUNT];
        for (int index = 0; index < indices.length; index++) {
            indices[index] = index;
        }
        Arrays.sort(indices, (left, right) -> {
            int leftX = left % TARGET_DIAMETER - TARGET_RADIUS;
            int leftZ = left / TARGET_DIAMETER - TARGET_RADIUS;
            int rightX = right % TARGET_DIAMETER - TARGET_RADIUS;
            int rightZ = right / TARGET_DIAMETER - TARGET_RADIUS;
            int distanceCompare = Integer.compare(leftX * leftX + leftZ * leftZ,
                    rightX * rightX + rightZ * rightZ);
            if (distanceCompare != 0) {
                return distanceCompare;
            }
            return Integer.compare(cellHash(leftX, leftZ), cellHash(rightX, rightZ));
        });
        int[][] order = new int[TARGET_COUNT][2];
        for (int rank = 0; rank < indices.length; rank++) {
            int index = indices[rank];
            order[rank][0] = index % TARGET_DIAMETER - TARGET_RADIUS;
            order[rank][1] = index / TARGET_DIAMETER - TARGET_RADIUS;
        }
        return order;
    }

    private static int cellHash(int x, int z) {
        int hash = x * 73428767 ^ z * 912931;
        return hash ^ hash >>> 13;
    }

    private enum Stage {
        NONE(0xFF000000),
        EMPTY(0xFF545454),
        STRUCTURE_STARTS(0xFF999999),
        STRUCTURE_REFERENCES(0xFF5F6191),
        BIOMES(0xFF80B252),
        NOISE(0xFFD1D1D1),
        SURFACE(0xFF726809),
        CARVERS(0xFF303572),
        FEATURES(0xFF21C600),
        INITIALIZE_LIGHT(0xFFCCCCCC),
        LIGHT(0xFFFFE0A0),
        SPAWN(0xFFF26060),
        FULL(0xFFFFFFFF);

        final int color;

        Stage(int color) {
            this.color = color;
        }

        static Stage minimum(Stage first, Stage second) {
            return first.ordinal() < second.ordinal() ? first : second;
        }
    }

    static final class Snapshot {
        final int radius;
        final int total;
        final int loadedCount;
        final boolean centerLoaded;
        final boolean[] loaded;

        Snapshot(int radius, int total, int loadedCount, boolean centerLoaded,
                 boolean[] loaded) {
            this.radius = radius;
            this.total = total;
            this.loadedCount = loadedCount;
            this.centerLoaded = centerLoaded;
            this.loaded = loaded;
        }

        float progress() {
            return total == 0 ? 0.0F : loadedCount / (float) total;
        }
    }
}
