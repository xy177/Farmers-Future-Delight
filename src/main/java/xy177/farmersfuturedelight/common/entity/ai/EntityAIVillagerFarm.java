package xy177.farmersfuturedelight.common.entity.ai;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.ForgeEventFactory;
import xy177.farmersfuturedelight.common.registry.FFDProfessionWorkstations;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRoutines;
import xy177.farmersfuturedelight.common.registry.FFDWorkstationLifecycle;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class EntityAIVillagerFarm extends EntityAIBase {
    private final EntityVillager villager;
    private final List<BlockPos> candidates = new ArrayList<>();
    private BlockPos target;
    private long nextAction;
    private int worked;
    private long expires;

    public EntityAIVillagerFarm(EntityVillager villager) {
        this.villager = villager;
        setMutexBits(3);
    }

    private boolean mayFarm() {
        return !villager.isChild() && !villager.isTrading() && "farmer".equals(
                FFDProfessionWorkstations.getCareerName(villager))
                && FFDVillagerRoutines.getActivity(villager) == FFDVillagerRoutines.Activity.WORK
                && FFDWorkstationLifecycle.getWorkstation(villager) != null
                && ForgeEventFactory.getMobGriefingEvent(villager.world, villager);
    }

    @Override
    public boolean shouldExecute() {
        if (!mayFarm() || villager.world.getTotalWorldTime() <= nextAction
                || !FFDVillagerHomes.canNavigate(villager, villager.getPosition())) return false;
        candidates.clear();
        BlockPos origin = villager.getPosition();
        for (BlockPos position : BlockPos.getAllInBox(origin.add(-1, -1, -1), origin.add(1, 1, 1))) {
            if (valid(position)) candidates.add(position.toImmutable());
        }
        chooseTarget();
        if (target == null) nextAction = villager.world.getTotalWorldTime() + 20L;
        return target != null;
    }

    private boolean valid(BlockPos position) {
        if (!villager.world.isBlockLoaded(position) || !villager.world.isBlockLoaded(position.down())) return false;
        IBlockState state = villager.world.getBlockState(position);
        return state.getBlock() instanceof BlockCrops && ((BlockCrops) state.getBlock()).isMaxAge(state)
                || state.getBlock().isAir(state, villager.world, position)
                && villager.world.getBlockState(position.down()).getBlock() instanceof BlockFarmland;
    }

    private void chooseTarget() {
        target = candidates.isEmpty() ? null : candidates.get(villager.getRNG().nextInt(candidates.size()));
    }

    @Override
    public void startExecuting() {
        worked = 0;
        expires = villager.world.getTotalWorldTime() + 60L;
        walkToTarget();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return worked < 200 && villager.world.getTotalWorldTime() <= expires && mayFarm()
                && target != null && villager.world.isBlockLoaded(target);
    }

    private void walkToTarget() {
        if (target != null) villager.getNavigator().tryMoveToXYZ(
                target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 0.5D);
    }

    @Override
    public void updateTask() {
        if (target == null) return;
        villager.getLookHelper().setLookPosition(target.getX() + 0.5D, target.getY() + 0.5D,
                target.getZ() + 0.5D, 10.0F, villager.getVerticalFaceSpeed());
        if (villager.getDistanceSqToCenter(target) >= 1.0D) return;
        long now = villager.world.getTotalWorldTime();
        if (now > nextAction) {
            IBlockState state = villager.world.getBlockState(target);
            if (state.getBlock() instanceof BlockCrops && ((BlockCrops) state.getBlock()).isMaxAge(state)) {
                villager.world.destroyBlock(target, true);
            } else if (state.getBlock().isAir(state, villager.world, target)
                    && villager.world.getBlockState(target.down()).getBlock() instanceof BlockFarmland) {
                plant();
            }
            if (state.getBlock() instanceof BlockCrops && !((BlockCrops) state.getBlock()).isMaxAge(state)) {
                candidates.remove(target);
                chooseTarget();
                nextAction = now + 20L;
                walkToTarget();
            }
        }
        worked++;
    }

    private void plant() {
        InventoryBasic inventory = villager.getVillagerInventory();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof IPlantable)) continue;
            IPlantable seed = (IPlantable) stack.getItem();
            if (seed.getPlantType(villager.world, target) != EnumPlantType.Crop) continue;
            IBlockState planted = seed.getPlant(villager.world, target);
            if (!villager.world.setBlockState(target, planted, 3)) return;
            villager.world.playSound(null, target, FFDSounds.CROP_PLANT,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
            inventory.decrStackSize(slot, 1);
            return;
        }
    }

    @Override
    public void resetTask() {
        villager.getNavigator().clearPath();
        target = null;
        candidates.clear();
        nextAction = villager.world.getTotalWorldTime() + 40L;
    }
}
