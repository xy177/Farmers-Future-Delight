package xy177.farmersfuturedelight.common.item;

import java.lang.reflect.Constructor;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;

import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.entity.EntityGlowItemFrame;
import xy177.farmersfuturedelight.common.registry.FFDEntities;

public class ItemGlowItemFrame extends Item {
    public ItemGlowItemFrame() {
        setCreativeTab(CreativeTabs.DECORATIONS);
        setMaxStackSize(64);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing, float hitX,
                                      float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        BlockPos framePos = pos.offset(facing);
        if (facing == EnumFacing.DOWN || facing == EnumFacing.UP
                || !player.canPlayerEdit(framePos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        EntityHanging frame = createFrame(world, framePos, facing);
        if (frame == null) {
            return EnumActionResult.FAIL;
        }
        if (frame.onValidSurface()) {
            if (!world.isRemote) {
                frame.playPlaceSound();
                world.spawnEntity(frame);
            }
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Nullable
    private static EntityHanging createFrame(World world, BlockPos pos, EnumFacing facing) {
        if (FFDEntities.isLocalGlowItemFrameEnabled()) {
            return new EntityGlowItemFrame(world, pos, facing);
        }
        EntityEntry external = FFDCompat.getExternalEntityEntry(
                FFDCompat.Feature.GLOW_ITEM_FRAME, "glow_item_frame");
        if (external == null) {
            return null;
        }
        try {
            Constructor<? extends Entity> constructor = external.getEntityClass()
                    .getConstructor(World.class, BlockPos.class, EnumFacing.class);
            Entity entity = constructor.newInstance(world, pos, facing);
            if (entity instanceof EntityHanging) {
                return (EntityHanging) entity;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        Entity entity = external.newInstance(world);
        if (!(entity instanceof EntityHanging)) {
            return null;
        }
        NBTTagCompound data = new NBTTagCompound();
        data.setByte("Facing", (byte) facing.getHorizontalIndex());
        data.setInteger("TileX", pos.getX());
        data.setInteger("TileY", pos.getY());
        data.setInteger("TileZ", pos.getZ());
        entity.readFromNBT(data);
        return (EntityHanging) entity;
    }
}
