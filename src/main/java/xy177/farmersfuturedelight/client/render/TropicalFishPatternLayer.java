package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import xy177.farmersfuturedelight.client.model.ModelTropicalFishA;
import xy177.farmersfuturedelight.client.model.ModelTropicalFishB;
import xy177.farmersfuturedelight.common.entity.EntityTropicalFish;

public class TropicalFishPatternLayer implements LayerRenderer<EntityTropicalFish> {
    private final RenderTropicalFish renderer;
    private final ModelTropicalFishA modelA = new ModelTropicalFishA(0.008F);
    private final ModelTropicalFishB modelB = new ModelTropicalFishB(0.008F);

    public TropicalFishPatternLayer(RenderTropicalFish renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityTropicalFish entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        if (entity.isInvisible()) {
            return;
        }
        ModelBase model = entity.getShape() == 0 ? modelA : modelB;
        renderer.bindPatternTexture(entity.getPatternTexture());
        float[] color = entity.getPatternColorComponents();
        GlStateManager.color(color[0], color[1], color[2], 1.0F);
        model.setModelAttributes(renderer.getMainModel());
        model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
