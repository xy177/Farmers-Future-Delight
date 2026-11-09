package xy177.farmersfuturedelight.client.gui;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import xy177.farmersfuturedelight.client.FFDModernMenus;
import xy177.farmersfuturedelight.client.friends.FFDFriendsClient;
import xy177.farmersfuturedelight.client.friends.FFDFriendsService;
import xy177.farmersfuturedelight.client.friends.FFDFriendsSkins;
import xy177.farmersfuturedelight.client.friends.FFDFriendsWidgets;

public final class FFDGuiFriends extends GuiScreen {
    private final GuiScreen parent;
    private final FFDFriendsClient client = FFDFriendsClient.INSTANCE;
    private final List<Row> rows = new ArrayList<>();
    private final IntBuffer scissor = BufferUtils.createIntBuffer(16);
    private List<ITextComponent> footerLines;
    private List<ITextComponent> emptyLines;
    private GuiTextField name;
    private GuiButton send;
    private GuiButton friendsTab;
    private GuiButton pendingTab;
    private GuiButton enable;
    private int left;
    private int top;
    private int panelWidth;
    private int bottom;
    private int listTop;
    private int rowsHeight;
    private int tab;
    private int scroll;
    private int selected;
    private int focus = -1;
    private boolean dragging;
    private int version = -1;

    public FFDGuiFriends(GuiScreen parent) { this.parent = parent; }

    public GuiScreen previous() { return parent; }

    private static String text(String key, Object... args) { return FFDFriendsWidgets.text(key, args); }

    @Override
    public void initGui() {
        String input = name == null ? "" : name.getText();
        if (parent != null) parent.setWorldAndResolution(mc, width, height);
        fontRenderer = FFDFriendsWidgets.font();
        Keyboard.enableRepeatEvents(true);
        panelWidth = Math.min(220, width - 32);
        left = (width - panelWidth) / 2;
        top = 40;
        bottom = height - 40;
        buttonList.clear();
        friendsTab = addButton(new FFDFriendsWidgets.Tab(0, left, top - 27, panelWidth / 2,
                text("gui.friends.tab_friends")));
        pendingTab = addButton(new FFDFriendsWidgets.Tab(1, left + panelWidth / 2, top - 27,
                panelWidth - panelWidth / 2, text("gui.friends.requests_count", 0)));
        name = new NameField(left + 8, top + 3, panelWidth - 39);
        name.setMaxStringLength(16);
        name.setText(input);
        name.setFocused(focus == 0);
        send = addButton(new FFDFriendsWidgets.Button(2, left + panelWidth - 28, top + 3, 20,
                text("gui.friends.send_request"), "send_request", 15, 15));
        enable = addButton(new FFDFriendsWidgets.Button(5, left + (panelWidth - 160) / 2,
                top + (bottom - top) / 2 + 20, 160, text("options.friendsList.confirm.turnOn")));
        footerLines = linkedText("gui.friends.manage_account_footer");
        rebuild();
        updateScreenState();
        client.refresh();
    }

    private List<ITextComponent> linkedText(String key) {
        ITextComponent link = new TextComponentTranslation("ffd.friends.vanilla.gui.friends.empty_state.link")
                .setStyle(new Style().setColor(TextFormatting.GRAY).setUnderlined(true).setClickEvent(new ClickEvent(
                        ClickEvent.Action.OPEN_URL, "https://aka.ms/MinecraftJavaXboxPrivacyAndSafety")));
        String value = text(key, "\u0001");
        int marker = value.indexOf('\u0001');
        ITextComponent message = new TextComponentString(marker < 0 ? value : value.substring(0, marker))
                .setStyle(new Style().setColor(TextFormatting.GRAY));
        if (marker >= 0) message.appendSibling(link).appendText(value.substring(marker + 1));
        return GuiUtilRenderComponents.splitText(message, panelWidth - 16, fontRenderer, false, true);
    }

