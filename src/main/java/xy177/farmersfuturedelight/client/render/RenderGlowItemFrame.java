package xy177.farmersfuturedelight.client.render;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.MapData;

import xy177.farmersfuturedelight.common.entity.EntityGlowItemFrame;

/** Item-frame renderer using the dedicated 26.3 glow frame model and full-bright pass. */
public class RenderGlowItemFrame extends Render<EntityGlowItemFrame> {
    private static final ResourceLocation MAP_BACKGROUND_TEXTURES =
            new ResourceLocation("textures/map/map_background.png");
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final ModelResourceLocation frameModel = new ModelResourceLocation(
            "farmers_future_delight:glow_item_frame", "normal");
    private final ModelResourceLocation mapModel = new ModelResourceLocation(
            "farmers_future_delight:glow_item_frame_map", "map");
    private final RenderItem itemRenderer;

    public RenderGlowItemFrame(RenderManager manager) {
        super(manager);
        itemRenderer = minecraft.getRenderItem();
    }

    @Override
    public void doRender(EntityGlowItemFrame entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        GlStateManager.pushAttrib();
        int packed = entity.getBrightnessForRender();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        renderFrame(entity, x, y, z);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                packed % 65536, packed / 65536);
        GlStateManager.popAttrib();
    }

    private void renderFrame(EntityGlowItemFrame entity, double x, double y, double z) {
        GlStateManager.pushMatrix();
        BlockPos hanging = entity.getHangingPosition();
        double dx = hanging.getX() - entity.posX + x;
        double dy = hanging.getY() - entity.posY + y;
        double dz = hanging.getZ() - entity.posZ + z;
        GlStateManager.translate(dx + 0.5D, dy + 0.5D, dz + 0.5D);
        GlStateManager.rotate(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRendererDispatcher dispatcher = minecraft.getBlockRendererDispatcher();
        ModelManager models = dispatcher.getBlockModelShapes().getModelManager();
        IBakedModel model = models.getModel(entity.getDisplayedItem().getItem() instanceof ItemMap
                ? mapModel : frameModel);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        if (renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(getTeamColor(entity));
        }
        dispatcher.getBlockModelRenderer().renderModelBrightnessColor(model, 1.0F, 1.0F, 1.0F, 1.0F);
        if (renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }
        GlStateManager.popMatrix();
        GlStateManager.translate(0.0F, 0.0F, 0.4375F);
        renderItem(entity);
        GlStateManager.popMatrix();
    }

    private void renderItem(EntityGlowItemFrame frame) {
        ItemStack stack = frame.getDisplayedItem();
        if (stack.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        boolean map = stack.getItem() instanceof ItemMap;
        int rotation = map ? frame.getRotation() % 4 * 2 : frame.getRotation();
        GlStateManager.rotate(rotation * 360.0F / 8.0F, 0.0F, 0.0F, 1.0F);
        if (map) {
                renderManager.renderEngine.bindTexture(MAP_BACKGROUND_TEXTURES);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.scale(0.0078125F, 0.0078125F, 0.0078125F);
                GlStateManager.translate(-64.0F, -64.0F, 0.0F);
                MapData mapData = ((ItemMap) stack.getItem()).getMapData(stack, frame.world);
                GlStateManager.translate(0.0F, 0.0F, -1.0F);
                if (mapData != null) {
                    minecraft.entityRenderer.getMapItemRenderer().renderMap(mapData, true);
                }
        } else {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                GlStateManager.pushAttrib();
                RenderHelper.enableStandardItemLighting();
                itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popAttrib();
        }
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityGlowItemFrame entity) {
        return null;
    }
}
