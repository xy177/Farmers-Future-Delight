package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.advancement.FFDCookingRecipeAdvancements;
import xy177.farmersfuturedelight.common.inventory.ContainerCookingFurnace;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.tile.TileEntityCookingFurnace;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDCookingRecipeBook {
    public static final List<WorkstationRecipes.Type> COOKING_TYPES = Collections.unmodifiableList(
            Arrays.asList(WorkstationRecipes.Type.SMOKING, WorkstationRecipes.Type.BLASTING));
    private static final String DATA = FarmerFutureDelight.MODID + ":cooking_recipe_book";
    private static final Map<EntityPlayerMP, State> STATES = new WeakHashMap<>();

    private FFDCookingRecipeBook() {
    }

    private static NBTTagCompound data(EntityPlayerMP player) {
        NBTTagCompound root = player.getEntityData();
        if (!root.hasKey(DATA, 10)) root.setTag(DATA, new NBTTagCompound());
        return root.getCompoundTag(DATA);
    }

    private static NBTTagCompound known(EntityPlayerMP player, WorkstationRecipes.Type type) {
        NBTTagCompound root = data(player);
        String key = type.name() + "Known";
        if (!root.hasKey(key, 10)) root.setTag(key, new NBTTagCompound());
        return root.getCompoundTag(key);
    }

    private static NBTTagCompound discovered(EntityPlayerMP player, WorkstationRecipes.Type type) {
        NBTTagCompound root = data(player);
        String key = type.name() + "Discovered";
        if (!root.hasKey(key, 10)) root.setTag(key, new NBTTagCompound());
        return root.getCompoundTag(key);
    }

    private static NBTTagCompound highlights(EntityPlayerMP player, WorkstationRecipes.Type type) {
        NBTTagCompound root = data(player);
        String key = type.name() + "Highlights";
        if (!root.hasKey(key, 10)) root.setTag(key, new NBTTagCompound());
        return root.getCompoundTag(key);
    }

    public static boolean isKnown(EntityPlayerMP player, WorkstationRecipes.Type type, ResourceLocation recipe) {
        return known(player, type).getBoolean(recipe.toString());
    }

    private static boolean setKnown(EntityPlayerMP player, WorkstationRecipes.Type type,
                                    ResourceLocation recipe, boolean granted) {
        if (!COOKING_TYPES.contains(type) || WorkstationRecipes.get(type, recipe) == null) return false;
        if (granted) discovered(player, type).setBoolean(recipe.toString(), true);
        NBTTagCompound known = known(player, type);
        boolean changed = known.getBoolean(recipe.toString()) != granted;
        if (granted) known.setBoolean(recipe.toString(), true);
        else known.removeTag(recipe.toString());
        if (changed && granted) {
            highlights(player, type).setBoolean(recipe.toString(), true);
            STATES.computeIfAbsent(player, ignored -> new State()).unlocks
                    .computeIfAbsent(type, ignored -> new LinkedHashSet<>()).add(recipe);
            FFDCookingRecipeAdvancements.trigger(player, type, recipe);
        } else if (!granted) {
            highlights(player, type).removeTag(recipe.toString());
        }
        return changed;
    }

    public static boolean grant(EntityPlayerMP player, WorkstationRecipes.Type type, ResourceLocation recipe) {
        boolean changed = setKnown(player, type, recipe, true);
        if (changed) synchronize(player);
        return changed;
    }

    public static int grant(EntityPlayerMP player, WorkstationRecipes.Type type,
                            Iterable<ResourceLocation> recipes) {
        int changed = 0;
        for (ResourceLocation recipe : recipes) {
            if (setKnown(player, type, recipe, true)) changed++;
        }
        if (changed > 0) synchronize(player);
        return changed;
    }

    public static boolean revoke(EntityPlayerMP player, WorkstationRecipes.Type type, ResourceLocation recipe) {
        boolean changed = setKnown(player, type, recipe, false);
        if (changed) synchronize(player);
        return changed;
    }

    public static int grantAll(EntityPlayerMP player) {
        return setAllKnown(player, true);
    }

    public static int revokeAll(EntityPlayerMP player) {
        return setAllKnown(player, false);
    }

    private static int setAllKnown(EntityPlayerMP player, boolean granted) {
        int changed = 0;
        for (WorkstationRecipes.Type type : COOKING_TYPES) {
            for (WorkstationRecipes.Recipe recipe : WorkstationRecipes.getRecipes(type)) {
                if (setKnown(player, type, recipe.getId(), granted)) changed++;
            }
        }
        if (changed > 0) synchronize(player);
        return changed;
    }

    private static void synchronize(EntityPlayerMP player) {
        State state = STATES.computeIfAbsent(player, ignored -> new State());
        sendUnlocks(player, state);
        state.ghost = "";
        if (player.openContainer instanceof ContainerCookingFurnace) {
            send(player, (ContainerCookingFurnace) player.openContainer);
        }
    }

    public static void discover(EntityPlayerMP player) {
        net.minecraft.advancements.CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.inventory);
        for (WorkstationRecipes.Type type : COOKING_TYPES) {
            NBTTagCompound discovered = discovered(player, type);
            for (WorkstationRecipes.Recipe recipe : WorkstationRecipes.getRecipes(type)) {
                if (FFDCookingRecipeAdvancements.hasDefault(type, recipe.getId())) continue;
                if (discovered.getBoolean(recipe.getId().toString()) || !hasIngredient(player, recipe)) continue;
                setKnown(player, type, recipe.getId(), true);
            }
        }
        sendUnlocks(player, STATES.computeIfAbsent(player, ignored -> new State()));
    }

    private static void sendUnlocks(EntityPlayerMP player, State state) {
        if (state.unlocks.isEmpty()) return;
        Map<WorkstationRecipes.Type, Set<ResourceLocation>> pending = new EnumMap<>(state.unlocks);
        state.unlocks.clear();
        NBTTagList entries = new NBTTagList();
        for (Map.Entry<WorkstationRecipes.Type, Set<ResourceLocation>> group : pending.entrySet()) {
            ItemStack station = new ItemStack(group.getKey() == WorkstationRecipes.Type.SMOKING
                    ? FFDBlocks.SMOKER : FFDBlocks.BLAST_FURNACE);
            for (ResourceLocation id : group.getValue()) {
                WorkstationRecipes.Recipe recipe = WorkstationRecipes.get(group.getKey(), id);
                if (recipe == null || !isKnown(player, group.getKey(), id)) continue;
                List<ItemStack> inputs = new ArrayList<>();
                Collections.addAll(inputs, recipe.getIngredients().get(0).getMatchingStacks());
                if (inputs.isEmpty()) inputs.addAll(inventory(player));
                for (ItemStack input : inputs) {
                    if (input.isEmpty() || !recipe.getIngredients().get(0).apply(input)) continue;
                    ItemStack output = recipe.assemble(Collections.singletonList(input));
                    if (output.isEmpty()) continue;
                    NBTTagCompound entry = new NBTTagCompound();
                    entry.setString("Id", id.toString());
                    entry.setString("Type", group.getKey().name());
                    entry.setTag("Station", station.serializeNBT());
                    entry.setTag("Output", output.serializeNBT());
                    entries.appendTag(entry);
                    break;
                }
            }
        }
        if (entries.tagCount() == 0) return;
        NBTTagCompound notification = new NBTTagCompound();
        notification.setString("Action", "unlock");
        notification.setTag("Recipes", entries);
        FFDNetwork.sendCookingBook(player, notification);
    }

    private static boolean hasIngredient(EntityPlayerMP player, WorkstationRecipes.Recipe recipe) {
        Ingredient ingredient = recipe.getIngredients().get(0);
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            if (ingredient.apply(player.inventory.getStackInSlot(slot))) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        NBTTagCompound original = event.getOriginal().getEntityData();
        if (original.hasKey(DATA, 10)) event.getEntityPlayer().getEntityData().setTag(DATA, original.getCompoundTag(DATA).copy());
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        State state = STATES.computeIfAbsent(player, ignored -> new State());
        List<WorkstationRecipes.Recipe> smoking = WorkstationRecipes.getRecipes(WorkstationRecipes.Type.SMOKING);
        List<WorkstationRecipes.Recipe> blasting = WorkstationRecipes.getRecipes(WorkstationRecipes.Type.BLASTING);
        boolean changed = state.captureInventory(player)
                || state.smoking != smoking || state.blasting != blasting;
        if (changed) discover(player);
        state.smoking = smoking;
        state.blasting = blasting;
        if (!(player.openContainer instanceof ContainerCookingFurnace)) {
            state.window = -1;
            state.ghost = "";
            return;
        }
        ContainerCookingFurnace menu = (ContainerCookingFurnace) player.openContainer;
        boolean slotsChanged = state.capture(menu.getFurnace());
        if (slotsChanged || state.window != menu.windowId) state.ghost = "";
        if (changed || slotsChanged || state.window != menu.windowId) {
            state.window = menu.windowId;
            send(player, menu);
        }
    }

    public static void handle(EntityPlayerMP player, NBTTagCompound request) {
        if (!(player.openContainer instanceof ContainerCookingFurnace) || player.isSpectator()) return;
        ContainerCookingFurnace menu = (ContainerCookingFurnace) player.openContainer;
        if (menu.windowId != request.getInteger("Window") || !menu.canInteractWith(player)) return;
        State state = STATES.computeIfAbsent(player, ignored -> new State());
        String action = request.getString("Action");
        if ("settings".equals(action)) {
            String key = menu.getFurnace().getRecipeType().name();
            data(player).setBoolean(key + "Open", request.getBoolean("Open"));
            data(player).setBoolean(key + "Filter", request.getBoolean("Filter"));
        } else if ("select".equals(action)) {
            String name = request.getString("Recipe");
            if (name.isEmpty() || name.length() > 256) return;
            ResourceLocation id;
            try { id = new ResourceLocation(name); }
            catch (IllegalArgumentException ignored) { return; }
            WorkstationRecipes.Type type = menu.getFurnace().getRecipeType();
            WorkstationRecipes.Recipe recipe = WorkstationRecipes.get(type, id);
            if (recipe == null || !isKnown(player, type, id)) return;
            String ghost = place(player, menu, recipe, request.getBoolean("Maximum"));
            if (ghost != null) state.ghost = ghost;
        } else if ("seen".equals(action)) {
            NBTTagList entries = request.getTagList("Recipes", 8);
            if (entries.tagCount() > 256) return;
            NBTTagCompound highlights = highlights(player, menu.getFurnace().getRecipeType());
            for (int index = 0; index < entries.tagCount(); index++) {
                String id = entries.getStringTagAt(index);
                if (id.length() <= 256) highlights.removeTag(id);
            }
            return;
        } else if ("clear".equals(action)) {
            state.ghost = "";
        } else if (!"request".equals(action)) return;
        discover(player);
        state.capture(menu.getFurnace());
        state.window = menu.windowId;
        state.captureInventory(player);
        send(player, menu);
    }

    private static boolean same(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty() && ItemStack.areItemsEqual(first, second)
                && ItemStack.areItemStackTagsEqual(first, second);
    }

    private static boolean usable(ItemStack stack) {
        return !stack.isEmpty() && !stack.isItemDamaged() && !stack.isItemEnchanted() && !stack.hasDisplayName();
    }

    private static List<ItemStack> inventory(EntityPlayerMP player) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : player.inventory.mainInventory) result.add(stack.copy());
        return result;
    }

    private static boolean returnStack(List<ItemStack> inventory, ItemStack source) {
        for (int pass = 0; pass < 2 && !source.isEmpty(); pass++) {
            for (int slot = 0; slot < inventory.size() && !source.isEmpty(); slot++) {
                ItemStack target = inventory.get(slot);
                if (pass == 0 ? !same(target, source) : !target.isEmpty()) continue;
                int count = Math.min(source.getCount(), Math.min(64, source.getMaxStackSize()) - target.getCount());
                if (count <= 0) continue;
                if (target.isEmpty()) inventory.set(slot, source.splitStack(count));
                else {
                    target.grow(count);
                    source.shrink(count);
                }
            }
        }
        return source.isEmpty();
    }

    private static List<ItemStack> available(List<ItemStack> inventory, ItemStack current) {
        Map<StackKey, ItemStack> counts = new LinkedHashMap<>();
        List<ItemStack> sources = new ArrayList<>(inventory);
        sources.add(0, current);
        for (ItemStack stack : sources) {
            if (!usable(stack)) continue;
            StackKey key = new StackKey(stack);
            ItemStack previous = counts.get(key);
            if (previous == null) counts.put(key, stack.copy());
            else previous.grow(stack.getCount());
        }
        return new ArrayList<>(counts.values());
    }

    private static ItemStack bestInput(List<ItemStack> available, Ingredient ingredient) {
        ItemStack best = ItemStack.EMPTY;
        for (ItemStack candidate : available) {
            if (ingredient.apply(candidate) && candidate.getCount() > best.getCount()) best = candidate;
        }
        return best;
    }

    public static String place(EntityPlayerMP player, ContainerCookingFurnace menu,
                               WorkstationRecipes.Recipe recipe, boolean maximum) {
        TileEntityCookingFurnace furnace = menu.getFurnace();
        if (!menu.canInteractWith(player) || recipe.getType() != furnace.getRecipeType()) return null;
        List<ItemStack> planned = inventory(player);
        ItemStack input = furnace.getStackInSlot(0);
        ItemStack candidate = bestInput(available(planned, input), recipe.getIngredients().get(0));
        int amount = candidate.isEmpty() ? 0 : maximum ? Math.min(candidate.getCount(), candidate.getMaxStackSize())
                : recipe.matches(Collections.singletonList(input)) ? input.getCount() + 1 : 1;
        if (!candidate.isEmpty() && (amount > candidate.getCount() || amount > candidate.getMaxStackSize())) return null;
        List<ItemStack> drops = new ArrayList<>();
        for (int slot : new int[]{0, 2}) {
            ItemStack returned = furnace.getStackInSlot(slot).copy();
            if (!returnStack(planned, returned)) {
                if (!player.isCreative()) return null;
                drops.add(returned);
            }
        }
        if (amount > 0) {
            int needed = amount;
            for (ItemStack stack : planned) {
                if (!usable(stack) || !same(stack, candidate)) continue;
                int taken = Math.min(needed, stack.getCount());
                stack.shrink(taken);
                needed -= taken;
                if (needed == 0) break;
            }
            if (needed != 0) return null;
        }
        for (int slot = 0; slot < planned.size(); slot++) player.inventory.mainInventory.set(slot, planned.get(slot));
        ItemStack placed = candidate.copy();
        placed.setCount(amount);
        furnace.setInventorySlotContents(0, placed);
        furnace.setInventorySlotContents(2, ItemStack.EMPTY);
        for (ItemStack drop : drops) player.dropItem(drop, false);
        player.inventory.markDirty();
        menu.detectAndSendChanges();
        return amount == 0 ? recipe.getId().toString() : "";
    }

    public static NBTTagCompound snapshot(EntityPlayerMP player, ContainerCookingFurnace menu) {
        TileEntityCookingFurnace furnace = menu.getFurnace();
        NBTTagCompound result = new NBTTagCompound();
        result.setInteger("Window", menu.windowId);
        String type = furnace.getRecipeType().name();
        result.setBoolean("Open", data(player).getBoolean(type + "Open"));
        result.setBoolean("Filter", data(player).getBoolean(type + "Filter"));
        result.setString("Ghost", STATES.computeIfAbsent(player, ignored -> new State()).ghost);
        NBTTagList entries = new NBTTagList();
        List<ItemStack> inventory = inventory(player);
        List<ItemStack> available = available(inventory, furnace.getStackInSlot(0));
        for (WorkstationRecipes.Recipe recipe : WorkstationRecipes.getRecipes(furnace.getRecipeType())) {
            if (!isKnown(player, furnace.getRecipeType(), recipe.getId())) continue;
            Ingredient ingredient = recipe.getIngredients().get(0);
            List<ItemStack> inputs = new ArrayList<>();
            Collections.addAll(inputs, ingredient.getMatchingStacks());
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty() && ingredient.apply(stack) && inputs.stream().noneMatch(existing -> same(existing, stack))) inputs.add(stack);
            }
            NBTTagList variants = new NBTTagList();
            for (ItemStack input : inputs) {
                if (input.isEmpty()) continue;
                ItemStack output = recipe.assemble(Collections.singletonList(input));
                if (output.isEmpty()) continue;
                NBTTagCompound variant = new NBTTagCompound();
                ItemStack displayInput = input.copy();
                displayInput.setCount(1);
                variant.setTag("Input", displayInput.serializeNBT());
                variant.setTag("Output", output.serializeNBT());
                variants.appendTag(variant);
            }
            if (variants.tagCount() == 0) continue;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Id", recipe.getId().toString());
            entry.setString("Group", recipe.getGroup());
            entry.setBoolean("Highlight", highlights(player, furnace.getRecipeType()).getBoolean(recipe.getId().toString()));
            entry.setTag("Variants", variants);
            entry.setBoolean("Craftable", !bestInput(available, ingredient).isEmpty());
            ItemStack output = new ItemStack(variants.getCompoundTagAt(0).getCompoundTag("Output"));
            entry.setInteger("Category", output.getItem() instanceof ItemBlock ? 1 : 2);
            entries.appendTag(entry);
        }
        result.setTag("Recipes", entries);
        return result;
    }

    private static void send(EntityPlayerMP player, ContainerCookingFurnace menu) {
        FFDNetwork.sendCookingBook(player, snapshot(player, menu));
    }

    private static final class StackKey {
        private final Item item;
        private final int metadata;
        private final NBTTagCompound tag;

        private StackKey(ItemStack stack) {
            item = stack.getItem();
            metadata = stack.getMetadata();
            tag = stack.getTagCompound();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof StackKey)) return false;
            StackKey key = (StackKey) other;
            return item == key.item && metadata == key.metadata && Objects.equals(tag, key.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, metadata, tag);
        }
    }

    private static final class State {
        private final Map<WorkstationRecipes.Type, Set<ResourceLocation>> unlocks = new EnumMap<>(WorkstationRecipes.Type.class);
        private int window = -1;
        private String ghost = "";
        private List<WorkstationRecipes.Recipe> smoking;
        private List<WorkstationRecipes.Recipe> blasting;
        private final List<ItemStack> slots = new ArrayList<>(Collections.nCopies(3, ItemStack.EMPTY));
        private final List<ItemStack> inventory = new ArrayList<>();

        private boolean captureInventory(EntityPlayerMP player) {
            boolean changed = inventory.size() != player.inventory.getSizeInventory();
            while (inventory.size() < player.inventory.getSizeInventory()) inventory.add(ItemStack.EMPTY);
            for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
                ItemStack current = player.inventory.getStackInSlot(slot);
                if (!ItemStack.areItemStacksEqual(current, inventory.get(slot))) {
                    changed = true;
                    inventory.set(slot, current.copy());
                }
            }
            return changed;
        }

        private boolean capture(TileEntityCookingFurnace furnace) {
            boolean changed = false;
            for (int slot = 0; slot < 3; slot++) {
                ItemStack current = furnace.getStackInSlot(slot);
                if (!ItemStack.areItemStacksEqual(current, slots.get(slot))) {
                    changed = true;
                    slots.set(slot, current.copy());
                }
            }
            return changed;
        }
    }
}
