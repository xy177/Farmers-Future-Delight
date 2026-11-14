package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelStrider;
import xy177.farmersfuturedelight.common.entity.EntityStrider;

public final class RenderStrider extends RenderLiving<EntityStrider> {
    private final ModelBase adultModel = new ModelStrider(false, 0.0F);
    private final ModelBase babyModel = new ModelStrider(true, 0.0F);

    public RenderStrider(RenderManager manager) {
        super(manager, new ModelStrider(false, 0.0F), 0.5F);
        addLayer(new SaddleLayer());
    }

    @Override
    public void doRender(EntityStrider entity, double posX, double posY, double posZ, float yaw, float partialTicks) {
        mainModel = entity.isChild() ? babyModel : adultModel;
        shadowSize = entity.isChild() ? 0.25F : 0.5F;
        super.doRender(entity, posX, posY, posZ, yaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityStrider entity) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/entity/strider/strider"
                + (entity.isCold() ? "_cold" : "") + (entity.isChild() ? "_baby" : "") + ".png");
    }

    @Override
    protected void applyRotations(EntityStrider entity, float age, float yaw, float partialTicks) {
        if (entity.isCold()) yaw += (float) (Math.cos(age * 3.25D) * Math.PI * 0.4D);
        super.applyRotations(entity, age, yaw, partialTicks);
    }

    private final class SaddleLayer implements LayerRenderer<EntityStrider> {
        private final ModelBase model = new ModelStrider(false, 0.05F);

        @Override
        public void doRenderLayer(EntityStrider entity, float swing, float swingAmount, float partialTicks,
                                  float age, float headYaw, float pitch, float scale) {
            if (!entity.isSaddled() || entity.isChild() || entity.isInvisible()) return;
            bindTexture(new ResourceLocation(FarmerFutureDelight.MODID, "textures/entity/equipment/strider_saddle/saddle.png"));
            GlStateManager.color(1, 1, 1, 1);
            model.setModelAttributes(getMainModel());
            model.render(entity, swing, swingAmount, age, headYaw, pitch, scale);
        }

        @Override public boolean shouldCombineTextures() { return false; }
    }
}
