package xy177.farmersfuturedelight.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public final class FFDCommandEntities {
    private static final Pattern INTEGER = Pattern.compile("[0-9]+");
    private static final Pattern DECIMAL = Pattern.compile("(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)");
    private static final Pattern COORDINATE = Pattern.compile(
            "[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)");

    private FFDCommandEntities() {
    }

    public static List<Entity> getEntities(MinecraftServer server, ICommandSender sender,
                                           String selector) throws CommandException {
        Selection selection = parse(sender, selector);
        List<Entity> entities = selection == null ? CommandBase.getEntityList(server, sender, selector)
                : select(sender, selector, selection, Entity.class);
        if (entities.isEmpty()) {
            throw new EntityNotFoundException(selector);
        }
        return entities;
    }

    public static Entity getEntity(MinecraftServer server, ICommandSender sender,
                                   String selector) throws CommandException {
        List<Entity> entities = getEntities(server, sender, selector);
        if (entities.size() != 1) {
            throw new EntityNotFoundException(selector);
        }
        return entities.get(0);
    }

    public static List<EntityPlayerMP> getPlayers(MinecraftServer server, ICommandSender sender,
                                                  String selector) throws CommandException {
        Selection selection = parse(sender, selector);
        if (selection == null) {
            return CommandBase.getPlayers(server, sender, selector);
        }
        List<EntityPlayerMP> players = select(sender, selector, selection, EntityPlayerMP.class);
        if (players.isEmpty()) {
            throw new PlayerNotFoundException("commands.generic.player.notFound", selector);
        }
        return players;
    }

    public static EntityPlayerMP getPlayer(MinecraftServer server, ICommandSender sender,
                                           String selector) throws CommandException {
        List<EntityPlayerMP> players = getPlayers(server, sender, selector);
        if (players.size() != 1) {
            throw new PlayerNotFoundException("commands.generic.player.notFound", selector);
        }
        return players.get(0);
    }

    public static String scoreHolder(Entity entity) {
        return entity instanceof EntityPlayer ? entity.getName() : entity.getCachedUniqueIdString();
    }

    private static Selection parse(ICommandSender sender, String selector) throws CommandException {
        if (selector.length() < 2 || selector.charAt(0) != '@'
                || "pares".indexOf(selector.charAt(1)) < 0) {
            return null;
        }
        if (selector.length() == 2) {
            return null;
        }
        if (selector.charAt(2) != '[' || selector.charAt(selector.length() - 1) != ']') {
            throw invalid(selector);
        }
        String kind = selector.substring(1, 2);
        List<String> selectorArguments = splitSelectorArguments(
                selector.substring(3, selector.length() - 1), selector);
        boolean usesModernArguments = false;
        for (String argument : selectorArguments) {
            int separator = argument.indexOf('=');
            if (separator > 0) {
                String name = argument.substring(0, separator).trim();
                if ("limit".equals(name) || "sort".equals(name)
                        || "distance".equals(name) || "type".equals(name)
                        || "nbt".equals(name)) {
                    usesModernArguments = true;
                    break;
                }
            }
        }
        if (!usesModernArguments) {
            return null;
        }
        int limit = "p".equals(kind) || "r".equals(kind) || "s".equals(kind)
                ? 1 : Integer.MAX_VALUE;
        String sort = "p".equals(kind) ? "nearest" : "r".equals(kind) ? "random" : "arbitrary";
        Vec3d position = sender.getPositionVector();
        double originX = position.x;
        double originY = position.y;
        double originZ = position.z;
        boolean worldLimited = false;
        DistanceRange distance = null;
        String entityType = null;
        List<NbtFilter> nbtFilters = new ArrayList<>();
        List<String> nativeArguments = new ArrayList<>();
        Set<String> singleValueArguments = new HashSet<>();
        for (String argument : selectorArguments) {
            int separator = argument.indexOf('=');
            if (separator <= 0) {
                throw invalid(selector);
            }
            String name = argument.substring(0, separator).trim();
            String value = argument.substring(separator + 1).trim();
            if ("limit".equals(name) || "sort".equals(name) || "distance".equals(name)) {
                if (!singleValueArguments.add(name)) {
                    throw invalid(selector);
                }
                if ("limit".equals(name)) {
                    limit = parseLimit(value, selector);
                } else if ("sort".equals(name)) {
                    if (!"nearest".equals(value) && !"furthest".equals(value)
                            && !"random".equals(value) && !"arbitrary".equals(value)) {
                        throw invalid(selector);
                    }
                    sort = value;
                } else {
                    distance = parseDistance(value, selector);
                    worldLimited = true;
                }
            } else if ("c".equals(name) || "r".equals(name) || "rm".equals(name)) {
                throw invalid(selector);
            } else if ("x".equals(name) || "y".equals(name) || "z".equals(name)) {
                if (!singleValueArguments.add(name)) {
                    throw invalid(selector);
                }
                double coordinate = parseNumber(value, selector, false);
                if ("x".equals(name)) {
                    originX = coordinate;
                } else if ("y".equals(name)) {
                    originY = coordinate;
                } else {
                    originZ = coordinate;
                }
                worldLimited = true;
            } else {
                if ("dx".equals(name) || "dy".equals(name) || "dz".equals(name)) {
                    worldLimited = true;
                }
                if ("type".equals(name)) {
                    if (!singleValueArguments.add(name) || value.isEmpty()) {
                        throw invalid(selector);
                    }
                    entityType = value;
                    continue;
                }
                if ("nbt".equals(name)) {
                    boolean inverted = value.startsWith("!");
                    String nbt = inverted ? value.substring(1) : value;
                    NBTBase expected;
                    try {
                        expected = JsonToNBT.getTagFromJson(nbt);
                    } catch (Exception exception) {
                        throw invalid(selector);
                    }
                    if (!(expected instanceof NBTTagCompound)) {
                        throw invalid(selector);
                    }
                    nbtFilters.add(new NbtFilter((NBTTagCompound) expected, inverted));
                    continue;
                }
                nativeArguments.add(name + "=" + value);
            }
        }
        nativeArguments.add("c=0");
        String nativeKind = "p".equals(kind) || "r".equals(kind) ? "a" : kind;
        return new Selection("@" + nativeKind + "[" + String.join(",", nativeArguments) + "]",
                new Vec3d(originX, originY, originZ), limit, sort, distance, worldLimited,
                entityType, nbtFilters);
    }

    private static List<String> splitSelectorArguments(String input, String selector)
            throws CommandException {
        List<String> arguments = new ArrayList<>();
        int start = 0;
        int squareDepth = 0;
        int curlyDepth = 0;
        char quote = 0;
        boolean escaped = false;
        for (int index = 0; index < input.length(); index++) {
            char character = input.charAt(index);
            if (quote != 0) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == quote) {
                    quote = 0;
                }
                continue;
            }
            if (character == '\'' || character == '"') {
                quote = character;
            } else if (character == '[') {
                squareDepth++;
            } else if (character == ']') {
                if (--squareDepth < 0) {
                    throw invalid(selector);
                }
            } else if (character == '{') {
                curlyDepth++;
            } else if (character == '}') {
                if (--curlyDepth < 0) {
                    throw invalid(selector);
                }
            } else if (character == ',' && squareDepth == 0 && curlyDepth == 0) {
                addSelectorArgument(arguments, input.substring(start, index), selector);
                start = index + 1;
            }
        }
        if (quote != 0 || squareDepth != 0 || curlyDepth != 0) {
            throw invalid(selector);
        }
        if (!input.isEmpty()) {
            addSelectorArgument(arguments, input.substring(start), selector);
        }
        return arguments;
    }

    private static void addSelectorArgument(List<String> arguments, String argument,
                                            String selector) throws CommandException {
        String trimmed = argument.trim();
        if (trimmed.isEmpty()) {
            throw invalid(selector);
        }
        arguments.add(trimmed);
    }

    private static int parseLimit(String value, String selector) throws CommandException {
        if (!INTEGER.matcher(value).matches()) {
            throw invalid(selector);
        }
        try {
            int limit = Integer.parseInt(value);
            if (limit <= 0) {
                throw invalid(selector);
            }
            return limit;
        } catch (NumberFormatException exception) {
            throw invalid(selector);
        }
    }

    private static double parseNumber(String value, String selector, boolean nonNegative)
            throws CommandException {
        if (!(nonNegative ? DECIMAL : COORDINATE).matcher(value).matches()) {
            throw invalid(selector);
        }
        try {
            double number = Double.parseDouble(value);
            if (!Double.isFinite(number) || nonNegative && number < 0.0D) {
                throw invalid(selector);
            }
            return number;
        } catch (NumberFormatException exception) {
            throw invalid(selector);
        }
    }

    private static DistanceRange parseDistance(String value, String selector)
            throws CommandException {
        int separator = value.indexOf("..");
        if (separator < 0) {
            double exact = parseNumber(value, selector, true);
            return new DistanceRange(exact, exact);
        }
        if (value.indexOf("..", separator + 2) >= 0) {
            throw invalid(selector);
        }
        String minimumText = value.substring(0, separator);
        String maximumText = value.substring(separator + 2);
        if (minimumText.isEmpty() && maximumText.isEmpty()) {
            throw invalid(selector);
        }
        double minimum = minimumText.isEmpty() ? 0.0D : parseNumber(minimumText, selector, true);
        double maximum = maximumText.isEmpty() ? Double.POSITIVE_INFINITY
                : parseNumber(maximumText, selector, true);
        if (minimum > maximum) {
            throw invalid(selector);
        }
        return new DistanceRange(minimum, maximum);
    }

    private static <TargetEntity extends Entity> List<TargetEntity> select(
            ICommandSender sender, String selector, Selection selection,
            Class<TargetEntity> entityClass) throws CommandException {
        ICommandSender candidateSender = new ContextSender(sender, sender.getCommandSenderEntity(),
                sender.getEntityWorld(), selection.origin);
        List<TargetEntity> candidates = new ArrayList<>(EntitySelector.matchEntities(
                candidateSender, selection.nativeSelector, entityClass));
        if (selection.entityType != null) {
            boolean inverted = selection.entityType.startsWith("!");
            ResourceLocation entityType;
            try {
                String type = inverted ? selection.entityType.substring(1) : selection.entityType;
                if (type.startsWith("#") || type.isEmpty()) {
                    throw new IllegalArgumentException(type);
                }
                entityType = new ResourceLocation(type);
            } catch (IllegalArgumentException exception) {
                throw invalid(selector);
            }
            candidates.removeIf(entity -> EntityList.isMatchingName(entity, entityType) == inverted);
        }
        if (!selection.nbtFilters.isEmpty()) {
            candidates.removeIf(entity -> {
                NBTTagCompound entityData = entity.writeToNBT(new NBTTagCompound());
                entityData.removeTag("id");
                UUID uuid = entity.getUniqueID();
                long most = uuid.getMostSignificantBits();
                long least = uuid.getLeastSignificantBits();
                entityData.setIntArray("UUID", new int[]{
                        (int) (most >> 32), (int) most,
                        (int) (least >> 32), (int) least});
                entityData.removeTag("UUIDMost");
                entityData.removeTag("UUIDLeast");
                addSelectedItemData(entity, entityData);
                for (NbtFilter filter : selection.nbtFilters) {
                    boolean matches = matchesCompound(filter.expected, entityData);
                    if (matches == filter.inverted) {
                        return true;
                    }
                }
                return false;
            });
        }
        candidates.removeIf(entity -> selection.worldLimited
                && entity.getEntityWorld() != sender.getEntityWorld()
                || selection.distance != null
                && !selection.distance.contains(distance(entity, selection.origin)));
        if ("nearest".equals(selection.sort) || "furthest".equals(selection.sort)) {
            Comparator<TargetEntity> comparator = Comparator.comparingDouble(
                    entity -> distance(entity, selection.origin));
            candidates.sort("furthest".equals(selection.sort) ? comparator.reversed() : comparator);
        } else if ("random".equals(selection.sort)) {
            Collections.shuffle(candidates, sender.getEntityWorld().rand);
        }
        return candidates.size() > selection.limit
                ? new ArrayList<>(candidates.subList(0, selection.limit)) : candidates;
    }

    private static double distance(Entity entity, Vec3d origin) {
        return Math.hypot(Math.hypot(entity.posX - origin.x, entity.posY - origin.y),
                entity.posZ - origin.z);
    }

    private static boolean matchesCompound(NBTTagCompound expected, NBTTagCompound actual) {
        for (String key : expected.getKeySet()) {
            if (!actual.hasKey(key)
                    || !matchesTag(expected.getTag(key), actual.getTag(key))) {
                return false;
            }
        }
        return true;
    }

    private static void addSelectedItemData(Entity entity, NBTTagCompound entityData) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        ItemStack stack = ((EntityPlayer) entity).getHeldItemMainhand();
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return;
        }
        NBTTagCompound selectedItem = new NBTTagCompound();
        selectedItem.setString("id", stack.getItem().getRegistryName().toString());
        selectedItem.setInteger("count", stack.getCount());
        entityData.setTag("SelectedItem", selectedItem);
    }

    private static boolean matchesTag(NBTBase expected, NBTBase actual) {
        if (expected instanceof NBTTagCompound) {
            return actual instanceof NBTTagCompound
                    && matchesCompound((NBTTagCompound) expected, (NBTTagCompound) actual);
        }
        if (expected instanceof NBTTagList) {
            if (!(actual instanceof NBTTagList)) {
                return false;
            }
            NBTTagList expectedList = (NBTTagList) expected;
            NBTTagList actualList = (NBTTagList) actual;
            if (expectedList.tagCount() == 0) {
                return actualList.tagCount() == 0;
            }
            if (actualList.tagCount() < expectedList.tagCount()) {
                return false;
            }
            for (int expectedIndex = 0; expectedIndex < expectedList.tagCount(); expectedIndex++) {
                boolean found = false;
                for (int actualIndex = 0; actualIndex < actualList.tagCount(); actualIndex++) {
                    if (matchesTag(expectedList.get(expectedIndex), actualList.get(actualIndex))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        return expected.equals(actual);
    }

    private static CommandException invalid(String selector) {
        return new CommandException("commands.farmers_future_delight.selector.invalid", selector);
    }

    private static final class Selection {
        private final String nativeSelector;
        private final Vec3d origin;
        private final int limit;
        private final String sort;
        private final DistanceRange distance;
        private final boolean worldLimited;
        private final String entityType;
        private final List<NbtFilter> nbtFilters;
        private Selection(String nativeSelector, Vec3d origin, int limit, String sort,
                          DistanceRange distance, boolean worldLimited, String entityType,
                          List<NbtFilter> nbtFilters) {
            this.nativeSelector = nativeSelector;
            this.origin = origin;
            this.limit = limit;
            this.sort = sort;
            this.distance = distance;
            this.worldLimited = worldLimited;
            this.entityType = entityType;
            this.nbtFilters = nbtFilters;
        }
    }

    private static final class NbtFilter {
        private final NBTTagCompound expected;
        private final boolean inverted;

        private NbtFilter(NBTTagCompound expected, boolean inverted) {
            this.expected = expected;
            this.inverted = inverted;
        }
    }

    private static final class DistanceRange {
        private final double minimum;
        private final double maximum;

        private DistanceRange(double minimum, double maximum) {
            this.minimum = minimum;
            this.maximum = maximum;
        }

        private boolean contains(double distance) {
            return distance >= minimum && distance <= maximum;
        }
    }
}
