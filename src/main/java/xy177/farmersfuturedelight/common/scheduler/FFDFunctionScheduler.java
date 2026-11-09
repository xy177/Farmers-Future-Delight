package xy177.farmersfuturedelight.common.scheduler;

import java.io.File;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.advancements.FunctionManager;
import net.minecraft.command.FunctionObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FFDFunctionScheduler extends WorldSavedData {
    private static final String DATA_NAME = "ffd_scheduled_functions";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Comparator<ScheduledFunction> ENTRY_ORDER = Comparator
            .comparingLong((ScheduledFunction entry) -> entry.dueTime)
            .thenComparingLong(entry -> entry.sequence);
    private final Queue<ScheduledFunction> entries = new PriorityQueue<>(ENTRY_ORDER);
    private long nextSequence;

    public FFDFunctionScheduler(String name) {
        super(name);
    }

    public static long schedule(MinecraftServer server, String target, long delay, boolean replace) {
        WorldServer world = server.worlds[0];
        FFDFunctionScheduler data = get(world);
        long dueTime = Math.addExact(world.getTotalWorldTime(), delay);
        if (replace) {
            data.entries.removeIf(entry -> entry.target.equals(target));
        } else if (data.entries.stream().anyMatch(
                entry -> entry.target.equals(target) && entry.dueTime == dueTime)) {
            return dueTime;
        }
        data.entries.add(new ScheduledFunction(target, dueTime, data.nextSequence++));
        data.markDirty();
        return dueTime;
    }

    public static int clear(MinecraftServer server, String target) {
        FFDFunctionScheduler data = get(server.worlds[0]);
        int previousSize = data.entries.size();
        data.entries.removeIf(entry -> entry.target.equals(target));
        int removed = previousSize - data.entries.size();
        if (removed > 0) {
            data.markDirty();
        }
        return removed;
    }

    public static boolean hasFunctionTag(MinecraftServer server, ResourceLocation tag) {
        return readTag(server.worlds[0], tag, new HashSet<>()) != null;
    }

    public static Set<String> getTargets(MinecraftServer server) {
        Set<String> targets = new LinkedHashSet<>();
        for (ScheduledFunction entry : get(server.worlds[0]).entries) {
            targets.add(entry.target);
        }
        return targets;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
            return;
        }
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return;
        }
        WorldServer[] worlds = server.worlds;
        if (worlds == null || worlds.length == 0 || worlds[0] == null) {
            return;
        }
        WorldServer world = worlds[0];
        FFDFunctionScheduler data = get(world);
        long currentTime = world.getTotalWorldTime();
        while (!data.entries.isEmpty() && data.entries.peek().dueTime <= currentTime) {
            ScheduledFunction entry = data.entries.remove();
            data.markDirty();
            executeTarget(server, world, entry.target);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        entries.clear();
        nextSequence = 0L;
        NBTTagList list = compound.getTagList("Entries", 10);
        for (int index = 0; index < list.tagCount(); index++) {
            NBTTagCompound entry = list.getCompoundTagAt(index);
            if (entry.hasKey("Target", 8) && entry.hasKey("DueTime", 4)) {
                entries.add(new ScheduledFunction(entry.getString("Target"),
                        entry.getLong("DueTime"), nextSequence++));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        List<ScheduledFunction> orderedEntries = new ArrayList<>(entries);
        Collections.sort(orderedEntries, ENTRY_ORDER);
        for (ScheduledFunction entry : orderedEntries) {
            NBTTagCompound serialized = new NBTTagCompound();
            serialized.setString("Target", entry.target);
            serialized.setLong("DueTime", entry.dueTime);
            list.appendTag(serialized);
        }
        compound.setTag("Entries", list);
        return compound;
    }

    private static FFDFunctionScheduler get(WorldServer world) {
        MapStorage storage = world.getPerWorldStorage();
        FFDFunctionScheduler data = (FFDFunctionScheduler) storage.getOrLoadData(
                FFDFunctionScheduler.class, DATA_NAME);
        if (data == null) {
            data = new FFDFunctionScheduler(DATA_NAME);
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    private static void executeTarget(MinecraftServer server, WorldServer world, String target) {
        if (target.startsWith("#")) {
            ResourceLocation tag;
            try {
                tag = new ResourceLocation(target.substring(1));
            } catch (RuntimeException exception) {
                return;
            }
            Set<ResourceLocation> visited = new HashSet<>();
            List<ResourceLocation> functions = readTag(world, tag, visited);
            if (functions == null) {
                LOGGER.warn("Skipping invalid scheduled function tag {}", tag);
                return;
            }
            for (ResourceLocation function : functions) {
                executeFunction(server.getFunctionManager(), server, target, function);
            }
            return;
        }
        try {
            executeFunction(server.getFunctionManager(), server, target,
                    new ResourceLocation(target));
        } catch (RuntimeException exception) {
            LOGGER.warn("Skipping invalid scheduled function target {}", target, exception);
        }
    }

    private static void executeFunction(FunctionManager manager, MinecraftServer server,
                                        String name, ResourceLocation id) {
        FunctionObject function = manager.getFunction(id);
        if (function != null) {
            manager.execute(function, new ScheduledFunctionSender(server, name));
        }
    }

    private static List<ResourceLocation> readTag(WorldServer world, ResourceLocation tag,
                                                   Set<ResourceLocation> visited) {
        Set<ResourceLocation> functions = new LinkedHashSet<>();
        if (!visited.add(tag)) {
            return null;
        }
        File file = getTagFile(world, tag);
        try {
            if (!isFunctionTagFile(world, tag)) {
                return null;
            }
            try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                JsonElement parsed = new JsonParser().parse(reader);
                if (!parsed.isJsonObject()) {
                    return null;
                }
                JsonObject object = parsed.getAsJsonObject();
                JsonElement valuesElement = object.get("values");
                if (valuesElement == null || !valuesElement.isJsonArray()) {
                    return null;
                }
                JsonArray values = valuesElement.getAsJsonArray();
                for (JsonElement valueElement : values) {
                    boolean required = true;
                    String value;
                    if (valueElement.isJsonPrimitive()
                            && valueElement.getAsJsonPrimitive().isString()) {
                        value = valueElement.getAsString();
                    } else if (valueElement.isJsonObject()) {
                        JsonObject member = valueElement.getAsJsonObject();
                        JsonElement idElement = member.get("id");
                        if (idElement == null || !idElement.isJsonPrimitive()
                                || !idElement.getAsJsonPrimitive().isString()) {
                            return null;
                        }
                        value = idElement.getAsString();
                        if (member.has("required")) {
                            JsonElement requirement = member.get("required");
                            if (!requirement.isJsonPrimitive()
                                    || !requirement.getAsJsonPrimitive().isBoolean()) {
                                return null;
                            }
                            required = requirement.getAsBoolean();
                        }
                    } else {
                        return null;
                    }
                    if (value.startsWith("?")) {
                        required = false;
                        value = value.substring(1);
                    }
                    boolean nestedTag = value.startsWith("#");
                    if (nestedTag) {
                        value = value.substring(1);
                    }
                    ResourceLocation id;
                    try {
                        id = new ResourceLocation(value);
                    } catch (RuntimeException exception) {
                        return null;
                    }
                    if (nestedTag) {
                        List<ResourceLocation> nested = readTag(world, id, visited);
                        if (nested != null) {
                            functions.addAll(nested);
                        } else if (required) {
                            return null;
                        }
                    } else if (world.getFunctionManager().getFunction(id) != null) {
                        functions.add(id);
                    } else if (required) {
                        return null;
                    }
                }
            }
        } catch (Exception exception) {
            LOGGER.warn("Unable to load scheduled function tag {}", tag, exception);
            return null;
        } finally {
            visited.remove(tag);
        }
        return new ArrayList<>(functions);
    }

    private static File getTagFile(WorldServer world, ResourceLocation tag) {
        File root = new File(world.getSaveHandler().getWorldDirectory(), "data");
        File modern = new File(root, tag.getResourceDomain() + "/tags/function/"
                + tag.getResourcePath() + ".json");
        return modern.isFile() ? modern : new File(root,
                "tags/functions/" + tag.getResourceDomain() + "/"
                        + tag.getResourcePath() + ".json");
    }

    private static boolean isFunctionTagFile(WorldServer world, ResourceLocation tag) {
        File root = new File(world.getSaveHandler().getWorldDirectory(), "data");
        File file = getTagFile(world, tag);
        try {
            return file.isFile()
                    && file.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator);
        } catch (Exception exception) {
            return false;
        }
    }

    private static final class ScheduledFunction {
        private final String target;
        private final long dueTime;
        private final long sequence;

        private ScheduledFunction(String target, long dueTime, long sequence) {
            this.target = target;
            this.dueTime = dueTime;
            this.sequence = sequence;
        }
    }

    private static final class ScheduledFunctionSender implements ICommandSender {
        private final MinecraftServer server;
        private final String name;

        private ScheduledFunctionSender(MinecraftServer server, String name) {
            this.server = server;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean canUseCommand(int permissionLevel, String commandName) {
            return xy177.farmersfuturedelight.common.FFDConfig
                    .canScheduledFunctionUseCommand(permissionLevel);
        }

        @Override
        public World getEntityWorld() {
            return server.worlds[0];
        }

        @Override
        public MinecraftServer getServer() {
            return server;
        }

        @Override
        public BlockPos getPosition() {
            return BlockPos.ORIGIN;
        }

        @Override
        public Vec3d getPositionVector() {
            return Vec3d.ZERO;
        }
    }
}
