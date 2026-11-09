package xy177.farmersfuturedelight.common;

import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public final class FFDSignText {
    private static final String GLOW_KEY = "glowingText";
    private static final String BACK_GLOW_KEY = "glowingBackText";
    private static final String WAXED_KEY = "waxedText";
    private static final String BACK_TEXT_PREFIX = "backText";

    private FFDSignText() {
    }

    public static boolean isGlowing(TileEntitySign sign) {
        return isGlowing(sign, false);
    }

    public static boolean isGlowing(TileEntitySign sign, boolean back) {
        return sign.getTileData().getBoolean(back ? BACK_GLOW_KEY : GLOW_KEY);
    }

    public static boolean setGlowing(TileEntitySign sign, boolean glowing) {
        return setGlowing(sign, glowing, false);
    }

    public static boolean setGlowing(TileEntitySign sign, boolean glowing, boolean back) {
        if (isGlowing(sign, back) == glowing || isWaxed(sign)) {
            return false;
        }
        sign.getTileData().setBoolean(back ? BACK_GLOW_KEY : GLOW_KEY, glowing);
        sign.markDirty();
        return true;
    }

    public static boolean applyDye(TileEntitySign sign, EnumDyeColor dye) {
        return applyDye(sign, dye, false);
    }

    public static boolean applyDye(TileEntitySign sign, EnumDyeColor dye, boolean back) {
        if (isWaxed(sign)) {
            return false;
        }
        TextFormatting formatting = textFormatting(dye);
        ITextComponent[] text = back ? getBackText(sign) : sign.signText;
        boolean changed = false;
        for (int i = 0; i < text.length; i++) {
            ITextComponent original = text[i];
            if (original == null) {
                original = new TextComponentString("");
            }
            ITextComponent copy = recolor(original, formatting);
            if (!copy.equals(original)) {
                changed = true;
            }
            text[i] = copy;
        }
        if (changed) {
            if (back) {
                setBackText(sign, text);
            }
            sign.markDirty();
        }
        return changed;
    }

    public static ITextComponent[] getBackText(TileEntitySign sign) {
        NBTTagCompound data = sign.getTileData();
        ITextComponent[] result = new ITextComponent[4];
        for (int index = 0; index < result.length; index++) {
            String key = BACK_TEXT_PREFIX + (index + 1);
            if (!data.hasKey(key, 8)) {
                result[index] = new TextComponentString("");
                continue;
            }
            try {
                ITextComponent parsed = ITextComponent.Serializer.jsonToComponent(data.getString(key));
                result[index] = parsed == null ? new TextComponentString("") : parsed;
            } catch (RuntimeException exception) {
                result[index] = new TextComponentString("");
            }
        }
        return result;
    }

    public static void setBackText(TileEntitySign sign, ITextComponent[] text) {
        NBTTagCompound data = sign.getTileData();
        for (int index = 0; index < 4; index++) {
            ITextComponent component = index < text.length && text[index] != null
                    ? text[index] : new TextComponentString("");
            data.setString(BACK_TEXT_PREFIX + (index + 1),
                    ITextComponent.Serializer.componentToJson(component));
        }
        sign.markDirty();
    }

    public static boolean isWaxed(TileEntitySign sign) {
        return sign.getTileData().getBoolean(WAXED_KEY);
    }

    public static boolean setWaxed(TileEntitySign sign, boolean waxed) {
        if (isWaxed(sign) == waxed) {
            return false;
        }
        sign.getTileData().setBoolean(WAXED_KEY, waxed);
        sign.markDirty();
        return true;
    }

    public static boolean isBackSide(TileEntitySign sign, EntityPlayer player) {
        int metadata = sign.getBlockMetadata();
        double yaw;
        if (sign.getBlockType() instanceof BlockStandingSign
                || sign.getBlockType() == Blocks.STANDING_SIGN) {
            yaw = metadata * 22.5D;
        } else if (sign.getBlockType() instanceof BlockWallSign || sign.getBlockType() == Blocks.WALL_SIGN) {
            yaw = metadata == 2 ? 180.0D : metadata == 4 ? 90.0D
                    : metadata == 5 ? -90.0D : 0.0D;
        } else {
            return false;
        }
        double radians = Math.toRadians(yaw);
        double frontX = Math.sin(radians);
        double frontZ = -Math.cos(radians);
        double dx = player.posX - (sign.getPos().getX() + 0.5D);
        double dz = player.posZ - (sign.getPos().getZ() + 0.5D);
        return dx * frontX + dz * frontZ < 0.0D;
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
