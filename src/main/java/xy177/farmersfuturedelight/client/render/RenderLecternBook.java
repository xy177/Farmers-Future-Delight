package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.common.block.BlockLectern;
import xy177.farmersfuturedelight.common.tile.TileEntityLectern;

public class RenderLecternBook extends TileEntitySpecialRenderer<TileEntityLectern> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "textures/entity/enchanting_table_book.png");
    private final ModelBook model = new ModelBook();

    @Override
    public void render(TileEntityLectern tile, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (tile.getBook().isEmpty()) {
            return;
        }
        bindTexture(TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 1.0625D, z + 0.5D);
        net.minecraft.util.EnumFacing facing = tile.getWorld().getBlockState(tile.getPos())
                .getValue(BlockLectern.FACING);
        GlStateManager.rotate(-facing.rotateY().getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(67.5F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.0D, -0.125D, 0.0D);
        model.render(null, 0.0F, 0.1F, 0.9F, 1.2F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }
}
