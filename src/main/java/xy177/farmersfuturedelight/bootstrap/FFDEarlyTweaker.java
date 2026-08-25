package xy177.farmersfuturedelight.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FFDEarlyTweaker implements ITweaker {
    private static final Logger LOGGER = LogManager.getLogger("FFD Height Core");
    private static final String ALFHEIM_TRANSFORMER =
            "xy177.farmersfuturedelight.core.FFDAlfheimCompatTransformer";
    private static final String[] ALFHEIM_MIXINS = {
            "dev.redstudio.alfheim.mixin.WorldMixin",
            "dev.redstudio.alfheim.mixin.ChunkMixin",
            "dev.redstudio.alfheim.mixin.ChunkCacheMixin"
    };
    private static final String LATE_TWEAKER =
            "xy177.farmersfuturedelight.bootstrap.FFDLateTweaker";

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        ensureModDiscovery();
        boolean cleanMix = isCleanMix(classLoader);
        if (!cleanMix) {
            preloadLateTweaker();
            if (FFDEarlyTweaker.class.getClassLoader() != classLoader) {
                detachSourceFromParent(classLoader);
                attachSourceToLaunchClassLoader(classLoader);
            }
        }
        Launch.blackboard.put("ffd.cleanMixActive", Boolean.valueOf(cleanMix));
        classLoader.addTransformerExclusion("xy177.farmersfuturedelight.core");
        if (!cleanMix) {
            classLoader.registerTransformer(ALFHEIM_TRANSFORMER);
        }
        List<String> tweakClasses = (List<String>) Launch.blackboard.get("TweakClasses");
        if (!tweakClasses.contains(LATE_TWEAKER)) {
            tweakClasses.add(LATE_TWEAKER);
        }
    }

    private static void ensureModDiscovery() {
        File source = findSourceJar();
        if (source == null || !source.isFile()) {
            return;
        }
        String fileName = source.getName();
        while (CoreModManager.getIgnoredMods().remove(fileName)) {
        }
        List<String> candidates = CoreModManager.getReparseableCoremods();
        if (!candidates.contains(fileName)) {
            candidates.add(fileName);
        }
    }

    private static void preloadLateTweaker() {
        try {
            Class.forName(LATE_TWEAKER, true, FFDEarlyTweaker.class.getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Unable to preload Farmer's Future Delight late tweaker",
                    exception);
        }
    }

    private static File findSourceJar() {
        try {
            URL resource = FFDEarlyTweaker.class.getResource("FFDEarlyTweaker.class");
            if (resource != null && "jar".equalsIgnoreCase(resource.getProtocol())) {
                URL jarLocation = ((JarURLConnection) resource.openConnection()).getJarFileURL();
                return new File(jarLocation.toURI());
            }
            URL location = FFDEarlyTweaker.class.getProtectionDomain().getCodeSource().getLocation();
            if (location != null && "file".equalsIgnoreCase(location.getProtocol())) {
                return new File(location.toURI());
            }
            return null;
        } catch (IOException | URISyntaxException exception) {
            throw new IllegalStateException("Unable to locate Farmer's Future Delight jar", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static void detachSourceFromParent(LaunchClassLoader classLoader) {
        ClassLoader parent = FFDEarlyTweaker.class.getClassLoader();
        File source = findSourceJar();
        if (!(parent instanceof URLClassLoader) || source == null || !source.isFile()) {
            return;
        }
        try {
            URL sourceUrl = source.toURI().toURL();
            Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
            ucpField.setAccessible(true);
            Object ucp = ucpField.get(parent);
            try {
                Method disableCaches = ucp.getClass().getDeclaredMethod("disableAllLookupCaches");
                disableCaches.setAccessible(true);
                disableCaches.invoke(null);
            } catch (NoSuchMethodException ignored) {
            }
            removeUrl((Collection<URL>) field(ucp, "path").get(ucp), sourceUrl);
            try {
                removeUrl((Collection<URL>) field(ucp, "urls").get(ucp), sourceUrl);
            } catch (NoSuchFieldException exception) {
                removeUrl((Collection<URL>) field(ucp, "unopenedUrls").get(ucp), sourceUrl);
            }
            Set<Object> removed = Collections.newSetFromMap(
                    new IdentityHashMap<Object, Boolean>());
            List<Object> loaders = (List<Object>) field(ucp, "loaders").get(ucp);
            for (Iterator<Object> iterator = loaders.iterator(); iterator.hasNext();) {
                Object loader = iterator.next();
                URL location = loaderLocation(loader);
                if (location != null && location.sameFile(sourceUrl)) {
                    removed.add(loader);
                    iterator.remove();
                }
            }
            Map<String, Object> loaderMap =
                    (Map<String, Object>) field(ucp, "lmap").get(ucp);
            for (Iterator<Map.Entry<String, Object>> iterator =
                    loaderMap.entrySet().iterator(); iterator.hasNext();) {
                if (removed.contains(iterator.next().getValue())) {
                    iterator.remove();
                }
            }
        } catch (ReflectiveOperationException | IOException exception) {
            throw new IllegalStateException("Unable to detach Farmer's Future Delight jar", exception);
        }
    }

    private static void attachSourceToLaunchClassLoader(LaunchClassLoader classLoader) {
        File source = findSourceJar();
        if (source == null || !source.isFile()) {
            return;
        }
        try {
            classLoader.addURL(source.toURI().toURL());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to attach Farmer's Future Delight jar", exception);
        }
    }

    private static Field field(Object target, String name) throws NoSuchFieldException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void removeUrl(Collection<URL> urls, URL source) {
        for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
            if (iterator.next().sameFile(source)) {
                iterator.remove();
            }
        }
    }

    private static URL loaderLocation(Object loader) throws IllegalAccessException {
        for (String name : new String[] {"csu", "base"}) {
            for (Class<?> type = loader.getClass(); type != null; type = type.getSuperclass()) {
                try {
                    Field field = type.getDeclaredField(name);
                    field.setAccessible(true);
                    return (URL) field.get(loader);
                } catch (NoSuchFieldException ignored) {
                }
            }
        }
        return null;
    }

    @Override
    public String getLaunchTarget() {
        return "";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

    private static boolean isCleanMix(LaunchClassLoader classLoader) {
        try {
            Class.forName("com.cleanroommc.cleanmix.service.CleanMixService", false,
                    classLoader);
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    static void reloadCleanMixAlfheimMixins(LaunchClassLoader classLoader) {
        try {
            Class<?> transformerClass = Class.forName(ALFHEIM_TRANSFORMER, true, classLoader);
            IClassTransformer transformer =
                    (IClassTransformer) transformerClass.getConstructor().newInstance();
            Class<?> classReaderClass = Class.forName("org.objectweb.asm.ClassReader", false,
                    classLoader);
            Class<?> classVisitorClass = Class.forName("org.objectweb.asm.ClassVisitor", false,
                    classLoader);
            Class<?> classNodeClass = Class.forName("org.objectweb.asm.tree.ClassNode", false,
                    classLoader);
            Field mixinTransformerField = Class.forName(
                    "org.spongepowered.asm.mixin.transformer.Proxy", false, classLoader)
                    .getDeclaredField("transformer");
            mixinTransformerField.setAccessible(true);
            Object mixinTransformer = mixinTransformerField.get(null);
            Method reload = mixinTransformer.getClass().getMethod("reload", String.class,
                    classNodeClass);
            Method getClassBytes = classLoader.getClass().getMethod("getClassBytes",
                    String.class);
            Field resourceCache = findField(getClassBytes.getDeclaringClass(), "resourceCache");
            Map<String, byte[]> cache = (Map<String, byte[]>) resourceCache.get(classLoader);
            for (String mixinClass : ALFHEIM_MIXINS) {
                byte[] original = (byte[]) getClassBytes.invoke(classLoader, mixinClass);
                if (original == null) {
                    continue;
                }
                byte[] patched = transformer.transform(mixinClass, mixinClass, original);
                cache.put(mixinClass, patched);
                Object classNode = classNodeClass.getConstructor().newInstance();
                Object classReader = classReaderClass.getConstructor(byte[].class)
                        .newInstance((Object) patched);
                classReaderClass.getMethod("accept", classVisitorClass, int.class)
                        .invoke(classReader, classNode, Integer.valueOf(0));
                if (!replacePendingMixin(mixinTransformer, mixinClass, classNode)) {
                    reload.invoke(mixinTransformer, mixinClass, classNode);
                }
            }
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            throw new IllegalStateException("Unable to reload Alfheim mixins for CleanMix",
                    cause == null ? exception : cause);
        } catch (ReflectiveOperationException | LinkageError exception) {
            throw new IllegalStateException("Unable to reload Alfheim mixins for CleanMix",
                    exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean replacePendingMixin(Object mixinTransformer, String mixinClass,
            Object classNode)
            throws ReflectiveOperationException {
        Object processor = findField(mixinTransformer.getClass(), "processor")
                .get(mixinTransformer);
        List<Object> configs = (List<Object>) findField(processor.getClass(), "configs")
                .get(processor);
        for (Object config : configs) {
            List<Object> mixins = (List<Object>) findField(config.getClass(), "mixins")
                    .get(config);
            for (Object mixin : mixins) {
                Method getClassName = mixin.getClass().getDeclaredMethod("getClassName");
                getClassName.setAccessible(true);
                String className = (String) getClassName.invoke(mixin);
                if (!mixinClass.equals(className)) {
                    continue;
                }
                Object pendingState = findField(mixin.getClass(), "pendingState").get(mixin);
                if (pendingState == null) {
                    return false;
                }
                findField(pendingState.getClass(), "classNode").set(pendingState, classNode);
                Method createClassNode = findMethod(pendingState.getClass(), "createClassNode",
                        int.class);
                Object validationClassNode = createClassNode.invoke(pendingState,
                        Integer.valueOf(0));
                findField(pendingState.getClass(), "validationClassNode")
                        .set(pendingState, validationClassNode);
                return true;
            }
        }
        return false;
    }

    private static Field findField(Class<?> owner, String name) throws NoSuchFieldException {
        for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Method findMethod(Class<?> owner, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
            try {
                Method method = type.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException(name);
    }

    static void refreshMixinTransformerDelegation(LaunchClassLoader classLoader) {
        try {
            Class<?> mixinService = Class.forName("org.spongepowered.asm.service.MixinService",
                    false, classLoader);
            Object service = mixinService.getMethod("getService").invoke(null);
            Class<?> serviceInterface = Class.forName("org.spongepowered.asm.service.IMixinService",
                    false, classLoader);
            Method getTransformerProvider = serviceInterface.getMethod("getTransformerProvider");
            Object provider = getTransformerProvider.invoke(service);
            Class<?> providerInterface = Class.forName(
                    "org.spongepowered.asm.service.ITransformerProvider", false, classLoader);
            providerInterface.getMethod("addTransformerExclusion", String.class)
                    .invoke(provider, "ffd.mixin.delegation.refresh");
        } catch (ClassNotFoundException exception) {
            return;
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("Unable to refresh Mixin transformer delegation", exception);
        }
    }
}
