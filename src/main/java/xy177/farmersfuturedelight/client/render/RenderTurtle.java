package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelTurtle;
import xy177.farmersfuturedelight.client.model.ModelTurtleBaby;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;

@SideOnly(Side.CLIENT)
public class RenderTurtle extends RenderLiving<EntityTurtle> {
    private final ModelTurtle adultModel;
    private final ModelTurtleBaby babyModel;
    private static final ResourceLocation ADULT_TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/turtle/turtle.png");
    private static final ResourceLocation BABY_TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/turtle/turtle_baby.png");

    public RenderTurtle(RenderManager manager) {
        super(manager, new ModelTurtle(), 0.7F);
        adultModel = (ModelTurtle) mainModel;
        babyModel = new ModelTurtleBaby();
    }

    @Override
    public void doRender(EntityTurtle turtle, double x, double y, double z, float entityYaw, float partialTicks) {
        mainModel = turtle.isChild() ? babyModel : adultModel;
        super.doRender(turtle, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTurtle turtle) {
        return turtle.isChild() ? BABY_TEXTURE : ADULT_TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityTurtle turtle, float partialTickTime) {
        if (turtle.isChild()) {
            shadowSize = 0.7F * 0.83F;
        } else {
            shadowSize = 0.7F;
        }
    }
}
