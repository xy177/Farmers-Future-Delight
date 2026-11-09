package xy177.farmersfuturedelight.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.GameRules;
import org.lwjgl.input.Keyboard;

public final class FFDGuiGameRules extends GuiScreen {
    private final GuiScreen parent;
    private final GameRules target;
    private final GameRules draft;
    private final GameRules defaults = xy177.farmersfuturedelight.client.FFDWorldCreation.defaults();
    private final List<RuleRow> rows = new ArrayList<>();
    private RuleList list;
    private GuiButton done;

    public FFDGuiGameRules(GuiScreen parent, GameRules rules) {
        this.parent = parent;
        target = rules;
        draft = xy177.farmersfuturedelight.client.FFDWorldCreation.defaults();
        draft.readFromNBT(rules.writeToNBT());
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        done = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
        addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
        rows.clear();
        for (String key : draft.getRules()) rows.add(new RuleRow(key));
        list = new RuleList();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        list.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        if (y >= 33 && y < height - 33) list.mouseClicked(x, y, button);
        for (RuleRow row : rows) {
            if (row.text != null) {
                row.text.mouseClicked(x, y, button);
                if (y < 33 || y >= height - 33) row.text.setFocused(false);
            }
        }
    }

    @Override
    protected void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        list.mouseReleased(x, y, button);
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        for (RuleRow row : rows) if (row.text != null && row.text.isFocused()) {
            row.text.textboxKeyTyped(c, key);
            if (row.valid()) draft.setOrCreateGameRule(row.key, row.text.getText());
            break;
        }
        done.enabled = rows.stream().allMatch(RuleRow::valid);
    }

    @Override
    public void updateScreen() {
        for (RuleRow row : rows) if (row.text != null) row.text.updateCursorCounter();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0 && done.enabled) target.readFromNBT(draft.writeToNBT());
        if (button.id == 0 || button.id == 1) mc.displayGuiScreen(parent);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        hovered = null;
        list.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRenderer, I18n.format("ffd.create.rules"), width / 2, 12, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (hovered != null) drawHoveringText(hovered, mouseX, mouseY);
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    private final class RuleList extends GuiListExtended {
        RuleList() { super(FFDGuiGameRules.this.mc, FFDGuiGameRules.this.width,
                FFDGuiGameRules.this.height, 33, FFDGuiGameRules.this.height - 33, 30); }
        @Override protected int getSize() { return rows.size(); }
        @Override public IGuiListEntry getListEntry(int index) { return rows.get(index); }
        @Override public int getListWidth() { return Math.min(420, FFDGuiGameRules.this.width - 30); }
        @Override protected int getScrollBarX() { return (FFDGuiGameRules.this.width + getListWidth()) / 2; }
    }

    private final class RuleRow implements GuiListExtended.IGuiListEntry {
        final String key;
        final boolean numeric;
        final GuiButton toggle;
        final GuiButton reset;
        final GuiTextField text;
        RuleRow(String key) {
            this.key = key;
            numeric = draft.areSameType(key, GameRules.ValueType.NUMERICAL_VALUE);
            boolean bool = draft.areSameType(key, GameRules.ValueType.BOOLEAN_VALUE);
            toggle = bool ? new GuiButton(0, 0, 0, 70, 20, label()) : null;
            reset = new GuiButton(1, 0, 0, 48, 20, I18n.format("controls.reset"));
            text = bool ? null : new GuiTextField(0, fontRenderer, 0, 0, 70, 20);
            if (text != null) { text.setMaxStringLength(256); text.setText(draft.getString(key)); }
        }
        String label() { return I18n.format(draft.getBoolean(key) ? "options.on" : "options.off"); }
        boolean valid() {
            if (text == null || !numeric) return true;
            try {
                int value = Integer.parseInt(text.getText());
                if (key.equals("randomTickSpeed") || key.equals("spawnRadius") || key.equals("maxEntityCramming")
                        || key.equals("maxCommandChainLength") || key.equals("playersSleepingPercentage")) return value >= 0;
                return true;
            } catch (NumberFormatException ex) { return false; }
        }
        @Override
        public void drawEntry(int index, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean selected, float partialTicks) {
            String translation = "ffd.gamerule." + key;
            String title = I18n.hasKey(translation) ? I18n.format(translation) : key;
            String clipped = fontRenderer.trimStringToWidth(title, listWidth - 132);
            fontRenderer.drawString(clipped, x, y + 6, 0xFFFFFF);
            int inputX = x + listWidth - 126;
            if (text != null) {
                text.x = inputX; text.y = y;
                text.setTextColor(valid() ? 0xFFFFFF : 0xFF5555);
                text.drawTextBox();
            } else {
                toggle.x = inputX; toggle.y = y;
                toggle.drawButton(mc, mouseX, mouseY, partialTicks);
            }
            reset.x = x + listWidth - 52; reset.y = y;
            reset.enabled = defaults.hasRule(key) && !(text == null ? draft.getString(key) : text.getText()).equals(defaults.getString(key));
            reset.drawButton(mc, mouseX, mouseY, partialTicks);
            if (mouseX >= x && mouseX < inputX - 3 && mouseY >= y && mouseY < y + 20
                    && mouseY >= 33 && mouseY < height - 33) {
                hovered = key;
            }
        }
        @Override
        public boolean mousePressed(int index, int mouseX, int mouseY, int button, int relX, int relY) {
            if (button != 0) return false;
            if (toggle != null && toggle.mousePressed(mc, mouseX, mouseY)) {
                draft.setOrCreateGameRule(key, Boolean.toString(!draft.getBoolean(key)));
                toggle.displayString = label(); return true;
            }
            if (reset.mousePressed(mc, mouseX, mouseY)) {
                draft.setOrCreateGameRule(key, defaults.getString(key));
                if (text != null) text.setText(draft.getString(key));
                else toggle.displayString = label();
                done.enabled = rows.stream().allMatch(RuleRow::valid);
                return true;
            }
            return false;
        }
        @Override public void mouseReleased(int index, int x, int y, int button, int relX, int relY) {
            reset.mouseReleased(x, y);
            if (toggle != null) toggle.mouseReleased(x, y);
        }
        @Override public void updatePosition(int index, int x, int y, float partialTicks) { }
    }

    private String hovered;
}
