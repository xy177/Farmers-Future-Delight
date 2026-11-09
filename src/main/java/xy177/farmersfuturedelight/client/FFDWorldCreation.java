package xy177.farmersfuturedelight.client;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDPortingGameRules;
import xy177.farmersfuturedelight.core.FFDGameplayHooks;
import xy177.farmersfuturedelight.client.gui.FFDGuiGameRules;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class FFDWorldCreation {
    private static final int FIRST_TAB = 19810;
    private static final int DIFFICULTY = 19813;
    private static final int RULES = 19814;
    private static final Map<GuiCreateWorld, State> STATES = new WeakHashMap<>();
    private static final ThreadLocal<State> CREATING = new ThreadLocal<>();
    private static final Method MORE = ObfuscationReflectionHelper.findMethod(GuiCreateWorld.class,
            "func_146316_a", Void.TYPE, Boolean.TYPE);

    private FFDWorldCreation() { }

    private static final class State {
        int tab;
        EnumDifficulty difficulty = EnumDifficulty.NORMAL;
        final GameRules rules = defaults();
        List<GuiButton> buttons;
    }

    public static GameRules defaults() {
        GameRules rules = new GameRules();
        FFDGameplayHooks.ensureSleepingPercentageRule(rules);
        FFDGameplayHooks.ensurePortingGameRules(rules);
        FFDPortingGameRules.ensure(rules);
        return rules;
    }

    private static boolean enabled(GuiScreen screen) {
        return FFDConfig.modernCreateWorldScreen && screen.getClass() == GuiCreateWorld.class;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void initialize(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!enabled(event.getGui())) return;
        GuiCreateWorld screen = (GuiCreateWorld) event.getGui();
        State state = STATES.computeIfAbsent(screen, key -> new State());
        state.buttons = event.getButtonList();
        for (int i = 0; i < 3; i++) state.buttons.add(new TabButton(FIRST_TAB + i,
                I18n.format("ffd.create.tab." + i)));
        state.buttons.add(new GuiButton(DIFFICULTY, 0, 0, 210, 20, ""));
        state.buttons.add(new GuiButton(RULES, 0, 0, 210, 20, I18n.format("ffd.create.rules")));
        layout(screen, state);
    }

    private static <T> T get(GuiCreateWorld screen, String name) {
        return ObfuscationReflectionHelper.getPrivateValue(GuiCreateWorld.class, screen, name);
    }

    private static void layout(GuiCreateWorld screen, State state) {
        try { MORE.invoke(screen, state.tab != 0); }
        catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
        GuiTextField name = get(screen, "field_146333_g"), seed = get(screen, "field_146335_h");
        int center = screen.width / 2;
        int contentHeight = state.tab == 0 ? 117 : state.tab == 1 ? 113 : 76;
        int first = 24 + (int) ((screen.height - 33 - 24 - contentHeight) / 6.0F);
        int tabsWidth = Math.min(400, screen.width) - 28;
        int tabWidth = ((tabsWidth / 3 + 1) / 2) * 2;
        int tabsLeft = (((screen.width - tabsWidth) / 2 + 1) / 2) * 2;
        name.x = center - 104;
        name.y = first + 13;
        name.width = 208;
        name.setVisible(state.tab == 0);
        seed.x = center - 154;
        seed.y = first + 41;
        seed.width = 308;
        seed.setVisible(state.tab == 1);
        boolean hardcore = get(screen, "field_146337_w");
        WorldType type = WorldType.WORLD_TYPES[(Integer) get(screen, "field_146331_K")];
        for (GuiButton b : state.buttons) {
            if (b.id >= FIRST_TAB && b.id < FIRST_TAB + 3) {
                b.x = tabsLeft + (b.id - FIRST_TAB) * tabWidth;
                b.width = tabWidth;
                ((TabButton) b).selected = b.id - FIRST_TAB == state.tab;
                b.enabled = true;
                continue;
            }
            if (b.id > 8 && b.id != DIFFICULTY && b.id != RULES) continue;
            if (b.id == 0 || b.id == 1) {
                b.x = (screen.width - 308) / 2 + (b.id == 0 ? 0 : 158);
                b.y = screen.height - 27;
                continue;
            }
            b.visible = false;
            if (state.tab == 0 && (b.id == 2 || b.id == 6 || b.id == DIFFICULTY)) {
                b.visible = true; b.x = center - 105; b.width = 210;
                b.y = first + (b.id == 2 ? 41 : b.id == DIFFICULTY ? 69 : 97);
                if (b.id == DIFFICULTY) {
                    b.enabled = !hardcore;
                    b.displayString = I18n.format("options.difficulty") + ": "
                            + I18n.format((hardcore ? EnumDifficulty.HARD : state.difficulty).getDifficultyResourceKey());
                }
                if (b.id == 6) {
                    b.enabled = !hardcore && type != WorldType.DEBUG_ALL_BLOCK_STATES;
                    b.displayString = I18n.format("ffd.create.allow_commands") + ": "
                            + I18n.format((Boolean) get(screen, "field_146340_t") && !hardcore ? "options.on" : "options.off");
                }
            } else if (state.tab == 1 && (b.id == 4 || b.id == 5 || b.id == 7 || b.id == 8)) {
                b.visible = true;
                if (b.id == 4 && type == WorldType.CUSTOMIZED) b.visible = false;
                b.x = center + (b.id == 8 ? 5 : b.id == 4 || b.id == 7 ? 111 : -155);
                b.width = b.id == 4 || b.id == 7 ? 44 : 150;
                b.y = first + (b.id == 4 ? 69 : b.id == 7 ? 93 : 0);
                if (b.id == 8) b.enabled = type.isCustomizable() && type != WorldType.DEBUG_ALL_BLOCK_STATES;
                if (b.id == 4) b.enabled = type != WorldType.DEBUG_ALL_BLOCK_STATES;
                if (b.id == 7) b.enabled = !hardcore && type != WorldType.DEBUG_ALL_BLOCK_STATES;
            } else if (state.tab == 2 && b.id == RULES) {
                b.visible = true; b.x = center - 105; b.width = 210; b.y = first;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void action(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (!enabled(event.getGui()) || !event.getButton().enabled) return;
        GuiCreateWorld screen = (GuiCreateWorld) event.getGui();
        State state = STATES.get(screen);
        if (state == null) return;
        int id = event.getButton().id;
        if (id >= FIRST_TAB && id < FIRST_TAB + 3) {
            event.setCanceled(true);
            if (state.tab == id - FIRST_TAB) return;
            state.tab = id - FIRST_TAB;
            ((GuiTextField) get(screen, "field_146333_g")).setFocused(state.tab == 0);
            ((GuiTextField) get(screen, "field_146335_h")).setFocused(state.tab == 1);
            layout(screen, state);
        } else if (id == DIFFICULTY) {
            event.setCanceled(true);
            state.difficulty = EnumDifficulty.getDifficultyEnum(state.difficulty.getDifficultyId() + 1);
            layout(screen, state);
        } else if (id == RULES) {
            event.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(new FFDGuiGameRules(screen, state.rules));
        }
    }

    @SubscribeEvent
    public static void afterAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (enabled(event.getGui()) && STATES.containsKey(event.getGui()))
            layout((GuiCreateWorld) event.getGui(), STATES.get(event.getGui()));
    }

    public static boolean draw(GuiCreateWorld screen, int mouseX, int mouseY, float partialTicks) {
        if (!enabled(screen)) return false;
        State state = STATES.get(screen);
        if (state == null) return false;
        screen.drawDefaultBackground();
        tile("tab_header_background", 0, 0, screen.width, 24, 16, 16);
        tile("footer_separator", 0, screen.height - 35, screen.width, 2, 32, 2);
        GuiButton firstTab = null, lastTab = null;
        for (GuiButton b : state.buttons) if (b instanceof TabButton) {
            if (firstTab == null) firstTab = b;
            lastTab = b;
        }
        if (firstTab != null) {
            tile("header_separator", 0, 22, firstTab.x, 2, 32, 2);
            tile("header_separator", lastTab.x + lastTab.width, 22,
                    screen.width - lastTab.x - lastTab.width, 2, 32, 2);
        }
        GuiTextField input = get(screen, state.tab == 0 ? "field_146333_g" : "field_146335_h");
        net.minecraft.client.gui.FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        if (state.tab != 2) {
            font.drawStringWithShadow(I18n.format(state.tab == 0 ? "selectWorld.enterName" : "selectWorld.enterSeed"),
                    input.x, input.y - 13, 0xFFFFFF);
            drawInput(input);
        }
        for (GuiButton b : state.buttons) {
            if (b.getClass() != GuiButton.class || b.id > 8 && b.id != DIFFICULTY && b.id != RULES) {
                b.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
            } else if (b.visible) {
                String text = b.displayString;
                if (state.tab == 1 && (b.id == 4 || b.id == 7)) {
                    font.drawStringWithShadow(I18n.format(b.id == 4 ? "ffd.create.structures" : "ffd.create.bonus_chest"),
                            screen.width / 2 - 155, b.y + 6, b.enabled ? 0xFFFFFF : 0xA0A0A0);
                    boolean on = b.id == 4 ? get(screen, "field_146341_s")
                            : (Boolean) get(screen, "field_146338_v") && !(Boolean) get(screen, "field_146337_w");
                    text = I18n.format(on ? "options.on" : "options.off");
                }
                drawButton(b, text, mouseX, mouseY);
            }
        }
        return true;
    }

    private static void drawInput(GuiTextField input) {
        if (!input.getVisible()) return;
        widget(input.isFocused() ? "text_field_highlighted" : "text_field",
                input.x, input.y, input.width, input.height, 200, 20, 1, 1, 1, 1);
        int x = input.x, y = input.y, width = input.width;
        boolean background = input.getEnableBackgroundDrawing();
        try {
            input.setEnableBackgroundDrawing(false);
            input.x += 4;
            input.y += (input.height - 8) / 2;
            input.width -= 8;
            input.drawTextBox();
        } finally {
            input.x = x;
            input.y = y;
            input.width = width;
            input.setEnableBackgroundDrawing(background);
        }
    }

    private static void tile(String texture, int x, int y, int width, int height, int tileWidth, int tileHeight) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        FFDModernMenus.tile(texture, 0, 0, width, height, tileWidth, tileHeight);
        GlStateManager.popMatrix();
    }

    private static void drawButton(GuiButton button, String text, int mouseX, int mouseY) {
        boolean hover = mouseX >= button.x && mouseX < button.x + button.width
                && mouseY >= button.y && mouseY < button.y + button.height;
        String texture = !button.enabled ? "button_disabled" : hover ? "button_highlighted" : "button";
        int border = button.enabled ? 3 : 1;
        widget(texture, button.x, button.y, button.width, button.height, 200, 20, border, border, border, border);
        button.drawCenteredString(Minecraft.getMinecraft().fontRenderer, text,
                button.x + button.width / 2, button.y + (button.height - 8) / 2,
                button.enabled ? 0xFFFFFF : 0xA0A0A0);
    }

    private static void widget(String texture, int x, int y, int width, int height,
                               int sourceWidth, int sourceHeight, int left, int top, int right, int bottom) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/gui/create_world/" + texture + ".png"));
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1, 1, 1, 1);
        int[] xs = {x, x + left, x + width - right, x + width};
        int[] ys = {y, y + top, y + height - bottom, y + height};
        int[] us = {0, left, sourceWidth - right, sourceWidth};
        int[] vs = {0, top, sourceHeight - bottom, sourceHeight};
        for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++) {
            int tileWidth = us[column + 1] - us[column], tileHeight = vs[row + 1] - vs[row];
            if (tileWidth <= 0 || tileHeight <= 0) continue;
            for (int dy = ys[row]; dy < ys[row + 1]; dy += tileHeight)
                for (int dx = xs[column]; dx < xs[column + 1]; dx += tileWidth) {
                    int w = Math.min(tileWidth, xs[column + 1] - dx);
                    int h = Math.min(tileHeight, ys[row + 1] - dy);
                    Gui.drawScaledCustomSizeModalRect(dx, dy, us[column], vs[row], w, h,
                            w, h, sourceWidth, sourceHeight);
                }
        }
        GlStateManager.disableBlend();
    }

    public static void launch(Minecraft mc, String folder, String name, WorldSettings settings, GuiCreateWorld screen) {
        State state = STATES.get(screen);
        if (state != null && enabled(screen)) CREATING.set(state);
        try { mc.launchIntegratedServer(folder, name, settings); }
        finally { CREATING.remove(); }
    }

    public static WorldInfo prepare(WorldInfo info) {
        State state = CREATING.get();
        if (state != null) {
            info.getGameRulesInstance().readFromNBT(state.rules.writeToNBT());
            info.setDifficulty(info.isHardcoreModeEnabled() ? EnumDifficulty.HARD : state.difficulty);
        }
        return info;
    }

    public static void recreate(GuiCreateWorld screen, WorldInfo original) {
        if (!enabled(screen)) return;
        State state = STATES.computeIfAbsent(screen, key -> new State());
        state.rules.readFromNBT(original.getGameRulesInstance().writeToNBT());
        if (original.getDifficulty() != null) state.difficulty = original.getDifficulty();
    }

    private static final class TabButton extends GuiButton {
        boolean selected;
        TabButton(int id, String text) { super(id, 0, 0, 124, 24, text); }
        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!visible) return;
            hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
            widget(selected ? hovered ? "tab_selected_highlighted" : "tab_selected"
                    : hovered ? "tab_highlighted" : "tab", x, y, width, height, 130, 24, 2, 2, 2, 0);
            if (selected) {
                tile("menu_background", x + 2, y + 2, width - 4, height - 2, 32, 32);
                int lineWidth = Math.min(mc.fontRenderer.getStringWidth(displayString), width - 4);
                int lineX = x + (width - lineWidth) / 2;
                Gui.drawRect(lineX, y + height - 2, lineX + lineWidth, y + height - 1, 0xFFFFFFFF);
            }
            drawCenteredString(mc.fontRenderer, displayString, x + width / 2,
                    y + (selected ? 0 : 3) + (height - (selected ? 0 : 3) - 9) / 2, 0xFFFFFF);
        }

        @Override
        public void playPressSound(net.minecraft.client.audio.SoundHandler soundHandler) { }
    }
}
