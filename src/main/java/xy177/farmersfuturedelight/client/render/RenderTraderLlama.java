package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelLlama;
import net.minecraft.client.renderer.entity.RenderLlama;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerLlamaDecor;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTraderLlama extends RenderLlama {
    private static final ResourceLocation TRADER_DECOR =
            new ResourceLocation("farmers_future_delight", "textures/entity/trader_llama.png");
    private static final ResourceLocation TRADER_BABY_DECOR =
            new ResourceLocation("farmers_future_delight", "textures/entity/trader_llama_baby.png");
    private static final ResourceLocation[] BABY_TEXTURES = {
            babyTexture("creamy"), babyTexture("white"), babyTexture("brown"), babyTexture("gray")
    };
    private final ModelLlama adultModel;
    private final ModelBabyTraderLlama babyModel = new ModelBabyTraderLlama(0.0F);

    public RenderTraderLlama(RenderManager manager) {
        super(manager);
        adultModel = (ModelLlama) mainModel;
        layerRenderers.removeIf(layer -> layer instanceof LayerLlamaDecor);
        addLayer(new TraderDecorLayer());
    }

    private static ResourceLocation babyTexture(String variant) {
        return new ResourceLocation("farmers_future_delight", "textures/entity/llama_" + variant + "_baby.png");
    }

    @Override
    public void doRender(EntityLlama entity, double positionX, double positionY, double positionZ,
                         float yaw, float partialTicks) {
        mainModel = modernBaby(entity) ? babyModel : adultModel;
        super.doRender(entity, positionX, positionY, positionZ, yaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLlama entity) {
        return modernBaby(entity) ? BABY_TEXTURES[Math.max(0, Math.min(3, entity.getVariant()))]
                : super.getEntityTexture(entity);
    }

    private static boolean modernBaby(EntityLlama entity) {
        return entity.isChild() && xy177.farmersfuturedelight.common.FFDConfig.modernBabyModels;
    }

    private final class TraderDecorLayer implements LayerRenderer<EntityLlama> {
        private final ModelLlama model = new ModelLlama(0.5F);
        private final ModelBabyTraderLlama babyDecor = new ModelBabyTraderLlama(0.5F);
        private final LayerLlamaDecor carpet = new LayerLlamaDecor(RenderTraderLlama.this);

        @Override
        public void doRenderLayer(EntityLlama entity, float limbSwing, float limbSwingAmount,
                                  float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (!entity.isChild() && entity.hasColor()) {
                carpet.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                return;
            }
            bindTexture(modernBaby(entity) ? TRADER_BABY_DECOR : TRADER_DECOR);
            ModelLlama decoration = modernBaby(entity) ? babyDecor : model;
            decoration.setModelAttributes(getMainModel());
            decoration.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
