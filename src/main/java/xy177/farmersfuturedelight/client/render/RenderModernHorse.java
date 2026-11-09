package xy177.farmersfuturedelight.client.render;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelModernHorse;
import xy177.farmersfuturedelight.common.item.ItemLeatherHorseArmor;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class RenderModernHorse extends RenderLiving<AbstractHorse> {
    private static final DataParameter<ItemStack> ARMOR_STACK = ObfuscationReflectionHelper.getPrivateValue(
            EntityHorse.class, null, "HORSE_ARMOR_STACK");
    private static RenderModernHorse renderer;
    private final FFDRenderLayerBridge<AbstractHorse> externalLayers = new FFDRenderLayerBridge<>();

    private RenderModernHorse(RenderManager manager) {
        super(manager, new ModelModernHorse(0, false), 0.75F);
        addLayer(new Equipment());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderHorse(RenderLivingEvent.Pre<?> event) {
        Class<?> type = event.getEntity().getClass();
        if (event.getEntity().isChild() || event.getRenderer() instanceof RenderModernHorse
                || (type != EntityHorse.class && type != EntitySkeletonHorse.class && type != EntityZombieHorse.class)
                || !event.getRenderer().getClass().getName().startsWith("net.minecraft.client.renderer.entity.")) return;
        AbstractHorse horse = (AbstractHorse) event.getEntity();
        if (horse instanceof EntityHorse && armorName(armor((EntityHorse) horse)) == null) return;
        List<LayerRenderer<?>> layers = ObfuscationReflectionHelper.getPrivateValue(RenderLivingBase.class,
                event.getRenderer(), "field_177097_h");
        for (LayerRenderer<?> layer : layers) {
            if (!layer.getClass().getName().startsWith("net.minecraft.client.renderer.entity.")
                    && layer.getClass() != LayerLeatherHorseArmor.class
                    && !FFDRenderLayerBridge.supports(layer)) return;
        }
        if (renderer == null) renderer = new RenderModernHorse(Minecraft.getMinecraft().getRenderManager());
        if (!renderer.externalLayers.synchronize(renderer, layers, renderer.layerRenderers)) return;
        event.setCanceled(true);
        boolean outlines = ObfuscationReflectionHelper.getPrivateValue(Render.class, event.getRenderer(), "field_188301_f");
        renderer.setRenderOutlines(outlines);
        try {
            renderer.doRender(horse, event.getX(), event.getY(), event.getZ(), horse.rotationYaw, event.getPartialRenderTick());
        } finally {
            renderer.setRenderOutlines(false);
        }
    }

    private static ItemStack armor(EntityHorse horse) {
        return horse.getDataManager().get(ARMOR_STACK);
    }

    private static String armorName(ItemStack stack) {
        if (stack.isEmpty()) return "";
        if (stack.getItem() == Items.IRON_HORSE_ARMOR) return "iron";
        if (stack.getItem() == Items.GOLDEN_HORSE_ARMOR) return "gold";
        if (stack.getItem() == Items.DIAMOND_HORSE_ARMOR) return "diamond";
        if (stack.getItem() == FFDItems.LEATHER_HORSE_ARMOR) return "leather";
        if (stack.getItem() == FFDItems.NETHERITE_HORSE_ARMOR) return "netherite";
        return null;
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/entity/horse/" + path + ".png");
    }

    @Override
    protected ResourceLocation getEntityTexture(AbstractHorse horse) {
        if (horse instanceof EntitySkeletonHorse) return texture("horse_skeleton");
        if (horse instanceof EntityZombieHorse) return texture("horse_zombie");
        return FFDModernHorseTextures.get((EntityHorse) horse, false);
    }

    @Override
    protected void preRenderCallback(AbstractHorse horse, float partialTicks) {
        if (horse instanceof EntityHorse) {
            GlStateManager.scale(1.1F, 1.1F, 1.1F);
        }
    }

    private final class Equipment implements LayerRenderer<AbstractHorse> {
        private final ModelModernHorse armorModel = new ModelModernHorse(0.1F, false);
        private final ModelModernHorse saddleModel = new ModelModernHorse(0, true);

        @Override
        public void doRenderLayer(AbstractHorse horse, float swing, float amount, float partial, float age,
                                  float yaw, float pitch, float scale) {
            if (horse.isInvisible()) return;
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            if (horse instanceof EntityHorse) {
                EntityHorse normalHorse = (EntityHorse) horse;
                ItemStack stack = armor(normalHorse);
                String name = armorName(stack);
                if (name != null && !name.isEmpty()) {
                    armorModel.setModelAttributes(mainModel);
                    armorModel.setLivingAnimations(horse, swing, amount, partial);
                    int color = "leather".equals(name) ? ItemLeatherHorseArmor.getColor(stack) : 0xFFFFFF;
                    GlStateManager.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
                    bindTexture(texture("equipment/" + name));
                    armorModel.render(horse, swing, amount, age, yaw, pitch, scale);
                    GlStateManager.color(1, 1, 1, 1);
                    if ("leather".equals(name)) {
                        bindTexture(texture("equipment/leather_overlay"));
                        armorModel.render(horse, swing, amount, age, yaw, pitch, scale);
                    }
                }
            }
            if (horse.isHorseSaddled()) {
                saddleModel.setModelAttributes(mainModel);
                saddleModel.setLivingAnimations(horse, swing, amount, partial);
                bindTexture(texture("equipment/saddle"));
                saddleModel.render(horse, swing, amount, age, yaw, pitch, scale);
            }
            GlStateManager.disableBlend();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
