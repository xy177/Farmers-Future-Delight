package xy177.farmersfuturedelight.common.tile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.block.BlockCookingFurnace;
import xy177.farmersfuturedelight.common.inventory.ContainerCookingFurnace;
import xy177.farmersfuturedelight.common.registry.FFDCookingRecipeBook;

public class TileEntityCookingFurnace extends TileEntityLockable implements ISidedInventory, ITickable {
    private static final int[] TOP_SLOTS = {0};
    private static final int[] BOTTOM_SLOTS = {2, 1};
    private static final int[] SIDE_SLOTS = {1};
    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private final Map<ResourceLocation, ExperienceEntry> recipesUsed = new LinkedHashMap<>();
    private final IItemHandler topHandler = new SidedInvWrapper(this, EnumFacing.UP);
    private final IItemHandler bottomHandler = new SidedInvWrapper(this, EnumFacing.DOWN);
    private final IItemHandler sideHandler = new SidedInvWrapper(this, EnumFacing.NORTH);
    private boolean smoking;
    private String customName;
    private int burnTime;
    private int fuelTime;
    private int cookTime;
    private int totalCookTime = 100;
    private List<WorkstationRecipes.Recipe> cachedRecipes;
    private ItemStack cachedInput = ItemStack.EMPTY;
    private WorkstationRecipes.Recipe cachedRecipe;
    private ResourceLocation activeRecipe;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public TileEntityCookingFurnace() {
    }

    public TileEntityCookingFurnace(boolean smoking) {
        this.smoking = smoking;
    }

    public WorkstationRecipes.Type getRecipeType() {
        return smoking ? WorkstationRecipes.Type.SMOKING : WorkstationRecipes.Type.BLASTING;
    }

    public boolean isSmoking() {
        return smoking;
    }

    @Nullable
    public WorkstationRecipes.Recipe getRecipe(ItemStack input) {
        List<WorkstationRecipes.Recipe> recipes = WorkstationRecipes.getRecipes(getRecipeType());
        if (recipes != cachedRecipes || !sameItem(cachedInput, input)) {
            cachedRecipes = recipes;
            cachedInput = input.copy();
            cachedRecipe = WorkstationRecipes.find(getRecipeType(), Collections.singletonList(input));
        }
        return cachedRecipe;
    }

    private static boolean sameItem(ItemStack first, ItemStack second) {
        return first.isEmpty() && second.isEmpty() || !first.isEmpty() && !second.isEmpty()
                && ItemStack.areItemsEqual(first, second) && ItemStack.areItemStackTagsEqual(first, second);
    }

