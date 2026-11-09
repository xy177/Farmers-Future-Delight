package xy177.farmersfuturedelight.common.registry;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipesArmorDyes;
import net.minecraft.world.World;
import net.minecraftforge.oredict.DyeUtils;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.item.ItemLeatherHorseArmor;

public final class FFDRecipeArmorDyes extends RecipesArmorDyes {
    private static final int[] DYE_COLORS = {
            0xF9FFFE, 16351261, 13061821, 3847130, 16701501, 8439583, 15961002, 4673362,
            0x9D9D97, 1481884, 8991416, 3949738, 8606770, 6192150, 11546150, 0x1D1D21
    };
    private final boolean horseArmor;

    public FFDRecipeArmorDyes(boolean horseArmor) {
        this.horseArmor = horseArmor;
    }

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        return !getCraftingResult(inventory).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack output = ItemStack.EMPTY;
        int red = 0;
        int green = 0;
        int blue = 0;
        int brightness = 0;
        int samples = 0;
        boolean hasDye = false;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            int color;
            if (isArmor(stack)) {
                if (!output.isEmpty()) return ItemStack.EMPTY;
                output = stack.copy();
                output.setCount(1);
                if (horseArmor) {
                    if (!ItemLeatherHorseArmor.hasColor(stack)) continue;
                    color = ItemLeatherHorseArmor.getColor(stack);
                } else {
                    ItemArmor armor = (ItemArmor) stack.getItem();
                    if (!armor.hasColor(stack)) continue;
                    color = armor.getColor(stack);
                }
            } else {
                EnumDyeColor dye = dyeColor(stack);
                if (dye == null) return ItemStack.EMPTY;
                color = DYE_COLORS[dye.getMetadata()];
                hasDye = true;
            }
            int colorRed = color >> 16 & 255;
            int colorGreen = color >> 8 & 255;
            int colorBlue = color & 255;
            red += colorRed;
            green += colorGreen;
            blue += colorBlue;
            brightness += Math.max(colorRed, Math.max(colorGreen, colorBlue));
            samples++;
        }
        if (output.isEmpty() || !hasDye) return ItemStack.EMPTY;
        red /= samples;
        green /= samples;
        blue /= samples;
        float averageBrightness = (float) brightness / samples;
        int maximum = Math.max(red, Math.max(green, blue));
        if (maximum > 0) {
            red = (int) (red * averageBrightness / maximum);
            green = (int) (green * averageBrightness / maximum);
            blue = (int) (blue * averageBrightness / maximum);
        }
        int result = red << 16 | green << 8 | blue;
        if (horseArmor) ItemLeatherHorseArmor.setColor(output, result);
        else ((ItemArmor) output.getItem()).setColor(output, result);
        return output;
    }

    private boolean isArmor(ItemStack stack) {
        if (horseArmor) return stack.getItem() == FFDItems.LEATHER_HORSE_ARMOR;
        return stack.getItem() instanceof ItemArmor
                && ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;
    }

    private static EnumDyeColor dyeColor(ItemStack stack) {
        if (stack.getItem() == Items.DYE && !FFDConfig.legacyDyeCompatibility) {
            EnumDyeColor color = EnumDyeColor.byDyeDamage(stack.getMetadata());
            if (color == EnumDyeColor.WHITE || color == EnumDyeColor.BLACK
                    || color == EnumDyeColor.BLUE || color == EnumDyeColor.BROWN) return null;
        }
        return DyeUtils.colorFromStack(stack).orElse(null);
    }
}
