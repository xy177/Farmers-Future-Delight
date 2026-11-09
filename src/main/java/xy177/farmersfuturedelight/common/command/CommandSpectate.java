package xy177.farmersfuturedelight.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.ITeleporter;

public final class CommandSpectate extends FFDServerCommand {
    @Override
    public String getName() {
        return "spectate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.farmers_future_delight.spectate.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    protected void executeOnServerThread(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length > 2) {
            throw new WrongUsageException(getUsage(sender));
        }
        Entity target = args.length == 0 ? null : FFDCommandEntities.getEntity(server, sender, args[0]);
        EntityPlayerMP player;
        if (args.length >= 2) {
            player = FFDCommandEntities.getPlayer(server, sender, args[1]);
        } else if (sender.getCommandSenderEntity() instanceof EntityPlayerMP) {
            player = (EntityPlayerMP) sender.getCommandSenderEntity();
        } else {
            throw new PlayerNotFoundException("commands.generic.player.unspecified");
        }
        if (target == player) {
            throw new CommandException("commands.farmers_future_delight.spectate.self");
        }
        if (!player.isSpectator()) {
            throw new CommandException("commands.farmers_future_delight.spectate.not_spectator",
                    player.getName());
        }
        if (target != null && target.isDead) {
            throw new CommandException("commands.farmers_future_delight.spectate.invalid_target",
                    target.getName());
        }
        if (target != null && target.world != player.world) {
            if (!ForgeHooks.onTravelToDimension(player, target.dimension)) {
                throw new CommandException("commands.farmers_future_delight.spectate.invalid_target",
                        target.getName());
            }
            player.dismountRidingEntity();
            server.getPlayerList().transferPlayerToDimension(player, target.dimension,
                    (ITeleporter) (world, entity, yaw) -> entity.setLocationAndAngles(
                            target.posX, target.posY, target.posZ,
                            player.rotationYaw, player.rotationPitch));
        }
        player.setSpectatingEntity(target);
        FFDCommandPredicates.recordResult(sender, 1);
        if (target == null) {
            sender.sendMessage(new TextComponentTranslation(
                    "commands.farmers_future_delight.spectate.stopped"));
        } else {
            sender.sendMessage(new TextComponentTranslation(
                    "commands.farmers_future_delight.spectate.started",
                    target.getDisplayName()));
        }
    }
}
