package xy177.farmersfuturedelight.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.input.Keyboard;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class GuiWritableBookEditor extends GuiScreenBook {
    private static final int MAX_PAGE_LENGTH = 1024;
    private static final int TEXT_X = 35;
    private static final int TEXT_Y = 30;
    private static final int TEXT_WIDTH = 114;
    private static final int TEXT_HEIGHT = 126;
    private static final int CURSOR_BLINK_TICKS = 6;
    private final NBTTagList pages;
    private int cursor;
    private int selectionAnchor;
    private int activePage = -1;
    private int updateCount;
    private int scrollLine;
    private boolean draggingSelection;

    public GuiWritableBookEditor(net.minecraft.entity.player.EntityPlayer player, ItemStack book) {
        super(player, book, true);
        pages = ObfuscationReflectionHelper.getPrivateValue(
                GuiScreenBook.class, this, "field_146483_y");
        selectionAnchor = 0;
    }

    @Override
    public void initGui() {
        super.initGui();
        for (int index = 0; index < buttonList.size(); index++) {
            GuiButton button = buttonList.get(index);
            if (button.id == 1 || button.id == 2) {
                button.x = (width - 192) / 2 + (button.id == 1 ? 116 : 43);
                button.y = 159;
            }
        }
        updateCursorPage();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        updateCount++;
        updateCursorPage();
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
        super.actionPerformed(button);
        updateCursorPage();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isSigning()) {
            super.keyTyped(typedChar, keyCode);
            return;
        }
        if (keyCode == 1) {
            super.keyTyped(typedChar, keyCode);
            return;
        }
        updateCursorPage();
        String page = currentPage();
        boolean shift = GuiScreen.isShiftKeyDown();
        if (GuiScreen.isCtrlKeyDown()) {
            if (keyCode == Keyboard.KEY_A) {
                selectionAnchor = 0;
                cursor = page.length();
                return;
            }
            if (keyCode == Keyboard.KEY_C || keyCode == Keyboard.KEY_X) {
                GuiScreen.setClipboardString(selectedText(page));
                if (keyCode == Keyboard.KEY_X) replaceSelection(page, "");
                return;
            }
            if (keyCode == Keyboard.KEY_V) {
                insertText(page, GuiScreen.getClipboardString());
                return;
            }
        }
        if (keyCode == Keyboard.KEY_LEFT) {
            moveCursor(cursor - (GuiScreen.isCtrlKeyDown() ? previousWordLength(page) : 1), shift);
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            moveCursor(cursor + (GuiScreen.isCtrlKeyDown() ? nextWordLength(page) : 1), shift);
        } else if (keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_DOWN) {
            moveCursorVertically(page, keyCode == Keyboard.KEY_UP ? -1 : 1, shift);
        } else if (keyCode == Keyboard.KEY_HOME) {
            moveCursor(lineStart(page, cursor), shift);
        } else if (keyCode == Keyboard.KEY_END) {
            moveCursor(lineEnd(page, cursor), shift);
        } else if (keyCode == Keyboard.KEY_BACK) {
            if (hasSelection()) {
                replaceSelection(page, "");
            } else if (cursor > 0) {
                selectionAnchor = cursor - 1;
                replaceSelection(page, "");
            }
        } else if (keyCode == Keyboard.KEY_DELETE) {
            if (hasSelection()) {
                replaceSelection(page, "");
            } else if (cursor < page.length()) {
                selectionAnchor = cursor + 1;
                replaceSelection(page, "");
            }
        } else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            insertText(page, "\n");
        } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            insertText(page, Character.toString(typedChar));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        updateCursorPage();
        if (mouseButton == 0 && isTextPosition(mouseX, mouseY)) {
            cursor = cursorAtPosition(currentPage(), mouseX, mouseY);
            selectionAnchor = cursor;
            draggingSelection = true;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton,
                                  long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (draggingSelection) cursor = cursorAtPosition(currentPage(), mouseX, mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        draggingSelection = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (isSigning()) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }
        updateCursorPage();
        String page = currentPage();
        List<LineSpan> lines = wrappedLines(page);
        int visibleLineCount = Math.max(1, TEXT_HEIGHT / fontRenderer.FONT_HEIGHT);
        int firstVisibleLine = Math.min(scrollLine, Math.max(0, lines.size() - visibleLineCount));
        int lastVisibleLine = Math.min(lines.size(), firstVisibleLine + visibleLineCount);
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/gui/container/writable_book.png"));
        drawTexturedModalRect((width - 192) / 2, 2, 0, 0, 192, 192);
        String indicator = I18n.format("book.pageIndicator", activePage + 1, pages.tagCount());
        fontRenderer.drawString(indicator, (width - 192) / 2 + 148
                - fontRenderer.getStringWidth(indicator), 18, 0);
        int left = (width - 192) / 2 + TEXT_X;
        int top = 2 + TEXT_Y;
        for (int lineIndex = firstVisibleLine; lineIndex < lastVisibleLine; lineIndex++) {
            LineSpan line = lines.get(lineIndex);
            fontRenderer.drawString(page.substring(line.start, line.end), left,
                    top + (lineIndex - firstVisibleLine) * fontRenderer.FONT_HEIGHT, 0);
        }
        if (hasSelection()) {
            int selectionStart = Math.min(cursor, selectionAnchor);
            int selectionEnd = Math.max(cursor, selectionAnchor);
            for (int lineIndex = firstVisibleLine; lineIndex < lastVisibleLine; lineIndex++) {
                LineSpan line = lines.get(lineIndex);
                int start = Math.max(selectionStart, line.start);
                int end = Math.min(selectionEnd, line.end);
                if (start < end) {
                    int x1 = left + fontRenderer.getStringWidth(page.substring(line.start, start));
                    int x2 = left + fontRenderer.getStringWidth(page.substring(line.start, end));
                    GuiModernTextSelection.draw(x1, top + (lineIndex - firstVisibleLine) * fontRenderer.FONT_HEIGHT,
                            x2, top + (lineIndex - firstVisibleLine + 1) * fontRenderer.FONT_HEIGHT);
                }
            }
        }
        if (updateCount / CURSOR_BLINK_TICKS % 2 == 0) {
            LineSpan line = lineForCursor(lines, cursor);
            if (line != null && lines.indexOf(line) >= firstVisibleLine
                    && lines.indexOf(line) < lastVisibleLine) {
                int x = left + fontRenderer.getStringWidth(page.substring(line.start, cursor));
                int y = top + (lines.indexOf(line) - firstVisibleLine) * fontRenderer.FONT_HEIGHT;
                drawRect(x, y, x + 1, y + fontRenderer.FONT_HEIGHT, 0xFF202020);
            }
        }
        for (GuiButton button : buttonList) {
            if (!button.visible) continue;
            if (button.id == 1 || button.id == 2) {
                boolean hovered = mouseX >= button.x && mouseY >= button.y
                        && mouseX < button.x + button.width && mouseY < button.y + button.height;
                mc.getTextureManager().bindTexture(new ResourceLocation(FarmerFutureDelight.MODID,
                        "textures/gui/sprites/widget/page_" + (button.id == 1 ? "forward" : "backward")
                                + (hovered ? "_highlighted" : "") + ".png"));
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                drawModalRectWithCustomSizedTexture(button.x, button.y, 0, 0, 23, 13, 23, 13);
            } else {
                GuiButton modern = new GuiModernButton(button.id, button.x, button.y,
                        button.width, button.displayString);
                modern.enabled = button.enabled;
                modern.drawButton(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    private void updateCursorPage() {
        int page = ObfuscationReflectionHelper.getPrivateValue(
                GuiScreenBook.class, this, "field_146484_x");
        if (page != activePage) {
            activePage = page;
            cursor = currentPage().length();
            selectionAnchor = cursor;
            scrollLine = 0;
            draggingSelection = false;
        }
        cursor = MathHelper.clamp(cursor, 0, currentPage().length());
        selectionAnchor = MathHelper.clamp(selectionAnchor, 0, currentPage().length());
        ensureCursorVisible(currentPage());
    }

    private String currentPage() {
        if (pages == null || activePage < 0 || activePage >= pages.tagCount()) return "";
        return pages.getStringTagAt(activePage);
    }

    private boolean isSigning() {
        return ObfuscationReflectionHelper.getPrivateValue(
                GuiScreenBook.class, this, "field_146480_s");
    }

    private boolean hasSelection() {
        return cursor != selectionAnchor;
    }

    private String selectedText(String page) {
        return page.substring(Math.min(cursor, selectionAnchor), Math.max(cursor, selectionAnchor));
    }

    private void moveCursor(int position, boolean extendSelection) {
        cursor = MathHelper.clamp(position, 0, currentPage().length());
        if (!extendSelection) selectionAnchor = cursor;
    }

    private void replaceSelection(String page, String inserted) {
        int start = Math.min(cursor, selectionAnchor);
        int end = Math.max(cursor, selectionAnchor);
        int remaining = Math.max(0, MAX_PAGE_LENGTH - page.length());
        if (inserted.length() > remaining) inserted = inserted.substring(0, remaining);
        String updated = page.substring(0, start) + inserted + page.substring(end);
        if (wrappedLines(updated).size() > TEXT_HEIGHT / fontRenderer.FONT_HEIGHT) return;
        setCurrentPage(updated);
        cursor = start + inserted.length();
        selectionAnchor = cursor;
    }

    private void insertText(String page, String inserted) {
        StringBuilder filtered = new StringBuilder();
        for (int index = 0; index < inserted.length(); index++) {
            char character = inserted.charAt(index);
            if (character == '\n' || ChatAllowedCharacters.isAllowedCharacter(character)) {
                filtered.append(character);
            }
        }
        replaceSelection(page, filtered.toString());
    }

    private void setCurrentPage(String value) {
        if (activePage < 0 || activePage >= pages.tagCount()) return;
        pages.set(activePage, new NBTTagString(value));
        ObfuscationReflectionHelper.setPrivateValue(GuiScreenBook.class,
                this, true, "field_146481_r");
    }

    private void moveCursorVertically(String page, int direction, boolean extendSelection) {
        List<LineSpan> lines = wrappedLines(page);
        LineSpan current = lineForCursor(lines, cursor);
        if (current == null) return;
        int currentLine = lines.indexOf(current);
        int targetLine = currentLine + direction;
        if (targetLine < 0 || targetLine >= lines.size()) return;
        int column = cursor - current.start;
        LineSpan target = lines.get(targetLine);
        moveCursor(Math.min(target.start + column, target.end), extendSelection);
    }

    private int previousWordLength(String page) {
        int position = cursor;
        while (position > 0 && Character.isWhitespace(page.charAt(position - 1))) position--;
        while (position > 0 && !Character.isWhitespace(page.charAt(position - 1))) position--;
        return cursor - position;
    }

    private int nextWordLength(String page) {
        int position = cursor;
        while (position < page.length() && !Character.isWhitespace(page.charAt(position))) position++;
        while (position < page.length() && Character.isWhitespace(page.charAt(position))) position++;
        return position - cursor;
    }

    private int lineStart(String page, int position) {
        int start = Math.max(page.lastIndexOf('\n', Math.max(0, position - 1)) + 1, 0);
        for (LineSpan line : wrappedLines(page)) {
            if (position >= line.start && position <= line.end) return line.start;
        }
        return start;
    }

    private int lineEnd(String page, int position) {
        for (LineSpan line : wrappedLines(page)) {
            if (position >= line.start && position <= line.end) return line.end;
        }
        int end = page.indexOf('\n', position);
        return end < 0 ? page.length() : end;
    }

    private boolean isTextPosition(int mouseX, int mouseY) {
        int left = (width - 192) / 2 + TEXT_X;
        int top = 2 + TEXT_Y;
        return mouseX >= left && mouseX <= left + TEXT_WIDTH
                && mouseY >= top && mouseY <= top + TEXT_HEIGHT;
    }

    private int cursorAtPosition(String page, int mouseX, int mouseY) {
        List<LineSpan> lines = wrappedLines(page);
        int left = (width - 192) / 2 + TEXT_X;
        int top = 2 + TEXT_Y;
        int lineIndex = MathHelper.clamp(scrollLine
                        + (mouseY - top) / fontRenderer.FONT_HEIGHT,
                0, Math.max(0, lines.size() - 1));
        LineSpan line = lines.isEmpty() ? new LineSpan(0, 0) : lines.get(lineIndex);
        int targetX = mouseX - left;
        for (int index = line.start; index < line.end; index++) {
            int before = fontRenderer.getStringWidth(page.substring(line.start, index));
            int after = fontRenderer.getStringWidth(page.substring(line.start, index + 1));
            if (targetX < (before + after) / 2) return index;
        }
        return line.end;
    }

    private LineSpan lineForCursor(List<LineSpan> lines, int position) {
        for (LineSpan line : lines) {
            if (position >= line.start && position <= line.end) return line;
        }
        return lines.isEmpty() ? null : lines.get(lines.size() - 1);
    }

    private void ensureCursorVisible(String page) {
        List<LineSpan> lines = wrappedLines(page);
        int visibleLineCount = Math.max(1, TEXT_HEIGHT / fontRenderer.FONT_HEIGHT);
        LineSpan line = lineForCursor(lines, cursor);
        if (line == null) return;
        int cursorLine = lines.indexOf(line);
        if (cursorLine < scrollLine) scrollLine = cursorLine;
        if (cursorLine >= scrollLine + visibleLineCount) {
            scrollLine = cursorLine - visibleLineCount + 1;
        }
        scrollLine = MathHelper.clamp(scrollLine, 0, Math.max(0, lines.size() - visibleLineCount));
    }

    private List<LineSpan> wrappedLines(String page) {
        List<LineSpan> lines = new ArrayList<>();
        int start = 0;
        while (start < page.length()) {
            int end = start;
            int lastSpace = -1;
            while (end < page.length() && page.charAt(end) != '\n'
                    && fontRenderer.getStringWidth(page.substring(start, end + 1)) <= TEXT_WIDTH) {
                if (Character.isWhitespace(page.charAt(end))) lastSpace = end;
                end++;
            }
            if (end < page.length() && !Character.isWhitespace(page.charAt(end)) && lastSpace > start) {
                end = lastSpace;
            }
            if (end == start && page.charAt(end) != '\n') end++;
            lines.add(new LineSpan(start, end));
            start = end;
            if (start < page.length() && Character.isWhitespace(page.charAt(start))) start++;
        }
        if (lines.isEmpty() || page.endsWith("\n")) lines.add(new LineSpan(page.length(), page.length()));
        return lines;
    }

    private static final class LineSpan {
        private final int start;
        private final int end;

        private LineSpan(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
