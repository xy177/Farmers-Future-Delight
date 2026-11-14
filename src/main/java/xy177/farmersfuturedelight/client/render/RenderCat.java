package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderOcelot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelOcelot;
import xy177.farmersfuturedelight.common.FFDConfig;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.entity.EntityCat;

@SideOnly(Side.CLIENT)
public class RenderCat extends RenderOcelot {
    private final ModelCat adultModel = new ModelCat();
    private final ModelCatBaby babyModel = new ModelCatBaby();
    private final ModelOcelot legacyBabyModel = new ModelOcelot();

    public RenderCat(RenderManager manager) {
        super(manager);
        addLayer(new LayerCatCollar());
    }

    @Override
    public void doRender(EntityOcelot entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        mainModel = entity.isChild() ? (FFDConfig.modernBabyModels ? babyModel : legacyBabyModel) : adultModel;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected void preRenderCallback(EntityOcelot entity, float partialTicks) {
        if (!entity.isChild() || !FFDConfig.modernBabyModels) {
            GlStateManager.scale(0.8F, 0.8F, 0.8F);
        }
    }

    @Override
    protected void applyRotations(EntityOcelot entity, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        if (entity instanceof EntityCat) {
            float lying = ((EntityCat) entity).getLieDownAmount(partialTicks);
            if (lying > 0.0F) {
                GlStateManager.translate(0.4F * lying, 0.15F * lying, 0.1F * lying);
                GlStateManager.rotate(90.0F * lying, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityOcelot entity) {
        if (!(entity instanceof EntityCat)) {
            return super.getEntityTexture(entity);
        }
        String variant = "cat_" + ((EntityCat) entity).getVariantName();
        if (entity.isChild() && FFDConfig.modernBabyModels) {
            variant += "_baby";
        }
        return new ResourceLocation("farmers_future_delight", "textures/entity/cat/" + variant + ".png");
    }
}
