package xy177.farmersfuturedelight.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public final class GuiModernCreativeInventory extends GuiContainerCreative {
    private static final Field PAGE = ObfuscationReflectionHelper.findField(GuiContainerCreative.class, "tabPage");
    private static final Field PAGES = ObfuscationReflectionHelper.findField(GuiContainerCreative.class, "maxPages");
    private static final Field SELECTED = ObfuscationReflectionHelper.findField(GuiContainerCreative.class, "field_147058_w");
    private static final Method SELECT_TAB = ObfuscationReflectionHelper.findMethod(GuiContainerCreative.class,
            "func_147050_b", void.class, CreativeTabs.class);
    private long layoutGeneration = -1;
    private CreativeTabs selectedTabSnapshot;

    public GuiModernCreativeInventory(EntityPlayer player) {
        super(player);
    }

    public static void restoreSelection(CreativeTabs[] previousTabs) {
        try {
            int selected = SELECTED.getInt(null);
            CreativeTabs tab = selected >= 0 && selected < previousTabs.length ? previousTabs[selected] : null;
            SELECTED.setInt(null, tab == null ? -1 : tab.getTabIndex());
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to preserve creative inventory selection", exception);
        }
    }

    @Override
    public void initGui() {
        try {
            PAGE.setInt(null, Math.max(0, Math.min(page(), FFDCreativeInventory.maxPage())));
            if (!selectedTabVisible()) SELECTED.setInt(null, FFDCreativeInventory.firstTab(page()).getTabIndex());
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to restore creative inventory selection", exception);
        }
        super.initGui();
        selectedTabSnapshot = selectedTab();
        synchronizeLayout();
    }

    @Override
    public void updateScreen() {
        if (layoutGeneration != FFDCreativeInventory.tabSnapshotGeneration()) synchronizeLayout();
        super.updateScreen();
        selectedTabSnapshot = selectedTab();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (layoutGeneration != FFDCreativeInventory.tabSnapshotGeneration()) synchronizeLayout();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private CreativeTabs selectedTab() {
        int selected = getSelectedTabIndex();
        return selected >= 0 && selected < CreativeTabs.CREATIVE_TAB_ARRAY.length
                ? CreativeTabs.CREATIVE_TAB_ARRAY[selected] : null;
    }

    private boolean selectedTabVisible() {
        return FFDCreativeInventory.pageOf(selectedTab()) >= 0;
    }

    private void synchronizeLayout() {
        try {
            int pages = FFDCreativeInventory.maxPage();
            PAGES.setInt(this, pages);
            PAGE.setInt(null, Math.max(0, Math.min(page(), pages)));
            if (!selectedTabVisible()) SELECT_TAB.invoke(this, FFDCreativeInventory.firstTab(page()));
            else if (selectedTabSnapshot != selectedTab()) SELECT_TAB.invoke(this, selectedTab());
            selectedTabSnapshot = selectedTab();
            buttonList.removeIf(button -> button.id == 101 || button.id == 102);
            if (pages > 0) {
                buttonList.add(new GuiButton(101, guiLeft, guiTop - 50, 20, 20, "<"));
                buttonList.add(new GuiButton(102, guiLeft + xSize - 20, guiTop - 50, 20, 20, ">"));
            }
            layoutGeneration = FFDCreativeInventory.tabSnapshotGeneration();
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to synchronize creative inventory pages", exception);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (layoutGeneration != FFDCreativeInventory.tabSnapshotGeneration()) synchronizeLayout();
        super.actionPerformed(button);
    }

    private int page() {
        try {
            return PAGE.getInt(null);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read creative inventory page", exception);
        }
    }

    public static int column(CreativeTabs tab) {
        if (!FFDCreativeInventory.modern() && FFDCreativeInventory.pageOf(tab) == 0) return tab.getTabColumn();
        int index = FFDCreativeInventory.modernIndex(tab);
        if (index >= 0) return index < 5 ? index : index - 5;
        if (tab == CreativeTabs.HOTBAR) return 5;
        if (tab == CreativeTabs.SEARCH || tab == CreativeTabs.INVENTORY) return 6;
        int extra = FFDCreativeInventory.extraIndex(tab);
        return extra < 0 ? -1 : extra % 5;
    }

    public static boolean top(CreativeTabs tab) {
        if (!FFDCreativeInventory.modern() && FFDCreativeInventory.pageOf(tab) == 0) return tab.isTabInFirstRow();
        int index = FFDCreativeInventory.modernIndex(tab);
        if (index >= 0) return index < 5;
        if (tab == CreativeTabs.HOTBAR || tab == CreativeTabs.SEARCH) return true;
        if (tab == CreativeTabs.INVENTORY) return false;
        int extra = FFDCreativeInventory.extraIndex(tab);
        return extra >= 0 && extra % 10 < 5;
    }

    private boolean visible(CreativeTabs tab) {
        return FFDCreativeInventory.pageOf(tab) >= 0 && (tab == CreativeTabs.SEARCH || tab == CreativeTabs.INVENTORY
                || FFDCreativeInventory.pageOf(tab) == page());
    }

    private int tabX(CreativeTabs tab) {
        int column = column(tab);
        if (!FFDCreativeInventory.modern()) {
            return tab.isAlignedRight() ? xSize - 28 * (6 - column) + 2 : column * 29;
        }
        return column >= 5 ? xSize - 27 * (7 - column) + 1 : column * 27;
    }

    @Override
    protected boolean isMouseOverTab(CreativeTabs tab, int mouseX, int mouseY) {
        if (!visible(tab)) return false;
        int x = tabX(tab);
        int y = top(tab) ? -32 : ySize;
        return mouseX >= x && mouseX <= x + (FFDCreativeInventory.modern() ? 26 : 28)
                && mouseY >= y && mouseY <= y + 32;
    }

    @Override
    protected boolean renderCreativeInventoryHoveringText(CreativeTabs tab, int mouseX, int mouseY) {
        if (!visible(tab)) return false;
        int x = tabX(tab);
        int y = top(tab) ? -32 : ySize;
        if (!isPointInRegion(x + 3, y + 3, FFDCreativeInventory.modern() ? 21 : 23, 27, mouseX, mouseY)) return false;
        drawHoveringText(net.minecraft.client.resources.I18n.format(tab.getTranslatedTabLabel()), mouseX, mouseY);
        return true;
    }

    @Override
    protected void drawTab(CreativeTabs tab) {
        if (!visible(tab)) return;
        boolean selected = tab.getTabIndex() == getSelectedTabIndex();
        boolean top = top(tab);
        int column = column(tab);
        boolean modern = FFDCreativeInventory.modern();
        int x = guiLeft + tabX(tab) - (!modern && tab.isAlignedRight() ? 2 : 0);
        int y = guiTop + (top ? -28 : ySize - 4);
        int textureX = (modern ? column == 0 ? 0 : column == 6 ? 5 : 1 : column) * 28;
        int textureY = (selected ? 32 : 0) + (top ? 0 : 64);
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(x, y, textureX, textureY, 28, 32, modern ? 26 : 28, 32, 256, 256);
        zLevel = 100;
        itemRender.zLevel = 100;
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        ItemStack icon = tab.getIconItemStack();
        itemRender.renderItemAndEffectIntoGUI(icon, x + (modern ? 5 : 6), y + (top ? 9 : 7));
        itemRender.renderItemOverlays(fontRenderer, icon, x + (modern ? 5 : 6), y + (top ? 9 : 7));
        GlStateManager.disableLighting();
        itemRender.zLevel = 0;
        zLevel = 0;
    }
}