    private void rebuild() {
        version = client.revision();
        rows.clear();
        rowsHeight = 0;
        listTop = top + (tab == 0 ? 44 : 0);
        emptyLines = linkedText(tab == 0 ? "gui.friends.empty_state" : "gui.friends.pending.empty");
        FFDFriendsService.Data data = client.data();
        if (tab == 0) {
            List<FFDFriendsService.Friend> list = new ArrayList<>(data.friends);
            list.sort(Comparator.comparingInt((FFDFriendsService.Friend friend) -> order(friend.id)));
            for (FFDFriendsService.Friend friend : list) addRow(friend, 0);
        } else {
            if (!data.incoming.isEmpty()) rowsHeight += 9;
            for (FFDFriendsService.Friend friend : data.incoming) addRow(friend, 1);
            if (!data.outgoing.isEmpty()) rowsHeight += 9;
            for (FFDFriendsService.Friend friend : data.outgoing) addRow(friend, 2);
        }
        selected = Math.min(selected, Math.max(0, rows.size() - 1));
        scroll = Math.min(scroll, maxScroll());
        pendingTab.displayString = text("gui.friends.requests_count", data.incoming.size());
    }

    private void addRow(FFDFriendsService.Friend friend, int kind) {
        rows.add(new Row(friend, kind, rowsHeight));
        rowsHeight += 28;
    }

    private int order(UUID id) {
        String state = client.presence().getOrDefault(id, "OFFLINE");
        switch (state) {
            case "PLAYING_HOSTED_SERVER": return 0;
            case "PLAYING_SERVER": return 1;
            case "PLAYING_REALMS": return 2;
            case "PLAYING_OFFLINE": return 3;
            case "ONLINE": return 4;
            default: return 5;
        }
    }

    private int contentHeight() {
        if (rows.isEmpty()) return (tab == 0 ? 56 : 0) + emptyLines.size() * 9;
        return rowsHeight + (tab == 0 ? footerLines.size() * 9 : 0);
    }

    private int maxScroll() {
        if (!client.ready() || !client.data().enabled || client.error() != null) return 0;
        return Math.max(0, contentHeight() - (bottom - listTop));
    }

    @Override
    public void updateScreen() {
        name.updateCursorCounter();
        if (version != client.revision()) rebuild();
        updateScreenState();
    }

