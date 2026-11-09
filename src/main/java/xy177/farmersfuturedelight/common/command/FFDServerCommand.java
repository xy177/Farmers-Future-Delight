package xy177.farmersfuturedelight.common.command;

import java.util.concurrent.ExecutionException;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public abstract class FFDServerCommand extends CommandBase {
    @Override
    public final void execute(MinecraftServer server, ICommandSender sender, String[] arguments)
            throws CommandException {
        if (server.isCallingFromMinecraftThread()) {
            executeOnServerThread(server, sender, arguments);
            return;
        }
        try {
            server.callFromMainThread(() -> {
                executeOnServerThread(server, sender, arguments);
                return null;
            }).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CommandException("commands.generic.exception");
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof CommandException) throw (CommandException) cause;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new CommandException("commands.generic.exception");
        }
    }

    protected abstract void executeOnServerThread(MinecraftServer server, ICommandSender sender,
                                                   String[] arguments) throws CommandException;
}
