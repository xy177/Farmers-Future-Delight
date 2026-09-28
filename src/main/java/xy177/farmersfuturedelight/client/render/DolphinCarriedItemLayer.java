package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.entity.EntityDolphin;

public class DolphinCarriedItemLayer implements LayerRenderer<EntityDolphin> {
    private final RenderLivingBase<?> renderer;

    public DolphinCarriedItemLayer(RenderLivingBase<?> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityDolphin dolphin, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        ItemStack stack = dolphin.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        if (stack.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        float pitch = MathHelper.abs(dolphin.rotationPitch) / 60.0F;
        if (dolphin.rotationPitch < 0.0F) {
            GlStateManager.translate(0.0F, 1.0F - pitch * 0.5F, -1.0F + pitch * 0.5F);
        } else {
            GlStateManager.translate(0.0F, 1.0F + pitch * 0.8F, -1.0F + pitch * 0.2F);
        }
        Minecraft.getMinecraft().getRenderItem().renderItem(stack,
                ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
