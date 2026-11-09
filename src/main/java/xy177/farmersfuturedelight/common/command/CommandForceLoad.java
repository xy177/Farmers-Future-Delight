package xy177.farmersfuturedelight.common.command;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public final class CommandForceLoad extends FFDServerCommand {
    @Override
    public String getName() {
        return "forceload";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.farmers_future_delight.forceload.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    protected void executeOnServerThread(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        WorldServer world = sender.getEntityWorld() instanceof WorldServer
                ? (WorldServer) sender.getEntityWorld() : server.worlds[0];
        if (args.length == 1 && "query".equals(args[0])) {
            notifyCommandListener(sender, this,
                    "commands.farmers_future_delight.forceload.query",
                    FFDForcedChunks.getChunks(world).size(), world.provider.getDimension());
            return;
        }
        if (args.length == 3 && "query".equals(args[0])) {
            BlockPos position = parseColumnPos(sender, args, 1);
            notifyCommandListener(sender, this,
                    "commands.farmers_future_delight.forceload.query_position",
                    position.getX(), position.getZ(),
                    FFDForcedChunks.isForced(world, position),
                    world.provider.getDimension());
            return;
        }
        if (args.length == 2 && "remove".equals(args[0]) && "all".equals(args[1])) {
            Set<Long> chunks = FFDForcedChunks.getChunks(world);
            int removed = FFDForcedChunks.remove(world, chunks);
            notifyCommandListener(sender, this,
                    "commands.farmers_future_delight.forceload.removed_all",
                    removed, world.provider.getDimension());
            return;
        }
        if (args.length != 3 && args.length != 5) {
            throw new WrongUsageException(getUsage(sender));
        }
        String operation = args[0];
        if (!"add".equals(operation) && !"remove".equals(operation)) {
            throw new WrongUsageException(getUsage(sender));
        }
        BlockPos first = parseColumnPos(sender, args, 1);
        BlockPos second = args.length == 5 ? parseColumnPos(sender, args, 3) : first;
        Set<Long> requested = getChunkRange(first, second);
        if ("add".equals(operation)) {
            int changed;
            try {
                changed = FFDForcedChunks.add(world, requested);
            } catch (FFDForcedChunks.CapacityException exception) {
                throw new CommandException("commands.farmers_future_delight.forceload." +
                        exception.getReason());
            }
            if (changed == 0) {
                throw new CommandException("commands.farmers_future_delight.forceload.unchanged");
            }
            notifyCommandListener(sender, this,
                    "commands.farmers_future_delight.forceload.added", changed);
        } else {
            int changed = FFDForcedChunks.remove(world, requested);
            if (changed == 0) {
                throw new CommandException("commands.farmers_future_delight.forceload.unchanged");
            }
            notifyCommandListener(sender, this,
                    "commands.farmers_future_delight.forceload.removed", changed);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                           String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "add", "query", "remove");
        }
        if (args.length == 2 && "remove".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, "all");
        }
        return java.util.Collections.emptyList();
    }

    private static Set<Long> getChunkRange(BlockPos first, BlockPos second)
            throws CommandException {
        int minX = Math.min(first.getX() >> 4, second.getX() >> 4);
        int maxX = Math.max(first.getX() >> 4, second.getX() >> 4);
        int minZ = Math.min(first.getZ() >> 4, second.getZ() >> 4);
        int maxZ = Math.max(first.getZ() >> 4, second.getZ() >> 4);
        long count = ((long) maxX - minX + 1L) * ((long) maxZ - minZ + 1L);
        if (count > 256L) {
            throw new CommandException("commands.farmers_future_delight.forceload.max_area");
        }
        Set<Long> chunks = new LinkedHashSet<>();
        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                chunks.add(((long) chunkX << 32) | (chunkZ & 0xffffffffL));
            }
        }
        return chunks;
    }

    private static BlockPos parseColumnPos(ICommandSender sender, String[] args, int start)
            throws CommandException {
        if (start + 1 >= args.length) {
            throw new WrongUsageException("commands.farmers_future_delight.forceload.usage");
        }
        return parseBlockPos(sender, new String[]{args[start], "0", args[start + 1]}, 0, false);
    }
}
