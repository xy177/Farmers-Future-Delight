package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import xy177.farmersfuturedelight.common.block.BlockCampfire;
import xy177.farmersfuturedelight.common.tile.TileEntityCampfire;

public class RenderCampfireContents extends TileEntitySpecialRenderer<TileEntityCampfire> {
    @Override
    public void render(TileEntityCampfire tile, double x, double y, double z, float partialTicks,
                       int destroyStage, float alpha) {
        ItemStack[] items = tile.getRenderItems();
        EnumFacing facing = tile.getWorld().getBlockState(tile.getPos()).getValue(BlockCampfire.FACING);
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack stack = items[slot];
            if (stack.isEmpty()) continue;
            EnumFacing direction = EnumFacing.getHorizontal(
                    (slot + facing.getHorizontalIndex()) & 3);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5D, y + 0.44921875D, z + 0.5D);
            GlStateManager.rotate(-direction.getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(-0.3125F, -0.3125F, 0.0F);
            GlStateManager.scale(0.375F, 0.375F, 0.375F);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack,
                    ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
    }
}
