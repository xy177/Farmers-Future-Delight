package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;

public final class FFDModernLootTable extends LootTable {
    private final LootTable delegate;

    public FFDModernLootTable(LootTable delegate) {
        super(new LootPool[0]);
        this.delegate = delegate;
    }

    @Override
    public List<ItemStack> generateLootForPools(Random random, LootContext context) {
        return delegate.generateLootForPools(random, context);
    }

    @Override
    public void fillInventory(IInventory inventory, Random random, LootContext context) {
        List<ItemStack> loot = generateLootForPools(random, context);
        List<Integer> slots = emptySlots(inventory, random);
        shuffleItems(loot, slots.size(), random);
        for (ItemStack stack : loot) {
            if (slots.isEmpty()) {
                return;
            }
            inventory.setInventorySlotContents(slots.remove(slots.size() - 1), stack);
        }
    }

    @Override
    public void freeze() {
        delegate.freeze();
    }

    @Override
    public boolean isFrozen() {
        return delegate.isFrozen();
    }

    @Override
    public LootPool getPool(String name) {
        return delegate.getPool(name);
    }

    @Override
    public LootPool removePool(String name) {
        return delegate.removePool(name);
    }

    @Override
    public void addPool(LootPool pool) {
        delegate.addPool(pool);
    }

    private static List<Integer> emptySlots(IInventory inventory, Random random) {
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            if (inventory.getStackInSlot(slot).isEmpty()) {
                slots.add(slot);
            }
        }
        Collections.shuffle(slots, random);
        return slots;
    }

    private static void shuffleItems(List<ItemStack> loot, int slotCount, Random random) {
        List<ItemStack> splittable = new ArrayList<>();
        Iterator<ItemStack> iterator = loot.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            if (stack.isEmpty()) {
                iterator.remove();
            } else if (stack.getCount() > 1) {
                splittable.add(stack);
                iterator.remove();
            }
        }

        while (slotCount - loot.size() - splittable.size() > 0 && !splittable.isEmpty()) {
            ItemStack stack = splittable.remove(MathHelper.getInt(random, 0,
                    splittable.size() - 1));
            ItemStack split = stack.splitStack(MathHelper.getInt(random, 1,
                    stack.getCount() / 2));
            addSplit(stack, loot, splittable, random);
            addSplit(split, loot, splittable, random);
        }

        loot.addAll(splittable);
        Collections.shuffle(loot, random);
    }

    private static void addSplit(ItemStack stack, List<ItemStack> loot,
                                 List<ItemStack> splittable, Random random) {
        if (stack.getCount() > 1 && random.nextBoolean()) {
            splittable.add(stack);
        } else {
            loot.add(stack);
        }
    }
}
