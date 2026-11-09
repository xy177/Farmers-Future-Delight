package xy177.farmersfuturedelight.common.tile;

import java.util.Collections;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.block.BlockCampfire;

public class TileEntityCampfire extends TileEntity implements ITickable {
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final int[] cookingProgress = new int[4];
    private final int[] cookingTime = new int[4];
    private Boolean wasLit;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void onLoad() {
        if (world != null) wasLit = isLit();
    }

    private boolean isLit() {
        IBlockState state = world.getBlockState(pos);
        return BlockCampfire.isLit(state, world, pos);
    }

    public void extinguish() {
        wasLit = false;
        if (world != null && world.isRemote) {
            boolean signalFire = world.getBlockState(pos.down()).getBlock()
                    == net.minecraft.init.Blocks.HAY_BLOCK;
            for (int count = 0; count < 20; count++) {
                BlockCampfire.makeSmokeParticles(world, pos, signalFire, true);
            }
        }
    }

    @Override
    public void update() {
        if (world == null) return;
        boolean lit = isLit();
        IBlockState state = world.getBlockState(pos);
        if (!world.isRemote && !lit && state.getBlock() instanceof BlockCampfire
                && state.getValue(BlockCampfire.LIT)) {
            world.setBlockState(pos, state.withProperty(BlockCampfire.LIT, false), 3);
        }
        boolean signalFire = world.getBlockState(pos.down()).getBlock()
                == net.minecraft.init.Blocks.HAY_BLOCK;
        if (Boolean.TRUE.equals(wasLit) && !lit) {
            if (world.isRemote) {
                for (int count = 0; count < 20; count++) {
                    BlockCampfire.makeSmokeParticles(world, pos, signalFire, true);
                }
            } else {
                world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_FIRE_EXTINGUISH,
                        net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
        wasLit = lit;
        if (world.isRemote) {
            if (lit && world.rand.nextFloat() < 0.11F) {
                int count = 2 + world.rand.nextInt(2);
                for (int index = 0; index < count; index++) {
                    BlockCampfire.makeSmokeParticles(world, pos, signalFire, false);
                }
            }
            for (int slot = 0; slot < items.size(); slot++) {
                if (!items.get(slot).isEmpty()) {
                    if (lit && world.rand.nextFloat() < 0.2F) {
                        int rotation = world.getBlockState(pos).getValue(BlockCampfire.FACING)
                                .getHorizontalIndex();
                        net.minecraft.util.EnumFacing direction = net.minecraft.util.EnumFacing
                                .getHorizontal((slot + rotation) % 4);
                        double x = pos.getX() + 0.5D - direction.getFrontOffsetX() * 0.3125D
                                + direction.rotateY().getFrontOffsetX() * 0.3125D;
                        double z = pos.getZ() + 0.5D - direction.getFrontOffsetZ() * 0.3125D
                                + direction.rotateY().getFrontOffsetZ() * 0.3125D;
                        for (int count = 0; count < 4; count++) {
                            world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL,
                                    x, pos.getY() + 0.5D, z, 0.0D, 0.0005D, 0.0D);
                        }
                    }
                    if (lit && cookingTime[slot] > 0) {
                        cookingProgress[slot] = Math.min(cookingTime[slot], cookingProgress[slot] + 1);
                    } else if (!lit) {
                        cookingProgress[slot] = Math.max(0, cookingProgress[slot] - 2);
                    }
                }
            }
            return;
        }
        boolean changed = false;
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack input = items.get(slot);
            if (input.isEmpty()) continue;
            if (lit) {
                changed = true;
                cookingProgress[slot]++;
                if (cookingProgress[slot] >= cookingTime[slot]) {
                    WorkstationRecipes.Recipe recipe = WorkstationRecipes.find(
                            WorkstationRecipes.Type.CAMPFIRE_COOKING, Collections.singletonList(input));
                    ItemStack output = recipe == null ? input.copy()
                            : recipe.assemble(Collections.singletonList(input));
                    if (!output.isEmpty()) {
                        EntityItem entity = new EntityItem(world, pos.getX() + 0.5D,
                                pos.getY() + 0.5D, pos.getZ() + 0.5D, output);
                        entity.setDefaultPickupDelay();
                        world.spawnEntity(entity);
                    }
                    items.set(slot, ItemStack.EMPTY);
                    cookingProgress[slot] = 0;
                    cookingTime[slot] = 0;
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                }
            } else {
                if (cookingProgress[slot] > 0) {
                    cookingProgress[slot] = Math.max(0, cookingProgress[slot] - 2);
                    changed = true;
                }
            }
        }
        if (changed) markDirty();
    }

    public boolean placeFood(EntityPlayer player, ItemStack held) {
        WorkstationRecipes.Recipe recipe = WorkstationRecipes.find(
                WorkstationRecipes.Type.CAMPFIRE_COOKING, Collections.singletonList(held));
        if (recipe == null) return false;
        int slot = firstEmptySlot();
        if (slot < 0) return false;
        ItemStack input = held.copy();
        input.setCount(1);
        items.set(slot, input);
        if (!player.capabilities.isCreativeMode) held.shrink(1);
        ItemStack remainder = input.getItem().getContainerItem(input);
        if (!player.capabilities.isCreativeMode && !remainder.isEmpty()) {
            if (!player.inventory.addItemStackToInventory(remainder)) player.dropItem(remainder, false);
        }
        cookingProgress[slot] = 0;
        cookingTime[slot] = recipe.getProcessingTime();
        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        return true;
    }

    private int firstEmptySlot() {
        for (int index = 0; index < items.size(); index++) {
            if (items.get(index).isEmpty()) return index;
        }
        return -1;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        items.clear();
        net.minecraft.inventory.ItemStackHelper.loadAllItems(tag, items);
        readArray(tag, "CookingProgress", cookingProgress);
        readArray(tag, "CookingTime", cookingTime);
    }

    private static void readArray(NBTTagCompound tag, String key, int[] destination) {
        int[] values = tag.getIntArray(key);
        for (int index = 0; index < destination.length; index++) {
            destination[index] = index < values.length ? Math.max(0, values[index]) : 0;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        net.minecraft.inventory.ItemStackHelper.saveAllItems(tag, items);
        tag.setIntArray("CookingProgress", cookingProgress);
        tag.setIntArray("CookingTime", cookingTime);
        return tag;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
        if (world != null) world.markBlockRangeForRenderUpdate(pos, pos);
    }

    public ItemStack[] getRenderItems() {
        ItemStack[] result = new ItemStack[items.size()];
        for (int index = 0; index < result.length; index++) result[index] = items.get(index).copy();
        return result;
    }

    public int getCookingProgress(int slot) {
        return cookingProgress[slot];
    }

    public int getCookingTime(int slot) {
        return cookingTime[slot];
    }

    public void dropContents() {
        if (world == null || world.isRemote) return;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                net.minecraft.inventory.InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(),
                        pos.getZ(), stack);
            }
        }
        items.clear();
    }
}
