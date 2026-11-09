package xy177.farmersfuturedelight.common.tile;

import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class TileEntityGrindstone extends TileEntity implements IInventory {
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public ItemStack getResult() {
        ItemStack first = items.get(0);
        ItemStack second = items.get(1);
        if (first.isEmpty() && second.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (first.getCount() > 1 || second.getCount() > 1) {
            return ItemStack.EMPTY;
        }
        WorkstationRecipes.Recipe customRecipe = getCustomRecipe();
        if (customRecipe != null) {
            return customRecipe.assemble(customInputs());
        }
        if (first.isEmpty() || second.isEmpty()) {
            ItemStack input = first.isEmpty() ? second : first;
            if (EnchantmentHelper.getEnchantments(input).isEmpty()) {
                return ItemStack.EMPTY;
            }
            return removeNonCurses(input.copy());
        }
        if (first.getItem() != second.getItem()) {
            return ItemStack.EMPTY;
        }
        int count = 1;
        int maximumDamage = Math.max(first.getMaxDamage(), second.getMaxDamage());
        int remaining = maximumDamage - first.getItemDamage()
                + maximumDamage - second.getItemDamage() + maximumDamage * 5 / 100;
        if (!first.isItemStackDamageable()) {
            if (first.getMaxStackSize() < 2 || !ItemStack.areItemStacksEqual(first, second)) {
                return ItemStack.EMPTY;
            }
            count = 2;
        }
        ItemStack result = first.copy();
        result.setCount(count);
        if (result.isItemStackDamageable()) {
            result.setItemDamage(Math.max(maximumDamage - remaining, 0));
        }
        mergeEnchantments(result, second);
        return removeNonCurses(result);
    }

    private static void mergeEnchantments(ItemStack target, ItemStack source) {
        Map<Enchantment, Integer> merged = new HashMap<>(EnchantmentHelper.getEnchantments(target));
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(source).entrySet()) {
            Enchantment enchantment = entry.getKey();
            int incoming = entry.getValue();
            Integer current = merged.get(enchantment);
            if (enchantment.isCurse() && current != null) {
                continue;
            }
            if (current != null && current == incoming && incoming < enchantment.getMaxLevel()) {
                merged.put(enchantment, incoming + 1);
            } else if (current == null || incoming > current) {
                merged.put(enchantment, incoming);
            }
        }
        EnchantmentHelper.setEnchantments(merged, target);
    }

    private static ItemStack removeNonCurses(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = new HashMap<>(
                EnchantmentHelper.getEnchantments(stack));
        enchantments.entrySet().removeIf(entry -> !entry.getKey().isCurse());
        EnchantmentHelper.setEnchantments(enchantments, stack);
        if (stack.getItem() == Items.ENCHANTED_BOOK && enchantments.isEmpty()) {
            NBTTagCompound tag = stack.getTagCompound();
            ItemStack book = new ItemStack(Items.BOOK, stack.getCount());
            if (tag != null) {
                tag.removeTag("StoredEnchantments");
                tag.removeTag("ench");
                book.setTagCompound(tag);
            }
            stack = book;
        }
        int repairCost = 0;
        for (int index = 0; index < enchantments.size(); index++) {
            repairCost = repairCost * 2 + 1;
        }
        stack.setRepairCost(repairCost);
        return stack;
    }

    private int getExperienceAmount() {
        WorkstationRecipes.Recipe customRecipe = getCustomRecipe();
        if (customRecipe != null) {
            float configured = customRecipe.getExperience();
            int experience = (int) configured;
            if (world.rand.nextFloat() < configured - experience) {
                experience++;
            }
            return experience;
        }
        int total = getExperienceFrom(items.get(0)) + getExperienceFrom(items.get(1));
        if (total <= 0) {
            return 0;
        }
        int half = MathHelper.ceil(total / 2.0F);
        return half + world.rand.nextInt(half);
    }

    private static int getExperienceFrom(ItemStack stack) {
        int total = 0;
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            if (!entry.getKey().isCurse()) {
                total += entry.getKey().getMinEnchantability(entry.getValue());
            }
        }
        return total;
    }

    private WorkstationRecipes.Recipe getCustomRecipe() {
        ItemStack first = items.get(0);
        ItemStack second = items.get(1);
        if (first.isEmpty() && second.isEmpty()) {
            return null;
        }
        if (first.isEmpty() || second.isEmpty()) {
            ItemStack input = first.isEmpty() ? second : first;
            return WorkstationRecipes.find(WorkstationRecipes.Type.GRINDING_SINGLE,
                    java.util.Collections.singletonList(input));
        }
        return WorkstationRecipes.find(WorkstationRecipes.Type.GRINDING,
                Arrays.asList(first, second));
    }

    private java.util.List<ItemStack> customInputs() {
        if (items.get(0).isEmpty() || items.get(1).isEmpty()) {
            return java.util.Collections.singletonList(
                    items.get(0).isEmpty() ? items.get(1) : items.get(0));
        }
        return Arrays.asList(items.get(0), items.get(1));
    }

    public void takeResult(EntityPlayer player) {
        if (world == null || world.isRemote || getResult().isEmpty()) {
            return;
        }
        int experience = getExperienceAmount();
        items.set(0, ItemStack.EMPTY);
        items.set(1, ItemStack.EMPTY);
        markDirty();
        while (experience > 0) {
            int split = EntityXPOrb.getXPSplit(experience);
            experience -= split;
            world.spawnEntity(new EntityXPOrb(world, pos.getX() + 0.5D,
                    pos.getY() + 0.5D, pos.getZ() + 0.5D, split));
        }
        world.playSound(null, pos, FFDSounds.GRINDSTONE_USE,
                net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public void dropContents() {
        for (int index = 0; index < items.size(); index++) {
            ItemStack stack = items.get(index);
            if (!stack.isEmpty()) {
                world.spawnEntity(new EntityItem(world, pos.getX() + 0.5D,
                        pos.getY() + 0.5D, pos.getZ() + 0.5D, stack.copy()));
                items.set(index, ItemStack.EMPTY);
            }
        }
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty() && items.get(1).isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < items.size() ? items.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index < 0 || index >= items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ItemStackHelper.getAndSplit(items, index, count);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (index < 0 || index >= items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ItemStackHelper.getAndRemove(items, index);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        items.set(index, stack.isEmpty() ? ItemStack.EMPTY : stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
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
        if (world != null && world.getTileEntity(pos) == this) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    @Override
    public String getName() {
        return "container.farmers_future_delight.grindstone";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index < 0 || index >= 2 || stack.isEmpty()) {
            return false;
        }
        if (stack.isItemStackDamageable() || !EnchantmentHelper.getEnchantments(stack).isEmpty()) {
            return true;
        }
        for (WorkstationRecipes.Recipe recipe : WorkstationRecipes.getRecipes(
                WorkstationRecipes.Type.GRINDING_SINGLE)) {
            if (recipe.getIngredients().get(0).apply(stack)) {
                return true;
            }
        }
        for (WorkstationRecipes.Recipe recipe : WorkstationRecipes.getRecipes(
                WorkstationRecipes.Type.GRINDING)) {
            if (recipe.getIngredients().get(index).apply(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        items.set(0, ItemStack.EMPTY);
        items.set(1, ItemStack.EMPTY);
        markDirty();
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        items = NonNullList.withSize(2, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, items);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        ItemStackHelper.saveAllItems(tag, items);
        return tag;
    }
}
