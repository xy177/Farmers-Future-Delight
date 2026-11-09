package xy177.farmersfuturedelight.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class CookingRecipeToast implements IToast {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(FarmerFutureDelight.MODID,
            "textures/gui/sprites/toast/recipe.png");
    private final List<ItemStack> stations = new ArrayList<>();
    private final List<ItemStack> outputs = new ArrayList<>();
    private long lastChanged;
    private boolean changed;

    public static void receive(NBTTagCompound data) {
        GuiToast manager = Minecraft.getMinecraft().getToastGui();
        CookingRecipeToast toast = manager.getToast(CookingRecipeToast.class, NO_TOKEN);
        NBTTagList entries = data.getTagList("Recipes", 10);
        for (int index = 0; index < entries.tagCount(); index++) {
            NBTTagCompound entry = entries.getCompoundTagAt(index);
            ItemStack output = new ItemStack(entry.getCompoundTag("Output"));
            if (output.isEmpty()) continue;
            if (toast == null) {
                toast = new CookingRecipeToast();
                manager.add(toast);
            }
            toast.stations.add(new ItemStack(entry.getCompoundTag("Station")));
            toast.outputs.add(output);
            toast.changed = true;
        }
    }

    @Override
    public Visibility draw(GuiToast manager, long elapsed) {
        if (changed) {
            lastChanged = elapsed;
            changed = false;
        }
        if (outputs.isEmpty()) return Visibility.HIDE;
        Minecraft minecraft = manager.getMinecraft();
        minecraft.getTextureManager().bindTexture(BACKGROUND);
        GlStateManager.color(1, 1, 1, 1);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);
        minecraft.fontRenderer.drawString(I18n.format("gui.farmers_future_delight.recipe_toast.title"), 30, 7, -11534256);
        minecraft.fontRenderer.drawString(I18n.format("gui.farmers_future_delight.recipe_toast.description"), 30, 18, -16777216);
        int index = (int) (elapsed / Math.max(1.0D, 5000.0D / outputs.size()) % outputs.size());
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.6F, 0.6F, 1);
        minecraft.getRenderItem().renderItemAndEffectIntoGUI(stations.get(index), 3, 3);
        GlStateManager.popMatrix();
        minecraft.getRenderItem().renderItemAndEffectIntoGUI(outputs.get(index), 8, 8);
        return elapsed - lastChanged >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }
}
