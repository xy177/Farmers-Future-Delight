package xy177.farmersfuturedelight.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.inventory.ContainerLoom;

public class GuiLoom extends GuiContainer {
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation(FarmerFutureDelight.MODID, "textures/gui/container/loom.png");
    private static final ResourceLocation PATTERN =
            new ResourceLocation(FarmerFutureDelight.MODID,
                    "textures/gui/sprites/container/loom/pattern.png");
    private static final ResourceLocation PATTERN_HIGHLIGHTED =
            new ResourceLocation(FarmerFutureDelight.MODID,
                    "textures/gui/sprites/container/loom/pattern_highlighted.png");
    private static final ResourceLocation PATTERN_SELECTED =
            new ResourceLocation(FarmerFutureDelight.MODID,
                    "textures/gui/sprites/container/loom/pattern_selected.png");
    private static final ResourceLocation SCROLLER =
            new ResourceLocation(FarmerFutureDelight.MODID,
                    "textures/gui/sprites/container/loom/scroller.png");
    private static final ResourceLocation SCROLLER_DISABLED =
            new ResourceLocation(FarmerFutureDelight.MODID,
                    "textures/gui/sprites/container/loom/scroller_disabled.png");
    private static final ResourceLocation ERROR =
            new ResourceLocation(FarmerFutureDelight.MODID,
                    "textures/gui/sprites/container/loom/error.png");
    private static final ResourceLocation PATTERN_SLOT =
            new ResourceLocation(FarmerFutureDelight.MODID,
                    "textures/gui/sprites/container/slot/banner_pattern.png");
    private static final ResourceLocation BANNER_SLOT =
            new ResourceLocation(FarmerFutureDelight.MODID,
                    "textures/gui/sprites/container/slot/banner.png");
    private static final ResourceLocation DYE_SLOT =
            new ResourceLocation(FarmerFutureDelight.MODID,
                    "textures/gui/sprites/container/slot/dye.png");
    private static final ResourceLocation[] EMPTY_SLOTS = {BANNER_SLOT, DYE_SLOT, PATTERN_SLOT};
    private static final Map<BannerPattern, ResourceLocation> PATTERN_TEXTURES =
            new EnumMap<>(BannerPattern.class);
    private final ContainerLoom container;
    private int patternOffset;

    public GuiLoom(ContainerLoom container) {
        super(container);
        this.container = container;
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
        fontRenderer.drawString(I18n.format("container.farmers_future_delight.loom"),
                8, 4, 0x404040);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, ySize - 94, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        for (int index = 0; index < EMPTY_SLOTS.length; index++) {
            net.minecraft.inventory.Slot slot = container.getSlot(index);
            if (!slot.getHasStack()) {
                mc.getTextureManager().bindTexture(EMPTY_SLOTS[index]);
                drawModalRectWithCustomSizedTexture(guiLeft + slot.xPos, guiTop + slot.yPos,
                        0, 0, 16, 16, 16, 16);
            }
        }
        drawPatternSelector(mouseX, mouseY);
        ItemStack output = container.getSlot(3).getStack();
        if (!output.isEmpty()) {
            drawBanner(output, guiLeft + 141, guiTop + 8, 20, 40);
        } else if (hasMaxPatterns()) {
            mc.getTextureManager().bindTexture(ERROR);
            drawModalRectWithCustomSizedTexture(guiLeft + 138, guiTop + 52,
                    0, 0, 26, 26, 26, 26);
        }
    }

