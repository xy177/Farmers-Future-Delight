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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.block.BlockCompatDeepslateOre;

public final class FFDCustomDeepslateOres {
    private static final Logger LOGGER = LogManager.getLogger("FFD Custom Deepslate Ores");
    private static final Pattern MATERIAL_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_]{0,47}");
    private static final Set<String> NATIVE_MATERIALS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("coal", "iron", "copper", "gold", "redstone",
                    "lapis", "diamond", "emerald")));
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<String, Entry> BY_MATERIAL = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File directory;

    private FFDCustomDeepslateOres() {
    }

    public static void load(File configDirectory) {
        ENTRIES.clear();
        BY_MATERIAL.clear();
        directory = new File(new File(configDirectory, FarmerFutureDelight.MODID),
                "custom_deepslate_ores");
        if (!directory.exists() && !directory.mkdirs()) {
            LOGGER.error("Could not create custom deepslate ore directory {}", directory);
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
            LOGGER.info("Loaded {} custom deepslate ore definitions", ENTRIES.size());
        }
    }

    public static List<Entry> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static Entry get(String material) {
        return material == null ? null : BY_MATERIAL.get(material.toLowerCase(Locale.ROOT));
    }

    public static Entry find(Block block) {
        for (Entry entry : ENTRIES) {
            if (entry.block == block) {
                return entry;
            }
        }
        return null;
    }

    public static Entry find(Item item) {
        for (Entry entry : ENTRIES) {
            if (entry.item == item) {
                return entry;
            }
        }
        return null;
    }

    public static void resetDerivedColors() {
        for (Entry entry : ENTRIES) {
            entry.derivedColor = 0xFFFFFF;
            entry.colorResolved = false;
        }
    }

    public static String displayName(Entry entry, ItemStack stack, String translated) {
        String key = stack.getUnlocalizedName() + ".name";
        if (translated != null && !translated.equals(key)) {
            return translated;
        }
        return entry.fallbackName(FarmerFutureDelight.proxy.getLanguageCode());
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
            if (NATIVE_MATERIALS.contains(material)) {
                throw new IllegalArgumentException("material conflicts with a native deepslate ore " + material);
            }
            if (BY_MATERIAL.containsKey(material)) {
                throw new IllegalArgumentException("duplicate material " + material);
            }
            SourceSelector ore = SourceSelector.parse(requiredString(json, "ore"));
            List<GenerationSpec> generation = generationList(json.get("generation"));
            Map<String, String> lang = languageMap(json.get("lang"));
            Entry entry = new Entry(material, ore, generation, lang);
            ENTRIES.add(entry);
            BY_MATERIAL.put(material, entry);
        } catch (Throwable error) {
            LOGGER.error("Skipping invalid custom deepslate ore definition {}: {}",
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

    private static List<GenerationSpec> generationList(JsonElement value) {
        if (value == null || !value.isJsonArray() || value.getAsJsonArray().size() == 0) {
            throw new IllegalArgumentException("generation must be a non-empty array");
        }
        List<GenerationSpec> result = new ArrayList<>();
        for (JsonElement element : value.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("generation entries must be objects");
            }
            result.add(generation(element.getAsJsonObject()));
        }
        return Collections.unmodifiableList(result);
    }

    private static GenerationSpec generation(JsonObject json) {
        Template template = Template.parse(requiredString(json, "template"));
        int count = optionalInt(json, "count", template.count, 0, 1000);
        int size = optionalInt(json, "size", template.size, 1, 64);
        int minY = optionalInt(json, "minY", template.minY, -320, 319);
        int maxY = optionalInt(json, "maxY", template.maxY, -320, 319);
        if (minY > maxY) {
            throw new IllegalArgumentException("generation minY must not exceed maxY");
        }
        Distribution distribution = Distribution.parse(optionalString(
                json, "distribution", template.distribution.key));
        float discardChance = optionalFloat(json, "discardOnAirExposure",
                template.discardChance, 0.0F, 1.0F);
        int plateau = optionalInt(json, "plateau", template.plateau, 0, 639);
        if (distribution == Distribution.UNIFORM && plateau != 0) {
            throw new IllegalArgumentException("uniform generation requires plateau 0");
        }
        if (distribution == Distribution.TRIANGLE && plateau != 0) {
            throw new IllegalArgumentException("triangle generation requires plateau 0");
        }
        if (plateau > maxY - minY) {
            throw new IllegalArgumentException("generation plateau exceeds the height range");
        }
        return new GenerationSpec(count, size, minY, maxY,
                distribution != Distribution.UNIFORM, discardChance, plateau);
    }

    private static String optionalString(JsonObject json, String name, String fallback) {
        JsonElement value = json.get(name);
        if (value == null || value.isJsonNull()) {
            return fallback;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                || value.getAsString().trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must be a non-empty string");
        }
        return value.getAsString().trim();
    }

    private static int optionalInt(JsonObject json, String name, int fallback,
                                   int minimum, int maximum) {
        JsonElement value = json.get(name);
        if (value == null || value.isJsonNull()) {
            return fallback;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(name + " must be an integer");
        }
        int result = value.getAsInt();
        if (result < minimum || result > maximum) {
            throw new IllegalArgumentException(name + " is out of range");
        }
        return result;
    }

    private static float optionalFloat(JsonObject json, String name, float fallback,
                                       float minimum, float maximum) {
        JsonElement value = json.get(name);
        if (value == null || value.isJsonNull()) {
            return fallback;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(name + " must be a number");
        }
        float result = value.getAsFloat();
        if (Float.isNaN(result) || Float.isInfinite(result)
                || result < minimum || result > maximum) {
            throw new IllegalArgumentException(name + " is out of range");
        }
        return result;
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
        example.addProperty("ore", "block:examplemod:example_ore@0");
        JsonArray generation = new JsonArray();
        JsonObject common = new JsonObject();
        common.addProperty("template", "common");
        common.addProperty("count", 6);
        common.addProperty("size", 8);
        common.addProperty("minY", -64);
        common.addProperty("maxY", -1);
        common.addProperty("distribution", "uniform");
        common.addProperty("discardOnAirExposure", 0.0F);
        common.addProperty("plateau", 0);
        generation.add(common);
        example.add("generation", generation);
        JsonObject lang = new JsonObject();
        lang.addProperty("zh_cn", "深层示例矿石");
        lang.addProperty("en_us", "Deepslate Example Ore");
        example.add("lang", lang);
        JsonObject guide = new JsonObject();
        guide.add("file", bilingualGuide(
                "文件名以下划线开头时只作为示例，不会加载。复制或重命名为不以下划线开头的 .json 文件后才会生效；每份文件定义一种自定义深层矿石，修改后必须重启游戏。",
                "Files whose names begin with an underscore are examples and are never loaded. Copy or rename this to a .json file without a leading underscore to enable it. Each file defines one custom deepslate ore, and changes require a game restart."));
        guide.add("material", bilingualGuide(
                "填写材质内部名，只能使用小写字母、数字和下划线，且不能使用 coal、iron、copper、gold、redstone、lapis、diamond 或 emerald。它决定注册名 farmers_future_delight:deepslate_compat_<material>_ore；相同材质始终合并为一个深层矿石对象。",
                "Enter the internal material name using lowercase letters, digits, and underscores. It cannot be coal, iron, copper, gold, redstone, lapis, diamond, or emerald. It determines the registry name farmers_future_delight:deepslate_compat_<material>_ore, and equal materials always merge into one deepslate ore object."));
        guide.add("ore", bilingualGuide(
                "填写所映射的普通矿石。推荐使用 block:模组ID:方块注册名@metadata；也支持 item:模组ID:物品注册名@metadata 和 oredict:矿物词典名。只有在方块注册阶段能够解析到至少一个普通矿石方块时才会注册对应深层矿石；精确 block 选择器最可靠。",
                "Enter the mapped normal ore. The recommended form is block:modid:block_registry_name@metadata. item:modid:item_registry_name@metadata and oredict:oreDictionaryName are also accepted. The deepslate ore registers only when at least one normal ore block can be resolved during block registration; an exact block selector is the most reliable."));
        guide.add("generation", bilingualGuide(
                "generation 是非空数组，可为同一种深层矿石定义多段矿脉。每段必须选择模板，并可覆盖该模板的全部生成数值。深层矿石只在洞穴与山崖世界类型中替换深板岩基底。",
                "generation is a non-empty array and may define multiple passes for the same deepslate ore. Every pass selects a template and may override all of its generation values. Deepslate ores replace deepslate base blocks only in the Caves & Cliffs world type."));
        guide.add("templates", bilingualGuide(
                "模板默认值：common=6次/大小8/-64至-1/均匀；uncommon=4次/大小6/-64至-1/均匀；rare=2次/大小4/-64至-1/均匀；triangle=6次/大小8/-64至-1/三角；buried=4次/大小8/-64至-8/梯形/空气暴露跳过0.75/平顶16。",
                "Template defaults: common=6 attempts/size 8/-64 to -1/uniform; uncommon=4/6/-64 to -1/uniform; rare=2/4/-64 to -1/uniform; triangle=6/8/-64 to -1/triangle; buried=4/8/-64 to -8/trapezoid/0.75 air-exposure discard/plateau 16."));
        guide.add("generationValues", bilingualGuide(
                "count 控制每区块尝试次数，size 控制矿脉大小，minY 与 maxY 控制抽样高度，distribution 可填 uniform、triangle 或 trapezoid，discardOnAirExposure 为0至1的空气暴露跳过概率，plateau 为梯形分布平顶宽度。uniform 与 triangle 的 plateau 必须为0。",
                "count controls attempts per chunk, size controls vein size, minY and maxY control sampled heights, distribution accepts uniform, triangle, or trapezoid, discardOnAirExposure is an air-exposure discard chance from 0 to 1, and plateau is the flat width of a trapezoid distribution. uniform and triangle require plateau 0."));
        guide.add("behavior", bilingualGuide(
                "深层矿石继承普通矿石的硬度、挖掘工具与等级、亮度、掉落、经验和烧制结果，并加入普通矿石已有的矿物词典及兼容机器配方。存在对应粗矿时仍沿用现有粗矿掉落规则。",
                "The deepslate ore inherits the normal ore's hardness, harvest tool and level, light, drops, experience, and smelting output, and joins the normal ore's ore-dictionary and compatible machine recipes. Existing raw-ore drop rules still apply when a matching raw ore exists."));
        guide.add("resourcePackTexture", bilingualGuide(
                "资源包可提供 assets/farmers_future_delight/textures/block/custom_deepslate_ores/<material>.png。存在专用贴图时直接使用完整贴图；未提供时使用深板岩底图，并从普通矿石贴图自动提取颜色。",
                "A resource pack may provide assets/farmers_future_delight/textures/block/custom_deepslate_ores/<material>.png. A supplied texture is used directly as the full texture. Without one, the shared deepslate base is used and a color is derived automatically from the normal ore texture."));
        guide.add("lang", bilingualGuide(
                "lang 按语言代码填写 JSON 回退名称。资源包可在 assets/farmers_future_delight/lang/<语言代码>.lang 中使用 tile.farmers_future_delight.deepslate_compat_<material>_ore.name=名称覆盖它。",
                "lang maps language codes to JSON fallback names. A resource pack can override them in assets/farmers_future_delight/lang/<language_code>.lang with tile.farmers_future_delight.deepslate_compat_<material>_ore.name=Name."));
        example.add("_guide", guide);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file),
                StandardCharsets.UTF_8)) {
            GSON.toJson(example, writer);
        } catch (Exception error) {
            LOGGER.error("Could not write custom deepslate ore example {}", file, error);
        }
    }

    private static JsonObject bilingualGuide(String chinese, String english) {
        JsonObject guide = new JsonObject();
        guide.addProperty("zh_cn", chinese);
        guide.addProperty("en_us", english);
        return guide;
    }

    public static final class Entry {
        private final String material;
        private final SourceSelector ore;
        private final List<GenerationSpec> generation;
        private final Map<String, String> lang;
        private final List<Source> resolvedSources = new ArrayList<>();
        private BlockCompatDeepslateOre block;
        private Item item;
        private int derivedColor = 0xFFFFFF;
        private boolean colorResolved;

        private Entry(String material, SourceSelector ore, List<GenerationSpec> generation,
                      Map<String, String> lang) {
            this.material = material;
            this.ore = ore;
            this.generation = generation;
            this.lang = lang;
        }

        public String material() {
            return material;
        }

        public List<GenerationSpec> generation() {
            return generation;
        }

        public List<Source> resolveSources(IForgeRegistry<Block> registry) {
            if (resolvedSources.isEmpty()) {
                resolvedSources.addAll(ore.resolve(registry));
            }
            return Collections.unmodifiableList(resolvedSources);
        }

        public ItemStack colorSource() {
            return resolvedSources.isEmpty() ? ItemStack.EMPTY
                    : resolvedSources.get(0).stack();
        }

        public void bindBlock(BlockCompatDeepslateOre value) {
            block = value;
        }

        public Item createItem(BlockCompatDeepslateOre value) {
            block = value;
            item = new CustomDeepslateOreItem(value, this);
            return item;
        }

        public int tintColor() {
            return derivedColor;
        }

        public void setDerivedColor(int color) {
            derivedColor = color & 0xFFFFFF;
            colorResolved = true;
        }

        public boolean isColorResolved() {
            return colorResolved;
        }

        private String fallbackName(String language) {
            String normalized = language == null ? "en_us" : language.toLowerCase(Locale.ROOT);
            String value = lang.get(normalized);
            if (value == null) {
                value = lang.get("en_us");
            }
            if (value == null && !lang.isEmpty()) {
                value = lang.values().iterator().next();
            }
            return value == null ? "Deepslate " + displayMaterial(material) + " Ore" : value;
        }
    }

    public static final class GenerationSpec {
        private final int count;
        private final int size;
        private final int minY;
        private final int maxY;
        private final boolean trapezoid;
        private final float discardChance;
        private final int plateau;

        private GenerationSpec(int count, int size, int minY, int maxY,
                               boolean trapezoid, float discardChance, int plateau) {
            this.count = count;
            this.size = size;
            this.minY = minY;
            this.maxY = maxY;
            this.trapezoid = trapezoid;
            this.discardChance = discardChance;
            this.plateau = plateau;
        }

        public int count() {
            return count;
        }

        public int size() {
            return size;
        }

        public int minY() {
            return minY;
        }

        public int maxY() {
            return maxY;
        }

        public boolean trapezoid() {
            return trapezoid;
        }

        public float discardChance() {
            return discardChance;
        }

        public int plateau() {
            return plateau;
        }
    }

    public static final class Source {
        private final IBlockState state;
        private final ItemStack stack;
        private final int harvestLevel;

        private Source(IBlockState state, ItemStack stack) {
            this.state = state;
            this.stack = stack.copy();
            int value = state.getBlock().getHarvestLevel(state);
            harvestLevel = value < 0 ? 1 : value;
        }

        public IBlockState state() {
            return state;
        }

        public ItemStack stack() {
            return stack.copy();
        }

        public int harvestLevel() {
            return harvestLevel;
        }
    }

    private static final class SourceSelector {
        private final String oreName;
        private final ResourceLocation blockName;
        private final ResourceLocation itemName;
        private final int metadata;

        private SourceSelector(String oreName, ResourceLocation blockName,
                               ResourceLocation itemName, int metadata) {
            this.oreName = oreName;
            this.blockName = blockName;
            this.itemName = itemName;
            this.metadata = metadata;
        }

        private static SourceSelector parse(String value) {
            String text = value == null ? "" : value.trim();
            if (text.startsWith("oredict:")) {
                String name = text.substring("oredict:".length()).trim();
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("empty ore dictionary selector");
                }
                return new SourceSelector(name, null, null, OreDictionary.WILDCARD_VALUE);
            }
            if (text.startsWith("block:")) {
                ParsedName parsed = ParsedName.parse(text.substring("block:".length()));
                return new SourceSelector(null, parsed.name, null, parsed.metadata);
            }
            if (text.startsWith("item:")) {
                ParsedName parsed = ParsedName.parse(text.substring("item:".length()));
                return new SourceSelector(null, null, parsed.name, parsed.metadata);
            }
            throw new IllegalArgumentException(
                    "ore must start with block:, item:, or oredict:");
        }

        private List<Source> resolve(IForgeRegistry<Block> registry) {
            List<Source> result = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            if (blockName != null) {
                addSelected(result, seen, registry.getValue(blockName), metadata);
            } else if (itemName != null) {
                Item item = ForgeRegistries.ITEMS.getValue(itemName);
                if (item != null && item != Items.AIR) {
                    addSelected(result, seen, Block.getBlockFromItem(item), metadata);
                }
            } else {
                for (ItemStack stack : OreDictionary.getOres(oreName, false)) {
                    if (!stack.isEmpty()) {
                        add(result, seen, Block.getBlockFromItem(stack.getItem()),
                                stack.getMetadata());
                    }
                }
            }
            return result;
        }

        private static void addSelected(List<Source> result, Set<String> seen,
                                        Block block, int metadata) {
            if (block == null || block == Blocks.AIR) {
                return;
            }
            if (metadata != OreDictionary.WILDCARD_VALUE) {
                add(result, seen, block, metadata);
                return;
            }
            for (IBlockState state : block.getBlockState().getValidStates()) {
                try {
                    add(result, seen, block, block.getMetaFromState(state));
                } catch (RuntimeException ignored) {
                }
            }
        }

        private static void add(List<Source> result, Set<String> seen,
                                Block block, int metadata) {
            if (block == null || block == Blocks.AIR || block.getRegistryName() == null) {
                return;
            }
            int meta = metadata;
            IBlockState state;
            try {
                state = block.getStateFromMeta(meta);
            } catch (RuntimeException ignored) {
                return;
            }
            if (state == null) {
                return;
            }
            String key = block.getRegistryName() + "@" + meta;
            if (seen.add(key)) {
                result.add(new Source(state, new ItemStack(block, 1, meta)));
            }
        }
    }

    private static final class ParsedName {
        private final ResourceLocation name;
        private final int metadata;

        private ParsedName(ResourceLocation name, int metadata) {
            this.name = name;
            this.metadata = metadata;
        }

        private static ParsedName parse(String value) {
            String text = value == null ? "" : value.trim();
            int metadata = 0;
            int separator = text.lastIndexOf('@');
            if (separator >= 0) {
                String meta = text.substring(separator + 1).trim();
                text = text.substring(0, separator).trim();
                if ("*".equals(meta)) {
                    metadata = OreDictionary.WILDCARD_VALUE;
                } else {
                    metadata = Integer.parseInt(meta);
                    if (metadata < 0 || metadata > OreDictionary.WILDCARD_VALUE) {
                        throw new IllegalArgumentException("metadata is out of range");
                    }
                }
            }
            return new ParsedName(new ResourceLocation(text), metadata);
        }
    }

    private enum Distribution {
        UNIFORM("uniform"),
        TRIANGLE("triangle"),
        TRAPEZOID("trapezoid");

        private final String key;

        Distribution(String key) {
            this.key = key;
        }

        private static Distribution parse(String value) {
            for (Distribution distribution : values()) {
                if (distribution.key.equalsIgnoreCase(value)) {
                    return distribution;
                }
            }
            throw new IllegalArgumentException(
                    "distribution must be uniform, triangle, or trapezoid");
        }
    }

    private enum Template {
        COMMON("common", 6, 8, -64, -1, Distribution.UNIFORM, 0.0F, 0),
        UNCOMMON("uncommon", 4, 6, -64, -1, Distribution.UNIFORM, 0.0F, 0),
        RARE("rare", 2, 4, -64, -1, Distribution.UNIFORM, 0.0F, 0),
        TRIANGLE("triangle", 6, 8, -64, -1, Distribution.TRIANGLE, 0.0F, 0),
        BURIED("buried", 4, 8, -64, -8, Distribution.TRAPEZOID, 0.75F, 16);

        private final String key;
        private final int count;
        private final int size;
        private final int minY;
        private final int maxY;
        private final Distribution distribution;
        private final float discardChance;
        private final int plateau;

        Template(String key, int count, int size, int minY, int maxY,
                 Distribution distribution, float discardChance, int plateau) {
            this.key = key;
            this.count = count;
            this.size = size;
            this.minY = minY;
            this.maxY = maxY;
            this.distribution = distribution;
            this.discardChance = discardChance;
            this.plateau = plateau;
        }

        private static Template parse(String value) {
            for (Template template : values()) {
                if (template.key.equalsIgnoreCase(value)) {
                    return template;
                }
            }
            throw new IllegalArgumentException(
                    "template must be common, uncommon, rare, triangle, or buried");
        }
    }

    private static String displayMaterial(String material) {
        StringBuilder result = new StringBuilder();
        boolean upper = true;
        for (int index = 0; index < material.length(); index++) {
            char value = material.charAt(index);
            if (value == '_') {
                result.append(' ');
                upper = true;
            } else {
                result.append(upper ? Character.toUpperCase(value) : value);
                upper = false;
            }
        }
        return result.toString();
    }

    private static final class CustomDeepslateOreItem extends ItemBlock {
        private final Entry entry;

        private CustomDeepslateOreItem(BlockCompatDeepslateOre block, Entry entry) {
            super(block);
            this.entry = entry;
            setRegistryName(block.getRegistryName());
            setUnlocalizedName(block.getUnlocalizedName());
            setCreativeTab(FFDCreativeTab.INSTANCE);
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack) {
            return displayName(entry, stack, super.getItemStackDisplayName(stack));
        }
    }
}
