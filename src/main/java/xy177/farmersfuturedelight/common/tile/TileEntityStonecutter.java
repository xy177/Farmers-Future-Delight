package xy177.farmersfuturedelight.common.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.api.WorkstationRecipes;

public class TileEntityStonecutter extends TileEntity implements IInventory {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int selectedRecipeIndex = -1;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public List<WorkstationRecipes.Recipe> getMatchingRecipes() {
        ItemStack input = items.get(0);
        if (input.isEmpty()) {
            return Collections.emptyList();
        }
        List<ItemStack> inputs = Collections.singletonList(input);
        List<WorkstationRecipes.Recipe> matches = new ArrayList<>();
        for (WorkstationRecipes.Recipe recipe : WorkstationRecipes.getRecipes(
                WorkstationRecipes.Type.STONECUTTING)) {
            if (recipe.matches(inputs)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    public int getSelectedRecipeIndex() {
        List<WorkstationRecipes.Recipe> recipes = getMatchingRecipes();
        return selectedRecipeIndex >= 0 && selectedRecipeIndex < recipes.size()
                ? selectedRecipeIndex : -1;
    }

    public ItemStack getSelectedResult() {
        List<WorkstationRecipes.Recipe> recipes = getMatchingRecipes();
        int index = getSelectedRecipeIndex();
        return index >= 0 ? recipes.get(index).assemble(Collections.singletonList(items.get(0)))
                : ItemStack.EMPTY;
    }

    public void selectRecipe(int index) {
        List<WorkstationRecipes.Recipe> recipes = getMatchingRecipes();
        if (index >= 0 && index < recipes.size() && selectedRecipeIndex != index) {
            selectedRecipeIndex = index;
            markDirty();
        }
    }

    public boolean canSelectRecipe(int index) {
        return index >= 0 && index < getMatchingRecipes().size();
    }

    public void consumeInput() {
        if (!getSelectedResult().isEmpty() && !items.get(0).isEmpty()) {
            items.get(0).shrink(1);
            if (items.get(0).isEmpty()) {
                items.set(0, ItemStack.EMPTY);
            }
            List<WorkstationRecipes.Recipe> recipes = getMatchingRecipes();
            if (recipes.isEmpty() || selectedRecipeIndex >= recipes.size()) {
                selectedRecipeIndex = -1;
            }
            markDirty();
        }
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public String getName() {
        return "container.farmers_future_delight.stonecutter";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    public String getGuiID() {
        return "farmers_future_delight:stonecutter";
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index == 0) {
            return items.get(0);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index == 0) {
            ItemStack result = ItemStackHelper.getAndSplit(items, 0, count);
            if (!result.isEmpty()) {
                markDirty();
            }
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (index != 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ItemStackHelper.getAndRemove(items, 0);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index != 0) {
            return;
        }
        boolean sameItem = ItemStack.areItemsEqual(items.get(0), stack)
                && ItemStack.areItemStackTagsEqual(items.get(0), stack);
        items.set(0, stack.isEmpty() ? ItemStack.EMPTY : stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        if (!sameItem) {
            selectedRecipeIndex = -1;
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null && !world.isRemote && world.getTileEntity(pos) == this) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 0;
    }

    @Override
    public int getField(int id) {
        return id == 0 ? selectedRecipeIndex : 0;
    }

    @Override
    public void setField(int id, int value) {
        if (id == 0 && (value == -1 || canSelectRecipe(value))) {
            selectedRecipeIndex = value;
        }
    }

    @Override
    public int getFieldCount() {
        return 1;
    }

    @Override
    public void clear() {
        items.set(0, ItemStack.EMPTY);
        selectedRecipeIndex = -1;
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        items = NonNullList.withSize(1, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, items);
        selectedRecipeIndex = -1;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        ItemStackHelper.saveAllItems(tag, items);
        return tag;
    }
}
