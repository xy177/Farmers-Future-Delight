package xy177.farmersfuturedelight.client;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IMerchant;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.lwjgl.input.Mouse;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.network.PacketMerchantState;

public final class GuiModernMerchant extends GuiContainer {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(FarmerFutureDelight.MODID,
            "textures/gui/container/modern_villager.png");
    private static final int[] LEVEL_XP = {0, 10, 70, 150, 250};
    private final IMerchant merchant;
    private PacketMerchantState state;
    private MerchantRecipeList offers;
    private int selection;
    private int scroll;
    private boolean dragging;

    private GuiModernMerchant(GuiMerchant original, PacketMerchantState state) {
        super(original.inventorySlots);
        merchant = original.getMerchant();
        xSize = 276;
        ySize = 166;
        for (int index = 0; index < inventorySlots.inventorySlots.size(); index++) {
            Slot slot = inventorySlots.inventorySlots.get(index);
            slot.xPos += 100;
            if (index < 3) slot.yPos -= 16;
        }
        update(state);
    }

    public static void receive(PacketMerchantState packet) {
        Minecraft client = Minecraft.getMinecraft();
        if (client.player == null || client.player.openContainer.windowId != packet.getWindowId()) return;
        if (client.currentScreen instanceof GuiModernMerchant) {
            ((GuiModernMerchant) client.currentScreen).update(packet);
        } else if (client.currentScreen != null && client.currentScreen.getClass() == GuiMerchant.class) {
            GuiModernMerchant screen = new GuiModernMerchant((GuiMerchant) client.currentScreen, packet);
            client.displayGuiScreen(screen);
            screen.merchant.setCustomer(client.player);
        }
    }