    public static int getFuelTime(ItemStack fuel) {
        return Math.max(0, TileEntityFurnace.getItemBurnTime(fuel) / 2);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        boolean wasBurning = burnTime > 0;
        boolean changed = false;
        if (wasBurning) {
            burnTime--;
        }
        ItemStack input = items.get(0);
        ItemStack fuel = items.get(1);
        if (burnTime > 0 || !input.isEmpty() && !fuel.isEmpty()) {
            WorkstationRecipes.Recipe recipe = getRecipe(input);
            ResourceLocation nextRecipe = recipe == null ? null : recipe.getId();
            if (activeRecipe == null ? nextRecipe != null : !activeRecipe.equals(nextRecipe)) {
                activeRecipe = nextRecipe;
                cookTime = 0;
            }
            ItemStack result = recipe == null ? ItemStack.EMPTY
                    : recipe.assemble(Collections.singletonList(input));
            if (canCook(result)) {
                totalCookTime = recipe.getProcessingTime();
                if (burnTime == 0) {
                    burnTime = getFuelTime(fuel);
                    fuelTime = burnTime;
                    if (burnTime > 0) {
                        consumeFuel(fuel);
                        changed = true;
                    }
                }
                if (burnTime > 0) {
                    cookTime++;
                    if (cookTime >= totalCookTime) {
                        cookTime = 0;
                        if (items.get(2).isEmpty()) {
                            items.set(2, result.copy());
                        } else {
                            items.get(2).grow(result.getCount());
                        }
                        input.shrink(1);
                        ExperienceEntry used = recipesUsed.get(recipe.getId());
                        if (used == null) {
                            recipesUsed.put(recipe.getId(), new ExperienceEntry(1, recipe.getExperience()));
                        } else if (used.count < Integer.MAX_VALUE) {
                            used.count++;
                        }
                        changed = true;
                    }
                } else {
                    cookTime = 0;
                }
            } else {
                cookTime = 0;
            }
        } else if (cookTime > 0) {
            cookTime = Math.max(0, cookTime - 2);
        }
        boolean burning = burnTime > 0;
        if (wasBurning != burning) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockCookingFurnace) {
                world.setBlockState(pos, state.withProperty(BlockCookingFurnace.LIT, burning), 3);
            }
            changed = true;
        }
        if (changed) {
            markDirty();
        }
    }

    private boolean canCook(ItemStack result) {
        if (result.isEmpty()) {
            return false;
        }
        ItemStack output = items.get(2);
        int maximum = Math.min(getInventoryStackLimit(), result.getMaxStackSize());
        return result.getCount() <= maximum && (output.isEmpty() || sameItem(output, result)
                && (long) output.getCount() + result.getCount() <= maximum);
    }

    private void consumeFuel(ItemStack fuel) {
        ItemStack remainder = fuel.getItem() == Items.LAVA_BUCKET
                ? new ItemStack(Items.BUCKET) : fuel.getItem().getContainerItem(fuel.copy());
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            items.set(1, remainder);
        } else if (!remainder.isEmpty()) {
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), remainder);
        }
    }

    public void grantUsedRecipes(EntityPlayerMP player) {
        if (world != null && !world.isRemote) {
            FFDCookingRecipeBook.grant(player, getRecipeType(), recipesUsed.keySet());
        }
    }

    public void releaseExperience(double x, double y, double z) {
        if (world == null || world.isRemote) {
            return;
        }
        for (ExperienceEntry entry : recipesUsed.values()) {
            double value = (double) entry.count * entry.experience;
            int amount = (int) Math.min(Integer.MAX_VALUE, Math.floor(value));
            if (amount < Integer.MAX_VALUE && world.rand.nextDouble() < value - Math.floor(value)) {
                amount++;
            }
            while (amount > 0) {
                int split = EntityXPOrb.getXPSplit(amount);
                amount -= split;
                world.spawnEntity(new EntityXPOrb(world, x, y, z, split));
            }
        }
        recipesUsed.clear();
        markDirty();
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setBoolean("Smoking", smoking);
        if (hasCustomName()) tag.setString("CustomName", customName);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        items = NonNullList.withSize(3, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, items);
        if (tag.hasKey("Smoking", 1)) smoking = tag.getBoolean("Smoking");
        burnTime = Math.max(0, tag.getInteger("BurnTime"));
        fuelTime = Math.max(0, tag.getInteger("FuelTime"));
        cookTime = Math.max(0, tag.getInteger("CookTime"));
        totalCookTime = Math.max(1, tag.getInteger("CookTimeTotal"));
        customName = tag.hasKey("CustomName", 8) ? tag.getString("CustomName") : null;
        activeRecipe = tag.hasKey("ActiveRecipe", 8) ? new ResourceLocation(tag.getString("ActiveRecipe")) : null;
        cachedRecipes = null;
        recipesUsed.clear();
        NBTTagList used = tag.getTagList("RecipesUsed", 10);
        for (int index = 0; index < used.tagCount(); index++) {
            NBTTagCompound entry = used.getCompoundTagAt(index);
            int count = entry.getInteger("Count");
            float experience = entry.getFloat("Experience");
            if (count > 0 && Float.isFinite(experience) && experience >= 0.0F) {
                recipesUsed.put(new ResourceLocation(entry.getString("Id")), new ExperienceEntry(count, experience));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        ItemStackHelper.saveAllItems(tag, items);
        tag.setBoolean("Smoking", smoking);
        tag.setInteger("BurnTime", burnTime);
        tag.setInteger("FuelTime", fuelTime);
        tag.setInteger("CookTime", cookTime);
        tag.setInteger("CookTimeTotal", totalCookTime);
        if (hasCustomName()) {
            tag.setString("CustomName", customName);
        }
        if (activeRecipe != null) {
            tag.setString("ActiveRecipe", activeRecipe.toString());
        }
        NBTTagList used = new NBTTagList();
        for (Map.Entry<ResourceLocation, ExperienceEntry> entry : recipesUsed.entrySet()) {
            NBTTagCompound value = new NBTTagCompound();
            value.setString("Id", entry.getKey().toString());
            value.setInteger("Count", entry.getValue().count);
            value.setFloat("Experience", entry.getValue().experience);
            used.appendTag(value);
        }
        tag.setTag("RecipesUsed", used);
        return tag;
    }

    @Override
    public int getSizeInventory() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack result = ItemStackHelper.getAndSplit(items, index, count);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack result = ItemStackHelper.getAndRemove(items, index);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        boolean same = sameItem(items.get(index), stack);
        items.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        if (index == 0 && !same) {
            cookTime = 0;
            WorkstationRecipes.Recipe recipe = getRecipe(stack);
            totalCookTime = recipe == null ? 100 : recipe.getProcessingTime();
            activeRecipe = recipe == null ? null : recipe.getId();
        }
        markDirty();
    }

    @Override
    public String getName() {
        return hasCustomName() ? customName : "container.farmers_future_delight."
                + (smoking ? "smoker" : "blast_furnace");
    }

    @Override
    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    public void setCustomName(String name) {
        customName = name;
        markDirty();
    }

    @Override
    public String getGuiID() {
        return FarmerFutureDelight.MODID + ":cooking_furnace";
    }

    @Override
    public Container createContainer(InventoryPlayer inventory, EntityPlayer player) {
        return new ContainerCookingFurnace(inventory, this);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 2) return false;
        if (index == 1) return getFuelTime(stack) > 0
                || stack.getItem() == Items.BUCKET && items.get(1).getItem() != Items.BUCKET;
        return index == 0;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing face) {
        return face == EnumFacing.UP ? TOP_SLOTS : face == EnumFacing.DOWN ? BOTTOM_SLOTS : SIDE_SLOTS;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, @Nullable EnumFacing face) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing face) {
        return face != EnumFacing.DOWN || index != 1
                || stack.getItem() == Items.BUCKET || stack.getItem() == Items.WATER_BUCKET;
    }

    @Override
    public int getField(int index) {
        switch (index) {
            case 0: return burnTime;
            case 1: return fuelTime;
            case 2: return cookTime;
            case 3: return totalCookTime;
            default: return 0;
        }
    }

    @Override
    public void setField(int index, int value) {
        switch (index) {
            case 0: burnTime = value; break;
            case 1: fuelTime = value; break;
            case 2: cookTime = value; break;
            case 3: totalCookTime = value; break;
            default: break;
        }
    }

    @Override
    public int getFieldCount() {
        return 4;
    }

    @Override
    public void clear() {
        items.clear();
        cookTime = 0;
        markDirty();
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing face) {
        if (face != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(face == EnumFacing.UP ? topHandler
                    : face == EnumFacing.DOWN ? bottomHandler : sideHandler);
        }
        return super.getCapability(capability, face);
    }

    private static final class ExperienceEntry {
        private int count;
        private final float experience;

        private ExperienceEntry(int count, float experience) {
            this.count = count;
            this.experience = experience;
        }
    }
}
