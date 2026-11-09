package xy177.farmersfuturedelight.common.entity.ai;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.UUID;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRoutines;

public final class EntityAIVillagerBreed extends EntityAIBase {
    private static final String FOOD_TAG = "farmers_future_delight:food_level";
    private static final Map<EntityVillager, UUID> PARTNERS = new WeakHashMap<>();
    private final EntityVillager villager;
    private EntityVillager partner;
    private long nextSearch;
    private long birthAt;
    private int ticks;

    public EntityAIVillagerBreed(EntityVillager villager) {
        this.villager = villager;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        long now = villager.world.getTotalWorldTime();
        if (now < nextSearch || !canBreed(villager) || PARTNERS.containsKey(villager)
                || FFDVillagerRoutines.getActivity(villager) != FFDVillagerRoutines.Activity.IDLE) return false;
        nextSearch = now + 200L + villager.getRNG().nextInt(200);
        for (EntityVillager candidate : villager.world.getEntitiesWithinAABB(EntityVillager.class,
                villager.getEntityBoundingBox().grow(8.0D))) {
            if (candidate == villager || !canBreed(candidate) || PARTNERS.containsKey(candidate)
                    || !villager.canEntityBeSeen(candidate) || !FFDVillagerHomes.canNavigate(villager,
                            candidate.getPosition())) continue;
            partner = candidate;
            return true;
        }
        return false;
    }

    @Override
    public void startExecuting() {
        PARTNERS.put(villager, partner.getUniqueID());
        PARTNERS.put(partner, villager.getUniqueID());
        ticks = 350;
        birthAt = villager.world.getTotalWorldTime() + 275L + villager.getRNG().nextInt(50);
        villager.world.setEntityState(villager, (byte)18);
        villager.world.setEntityState(partner, (byte)18);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return ticks-- > 0 && partner != null && canBreed(villager) && canBreed(partner)
                && villager.world.getTotalWorldTime() <= birthAt
                && FFDVillagerRoutines.getActivity(villager) == FFDVillagerRoutines.Activity.IDLE;
    }

    @Override
    public void updateTask() {
        villager.getLookHelper().setLookPositionWithEntity(partner, 10.0F, 30.0F);
        partner.getLookHelper().setLookPositionWithEntity(villager, 10.0F, 30.0F);
        if (ticks % 20 == 0) {
            if (FFDVillagerHomes.canNavigate(villager, partner.getPosition())) {
                villager.getNavigator().tryMoveToXYZ(partner.posX, partner.posY, partner.posZ, 0.5D);
            }
            if (FFDVillagerHomes.canNavigate(partner, villager.getPosition())) {
                partner.getNavigator().tryMoveToXYZ(villager.posX, villager.posY, villager.posZ, 0.5D);
            }
        }
        if (villager.getDistanceSq(partner) > 5.0D) return;
        if (villager.world.getTotalWorldTime() >= birthAt) {
            digestFood(villager);
            digestFood(partner);
            giveBirth();
            ticks = 0;
        } else if (villager.getRNG().nextInt(35) == 0) {
            villager.world.setEntityState(villager, (byte)12);
            villager.world.setEntityState(partner, (byte)12);
        }
    }

    private void giveBirth() {
        BlockPos bed = FFDVillagerHomes.findVacantBed(villager, true);
        if (bed == null) {
            villager.world.setEntityState(villager, (byte)13);
            villager.world.setEntityState(partner, (byte)13);
            return;
        }
        EntityVillager proposed = villager.createChild(partner);
        BabyEntitySpawnEvent event = new BabyEntitySpawnEvent(villager, partner, proposed);
        if (MinecraftForge.EVENT_BUS.post(event) || event.getChild() == null) return;
        EntityAgeable child = event.getChild();
        child.setGrowingAge(-24000);
        child.setLocationAndAngles(villager.posX, villager.posY, villager.posZ, 0.0F, 0.0F);
        boolean bound = child.getClass() == EntityVillager.class;
        if (bound && !FFDVillagerHomes.bind((EntityVillager) child, bed)) return;
        if (!villager.world.spawnEntity(child)) {
            if (bound) FFDVillagerHomes.release((EntityVillager) child);
            return;
        }
        villager.setGrowingAge(6000);
        partner.setGrowingAge(6000);
        villager.world.setEntityState(child, (byte)12);
    }

    @Override
    public void resetTask() {
        PARTNERS.remove(villager);
        if (partner != null && villager.getUniqueID().equals(PARTNERS.get(partner))) PARTNERS.remove(partner);
        partner = null;
        villager.getNavigator().clearPath();
    }

    public static boolean canBreed(EntityVillager villager) {
        return villager.getClass() == EntityVillager.class && villager.isEntityAlive()
                && !villager.isAIDisabled() && villager.getGrowingAge() == 0 && !villager.isTrading()
                && !FFDVillagerHomes.isSleeping(villager) && !FFDVillagerRoutines.isPanicking(villager)
                && foodPoints(villager) >= 12;
    }

    public static int foodPoints(EntityVillager villager) {
        return Math.max(0, villager.getEntityData().getInteger(FOOD_TAG)) + inventoryFoodPoints(villager);
    }

    public static int inventoryFoodPoints(EntityVillager villager) {
        InventoryBasic inventory = villager.getVillagerInventory();
        int points = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) points += nutrition(stack.getItem()) * stack.getCount();
        }
        return points;
    }

    private static int nutrition(Item item) {
        return item == Items.BREAD ? 4 : item == Items.CARROT || item == Items.POTATO
                || item == Items.BEETROOT ? 1 : 0;
    }

    private static void digestFood(EntityVillager villager) {
        InventoryBasic inventory = villager.getVillagerInventory();
        int points = Math.max(0, villager.getEntityData().getInteger(FOOD_TAG));
        for (int slot = 0; slot < inventory.getSizeInventory() && points < 12; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            int value = nutrition(stack.getItem());
            if (value == 0) continue;
            int count = Math.min(stack.getCount(), (12 - points + value - 1) / value);
            inventory.decrStackSize(slot, count);
            points += count * value;
        }
        villager.getEntityData().setInteger(FOOD_TAG, Math.max(0, points - 12));
    }
}
