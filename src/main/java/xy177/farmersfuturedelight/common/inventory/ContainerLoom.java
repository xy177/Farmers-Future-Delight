package xy177.farmersfuturedelight.common.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.item.ItemBannerPattern;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDBannerPatterns;
import xy177.farmersfuturedelight.common.FFDConfig;

public class ContainerLoom extends ContainerWorkstation {
    private final InventoryBasic inputs = new InventoryBasic("loom", false, 3);
    private final InventoryBasic output = new InventoryBasic("loom_result", false, 1);
    private final InventoryPlayer playerInventory;
    private final World world;
    private final BlockPos pos;
    private List<BannerPattern> selectablePatterns = new ArrayList<>();
    private int selectedIndex = -1;
    private int syncedSelectedIndex = -1;
    private int layerLimit = FFDBannerPatterns.getLayerLimit();
    private int syncedLayerLimit = -1;

    public ContainerLoom(InventoryPlayer playerInventory, World world, BlockPos pos) {
        super(4);
        this.playerInventory = playerInventory;
        this.world = world;
        this.pos = pos;
        inputs.addInventoryChangeListener(inventory -> refresh());
        addSlotToContainer(new Slot(inputs, 0, 13, 26) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == Items.BANNER;
            }
        });
        addSlotToContainer(new Slot(inputs, 1, 33, 26) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return dyeColor(stack) != null;
            }
        });
        addSlotToContainer(new Slot(inputs, 2, 23, 45) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return isPatternInput(stack);
            }
        });
        addSlotToContainer(new ResultSlot());
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
        refresh();
    }

    public List<BannerPattern> getSelectablePatterns() {
        return selectablePatterns;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public int getLayerLimit() {
        return layerLimit;
    }

    private static boolean isPatternInput(ItemStack stack) {
        return stack.getItem() instanceof ItemBannerPattern
                || !FFDBannerPatterns.ingredientPatterns(stack).isEmpty();
    }

    public ItemStack getBanner() {
        return inputs.getStackInSlot(0);
    }

    public ItemStack getDye() {
        return inputs.getStackInSlot(1);
    }

    public ItemStack getPattern() {
        return inputs.getStackInSlot(2);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 1, layerLimit);
        listener.sendWindowProperty(this, 0, selectedIndex);
    }

    @Override
    public void detectAndSendChanges() {
        if (!world.isRemote) {
            int limit = FFDBannerPatterns.getLayerLimit();
            if (layerLimit != limit) {
                layerLimit = limit;
                updateResult();
            }
        }
        super.detectAndSendChanges();
        if (syncedLayerLimit != layerLimit) {
            for (IContainerListener listener : listeners) listener.sendWindowProperty(this, 1, layerLimit);
            syncedLayerLimit = layerLimit;
        }
        if (syncedSelectedIndex != selectedIndex) {
            for (IContainerListener listener : listeners) {
                listener.sendWindowProperty(this, 0, selectedIndex);
            }
            syncedSelectedIndex = selectedIndex;
        }
    }

    @Override
    public void updateProgressBar(int id, int value) {
        if (id == 0) {
            selectedIndex = value;
            updateResult();
        } else if (id == 1) {
            layerLimit = Math.max(0, value);
            updateResult();
        }
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int index) {
        if (!canInteractWith(player) || index < 0 || index >= selectablePatterns.size()
                || inputs.getStackInSlot(0).isEmpty() || dyeColor(inputs.getStackInSlot(1)) == null) {
            return false;
        }
        selectedIndex = index;
        updateResult();
        return true;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return world.getBlockState(pos).getBlock() == FFDBlocks.LOOM
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return slot.slotNumber != 3 && super.canDragIntoSlot(slot);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.inventory != output && super.canMergeSlot(stack, slot);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (index == 3) {
            int room = 0;
            for (int slotIndex = 4; slotIndex < 40; slotIndex++) {
                ItemStack existing = inventorySlots.get(slotIndex).getStack();
                if (existing.isEmpty()) {
                    room += stack.getMaxStackSize();
                } else if (ItemStack.areItemsEqual(existing, stack)
                        && ItemStack.areItemStackTagsEqual(existing, stack)) {
                    room += Math.max(0, stack.getMaxStackSize() - existing.getCount());
                }
            }
            if (room < stack.getCount() || !mergeItemStack(stack, 4, 40, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, original);
            slot.onTake(player, stack);
        } else if (index < 3) {
            if (!mergeItemStack(stack, 4, 40, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() == Items.BANNER) {
            if (!mergeItemStack(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (dyeColor(stack) != null) {
            if (!mergeItemStack(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isPatternInput(stack)) {
            if (!mergeItemStack(stack, 2, 3, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 31) {
            if (!mergeItemStack(stack, 31, 40, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(stack, 4, 31, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        if (index != 3) {
            slot.onTake(player, stack);
        }
        return original;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote) {
            clearContainer(player, player.world, inputs);
            output.clear();
        }
    }

    private void refresh() {
        BannerPattern previous = selectedIndex >= 0 && selectedIndex < selectablePatterns.size()
                ? selectablePatterns.get(selectedIndex) : null;
        selectablePatterns = new ArrayList<>();
        ItemStack pattern = inputs.getStackInSlot(2);
        if (pattern.getItem() instanceof ItemBannerPattern) {
            selectablePatterns.add(((ItemBannerPattern) pattern.getItem()).getPattern());
        } else if (!pattern.isEmpty()) {
            selectablePatterns.addAll(FFDBannerPatterns.ingredientPatterns(pattern));
        } else {
            for (BannerPattern candidate : BannerPattern.values()) {
                if (candidate != BannerPattern.BASE && candidate
                        != xy177.farmersfuturedelight.common.registry.FFDBannerPatterns.GLOBE
                        && !candidate.hasPatternItem()) {
                    selectablePatterns.add(candidate);
                }
            }
        }
        selectedIndex = selectablePatterns.size() == 1 ? 0 : selectablePatterns.indexOf(previous);
        updateResult();
    }

    private void updateResult() {
        ItemStack banner = inputs.getStackInSlot(0);
        ItemStack dye = inputs.getStackInSlot(1);
        EnumDyeColor color = dyeColor(dye);
        ItemStack result = ItemStack.EMPTY;
        if (!banner.isEmpty() && color != null && selectedIndex >= 0
                && selectedIndex < selectablePatterns.size()) {
            NBTTagCompound root = banner.getTagCompound() == null
                    ? new NBTTagCompound() : banner.getTagCompound().copy();
            NBTTagCompound blockTag = root.getCompoundTag("BlockEntityTag");
            NBTTagList patterns = blockTag.getTagList("Patterns", 10);
            if (patterns.tagCount() < layerLimit) {
                NBTTagCompound layer = new NBTTagCompound();
                layer.setString("Pattern", selectablePatterns.get(selectedIndex).getHashname());
                layer.setInteger("Color", color.getDyeDamage());
                patterns.appendTag(layer);
                blockTag.setTag("Patterns", patterns);
                root.setTag("BlockEntityTag", blockTag);
                result = banner.copy();
                result.setCount(1);
                result.setTagCompound(root);
            }
        }
        output.setInventorySlotContents(0, result);
    }

    private static EnumDyeColor dyeColor(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() == FFDItems.WHITE_DYE) return EnumDyeColor.WHITE;
        if (stack.getItem() == FFDItems.BLACK_DYE) return EnumDyeColor.BLACK;
        if (stack.getItem() == FFDItems.BLUE_DYE) return EnumDyeColor.BLUE;
        if (stack.getItem() == FFDItems.BROWN_DYE) return EnumDyeColor.BROWN;
        if (stack.getItem() == Items.DYE && !FFDConfig.legacyDyeCompatibility) {
            EnumDyeColor color = EnumDyeColor.byDyeDamage(stack.getMetadata());
            if (color == EnumDyeColor.WHITE || color == EnumDyeColor.BLACK
                    || color == EnumDyeColor.BLUE || color == EnumDyeColor.BROWN) {
                return null;
            }
        }
        return net.minecraftforge.oredict.DyeUtils.colorFromStack(stack).orElse(null);
    }

    private class ResultSlot extends Slot {
        private ResultSlot() {
            super(output, 0, 143, 57);
        }

        @Override
        public ItemStack decrStackSize(int amount) {
            return super.decrStackSize(getStack().getCount());
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            if (!player.world.isRemote) {
                ItemStack pattern = inputs.getStackInSlot(2);
                if (!pattern.isEmpty() && !(pattern.getItem() instanceof ItemBannerPattern)) {
                    ItemStack consumed = pattern.copy();
                    consumed.setCount(1);
                    ItemStack remainder = net.minecraftforge.common.ForgeHooks.getContainerItem(consumed);
                    pattern.shrink(1);
                    if (!remainder.isEmpty()) {
                        if (pattern.isEmpty()) inputs.setInventorySlotContents(2, remainder);
                        else if (!player.inventory.addItemStackToInventory(remainder)) player.dropItem(remainder, false);
                    }
                }
                inputs.getStackInSlot(0).shrink(1);
                inputs.getStackInSlot(1).shrink(1);
                inputs.markDirty();
                world.playSound(null, pos,
                        xy177.farmersfuturedelight.common.registry.FFDSounds.LOOM_TAKE_RESULT,
                        SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            return super.onTake(player, stack);
        }
    }
}
