package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.util.ResourceLocation;

public final class LayerIronGolemCracks implements LayerRenderer<EntityIronGolem> {
    private static final ResourceLocation LOW = texture("low");
    private static final ResourceLocation MEDIUM = texture("medium");
    private static final ResourceLocation HIGH = texture("high");
    private final RenderLivingBase<?> renderer;
    private final ModelIronGolem model;

    public LayerIronGolemCracks(RenderLivingBase<?> renderer) {
        this.renderer = renderer;
        this.model = (ModelIronGolem) renderer.getMainModel();
    }

    @Override
    public void doRenderLayer(EntityIronGolem golem, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        float healthRatio = golem.getHealth() / golem.getMaxHealth();
        ResourceLocation texture = healthRatio < 0.25F ? HIGH
                : healthRatio < 0.5F ? MEDIUM
                : healthRatio < 0.75F ? LOW : null;
        if (texture == null) {
            return;
        }
        model.setModelAttributes(renderer.getMainModel());
        model.setLivingAnimations(golem, limbSwing, limbSwingAmount, partialTicks);
        model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                headPitch, scale, golem);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        model.render(golem, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                headPitch, scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    private static ResourceLocation texture(String severity) {
        return new ResourceLocation("farmers_future_delight", "textures/entity/iron_golem/iron_golem_crackiness_"
                + severity + ".png");
    }
}
