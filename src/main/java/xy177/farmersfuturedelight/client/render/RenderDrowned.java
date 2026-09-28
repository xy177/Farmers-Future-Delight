package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.ForgeHooksClient;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelBabyDrowned;
import xy177.farmersfuturedelight.client.model.ModelDrowned;
import xy177.farmersfuturedelight.common.entity.EntityDrowned;

public class RenderDrowned extends RenderBiped<EntityDrowned> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/zombie/drowned.png");
    private static final ResourceLocation BABY_TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/zombie/drowned_baby.png");
    private static final ResourceLocation OUTER = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/zombie/drowned_outer_layer.png");
    private static final ResourceLocation BABY_OUTER = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/zombie/drowned_outer_layer_baby.png");
    private final ModelBase adultModel;
    private final ModelBase babyModel = new ModelBabyDrowned(0.0F);

    public RenderDrowned(RenderManager manager) {
        super(manager, new ModelDrowned(0.0F, 0.0F, 64, 64), 0.5F);
        adultModel = mainModel;
        layerRenderers.removeIf(layer -> layer.getClass() == LayerHeldItem.class);
        addLayer(new DrownedHeldItemLayer(this));
        addLayer(new DrownedArmorLayer(this));
        addLayer(new OuterLayer(this));
    }

    @Override
    public void doRender(EntityDrowned drowned, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        mainModel = drowned.isChild() ? babyModel : adultModel;
        shadowSize = 0.5F;
        super.doRender(drowned, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDrowned entity) {
        return entity.isChild() ? BABY_TEXTURE : TEXTURE;
    }

    @Override
    protected void applyRotations(EntityDrowned drowned, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.applyRotations(drowned, ageInTicks, rotationYaw, partialTicks);
        float swimAmount = drowned.getSwimAmount(partialTicks);
        if (swimAmount > 0.0F) {
            GlStateManager.rotate(swimAmount * (-10.0F - drowned.rotationPitch),
                    1.0F, 0.0F, 0.0F);
        }
    }

    private static final class DrownedHeldItemLayer implements LayerRenderer<EntityDrowned> {
        private final RenderDrowned renderer;

        private DrownedHeldItemLayer(RenderDrowned renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityDrowned drowned, float limbSwing, float limbSwingAmount,
                                  float partialTicks, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scale) {
            boolean rightPrimary = drowned.getPrimaryHand() == EnumHandSide.RIGHT;
            ItemStack right = rightPrimary
                    ? drowned.getHeldItemMainhand() : drowned.getHeldItemOffhand();
            ItemStack left = rightPrimary
                    ? drowned.getHeldItemOffhand() : drowned.getHeldItemMainhand();
            if (right.isEmpty() && left.isEmpty()) {
                return;
            }
            GlStateManager.pushMatrix();
            renderHeldItem(drowned, right, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,
                    EnumHandSide.RIGHT);
            renderHeldItem(drowned, left, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND,
                    EnumHandSide.LEFT);
            GlStateManager.popMatrix();
        }

        private void renderHeldItem(EntityDrowned drowned, ItemStack stack,
                                    ItemCameraTransforms.TransformType transform,
                                    EnumHandSide hand) {
            if (stack.isEmpty()) {
                return;
            }
            GlStateManager.pushMatrix();
            if (drowned.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            ((ModelBiped) renderer.getMainModel()).postRenderArm(0.0625F, hand);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            boolean left = hand == EnumHandSide.LEFT;
            boolean child = drowned.isChild();
            GlStateManager.translate(child ? 0.0F : (left ? -1.0F : 1.0F) / 16.0F,
                    child ? 0.0625F : 0.125F, child ? -0.28125F : -0.625F);
            Minecraft.getMinecraft().getItemRenderer().renderItemSide(
                    drowned, stack, transform, left);
            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }

    private static final class DrownedArmorLayer extends LayerBipedArmor {
        private DrownedArmorLayer(RenderDrowned renderer) {
            super(renderer);
        }

        @Override
        protected void initArmor() {
            modelLeggings = new ModelDrowned(0.5F, true);
            modelArmor = new ModelDrowned(1.0F, true);
        }

        @Override
        protected ModelBiped getArmorModelHook(EntityLivingBase entity, ItemStack itemStack,
                                               EntityEquipmentSlot slot, ModelBiped model) {
            return ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
        }
    }

    private static final class OuterLayer implements LayerRenderer<EntityDrowned> {
        private final RenderDrowned renderer;
        private final ModelDrowned adultModel = new ModelDrowned(0.25F, 0.0F, 64, 64);
        private final ModelDrowned babyModel = new ModelBabyDrowned(0.25F);

        private OuterLayer(RenderDrowned renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityDrowned drowned, float limbSwing,
                                  float limbSwingAmount, float partialTicks,
                                  float ageInTicks, float netHeadYaw, float headPitch,
                                  float scale) {
            ModelDrowned model = drowned.isChild() ? babyModel : adultModel;
            renderer.bindTexture(drowned.isChild() ? BABY_OUTER : OUTER);
            model.setModelAttributes(renderer.getMainModel());
            model.setLivingAnimations(drowned, limbSwing, limbSwingAmount, partialTicks);
            model.render(drowned, limbSwing, limbSwingAmount, ageInTicks,
                    netHeadYaw, headPitch, scale);
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
