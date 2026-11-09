package xy177.farmersfuturedelight.common.tile;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import xy177.farmersfuturedelight.api.CompostingRules;
import xy177.farmersfuturedelight.common.block.BlockComposter;

public class TileEntityComposter extends TileEntity implements ISidedInventory {
    private static final int[] INPUT = {0};
    private static final int[] OUTPUT = {1};
    private static final int[] NONE = {};
    private final IItemHandler topHandler = new ComposterItemHandler(EnumFacing.UP);
    private final IItemHandler bottomHandler = new ComposterItemHandler(EnumFacing.DOWN);
    private final IItemHandler sideHandler = new ComposterItemHandler(EnumFacing.NORTH);

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public int getLevel() {
        if (world == null) {
            return -1;
        }
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BlockComposter ? state.getValue(BlockComposter.LEVEL) : -1;
    }

    @Override
    public void onLoad() {
        if (!world.isRemote && getLevel() == 7) {
            world.scheduleUpdate(pos, getBlockType(), 20);
        }
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return getLevel() != 8;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 1 && getLevel() == 8 ? new ItemStack(Items.DYE, 1, 15) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        return slot == 1 && count > 0 ? BlockComposter.extract(world, pos, false) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return decrStackSize(slot, 1);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot == 0 && isItemValidForSlot(slot, stack)) {
            BlockComposter.insert(world, pos, CompostingRules.find(stack));
        } else if (slot == 1 && stack.isEmpty() && getLevel() == 8) {
            BlockComposter.extract(world, pos, false);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        CompostingRules.Rule rule = CompostingRules.find(stack);
        return slot == 0 && getLevel() >= 0 && getLevel() < 7
                && rule != null;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing face) {
        return face == EnumFacing.UP ? INPUT : face == EnumFacing.DOWN ? OUTPUT : NONE;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing face) {
        return face == EnumFacing.UP && isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, EnumFacing face) {
        return face == EnumFacing.DOWN && slot == 1 && getLevel() == 8;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing face) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, face);
    }

    @Override
    @Nullable
    public <Value> Value getCapability(Capability<Value> capability, @Nullable EnumFacing face) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(face == EnumFacing.UP
                    ? topHandler : face == EnumFacing.DOWN ? bottomHandler : sideHandler);
        }
        return super.getCapability(capability, face);
    }

    @Override
    public String getName() {
        return "tile.farmers_future_delight.composter.name";
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
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this && player.getDistanceSq(pos) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public int getField(int id) {
        return id == 0 ? getLevel() : 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 1;
    }

    @Override
    public void clear() {
        if (getLevel() >= 0 && !world.isRemote) {
            world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockComposter.LEVEL, 0), 3);
        }
    }

    private final class ComposterItemHandler implements IItemHandler {
        private final EnumFacing face;

        private ComposterItemHandler(EnumFacing face) {
            this.face = face;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 && face == EnumFacing.DOWN ? TileEntityComposter.this.getStackInSlot(1)
                    : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || face != EnumFacing.UP || !isItemValidForSlot(0, stack)) return stack;
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) BlockComposter.insert(world, pos, CompostingRules.find(stack));
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || face != EnumFacing.DOWN || amount <= 0 || getLevel() != 8) {
                return ItemStack.EMPTY;
            }
            ItemStack result = new ItemStack(Items.DYE, 1, 15);
            if (!simulate) BlockComposter.extract(world, pos, false);
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && face == EnumFacing.UP && isItemValidForSlot(0, stack);
        }
    }
}