    private boolean validName() {
        String value = name.getText();
        if (!value.matches("[A-Za-z0-9_]{1,16}") || value.equalsIgnoreCase(mc.getSession().getUsername())) return false;
        for (FFDFriendsService.Friend friend : client.data().friends) if (friend.name.equalsIgnoreCase(value)) return false;
        for (FFDFriendsService.Friend friend : client.data().incoming) if (friend.name.equalsIgnoreCase(value)) return false;
        for (FFDFriendsService.Friend friend : client.data().outgoing) if (friend.name.equalsIgnoreCase(value)) return false;
        return true;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (parent != null) parent.drawScreen(-1, -1, partialTicks);
        else drawDefaultBackground();
        FFDModernMenus.blurOverlay(this);
        FFDFriendsWidgets.panel("background", left - 8, top - 8, panelWidth + 16,
                bottom - top + 17, 236, 34, 8);
        updateScreenState();
        if (tab == 0) {
            name.drawTextBox();
            String label = text("gui.friends.my_profile_name");
            fontRenderer.drawStringWithShadow(label, left + 8, top + 29, 0xA0A0A0);
            fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(mc.getSession().getUsername(),
                    Math.max(0, panelWidth - 16 - fontRenderer.getStringWidth(label))),
                    left + 8 + fontRenderer.getStringWidth(label), top + 29, 0xFFFFFF);
            FFDFriendsWidgets.sprite("list_separator_top", left, listTop - 2, panelWidth, 2, 32, 2);
        }
        if (client.error() != null) {
            String key = "ffd.friends.error." + client.error().name().toLowerCase(Locale.ROOT);
            centeredText(I18n.format(key), listTop + (bottom - listTop - 24) / 2, 0xFF7777);
        } else if (!client.ready()) {
            drawCenteredString(fontRenderer, text(tab == 0 ? "gui.friends.loading_friends" : "gui.friends.loading_requests"),
                    width / 2, listTop + (bottom - listTop) / 2 - 4, 0xA0A0A0);
        } else if (!client.data().enabled) {
            centeredText(text("options.friendsList.confirm.message", text("options.friendsList.confirm.message.link")),
                    listTop + 8, 0xA0A0A0);
        } else drawList(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (client.actionError() != null) {
            String message = I18n.format("ffd.friends.error." + client.actionError().name().toLowerCase(Locale.ROOT));
            Gui.drawRect(left, bottom - 12, left + panelWidth, bottom, 0xEE383838);
            fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(message, panelWidth - 8), left + 4, bottom - 10, 0xFF7777);
            if (mouseX >= left && mouseX < left + panelWidth && mouseY >= bottom - 12 && mouseY < bottom)
                FFDFriendsWidgets.tooltip(this, message, mouseX, mouseY, bottom - 12, 12);
        }
        if (send.visible && send.isMouseOver())
            FFDFriendsWidgets.tooltip(this, text(name.getText().trim().isEmpty()
                    ? "gui.friends.empty_nickname" : "gui.friends.send_request"), mouseX, mouseY, send);
        for (Row row : rows) for (GuiButton button : row.buttons)
            if (button.visible && button.isMouseOver() && mouseY >= listTop && mouseY < bottom)
                FFDFriendsWidgets.tooltip(this, button.displayString, mouseX, mouseY, button);
        if (tab == 0 && mouseX >= left + 8 && mouseX < left + panelWidth - 8 && mouseY >= top + 29 && mouseY < top + 38)
            FFDFriendsWidgets.tooltip(this, text("gui.friends.copy_to_clipboard"), mouseX, mouseY,
                    top + 29, 9);
        GlStateManager.color(1, 1, 1, 1);
    }

    private void centeredText(String message, int y, int color) {
        for (String line : fontRenderer.listFormattedStringToWidth(message, panelWidth - 24)) {
            drawCenteredString(fontRenderer, line, width / 2, y, color);
            y += 9;
        }
    }

    private void updateScreenState() {
        boolean enabled = client.ready() && client.data().enabled && client.error() == null;
        name.setVisible(tab == 0);
        name.setEnabled(enabled && !client.busy());
        send.visible = tab == 0;
        send.enabled = enabled && client.canAct() && validName();
        enable.visible = client.ready() && !client.data().enabled && client.error() == null;
        enable.enabled = client.canAct();
        ((FFDFriendsWidgets.Tab) friendsTab).selected = tab == 0;
        ((FFDFriendsWidgets.Tab) pendingTab).selected = tab == 1;
    }

    private void drawList(int mouseX, int mouseY, float partialTicks) {
        scissor.clear();
        GL11.glGetInteger(GL11.GL_SCISSOR_BOX, scissor);
        boolean previous = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        int scale = new ScaledResolution(mc).getScaleFactor();
        int x = left * scale, y = mc.displayHeight - bottom * scale;
        int w = panelWidth * scale, h = Math.max(0, bottom - listTop) * scale;
        if (previous) {
            int right = Math.min(x + w, scissor.get(0) + scissor.get(2));
            int upper = Math.min(y + h, scissor.get(1) + scissor.get(3));
            x = Math.max(x, scissor.get(0)); y = Math.max(y, scissor.get(1));
            w = Math.max(0, right - x); h = Math.max(0, upper - y);
        }
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, w, h);
        try {
            if (rows.isEmpty()) drawEmpty();
            else {
                int lastKind = -1;
                for (int i = 0; i < rows.size(); i++) {
                    Row row = rows.get(i);
                    int rowY = listTop + row.offset - scroll;
                    if (tab == 1 && row.kind != lastKind)
                        drawCenteredString(fontRenderer, TextFormatting.BOLD.toString() + TextFormatting.UNDERLINE
                                + text(row.kind == 1 ? "gui.friends.pending.received" : "gui.friends.pending.sent"),
                                width / 2, rowY - 9, 0xFFFFFF);
                    lastKind = row.kind;
                    boolean visible = rowY + 28 > listTop && rowY < bottom;
                    for (GuiButton button : row.buttons) button.visible = visible;
                    if (!visible) continue;
                    if (focus == 3 && selected == i)
                        Gui.drawRect(left + 4, rowY, left + panelWidth - 4, rowY + 28, 0x44444444);
                    avatar(row.friend, left + 8, rowY + 2);
                    int nameWidth = panelWidth - (row.kind == 1 ? 90 : 66);
                    fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(row.friend.name, nameWidth),
                            left + 36, rowY + (row.kind == 0 ? 5 : 10), 0xFFFFFF);
                    if (row.kind == 0) {
                        String state = "gui.friends.presence.status."
                                + client.presence().getOrDefault(row.friend.id, "OFFLINE").toLowerCase(Locale.ROOT);
                        fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(text(state), nameWidth),
                                left + 36, rowY + 16, order(row.friend.id) < 5 ? 0x00FF00 : 0xA0A0A0);
                    }
                    for (GuiButton button : row.buttons) {
                        button.y = rowY + 4;
                        button.enabled = client.canAct();
                        button.drawButton(mc, mouseX, mouseY, partialTicks);
                    }
                }
                if (tab == 0) drawLinkedLines(footerLines, listTop + rowsHeight - scroll);
            }
        } finally {
            GL11.glScissor(scissor.get(0), scissor.get(1), scissor.get(2), scissor.get(3));
            if (!previous) GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
        if (maxScroll() > 0) {
            int thumb = thumbHeight();
            int sy = listTop + (bottom - listTop - thumb) * scroll / maxScroll();
            FFDFriendsWidgets.panel("scrollbar_background", left + panelWidth - 6, listTop, 6, bottom - listTop, 6, 32, 1);
            FFDFriendsWidgets.panel("scrollbar", left + panelWidth - 6, sy, 6, thumb, 6, 32, 1);
        }
    }

    private void drawEmpty() {
        int content = contentHeight();
        int y = listTop + Math.max(0, (bottom - listTop - content) / 2) - scroll;
        if (tab == 0) {
            FFDFriendsWidgets.sprite("illustrations_00", left + (panelWidth - 128) / 2, y, 128, 48, 128, 48);
            y += 56;
        }
        drawLinkedLines(emptyLines, y);
    }

    private void drawLinkedLines(List<ITextComponent> lines, int y) {
        for (ITextComponent line : lines) {
            String value = line.getFormattedText();
            fontRenderer.drawStringWithShadow(value, left + (panelWidth - fontRenderer.getStringWidth(value)) / 2, y, 0xA0A0A0);
            y += 9;
        }
    }

    private ITextComponent linkedAt(int x, int y) {
        if (y < listTop || y >= bottom) return null;
        List<ITextComponent> lines;
        int firstY;
        if (rows.isEmpty()) {
            lines = emptyLines;
            firstY = listTop + Math.max(0, (bottom - listTop - contentHeight()) / 2)
                    + (tab == 0 ? 56 : 0) - scroll;
        } else {
            if (tab != 0) return null;
            lines = footerLines;
            firstY = listTop + rowsHeight - scroll;
        }
        int row = (y - firstY) / 9;
        if (y < firstY || row >= lines.size()) return null;
        ITextComponent line = lines.get(row);
        int start = left + (panelWidth - fontRenderer.getStringWidth(line.getFormattedText())) / 2;
        for (ITextComponent part : line) {
            int end = start + fontRenderer.getStringWidth(part.getStyle().getFormattingCode() + part.getUnformattedComponentText());
            if (x >= start && x < end && part.getStyle().getClickEvent() != null) return part;
            start = end;
        }
        return null;
    }

    private void avatar(FFDFriendsService.Friend friend, int x, int y) {
        mc.getTextureManager().bindTexture(FFDFriendsSkins.skin(friend));
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        Gui.drawScaledCustomSizeModalRect(x, y, 8, 8, 8, 8, 24, 24, 64, 64);
        Gui.drawScaledCustomSizeModalRect(x, y, 40, 8, 8, 8, 24, 24, 64, 64);
    }

    private void selectTab(int value) {
        tab = value;
        scroll = selected = 0;
        name.setFocused(false);
        rebuild();
        updateScreenState();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled || !button.visible) return;
        if (button.id == 0 || button.id == 1) { focus = 2; selectTab(button.id); }
        else if (button.id == 2 && validName()) {
            String submitted = name.getText();
            client.change(null, submitted, true, success -> { if (name.getText().equals(submitted)) name.setText(""); });
        } else if (button.id == 5) FFDGuiFriendsSettings.confirmEnabled(this);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        if (x < left - 8 || x > left + panelWidth + 8 || y < top - 27 || y > bottom + 16) {
            mc.displayGuiScreen(parent); return;
        }
        super.mouseClicked(x, y, button);
        if (mc.currentScreen != this || button != 0) return;
        if (name.getVisible()) name.mouseClicked(x, y, button);
        if (name.isFocused()) focus = 0;
        if (!client.ready() || !client.data().enabled || client.error() != null) return;
        if (x >= left + panelWidth - 6 && x < left + panelWidth && y >= listTop && y < bottom && maxScroll() > 0) {
            dragging = true;
            drag(y);
            return;
        }
        if (tab == 0 && y >= top + 29 && y < top + 38 && x >= left + 8 && x < left + panelWidth - 8)
            setClipboardString(mc.getSession().getUsername());
        ITextComponent link = linkedAt(x, y);
        if (link != null && handleComponentClick(link)) return;
        if (y < listTop || y >= bottom) return;
        for (Row row : rows) {
            for (int j = 0; j < row.buttons.size(); j++) if (row.buttons.get(j).mousePressed(mc, x, y)) {
                row.buttons.get(j).playPressSound(mc.getSoundHandler());
                perform(row, j == 0 && row.kind == 1);
                return;
            }
        }
    }

    private void perform(Row row, boolean accept) {
        if (!client.canAct()) return;
        UUID selectedAccount = client.account();
        if (row.kind == 0) mc.displayGuiScreen(new GuiYesNo((yes, id) -> {
            mc.displayGuiScreen(this);
            if (yes && selectedAccount != null && selectedAccount.equals(client.account()))
                client.change(row.friend.id, null, false, null);
        }, text("gui.friends.confirm_title"), text("gui.friends.confirm_unfriend"), 0));
        else client.change(row.friend.id, null, accept, null);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int delta = Mouse.getEventDWheel();
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (delta != 0 && x >= left && x < left + panelWidth && y >= listTop && y < bottom)
            scroll = Math.max(0, Math.min(maxScroll(), scroll - Integer.signum(delta) * 10));
    }

    private int thumbHeight() {
        int available = bottom - listTop;
        return Math.max(1, Math.min(available - 8, Math.max(32, available * available / Math.max(1, contentHeight()))));
    }

    private void drag(int y) {
        int thumb = thumbHeight();
        scroll = Math.max(0, Math.min(maxScroll(),
                (y - listTop - thumb / 2) * maxScroll() / Math.max(1, bottom - listTop - thumb)));
    }

    @Override
    protected void mouseClickMove(int x, int y, int button, long elapsed) {
        if (dragging && button == 0) drag(y);
    }

    @Override
    protected void mouseReleased(int x, int y, int button) {
        dragging = false;
        super.mouseReleased(x, y, button);
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        if (key == Keyboard.KEY_TAB) {
            focus = (focus + (isShiftKeyDown() ? 3 : 1)) & 3;
            if (tab == 1 && focus < 2) focus = isShiftKeyDown() ? 3 : 2;
            name.setFocused(focus == 0 && tab == 0);
            return;
        }
        if (name.isFocused() && name.getVisible()) {
            if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) actionPerformed(send);
            else name.textboxKeyTyped(c, key);
            return;
        }
        if (key == Keyboard.KEY_R) { client.refresh(); return; }
        if (focus == 2 && (key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT)) {
            selectTab(1 - tab); return;
        }
        if (focus == 3 && (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN)) {
            selected = Math.max(0, Math.min(rows.size() - 1, selected + (key == Keyboard.KEY_UP ? -1 : 1)));
            if (!rows.isEmpty()) {
                int offset = rows.get(selected).offset;
                scroll = Math.max(0, Math.min(maxScroll(), Math.max(offset - (bottom - listTop) + 28, Math.min(scroll, offset))));
            }
        } else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
            if (focus == 3 && !rows.isEmpty() && client.ready() && client.data().enabled && client.error() == null)
                perform(rows.get(selected), rows.get(selected).kind == 1);
            else if (focus == 1) actionPerformed(send);
        } else if (focus == 3 && key == Keyboard.KEY_DELETE && !rows.isEmpty() && client.error() == null)
            perform(rows.get(selected), false);
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    private final class Row {
        final FFDFriendsService.Friend friend;
        final int kind;
        final int offset;
        final List<GuiButton> buttons = new ArrayList<>();

        Row(FFDFriendsService.Friend friend, int kind, int offset) {
            this.friend = friend; this.kind = kind; this.offset = offset;
            int right = left + panelWidth - 8;
            if (kind == 1) buttons.add(new FFDFriendsWidgets.Button(0, right - 44, 0, 20,
                    text("gui.friends.accept"), "accept", 18, 18));
            buttons.add(new FFDFriendsWidgets.Button(1, right - 20, 0, 20,
                    text(kind == 0 ? "gui.friends.unfriend" : kind == 1 ? "gui.friends.decline" : "gui.friends.cancel_request"),
                    kind == 0 ? "remove" : kind == 1 ? "reject" : "cancel", kind == 0 ? 13 : kind == 1 ? 18 : 12,
                    kind == 0 ? 11 : kind == 1 ? 18 : 12));
            for (GuiButton button : buttons) button.visible = false;
        }
    }

    private static final class NameField extends GuiTextField {
        NameField(int x, int y, int width) {
            super(0, FFDFriendsWidgets.font(), x, y, width, 20);
            setEnableBackgroundDrawing(false);
        }

        @Override
        public void drawTextBox() {
            if (!getVisible()) return;
            Gui.drawRect(x, y, x + width, y + height, isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0);
            Gui.drawRect(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF000000);
            int oldX = x, oldY = y, oldWidth = width, oldHeight = height;
            x += 4; y += 6; width -= 8; height = 8;
            try {
                if (getText().isEmpty()) FFDFriendsWidgets.font().drawStringWithShadow(
                        FFDFriendsWidgets.font().trimStringToWidth(text("gui.friends.enter_nickname"), width), x, y, 0x707070);
                super.drawTextBox();
            } finally { x = oldX; y = oldY; width = oldWidth; height = oldHeight; }
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            if (button == 0 && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
                int oldX = x, oldWidth = width;
                x += 4; width -= 8;
                try { return super.mouseClicked(Math.max(x, Math.min(mouseX, x + width - 1)), mouseY, button); }
                finally { x = oldX; width = oldWidth; }
            } else return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}