    private void drawPatternSelector(int mouseX, int mouseY) {
        List<BannerPattern> patterns = container.getSelectablePatterns();
        int scrollRows = Math.max(0, (patterns.size() + 3) / 4 - 4);
        patternOffset = Math.min(patternOffset, scrollRows * 4);
        boolean displayPatterns = displaysPatterns();
        int visibleCount = displayPatterns ? Math.min(16, patterns.size() - patternOffset) : 0;
        int left = guiLeft + 60;
        int top = guiTop + 13;
        for (int visibleIndex = 0; visibleIndex < visibleCount; visibleIndex++) {
            int patternIndex = patternOffset + visibleIndex;
            int x = left + visibleIndex % 4 * 14;
            int y = top + visibleIndex / 4 * 14;
            boolean selected = patternIndex == container.getSelectedIndex();
            boolean hovered = mouseX >= x && mouseX < x + 14
                    && mouseY >= y && mouseY < y + 14;
            ResourceLocation frame = selected ? PATTERN_SELECTED
                    : hovered ? PATTERN_HIGHLIGHTED : PATTERN;
            mc.getTextureManager().bindTexture(frame);
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, 14, 14, 14, 14);
            drawPattern(patterns.get(patternIndex), x + 4, y + 2);
        }
        boolean hasMore = displayPatterns && scrollRows > 0;
        mc.getTextureManager().bindTexture(hasMore ? SCROLLER : SCROLLER_DISABLED);
        int thumbOffset = !hasMore ? 0 : (int) (41.0F * patternOffset / (scrollRows * 4));
        drawModalRectWithCustomSizedTexture(guiLeft + 119, top + thumbOffset,
                0, 0, 12, 15, 12, 15);
    }

    private boolean hasMaxPatterns() {
        NBTTagCompound data = container.getBanner().getSubCompound("BlockEntityTag");
        return net.minecraft.tileentity.TileEntityBanner.getPatterns(container.getBanner())
                >= container.getLayerLimit();
    }

    private boolean displaysPatterns() {
        return !container.getBanner().isEmpty() && !container.getDye().isEmpty()
                && container.getSlot(1).isItemValid(container.getDye()) && !hasMaxPatterns()
                && !container.getSelectablePatterns().isEmpty();
    }

    private void drawPattern(BannerPattern pattern, int x, int y) {
        ResourceLocation texture = PATTERN_TEXTURES.get(pattern);
        if (texture == null) {
            texture = BannerTextures.BANNER_DESIGNS.getResourceLocation(
                    "ffd_loom_" + pattern.getHashname(),
                    Arrays.asList(BannerPattern.BASE, pattern),
                    Arrays.asList(EnumDyeColor.GRAY, EnumDyeColor.WHITE));
            if (texture != null) {
                PATTERN_TEXTURES.put(pattern, texture);
            }
        }
        if (texture != null) {
            mc.getTextureManager().bindTexture(texture);
            drawScaledCustomSizeModalRect(x, y, 0.0F, 1.0F, 21, 40,
                    5, 10, 64.0F, 64.0F);
        }
    }

    private void drawBanner(ItemStack stack, int x, int y, int width, int height) {
        NBTTagCompound blockTag = stack.getSubCompound("BlockEntityTag");
        int baseColor = blockTag != null && blockTag.hasKey("Base", 3)
                ? blockTag.getInteger("Base") : stack.getMetadata();
        List<BannerPattern> patterns = new ArrayList<>();
        List<EnumDyeColor> colors = new ArrayList<>();
        patterns.add(BannerPattern.BASE);
        colors.add(EnumDyeColor.byDyeDamage(baseColor));
        if (blockTag != null) {
            NBTTagList layers = blockTag.getTagList("Patterns", 10);
            for (int index = 0; index < layers.tagCount(); index++) {
                NBTTagCompound layer = layers.getCompoundTagAt(index);
                BannerPattern pattern = BannerPattern.byHash(layer.getString("Pattern"));
                if (pattern != null) {
                    patterns.add(pattern);
                    colors.add(EnumDyeColor.byDyeDamage(layer.getInteger("Color")));
                }
            }
        }
        StringBuilder textureKey = new StringBuilder("ffd_loom_preview_");
        for (int index = 0; index < patterns.size(); index++) {
            textureKey.append(patterns.get(index).getHashname())
                    .append(colors.get(index).getDyeDamage()).append('_');
        }
        ResourceLocation texture = BannerTextures.BANNER_DESIGNS.getResourceLocation(
                textureKey.toString(), patterns, colors);
        if (texture != null) {
            mc.getTextureManager().bindTexture(texture);
            drawScaledCustomSizeModalRect(x, y, 1.0F, 1.0F, 20, 40,
                    width, height, 64.0F, 64.0F);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int listX = mouseX - guiLeft - 60;
        int listY = mouseY - guiTop - 13;
        if (displaysPatterns() && mouseButton == 0 && listX >= 0 && listX < 56 && listY >= 0 && listY < 56) {
            int visibleIndex = listY / 14 * 4 + listX / 14;
            int patternIndex = patternOffset + visibleIndex;
            if (patternIndex < container.getSelectablePatterns().size()) {
                mc.playerController.sendEnchantPacket(container.windowId, patternIndex);
            }
            return;
        }
        if (displaysPatterns() && mouseButton == 0 && listX >= 59 && listX < 71 && listY >= 0 && listY < 56) {
            int maxRow = Math.max(0, (container.getSelectablePatterns().size() + 3) / 4 - 4);
            int row = Math.round((listY - 7) * maxRow / 41.0F);
            patternOffset = Math.max(0, Math.min(maxRow, row)) * 4;
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mouseX = Mouse.getX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;
            int listX = mouseX - guiLeft - 60;
            int listY = mouseY - guiTop - 13;
            if (displaysPatterns() && listX >= 0 && listX < 56 && listY >= 0 && listY < 56) {
                int direction = wheel > 0 ? -1 : 1;
                int maxOffset = Math.max(0, (container.getSelectablePatterns().size() + 3) / 4 - 4) * 4;
                patternOffset = Math.max(0, Math.min(maxOffset, patternOffset + direction * 4));
                return;
            }
        }
        super.handleMouseInput();
    }
}
