package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSquid;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelGlowSquidBaby;
import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;

public class RenderGlowSquid extends RenderSquid {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/glow_squid.png");
    private static final ResourceLocation BABY_TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/glow_squid_baby.png");
    private final ModelBase adultModel;
    private final ModelBase babyModel = new ModelGlowSquidBaby();

    public RenderGlowSquid(RenderManager renderManager) {
        super(renderManager);
        adultModel = mainModel;
    }

    @Override
    public void doRender(EntitySquid entity, double x, double y, double z, float entityYaw,
                         float partialTicks) {
        EntityGlowSquid squid = (EntityGlowSquid) entity;
        mainModel = squid.isChild() ? babyModel : adultModel;
        shadowSize = 0.7F;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected void applyRotations(EntitySquid entity, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        float pitch = entity.prevSquidPitch
                + (entity.squidPitch - entity.prevSquidPitch) * partialTicks;
        float yaw = entity.prevSquidYaw
                + (entity.squidYaw - entity.prevSquidYaw) * partialTicks;
        boolean child = entity instanceof EntityGlowSquid
                && ((EntityGlowSquid) entity).isChild();
        GlStateManager.translate(0.0F, child ? 0.25F : 0.5F, 0.0F);
        GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, child ? -0.6F : -1.2F, 0.0F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySquid entity) {
        return entity instanceof EntityGlowSquid && ((EntityGlowSquid) entity).isChild()
                ? BABY_TEXTURE : TEXTURE;
    }
}
