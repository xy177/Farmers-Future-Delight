package xy177.farmersfuturedelight.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class FFDBeaconPaymentRenderer {
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation("minecraft", "textures/gui/container/beacon.png");

    private FFDBeaconPaymentRenderer() {
    }

    @SubscribeEvent
    public static void drawPayment(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof GuiBeacon)) return;
        ItemStack payment = FFDItems.effectiveStack(FFDItems.NETHERITE_INGOT);
        if (payment.isEmpty() || !payment.getItem().isBeaconPayment(payment)) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        int left = (event.getGui().width - 230) / 2;
        int top = (event.getGui().height - 219) / 2;
        GlStateManager.pushAttrib();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(BACKGROUND);
        Gui.drawModalRectWithCustomSizedTexture(left + 36, top + 108, 58, 108, 5, 18, 256, 256);
        RenderHelper.enableGUIStandardItemLighting();
        RenderItem renderer = minecraft.getRenderItem();
        float previousLevel = renderer.zLevel;
        renderer.zLevel = 100.0F;
        renderer.renderItemAndEffectIntoGUI(payment, left + 20, top + 109);
        renderer.zLevel = previousLevel;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popAttrib();
    }
}
