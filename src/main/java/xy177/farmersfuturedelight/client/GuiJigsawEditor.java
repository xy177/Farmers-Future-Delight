package xy177.farmersfuturedelight.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Keyboard;
import xy177.farmersfuturedelight.common.block.BlockJigsaw;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.network.PacketJigsawEdit;
import xy177.farmersfuturedelight.common.tile.TileEntityJigsaw;
import xy177.farmersfuturedelight.common.worldgen.FFDJigsawPlacement;

public final class GuiJigsawEditor extends GuiScreen {
    private static final String[] KEYS = {"pool", "name", "target", "final_state",
            "selection_priority", "placement_priority"};
    private final TileEntityJigsaw tile;
    private final NBTTagCompound values;
    private final GuiTextField[] fields = new GuiTextField[6];
    private GuiButton done;
    private GuiButton generate;
    private GuiSlider levels;
    private int levelsValue;
    private boolean aligned;
    private boolean keepJigsaws = true;
    private boolean jointVisible;

    public GuiJigsawEditor(TileEntityJigsaw tile) {
        this.tile = tile;
        values = tile.getDefinition();
        values.setString("selection_priority", Integer.toString(values.getInteger("selection_priority")));
        values.setString("placement_priority", Integer.toString(values.getInteger("placement_priority")));
        aligned = "aligned".equals(values.getString("joint"));
    }

    @Override
    public void initGui() {
        if (levels != null) levelsValue = levels.getValueInt();
        for (int index = 0; index < fields.length; index++) {
            if (fields[index] != null) values.setString(KEYS[index], fields[index].getText());
        }
        buttonList.clear();
        for (int index = 0; index < fields.length; index++) {
            int fieldX = index < 4 ? width / 2 - 153 : width / 2 - 153 + (index - 4) * 103;
            int fieldY = index < 4 ? 20 + index * 35 : 160;
            int fieldWidth = index < 4 ? 300 : 98;
            GuiTextField field = new GuiTextField(index, fontRenderer, fieldX, fieldY, fieldWidth, 20);
            field.setMaxStringLength(index < 3 ? 128 : index == 3 ? 256 : 3);
            field.setText(values.getString(KEYS[index]));
            field.setFocused(index == 0);
            fields[index] = field;
        }
        jointVisible = tile.getWorld() != null
                && tile.getWorld().getBlockState(tile.getPos()).getValue(BlockJigsaw.ORIENTATION).front.getAxis()
                == net.minecraft.util.EnumFacing.Axis.Y;
        GuiButton joint = new GuiButton(2, width / 2 + 54, 160, 100, 20, jointText());
        joint.visible = jointVisible;
        joint.enabled = jointVisible;
        buttonList.add(joint);
        levels = new GuiSlider(5, width / 2 - 154, 185, 100, 20,
                I18n.format("gui.farmers_future_delight.jigsaw.levels") + ": ", "", 0, 20, levelsValue,
                false, true, slider -> {});
        buttonList.add(levels);
        buttonList.add(new GuiButton(3, width / 2 - 50, 185, 100, 20, keepText()));
        generate = addButton(new GuiButton(1, width / 2 + 54, 185, 100, 20,
                I18n.format("gui.farmers_future_delight.jigsaw.generate")));
        done = addButton(new GuiButton(0, width / 2 - 154, 210, 150, 20, I18n.format("gui.done")));
        addButton(new GuiButton(4, width / 2 + 4, 210, 150, 20, I18n.format("gui.cancel")));
        validateFields();
    }

    private String jointText() {
        return I18n.format("gui.farmers_future_delight.jigsaw." + (aligned ? "aligned" : "rollable"));
    }

    private String keepText() {
        return I18n.format("gui.farmers_future_delight.jigsaw.keep") + ": "
                + I18n.format(keepJigsaws ? "options.on" : "options.off");
    }

    @Override
    public void updateScreen() {
        for (GuiTextField field : fields) field.updateCursorCounter();
    }

    private NBTTagCompound definition() {
        NBTTagCompound data = new NBTTagCompound();
        for (int index = 0; index < 4; index++) data.setString(KEYS[index], fields[index].getText());
        data.setInteger("selection_priority", Integer.parseInt(fields[4].getText()));
        data.setInteger("placement_priority", Integer.parseInt(fields[5].getText()));
        data.setString("joint", aligned ? "aligned" : "rollable");
        return data;
    }

    private void validateFields() {
        boolean valid = true;
        try {
            for (int index = 0; index < 3; index++) {
                if (fields[index].getText().isEmpty()) throw new IllegalArgumentException();
                new net.minecraft.util.ResourceLocation(fields[index].getText());
            }
            FFDJigsawPlacement.parseFinalState(fields[3].getText());
            Integer.parseInt(fields[4].getText());
            Integer.parseInt(fields[5].getText());
        } catch (IllegalArgumentException exception) {
            valid = false;
        }
        done.enabled = valid;
        generate.enabled = valid;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 2) {
            aligned = !aligned;
            button.displayString = jointText();
        } else if (button.id == 3) {
            keepJigsaws = !keepJigsaws;
            button.displayString = keepText();
        } else if (button.id == 4) {
            mc.displayGuiScreen(null);
        } else if ((button.id == 0 || button.id == 1) && button.enabled) {
            FFDNetwork.editJigsaw(new PacketJigsawEdit(tile.getPos(), definition(),
                    button.id == 1, keepJigsaws, levels.getValueInt()));
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_TAB) {
            int selected = -1;
            for (int index = 0; index < fields.length; index++) {
                if (fields[index].isFocused()) selected = index;
                fields[index].setFocused(false);
            }
            fields[Math.floorMod(selected + (isShiftKeyDown() ? -1 : 1), fields.length)].setFocused(true);
        } else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            if (done.enabled) actionPerformed(done);
        } else {
            for (GuiTextField field : fields) field.textboxKeyTyped(typedChar, keyCode);
            super.keyTyped(typedChar, keyCode);
        }
        validateFields();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (GuiTextField field : fields) field.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        for (int index = 0; index < fields.length; index++) {
            drawString(fontRenderer, I18n.format("gui.farmers_future_delight.jigsaw." + KEYS[index]),
                    fields[index].x, index < 4 ? 10 + index * 35 : 150, 0xA0A0A0);
            fields[index].drawTextBox();
        }
        if (jointVisible) {
            drawString(fontRenderer, I18n.format("gui.farmers_future_delight.jigsaw.joint"),
                    width / 2 + 53, 150, 0xA0A0A0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
