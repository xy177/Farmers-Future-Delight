package xy177.farmersfuturedelight.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.api.CommandStorageApi;

public final class CommandData extends FFDServerCommand {
    @Override
    public String getName() {
        return "data";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.farmers_future_delight.data.usage";
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
        if (!"get".equals(args[0]) && !"merge".equals(args[0])
                && !"remove".equals(args[0]) && !"modify".equals(args[0])) {
            throw new WrongUsageException(getUsage(sender));
        }
        ParsedTarget parsed = parseTarget(server, sender, args, 1);
        if ("get".equals(args[0])) {
            executeGet(sender, parsed.target, args, parsed.nextIndex);
        } else if ("merge".equals(args[0])) {
            executeMerge(sender, parsed.target, args, parsed.nextIndex);
        } else if ("remove".equals(args[0])) {
            executeRemove(sender, parsed.target, args, parsed.nextIndex);
        } else {
            executeModify(server, sender, parsed.target, args, parsed.nextIndex);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                           String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "get", "merge", "modify", "remove");
        }
        if (args.length == 2 && ("get".equals(args[0]) || "merge".equals(args[0])
                || "modify".equals(args[0]) || "remove".equals(args[0]))) {
            return getListOfStringsMatchingLastWord(args, "entity", "block", "storage");
        }
        if (args.length == 3 && "storage".equals(args[1])) {
            List<String> ids = new ArrayList<>(CommandStorageApi.getIds(server));
            Collections.sort(ids);
            return getListOfStringsMatchingLastWord(args, ids);
        }
        if (args.length == 3 && "entity".equals(args[1])) {
            return getListOfStringsMatchingLastWord(args, "@s", "@p", "@a", "@e");
        }
        if (args.length >= 3 && "modify".equals(args[0])) {
            int targetEnd = targetEndIndex(args, 1);
            if (targetEnd < 0) {
                return Collections.emptyList();
            }
            int operationIndex = targetEnd + 1;
            if (args.length <= operationIndex) {
                return Collections.emptyList();
            }
            if (args.length == operationIndex + 1) {
                return getListOfStringsMatchingLastWord(args,
                        "set", "merge", "append", "prepend", "insert");
            }
            String operation = args[operationIndex];
            int sourceModeIndex = "insert".equals(operation)
                    ? operationIndex + 2 : operationIndex + 1;
            if (args.length <= sourceModeIndex) {
                return Collections.emptyList();
            }
            if (args.length == sourceModeIndex + 1) {
                return "set".equals(operation)
                        ? getListOfStringsMatchingLastWord(args, "value", "from", "string")
                        : getListOfStringsMatchingLastWord(args, "value", "from");
            }
            if ("from".equals(args[sourceModeIndex])
                    || "string".equals(args[sourceModeIndex])) {
                int sourceTargetIndex = sourceModeIndex + 1;
                if (args.length == sourceTargetIndex + 1) {
                    return getListOfStringsMatchingLastWord(args, "entity", "block", "storage");
                }
                if (args.length == sourceTargetIndex + 2
                        && "storage".equals(args[sourceTargetIndex])) {
                    List<String> ids = new ArrayList<>(CommandStorageApi.getIds(server));
                    Collections.sort(ids);
                    return getListOfStringsMatchingLastWord(args, ids);
                }
                if (args.length == sourceTargetIndex + 2
                        && "entity".equals(args[sourceTargetIndex])) {
                    return getListOfStringsMatchingLastWord(args, "@s", "@p", "@a", "@e");
                }
            }
        }
        return Collections.emptyList();
    }

    private static int targetEndIndex(String[] args, int startIndex) {
        if (startIndex >= args.length) {
            return -1;
        }
        if ("storage".equals(args[startIndex])) {
            return args.length > startIndex + 2 ? startIndex + 2 : -1;
        }
        if ("entity".equals(args[startIndex])) {
            return args.length > startIndex + 2 ? startIndex + 2 : -1;
        }
        if ("block".equals(args[startIndex])) {
            return args.length > startIndex + 4 ? startIndex + 4 : -1;
        }
        return -1;
    }

    private void executeGet(ICommandSender sender, DataTarget target, String[] args,
                            int startIndex) throws CommandException {
        if (args.length > startIndex + 2) {
            throw new WrongUsageException("commands.farmers_future_delight.data.usage");
        }
        NBTTagCompound data = target.read();
        List<NBTBase> values;
        String pathText = "";
        if (args.length > startIndex) {
            pathText = args[startIndex];
            values = parsePath(pathText).get(data);
            if (values.isEmpty()) {
                throw new CommandException("commands.farmers_future_delight.data.path_missing", pathText);
            }
            if (values.size() != 1) {
                throw new CommandException("commands.farmers_future_delight.data.path_multiple", pathText);
            }
        } else {
            values = Collections.<NBTBase>singletonList(data);
        }
        double scale = 1.0D;
        if (args.length == startIndex + 2) {
            try {
                scale = Double.parseDouble(args[startIndex + 1]);
            } catch (NumberFormatException exception) {
                throw new CommandException("commands.farmers_future_delight.data.invalid_scale",
                        args[startIndex + 1]);
            }
            if (values.size() != 1 || !isNumeric(values.get(0))) {
                throw new CommandException("commands.farmers_future_delight.data.not_single_number", pathText);
            }
            double result = ((NBTPrimitive) values.get(0)).getDouble() * scale;
            notifyCommandListener(sender, this, "commands.farmers_future_delight.data.get_scaled",
                    target.displayName(), pathText, scale, (int) Math.floor(result));
            FFDCommandPredicates.recordResult(sender, (int) Math.floor(result));
            return;
        }
        StringBuilder output = new StringBuilder();
        for (NBTBase value : values) {
            if (output.length() > 0) {
                output.append(", ");
            }
            output.append(value);
        }
        notifyCommandListener(sender, this, "commands.farmers_future_delight.data.get",
                target.displayName(), pathText, output.toString());
        FFDCommandPredicates.recordResult(sender,
                args.length > startIndex ? getDataResult(values.get(0), pathText) : 1);
    }

    private void executeMerge(ICommandSender sender, DataTarget target, String[] args,
                              int startIndex) throws CommandException {
        if (args.length <= startIndex) {
            throw new WrongUsageException("commands.farmers_future_delight.data.usage");
        }
        NBTTagCompound value = parseCompound(args, startIndex);
        NBTTagCompound before = target.read();
        NBTTagCompound after = before.copy();
        after.merge(value);
        if (after.equals(before)) {
            throw new CommandException("commands.farmers_future_delight.data.unchanged");
        }
        target.write(after);
        notifyCommandListener(sender, this, "commands.farmers_future_delight.data.modified",
                target.displayName());
        FFDCommandPredicates.recordResult(sender, 1);
    }

    private void executeRemove(ICommandSender sender, DataTarget target, String[] args,
                               int startIndex) throws CommandException {
        if (args.length != startIndex + 1) {
            throw new WrongUsageException("commands.farmers_future_delight.data.usage");
        }
        NBTTagCompound data = target.read();
        int removed = parsePath(args[startIndex]).remove(data);
        if (removed == 0) {
            throw new CommandException("commands.farmers_future_delight.data.path_missing",
                    args[startIndex]);
        }
        target.write(data);
        notifyCommandListener(sender, this, "commands.farmers_future_delight.data.modified",
                target.displayName());
        FFDCommandPredicates.recordResult(sender, removed);
    }

    private void executeModify(MinecraftServer server, ICommandSender sender, DataTarget target,
                               String[] args, int startIndex) throws CommandException {
        if (args.length < startIndex + 3) {
            throw new WrongUsageException("commands.farmers_future_delight.data.usage");
        }
        FFDNbtPath targetPath = parsePath(args[startIndex]);
        String operation = args[startIndex + 1];
        int sourceModeIndex = startIndex + 2;
        int insertionIndex = 0;
        if ("insert".equals(operation)) {
            if (args.length < startIndex + 4) {
                throw new WrongUsageException("commands.farmers_future_delight.data.usage");
            }
            try {
                insertionIndex = Integer.parseInt(args[startIndex + 2]);
            } catch (NumberFormatException exception) {
                throw new CommandException("commands.farmers_future_delight.data.invalid_index",
                        args[startIndex + 2]);
            }
            sourceModeIndex = startIndex + 3;
        } else if (!"set".equals(operation) && !"merge".equals(operation)
                && !"append".equals(operation) && !"prepend".equals(operation)) {
            throw new WrongUsageException("commands.farmers_future_delight.data.usage");
        }
        List<NBTBase> source;
        if ("set".equals(operation) && sourceModeIndex < args.length
                && "string".equals(args[sourceModeIndex])) {
            source = parseStringSource(server, sender, args, sourceModeIndex + 1);
        } else {
            source = parseSource(server, sender, args, sourceModeIndex);
        }
        NBTTagCompound data = target.read();
        int changed;
        if ("set".equals(operation)) {
            changed = source.isEmpty() ? 0
                    : targetPath.set(data, source.get(source.size() - 1));
        } else if ("merge".equals(operation)) {
            NBTTagCompound merged = new NBTTagCompound();
            for (NBTBase value : source) {
                if (!(value instanceof NBTTagCompound)) {
                    throw new CommandException("commands.farmers_future_delight.data.expected_compound",
                            value.toString());
                }
                merged.merge((NBTTagCompound) value);
            }
            changed = targetPath.merge(data, merged);
        } else {
            int index = "prepend".equals(operation) ? 0
                    : "append".equals(operation) ? -1 : insertionIndex;
            changed = targetPath.insert(data, index, source);
        }
        if (changed == 0) {
            throw new CommandException("commands.farmers_future_delight.data.unchanged");
        }
        target.write(data);
        notifyCommandListener(sender, this, "commands.farmers_future_delight.data.modified",
                target.displayName());
        FFDCommandPredicates.recordResult(sender, changed);
    }

    private static List<NBTBase> parseSource(MinecraftServer server, ICommandSender sender,
                                              String[] args, int index)
            throws CommandException {
        if (index >= args.length) {
            throw new WrongUsageException("commands.farmers_future_delight.data.usage");
        }
        if ("value".equals(args[index])) {
            if (index + 1 >= args.length) {
                throw new WrongUsageException("commands.farmers_future_delight.data.usage");
            }
            return Collections.singletonList(parseValue(args, index + 1));
        }
        if ("from".equals(args[index])) {
            ParsedTarget parsed = parseTarget(server, sender, args, index + 1);
            NBTTagCompound source = parsed.target.read();
            if (parsed.nextIndex == args.length) {
                return Collections.<NBTBase>singletonList(source);
            }
            if (parsed.nextIndex + 1 != args.length) {
                throw new WrongUsageException("commands.farmers_future_delight.data.usage");
            }
            return parsePath(args[parsed.nextIndex]).get(source);
        }
        throw new WrongUsageException("commands.farmers_future_delight.data.usage");
    }

    private static List<NBTBase> parseStringSource(MinecraftServer server, ICommandSender sender,
                                                    String[] args, int index)
            throws CommandException {
        ParsedTarget parsed = parseTarget(server, sender, args, index);
        NBTTagCompound source = parsed.target.read();
        List<NBTBase> values;
        Integer substringStart = null;
        Integer substringEnd = null;
        if (parsed.nextIndex == args.length) {
            values = Collections.<NBTBase>singletonList(source);
        } else {
            int trailing = args.length - parsed.nextIndex;
            if (trailing < 1 || trailing > 3) {
                throw new WrongUsageException("commands.farmers_future_delight.data.usage");
            }
            values = parsePath(args[parsed.nextIndex]).get(source);
            if (values.isEmpty()) {
                throw new CommandException("commands.farmers_future_delight.data.path_missing",
                        args[parsed.nextIndex]);
            }
            if (trailing >= 2) {
                substringStart = parseSubstringIndex(args[parsed.nextIndex + 1]);
                substringEnd = trailing == 3
                        ? parseSubstringIndex(args[parsed.nextIndex + 2]) : null;
            }
        }
        List<NBTBase> result = new ArrayList<>(values.size());
        for (NBTBase value : values) {
            String text = getStringValue(value);
            if (substringStart != null) {
                int length = text.length();
                int absoluteStart = substringStart >= 0 ? substringStart : length + substringStart;
                int absoluteEnd = substringEnd == null ? length
                        : substringEnd >= 0 ? substringEnd : length + substringEnd;
                if (absoluteStart < 0 || absoluteEnd > length || absoluteStart > absoluteEnd) {
                    throw new CommandException("commands.farmers_future_delight.data.invalid_substring",
                            absoluteStart, absoluteEnd);
                }
                text = text.substring(absoluteStart, absoluteEnd);
            }
            result.add(new NBTTagString(text));
        }
        return result;
    }

    private static String getStringValue(NBTBase value) throws CommandException {
        if (value instanceof NBTTagString) {
            return ((NBTTagString) value).getString();
        }
        if (isNumeric(value)) {
            return value.toString();
        }
        throw new CommandException("commands.farmers_future_delight.data.expected_value",
                value.toString());
    }

    private static int parseSubstringIndex(String value) throws CommandException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new CommandException(
                    "commands.farmers_future_delight.data.invalid_substring_index", value);
        }
    }

    private static ParsedTarget parseTarget(MinecraftServer server, ICommandSender sender,
                                            String[] args, int startIndex)
            throws CommandException {
        if (startIndex >= args.length) {
            throw new WrongUsageException("commands.farmers_future_delight.data.usage");
        }
        String type = args[startIndex];
        if ("storage".equals(type)) {
            if (startIndex + 1 >= args.length) {
                throw new WrongUsageException("commands.farmers_future_delight.data.usage");
            }
            ResourceLocation id = parseId(args[startIndex + 1]);
            return new ParsedTarget(new StorageTarget(server, id), startIndex + 2);
        }
        if ("block".equals(type)) {
            if (startIndex + 3 >= args.length) {
                throw new WrongUsageException("commands.farmers_future_delight.data.usage");
            }
            BlockPos position = FFDCommandCoordinates.parseBlockPosition(
                    sender, args, startIndex + 1);
            return new ParsedTarget(new BlockTarget(sender.getEntityWorld(), position),
                    startIndex + 4);
        }
        if ("entity".equals(type)) {
            if (startIndex + 1 >= args.length) {
                throw new WrongUsageException("commands.farmers_future_delight.data.usage");
            }
            Entity entity = FFDCommandEntities.getEntity(server, sender, args[startIndex + 1]);
            return new ParsedTarget(new EntityTarget(entity), startIndex + 2);
        }
        throw new WrongUsageException("commands.farmers_future_delight.data.usage");
    }

    static int getExecuteStoreTargetLength(String[] args, int startIndex)
            throws CommandException {
        if (startIndex >= args.length) {
            throw new WrongUsageException("commands.farmers_future_delight.data.usage");
        }
        String type = args[startIndex];
        if (("storage".equals(type) || "entity".equals(type))
                && startIndex + 1 < args.length) {
            return 2;
        }
        if ("block".equals(type) && startIndex + 3 < args.length) {
            return 4;
        }
        throw new WrongUsageException("commands.farmers_future_delight.data.usage");
    }

    static ExecuteStoreTarget createExecuteStoreTarget(MinecraftServer server,
                                                        ICommandSender sender, String[] args,
                                                        int targetStart, int pathIndex,
                                                        int typeIndex, int scaleIndex)
            throws CommandException {
        ParsedTarget parsed = parseTarget(server, sender, args, targetStart);
        if (parsed.nextIndex != pathIndex || typeIndex >= args.length
                || scaleIndex >= args.length) {
            throw new WrongUsageException("commands.farmers_future_delight.data.usage");
        }
        FFDNbtPath path = parsePath(args[pathIndex]);
        String type = args[typeIndex];
        if (!"byte".equals(type) && !"short".equals(type) && !"int".equals(type)
                && !"long".equals(type) && !"float".equals(type)
                && !"double".equals(type)) {
            throw new WrongUsageException("commands.farmers_future_delight.data.usage");
        }
        double scale;
        try {
            scale = Double.parseDouble(args[scaleIndex]);
            if (!Double.isFinite(scale)) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            throw new CommandException("commands.farmers_future_delight.execute.invalid_scale",
                    args[scaleIndex]);
        }
        return new ExecuteStoreTarget(parsed.target, path, type, scale);
    }

    private static TileEntity getBlockEntity(World world, BlockPos position) throws CommandException {
        if (!world.isBlockLoaded(position)) {
            throw new CommandException("commands.farmers_future_delight.data.block_unloaded",
                    position.getX(), position.getY(), position.getZ());
        }
        TileEntity tile = world.getTileEntity(position);
        if (tile == null) {
            throw new CommandException("commands.farmers_future_delight.data.no_block_entity",
                    position.getX(), position.getY(), position.getZ());
        }
        return tile;
    }

    private static void preserveTag(NBTTagCompound target, NBTTagCompound original, String key) {
        if (original.hasKey(key)) {
            target.setTag(key, original.getTag(key).copy());
        } else {
            target.removeTag(key);
        }
    }

    private static NBTTagCompound parseCompound(String[] args, int start) throws CommandException {
        String text = String.join(" ", java.util.Arrays.copyOfRange(args, start, args.length));
        try {
            return JsonToNBT.getTagFromJson(text);
        } catch (Exception exception) {
            throw new CommandException("commands.farmers_future_delight.data.invalid_nbt", text);
        }
    }

    private static NBTBase parseValue(String[] args, int start) throws CommandException {
        String text = String.join(" ", java.util.Arrays.copyOfRange(args, start, args.length));
        try {
            return JsonToNBT.getTagFromJson(text);
        } catch (Exception exception) {
            try {
                NBTTagCompound wrapper = JsonToNBT.getTagFromJson("{value:" + text + "}");
                NBTBase value = wrapper.getTag("value");
                if (value != null) {
                    return value;
                }
            } catch (Exception nested) {
                throw new CommandException("commands.farmers_future_delight.data.invalid_nbt", text);
            }
            throw new CommandException("commands.farmers_future_delight.data.invalid_value");
        }
    }

    private static ResourceLocation parseId(String value) throws CommandException {
        try {
            return new ResourceLocation(value);
        } catch (RuntimeException exception) {
            throw new CommandException("commands.farmers_future_delight.data.invalid_id", value);
        }
    }

    private static FFDNbtPath parsePath(String value) throws CommandException {
        try {
            return FFDNbtPath.parse(value);
        } catch (IllegalArgumentException exception) {
            throw new CommandException("commands.farmers_future_delight.data.invalid_path", value);
        }
    }

    private static boolean isNumeric(NBTBase value) {
        return value.getId() >= 1 && value.getId() <= 6;
    }

    private static int getDataResult(NBTBase value, String path) throws CommandException {
        if (isNumeric(value)) {
            return (int) Math.floor(((NBTPrimitive) value).getDouble());
        }
        if (value instanceof NBTTagList) {
            return ((NBTTagList) value).tagCount();
        }
        if (value instanceof NBTTagCompound) {
            return ((NBTTagCompound) value).getKeySet().size();
        }
        if (value instanceof NBTTagString) {
            return ((NBTTagString) value).getString().length();
        }
        if (value instanceof NBTTagByteArray) {
            return ((NBTTagByteArray) value).getByteArray().length;
        }
        if (value instanceof NBTTagIntArray) {
            return ((NBTTagIntArray) value).getIntArray().length;
        }
        throw new CommandException("commands.farmers_future_delight.data.path_missing", path);
    }

    private interface DataTarget {
        NBTTagCompound read() throws CommandException;

        void write(NBTTagCompound data) throws CommandException;

        String displayName();
    }

    static final class ExecuteStoreTarget {
        private final DataTarget target;
        private final FFDNbtPath path;
        private final String type;
        private final double scale;

        private ExecuteStoreTarget(DataTarget target, FFDNbtPath path, String type,
                                   double scale) {
            this.target = target;
            this.path = path;
            this.type = type;
            this.scale = scale;
        }

        void apply(int commandResult, boolean storeResult) throws CommandException {
            int value = storeResult ? commandResult : commandResult == 0 ? 0 : 1;
            double scaled = value * scale;
            NBTBase numeric;
            switch (type) {
                case "byte":
                    numeric = new NBTTagByte((byte) scaled);
                    break;
                case "short":
                    numeric = new NBTTagShort((short) scaled);
                    break;
                case "int":
                    numeric = new NBTTagInt((int) scaled);
                    break;
                case "long":
                    numeric = new NBTTagLong((long) scaled);
                    break;
                case "float":
                    numeric = new NBTTagFloat((float) scaled);
                    break;
                default:
                    numeric = new NBTTagDouble(scaled);
                    break;
            }
            NBTTagCompound data = target.read();
            if (path.set(data, numeric) > 0) {
                target.write(data);
            }
        }
    }

    private static final class ParsedTarget {
        private final DataTarget target;
        private final int nextIndex;

        private ParsedTarget(DataTarget target, int nextIndex) {
            this.target = target;
            this.nextIndex = nextIndex;
        }
    }

    private static final class StorageTarget implements DataTarget {
        private final MinecraftServer server;
        private final ResourceLocation id;

        private StorageTarget(MinecraftServer server, ResourceLocation id) {
            this.server = server;
            this.id = id;
        }

        @Override
        public NBTTagCompound read() {
            return CommandStorageApi.get(server, id);
        }

        @Override
        public void write(NBTTagCompound data) {
            CommandStorageApi.set(server, id, data);
        }

        @Override
        public String displayName() {
            return id.toString();
        }
    }

    private static final class BlockTarget implements DataTarget {
        private final World world;
        private final BlockPos position;

        private BlockTarget(World world, BlockPos position) {
            this.world = world;
            this.position = position;
        }

        @Override
        public NBTTagCompound read() throws CommandException {
            return getBlockEntity(world, position).writeToNBT(new NBTTagCompound());
        }

        @Override
        public void write(NBTTagCompound data) throws CommandException {
            TileEntity tile = getBlockEntity(world, position);
            NBTTagCompound original = tile.writeToNBT(new NBTTagCompound());
            NBTTagCompound updated = data.copy();
            preserveTag(updated, original, "id");
            preserveTag(updated, original, "x");
            preserveTag(updated, original, "y");
            preserveTag(updated, original, "z");
            if (updated.equals(original)) {
                throw new CommandException("commands.farmers_future_delight.data.unchanged");
            }
            tile.readFromNBT(updated);
            tile.markDirty();
            net.minecraft.block.state.IBlockState state = world.getBlockState(position);
            world.notifyBlockUpdate(position, state, state, 3);
        }

        @Override
        public String displayName() {
            return position.toString();
        }
    }

    private static final class EntityTarget implements DataTarget {
        private final Entity entity;

        private EntityTarget(Entity entity) {
            this.entity = entity;
        }

        @Override
        public NBTTagCompound read() {
            return entity.writeToNBT(new NBTTagCompound());
        }

        @Override
        public void write(NBTTagCompound data) throws CommandException {
            if (entity instanceof EntityPlayer) {
                throw new CommandException("commands.farmers_future_delight.data.player_unsupported");
            }
            NBTTagCompound original = entity.writeToNBT(new NBTTagCompound());
            NBTTagCompound updated = data.copy();
            preserveTag(updated, original, "id");
            preserveTag(updated, original, "UUID");
            preserveTag(updated, original, "UUIDMost");
            preserveTag(updated, original, "UUIDLeast");
            if (updated.equals(original)) {
                throw new CommandException("commands.farmers_future_delight.data.unchanged");
            }
            entity.readFromNBT(updated);
        }

        @Override
        public String displayName() {
            return entity.getName();
        }
    }
}
