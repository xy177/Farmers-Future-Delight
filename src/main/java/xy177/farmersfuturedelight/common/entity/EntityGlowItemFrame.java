package xy177.farmersfuturedelight.common.entity;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import xy177.farmersfuturedelight.common.registry.FFDItems;

public class EntityGlowItemFrame extends EntityItemFrame implements IEntityAdditionalSpawnData {
    public EntityGlowItemFrame(World world) {
        super(world);
    }

    public EntityGlowItemFrame(World world, BlockPos pos, EnumFacing facing) {
        super(world, pos, facing);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        BlockPos pos = getHangingPosition();
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());
        buffer.writeByte(facingDirection.getHorizontalIndex());
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        hangingPosition = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        updateFacingWithBoundingBox(EnumFacing.getHorizontal(buffer.readUnsignedByte()));
    }

    @Override
    public void dropItemOrSelf(@Nullable Entity entity, boolean dropFrame) {
        if (!world.getGameRules().getBoolean("doEntityDrops")) {
            return;
        }

        ItemStack displayed = getDisplayedItem();
        if (entity instanceof EntityPlayer
                && ((EntityPlayer) entity).capabilities.isCreativeMode) {
            removeFrameFromMap(displayed);
            return;
        }

        if (dropFrame) {
            ItemStack frame = FFDItems.effectiveStack(FFDItems.GLOW_ITEM_FRAME);
            if (!frame.isEmpty()) {
                entityDropItem(frame, 0.0F);
            }
        }
        if (!displayed.isEmpty()) {
            ItemStack copy = displayed.copy();
            removeFrameFromMap(copy);
            entityDropItem(copy, 0.0F);
        }
    }

    @Override
    public void onBroken(@Nullable Entity brokenEntity) {
        playSound(SoundEvents.ENTITY_ITEMFRAME_BREAK, 1.0F, 1.0F);
        dropItemOrSelf(brokenEntity, true);
    }

    @Override
    public void playPlaceSound() {
        playSound(SoundEvents.ENTITY_ITEMFRAME_PLACE, 1.0F, 1.0F);
    }

    private void removeFrameFromMap(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemMap) {
            MapData map = ((ItemMap) stack.getItem()).getMapData(stack, world);
            if (map != null) {
                map.mapDecorations.remove("frame-" + getEntityId());
            }
        }
        if (!stack.isEmpty()) {
            stack.setItemFrame(null);
        }
        setDisplayedItem(ItemStack.EMPTY);
    }
}
