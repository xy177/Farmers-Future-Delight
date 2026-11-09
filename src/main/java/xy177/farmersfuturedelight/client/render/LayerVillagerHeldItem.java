package xy177.farmersfuturedelight.client.render;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderVillager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerShowTrades;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class LayerVillagerHeldItem implements LayerRenderer<EntityVillager> {
    private static final Set<RenderVillager> INSTALLED = Collections.newSetFromMap(new WeakHashMap<>());
    private final RenderLivingBase<EntityVillager> renderer;

    public LayerVillagerHeldItem(RenderLivingBase<EntityVillager> renderer) {
        this.renderer = renderer;
    }

    @SubscribeEvent
    public static void install(RenderLivingEvent.Pre<?> event) {
        if (event.getEntity().getClass() != EntityVillager.class
                || !(event.getRenderer() instanceof RenderVillager)
                || !(event.getRenderer().getMainModel() instanceof ModelVillager)) return;
        RenderVillager renderer = (RenderVillager) event.getRenderer();
        if (INSTALLED.add(renderer)) renderer.addLayer(new LayerVillagerHeldItem(renderer));
    }

    @Override
    public void doRenderLayer(EntityVillager villager, float limbSwing, float limbAmount,
                              float partialTicks, float age, float yaw, float pitch, float scale) {
        if (villager.isInvisible()) return;
        ItemStack stack = villager.getHeldItemMainhand();
        if (stack.isEmpty()) stack = EntityAIVillagerShowTrades.getDisplayedItem(villager);
        if (stack.isEmpty()) return;
        GlStateManager.pushMatrix();
        try {
            ((ModelVillager) renderer.getMainModel()).villagerArms.postRender(scale);
            GlStateManager.rotate(0.75F * 180.0F / (float) Math.PI, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(1.07F, 1.07F, 1.07F);
            GlStateManager.translate(0.0F, 0.13F, -0.34F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            Minecraft.getMinecraft().getItemRenderer().renderItem(villager, stack,
                    ItemCameraTransforms.TransformType.GROUND);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
