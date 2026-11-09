package xy177.farmersfuturedelight.common.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeRepairItem;

public final class FFDRecipeRepairItem extends RecipeRepairItem {
    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack output = super.getCraftingResult(inventory);
        if (output.isEmpty()) {
            return output;
        }
        Map<Enchantment, Integer> curses = new LinkedHashMap<>();
        for (int index = 0; index < inventory.getSizeInventory(); index++) {
            for (Map.Entry<Enchantment, Integer> enchantment : EnchantmentHelper
                    .getEnchantments(inventory.getStackInSlot(index)).entrySet()) {
                if (enchantment.getKey().isCurse()) {
                    curses.merge(enchantment.getKey(), enchantment.getValue(), Math::max);
                }
            }
        }
        if (!curses.isEmpty()) {
            EnchantmentHelper.setEnchantments(curses, output);
        }
        return output;
    }
}
