package xy177.farmersfuturedelight.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.tile.TileEntityCookingFurnace;

public final class CookingRecipeBook extends Gui {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final TileEntityCookingFurnace furnace;
    private final List<Entry> recipes = new ArrayList<>();
    private final List<Collection> visible = new ArrayList<>();
    private final List<Entry> overlay = new ArrayList<>();
    private final List<ItemStack> fuels = new ArrayList<>();
    private final Set<String> shown = new HashSet<>();
    private final Map<String, Float> animations = new LinkedHashMap<>();
    private GuiTextField search;
    private int window;
    private int panelX;
    private int panelY;
    private int page;
    private int category;
    private int ticks;
    private int overlayX;
    private int overlayY;
    private boolean open;
    private boolean filtering;
    private boolean narrow;
    private String ghost = "";

    public CookingRecipeBook(TileEntityCookingFurnace furnace) {
        this.furnace = furnace;
    }

    public void init(int width, int height, int window) {
        this.window = window;
        narrow = width < 379;
        panelX = (width - 147) / 2 - (narrow ? 0 : 86);
        panelY = (height - 166) / 2;
        String previous = search == null ? "" : search.getText();
        search = new GuiTextField(0, minecraft.fontRenderer, panelX + 29, panelY + 16, 73, 8);
        search.setMaxStringLength(50);
        search.setEnableBackgroundDrawing(false);
        search.setTextColor(0xFFFFFF);
        search.setText(previous);
    }

