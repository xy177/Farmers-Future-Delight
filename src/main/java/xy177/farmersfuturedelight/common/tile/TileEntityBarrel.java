package xy177.farmersfuturedelight.common.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.block.BlockBarrel;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class TileEntityBarrel extends TileEntityLockableLoot {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int openers;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public int getSizeInventory() {
        return 27;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return hasCustomName() ? customName : "container.farmers_future_delight.barrel";
    }

    @Override
    public String getGuiID() {
        return "minecraft:container";
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public Container createContainer(InventoryPlayer inventory, EntityPlayer player) {
        fillWithLoot(player);
        return new ContainerChest(inventory, this, player);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        items = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        if (!checkLootAndRead(tag)) {
            ItemStackHelper.loadAllItems(tag, items);
        }
        if (tag.hasKey("CustomName", 8)) {
            customName = tag.getString("CustomName");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (!checkLootAndWrite(tag)) {
            ItemStackHelper.saveAllItems(tag, items);
        }
        if (hasCustomName()) {
            tag.setString("CustomName", customName);
        }
        return tag;
    }

    @Override
    public void onLoad() {
        if (!world.isRemote && world.getBlockState(pos).getBlock() instanceof BlockBarrel
                && world.getBlockState(pos).getValue(BlockBarrel.OPEN)) {
            world.scheduleUpdate(pos, getBlockType(), 5);
        }
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (world != null && !world.isRemote && !isInvalid() && !player.isSpectator()) {
            setOpeners(Math.max(0, openers) + 1);
            world.scheduleUpdate(pos, getBlockType(), 5);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (world != null && !world.isRemote && !isInvalid() && !player.isSpectator()) {
            setOpeners(Math.max(0, openers - 1));
        }
    }

    public void recheckOpeners() {
        if (world.isRemote || isInvalid()) {
            return;
        }
        int count = 0;
        for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class,
                new AxisAlignedBB(pos).grow(5.0D))) {
            if (!player.isSpectator() && player.openContainer instanceof ContainerChest
                    && ((ContainerChest) player.openContainer).getLowerChestInventory() == this) {
                count++;
            }
        }
        setOpeners(count);
        if (count > 0) {
            world.scheduleUpdate(pos, getBlockType(), 5);
        }
    }

    private void setOpeners(int count) {
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockBarrel)) {
            return;
        }
        boolean open = count > 0;
        if (state.getValue(BlockBarrel.OPEN) != open) {
            playSound(state, open ? FFDSounds.BARREL_OPEN : FFDSounds.BARREL_CLOSE);
            world.setBlockState(pos, state.withProperty(BlockBarrel.OPEN, open), 3);
        }
        openers = count;
    }

    private void playSound(IBlockState state, SoundEvent sound) {
        EnumFacing direction = state.getValue(BlockBarrel.FACING);
        world.playSound(null, pos.getX() + 0.5D + direction.getFrontOffsetX() * 0.5D,
                pos.getY() + 0.5D + direction.getFrontOffsetY() * 0.5D,
                pos.getZ() + 0.5D + direction.getFrontOffsetZ() * 0.5D,
                sound, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
    }
}
