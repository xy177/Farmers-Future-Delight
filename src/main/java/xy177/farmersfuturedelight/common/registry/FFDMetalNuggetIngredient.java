package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreDictionary;

public final class FFDMetalNuggetIngredient extends Ingredient {
    private static final String[] ORES = {"nuggetCopper", "nuggetIron", "nuggetGold"};

    public FFDMetalNuggetIngredient() {
        super(new ItemStack(Items.IRON_NUGGET), new ItemStack(Items.GOLD_NUGGET));
    }

    @Override
    public boolean apply(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() == Items.IRON_NUGGET || stack.getItem() == Items.GOLD_NUGGET) return true;
        for (String ore : ORES) {
            if (OreDictionary.containsMatch(false, OreDictionary.getOres(ore, false), stack)) return true;
        }
        return false;
    }

    @Override
    public ItemStack[] getMatchingStacks() {
        List<ItemStack> matches = new ArrayList<>();
        matches.add(new ItemStack(Items.IRON_NUGGET));
        matches.add(new ItemStack(Items.GOLD_NUGGET));
        for (String ore : ORES) matches.addAll(OreDictionary.getOres(ore, false));
        return matches.toArray(new ItemStack[0]);
    }

    @Override
    public boolean isSimple() {
        return false;
    }
}
