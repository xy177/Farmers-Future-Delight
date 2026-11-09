package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.entity.EntityPanda;

@SideOnly(Side.CLIENT)
public class RenderPanda extends RenderLiving<EntityPanda> {
    private final ModelPanda adultModel;
    private final ModelPanda babyModel = new ModelPanda(true);

    public RenderPanda(RenderManager manager) {
        this(manager, new ModelPanda());
    }

    private RenderPanda(RenderManager manager, ModelPanda model) {
        super(manager, model, 0.9F);
        adultModel = model;
        addLayer(new LayerPandaHeldItem());
    }

    @Override
    public void doRender(EntityPanda entity, double xPosition, double yPosition, double zPosition,
                         float entityYaw, float partialTicks) {
        mainModel = entity.isChild() ? babyModel : adultModel;
        super.doRender(entity, xPosition, yPosition, zPosition, entityYaw, partialTicks);
    }

    @Override
    protected void applyRotations(EntityPanda entity, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        int rollPosition = entity.getRollTicks();
        if (rollPosition > 0 && rollPosition < 32) {
            int phase = rollPosition / 8;
            float angle = phase * 90.0F + (rollPosition - phase * 8) * 90.0F / 7.0F;
            if (rollPosition + 1 < (phase + 1) * 8) {
                angle += partialTicks * 90.0F / 7.0F;
            }
            float height = entity.isChild() ? 0.3F : 0.8F;
            float translation = phase == 0 ? (height + 0.2F) * angle / 90.0F
                    : phase == 1 ? height + 0.2F + (height - 0.2F) * (angle - 90.0F) / 90.0F
                    : phase == 2 ? height + height * (270.0F - angle) / 90.0F
                    : height * (360.0F - angle) / 90.0F;
            GlStateManager.translate(0.0F, translation, 0.0F);
            GlStateManager.rotate(-angle, 1.0F, 0.0F, 0.0F);
        }
        float pitch = entity.prevRotationPitch
                + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        float sitting = entity.getSitAmount(partialTicks);
        if (sitting > 0.0F) {
            GlStateManager.translate(0.0F, 0.8F * sitting, 0.0F);
            GlStateManager.rotate(pitch + 90.0F * sitting, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, -sitting, 0.0F);
            if (entity.isScared()) {
                GlStateManager.rotate(MathHelper.cos(ageInTicks * 1.25F) * (float) Math.PI * 0.05F,
                        0.0F, 1.0F, 0.0F);
                if (entity.isChild()) {
                    GlStateManager.translate(0.0F, 0.8F, 0.55F);
                }
            }
        }
        float onBack = entity.getOnBackAmount(partialTicks);
        if (onBack > 0.0F) {
            GlStateManager.translate(0.0F, (entity.isChild() ? 0.5F : 1.3F) * onBack, 0.0F);
            GlStateManager.rotate(pitch + 180.0F * onBack, 1.0F, 0.0F, 0.0F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPanda panda) {
        String variant = panda.getVariantTextureName();
        String texture = panda.isChild()
                ? variant.isEmpty() ? "panda_baby" : variant + "_panda_baby"
                : "panda" + (variant.isEmpty() ? "" : "_" + variant);
        return new ResourceLocation("farmers_future_delight", "textures/entity/panda/" + texture + ".png");
    }
}
