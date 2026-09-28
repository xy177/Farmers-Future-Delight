package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSquid;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelGlowSquidBaby;

public class RenderAgeableSquid extends RenderSquid {
    private static final ResourceLocation BABY_TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/squid_baby.png");
    private final ModelBase adultModel;
    private final ModelBase babyModel = new ModelGlowSquidBaby();

    public RenderAgeableSquid(RenderManager renderManager) {
        super(renderManager);
        adultModel = mainModel;
    }

    @Override
    public void doRender(EntitySquid entity, double x, double y, double z, float entityYaw,
                         float partialTicks) {
        mainModel = entity.isChild() ? babyModel : adultModel;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySquid entity) {
        return entity.isChild() ? BABY_TEXTURE : super.getEntityTexture(entity);
    }

    @Override
    protected void applyRotations(EntitySquid entity, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        float pitch = entity.prevSquidPitch
                + (entity.squidPitch - entity.prevSquidPitch) * partialTicks;
        float yaw = entity.prevSquidYaw
                + (entity.squidYaw - entity.prevSquidYaw) * partialTicks;
        GlStateManager.translate(0.0F, entity.isChild() ? 0.25F : 0.5F, 0.0F);
        GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, entity.isChild() ? -0.6F : -1.2F, 0.0F);
    }
}
