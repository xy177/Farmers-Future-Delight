package xy177.farmersfuturedelight.common.tile;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.storage.MapData;
import xy177.farmersfuturedelight.common.item.FFDMapItemAccess;
import xy177.farmersfuturedelight.common.item.FFDMapLockData;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class TileEntityCartographyTable extends TileEntity implements IInventory {
    public static final int NONE = 0;
    public static final int SCALE = 1;
    public static final int DUPLICATE = 2;
    public static final int LOCK = 3;
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public int getOperation() {
        ItemStack map = items.get(0);
        ItemStack additional = items.get(1);
        if (world == null || map.isEmpty() || !(map.getItem() instanceof ItemMap)) {
            return NONE;
        }
        MapData data = ((ItemMap) map.getItem()).getMapData(map, world);
        if (data == null) {
            return NONE;
        }
        boolean locked = FFDMapLockData.isLocked(world, map, data);
        if (additional.getItem() == Items.PAPER && !locked
                && data.scale < 4) {
            return SCALE;
        }
        if (additional.getItem() == Items.MAP) {
            return DUPLICATE;
        }
        if (additional.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.GLASS_PANE)
                && !locked) {
            return LOCK;
        }
        return NONE;
    }

    public MapData getMapData() {
        ItemStack map = items.get(0);
        if (world == null || map.isEmpty() || !(map.getItem() instanceof ItemMap)) {
            return null;
        }
        return ((ItemMap) map.getItem()).getMapData(map, world);
    }

    public ItemStack getResult() {
        int operation = getOperation();
        if (operation == NONE) {
            return ItemStack.EMPTY;
        }
        ItemStack result = items.get(0).copy();
        result.setCount(operation == DUPLICATE ? 2 : 1);
        return result;
    }

    public boolean takeResult(ItemStack result) {
        if (world == null || world.isRemote || result.isEmpty()) {
            return false;
        }
        int operation = getOperation();
        if (operation == NONE) {
            return false;
        }
        if (operation == SCALE) {
            FFDMapItemAccess.scaleMap(result, world);
        } else if (operation == LOCK) {
            FFDMapLockData.lockCopy(world, result);
        }
        items.get(0).shrink(1);
        items.get(1).shrink(1);
        if (items.get(0).isEmpty()) {
            items.set(0, ItemStack.EMPTY);
        }
        if (items.get(1).isEmpty()) {
            items.set(1, ItemStack.EMPTY);
        }
        markDirty();
        world.playSound(null, pos, FFDSounds.CARTOGRAPHY_TABLE_TAKE_RESULT,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        return true;
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
        return "container.farmers_future_delight.cartography_table";
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
        if (index == 0) {
            return world != null && stack.getItem() instanceof ItemMap
                    && ((ItemMap) stack.getItem()).getMapData(stack, world) != null;
        }
        return index == 1 && (stack.getItem() == Items.PAPER || stack.getItem() == Items.MAP
                || stack.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.GLASS_PANE));
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
