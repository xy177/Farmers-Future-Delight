package xy177.farmersfuturedelight.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.inventory.ContainerGrindstone;
import xy177.farmersfuturedelight.common.tile.TileEntityGrindstone;

public class GuiGrindstone extends GuiContainer {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/gui/container/grindstone.png");
    private static final ResourceLocation ERROR = new ResourceLocation(FarmerFutureDelight.MODID,
            "textures/gui/sprites/container/grindstone/error.png");
    private final InventoryPlayer playerInventory;
    private final TileEntityGrindstone grindstone;

    public GuiGrindstone(InventoryPlayer playerInventory, TileEntityGrindstone grindstone) {
        super(new ContainerGrindstone(playerInventory, grindstone));
        this.playerInventory = playerInventory;
        this.grindstone = ((ContainerGrindstone) inventorySlots).getWorkInventory();
        xSize = 176;
        ySize = 166;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(grindstone.getDisplayName().getUnformattedText(), 8, 6,
                4210752);
        fontRenderer.drawString(playerInventory.getDisplayName().getUnformattedText(), 8,
                ySize - 94, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
        if ((!grindstone.getStackInSlot(0).isEmpty() || !grindstone.getStackInSlot(1).isEmpty())
                && inventorySlots.getSlot(2).getStack().isEmpty()) {
            mc.getTextureManager().bindTexture(ERROR);
            drawModalRectWithCustomSizedTexture(guiLeft + 92, guiTop + 31, 0, 0,
                    28, 21, 28, 21);
        }
    }
}
