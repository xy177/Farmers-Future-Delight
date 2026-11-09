package xy177.farmersfuturedelight.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.inventory.ContainerCartographyTable;
import xy177.farmersfuturedelight.common.tile.TileEntityCartographyTable;

public class GuiCartographyTable extends GuiContainer {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/gui/container/cartography_table.png");
    private static final ResourceLocation ERROR = sprite("cartography_table/error");
    private static final ResourceLocation SCALED_MAP = sprite("cartography_table/scaled_map");
    private static final ResourceLocation DUPLICATED_MAP = sprite("cartography_table/duplicated_map");
    private static final ResourceLocation MAP = sprite("cartography_table/map");
    private static final ResourceLocation LOCKED = sprite("cartography_table/locked");
    private final InventoryPlayer playerInventory;
    private final TileEntityCartographyTable table;

    public GuiCartographyTable(InventoryPlayer playerInventory,
                              TileEntityCartographyTable table) {
        super(new ContainerCartographyTable(playerInventory, table));
        this.playerInventory = playerInventory;
        this.table = ((ContainerCartographyTable) inventorySlots).getWorkInventory();
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
        fontRenderer.drawString(table.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        fontRenderer.drawString(playerInventory.getDisplayName().getUnformattedText(), 8,
                ySize - 94, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
        drawMapPreview();
    }

    private void drawMapPreview() {
        MapData mapData = table.getMapData();
        ItemStack additional = table.getStackInSlot(1);
        int operation = table.getOperation();
        if ((additional.getItem() == Items.PAPER || additional.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.GLASS_PANE))
                && operation == TileEntityCartographyTable.NONE) {
            drawSprite(ERROR, 35, 31, 28, 21);
        }
        if (operation == TileEntityCartographyTable.SCALE) {
            drawSprite(SCALED_MAP, 67, 13, 66, 66);
            drawMap(mapData, 85, 31, 0.226F);
        } else if (operation == TileEntityCartographyTable.DUPLICATE) {
            drawSprite(DUPLICATED_MAP, 83, 13, 50, 66);
            drawMap(mapData, 86, 16, 0.34F);
            drawSprite(DUPLICATED_MAP, 67, 29, 50, 66);
            drawMap(mapData, 70, 32, 0.34F);
        } else if (operation == TileEntityCartographyTable.LOCK) {
            drawSprite(MAP, 67, 13, 66, 66);
            drawMap(mapData, 71, 17, 0.45F);
            drawSprite(LOCKED, 118, 60, 10, 14);
        } else {
            drawSprite(MAP, 67, 13, 66, 66);
            drawMap(mapData, 71, 17, 0.45F);
        }
    }

    private void drawMap(MapData mapData, int x, int y, float scale) {
        if (mapData == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft + x, guiTop + y, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        GlStateManager.disableDepth();
        mc.entityRenderer.getMapItemRenderer().renderMap(mapData, true);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void drawSprite(ResourceLocation texture, int x, int y, int width, int height) {
        mc.getTextureManager().bindTexture(texture);
        drawModalRectWithCustomSizedTexture(guiLeft + x, guiTop + y, 0, 0,
                width, height, width, height);
        mc.getTextureManager().bindTexture(BACKGROUND);
    }

    private static ResourceLocation sprite(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/gui/sprites/container/" + path + ".png");
    }
}
