package xy177.farmersfuturedelight.common.entity.ai;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.common.block.BlockSweetBerryBush;
import xy177.farmersfuturedelight.common.entity.EntityFox;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityAIFoxEatBerries extends EntityAIBase {
    private final EntityFox fox;
    private final double speed;
    @Nullable
    private BlockPos target;
    private int runTicks;
    private int waitTicks;

    public EntityAIFoxEatBerries(EntityFox fox, double speed) {
        this.fox = fox;
        this.speed = speed;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        target = null;
        if (!FFDItems.isSweetBerryEnabled() || fox.isSleeping() || fox.getAttackTarget() != null
                || !fox.getHeldItemMainhand().isEmpty()) {
            return false;
        }
        BlockPos origin = fox.getPosition();
        double nearest = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -8; x <= 8; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -8; z <= 8; z++) {
                    cursor.setPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    IBlockState state = fox.world.getBlockState(cursor);
                    if (state.getBlock() != FFDBlocks.SWEET_BERRY_BUSH
                            || state.getValue(BlockSweetBerryBush.AGE) < 2) {
                        continue;
                    }
                    double distance = fox.getDistanceSqToCenter(cursor);
                    if (distance < nearest) {
                        nearest = distance;
                        target = cursor.toImmutable();
                    }
                }
            }
        }
        return nearest < Double.MAX_VALUE;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return target != null && runTicks < 240
                && !fox.isSleeping() && fox.getAttackTarget() == null
                && fox.getHeldItemMainhand().isEmpty()
                && fox.world.getBlockState(target).getBlock() == FFDBlocks.SWEET_BERRY_BUSH
                && fox.world.getBlockState(target).getValue(BlockSweetBerryBush.AGE) >= 2;
    }

    @Override
    public void startExecuting() {
        if (target == null) {
            return;
        }
        fox.setSitting(false);
        fox.setSleeping(false);
        runTicks = 0;
        waitTicks = 0;
        fox.getNavigator().tryMoveToXYZ(target.getX() + 0.5D, target.getY(),
                target.getZ() + 0.5D, speed);
    }

    @Override
    public void resetTask() {
        target = null;
        runTicks = 0;
        waitTicks = 0;
        fox.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (target == null) {
            return;
        }
        runTicks++;
        if (runTicks % 20 == 0 && fox.getDistanceSqToCenter(target) > 4.0D) {
            fox.getNavigator().tryMoveToXYZ(target.getX() + 0.5D, target.getY(),
                    target.getZ() + 0.5D, speed);
        }
        if (fox.getDistanceSqToCenter(target) <= 4.0D) {
            if (++waitTicks >= 40) {
                harvest();
            }
        } else {
            waitTicks = 0;
            if (fox.getRNG().nextFloat() < 0.01F) {
                fox.playFoxSound(FFDSounds.FOX_SNIFF);
            }
        }
    }

    private void harvest() {
        IBlockState state = fox.world.getBlockState(target);
        if (fox.world.isRemote || state.getBlock() != FFDBlocks.SWEET_BERRY_BUSH
                || state.getValue(BlockSweetBerryBush.AGE) < 2
                || !fox.world.getGameRules().getBoolean("mobGriefing")) {
            return;
        }
        int age = state.getValue(BlockSweetBerryBush.AGE);
        int count = 1 + fox.getRNG().nextInt(2) + (age == 3 ? 1 : 0);
        ItemStack held = FFDItems.effectiveStack(FFDItems.SWEET_BERRIES);
        if (held.isEmpty()) {
            return;
        }
        if (count > 1) {
            ItemStack dropped = held.copy();
            dropped.setCount(count - 1);
            fox.entityDropItem(dropped, 0.0F);
        }
        held.setCount(1);
        fox.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, held);
        fox.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 2.0F);
        fox.world.setBlockState(target, state.withProperty(BlockSweetBerryBush.AGE, 1), 2);
        resetTask();
    }
}
