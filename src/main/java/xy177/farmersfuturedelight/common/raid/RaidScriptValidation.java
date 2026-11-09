package xy177.farmersfuturedelight.common.raid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition;
import xy177.farmersfuturedelight.api.raid.RaidEventRegistry;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class RaidScriptValidation {
    private static final Set<String> FAILURES = new LinkedHashSet<>();
    private static final Map<String, Set<Integer>> REFERENCES = new LinkedHashMap<>();
    private static final Pattern BUILDER = Pattern.compile("\\b(?:val|var)\\s+(\\w+)\\s*=\\s*\\w+\\.(create|get)\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");
    private static final Pattern WAVE_EDIT = Pattern.compile("\\b(\\w+)\\.(setRestTime|setLoop|setWaveMobCount)\\s*\\(\\s*(\\d+)");
    private static final Pattern WAVE_CALL = Pattern.compile("\\.(isInRaidWave|onWave|onRaidWave|getRaidElapsedTicks)\\s*\\(\\s*\"([^\"]+)\"\\s*,\\s*(\\d+)(?:\\s*,\\s*(\\d+))?");

    private RaidScriptValidation() { }

    public static synchronized void record(String id, String reason) { FAILURES.add(id + ": " + reason); }
    public static synchronized void reference(String id, int number) { REFERENCES.computeIfAbsent(id, key -> new LinkedHashSet<>()).add(number); }

    public static boolean knownWave(String id, int wave) {
        RaidEventDefinition definition;
        try {
            definition = RaidEventRegistry.get(RaidEventDefinition.identifier(id));
        } catch (IllegalArgumentException exception) { return false; }
        if (definition == null || wave <= 0) return false;
        for (net.minecraft.world.EnumDifficulty difficulty : definition.getWaves().keySet()) {
            if (definition.getScriptRules() != null) {
                if (definition.getScriptRules().numbers(definition, difficulty, 255).contains(wave)) return true;
            } else if (wave <= definition.getWaves(difficulty).size() + definition.getBonusWaves(difficulty).size()) return true;
        }
        return false;
    }

    public static List<String> check(Path scripts) {
        Set<String> errors;
        synchronized (RaidScriptValidation.class) {
            errors = new LinkedHashSet<>(FAILURES);
            for (Map.Entry<String, Set<Integer>> entry : REFERENCES.entrySet()) {
                for (int wave : entry.getValue()) if (!knownWave(entry.getKey(), wave)) errors.add("Undefined raid wave " + entry.getKey() + " / " + wave);
            }
        }
        if (Files.isDirectory(scripts)) {
            try (Stream<Path> paths = Files.walk(scripts)) {
                paths.filter(path -> path.toString().endsWith(".zs")).forEach(path -> {
                    try {
                        if (Files.size(path) > 1048576) return;
                        String source = stripComments(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                        Map<String, String> builders = new LinkedHashMap<>();
                        Matcher declarations = BUILDER.matcher(source);
                        while (declarations.find()) builders.put(declarations.group(1), declarations.group(3));
                        Matcher edits = WAVE_EDIT.matcher(source);
                        while (edits.find()) {
                            String id = builders.get(edits.group(1));
                            if (id != null) checkNumber(errors, path, source, edits.start(), id, edits.group(3));
                        }
                        Matcher calls = WAVE_CALL.matcher(source);
                        while (calls.find()) {
                            if (!calls.group(1).equals("getRaidElapsedTicks") || !calls.group(3).equals("0")) checkNumber(errors, path, source, calls.start(), calls.group(2), calls.group(3));
                            if (calls.group(1).equals("isInRaidWave") && calls.group(4) != null) checkNumber(errors, path, source, calls.start(), calls.group(2), calls.group(4));
                        }
                    } catch (IOException exception) { errors.add("Could not read " + path.getFileName()); }
                });
            } catch (IOException exception) { errors.add("Could not read raid scripts: " + exception.getMessage()); }
        }
        return new ArrayList<>(errors);
    }

    private static void checkNumber(Set<String> errors, Path file, String source, int offset, String id, String number) {
        int wave;
        try { wave = Integer.parseInt(number); } catch (NumberFormatException exception) { wave = -1; }
        if (!knownWave(id, wave)) {
            int line = 1;
            for (int index = 0; index < offset; index++) if (source.charAt(index) == '\n') line++;
            errors.add(file.getFileName() + ":" + line + ": Undefined raid wave " + id + " / " + number);
        }
    }

    private static String stripComments(String input) {
        StringBuilder output = new StringBuilder(input);
        char quote = 0;
        boolean line = false;
        boolean block = false;
        for (int index = 0; index < input.length(); index++) {
            char value = input.charAt(index);
            char next = index + 1 < input.length() ? input.charAt(index + 1) : 0;
            if (line || block) {
                if (value != '\n' && value != '\r') output.setCharAt(index, ' ');
                if (line && value == '\n') line = false;
                if (block && value == '*' && next == '/') {
                    output.setCharAt(++index, ' ');
                    block = false;
                }
            } else if (quote != 0) {
                if (value == '\\') index++;
                else if (value == quote) quote = 0;
            } else if (value == '"' || value == '\'') quote = value;
            else if (value == '/' && (next == '/' || next == '*') || value == '#') {
                line = value == '#' || next == '/';
                block = !line;
                output.setCharAt(index, ' ');
                if (value == '/') output.setCharAt(++index, ' ');
            }
        }
        return output.toString();
    }

    private static void report(ICommandSender sender, String message) {
        TextComponentString text = new TextComponentString("[FFD] " + message);
        text.getStyle().setColor(TextFormatting.RED);
        sender.sendMessage(text);
    }

    @SubscribeEvent
    public static void command(CommandEvent event) {
        String[] arguments = event.getParameters();
        if (!event.getCommand().getName().equals("crafttweaker") && !event.getCommand().getName().equals("ct")) return;
        if (arguments.length == 0 || !arguments[0].equals("syntax") || event.getSender().getServer() == null) return;
        event.getSender().getServer().addScheduledTask(() -> {
            for (String error : check(event.getSender().getServer().getFile("scripts").toPath())) report(event.getSender(), error);
        });
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;
        synchronized (RaidScriptValidation.class) {
            for (String error : FAILURES) report(event.player, error);
        }
    }
}
