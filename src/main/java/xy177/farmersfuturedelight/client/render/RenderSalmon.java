package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelSalmon;
import xy177.farmersfuturedelight.common.entity.EntitySalmon;

public class RenderSalmon extends RenderLiving<EntitySalmon> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/fish/salmon.png");

    public RenderSalmon(RenderManager manager) {
        super(manager, new ModelSalmon(), 0.4F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySalmon entity) {
        return TEXTURE;
    }

    @Override
    protected void applyRotations(EntitySalmon entity, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        float amplitude = entity.isInWater() ? 1.0F : 1.3F;
        float speed = entity.isInWater() ? 1.0F : 1.7F;
        GlStateManager.rotate(amplitude * 4.3F * MathHelper.sin(speed * 0.6F * ageInTicks),
                0.0F, 1.0F, 0.0F);
        if (!entity.isInWater()) {
            GlStateManager.translate(0.2F, 0.1F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    @Override
    protected void preRenderCallback(EntitySalmon entity, float partialTickTime) {
        float scale = entity.getSalmonScale();
        GlStateManager.scale(scale, scale, scale);
    }
}
