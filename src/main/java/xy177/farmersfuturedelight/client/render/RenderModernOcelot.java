package xy177.farmersfuturedelight.client.render;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderOcelot;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class RenderModernOcelot extends RenderOcelot {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "farmers_future_delight", "textures/entity/cat/ocelot.png");
    private static RenderModernOcelot renderer;
    private final FFDRenderLayerBridge<EntityOcelot> externalLayers = new FFDRenderLayerBridge<>();

    private RenderModernOcelot(RenderManager manager) {
        super(manager);
        mainModel = new ModelCat();
    }

    static void renderOcelot(RenderLivingEvent.Pre<?> event) {
        if (FFDConfig.catMode == FFDConfig.FeatureMode.DISABLED
                || event.getRenderer().getClass() != RenderOcelot.class) return;
        List<LayerRenderer<?>> layers = ObfuscationReflectionHelper.getPrivateValue(
                RenderLivingBase.class, event.getRenderer(), "field_177097_h");
        for (LayerRenderer<?> layer : layers) {
            if (!layer.getClass().getName().startsWith("net.minecraft.client.renderer.entity.")
                    && !FFDRenderLayerBridge.supports(layer)) return;
        }
        if (renderer == null) renderer = new RenderModernOcelot(Minecraft.getMinecraft().getRenderManager());
        if (!renderer.externalLayers.synchronize(renderer, layers, renderer.layerRenderers)) return;
        event.setCanceled(true);
        renderer.setRenderOutlines(ObfuscationReflectionHelper.getPrivateValue(
                Render.class, event.getRenderer(), "field_188301_f"));
        try {
            renderer.doRender((EntityOcelot) event.getEntity(), event.getX(), event.getY(), event.getZ(),
                    event.getEntity().rotationYaw, event.getPartialRenderTick());
        } finally {
            renderer.setRenderOutlines(false);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityOcelot entity) {
        return entity.isTamed() ? super.getEntityTexture(entity) : TEXTURE;
    }
}
