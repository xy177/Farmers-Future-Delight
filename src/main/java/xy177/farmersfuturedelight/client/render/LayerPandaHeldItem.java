package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityPanda;

public class LayerPandaHeldItem implements LayerRenderer<EntityPanda> {
    @Override
    public void doRenderLayer(EntityPanda panda, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        ItemStack stack = panda.getHeldItemMainhand();
        if (stack.isEmpty() || !panda.isSitting() || panda.isScared()) {
            return;
        }
        GlStateManager.pushMatrix();
        float itemHeight = 1.4F;
        float itemDepth = -0.6F;
        if (panda.isEating()) {
            float movement = MathHelper.sin(ageInTicks * 0.6F);
            itemDepth -= 0.2F * movement + 0.2F;
            itemHeight -= 0.09F * movement;
        }
        GlStateManager.translate(0.1F, itemHeight, itemDepth);
        Minecraft.getMinecraft().getRenderItem().renderItem(
                stack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
