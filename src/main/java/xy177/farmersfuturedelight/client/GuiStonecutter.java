package xy177.farmersfuturedelight.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.inventory.ContainerStonecutter;
import xy177.farmersfuturedelight.common.tile.TileEntityStonecutter;

public class GuiStonecutter extends GuiContainer {
    private final InventoryPlayer playerInventory;
    private final TileEntityStonecutter stonecutter;
    private final ResourceLocation background = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/gui/container/stonecutter.png");
    private static final ResourceLocation RECIPE = sprite("recipe");
    private static final ResourceLocation RECIPE_HIGHLIGHTED = sprite("recipe_highlighted");
    private static final ResourceLocation RECIPE_SELECTED = sprite("recipe_selected");
    private static final ResourceLocation SCROLLER = sprite("scroller");
    private static final ResourceLocation SCROLLER_DISABLED = sprite("scroller_disabled");
    private int startIndex;
    private float scrollOffset;
    private boolean scrolling;
    private List<net.minecraft.util.ResourceLocation> displayedRecipes = new ArrayList<>();

    public GuiStonecutter(InventoryPlayer inventory, TileEntityStonecutter stonecutter) {
        super(new ContainerStonecutter(inventory, stonecutter));
        playerInventory = inventory;
        this.stonecutter = ((ContainerStonecutter) inventorySlots).getWorkInventory();
        xSize = 176;
        ySize = 166;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (isRecipeArea(mouseX, mouseY)) {
            ItemStack stack = recipeAt(mouseX, mouseY);
            if (!stack.isEmpty()) {
                renderToolTip(stack, mouseX, mouseY);
            }
        } else {
            renderHoveredToolTip(mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(stonecutter.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        fontRenderer.drawString(playerInventory.getDisplayName().getUnformattedText(), 8,
                ySize - 94, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(background);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
        List<WorkstationRecipes.Recipe> recipes = stonecutter.getMatchingRecipes();
        List<net.minecraft.util.ResourceLocation> recipeIds = new ArrayList<>();
        for (WorkstationRecipes.Recipe recipe : recipes) {
            recipeIds.add(recipe.getId());
        }
        if (!displayedRecipes.equals(recipeIds)) {
            displayedRecipes = recipeIds;
            startIndex = 0;
            scrollOffset = 0.0F;
        }
        int visibleEnd = Math.min(recipes.size(), startIndex + 12);
        for (int index = startIndex; index < visibleEnd; index++) {
            int offset = index - startIndex;
            int x = guiLeft + 52 + offset % 4 * 16;
            int y = guiTop + 15 + offset / 4 * 18;
            boolean selected = index == stonecutter.getSelectedRecipeIndex();
            boolean hovered = mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
            mc.getTextureManager().bindTexture(selected ? RECIPE_SELECTED
                    : hovered ? RECIPE_HIGHLIGHTED : RECIPE);
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 18, 16, 18);
            ItemStack result = recipes.get(index).assemble(
                    java.util.Collections.singletonList(stonecutter.getStackInSlot(0)));
            if (!result.isEmpty()) {
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemAndEffectIntoGUI(result, x, y + 1);
                RenderHelper.disableStandardItemLighting();
            }
        }
        mc.getTextureManager().bindTexture(recipes.size() > 12 ? SCROLLER : SCROLLER_DISABLED);
        int thumbY = guiTop + 15 + Math.round(scrollOffset * 39.0F);
        drawModalRectWithCustomSizedTexture(guiLeft + 119, thumbY, 0, 0, 12, 15, 12, 15);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && isRecipeArea(mouseX, mouseY)) {
            int index = recipeIndexAt(mouseX, mouseY);
            if (index >= 0 && index < stonecutter.getMatchingRecipes().size()) {
                mc.playerController.sendEnchantPacket(inventorySlots.windowId, index);
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(
                        SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return;
            }
        }
        if (mouseButton == 0 && isScrollBar(mouseX, mouseY)
                && stonecutter.getMatchingRecipes().size() > 12) {
            scrolling = true;
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton,
                                  long timeSinceLastClick) {
        if (scrolling && stonecutter.getMatchingRecipes().size() > 12) {
            updateScroll(mouseY);
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        scrolling = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && stonecutter.getMatchingRecipes().size() > 12) {
            scrollOffset = Math.max(0.0F, Math.min(1.0F,
                    scrollOffset - Math.signum(wheel) / (float) getOffscreenRows()));
            updateStartIndex();
        }
    }

    private boolean isRecipeArea(int mouseX, int mouseY) {
        return mouseX >= guiLeft + 52 && mouseX < guiLeft + 116
                && mouseY >= guiTop + 14 && mouseY < guiTop + 68;
    }

    private boolean isScrollBar(int mouseX, int mouseY) {
        return mouseX >= guiLeft + 119 && mouseX < guiLeft + 131
                && mouseY >= guiTop + 14 && mouseY < guiTop + 68;
    }

    private int recipeIndexAt(int mouseX, int mouseY) {
        int column = (mouseX - guiLeft - 52) / 16;
        int row = (mouseY - guiTop - 14) / 18;
        if (column < 0 || column >= 4 || row < 0 || row >= 3) {
            return -1;
        }
        return startIndex + row * 4 + column;
    }

    private static ResourceLocation sprite(String name) {
        return new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/gui/sprites/container/stonecutter/" + name + ".png");
    }

    private ItemStack recipeAt(int mouseX, int mouseY) {
        int index = recipeIndexAt(mouseX, mouseY);
        List<WorkstationRecipes.Recipe> recipes = stonecutter.getMatchingRecipes();
        return index >= 0 && index < recipes.size()
                ? recipes.get(index).assemble(
                java.util.Collections.singletonList(stonecutter.getStackInSlot(0)))
                : ItemStack.EMPTY;
    }

    private int getOffscreenRows() {
        return Math.max(1, (stonecutter.getMatchingRecipes().size() + 3) / 4 - 3);
    }

    private void updateScroll(int mouseY) {
        scrollOffset = Math.max(0.0F, Math.min(1.0F,
                (mouseY - guiTop - 21.5F) / 39.0F));
        updateStartIndex();
    }

    private void updateStartIndex() {
        int offscreenRows = getOffscreenRows();
        startIndex = Math.round(scrollOffset * offscreenRows) * 4;
    }
}
