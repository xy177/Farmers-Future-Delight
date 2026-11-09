package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import xy177.farmersfuturedelight.common.entity.EntityFox;

public class LayerFoxHeldItem implements LayerRenderer<EntityFox> {
    private final RenderFox renderer;

    public LayerFoxHeldItem(RenderFox renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityFox fox, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        ItemStack stack = fox.getHeldItemMainhand();
        if (stack.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        ModelFox model = (ModelFox) renderer.getMainModel();
        GlStateManager.translate(model.head.rotationPointX * scale,
                model.head.rotationPointY * scale, model.head.rotationPointZ * scale);
        if (fox.isChild()) {
            GlStateManager.scale(0.75F, 0.75F, 0.75F);
        }
        GlStateManager.rotate(fox.getInterestedAngle(partialTicks) * 19.8F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(netHeadYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(headPitch, 1.0F, 0.0F, 0.0F);
        if (fox.isSleeping()) {
            GlStateManager.translate(fox.isChild() ? 0.4F : 0.46F, 0.26F,
                    fox.isChild() ? 0.15F : 0.22F);
        } else {
            GlStateManager.translate(0.06F, fox.isChild() ? 0.26F : 0.27F, -0.5F);
        }
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        if (fox.isSleeping()) {
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        }
        Minecraft.getMinecraft().getRenderItem().renderItem(
                stack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
