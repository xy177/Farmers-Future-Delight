package xy177.farmersfuturedelight.common.command;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.util.concurrent.Futures;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandExecuteAt;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;

import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.CommandStorageApi;
import xy177.farmersfuturedelight.core.FFDHeightHooks;
import xy177.farmersfuturedelight.common.world.biome.FFDBiomeCompatibility;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDCommandPredicates {
    private static final Gson CONDITION_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(LootCondition.class,
                    new LootConditionManager.Serializer())
            .create();

    private FFDCommandPredicates() {
    }

    public static void recordResult(ICommandSender sender, int result) {
        sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, result);
        if (sender instanceof ContextSender) {
            ((ContextSender) sender).setCommandResult(result, true);
        }
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        if (!(event.getCommand() instanceof CommandExecuteAt)) {
            return;
        }
        String[] arguments = event.getParameters();
        if (arguments.length < 2 || !("if".equals(arguments[0])
                || "unless".equals(arguments[0]) || "as".equals(arguments[0])
                || "at".equals(arguments[0]) || "in".equals(arguments[0])
                || "positioned".equals(arguments[0]) || "align".equals(arguments[0])
                || "rotated".equals(arguments[0]) || "facing".equals(arguments[0])
                || "anchored".equals(arguments[0]) || "store".equals(arguments[0]))) {
            return;
        }
        event.setCanceled(true);
        MinecraftServer server = event.getSender().getServer();
        if (server != null && !server.isCallingFromMinecraftThread()) {
            Futures.getUnchecked(server.addScheduledTask(
                    () -> executeConditionalCommand(event.getSender(), arguments)));
        } else {
            executeConditionalCommand(event.getSender(), arguments);
        }
    }

    private static void executeConditionalCommand(ICommandSender sender, String[] arguments) {
        try {
            MinecraftServer server = sender.getServer();
            if (server == null) {
                throw new CommandException("commands.farmers_future_delight.predicate.unavailable");
            }
            List<ContextSender> contexts = new ArrayList<>();
            contexts.add(sender instanceof ContextSender ? (ContextSender) sender
                    : new ContextSender(sender, sender.getCommandSenderEntity(),
                    sender.getEntityWorld(), sender.getPositionVector()));
            List<ScoreStore> scoreStores = new ArrayList<>();
            List<DataStore> dataStores = new ArrayList<>();
            int cursor = 0;
            while (cursor < arguments.length && !"run".equals(arguments[cursor])) {
                String subcommand = arguments[cursor];
                if ("as".equals(subcommand) || "at".equals(subcommand)) {
                    if (cursor + 1 >= arguments.length) {
                        throw new CommandException("commands.farmers_future_delight.execute.usage");
                    }
                    List<ContextSender> expanded = new ArrayList<>();
                    for (ContextSender context : contexts) {
                        for (Entity target : getEntities(server, context,
                                arguments[cursor + 1])) {
                            expanded.add("as".equals(subcommand)
                                    ? context.withExecutor(target)
                                    : context.at(target));
                        }
                    }
                    contexts = expanded;
                    cursor += 2;
                } else if ("in".equals(subcommand)) {
                    if (cursor + 1 >= arguments.length) {
                        throw new CommandException("commands.farmers_future_delight.execute.usage");
                    }
                    int dimensionId = parseDimensionId(arguments[cursor + 1]);
                    if (!DimensionManager.isDimensionRegistered(dimensionId)) {
                        throw new CommandException(
                                "commands.farmers_future_delight.execute.dimension_invalid",
                                arguments[cursor + 1]);
                    }
                    WorldServer targetWorld = server.getWorld(dimensionId);
                    if (targetWorld == null) {
                        throw new CommandException(
                                "commands.farmers_future_delight.execute.dimension_unloaded",
                                arguments[cursor + 1]);
                    }
                    List<ContextSender> changedWorld = new ArrayList<>();
                    for (ContextSender context : contexts) {
                        changedWorld.add(context.in(targetWorld));
                    }
                    contexts = changedWorld;
                    cursor += 2;
                } else if ("positioned".equals(subcommand)) {
                    if (cursor + 1 >= arguments.length) {
                        throw new CommandException("commands.farmers_future_delight.execute.usage");
                    }
                    if ("over".equals(arguments[cursor + 1])) {
                        if (cursor + 2 >= arguments.length) {
                            throw new CommandException(
                                    "commands.farmers_future_delight.execute.usage");
                        }
                        String heightmap = validateHeightmap(arguments[cursor + 2]);
                        List<ContextSender> positioned = new ArrayList<>();
                        for (ContextSender context : contexts) {
                            Vec3d origin = context.getPositionVector();
                            int x = (int) Math.floor(origin.x);
                            int z = (int) Math.floor(origin.z);
                            int y = getHeightmapHeight(context.getEntityWorld(), x, z, heightmap);
                            positioned.add(context.positionedAt(
                                    new Vec3d(origin.x, y, origin.z)));
                        }
                        contexts = positioned;
                        cursor += 3;
                    } else if ("as".equals(arguments[cursor + 1])) {
                        if (cursor + 2 >= arguments.length) {
                            throw new CommandException(
                                    "commands.farmers_future_delight.execute.usage");
                        }
                        List<ContextSender> expanded = new ArrayList<>();
                        for (ContextSender context : contexts) {
                            for (Entity target : getEntities(server, context,
                                    arguments[cursor + 2])) {
                                expanded.add(context.positionedAt(target));
                            }
                        }
                        contexts = expanded;
                        cursor += 3;
                    } else {
                        if (cursor + 3 >= arguments.length) {
                            throw new CommandException(
                                    "commands.farmers_future_delight.execute.usage");
                        }
                        validateCoordinates(arguments, cursor + 1, true);
                        List<ContextSender> positioned = new ArrayList<>();
                        for (ContextSender context : contexts) {
                            positioned.add(context.positionedAtFeet(parseCoordinates(context,
                                    arguments, cursor + 1, true)));
                        }
                        contexts = positioned;
                        cursor += 4;
                    }
                } else if ("rotated".equals(subcommand)) {
                    if (cursor + 1 >= arguments.length) {
                        throw new CommandException("commands.farmers_future_delight.execute.usage");
                    }
                    if ("as".equals(arguments[cursor + 1])) {
                        if (cursor + 2 >= arguments.length) {
                            throw new CommandException(
                                    "commands.farmers_future_delight.execute.usage");
                        }
                        List<ContextSender> rotated = new ArrayList<>();
                        for (ContextSender context : contexts) {
                            for (Entity target : getEntities(server, context,
                                    arguments[cursor + 2])) {
                                rotated.add(context.withRotation(target.rotationPitch,
                                        target.rotationYaw));
                            }
                        }
                        contexts = rotated;
                        cursor += 3;
                    } else {
                        if (cursor + 2 >= arguments.length) {
                            throw new CommandException(
                                    "commands.farmers_future_delight.execute.usage");
                        }
                        CommandBase.parseDouble(0.0D, arguments[cursor + 1], false);
                        CommandBase.parseDouble(0.0D, arguments[cursor + 2], false);
                        List<ContextSender> rotated = new ArrayList<>();
                        for (ContextSender context : contexts) {
                            double yaw = CommandBase.parseDouble(context.getYaw(),
                                    arguments[cursor + 1], false);
                            double pitch = CommandBase.parseDouble(context.getPitch(),
                                    arguments[cursor + 2], false);
                            rotated.add(context.withRotation((float) pitch, (float) yaw));
                        }
                        contexts = rotated;
                        cursor += 3;
                    }
                } else if ("facing".equals(subcommand)) {
                    if (cursor + 1 >= arguments.length) {
                        throw new CommandException("commands.farmers_future_delight.execute.usage");
                    }
                    if ("entity".equals(arguments[cursor + 1])) {
                        if (cursor + 3 >= arguments.length) {
                            throw new CommandException(
                                    "commands.farmers_future_delight.execute.usage");
                        }
                        boolean targetEyes = parseAnchor(arguments[cursor + 3]);
                        List<ContextSender> facing = new ArrayList<>();
                        for (ContextSender context : contexts) {
                            for (Entity target : getEntities(server, context,
                                    arguments[cursor + 2])) {
                                double targetY = target.posY
                                        + (targetEyes ? target.getEyeHeight() : 0.0D);
                                facing.add(context.facing(new Vec3d(target.posX, targetY,
                                        target.posZ)));
                            }
                        }
                        contexts = facing;
                        cursor += 4;
                    } else {
                        if (cursor + 3 >= arguments.length) {
                            throw new CommandException(
                                    "commands.farmers_future_delight.execute.usage");
                        }
                        validateCoordinates(arguments, cursor + 1, false);
                        List<ContextSender> facing = new ArrayList<>();
                        for (ContextSender context : contexts) {
                            facing.add(context.facing(parseCoordinates(context, arguments,
                                    cursor + 1, false)));
                        }
                        contexts = facing;
                        cursor += 4;
                    }
                } else if ("anchored".equals(subcommand)) {
                    if (cursor + 1 >= arguments.length) {
                        throw new CommandException("commands.farmers_future_delight.execute.usage");
                    }
                    boolean eyesAnchor = parseAnchor(arguments[cursor + 1]);
                    List<ContextSender> anchored = new ArrayList<>();
                    for (ContextSender context : contexts) {
                        anchored.add(context.withEyesAnchor(eyesAnchor));
                    }
                    contexts = anchored;
                    cursor += 2;
                } else if ("align".equals(subcommand)) {
                    if (cursor + 1 >= arguments.length) {
                        throw new CommandException(
                                "commands.farmers_future_delight.execute.usage");
                    }
                    List<ContextSender> aligned = new ArrayList<>();
                    for (ContextSender context : contexts) {
                        aligned.add(context.positionedAt(
                                alignPosition(context.getPositionVector(), arguments[cursor + 1])));
                    }
                    contexts = aligned;
                    cursor += 2;
                } else if ("store".equals(subcommand)) {
                    if (cursor + 2 < arguments.length
                            && "score".equals(arguments[cursor + 2])) {
                        ScoreStore store = parseScoreStore(arguments, cursor);
                        scoreStores.add(store);
                        cursor += store.length;
                    } else {
                        DataStore store = parseDataStore(arguments, cursor);
                        dataStores.add(store);
                        cursor += store.length;
                    }
                } else if ("if".equals(subcommand) || "unless".equals(subcommand)) {
                    List<ContextSender> matching = new ArrayList<>();
                    int conditionLength = getConditionLength(arguments, cursor);
                    if (cursor + conditionLength > arguments.length) {
                        throw new CommandException("commands.farmers_future_delight.execute.usage");
                    }
                    for (ContextSender context : contexts) {
                        ConditionResult result = evaluateCondition(server, context,
                                arguments, cursor);
                        if (result.matches == "if".equals(subcommand)) {
                            matching.add(context);
                        }
                    }
                    contexts = matching;
                    cursor += conditionLength;
                } else {
                    throw new CommandException("commands.farmers_future_delight.execute.usage");
                }
            }
            if (cursor + 1 >= arguments.length || !"run".equals(arguments[cursor])) {
                throw new CommandException("commands.farmers_future_delight.execute.usage");
            }
            List<List<ResolvedScoreStore>> resolvedStores = new ArrayList<>();
            List<List<ResolvedDataStore>> resolvedDataStores = new ArrayList<>();
            if (!scoreStores.isEmpty()) {
                for (ContextSender context : contexts) {
                    List<ResolvedScoreStore> contextStores = new ArrayList<>();
                    for (ScoreStore store : scoreStores) {
                        contextStores.add(store.resolve(server, context));
                    }
                    resolvedStores.add(contextStores);
                }
            }
            if (!dataStores.isEmpty()) {
                for (ContextSender context : contexts) {
                    List<ResolvedDataStore> contextStores = new ArrayList<>();
                    for (DataStore store : dataStores) {
                        contextStores.add(store.resolve(server, context, arguments));
                    }
                    resolvedDataStores.add(contextStores);
                }
            }
            String nestedCommand = String.join(" ",
                    java.util.Arrays.copyOfRange(arguments, cursor + 1, arguments.length));
            int successCount = 0;
            boolean succeeded = false;
            for (int contextIndex = 0; contextIndex < contexts.size(); contextIndex++) {
                ContextSender context = contexts.get(contextIndex);
                context.clearCommandResult();
                int executions = server.getCommandManager().executeCommand(context, nestedCommand);
                int result = context.getCommandResult(executions);
                boolean contextSucceeded = context.didCommandSucceed(executions);
                successCount += result;
                succeeded |= contextSucceeded;
                if (!scoreStores.isEmpty()) {
                    for (ResolvedScoreStore store : resolvedStores.get(contextIndex)) {
                        store.apply(result, contextSucceeded);
                    }
                }
                if (!dataStores.isEmpty()) {
                    for (ResolvedDataStore store :
                            resolvedDataStores.get(contextIndex)) {
                        store.apply(result, contextSucceeded);
                    }
                }
            }
            sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, successCount);
            if (sender instanceof ContextSender) {
                ((ContextSender) sender).setCommandResult(successCount, succeeded);
            }
        } catch (CommandException exception) {
            TextComponentTranslation message = new TextComponentTranslation(
                    exception.getMessage(), exception.getErrorObjects());
            message.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(message);
            sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, 0);
            if (sender instanceof ContextSender) {
                ((ContextSender) sender).setCommandResult(0, false);
            }
        }
    }

    private static List<Entity> getEntities(MinecraftServer server, ICommandSender sender,
                                            String selector) throws CommandException {
        try {
            return FFDCommandEntities.getEntities(server, sender, selector);
        } catch (EntityNotFoundException exception) {
            return java.util.Collections.emptyList();
        }
    }

    private static int getConditionLength(String[] arguments, int cursor) throws CommandException {
        if (cursor + 1 >= arguments.length) {
            throw new CommandException("commands.farmers_future_delight.execute.usage");
        }
        String condition = arguments[cursor + 1];
        if ("score".equals(condition)) {
            if (cursor + 4 >= arguments.length) {
                throw new CommandException("commands.farmers_future_delight.execute.usage");
            }
            String operation = arguments[cursor + 4];
            if ("matches".equals(operation)) {
                if (cursor + 5 >= arguments.length) {
                    throw new CommandException("commands.farmers_future_delight.execute.usage");
                }
                parseScoreRange(arguments[cursor + 5]);
                return 6;
            }
            if (!isScoreOperation(operation) || cursor + 6 >= arguments.length) {
                throw new CommandException("commands.farmers_future_delight.execute.usage");
            }
            return 7;
        }
        if ("predicate".equals(condition) || "entity".equals(condition)
                || "dimension".equals(condition)) {
            return 3;
        }
        if ("loaded".equals(condition)) {
            return 5;
        }
        if ("biome".equals(condition)) {
            return 6;
        }
        if ("block".equals(condition)) {
            return 6;
        }
        if ("blocks".equals(condition)) {
            return 12;
        }
        if ("data".equals(condition)) {
            int target = cursor + 2;
            if (target >= arguments.length) {
                throw new CommandException("commands.farmers_future_delight.execute.usage");
            }
            if ("storage".equals(arguments[target])) {
                return 5;
            }
            if ("entity".equals(arguments[target])) {
                return 5;
            }
            if ("block".equals(arguments[target])) {
                return 7;
            }
        }
        throw new CommandException("commands.farmers_future_delight.execute.usage");
    }

    private static ScoreStore parseScoreStore(String[] arguments, int cursor)
            throws CommandException {
        if (cursor + 4 >= arguments.length
                || !("result".equals(arguments[cursor + 1])
                || "success".equals(arguments[cursor + 1]))
                || !"score".equals(arguments[cursor + 2])) {
            throw new CommandException("commands.farmers_future_delight.execute.usage");
        }
        return new ScoreStore("result".equals(arguments[cursor + 1]),
                arguments[cursor + 3], arguments[cursor + 4], 5);
    }

    private static DataStore parseDataStore(String[] arguments, int cursor)
            throws CommandException {
        if (cursor + 2 >= arguments.length
                || !("result".equals(arguments[cursor + 1])
                || "success".equals(arguments[cursor + 1]))) {
            throw new CommandException("commands.farmers_future_delight.execute.usage");
        }
        int targetStart = cursor + ("data".equals(arguments[cursor + 2]) ? 3 : 2);
        int targetLength = CommandData.getExecuteStoreTargetLength(arguments, targetStart);
        int pathIndex = targetStart + targetLength;
        int typeIndex = pathIndex + 1;
        int scaleIndex = pathIndex + 2;
        if (scaleIndex >= arguments.length) {
            throw new CommandException("commands.farmers_future_delight.execute.usage");
        }
        String type = arguments[typeIndex];
        if (!("byte".equals(type) || "short".equals(type) || "int".equals(type)
                || "long".equals(type) || "float".equals(type) || "double".equals(type))) {
            throw new CommandException("commands.farmers_future_delight.execute.usage");
        }
        try {
            double scale = Double.parseDouble(arguments[scaleIndex]);
            if (!Double.isFinite(scale)) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            throw new CommandException("commands.farmers_future_delight.execute.invalid_scale",
                    arguments[scaleIndex]);
        }
        return new DataStore("result".equals(arguments[cursor + 1]), targetStart,
                pathIndex, typeIndex, scaleIndex, scaleIndex - cursor + 1);
    }

    private static BlockPos parseBlockPosition(ContextSender sender, String[] arguments,
                                               int start) throws CommandException {
        return FFDCommandCoordinates.parseBlockPosition(sender, arguments, start);
    }

    private static Vec3d parseCoordinates(ContextSender sender, String[] arguments,
                                         int start, boolean centerBlock)
            throws CommandException {
        return FFDCommandCoordinates.parse(sender, arguments, start, centerBlock);
    }

    private static void validateCoordinates(String[] arguments, int start, boolean centerBlock)
            throws CommandException {
        FFDCommandCoordinates.validate(arguments, start, centerBlock);
    }

    private static Vec3d alignPosition(Vec3d position, String axes) throws CommandException {
        boolean alignX = false;
        boolean alignY = false;
        boolean alignZ = false;
        for (int index = 0; index < axes.length(); index++) {
            char axis = axes.charAt(index);
            if (axis == 'x' && !alignX) {
                alignX = true;
            } else if (axis == 'y' && !alignY) {
                alignY = true;
            } else if (axis == 'z' && !alignZ) {
                alignZ = true;
            } else {
                throw new CommandException(
                        "commands.farmers_future_delight.execute.invalid_axes", axes);
            }
        }
        if (!alignX && !alignY && !alignZ) {
            throw new CommandException(
                    "commands.farmers_future_delight.execute.invalid_axes", axes);
        }
        return new Vec3d(alignX ? Math.floor(position.x) : position.x,
                alignY ? Math.floor(position.y) : position.y,
                alignZ ? Math.floor(position.z) : position.z);
    }

    private static String validateHeightmap(String value) throws CommandException {
        switch (value) {
            case "world_surface":
            case "ocean_floor":
            case "motion_blocking":
            case "motion_blocking_no_leaves":
                return value;
            default:
                throw new CommandException(
                        "commands.farmers_future_delight.execute.heightmap_invalid", value);
        }
    }

    private static int getHeightmapHeight(World world, int x, int z, String heightmap)
            throws CommandException {
        int minimumY = FFDHeightHooks.minY(world);
        int maximumY = FFDHeightHooks.maxYExclusive(world);
        if (!world.isBlockLoaded(new BlockPos(x, minimumY, z))) {
            throw new CommandException("commands.farmers_future_delight.data.block_unloaded",
                    x, minimumY, z);
        }
        for (int y = maximumY - 1; y >= minimumY; y--) {
            BlockPos position = new BlockPos(x, y, z);
            IBlockState state = world.getBlockState(position);
            Material material = state.getMaterial();
            boolean blocking = material.blocksMovement();
            boolean liquid = material.isLiquid();
            boolean matches;
            switch (heightmap) {
                case "world_surface":
                    matches = !state.getBlock().isAir(state, world, position);
                    break;
                case "ocean_floor":
                    matches = blocking;
                    break;
                case "motion_blocking":
                    matches = blocking || liquid;
                    break;
                case "motion_blocking_no_leaves":
                    matches = blocking && material != Material.LEAVES || liquid;
                    break;
                default:
                    throw new CommandException(
                            "commands.farmers_future_delight.execute.heightmap_invalid",
                            heightmap);
            }
            if (matches) return y + 1;
        }
        return minimumY;
    }

    private static ConditionResult evaluateCondition(MinecraftServer server,
                                                       ContextSender sender,
                                                       String[] arguments, int cursor)
            throws CommandException {
        String conditionType = arguments[cursor + 1];
        World senderWorld = sender.getEntityWorld();
        WorldServer world = senderWorld instanceof WorldServer
                ? (WorldServer) senderWorld : server.worlds[0];
        Entity executor = sender.getCommandSenderEntity();
        boolean result;
        int length;
        if ("predicate".equals(conditionType)) {
            LootContext.Builder builder = new LootContext.Builder(world);
            if (executor != null) {
                builder.withLootedEntity(executor);
            }
            if (executor instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) executor;
                builder.withPlayer(player).withLuck(player.getLuck());
            }
            result = testPredicate(world, parseId(arguments[cursor + 2]), builder.build(),
                    world.rand, new ArrayList<ResourceLocation>());
            length = 3;
        } else if ("entity".equals(conditionType)) {
            result = !getEntities(server, sender, arguments[cursor + 2]).isEmpty();
            length = 3;
        } else if ("score".equals(conditionType)) {
            if (cursor + 4 >= arguments.length) {
                throw new CommandException("commands.farmers_future_delight.execute.usage");
            }
            Scoreboard scoreboard = world.getScoreboard();
            Integer targetScore = getScoreValue(scoreboard, server, sender,
                    arguments[cursor + 2], arguments[cursor + 3]);
            String operation = arguments[cursor + 4];
            if ("matches".equals(operation)) {
                if (cursor + 5 >= arguments.length) {
                    throw new CommandException("commands.farmers_future_delight.execute.usage");
                }
                int[] range = parseScoreRange(arguments[cursor + 5]);
                result = targetScore != null && targetScore >= range[0]
                        && targetScore <= range[1];
                length = 6;
            } else {
                if (cursor + 6 >= arguments.length) {
                    throw new CommandException("commands.farmers_future_delight.execute.usage");
                }
                Integer sourceScore = getScoreValue(scoreboard, server, sender,
                        arguments[cursor + 5], arguments[cursor + 6]);
                result = targetScore != null && sourceScore != null
                        && compareScores(targetScore, sourceScore, operation);
                length = 7;
            }
        } else if ("dimension".equals(conditionType)) {
            result = world.provider.getDimension() == parseDimensionId(arguments[cursor + 2]);
            length = 3;
        } else if ("loaded".equals(conditionType)) {
            BlockPos position = parseBlockPosition(sender, arguments, cursor + 2);
            result = world.isBlockLoaded(position);
            length = 5;
        } else if ("biome".equals(conditionType)) {
            BlockPos position = parseBlockPosition(sender, arguments, cursor + 2);
            ResourceLocation biomeId = parseId(arguments[cursor + 5]);
            if (!FFDBiomeCompatibility.isKnown(biomeId)) {
                throw new CommandException("commands.farmers_future_delight.execute.biome_unknown",
                        biomeId);
            }
            result = FFDBiomeCompatibility.matches(world, position, biomeId);
            length = 6;
        } else if ("block".equals(conditionType)) {
            if (cursor + 5 >= arguments.length) {
                throw new CommandException("commands.farmers_future_delight.execute.usage");
            }
            BlockPos position = parseBlockPosition(sender, arguments, cursor + 2);
            result = testBlock(world, position, arguments[cursor + 5]);
            length = 6;
        } else if ("blocks".equals(conditionType)) {
            if (cursor + 11 >= arguments.length) {
                throw new CommandException("commands.farmers_future_delight.execute.usage");
            }
            result = testBlocks(world, sender, arguments, cursor + 2,
                    arguments[cursor + 11]);
            length = 12;
        } else if ("data".equals(conditionType)) {
            int targetIndex = cursor + 2;
            NBTTagCompound targetData;
            int pathIndex;
            if (targetIndex >= arguments.length) {
                throw new CommandException("commands.farmers_future_delight.execute.usage");
            }
            if ("storage".equals(arguments[targetIndex])) {
                if (targetIndex + 2 >= arguments.length) {
                    throw new CommandException("commands.farmers_future_delight.execute.usage");
                }
                ResourceLocation storageId = parseId(arguments[targetIndex + 1]);
                targetData = CommandStorageApi.get(server, storageId);
                pathIndex = targetIndex + 2;
            } else if ("entity".equals(arguments[targetIndex])) {
                if (targetIndex + 2 >= arguments.length) {
                    throw new CommandException("commands.farmers_future_delight.execute.usage");
                }
                Entity target = FFDCommandEntities.getEntity(server, sender,
                        arguments[targetIndex + 1]);
                targetData = target.writeToNBT(new NBTTagCompound());
                pathIndex = targetIndex + 2;
            } else if ("block".equals(arguments[targetIndex])) {
                BlockPos position = parseBlockPosition(sender, arguments, targetIndex + 1);
                if (!world.isBlockLoaded(position)) {
                    throw new CommandException("commands.farmers_future_delight.data.block_unloaded",
                            position.getX(), position.getY(), position.getZ());
                }
                TileEntity tile = world.getTileEntity(position);
                if (tile == null) {
                    throw new CommandException("commands.farmers_future_delight.data.no_block_entity",
                            position.getX(), position.getY(), position.getZ());
                }
                targetData = tile.writeToNBT(new NBTTagCompound());
                pathIndex = targetIndex + 4;
            } else {
                throw new CommandException("commands.farmers_future_delight.execute.usage");
            }
            if (pathIndex >= arguments.length) {
                throw new CommandException("commands.farmers_future_delight.execute.usage");
            }
            FFDNbtPath path;
            try {
                path = FFDNbtPath.parse(arguments[pathIndex]);
            } catch (IllegalArgumentException exception) {
                throw new CommandException("commands.farmers_future_delight.data.invalid_path",
                        arguments[pathIndex]);
            }
            result = !path.get(targetData).isEmpty();
            length = pathIndex - cursor + 1;
        } else {
            throw new CommandException("commands.farmers_future_delight.execute.usage");
        }
        return new ConditionResult(result, length);
    }

    private static boolean testPredicate(WorldServer world, ResourceLocation id,
                                         LootContext context, Random random,
                                         List<ResourceLocation> references)
            throws CommandException {
        if (references.contains(id)) {
            throw new CommandException("commands.farmers_future_delight.predicate.circular", id);
        }
        references.add(id);
        try {
            JsonElement root = readPredicate(world, id);
            return testNode(world, root, context, random, references);
        } finally {
            references.remove(references.size() - 1);
        }
    }

    private static boolean testNode(WorldServer world, JsonElement element,
                                    LootContext context, Random random,
                                    List<ResourceLocation> references)
            throws CommandException {
        if (element.isJsonArray()) {
            for (JsonElement value : element.getAsJsonArray()) {
                if (!testNode(world, value, context, random, references)) {
                    return false;
                }
            }
            return true;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return testPredicate(world, parseId(element.getAsString()), context, random, references);
        }
        if (!element.isJsonObject()) {
            throw new CommandException("commands.farmers_future_delight.predicate.invalid");
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("conditions") && !object.has("condition") && !object.has("type")) {
            JsonElement conditions = object.get("conditions");
            if (!conditions.isJsonArray()) {
                throw new CommandException("commands.farmers_future_delight.predicate.invalid");
            }
            return testNode(world, conditions, context, random, references);
        }
        String typeKey = object.has("type") ? "type" : "condition";
        if (!object.has(typeKey) || !object.get(typeKey).isJsonPrimitive()) {
            throw new CommandException("commands.farmers_future_delight.predicate.invalid");
        }
        String type = object.get(typeKey).getAsString();
        if ("minecraft:all_of".equals(type)) {
            return testTerms(world, object, "terms", true, context, random, references);
        }
        if ("minecraft:any_of".equals(type)) {
            return testTerms(world, object, "terms", false, context, random, references);
        }
        if ("minecraft:inverted".equals(type)) {
            JsonElement term = object.get("term");
            if (term == null) {
                throw new CommandException("commands.farmers_future_delight.predicate.invalid");
            }
            return !testNode(world, term, context, random, references);
        }
        if ("minecraft:reference".equals(type)) {
            JsonElement name = object.get("name");
            if (name == null || !name.isJsonPrimitive()) {
                throw new CommandException("commands.farmers_future_delight.predicate.invalid");
            }
            ResourceLocation referenceId = parseId(name.getAsString());
            return testPredicate(world, referenceId, context, random, references);
        }
        if ("minecraft:weather_check".equals(type) || "weather_check".equals(type)) {
            return testWeatherCheck(world, object);
        }
        if ("minecraft:time_check".equals(type) || "time_check".equals(type)) {
            return testTimeCheck(world, object);
        }
        try {
            JsonObject legacy = object;
            if ("type".equals(typeKey)) {
                legacy = new JsonObject();
                for (java.util.Map.Entry<String, JsonElement> property : object.entrySet()) {
                    if (!"type".equals(property.getKey())) {
                        legacy.add(property.getKey(), property.getValue());
                    }
                }
                legacy.addProperty("condition", type);
            }
            LootCondition condition = CONDITION_GSON.fromJson(legacy, LootCondition.class);
            return condition != null && condition.testCondition(random, context);
        } catch (RuntimeException exception) {
            throw new CommandException("commands.farmers_future_delight.predicate.invalid_type", type);
        }
    }

    private static boolean testWeatherCheck(WorldServer world, JsonObject object)
            throws CommandException {
        if (object.has("raining") && (!object.get("raining").isJsonPrimitive()
                || !object.getAsJsonPrimitive("raining").isBoolean())) {
            throw new CommandException("commands.farmers_future_delight.predicate.invalid");
        }
        if (object.has("thundering") && (!object.get("thundering").isJsonPrimitive()
                || !object.getAsJsonPrimitive("thundering").isBoolean())) {
            throw new CommandException("commands.farmers_future_delight.predicate.invalid");
        }
        if (object.has("raining")
                && object.get("raining").getAsBoolean() != world.isRaining()) {
            return false;
        }
        return !object.has("thundering")
                || object.get("thundering").getAsBoolean() == world.isThundering();
    }

    private static boolean testTimeCheck(WorldServer world, JsonObject object)
            throws CommandException {
        if (object.has("type") || object.has("clock")) {
            if (!object.has("clock") || !object.get("clock").isJsonPrimitive()
                    || !object.getAsJsonPrimitive("clock").isString()) {
                throw new CommandException("commands.farmers_future_delight.predicate.invalid");
            }
            ResourceLocation clock = parseId(object.get("clock").getAsString());
            if (!new ResourceLocation("minecraft", "overworld").equals(clock)) {
                throw new CommandException("commands.farmers_future_delight.predicate.clock",
                        clock);
            }
        }
        JsonElement value = object.get("value");
        if (value == null) {
            throw new CommandException("commands.farmers_future_delight.predicate.invalid");
        }
        int[] range = parseIntRange(value);
        long time = object.has("clock")
                ? world.getMinecraftServer().getWorld(0).getWorldTime() : world.getWorldTime();
        if (object.has("period")) {
            long period;
            try {
                JsonElement periodValue = object.get("period");
                if (!periodValue.isJsonPrimitive()
                        || !periodValue.getAsJsonPrimitive().isNumber()) {
                    throw new NumberFormatException();
                }
                period = Long.parseLong(periodValue.getAsString());
            } catch (RuntimeException exception) {
                throw new CommandException("commands.farmers_future_delight.predicate.invalid");
            }
            if (period <= 0) {
                throw new CommandException("commands.farmers_future_delight.predicate.invalid");
            }
            time %= period;
        }
        int testedTime = (int) time;
        return testedTime >= range[0] && testedTime <= range[1];
    }

    private static int[] parseIntRange(JsonElement element) throws CommandException {
        try {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                int exact = parseInteger(element);
                return new int[]{exact, exact};
            }
            if (!element.isJsonObject()) {
                throw new NumberFormatException();
            }
            JsonObject object = element.getAsJsonObject();
            int min = object.has("min") ? parseInteger(object.get("min")) : Integer.MIN_VALUE;
            int max = object.has("max") ? parseInteger(object.get("max")) : Integer.MAX_VALUE;
            if (min > max) {
                throw new NumberFormatException();
            }
            return new int[]{min, max};
        } catch (RuntimeException exception) {
            throw new CommandException("commands.farmers_future_delight.predicate.invalid");
        }
    }

    private static int parseInteger(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isNumber()) {
            throw new NumberFormatException();
        }
        return Integer.parseInt(element.getAsString());
    }

    private static boolean testTerms(WorldServer world, JsonObject object, String key,
                                     boolean all, LootContext context, Random random,
                                     List<ResourceLocation> references)
            throws CommandException {
        JsonElement terms = object.get(key);
        if (terms == null || !terms.isJsonArray()) {
            throw new CommandException("commands.farmers_future_delight.predicate.invalid");
        }
        JsonArray values = terms.getAsJsonArray();
        for (JsonElement value : values) {
            boolean result = testNode(world, value, context, random, references);
            if (all && !result) {
                return false;
            }
            if (!all && result) {
                return true;
            }
        }
        return all;
    }

    private static JsonElement readPredicate(WorldServer world, ResourceLocation id)
            throws CommandException {
        File root = new File(world.getSaveHandler().getWorldDirectory(), "data");
        try {
            String canonicalRoot = root.getCanonicalPath() + File.separator;
            for (String directory : new String[]{"predicate", "predicates"}) {
                File file = new File(root, id.getResourceDomain() + File.separator + directory
                        + File.separator + id.getResourcePath() + ".json");
                if (!file.getCanonicalPath().startsWith(canonicalRoot)) {
                    throw new CommandException("commands.farmers_future_delight.predicate.invalid_id", id);
                }
                if (file.isFile()) {
                    try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                        return new JsonParser().parse(reader);
                    }
                }
            }
            for (String directory : new String[]{"predicate", "predicates"}) {
                String resource = "/data/" + id.getResourceDomain() + "/" + directory + "/"
                        + id.getResourcePath() + ".json";
                try (InputStream stream = FFDCommandPredicates.class.getResourceAsStream(resource)) {
                    if (stream != null) {
                        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                            return new JsonParser().parse(reader);
                        }
                    }
                }
            }
            throw new CommandException("commands.farmers_future_delight.predicate.missing", id);
        } catch (CommandException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new CommandException("commands.farmers_future_delight.predicate.invalid", id);
        }
    }

    private static ResourceLocation parseId(String value) throws CommandException {
        try {
            return new ResourceLocation(value);
        } catch (RuntimeException exception) {
            throw new CommandException("commands.farmers_future_delight.predicate.invalid_id", value);
        }
    }

    private static int parseDimensionId(String value) throws CommandException {
        if ("minecraft:overworld".equals(value) || "overworld".equals(value)) return 0;
        if ("minecraft:the_nether".equals(value) || "the_nether".equals(value)) return -1;
        if ("minecraft:the_end".equals(value) || "the_end".equals(value)) return 1;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new CommandException("commands.farmers_future_delight.execute.dimension_invalid",
                    value);
        }
    }

    private static boolean parseAnchor(String value) throws CommandException {
        if ("feet".equals(value)) return false;
        if ("eyes".equals(value)) return true;
        throw new CommandException("commands.farmers_future_delight.execute.invalid_anchor",
                value);
    }

    private static Integer getScoreValue(Scoreboard scoreboard, MinecraftServer server,
                                         ICommandSender sender, String holderName,
                                         String objectiveName) throws CommandException {
        ScoreObjective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            throw new CommandException("commands.farmers_future_delight.execute.score_objective",
                    objectiveName);
        }
        String holder = holderName.startsWith("@")
                ? FFDCommandEntities.scoreHolder(FFDCommandEntities.getEntity(server, sender, holderName))
                : CommandBase.getEntityName(server, sender, holderName);
        if (!scoreboard.entityHasObjective(holder, objective)) {
            return null;
        }
        Score score = scoreboard.getOrCreateScore(holder, objective);
        return score.getScorePoints();
    }

    private static int[] parseScoreRange(String value) throws CommandException {
        int separator = value.indexOf("..");
        try {
            if (separator < 0) {
                int exact = Integer.parseInt(value);
                return new int[]{exact, exact};
            }
            if (value.indexOf("..", separator + 2) >= 0) {
                throw new NumberFormatException();
            }
            String minimum = value.substring(0, separator);
            String maximum = value.substring(separator + 2);
            if (minimum.isEmpty() && maximum.isEmpty()) {
                throw new NumberFormatException();
            }
            int lower = minimum.isEmpty() ? Integer.MIN_VALUE : Integer.parseInt(minimum);
            int upper = maximum.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(maximum);
            if (lower > upper) {
                throw new NumberFormatException();
            }
            return new int[]{lower, upper};
        } catch (NumberFormatException exception) {
            throw new CommandException("commands.farmers_future_delight.execute.score_range",
                    value);
        }
    }

    private static boolean compareScores(int target, int source, String operation)
            throws CommandException {
        switch (operation) {
            case "<":
                return target < source;
            case "<=":
                return target <= source;
            case "=":
                return target == source;
            case ">=":
                return target >= source;
            case ">":
                return target > source;
            default:
                throw new CommandException("commands.farmers_future_delight.execute.score_operation",
                        operation);
        }
    }

    private static boolean isScoreOperation(String operation) {
        return "<".equals(operation) || "<=".equals(operation) || "=".equals(operation)
                || ">=".equals(operation) || ">".equals(operation);
    }

    private static final class DataStore {
        private final boolean storeResult;
        private final int targetStart;
        private final int pathIndex;
        private final int typeIndex;
        private final int scaleIndex;
        private final int length;

        private DataStore(boolean storeResult, int targetStart, int pathIndex,
                          int typeIndex, int scaleIndex, int length) {
            this.storeResult = storeResult;
            this.targetStart = targetStart;
            this.pathIndex = pathIndex;
            this.typeIndex = typeIndex;
            this.scaleIndex = scaleIndex;
            this.length = length;
        }

        private ResolvedDataStore resolve(MinecraftServer server, ContextSender sender,
                                          String[] arguments) throws CommandException {
            return new ResolvedDataStore(CommandData.createExecuteStoreTarget(server, sender,
                    arguments, targetStart, pathIndex, typeIndex, scaleIndex), storeResult);
        }
    }

    private static final class ResolvedDataStore {
        private final CommandData.ExecuteStoreTarget target;
        private final boolean storeResult;

        private ResolvedDataStore(CommandData.ExecuteStoreTarget target, boolean storeResult) {
            this.target = target;
            this.storeResult = storeResult;
        }

        private void apply(int commandResult, boolean succeeded) {
            try {
                target.apply(storeResult ? commandResult : succeeded ? 1 : 0, true);
            } catch (CommandException ignored) {
            }
        }
    }

    private static final class ScoreStore {
        private final boolean storeResult;
        private final String targets;
        private final String objectiveName;
        private final int length;

        private ScoreStore(boolean storeResult, String targets, String objectiveName,
                          int length) {
            this.storeResult = storeResult;
            this.targets = targets;
            this.objectiveName = objectiveName;
            this.length = length;
        }

        private ResolvedScoreStore resolve(MinecraftServer server, ContextSender sender)
                throws CommandException {
            Scoreboard scoreboard = sender.getEntityWorld().getScoreboard();
            ScoreObjective objective = scoreboard.getObjective(objectiveName);
            if (objective == null) {
                throw new CommandException("commands.farmers_future_delight.execute.score_objective",
                        objectiveName);
            }
            return new ResolvedScoreStore(scoreboard, objective,
                    resolveTargets(server, sender), storeResult);
        }

        private List<String> resolveTargets(MinecraftServer server, ContextSender sender)
                throws CommandException {
            List<String> names = new ArrayList<>();
            if ("*".equals(targets)) {
                java.util.LinkedHashSet<String> tracked = new java.util.LinkedHashSet<>();
                for (Score score : sender.getEntityWorld().getScoreboard().getScores()) {
                    tracked.add(score.getPlayerName());
                }
                names.addAll(tracked);
            } else if (targets.startsWith("@")) {
                for (Entity entity : FFDCommandEntities.getEntities(server, sender, targets)) {
                    names.add(FFDCommandEntities.scoreHolder(entity));
                }
            } else {
                names.add(targets);
            }
            return names;
        }
    }

    private static final class ResolvedScoreStore {
        private final Scoreboard scoreboard;
        private final ScoreObjective objective;
        private final List<String> targets;
        private final boolean storeResult;

        private ResolvedScoreStore(Scoreboard scoreboard, ScoreObjective objective,
                                   List<String> targets, boolean storeResult) {
            this.scoreboard = scoreboard;
            this.objective = objective;
            this.targets = targets;
            this.storeResult = storeResult;
        }

        private void apply(int commandResult, boolean succeeded) {
            int value = storeResult ? commandResult : succeeded ? 1 : 0;
            for (String target : targets) {
                scoreboard.getOrCreateScore(target, objective).setScorePoints(value);
            }
        }
    }

    private static boolean testBlock(WorldServer world, BlockPos position, String input)
            throws CommandException {
        if (!world.isBlockLoaded(position)) {
            throw new CommandException("commands.farmers_future_delight.data.block_unloaded",
                    position.getX(), position.getY(), position.getZ());
        }
        int propertiesStart = input.indexOf('[');
        int dataStart = input.indexOf('{');
        if (dataStart >= 0 && (propertiesStart < 0 || dataStart < propertiesStart)) {
            propertiesStart = -1;
        }
        int idEnd = input.length();
        if (propertiesStart >= 0) idEnd = Math.min(idEnd, propertiesStart);
        if (dataStart >= 0) idEnd = Math.min(idEnd, dataStart);
        ResourceLocation blockId = parseBlockId(input.substring(0, idEnd));
        if (!Block.REGISTRY.containsKey(blockId)) {
            throw new CommandException("commands.farmers_future_delight.execute.block_unknown",
                    blockId);
        }
        Block block = Block.REGISTRY.getObject(blockId);
        int propertiesEnd = -1;
        if (propertiesStart >= 0) {
            propertiesEnd = input.indexOf(']', propertiesStart + 1);
            if (propertiesEnd < 0 || propertiesStart != idEnd
                    || (dataStart >= 0 && dataStart < propertiesEnd)) {
                throw new CommandException("commands.farmers_future_delight.execute.block_invalid",
                        input);
            }
        }
        int expectedDataStart = propertiesEnd >= 0 ? propertiesEnd + 1 : idEnd;
        if (dataStart >= 0 && dataStart != expectedDataStart) {
            throw new CommandException("commands.farmers_future_delight.execute.block_invalid",
                    input);
        }
        if (dataStart < 0 && expectedDataStart != input.length()) {
            throw new CommandException("commands.farmers_future_delight.execute.block_invalid",
                    input);
        }
        if (propertiesStart < 0 && idEnd != input.length() && dataStart < 0) {
            throw new CommandException("commands.farmers_future_delight.execute.block_invalid",
                    input);
        }
        IBlockState state = world.getBlockState(position);
        if (state.getBlock() != block) return false;
        if (propertiesStart >= 0) {
            String properties = input.substring(propertiesStart + 1, propertiesEnd);
            try {
                if (!CommandBase.convertArgToBlockStatePredicate(block, properties).apply(state)) {
                    return false;
                }
            } catch (Exception exception) {
                throw new CommandException("commands.farmers_future_delight.execute.block_invalid",
                        input);
            }
        }
        if (dataStart < 0) return true;
        if (!input.endsWith("}")) {
            throw new CommandException("commands.farmers_future_delight.execute.block_invalid",
                    input);
        }
        NBTTagCompound expected;
        try {
            expected = JsonToNBT.getTagFromJson(input.substring(dataStart));
        } catch (Exception exception) {
            throw new CommandException("commands.farmers_future_delight.execute.block_invalid",
                    input);
        }
        net.minecraft.tileentity.TileEntity tile = world.getTileEntity(position);
        if (tile == null) return false;
        NBTTagCompound actual = tile.writeToNBT(new NBTTagCompound());
        return FFDNbtPath.matchesCompound(expected, actual);
    }

    private static boolean testBlocks(WorldServer world, ContextSender sender,
                                      String[] arguments, int start, String mode)
            throws CommandException {
        if (!"all".equals(mode) && !"masked".equals(mode)) {
            throw new CommandException("commands.farmers_future_delight.execute.block_invalid",
                    mode);
        }
        BlockPos first = parseBlockPosition(sender, arguments, start);
        BlockPos second = parseBlockPosition(sender, arguments, start + 3);
        BlockPos destination = parseBlockPosition(sender, arguments, start + 6);
        int minX = Math.min(first.getX(), second.getX());
        int minY = Math.min(first.getY(), second.getY());
        int minZ = Math.min(first.getZ(), second.getZ());
        int maxX = Math.max(first.getX(), second.getX());
        int maxY = Math.max(first.getY(), second.getY());
        int maxZ = Math.max(first.getZ(), second.getZ());
        long xSpan = (long) maxX - minX + 1L;
        long ySpan = (long) maxY - minY + 1L;
        long zSpan = (long) maxZ - minZ + 1L;
        if (xSpan > 32768L || ySpan > 32768L || zSpan > 32768L
                || xSpan * ySpan * zSpan > 32768L) {
            throw new CommandException("commands.farmers_future_delight.execute.blocks_too_large",
                    32768);
        }
        long destinationMaxX = (long) destination.getX() + xSpan - 1L;
        long destinationMaxY = (long) destination.getY() + ySpan - 1L;
        long destinationMaxZ = (long) destination.getZ() + zSpan - 1L;
        if (destinationMaxX > 30000000L || destinationMaxY > 30000000L
                || destinationMaxZ > 30000000L) {
            throw new CommandException("commands.farmers_future_delight.execute.block_invalid",
                    destination);
        }
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    BlockPos sourcePos = new BlockPos(x, y, z);
                    if (!world.isBlockLoaded(sourcePos)) {
                        throw new CommandException(
                                "commands.farmers_future_delight.data.block_unloaded",
                                x, y, z);
                    }
                    IBlockState sourceState = world.getBlockState(sourcePos);
                    if ("masked".equals(mode) && sourceState.getBlock() == Blocks.AIR) {
                        continue;
                    }
                    long targetX = (long) destination.getX() + x - minX;
                    long targetY = (long) destination.getY() + y - minY;
                    long targetZ = (long) destination.getZ() + z - minZ;
                    BlockPos targetPos = new BlockPos((int) targetX, (int) targetY,
                            (int) targetZ);
                    if (!world.isBlockLoaded(targetPos)) {
                        throw new CommandException(
                                "commands.farmers_future_delight.data.block_unloaded",
                                targetX, targetY, targetZ);
                    }
                    if (!sourceState.equals(world.getBlockState(targetPos))) {
                        return false;
                    }
                    TileEntity sourceTile = world.getTileEntity(sourcePos);
                    if (sourceTile != null) {
                        TileEntity targetTile = world.getTileEntity(targetPos);
                        if (targetTile == null || sourceTile.getClass() != targetTile.getClass()) {
                            return false;
                        }
                        NBTTagCompound sourceData = sourceTile.writeToNBT(new NBTTagCompound());
                        NBTTagCompound targetData = targetTile.writeToNBT(new NBTTagCompound());
                        for (String key : new String[]{"id", "x", "y", "z"}) {
                            sourceData.removeTag(key);
                            targetData.removeTag(key);
                        }
                        if (!sourceData.equals(targetData)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static ResourceLocation parseBlockId(String value) throws CommandException {
        try {
            return new ResourceLocation(value);
        } catch (RuntimeException exception) {
            throw new CommandException("commands.farmers_future_delight.execute.block_invalid",
                    value);
        }
    }
}
