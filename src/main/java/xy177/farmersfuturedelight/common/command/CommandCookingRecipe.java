package xy177.farmersfuturedelight.common.command;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.registry.FFDCookingRecipeBook;

public final class CommandCookingRecipe extends FFDServerCommand {
    private final ICommand original;

    public CommandCookingRecipe(ICommand original) {
        this.original = original;
    }

    @Override
    public String getName() {
        return original.getName();
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return original.getUsage(sender);
    }

    @Override
    public List<String> getAliases() {
        return original.getAliases();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return original instanceof CommandBase ? ((CommandBase) original).getRequiredPermissionLevel() : 2;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return original.checkPermission(server, sender);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return original.isUsernameIndex(args, index);
    }

    @Override
    protected void executeOnServerThread(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length < 3) throw new WrongUsageException(getUsage(sender));
        boolean granted = "give".equalsIgnoreCase(args[0]);
        if (!granted && !"take".equalsIgnoreCase(args[0])) {
            original.execute(server, sender, args);
            return;
        }
        if ("*".equals(args[2])) {
            String[] targeted = args.clone();
            for (EntityPlayerMP player : getPlayers(server, sender, args[1])) {
                targeted[1] = player.getName();
                original.execute(server, sender, targeted);
                if (granted) FFDCookingRecipeBook.grantAll(player);
                else FFDCookingRecipeBook.revokeAll(player);
            }
            return;
        }
        ResourceLocation id = new ResourceLocation(args[2]);
        if (CraftingManager.getRecipe(id) != null || !isCookingRecipe(id)) {
            original.execute(server, sender, args);
            return;
        }
        for (EntityPlayerMP player : getPlayers(server, sender, args[1])) {
            boolean changed = false;
            for (WorkstationRecipes.Type type : FFDCookingRecipeBook.COOKING_TYPES) {
                if (granted) changed |= FFDCookingRecipeBook.grant(player, type, id);
                else changed |= FFDCookingRecipeBook.revoke(player, type, id);
            }
            if (!changed) {
                throw new CommandException(granted ? "commands.recipe.alreadyHave" : "commands.recipe.dontHave",
                        player.getName(), id.toString());
            }
            notifyCommandListener(sender, this, granted ? "commands.recipe.give.success.one"
                    : "commands.recipe.take.success.one", granted ? player.getName() : id.toString(),
                    granted ? id.toString() : player.getName());
        }
    }

    private static boolean isCookingRecipe(ResourceLocation id) {
        for (WorkstationRecipes.Type type : FFDCookingRecipeBook.COOKING_TYPES) {
            if (WorkstationRecipes.get(type, id) != null) return true;
        }
        return false;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                           String[] args, @Nullable BlockPos targetPos) {
        if (args.length != 3) return original.getTabCompletions(server, sender, args, targetPos);
        Set<String> completions = new LinkedHashSet<>(original.getTabCompletions(server, sender, args, targetPos));
        completions.addAll(getListOfStringsMatchingLastWord(args, "*"));
        Set<ResourceLocation> recipes = new LinkedHashSet<>();
        for (WorkstationRecipes.Type type : FFDCookingRecipeBook.COOKING_TYPES) {
            for (WorkstationRecipes.Recipe recipe : WorkstationRecipes.getRecipes(type)) {
                recipes.add(recipe.getId());
            }
        }
        completions.addAll(getListOfStringsMatchingLastWord(args, recipes));
        return new ArrayList<>(completions);
    }
}
