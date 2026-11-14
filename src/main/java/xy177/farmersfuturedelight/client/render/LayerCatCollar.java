package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelOcelot;
import xy177.farmersfuturedelight.common.FFDConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.entity.EntityCat;

@SideOnly(Side.CLIENT)
public class LayerCatCollar implements LayerRenderer<EntityOcelot> {
    private static final ResourceLocation COLLAR_TEXTURE = new ResourceLocation(
            "farmers_future_delight", "textures/entity/cat/cat_collar.png");
    private static final ResourceLocation BABY_COLLAR_TEXTURE = new ResourceLocation(
            "farmers_future_delight", "textures/entity/cat/cat_collar_baby.png");
    private final ModelCat adultModel = new ModelCat();
    private final ModelCatBaby babyModel = new ModelCatBaby();
    private final ModelOcelot legacyBabyModel = new ModelOcelot();

    public LayerCatCollar() {
    }

    @Override
    public void doRenderLayer(EntityOcelot entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        if (!(entity instanceof EntityCat) || !entity.isTamed()) {
            return;
        }
        float[] color = ((EntityCat) entity).getCollarColor().getColorComponentValues();
        boolean modernBaby = entity.isChild() && FFDConfig.modernBabyModels;
        ModelBase collarModel = entity.isChild() ? (modernBaby ? babyModel : legacyBabyModel) : adultModel;
        collarModel.isChild = entity.isChild();
        collarModel.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        GlStateManager.color(color[0], color[1], color[2], 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(
                modernBaby ? BABY_COLLAR_TEXTURE : COLLAR_TEXTURE);
        collarModel.render(entity, limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
