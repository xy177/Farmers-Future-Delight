package xy177.farmersfuturedelight.common.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.block.BlockStrippedLog;

public final class FFDCustomStrippedWoods {
    private static final Logger LOGGER = LogManager.getLogger("FFD Custom Stripped Woods");
    private static final Pattern MATERIAL_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_]{0,47}");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<String, Entry> BY_MATERIAL = new HashMap<>();
    private static File directory;

    private FFDCustomStrippedWoods() {
    }

    public static void load(File configDirectory) {
        ENTRIES.clear();
        BY_MATERIAL.clear();
        directory = new File(new File(configDirectory, FarmerFutureDelight.MODID),
                "custom_stripped_woods");
        if (!directory.exists() && !directory.mkdirs()) {
            LOGGER.error("Could not create custom stripped wood directory {}", directory);
            return;
        }
        createExampleFile();
        File[] files = directory.listFiles(file -> file.isFile()
                && file.getName().toLowerCase(Locale.ROOT).endsWith(".json")
                && !file.getName().startsWith("_"));
        if (files == null) {
            return;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
            loadFile(file);
        }
        if (!ENTRIES.isEmpty()) {
            LOGGER.info("Loaded {} custom stripped wood definitions", ENTRIES.size());
        }
    }

    public static List<Entry> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static void resetDerivedColors() {
        for (Entry entry : ENTRIES) {
            entry.derivedColor = 0xFFFFFF;
            entry.colorResolved = false;
        }
    }

    public static Entry get(String material) {
        return material == null ? null : BY_MATERIAL.get(material.toLowerCase(Locale.ROOT));
    }

    public static File definitionDirectory() {
        return directory;
    }

    public static File generationDirectory() {
        return directory == null ? null : new File(directory.getParentFile(),
                "stripped_wood_generation");
    }

    public static void prepare(IForgeRegistry<Block> registry) {
        for (Entry entry : ENTRIES) {
            entry.prepare(registry);
        }
    }

    public static Entry findSource(IBlockState state) {
        if (state == null) {
            return null;
        }
        for (Entry entry : ENTRIES) {
            if (entry.isEnabled() && entry.matchesSource(state)) {
                return entry;
            }
        }
        return null;
    }

    public static Entry find(Block block) {
        if (block == null) {
            return null;
        }
        for (Entry entry : ENTRIES) {
            if (entry.logBlock() == block || entry.woodBlock() == block) {
                return entry;
            }
        }
        return null;
    }

    public static Entry find(Item item) {
        if (item == null) {
            return null;
        }
        for (Entry entry : ENTRIES) {
            if (entry.logItem() == item || entry.woodItem() == item) {
                return entry;
            }
        }
        return null;
    }

    public static boolean isWoodCandidate(Block block) {
        if (block == null || block.getBlockState().getValidStates().isEmpty()) {
            return false;
        }
        for (IBlockState state : block.getBlockState().getValidStates()) {
            if (invalidReason(block, state) != null) {
                return false;
            }
        }
        return true;
    }

    public static List<Integer> candidateMetadata(Block block) {
        if (!isWoodCandidate(block)) {
            return Collections.emptyList();
        }
        List<IBlockState> representatives = new ArrayList<>();
        for (IBlockState state : block.getBlockState().getValidStates()) {
            int existing = variantIndex(representatives, state);
            if (existing < 0) {
                representatives.add(state);
            } else if (axis(state) == EnumFacing.Axis.Y
                    && axis(representatives.get(existing)) != EnumFacing.Axis.Y) {
                representatives.set(existing, state);
            }
        }
        List<Integer> result = new ArrayList<>();
        for (IBlockState state : representatives) {
            int metadata = block.getMetaFromState(state);
            if (!result.contains(metadata)) {
                result.add(metadata);
            }
        }
        Collections.sort(result);
        return result;
    }

    public static String invalidReason(Block block, IBlockState state) {
        if (block == null || state == null) {
            return "missing block or state";
        }
        ResourceLocation name = block.getRegistryName();
        if (name == null) {
            return "missing registry name";
        }
        if (FarmerFutureDelight.MODID.equals(name.getResourceDomain())) {
            return "belongs to Farmer's Future Delight";
        }
        if (!(block instanceof BlockRotatedPillar)) {
            return "not a rotated pillar block";
        }
        if (block.getMaterial(state) != Material.WOOD) {
            return "material is not wood";
        }
        if (!hasAxis(state)) {
            return "missing log axis property";
        }
        if (name.getResourcePath().toLowerCase(Locale.ROOT).contains("stripped")) {
            return "already appears to be stripped wood";
        }
        Item item = Item.getItemFromBlock(block);
        if (item == null || item == Items.AIR) {
            return "missing block item";
        }
        return null;
    }

    public static EnumFacing.Axis axis(IBlockState state) {
        if (state.getPropertyKeys().contains(BlockRotatedPillar.AXIS)) {
            return state.getValue(BlockRotatedPillar.AXIS);
        }
        if (state.getPropertyKeys().contains(BlockLog.LOG_AXIS)) {
            BlockLog.EnumAxis axis = state.getValue(BlockLog.LOG_AXIS);
            return axis == BlockLog.EnumAxis.X ? EnumFacing.Axis.X
                    : axis == BlockLog.EnumAxis.Z ? EnumFacing.Axis.Z : EnumFacing.Axis.Y;
        }
        return EnumFacing.Axis.Y;
    }

    private static boolean hasAxis(IBlockState state) {
        return state.getPropertyKeys().contains(BlockRotatedPillar.AXIS)
                || state.getPropertyKeys().contains(BlockLog.LOG_AXIS);
    }

    private static boolean isAxisProperty(IProperty<?> property) {
        return property == BlockRotatedPillar.AXIS || property == BlockLog.LOG_AXIS;
    }

    private static int variantIndex(List<IBlockState> states, IBlockState candidate) {
        for (int index = 0; index < states.size(); index++) {
            if (sameWoodVariant(states.get(index), candidate)) {
                return index;
            }
        }
        return -1;
    }

    private static boolean sameWoodVariant(IBlockState first, IBlockState second) {
        if (first.getBlock() != second.getBlock()) {
            return false;
        }
        for (IProperty<?> property : first.getPropertyKeys()) {
            if (isAxisProperty(property)) {
                continue;
            }
            if (!second.getPropertyKeys().contains(property)
                    || !first.getValue(property).equals(second.getValue(property))) {
                return false;
            }
        }
        for (IProperty<?> property : second.getPropertyKeys()) {
            if (isAxisProperty(property)) {
                continue;
            }
            if (!first.getPropertyKeys().contains(property)
                    || !first.getValue(property).equals(second.getValue(property))) {
                return false;
            }
        }
        return true;
    }

    private static void loadFile(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file),
                StandardCharsets.UTF_8)) {
            JsonElement root = new JsonParser().parse(reader);
            if (!root.isJsonObject()) {
                throw new IllegalArgumentException("root must be a JSON object");
            }
            JsonObject json = root.getAsJsonObject();
            String material = requiredString(json, "material").trim().toLowerCase(Locale.ROOT);
            if (!MATERIAL_PATTERN.matcher(material).matches()) {
                throw new IllegalArgumentException("material must match " + MATERIAL_PATTERN.pattern());
            }
            if (BY_MATERIAL.containsKey(material)) {
                throw new IllegalArgumentException("duplicate material " + material);
            }
            List<Selector> log = optionalSelectors(json, "log");
            List<Selector> wood = optionalSelectors(json, "wood");
            if (log.isEmpty() && wood.isEmpty()) {
                throw new IllegalArgumentException("at least one of log or wood is required");
            }
            Integer color = optionalColor(json);
            Map<String, String> lang = languageMap(json.get("lang"));
            Entry entry = new Entry(material, log, wood, color, lang);
            ENTRIES.add(entry);
            BY_MATERIAL.put(material, entry);
        } catch (Throwable error) {
            LOGGER.error("Skipping invalid custom stripped wood definition {}: {}",
                    file.getName(), error.getMessage());
        }
    }

    private static String requiredString(JsonObject json, String name) {
        JsonElement value = json.get(name);
        if (value == null || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isString()
                || value.getAsString().trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must be a non-empty string");
        }
        return value.getAsString();
    }

    private static List<Selector> optionalSelectors(JsonObject json, String name) {
        JsonElement value = json.get(name);
        if (value == null || value.isJsonNull()) {
            return Collections.emptyList();
        }
        List<Selector> selectors = new ArrayList<>();
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            selectors.add(Selector.parse(value.getAsString()));
        } else if (value.isJsonArray()) {
            for (JsonElement element : value.getAsJsonArray()) {
                if (!element.isJsonPrimitive()
                        || !element.getAsJsonPrimitive().isString()) {
                    throw new IllegalArgumentException(name + " entries must be strings");
                }
                selectors.add(Selector.parse(element.getAsString()));
            }
        } else {
            throw new IllegalArgumentException(name + " must be a block selector string or array");
        }
        if (selectors.isEmpty()) {
            throw new IllegalArgumentException(name + " must contain at least one selector");
        }
        return Collections.unmodifiableList(selectors);
    }

    private static Integer optionalColor(JsonObject json) {
        JsonElement value = json.get("color");
        if (value == null || value.isJsonNull()) {
            return null;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("color must be auto or #RRGGBB");
        }
        String text = value.getAsString().trim();
        if ("auto".equalsIgnoreCase(text)) {
            return null;
        }
        if (!text.matches("#[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("color must be auto or #RRGGBB");
        }
        return Integer.parseInt(text.substring(1), 16);
    }

    private static Map<String, String> languageMap(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return Collections.emptyMap();
        }
        if (!value.isJsonObject()) {
            throw new IllegalArgumentException("lang must be an object");
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
            if (!entry.getValue().isJsonPrimitive()
                    || !entry.getValue().getAsJsonPrimitive().isString()
                    || entry.getValue().getAsString().trim().isEmpty()) {
                throw new IllegalArgumentException("language names must be non-empty strings");
            }
            result.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().getAsString());
        }
        return Collections.unmodifiableMap(result);
    }

    private static void createExampleFile() {
        File file = new File(directory, "_example.json");
        if (file.exists()) {
            return;
        }
        JsonObject example = new JsonObject();
        example.addProperty("material", "example");
        JsonArray logs = new JsonArray();
        logs.add("block:examplemod:example_log@0");
        example.add("log", logs);
        JsonArray woods = new JsonArray();
        woods.add("block:examplemod:example_wood@0");
        example.add("wood", woods);
        example.addProperty("color", "auto");
        JsonObject lang = new JsonObject();
        lang.addProperty("zh_cn", "示例木");
        lang.addProperty("en_us", "Example Wood");
        example.add("lang", lang);
        JsonObject guide = new JsonObject();
        guide.addProperty("zh_cn", "文件名以下划线开头时只作为示例，不会加载。复制或重命名为不以下划线开头的 .json 文件后才会生效；每份文件定义一个木材族，修改后必须重启游戏。log 和 wood 可以填写字符串或字符串数组，使用 block:模组ID:注册名@metadata；省略 @metadata 时按 0 处理，使用 @* 可匹配该方块的所有 metadata。精确 metadata 会忽略原木的轴向位，只匹配其余木材变体。来源必须是 Material.WOOD 的原木/木头类旋转柱方块。color 可填写 #RRGGBB，或省略/填写 auto 从来源木头模型自动取色。生成的注册名为 farmers_future_delight:custom_stripped_<material>_log 和 farmers_future_delight:custom_stripped_<material>_wood。");
        guide.addProperty("en_us", "Files whose names begin with an underscore are examples and are never loaded. Copy or rename this to a .json file without a leading underscore to enable it; each file defines one wood family and changes require a game restart. log and wood accept a string or string array using block:modid:registry_name@metadata; omitted metadata defaults to 0, while @* matches every metadata. Exact metadata ignores the source log axis and matches the remaining wood variant. Sources must be Material.WOOD rotated-pillar log/wood blocks. color may be #RRGGBB, or omitted/auto to derive a tint from the source wood model. The generated registry names are farmers_future_delight:custom_stripped_<material>_log and farmers_future_delight:custom_stripped_<material>_wood.");
        example.add("_guide", guide);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file),
                StandardCharsets.UTF_8)) {
            GSON.toJson(example, writer);
        } catch (Exception error) {
            LOGGER.error("Could not create custom stripped wood example {}: {}",
                    file, error.getMessage());
        }
    }

    public static final class Entry {
        private final String material;
        private final List<Selector> logSources;
        private final List<Selector> woodSources;
        private final Integer configuredColor;
        private final Map<String, String> lang;
        private final BlockStrippedLog logBlock;
        private final BlockStrippedLog woodBlock;
        private final ItemBlock logItem;
        private final ItemBlock woodItem;
        private final List<Block> sourceLogs = new ArrayList<>();
        private final List<Block> sourceWoods = new ArrayList<>();
        private boolean prepared;
        private boolean valid;
        private int derivedColor = 0xFFFFFF;
        private boolean colorResolved;

        private Entry(String material, List<Selector> logSources, List<Selector> woodSources,
                      Integer configuredColor, Map<String, String> lang) {
            this.material = material;
            this.logSources = logSources;
            this.woodSources = woodSources;
            this.configuredColor = configuredColor;
            this.lang = lang;
            this.logBlock = logSources.isEmpty() ? null : new BlockStrippedLog(
                    "custom_stripped_" + material + "_log", MapColor.WOOD);
            this.woodBlock = woodSources.isEmpty() ? null : new BlockStrippedLog(
                    "custom_stripped_" + material + "_wood", MapColor.WOOD);
            this.logItem = logBlock == null ? null : new CustomItemBlock(this, logBlock, false);
            this.woodItem = woodBlock == null ? null : new CustomItemBlock(this, woodBlock, true);
        }

        private void prepare(IForgeRegistry<Block> registry) {
            if (prepared) {
                return;
            }
            prepared = true;
            boolean logsValid = resolveSources(registry, logSources, sourceLogs, "log");
            boolean woodsValid = resolveSources(registry, woodSources, sourceWoods, "wood");
            valid = logsValid && woodsValid
                    && (!logSources.isEmpty() || !woodSources.isEmpty());
            if (!valid) {
                LOGGER.error("Skipping custom stripped wood {} because a source block is invalid",
                        material);
            }
        }

        private boolean resolveSources(IForgeRegistry<Block> registry, List<Selector> selectors,
                                       List<Block> resolved, String kind) {
            boolean validSelectors = true;
            for (Selector selector : selectors) {
                Block block = registry.getValue(selector.blockName);
                if (block == null || block == net.minecraft.init.Blocks.AIR) {
                    LOGGER.error("Custom stripped wood {} {} source {} does not exist",
                            material, kind, selector.blockName);
                    validSelectors = false;
                    continue;
                }
                String reason = selector.invalidReason(block);
                if (reason != null) {
                    LOGGER.error("Custom stripped wood {} {} source {} is invalid: {}",
                            material, kind, selector.blockName, reason);
                    validSelectors = false;
                    continue;
                }
                resolved.add(block);
            }
            return validSelectors;
        }

        public String material() {
            return material;
        }

        public Block logBlock() {
            return logBlock;
        }

        public Block woodBlock() {
            return woodBlock;
        }

        public Item logItem() {
            return logItem;
        }

        public Item woodItem() {
            return woodItem;
        }

        public boolean isEnabled() {
            return prepared && valid && FFDCompat.isEnabled(
                    xy177.farmersfuturedelight.common.FFDConfig.strippedWoodMode,
                    FFDCompat.Feature.AQUATIC_DECOR);
        }

        public boolean isLogRegistered() {
            return logBlock != null && ForgeRegistries.BLOCKS.containsKey(logBlock.getRegistryName());
        }

        public boolean isWoodRegistered() {
            return woodBlock != null && ForgeRegistries.BLOCKS.containsKey(woodBlock.getRegistryName());
        }

        public boolean isRegistered() {
            return isLogRegistered() || isWoodRegistered();
        }

        public boolean matchesSource(IBlockState state) {
            return isEnabled() && (matches(state, logSources, sourceLogs)
                    || matches(state, woodSources, sourceWoods));
        }

        private boolean matches(IBlockState state, List<Selector> selectors, List<Block> sources) {
            for (int index = 0; index < selectors.size() && index < sources.size(); index++) {
                if (selectors.get(index).matches(state, sources.get(index))) {
                    return true;
                }
            }
            return false;
        }

        public IBlockState strippedState(IBlockState state) {
            if (!isEnabled()) {
                return null;
            }
            Block target;
            if (matches(state, logSources, sourceLogs)) {
                target = logBlock;
            } else if (matches(state, woodSources, sourceWoods)) {
                target = woodBlock;
            } else {
                return null;
            }
            return target == null ? null : target.getDefaultState().withProperty(
                    BlockRotatedPillar.AXIS, axis(state));
        }

        public ItemStack sourceStack() {
            Block block = sourceBlock(false);
            Selector selector = sourceSelector(false);
            if (block == null || selector == null) {
                block = sourceBlock(true);
                selector = sourceSelector(true);
            }
            if (block == null || selector == null) {
                return ItemStack.EMPTY;
            }
            Item item = Item.getItemFromBlock(block);
            if (item == null || item == Items.AIR) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(item, 1, selector.metadata());
        }

        public Block sourceBlock(boolean wood) {
            List<Block> sources = wood ? sourceWoods : sourceLogs;
            return sources.isEmpty() ? null : sources.get(0);
        }

        public Selector sourceSelector(boolean wood) {
            List<Selector> sources = wood ? woodSources : logSources;
            return sources.isEmpty() ? null : sources.get(0);
        }

        public int tintColor() {
            return configuredColor == null ? derivedColor : configuredColor;
        }

        public boolean isColorResolved() {
            return configuredColor != null || colorResolved;
        }

        public void setDerivedColor(int color) {
            derivedColor = color & 0xFFFFFF;
            colorResolved = true;
        }

        public String displayName(boolean wood, String language, String fallback) {
            String normalized = language == null ? "en_us" : language.toLowerCase(Locale.ROOT);
            String value = lang.get(normalized);
            if (value == null) {
                value = lang.get("en_us");
            }
            if (value == null && !lang.isEmpty()) {
                value = lang.values().iterator().next();
            }
            if (value == null) {
                value = fallback;
            }
            if (normalized.startsWith("zh_")) {
                return "去皮" + value + (wood ? "木" : "原木");
            }
            return "Stripped " + value + (wood ? " Wood" : " Log");
        }
    }

    public static final class Selector {
        private final ResourceLocation blockName;
        private final int metadata;

        private Selector(ResourceLocation blockName, int metadata) {
            this.blockName = blockName;
            this.metadata = metadata;
        }

        public static Selector parse(String value) {
            String text = value == null ? "" : value.trim();
            if (text.startsWith("block:")) {
                text = text.substring("block:".length()).trim();
            }
            int metadata = 0;
            int separator = text.lastIndexOf('@');
            if (separator >= 0) {
                String meta = text.substring(separator + 1);
                text = text.substring(0, separator);
                if ("*".equals(meta)) {
                    metadata = -1;
                } else {
                    metadata = Integer.parseInt(meta);
                    if (metadata < 0 || metadata > 15) {
                        throw new IllegalArgumentException("block metadata is out of range");
                    }
                }
            }
            ResourceLocation name = new ResourceLocation(text);
            return new Selector(name, metadata);
        }

        public int metadata() {
            return metadata < 0 ? 0 : metadata;
        }

        public boolean matches(IBlockState state, Block block) {
            if (block == null || state == null || state.getBlock() != block) {
                return false;
            }
            if (metadata < 0) {
                return true;
            }
            IBlockState selected = block.getStateFromMeta(metadata);
            return sameWoodVariant(selected, state);
        }

        public IBlockState state(Block block) {
            return block.getStateFromMeta(metadata());
        }

        private String invalidReason(Block block) {
            if (metadata < 0) {
                if (block.getBlockState().getValidStates().isEmpty()) {
                    return "block has no valid states";
                }
                for (IBlockState state : block.getBlockState().getValidStates()) {
                    String reason = FFDCustomStrippedWoods.invalidReason(block, state);
                    if (reason != null) {
                        return reason;
                    }
                }
                return null;
            }
            return FFDCustomStrippedWoods.invalidReason(block, state(block));
        }

        public String text() {
            return "block:" + blockName + "@" + (metadata < 0 ? "*" : metadata);
        }
    }

    private static final class CustomItemBlock extends ItemBlock {
        private final Entry entry;
        private final boolean wood;

        private CustomItemBlock(Entry entry, Block block, boolean wood) {
            super(block);
            this.entry = entry;
            this.wood = wood;
            setRegistryName(block.getRegistryName());
            setUnlocalizedName(FarmerFutureDelight.MODID + "."
                    + block.getRegistryName().getResourcePath());
            setCreativeTab(FFDCreativeTab.INSTANCE);
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack) {
            return entry.displayName(wood, FarmerFutureDelight.proxy.getLanguageCode(),
                    stack.getUnlocalizedName().substring(stack.getUnlocalizedName().indexOf('.') + 1));
        }
    }
}
