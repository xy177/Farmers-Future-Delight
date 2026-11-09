package xy177.farmersfuturedelight.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class FFDRenderLayerBridge<T extends EntityLivingBase> {
    private static final Logger LOGGER = LogManager.getLogger("FFD Render Layer Bridge");
    private final IdentityHashMap<LayerRenderer<?>, LayerRenderer<T>> copies = new IdentityHashMap<>();
    private final Set<Class<?>> failed = Collections.newSetFromMap(new IdentityHashMap<Class<?>, Boolean>());
    private final List<LayerRenderer<?>> sources = new ArrayList<>();
    private final List<LayerRenderer<T>> attached = new ArrayList<>();

    static boolean supports(LayerRenderer<?> layer) {
        switch (layer.getClass().getName()) {
            case "com.github.alexthe666.iceandfire.client.render.entity.layer.LayerStoneEntity":
            case "com.github.alexthe666.iceandfire.client.render.entity.layer.LayerStoneEntityCrack":
            case "com.github.alexthe666.iceandfire.client.render.entity.layer.LayerChainedEntity":
            case "sillyarmorrender.client.LayerCosmic":
            case "electroblob.wizardry.client.renderer.entity.layers.LayerStone":
            case "electroblob.wizardry.client.renderer.entity.layers.LayerFrost":
            case "electroblob.wizardry.client.renderer.entity.layers.LayerMindControl":
            case "electroblob.wizardry.client.renderer.entity.layers.LayerSummonAnimation":
            case "electroblob.wizardry.client.renderer.entity.layers.LayerDisintegrateAnimation":
            case "electroblob.wizardry.client.renderer.entity.layers.LayerOak":
            case "electroblob.wizardry.client.renderer.entity.layers.LayerIron":
            case "electroblob.wizardry.client.renderer.entity.layers.LayerDiamond":
                return true;
            default:
                return false;
        }
    }

    boolean synchronize(RenderLivingBase<T> renderer, List<LayerRenderer<?>> original,
                        List<LayerRenderer<T>> destination) {
        int count = 0;
        boolean changed = false;
        for (LayerRenderer<?> layer : original) {
            if (!supports(layer)) continue;
            if (count >= sources.size() || sources.get(count) != layer) changed = true;
            count++;
        }
        if (!changed && count == sources.size()) return true;
        List<LayerRenderer<?>> nextSources = new ArrayList<>();
        List<LayerRenderer<T>> nextLayers = new ArrayList<>();
        for (LayerRenderer<?> layer : original) {
            if (!supports(layer)) continue;
            LayerRenderer<T> copy = copies.get(layer);
            if (copy == null) {
                if (failed.contains(layer.getClass())) return false;
                try {
                    copy = bind(layer, renderer, destination);
                    copies.put(layer, copy);
                } catch (ReflectiveOperationException | LinkageError exception) {
                    failed.add(layer.getClass());
                    LOGGER.warn("Could not bind render layer {} to modern renderer; retaining original renderer",
                            layer.getClass().getName(), exception);
                    return false;
                }
            }
            nextSources.add(layer);
            nextLayers.add(copy);
        }
        destination.removeAll(attached);
        destination.addAll(nextLayers);
        sources.clear();
        sources.addAll(nextSources);
        attached.clear();
        attached.addAll(nextLayers);
        return true;
    }

    @SuppressWarnings("unchecked")
    private LayerRenderer<T> bind(LayerRenderer<?> layer, RenderLivingBase<T> renderer,
                                  List<LayerRenderer<T>> destination)
            throws ReflectiveOperationException {
        if ("sillyarmorrender.client.LayerCosmic".equals(layer.getClass().getName())) {
            LayerArmorBase<?> armor = null;
            for (LayerRenderer<T> candidate : destination) {
                if (candidate instanceof LayerArmorBase) {
                    armor = (LayerArmorBase<?>) candidate;
                    break;
                }
            }
            return (LayerRenderer<T>) layer.getClass()
                    .getConstructor(RenderLivingBase.class, LayerArmorBase.class).newInstance(renderer, armor);
        }
        Class<?> parameter = layer.getClass().getName().endsWith(".LayerChainedEntity")
                ? Render.class : RenderLivingBase.class;
        return (LayerRenderer<T>) layer.getClass().getConstructor(parameter).newInstance(renderer);
    }
}