    private void update(PacketMerchantState packet) {
        state = packet;
        offers = new MerchantRecipeList(packet.getOffers());
        merchant.setRecipes(offers);
        selection = MathHelper.clamp(selection, 0, Math.max(0, offers.size() - 1));
        scroll = MathHelper.clamp(scroll, 0, Math.max(0, offers.size() - 7));
        ((ContainerMerchant) inventorySlots).setCurrentRecipeIndex(selection);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        int localX = mouseX - guiLeft;
        int localY = mouseY - guiTop;
        int row = (localY - 18) / 20;
        if (localY >= 18 && row < 7 && row + scroll < offers.size()) {
            MerchantRecipe offer = offers.get(row + scroll);
            ItemStack hovered = localX >= 10 && localX < 35 ? offer.getItemToBuy()
                    : localX >= 40 && localX < 57 ? offer.getSecondItemToBuy()
                    : localX >= 73 && localX < 90 ? offer.getItemToSell() : ItemStack.EMPTY;
            if (!hovered.isEmpty()) renderToolTip(hovered, mouseX, mouseY);
        }
        if (!offers.isEmpty() && offers.get(selection).isRecipeDisabled() && state.showsProgress()
                && localX >= 186 && localX < 208 && localY >= 35 && localY < 56) {
            drawHoveringText(I18n.format("ffd.merchant.deprecated"), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = merchant.getDisplayName().getUnformattedText();
        if (state.showsProgress() && state.getLevel() >= 1 && state.getLevel() <= 5) {
            title = I18n.format("ffd.merchant.title", title, I18n.format("ffd.merchant.level." + state.getLevel()));
        }
        fontRenderer.drawString(title, 187 - fontRenderer.getStringWidth(title) / 2, 6, 4210752);
        String trades = I18n.format("ffd.merchant.trades");
        fontRenderer.drawString(trades, 48 - fontRenderer.getStringWidth(trades) / 2, 6, 4210752);
        fontRenderer.drawString(I18n.format("container.inventory"), 107, 72, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 512, 256);
        boolean canScroll = offers.size() > 7;
        int handle = canScroll ? (int) (113.0F * scroll / (offers.size() - 7)) : 0;
        sprite(canScroll ? "scroller" : "scroller_disabled", guiLeft + 94, guiTop + 18 + handle, 6, 27);
        if (!offers.isEmpty() && offers.get(selection).isRecipeDisabled()) {
            sprite("out_of_stock", guiLeft + 182, guiTop + 35, 28, 21);
        }
        drawExperience();
        NBTTagList metadata = state.getOffers().getTagList("Recipes", 10);
        for (int row = 0; row < 7 && row + scroll < offers.size(); row++) {
            int top = guiTop + 18 + row * 20;
            boolean hovered = mouseX >= guiLeft + 5 && mouseX < guiLeft + 93
                    && mouseY >= top && mouseY < top + 20;
            drawButton(guiLeft + 5, top, hovered);
            MerchantRecipe offer = offers.get(row + scroll);
            ItemStack cost = offer.getItemToBuy();
            ItemStack base = cost.copy();
            base.setCount(metadata.getCompoundTagAt(row + scroll).getInteger("ffdBaseCost"));
            int itemY = top + 1;
            RenderHelper.enableGUIStandardItemLighting();
            itemRender.renderItemAndEffectIntoGUI(cost, guiLeft + 10, itemY);
            if (base.getCount() == cost.getCount()) {
                itemRender.renderItemOverlayIntoGUI(fontRenderer, cost, guiLeft + 10, itemY, null);
            } else {
                itemRender.renderItemOverlayIntoGUI(fontRenderer, base, guiLeft + 10, itemY,
                        base.getCount() == 1 ? "1" : null);
                itemRender.renderItemOverlayIntoGUI(fontRenderer, cost, guiLeft + 24, itemY,
                        cost.getCount() == 1 ? "1" : null);
            }
            renderStack(offer.getSecondItemToBuy(), guiLeft + 40, itemY);
            renderStack(offer.getItemToSell(), guiLeft + 73, itemY);
            RenderHelper.disableStandardItemLighting();
            if (base.getCount() != cost.getCount()) {
                sprite("discount_strikethrough", guiLeft + 17, itemY + 12, 9, 2);
            }
            sprite(offer.isRecipeDisabled() ? "trade_arrow_out_of_stock" : "trade_arrow",
                    guiLeft + 60, itemY + 3, 10, 9);
        }
    }

    private void renderStack(ItemStack stack, int left, int top) {
        if (stack.isEmpty()) return;
        itemRender.renderItemAndEffectIntoGUI(stack, left, top);
        itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, left, top, null);
    }

    private void drawExperience() {
        int level = state.getLevel();
        if (!state.showsProgress() || level < 1 || level >= 5 || offers.isEmpty()) return;
        int left = guiLeft + 136;
        int top = guiTop + 16;
        sprite("experience_bar_background", left, top, 102, 5);
        float scale = 102.0F / (LEVEL_XP[level] - LEVEL_XP[level - 1]);
        int width = MathHelper.clamp(MathHelper.floor((state.getExperience() - LEVEL_XP[level - 1]) * scale), 0, 102);
        bindSprite("experience_bar_current");
        drawModalRectWithCustomSizedTexture(left, top, 0, 0, width, 5, 102, 5);
        int gain = inventorySlots.getSlot(2).getHasStack()
                ? state.getOffers().getTagList("Recipes", 10).getCompoundTagAt(selection).getInteger("ffdExperience") : 0;
        int future = Math.min(102 - width, MathHelper.floor(gain * scale));
        if (future > 0) {
            bindSprite("experience_bar_result");
            drawModalRectWithCustomSizedTexture(left + width, top, width, 0, future, 5, 102, 5);
        }
    }

    private void drawButton(int left, int top, boolean hovered) {
        mc.getTextureManager().bindTexture(new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/gui/sprites/widget/button" + (hovered ? "_highlighted" : "") + ".png"));
        drawModalRectWithCustomSizedTexture(left, top, 0, 0, 44, 20, 200, 20);
        drawModalRectWithCustomSizedTexture(left + 44, top, 156, 0, 44, 20, 200, 20);
    }

    private void sprite(String name, int left, int top, int width, int height) {
        bindSprite(name);
        drawModalRectWithCustomSizedTexture(left, top, 0, 0, width, height, width, height);
    }

    private void bindSprite(String name) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/gui/sprites/container/villager/" + name + ".png"));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int left = mouseX - guiLeft;
        int top = mouseY - guiTop;
        if (mouseButton == 0 && left >= 5 && left < 93 && top >= 18 && top < 158) {
            int index = (top - 18) / 20 + scroll;
            if (index < offers.size()) {
                selection = index;
                ((ContainerMerchant) inventorySlots).setCurrentRecipeIndex(index);
                FFDNetwork.selectMerchantTrade(inventorySlots.windowId, index);
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(
                        net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return;
        }
        if (mouseButton == 0 && offers.size() > 7 && left >= 94 && left < 100 && top >= 18 && top <= 158) {
            dragging = true;
            moveScroller(mouseY);
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) moveScroller(mouseY);
        else super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    private void moveScroller(int mouseY) {
        scroll = MathHelper.clamp(Math.round((mouseY - guiTop - 18 - 13.5F) / 113.0F
                * (offers.size() - 7)), 0, Math.max(0, offers.size() - 7));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && offers.size() > 7) {
            scroll = MathHelper.clamp(scroll - Integer.signum(wheel), 0, offers.size() - 7);
        }
    }
}
