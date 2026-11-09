package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.entity.EntityWanderingTrader;

@SideOnly(Side.CLIENT)
public class RenderWanderingTrader extends RenderLiving<EntityWanderingTrader> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("farmers_future_delight", "textures/entity/wandering_trader.png");

    public RenderWanderingTrader(RenderManager manager) {
        super(manager, new ModelWanderingTrader(), 0.5F);
        addLayer(new LayerCustomHead(((ModelWanderingTrader) getMainModel()).villagerHead));
        addLayer(new CrossedArmsItemLayer());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWanderingTrader entity) {
        return TEXTURE;
    }

    private final class CrossedArmsItemLayer implements LayerRenderer<EntityWanderingTrader> {
        @Override
        public void doRenderLayer(EntityWanderingTrader entity, float limbSwing, float limbSwingAmount,
                                  float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.getHeldItemMainhand().isEmpty()) return;
            GlStateManager.pushMatrix();
            ((ModelWanderingTrader) getMainModel()).villagerArms.postRender(scale);
            GlStateManager.rotate(0.75F * 180.0F / (float) Math.PI, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(1.07F, 1.07F, 1.07F);
            GlStateManager.translate(0.0F, 0.13F, -0.34F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            Minecraft.getMinecraft().getItemRenderer().renderItem(entity, entity.getHeldItemMainhand(),
                    ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
