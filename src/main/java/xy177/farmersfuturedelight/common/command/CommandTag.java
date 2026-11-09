package xy177.farmersfuturedelight.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public final class CommandTag extends FFDServerCommand {
    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.farmers_future_delight.tag.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    protected void executeOnServerThread(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length < 2 || args.length > 3) {
            throw new WrongUsageException(getUsage(sender));
        }
        List<Entity> targets = FFDCommandEntities.getEntities(server, sender, args[0]);
        String operation = args[1];
        if ("list".equals(operation)) {
            if (args.length != 2) {
                throw new WrongUsageException(getUsage(sender));
            }
            listTags(sender, targets);
            return;
        }
        if (args.length != 3 || !("add".equals(operation) || "remove".equals(operation))) {
            throw new WrongUsageException(getUsage(sender));
        }
        String tag = args[2];
        int changed = 0;
        for (Entity target : targets) {
            boolean success = "add".equals(operation)
                    ? target.addTag(tag) : target.removeTag(tag);
            if (success) {
                changed++;
            }
        }
        if (changed == 0) {
            throw new CommandException("commands.farmers_future_delight.tag.unchanged", tag);
        }
        sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, changed);
        notifyCommandListener(sender, this,
                "commands.farmers_future_delight.tag." + operation + ".success",
                tag, changed);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                           String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, "add", "list", "remove");
        }
        if (args.length == 3 && ("add".equals(args[1]) || "remove".equals(args[1]))) {
            Set<String> tags = new TreeSet<>();
            try {
                for (Entity target : FFDCommandEntities.getEntities(server, sender, args[0])) {
                    tags.addAll(target.getTags());
                }
            } catch (CommandException ignored) {
                return Collections.emptyList();
            }
            return getListOfStringsMatchingLastWord(args, new ArrayList<>(tags));
        }
        return Collections.emptyList();
    }

    private void listTags(ICommandSender sender, List<Entity> targets) {
        Set<String> tags = new TreeSet<>();
        for (Entity target : targets) {
            tags.addAll(target.getTags());
        }
        if (tags.isEmpty()) {
            notifyCommandListener(sender, this,
                    "commands.farmers_future_delight.tag.list_empty", targets.size());
        } else {
            notifyCommandListener(sender, this,
                    "commands.farmers_future_delight.tag.list",
                    targets.size(), tags.size(), String.join(", ", tags));
        }
        sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, tags.size());
    }
}
