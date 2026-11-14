package xy177.farmersfuturedelight.common;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class FFDLodestoneCompass {
    private static final String KEY = "FFDLodestone";

    private FFDLodestoneCompass() {
    }

    public static boolean bind(World world, BlockPos pos, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() != Items.COMPASS) return false;
        if (world.isRemote) return true;
        world.playSound(null, pos, FFDSounds.LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        boolean replace = !player.capabilities.isCreativeMode && held.getCount() == 1;
        ItemStack compass = replace ? held : held.copy();
        compass.setCount(1);
        NBTTagCompound target = new NBTTagCompound();
        target.setInteger("Dimension", world.provider.getDimension());
        target.setInteger("X", pos.getX());
        target.setInteger("Y", pos.getY());
        target.setInteger("Z", pos.getZ());
        compass.setTagInfo(KEY, target);
        if (player instanceof net.minecraft.entity.player.EntityPlayerMP) {
            xy177.farmersfuturedelight.common.advancement.FFDAdvancements.USE_LODESTONE.trigger(
                    (net.minecraft.entity.player.EntityPlayerMP) player);
        }
        if (!replace) {
            if (!player.capabilities.isCreativeMode) held.shrink(1);
            if (!player.inventory.addItemStackToInventory(compass)) player.dropItem(compass, false);
        }
        return true;
    }

    public static boolean isBound(ItemStack stack) {
        return stack.getItem() == Items.COMPASS && stack.getTagCompound() != null
                && stack.getTagCompound().hasKey(KEY, 10);
    }

    public static String name(String original, ItemStack stack) {
        return isBound(stack) ? "item.farmers_future_delight.lodestone_compass" : original;
    }

    public static boolean foil(boolean original, ItemStack stack) {
        return original || isBound(stack);
    }

    @Nullable public static BlockPos target(ItemStack stack, World world) {
        if (!isBound(stack)) return null;
        NBTTagCompound target = stack.getTagCompound().getCompoundTag(KEY);
        if (target.getBoolean("Lost") || target.getInteger("Dimension") != world.provider.getDimension()) return null;
        return new BlockPos(target.getInteger("X"), target.getInteger("Y"), target.getInteger("Z"));
    }

    public static void tick(ItemStack stack, World world, Entity owner) {
        if (world.isRemote || !(owner instanceof EntityPlayer) || !isBound(stack)) return;
        BlockPos pos = target(stack, world);
        if (pos != null && world.isBlockLoaded(pos) && world.getBlockState(pos).getBlock() != FFDBlocks.LODESTONE) {
            stack.getTagCompound().getCompoundTag(KEY).setBoolean("Lost", true);
        }
    }
}
