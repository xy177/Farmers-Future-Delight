package xy177.farmersfuturedelight.bootstrap;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public final class FFDLateTweaker implements ITweaker {
    private static final String HEIGHT_TRANSFORMER =
            "xy177.farmersfuturedelight.core.FFDHeightTransformer";

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        if (Boolean.TRUE.equals(Launch.blackboard.get("ffd.cleanMixActive"))) {
            FFDEarlyTweaker.reloadCleanMixAlfheimMixins(classLoader);
        }
        classLoader.registerTransformer(HEIGHT_TRANSFORMER);
        if (!Boolean.TRUE.equals(Launch.blackboard.get("ffd.cleanMixActive"))) {
            moveAfterDeobfuscation(classLoader);
            FFDEarlyTweaker.refreshMixinTransformerDelegation(classLoader);
        }
    }

    @Override
    public String getLaunchTarget() {
        return "";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

    private static void moveAfterDeobfuscation(LaunchClassLoader classLoader) {
        List<IClassTransformer> transformers = classLoader.getTransformers();
        IClassTransformer heightTransformer = null;
        int heightIndex = -1;
        int deobfuscationIndex = -1;
        for (int index = 0; index < transformers.size(); index++) {
            IClassTransformer transformer = transformers.get(index);
            if (HEIGHT_TRANSFORMER.equals(transformer.getClass().getName())) {
                heightTransformer = transformer;
                heightIndex = index;
            }
            if ("net.minecraftforge.fml.common.asm.transformers.DeobfuscationTransformer"
                    .equals(transformer.getClass().getName())) {
                deobfuscationIndex = index;
            }
        }
        if (heightTransformer == null) {
            throw new IllegalStateException("FFD height transformer was not registered");
        }
        if (deobfuscationIndex < 0) {
            throw new IllegalStateException("FML deobfuscation transformer was not registered");
        }
        if (heightIndex == deobfuscationIndex + 1) {
            return;
        }
        moveAfterDeobfuscation(mutableTransformers(classLoader), heightTransformer);
    }

    @SuppressWarnings("unchecked")
    private static List<IClassTransformer> mutableTransformers(LaunchClassLoader classLoader) {
        List<IClassTransformer> exposed = classLoader.getTransformers();
        if (!exposed.isEmpty()) {
            try {
                exposed.set(0, exposed.get(0));
                return exposed;
            } catch (UnsupportedOperationException ignored) {
            }
        }
        try {
            Field field = LaunchClassLoader.class.getDeclaredField("transformers");
            field.setAccessible(true);
            return (List<IClassTransformer>) field.get(classLoader);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access LaunchWrapper transformers", exception);
        }
    }

    private static void moveAfterDeobfuscation(List<IClassTransformer> transformers,
            IClassTransformer heightTransformer) {
        for (IClassTransformer transformer : transformers) {
            if (transformer != heightTransformer
                    && HEIGHT_TRANSFORMER.equals(transformer.getClass().getName())) {
                throw new IllegalStateException("FFD height transformer was registered twice");
            }
        }
        transformers.remove(heightTransformer);
        for (int index = 0; index < transformers.size(); index++) {
            if ("net.minecraftforge.fml.common.asm.transformers.DeobfuscationTransformer"
                    .equals(transformers.get(index).getClass().getName())) {
                transformers.add(index + 1, heightTransformer);
                return;
            }
        }
        throw new IllegalStateException("FML deobfuscation transformer was not registered");
    }
}
