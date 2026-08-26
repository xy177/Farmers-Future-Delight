package xy177.farmersfuturedelight.common.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.oredict.OreDictionary;
import xy177.farmersfuturedelight.common.registry.FFDCustomRawOres;
import xy177.farmersfuturedelight.common.registry.FFDRawOres;

public final class CommandRawOre extends CommandBase {
    private static final Pattern MATERIAL_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_]{0,47}");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> BUILT_IN = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(FFDRawOres.NAMES)));
    private static final Set<String> EXCLUDED_MATERIALS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("ancientdebris", "ancient_debris", "debris",
                    "netherite", "netheritescrap", "netherite_scrap")));
    private static final String[] PRODUCT_PREFIXES = {"ingot", "gem", "dust", "crystal", "nugget"};
    private static final Map<String, Map<String, String>> LOCALIZATION_CACHE = new HashMap<>();

    @Override
    public String getName() {
        return "ffd_rawore";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ffd_rawore <scan|generate> [all]";
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
        generate(sender, candidates, all);
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
        String[] names = OreDictionary.getOreNames();
        Arrays.sort(names);
        for (String oreName : names) {
            if (oreName == null || !oreName.startsWith("ore") || oreName.length() <= 3) {
                continue;
            }
            String material = canonicalMaterial(oreName.substring(3));
            if (material == null || BUILT_IN.contains(material)) {
                continue;
            }
            List<ItemStack> valid = validSourceStacks(OreDictionary.getOres(oreName));
            if (valid.isEmpty()) {
                continue;
            }
            if (isExcludedOre(oreName, material, valid)) {
                continue;
            }
            Candidate candidate = candidates.get(material);
            if (candidate == null) {
                candidate = new Candidate(material);
                candidates.put(material, candidate);
            }
            candidate.oreNames.add(oreName);
            for (ItemStack stack : valid) {
                candidate.addSource(stack);
            }
        }
        List<Candidate> result = new ArrayList<>(candidates.values());
        for (Candidate candidate : result) {
            candidate.resolve(all);
        }
        Collections.sort(result, Comparator.comparing(value -> value.material));
        return result;
    }

    private static List<ItemStack> validSourceStacks(List<ItemStack> stacks) {
        Map<String, ItemStack> unique = new LinkedHashMap<>();
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }
            Block block = Block.getBlockFromItem(stack.getItem());
            ResourceLocation itemName = stack.getItem().getRegistryName();
            ResourceLocation blockName = block == null ? null : block.getRegistryName();
            if (block == null || itemName == null || blockName == null
                    || !ForgeRegistries.ITEMS.containsKey(itemName)
                    || !ForgeRegistries.BLOCKS.containsKey(blockName)) {
                continue;
            }
            unique.put(stackKey(stack), stack.copy());
        }
        return new ArrayList<>(unique.values());
    }

    private static String canonicalMaterial(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return null;
        }
        String material = suffix.replaceAll("[^A-Za-z0-9]+", "_")
                .toLowerCase(Locale.ROOT).replaceAll("_+", "_");
        while (material.startsWith("_")) {
            material = material.substring(1);
        }
        while (material.endsWith("_")) {
            material = material.substring(0, material.length() - 1);
        }
        if ("aluminum".equals(material)) {
            material = "aluminium";
        } else if ("chrome".equals(material)) {
            material = "chromium";
        } else if ("mythril".equals(material)) {
            material = "mithril";
        } else if ("rose_gold".equals(material)) {
            material = "rosegold";
        }
        return MATERIAL_PATTERN.matcher(material).matches() ? material : null;
    }

    private static boolean isExcludedOre(String oreName, String material, List<ItemStack> sources) {
        String normalizedOreName = oreName.toLowerCase(Locale.ROOT).replace("_", "");
        if (EXCLUDED_MATERIALS.contains(material)
                || "oreancientdebris".equals(normalizedOreName)
                || "orenetherite".equals(normalizedOreName)
                || "orenetheritescrap".equals(normalizedOreName)) {
            return true;
        }
        for (ItemStack source : sources) {
            ResourceLocation name = source.getItem().getRegistryName();
            if (name == null) {
                continue;
            }
            String path = name.getResourcePath().toLowerCase(Locale.ROOT).replace("_", "");
            if (path.contains("ancientdebris") || path.contains("netheritescrap")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isExcludedStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return false;
        }
        String path = stack.getItem().getRegistryName().getResourcePath()
                .toLowerCase(Locale.ROOT).replace("_", "");
        return path.contains("ancientdebris") || path.contains("netheritescrap");
    }

    private static boolean externalRawOreExists(String material) {
        ResourceLocation itemName = new ResourceLocation("raw_" + material);
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation name = item.getRegistryName();
            if (name != null && itemName.getResourcePath().equals(name.getResourcePath())
                    && !"farmers_future_delight".equals(name.getResourceDomain())) {
                return true;
            }
        }
        return false;
    }

    private static List<ProductMatch> productMatches(ItemStack output, String material) {
        List<ProductMatch> matches = new ArrayList<>();
        for (String oreName : OreDictionary.getOreNames()) {
            String category = productCategory(oreName);
            if (category == null || !material.equals(canonicalMaterial(oreName.substring(category.length())))) {
                continue;
            }
            for (ItemStack candidate : OreDictionary.getOres(oreName)) {
                if (sameItem(candidate, output)) {
                    matches.add(new ProductMatch(category, oreName));
                    break;
                }
            }
        }
        Collections.sort(matches, Comparator.comparingInt(value -> categoryRank(value.category)));
        return matches;
    }

    private static ProductMatch findDictionaryProduct(String material, boolean all) {
        Map<String, ProductMatch> byStack = new LinkedHashMap<>();
        for (String oreName : OreDictionary.getOreNames()) {
            String category = productCategory(oreName);
            if (category == null || (!all && !"ingot".equals(category))
                    || !material.equals(canonicalMaterial(oreName.substring(category.length())))) {
                continue;
            }
            List<ItemStack> stacks = validItemStacks(OreDictionary.getOres(oreName));
            if (stacks.size() != 1) {
                continue;
            }
            ItemStack stack = stacks.get(0);
            byStack.put(stackKey(stack), new ProductMatch(category, oreName, stack));
        }
        if (byStack.size() != 1) {
            return null;
        }
        ProductMatch result = byStack.values().iterator().next();
        result.selector = "oredict:" + result.oreName;
        return result;
    }

    private static List<ItemStack> validItemStacks(List<ItemStack> stacks) {
        Map<String, ItemStack> unique = new LinkedHashMap<>();
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty() || stack.getItem() == Items.AIR
                    || stack.getItem().getRegistryName() == null) {
                continue;
            }
            unique.put(stackKey(stack), stack.copy());
        }
        return new ArrayList<>(unique.values());
    }

    private static List<ItemStack> noSilkTouchDrops(ItemStack source) {
        if (source == null || source.isEmpty() || !(source.getItem() instanceof ItemBlock)) {
            return Collections.emptyList();
        }
        Block block = Block.getBlockFromItem(source.getItem());
        if (block == null) {
            return Collections.emptyList();
        }
        IBlockState state;
        try {
            state = block.getStateFromMeta(source.getMetadata());
        } catch (Throwable error) {
            state = block.getDefaultState();
        }
        Map<String, ItemStack> drops = new LinkedHashMap<>();
        for (int seed = 0; seed < 4; seed++) {
            try {
                Item item = block.getItemDropped(state, new java.util.Random(seed), 0);
                if (item == null || item == Items.AIR) {
                    continue;
                }
                int metadata = block.damageDropped(state);
                ItemStack drop = new ItemStack(item, 1, metadata);
                if (!drop.isEmpty() && drop.getItem().getRegistryName() != null) {
                    drops.put(stackKey(drop), drop);
                }
            } catch (Throwable ignored) {
            }
        }
        return new ArrayList<>(drops.values());
    }

    private static boolean isIntermediateDrop(ItemStack drop) {
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(drop);
        return result != null && !result.isEmpty() && !sameItem(result, drop);
    }

    private static String productCategory(String oreName) {
        for (String prefix : PRODUCT_PREFIXES) {
            if (oreName.startsWith(prefix) && oreName.length() > prefix.length()) {
                return prefix;
            }
        }
        return null;
    }

    private static int categoryRank(String category) {
        for (int i = 0; i < PRODUCT_PREFIXES.length; i++) {
            if (PRODUCT_PREFIXES[i].equals(category)) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    private static boolean sameItem(ItemStack first, ItemStack second) {
        return first != null && second != null && !first.isEmpty() && !second.isEmpty()
                && first.getItem() == second.getItem() && first.getMetadata() == second.getMetadata();
    }

    private static String stackKey(ItemStack stack) {
        ResourceLocation name = stack.getItem().getRegistryName();
        return (name == null ? "?" : name.toString()) + "@" + stack.getMetadata();
    }

    private static String itemSelector(ItemStack stack) {
        ResourceLocation name = stack.getItem().getRegistryName();
        return "item:" + name + "@" + stack.getMetadata();
    }

    private static void sendScanSummary(ICommandSender sender, List<Candidate> candidates, boolean all) {
        Summary summary = summarize(candidates, all, false);
        send(sender, all ? "commands.farmers_future_delight.rawore.scan_mode_all"
                : "commands.farmers_future_delight.rawore.scan_mode_default");
        send(sender, "commands.farmers_future_delight.rawore.candidates", candidates.size());
        send(sender, "commands.farmers_future_delight.rawore.summary", summary.high,
                summary.medium, summary.low);
        for (Candidate candidate : candidates) {
            if (candidate.resolution.status != Status.HIGH) {
                send(sender, "commands.farmers_future_delight.rawore.candidate_reason",
                        candidate.material, candidate.resolution.reason.component());
            }
        }
    }

    private static void generate(ICommandSender sender, List<Candidate> candidates, boolean all) {
        File definitionDirectory = FFDCustomRawOres.definitionDirectory();
        File generationDirectory = FFDCustomRawOres.generationDirectory();
        if (definitionDirectory == null || generationDirectory == null) {
            send(sender, "commands.farmers_future_delight.rawore.config_uninitialized");
            return;
        }
        File drafts = new File(generationDirectory, "drafts");
        File reports = new File(generationDirectory, "reports");
        if (!definitionDirectory.exists()) {
            definitionDirectory.mkdirs();
        }
        if (!drafts.exists()) {
            drafts.mkdirs();
        }
        if (!reports.exists()) {
            reports.mkdirs();
        }
        Summary summary = summarize(candidates, all, true);
        StringBuilder report = new StringBuilder();
        report.append("Farmer's Future Delight raw ore generation report\n");
        report.append("Mode: ").append(all ? "all" : "default").append('\n');
        for (Candidate candidate : candidates) {
            if (candidate.resolution.status == Status.HIGH) {
                File output = new File(definitionDirectory, candidate.material + ".json");
                if (output.exists() || FFDCustomRawOres.get(candidate.material) != null) {
                    summary.existing++;
                    report.append("EXISTING ").append(candidate.material).append('\n');
                    continue;
                }
                try {
                    writeDefinition(output, candidate);
                    summary.generated++;
                    report.append("GENERATED ").append(candidate.material).append('\n');
                } catch (Exception error) {
                    summary.low++;
                    report.append("ERROR ").append(candidate.material).append(": ")
                            .append(error.getMessage()).append('\n');
                }
            } else if (candidate.resolution.status == Status.MEDIUM) {
                File output = new File(drafts, "_" + candidate.material + ".json");
                if (!output.exists()) {
                    try {
                        writeDefinition(output, candidate);
                        report.append("DRAFT ").append(candidate.material).append('\n');
                    } catch (Exception error) {
                        report.append("DRAFT_ERROR ").append(candidate.material).append(": ")
                                .append(error.getMessage()).append('\n');
                    }
                } else {
                    report.append("DRAFT_EXISTS ").append(candidate.material).append('\n');
                }
            } else {
                report.append("REPORT ").append(candidate.material).append(": ")
                        .append(candidate.resolution.reason.reportText()).append('\n');
            }
        }
        report.insert(report.indexOf("Mode:"), "Generated: " + summary.generated + "\n")
                .insert(report.indexOf("Mode:"), "Drafts: " + summary.medium + "\n")
                .insert(report.indexOf("Mode:"), "Reports: " + summary.low + "\n");
        File reportFile = new File(reports, "raw_ore_generation_"
                + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date()) + ".txt");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(reportFile),
                StandardCharsets.UTF_8)) {
            writer.write(report.toString());
        } catch (Exception error) {
            send(sender, "commands.farmers_future_delight.rawore.report_write_failed",
                    error.getMessage());
        }
        send(sender, "commands.farmers_future_delight.rawore.generation_complete",
                summary.generated, summary.medium, summary.low, summary.existing);
        send(sender, "commands.farmers_future_delight.rawore.definition_directory",
                definitionDirectory.getPath());
        send(sender, "commands.farmers_future_delight.rawore.generation_directory",
                generationDirectory.getPath());
        send(sender, "commands.farmers_future_delight.rawore.restart_required");
    }

    private static Summary summarize(List<Candidate> candidates, boolean all, boolean writing) {
        Summary summary = new Summary();
        for (Candidate candidate : candidates) {
            if (candidate.resolution.status == Status.HIGH) {
                if (!writing) {
                    summary.high++;
                }
            } else if (candidate.resolution.status == Status.MEDIUM) {
                summary.medium++;
            } else {
                summary.low++;
            }
        }
        return summary;
    }

    private static void writeDefinition(File file, Candidate candidate) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("material", candidate.material);
        JsonArray ores = new JsonArray();
        for (String oreName : candidate.oreNames) {
            ores.add("oredict:" + oreName);
        }
        json.add("ores", ores);
        json.addProperty("smeltResult", candidate.resolution.selector);
        json.addProperty("color", "auto");
        JsonObject lang = new JsonObject();
        Map<String, String> productNames = localizedNames(candidate.resolution.stack);
        String englishName = productNames.get("en_us");
        if (englishName == null || englishName.trim().isEmpty()) {
            englishName = humanize(candidate.material);
        }
        String category = productCategoryFor(candidate.resolution.stack, candidate.material);
        englishName = stripProductSuffix(englishName, category, "en_us");
        lang.addProperty("en_us", generatedName("en_us",
                "item.farmers_future_delight.raw_ore.generated", englishName));
        String chineseName = productNames.get("zh_cn");
        if (chineseName != null && !chineseName.trim().isEmpty()) {
            chineseName = stripProductSuffix(chineseName, category, "zh_cn");
            lang.addProperty("zh_cn", generatedName("zh_cn",
                    "item.farmers_future_delight.raw_ore.generated", chineseName));
        }
        json.add("lang", lang);
        JsonObject blockLang = new JsonObject();
        blockLang.addProperty("en_us", generatedName("en_us",
                "tile.farmers_future_delight.raw_ore_block.generated", englishName));
        if (chineseName != null && !chineseName.trim().isEmpty()) {
            blockLang.addProperty("zh_cn", generatedName("zh_cn",
                    "tile.farmers_future_delight.raw_ore_block.generated", chineseName));
        }
        json.add("blockLang", blockLang);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file),
                StandardCharsets.UTF_8)) {
            GSON.toJson(json, writer);
        }
    }

    private static String humanize(String material) {
        String[] parts = material.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }

    private static String productCategoryFor(ItemStack stack, String material) {
        List<ProductMatch> matches = productMatches(stack, material);
        return matches.isEmpty() ? null : matches.get(0).category;
    }

    private static String stripProductSuffix(String name, String category, String language) {
        if (name == null || name.trim().isEmpty() || category == null) {
            return name;
        }
        String suffixes = languageTranslations("farmers_future_delight", language)
                .get("item.farmers_future_delight.raw_ore.generated.suffix." + category);
        if (suffixes == null || suffixes.trim().isEmpty()) {
            return name;
        }
        String result = name.trim();
        String[] values = suffixes.split("\\|");
        for (String value : values) {
            String suffix = value.trim();
            if (suffix.isEmpty() || !endsWithLocalized(result, suffix, language)) {
                continue;
            }
            result = result.substring(0, result.length() - suffix.length()).trim();
            break;
        }
        return result.isEmpty() ? name.trim() : result;
    }

    private static boolean endsWithLocalized(String value, String suffix, String language) {
        if (language != null && language.toLowerCase(Locale.ROOT).startsWith("en")) {
            return value.toLowerCase(Locale.ROOT).endsWith(suffix.toLowerCase(Locale.ROOT));
        }
        return value.endsWith(suffix);
    }

    private static String generatedName(String language, String key, String name) {
        String template = languageTranslations("farmers_future_delight", language).get(key);
        if (template == null || template.trim().isEmpty()) {
            return name;
        }
        return String.format(Locale.ROOT, template, name);
    }

    private static Map<String, String> localizedNames(ItemStack stack) {
        Map<String, String> result = new LinkedHashMap<>();
        if (stack == null || stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return result;
        }
        ResourceLocation registryName = stack.getItem().getRegistryName();
        for (String language : new String[] {"en_us", "zh_cn"}) {
            Map<String, String> translations = languageTranslations(
                    registryName.getResourceDomain(), language);
            for (String key : localizationKeys(stack)) {
                String value = translations.get(key);
                if (value != null && !value.trim().isEmpty()) {
                    result.put(language, value.trim());
                    break;
                }
            }
        }
        return result;
    }

    private static List<String> localizationKeys(ItemStack stack) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        String unlocalized = stack.getUnlocalizedName();
        if (unlocalized != null && !unlocalized.isEmpty()) {
            keys.add(unlocalized + ".name");
        }
        String itemUnlocalized = stack.getItem().getUnlocalizedName(stack);
        if (itemUnlocalized != null && !itemUnlocalized.isEmpty()) {
            keys.add(itemUnlocalized + ".name");
        }
        ResourceLocation name = stack.getItem().getRegistryName();
        if (name != null) {
            keys.add("item." + name.getResourceDomain() + "."
                    + name.getResourcePath() + ".name");
            keys.add("tile." + name.getResourceDomain() + "."
                    + name.getResourcePath() + ".name");
        }
        return new ArrayList<>(keys);
    }

    private static Map<String, String> languageTranslations(String domain, String language) {
        String cacheKey = domain + "|" + language;
        Map<String, String> cached = LOCALIZATION_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        Map<String, String> translations = new LinkedHashMap<>();
        String prefix = "assets/" + domain.toLowerCase(Locale.ROOT) + "/lang/";
        try {
            for (ModContainer container : Loader.instance().getActiveModList()) {
                File source = container.getSource();
                if (source == null || !source.exists()) {
                    continue;
                }
                if (source.isDirectory()) {
                    File file = new File(source, prefix + language + ".lang");
                    if (file.isFile()) {
                        readLang(fileInput(file), translations);
                    }
                    continue;
                }
                try (ZipFile zip = new ZipFile(source)) {
                    ZipEntry entry = findLanguageEntry(zip, prefix, language);
                    if (entry != null) {
                        try (InputStream input = zip.getInputStream(entry)) {
                            readLang(input, translations);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        Map<String, String> immutable = Collections.unmodifiableMap(translations);
        LOCALIZATION_CACHE.put(cacheKey, immutable);
        return immutable;
    }

    private static ZipEntry findLanguageEntry(ZipFile zip, String prefix, String language) {
        String expected = (prefix + language + ".lang").toLowerCase(Locale.ROOT);
        java.util.Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().toLowerCase(Locale.ROOT).equals(expected)) {
                return entry;
            }
        }
        return null;
    }

    private static InputStream fileInput(File file) throws Exception {
        return new FileInputStream(file);
    }

    private static void readLang(InputStream input, Map<String, String> translations)
            throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input,
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0 && line.charAt(0) == '\uFEFF') {
                    line = line.substring(1);
                }
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                if (!key.isEmpty() && !value.isEmpty()) {
                    translations.put(key, value);
                }
            }
        }
    }

    private static void send(ICommandSender sender, String key, Object... args) {
        ITextComponent message = new TextComponentTranslation(key, args);
        sender.sendMessage(new TextComponentString("[FFD] ").appendSibling(message));
    }

    private enum Status {
        HIGH,
        MEDIUM,
        LOW
    }

    private static final class Summary {
        private int high;
        private int medium;
        private int low;
        private int generated;
        private int existing;
    }

    private static final class Candidate {
        private final String material;
        private final Set<String> oreNames = new LinkedHashSet<>();
        private final Map<String, ItemStack> sources = new LinkedHashMap<>();
        private Resolution resolution;

        private Candidate(String material) {
            this.material = material;
        }

        private void addSource(ItemStack stack) {
            sources.put(stackKey(stack), stack.copy());
        }

        private void resolve(boolean all) {
            if (FFDCustomRawOres.get(material) != null) {
                resolution = Resolution.low("commands.farmers_future_delight.rawore.reason.custom_definition");
                return;
            }
            if (externalRawOreExists(material)) {
                resolution = Resolution.low(
                        "commands.farmers_future_delight.rawore.reason.external_raw_ore", material);
                return;
            }
            List<ItemStack> directDrops = new ArrayList<>();
            List<ItemStack> sourceStacks = new ArrayList<>(sources.values());
            for (ItemStack source : sourceStacks) {
                List<ItemStack> variants = noSilkTouchDrops(source);
                if (variants.size() != 1) {
                    resolution = Resolution.low(variants.isEmpty()
                            ? "commands.farmers_future_delight.rawore.reason.no_stable_drop"
                            : "commands.farmers_future_delight.rawore.reason.multiple_drops");
                    return;
                }
                ItemStack directDrop = variants.get(0);
                if (!sameItem(directDrop, source) && isIntermediateDrop(directDrop)) {
                    resolution = Resolution.low(
                            "commands.farmers_future_delight.rawore.reason.intermediate_drop");
                    return;
                }
                directDrops.add(directDrop);
            }
            List<ItemStack> outputs = new ArrayList<>();
            boolean complete = true;
            for (ItemStack source : sources.values()) {
                ItemStack output = FurnaceRecipes.instance().getSmeltingResult(source);
                if (output == null || output.isEmpty()) {
                    complete = false;
                    continue;
                }
                if (isExcludedStack(output)) {
                    resolution = Resolution.low(
                            "commands.farmers_future_delight.rawore.reason.excluded_chain");
                    return;
                }
                if (!contains(outputs, output)) {
                    outputs.add(output.copy());
                }
            }
            TinkerProduct tinker = !all && allDirectDropsAreSources(directDrops, sourceStacks)
                    ? findTinkerIngot(sourceStacks, material) : null;
            if (outputs.size() > 1) {
                if (tinker != null) {
                    resolution = new Resolution(Status.HIGH, tinker.selector, tinker.stack,
                            Reason.of("commands.farmers_future_delight.rawore.reason.tinker_ingot"));
                } else {
                    resolution = Resolution.low(
                            "commands.farmers_future_delight.rawore.reason.inconsistent_furnace");
                }
                return;
            }
            if (outputs.size() == 1) {
                ItemStack output = outputs.get(0);
                List<ProductMatch> matches = productMatches(output, material);
                if (!matches.isEmpty()) {
                    ProductMatch match = matches.get(0);
                    if (!all && !"ingot".equals(match.category)) {
                        if (tinker != null) {
                            resolution = new Resolution(Status.HIGH, tinker.selector, tinker.stack,
                                    Reason.of("commands.farmers_future_delight.rawore.reason.tinker_ingot"));
                        } else {
                            resolution = Resolution.low(nonIngotReason(match.category));
                        }
                        return;
                    }
                    if (!allDirectDropsMatch(directDrops, sourceStacks, output)) {
                        if (tinker != null) {
                            resolution = new Resolution(Status.HIGH, tinker.selector, tinker.stack,
                                    Reason.of("commands.farmers_future_delight.rawore.reason.tinker_ingot"));
                        } else {
                            resolution = Resolution.low(
                                    "commands.farmers_future_delight.rawore.reason.drop_not_product");
                        }
                        return;
                    }
                    resolution = new Resolution(complete ? Status.HIGH : Status.MEDIUM,
                            itemSelector(output), output,
                            Reason.of(complete
                                    ? "commands.farmers_future_delight.rawore.reason.furnace_complete"
                                    : "commands.farmers_future_delight.rawore.reason.furnace_partial"));
                    return;
                }
                if (tinker != null) {
                    resolution = new Resolution(Status.HIGH, tinker.selector, tinker.stack,
                            Reason.of("commands.farmers_future_delight.rawore.reason.tinker_ingot"));
                } else {
                    resolution = Resolution.low(
                            "commands.farmers_future_delight.rawore.reason.no_product_tag");
                }
                return;
            }
            if (tinker != null) {
                resolution = new Resolution(Status.HIGH, tinker.selector, tinker.stack,
                        Reason.of("commands.farmers_future_delight.rawore.reason.tinker_ingot"));
                return;
            }
            ProductMatch dictionary = findDictionaryProduct(material, all);
            if (dictionary != null) {
                if (!allDirectDropsMatch(directDrops, sourceStacks, dictionary.stack)) {
                    resolution = Resolution.low(
                            "commands.farmers_future_delight.rawore.reason.drop_not_product");
                    return;
                }
                resolution = new Resolution(Status.MEDIUM, dictionary.selector, dictionary.stack,
                        Reason.of("commands.farmers_future_delight.rawore.reason.dictionary_product"));
            } else {
                resolution = Resolution.low(all
                        ? "commands.farmers_future_delight.rawore.reason.no_unique_all_product"
                        : "commands.farmers_future_delight.rawore.reason.no_unique_ingot");
            }
        }

        private static String nonIngotReason(String category) {
            if ("gem".equals(category)) {
                return "commands.farmers_future_delight.rawore.reason.non_ingot_gem";
            }
            if ("dust".equals(category)) {
                return "commands.farmers_future_delight.rawore.reason.non_ingot_dust";
            }
            if ("crystal".equals(category)) {
                return "commands.farmers_future_delight.rawore.reason.non_ingot_crystal";
            }
            if ("nugget".equals(category)) {
                return "commands.farmers_future_delight.rawore.reason.non_ingot_nugget";
            }
            return "commands.farmers_future_delight.rawore.reason.non_ingot_other";
        }

        private static boolean allDirectDropsMatch(List<ItemStack> directDrops,
                                                   List<ItemStack> sourceStacks,
                                                   ItemStack expected) {
            if (directDrops.isEmpty() || directDrops.size() != sourceStacks.size()
                    || expected == null || expected.isEmpty()) {
                return false;
            }
            for (int i = 0; i < directDrops.size(); i++) {
                ItemStack directDrop = directDrops.get(i);
                if (!sameItem(directDrop, sourceStacks.get(i))
                        && !sameItem(directDrop, expected)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean allDirectDropsAreSources(List<ItemStack> directDrops,
                                                        List<ItemStack> sourceStacks) {
            if (directDrops.isEmpty() || directDrops.size() != sourceStacks.size()) {
                return false;
            }
            for (int i = 0; i < directDrops.size(); i++) {
                if (!sameItem(directDrops.get(i), sourceStacks.get(i))) {
                    return false;
                }
            }
            return true;
        }

        private static boolean contains(List<ItemStack> stacks, ItemStack target) {
            for (ItemStack stack : stacks) {
                if (sameItem(stack, target)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class ProductMatch {
        private final String category;
        private final String oreName;
        private ItemStack stack;
        private String selector;

        private ProductMatch(String category, String oreName) {
            this(category, oreName, ItemStack.EMPTY);
        }

        private ProductMatch(String category, String oreName, ItemStack stack) {
            this.category = category;
            this.oreName = oreName;
            this.stack = stack;
        }
    }

    private static TinkerProduct findTinkerIngot(List<ItemStack> sources, String material) {
        try {
            Class<?> registry = Class.forName("slimeknights.tconstruct.library.TinkerRegistry");
            Class<?> smeltery = Class.forName("slimeknights.tconstruct.smeltery.TinkerSmeltery");
            Object castValue = staticField(smeltery, "castIngot");
            if (!(castValue instanceof ItemStack) || ((ItemStack) castValue).isEmpty()) {
                return null;
            }
            ItemStack cast = (ItemStack) castValue;
            Fluid commonFluid = null;
            ItemStack commonOutput = ItemStack.EMPTY;
            for (ItemStack source : sources) {
                Object melting = invokeStatic(registry, "getMelting", source);
                FluidStack fluidStack = asFluidStack(invoke(melting, "getResult"));
                if (fluidStack == null || fluidStack.getFluid() == null) {
                    return null;
                }
                Fluid fluid = fluidStack.getFluid();
                if (commonFluid != null && commonFluid != fluid) {
                    return null;
                }
                commonFluid = fluid;
                Object casting = invokeStatic(registry, "getTableCasting", cast, fluid);
                if (casting == null) {
                    return null;
                }
                ItemStack output = asItemStack(invoke(casting, "getResult", cast, fluid));
                if (output.isEmpty()) {
                    output = asItemStack(invoke(casting, "getResult"));
                }
                if (output.isEmpty() || isExcludedStack(output)) {
                    return null;
                }
                if (commonOutput.isEmpty()) {
                    commonOutput = output.copy();
                } else if (!sameItem(commonOutput, output)) {
                    return null;
                }
            }
            if (commonOutput.isEmpty()) {
                return null;
            }
            boolean ingot = false;
            for (ProductMatch match : productMatches(commonOutput, material)) {
                if ("ingot".equals(match.category)) {
                    ingot = true;
                    break;
                }
            }
            return ingot ? new TinkerProduct(itemSelector(commonOutput), commonOutput) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static FluidStack asFluidStack(Object value) {
        return value instanceof FluidStack ? ((FluidStack) value).copy() : null;
    }

    private static ItemStack asItemStack(Object value) {
        return value instanceof ItemStack && !((ItemStack) value).isEmpty()
                ? ((ItemStack) value).copy() : ItemStack.EMPTY;
    }

    private static Object staticField(Class<?> type, String name) throws ReflectiveOperationException {
        Field field = type.getField(name);
        return field.get(null);
    }

    private static Object invokeStatic(Class<?> type, String name, Object... args) {
        for (Method method : type.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !method.getName().equals(name)
                    || method.getParameterTypes().length != args.length
                    || !matches(method.getParameterTypes(), args)) {
                continue;
            }
            try {
                return method.invoke(null, args);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Object invoke(Object target, String name, Object... args) {
        if (target == null) {
            return null;
        }
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name)
                    || method.getParameterTypes().length != args.length
                    || !matches(method.getParameterTypes(), args)) {
                continue;
            }
            try {
                return method.invoke(target, args);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    private static boolean matches(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] == null) {
                continue;
            }
            if (parameterTypes[i].isPrimitive()) {
                if (!primitiveWrapper(parameterTypes[i]).isInstance(args[i])) {
                    return false;
                }
            } else if (!parameterTypes[i].isInstance(args[i])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> primitiveWrapper(Class<?> primitive) {
        if (primitive == int.class) {
            return Integer.class;
        }
        if (primitive == boolean.class) {
            return Boolean.class;
        }
        if (primitive == float.class) {
            return Float.class;
        }
        if (primitive == double.class) {
            return Double.class;
        }
        if (primitive == long.class) {
            return Long.class;
        }
        if (primitive == short.class) {
            return Short.class;
        }
        if (primitive == byte.class) {
            return Byte.class;
        }
        if (primitive == char.class) {
            return Character.class;
        }
        return primitive;
    }

    private static final class TinkerProduct {
        private final String selector;
        private final ItemStack stack;

        private TinkerProduct(String selector, ItemStack stack) {
            this.selector = selector;
            this.stack = stack;
        }
    }

    private static final class Resolution {
        private final Status status;
        private final String selector;
        private final ItemStack stack;
        private final Reason reason;

        private Resolution(Status status, String selector, ItemStack stack, Reason reason) {
            this.status = status;
            this.selector = selector;
            this.stack = stack;
            this.reason = reason;
        }

        private static Resolution low(String key, Object... args) {
            return new Resolution(Status.LOW, null, ItemStack.EMPTY, Reason.of(key, args));
        }
    }

    private static final class Reason {
        private final String key;
        private final Object[] args;

        private Reason(String key, Object[] args) {
            this.key = key;
            this.args = args;
        }

        private static Reason of(String key, Object... args) {
            return new Reason(key, args == null ? new Object[0] : args.clone());
        }

        private ITextComponent component() {
            return new TextComponentTranslation(key, args);
        }

        private String reportText() {
            String template = languageTranslations("farmers_future_delight", "en_us").get(key);
            if (template == null || template.trim().isEmpty()) {
                return key;
            }
            try {
                return String.format(Locale.ROOT, template, args);
            } catch (RuntimeException error) {
                return key;
            }
        }
    }
}
