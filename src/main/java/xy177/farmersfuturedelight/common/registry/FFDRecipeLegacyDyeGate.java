package xy177.farmersfuturedelight.common.registry;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDRecipeLegacyDyeGate extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final IRecipe delegate;

    public FFDRecipeLegacyDyeGate(IRecipe delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        return isEnabled() && allowsLegacyDyes(inventory) && delegate.matches(inventory, world);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        return isEnabled() && allowsLegacyDyes(inventory)
                ? delegate.getCraftingResult(inventory) : ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return delegate.canFit(width, height);
    }

    @Override
    public ItemStack getRecipeOutput() {
        return delegate.getRecipeOutput();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory) {
        return delegate.getRemainingItems(inventory);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return delegate.getIngredients();
    }

    @Override
    public boolean isDynamic() {
        return delegate.isDynamic();
    }

    @Override
    public String getGroup() {
        return delegate.getGroup();
    }

    private static boolean allowsLegacyDyes(InventoryCrafting inventory) {
        if (FFDConfig.legacyDyeCompatibility) return true;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() == Items.DYE && isSeparatedColor(stack.getMetadata())) return false;
        }
        return true;
    }

    private boolean isEnabled() {
        return !(delegate instanceof net.minecraft.item.crafting.RecipesBanners.RecipeAddPattern)
                || !FFDItems.isItemRegistered(FFDItems.LOOM);
    }

    private static boolean isSeparatedColor(int metadata) {
        return metadata == EnumDyeColor.WHITE.getDyeDamage()
                || metadata == EnumDyeColor.BLACK.getDyeDamage()
                || metadata == EnumDyeColor.BLUE.getDyeDamage()
                || metadata == EnumDyeColor.BROWN.getDyeDamage();
    }

}
