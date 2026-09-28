package xy177.farmersfuturedelight.client.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;

public final class FFDChatAutocomplete {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final int MAX_VISIBLE = 10;
    private static final int ROW_HEIGHT = 12;

    private final Minecraft minecraft;
    private final GuiTextField textField;
    private final List<String> completions = new ArrayList<>();
    private int selected;
    private int offset;
    private boolean tabActive;
    private Request pendingRequest;
    private Request queuedRequest;
    private Request tabRequest;
    private int tabOffset;
    private ClickBox clickBox = new ClickBox(0, 0, 0, 0);

    public FFDChatAutocomplete(GuiTextField textField) {
        this.minecraft = Minecraft.getMinecraft();
        this.textField = textField;
    }

    public boolean onKeyPress(int keyCode) {
        if (keyCode == Keyboard.KEY_TAB) {
            return complete(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                    || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
        }
        if (completions.isEmpty() || !tabActive) {
            return false;
        }
        if (keyCode == Keyboard.KEY_UP) {
            select(selected == 0 ? completions.size() - 1 : selected - 1);
            return true;
        }
        if (keyCode == Keyboard.KEY_DOWN) {
            select((selected + 1) % completions.size());
            return true;
        }
        return false;
    }

    private boolean complete(boolean reverse) {
        if (completions.isEmpty()) {
            Request request = queuedRequest != null ? queuedRequest : pendingRequest;
            if (request == null || !request.matches(textField)) {
                return false;
            }
            if (tabRequest != request) {
                tabRequest = request;
                tabOffset = reverse ? -1 : 0;
            } else {
                tabOffset += reverse ? -1 : 1;
            }
            return true;
        }
        select(Math.floorMod(selected + (reverse ? -1 : tabActive ? 1 : 0),
                completions.size()));
        tabActive = true;
        return true;
    }

    public boolean onClick(int mouseX, int mouseY) {
        if (!clickBox.contains(mouseX, mouseY)) {
            return false;
        }
        int index = offset + (mouseY - clickBox.y) / ROW_HEIGHT;
        if (index >= 0 && index < completions.size()) {
            select(index);
            tabActive = true;
        }
        return true;
    }

    public boolean onScroll(int mouseX, int mouseY, int scroll) {
        if (!clickBox.contains(mouseX, mouseY)) {
            return false;
        }
        offset = Math.max(0, Math.min(offset + scroll,
                Math.max(0, completions.size() - MAX_VISIBLE)));
        return true;
    }

    public void render(int mouseX, int mouseY, FontRenderer font) {
        if (completions.isEmpty()) {
            clickBox = new ClickBox(0, 0, 0, 0);
            return;
        }
        int availableRows = Math.max(1, (textField.y - 4) / ROW_HEIGHT);
        int visible = Math.min(Math.min(MAX_VISIBLE, availableRows), completions.size() - offset);
        if (visible <= 0) {
            return;
        }
        String text = textField.getText();
        int cursor = textField.getCursorPosition();
        int start = replacementStart(text, cursor);
        int textOrigin = textField.x + (textField.getEnableBackgroundDrawing() ? 4 : 0);
        int x = textOrigin + Math.min(font.getStringWidth(text.substring(0, start)),
                Math.max(0, textField.getWidth() - 2));
        int width = 0;
        for (int index = offset; index < offset + visible; index++) {
            width = Math.max(width, font.getStringWidth(completions.get(index)));
        }
        int screenWidth = minecraft.currentScreen == null ? textField.x + textField.getWidth()
                : minecraft.currentScreen.width;
        int left = Math.max(2, Math.min(x, screenWidth - width - 8));
        int top = Math.max(2, textField.y - visible * ROW_HEIGHT - 3);
        int right = Math.min(screenWidth - 2, left + width + 6);
        int bottom = top + visible * ROW_HEIGHT;
        int hovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom
                ? offset + (mouseY - top) / ROW_HEIGHT : -1;
        if (hovered >= 0 && hovered < completions.size()) {
            selected = hovered;
        }
        String query = currentQuery();
        String selectedCompletion = selected >= 0 && selected < completions.size()
                ? completions.get(selected) : "";
        if (selectedCompletion.regionMatches(true, 0, query, 0, query.length())
                && selectedCompletion.length() > query.length()) {
            int previewX = textOrigin + font.getStringWidth(text.substring(0, cursor));
            font.drawString(selectedCompletion.substring(query.length()), previewX,
                    textField.y, 0xFF808080);
        }
        Gui.drawRect(left, top, right, bottom, 0xD0000000);
        for (int row = 0; row < visible; row++) {
            int index = offset + row;
            font.drawString(completions.get(index), left + 3,
                    top + row * ROW_HEIGHT + 2, index == selected
                            ? 0xFFFFFF00 : 0xFFFFFFFF);
        }
        clickBox = new ClickBox(left, top, right - left, bottom - top);
    }

    public void requestUpdate() {
        if (minecraft.player == null || minecraft.player.connection == null) {
            pendingRequest = null;
            clear();
            return;
        }
        String text = textField.getText();
        int cursor = textField.getCursorPosition();
        if (cursor <= 0 || text.isEmpty()) {
            clear();
            return;
        }
        String prefix = text.substring(0, cursor);
        ClientCommandHandler.instance.autoComplete(prefix);
        String[] local = ClientCommandHandler.instance.latestAutoComplete;
        Request request = new Request(text, cursor, prefix,
                local == null ? new String[0] : local.clone());
        completions.clear();
        selected = 0;
        offset = 0;
        tabActive = false;
        tabRequest = null;
        tabOffset = 0;
        if (pendingRequest == null) {
            sendRequest(request);
        } else {
            queuedRequest = request;
        }
    }

    public void setCompletions(String[] serverCompletions) {
        Request request = pendingRequest;
        pendingRequest = null;
        if (request == null) {
            return;
        }
        if (queuedRequest == null && request.matches(textField)) {
            String currentWord = currentWord(request.prefix);
            boolean commandRoot = currentWord.startsWith("/");
            Set<String> merged = new LinkedHashSet<>();
            addMatches(merged, request.local, currentWord, commandRoot);
            addMatches(merged, serverCompletions, currentWord, commandRoot);
            completions.clear();
            completions.addAll(merged);
            Collections.sort(completions, new Comparator<String>() {
                @Override
                public int compare(String left, String right) {
                    int result = left.compareToIgnoreCase(right);
                    return result != 0 ? result : left.compareTo(right);
                }
            });
            selected = 0;
            offset = 0;
            tabActive = false;
            if (tabRequest == request && !completions.isEmpty()) {
                select(Math.floorMod(tabOffset, completions.size()));
                tabActive = true;
            }
        }
        if (tabRequest == request) {
            tabRequest = null;
        }
        sendQueuedRequest();
    }

    public void clear() {
        if (pendingRequest != null) {
            pendingRequest.cancelled = true;
        }
        queuedRequest = null;
        tabRequest = null;
        tabOffset = 0;
        completions.clear();
        selected = 0;
        offset = 0;
        tabActive = false;
        clickBox = new ClickBox(0, 0, 0, 0);
    }

    private void addMatches(Set<String> output, String[] values, String currentWord,
                            boolean commandRoot) {
        if (values == null) {
            return;
        }
        String query = commandRoot ? currentWord.substring(1) : currentWord;
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                continue;
            }
            value = TextFormatting.getTextWithoutFormattingCodes(value);
            if (commandRoot && value.startsWith("/")) {
                value = value.substring(1);
            }
            if (value.regionMatches(true, 0, query, 0, query.length())) {
                output.add(value);
            }
        }
    }

    private void select(int index) {
        if (index < 0 || index >= completions.size()) {
            return;
        }
        selected = index;
        if (selected < offset) {
            offset = selected;
        } else if (selected >= offset + MAX_VISIBLE) {
            offset = selected - MAX_VISIBLE + 1;
        }
        String text = textField.getText();
        int cursor = textField.getCursorPosition();
        int start = replacementStart(text, cursor);
        StringBuilder updated = new StringBuilder(text);
        updated.replace(start, cursor, completions.get(selected));
        textField.setText(updated.toString());
        textField.setCursorPosition(start + completions.get(selected).length());
    }

    private void sendRequest(Request request) {
        pendingRequest = request;
        minecraft.player.connection.sendPacket(new CPacketTabComplete(request.prefix, null, false));
    }

    private void sendQueuedRequest() {
        if (queuedRequest != null && minecraft.player != null
                && minecraft.player.connection != null) {
            Request next = queuedRequest;
            queuedRequest = null;
            sendRequest(next);
        }
    }

    private String currentPrefix() {
        String text = textField.getText();
        return text.substring(0, Math.min(textField.getCursorPosition(), text.length()));
    }

    private static String currentWord(String prefix) {
        return prefix.substring(wordStart(prefix, prefix.length()));
    }

    private String currentQuery() {
        String word = currentWord(currentPrefix());
        return word.startsWith("/") ? word.substring(1) : word;
    }

    private static int replacementStart(String text, int cursor) {
        int start = wordStart(text, cursor);
        return start == 0 && text.startsWith("/") ? 1 : start;
    }

    private static int wordStart(String text, int cursor) {
        int end = Math.min(Math.max(cursor, 0), text.length());
        Matcher matcher = WHITESPACE.matcher(text.substring(0, end));
        int start = 0;
        while (matcher.find()) {
            start = matcher.end();
        }
        return start;
    }

    private static final class Request {
        private final String text;
        private final int cursor;
        private final String prefix;
        private final String[] local;
        private boolean cancelled;

        private Request(String text, int cursor, String prefix, String[] local) {
            this.text = text;
            this.cursor = cursor;
            this.prefix = prefix;
            this.local = local;
        }

        private boolean matches(GuiTextField field) {
            return !cancelled && cursor == field.getCursorPosition()
                    && text.equals(field.getText())
                    && prefix.equals(text.substring(0, cursor));
        }
    }

    private static final class ClickBox {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private ClickBox(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
