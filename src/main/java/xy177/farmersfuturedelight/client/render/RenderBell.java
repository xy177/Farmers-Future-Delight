package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.common.tile.TileEntityBell;

public class RenderBell extends TileEntitySpecialRenderer<TileEntityBell> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "farmers_future_delight", "textures/entity/bell/bell_body.png");
    private final ModelBell model = new ModelBell();

    @Override
    public void render(TileEntityBell tile, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        bindTexture(TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        model.render(tile.getTicks(partialTicks), tile.getDirection(), 0.0625F);
        GlStateManager.popMatrix();
    }
}
