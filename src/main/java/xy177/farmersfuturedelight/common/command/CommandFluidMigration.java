package xy177.farmersfuturedelight.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import xy177.farmersfuturedelight.common.fluid.FFDFluidMigration;

public final class CommandFluidMigration extends CommandBase {
    @Override public String getName() { return "ffd_fluidmigration"; }
    @Override public String getUsage(ICommandSender sender) {
        return "/ffd_fluidmigration status | <convert|discard> <token>";
    }
    @Override public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return FFDFluidMigration.canDecide(server, sender);
    }
    @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length == 1 && "status".equals(args[0])) {
            FFDFluidMigration.status(server, sender);
        } else if (args.length == 2 && ("convert".equals(args[0]) || "discard".equals(args[0]))) {
            sender.sendMessage(new TextComponentTranslation(
                    FFDFluidMigration.decide(server, sender, args[1], "convert".equals(args[0]))));
        } else throw new WrongUsageException(getUsage(sender));
    }
}
