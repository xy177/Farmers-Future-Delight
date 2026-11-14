package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelHoglin;
import xy177.farmersfuturedelight.common.entity.EntityHoglin;

public final class RenderHoglin<T extends EntityHoglin> extends RenderLiving<T> {
    private final ModelBase adultModel = new ModelHoglin(false);
    private final ModelBase babyModel = new ModelHoglin(true);

    public RenderHoglin(RenderManager manager) {
        super(manager, new ModelHoglin(false), 0.7F);
    }

    @Override
    public void doRender(T hoglin, double posX, double posY, double posZ,
                         float entityYaw, float partialTicks) {
        mainModel = hoglin.isChild() ? babyModel : adultModel;
        shadowSize = hoglin.isChild() ? 0.35F : 0.7F;
        super.doRender(hoglin, posX, posY, posZ, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(T hoglin) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/entity/hoglin/"
                + (hoglin.isZoglin() ? "zoglin" : "hoglin") + (hoglin.isChild() ? "_baby" : "") + ".png");
    }

    @Override
    protected void applyRotations(T hoglin, float ageInTicks, float rotationYaw, float partialTicks) {
        if (hoglin.isConverting()) rotationYaw += (float) (Math.cos(ageInTicks * 3.25D) * Math.PI * 0.4D);
        super.applyRotations(hoglin, ageInTicks, rotationYaw, partialTicks);
    }
}
