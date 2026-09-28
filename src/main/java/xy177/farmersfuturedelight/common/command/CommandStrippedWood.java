package xy177.farmersfuturedelight.common.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDCustomStrippedWoods;

public final class CommandStrippedWood extends CommandBase {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String[] FAMILY_SUFFIXES = {"_hyphae", "_stem", "_wood", "_log"};

    @Override
    public String getName() {
        return "ffd_strippedwood";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ffd_strippedwood <scan|generate> [all]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(net.minecraft.server.MinecraftServer server, ICommandSender sender,
                        String[] args) throws CommandException {
        if (args.length < 1 || args.length > 2
                || (!"scan".equalsIgnoreCase(args[0])
                && !"generate".equalsIgnoreCase(args[0]))
                || (args.length == 2 && !"all".equalsIgnoreCase(args[1]))) {
            throw new WrongUsageException(getUsage(sender));
        }
        boolean all = args.length == 2;
        List<Candidate> candidates = scanCandidates(all);
        if ("scan".equalsIgnoreCase(args[0])) {
            sendScanSummary(sender, candidates, all);
            return;
        }
        generate(sender, candidates);
    }

    @Override
    public List<String> getTabCompletions(net.minecraft.server.MinecraftServer server,
                                          ICommandSender sender, String[] args,
                                          net.minecraft.util.math.BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "scan", "generate");
        }
        if (args.length == 2 && ("scan".equalsIgnoreCase(args[0])
                || "generate".equalsIgnoreCase(args[0]))) {
            return getListOfStringsMatchingLastWord(args, "all");
        }
        return Collections.emptyList();
    }

    private static List<Candidate> scanCandidates(boolean all) {
        Map<String, Candidate> candidates = new LinkedHashMap<>();
        for (Block block : ForgeRegistries.BLOCKS) {
            ResourceLocation name = block.getRegistryName();
            if (name == null || "minecraft".equals(name.getResourceDomain())
                    || !FFDCustomStrippedWoods.isWoodCandidate(block)) {
                continue;
            }
            Family family = family(block, all);
            if (family == null) {
                continue;
            }
            List<Integer> metadata = FFDCustomStrippedWoods.candidateMetadata(block);
            if (metadata.isEmpty()) {
                continue;
            }
            String key = name.getResourceDomain().toLowerCase(Locale.ROOT)
                    + ":" + family.name;
            Candidate candidate = candidates.get(key);
            if (candidate == null) {
                candidate = new Candidate(name.getResourceDomain(), family.name);
                candidates.put(key, candidate);
            }
            for (Integer value : metadata) {
                String selector = "block:" + name + "@" + value;
                candidate.add(family.wood ? candidate.woods : candidate.logs, selector);
            }
        }
        List<Candidate> result = new ArrayList<>(candidates.values());
        for (Candidate candidate : result) {
            candidate.finish();
        }
        result.removeIf(candidate -> candidate.logs.isEmpty() && candidate.woods.isEmpty());
        result.sort(Comparator.comparing(Candidate::material));
        return result;
    }

    private static Family family(Block block, boolean all) {
        ResourceLocation name = block.getRegistryName();
        String path = name.getResourcePath().toLowerCase(Locale.ROOT);
        boolean recognized = block instanceof BlockLog;
        boolean wood = false;
        String family = path;
        for (String suffix : FAMILY_SUFFIXES) {
            if (path.endsWith(suffix) && path.length() > suffix.length()) {
                recognized = true;
                family = path.substring(0, path.length() - suffix.length());
                wood = "_wood".equals(suffix) || "_hyphae".equals(suffix);
                break;
            }
        }
        if (!recognized && !all) {
            return null;
        }
        if (family.isEmpty()) {
            family = path;
        }
        return new Family(normalize(family), wood);
    }

    private static String normalize(String value) {
        StringBuilder result = new StringBuilder();
        boolean underscore = false;
        for (int index = 0; index < value.length(); index++) {
            char character = Character.toLowerCase(value.charAt(index));
            if ((character >= 'a' && character <= 'z')
                    || (character >= '0' && character <= '9')) {
                result.append(character);
                underscore = false;
            } else if (!underscore && result.length() > 0) {
                result.append('_');
                underscore = true;
            }
        }
        while (result.length() > 0 && result.charAt(result.length() - 1) == '_') {
            result.deleteCharAt(result.length() - 1);
        }
        return result.length() == 0 ? "wood" : result.toString();
    }

    private static void sendScanSummary(ICommandSender sender, List<Candidate> candidates,
                                        boolean all) {
        sender.sendMessage(new TextComponentTranslation(
                "commands.farmers_future_delight.strippedwood.scan_summary",
                candidates.size(), all ? "all" : "named families"));
        for (Candidate candidate : candidates) {
            sender.sendMessage(new TextComponentString("- " + candidate.material()
                    + " | log=" + candidate.logs.size() + " | wood="
                    + candidate.woods.size()));
        }
        if (candidates.isEmpty()) {
            sender.sendMessage(new TextComponentTranslation(
                    "commands.farmers_future_delight.strippedwood.none"));
        }
    }

    private static void generate(ICommandSender sender, List<Candidate> candidates) {
        File directory = FFDCustomStrippedWoods.definitionDirectory();
        if (directory == null || (!directory.exists() && !directory.mkdirs())) {
            sender.sendMessage(new TextComponentTranslation(
                    "commands.farmers_future_delight.strippedwood.directory_error"));
            return;
        }
        int generated = 0;
        int skipped = 0;
        List<String> generatedFiles = new ArrayList<>();
        List<String> skippedFiles = new ArrayList<>();
        for (Candidate candidate : candidates) {
            File target = new File(directory, candidate.material() + ".json");
            if (target.exists() || FFDCustomStrippedWoods.get(candidate.material()) != null) {
                skipped++;
                skippedFiles.add(target.getName());
                continue;
            }
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(target),
                    StandardCharsets.UTF_8)) {
                GSON.toJson(candidate.json(), writer);
                generated++;
                generatedFiles.add(target.getName());
            } catch (Exception error) {
                skipped++;
                skippedFiles.add(target.getName());
                sender.sendMessage(new TextComponentString("Could not write "
                        + target.getName() + ": " + error.getMessage()));
            }
        }
        writeReport(candidates, generatedFiles, skippedFiles);
        sender.sendMessage(new TextComponentTranslation(
                "commands.farmers_future_delight.strippedwood.generated", generated, skipped));
        if (!generatedFiles.isEmpty()) {
            sender.sendMessage(new TextComponentTranslation(
                    "commands.farmers_future_delight.strippedwood.restart"));
        }
        if (!skippedFiles.isEmpty()) {
            sender.sendMessage(new TextComponentString("Skipped: "
                    + String.join(", ", skippedFiles)));
        }
    }

    private static void writeReport(List<Candidate> candidates, List<String> generated,
                                   List<String> skipped) {
        File directory = FFDCustomStrippedWoods.generationDirectory();
        if (directory == null || (!directory.exists() && !directory.mkdirs())) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT)
                .format(new Date());
        JsonObject root = new JsonObject();
        root.addProperty("generatedAt", timestamp);
        root.addProperty("source", "ffd_strippedwood generate");
        JsonArray entries = new JsonArray();
        for (Candidate candidate : candidates) {
            entries.add(candidate.json());
        }
        root.add("candidates", entries);
        JsonArray generatedFiles = new JsonArray();
        for (String value : generated) {
            generatedFiles.add(value);
        }
        root.add("generatedFiles", generatedFiles);
        JsonArray skippedFiles = new JsonArray();
        for (String value : skipped) {
            skippedFiles.add(value);
        }
        root.add("skippedFiles", skippedFiles);
        File report = new File(directory, "scan-" + timestamp + ".json");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(report),
                StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (Exception ignored) {
        }
    }

    private static final class Family {
        private final String name;
        private final boolean wood;

        private Family(String name, boolean wood) {
            this.name = name;
            this.wood = wood;
        }
    }

    private static final class Candidate {
        private final String modid;
        private final String family;
        private final Set<String> logs = new LinkedHashSet<>();
        private final Set<String> woods = new LinkedHashSet<>();
        private String material;

        private Candidate(String modid, String family) {
            this.modid = normalize(modid);
            this.family = normalize(family);
        }

        private void add(Set<String> selectors, String selector) {
            selectors.add(selector);
        }

        private void finish() {
            material = materialName(modid, family);
        }

        private String material() {
            return material;
        }

        private JsonObject json() {
            JsonObject root = new JsonObject();
            root.addProperty("material", material);
            if (!logs.isEmpty()) {
                root.add("log", array(logs));
            }
            if (!woods.isEmpty()) {
                root.add("wood", array(woods));
            }
            root.addProperty("color", "auto");
            JsonObject guide = new JsonObject();
            guide.addProperty("en_us", "Generated from verified Material.WOOD rotated-pillar blocks. Review the selectors before enabling.");
            guide.addProperty("zh_cn", "由已验证为 Material.WOOD 的旋转柱木头方块生成，请启用前审核选择器。");
            root.add("_guide", guide);
            return root;
        }

        private JsonArray array(Set<String> values) {
            JsonArray result = new JsonArray();
            for (String value : values) {
                result.add(value);
            }
            return result;
        }

        private static String materialName(String modid, String family) {
            String value = normalize(modid + "_" + family);
            if (value.length() <= 48) {
                return value;
            }
            String hash = Integer.toHexString(value.hashCode());
            if (hash.length() > 8) {
                hash = hash.substring(hash.length() - 8);
            }
            int prefixLength = 48 - hash.length() - 1;
            return value.substring(0, prefixLength) + "_" + hash;
        }
    }
}
