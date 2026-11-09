package xy177.farmersfuturedelight.common.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class PotionRaidEffect extends Potion {
    private final ResourceLocation icon;

    public PotionRaidEffect(String name, int color, boolean beneficial) {
        super(false, color);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setPotionName("effect." + FarmerFutureDelight.MODID + "." + name);
        if (beneficial) setBeneficial();
        icon = new ResourceLocation(FarmerFutureDelight.MODID, "textures/mob_effect/" + name + ".png");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) {
        drawIcon(x + 6, y + 7, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        drawIcon(x + 3, y + 3, alpha);
    }

    @SideOnly(Side.CLIENT)
    private void drawIcon(int x, int y, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(icon);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 18, 18, 18, 18);
    }
}
