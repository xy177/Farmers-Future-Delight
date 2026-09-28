package xy177.farmersfuturedelight.core;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FFDPulsarHooks {
    private static final Logger LOGGER = LogManager.getLogger("FFD Pulsar Compat");
    private static final Map<World, Object> CONTEXTS =
            Collections.synchronizedMap(new WeakHashMap<World, Object>());
    private static volatile boolean resolved;
    private static Method mappedContext;
    private static Method delegateContext;

    private FFDPulsarHooks() {
    }

    public static Object getHeightContext(World world) {
        ensureResolved();
        if (!FFDHeightHooks.isExtended(world)) {
            return invokeDelegate(world);
        }

        Object cached = CONTEXTS.get(world);
        if (cached != null) {
            return cached;
        }

        int minimumSection = FFDHeightHooks.minSectionY(world);
        int maximumSection = FFDHeightHooks.maxSectionYExclusive(world) - 1;
        int[] mapping = new int[maximumSection - minimumSection + 1];
        for (int sectionY = minimumSection; sectionY <= maximumSection; sectionY++) {
            int storageIndex = FFDHeightHooks.storageIndexForSectionY(sectionY, world);
            if (storageIndex < 0) {
                throw new IllegalStateException("No FFD storage index for section " + sectionY);
            }
            mapping[sectionY - minimumSection] = storageIndex;
        }

        try {
            Object context = mappedContext.invoke(null, minimumSection, maximumSection, mapping);
            CONTEXTS.put(world, context);
            LOGGER.info("Pulsar height integration active for dimension {}: sections {}..{}",
                    world.provider == null ? 0 : world.provider.getDimension(), minimumSection,
                    maximumSection);
            return context;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to construct Pulsar height context", exception);
        }
    }

    private static Object invokeDelegate(World world) {
        try {
            return delegateContext.invoke(null, world);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke Pulsar height context bridge", exception);
        }
    }

    private static void ensureResolved() {
        if (resolved) {
            return;
        }
        synchronized (FFDPulsarHooks.class) {
            if (resolved) {
                return;
            }
            try {
                ClassLoader loader = FFDPulsarHooks.class.getClassLoader();
                Class<?> contextClass = Class.forName(
                        "com.sumirelabs.pulsar.util.WorldHeightContext", false, loader);
                Class<?> bridgeClass = Class.forName(
                        "com.sumirelabs.pulsar.compat.DepthsUpdateBridge", false, loader);
                mappedContext = contextClass.getMethod("mapped", int.class, int.class,
                        int[].class);
                delegateContext = bridgeClass.getMethod("getHeightContext", World.class);
                resolved = true;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Failed to resolve Pulsar height API", exception);
            }
        }
    }
}
