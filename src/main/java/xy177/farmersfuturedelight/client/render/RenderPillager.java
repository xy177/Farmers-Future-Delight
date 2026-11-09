package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelIllager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.entity.EntityPillager;

@SideOnly(Side.CLIENT)
public class RenderPillager extends RenderLiving<EntityPillager> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("farmers_future_delight", "textures/entity/illager/pillager.png");

    public RenderPillager(RenderManager manager) {
        super(manager, new ModelPillager(), 0.5F);
        addLayer(new LayerCustomHead(((ModelIllager) getMainModel()).head));
        addLayer(new LayerHeldItem(this) {
            @Override
            public void doRenderLayer(EntityLivingBase entity, float limbSwing,
                                      float limbSwingAmount, float partialTicks,
                                      float ageInTicks, float netHeadYaw,
                                      float headPitch, float scale) {
                super.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks,
                        ageInTicks, netHeadYaw, headPitch, scale);
            }

            @Override
            protected void translateToHand(EnumHandSide side) {
                ((ModelIllager) getMainModel()).getArm(side).postRender(0.0625F);
            }
        });
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPillager entity) {
        return TEXTURE;
    }

}
