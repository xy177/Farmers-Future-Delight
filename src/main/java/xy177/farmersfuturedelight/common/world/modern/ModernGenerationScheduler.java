package xy177.farmersfuturedelight.common.world.modern;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import xy177.farmersfuturedelight.common.worldgen.FFDModernStoneProvider;

public final class ModernGenerationScheduler implements AutoCloseable {
    private final ModernGenerationSettings settings;
    private final java.util.function.BiFunction<Integer, Integer, ModernGenerationInput> inputs;
    private final long seed;
    private final boolean skylight;
    private final Thread owner = Thread.currentThread();
    private final ModernGenerationCache cache = new ModernGenerationCache();
    private final ModernNoiseContext synchronousContext;
    private final ThreadLocal<ModernNoiseContext> contexts;
    private final ThreadPoolExecutor executor;
    private final Map<Long, Task> tasks = new LinkedHashMap<>(32, 0.75F, true);
    private final Map<Long, Boolean> consumed = new LinkedHashMap<Long, Boolean>(4096, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
            return size() > 4096;
        }
    };
    private final AtomicLong completed = new AtomicLong();
    private final AtomicLong calculationNanos = new AtomicLong();
    private final AtomicLong calculationCpuNanos = new AtomicLong();
    private final AtomicLong baseNanos = new AtomicLong();
    private final ThreadMXBean threadMetrics = ManagementFactory.getThreadMXBean();
    private long requests;
    private long reused;
    private long waitNanos;
    private long inputNanos;
    private long sequence;
    private int peakPending;
    private boolean closed;
    private int previousX;
    private int previousZ;
    private boolean hasPrevious;
    private int focusX;
    private int focusZ;
    private long playerDeadline = Long.MAX_VALUE;
    private int playerPreparationDepth;

    public ModernGenerationScheduler(long seed, boolean skylight, FFDModernStoneProvider stones,
                                     ModernGenerationSettings settings) {
        this(seed, skylight, stones, settings, null);
    }

    public ModernGenerationScheduler(long seed, boolean skylight, FFDModernStoneProvider stones,
                                     ModernGenerationSettings settings,
                                     java.util.function.BiFunction<Integer, Integer, ModernGenerationInput> inputs) {
        this.seed = seed;
        this.inputs = inputs;
        this.skylight = skylight;
        this.settings = settings;
        synchronousContext = settings.workers == 0 ? new ModernNoiseContext(seed, stones, cache) : null;
        contexts = ThreadLocal.withInitial(() -> new ModernNoiseContext(seed, stones, cache));
        if (settings.workers == 0) {
            executor = null;
        } else {
            AtomicInteger threadNumber = new AtomicInteger();
            executor = new ThreadPoolExecutor(settings.workers, settings.workers, 30L, TimeUnit.SECONDS,
                    new PriorityBlockingQueue<>(), runnable -> {
                        Thread thread = new Thread(runnable, "FFD-Worldgen-" + threadNumber.incrementAndGet());
                        thread.setDaemon(true);
                        return thread;
                    }) {
                @Override
                protected void terminated() {
                    cache.clear();
                    super.terminated();
                }
            };
            executor.allowCoreThreadTimeOut(true);
        }
    }

    public ModernProtoChunk take(int chunkX, int chunkZ) {
        checkOwner();
        requests++;
        long key = key(chunkX, chunkZ);
        Task task = tasks.get(key);
        if (task != null) {
            reused++;
            if (!task.started && executor != null && executor.remove(task)) {
                task.priority = 0;
                executor.execute(task);
            }
        } else {
            makeRoom();
            task = createTask(chunkX, chunkZ, 0);
            tasks.put(key, task);
            peakPending = Math.max(peakPending, tasks.size());
            if (executor == null) task.run();
            else executor.execute(task);
        }
        long start = System.nanoTime();
        try {
            ModernProtoChunk result = task.get();
            consumed.put(key, Boolean.TRUE);
            return result;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted awaiting modern chunk " + chunkX + "," + chunkZ, exception);
        } catch (ExecutionException | CancellationException exception) {
            throw new IllegalStateException("Modern generation failed at " + chunkX + "," + chunkZ + " seed=" + seed,
                    exception instanceof ExecutionException ? exception.getCause() : exception);
        } finally {
            waitNanos += System.nanoTime() - start;
            tasks.remove(key);
        }
    }

    public void beginPlayerTick() {
        checkOwner();
        playerDeadline = System.nanoTime() + 15000000L;
    }

    public boolean canPreparePlayerChunk() {
        checkOwner();
        return System.nanoTime() < playerDeadline;
    }

    public void beginPlayerGeneration() {
        checkOwner();
        playerPreparationDepth++;
    }

    public void endPlayerGeneration() {
        checkOwner();
        playerPreparationDepth--;
    }

    public boolean requestReady(int chunkX, int chunkZ) {
        checkOwner();
        if (executor == null) return true;
        long key = key(chunkX, chunkZ);
        Task task = tasks.get(key);
        if (task == null) {
            if (tasks.size() >= settings.capacity) {
                Iterator<Task> iterator = tasks.values().iterator();
                boolean reserved = false;
                while (iterator.hasNext()) {
                    Task candidate = iterator.next();
                    if (candidate.priority != 0 && (candidate.isDone() || !candidate.started)) {
                        candidate.cancel(false);
                        executor.remove(candidate);
                        iterator.remove();
                        reserved = true;
                        break;
                    }
                }
                if (!reserved) return false;
            }
            task = createTask(chunkX, chunkZ, 0);
            tasks.put(key, task);
            peakPending = Math.max(peakPending, tasks.size());
            executor.execute(task);
        } else if (task.priority != 0) {
            if (!task.started && executor.remove(task)) {
                task.priority = 0;
                executor.execute(task);
            } else {
                task.priority = 0;
            }
        }
        return task.isDone();
    }

    public void prefetch(int chunkX, int chunkZ) {
        checkOwner();
        if (executor == null || playerPreparationDepth != 0) return;
        focusX = chunkX;
        focusZ = chunkZ;
        int directionX = 0;
        int directionZ = 1;
        if (hasPrevious && Math.abs((long) chunkX - previousX) + Math.abs((long) chunkZ - previousZ) == 1L) {
            directionX = chunkX - previousX;
            directionZ = chunkZ - previousZ;
        }
        previousX = chunkX;
        previousZ = chunkZ;
        hasPrevious = true;
        int sideX = directionZ == 0 ? 0 : 1;
        int sideZ = directionX == 0 ? 0 : 1;
        int offered = 0;
        for (int distance = 1; distance <= settings.lookahead && offered < settings.lookahead; distance++) {
            if (offer(chunkX + directionX * distance, chunkZ + directionZ * distance)) offered++;
            if (offered < settings.lookahead && distance == 1 && offer(chunkX + sideX, chunkZ + sideZ)) offered++;
            if (offered < settings.lookahead
                    && offer(chunkX + directionX * distance + sideX, chunkZ + directionZ * distance + sideZ)) offered++;
        }
    }

    private boolean offer(int chunkX, int chunkZ) {
        long key = key(chunkX, chunkZ);
        if (tasks.containsKey(key) || consumed.containsKey(key)) return false;
        if (tasks.size() >= settings.capacity) {
            long distance = distance(chunkX, chunkZ);
            Task farthest = null;
            long farthestKey = 0L;
            for (Map.Entry<Long, Task> entry : tasks.entrySet()) {
                Task candidate = entry.getValue();
                long candidateDistance = distance(candidate.chunkX, candidate.chunkZ);
                if (candidate.priority != 0 && candidateDistance > distance
                        && (candidate.isDone() || !candidate.started)) {
                    farthest = candidate;
                    farthestKey = entry.getKey();
                    distance = candidateDistance;
                }
            }
            if (farthest == null) return false;
            farthest.cancel(false);
            executor.remove(farthest);
            tasks.remove(farthestKey);
        }
        Task task = createTask(chunkX, chunkZ, 1);
        tasks.put(key, task);
        peakPending = Math.max(peakPending, tasks.size());
        executor.execute(task);
        return true;
    }

    private long distance(int chunkX, int chunkZ) {
        return Math.abs((long) chunkX - focusX) + Math.abs((long) chunkZ - focusZ);
    }

    private void makeRoom() {
        if (tasks.size() < settings.capacity) return;
        Iterator<Task> iterator = tasks.values().iterator();
        while (iterator.hasNext()) {
            Task candidate = iterator.next();
            if (candidate.isDone() || !candidate.started) {
                candidate.cancel(false);
                if (executor != null) executor.remove(candidate);
                iterator.remove();
                return;
            }
        }
        throw new IllegalStateException("Modern generation capacity exhausted by running tasks");
    }

    public Map<String, Long> statistics() {
        checkOwner();
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("requests", requests);
        result.put("reused", reused);
        result.put("completed", completed.get());
        result.put("calculationNanos", calculationNanos.get());
        result.put("calculationCpuNanos", calculationCpuNanos.get());
        result.put("baseNanos", baseNanos.get());
        result.put("inputNanos", inputNanos);
        result.put("waitNanos", waitNanos);
        result.put("pending", (long) tasks.size());
        result.put("peakPending", (long) peakPending);
        result.put("workers", (long) settings.workers);
        cache.statistics(result);
        result.putAll(ModernTerrainProfiler.snapshot());
        return result;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        for (Task task : tasks.values()) task.cancel(true);
        tasks.clear();
        consumed.clear();
        if (executor != null) executor.shutdownNow();
        else cache.clear();
        contexts.remove();
    }

    private void checkOwner() {
        if (closed) throw new IllegalStateException("Modern generation scheduler is closed");
        if (Thread.currentThread() != owner) throw new IllegalStateException("Modern generation requests require the world thread");
    }

    private static long key(int chunkX, int chunkZ) {
        return (long) chunkX << 32 ^ chunkZ & 0xffffffffL;
    }

    private Task createTask(int chunkX, int chunkZ, int priority) {
        long inputStart = System.nanoTime();
        ModernGenerationInput input = inputs == null ? null : inputs.apply(chunkX, chunkZ);
        inputNanos += System.nanoTime() - inputStart;
        return new Task(() -> {
            long start = System.nanoTime();
            long cpuStart = threadMetrics.isCurrentThreadCpuTimeSupported()
                    && threadMetrics.isThreadCpuTimeEnabled() ? threadMetrics.getCurrentThreadCpuTime() : -1L;
            ModernNoiseContext context = executor == null ? synchronousContext : contexts.get();
            ModernProtoChunk result = ModernProtoChunk.generate(context, chunkX, chunkZ, skylight);
            baseNanos.addAndGet(System.nanoTime() - start);
            if (input != null) context.finish(result, input, skylight);
            calculationNanos.addAndGet(System.nanoTime() - start);
            if (cpuStart >= 0L) calculationCpuNanos.addAndGet(threadMetrics.getCurrentThreadCpuTime() - cpuStart);
            completed.incrementAndGet();
            return result;
        }, priority, sequence++, chunkX, chunkZ);
    }

    private static final class Task extends FutureTask<ModernProtoChunk> implements Comparable<Task> {
        private final long order;
        private final int chunkX;
        private final int chunkZ;
        private int priority;
        private volatile boolean started;

        private Task(java.util.concurrent.Callable<ModernProtoChunk> action, int priority, long order,
                     int chunkX, int chunkZ) {
            super(action);
            this.priority = priority;
            this.order = order;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public void run() {
            started = true;
            super.run();
        }

        @Override
        public int compareTo(Task other) {
            int comparison = Integer.compare(priority, other.priority);
            return comparison == 0 ? Long.compare(order, other.order) : comparison;
        }
    }
}
