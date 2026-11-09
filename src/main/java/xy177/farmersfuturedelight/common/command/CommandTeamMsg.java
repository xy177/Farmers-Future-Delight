package xy177.farmersfuturedelight.common.command;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public final class CommandTeamMsg extends FFDServerCommand {
    @Override
    public String getName() {
        return "teammsg";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("tm");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.farmers_future_delight.teammsg.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    protected void executeOnServerThread(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        Entity source = sender.getCommandSenderEntity();
        if (source == null) {
            throw new EntityNotFoundException("commands.generic.entity.notFound");
        }
        Team team = source.getTeam();
        if (team == null) {
            throw new CommandException("commands.farmers_future_delight.teammsg.no_team");
        }
        ITextComponent message = getChatComponentFromNthArg(sender, args, 0);
        ITextComponent teamName = new TextComponentString("[" + (team instanceof ScorePlayerTeam
                ? ((ScorePlayerTeam) team).getDisplayName() : team.getName()) + "]");
        teamName.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/teammsg ")).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new TextComponentTranslation("commands.farmers_future_delight.teammsg.hover")));
        if (team instanceof ScorePlayerTeam) {
            teamName.getStyle().setColor(((ScorePlayerTeam) team).getColor());
        }
        int receivers = 0;
        for (EntityPlayerMP receiver : server.getPlayerList().getPlayers()) {
            if (receiver == source || receiver.getTeam() == team) {
                String translation = receiver == source
                        ? "commands.farmers_future_delight.teammsg.sent"
                        : "commands.farmers_future_delight.teammsg.text";
                receiver.sendMessage(new TextComponentTranslation(translation,
                        teamName.createCopy(), source.getDisplayName(), message.createCopy()));
                receivers++;
            }
        }
        FFDCommandPredicates.recordResult(sender, receivers);
    }
}
