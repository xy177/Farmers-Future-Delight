package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.entity.EntityFox;

@SideOnly(Side.CLIENT)
public class RenderFox extends RenderLiving<EntityFox> {
    private final ModelFox adultModel;
    private final ModelFox babyModel = new ModelFox(true);

    public RenderFox(RenderManager manager) {
        this(manager, new ModelFox());
    }

    private RenderFox(RenderManager manager, ModelFox model) {
        super(manager, model, 0.4F);
        adultModel = model;
        addLayer(new LayerFoxHeldItem(this));
    }

    @Override
    public void doRender(EntityFox entity, double xPosition, double yPosition, double zPosition,
                         float entityYaw, float partialTicks) {
        mainModel = entity.isChild() ? babyModel : adultModel;
        super.doRender(entity, xPosition, yPosition, zPosition, entityYaw, partialTicks);
    }

    @Override
    protected void applyRotations(EntityFox entity, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        if (entity.isPouncing()) {
            float pitch = entity.prevRotationPitch
                    + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            GlStateManager.rotate(-pitch, 1.0F, 0.0F, 0.0F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFox fox) {
        String texture = fox.isSnowVariant() ? "fox_snow" : "fox";
        if (fox.isSleeping()) texture += "_sleep";
        if (fox.isChild()) texture += "_baby";
        return new ResourceLocation("farmers_future_delight", "textures/entity/fox/" + texture + ".png");
    }
}
