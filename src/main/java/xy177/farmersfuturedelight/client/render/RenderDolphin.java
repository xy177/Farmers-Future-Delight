package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelBabyDolphin;
import xy177.farmersfuturedelight.client.model.ModelDolphin;
import xy177.farmersfuturedelight.common.entity.EntityDolphin;

public class RenderDolphin extends RenderLiving<EntityDolphin> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/dolphin.png");
    private static final ResourceLocation BABY_TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/dolphin_baby.png");
    private final ModelBase adultModel;
    private final ModelBase babyModel = new ModelBabyDolphin();

    public RenderDolphin(RenderManager manager) {
        super(manager, new ModelDolphin(), 0.7F);
        adultModel = mainModel;
        addLayer(new DolphinCarriedItemLayer(this));
    }

    @Override
    public void doRender(EntityDolphin dolphin, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        mainModel = dolphin.isChild() ? babyModel : adultModel;
        shadowSize = dolphin.isChild() ? 0.455F : 0.7F;
        super.doRender(dolphin, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDolphin entity) {
        return entity.isChild() ? BABY_TEXTURE : TEXTURE;
    }
}
