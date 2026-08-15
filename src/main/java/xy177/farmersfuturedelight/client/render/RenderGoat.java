package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelGoat;
import xy177.farmersfuturedelight.client.model.ModelGoatBaby;
import xy177.farmersfuturedelight.common.entity.EntityGoat;

@SideOnly(Side.CLIENT)
public class RenderGoat extends RenderLiving<EntityGoat> {
    private static final ResourceLocation ADULT_TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/goat/goat.png");
    private static final ResourceLocation BABY_TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/goat/goat_baby.png");

    private final ModelBase adultModel;
    private final ModelBase babyModel;

    public RenderGoat(RenderManager manager) {
        super(manager, new ModelGoat(), 0.7F);
        adultModel = mainModel;
        babyModel = new ModelGoatBaby();
    }

    @Override
    public void doRender(EntityGoat goat, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        mainModel = goat.isChild() ? babyModel : adultModel;
        shadowSize = goat.isChild() ? 0.385F : 0.7F;
        super.doRender(goat, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGoat goat) {
        return goat.isChild() ? BABY_TEXTURE : ADULT_TEXTURE;
    }
}
