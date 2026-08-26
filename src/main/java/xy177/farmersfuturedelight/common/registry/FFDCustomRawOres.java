package xy177.farmersfuturedelight.common.registry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.block.BlockFutureMetal;

public final class FFDCustomRawOres {
    private static final Logger LOGGER = LogManager.getLogger("FFD Custom Raw Ores");
    private static final Pattern MATERIAL_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_]{0,47}");
    private static final Set<String> BUILT_IN_MATERIALS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(FFDRawOres.NAMES)));
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<String, Entry> BY_MATERIAL = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File directory;

    private FFDCustomRawOres() {
    }

    public static void load(File configDirectory) {
        ENTRIES.clear();
        BY_MATERIAL.clear();
        directory = new File(new File(configDirectory, FarmerFutureDelight.MODID),
                "custom_raw_ores");
        if (!directory.exists() && !directory.mkdirs()) {
            LOGGER.error("Could not create custom raw ore directory {}", directory);
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
            LOGGER.info("Loaded {} custom raw ore definitions", ENTRIES.size());
        }
    }

    public static List<Entry> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static File definitionDirectory() {
        return directory;
    }

    public static File generationDirectory() {
        return directory == null ? null : new File(directory.getParentFile(), "raw_ore_generation");
    }

    public static Entry get(String material) {
        return material == null ? null : BY_MATERIAL.get(material.toLowerCase(Locale.ROOT));
    }

    public static Entry findSource(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (Entry entry : ENTRIES) {
            if (entry.isRegistered() && entry.matchesSource(stack)) {
                return entry;
            }
        }
        return null;
    }

    public static Entry find(Item item) {
        for (Entry entry : ENTRIES) {
            if (entry.item == item || entry.blockItem == item) {
                return entry;
            }
        }
        return null;
    }

    public static Entry find(Block block) {
        for (Entry entry : ENTRIES) {
            if (entry.block == block) {
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

    public static String displayName(Entry entry, boolean block, ItemStack stack,
                                     String translated) {
        String key = stack.getUnlocalizedName() + ".name";
        if (translated != null && !translated.equals(key)) {
            return translated;
        }
        String language = FarmerFutureDelight.proxy.getLanguageCode();
        return entry.fallbackName(language, block);
    }

    public static String oreSuffix(String material) {
        StringBuilder result = new StringBuilder();
        boolean upper = true;
        for (int i = 0; i < material.length(); i++) {
            char value = material.charAt(i);
            if (value == '_') {
                upper = true;
            } else {
                result.append(upper ? Character.toUpperCase(value) : value);
                upper = false;
            }
        }
        return result.toString();
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
            if (BUILT_IN_MATERIALS.contains(material)) {
                throw new IllegalArgumentException("material conflicts with built-in raw ore " + material);
            }
            if (BY_MATERIAL.containsKey(material)) {
                throw new IllegalArgumentException("duplicate material " + material);
            }
            List<Selector> ores = selectorList(json, "ores");
            Selector result = Selector.parse(requiredString(json, "smeltResult"));
            Integer color = optionalColor(json);
            Map<String, String> lang = languageMap(json.get("lang"));
            Map<String, String> blockLang = languageMap(json.get("blockLang"));
            Entry entry = new Entry(material, ores, result, color, lang, blockLang);
            ENTRIES.add(entry);
            BY_MATERIAL.put(material, entry);
        } catch (Throwable error) {
            LOGGER.error("Skipping invalid custom raw ore definition {}: {}",
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

    private static List<Selector> selectorList(JsonObject json, String name) {
        JsonElement value = json.get(name);
        if (value == null || !value.isJsonArray()) {
            throw new IllegalArgumentException(name + " must be an array");
        }
        List<Selector> selectors = new ArrayList<>();
        for (JsonElement element : value.getAsJsonArray()) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException(name + " entries must be strings");
            }
            selectors.add(Selector.parse(element.getAsString()));
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
            throw new IllegalArgumentException("language values must be an object");
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
        JsonArray ores = new JsonArray();
        ores.add("oredict:oreExample");
        ores.add("item:examplemod:example_ore@0");
        example.add("ores", ores);
        example.addProperty("smeltResult", "oredict:ingotExample");
        example.addProperty("color", "auto");
        JsonObject lang = new JsonObject();
        lang.addProperty("zh_cn", "粗示例矿");
        lang.addProperty("en_us", "Raw Example");
        example.add("lang", lang);
        JsonObject blockLang = new JsonObject();
        blockLang.addProperty("zh_cn", "粗示例矿块");
        blockLang.addProperty("en_us", "Block of Raw Example");
        example.add("blockLang", blockLang);
        JsonObject guide = new JsonObject();
        guide.add("file", bilingualGuide(
                "文件名以下划线开头时只作为示例，不会加载。复制或重命名为不以下划线开头的 .json 文件后才会生效；每份文件定义一种自定义粗矿，修改后必须重启游戏。",
                "Files whose names begin with an underscore are examples and are never loaded. Copy or rename this to a .json file without a leading underscore to enable it. Each file defines one custom raw ore, and changes require a game restart."));
        guide.add("material", bilingualGuide(
                "填写材质内部名，只能使用小写字母 a-z、数字 0-9 和下划线，且不能与内置材质或其他自定义 JSON 重复。它决定物品与方块注册名 farmers_future_delight:raw_<material>、farmers_future_delight:raw_<material>_block，以及矿物词典名 raw<Material>、blockRaw<Material>。",
                "Enter the internal material name using only lowercase a-z, digits 0-9, and underscores. It must not duplicate a built-in material or another custom JSON. It determines the registry names farmers_future_delight:raw_<material> and farmers_future_delight:raw_<material>_block, plus the ore-dictionary names raw<Material> and blockRaw<Material>."));
        guide.add("ores", bilingualGuide(
                "填写至少一个矿石选择器。匹配到的矿石会被识别为该材质的来源，用于挖掘与兼容机器的粗矿掉落转换。可同时填写矿物词典和精确物品，以覆盖多个模组的同类矿石。",
                "Enter at least one ore selector. Matching ores become sources for this material and are used by mining and compatible-machine raw-ore drop conversion. Ore-dictionary and exact-item selectors may be combined to cover equivalent ores from multiple mods."));
        guide.add("selectorFormat", bilingualGuide(
                "矿物词典格式为 oredict:oreExample。物品格式为 item:modid:registry_name，可追加 @0 等 metadata；省略 metadata 或填写 @* 表示匹配任意 metadata。",
                "Use oredict:oreExample for an ore-dictionary selector. Use item:modid:registry_name for an item selector, optionally followed by metadata such as @0. Omitting metadata or using @* matches any metadata."));
        guide.add("smeltResult", bilingualGuide(
                "填写一个矿物词典或物品选择器，作为粗矿的烧制产物。矿物词典会使用首个有效物品。该产物还用于自动取色，并帮助机器配方推导对应的锭、宝石、粉或晶体材质后缀；无法解析时相关烧制与联动配方不会添加。",
                "Enter one ore-dictionary or item selector as the smelting output. An ore-dictionary selector uses its first valid stack. This output also supplies automatic coloring and helps machine recipes derive the matching ingot, gem, dust, or crystal suffix. Smelting and related compatibility recipes are skipped when it cannot be resolved."));
        guide.add("color", bilingualGuide(
                "可填写 #RRGGBB 作为固定染色值。省略 color，或填写 auto（不区分大小写），都会从烧制产物模型所用贴图的第一帧选取四组主要代表色并取平均值，自动为粗矿与粗矿块分配颜色。若资源包提供了该材质的专用贴图，则直接使用专用贴图且不再染色。",
                "Enter #RRGGBB to use a fixed tint. Omitting color or entering auto in any letter case derives a color automatically by selecting four main representative colors from the first frame of textures used by the smelting output model and averaging them. A material-specific resource-pack texture is used directly and disables tinting."));
        guide.add("lang", bilingualGuide(
                "按语言代码填写粗矿物品的 JSON 回退名称，例如 zh_cn、en_us。当前语言不存在时依次回退到 en_us、首个已填写名称，最后才生成默认英文名。资源包 .lang 中的名称优先于这里。",
                "Map language codes such as zh_cn and en_us to fallback names for the raw-ore item. Missing languages fall back to en_us, then the first supplied name, and finally a generated English name. Resource-pack .lang entries take priority over these values."));
        guide.add("blockLang", bilingualGuide(
                "按语言代码填写粗矿块的 JSON 回退名称。省略时会根据 lang 生成中文“名称+块”或英文“Block of + 名称”。资源包 .lang 中的名称优先于这里。",
                "Map language codes to fallback names for the raw-ore block. When omitted, the name is derived from lang as Chinese 'name + block' or English 'Block of + name'. Resource-pack .lang entries take priority over these values."));
        guide.add("resourcePackTextures", bilingualGuide(
                "资源包可分别提供 assets/farmers_future_delight/textures/item/custom_raw_ores/<material>.png 和 assets/farmers_future_delight/textures/block/custom_raw_ore_blocks/<material>.png。存在某一专用贴图时，对应物品或方块直接使用该贴图且不应用 color；未提供的一侧仍使用公共灰度底图与染色。",
                "A resource pack may provide assets/farmers_future_delight/textures/item/custom_raw_ores/<material>.png and assets/farmers_future_delight/textures/block/custom_raw_ore_blocks/<material>.png independently. A supplied texture is used directly without color tinting; a missing side still uses the shared grayscale texture and tint."));
        guide.add("resourcePackLang", bilingualGuide(
                "在资源包 assets/farmers_future_delight/lang/<语言代码>.lang 中使用 item.farmers_future_delight.raw_<material>.name=物品名称，以及 tile.farmers_future_delight.raw_<material>_block.name=方块名称。它们会覆盖 JSON 的 lang 与 blockLang。",
                "In assets/farmers_future_delight/lang/<language_code>.lang, use item.farmers_future_delight.raw_<material>.name=Item Name and tile.farmers_future_delight.raw_<material>_block.name=Block Name. These entries override JSON lang and blockLang values."));
        guide.add("effects", bilingualGuide(
                "有效定义会注册粗矿物品与粗矿块，并加入熔炉烧制、9:1 压缩与 1:9 解压、矿物词典、矿石掉落转换及可推导的机器联动配方。features.rawOreMode=DISABLED 或 rawOreMaterialToggles 中 <material>=false 会关闭该材质；缺少单独开关时默认启用。",
                "A valid definition registers a raw-ore item and storage block, furnace smelting, 9:1 compression and 1:9 decompression, ore-dictionary entries, ore-drop conversion, and machine compatibility recipes that can be derived. features.rawOreMode=DISABLED or <material>=false in rawOreMaterialToggles disables it; a missing per-material toggle defaults to enabled."));
        example.add("_guide", guide);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file),
                StandardCharsets.UTF_8)) {
            GSON.toJson(example, writer);
        } catch (Throwable error) {
            LOGGER.error("Could not create custom raw ore example {}", file, error);
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
        private final List<Selector> ores;
        private final Selector smeltResult;
        private final Integer configuredColor;
        private final Map<String, String> lang;
        private final Map<String, String> blockLang;
        private final BlockFutureMetal block;
        private final Item item;
        private final ItemBlock blockItem;
        private int derivedColor = 0xFFFFFF;
        private boolean colorResolved;

        private Entry(String material, List<Selector> ores, Selector smeltResult,
                      Integer configuredColor, Map<String, String> lang,
                      Map<String, String> blockLang) {
            this.material = material;
            this.ores = ores;
            this.smeltResult = smeltResult;
            this.configuredColor = configuredColor;
            this.lang = lang;
            this.blockLang = blockLang;
            this.block = new BlockFutureMetal("raw_" + material + "_block", 1);
            this.item = new CustomRawOreItem(this);
            this.blockItem = new CustomRawOreItemBlock(this);
        }

        public String material() {
            return material;
        }

        public Block block() {
            return block;
        }

        public Item item() {
            return item;
        }

        public Item blockItem() {
            return blockItem;
        }

        public String rawOreName() {
            return "raw" + oreSuffix(material);
        }

        public String rawBlockOreName() {
            return "blockRaw" + oreSuffix(material);
        }

        public boolean isEnabled() {
            return FFDConfig.rawOreMode != FFDConfig.FeatureMode.DISABLED
                    && FFDConfig.isRawOreMaterialEnabled(material);
        }

        public boolean isRegistered() {
            return item.getRegistryName() != null
                    && ForgeRegistries.ITEMS.containsKey(item.getRegistryName());
        }

        public boolean isBlockRegistered() {
            return block.getRegistryName() != null
                    && ForgeRegistries.BLOCKS.containsKey(block.getRegistryName());
        }

        public ItemStack rawStack() {
            return isRegistered() ? new ItemStack(item) : ItemStack.EMPTY;
        }

        public ItemStack blockStack() {
            return isBlockRegistered() && ForgeRegistries.ITEMS.containsKey(
                    blockItem.getRegistryName()) ? new ItemStack(blockItem) : ItemStack.EMPTY;
        }

        public boolean matchesSource(ItemStack stack) {
            for (Selector selector : ores) {
                if (selector.matches(stack)) {
                    return true;
                }
            }
            return false;
        }

        public boolean matchesSmeltResult(ItemStack stack) {
            return smeltResult.matches(stack);
        }

        public ItemStack resolveSmeltResult() {
            return smeltResult.resolve();
        }

        public String refinedSuffix() {
            String suffix = suffixFromOreName(smeltResult.oreName());
            if (suffix != null) {
                return suffix;
            }
            ItemStack output = resolveSmeltResult();
            for (int oreId : OreDictionary.getOreIDs(output)) {
                suffix = suffixFromOreName(OreDictionary.getOreName(oreId));
                if (suffix != null) {
                    return suffix;
                }
            }
            return oreSuffix(material);
        }

        public ItemStack resolveRefinedBlock() {
            String oreName = smeltResult.oreName();
            if (oreName != null && oreName.startsWith("ingot") && oreName.length() > 5) {
                return FFDCompat.firstOreDictionaryStack("block" + oreName.substring(5));
            }
            ItemStack output = resolveSmeltResult();
            for (int oreId : OreDictionary.getOreIDs(output)) {
                String name = OreDictionary.getOreName(oreId);
                if (name.startsWith("ingot") && name.length() > 5) {
                    ItemStack block = FFDCompat.firstOreDictionaryStack(
                            "block" + name.substring(5));
                    if (!block.isEmpty()) {
                        return block;
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        public List<ItemStack> sourceStacks() {
            List<ItemStack> result = new ArrayList<>();
            for (Selector selector : ores) {
                result.addAll(selector.resolveAll());
            }
            return result;
        }

        public int tintColor() {
            return configuredColor == null ? derivedColor : configuredColor;
        }

        public boolean hasConfiguredColor() {
            return configuredColor != null;
        }

        public void setDerivedColor(int color) {
            derivedColor = color & 0xFFFFFF;
            colorResolved = true;
        }

        public boolean isColorResolved() {
            return configuredColor != null || colorResolved;
        }

        private String fallbackName(String language, boolean blockName) {
            String normalized = language == null ? "en_us" : language.toLowerCase(Locale.ROOT);
            Map<String, String> names = blockName ? blockLang : lang;
            String value = names.get(normalized);
            if (value == null) {
                value = names.get("en_us");
            }
            if (value == null && !names.isEmpty()) {
                value = names.values().iterator().next();
            }
            if (value != null) {
                return value;
            }
            String base = lang.get(normalized);
            if (base == null) {
                base = lang.get("en_us");
            }
            if (base == null && !lang.isEmpty()) {
                base = lang.values().iterator().next();
            }
            if (base == null) {
                base = "Raw " + oreSuffix(material);
            }
            if (!blockName) {
                return base;
            }
            return normalized.startsWith("zh_") ? base + "块" : "Block of " + base;
        }

        private String suffixFromOreName(String name) {
            if (name == null) {
                return null;
            }
            String[] prefixes = {"ingot", "gem", "dust", "crystal", "nugget"};
            for (String prefix : prefixes) {
                if (name.startsWith(prefix) && name.length() > prefix.length()) {
                    return name.substring(prefix.length());
                }
            }
            return null;
        }
    }

    public static final class Selector {
        private final String oreName;
        private final ResourceLocation itemName;
        private final int metadata;

        private Selector(String oreName, ResourceLocation itemName, int metadata) {
            this.oreName = oreName;
            this.itemName = itemName;
            this.metadata = metadata;
        }

        public static Selector parse(String value) {
            String text = value == null ? "" : value.trim();
            if (text.startsWith("oredict:")) {
                String oreName = text.substring("oredict:".length()).trim();
                if (oreName.isEmpty()) {
                    throw new IllegalArgumentException("empty ore dictionary selector");
                }
                return new Selector(oreName, null, OreDictionary.WILDCARD_VALUE);
            }
            if (text.startsWith("item:")) {
                String item = text.substring("item:".length()).trim();
                int metadata = OreDictionary.WILDCARD_VALUE;
                int separator = item.lastIndexOf('@');
                if (separator >= 0) {
                    String meta = item.substring(separator + 1);
                    item = item.substring(0, separator);
                    if (!"*".equals(meta)) {
                        metadata = Integer.parseInt(meta);
                        if (metadata < 0 || metadata > OreDictionary.WILDCARD_VALUE) {
                            throw new IllegalArgumentException("item metadata is out of range");
                        }
                    }
                }
                return new Selector(null, new ResourceLocation(item), metadata);
            }
            throw new IllegalArgumentException("selector must start with oredict: or item:");
        }

        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (oreName != null) {
                int target = OreDictionary.getOreID(oreName);
                for (int id : OreDictionary.getOreIDs(stack)) {
                    if (id == target) {
                        return true;
                    }
                }
                return false;
            }
            return itemName.equals(stack.getItem().getRegistryName())
                    && (metadata == OreDictionary.WILDCARD_VALUE
                    || metadata == stack.getMetadata());
        }

        public ItemStack resolve() {
            if (oreName != null) {
                return FFDCompat.firstOreDictionaryStack(oreName);
            }
            Item item = ForgeRegistries.ITEMS.getValue(itemName);
            if (item == null || item == Items.AIR) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(item, 1,
                    metadata == OreDictionary.WILDCARD_VALUE ? 0 : metadata);
        }

        public List<ItemStack> resolveAll() {
            if (oreName != null) {
                List<ItemStack> result = new ArrayList<>();
                for (ItemStack stack : OreDictionary.getOres(oreName)) {
                    result.add(stack.copy());
                }
                return result;
            }
            ItemStack stack = resolve();
            return stack.isEmpty() ? Collections.emptyList()
                    : Collections.singletonList(stack);
        }

        public String oreName() {
            return oreName;
        }
    }

    private static final class CustomRawOreItem extends Item {
        private final Entry entry;

        private CustomRawOreItem(Entry entry) {
            this.entry = entry;
            setRegistryName(FarmerFutureDelight.MODID, "raw_" + entry.material);
            setUnlocalizedName(FarmerFutureDelight.MODID + ".raw_" + entry.material);
            setCreativeTab(FFDCreativeTab.INSTANCE);
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack) {
            return displayName(entry, false, stack, super.getItemStackDisplayName(stack));
        }
    }

    private static final class CustomRawOreItemBlock extends ItemBlock {
        private final Entry entry;

        private CustomRawOreItemBlock(Entry entry) {
            super(entry.block);
            this.entry = entry;
            setRegistryName(entry.block.getRegistryName());
            setUnlocalizedName(FarmerFutureDelight.MODID + ".raw_"
                    + entry.material + "_block");
            setCreativeTab(FFDCreativeTab.INSTANCE);
        }

        @Override
        public int getItemBurnTime(ItemStack stack) {
            return 0;
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack) {
            return displayName(entry, true, stack, super.getItemStackDisplayName(stack));
        }
    }
}
