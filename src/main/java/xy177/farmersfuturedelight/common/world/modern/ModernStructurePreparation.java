package xy177.farmersfuturedelight.common.world.modern;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.StructureStart;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeProvider;

public final class ModernStructurePreparation {
    private static final Map<World, Map<MapGenStronghold, Preparation>> PREPARATIONS = new IdentityHashMap<>();

    private ModernStructurePreparation() {
    }

    public static boolean skipStronghold(MapGenStronghold generator, World world, boolean initialized,
                                         double spacing, int count, int spread, int chunkX, int chunkZ) {
        if (initialized || world == null || generator.getClass() != MapGenStronghold.class
                || !ModernGenerationLifecycle.isActive(world) || spacing > 1024.0D) return false;
        double minimumRadius = spacing * 2.75D - 12.0D;
        boolean skip = minimumRadius > 0.0D
                && (double) chunkX * chunkX + (double) chunkZ * chunkZ < minimumRadius * minimumRadius;
        if (skip && count <= 4096 && world.getBiomeProvider() instanceof FFDModernBiomeProvider) {
            Map<MapGenStronghold, Preparation> preparations = PREPARATIONS.computeIfAbsent(world,
                    ignored -> new IdentityHashMap<>());
            if (!preparations.containsKey(generator)) {
                preparations.put(generator, new Preparation(world, generator, spacing, count, spread));
            }
        }
        return skip;
    }

    public static boolean applyPositions(MapGenStronghold generator, World world, ChunkPos[] coordinates,
                                         Map<Long, StructureStart> existing, double spacing, int spread) {
        Map<MapGenStronghold, Preparation> preparations = PREPARATIONS.get(world);
        Preparation preparation = preparations == null ? null : preparations.remove(generator);
        if (preparation == null) return false;
        if (!preparation.positions.isDone() || preparation.positions.isCancelled()
                || spacing != preparation.spacing || spread != preparation.spread
                || coordinates.length != preparation.count || !generator.allowedBiomes.equals(preparation.allowed)) {
            preparation.positions.cancel(true);
            return false;
        }
        try {
            ChunkPos[] prepared = preparation.positions.get();
            int index = 0;
            for (StructureStart start : existing.values()) {
                if (index < coordinates.length) coordinates[index++] = new ChunkPos(start.getChunkPosX(), start.getChunkPosZ());
            }
            if (existing.size() < coordinates.length) {
                System.arraycopy(prepared, existing.size(), coordinates, existing.size(), coordinates.length - existing.size());
            }
            return true;
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted stronghold preparation", failure);
        } catch (ExecutionException failure) {
            throw new IllegalStateException("Stronghold preparation failed", failure.getCause());
        }
    }

    public static void close(World world) {
        Map<MapGenStronghold, Preparation> preparations = PREPARATIONS.remove(world);
        if (preparations != null) {
            for (Preparation preparation : preparations.values()) preparation.positions.cancel(true);
        }
    }

    private static final class Preparation {
        private final double spacing;
        private final int count;
        private final int spread;
        private final List<Biome> allowed;
        private final FutureTask<ChunkPos[]> positions;

        private Preparation(World world, MapGenStronghold generator, double spacing, int count, int spread) {
            this.spacing = spacing;
            this.count = count;
            this.spread = spread;
            allowed = new ArrayList<>(generator.allowedBiomes);
            FFDModernBiomeProvider provider = FFDModernBiomeProvider.snapshot(world);
            long seed = world.getSeed();
            positions = new FutureTask<>(() -> calculate(provider, seed));
            Thread worker = new Thread(positions, "FFD-Stronghold-Preparation");
            worker.setDaemon(true);
            worker.start();
        }

        private ChunkPos[] calculate(FFDModernBiomeProvider provider, long seed) {
            ChunkPos[] coordinates = new ChunkPos[count];
            Random random = new Random(seed);
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int ring = 0;
            int inRing = 0;
            int ringSpread = spread;
            for (int index = 0; index < count; index++) {
                if (Thread.currentThread().isInterrupted()) throw new CancellationException();
                double radius = 4.0D * spacing + spacing * (double) ring * 6.0D
                        + (random.nextDouble() - 0.5D) * spacing * 2.5D;
                int chunkX = (int) Math.round(Math.cos(angle) * radius);
                int chunkZ = (int) Math.round(Math.sin(angle) * radius);
                BlockPos position = provider.findBiomePosition((chunkX << 4) + 8, (chunkZ << 4) + 8, 112, allowed, random);
                if (position != null) {
                    chunkX = position.getX() >> 4;
                    chunkZ = position.getZ() >> 4;
                }
                coordinates[index] = new ChunkPos(chunkX, chunkZ);
                angle += (Math.PI * 2D) / (double) ringSpread;
                if (++inRing == ringSpread) {
                    ring++;
                    inRing = 0;
                    ringSpread += 2 * ringSpread / (ring + 1);
                    ringSpread = Math.min(ringSpread, count - index);
                    angle += random.nextDouble() * Math.PI * 2.0D;
                }
            }
            return coordinates;
        }
    }
}
