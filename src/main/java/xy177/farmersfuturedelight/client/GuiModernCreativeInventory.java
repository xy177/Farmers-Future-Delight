package xy177.farmersfuturedelight.client;

import java.lang.reflect.Field;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public final class GuiModernCreativeInventory extends GuiContainerCreative {
    private static final Field PAGE = ObfuscationReflectionHelper.findField(GuiContainerCreative.class, "tabPage");
    private static final Field PAGES = ObfuscationReflectionHelper.findField(GuiContainerCreative.class, "maxPages");

    public GuiModernCreativeInventory(EntityPlayer player) {
        super(player);
    }

    @Override
    public void initGui() {
        super.initGui();
        try {
            int pages = (FFDCreativeInventory.extraTabs().size() + 9) / 10;
            PAGES.setInt(this, pages);
            if (PAGE.getInt(null) > pages) PAGE.setInt(null, pages);
            if (pages == 0) buttonList.removeIf(button -> button.id == 101 || button.id == 102);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to initialize creative inventory pages", exception);
        }
    }

    private int page() {
        try {
            return PAGE.getInt(null);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read creative inventory page", exception);
        }
    }

    public static int column(CreativeTabs tab) {
        int index = FFDCreativeInventory.modernIndex(tab);
        if (index >= 0) return index < 5 ? index : index - 5;
        if (tab == CreativeTabs.HOTBAR) return 5;
        if (tab == CreativeTabs.SEARCH || tab == CreativeTabs.INVENTORY) return 6;
        return Math.max(0, FFDCreativeInventory.extraTabs().indexOf(tab)) % 5;
    }

    public static boolean top(CreativeTabs tab) {
        int index = FFDCreativeInventory.modernIndex(tab);
        if (index >= 0) return index < 5;
        if (tab == CreativeTabs.HOTBAR || tab == CreativeTabs.SEARCH) return true;
        if (tab == CreativeTabs.INVENTORY) return false;
        return Math.max(0, FFDCreativeInventory.extraTabs().indexOf(tab)) % 10 < 5;
    }

    private boolean visible(CreativeTabs tab) {
        return tab != null && (tab == CreativeTabs.SEARCH || tab == CreativeTabs.INVENTORY
                || tab.getTabPage() == page());
    }

    private int tabX(CreativeTabs tab) {
        int column = column(tab);
        return column >= 5 ? xSize - 27 * (7 - column) + 1 : column * 27;
    }

    @Override
    protected boolean isMouseOverTab(CreativeTabs tab, int mouseX, int mouseY) {
        if (!visible(tab)) return false;
        int x = tabX(tab);
        int y = top(tab) ? -32 : ySize;
        return mouseX >= x && mouseX <= x + 26 && mouseY >= y && mouseY <= y + 32;
    }

    @Override
    protected boolean renderCreativeInventoryHoveringText(CreativeTabs tab, int mouseX, int mouseY) {
        if (!visible(tab)) return false;
        int x = tabX(tab);
        int y = top(tab) ? -32 : ySize;
        if (!isPointInRegion(x + 3, y + 3, 21, 27, mouseX, mouseY)) return false;
        drawHoveringText(net.minecraft.client.resources.I18n.format(tab.getTranslatedTabLabel()), mouseX, mouseY);
        return true;
    }

    @Override
    protected void drawTab(CreativeTabs tab) {
        if (!visible(tab)) return;
        boolean selected = tab.getTabIndex() == getSelectedTabIndex();
        boolean top = top(tab);
        int column = column(tab);
        int x = guiLeft + tabX(tab);
        int y = guiTop + (top ? -28 : ySize - 4);
        int textureX = (column == 0 ? 0 : column == 6 ? 5 : 1) * 28;
        int textureY = (selected ? 32 : 0) + (top ? 0 : 64);
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(x, y, textureX, textureY, 28, 32, 26, 32, 256, 256);
        zLevel = 100;
        itemRender.zLevel = 100;
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        ItemStack icon = tab.getIconItemStack();
        itemRender.renderItemAndEffectIntoGUI(icon, x + 5, y + (top ? 9 : 7));
        itemRender.renderItemOverlays(fontRenderer, icon, x + 5, y + (top ? 9 : 7));
        GlStateManager.disableLighting();
        itemRender.zLevel = 0;
        zLevel = 0;
    }
}
