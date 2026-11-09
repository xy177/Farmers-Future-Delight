package xy177.farmersfuturedelight.common.command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import xy177.farmersfuturedelight.common.FFDBlockLootTables;

public final class CommandLoot extends FFDServerCommand {
    @Override
    public String getName() {
        return "loot";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.farmers_future_delight.loot.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    protected void executeOnServerThread(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException(getUsage(sender));
        }
        Target target = parseTarget(server, sender, args);
        List<ItemStack> loot = generateLoot(server, sender, args, target.sourceIndex);
        int delivered = deliver(server, sender, target, loot);
        FFDCommandPredicates.recordResult(sender, delivered);
        notifyCommandListener(sender, this, "commands.farmers_future_delight.loot.success", delivered);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                           String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "give", "insert", "replace", "spawn");
        }
        if (args.length == 2 && "replace".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, "block", "entity");
        }
        if (args.length == 6 && "replace".equals(args[0]) && "block".equals(args[1])) {
            List<String> slots = new ArrayList<>();
            for (int slot = 0; slot < 54; slot++) {
                slots.add("container." + slot);
            }
            return getListOfStringsMatchingLastWord(args, slots);
        }
        if (args.length == 4 && "replace".equals(args[0]) && "entity".equals(args[1])) {
            return getListOfStringsMatchingLastWord(args, "weapon.mainhand", "weapon.offhand",
                    "armor.head", "armor.chest", "armor.legs", "armor.feet", "container.0");
        }
        int sourceIndex = getSourceIndex(args);
        if (sourceIndex >= 0 && args.length == sourceIndex + 1) {
            return getListOfStringsMatchingLastWord(args, "fish", "kill", "loot", "mine");
        }
        if (sourceIndex >= 0 && args.length == sourceIndex + 2
                && ("fish".equals(args[sourceIndex]) || "loot".equals(args[sourceIndex]))) {
            List<String> tables = new ArrayList<>();
            for (ResourceLocation table : net.minecraft.world.storage.loot.LootTableList.getAll()) {
                tables.add(table.toString());
            }
            return getListOfStringsMatchingLastWord(args, tables);
        }
        return Collections.emptyList();
    }

    private static int getSourceIndex(String[] args) {
        if (args.length >= 5 && "spawn".equals(args[0])) {
            return 4;
        }
        if (args.length >= 2 && "give".equals(args[0])) {
            return 2;
        }
        if (args.length >= 5 && "insert".equals(args[0])) {
            return 4;
        }
        if (args.length >= 6 && "replace".equals(args[0]) && "block".equals(args[1])) {
            return args.length > 6 && isInteger(args[6]) ? 7 : 6;
        }
        if (args.length >= 4 && "replace".equals(args[0]) && "entity".equals(args[1])) {
            return args.length > 4 && isInteger(args[4]) ? 5 : 4;
        }
        return -1;
    }

    private static Target parseTarget(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        String type = args[0];
        if ("spawn".equals(type)) {
            require(args, 5);
            Target target = new Target(TargetType.SPAWN, 4);
            target.position = parsePosition(sender, args, 1);
            return target;
        }
        if ("give".equals(type)) {
            require(args, 3);
            Target target = new Target(TargetType.GIVE, 2);
            target.players = FFDCommandEntities.getPlayers(server, sender, args[1]);
            return target;
        }
        if ("insert".equals(type)) {
            require(args, 5);
            Target target = new Target(TargetType.INSERT, 4);
            target.blockPos = FFDCommandCoordinates.parseBlockPosition(sender, args, 1);
            return target;
        }
        if ("replace".equals(type) && args.length > 1 && "block".equals(args[1])) {
            require(args, 7);
            Target target = new Target(TargetType.REPLACE_BLOCK, 6);
            target.blockPos = FFDCommandCoordinates.parseBlockPosition(sender, args, 2);
            target.slot = parseContainerSlot(args[5]);
            target.sourceIndex = parseOptionalCount(args, 6, target);
            return target;
        }
        if ("replace".equals(type) && args.length > 1 && "entity".equals(args[1])) {
            require(args, 5);
            Target target = new Target(TargetType.REPLACE_ENTITY, 4);
            target.entities = FFDCommandEntities.getEntities(server, sender, args[2]);
            target.slotName = args[3];
            target.sourceIndex = parseOptionalCount(args, 4, target);
            return target;
        }
        throw new WrongUsageException("commands.farmers_future_delight.loot.usage");
    }

    private static int parseOptionalCount(String[] args, int sourceIndex, Target target)
            throws CommandException {
        if (sourceIndex < args.length && isInteger(args[sourceIndex])) {
            target.count = parseInt(args[sourceIndex], 0);
            return sourceIndex + 1;
        }
        return sourceIndex;
    }

    private static List<ItemStack> generateLoot(MinecraftServer server, ICommandSender sender,
                                                 String[] args, int index) throws CommandException {
        if (index >= args.length) {
            throw new WrongUsageException("commands.farmers_future_delight.loot.usage");
        }
        String source = args[index];
        if ("loot".equals(source)) {
            requireExact(args, index, 2, 2);
            return generateTable(server, sender, parseTableId(args[index + 1]));
        }
        if ("fish".equals(source)) {
            requireExact(args, index, 5, 6);
            ResourceLocation tableId = parseTableId(args[index + 1]);
            BlockPos pos = FFDCommandCoordinates.parseBlockPosition(sender, args, index + 2);
            Vec3d origin = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D);
            ItemStack tool = args.length == index + 6
                    ? parseTool(sender, args[index + 5]) : ItemStack.EMPTY;
            WorldServer world = getWorld(server, sender);
            if (!world.isBlockLoaded(pos)) {
                throw new CommandException("commands.farmers_future_delight.loot.unloaded_block");
            }
            List<ItemStack> loot = FFDBlockLootTables.generateTableLoot(world, tableId, tool,
                    null, origin);
            if (loot == null) {
                throw new CommandException("commands.farmers_future_delight.loot.invalid_table",
                        tableId);
            }
            return loot;
        }
        if ("kill".equals(source)) {
            requireExact(args, index, 2, 2);
            Entity target = FFDCommandEntities.getEntity(server, sender, args[index + 1]);
            ResourceLocation tableId = getEntityLootTable(target);
            if (tableId == null) {
                throw new CommandException("commands.farmers_future_delight.loot.no_entity_table",
                        target.getDisplayName());
            }
            return generateTable(server, sender, tableId, target, DamageSource.MAGIC);
        }
        if ("mine".equals(source)) {
            requireExact(args, index, 4, 5);
            BlockPos pos = FFDCommandCoordinates.parseBlockPosition(sender, args, index + 1);
            ItemStack tool = args.length > index + 4
                    ? parseTool(sender, args[index + 4]) : ItemStack.EMPTY;
            return generateBlockDrops(server, sender, pos, tool);
        }
        throw new WrongUsageException("commands.farmers_future_delight.loot.usage");
    }

    private static List<ItemStack> generateTable(MinecraftServer server, ICommandSender sender,
                                                  ResourceLocation tableId)
            throws CommandException {
        return generateTable(server, sender, tableId, sender.getCommandSenderEntity(), null);
    }

    private static List<ItemStack> generateTable(MinecraftServer server, ICommandSender sender,
                                                  ResourceLocation tableId,
                                                  @Nullable Entity lootedEntity,
                                                  @Nullable DamageSource damageSource)
            throws CommandException {
        WorldServer world = getWorld(server, sender);
        FFDBlockLootTables.registerTable(tableId);
        LootTable table;
        try {
            table = world.getLootTableManager().getLootTableFromLocation(tableId);
        } catch (RuntimeException exception) {
            throw new CommandException("commands.farmers_future_delight.loot.invalid_table", tableId);
        }
        if (table == LootTable.EMPTY_LOOT_TABLE) {
            throw new CommandException("commands.farmers_future_delight.loot.invalid_table", tableId);
        }
        LootContext.Builder contextBuilder = new LootContext.Builder(world);
        EntityPlayer player = sender.getCommandSenderEntity() instanceof EntityPlayer
                ? (EntityPlayer) sender.getCommandSenderEntity() : null;
        if (player != null) {
            contextBuilder.withLootedEntity(player).withPlayer(player).withLuck(player.getLuck());
        }
        if (lootedEntity != null) {
            contextBuilder.withLootedEntity(lootedEntity);
        }
        if (damageSource != null) {
            contextBuilder.withDamageSource(damageSource);
        }
        return table.generateLootForPools(world.rand, contextBuilder.build());
    }

    private static List<ItemStack> generateBlockDrops(MinecraftServer server,
                                                       ICommandSender sender,
                                                       BlockPos pos,
                                                       ItemStack tool)
            throws CommandException {
        WorldServer world = getWorld(server, sender);
        if (!world.isBlockLoaded(pos)) {
            throw new CommandException("commands.farmers_future_delight.loot.unloaded_block");
        }
        net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        EntityPlayer player = sender.getCommandSenderEntity() instanceof EntityPlayer
                ? (EntityPlayer) sender.getCommandSenderEntity() : null;
        Vec3d origin = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        List<ItemStack> lootTableDrops = FFDBlockLootTables.generateLoot(world, state, tool,
                player, origin);
        if (lootTableDrops != null) {
            return lootTableDrops;
        }
        if (player != null && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0
                && block.canSilkHarvest(world, pos, state, player)) {
            ItemStack silkTouchDrop = getSilkTouchDrop(block, state);
            return silkTouchDrop.isEmpty() ? Collections.<ItemStack>emptyList()
                    : Collections.singletonList(silkTouchDrop);
        }
        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool);
        return block.getDrops(world, pos, state, fortune);
    }

    private static ItemStack getSilkTouchDrop(Block block,
                                               net.minecraft.block.state.IBlockState state) {
        try {
            Method method = ObfuscationReflectionHelper.findMethod(Block.class,
                    "func_180643_i", ItemStack.class, net.minecraft.block.state.IBlockState.class);
            Object result = method.invoke(block, state);
            return result instanceof ItemStack ? (ItemStack) result : ItemStack.EMPTY;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return ItemStack.EMPTY;
        }
    }

    private static int deliver(MinecraftServer server, ICommandSender sender,
                               Target target, List<ItemStack> loot) throws CommandException {
        if (target.type == TargetType.SPAWN) {
            WorldServer world = getWorld(server, sender);
            int delivered = 0;
            for (ItemStack stack : loot) {
                EntityItem item = new EntityItem(world, target.position.x,
                        target.position.y, target.position.z, stack.copy());
                item.setDefaultPickupDelay();
                if (world.spawnEntity(item)) {
                    delivered++;
                }
            }
            return delivered;
        }
        if (target.type == TargetType.GIVE) {
            int delivered = 0;
            for (ItemStack stack : loot) {
                for (EntityPlayerMP player : target.players) {
                    if (player.inventory.addItemStackToInventory(stack.copy())) {
                        delivered++;
                    }
                }
            }
            return delivered;
        }
        if (target.type == TargetType.INSERT || target.type == TargetType.REPLACE_BLOCK) {
            IInventory inventory = getInventory(server, sender, target.blockPos);
            if (target.type == TargetType.INSERT) {
                int delivered = 0;
                for (ItemStack stack : loot) {
                    if (insert(inventory, stack.copy())) {
                        delivered++;
                    }
                }
                inventory.markDirty();
                return delivered;
            }
            int count = target.count == null ? loot.size() : target.count;
            checkSlot(target.slot, inventory.getSizeInventory());
            int delivered = 0;
            for (int offset = 0; offset < count; offset++) {
                int slot = target.slot + offset;
                if (slot >= inventory.getSizeInventory()) {
                    break;
                }
                ItemStack stack = offset < loot.size() ? loot.get(offset).copy() : ItemStack.EMPTY;
                if (!stack.isEmpty() && !inventory.isItemValidForSlot(slot, stack)) {
                    continue;
                }
                inventory.setInventorySlotContents(slot, stack);
                delivered++;
            }
            inventory.markDirty();
            return delivered;
        }
        int count = target.count == null ? loot.size() : target.count;
        int delivered = 0;
        for (Entity entity : target.entities) {
            for (int offset = 0; offset < count; offset++) {
                ItemStack stack = offset < loot.size() ? loot.get(offset).copy() : ItemStack.EMPTY;
                if (replaceEntitySlot(entity, target.slotName, offset, stack)) {
                    delivered++;
                }
            }
            if (entity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) entity).inventoryContainer.detectAndSendChanges();
            }
        }
        return delivered;
    }

    private static boolean replaceEntitySlot(Entity entity, String slotName, int offset,
                                             ItemStack stack)
            throws CommandException {
        int slotId = parseEntitySlotId(slotName) + offset;
        return entity.replaceItemInInventory(slotId, stack);
    }

    private static int parseEntitySlotId(String slotName) throws CommandException {
        if ("weapon.mainhand".equals(slotName)) {
            return 98;
        }
        if ("weapon.offhand".equals(slotName)) {
            return 99;
        }
        if (slotName.startsWith("armor.")) {
            String armorSlot = slotName.substring("armor.".length());
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR
                        && slot.getName().equals(armorSlot)) {
                    return 100 + slot.getIndex();
                }
            }
        }
        if (slotName.startsWith("container.")) {
            return parseContainerSlot(slotName);
        }
        throw new CommandException("commands.farmers_future_delight.loot.invalid_slot", slotName);
    }

    private static IInventory getInventory(MinecraftServer server, ICommandSender sender,
                                           BlockPos pos) throws CommandException {
        WorldServer world = getWorld(server, sender);
        if (!world.isBlockLoaded(pos)) {
            throw new CommandException("commands.farmers_future_delight.loot.unloaded_block");
        }
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof IInventory)) {
            throw new CommandException("commands.farmers_future_delight.loot.not_container", pos);
        }
        return (IInventory) tile;
    }

    private static boolean insert(IInventory inventory, ItemStack stack) {
        boolean changed = false;
        for (int slot = 0; slot < inventory.getSizeInventory() && !stack.isEmpty(); slot++) {
            if (!inventory.isItemValidForSlot(slot, stack)) {
                continue;
            }
            ItemStack existing = inventory.getStackInSlot(slot);
            if (existing.isEmpty()) {
                int count = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(),
                        inventory.getInventoryStackLimit()));
                ItemStack placed = stack.copy();
                placed.setCount(count);
                inventory.setInventorySlotContents(slot, placed);
                stack.shrink(count);
                changed = true;
            } else if (ItemStack.areItemsEqual(existing, stack)
                    && ItemStack.areItemStackTagsEqual(existing, stack)) {
                int max = Math.min(existing.getMaxStackSize(), inventory.getInventoryStackLimit());
                int count = Math.min(stack.getCount(), Math.max(0, max - existing.getCount()));
                if (count > 0) {
                    existing.grow(count);
                    stack.shrink(count);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static int parseContainerSlot(String slotName) throws CommandException {
        if (!slotName.startsWith("container.")) {
            throw new CommandException("commands.farmers_future_delight.loot.invalid_slot",
                    slotName);
        }
        try {
            return Integer.parseInt(slotName.substring("container.".length()));
        } catch (NumberFormatException exception) {
            throw new CommandException("commands.farmers_future_delight.loot.invalid_slot",
                    slotName);
        }
    }

    private static void checkSlot(int slot, int size) throws CommandException {
        if (slot < 0 || slot >= size) {
            throw new CommandException("commands.farmers_future_delight.loot.invalid_slot",
                    "container." + slot);
        }
    }

    private static ItemStack parseTool(ICommandSender sender, String value) throws CommandException {
        Entity entity = sender.getCommandSenderEntity();
        if (("mainhand".equals(value) || "offhand".equals(value))
                && entity instanceof EntityLivingBase) {
            return ((EntityLivingBase) entity).getHeldItem(
                    "mainhand".equals(value) ? net.minecraft.util.EnumHand.MAIN_HAND
                            : net.minecraft.util.EnumHand.OFF_HAND).copy();
        }
        return new ItemStack(getItemByText(sender, value));
    }

    private static Vec3d parsePosition(ICommandSender sender, String[] args, int start)
            throws CommandException {
        return FFDCommandCoordinates.parse(sender, args, start, false);
    }

    private static ResourceLocation parseTableId(String value) throws CommandException {
        try {
            return new ResourceLocation(value);
        } catch (RuntimeException exception) {
            throw new CommandException("commands.farmers_future_delight.loot.invalid_table", value);
        }
    }

    private static ResourceLocation getEntityLootTable(Entity entity) {
        if (!(entity instanceof EntityLiving)) {
            return null;
        }
        Class<?> current = entity.getClass();
        while (current != null && EntityLiving.class.isAssignableFrom(current)) {
            try {
                Method method = current.getDeclaredMethod("getLootTable");
                method.setAccessible(true);
                Object value = method.invoke(entity);
                return value instanceof ResourceLocation ? (ResourceLocation) value : null;
            } catch (NoSuchMethodException exception) {
                current = current.getSuperclass();
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return null;
            }
        }
        return null;
    }

    private static WorldServer getWorld(MinecraftServer server, ICommandSender sender) {
        return server.getWorld(sender.getEntityWorld().provider.getDimension());
    }

    private static void require(String[] args, int length) throws WrongUsageException {
        if (args.length < length) {
            throw new WrongUsageException("commands.farmers_future_delight.loot.usage");
        }
    }

    private static void requireExact(String[] args, int start, int minLength, int maxLength)
            throws WrongUsageException {
        if (args.length < start + minLength || args.length > start + maxLength) {
            throw new WrongUsageException("commands.farmers_future_delight.loot.usage");
        }
    }

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private enum TargetType {
        SPAWN,
        GIVE,
        INSERT,
        REPLACE_BLOCK,
        REPLACE_ENTITY
    }

    private static final class Target {
        private final TargetType type;
        private int sourceIndex;
        private BlockPos blockPos;
        private Vec3d position;
        private List<EntityPlayerMP> players;
        private List<Entity> entities;
        private int slot;
        private String slotName;
        private Integer count;

        private Target(TargetType type, int sourceIndex) {
            this.type = type;
            this.sourceIndex = sourceIndex;
        }
    }
}
