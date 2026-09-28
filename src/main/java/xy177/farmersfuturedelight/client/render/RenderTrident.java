package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelTrident;
import xy177.farmersfuturedelight.common.entity.EntityTrident;

public class RenderTrident extends Render<EntityTrident> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/trident.png");
    private final ModelTrident model = new ModelTrident();

    public RenderTrident(RenderManager manager) {
        super(manager);
    }

    @Override
    public boolean shouldRender(EntityTrident trident, ICamera camera, double camX,
                                double camY, double camZ) {
        AxisAlignedBB bounds = trident.getEntityBoundingBox().grow(1.5D);
        return trident.isInRangeToRender3d(camX, camY, camZ)
                && (trident.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(bounds));
    }

    @Override
    public void doRender(EntityTrident trident, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        bindEntityTexture(trident);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(trident.prevRotationYaw
                + (trident.rotationYaw - trident.prevRotationYaw) * partialTicks - 90.0F,
                0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(trident.prevRotationPitch
                + (trident.rotationPitch - trident.prevRotationPitch) * partialTicks + 90.0F,
                0.0F, 0.0F, 1.0F);
        model.render();
        GlStateManager.popMatrix();
        super.doRender(trident, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTrident entity) {
        return TEXTURE;
    }
}
