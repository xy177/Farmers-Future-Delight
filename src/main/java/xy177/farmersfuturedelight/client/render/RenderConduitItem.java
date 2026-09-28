package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import xy177.farmersfuturedelight.common.tile.TileEntityConduit;

public class RenderConduitItem extends TileEntityItemStackRenderer {
    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        TileEntityConduit tile = new TileEntityConduit();
        TileEntitySpecialRenderer<?> specialRenderer =
                TileEntityRendererDispatcher.instance.getRenderer(tile);
        RenderConduit renderer = (RenderConduit) specialRenderer;
        renderer.renderItem(partialTicks);
    }
}