    public int left(int width, int imageWidth) {
        return open && !narrow ? 177 + (width - imageWidth - 200) / 2 : (width - imageWidth) / 2;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public boolean hidesContainer() {
        return narrow && open;
    }

    public void request() {
        send("request");
    }

    public void apply(NBTTagCompound data) {
        if (data.getInteger("Window") != window) return;
        open = data.getBoolean("Open");
        filtering = data.getBoolean("Filter");
        ghost = data.getString("Ghost");
        recipes.clear();
        NBTTagList entries = data.getTagList("Recipes", 10);
        for (int index = 0; index < entries.tagCount(); index++) {
            Entry entry = new Entry(entries.getCompoundTagAt(index));
            if (!entry.inputs.isEmpty()) recipes.add(entry);
        }
        shown.removeIf(id -> recipes.stream().noneMatch(entry -> entry.id.equals(id) && entry.highlight));
        refresh(false);
    }

    public void tick() {
        ++ticks;
        if (search != null) search.updateCursorCounter();
    }

    private void refresh(boolean resetPage) {
        visible.clear();
        overlay.clear();
        Map<String, Collection> grouped = new LinkedHashMap<>();
        if (category > 0 && !hasCategory(category)) category = 0;
        String query = search == null ? "" : search.getText().toLowerCase(Locale.ROOT);
        for (Entry entry : recipes) {
            if (!furnace.isSmoking() && category > 0 && entry.category != category) continue;
            String key = entry.group.isEmpty() ? "recipe:" + entry.id : "group:" + entry.category + ":" + entry.group;
            grouped.computeIfAbsent(key, ignored -> new Collection()).entries.add(entry);
        }
        for (Collection collection : grouped.values()) {
            boolean matches = query.isEmpty();
            for (Entry entry : collection.entries) {
                for (ItemStack output : entry.outputs) {
                    if (output.getDisplayName().toLowerCase(Locale.ROOT).contains(query)
                            || output.getItem().getRegistryName().toString().contains(query)) matches = true;
                }
            }
            if (filtering) collection.entries.removeIf(entry -> !entry.craftable);
            if (matches && !collection.entries.isEmpty()) visible.add(collection);
        }
        page = resetPage ? 0 : Math.min(page, Math.max(0, (visible.size() - 1) / 20));
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/gui/" + path);
    }

    private void sprite(String name, int left, int top, int width, int height) {
        GlStateManager.color(1, 1, 1, 1);
        minecraft.getTextureManager().bindTexture(texture("sprites/recipe_book/" + name + ".png"));
        drawModalRectWithCustomSizedTexture(left, top, 0, 0, width, height, width, height);
    }

    private void nineSlice(String path, int left, int top, int width, int height,
                           int sourceWidth, int sourceHeight, int border) {
        GlStateManager.color(1, 1, 1, 1);
        minecraft.getTextureManager().bindTexture(texture("sprites/" + path + ".png"));
        for (int row = 0; row < 3; row++) {
            int sourceTop = row == 0 ? 0 : row == 1 ? border : sourceHeight - border;
            int targetTop = top + (row == 0 ? 0 : row == 1 ? border : height - border);
            int targetHeight = row == 1 ? height - border * 2 : border;
            int tileHeight = row == 1 ? sourceHeight - border * 2 : border;
            for (int column = 0; column < 3; column++) {
                int sourceLeft = column == 0 ? 0 : column == 1 ? border : sourceWidth - border;
                int targetLeft = left + (column == 0 ? 0 : column == 1 ? border : width - border);
                int targetWidth = column == 1 ? width - border * 2 : border;
                int tileWidth = column == 1 ? sourceWidth - border * 2 : border;
                for (int offsetY = 0; offsetY < targetHeight; offsetY += tileHeight) {
                    for (int offsetX = 0; offsetX < targetWidth; offsetX += tileWidth) {
                        drawModalRectWithCustomSizedTexture(targetLeft + offsetX, targetTop + offsetY,
                                sourceLeft, sourceTop, Math.min(tileWidth, targetWidth - offsetX),
                                Math.min(tileHeight, targetHeight - offsetY), sourceWidth, sourceHeight);
                    }
                }
            }
        }
    }

    private boolean within(int mouseX, int mouseY, int left, int top, int width, int height) {
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    public void render(int mouseX, int mouseY, int buttonX, int buttonY, float partialTicks) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        if (!hidesContainer()) sprite(within(mouseX, mouseY, buttonX, buttonY, 20, 18)
                ? "button_highlighted" : "button", buttonX, buttonY, 20, 18);
        if (!open) {
            GlStateManager.enableDepth();
            return;
        }
        GlStateManager.color(1, 1, 1, 1);
        minecraft.getTextureManager().bindTexture(texture("recipe_book.png"));
        drawModalRectWithCustomSizedTexture(panelX, panelY, 1, 1, 147, 166, 256, 256);
        nineSlice("widget/text_field" + (search.isFocused() ? "_highlighted" : ""),
                panelX + 25, panelY + 13, 81, 14, 200, 20, 1);
        search.drawTextBox();
        if (search.getText().isEmpty() && !search.isFocused()) {
            minecraft.fontRenderer.drawStringWithShadow(net.minecraft.util.text.TextFormatting.ITALIC
                    + I18n.format("gui.recipebook.search_hint"), panelX + 29, panelY + 16, 0x808080);
        }
        boolean filterHovered = within(mouseX, mouseY, panelX + 110, panelY + 12, 26, 16);
        sprite("furnace_filter_" + (filtering ? "enabled" : "disabled") + (filterHovered ? "_highlighted" : ""),
                panelX + 110, panelY + 12, 26, 16);
        List<Integer> tabs = tabs();
        for (int tab = 0; tab < tabs.size(); tab++) {
            int index = tabs.get(tab);
            int tabY = panelY + 3 + 27 * tab;
            int iconOffset = index == category ? -2 : 0;
            sprite(index == category ? "tab_selected" : "tab", panelX - (index == category ? 32 : 30), tabY, 35, 27);
            RenderHelper.enableGUIStandardItemLighting();
            if (index == 0) minecraft.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.COMPASS), panelX - 21 + iconOffset, tabY + 5);
            else if (furnace.isSmoking()) minecraft.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.PORKCHOP), panelX - 21 + iconOffset, tabY + 5);
            else if (index == 1) minecraft.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(net.minecraft.init.Blocks.REDSTONE_ORE), panelX - 21 + iconOffset, tabY + 5);
            else {
                minecraft.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.IRON_SHOVEL), panelX - 27 + iconOffset, tabY + 5);
                minecraft.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.GOLDEN_LEGGINGS), panelX - 16 + iconOffset, tabY + 5);
            }
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableDepth();
        }
        NBTTagList seen = new NBTTagList();
        float now = ticks + partialTicks;
        for (int index = page * 20; index < Math.min(visible.size(), (page + 1) * 20); index++) {
            Collection collection = visible.get(index);
            Entry entry = collection.current(ticks);
            int offset = index % 20;
            int left = panelX + 11 + offset % 5 * 25;
            int top = panelY + 31 + offset / 5 * 25;
            for (Entry member : collection.entries) {
                if (!member.highlight || !shown.add(member.id)) continue;
                seen.appendTag(new NBTTagString(member.id));
                animations.put(entry.id, now);
            }
            Float start = animations.get(entry.id);
            float remaining = start == null ? 0 : 15.0F - now + start;
            if (remaining > 0) {
                float scale = 1.0F + 0.1F * (float) Math.sin(remaining / 15.0F * Math.PI);
                GlStateManager.pushMatrix();
                GlStateManager.translate(left + 8, top + 12, 0);
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.translate(-left - 8, -top - 12, 0);
            }
            sprite("slot_" + (collection.entries.size() > 1 ? "many_" : "")
                    + (collection.craftable() ? "craftable" : "uncraftable"), left, top, 25, 25);
            RenderHelper.enableGUIStandardItemLighting();
            int itemOffset = 4;
            if (collection.entries.size() > 1 && collection.sameOutput()) {
                minecraft.getRenderItem().renderItemAndEffectIntoGUI(entry.output(ticks / collection.entries.size()), left + 5, top + 5);
                --itemOffset;
            }
            minecraft.getRenderItem().renderItemAndEffectIntoGUI(entry.output(ticks / collection.entries.size()), left + itemOffset, top + itemOffset);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableDepth();
            if (remaining > 0) GlStateManager.popMatrix();
        }
        if (seen.tagCount() > 0) {
            NBTTagCompound request = request("seen");
            request.setTag("Recipes", seen);
            FFDNetwork.updateCookingBook(request);
        }
        int pages = (visible.size() + 19) / 20;
        if (pages > 1) {
            if (page > 0) sprite(within(mouseX, mouseY, panelX + 38, panelY + 137, 12, 17)
                    ? "page_backward_highlighted" : "page_backward", panelX + 38, panelY + 137, 12, 17);
            if (page < pages - 1) sprite(within(mouseX, mouseY, panelX + 93, panelY + 137, 12, 17)
                    ? "page_forward_highlighted" : "page_forward", panelX + 93, panelY + 137, 12, 17);
            String text = I18n.format("gui.recipebook.page", page + 1, pages);
            minecraft.fontRenderer.drawString(text, panelX + 73 - minecraft.fontRenderer.getStringWidth(text) / 2, panelY + 141, 0xFFFFFF);
        }
        renderOverlay(mouseX, mouseY);
        GlStateManager.enableDepth();
    }

    private void renderOverlay(int mouseX, int mouseY) {
        if (overlay.isEmpty()) return;
        int columns = overlay.size() <= 16 ? 4 : 5;
        nineSlice("recipe_book/overlay_recipe", overlayX, overlayY,
                Math.min(columns, overlay.size()) * 25 + 8, (overlay.size() + columns - 1) / columns * 25 + 8,
                32, 32, 4);
        for (int index = 0; index < overlay.size(); index++) {
            Entry entry = overlay.get(index);
            int left = overlayX + 4 + index % columns * 25;
            int top = overlayY + 5 + index / columns * 25;
            sprite("furnace_overlay" + (entry.craftable ? "" : "_disabled")
                    + (within(mouseX, mouseY, left, top, 24, 24) ? "_highlighted" : ""), left, top, 24, 24);
            GlStateManager.pushMatrix();
            GlStateManager.translate(left + 12.0F, top + 12.0F, 0);
            GlStateManager.scale(0.375F, 0.375F, 1);
            RenderHelper.enableGUIStandardItemLighting();
            minecraft.getRenderItem().renderItemAndEffectIntoGUI(entry.input(ticks), -8, -8);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
        }
    }

    private void showOverlay(Collection collection, int left, int top) {
        overlay.clear();
        for (boolean craftable : new boolean[]{true, false}) {
            for (Entry entry : collection.entries) if (entry.craftable == craftable) overlay.add(entry);
        }
        int columns = overlay.size() <= 16 ? 4 : 5;
        int rows = (overlay.size() + columns - 1) / columns;
        int centerX = panelX + 147 / 2;
        int centerY = panelY + 13 + 166 / 2;
        overlayX = left;
        overlayY = top;
        int right = left + Math.min(overlay.size(), columns) * 25;
        if (right > centerX + 50) overlayX -= (right - centerX - 50) / 25 * 25;
        if (top + rows * 25 > centerY + 50) overlayY -= (int) Math.ceil((top + rows * 25 - centerY - 50) / 25.0D) * 25;
        if (overlayY < centerY - 100) overlayY -= (int) Math.ceil((overlayY - centerY + 100) / 25.0D) * 25;
    }

    private boolean hasCategory(int value) {
        for (Entry entry : recipes) if ((!filtering || entry.craftable) && (furnace.isSmoking() || entry.category == value)) return true;
        return false;
    }

    private List<Integer> tabs() {
        List<Integer> result = new ArrayList<>();
        result.add(0);
        for (int value = 1; value < (furnace.isSmoking() ? 2 : 3); value++) if (hasCategory(value)) result.add(value);
        return result;
    }

    public boolean click(int mouseX, int mouseY, int button, int buttonX, int buttonY) {
        if (open && !overlay.isEmpty()) {
            Entry selected = overlayHovered(mouseX, mouseY);
            if (button == 0 && selected != null) select(selected);
            overlay.clear();
            return true;
        }
        if (open && button == 1) {
            Collection collection = hovered(mouseX, mouseY);
            if (collection != null && collection.entries.size() > 1) {
                int offset = visible.indexOf(collection) % 20;
                showOverlay(collection, panelX + 11 + offset % 5 * 25, panelY + 31 + offset / 5 * 25);
                playClick();
                return true;
            }
        }
        if (button != 0) return contains(mouseX, mouseY);
        if (!hidesContainer() && within(mouseX, mouseY, buttonX, buttonY, 20, 18)) {
            open = !open;
            overlay.clear();
            playClick();
            search.setFocused(false);
            settings();
            return true;
        }
        if (!open) return false;
        search.mouseClicked(mouseX, mouseY, button);
        search.setFocused(within(mouseX, mouseY, panelX + 8, panelY + 13, 98, 14));
        if (search.isFocused()) return true;
        if (within(mouseX, mouseY, panelX + 110, panelY + 12, 26, 16)) {
            filtering = !filtering;
            playClick();
            refresh(true);
            settings();
            return true;
        }
        List<Integer> tabs = tabs();
        for (int tab = 0; tab < tabs.size(); tab++) {
            int index = tabs.get(tab);
            if (within(mouseX, mouseY, panelX - 32, panelY + 3 + tab * 27, 37, 27)) {
                category = index;
                playClick();
                refresh(true);
                return true;
            }
        }
        if (page > 0 && within(mouseX, mouseY, panelX + 38, panelY + 137, 12, 17)) {
            --page;
            playClick();
            return true;
        }
        if ((page + 1) * 20 < visible.size() && within(mouseX, mouseY, panelX + 93, panelY + 137, 12, 17)) {
            ++page;
            playClick();
            return true;
        }
        Collection collection = hovered(mouseX, mouseY);
        if (collection != null) {
            select(collection.current(ticks));
            return true;
        }
        return within(mouseX, mouseY, panelX - 30, panelY, 177, 166);
    }

    private void select(Entry entry) {
        playClick();
        NBTTagCompound request = request("select");
        request.setString("Recipe", entry.id);
        request.setBoolean("Maximum", net.minecraft.client.gui.GuiScreen.isShiftKeyDown());
        FFDNetwork.updateCookingBook(request);
        if (narrow) {
            open = false;
            settings();
        }
    }

    public boolean key(char character, int key) {
        if (!open) return false;
        if (key == org.lwjgl.input.Keyboard.KEY_ESCAPE && !overlay.isEmpty()) {
            overlay.clear();
            return true;
        }
        if (key == org.lwjgl.input.Keyboard.KEY_ESCAPE && narrow) {
            open = false;
            settings();
            return true;
        }
        if (minecraft.gameSettings.keyBindChat.isActiveAndMatches(key) && !search.isFocused()) {
            search.setFocused(true);
            return true;
        }
        if (!search.isFocused()) return false;
        if (key == org.lwjgl.input.Keyboard.KEY_ESCAPE) {
            search.setFocused(false);
            return true;
        }
        if (search.textboxKeyTyped(character, key)) refresh(true);
        return true;
    }

    public boolean contains(int mouseX, int mouseY) {
        return open && (!overlay.isEmpty() || within(mouseX, mouseY, panelX - 32, panelY, 179, 166));
    }

    private Collection hovered(int mouseX, int mouseY) {
        if (!open) return null;
        for (int offset = 0; offset < 20; offset++) {
            int index = page * 20 + offset;
            if (index < visible.size() && within(mouseX, mouseY, panelX + 11 + offset % 5 * 25,
                    panelY + 31 + offset / 5 * 25, 25, 25)) return visible.get(index);
        }
        return null;
    }

    private Entry overlayHovered(int mouseX, int mouseY) {
        int columns = overlay.size() <= 16 ? 4 : 5;
        for (int index = 0; index < overlay.size(); index++) {
            if (within(mouseX, mouseY, overlayX + 4 + index % columns * 25,
                    overlayY + 5 + index / columns * 25, 24, 24)) return overlay.get(index);
        }
        return null;
    }

    public ItemStack tooltip(int mouseX, int mouseY, int guiLeft, int guiTop) {
        if (!overlay.isEmpty()) return ItemStack.EMPTY;
        Collection collection = hovered(mouseX, mouseY);
        if (collection != null) return collection.current(ticks).output(ticks / collection.entries.size());
        if (hidesContainer()) return ItemStack.EMPTY;
        Entry entry = ghostEntry();
        if (entry == null) return ItemStack.EMPTY;
        if (within(mouseX, mouseY, guiLeft + 56, guiTop + 17, 16, 16)) return entry.input(ticks);
        if (within(mouseX, mouseY, guiLeft + 112, guiTop + 31, 24, 24)) return entry.output(ticks);
        if (furnace.getStackInSlot(1).isEmpty() && within(mouseX, mouseY, guiLeft + 56, guiTop + 53, 16, 16)) return fuel();
        return ItemStack.EMPTY;
    }

    public String filterTooltip(int mouseX, int mouseY) {
        return open && overlay.isEmpty() && within(mouseX, mouseY, panelX + 110, panelY + 12, 26, 16)
                ? I18n.format(filtering ? "gui.recipebook.toggleRecipes.smeltable" : "gui.recipebook.toggleRecipes.all") : "";
    }

    public boolean hasMoreRecipes(int mouseX, int mouseY) {
        Collection collection = overlay.isEmpty() ? hovered(mouseX, mouseY) : null;
        return collection != null && collection.entries.size() > 1;
    }

    private Entry ghostEntry() {
        for (Entry entry : recipes) if (entry.id.equals(ghost)) return entry;
        return null;
    }

    private ItemStack fuel() {
        if (fuels.isEmpty()) {
            NonNullList<ItemStack> variants = NonNullList.create();
            for (Item item : Item.REGISTRY) item.getSubItems(CreativeTabs.SEARCH, variants);
            for (ItemStack stack : variants) if (TileEntityCookingFurnace.getFuelTime(stack) > 0) fuels.add(stack.copy());
        }
        return fuels.isEmpty() ? ItemStack.EMPTY : fuels.get(ticks / 30 % fuels.size());
    }

    public void renderGhost(int guiLeft, int guiTop) {
        Entry entry = ghostEntry();
        if (entry == null) return;
        ghostItem(entry.input(ticks), guiLeft + 56, guiTop + 17, false);
        ghostItem(entry.output(ticks), guiLeft + 116, guiTop + 35, true);
        if (furnace.getStackInSlot(1).isEmpty()) ghostItem(fuel(), guiLeft + 56, guiTop + 53, false);
    }

    private void ghostItem(ItemStack stack, int left, int top, boolean result) {
        GlStateManager.disableDepth();
        drawRect(left - (result ? 4 : 0), top - (result ? 4 : 0), left + (result ? 20 : 16), top + (result ? 20 : 16), 0x30FF0000);
        RenderHelper.enableGUIStandardItemLighting();
        minecraft.getRenderItem().renderItemAndEffectIntoGUI(stack, left, top);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        drawRect(left, top, left + 16, top + 16, 0x30FFFFFF);
        GlStateManager.enableDepth();
    }

    public void clearGhost() {
        if (!ghost.isEmpty()) {
            ghost = "";
            send("clear");
        }
    }

    private NBTTagCompound request(String action) {
        NBTTagCompound result = new NBTTagCompound();
        result.setInteger("Window", window);
        result.setString("Action", action);
        return result;
    }

    private void send(String action) {
        FFDNetwork.updateCookingBook(request(action));
    }

    private void settings() {
        NBTTagCompound result = request("settings");
        result.setBoolean("Open", open);
        result.setBoolean("Filter", filtering);
        FFDNetwork.updateCookingBook(result);
    }

    private void playClick() {
        minecraft.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(
                net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private static final class Entry {
        private final String id;
        private final String group;
        private final boolean craftable;
        private final int category;
        private final boolean highlight;
        private final List<ItemStack> inputs = new ArrayList<>();
        private final List<ItemStack> outputs = new ArrayList<>();

        private Entry(NBTTagCompound data) {
            id = data.getString("Id");
            group = data.getString("Group");
            craftable = data.getBoolean("Craftable");
            category = data.getInteger("Category");
            highlight = data.getBoolean("Highlight");
            NBTTagList variants = data.getTagList("Variants", 10);
            for (int index = 0; index < variants.tagCount(); index++) {
                NBTTagCompound variant = variants.getCompoundTagAt(index);
                inputs.add(new ItemStack(variant.getCompoundTag("Input")));
                outputs.add(new ItemStack(variant.getCompoundTag("Output")));
            }
        }

        private ItemStack input(int ticks) {
            return inputs.get(ticks / 30 % inputs.size());
        }

        private ItemStack output(int ticks) {
            return outputs.get(ticks / 30 % outputs.size());
        }
    }

    private static final class Collection {
        private final List<Entry> entries = new ArrayList<>();

        private Entry current(int ticks) {
            return entries.get(ticks / 30 % entries.size());
        }

        private boolean craftable() {
            for (Entry entry : entries) if (entry.craftable) return true;
            return false;
        }

        private boolean sameOutput() {
            ItemStack first = entries.get(0).outputs.get(0);
            for (Entry entry : entries) {
                for (ItemStack output : entry.outputs) {
                    if (!ItemStack.areItemsEqual(first, output) || !ItemStack.areItemStackTagsEqual(first, output)) return false;
                }
            }
            return true;
        }
    }
}
