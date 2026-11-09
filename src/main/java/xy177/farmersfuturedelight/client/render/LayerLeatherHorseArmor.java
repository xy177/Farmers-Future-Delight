package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.item.ItemLeatherHorseArmor;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class LayerLeatherHorseArmor implements LayerRenderer<EntityHorse> {
    private static final DataParameter<ItemStack> ARMOR_STACK = ObfuscationReflectionHelper.getPrivateValue(
            EntityHorse.class, null, "HORSE_ARMOR_STACK");
    private static final ResourceLocation LEATHER = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/horse/leather_armor.png");
    private static final ResourceLocation NETHERITE = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/horse/netherite_armor.png");
    private static final ResourceLocation LEATHER_OVERLAY = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/entity/horse/leather_armor_overlay.png");
    private final RenderLivingBase<?> renderer;
    private final ModelHorseArmor model = new ModelHorseArmor();

    public LayerLeatherHorseArmor(RenderLivingBase<?> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityHorse horse, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        ItemStack armor = horse.getDataManager().get(ARMOR_STACK);
        if (armor.isEmpty() || (armor.getItem() != FFDItems.LEATHER_HORSE_ARMOR
                && armor.getItem() != FFDItems.NETHERITE_HORSE_ARMOR)) {
            return;
        }

        boolean leather = armor.getItem() == FFDItems.LEATHER_HORSE_ARMOR;
        int color = leather ? ItemLeatherHorseArmor.getColor(armor) : 0xFFFFFF;
        model.setModelAttributes(renderer.getMainModel());
        GlStateManager.color(((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(leather ? LEATHER : NETHERITE);
        model.setLivingAnimations(horse, limbSwing, limbSwingAmount, partialTicks);
        model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                headPitch, scale, horse);
        model.render(horse, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                headPitch, scale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (leather) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(LEATHER_OVERLAY);
            model.render(horse, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                    headPitch, scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
