package xy177.farmersfuturedelight.client;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.inventory.ContainerCookingFurnace;
import xy177.farmersfuturedelight.common.tile.TileEntityCookingFurnace;

public class GuiCookingFurnace extends GuiContainer {
    private final InventoryPlayer playerInventory;
    private final TileEntityCookingFurnace furnace;
    private final ResourceLocation background;
    private final ResourceLocation flame;
    private final ResourceLocation progress;
    private final CookingRecipeBook recipeBook;

    public GuiCookingFurnace(InventoryPlayer inventory, TileEntityCookingFurnace furnace) {
        super(new ContainerCookingFurnace(inventory, furnace));
        playerInventory = inventory;
        this.furnace = furnace;
        recipeBook = new CookingRecipeBook(furnace);
        String name = furnace.isSmoking() ? "smoker" : "blast_furnace";
        background = texture("container/" + name + ".png");
        flame = texture("sprites/container/" + name + "/lit_progress.png");
        progress = texture("sprites/container/" + name + "/burn_progress.png");
    }

    @Override
    public void initGui() {
        super.initGui();
        recipeBook.init(width, height, inventorySlots.windowId);
        guiLeft = recipeBook.left(width, xSize);
        recipeBook.request();
    }

    public static void receiveBook(NBTTagCompound data) {
        if ("unlock".equals(data.getString("Action"))) {
            CookingRecipeToast.receive(data);
            return;
        }
        if (Minecraft.getMinecraft().currentScreen instanceof GuiCookingFurnace) {
            GuiCookingFurnace screen = (GuiCookingFurnace) Minecraft.getMinecraft().currentScreen;
            screen.recipeBook.setWindow(screen.inventorySlots.windowId);
            screen.recipeBook.apply(data);
            screen.guiLeft = screen.recipeBook.left(screen.width, screen.xSize);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        recipeBook.setWindow(inventorySlots.windowId);
        recipeBook.tick();
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/gui/" + path);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        if (!recipeBook.hidesContainer()) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            recipeBook.renderGhost(guiLeft, guiTop);
        } else {
            drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        }
        recipeBook.render(mouseX, mouseY, guiLeft + 20, height / 2 - 49, partialTicks);
        ItemStack tooltip = recipeBook.tooltip(mouseX, mouseY, guiLeft, guiTop);
        if (!tooltip.isEmpty()) {
            java.util.List<String> lines = getItemToolTip(tooltip);
            if (recipeBook.hasMoreRecipes(mouseX, mouseY)) lines.add(
                    net.minecraft.client.resources.I18n.format("gui.recipebook.moreRecipes"));
            drawHoveringText(lines, mouseX, mouseY);
        }
        else if (!recipeBook.hidesContainer() && !recipeBook.contains(mouseX, mouseY)) renderHoveredToolTip(mouseX, mouseY);
        String filter = recipeBook.filterTooltip(mouseX, mouseY);
        if (!filter.isEmpty()) drawHoveringText(filter, mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (recipeBook.click(mouseX, mouseY, button, guiLeft + 20, height / 2 - 49)) {
            guiLeft = recipeBook.left(width, xSize);
            return;
        }
        if (!recipeBook.hidesContainer()) super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char character, int key) throws IOException {
        if (recipeBook.key(character, key)) {
            guiLeft = recipeBook.left(width, xSize);
            return;
        }
        super.keyTyped(character, key);
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int button, ClickType type) {
        super.handleMouseClick(slot, slotId, button, type);
        if (slot != null && slot.slotNumber < 3) recipeBook.clearGhost();
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int left, int top) {
        return !recipeBook.contains(mouseX, mouseY) && super.hasClickedOutside(mouseX, mouseY, left, top);
    }

    @Override
    protected boolean isPointInRegion(int left, int top, int width, int height, int mouseX, int mouseY) {
        return !recipeBook.hidesContainer() && super.isPointInRegion(left, top, width, height, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = furnace.getDisplayName().getUnformattedText();
        fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 6, 4210752);
        fontRenderer.drawString(playerInventory.getDisplayName().getUnformattedText(), 8, ySize - 94, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(background);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
        int fuel = furnace.getField(1);
        if (fuel > 0 && furnace.getField(0) > 0) {
            int height = Math.min(14, (int) Math.ceil(13.0D * furnace.getField(0) / fuel) + 1);
            mc.getTextureManager().bindTexture(flame);
            drawModalRectWithCustomSizedTexture(guiLeft + 56, guiTop + 50 - height,
                    0, 14 - height, 14, height, 14, 14);
        }
        int total = furnace.getField(3);
        int width = total > 0 ? Math.min(24, (int) Math.ceil(24.0D * furnace.getField(2) / total)) : 0;
        if (width > 0) {
            mc.getTextureManager().bindTexture(progress);
            drawModalRectWithCustomSizedTexture(guiLeft + 79, guiTop + 34,
                    0, 0, width, 16, 24, 16);
        }
    }
}
