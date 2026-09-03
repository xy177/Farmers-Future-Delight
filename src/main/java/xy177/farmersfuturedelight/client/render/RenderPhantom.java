package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelPhantom;
import xy177.farmersfuturedelight.common.entity.EntityPhantom;

@SideOnly(Side.CLIENT)
public class RenderPhantom extends RenderLiving<EntityPhantom> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/phantom/phantom.png");
    private static final ResourceLocation EYES = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/phantom/phantom_eyes.png");

    public RenderPhantom(RenderManager manager) {
        super(manager, new ModelPhantom(), 0.75F);
        addLayer(new EyesLayer(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPhantom entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityPhantom phantom, float partialTickTime) {
        float scale = 1.0F + 0.15F * phantom.getPhantomSize();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0.0F, 1.3125F, 0.1875F);
    }

    private static final class EyesLayer implements LayerRenderer<EntityPhantom> {
        private final RenderPhantom renderer;

        private EyesLayer(RenderPhantom renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityPhantom phantom, float limbSwing, float limbSwingAmount,
                                  float partialTicks, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scale) {
            renderer.bindTexture(EYES);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(!phantom.isInvisible());
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
            renderer.getMainModel().render(phantom, limbSwing, limbSwingAmount, ageInTicks,
                    netHeadYaw, headPitch, scale);
            Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
            int packedLight = phantom.getBrightnessForRender();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                    packedLight % 65536, packedLight / 65536);
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
