package xy177.farmersfuturedelight.common.registry;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import xy177.farmersfuturedelight.api.CompostingRules;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.BlockComposter;

public final class FFDVillagerWork {
    private FFDVillagerWork() {
    }

    public static void use(EntityVillager villager, BlockPos position) {
        if (!FFDConfig.modernVillagerSystem) return;
        String profession = FFDModernVillagerTrades.getModernProfession(villager);
        net.minecraft.util.SoundEvent sound = profession == null ? null : net.minecraft.util.SoundEvent.REGISTRY.getObject(
                new net.minecraft.util.ResourceLocation("farmers_future_delight", "entity.villager.work_" + profession));
        if (sound != null) villager.playSound(sound, 1.0F, 1.0F);
        if (!"farmer".equals(FFDProfessionWorkstations.getCareerName(villager))
                || !(villager.world.getBlockState(position).getBlock() instanceof BlockComposter)) return;
        InventoryBasic inventory = villager.getVillagerInventory();
        int bread = 0;
        int wheat = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() == Items.BREAD) bread += stack.getCount();
            if (stack.getItem() == Items.WHEAT) wheat += stack.getCount();
        }
        if (bread <= 36) {
            int craft = Math.min(3, wheat / 3);
            int remaining = craft * 3;
            for (int slot = 0; slot < inventory.getSizeInventory() && remaining > 0; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.getItem() != Items.WHEAT) continue;
                int count = Math.min(stack.getCount(), remaining);
                inventory.decrStackSize(slot, count);
                remaining -= count;
            }
            if (craft > 0) {
                ItemStack overflow = inventory.addItem(new ItemStack(Items.BREAD, craft));
                if (!overflow.isEmpty()) villager.entityDropItem(overflow, 0.5F);
            }
        }
        if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(villager.world, villager)) return;
        if (villager.world.getBlockState(position).getValue(BlockComposter.LEVEL) == 8) {
            BlockComposter.extract(villager.world, position, true);
        }
        int budget = 20;
        Map<Item, Integer> seen = new HashMap<>();
        for (int slot = inventory.getSizeInventory() - 1; slot >= 0 && budget > 0; slot--) {
            ItemStack stack = inventory.getStackInSlot(slot);
            CompostingRules.Rule rule = CompostingRules.find(stack);
            if (stack.isEmpty() || rule == null || !rule.canVillagerCompost()) continue;
            int count = stack.getCount();
            int total = seen.getOrDefault(stack.getItem(), 0) + count;
            seen.put(stack.getItem(), total);
            int use = Math.min(Math.min(total - 10, budget), count);
            if (use <= 0) continue;
            for (int attempt = 0; attempt < use; attempt++) {
                if (villager.world.getBlockState(position).getValue(BlockComposter.LEVEL) >= 7) return;
                BlockComposter.insert(villager.world, position, rule);
                stack.shrink(1);
                budget--;
            }
            if (stack.isEmpty()) inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
        }
        inventory.markDirty();
    }

    public static void shareFood(EntityVillager source, EntityVillager destination) {
        boolean farmer = "farmer".equals(FFDProfessionWorkstations.getCareerName(source));
        int sourceFood = xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerBreed.inventoryFoodPoints(source);
        int targetFood = xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerBreed.inventoryFoodPoints(destination);
        InventoryBasic inventory = source.getVillagerInventory();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            Item item = stack.getItem();
            boolean food = item == Items.BREAD || item == Items.POTATO || item == Items.CARROT || item == Items.BEETROOT;
            if (!(food && sourceFood >= 24 && (farmer || targetFood < 12)
                    || farmer && item == Items.WHEAT && stack.getCount() > stack.getMaxStackSize() / 2)) continue;
            int count = stack.getCount() > stack.getMaxStackSize() / 2 ? stack.getCount() / 2
                    : stack.getCount() > 24 ? stack.getCount() - 24 : 0;
            if (count == 0) continue;
            EntityItem dropped = new EntityItem(source.world, source.posX, source.posY + source.getEyeHeight() - 0.3D,
                    source.posZ, stack.copy());
            dropped.getItem().setCount(count);
            Vec3d direction = new Vec3d(destination.posX - source.posX,
                    destination.posY - source.posY, destination.posZ - source.posZ).normalize().scale(0.3D);
            dropped.motionX = direction.x;
            dropped.motionY = direction.y + 0.1D;
            dropped.motionZ = direction.z;
            dropped.setDefaultPickupDelay();
            if (source.world.spawnEntity(dropped)) inventory.decrStackSize(slot, count);
            return;
        }
    }

    public static boolean hasEnoughFood(boolean original, EntityVillager villager) {
        return FFDConfig.modernVillagerSystem && villager.getClass() == EntityVillager.class
                ? xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerBreed.foodPoints(villager) >= 12 : original;
    }

    public static boolean hasExcessFood(boolean original, EntityVillager villager) {
        return FFDConfig.modernVillagerSystem && villager.getClass() == EntityVillager.class
                ? xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerBreed.inventoryFoodPoints(villager) >= 24 : original;
    }

    public static boolean wantsMoreFood(boolean original, EntityVillager villager) {
        return FFDConfig.modernVillagerSystem && villager.getClass() == EntityVillager.class
                ? xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerBreed.inventoryFoodPoints(villager) < 12 : original;
    }

    public static net.minecraft.util.SoundEvent ambientSound(net.minecraft.util.SoundEvent original,
                                                             EntityVillager villager) {
        return FFDConfig.modernVillagerSystem && villager.getClass() == EntityVillager.class
                && FFDVillagerHomes.isSleeping(villager) ? null : original;
    }
}
