package xy177.farmersfuturedelight.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import xy177.farmersfuturedelight.common.scheduler.FFDFunctionScheduler;

public final class CommandSchedule extends FFDServerCommand {
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "^([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?)([dst]?)$");

    @Override
    public String getName() {
        return "schedule";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.farmers_future_delight.schedule.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    protected void executeOnServerThread(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length == 2 && "clear".equals(args[0])) {
            String target = parseClearTarget(args[1]);
            int removed = FFDFunctionScheduler.clear(server, target);
            if (removed == 0) {
                throw new CommandException("commands.farmers_future_delight.schedule.cleared_none", target);
            }
            notifyCommandListener(sender, this,
                    "commands.farmers_future_delight.schedule.cleared", target, removed);
            FFDCommandPredicates.recordResult(sender, removed);
            return;
        }
        if ((args.length != 3 && args.length != 4) || !"function".equals(args[0])) {
            throw new WrongUsageException(getUsage(sender));
        }
        String target = parseTarget(server, args[1]);
        int delay = parseDelay(args[2]);
        String mode = args.length == 4 ? args[3] : "replace";
        if (!"append".equals(mode) && !"replace".equals(mode)) {
            throw new WrongUsageException(getUsage(sender));
        }
        long dueTime;
        try {
            dueTime = FFDFunctionScheduler.schedule(server, target, delay, "replace".equals(mode));
        } catch (ArithmeticException exception) {
            throw new CommandException("commands.farmers_future_delight.schedule.invalid_time", args[2]);
        }
        notifyCommandListener(sender, this,
                "commands.farmers_future_delight.schedule.scheduled", target, delay);
        FFDCommandPredicates.recordResult(sender,
                (int) Math.floorMod(dueTime, (long) Integer.MAX_VALUE));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                           String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "function", "clear");
        }
        if (args.length == 2 && "clear".equals(args[0])) {
            List<String> targets = new ArrayList<>(FFDFunctionScheduler.getTargets(server));
            Collections.sort(targets);
            return getListOfStringsMatchingLastWord(args, targets);
        }
        if (args.length == 2 && "function".equals(args[0])) {
            List<String> functions = new ArrayList<>();
            for (Map.Entry<ResourceLocation, ?> entry : server.getFunctionManager()
                    .getFunctions().entrySet()) {
                functions.add(entry.getKey().toString());
            }
            Collections.sort(functions);
            return getListOfStringsMatchingLastWord(args, functions);
        }
        if (args.length == 4 && "function".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, "append", "replace");
        }
        return Collections.emptyList();
    }

    private static String parseTarget(MinecraftServer server, String value) throws CommandException {
        boolean tag = value.startsWith("#");
        String idValue = tag ? value.substring(1) : value;
        ResourceLocation id;
        try {
            id = new ResourceLocation(idValue);
        } catch (RuntimeException exception) {
            throw new CommandException("commands.farmers_future_delight.schedule.invalid_target", value);
        }
        boolean exists = tag
                ? FFDFunctionScheduler.hasFunctionTag(server, id)
                : server.getFunctionManager().getFunction(id) != null;
        if (!exists) {
            throw new CommandException("commands.farmers_future_delight.schedule.invalid_target", value);
        }
        return (tag ? "#" : "") + id;
    }

    private static String parseClearTarget(String value) throws CommandException {
        boolean tag = value.startsWith("#");
        String idValue = tag ? value.substring(1) : value;
        try {
            ResourceLocation id = new ResourceLocation(idValue);
            return (tag ? "#" : "") + id;
        } catch (RuntimeException exception) {
            throw new CommandException("commands.farmers_future_delight.schedule.invalid_target", value);
        }
    }

    private static int parseDelay(String value) throws CommandException {
        Matcher matcher = TIME_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new CommandException("commands.farmers_future_delight.schedule.invalid_time", value);
        }
        try {
            float amount = Float.parseFloat(matcher.group(1));
            float multiplier;
            String unit = matcher.group(2);
            if ("d".equals(unit)) {
                multiplier = 24000.0F;
            } else if ("s".equals(unit)) {
                multiplier = 20.0F;
            } else {
                multiplier = 1.0F;
            }
            if (Float.isNaN(amount) || Float.isInfinite(amount)) {
                throw new NumberFormatException();
            }
            int ticks = Math.round(amount * multiplier);
            if (ticks < 0) {
                throw new NumberFormatException();
            }
            if (ticks == 0) {
                throw new CommandException("commands.farmers_future_delight.schedule.same_tick");
            }
            return ticks;
        } catch (NumberFormatException exception) {
            throw new CommandException("commands.farmers_future_delight.schedule.invalid_time", value);
        }
    }
}
