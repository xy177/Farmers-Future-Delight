package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.client.model.ModelTropicalFishA;
import xy177.farmersfuturedelight.client.model.ModelTropicalFishB;
import xy177.farmersfuturedelight.common.entity.EntityTropicalFish;

public class RenderTropicalFish extends RenderLiving<EntityTropicalFish> {
    private final ModelTropicalFishA modelA = new ModelTropicalFishA();
    private final ModelTropicalFishB modelB = new ModelTropicalFishB();

    public RenderTropicalFish(RenderManager manager) {
        super(manager, new ModelTropicalFishA(), 0.15F);
        addLayer(new TropicalFishPatternLayer(this));
    }

    @Override
    public void doRender(EntityTropicalFish entity, double x, double y, double z, float entityYaw,
                         float partialTicks) {
        mainModel = entity.getShape() == 0 ? modelA : modelB;
        float[] color = entity.getBaseColorComponents();
        GlStateManager.color(color[0], color[1], color[2], 1.0F);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTropicalFish entity) {
        return entity.getBaseTexture();
    }

    @Override
    protected void applyRotations(EntityTropicalFish entity, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        GlStateManager.rotate(4.3F * MathHelper.sin(0.6F * ageInTicks), 0.0F, 1.0F, 0.0F);
        if (!entity.isInWater()) {
            GlStateManager.translate(0.2F, 0.1F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    public void bindPatternTexture(ResourceLocation texture) {
        bindTexture(texture);
    }

    public ModelBase patternModel(EntityTropicalFish entity) {
        return entity.getShape() == 0 ? new ModelTropicalFishA(0.008F)
                : new ModelTropicalFishB(0.008F);
    }
}
