package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelBee;
import xy177.farmersfuturedelight.client.model.ModelBeeBaby;
import xy177.farmersfuturedelight.common.entity.EntityBee;

@SideOnly(Side.CLIENT)
public class RenderBee extends RenderLiving<EntityBee> {
    private static final ResourceLocation BEE = texture("bee.png");
    private static final ResourceLocation BEE_NECTAR = texture("bee_nectar.png");
    private static final ResourceLocation BEE_ANGRY = texture("bee_angry.png");
    private static final ResourceLocation BEE_ANGRY_NECTAR = texture("bee_angry_nectar.png");
    private static final ResourceLocation BEE_BABY = texture("bee_baby.png");
    private static final ResourceLocation BEE_BABY_NECTAR = texture("bee_nectar_baby.png");
    private static final ResourceLocation BEE_BABY_ANGRY = texture("bee_angry_baby.png");
    private static final ResourceLocation BEE_BABY_ANGRY_NECTAR = texture("bee_angry_nectar_baby.png");
    private final ModelBase adultModel;
    private final ModelBase babyModel = new ModelBeeBaby();

    public RenderBee(RenderManager manager) {
        super(manager, new ModelBee(), 0.4F);
        adultModel = mainModel;
    }

    @Override
    public void doRender(EntityBee bee, double x, double y, double z, float entityYaw,
                         float partialTicks) {
        mainModel = bee.isChild() ? babyModel : adultModel;
        shadowSize = bee.isChild() ? 0.2F : 0.4F;
        super.doRender(bee, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBee bee) {
        if (bee.isChild()) {
            if (bee.getAnger() > 0) {
                return bee.hasNectar() ? BEE_BABY_ANGRY_NECTAR : BEE_BABY_ANGRY;
            }
            return bee.hasNectar() ? BEE_BABY_NECTAR : BEE_BABY;
        }
        if (bee.getAnger() > 0) {
            return bee.hasNectar() ? BEE_ANGRY_NECTAR : BEE_ANGRY;
        }
        return bee.hasNectar() ? BEE_NECTAR : BEE;
    }

    @Override
    protected void preRenderCallback(EntityBee bee, float partialTickTime) {
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/entity/bee/" + name);
    }
}
