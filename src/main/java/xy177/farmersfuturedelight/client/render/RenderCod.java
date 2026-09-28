package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelCod;
import xy177.farmersfuturedelight.common.entity.EntityCod;

public class RenderCod extends RenderLiving<EntityCod> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/fish/cod.png");

    public RenderCod(RenderManager manager) {
        super(manager, new ModelCod(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCod entity) {
        return TEXTURE;
    }

    @Override
    protected void applyRotations(EntityCod entity, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        GlStateManager.rotate(4.3F * MathHelper.sin(0.6F * ageInTicks), 0.0F, 1.0F, 0.0F);
        if (!entity.isInWater()) {
            GlStateManager.translate(0.1F, 0.1F, -0.1F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        }
    }
}
