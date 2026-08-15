package xy177.farmersfuturedelight.common;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

/** Shared sign text state and dye operations for both logical sides. */
public final class FFDSignText {
    private static final String GLOW_KEY = "glowingText";

    private FFDSignText() {
    }

    public static boolean isGlowing(TileEntitySign sign) {
        return sign.getTileData().getBoolean(GLOW_KEY);
    }

    public static boolean setGlowing(TileEntitySign sign, boolean glowing) {
        if (isGlowing(sign) == glowing) {
            return false;
        }
        sign.getTileData().setBoolean(GLOW_KEY, glowing);
        sign.markDirty();
        return true;
    }

    public static boolean applyDye(TileEntitySign sign, EnumDyeColor dye) {
        TextFormatting formatting = textFormatting(dye);
        boolean changed = false;
        for (int i = 0; i < sign.signText.length; i++) {
            ITextComponent original = sign.signText[i];
            if (original == null) {
                original = new TextComponentString("");
            }
            ITextComponent copy = recolor(original, formatting);
            if (!copy.equals(original)) {
                changed = true;
            }
            sign.signText[i] = copy;
        }
        if (changed) {
            sign.markDirty();
        }
        return changed;
    }

    private static ITextComponent recolor(ITextComponent component, TextFormatting formatting) {
        ITextComponent copy = component.createCopy();
        copy.getStyle().setColor(formatting);
        for (ITextComponent sibling : copy.getSiblings()) {
            recolorInPlace(sibling, formatting);
        }
        return copy;
    }

    private static void recolorInPlace(ITextComponent component, TextFormatting formatting) {
        component.getStyle().setColor(formatting);
        for (ITextComponent sibling : component.getSiblings()) {
            recolorInPlace(sibling, formatting);
        }
    }

    private static TextFormatting textFormatting(EnumDyeColor dye) {
        switch (dye) {
            case WHITE: return TextFormatting.WHITE;
            case ORANGE: return TextFormatting.GOLD;
            case MAGENTA: return TextFormatting.AQUA;
            case LIGHT_BLUE: return TextFormatting.BLUE;
            case YELLOW: return TextFormatting.YELLOW;
            case LIME: return TextFormatting.GREEN;
            case PINK: return TextFormatting.LIGHT_PURPLE;
            case GRAY: return TextFormatting.DARK_GRAY;
            case SILVER: return TextFormatting.GRAY;
            case CYAN: return TextFormatting.DARK_AQUA;
            case PURPLE: return TextFormatting.DARK_PURPLE;
            case BLUE: return TextFormatting.DARK_BLUE;
            case BROWN: return TextFormatting.GOLD;
            case GREEN: return TextFormatting.DARK_GREEN;
            case RED: return TextFormatting.DARK_RED;
            case BLACK: return TextFormatting.BLACK;
            default: return TextFormatting.WHITE;
        }
    }
}
