package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelPufferfish;
import xy177.farmersfuturedelight.common.entity.EntityPufferfish;

public class RenderPufferfish extends RenderLiving<EntityPufferfish> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/fish/pufferfish.png");
    private final ModelBase[] models = {
            new ModelPufferfish(0), new ModelPufferfish(1), new ModelPufferfish(2)
    };

    public RenderPufferfish(RenderManager manager) {
        super(manager, new ModelPufferfish(2), 0.1F);
    }

    @Override
    public void doRender(EntityPufferfish entity, double x, double y, double z, float entityYaw,
                         float partialTicks) {
        int state = entity.getPuffState();
        mainModel = models[state];
        shadowSize = 0.1F + 0.1F * state;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPufferfish entity) {
        return TEXTURE;
    }

    @Override
    protected void applyRotations(EntityPufferfish entity, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        GlStateManager.translate(0.0F, MathHelper.cos(ageInTicks * 0.05F) * 0.08F, 0.0F);
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
    }
}
