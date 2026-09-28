package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDRiptide;

public class LayerRiptide implements LayerRenderer<AbstractClientPlayer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/trident_riptide.png");
    private final RenderPlayer renderer;
    private final RiptideModel model = new RiptideModel();

    public LayerRiptide(RenderPlayer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing,
                              float limbSwingAmount, float partialTicks, float ageInTicks,
                              float netHeadYaw, float headPitch, float scale) {
        if (!FFDRiptide.isActive(player)) {
            return;
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        renderer.bindTexture(TEXTURE);
        for (int index = 0; index < 3; index++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(ageInTicks * -(45.0F + index * 5.0F),
                    0.0F, 1.0F, 0.0F);
            float size = 0.75F * index;
            GlStateManager.scale(size, size, size);
            GlStateManager.translate(0.0F, -0.2F + 0.6F * index, 0.0F);
            model.render(player, scale);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    private static final class RiptideModel extends ModelBase {
        private final ModelRenderer box;

        private RiptideModel() {
            textureWidth = 64;
            textureHeight = 64;
            box = new ModelRenderer(this, 0, 0);
            box.addBox(-8.0F, -16.0F, -8.0F, 16, 32, 16);
        }

        private void render(Entity entity, float scale) {
            box.render(scale);
        }
    }
}
