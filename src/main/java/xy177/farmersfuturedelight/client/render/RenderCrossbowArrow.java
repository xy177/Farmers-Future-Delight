package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderTippedArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.common.entity.EntityCrossbowArrow;

public final class RenderCrossbowArrow extends RenderTippedArrow {
    private static final ResourceLocation SPECTRAL = new ResourceLocation("textures/entity/projectiles/spectral_arrow.png");

    public RenderCrossbowArrow(RenderManager manager) {
        super(manager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTippedArrow arrow) {
        return arrow instanceof EntityCrossbowArrow && ((EntityCrossbowArrow) arrow).isSpectralArrow()
                ? SPECTRAL : super.getEntityTexture(arrow);
    }
}
